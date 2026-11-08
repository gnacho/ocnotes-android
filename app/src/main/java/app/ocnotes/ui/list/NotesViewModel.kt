package app.ocnotes.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.ocnotes.data.local.NoteEntity
import app.ocnotes.data.repo.NotesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface NotesScope {
    data object All : NotesScope
    data object Favorites : NotesScope
}

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(private val repository: NotesRepository) : ViewModel() {
    private val scope = MutableStateFlow<NotesScope>(NotesScope.All)
    private val _query = MutableStateFlow("")
    private val _syncing = MutableStateFlow(false)
    private val _syncError = MutableStateFlow<String?>(null)

    init {
        // Sync on open: initial when empty, incremental otherwise.
        sync()
    }

    val scopeState: StateFlow<NotesScope> = scope
    val query: StateFlow<String> = _query
    val syncing: StateFlow<Boolean> = _syncing
    val syncError: StateFlow<String?> = _syncError

    val notes: StateFlow<List<NoteEntity>> = combine(scope, _query) { s, q -> s to q }
        .flatMapLatest { (s, q) ->
            val source = when (s) {
                NotesScope.All -> repository.observeNotes()
                NotesScope.Favorites -> repository.observeFavorites()
            }
            source.map { list -> if (q.isBlank()) list else list.filter { matches(it, q) } }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun selectScope(s: NotesScope) {
        scope.value = s
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    private fun matches(note: NoteEntity, query: String): Boolean {
        val needle = query.trim()
        return note.title.contains(needle, ignoreCase = true) ||
            note.content.contains(needle, ignoreCase = true) ||
            note.category.contains(needle, ignoreCase = true)
    }

    fun toggleFavorite(note: NoteEntity) {
        viewModelScope.launch { repository.setFavorite(note.id, !note.favorite) }
    }

    fun sync() {
        viewModelScope.launch {
            _syncing.value = true
            try {
                repository.sync()
                _syncError.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // A failed sync (offline, 4xx/5xx) must never crash the app and
                // must keep the local cache: surface it and retry on next sync.
                _syncError.value = e.message ?: "Sync failed"
            } finally {
                _syncing.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
    }

    companion object {
        fun factory(repository: NotesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    NotesViewModel(repository) as T
            }
    }
}
