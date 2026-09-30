package dev.pdv.yamulite.tv.ui.main.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.pdv.yamulite.tv.data.auth.AuthRepository
import dev.pdv.yamulite.tv.data.music.MusicRepository
import dev.pdv.yamulite.tv.data.settings.CodecPreference
import dev.pdv.yamulite.tv.data.settings.Quality
import dev.pdv.yamulite.tv.data.settings.SettingsStore
import dev.pdv.yamulite.tv.data.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: SettingsStore,
    private val authRepo: AuthRepository,
    private val musicRepo: MusicRepository,
) : ViewModel() {
    val quality = store.quality.stateIn(viewModelScope, SharingStarted.Eagerly, Quality.High)
    val codec = store.codec.stateIn(viewModelScope, SharingStarted.Eagerly, CodecPreference.AacPreferred)
    val themeMode = store.themeMode.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.System)

    fun setQuality(q: Quality) = viewModelScope.launch { store.setQuality(q) }
    fun setCodec(c: CodecPreference) = viewModelScope.launch { store.setCodec(c) }
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { store.setThemeMode(mode) }

    fun logout() = viewModelScope.launch {
        musicRepo.resetSession()
        authRepo.logout()
    }
}
