package bd.du.bangla.shahittopotrika.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bd.du.bangla.shahittopotrika.ShahittoPotrikaApplication
import bd.du.bangla.shahittopotrika.data.model.Article
import bd.du.bangla.shahittopotrika.data.model.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as ShahittoPotrikaApplication).repository

    private val _results = MutableStateFlow<UiState<List<Article>>?>(null)
    val results: StateFlow<UiState<List<Article>>?> = _results

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private var searchJob: Job? = null

    fun onQueryChange(q: String) { _query.value = q }

    fun search() {
        val q = _query.value.trim()
        if (q.isBlank()) { _results.value = null; return }
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            _results.value = UiState.Loading
            repo.search(q).fold(
                onSuccess = { _results.value = UiState.Success(it) },
                onFailure = { _results.value = UiState.Error(it.message ?: "অজানা সমস্যা") }
            )
        }
    }

    fun clear() { searchJob?.cancel(); _query.value = ""; _results.value = null }
}
