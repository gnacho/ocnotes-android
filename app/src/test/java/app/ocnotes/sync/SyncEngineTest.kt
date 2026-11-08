package app.ocnotes.sync

import app.ocnotes.data.remote.dto.NoteDto
import app.ocnotes.testutil.FakeNoteDao
import app.ocnotes.testutil.FakeNotesApi
import app.ocnotes.testutil.note
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncEngineTest {

    @Test
    fun `initialSync upserts every note`() = runTest {
        val dao = FakeNoteDao()
        val api = FakeNotesApi(
            notesResponse = listOf(
                NoteDto(1, etag = "a", readonly = false, modified = 100, title = "A", category = "", content = "a", favorite = false),
                NoteDto(2, etag = "b", readonly = false, modified = 200, title = "B", category = "c", content = "b", favorite = true),
            )
        )

        SyncEngine(api, dao).initialSync()

        assertEquals(listOf<Long?>(null), api.notesCalls)
        assertEquals(setOf(1L, 2L), dao.notes.map { it.id }.toSet())
    }

    @Test
    fun `incrementalSync sends watermark and upserts only full notes`() = runTest {
        val dao = FakeNoteDao().apply {
            notes.add(note(1, modified = 100))
            notes.add(note(2, modified = 200))
        }
        val api = FakeNotesApi(
            pruneResponse = listOf(
                NoteDto(1), // stub: unchanged
                NoteDto(2, etag = "b", readonly = false, modified = 200, title = "B", category = "", content = "b2", favorite = false),
                NoteDto(3, etag = "c", readonly = false, modified = 300, title = "C", category = "", content = "c", favorite = false),
            )
        )

        SyncEngine(api, dao).incrementalSync()

        assertEquals(listOf<Long?>(200), api.notesCalls)
        assertEquals(setOf(1L, 2L, 3L), dao.notes.map { it.id }.toSet())
    }

    @Test
    fun `incrementalSync deletes notes missing from the server response`() = runTest {
        val dao = FakeNoteDao().apply {
            notes.add(note(1, modified = 100))
            notes.add(note(2, modified = 200))
        }
        val api = FakeNotesApi(
            pruneResponse = listOf(NoteDto(1)) // note 2 was deleted server-side
        )

        SyncEngine(api, dao).incrementalSync()

        assertEquals(setOf(1L), dao.notes.map { it.id }.toSet())
        assertEquals(listOf(listOf(2L)), dao.deleteCalls)
    }

    @Test
    fun `pushPending deletes tombstones then sends dirty edits`() = runTest {
        val dao = FakeNoteDao().apply {
            notes.add(note(1, deleted = true, etag = "e1"))
            notes.add(note(2, dirty = true, etag = "e2", content = "edited"))
        }
        val api = FakeNotesApi()

        SyncEngine(api, dao).pushPending()

        assertEquals(listOf(1L to "\"e1\""), api.deleteCalls)
        assertEquals(1, api.updateCalls.size)
        assertEquals(2L, api.updateCalls.first().first)
        assertEquals("edited", api.updateCalls.first().second.content)
        // dirty flag cleared after ack; fresh server copy stored
        assertEquals(false, dao.notes.first { it.id == 2L }.dirty)
    }

    @Test
    fun `pushPending swallows 404 on delete`() = runTest {
        val dao = FakeNoteDao().apply {
            notes.add(note(1, deleted = true))
        }
        val api = FakeNotesApi(deleteErrorCode = 404)

        SyncEngine(api, dao).pushPending()

        // The tombstone is removed even though the server said 404.
        assertEquals(emptyList<Any>(), dao.notes)
    }

    @Test
    fun `pushPending sends nothing when there is nothing pending`() = runTest {
        val dao = FakeNoteDao().apply { notes.add(note(1)) }
        val api = FakeNotesApi()

        SyncEngine(api, dao).pushPending()

        assertEquals(emptyList<Pair<Long, String>>(), api.deleteCalls)
        assertEquals(emptyList<Triple<Long, Any, String>>(), api.updateCalls)
    }
}
