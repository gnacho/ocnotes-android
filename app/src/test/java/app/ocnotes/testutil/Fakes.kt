package app.ocnotes.testutil

import app.ocnotes.data.local.NoteDao
import app.ocnotes.data.local.NoteEntity
import app.ocnotes.data.remote.NotesApi
import app.ocnotes.data.remote.dto.CreateNoteRequest
import app.ocnotes.data.remote.dto.NoteDto
import app.ocnotes.data.remote.dto.SettingsResponse
import app.ocnotes.data.remote.dto.UpdateNoteRequest
import app.ocnotes.data.session.Session
import app.ocnotes.data.session.SessionStore
import app.ocnotes.data.settings.AppSettings
import app.ocnotes.data.settings.SettingsStore
import app.ocnotes.data.settings.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

fun note(
    id: Long,
    modified: Long = id,
    favorite: Boolean = false,
    dirty: Boolean = false,
    deleted: Boolean = false,
    category: String = "",
    content: String = "c$id",
    title: String = "t$id",
    etag: String = "e$id",
) = NoteEntity(
    id = id,
    etag = etag,
    readonly = false,
    modified = modified,
    title = title,
    category = category,
    content = content,
    favorite = favorite,
    dirty = dirty,
    deleted = deleted,
)

class FakeSessionStore(initial: Session? = null) : SessionStore {
    private val state = MutableStateFlow<Session?>(initial)
    override val session: Flow<Session?> = state
    override suspend fun save(session: Session) { state.value = session }
    override suspend fun clear() { state.value = null }
}

class FakeSettingsStore : SettingsStore {
    private val state = MutableStateFlow(AppSettings())
    override val settings: Flow<AppSettings> = state
    override suspend fun setTheme(mode: ThemeMode) { state.value = state.value.copy(theme = mode) }
    override suspend fun setSyncInterval(minutes: Int) {
        state.value = state.value.copy(syncIntervalMinutes = minutes)
    }
}

class FakeNoteDao : NoteDao {
    val notes = mutableListOf<NoteEntity>()
    val upsertCalls = mutableListOf<NoteEntity>()
    val deleteCalls = mutableListOf<List<Long>>()

    override suspend fun upsertAll(notes: List<NoteEntity>) {
        val ids = notes.map { it.id }.toSet()
        this.notes.removeAll { it.id in ids }
        this.notes.addAll(notes)
    }

    override suspend fun upsert(note: NoteEntity) {
        notes.removeAll { it.id == note.id }
        notes.add(note)
        upsertCalls.add(note)
    }

    override fun getAll(): Flow<List<NoteEntity>> = emptyFlow()
    override fun getFavorites(): Flow<List<NoteEntity>> = emptyFlow()
    override fun getById(id: Long): Flow<NoteEntity?> = emptyFlow()
    override suspend fun getByIdOnce(id: Long): NoteEntity? = notes.firstOrNull { it.id == id }
    override suspend fun getHighestModified(): Long? =
        notes.filter { !it.deleted }.maxOfOrNull { it.modified }
    override suspend fun getAllIdsOnce(): List<Long> = notes.filter { !it.deleted }.map { it.id }
    override suspend fun getPendingUpdates(): List<NoteEntity> = notes.filter { it.dirty && !it.deleted }
    override suspend fun getPendingDeletes(): List<NoteEntity> = notes.filter { it.deleted }

    override suspend fun deleteByIds(ids: List<Long>) {
        deleteCalls.add(ids)
        notes.removeAll { it.id in ids }
    }

    override suspend fun clear() { notes.clear() }
}

class FakeNotesApi(
    var settingsResponse: SettingsResponse = SettingsResponse(),
    var notesResponse: List<NoteDto> = emptyList(),
    var pruneResponse: List<NoteDto> = emptyList(),
    var createdNote: NoteDto = NoteDto(
        id = 1,
        etag = "e1",
        readonly = false,
        modified = 1,
        title = "t",
        category = "",
        content = "",
        favorite = false,
    ),
    var settingsErrorCode: Int? = null,
    var notesErrorCode: Int? = null,
    var deleteErrorCode: Int? = null,
) : NotesApi {
    val notesCalls = mutableListOf<Long?>()
    val createCalls = mutableListOf<CreateNoteRequest>()
    val updateCalls = mutableListOf<Triple<Long, UpdateNoteRequest, String>>()
    val deleteCalls = mutableListOf<Pair<Long, String>>()

    override suspend fun settings(): SettingsResponse {
        settingsErrorCode?.let { code ->
            throw HttpException(
                Response.error<SettingsResponse>(code, "".toResponseBody("application/json".toMediaType()))
            )
        }
        return settingsResponse
    }

    override suspend fun updateSettings(body: SettingsResponse): SettingsResponse = body

    override suspend fun notes(pruneBefore: Long?): List<NoteDto> {
        notesCalls.add(pruneBefore)
        notesErrorCode?.let { code ->
            throw HttpException(
                Response.error<List<NoteDto>>(code, "".toResponseBody("application/json".toMediaType()))
            )
        }
        return if (pruneBefore == null) notesResponse else pruneResponse
    }

    override suspend fun createNote(body: CreateNoteRequest): NoteDto {
        createCalls.add(body)
        return createdNote.copy(title = body.title, content = body.content, category = body.category)
    }

    override suspend fun note(id: Long): NoteDto = createdNote.copy(id = id)

    override suspend fun updateNote(id: Long, body: UpdateNoteRequest, etag: String): NoteDto {
        updateCalls.add(Triple(id, body, etag))
        return NoteDto(
            id = id,
            etag = "e$id-2",
            readonly = false,
            modified = 2,
            title = body.title ?: "",
            category = body.category ?: "",
            content = body.content ?: "",
            favorite = body.favorite ?: false,
        )
    }

    override suspend fun deleteNote(id: Long, etag: String) {
        deleteErrorCode?.let { code ->
            throw HttpException(
                Response.error<Unit>(code, "".toResponseBody("application/json".toMediaType()))
            )
        }
        deleteCalls.add(id to etag)
    }
}
