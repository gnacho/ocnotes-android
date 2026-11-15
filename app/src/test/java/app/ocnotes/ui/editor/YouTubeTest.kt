package app.ocnotes.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeTest {

    @Test
    fun `watch url`() {
        assertEquals("dQw4w9WgXcQ", youTubeId("http://www.youtube.com/watch?feature=player_embedded&v=dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", youTubeId("https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=42s"))
        assertEquals("dQw4w9WgXcQ", youTubeId("https://m.youtube.com/watch?v=dQw4w9WgXcQ"))
    }

    @Test
    fun `short and embed forms`() {
        assertEquals("dQw4w9WgXcQ", youTubeId("https://youtu.be/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", youTubeId("https://youtu.be/dQw4w9WgXcQ?t=10"))
        assertEquals("dQw4w9WgXcQ", youTubeId("https://www.youtube.com/embed/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", youTubeId("https://www.youtube.com/shorts/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", youTubeId("https://www.youtube.com/live/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", youTubeId("https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ"))
    }

    @Test
    fun `thumbnail url from the classic embed`() {
        assertEquals("dQw4w9WgXcQ", youTubeId("http://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg"))
        assertEquals("dQw4w9WgXcQ", youTubeId("https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg"))
        assertEquals("dQw4w9WgXcQ", youTubeId("https://i.ytimg.com/vi_webp/dQw4w9WgXcQ/maxresdefault.webp"))
    }

    @Test
    fun `not youtube`() {
        assertNull(youTubeId("https://example.com/watch?v=dQw4w9WgXcQ"))
        assertNull(youTubeId("https://example.com/image.jpg"))
        assertNull(youTubeId("https://www.youtube.com/watch?v=short"))
        assertNull(youTubeId(""))
    }

    @Test
    fun `embed and thumbnail urls`() {
        assertEquals(
            "https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ?rel=0&playsinline=1",
            youTubeEmbedUrl("dQw4w9WgXcQ"),
        )
        assertEquals("https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg", youTubeThumbnailUrl("dQw4w9WgXcQ"))
    }
}
