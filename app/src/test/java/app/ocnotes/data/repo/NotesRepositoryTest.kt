package app.ocnotes.data.repo

import app.ocnotes.data.remote.dto.NoteDto
import app.ocnotes.testutil.FakeNoteDao
import app.ocnotes.testutil.FakeNotesApi
import app.ocnotes.testutil.FakeSessionStore
import app.ocnotes.testutil.note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotesRepositoryTest {

    private fun repo(
        sessionStore: FakeSessionStore = FakeSessionStore(),
        dao: FakeNoteDao = FakeNoteDao(),
        api: FakeNotesApi = FakeNotesApi(),
    ) = NotesRepository(
        sessionStore = sessionStore,
        noteDao = dao,
        apiFactory = { api },
    )

    @Test
    fun `login saves the session on success`() = runTest {
        val sessionStore = FakeSessionStore()
        val repo = repo(sessionStore = sessionStore)

        val result = repo.login("https://example.com", "alice", "token")

        assertEquals(NotesRepository.LoginResult.Success, result)
        assertEquals("alice", sessionStore.session.first()?.username)
    }

    @Test
    fun `login reports 401 without saving the session`() = runTest {
        val sessionStore = FakeSessionStore()
        val api = FakeNotesApi(settingsErrorCode = 401)
        val repo = repo(sessionStore = sessionStore, api = api)

        val result = repo.login("https://example.com", "alice", "bad")

        assertTrue(result is NotesRepository.LoginResult.Error)
        assertNull(sessionStore.session.first())
    }

    @Test
    fun `login clears local data when the account changes`() = runTest {
        val sessionStore = FakeSessionStore()
        val dao = FakeNoteDao().apply { notes.add(note(1)) }
        val repo = repo(sessionStore = sessionStore, dao = dao)

        repo.login("https://example.com", "alice", "token")

        assertTrue(dao.notes.isEmpty())
    }

    @Test
    fun `sync runs initial pull when the database is empty`() = runTest {
        val sessionStore = FakeSessionStore()
        sessionStore.save(app.ocnotes.data.session.Session("https://example.com", "alice", "token"))
        val dao = FakeNoteDao()
        val api = FakeNotesApi(notesResponse = listOf(NoteDto(1, etag = "e", readonly = false, modified = 5, title = "a", category = "", content = "a", favorite = false)))
        val repo = repo(sessionStore, dao, api)

        repo.sync()

        assertEquals(listOf<Long?>(null), api.notesCalls)
        assertEquals(1, dao.notes.size)
    }

    @Test
    fun `createNote posts and stores the returned note`() = runTest {
        val sessionStore = FakeSessionStore()
        sessionStore.save(app.ocnotes.data.session.Session("https://example.com", "alice", "token"))
        val dao = FakeNoteDao()
        val api = FakeNotesApi()
        val repo = repo(sessionStore, dao, api)

        repo.createNote("hello", "hello\nworld", "cat")

        assertEquals("hello", api.createCalls.first().title)
        assertEquals(1, dao.notes.size)
    }

    @Test
    fun `updateNote stores the edit and flags it dirty`() = runTest {
        val sessionStore = FakeSessionStore()
        sessionStore.save(app.ocnotes.data.session.Session("https://example.com", "alice", "token"))
        val dao = FakeNoteDao().apply { notes.add(note(1, content = "old")) }
        val repo = repo(sessionStore, dao)

        repo.updateNote(1, "hello", "new body", "cat")

        val stored = dao.notes.first { it.id == 1L }
        assertEquals("new body", stored.content)
        assertEquals("cat", stored.category)
        assertTrue(stored.dirty)
    }

    @Test
    fun `setFavorite flags dirty and deleteNote leaves a tombstone`() = runTest {
        val sessionStore = FakeSessionStore()
        sessionStore.save(app.ocnotes.data.session.Session("https://example.com", "alice", "token"))
        val dao = FakeNoteDao().apply { notes.add(note(1)) }
        val repo = repo(sessionStore, dao)

        repo.setFavorite(1, true)
        assertTrue(dao.notes.first { it.id == 1L }.dirty)
        assertTrue(dao.notes.first { it.id == 1L }.favorite)

        repo.deleteNote(1)
        assertTrue(dao.notes.first { it.id == 1L }.deleted)
    }
}
