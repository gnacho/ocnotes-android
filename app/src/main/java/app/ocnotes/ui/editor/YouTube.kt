package app.ocnotes.ui.editor

private val YT_ID = Regex("^[A-Za-z0-9_-]{11}$")

private val PATTERNS = listOf(
    Regex("(?:youtube\\.com|youtube-nocookie\\.com)/watch\\?(?:[^#]*&)?v=([A-Za-z0-9_-]{11})", RegexOption.IGNORE_CASE),
    Regex("(?:youtube\\.com|youtube-nocookie\\.com)/embed/([A-Za-z0-9_-]{11})", RegexOption.IGNORE_CASE),
    Regex("(?:youtube\\.com|youtube-nocookie\\.com)/shorts/([A-Za-z0-9_-]{11})", RegexOption.IGNORE_CASE),
    Regex("(?:youtube\\.com|youtube-nocookie\\.com)/live/([A-Za-z0-9_-]{11})", RegexOption.IGNORE_CASE),
    Regex("youtu\\.be/([A-Za-z0-9_-]{11})", RegexOption.IGNORE_CASE),
    // Thumbnail itself, e.g. img.youtube.com/vi/ID/0.jpg
    Regex("(?:i\\.ytimg\\.com|img\\.youtube\\.com)/vi(?:_webp)?/([A-Za-z0-9_-]{11})/", RegexOption.IGNORE_CASE),
)

/**
 * Extract the video id of a YouTube URL, or null when the URL is not one.
 *
 * Accepts the watch, embed, shorts, live and youtu.be forms, plus the thumbnail
 * URL that the classic HTML embed uses as the image source.
 */
fun youTubeId(url: String): String? {
    val trimmed = url.trim()
    for (pattern in PATTERNS) {
        val match = pattern.find(trimmed) ?: continue
        val id = match.groupValues[1]
        if (YT_ID.matches(id)) return id
    }
    return null
}

fun youTubeEmbedUrl(id: String): String =
    "https://www.youtube-nocookie.com/embed/$id?rel=0&playsinline=1"

fun youTubeThumbnailUrl(id: String): String = "https://i.ytimg.com/vi/$id/hqdefault.jpg"
