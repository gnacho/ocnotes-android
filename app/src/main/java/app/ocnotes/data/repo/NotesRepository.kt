package app.ocnotes.data.repo

import app.ocnotes.data.local.NoteDao
import app.ocnotes.data.local.NoteEntity
import app.ocnotes.data.remote.NotesApi
import app.ocnotes.data.remote.dto.CreateNoteRequest
import app.ocnotes.data.remote.dto.SettingsResponse
import app.ocnotes.data.remote.dto.UpdateNoteRequest
import app.ocnotes.data.session.Session
import app.ocnotes.data.session.SessionStore
import app.ocnotes.data.toEntity
import app.ocnotes.sync.SyncEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

// The Notes API derives the list title from the first line of the content. We
// mirror that on save so the server-side title stays meaningful.
fun deriveTitle(content: String): String {
    val first = content.lineSequence()
        .map { it.trim() }
        .firstOrNull { it.isNotEmpty() }
    return first ?: "New note"
}

// Single concurrency boundary between UI (ViewModels) and data sources.
class NotesRepository(
    private val sessionStore: SessionStore,
    private val noteDao: NoteDao,
    private val apiFactory: (Session) -> NotesApi,
) {
    val session: Flow<Session?> = sessionStore.session

    sealed interface LoginResult {
        data object Success : LoginResult
        data class Error(val message: String) : LoginResult
    }

    suspend fun login(serverUrl: String, username: String, token: String): LoginResult {
        val newSession = Session(serverUrl, username, token)
        return try {
            // GET /settings is the authenticated "ping" of the Notes API.
            apiFactory(newSession).settings()
            val previous = sessionStore.session.first()
            val changedAccount = previous == null ||
                previous.username != username || previous.serverUrl != serverUrl
            sessionStore.save(newSession)
            if (changedAccount) clearLocalData()
            LoginResult.Success
        } catch (e: HttpException) {
            val msg = if (e.code() == 401) {
                "Invalid username or app token"
            } else {
                "Server error (${e.code()})"
            }
            LoginResult.Error(msg)
        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "Login failed")
        }
    }

    suspend fun logout() {
        sessionStore.clear()
        clearLocalData()
    }

    suspend fun sync() {
        val session = sessionStore.session.first() ?: return
        val engine = SyncEngine(apiFactory(session), noteDao)
        engine.pushPending()
        val hasNotes = noteDao.getHighestModified() != null
        if (hasNotes) engine.incrementalSync() else engine.initialSync()
    }

    // Pushes queued edits/deletes without pulling. Used after a save so the
    // server receives the change promptly; failures keep the local queue.
    suspend fun pushPending() {
        val session = sessionStore.session.first() ?: return
        SyncEngine(apiFactory(session), noteDao).pushPending()
    }

    fun observeNotes(): Flow<List<NoteEntity>> = noteDao.getAll()
    fun observeFavorites(): Flow<List<NoteEntity>> = noteDao.getFavorites()
    fun observeNote(id: Long): Flow<NoteEntity?> = noteDao.getById(id)

    suspend fun getNote(id: Long): NoteEntity? = noteDao.getByIdOnce(id)

    suspend fun createNote(content: String, category: String): NoteEntity {
        val api = currentApi()
            ?: throw IllegalStateException("Not logged in")
        val created = api.createNote(CreateNoteRequest(deriveTitle(content), content, category))
        val entity = created.toEntity()
        noteDao.upsert(entity)
        return entity
    }

    suspend fun updateNote(id: Long, content: String, category: String) {
        val current = noteDao.getByIdOnce(id) ?: return
        noteDao.upsert(
            current.copy(
                title = deriveTitle(content),
                content = content,
                category = category,
                dirty = true,
            )
        )
    }

    suspend fun setFavorite(id: Long, favorite: Boolean) {
        val current = noteDao.getByIdOnce(id) ?: return
        noteDao.upsert(current.copy(favorite = favorite, dirty = true))
    }

    suspend fun deleteNote(id: Long) {
        val current = noteDao.getByIdOnce(id) ?: return
        noteDao.upsert(current.copy(deleted = true))
    }

    suspend fun getNotesSettings(): SettingsResponse? = currentApi()?.settings()
    suspend fun updateNotesSettings(settings: SettingsResponse): SettingsResponse? =
        currentApi()?.updateSettings(settings)

    private suspend fun currentApi(): NotesApi? {
        val session = sessionStore.session.first() ?: return null
        return apiFactory(session)
    }

    private suspend fun clearLocalData() {
        noteDao.clear()
    }
}
