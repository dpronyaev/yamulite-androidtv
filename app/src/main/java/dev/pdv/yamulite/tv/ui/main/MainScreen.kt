package dev.pdv.yamulite.tv.ui.main

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.pdv.yamulite.tv.ui.main.album.AlbumScreen
import dev.pdv.yamulite.tv.ui.main.artist.ArtistScreen
import dev.pdv.yamulite.tv.ui.main.components.CoverImage
import dev.pdv.yamulite.tv.ui.main.components.displayLine
import dev.pdv.yamulite.tv.ui.main.favorites.FavoritesScreen
import dev.pdv.yamulite.tv.ui.main.nowplaying.NowPlayingScreen
import dev.pdv.yamulite.tv.ui.main.nowplaying.NowPlayingViewModel
import dev.pdv.yamulite.tv.ui.main.search.SearchScreen
import dev.pdv.yamulite.tv.ui.main.settings.SettingsScreen
import dev.pdv.yamulite.tv.ui.tv.TvSurface

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Search("search", "Поиск", Icons.Filled.Search),
    Favorites("favorites", "Избранное", Icons.Filled.Favorite),
    NowPlaying("now", "Сейчас играет", Icons.Filled.MusicNote),
    Settings("settings", "Настройки", Icons.Filled.Settings),
}

private const val RAIL_WIDTH = 260

@Composable
fun MainScreen() {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun goToTab(tab: Tab) {
        if (currentRoute != tab.route) {
            nav.navigate(tab.route) {
                popUpTo(nav.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(RAIL_WIDTH.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Text(
                "YaMuLite",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(24.dp),
            )
            Tab.entries.forEach { tab ->
                val selected = backStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
                NavRailItem(
                    label = tab.label,
                    icon = tab.icon,
                    selected = selected,
                    onClick = { goToTab(tab) },
                )
            }
            Spacer(Modifier.weight(1f))
            MiniPlayerSection(onOpen = { goToTab(Tab.NowPlaying) })
        }

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            NavHost(
                navController = nav,
                startDestination = Tab.Search.route,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(Tab.Search.route) {
                    SearchScreen(
                        onArtistClick = { id -> nav.navigate("artist/$id") },
                        onAlbumClick = { id -> nav.navigate("album/$id") },
                    )
                }
                composable(Tab.Favorites.route) { FavoritesScreen() }
                composable(Tab.NowPlaying.route) {
                    NowPlayingScreen(
                        onArtistClick = { id -> nav.navigate("artist/$id") },
                        onAlbumClick = { id -> nav.navigate("album/$id") },
                    )
                }
                composable(Tab.Settings.route) { SettingsScreen() }
                composable(
                    route = "artist/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.LongType }),
                ) {
                    ArtistScreen(
                        onBack = { nav.popBackStack() },
                        onAlbumClick = { id -> nav.navigate("album/$id") },
                    )
                }
                composable(
                    route = "album/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.LongType }),
                ) {
                    AlbumScreen(onBack = { nav.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun NavRailItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    TvSurface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Owns its own [NowPlayingViewModel] collection so the position updates that tick twice a
 * second during playback recompose only this small subtree — not the whole [MainScreen] (nav
 * rail items, the NavHost's wrapping [Box], ...) the way a single top-level
 * `collectAsStateWithLifecycle()` call would, since every tick produces a structurally "new"
 * [dev.pdv.yamulite.tv.data.playback.PlaybackUi]. [derivedStateOf] narrows that down further so
 * this composable itself only re-executes when the track or play state actually changes, not on
 * every position tick.
 */
@Composable
private fun MiniPlayerSection(onOpen: () -> Unit, vm: NowPlayingViewModel = hiltViewModel()) {
    val playback by vm.state.collectAsStateWithLifecycle()
    val trackAndPlaying by remember { derivedStateOf { playback.track to playback.isPlaying } }
    val (track, isPlaying) = trackAndPlaying
    if (track == null) return

    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    MiniPlayer(
        trackLine = remember(track) { track.displayLine() },
        coverUri = track.coverUri ?: track.albums.firstOrNull()?.coverUri,
        isPlaying = isPlaying,
        onToggle = vm::togglePlayPause,
        onOpen = onOpen,
    )
}

@Composable
private fun MiniPlayer(
    trackLine: String,
    coverUri: String?,
    isPlaying: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
) {
    TvSurface(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth().padding(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().padding(10.dp),
        ) {
            CoverImage(coverUri = coverUri, side = 40.dp)
            Text(
                trackLine,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
    Spacer(Modifier.height(4.dp))
    TvSurface(onClick = onToggle, modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 12.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Пауза" else "Играть",
            )
        }
    }
}
