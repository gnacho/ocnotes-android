package app.ocnotes

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.ocnotes.data.settings.AppSettings
import app.ocnotes.data.settings.NoteOpenMode
import app.ocnotes.data.settings.ThemeMode
import app.ocnotes.sync.SyncScheduler
import app.ocnotes.ui.editor.NoteEditorScreen
import app.ocnotes.ui.editor.NoteEditorViewModel
import app.ocnotes.ui.list.NotesCounts
import app.ocnotes.ui.list.NotesScreen
import app.ocnotes.ui.list.NotesViewModel
import app.ocnotes.ui.list.CategoryCount
import app.ocnotes.ui.login.LoginScreen
import app.ocnotes.ui.login.LoginViewModel
import app.ocnotes.ui.settings.SettingsScreen
import app.ocnotes.ui.settings.SettingsViewModel
import app.ocnotes.ui.theme.NotesTheme

// Screen routing. -1 = list, 0 = new note, >0 = edit note by id.
private const val SCREEN_LIST = -1L
private const val SCREEN_NEW = 0L

class MainActivity : ComponentActivity() {

    private val repository get() = (application as OcnotesApplication).container.repository
    private val settingsStore get() = (application as OcnotesApplication).container.settingsStore

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModel.factory(repository)
    }
    private val notesViewModel: NotesViewModel by viewModels {
        NotesViewModel.factory(repository)
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.factory(settingsStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val launchNoteId = if (savedInstanceState == null) {
            intent?.getLongExtra("noteId", -1L) ?: -1L
        } else {
            -1L
        }
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
            val darkTheme = when (settings.theme) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            LaunchedEffect(settings.preventScreenshots) {
                if (settings.preventScreenshots) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
            NotesTheme(darkTheme = darkTheme) {
                val session by repository.session.collectAsStateWithLifecycle(initialValue = null)

                if (session == null) {
                    val state by loginViewModel.state.collectAsStateWithLifecycle()
                    LoginScreen(
                        state = state,
                        onServerUrlChange = loginViewModel::onServerUrlChange,
                        onUsernameChange = loginViewModel::onUsernameChange,
                        onTokenChange = loginViewModel::onTokenChange,
                        onLogin = loginViewModel::login,
                    )
                } else {
                    AppContent(initialNoteId = launchNoteId)
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun AppContent(initialNoteId: Long = -1L) {
        val settings by settingsViewModel.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
        val categories by notesViewModel.categories.collectAsStateWithLifecycle()
        val counts by notesViewModel.counts.collectAsStateWithLifecycle()
        val categoryNames = categories.map { it.name }

        var screen by rememberSaveable { mutableStateOf(if (initialNoteId > 0) initialNoteId else SCREEN_LIST) }
        var editorKey by rememberSaveable { mutableStateOf(0L) }
        var showSettings by rememberSaveable { mutableStateOf(false) }

        BackHandler(enabled = showSettings || screen != SCREEN_LIST) {
            if (showSettings) {
                showSettings = false
            } else {
                screen = SCREEN_LIST
            }
        }

        LaunchedEffect(settings.syncIntervalMinutes, settings.backgroundSync, settings.wifiOnly) {
            SyncScheduler.schedule(
                context = this@MainActivity,
                minutes = settings.syncIntervalMinutes.toLong(),
                enabled = settings.backgroundSync,
                wifiOnly = settings.wifiOnly,
            )
        }

        if (showSettings) {
            SettingsScreen(viewModel = settingsViewModel, onBack = { showSettings = false })
        } else {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    val forward = targetState > initialState
                    (slideInHorizontally(tween(250)) { w -> if (forward) w else -w } + fadeIn(tween(250))) togetherWith
                        (slideOutHorizontally(tween(250)) { w -> if (forward) -w else w } + fadeOut(tween(250)))
                },
                label = "note-navigation",
            ) { target ->
                when {
                    target == SCREEN_NEW -> NoteEditorScreen(
                        viewModel = viewModel(key = "editor-$editorKey") {
                            NoteEditorViewModel(repository, null, startInEdit = settings.directEditing)
                        },
                        categories = categoryNames,
                        monospace = settings.monospace,
                        fontSizeSp = settings.fontSizeSp,
                        styled = settings.noteOpenMode == NoteOpenMode.RICH,
                        keepScreenOn = settings.keepScreenOn,
                        onClose = { screen = SCREEN_LIST },
                    )
                    target > 0L -> NoteEditorScreen(
                        viewModel = viewModel(key = "editor-$editorKey") {
                            NoteEditorViewModel(repository, target, startInEdit = settings.directEditing)
                        },
                        categories = categoryNames,
                        monospace = settings.monospace,
                        fontSizeSp = settings.fontSizeSp,
                        styled = settings.noteOpenMode == NoteOpenMode.RICH,
                        keepScreenOn = settings.keepScreenOn,
                        onClose = { screen = SCREEN_LIST },
                    )
                    else -> {
                        val notes by notesViewModel.notes.collectAsStateWithLifecycle()
                        val scope by notesViewModel.scopeState.collectAsStateWithLifecycle()
                        val syncing by notesViewModel.syncing.collectAsStateWithLifecycle()
                        val syncError by notesViewModel.syncError.collectAsStateWithLifecycle()
                        val query by notesViewModel.query.collectAsStateWithLifecycle()
                        val session by repository.session.collectAsStateWithLifecycle(initialValue = null)
                        val userInitial = session?.username?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                        NotesScreen(
                            notes = notes,
                            scope = scope,
                            counts = counts,
                            categories = categories,
                            syncing = syncing,
                            gridView = settings.gridView,
                            swipeActions = settings.swipeActions,
                            syncError = syncError,
                            userInitial = userInitial,
                            query = query,
                            onQueryChange = notesViewModel::setQuery,
                            onSelectScope = notesViewModel::selectScope,
                            onToggleFavorite = notesViewModel::toggleFavorite,
                            onDeleteNote = notesViewModel::deleteNote,
                            onNoteClick = {
                                editorKey++
                                screen = it.id
                            },
                            onNewNote = {
                                editorKey++
                                screen = SCREEN_NEW
                            },
                            onSync = notesViewModel::sync,
                            onLogout = notesViewModel::logout,
                            onOpenSettings = { showSettings = true },
                        )
                    }
                }
            }
        }
    }
}
