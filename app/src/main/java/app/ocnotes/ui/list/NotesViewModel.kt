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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface NotesScope {
    data object All : NotesScope
    data object Favorites : NotesScope
    data object Uncategorized : NotesScope
    data class Category(val name: String) : NotesScope
}

data class CategoryCount(val name: String, val count: Int)

data class NotesCounts(val total: Int = 0, val favorites: Int = 0, val uncategorized: Int = 0)

fun filterByScope(notes: List<NoteEntity>, scope: NotesScope): List<NoteEntity> = when (scope) {
    NotesScope.All -> notes
    NotesScope.Favorites -> notes.filter { it.favorite }
    NotesScope.Uncategorized -> notes.filter { it.category.isBlank() }
    is NotesScope.Category -> notes.filter { it.category == scope.name }
}

private fun matches(note: NoteEntity, query: String): Boolean {
    val needle = query.trim()
    return note.title.contains(needle, ignoreCase = true) ||
        note.content.contains(needle, ignoreCase = true) ||
        note.category.contains(needle, ignoreCase = true)
}

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(private val repository: NotesRepository) : ViewModel() {
    private val scope = MutableStateFlow<NotesScope>(NotesScope.All)
    private val _query = MutableStateFlow("")
    private val _syncing = MutableStateFlow(false)
    private val _syncError = MutableStateFlow<String?>(null)

    val scopeState: StateFlow<NotesScope> = scope
    val query: StateFlow<String> = _query
    val syncing: StateFlow<Boolean> = _syncing
    val syncError: StateFlow<String?> = _syncError

    private val allNotes: StateFlow<List<NoteEntity>> = repository.observeNotes()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val notes: StateFlow<List<NoteEntity>> = combine(allNotes, scope, _query) { list, s, q ->
        val scoped = filterByScope(list, s)
        if (q.isBlank()) scoped else scoped.filter { matches(it, q) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val categories: StateFlow<List<CategoryCount>> = allNotes.map { list ->
        list.filter { it.category.isNotBlank() }
            .groupBy { it.category }
            .map { (name, notes) -> CategoryCount(name, notes.size) }
            .sortedBy { it.name.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val counts: StateFlow<NotesCounts> = allNotes.map { list ->
        NotesCounts(
            total = list.size,
            favorites = list.count { it.favorite },
            uncategorized = list.count { it.category.isBlank() },
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, NotesCounts())

    fun selectScope(s: NotesScope) {
        scope.value = s
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun toggleFavorite(note: NoteEntity) {
        viewModelScope.launch { repository.setFavorite(note.id, !note.favorite) }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repository.deleteNote(note.id) }
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
