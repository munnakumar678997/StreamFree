package com.streamfree.app.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class HistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val uploaderName: String,
    val duration: Long,
    val watchedAt: Long = System.currentTimeMillis(),
    val watchedSeconds: Long = 0   // resume position
)
