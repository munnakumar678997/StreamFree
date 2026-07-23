package com.streamfree.app.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val uploaderName: String,
    val duration: Long,
    val savedAt: Long = System.currentTimeMillis()
)
