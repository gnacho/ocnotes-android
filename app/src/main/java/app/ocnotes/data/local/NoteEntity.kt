package app.ocnotes.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "note",
    indices = [
        Index("favorite"),
        Index("modified"),
    ],
)
data class NoteEntity(
    @PrimaryKey val id: Long,
    val etag: String,
    val readonly: Boolean,
    // Unix seconds, matching the Notes API (not milliseconds).
    val modified: Long,
    val title: String,
    val category: String,
    val content: String,
    val favorite: Boolean,
    // Outbound queue: true when the local edit differs from the server.
    val dirty: Boolean = false,
    // Tombstone: true when pending a server-side delete.
    val deleted: Boolean = false,
)
