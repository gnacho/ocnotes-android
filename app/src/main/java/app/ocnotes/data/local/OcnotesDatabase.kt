package app.ocnotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class OcnotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
