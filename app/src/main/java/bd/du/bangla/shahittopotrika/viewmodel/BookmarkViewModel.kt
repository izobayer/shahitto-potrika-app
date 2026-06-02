package bd.du.bangla.shahittopotrika.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bd.du.bangla.shahittopotrika.ShahittoPotrikaApplication
import bd.du.bangla.shahittopotrika.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BookmarkViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as ShahittoPotrikaApplication).repository

    private val _selectedFolder = MutableStateFlow("সব")
    val selectedFolder: StateFlow<String> = _selectedFolder

    val folderMetadata: StateFlow<String> =
        repo.prefs.folderMetadata
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

    val folders: StateFlow<List<String>> =
        repo.getAllFolderNames()
            .map { list ->
                val base = mutableListOf("সব", "পছন্দসমূহ")
                list.forEach {
                    if (it != "পছন্দসমূহ" && it.isNotBlank() && it !in base) {
                        base.add(it)
                    }
                }
                base.toList()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("সব", "পছন্দসমূহ"))

    val bookmarks: StateFlow<List<BookmarkEntity>> =
        _selectedFolder.flatMapLatest { folder ->
            if (folder == "সব") {
                repo.getAllBookmarks()
            } else {
                repo.getBookmarksInFolder(folder)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectFolder(folder: String) {
        _selectedFolder.value = folder
    }

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
