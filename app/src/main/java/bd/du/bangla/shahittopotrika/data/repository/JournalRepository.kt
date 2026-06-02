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
    private val commentDao  = db.articleCommentDao()
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
        try {
            val article = JournalParser.fetchArticleDetail(articleUrl)
            articleDao.insert(article.toEntity())
            article
        } catch (e: Exception) {
            val id = articleUrl.substringAfterLast("/")
            val cached = articleDao.getArticleById(id)
            cached?.toArticle() ?: throw e
        }
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
    fun getBookmarksInFolder(folderName: String): Flow<List<BookmarkEntity>> = bookmarkDao.getBookmarksInFolder(folderName)
    fun isBookmarked(articleId: String): Flow<Boolean> = bookmarkDao.isBookmarked(articleId)
    fun getFoldersForArticleFlow(articleId: String): Flow<List<String>> = bookmarkDao.getFoldersForArticleFlow(articleId)
    fun getAllFolderNames(): Flow<List<String>> = bookmarkDao.getAllFolderNames()

    suspend fun addBookmark(article: Article) {
        bookmarkDao.insert(BookmarkEntity(
            articleId = article.id, title = article.title,
            authors = article.authors, url = article.url, pdfUrl = article.pdfUrl))
    }

    suspend fun addBookmarkToFolder(article: Article, folderName: String) {
        bookmarkDao.insert(BookmarkEntity(
            articleId = article.id, folderName = folderName, title = article.title,
            authors = article.authors, url = article.url, pdfUrl = article.pdfUrl))
    }

    suspend fun removeBookmark(articleId: String) = bookmarkDao.delete(articleId)
    suspend fun deleteSpecificBookmark(articleId: String, folderName: String) = bookmarkDao.deleteSpecificBookmark(articleId, folderName)
    suspend fun updateBookmarkOrder(articleId: String, order: Int) = bookmarkDao.updateOrder(articleId, order)

    suspend fun updateArticleBookmarks(article: Article, checkedFolders: List<String>, uncheckedFolders: List<String>) {
        for (folder in checkedFolders) {
            addBookmarkToFolder(article, folder)
        }
        for (folder in uncheckedFolders) {
            deleteSpecificBookmark(article.id, folder)
        }
    }

    // ── Reading history ────────────────────────────────────
    fun getReadHistory(): Flow<List<ReadHistoryEntity>> = historyDao.getAll()
    fun getHistoryForArticle(articleId: String): Flow<ReadHistoryEntity?> = historyDao.getHistoryByIdFlow(articleId)
    suspend fun updateReadingProgress(articleId: String, progress: Float, offset: Int) = historyDao.updateProgress(articleId, progress, offset)

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

    // ── Comments ───────────────────────────────────────────
    fun getCommentsForArticle(articleId: String): Flow<List<ArticleCommentEntity>> =
        commentDao.getCommentsForArticle(articleId)

    suspend fun addComment(articleId: String, userName: String, userEmail: String, commentText: String) {
        commentDao.insert(ArticleCommentEntity(
            articleId = articleId, userName = userName, userEmail = userEmail, commentText = commentText
        ))
    }

    suspend fun deleteComment(commentId: String) = commentDao.delete(commentId)
}
