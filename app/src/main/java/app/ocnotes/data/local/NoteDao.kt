package app.ocnotes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(notes: List<NoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity)

    @Query("SELECT * FROM note WHERE deleted = 0 ORDER BY favorite DESC, modified DESC")
    fun getAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM note WHERE deleted = 0 AND favorite = 1 ORDER BY modified DESC")
    fun getFavorites(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM note WHERE id = :id")
    fun getById(id: Long): Flow<NoteEntity?>

    @Query("SELECT * FROM note WHERE id = :id")
    suspend fun getByIdOnce(id: Long): NoteEntity?

    @Query("SELECT MAX(modified) FROM note WHERE deleted = 0")
    suspend fun getHighestModified(): Long?

    @Query("SELECT id FROM note WHERE deleted = 0")
    suspend fun getAllIdsOnce(): List<Long>

    // Outbound queue.
    @Query("SELECT * FROM note WHERE dirty = 1 AND deleted = 0")
    suspend fun getPendingUpdates(): List<NoteEntity>

    @Query("SELECT * FROM note WHERE deleted = 1")
    suspend fun getPendingDeletes(): List<NoteEntity>

    @Query("DELETE FROM note WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM note")
    suspend fun clear()
}
