package app.ocnotes.ui.editor

import app.ocnotes.data.repo.NotesRepository
import app.ocnotes.data.session.Session
import app.ocnotes.testutil.FakeNoteDao
import app.ocnotes.testutil.FakeNotesApi
import app.ocnotes.testutil.FakeSessionStore
import app.ocnotes.testutil.note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteEditorViewModelTest {

    private fun repo(dao: FakeNoteDao, api: FakeNotesApi = FakeNotesApi()) = NotesRepository(
        sessionStore = FakeSessionStore(Session("https://example.com", "alice", "token")),
        noteDao = dao,
        apiFactory = { api },
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `new note save creates it and closes`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dao = FakeNoteDao()
            val api = FakeNotesApi()
            val vm = NoteEditorViewModel(repo(dao, api), noteId = null)

            assertTrue(vm.isNew)
            vm.onContentChange("hello")
            vm.save()
            advanceUntilIdle()

            assertEquals(1, api.createCalls.size)
            assertEquals("hello", api.createCalls.first().title)
            assertTrue(vm.closed.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `existing note reloads and save updates it`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dao = FakeNoteDao().apply { notes.add(note(1, content = "old")) }
            val api = FakeNotesApi()
            val vm = NoteEditorViewModel(repo(dao, api), noteId = 1)
            advanceUntilIdle()

            assertEquals("old", vm.content.value)

            vm.onContentChange("new body")
            vm.save()
            advanceUntilIdle()

            val stored = dao.notes.first { it.id == 1L }
            assertEquals("new body", stored.content)
            assertEquals(1, api.updateCalls.size)
            assertTrue(vm.closed.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `delete leaves a tombstone and closes`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dao = FakeNoteDao().apply { notes.add(note(1)) }
            val vm = NoteEditorViewModel(repo(dao), noteId = 1)

            vm.delete()
            advanceUntilIdle()

            assertTrue(dao.notes.first { it.id == 1L }.deleted)
            assertTrue(vm.closed.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `toggleFavorite only works for existing notes`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dao = FakeNoteDao().apply { notes.add(note(1, favorite = false)) }
            val existing = NoteEditorViewModel(repo(dao), noteId = 1)
            advanceUntilIdle()
            existing.toggleFavorite()
            advanceUntilIdle()
            assertTrue(dao.notes.first { it.id == 1L }.favorite)

            val fresh = NoteEditorViewModel(repo(dao), noteId = null)
            fresh.toggleFavorite()
            advanceUntilIdle()
            assertFalse(fresh.favorite.value)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
