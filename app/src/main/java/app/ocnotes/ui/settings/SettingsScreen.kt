package app.ocnotes.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Swipe
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ocnotes.data.settings.FontSizeOption
import app.ocnotes.data.settings.NoteOpenMode
import app.ocnotes.data.settings.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit, onReauthenticate: () -> Unit) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader("Sincronización")
            SwitchRow(Icons.Outlined.Wifi, "Sincronizar solo sobre Wi-Fi", null, settings.wifiOnly) {
                viewModel.setWifiOnly(it)
            }
            SwitchRow(Icons.Outlined.Autorenew, "Sincronización en segundo plano", null, settings.backgroundSync) {
                viewModel.setBackgroundSync(it)
            }

            HorizontalDivider()
            SectionHeader("Apariencia y comportamiento")
            ValueRow(
                Icons.Outlined.DarkMode,
                "Tema",
                when (settings.theme) {
                    ThemeMode.SYSTEM -> "Predeterminado del sistema"
                    ThemeMode.LIGHT -> "Claro"
                    ThemeMode.DARK -> "Oscuro"
                },
            ) { dialog = "theme" }
            ValueRow(
                Icons.Outlined.Edit,
                "Comportamiento de apertura de las notas",
                if (settings.noteOpenMode == NoteOpenMode.PLAIN) "Modo de edición en texto plano" else "Editor con estilos",
            ) { dialog = "openmode" }
            SwitchRow(
                Icons.Outlined.TextFields,
                "Edición directa",
                "Si está desactivada, la nota se abre en vista previa.",
                settings.directEditing,
            ) { viewModel.setDirectEditing(it) }
            SwitchRow(Icons.Outlined.TextFields, "Tipo de letra monoespaciado", null, settings.monospace) {
                viewModel.setMonospace(it)
            }
            ValueRow(
                Icons.Outlined.FormatSize,
                "Tamaño de fuente",
                when (settings.fontSize) {
                    FontSizeOption.SMALL -> "Pequeño"
                    FontSizeOption.MEDIUM -> "Mediano"
                    FontSizeOption.LARGE -> "Grande"
                },
            ) { dialog = "fontsize" }
            SwitchRow(Icons.Outlined.LightMode, "Mantener la pantalla encendida", "Al ver o editar una nota", settings.keepScreenOn) {
                viewModel.setKeepScreenOn(it)
            }
            SwitchRow(
                Icons.Outlined.GridView,
                "Vista en cuadrícula",
                null,
                settings.gridView,
            ) { viewModel.setGridView(it) }
            SwitchRow(
                Icons.Outlined.Swipe,
                "Swipe actions",
                "Marcar como favorita o borrar con un deslizamiento",
                settings.swipeActions,
            ) { viewModel.setSwipeActions(it) }

            HorizontalDivider()
            SectionHeader("Seguridad")
            SwitchRow(Icons.Outlined.Block, "Evitar la captura de pantalla", null, settings.preventScreenshots) {
                viewModel.setPreventScreenshots(it)
            }

            HorizontalDivider()
            SectionHeader("Cuenta")
            ValueRow(
                Icons.Outlined.Key,
                "Actualizar token",
                "Sustituirlo sin borrar las notas",
            ) { onReauthenticate() }
            Spacer(Modifier.padding(bottom = 16.dp))
        }
    }

    when (dialog) {
        "theme" -> OptionDialog(
            title = "Tema",
            options = listOf(ThemeMode.SYSTEM to "Predeterminado del sistema", ThemeMode.LIGHT to "Claro", ThemeMode.DARK to "Oscuro"),
            selected = settings.theme,
            onDismiss = { dialog = null },
            onPick = { viewModel.setTheme(it); dialog = null },
        )
        "openmode" -> OptionDialog(
            title = "Comportamiento de apertura",
            options = listOf(NoteOpenMode.PLAIN to "Modo de edición en texto plano", NoteOpenMode.RICH to "Editor con estilos"),
            selected = settings.noteOpenMode,
            onDismiss = { dialog = null },
            onPick = { viewModel.setNoteOpenMode(it); dialog = null },
        )
        "fontsize" -> OptionDialog(
            title = "Tamaño de fuente",
            options = listOf(FontSizeOption.SMALL to "Pequeño", FontSizeOption.MEDIUM to "Mediano", FontSizeOption.LARGE to "Grande"),
            selected = settings.fontSize,
            onDismiss = { dialog = null },
            onPick = { viewModel.setFontSize(it); dialog = null },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}

@Composable
private fun ValueRow(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun <T> OptionDialog(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onDismiss: () -> Unit,
    onPick: (T) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    TextButton(onClick = { onPick(value) }, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                label,
                                modifier = Modifier.weight(1f),
                                color = if (value == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                            if (value == selected) Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
