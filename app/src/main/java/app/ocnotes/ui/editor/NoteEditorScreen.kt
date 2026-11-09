package app.ocnotes.ui.editor

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ocnotes.MainActivity
import app.ocnotes.R
import app.ocnotes.ui.theme.FavoriteStar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel,
    categories: List<String>,
    monospace: Boolean,
    fontSizeSp: Float,
    styled: Boolean,
    keepScreenOn: Boolean,
    onClose: () -> Unit,
) {
    val title by viewModel.title.collectAsStateWithLifecycle()
    val content by viewModel.content.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val favorite by viewModel.favorite.collectAsStateWithLifecycle()
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val searchActive by viewModel.searchActive.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val closed by viewModel.closed.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val errorText = error

    val context = LocalContext.current
    val view = LocalView.current
    LaunchedEffect(closed) { if (closed) onClose() }
    // Back (button or gesture) saves before leaving, so edits are never lost.
    BackHandler { viewModel.persist(close = true) }
    DisposableEffect(keepScreenOn) {
        view.keepScreenOn = keepScreenOn
        onDispose { view.keepScreenOn = false }
    }

    var showRename by remember { mutableStateOf(false) }
    var showCategory by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    val scheme = MaterialTheme.colorScheme
    val style = MarkdownStyle(
        linkColor = scheme.primary,
        codeBackground = scheme.surfaceVariant,
        codeColor = scheme.onSurfaceVariant,
        baseFontSize = fontSizeSp.sp,
        highlightColor = scheme.primary.copy(alpha = 0.30f),
    )
    val transformation: VisualTransformation = remember(style, searchQuery, styled) {
        if (styled) MarkdownVisualTransformation(style, searchQuery) else VisualTransformation.None
    }
    val textStyle = TextStyle(
        fontSize = fontSizeSp.sp,
        fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
        lineHeight = TextUnit.Unspecified,
        color = scheme.onSurface,
    )

    Scaffold(
        topBar = {
            EditorTopBar(
                title = title.ifBlank { if (viewModel.isNew) "Nueva nota" else "Sin título" },
                category = category,
                favorite = favorite,
                mode = mode,
                searchActive = searchActive,
                canFavorite = !viewModel.isNew,
                onBack = { viewModel.persist(close = true) },
                onSearch = { viewModel.setSearchActive(!searchActive) },
                onToggleMode = viewModel::toggleMode,
                onFavorite = viewModel::toggleFavorite,
                onRename = { showRename = true },
                onCategory = { showCategory = true },
                onShare = { shareNote(context, viewModel.shareText()) },
                onDelete = { showDelete = true },
                onCancel = onClose,
                onSave = { viewModel.persist() },
                onPin = {
                    viewModel.savedNoteId()?.let { id -> pinNoteToHome(context, id, title) }
                },
                canPin = viewModel.savedNoteId() != null,
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (searchActive) {
                SearchBar(query = searchQuery, onQueryChange = viewModel::setSearchQuery, onClose = { viewModel.setSearchActive(false) })
            }
            if (errorText != null) {
                Text(
                    text = errorText,
                    color = scheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            Box(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                if (mode == EditorMode.VIEW) {
                    SelectionContainer {
                        Text(
                            text = if (styled) markdownAnnotatedString(content, style, searchQuery) else androidx.compose.ui.text.AnnotatedString(content),
                            style = textStyle,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 12.dp),
                        )
                    }
                } else {
                    BasicTextField(
                        value = content,
                        onValueChange = viewModel::onContentChange,
                        textStyle = textStyle,
                        visualTransformation = transformation,
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(scheme.primary),
                        modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
                        decorationBox = { inner ->
                            if (content.isEmpty()) {
                                Text(
                                    "Escribe en Markdown…",
                                    style = textStyle,
                                    color = scheme.onSurfaceVariant,
                                )
                            }
                            inner()
                        },
                    )
                }
            }
        }
    }

    if (showRename) {
        RenameDialog(
            initial = title,
            onDismiss = { showRename = false },
            onConfirm = { value ->
                viewModel.onTitleChange(value)
                viewModel.persist()
                showRename = false
            },
        )
    }
    if (showCategory) {
        CategoryDialog(
            initial = category,
            categories = categories,
            onDismiss = { showCategory = false },
            onConfirm = { value ->
                viewModel.setCategory(value)
                viewModel.persist()
                showCategory = false
            },
        )
    }
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Borrar nota") },
            text = { Text("¿Seguro que quieres borrar esta nota?") },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    viewModel.delete()
                }) { Text("Borrar", color = scheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun EditorTopBar(
    title: String,
    category: String,
    favorite: Boolean,
    mode: EditorMode,
    searchActive: Boolean,
    canFavorite: Boolean,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onToggleMode: () -> Unit,
    onFavorite: () -> Unit,
    onRename: () -> Unit,
    onCategory: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onPin: () -> Unit,
    canPin: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (category.isNotBlank()) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconButton(onClick = onSearch, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Filled.Search, contentDescription = "Buscar en la nota")
        }
        IconButton(onClick = onToggleMode, modifier = Modifier.size(40.dp)) {
            Icon(
                Icons.Outlined.Visibility,
                contentDescription = "Vista previa",
                tint = if (mode == EditorMode.VIEW) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        if (canFavorite) {
            IconButton(onClick = onFavorite, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorita",
                    tint = if (favorite) FavoriteStar else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onRename, modifier = Modifier.size(40.dp)) {
            Text("T", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        OverflowMenu(
            onRename = onRename,
            onCategory = onCategory,
            onShare = onShare,
            onDelete = onDelete,
            onCancel = onCancel,
            onSave = onSave,
            onPin = onPin,
            canPin = canPin,
            canFavorite = canFavorite,
            onFavorite = onFavorite,
        )
    }
}

@Composable
private fun OverflowMenu(
    onRename: () -> Unit,
    onCategory: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onPin: () -> Unit,
    canPin: Boolean,
    canFavorite: Boolean,
    onFavorite: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Más")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text("Guardar") }, onClick = { open = false; onSave() })
            DropdownMenuItem(text = { Text("Cambiar nombre") }, onClick = { open = false; onRename() })
            DropdownMenuItem(text = { Text("Categoría") }, onClick = { open = false; onCategory() })
            DropdownMenuItem(text = { Text("Compartir") }, onClick = { open = false; onShare() })
            DropdownMenuItem(text = { Text("Mover a categoría") }, onClick = { open = false; onCategory() })
            if (canPin) {
                DropdownMenuItem(text = { Text("Acoplar a la pantalla de inicio") }, onClick = { open = false; onPin() })
            }
            if (canFavorite) {
                DropdownMenuItem(text = { Text("Favorita") }, onClick = { open = false; onFavorite() })
            }
            DropdownMenuItem(text = { Text("Cancelar") }, onClick = { open = false; onCancel() })
            DropdownMenuItem(text = { Text("Borrar") }, onClick = { open = false; onDelete() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        placeholder = { Text("Buscar en esta nota") },
        singleLine = true,
        shape = RoundedCornerShape(26.dp),
        trailingIcon = {
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Cerrar búsqueda") }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun RenameDialog(initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar nombre") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Título") },
                singleLine = true,
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(value) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun CategoryDialog(
    initial: String,
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Categoría") },
        text = {
            Column {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Categoría") },
                    singleLine = true,
                )
                if (categories.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Existentes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(
                        Modifier.heightIn(max = 160.dp).verticalScroll(rememberScrollState()),
                    ) {
                        categories.forEach { c ->
                            AssistChip(
                                onClick = { value = c },
                                label = { Text(c) },
                                modifier = Modifier.padding(vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(value) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

private fun shareNote(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Compartir nota"))
}

private fun pinNoteToHome(context: Context, noteId: Long, title: String) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val intent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        putExtra("noteId", noteId)
    }
    val shortcut = ShortcutInfoCompat.Builder(context, "note-$noteId")
        .setShortLabel(title.ifBlank { "Nota" })
        .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
        .setIntent(intent)
        .build()
    ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
}
