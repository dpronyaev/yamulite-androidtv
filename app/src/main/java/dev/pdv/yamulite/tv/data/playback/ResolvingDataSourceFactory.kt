package dev.pdv.yamulite.tv.data.playback

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.TransferListener
import dev.pdv.yamulite.tv.data.music.StreamUrlResolver
import dev.pdv.yamulite.tv.data.settings.CodecPreference
import dev.pdv.yamulite.tv.data.settings.Quality
import dev.pdv.yamulite.tv.data.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResolvingDataSourceFactory @Inject constructor(
    private val resolver: StreamUrlResolver,
    settings: SettingsStore,
    private val downloads: DownloadManager,
) : DataSource.Factory {

    // ExoPlayer calls open() on a background loader thread — runBlocking is safe here
    private val httpFactory = DefaultHttpDataSource.Factory()

    // Kept current reactively so open() never needs runBlocking for settings reads
    @Volatile private var currentQuality: Quality = Quality.High
    @Volatile private var currentCodec: CodecPreference = CodecPreference.AacPreferred

    // LRU cache of resolved stream URLs — repeated plays skip the two-step HTTP resolve
    // LinkedHashMap in access-order mode; removeEldestEntry caps size at 20 entries
    private val urlCache = object : LinkedHashMap<String, String>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<String, String>) = size > 20
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch { settings.quality.collect { currentQuality = it } }
        scope.launch { settings.codec.collect { currentCodec = it } }
    }

    override fun createDataSource(): DataSource = ResolvingDataSource()

    private inner class ResolvingDataSource : DataSource {
        private var delegate: DataSource? = null

        override fun addTransferListener(transferListener: TransferListener) {
            delegate?.addTransferListener(transferListener)
        }

        override fun open(dataSpec: DataSpec): Long {
            if (dataSpec.uri.scheme != "yamulite") {
                val ds = httpFactory.createDataSource()
                delegate = ds
                return ds.open(dataSpec)
            }

            val trackId = dataSpec.uri.lastPathSegment
                ?: throw IOException("Missing track id in ${dataSpec.uri}")

            val localPath = downloads.localPath(trackId)
            val realUri = if (localPath != null) {
                Uri.fromFile(File(localPath))
            } else {
                val q = currentQuality
                val c = currentCodec
                val cacheKey = "$trackId:${q.name}:${c.name}"
                val cachedUrl = synchronized(urlCache) { urlCache[cacheKey] }
                val url = cachedUrl ?: run {
                    val resolved = runBlocking { resolver.resolve(trackId, q.bitrate, c) }
                        ?: if (c != CodecPreference.Mp3Only)
                            runBlocking { resolver.resolve(trackId, q.bitrate, CodecPreference.Mp3Only) }
                        else null
                    resolved?.url?.also { u ->
                        synchronized(urlCache) { urlCache[cacheKey] = u }
                    } ?: throw IOException("Could not resolve stream URL for track $trackId")
                }
                Uri.parse(url)
            }

            val realSpec = dataSpec.buildUpon().setUri(realUri).build()
            val ds: DataSource = if (localPath != null) FileDataSource() else httpFactory.createDataSource()
            delegate = ds
            return ds.open(realSpec)
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int =
            delegate?.read(buffer, offset, length) ?: C.RESULT_END_OF_INPUT

        override fun getUri(): Uri? = delegate?.uri

        override fun close() {
            delegate?.close()
            delegate = null
        }
    }
}
