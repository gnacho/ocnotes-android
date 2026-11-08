package app.ocnotes.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownSpanTest {

    private fun spans(text: String) = parseMarkdownSpans(text)

    @Test
    fun `plain text has no spans`() {
        assertTrue(spans("plain text").isEmpty())
    }

    @Test
    fun `heading styles the whole line`() {
        assertEquals(listOf(MdSpan(0, 7, MdStyle.Heading(1))), spans("# Title"))
        assertEquals(listOf(MdSpan(0, 6, MdStyle.Heading(2))), spans("## Two"))
        assertEquals(listOf(MdSpan(0, 10, MdStyle.Heading(6))), spans("###### Six"))
    }

    @Test
    fun `hash without space is not a heading`() {
        assertTrue(spans("#notaheading").isEmpty())
    }

    @Test
    fun `bold styles the inner text`() {
        assertEquals(listOf(MdSpan(4, 5, MdStyle.Bold)), spans("a **b** c"))
        assertEquals(listOf(MdSpan(2, 6, MdStyle.Bold)), spans("__bold__"))
    }

    @Test
    fun `italic styles the inner text`() {
        assertEquals(listOf(MdSpan(1, 2, MdStyle.Italic)), spans("*i*"))
        assertEquals(listOf(MdSpan(1, 7, MdStyle.Italic)), spans("_italic_"))
    }

    @Test
    fun `bold italic`() {
        assertEquals(listOf(MdSpan(3, 4, MdStyle.BoldItalic)), spans("***x***"))
    }

    @Test
    fun `inline code styles the whole span`() {
        assertEquals(listOf(MdSpan(0, 6, MdStyle.Code)), spans("`code`"))
    }

    @Test
    fun `link styles the text part`() {
        assertEquals(listOf(MdSpan(1, 2, MdStyle.Link)), spans("[t](u)"))
    }

    @Test
    fun `strike styles the inner text`() {
        assertEquals(listOf(MdSpan(2, 3, MdStyle.Strike)), spans("~~s~~"))
    }

    @Test
    fun `heading can also contain inline styles`() {
        val result = spans("# **b**")
        assertEquals(
            listOf(MdSpan(0, 7, MdStyle.Heading(1)), MdSpan(4, 5, MdStyle.Bold)),
            result,
        )
    }

    @Test
    fun `line offsets are absolute on multiline text`() {
        val result = spans("# A\nb **x**")
        assertEquals(
            listOf(MdSpan(0, 3, MdStyle.Heading(1)), MdSpan(8, 9, MdStyle.Bold)),
            result,
        )
    }

    @Test
    fun `bold and italic do not overlap`() {
        val result = spans("**a** *b*")
        assertEquals(
            listOf(MdSpan(2, 3, MdStyle.Bold), MdSpan(7, 8, MdStyle.Italic)),
            result,
        )
    }
}
