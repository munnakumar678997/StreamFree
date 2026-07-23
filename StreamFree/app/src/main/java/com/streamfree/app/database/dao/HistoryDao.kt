package com.streamfree.app.database.dao

import androidx.room.*
import com.streamfree.app.database.entity.HistoryEntity
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Completable

@Dao
interface HistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    fun getAll(): Flowable<List<HistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE videoId = :videoId LIMIT 1")
    suspend fun get(videoId: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoryEntity)

    @Delete
    suspend fun delete(entity: HistoryEntity)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM watch_history")
    suspend fun count(): Int
}
