package app.ocnotes.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

data class MarkdownStyle(
    val linkColor: Color,
    val codeBackground: Color,
    val codeColor: Color,
    val baseFontSize: TextUnit,
    val highlightColor: Color,
) {
    val base: TextUnit get() = if (baseFontSize.isSpecified) baseFontSize else 16.sp
}

private fun headingMultiplier(level: Int): Float = when (level) {
    1 -> 1.75f
    2 -> 1.5f
    3 -> 1.3f
    4 -> 1.18f
    5 -> 1.08f
    else -> 1.0f
}

// Builds the styled (and optionally search-highlighted) string. Shared by the
// edit field and the read-only view so both render identically.
fun markdownAnnotatedString(
    raw: String,
    style: MarkdownStyle,
    highlightQuery: String = "",
): AnnotatedString {
    val builder = AnnotatedString.Builder(raw)
    for (span in parseMarkdownSpans(raw)) {
        val spanStyle = when (val s = span.style) {
            is MdStyle.Heading -> SpanStyle(
                fontSize = style.base * headingMultiplier(s.level),
                fontWeight = FontWeight.Bold,
            )
            MdStyle.Bold -> SpanStyle(fontWeight = FontWeight.Bold)
            MdStyle.Italic -> SpanStyle(fontStyle = FontStyle.Italic)
            MdStyle.BoldItalic -> SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
            MdStyle.Strike -> SpanStyle(textDecoration = TextDecoration.LineThrough)
            MdStyle.Code -> SpanStyle(
                fontFamily = FontFamily.Monospace,
                background = style.codeBackground,
                color = style.codeColor,
            )
            MdStyle.Link -> SpanStyle(color = style.linkColor, textDecoration = TextDecoration.Underline)
        }
        builder.addStyle(spanStyle, span.start, span.end)
    }
    val q = highlightQuery.trim()
    if (q.isNotEmpty()) {
        var i = raw.indexOf(q, 0, ignoreCase = true)
        while (i >= 0) {
            builder.addStyle(SpanStyle(background = style.highlightColor), i, i + q.length)
            i = raw.indexOf(q, i + q.length, ignoreCase = true)
        }
    }
    return builder.toAnnotatedString()
}

class MarkdownVisualTransformation(
    private val style: MarkdownStyle,
    private val query: String = "",
) : VisualTransformation {
    private var cachedText: String? = null
    private var cachedQuery: String? = null
    private var cached: AnnotatedString? = null

    override fun filter(text: AnnotatedString): TransformedText {
        val annotated = if (text.text == cachedText && query == cachedQuery) {
            cached!!
        } else {
            markdownAnnotatedString(text.text, style, query).also {
                cachedText = text.text
                cachedQuery = query
                cached = it
            }
        }
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}
