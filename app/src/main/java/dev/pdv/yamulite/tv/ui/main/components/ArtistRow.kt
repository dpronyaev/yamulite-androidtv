package dev.pdv.yamulite.tv.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.pdv.yamulite.tv.data.music.dto.AlbumDto
import dev.pdv.yamulite.tv.data.music.dto.ArtistDto
import dev.pdv.yamulite.tv.ui.tv.TvSurface

@Composable
fun ArtistRow(artist: ArtistDto, onClick: () -> Unit = {}, focusRequester: FocusRequester? = null) {
    TvSurface(onClick = onClick, focusRequester = focusRequester, modifier = Modifier.fillMaxWidth().height(76.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            CoverImage(coverUri = artist.cover?.uri ?: artist.ogImage, side = 56.dp)
            Text(
                artist.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun AlbumRow(album: AlbumDto, onClick: () -> Unit = {}, focusRequester: FocusRequester? = null) {
    TvSurface(onClick = onClick, focusRequester = focusRequester, modifier = Modifier.fillMaxWidth().height(76.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            CoverImage(coverUri = album.coverUri, side = 56.dp)
            val line = remember(album.id, album.title, album.artists) {
                val artist = album.artists.joinToString(", ") { it.name }
                if (artist.isBlank()) album.title else "$artist — ${album.title}"
            }
            Text(
                line,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
