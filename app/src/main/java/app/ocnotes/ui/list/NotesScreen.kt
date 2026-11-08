package app.ocnotes.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ocnotes.data.local.NoteEntity
import app.ocnotes.util.formatRelativeTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notes: List<NoteEntity>,
    scope: NotesScope,
    syncing: Boolean,
    syncError: String? = null,
    onSelectScope: (NotesScope) -> Unit,
    onToggleFavorite: (NoteEntity) -> Unit,
    onNoteClick: (NoteEntity) -> Unit,
    onNewNote: () -> Unit,
    onSync: () -> Unit,
    onLogout: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("ocnotes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                NavigationDrawerItem(
                    label = { Text("All notes") },
                    selected = scope == NotesScope.All,
                    onClick = {
                        onSelectScope(NotesScope.All)
                        coroutineScope.launch { drawerState.close() }
                    },
                )
                NavigationDrawerItem(
                    label = { Text("Favorites") },
                    selected = scope == NotesScope.Favorites,
                    onClick = {
                        onSelectScope(NotesScope.Favorites)
                        coroutineScope.launch { drawerState.close() }
                    },
                )
                Spacer(Modifier.weight(1f))
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        onOpenSettings()
                    },
                )
                TextButton(onClick = onLogout) { Text("Log out") }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (scope == NotesScope.All) "Notes" else "Favorites") },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Text("☰")
                        }
                    },
                    actions = {
                        IconButton(onClick = onSync, enabled = !syncing) {
                            Text(if (syncing) "…" else "↻")
                        }
                    },
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onNewNote,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("New note") },
                )
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                if (syncError != null) {
                    Text(
                        text = "Sync failed: $syncError",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
                if (notes.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No notes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(notes, key = { it.id }) { note ->
                            NoteRow(
                                note = note,
                                onToggleFavorite = { onToggleFavorite(note) },
                                onClick = { onNoteClick(note) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteRow(
    note: NoteEntity,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { "New note" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (note.content.isNotBlank()) {
                    Text(
                        text = note.content.replace('\n', ' '),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (note.favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (note.favorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatRelativeTime(note.modified),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (note.category.isNotBlank()) {
                Spacer(Modifier.width(12.dp))
                Text(
                    text = note.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
