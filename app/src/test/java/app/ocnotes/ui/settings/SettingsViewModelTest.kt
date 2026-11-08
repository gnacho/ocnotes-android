package app.ocnotes.ui.settings

import app.ocnotes.data.settings.ThemeMode
import app.ocnotes.testutil.FakeSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `setTheme updates the theme`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val vm = SettingsViewModel(FakeSettingsStore())
            vm.setTheme(ThemeMode.DARK)
            advanceUntilIdle()
            assertEquals(ThemeMode.DARK, vm.settings.value.theme)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `setSyncInterval updates the interval`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val vm = SettingsViewModel(FakeSettingsStore())
            vm.setSyncInterval(60)
            advanceUntilIdle()
            assertEquals(60, vm.settings.value.syncIntervalMinutes)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
