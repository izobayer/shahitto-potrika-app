package bd.du.bangla.shahittopotrika.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val articleId: String,
    val title: String,
    val authors: String,
    val url: String,
    val pdfUrl: String?,
    val savedAt: Long = System.currentTimeMillis()
)
