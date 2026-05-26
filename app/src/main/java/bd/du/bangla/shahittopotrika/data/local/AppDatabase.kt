package bd.du.bangla.shahittopotrika.data.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import bd.du.bangla.shahittopotrika.data.local.dao.*
import bd.du.bangla.shahittopotrika.data.local.entity.*

@Database(
    entities = [
        IssueEntity::class,
        ArticleEntity::class,
        BookmarkEntity::class,
        ReadHistoryEntity::class,
        ArticleNoteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun issueDao(): IssueDao
    abstract fun articleDao(): ArticleDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readHistoryDao(): ReadHistoryDao
    abstract fun articleNoteDao(): ArticleNoteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS `read_history` (
                    `articleId` TEXT NOT NULL PRIMARY KEY,
                    `title` TEXT NOT NULL,
                    `authors` TEXT NOT NULL,
                    `url` TEXT NOT NULL,
                    `readAt` INTEGER NOT NULL)""")
                db.execSQL("""CREATE TABLE IF NOT EXISTS `article_notes` (
                    `articleId` TEXT NOT NULL PRIMARY KEY,
                    `articleTitle` TEXT NOT NULL,
                    `noteText` TEXT NOT NULL,
                    `updatedAt` INTEGER NOT NULL)""")
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shahitto_potrika.db"
                )
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
            }
    }
}
