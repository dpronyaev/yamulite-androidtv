package dev.pdv.yamulite.tv.ui.main.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pdv.yamulite.tv.ui.main.components.TrackRow
import dev.pdv.yamulite.tv.ui.main.components.rememberDownloadInfo
import dev.pdv.yamulite.tv.ui.main.components.rememberIsLiked
import dev.pdv.yamulite.tv.ui.tv.RetryButton

@Composable
fun FavoritesScreen(vm: FavoritesViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val likedIds = vm.likedIds.collectAsStateWithLifecycle()
    val downloadStates = vm.downloadStates.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val firstItemFocusRequester = remember { FocusRequester() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.syncLikes() }

    LaunchedEffect(state.tracks.firstOrNull()?.id) {
        if (state.tracks.isNotEmpty()) firstItemFocusRequester.requestFocus()
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastIndex = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = lazyListState.layoutInfo.totalItemsCount
            lastIndex >= total - 3 && total > 0
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && state.hasMore && !state.loadingMore) vm.loadMore()
    }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        when {
            state.loading && state.tracks.isEmpty() ->
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            state.error != null -> Column(
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Ошибка: ${state.error}", color = MaterialTheme.colorScheme.error)
                RetryButton(onClick = vm::refresh)
            }
            state.tracks.isEmpty() -> Text(
                "В избранном пусто",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {
                itemsIndexed(state.tracks, key = { _, t -> t.id }) { idx, track ->
                    TrackRow(
                        track = track,
                        isLiked = rememberIsLiked(track.id, likedIds),
                        download = rememberDownloadInfo(track.id, downloadStates),
                        onClick = { vm.play(state.tracks, idx) },
                        onLikeToggle = { vm.toggleLike(track.id) },
                        onDownloadClick = { vm.onDownloadClick(track.id) },
                        focusRequester = if (idx == 0) firstItemFocusRequester else null,
                    )
                }
                if (state.loadingMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator() }
                    }
                }
            }
        }
    }
}
