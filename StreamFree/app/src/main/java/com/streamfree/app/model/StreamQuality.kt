package com.streamfree.app.model

data class StreamQuality(
    val label: String,          // e.g. "1080p", "720p", "Audio only"
    val url: String,
    val mimeType: String,
    val bitrate: Int,
    val width: Int = 0,
    val height: Int = 0,
    val fps: Int = 0,
    val isAudioOnly: Boolean = false,
    val isAdaptive: Boolean = false,
    val itag: Int = 0
) {
    val isHD get() = height >= 720
    val isFullHD get() = height >= 1080
    val qualityScore get() = height * 100 + fps + bitrate / 1000
}
