package app.ocnotes.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ocnotes.data.repo.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EditorMode { VIEW, EDIT }

// Editor for one note. `noteId == null` is the "new note" mode.
class NoteEditorViewModel(
    private val repository: NotesRepository,
    private val noteId: Long?,
    startInEdit: Boolean = true,
) : ViewModel() {
    val isNew: Boolean = noteId == null

    fun savedNoteId(): Long? = noteId

    private val _title = MutableStateFlow("")
    private val _content = MutableStateFlow("")
    private val _category = MutableStateFlow("")
    private val _favorite = MutableStateFlow(false)
    private val _mode = MutableStateFlow(if (isNew || startInEdit) EditorMode.EDIT else EditorMode.VIEW)
    private val _searchActive = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _busy = MutableStateFlow(false)
    private val _closed = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val title: StateFlow<String> = _title
    val content: StateFlow<String> = _content
    val category: StateFlow<String> = _category
    val favorite: StateFlow<Boolean> = _favorite
    val mode: StateFlow<EditorMode> = _mode
    val searchActive: StateFlow<Boolean> = _searchActive
    val searchQuery: StateFlow<String> = _searchQuery
    val busy: StateFlow<Boolean> = _busy
    val closed: StateFlow<Boolean> = _closed
    val error: StateFlow<String?> = _error

    init {
        reload()
    }

    fun reload() {
        val id = noteId ?: return
        viewModelScope.launch {
            repository.getNote(id)?.let { note ->
                _title.value = note.title
                _content.value = note.content
                _category.value = note.category
                _favorite.value = note.favorite
            }
        }
    }

    fun toggleMode() {
        _mode.value = if (_mode.value == EditorMode.EDIT) EditorMode.VIEW else EditorMode.EDIT
    }

    fun setSearchActive(active: Boolean) {
        _searchActive.value = active
        if (!active) _searchQuery.value = ""
        if (active) _mode.value = EditorMode.VIEW
    }

    fun setSearchQuery(value: String) {
        _searchQuery.value = value
    }

    fun onTitleChange(value: String) {
        _title.value = value
        _error.value = null
    }

    fun onContentChange(value: String) {
        _content.value = value
        _error.value = null
    }

    fun setCategory(value: String) {
        _category.value = value
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
                    repository.createNote(_title.value, _content.value, _category.value)
                    _closed.value = true
                } else {
                    repository.updateNote(noteId, _title.value, _content.value, _category.value)
                    _closed.value = true
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

    fun shareText(): String {
        val t = _title.value.trim()
        return if (t.isEmpty()) _content.value else "$t\n\n${_content.value}"
    }
}
