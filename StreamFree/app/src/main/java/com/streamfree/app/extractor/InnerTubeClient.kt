package com.streamfree.app.extractor

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

/**
 * Lightweight direct Innertube API wrapper.
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │  WHY THIS APP HAS NO ADS — THE COMPLETE EXPLANATION          │
 * ├──────────────────────────────────────────────────────────────┤
 * │  1. We POST to /youtubei/v1/player with ANDROID client       │
 * │  2. YouTube returns JSON player response                     │
 * │  3. extractStreams() reads ONLY streamingData node           │
 * │  4. Within streamingData: adaptiveFormats + formats → USED   │
 * │  5. adPlacements, adSlots, etc. → NEVER read, NEVER called  │
 * │  6. ExoPlayer plays the direct stream URL                    │
 * │  7. No ad server URL ever constructed or fetched             │
 * └──────────────────────────────────────────────────────────────┘
 *
 * Supported clients (fallback order):
 *   ANDROID → best streams, no ads (primary)
 *   IOS     → age-restricted fallback
 *   WEB_EMBEDDED → embeddable video fallback
 */
object InnerTubeClient {

    private const val BASE = "https://www.youtube.com/youtubei/v1"
    private val gson = Gson()
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // ── Client Definitions ────────────────────────────────────────

    data class Client(
        val name: String,
        val id: String,
        val version: String,
        val androidSdk: Int? = null,
        val userAgent: String
    )

    val ANDROID = Client(
        name = "ANDROID", id = "3", version = "21.03.36", androidSdk = 36,
        userAgent = "com.google.android.youtube/21.03.36 (Linux; U; Android 16; en_US) gzip"
    )
    val IOS = Client(
        name = "IOS", id = "5", version = "21.03.2",
        userAgent = "com.google.ios.youtube/21.03.2 (iPhone16,2; U; CPU iOS 18_7_2 like Mac OS X)"
    )
    val WEB_EMBEDDED = Client(
        name = "WEB_EMBEDDED_PLAYER", id = "56", version = "1.20260122.01.00",
        userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    )

    // ── Context Builder ───────────────────────────────────────────

    private fun buildContext(client: Client, hl: String = "en", gl: String = "US"): JsonObject {
        val clientObj = JsonObject().apply {
            addProperty("clientName", client.name)
            addProperty("clientVersion", client.version)
            addProperty("hl", hl)
            addProperty("gl", gl)
            addProperty("utcOffsetMinutes", 0)
            client.androidSdk?.let { addProperty("androidSdkVersion", it) }
        }
        return JsonObject().apply {
            add("client", clientObj)
            add("user", JsonObject().apply { addProperty("lockedSafetyMode", false) })
        }
    }

    // ── API Calls ─────────────────────────────────────────────────

    /**
     * Get player response for a video.
     * contentCheckOk + racyCheckOk bypass content/age gates.
     * Only streamingData will be used from this response — no ads.
     */
    fun playerResponse(videoId: String, client: Client = ANDROID): JsonObject {
        val body = JsonObject().apply {
            add("context", buildContext(client))
            addProperty("videoId", videoId)
            addProperty("contentCheckOk", true)
            addProperty("racyCheckOk", true)
            addProperty("cpn", generateCpn())
        }
        return post("$BASE/player", body, client)
    }

    fun search(query: String, client: Client = ANDROID): JsonObject {
        val body = JsonObject().apply {
            add("context", buildContext(client))
            addProperty("query", query)
        }
        return post("$BASE/search", body, client)
    }

    fun browse(browseId: String, client: Client = ANDROID): JsonObject {
        val body = JsonObject().apply {
            add("context", buildContext(client))
            addProperty("browseId", browseId)
        }
        return post("$BASE/browse", body, client)
    }

    fun next(videoId: String, client: Client = ANDROID): JsonObject {
        val body = JsonObject().apply {
            add("context", buildContext(client))
            addProperty("videoId", videoId)
        }
        return post("$BASE/next", body, client)
    }

    // ── Stream Extraction (NO ADS) ────────────────────────────────

