package bd.du.bangla.shahittopotrika.data.local.dao

import androidx.room.*
import bd.du.bangla.shahittopotrika.data.local.entity.ReadHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadHistoryDao {
    @Query("SELECT * FROM read_history ORDER BY readAt DESC")
    fun getAll(): Flow<List<ReadHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReadHistoryEntity)

    @Query("DELETE FROM read_history WHERE articleId = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM read_history")
    suspend fun clearAll()

    @Query("SELECT * FROM read_history WHERE articleId = :id LIMIT 1")
    fun getHistoryByIdFlow(id: String): Flow<ReadHistoryEntity?>

    @Query("UPDATE read_history SET progress = :progress, scrollOffset = :offset WHERE articleId = :id")
    suspend fun updateProgress(id: String, progress: Float, offset: Int)
}
