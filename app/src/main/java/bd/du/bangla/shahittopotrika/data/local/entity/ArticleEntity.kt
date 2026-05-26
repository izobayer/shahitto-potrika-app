package bd.du.bangla.shahittopotrika.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import bd.du.bangla.shahittopotrika.data.model.Article

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val authors: String,
    val abstract: String,
    val url: String,
    val pdfUrl: String?,
    val keywords: String = "",          // comma-separated
    val doi: String? = null,
    val issueUrl: String = "",
    val cachedAt: Long = System.currentTimeMillis()
)

fun ArticleEntity.toArticle() = Article(
    id, title, authors, abstract, url, pdfUrl,
    keywords = if (keywords.isBlank()) emptyList() else keywords.split(","),
    doi = doi
)

fun Article.toEntity(issueUrl: String = "") = ArticleEntity(
    id, title, authors, abstract, url, pdfUrl,
    keywords = keywords.joinToString(","),
    doi = doi,
    issueUrl = issueUrl
)
