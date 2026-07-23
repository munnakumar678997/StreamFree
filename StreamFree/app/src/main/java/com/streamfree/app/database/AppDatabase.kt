package com.streamfree.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.streamfree.app.database.dao.HistoryDao
import com.streamfree.app.database.dao.SubscriptionDao
import com.streamfree.app.database.dao.BookmarkDao
import com.streamfree.app.database.dao.DownloadDao
import com.streamfree.app.database.entity.HistoryEntity
import com.streamfree.app.database.entity.SubscriptionEntity
import com.streamfree.app.database.entity.BookmarkEntity
import com.streamfree.app.database.entity.DownloadEntity

@Database(
    entities = [
        HistoryEntity::class,
        SubscriptionEntity::class,
        BookmarkEntity::class,
        DownloadEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "streamfree.db"
            )
            .fallbackToDestructiveMigration()
            .build()
            .also { INSTANCE = it }
        }
    }
}
