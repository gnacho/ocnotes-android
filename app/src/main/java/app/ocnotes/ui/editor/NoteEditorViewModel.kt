package app.ocnotes.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ocnotes.data.repo.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EditorMode { VIEW, EDIT }

// Editor for one note. `noteId == null` is the "new note" mode; it is created on
// the first persist that has some content. Edits are persisted on back/dialogs
// and by the explicit "Guardar" action, so changing the category or the text is
// never silently lost.
class NoteEditorViewModel(
    private val repository: NotesRepository,
    private val noteId: Long?,
    startInEdit: Boolean = true,
) : ViewModel() {
    val isNew: Boolean = noteId == null
    private var currentId: Long? = noteId

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

    fun savedNoteId(): Long? = currentId

    fun reload() {
        val id = currentId ?: return
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
        _error.value = null
    }

    fun toggleFavorite() {
        val id = currentId ?: return
        viewModelScope.launch {
            _favorite.update { !it }
            repository.setFavorite(id, _favorite.value)
        }
    }

    fun save() = persist(close = true)

    // Persists the current title/content/category. For a new note it creates it
    // on the first non-empty persist. `close` navigates back once stored.
    fun persist(close: Boolean = false) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            try {
                val id = currentId
                if (id == null) {
                    if (_title.value.isBlank() && _content.value.isBlank()) {
                        if (close) _closed.value = true
                        return@launch
                    }
                    val created = repository.createNote(_title.value, _content.value, _category.value)
                    currentId = created.id
                } else {
                    repository.updateNote(id, _title.value, _content.value, _category.value)
                    runCatching { repository.pushPending() }
                }
                if (close) _closed.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Save failed"
            } finally {
                _busy.value = false
            }
        }
    }

    fun delete() {
        val id = currentId ?: return
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
