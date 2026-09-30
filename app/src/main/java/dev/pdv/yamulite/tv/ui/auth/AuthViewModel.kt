package dev.pdv.yamulite.tv.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pdv.yamulite.tv.data.auth.AuthEvent
import dev.pdv.yamulite.tv.data.auth.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Polling(
        val userCode: String,
        val verificationUrl: String,
        val expiresAt: Long,
    ) : AuthUiState()
    data object Done : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private var job: Job? = null

    fun start() {
        job?.cancel()
        _state.value = AuthUiState.Loading
        job = viewModelScope.launch {
            repo.startAuth().collect { event ->
                _state.value = when (event) {
                    is AuthEvent.CodeReceived -> AuthUiState.Polling(
                        event.userCode,
                        event.verificationUrl,
                        event.expiresAt,
                    )
                    AuthEvent.Authorized -> AuthUiState.Done
                    is AuthEvent.Failed -> AuthUiState.Error(event.message)
                }
            }
        }
    }
}
