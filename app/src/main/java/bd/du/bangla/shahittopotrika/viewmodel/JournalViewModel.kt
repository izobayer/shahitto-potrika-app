package bd.du.bangla.shahittopotrika.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bd.du.bangla.shahittopotrika.ShahittoPotrikaApplication
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

    // ── Articles for selected issue ─────────────────────────
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

    // ── Bookmark state ──────────────────────────────────────
    private val _currentArticleId = MutableStateFlow("")
    val isBookmarked: StateFlow<Boolean> =
        _currentArticleId
            .filter { it.isNotBlank() }
            .flatMapLatest { repo.isBookmarked(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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
                if (cached != null) {
                    _currentIssue.value = UiState.Success(cached)
                    return@launch
                }
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
        if (!forceRefresh) return   // Flow handles cached data automatically
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshingArchive.value = true
            repo.refreshIssueArchive()
            _isRefreshingArchive.value = false
        }
    }

    fun loadArticlesForIssue(issueUrl: String, forceRefresh: Boolean = false) {
        _currentIssueUrl.value = issueUrl
        if (!forceRefresh) {
            // Flow from DB handles it; also trigger network refresh
        }
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
                onSuccess = { _articleDetail.value = UiState.Success(it) },
                onFailure = { _articleDetail.value = UiState.Error(it.message ?: "লোড করতে সমস্যা") }
            )
        }
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isBookmarked.value) {
                repo.removeBookmark(article.id)
            } else {
                repo.addBookmark(article)
            }
        }
    }

    fun removeBookmark(articleId: String) {
        viewModelScope.launch(Dispatchers.IO) { repo.removeBookmark(articleId) }
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
