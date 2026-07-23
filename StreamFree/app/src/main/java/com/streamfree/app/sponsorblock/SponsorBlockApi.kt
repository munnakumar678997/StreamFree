package com.streamfree.app.sponsorblock

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * SponsorBlock API — skip sponsor/intro/outro segments automatically.
 * Community-sourced segment data from sponsor.ajay.app
 *
 * Categories we skip by default:
 *   sponsor     — paid product promotion
 *   intro       — channel intro animation
 *   outro       — end card / subscribe reminder
 *   selfpromo   — unpaid self-promotion
 *   interaction — like/subscribe nudges
 *   preview     — video recap/preview
 *   filler      — tangent filler content
 */
interface SponsorBlockApi {
    @GET("api/skipSegments")
    suspend fun getSegments(
        @Query("videoID") videoId: String,
        @Query("categories") categories: String = "[\"sponsor\",\"intro\",\"outro\",\"selfpromo\",\"interaction\",\"preview\",\"filler\"]"
    ): List<SponsorSegment>
}

data class SponsorSegment(
    val category: String,
    val actionType: String,
    val segment: List<Double>,
    val UUID: String,
    val videoDuration: Double,
    val locked: Int,
    val votes: Int,
    val description: String
) {
    val startMs get() = (segment.getOrElse(0) { 0.0 } * 1000).toLong()
    val endMs   get() = (segment.getOrElse(1) { 0.0 } * 1000).toLong()
    val durationMs get() = endMs - startMs
}
