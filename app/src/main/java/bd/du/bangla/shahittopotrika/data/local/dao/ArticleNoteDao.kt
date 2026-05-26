package bd.du.bangla.shahittopotrika.data.local.dao

import androidx.room.*
import bd.du.bangla.shahittopotrika.data.local.entity.ArticleNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleNoteDao {
    @Query("SELECT * FROM article_notes ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<ArticleNoteEntity>>

    @Query("SELECT * FROM article_notes WHERE articleId = :id LIMIT 1")
    fun getNote(id: String): Flow<ArticleNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: ArticleNoteEntity)

    @Query("DELETE FROM article_notes WHERE articleId = :id")
    suspend fun delete(id: String)
}
