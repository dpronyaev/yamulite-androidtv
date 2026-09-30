package dev.pdv.yamulite.tv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pdv.yamulite.tv.ui.auth.AuthScreen
import dev.pdv.yamulite.tv.ui.main.MainScreen

@Composable
fun AppRoot(vm: AppViewModel = hiltViewModel()) {
    val gate by vm.gate.collectAsStateWithLifecycle()
    when {
        !gate.ready -> Splash()
        gate.token == null -> AuthScreen()
        else -> MainScreen()
    }
}

@Composable
private fun Splash() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
