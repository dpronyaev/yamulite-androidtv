package dev.pdv.yamulite.tv.ui.main.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pdv.yamulite.tv.data.music.SearchType
import dev.pdv.yamulite.tv.data.music.dto.TrackDto
import dev.pdv.yamulite.tv.data.playback.DownloadInfo
import dev.pdv.yamulite.tv.ui.main.components.AlbumRow
import dev.pdv.yamulite.tv.ui.main.components.ArtistRow
import dev.pdv.yamulite.tv.ui.main.components.TrackRow
import dev.pdv.yamulite.tv.ui.main.components.rememberDownloadInfo
import dev.pdv.yamulite.tv.ui.main.components.rememberIsLiked
import dev.pdv.yamulite.tv.ui.tv.TvSurface

@Composable
fun SearchScreen(
    onArtistClick: (Long) -> Unit = {},
    onAlbumClick: (Long) -> Unit = {},
    vm: SearchViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val likedIds = vm.likedIds.collectAsStateWithLifecycle()
    val downloadStates = vm.downloadStates.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val fieldFocusRequester = remember { FocusRequester() }
    val tabFocusRequesters = remember {
        SearchType.entries.associateWith { FocusRequester() }
    }

    LaunchedEffect(Unit) { fieldFocusRequester.requestFocus() }

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

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::onQueryChange,
            placeholder = { Text("Поиск треков, исполнителей, альбомов") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            // A text field normally swallows DPAD_DOWN for cursor movement, which on a
            // remote-only TV means the D-pad can never leave the search box. Reclaim that key
            // and send focus straight to the active tab — geometric focus search (moveFocus)
            // picks unpredictably among three same-row tabs below a full-width field, so the
            // target has to be named explicitly rather than guessed by position.
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(fieldFocusRequester)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown || event.key != Key.DirectionDown) return@onPreviewKeyEvent false
                    tabFocusRequesters.getValue(state.type).requestFocus()
                    true
                },
        )
        Spacer(Modifier.height(12.dp))

        val tabs = listOf(SearchType.Tracks, SearchType.Artists, SearchType.Albums)
        val labels = mapOf(
            SearchType.Tracks to "Треки",
            SearchType.Artists to "Исполнители",
            SearchType.Albums to "Альбомы",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            tabs.forEach { t ->
                TvSurface(
                    onClick = { vm.onTypeChange(t) },
                    color = if (state.type == t) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (state.type == t) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    focusRequester = tabFocusRequesters.getValue(t),
                    modifier = Modifier.height(48.dp),
                ) {
                    Box(Modifier.padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                        Text(labels.getValue(t), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).padding(top = 24.dp),
                )
                state.error != null -> Text(
                    "Ошибка: ${state.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                )
                state.query.isBlank() -> Text(
                    "Начните вводить запрос",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                )
                else -> Results(
                    state = state,
                    likedIds = likedIds,
                    downloadStates = downloadStates,
                    lazyListState = lazyListState,
                    onToggleLike = vm::toggleLike,
                    onPlay = vm::play,
                    onDownloadClick = vm::onDownloadClick,
                    onArtistClick = onArtistClick,
                    onAlbumClick = onAlbumClick,
                )
            }
        }
    }
}

@Composable
private fun Results(
    state: SearchUiState,
    likedIds: State<Set<String>>,
    downloadStates: State<Map<String, DownloadInfo>>,
    lazyListState: LazyListState,
    onToggleLike: (String) -> Unit,
    onPlay: (List<TrackDto>, Int) -> Unit,
    onDownloadClick: (String) -> Unit,
    onArtistClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
) {
    LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {
        when (state.type) {
            SearchType.Tracks -> {
                itemsIndexed(state.results.tracks, key = { _, t -> t.id }) { idx, track ->
                    TrackRow(
                        track = track,
                        isLiked = rememberIsLiked(track.id, likedIds),
                        download = rememberDownloadInfo(track.id, downloadStates),
                        onClick = { onPlay(state.results.tracks, idx) },
                        onLikeToggle = { onToggleLike(track.id) },
                        onDownloadClick = { onDownloadClick(track.id) },
                    )
                }
                if (state.results.tracks.isEmpty()) item { EmptyHint() }
            }
            SearchType.Artists -> {
                items(state.results.artists, key = { it.id }) { artist ->
                    ArtistRow(artist, onClick = { onArtistClick(artist.id) })
                }
                if (state.results.artists.isEmpty()) item { EmptyHint() }
            }
            SearchType.Albums -> {
                items(state.results.albums, key = { it.id }) { album ->
                    AlbumRow(album, onClick = { onAlbumClick(album.id) })
                }
                if (state.results.albums.isEmpty()) item { EmptyHint() }
            }
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

@Composable
private fun EmptyHint() {
    Text(
        "Ничего не найдено",
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
