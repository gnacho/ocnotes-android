package app.ocnotes.di

import android.content.Context
import androidx.room.Room
import app.ocnotes.data.local.OcnotesDatabase
import app.ocnotes.data.remote.ApiClient
import app.ocnotes.data.remote.Credentials
import app.ocnotes.data.repo.NotesRepository
import app.ocnotes.data.session.DataStoreSessionStore
import app.ocnotes.data.session.SessionStore
import app.ocnotes.data.settings.DataStoreSettingsStore
import app.ocnotes.data.settings.SettingsStore

// Manual DI (no Hilt for the MVP): a single graph owned by the Application.
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: OcnotesDatabase = Room.databaseBuilder(
        appContext,
        OcnotesDatabase::class.java,
        "ocnotes.db",
    ).build()

    val sessionStore: SessionStore = DataStoreSessionStore(appContext)

    val settingsStore: SettingsStore = DataStoreSettingsStore(appContext)

    val repository: NotesRepository = NotesRepository(
        sessionStore = sessionStore,
        noteDao = database.noteDao(),
        apiFactory = { session ->
            ApiClient.create(session.baseUrl) { Credentials(session.username, session.token) }
        },
    )
}