    /**
     * Extract video/audio stream URLs from player response.
     *
     * We read EXACTLY two keys from streamingData:
     *   - adaptiveFormats: separate video and audio tracks (DASH)
     *   - formats: combined video+audio tracks
     *
     * We deliberately never read:
     *   - adPlacements       (ad injection timestamps)
     *   - adSlots            (ad configuration)
     *   - adBreakHeartbeatParams (ad tracking beacons)
     *   - paidContentOverlay (overlay ads)
     *
     * This is why StreamFree has zero ads.
     */
    fun extractStreams(playerResp: JsonObject): List<StreamUrl> {
        val streams = mutableListOf<StreamUrl>()
        val streamingData = playerResp.getAsJsonObject("streamingData") ?: return streams

        fun parseFormats(array: JsonArray?, isAdaptive: Boolean) {
            array?.forEach { el ->
                val f = el.asJsonObject
                val url = f.get("url")?.asString ?: decipherUrl(f) ?: return@forEach
                streams.add(StreamUrl(
                    url = url,
                    mimeType = f.get("mimeType")?.asString ?: "",
                    bitrate = f.get("bitrate")?.asInt ?: 0,
                    quality = f.get("qualityLabel")?.asString ?: f.get("quality")?.asString ?: "",
                    width = f.get("width")?.asInt,
                    height = f.get("height")?.asInt,
                    fps = f.get("fps")?.asInt,
                    contentLength = f.get("contentLength")?.asLong,
                    isAdaptive = isAdaptive,
                    itag = f.get("itag")?.asInt ?: 0
                ))
            }
        }

        parseFormats(streamingData.getAsJsonArray("adaptiveFormats"), true)
        parseFormats(streamingData.getAsJsonArray("formats"), false)

        // ↓ These fields are INTENTIONALLY never read — they contain ad data:
        // streamingData["adPlacements"]
        // streamingData["adSlots"]
        // playerResp["adBreakHeartbeatParams"]
        // playerResp["paidContentOverlay"]

        return streams.sortedByDescending { it.bitrate }
    }

    fun extractVideoDetails(playerResp: JsonObject): VideoDetails? {
        val vd = playerResp.getAsJsonObject("videoDetails") ?: return null
        return VideoDetails(
            videoId = vd.get("videoId")?.asString ?: return null,
            title = vd.get("title")?.asString ?: "",
            author = vd.get("author")?.asString ?: "",
            channelId = vd.get("channelId")?.asString ?: "",
            viewCount = vd.get("viewCount")?.asLong ?: 0,
            lengthSeconds = vd.get("lengthSeconds")?.asLong ?: 0,
            description = vd.get("shortDescription")?.asString ?: "",
            isLive = vd.get("isLiveContent")?.asBoolean ?: false,
            thumbnails = vd.getAsJsonArray("thumbnail")
                ?.firstOrNull()?.asJsonObject
                ?.getAsJsonArray("thumbnails")
                ?.map { it.asJsonObject.get("url")?.asString ?: "" }
                ?: listOf("https://i.ytimg.com/vi/${vd.get("videoId")?.asString}/maxresdefault.jpg")
        )
    }

    // ── Private Helpers ───────────────────────────────────────────

    private fun decipherUrl(fmt: JsonObject): String? {
        val cipher = fmt.get("signatureCipher")?.asString ?: fmt.get("cipher")?.asString ?: return null
        return cipher.split("&")
            .associate { pair -> pair.split("=", limit = 2).let { it[0] to URLDecoder.decode(it[1], "UTF-8") } }["url"]
    }

    private fun post(url: String, body: JsonObject, client: Client): JsonObject {
        val req = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", client.userAgent)
            .addHeader("X-YouTube-Client-Name", client.id)
            .addHeader("X-YouTube-Client-Version", client.version)
            .addHeader("Origin", "https://www.youtube.com")
            .post(gson.toJson(body).toRequestBody("application/json".toMediaType()))
            .build()
        return try {
            val resp = http.newCall(req).execute()
            gson.fromJson(resp.body?.string() ?: "{}", JsonObject::class.java)
        } catch (e: Exception) { JsonObject() }
    }

    private fun generateCpn(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"
        return (1..16).map { chars[chars.indices.random()] }.joinToString("")
    }

    // ── Data Classes ──────────────────────────────────────────────

    data class StreamUrl(
        val url: String,
        val mimeType: String,
        val bitrate: Int,
        val quality: String,
        val width: Int?,
        val height: Int?,
        val fps: Int?,
        val contentLength: Long?,
        val isAdaptive: Boolean,
        val itag: Int
    ) {
        val isVideo get() = mimeType.startsWith("video/")
        val isAudio get() = mimeType.startsWith("audio/")
        val resolutionLabel get() = height?.let { "${it}p" } ?: quality
    }

    data class VideoDetails(
        val videoId: String,
        val title: String,
        val author: String,
        val channelId: String,
        val viewCount: Long,
        val lengthSeconds: Long,
        val description: String,
        val isLive: Boolean,
        val thumbnails: List<String>
    ) {
        val bestThumbnail get() = thumbnails.lastOrNull() ?: ""
        val durationFormatted get() = String.format("%d:%02d:%02d",
            lengthSeconds / 3600, (lengthSeconds % 3600) / 60, lengthSeconds % 60)
            .trimStart('0', ':').ifEmpty { "0:00" }
    }
}
