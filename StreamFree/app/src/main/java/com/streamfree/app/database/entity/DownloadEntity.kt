package com.streamfree.app.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val uploaderName: String,
    val filePath: String,
    val fileSize: Long = 0,
    val quality: String = "",
    val mimeType: String = "",
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0
)

enum class DownloadStatus { QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED }
