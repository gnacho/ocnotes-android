package app.ocnotes.ui.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import app.ocnotes.data.local.NoteEntity
import app.ocnotes.ui.theme.FavoriteStar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notes: List<NoteEntity>,
    scope: NotesScope,
    counts: NotesCounts,
    categories: List<CategoryCount>,
    syncing: Boolean,
    gridView: Boolean = false,
    swipeActions: Boolean = true,
    syncError: String? = null,
    userInitial: String = "?",
    query: String,
    onQueryChange: (String) -> Unit,
    onSelectScope: (NotesScope) -> Unit,
    onToggleFavorite: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onNoteClick: (NoteEntity) -> Unit,
    onNewNote: () -> Unit,
    onSync: () -> Unit,
    onLogout: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val entries = remember(notes) { buildNoteList(notes) }
    var drawerOpen by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = drawerOpen) { drawerOpen = false }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                NotesTopBar(
                    query = query,
                    onQueryChange = onQueryChange,
                    userInitial = userInitial,
                    onMenu = { drawerOpen = true },
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNewNote,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Nueva nota")
                }
            },
        ) { padding ->
            PullToRefreshBox(
                isRefreshing = syncing,
                onRefresh = onSync,
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                when {
                    entries.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (query.isBlank()) "Sin notas" else "Sin resultados",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    gridView -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (syncError != null) {
                            item { SyncBanner(syncError) }
                        }
                        items(entries.filterIsInstance<NoteListEntry.Card>(), key = { it.note.id }) { entry ->
                            GridNoteCard(
                                note = entry.note,
                                onToggleFavorite = { onToggleFavorite(entry.note) },
                                onClick = { onNoteClick(entry.note) },
                            )
                        }
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (syncError != null) {
                            item { SyncBanner(syncError) }
                        }
                        items(entries, key = { keyOf(it) }) { entry ->
                            when (entry) {
                                is NoteListEntry.Header -> SectionHeader(entry.label)
                                is NoteListEntry.Card -> {
                                    if (swipeActions) {
                                        SwipeableNoteCard(
                                            note = entry.note,
                                            onToggleFavorite = { onToggleFavorite(entry.note) },
                                            onDelete = { onDeleteNote(entry.note) },
                                            onClick = { onNoteClick(entry.note) },
                                        )
                                    } else {
                                        NoteCard(
                                            note = entry.note,
                                            onToggleFavorite = { onToggleFavorite(entry.note) },
                                            onClick = { onNoteClick(entry.note) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = drawerOpen,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { drawerOpen = false },
            )
        }

        AnimatedVisibility(
            visible = drawerOpen,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it }),
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            ModalDrawerSheet(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.8f)) {
                var showAbout by remember { mutableStateOf(false) }
                fun pick(s: NotesScope) {
                    onSelectScope(s)
                    drawerOpen = false
                }
                Column(Modifier.fillMaxSize().padding(vertical = 16.dp)) {
                    Text(
                        "ocnotes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { drawerOpen = false }
                            .padding(horizontal = 16.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    DrawerItem("Todas las notas", Icons.Outlined.Schedule, counts.total, scope == NotesScope.All) { pick(NotesScope.All) }
                    DrawerItem("Favoritos", Icons.Outlined.StarBorder, counts.favorites, scope == NotesScope.Favorites) { pick(NotesScope.Favorites) }
                    DrawerItem("Sin categoría", Icons.Outlined.Folder, counts.uncategorized, scope == NotesScope.Uncategorized) { pick(NotesScope.Uncategorized) }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        categories.forEach { c ->
                            DrawerItem(
                                c.name,
                                Icons.Outlined.Folder,
                                c.count,
                                scope == NotesScope.Category(c.name),
                            ) { pick(NotesScope.Category(c.name)) }
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    DrawerItem("Ajustes", Icons.Filled.Settings, null, false) {
                        drawerOpen = false
                        onOpenSettings()
                    }
                    DrawerItem("Acerca de", Icons.Outlined.Info, null, false) { showAbout = true }
                    TextButton(onClick = onLogout, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text("Cerrar sesión")
                    }
                }
                if (showAbout) {
                    AlertDialog(
                        onDismissRequest = { showAbout = false },
                        title = { Text("ocnotes") },
                        text = { Text("Cliente de notas para OpenCloud / Nextcloud.\nVersión ${app.ocnotes.BuildConfig.VERSION_NAME}") },
                        confirmButton = { TextButton(onClick = { showAbout = false }) { Text("Cerrar") } },
                    )
                }
            }
        }
    }
}

private fun keyOf(entry: NoteListEntry): Any = when (entry) {
    is NoteListEntry.Header -> "h:${entry.label}"
    is NoteListEntry.Card -> "n:${entry.note.id}"
}

@Composable
private fun SyncBanner(message: String) {
    Text(
        text = "Sync failed: $message",
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
    )
}

@Composable
private fun NotesTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    userInitial: String,
    onMenu: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenu) {
            Icon(Icons.Filled.Menu, contentDescription = "Menú")
        }
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("Buscar en todas las notas", maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            singleLine = true,
            shape = RoundedCornerShape(26.dp),
            trailingIcon = {
                if (query.isEmpty()) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda")
                    }
                }
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = userInitial,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Text(
        text = label,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 2.dp),
    )
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    count: Int?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (count != null) {
                    Text(
                        text = count.toString(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableNoteCard(
    note: NoteEntity,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    var done by remember(note.id) { mutableStateOf(false) }
    val state = rememberSwipeToDismissBoxState()
    LaunchedEffect(state.currentValue) {
        if (done) return@LaunchedEffect
        when (state.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                done = true
                onToggleFavorite()
            }
            SwipeToDismissBoxValue.EndToStart -> {
                done = true
                onDelete()
            }
            SwipeToDismissBoxValue.Settled -> Unit
        }
    }
    key(note.id, done) {
        SwipeToDismissBox(
            state = state,
            enableDismissFromStartToEnd = !done,
            enableDismissFromEndToStart = !done,
            backgroundContent = {
                val bg = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(bg)
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                        Alignment.CenterEnd
                    } else {
                        Alignment.CenterStart
                    },
                ) {
                    Text(
                        if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) "Borrar" else "Favorita",
                    )
                }
            },
        ) {
            NoteCard(note = note, onToggleFavorite = onToggleFavorite, onClick = onClick)
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (note.favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorita",
                    tint = if (note.favorite) FavoriteStar else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(2.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = note.title.ifBlank { "Nueva nota" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatCardDate(note.modified),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val preview = plainPreview(note.content)
                if (preview.isNotEmpty() || note.category.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = preview,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (note.category.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            CategoryChip(note.category)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridNoteCard(
    note: NoteEntity,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = note.title.ifBlank { "Nueva nota" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (note.favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorita",
                        tint = if (note.favorite) FavoriteStar else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            val preview = plainPreview(note.content)
            if (preview.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatCardDate(note.modified),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (note.category.isNotBlank()) {
                    CategoryChip(note.category)
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
