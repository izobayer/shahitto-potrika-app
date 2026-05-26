package bd.du.bangla.shahittopotrika.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import bd.du.bangla.shahittopotrika.data.model.Issue

@Entity(tableName = "issues")
data class IssueEntity(
    @PrimaryKey val id: String,
    val title: String,
    val volume: String,
    val number: String,
    val year: String,
    val coverImageUrl: String?,
    val url: String,
    val isCurrent: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis()
)

fun IssueEntity.toIssue() = Issue(id, title, volume, number, year, coverImageUrl, url)
fun Issue.toEntity(isCurrent: Boolean = false) =
    IssueEntity(id, title, volume, number, year, coverImageUrl, url, isCurrent)
