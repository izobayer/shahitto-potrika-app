package bd.du.bangla.shahittopotrika.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import bd.du.bangla.shahittopotrika.data.local.dao.ArticleDao
import bd.du.bangla.shahittopotrika.data.local.dao.BookmarkDao
import bd.du.bangla.shahittopotrika.data.local.dao.IssueDao
import bd.du.bangla.shahittopotrika.data.local.entity.ArticleEntity
import bd.du.bangla.shahittopotrika.data.local.entity.BookmarkEntity
import bd.du.bangla.shahittopotrika.data.local.entity.IssueEntity

@Database(
    entities = [IssueEntity::class, ArticleEntity::class, BookmarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun issueDao(): IssueDao
    abstract fun articleDao(): ArticleDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shahitto_potrika.db"
                ).build().also { INSTANCE = it }
            }
    }
}
