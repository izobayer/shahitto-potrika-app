package bd.du.bangla.shahittopotrika.data.repository

import android.content.Context
import bd.du.bangla.shahittopotrika.data.local.AppDatabase
import bd.du.bangla.shahittopotrika.data.local.entity.*
import bd.du.bangla.shahittopotrika.data.model.Article
import bd.du.bangla.shahittopotrika.data.model.Issue
import bd.du.bangla.shahittopotrika.data.model.JournalInfo
import bd.du.bangla.shahittopotrika.data.parser.JournalParser
import bd.du.bangla.shahittopotrika.data.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class JournalRepository(context: Context) {

    private val db          = AppDatabase.get(context)
    private val issueDao    = db.issueDao()
    private val articleDao  = db.articleDao()
    private val bookmarkDao = db.bookmarkDao()
    private val historyDao  = db.readHistoryDao()
    private val noteDao     = db.articleNoteDao()
    val prefs               = UserPreferences(context)

    // ── Issues ─────────────────────────────────────────────
    fun getIssueArchiveFlow(): Flow<List<Issue>> =
        issueDao.getAllIssues().map { it.map(IssueEntity::toIssue) }

    suspend fun refreshIssueArchive(): Result<Unit> = runCatching {
        val issues = JournalParser.fetchIssueArchive()
        issueDao.clearArchive()
        issueDao.insertAll(issues.map { it.toEntity(isCurrent = false) })
    }

    suspend fun getCachedCurrentIssue(): Issue? =
        issueDao.getCurrentIssue()?.toIssue()

    suspend fun refreshCurrentIssue(): Result<Issue?> = runCatching {
        val issue = JournalParser.fetchCurrentIssue()
        issueDao.clearCurrent()
        if (issue != null) issueDao.insert(issue.toEntity(isCurrent = true))
        issue
    }

    // ── Articles ───────────────────────────────────────────
    fun getArticlesFlow(issueUrl: String): Flow<List<Article>> =
        articleDao.getArticlesForIssue(issueUrl).map { it.map(ArticleEntity::toArticle) }

    suspend fun refreshArticles(issueUrl: String): Result<Unit> = runCatching {
        val articles = JournalParser.fetchArticlesForIssue(issueUrl)
        articleDao.clearForIssue(issueUrl)
        articleDao.insertAll(articles.map { it.toEntity(issueUrl) })
    }

    suspend fun getArticleDetail(articleUrl: String): Result<Article> = runCatching {
        val article = JournalParser.fetchArticleDetail(articleUrl)
        articleDao.insert(article.toEntity())
        article
    }

    // ── Search ─────────────────────────────────────────────
    suspend fun search(query: String): Result<List<Article>> = runCatching {
        JournalParser.search(query)
    }

    // ── Journal info ───────────────────────────────────────
    suspend fun getJournalInfo(): Result<JournalInfo> = runCatching {
        JournalParser.fetchJournalInfo()
    }

    // ── Bookmarks ──────────────────────────────────────────
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
    fun isBookmarked(articleId: String): Flow<Boolean> = bookmarkDao.isBookmarked(articleId)

    suspend fun addBookmark(article: Article) {
        bookmarkDao.insert(BookmarkEntity(
            articleId = article.id, title = article.title,
            authors = article.authors, url = article.url, pdfUrl = article.pdfUrl))
    }

    suspend fun removeBookmark(articleId: String) = bookmarkDao.delete(articleId)

    // ── Reading history ────────────────────────────────────
    fun getReadHistory(): Flow<List<ReadHistoryEntity>> = historyDao.getAll()

    suspend fun markAsRead(article: Article) {
        historyDao.insert(ReadHistoryEntity(
            articleId = article.id, title = article.title,
            authors = article.authors, url = article.url))
    }

    suspend fun deleteFromHistory(id: String) = historyDao.delete(id)
    suspend fun clearHistory() = historyDao.clearAll()

    // ── Notes ──────────────────────────────────────────────
    fun getAllNotes(): Flow<List<ArticleNoteEntity>> = noteDao.getAll()
    fun getNoteForArticle(articleId: String): Flow<ArticleNoteEntity?> = noteDao.getNote(articleId)

    suspend fun saveNote(articleId: String, articleTitle: String, text: String) {
        if (text.isBlank()) noteDao.delete(articleId)
        else noteDao.upsert(ArticleNoteEntity(articleId, articleTitle, text))
    }

    suspend fun deleteNote(articleId: String) = noteDao.delete(articleId)
}
