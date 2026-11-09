package app.ocnotes.ui.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

private val IMG_SRC = Regex("<img[^>]*\\bsrc\\s*=\\s*\"([^\"]+)\"", RegexOption.IGNORE_CASE)
private val A_HREF = Regex("<a[^>]*\\bhref\\s*=\\s*\"([^\"]+)\"", RegexOption.IGNORE_CASE)

// Read-only note view: markdown-styled text per line, plus real images for raw
// HTML <img> (e.g. a YouTube thumbnail) and clickable <a href> links.
@Composable
fun MarkdownContentView(
    content: String,
    style: MarkdownStyle,
    textStyle: TextStyle,
    searchQuery: String,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.verticalScroll(rememberScrollState())) {
        content.split("\n").forEach { line ->
            val src = IMG_SRC.find(line)?.groupValues?.get(1)
            if (src != null) {
                val href = A_HREF.find(line)?.groupValues?.get(1)
                AsyncImage(
                    model = src,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .width(240.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .then(if (href != null) Modifier.clickable { onOpenLink(href) } else Modifier),
                )
            } else {
                Text(
                    text = markdownAnnotatedString(line.ifEmpty { " " }, style, searchQuery),
                    style = textStyle,
                )
            }
        }
    }
}
