package app.ocnotes.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {
    private val now = 1_700_000_000_000L
    private val nowSeconds = now / 1000

    @Test
    fun `seconds ago is just now`() {
        assertEquals("just now", formatRelativeTime(nowSeconds - 10, now))
    }

    @Test
    fun `minutes ago`() {
        assertEquals("5 min ago", formatRelativeTime(nowSeconds - 300, now))
    }

    @Test
    fun `hours ago`() {
        assertEquals("2 h ago", formatRelativeTime(nowSeconds - 7200, now))
    }

    @Test
    fun `days ago`() {
        assertEquals("3 days ago", formatRelativeTime(nowSeconds - 3 * 86400, now))
    }

    @Test
    fun `zero is empty`() {
        assertEquals("", formatRelativeTime(0, now))
    }
}
