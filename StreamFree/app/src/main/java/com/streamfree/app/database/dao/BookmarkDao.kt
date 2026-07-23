package com.streamfree.app.database.dao

import androidx.room.*
import com.streamfree.app.database.entity.BookmarkEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY savedAt DESC")
    fun getAll(): Flowable<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE videoId = :videoId)")
    suspend fun isBookmarked(videoId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE videoId = :videoId")
    suspend fun deleteById(videoId: String)

    @Query("DELETE FROM bookmarks")
    suspend fun clearAll()
}
