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
}
