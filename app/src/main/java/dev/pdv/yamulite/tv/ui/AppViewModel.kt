package dev.pdv.yamulite.tv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pdv.yamulite.tv.data.auth.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthGate(val ready: Boolean, val token: String?)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepo: AuthRepository,
) : ViewModel() {
    val gate: StateFlow<AuthGate> = authRepo.tokenFlow
        .map { AuthGate(ready = true, token = it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AuthGate(ready = false, token = null))

    fun logout() = viewModelScope.launch { authRepo.logout() }
}
