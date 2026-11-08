package app.ocnotes.ui.list

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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotesViewModelTest {

    private fun viewModel(dao: FakeNoteDao = FakeNoteDao()): Pair<NotesViewModel, FakeNoteDao> {
        val repo = NotesRepository(
            sessionStore = FakeSessionStore(),
            noteDao = dao,
            apiFactory = { FakeNotesApi() },
        )
        return NotesViewModel(repo) to dao
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `toggleFavorite flips the note favorite`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dao = FakeNoteDao().apply { notes.add(note(1, favorite = false)) }
            val (vm, _) = viewModel(dao)

            vm.toggleFavorite(note(1, favorite = false))
            advanceUntilIdle()

            assertTrue(dao.notes.first { it.id == 1L }.favorite)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `selectScope updates the scope state`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val (vm, _) = viewModel()
            assertEquals(NotesScope.All, vm.scopeState.value)

            vm.selectScope(NotesScope.Favorites)
            advanceUntilIdle()

            assertEquals(NotesScope.Favorites, vm.scopeState.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `logout clears the session`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val (vm, dao) = viewModel(FakeNoteDao().apply { notes.add(note(1)) })

            vm.logout()
            advanceUntilIdle()

            assertFalse(dao.notes.isNotEmpty())
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `sync failure sets an error instead of crashing`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repo = NotesRepository(
                sessionStore = FakeSessionStore(Session("https://example.com", "alice", "token")),
                noteDao = FakeNoteDao(),
                apiFactory = { FakeNotesApi(notesErrorCode = 500) },
            )
            // init { sync() } runs against a throwing API: must not throw.
            val vm = NotesViewModel(repo)
            advanceUntilIdle()

            assertNotNull(vm.syncError.value)
            assertFalse(vm.syncing.value)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
