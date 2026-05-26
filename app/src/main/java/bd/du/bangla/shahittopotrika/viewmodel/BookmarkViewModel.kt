package bd.du.bangla.shahittopotrika.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import bd.du.bangla.shahittopotrika.ShahittoPotrikaApplication
import bd.du.bangla.shahittopotrika.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope

class BookmarkViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as ShahittoPotrikaApplication).repository

    val bookmarks: StateFlow<List<BookmarkEntity>> =
        repo.getAllBookmarks()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
