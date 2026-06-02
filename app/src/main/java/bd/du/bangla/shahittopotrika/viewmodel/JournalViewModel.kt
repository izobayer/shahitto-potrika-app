package bd.du.bangla.shahittopotrika.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bd.du.bangla.shahittopotrika.ShahittoPotrikaApplication
import bd.du.bangla.shahittopotrika.data.local.entity.ArticleNoteEntity
import bd.du.bangla.shahittopotrika.data.local.entity.ReadHistoryEntity
import bd.du.bangla.shahittopotrika.data.model.Article
import bd.du.bangla.shahittopotrika.data.model.Issue
import bd.du.bangla.shahittopotrika.data.model.JournalInfo
import bd.du.bangla.shahittopotrika.data.model.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class JournalViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as ShahittoPotrikaApplication).repository

    // ── Current issue ───────────────────────────────────────
    private val _currentIssue = MutableStateFlow<UiState<Issue>>(UiState.Loading)
    val currentIssue: StateFlow<UiState<Issue>> = _currentIssue

    private val _isRefreshingHome = MutableStateFlow(false)
    val isRefreshingHome: StateFlow<Boolean> = _isRefreshingHome

    // ── Issue archive ───────────────────────────────────────
    private val _isRefreshingArchive = MutableStateFlow(false)
    val isRefreshingArchive: StateFlow<Boolean> = _isRefreshingArchive

    val issueArchive: StateFlow<UiState<List<Issue>>> =
        repo.getIssueArchiveFlow()
            .map<List<Issue>, UiState<List<Issue>>> { UiState.Success(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    // ── Articles ────────────────────────────────────────────
    private val _currentIssueUrl = MutableStateFlow("")
    private val _isRefreshingArticles = MutableStateFlow(false)
    val isRefreshingArticles: StateFlow<Boolean> = _isRefreshingArticles

    val articles: StateFlow<UiState<List<Article>>> =
        _currentIssueUrl
            .filter { it.isNotBlank() }
            .flatMapLatest { url ->
                repo.getArticlesFlow(url)
                    .map<List<Article>, UiState<List<Article>>> { UiState.Success(it) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    // ── Article detail ──────────────────────────────────────
    private val _articleDetail = MutableStateFlow<UiState<Article>>(UiState.Loading)
    val articleDetail: StateFlow<UiState<Article>> = _articleDetail

    // ── Bookmark ────────────────────────────────────────────
    private val _currentArticleId = MutableStateFlow("")
    val isBookmarked: StateFlow<Boolean> =
        _currentArticleId.filter { it.isNotBlank() }
            .flatMapLatest { repo.isBookmarked(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentArticleFolders: StateFlow<List<String>> =
        _currentArticleId.filter { it.isNotBlank() }
            .flatMapLatest { repo.getFoldersForArticleFlow(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFolderNames: StateFlow<List<String>> =
        repo.getAllFolderNames()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folderMetadata: StateFlow<String> = repo.prefs.folderMetadata
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "{}")

    fun updateFolderMetadata(folderName: String, emoji: String, colorHex: String) {
        viewModelScope.launch {
            val currentJson = folderMetadata.value
            val newJson = try {
                val jsonObject = org.json.JSONObject(currentJson)
                val folderObj = org.json.JSONObject().apply {
                    put("emoji", emoji)
                    put("colorHex", colorHex)
                }
                jsonObject.put(folderName, folderObj)
                jsonObject.toString()
            } catch (e: Exception) {
                val jsonObject = org.json.JSONObject()
                val folderObj = org.json.JSONObject().apply {
                    put("emoji", emoji)
                    put("colorHex", colorHex)
                }
                jsonObject.put(folderName, folderObj)
                jsonObject.toString()
            }
            repo.prefs.setFolderMetadata(newJson)
        }
    }

    // ── Note for current article ────────────────────────────
    // ── Note for current article ────────────────────────────
    val currentNote: StateFlow<ArticleNoteEntity?> =
        _currentArticleId.filter { it.isNotBlank() }
            .flatMapLatest { repo.getNoteForArticle(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentArticleComments: StateFlow<List<bd.du.bangla.shahittopotrika.data.local.entity.ArticleCommentEntity>> =
        _currentArticleId.filter { it.isNotBlank() }
            .flatMapLatest { repo.getCommentsForArticle(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isUserLoggedIn: StateFlow<Boolean> = repo.prefs.isUserLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userName: StateFlow<String> = repo.prefs.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userEmail: StateFlow<String> = repo.prefs.userEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // ── Read history ────────────────────────────────────────
    val readHistory: StateFlow<List<ReadHistoryEntity>> =
        repo.getReadHistory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentArticleHistory: StateFlow<ReadHistoryEntity?> =
        _currentArticleId.filter { it.isNotBlank() }
            .flatMapLatest { repo.getHistoryForArticle(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ── Journal info ────────────────────────────────────────
    private val _journalInfo = MutableStateFlow<UiState<JournalInfo>>(UiState.Loading)
    val journalInfo: StateFlow<UiState<JournalInfo>> = _journalInfo

    init {
        loadCurrentIssue()
        loadJournalInfo()
        loadIssueArchive()
    }

    fun loadCurrentIssue(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!forceRefresh) {
                val cached = repo.getCachedCurrentIssue()
                if (cached != null) { _currentIssue.value = UiState.Success(cached); return@launch }
            }
            _isRefreshingHome.value = true
            repo.refreshCurrentIssue().fold(
                onSuccess = { issue ->
                    _currentIssue.value = if (issue != null) UiState.Success(issue)
                                          else UiState.Error("চলতি সংখ্যা পাওয়া যায়নি")
                },
                onFailure = { e ->
                    val cached = repo.getCachedCurrentIssue()
                    _currentIssue.value = if (cached != null) UiState.Success(cached)
                                          else UiState.Error(e.message ?: "নেটওয়ার্ক সমস্যা")
                }
            )
            _isRefreshingHome.value = false
        }
    }

    fun loadIssueArchive(forceRefresh: Boolean = false) {
        if (!forceRefresh) return
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshingArchive.value = true
            repo.refreshIssueArchive()
            _isRefreshingArchive.value = false
        }
    }

    fun loadArticlesForIssue(issueUrl: String, forceRefresh: Boolean = false) {
        _currentIssueUrl.value = issueUrl
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshingArticles.value = true
            repo.refreshArticles(issueUrl)
            _isRefreshingArticles.value = false
        }
    }

    fun loadArticleDetail(articleUrl: String) {
        val id = articleUrl.substringAfterLast("/")
        _currentArticleId.value = id
        viewModelScope.launch(Dispatchers.IO) {
            _articleDetail.value = UiState.Loading
            repo.getArticleDetail(articleUrl).fold(
                onSuccess = { article ->
                    _articleDetail.value = UiState.Success(article)
                    repo.markAsRead(article)       // auto-add to history
                },
                onFailure = { _articleDetail.value = UiState.Error(it.message ?: "লোড করতে সমস্যা") }
            )
        }
    }

    fun toggleBookmark(article: Article) = viewModelScope.launch(Dispatchers.IO) {
        if (isBookmarked.value) repo.removeBookmark(article.id) else repo.addBookmark(article)
    }

    fun updateArticleBookmarks(article: Article, checkedFolders: List<String>, uncheckedFolders: List<String>) =
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateArticleBookmarks(article, checkedFolders, uncheckedFolders)
        }

    fun removeBookmark(articleId: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.removeBookmark(articleId)
    }

    fun saveNote(articleId: String, articleTitle: String, text: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repo.saveNote(articleId, articleTitle, text)
        }

    fun deleteNote(articleId: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteNote(articleId)
    }

    /** Allows notes screen to activate the note flow without re-fetching the article. */
    fun setCurrentArticle(id: String) {
        _currentArticleId.value = id
    }

    fun addComment(articleId: String, userName: String, userEmail: String, commentText: String) =
        viewModelScope.launch(Dispatchers.IO) {
            repo.addComment(articleId, userName, userEmail, commentText)
        }

    fun deleteComment(commentId: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteComment(commentId)
    }

    fun deleteFromHistory(id: String) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteFromHistory(id)
    }

    fun clearHistory() = viewModelScope.launch(Dispatchers.IO) {
        repo.clearHistory()
    }

    fun updateReadingProgress(articleId: String, progress: Float, offset: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateReadingProgress(articleId, progress, offset)
        }

    fun loadJournalInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            _journalInfo.value = UiState.Loading
            repo.getJournalInfo().fold(
                onSuccess = { _journalInfo.value = UiState.Success(it) },
                onFailure = { _journalInfo.value = UiState.Error(it.message ?: "লোড করতে সমস্যা") }
            )
        }
    }
}
