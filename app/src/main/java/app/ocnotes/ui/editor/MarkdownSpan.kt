package app.ocnotes.ui.editor

// Pure markdown span detection: no Compose, no Android. Maps a source string to
// styled ranges (character offsets) so the editor can render them live. Only the
// constructs that can be shown inside a text field are handled; block structure
// (lists, images, tables) stays as raw text.
sealed interface MdStyle {
    data class Heading(val level: Int) : MdStyle
    data object Bold : MdStyle
    data object Italic : MdStyle
    data object BoldItalic : MdStyle
    data object Strike : MdStyle
    data object Code : MdStyle
    data object Link : MdStyle
}

data class MdSpan(val start: Int, val end: Int, val style: MdStyle)

private val HEADING = Regex("^(#{1,6})\\s")
private val CODE = Regex("`([^`\\n]+)`")
private val LINK = Regex("\\[([^\\]\\n]+)]\\(([^)\\n]*)\\)")
private val BOLD_ITALIC = Regex("\\*\\*\\*([^*\\n]+)\\*\\*\\*")
private val BOLD = Regex("(\\*\\*|__)(.+?)\\1")
private val ITALIC = Regex("(\\*|_)([^*_\\n]+)\\1")
private val STRIKE = Regex("~~([^~\\n]+)~~")

// Returns non-overlapping spans (headings may overlap inline spans on purpose, so
// a heading can also contain bold/italic). Inline constructs are matched in
// priority order against a "consumed" mask to avoid double-styling.
fun parseMarkdownSpans(text: String): List<MdSpan> {
    if (text.isEmpty()) return emptyList()
    val spans = mutableListOf<MdSpan>()
    val consumed = BooleanArray(text.length)

    fun isFree(start: Int, end: Int): Boolean {
        if (start < 0 || end > text.length || start >= end) return false
        for (i in start until end) if (consumed[i]) return false
        return true
    }

    fun mark(start: Int, end: Int) {
        for (i in start until end) consumed[i] = true
    }

    // Headings: whole line, including the leading #s.
    var lineStart = 0
    while (lineStart <= text.length) {
        val nl = text.indexOf('\n', lineStart)
        val lineEnd = if (nl == -1) text.length else nl
        val line = text.substring(lineStart, lineEnd)
        HEADING.find(line)?.let { m ->
            spans.add(MdSpan(lineStart, lineEnd, MdStyle.Heading(m.groupValues[1].length)))
        }
        if (nl == -1) break
        lineStart = nl + 1
    }

    fun addWhole(regex: Regex, style: MdStyle, group: Int = 0) {
        var index = 0
        while (index <= text.length) {
            val m = regex.find(text, index) ?: break
            val s = m.range.first
            val e = m.range.last + 1
            if (!isFree(s, e)) {
                // Retry from the next character: a skipped match must not swallow
                // a later valid one (e.g. the `* *` overlap inside `**a** *b*`).
                index = s + 1
                continue
            }
            if (group == 0) {
                spans.add(MdSpan(s, e, style))
            } else {
                val g = m.groups[group]
                if (g == null) {
                    index = e
                    continue
                }
                spans.add(MdSpan(g.range.first, g.range.last + 1, style))
            }
            mark(s, e)
            index = e
        }
    }

    addWhole(CODE, MdStyle.Code)
    addWhole(LINK, MdStyle.Link, group = 1)
    addWhole(BOLD_ITALIC, MdStyle.BoldItalic, group = 1)
    addWhole(BOLD, MdStyle.Bold, group = 2)
    addWhole(ITALIC, MdStyle.Italic, group = 2)
    addWhole(STRIKE, MdStyle.Strike, group = 1)

    return spans
}
