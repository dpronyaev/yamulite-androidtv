package dev.pdv.yamulite.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import dev.pdv.yamulite.tv.ui.AppRoot
import dev.pdv.yamulite.tv.ui.theme.YaMuLiteTvTheme

/**
 * Single Activity, always dark (10-foot UI, no light-mode toggle worth the couch-viewing cost).
 *
 * No custom key handling is needed for the transport buttons on the remote
 * (play/pause/next/previous): [dev.pdv.yamulite.tv.data.playback.PlaybackService] registers a
 * MediaSession, and Android routes KEYCODE_MEDIA_* from any remote straight to the active
 * session — that works even while a different screen (Search, Settings, ...) has focus.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YaMuLiteTvTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppRoot()
                }
            }
        }
    }
}
