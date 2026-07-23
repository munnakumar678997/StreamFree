package com.streamfree.app.download

import android.content.Context
import com.streamfree.app.database.dao.DownloadDao
import com.streamfree.app.database.entity.DownloadEntity
import com.streamfree.app.database.entity.DownloadStatus
import io.reactivex.rxjava3.core.Flowable

class DownloadRepository(
    private val dao: DownloadDao,
    private val context: Context
) {
    fun getAll(): Flowable<List<DownloadEntity>> = dao.getAll()

    suspend fun enqueue(entity: DownloadEntity) = dao.insert(entity)

    suspend fun updateProgress(videoId: String, progress: Int) =
        dao.updateProgress(videoId, progress, DownloadStatus.DOWNLOADING)

    suspend fun updateStatus(
        videoId: String,
        status: DownloadStatus,
        filePath: String? = null,
        fileSize: Long = 0
    ) {
        if (status == DownloadStatus.COMPLETED && filePath != null) {
            val existing = dao.get(videoId)
            existing?.let { dao.insert(it.copy(status = status, filePath = filePath, fileSize = fileSize, completedAt = System.currentTimeMillis())) }
        } else {
            dao.updateStatus(videoId, status)
        }
    }

    suspend fun delete(videoId: String) = dao.deleteById(videoId)
}
