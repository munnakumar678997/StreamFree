package com.streamfree.app.database.dao

import androidx.room.*
import com.streamfree.app.database.entity.DownloadEntity
import com.streamfree.app.database.entity.DownloadStatus
import io.reactivex.rxjava3.core.Flowable

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY startedAt DESC")
    fun getAll(): Flowable<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status")
    fun getByStatus(status: DownloadStatus): Flowable<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE videoId = :videoId LIMIT 1")
    suspend fun get(videoId: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DownloadEntity)

    @Query("UPDATE downloads SET progress = :progress, status = :status WHERE videoId = :videoId")
    suspend fun updateProgress(videoId: String, progress: Int, status: DownloadStatus)

    @Query("UPDATE downloads SET status = :status, completedAt = :completedAt WHERE videoId = :videoId")
    suspend fun updateStatus(videoId: String, status: DownloadStatus, completedAt: Long = 0)

    @Query("DELETE FROM downloads WHERE videoId = :videoId")
    suspend fun deleteById(videoId: String)
}
