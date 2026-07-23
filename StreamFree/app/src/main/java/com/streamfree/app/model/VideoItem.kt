package com.streamfree.app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoItem(
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val uploaderName: String,
    val viewCount: Long = -1,
    val duration: Long = -1,
    val uploadDate: String = "",
    val url: String = ""
) : Parcelable {
    val viewCountFormatted: String get() = when {
        viewCount < 0   -> ""
        viewCount >= 1_000_000_000 -> "${viewCount / 1_000_000_000}B views"
        viewCount >= 1_000_000     -> "${viewCount / 1_000_000}M views"
        viewCount >= 1_000         -> "${viewCount / 1_000}K views"
        else                       -> "$viewCount views"
    }
    val durationFormatted: String get() = if (duration <= 0) "LIVE" else
        String.format("%d:%02d", duration / 60, duration % 60).let {
            if (duration >= 3600) String.format("%d:%02d:%02d", duration / 3600, (duration % 3600) / 60, duration % 60) else it
        }
    val youtubeUrl: String get() = if (url.isNotEmpty()) url else "https://www.youtube.com/watch?v=$videoId"
}
