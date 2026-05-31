package bd.du.bangla.shahittopotrika.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bd.du.bangla.shahittopotrika.ShahittoPotrikaApplication
import bd.du.bangla.shahittopotrika.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarkViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as ShahittoPotrikaApplication).repository

    val bookmarks: StateFlow<List<BookmarkEntity>> =
        repo.getAllBookmarks()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Move item at [fromIndex] up or down by one position. */
    fun moveBookmark(fromIndex: Int, toIndex: Int) {
        val list = bookmarks.value.toMutableList()
        if (fromIndex !in list.indices || toIndex !in list.indices) return
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        viewModelScope.launch {
            list.forEachIndexed { idx, bm ->
                repo.updateBookmarkOrder(bm.articleId, idx)
            }
        }
    }
}
