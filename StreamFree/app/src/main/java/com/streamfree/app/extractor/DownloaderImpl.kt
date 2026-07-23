package com.streamfree.app.extractor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request as ExtractorRequest
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.util.concurrent.TimeUnit

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║            AD-FREE MECHANISM — READ THIS CAREFULLY            ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║  NewPipe/StreamFree does NOT "block" ads in the traditional   ║
 * ║  sense. Instead, we use YouTube's internal Innertube API      ║
 * ║  with the ANDROID client. The response contains:             ║
 * ║                                                               ║
 * ║   streamingData.adaptiveFormats  ← USED (video/audio URLs)   ║
 * ║   streamingData.formats          ← USED (combined streams)   ║
 * ║   adPlacements                   ← IGNORED (never parsed)    ║
 * ║   adSlots                        ← IGNORED (never parsed)    ║
 * ║   adBreakHeartbeatParams         ← IGNORED (never parsed)    ║
 * ║   adVideoId                      ← IGNORED (never parsed)    ║
 * ║                                                               ║
 * ║  No ad server is ever contacted. No ad URL is ever called.   ║
 * ║  Ads simply don't exist in our data pipeline.                ║
 * ╚═══════════════════════════════════════════════════════════════╝
 *
 * Technically: we mimic YouTube's own Android app by sending
 *   X-YouTube-Client-Name: 3   (ANDROID client ID)
 *   X-YouTube-Client-Version: 21.03.36
 *   User-Agent: com.google.android.youtube/21.03.36 ...
 *
 * YouTube returns exactly the same streamingData it gives to
 * the Android app — direct stream URLs with no ad injection.
 */
class DownloaderImpl private constructor() : Downloader() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    override fun execute(request: ExtractorRequest): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val requestBuilder = Request.Builder().url(url)

        // Android client headers — key to receiving ad-free stream responses
        requestBuilder.addHeader("User-Agent", USER_AGENT)
        requestBuilder.addHeader("Accept-Language", "en-US,en;q=0.9")
        requestBuilder.addHeader("X-YouTube-Client-Name", ANDROID_CLIENT_ID)
        requestBuilder.addHeader("X-YouTube-Client-Version", ANDROID_CLIENT_VERSION)

        // Apply any headers from NewPipeExtractor (e.g., Authorization for age-gated)
        headers?.forEach { (key, values) ->
            values.forEach { value -> requestBuilder.addHeader(key, value) }
        }

        when (httpMethod) {
            "GET"  -> requestBuilder.get()
            "POST" -> {
                val body = dataToSend ?: ByteArray(0)
                requestBuilder.post(body.toRequestBody("application/json; charset=utf-8".toMediaType()))
            }
            "HEAD" -> requestBuilder.head()
            else   -> throw Exception("Unsupported HTTP method: $httpMethod")
        }

        val response = client.newCall(requestBuilder.build()).execute()

        if (response.code == 429) {
            throw ReCaptchaException("Rate-limited by YouTube — try again later", url)
        }

        val responseBody = response.body?.string() ?: ""
        return Response(
            response.code,
            response.message,
            response.headers.toMultimap(),
            responseBody,
            response.request.url.toString()
        )
    }

    companion object {
        /**
         * ANDROID Innertube client identifier.
         * clientName "3" = ANDROID in YouTube's internal enum.
         * This is what YouTube's own Android app sends.
         */
        const val ANDROID_CLIENT_ID = "3"
        const val ANDROID_CLIENT_VERSION = "21.03.36"

        /**
         * User-Agent matching YouTube Android app.
         * Wrong UA → YouTube may return a different (web) response that includes ad logic.
         */
        const val USER_AGENT =
            "com.google.android.youtube/$ANDROID_CLIENT_VERSION (Linux; U; Android 16; en_US) gzip"

        @Volatile private var instance: DownloaderImpl? = null

        fun getInstance(): DownloaderImpl = instance ?: synchronized(this) {
            instance ?: DownloaderImpl().also { instance = it }
        }
    }
}
