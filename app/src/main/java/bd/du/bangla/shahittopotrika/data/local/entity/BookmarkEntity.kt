package bd.du.bangla.shahittopotrika.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "bookmarks",
    primaryKeys = ["articleId", "folderName"]
)
data class BookmarkEntity(
    val articleId: String,
    val folderName: String = "পছন্দসমূহ",
    val title: String,
    val authors: String,
    val url: String,
    val pdfUrl: String?,
    val savedAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
