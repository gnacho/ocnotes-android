package app.ocnotes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Favorite star gold, sampled from the reference list.
val FavoriteStar = Color(0xFFF4CB00)

// Dark-first palette: near-black background, subtle card border, light-blue accent.
private val NotesDark = darkColorScheme(
    primary = Color(0xFF97CAF7),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF0D4870),
    onPrimaryContainer = Color(0xFFCDE5FF),
    secondary = Color(0xFFB8C4D4),
    onSecondary = Color(0xFF23303F),
    background = Color(0xFF0E1013),
    onBackground = Color(0xFFE1E4E9),
    surface = Color(0xFF14171C),
    onSurface = Color(0xFFE1E4E9),
    surfaceVariant = Color(0xFF272A2F),
    onSurfaceVariant = Color(0xFF9AA3AD),
    outline = Color(0xFF2E333A),
    outlineVariant = Color(0xFF23272D),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF3B0A12),
)

private val NotesLight = lightColorScheme(
    primary = Color(0xFF1F6FEB),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF00214D),
    secondary = Color(0xFF52606D),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF15181C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF15181C),
    surfaceVariant = Color(0xFFEEF1F4),
    onSurfaceVariant = Color(0xFF5B636C),
    outline = Color(0xFFD5DAE0),
    outlineVariant = Color(0xFFE4E8EC),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

@Composable
fun NotesTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) NotesDark else NotesLight,
        content = content,
    )
}
