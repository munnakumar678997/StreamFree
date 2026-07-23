package com.streamfree.app.sponsorblock

class SponsorBlockRepository(private val api: SponsorBlockApi) {

    private val cache = mutableMapOf<String, List<SponsorSegment>>()

    suspend fun getSegments(videoId: String): List<SponsorSegment> {
        cache[videoId]?.let { return it }
        return try {
            api.getSegments(videoId).also { cache[videoId] = it }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearCache() = cache.clear()

    companion object {
        /** Check if current position is inside a sponsor segment that should be skipped */
        fun shouldSkip(positionMs: Long, segments: List<SponsorSegment>): SponsorSegment? =
            segments.firstOrNull { seg ->
                positionMs >= seg.startMs && positionMs < seg.endMs - 500 // 500ms buffer
            }
    }
}
