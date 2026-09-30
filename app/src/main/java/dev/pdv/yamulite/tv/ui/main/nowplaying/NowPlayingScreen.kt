package dev.pdv.yamulite.tv.ui.main.nowplaying

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pdv.yamulite.tv.data.playback.PlaybackUi
import dev.pdv.yamulite.tv.ui.main.components.CoverImage
import dev.pdv.yamulite.tv.ui.main.components.FavoriteToggleButton
import dev.pdv.yamulite.tv.ui.main.components.displayLine
import dev.pdv.yamulite.tv.ui.tv.TvSurface

@Composable
fun NowPlayingScreen(
    onArtistClick: (Long) -> Unit = {},
    onAlbumClick: (Long) -> Unit = {},
    vm: NowPlayingViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val likedIds by vm.likedIds.collectAsStateWithLifecycle()
    val playFocusRequester = remember { FocusRequester() }

    Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        val track = state.track
        if (track == null) {
            Text(
                "Сейчас ничего не играет",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center),
            )
            return@Box
        }

        LaunchedEffect(Unit) { playFocusRequester.requestFocus() }

        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize(),
        ) {
            // The nav rail (260dp) already claims part of the screen before this composable
            // ever sees it, so the cover has to stay modest or the transport row on the right
            // has no room left and its last button (like) gets laid out past the screen edge.
            CoverImage(
                coverUri = track.coverUri ?: track.albums.firstOrNull()?.coverUri,
                side = 260.dp,
                pixelSize = 600,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = track.displayLine(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                )
                state.error?.let {
                    Text("Ошибка: $it", color = MaterialTheme.colorScheme.error)
                }
                SeekControl(state = state, onSeek = vm::seekTo)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TransportButton(
                        icon = Icons.Filled.SkipPrevious,
                        contentDescription = "Предыдущий",
                        enabled = state.hasPrevious,
                        onClick = vm::previous,
                        size = 48.dp,
                    )
                    TvSurface(
                        onClick = vm::togglePlayPause,
                        focusRequester = playFocusRequester,
                        shape = CircleShape,
                        modifier = Modifier.size(68.dp),
                    ) {
                        Box(Modifier.size(68.dp), contentAlignment = Alignment.Center) {
                            when {
                                state.isLoading -> CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                state.isPlaying -> Icon(Icons.Filled.Pause, contentDescription = "Пауза", modifier = Modifier.size(34.dp))
                                else -> Icon(Icons.Filled.PlayArrow, contentDescription = "Играть", modifier = Modifier.size(34.dp))
                            }
                        }
                    }
                    TransportButton(
                        icon = Icons.Filled.SkipNext,
                        contentDescription = "Следующий",
                        enabled = state.hasNext,
                        onClick = vm::next,
                        size = 48.dp,
                    )
                    FavoriteToggleButton(
                        isLiked = track.id in likedIds,
                        onToggle = vm::toggleLike,
                        iconSize = 28.dp,
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TransportButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
) {
    TvSurface(onClick = onClick, enabled = enabled, shape = CircleShape, modifier = Modifier.size(size)) {
        Box(Modifier.size(size), contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(size * 0.55f),
                tint = if (enabled) androidx.compose.material3.LocalContentColor.current
                else androidx.compose.material3.LocalContentColor.current.copy(alpha = 0.35f),
            )
        }
    }
}

/**
 * A focusable progress row that seeks ±10s on D-pad left/right while focused — the TV
 * equivalent of the phone app's draggable [androidx.compose.material3.Slider], which needs a
 * touchscreen to drag and has no remote-friendly replacement in Compose.
 */
@Composable
private fun SeekControl(state: PlaybackUi, onSeek: (Long) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val duration = state.durationMs
    val progress = if (duration > 0) (state.positionMs.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val borderColor by animateColorAsState(
        if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "seekBorder",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(enabled = duration > 0)
            .onKeyEvent { event ->
                if (duration <= 0 || event.type != androidx.compose.ui.input.key.KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionLeft -> { onSeek((state.positionMs - SEEK_STEP_MS).coerceAtLeast(0L)); true }
                    Key.DirectionRight -> { onSeek((state.positionMs + SEEK_STEP_MS).coerceAtMost(duration)); true }
                    else -> false
                }
            }
            .padding(vertical = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(if (isFocused) 2.dp else 0.dp, borderColor, RoundedCornerShape(4.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(formatTime(state.positionMs), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (isFocused) {
                Text(
                    "◀ −10с   +10с ▶",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(formatTime(duration), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private const val SEEK_STEP_MS = 10_000L

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
