package dev.pdv.yamulite.tv.ui.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RetryButton(onClick: () -> Unit, label: String = "Повторить") {
    TvSurface(onClick = onClick, modifier = Modifier.height(52.dp).width(180.dp)) {
        Box(Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
    }
}
