package bd.du.bangla.shahittopotrika.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "article_notes")
data class ArticleNoteEntity(
    @PrimaryKey val articleId: String,
    val articleTitle: String,
    val noteText: String,
    val updatedAt: Long = System.currentTimeMillis()
)
