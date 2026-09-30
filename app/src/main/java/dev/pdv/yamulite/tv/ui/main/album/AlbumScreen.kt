package dev.pdv.yamulite.tv.ui.main.album

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pdv.yamulite.tv.ui.main.components.CoverImage
import dev.pdv.yamulite.tv.ui.main.components.TrackRow
import dev.pdv.yamulite.tv.ui.main.components.rememberDownloadInfo
import dev.pdv.yamulite.tv.ui.main.components.rememberIsLiked
import dev.pdv.yamulite.tv.ui.tv.RetryButton
import dev.pdv.yamulite.tv.ui.tv.TvSurface

@Composable
fun AlbumScreen(
    onBack: () -> Unit,
    vm: AlbumViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val likedIds = vm.likedIds.collectAsStateWithLifecycle()
    val downloadStates = vm.downloadStates.collectAsStateWithLifecycle()
    val backFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { backFocusRequester.requestFocus() }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        TvSurface(onClick = onBack, focusRequester = backFocusRequester, modifier = Modifier.size(48.dp)) {
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
        }
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.loading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Ошибка: ${state.error}", color = MaterialTheme.colorScheme.error)
                    RetryButton(onClick = vm::refresh)
                }
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item { Header(state) }
                    if (state.tracks.isEmpty()) {
                        item {
                            Text(
                                "В альбоме нет треков",
                                modifier = Modifier.padding(24.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        itemsIndexed(state.tracks, key = { _, t -> t.id }) { idx, track ->
                            TrackRow(
                                track = track,
                                isLiked = rememberIsLiked(track.id, likedIds),
                                download = rememberDownloadInfo(track.id, downloadStates),
                                onClick = { vm.play(state.tracks, idx) },
                                onLikeToggle = { vm.toggleLike(track.id) },
                                onDownloadClick = { vm.onDownloadClick(track.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(state: AlbumUiState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverImage(coverUri = state.coverUri, side = 140.dp, pixelSize = 320)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(state.title, style = MaterialTheme.typography.headlineSmall)
            if (state.artistsLine.isNotBlank()) {
                Text(state.artistsLine, style = MaterialTheme.typography.bodyLarge)
            }
            state.year?.let {
                Text(
                    it.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
