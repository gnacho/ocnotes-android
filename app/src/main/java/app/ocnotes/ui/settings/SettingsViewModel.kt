package app.ocnotes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.ocnotes.data.settings.AppSettings
import app.ocnotes.data.settings.FontSizeOption
import app.ocnotes.data.settings.NoteOpenMode
import app.ocnotes.data.settings.SettingsStore
import app.ocnotes.data.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val store: SettingsStore) : ViewModel() {
    val settings: StateFlow<AppSettings> = store.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { store.setTheme(mode) }
    fun setSyncInterval(minutes: Int) = viewModelScope.launch { store.setSyncInterval(minutes) }
    fun setBackgroundSync(enabled: Boolean) = viewModelScope.launch { store.setBackgroundSync(enabled) }
    fun setWifiOnly(enabled: Boolean) = viewModelScope.launch { store.setWifiOnly(enabled) }
    fun setNoteOpenMode(mode: NoteOpenMode) = viewModelScope.launch { store.setNoteOpenMode(mode) }
    fun setDirectEditing(enabled: Boolean) = viewModelScope.launch { store.setDirectEditing(enabled) }
    fun setMonospace(enabled: Boolean) = viewModelScope.launch { store.setMonospace(enabled) }
    fun setFontSize(size: FontSizeOption) = viewModelScope.launch { store.setFontSize(size) }
    fun setKeepScreenOn(enabled: Boolean) = viewModelScope.launch { store.setKeepScreenOn(enabled) }
    fun setPreventScreenshots(enabled: Boolean) = viewModelScope.launch { store.setPreventScreenshots(enabled) }

    companion object {
        fun factory(store: SettingsStore): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SettingsViewModel(store) as T
            }
    }
}
