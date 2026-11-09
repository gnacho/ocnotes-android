package app.ocnotes.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class NoteOpenMode { PLAIN, RICH }

enum class FontSizeOption { SMALL, MEDIUM, LARGE }

data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val syncIntervalMinutes: Int = 15,
    val backgroundSync: Boolean = true,
    val wifiOnly: Boolean = false,
    val noteOpenMode: NoteOpenMode = NoteOpenMode.RICH,
    val directEditing: Boolean = true,
    val monospace: Boolean = false,
    val fontSize: FontSizeOption = FontSizeOption.MEDIUM,
    val keepScreenOn: Boolean = true,
    val preventScreenshots: Boolean = false,
    val gridView: Boolean = false,
    val swipeActions: Boolean = true,
) {
    val fontSizeSp: Float
        get() = when (fontSize) {
            FontSizeOption.SMALL -> 14f
            FontSizeOption.MEDIUM -> 16f
            FontSizeOption.LARGE -> 19f
        }
}

interface SettingsStore {
    val settings: Flow<AppSettings>
    suspend fun setTheme(mode: ThemeMode)
    suspend fun setSyncInterval(minutes: Int)
    suspend fun setBackgroundSync(enabled: Boolean)
    suspend fun setWifiOnly(enabled: Boolean)
    suspend fun setNoteOpenMode(mode: NoteOpenMode)
    suspend fun setDirectEditing(enabled: Boolean)
    suspend fun setMonospace(enabled: Boolean)
    suspend fun setFontSize(size: FontSizeOption)
    suspend fun setKeepScreenOn(enabled: Boolean)
    suspend fun setPreventScreenshots(enabled: Boolean)
    suspend fun setGridView(enabled: Boolean)
    suspend fun setSwipeActions(enabled: Boolean)
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreSettingsStore(private val context: Context) : SettingsStore {
    private val keyTheme = stringPreferencesKey("theme")
    private val keyInterval = intPreferencesKey("sync_interval_minutes")
    private val keyBackground = booleanPreferencesKey("background_sync")
    private val keyWifi = booleanPreferencesKey("wifi_only")
    private val keyOpenMode = stringPreferencesKey("note_open_mode")
    private val keyDirectEditing = booleanPreferencesKey("direct_editing")
    private val keyMonospace = booleanPreferencesKey("monospace")
    private val keyFontSize = stringPreferencesKey("font_size")
    private val keyKeepScreenOn = booleanPreferencesKey("keep_screen_on")
    private val keyPreventShot = booleanPreferencesKey("prevent_screenshots")
    private val keyGridView = booleanPreferencesKey("grid_view")
    private val keySwipeActions = booleanPreferencesKey("swipe_actions")

    override val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            theme = p[keyTheme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            syncIntervalMinutes = p[keyInterval] ?: 15,
            backgroundSync = p[keyBackground] ?: true,
            wifiOnly = p[keyWifi] ?: false,
            noteOpenMode = p[keyOpenMode]?.let { runCatching { NoteOpenMode.valueOf(it) }.getOrNull() } ?: NoteOpenMode.RICH,
            directEditing = p[keyDirectEditing] ?: true,
            monospace = p[keyMonospace] ?: false,
            fontSize = p[keyFontSize]?.let { runCatching { FontSizeOption.valueOf(it) }.getOrNull() } ?: FontSizeOption.MEDIUM,
            keepScreenOn = p[keyKeepScreenOn] ?: true,
            preventScreenshots = p[keyPreventShot] ?: false,
            gridView = p[keyGridView] ?: false,
            swipeActions = p[keySwipeActions] ?: true,
        )
    }

    override suspend fun setTheme(mode: ThemeMode) { context.dataStore.edit { it[keyTheme] = mode.name } }
    override suspend fun setSyncInterval(minutes: Int) { context.dataStore.edit { it[keyInterval] = minutes } }
    override suspend fun setBackgroundSync(enabled: Boolean) { context.dataStore.edit { it[keyBackground] = enabled } }
    override suspend fun setWifiOnly(enabled: Boolean) { context.dataStore.edit { it[keyWifi] = enabled } }
    override suspend fun setNoteOpenMode(mode: NoteOpenMode) { context.dataStore.edit { it[keyOpenMode] = mode.name } }
    override suspend fun setDirectEditing(enabled: Boolean) { context.dataStore.edit { it[keyDirectEditing] = enabled } }
    override suspend fun setMonospace(enabled: Boolean) { context.dataStore.edit { it[keyMonospace] = enabled } }
    override suspend fun setFontSize(size: FontSizeOption) { context.dataStore.edit { it[keyFontSize] = size.name } }
    override suspend fun setKeepScreenOn(enabled: Boolean) { context.dataStore.edit { it[keyKeepScreenOn] = enabled } }
    override suspend fun setPreventScreenshots(enabled: Boolean) { context.dataStore.edit { it[keyPreventShot] = enabled } }
    override suspend fun setGridView(enabled: Boolean) { context.dataStore.edit { it[keyGridView] = enabled } }
    override suspend fun setSwipeActions(enabled: Boolean) { context.dataStore.edit { it[keySwipeActions] = enabled } }
}
