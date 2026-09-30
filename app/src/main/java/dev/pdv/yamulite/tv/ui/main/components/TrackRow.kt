package dev.pdv.yamulite.tv.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.pdv.yamulite.tv.data.music.dto.TrackDto
import dev.pdv.yamulite.tv.data.playback.DownloadInfo
import dev.pdv.yamulite.tv.data.playback.DownloadState
import dev.pdv.yamulite.tv.ui.tv.TvSurface

fun TrackDto.displayLine(): String {
    val artist = artists.joinToString(", ") { it.name }
    val t = title.orEmpty()
    return when {
        artist.isBlank() && t.isBlank() -> "(без названия)"
        artist.isBlank() -> t
        t.isBlank() -> artist
        else -> "$artist — $t"
    }
}

/**
 * Three independent D-pad focus stops — cover (play), heart (like), cloud (download) — rather
 * than one focusable row containing more focusables. Compose's 2D focus search treats a
 * focusable-inside-a-focusable as unreachable by D-pad left/right (the child's bounds sit
 * *inside* the parent's, not beside it), so nesting them here would make like/download
 * unreachable with a remote even though they'd still work fine with a mouse or touch.
 */
@Composable
fun TrackRow(
    track: TrackDto,
    isLiked: Boolean,
    download: DownloadInfo?,
    onClick: () -> Unit,
    onLikeToggle: () -> Unit,
    onDownloadClick: () -> Unit,
    focusRequester: FocusRequester? = null,
) {
    val cover = track.coverUri ?: track.albums.firstOrNull()?.coverUri
    val displayText = remember(track.id, track.title, track.artists) { track.displayLine() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().height(76.dp).padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        TvSurface(onClick = onClick, focusRequester = focusRequester, modifier = Modifier.size(56.dp)) {
            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                CoverImage(coverUri = cover, side = 56.dp)
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Играть",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Text(
            text = displayText,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        FavoriteToggleButton(isLiked = isLiked, onToggle = onLikeToggle)
        DownloadIndicator(download = download, onClick = onDownloadClick)
    }
}

@Composable
private fun DownloadIndicator(download: DownloadInfo?, onClick: () -> Unit) {
    when (download?.state) {
        null -> TvSurface(onClick = onClick, modifier = Modifier.size(40.dp)) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.CloudDownload, contentDescription = "Скачать")
            }
        }
        DownloadState.Failed -> TvSurface(onClick = onClick, modifier = Modifier.size(40.dp)) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.ErrorOutline,
                    contentDescription = "Повторить скачивание",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        DownloadState.Downloading -> Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (download.progress > 0f) {
                CircularProgressIndicator(
                    progress = { download.progress },
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp,
                )
            } else {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            }
        }
        DownloadState.Done -> TvSurface(onClick = onClick, modifier = Modifier.size(40.dp)) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.CloudDone,
                    contentDescription = "Удалить скачанное",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
