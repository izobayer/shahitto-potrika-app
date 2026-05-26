package bd.du.bangla.shahittopotrika.data.local.dao

import androidx.room.*
import bd.du.bangla.shahittopotrika.data.local.entity.IssueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IssueDao {
    @Query("SELECT * FROM issues WHERE isCurrent = 0 ORDER BY cachedAt DESC")
    fun getAllIssues(): Flow<List<IssueEntity>>

    @Query("SELECT * FROM issues WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentIssue(): IssueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(issues: List<IssueEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(issue: IssueEntity)

    @Query("DELETE FROM issues WHERE isCurrent = 0")
    suspend fun clearArchive()

    @Query("DELETE FROM issues WHERE isCurrent = 1")
    suspend fun clearCurrent()
}
