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

// Turns raw markdown into an AnnotatedString with the styles applied, without
// changing any character, so the offset mapping is the identity and the cursor
// behaves exactly like a plain field.
class MarkdownVisualTransformation(
    private val linkColor: Color,
    private val codeBackground: Color,
    private val codeColor: Color,
    private val baseFontSize: TextUnit,
) : VisualTransformation {
    private var cachedText: String? = null
    private var cached: AnnotatedString? = null

    private val base: TextUnit = if (baseFontSize.isSpecified) baseFontSize else 16.sp

    override fun filter(text: AnnotatedString): TransformedText {
        val annotated = if (text.text == cachedText) {
            cached!!
        } else {
            build(text.text).also {
                cachedText = text.text
                cached = it
            }
        }
        return TransformedText(annotated, OffsetMapping.Identity)
    }

    private fun headingSize(level: Int): TextUnit = base * when (level) {
        1 -> 1.75f
        2 -> 1.5f
        3 -> 1.3f
        4 -> 1.18f
        5 -> 1.08f
        else -> 1.0f
    }

    private fun build(raw: String): AnnotatedString {
        val builder = AnnotatedString.Builder(raw)
        for (span in parseMarkdownSpans(raw)) {
            val style = when (val s = span.style) {
                is MdStyle.Heading -> SpanStyle(
                    fontSize = headingSize(s.level),
                    fontWeight = FontWeight.Bold,
                )
                MdStyle.Bold -> SpanStyle(fontWeight = FontWeight.Bold)
                MdStyle.Italic -> SpanStyle(fontStyle = FontStyle.Italic)
                MdStyle.BoldItalic -> SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
                MdStyle.Strike -> SpanStyle(textDecoration = TextDecoration.LineThrough)
                MdStyle.Code -> SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = codeBackground,
                    color = codeColor,
                )
                MdStyle.Link -> SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
            }
            builder.addStyle(style, span.start, span.end)
        }
        return builder.toAnnotatedString()
    }
}
