package bd.du.bangla.shahittopotrika.data.local.dao

import androidx.room.*
import bd.du.bangla.shahittopotrika.data.local.entity.ArticleCommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleCommentDao {
    @Query("SELECT * FROM article_comments WHERE articleId = :articleId ORDER BY timestamp ASC")
    fun getCommentsForArticle(articleId: String): Flow<List<ArticleCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: ArticleCommentEntity)

    @Query("DELETE FROM article_comments WHERE id = :commentId")
    suspend fun delete(commentId: String)

    @Query("DELETE FROM article_comments WHERE articleId = :articleId")
    suspend fun deleteCommentsForArticle(articleId: String)
}
