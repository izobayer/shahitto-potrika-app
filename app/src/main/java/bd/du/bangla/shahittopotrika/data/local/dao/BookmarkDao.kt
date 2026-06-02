package bd.du.bangla.shahittopotrika.data.local.dao

import androidx.room.*
import bd.du.bangla.shahittopotrika.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY sortOrder ASC, savedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE folderName = :folderName ORDER BY sortOrder ASC, savedAt DESC")
    fun getBookmarksInFolder(folderName: String): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE articleId = :articleId)")
    fun isBookmarked(articleId: String): Flow<Boolean>

    @Query("SELECT folderName FROM bookmarks WHERE articleId = :articleId")
    fun getFoldersForArticleFlow(articleId: String): Flow<List<String>>

    @Query("SELECT DISTINCT folderName FROM bookmarks")
    fun getAllFolderNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE articleId = :articleId")
    suspend fun delete(articleId: String)

    @Query("DELETE FROM bookmarks WHERE articleId = :articleId AND folderName = :folderName")
    suspend fun deleteSpecificBookmark(articleId: String, folderName: String)

    @Update
    suspend fun update(bookmark: BookmarkEntity)

    @Query("UPDATE bookmarks SET sortOrder = :order WHERE articleId = :articleId")
    suspend fun updateOrder(articleId: String, order: Int)
}
