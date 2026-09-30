package dev.pdv.yamulite.tv.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

fun yandexCoverUrl(uri: String?, size: Int = 200): String? =
    uri?.takeIf { it.isNotBlank() }
        ?.let { "https://${it.replace("%%", "${size}x${size}")}" }

@Composable
fun CoverImage(
    coverUri: String?,
    modifier: Modifier = Modifier,
    side: Dp = 56.dp,
    pixelSize: Int = 200,
) {
    val url = remember(coverUri, pixelSize) { yandexCoverUrl(coverUri, pixelSize) }
    val shape = RoundedCornerShape(8.dp)
    if (url == null) {
        androidx.compose.foundation.layout.Box(
            modifier = modifier
                .size(side)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
    } else {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(side)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
    }
}
