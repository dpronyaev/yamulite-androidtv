package dev.pdv.yamulite.tv.data.music

import dev.pdv.yamulite.tv.data.settings.CodecPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

data class ResolvedStream(val url: String, val codec: String)

@Singleton
class StreamUrlResolver @Inject constructor(
    private val api: MusicApi,
    private val okHttp: OkHttpClient,
) {
    suspend fun resolve(
        trackId: String,
        preferredBitrate: Int,
        codecPref: CodecPreference = CodecPreference.Unrestricted,
    ): ResolvedStream? = withContext(Dispatchers.IO) {
        val options = api.downloadInfo(trackId).result

        val allowed = when (codecPref) {
            CodecPreference.Mp3Only ->
                options.filter { it.codec.equals("mp3", ignoreCase = true) }
            CodecPreference.AacPreferred ->
                options.filter { it.codec.lowercase() in setOf("mp3", "aac", "aac-v3") }
            CodecPreference.Unrestricted ->
                options
        }

        // flac is lossless — no bitrate cap; all others are capped by preferredBitrate
        val withinBudget = allowed.filter { opt ->
            opt.codec.equals("flac", ignoreCase = true) || opt.bitrateInKbps <= preferredBitrate
        }

        val pool = withinBudget.ifEmpty { allowed }
        val pick = pool
            .sortedWith(
                compareByDescending<dev.pdv.yamulite.tv.data.music.dto.DownloadInfoDto> {
                    CODEC_RANK[it.codec.lowercase()] ?: 0
                }.thenByDescending { it.bitrateInKbps }
            )
            .firstOrNull() ?: return@withContext null

        val xml = okHttp.newCall(Request.Builder().url(pick.downloadInfoUrl).build())
            .execute()
            .use { it.body?.string().orEmpty() }
        val info = parseInfoXml(xml) ?: return@withContext null
        ResolvedStream(url = signedUrl(pick.codec, info), codec = pick.codec.lowercase())
    }

    private data class InfoFields(val host: String, val path: String, val ts: String, val s: String)

    private fun parseInfoXml(xml: String): InfoFields? {
        fun field(tag: String) = Regex("<$tag>(.+?)</$tag>").find(xml)?.groupValues?.get(1)
        val host = field("host") ?: return null
        val path = field("path") ?: return null
        val ts = field("ts") ?: return null
        val s = field("s") ?: return null
        return InfoFields(host, path, ts, s)
    }

    private fun signedUrl(codec: String, info: InfoFields): String {
        val prefix = when (codec.lowercase()) {
            "aac", "aac-v3" -> "get-aac"
            "flac" -> "get-flac"
            else -> "get-mp3"
        }
        val pathNoSlash = info.path.removePrefix("/")
        val signed = md5Hex(SALT + pathNoSlash + info.s)
        return "https://${info.host}/$prefix/$signed/${info.ts}${info.path}"
    }

    private fun md5Hex(s: String): String =
        MessageDigest.getInstance("MD5").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

    companion object {
        private const val SALT = "XGRlBW9FXlekgbPrRHuSiA"

        // Higher rank = preferred when bitrate budget allows
        private val CODEC_RANK = mapOf("flac" to 4, "aac-v3" to 3, "aac" to 2, "mp3" to 1, "opus" to 0)
    }
}
