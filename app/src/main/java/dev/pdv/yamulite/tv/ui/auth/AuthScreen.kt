package dev.pdv.yamulite.tv.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pdv.yamulite.tv.ui.tv.TvSurface
import dev.pdv.yamulite.tv.ui.tv.rememberQrCode
import dev.pdv.yamulite.tv.ui.tv.requestOnce

@Composable
fun AuthScreen(vm: AuthViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.start() }

    Box(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (val s = state) {
            AuthUiState.Idle, AuthUiState.Loading, AuthUiState.Done -> Loading()
            is AuthUiState.Polling -> CodeBlock(s)
            is AuthUiState.Error -> ErrorBlock(s.message, onRetry = vm::start)
        }
    }
}

@Composable
private fun Loading() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Получаем код…", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CodeBlock(s: AuthUiState.Polling) {
    val qr = rememberQrCode(s.verificationUrl)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            "Войдите в Яндекс.Музыку",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (qr != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(12.dp),
                    ) {
                        Image(bitmap = qr, contentDescription = "QR-код для входа", modifier = Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Отсканируйте телефоном",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.width(360.dp),
            ) {
                Text(
                    "…или откройте на телефоне/компьютере:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                Text(
                    s.verificationUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "и введите код:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        s.userCode,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        CircularProgressIndicator()
        Text("Ожидаем подтверждения…", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Ошибка авторизации",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
        )
        Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        TvSurface(
            onClick = onRetry,
            focusRequester = focusRequester,
            modifier = Modifier.width(200.dp).height(56.dp),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Повторить", style = MaterialTheme.typography.titleMedium)
            }
        }
        focusRequester.requestOnce()
    }
}
