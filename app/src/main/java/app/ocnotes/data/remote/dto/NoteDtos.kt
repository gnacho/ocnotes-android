package app.ocnotes.data.remote.dto

import kotlinx.serialization.Serializable

// A note from GET /notes. A pruneBefore response mixes full notes with `{id}`
// stubs for unchanged notes, so every field except `id` is nullable: `modified`
// being non-null is what distinguishes a full note from a stub.
@Serializable
data class NoteDto(
    val id: Long,
    val etag: String? = null,
    val readonly: Boolean? = null,
    val modified: Long? = null,
    val title: String? = null,
    val category: String? = null,
    val content: String? = null,
    val favorite: Boolean? = null,
)

@Serializable
data class CreateNoteRequest(
    val title: String,
    val content: String,
    val category: String,
)

@Serializable
data class UpdateNoteRequest(
    val title: String? = null,
    val content: String? = null,
    val category: String? = null,
    val favorite: Boolean? = null,
)

// The Notes API settings are an arbitrary map; we model the two keys clients
// care about and ignore the rest on read. Defaults match the server's lazy seed.
@Serializable
data class SettingsResponse(
    val notesPath: String = "Notes",
    val fileSuffix: String = ".md",
)
