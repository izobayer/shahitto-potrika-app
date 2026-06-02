package bd.du.bangla.shahittopotrika.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "article_comments")
data class ArticleCommentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val articleId: String,
    val userName: String,
    val userEmail: String,
    val commentText: String,
    val timestamp: Long = System.currentTimeMillis()
)
