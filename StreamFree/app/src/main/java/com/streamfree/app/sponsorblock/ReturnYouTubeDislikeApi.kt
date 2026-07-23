package com.streamfree.app.sponsorblock

import okhttp3.OkHttpClient
import com.google.gson.Gson
import okhttp3.Request

/**
 * Return YouTube Dislike — community-sourced dislike counts.
 * Uses returnyoutubedislike.com API (free, no auth needed).
 */
object ReturnYouTubeDislikeApi {

    private val client = OkHttpClient()
    private val gson = Gson()

    data class VoteData(
        val id: String,
        val dateCreated: String,
        val likes: Long,
        val dislikes: Long,
        val rating: Double,
        val viewCount: Long,
        val deleted: Boolean
    )

    suspend fun getVotes(videoId: String): VoteData? = try {
        val req = Request.Builder()
            .url("https://returnyoutubedislikeapi.com/votes?videoId=$videoId")
            .addHeader("Accept", "application/json")
            .build()
        val resp = client.newCall(req).execute()
        gson.fromJson(resp.body?.string(), VoteData::class.java)
    } catch (e: Exception) { null }
}
