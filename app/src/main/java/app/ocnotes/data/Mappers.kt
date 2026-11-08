package app.ocnotes.data

import app.ocnotes.data.local.NoteEntity
import app.ocnotes.data.remote.dto.NoteDto

// Maps a full note DTO to its entity. Stubs (`{id}` from a pruneBefore response)
// are never mapped: the caller filters by `modified != null`.
fun NoteDto.toEntity(): NoteEntity = NoteEntity(
    id = id,
    etag = etag ?: "",
    readonly = readonly ?: false,
    modified = modified ?: 0L,
    title = title ?: "",
    category = category ?: "",
    content = content ?: "",
    favorite = favorite ?: false,
)
