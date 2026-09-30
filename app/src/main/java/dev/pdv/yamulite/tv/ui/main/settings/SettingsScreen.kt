package dev.pdv.yamulite.tv.ui.main.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pdv.yamulite.tv.data.settings.CodecPreference
import dev.pdv.yamulite.tv.data.settings.Quality
import dev.pdv.yamulite.tv.ui.tv.TvSurface

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val currentQuality by vm.quality.collectAsStateWithLifecycle()
    val currentCodec by vm.codec.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Выйти из аккаунта?") },
            text = { Text("Сессия будет завершена. Для входа потребуется снова пройти авторизацию через Яндекс.") },
            confirmButton = {
                TextButton(onClick = { vm.logout(); showLogoutDialog = false }) {
                    Text("Выйти", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Отмена") }
            },
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Качество музыки", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Quality.entries.forEach { q ->
            OptionRow(label = q.label, selected = currentQuality == q, onClick = { vm.setQuality(q) })
        }

        Spacer(Modifier.height(24.dp))
        Text("Кодек", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        CodecPreference.entries.forEach { c ->
            OptionRow(label = c.label, selected = currentCodec == c, onClick = { vm.setCodec(c) })
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Скачивание запускается кнопкой облака рядом с треком в списках. Скачанные " +
                "треки воспроизводятся локально и не расходуют интернет.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))
        TvSurface(
            onClick = { showLogoutDialog = true },
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.width(280.dp).height(56.dp),
        ) {
            androidx.compose.foundation.layout.Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Выйти из аккаунта", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    TvSurface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        ) {
            if (selected) {
                androidx.compose.material3.Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                Spacer(Modifier.width(24.dp))
            }
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
