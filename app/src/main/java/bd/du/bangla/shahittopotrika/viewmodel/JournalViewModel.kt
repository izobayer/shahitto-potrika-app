package bd.du.bangla.shahittopotrika.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bd.du.bangla.shahittopotrika.data.model.Article
import bd.du.bangla.shahittopotrika.data.model.Issue
import bd.du.bangla.shahittopotrika.data.model.JournalInfo
import bd.du.bangla.shahittopotrika.data.model.UiState
import bd.du.bangla.shahittopotrika.data.parser.JournalParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JournalViewModel : ViewModel() {

    // ── Current issue ───────────────────────────────────────
    private val _currentIssue = MutableStateFlow<UiState<Issue>>(UiState.Loading)
    val currentIssue: StateFlow<UiState<Issue>> = _currentIssue

    // ── Issue archive ───────────────────────────────────────
    private val _issueArchive = MutableStateFlow<UiState<List<Issue>>>(UiState.Loading)
    val issueArchive: StateFlow<UiState<List<Issue>>> = _issueArchive

    // ── Articles for selected issue ────────────────────────
    private val _articles = MutableStateFlow<UiState<List<Article>>>(UiState.Loading)
    val articles: StateFlow<UiState<List<Article>>> = _articles

    // ── Single article detail ──────────────────────────────
    private val _articleDetail = MutableStateFlow<UiState<Article>>(UiState.Loading)
    val articleDetail: StateFlow<UiState<Article>> = _articleDetail

    // ── Journal info ───────────────────────────────────────
    private val _journalInfo = MutableStateFlow<UiState<JournalInfo>>(UiState.Loading)
    val journalInfo: StateFlow<UiState<JournalInfo>> = _journalInfo

    init {
        loadCurrentIssue()
        loadJournalInfo()
    }

    fun loadCurrentIssue() {
        viewModelScope.launch(Dispatchers.IO) {
            _currentIssue.value = UiState.Loading
            try {
                val issue = JournalParser.fetchCurrentIssue()
                _currentIssue.value = if (issue != null) UiState.Success(issue)
                                      else UiState.Error("চলতি সংখ্যা পাওয়া যায়নি")
            } catch (e: Exception) {
                _currentIssue.value = UiState.Error(e.message ?: "অজানা সমস্যা")
            }
        }
    }

    fun loadIssueArchive() {
        viewModelScope.launch(Dispatchers.IO) {
            _issueArchive.value = UiState.Loading
            try {
                val list = JournalParser.fetchIssueArchive()
                _issueArchive.value = UiState.Success(list)
            } catch (e: Exception) {
                _issueArchive.value = UiState.Error(e.message ?: "অজানা সমস্যা")
            }
        }
    }

    fun loadArticlesForIssue(issueUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _articles.value = UiState.Loading
            try {
                val list = JournalParser.fetchArticlesForIssue(issueUrl)
                _articles.value = UiState.Success(list)
            } catch (e: Exception) {
                _articles.value = UiState.Error(e.message ?: "অজানা সমস্যা")
            }
        }
    }

    fun loadArticleDetail(articleUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _articleDetail.value = UiState.Loading
            try {
                val article = JournalParser.fetchArticleDetail(articleUrl)
                _articleDetail.value = UiState.Success(article)
            } catch (e: Exception) {
                _articleDetail.value = UiState.Error(e.message ?: "অজানা সমস্যা")
            }
        }
    }

    fun loadJournalInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            _journalInfo.value = UiState.Loading
            try {
                val info = JournalParser.fetchJournalInfo()
                _journalInfo.value = UiState.Success(info)
            } catch (e: Exception) {
                _journalInfo.value = UiState.Error(e.message ?: "অজানা সমস্যা")
            }
        }
    }
}
