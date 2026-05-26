package bd.du.bangla.shahittopotrika.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "read_history")
data class ReadHistoryEntity(
    @PrimaryKey val articleId: String,
    val title: String,
    val authors: String,
    val url: String,
    val readAt: Long = System.currentTimeMillis()
)
