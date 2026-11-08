package app.ocnotes.sync

import app.ocnotes.data.local.NoteDao
import app.ocnotes.data.remote.NotesApi
import app.ocnotes.data.remote.dto.CreateNoteRequest
import app.ocnotes.data.remote.dto.UpdateNoteRequest
import app.ocnotes.data.toEntity
import retrofit2.HttpException

// Orchestrates the Notes API v1.4 sync protocol against the local DAO.
// Pure Kotlin: testable in the JVM with fakes.
class SyncEngine(
    private val api: NotesApi,
    private val noteDao: NoteDao,
) {
    suspend fun initialSync() {
        val notes = api.notes()
        noteDao.upsertAll(notes.map { it.toEntity() })
    }

    suspend fun incrementalSync() {
        val watermark = noteDao.getHighestModified() ?: 0L
        // pruneBefore returns full notes for `modified >= watermark` and `{id}`
        // stubs for older ones. The response always contains every server id,
        // so ids missing locally are notes deleted server-side.
        val response = api.notes(pruneBefore = watermark)
        val full = response.filter { it.modified != null }.map { it.toEntity() }
        noteDao.upsertAll(full)

        val serverIds = response.map { it.id }.toSet()
        val staleIds = noteDao.getAllIdsOnce().filter { it !in serverIds }
        if (staleIds.isNotEmpty()) noteDao.deleteByIds(staleIds)
    }

    // Drains pending deletes and local edits to the server. Dirty flags are
    // cleared only after a successful server ack (or a 404 on delete).
    suspend fun pushPending() {
        for (note in noteDao.getPendingDeletes()) {
            try {
                api.deleteNote(note.id, quoteEtag(note.etag))
            } catch (e: HttpException) {
                if (e.code() != 404) throw e // already gone server-side is fine
            }
            noteDao.deleteByIds(listOf(note.id))
        }

        for (note in noteDao.getPendingUpdates()) {
            val updated = api.updateNote(
                note.id,
                UpdateNoteRequest(
                    title = note.title,
                    content = note.content,
                    category = note.category,
                    favorite = note.favorite,
                ),
                quoteEtag(note.etag),
            )
            noteDao.upsert(updated.toEntity().copy(dirty = false))
        }
    }

    companion object {
        // The API expects a quoted If-Match etag; the backend strips quotes, so
        // sending them is safe everywhere and required by upstream Nextcloud.
        fun quoteEtag(etag: String): String =
            if (etag.startsWith("\"")) etag else "\"$etag\""
    }
}
