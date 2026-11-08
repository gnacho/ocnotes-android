package app.ocnotes.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ocnotes.data.repo.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Editor for one note. `noteId == null` is the "new note" mode: content and
// category live only in the ViewModel until the first save creates the note.
class NoteEditorViewModel(
    private val repository: NotesRepository,
    private val noteId: Long?,
) : ViewModel() {
    val isNew: Boolean = noteId == null

    private val _content = MutableStateFlow("")
    private val _category = MutableStateFlow("")
    private val _favorite = MutableStateFlow(false)
    private val _busy = MutableStateFlow(false)
    private val _closed = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val content: StateFlow<String> = _content
    val category: StateFlow<String> = _category
    val favorite: StateFlow<Boolean> = _favorite
    val busy: StateFlow<Boolean> = _busy
    val closed: StateFlow<Boolean> = _closed
    val error: StateFlow<String?> = _error

    init {
        reload()
    }

    // Re-seeds the editable fields from the store. Called on creation and again
    // every time the editor opens, so reopening a note shows its latest content.
    fun reload() {
        val id = noteId ?: return
        viewModelScope.launch {
            repository.getNote(id)?.let { note ->
                _content.value = note.content
                _category.value = note.category
                _favorite.value = note.favorite
            }
        }
    }

    fun onContentChange(value: String) {
        _content.value = value
        _error.value = null
    }

    fun onCategoryChange(value: String) {
        _category.value = value
        _error.value = null
    }

    fun toggleFavorite() {
        val id = noteId ?: return
        viewModelScope.launch {
            _favorite.update { !it }
            repository.setFavorite(id, _favorite.value)
        }
    }

    fun save() {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            try {
                if (noteId == null) {
                    repository.createNote(_content.value, _category.value)
                    _closed.value = true
                } else {
                    repository.updateNote(noteId, _content.value, _category.value)
                    _closed.value = true
                    // Push promptly. If it fails (offline), the local dirty edit
                    // stays queued and is sent on the next sync.
                    runCatching { repository.pushPending() }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Save failed"
            } finally {
                _busy.value = false
            }
        }
    }

    fun delete() {
        val id = noteId ?: return
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            try {
                repository.deleteNote(id)
                _closed.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Delete failed"
            } finally {
                _busy.value = false
            }
        }
    }
}
