package app.ocnotes.ui.login

import app.ocnotes.data.repo.NotesRepository
import app.ocnotes.testutil.FakeNoteDao
import app.ocnotes.testutil.FakeNotesApi
import app.ocnotes.testutil.FakeSessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginViewModelTest {

    private fun repo(api: FakeNotesApi = FakeNotesApi()) = NotesRepository(
        sessionStore = FakeSessionStore(),
        noteDao = FakeNoteDao(),
        apiFactory = { api },
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `successful login sets loggedIn`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val vm = LoginViewModel(repo())
            vm.onServerUrlChange("https://example.com")
            vm.onUsernameChange("alice")
            vm.onTokenChange("token")

            vm.login()
            advanceUntilIdle()

            assertTrue(vm.state.value.loggedIn)
            assertNull(vm.state.value.error)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `bad credentials surface an error`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val vm = LoginViewModel(repo(FakeNotesApi(settingsErrorCode = 401)))
            vm.onServerUrlChange("https://example.com")
            vm.onUsernameChange("alice")
            vm.onTokenChange("bad")

            vm.login()
            advanceUntilIdle()

            assertNotNull(vm.state.value.error)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `credential update prefills the account and clears the token`() {
        val vm = LoginViewModel(repo())

        vm.startCredentialUpdate("https://example.com", "alice")

        val state = vm.state.value
        assertTrue(state.updatingCredentials)
        assertEquals("https://example.com", state.serverUrl)
        assertEquals("alice", state.username)
        assertEquals("", state.token)
    }
}
