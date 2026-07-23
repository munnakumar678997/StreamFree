package com.streamfree.app.extractor

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.playlist.PlaylistInfo
import org.schabi.newpipe.extractor.kiosk.KioskInfo
import com.streamfree.app.model.VideoItem
import com.streamfree.app.model.ChannelItem

/**
 * High-level extractor that wraps NewPipeExtractor library calls.
 * All extraction runs on IO threads via RxJava.
 *
 * Ad-free guarantee: NewPipeExtractor itself only parses
 * streamingData from the ANDROID Innertube client (via our DownloaderImpl).
 * Ad metadata is never in scope.
 */
object YouTubeExtractor {

    private val youtube get() = NewPipe.getService(0) // YouTube = service 0

    // ── Search ─────────────────────────────────────────────────────

    fun search(query: String): Single<List<VideoItem>> = Single.fromCallable {
        val searchInfo = SearchInfo.getInfo(youtube, youtube.searchQHFactory.fromQuery(query))
        searchInfo.relatedItems.mapNotNull { item ->
            if (item is org.schabi.newpipe.extractor.stream.StreamInfoItem) {
                VideoItem(
                    videoId = extractVideoId(item.url),
                    title = item.name,
                    thumbnailUrl = item.thumbnailUrl ?: "",
                    uploaderName = item.uploaderName ?: "",
                    viewCount = item.viewCount,
                    duration = item.duration,
                    uploadDate = item.textualUploadDate ?: "",
                    url = item.url
                )
            } else null
        }
    }.subscribeOn(Schedulers.io())

    // ── Stream Info (Video Details + Streams) ─────────────────────

    fun getStreamInfo(url: String): Single<StreamInfo> = Single.fromCallable {
        StreamInfo.getInfo(youtube, url)
    }.subscribeOn(Schedulers.io())

    // ── Trending ──────────────────────────────────────────────────

    fun getTrending(): Single<List<VideoItem>> = Single.fromCallable {
        val kioskList = youtube.kioskList
        val kioskInfo = KioskInfo.getInfo(youtube, kioskList.defaultKioskId)
        kioskInfo.relatedItems.mapNotNull { item ->
            if (item is org.schabi.newpipe.extractor.stream.StreamInfoItem) {
                VideoItem(
                    videoId = extractVideoId(item.url),
                    title = item.name,
                    thumbnailUrl = item.thumbnailUrl ?: "",
                    uploaderName = item.uploaderName ?: "",
                    viewCount = item.viewCount,
                    duration = item.duration,
                    uploadDate = item.textualUploadDate ?: "",
                    url = item.url
                )
            } else null
        }
    }.subscribeOn(Schedulers.io())

    // ── Channel Info ──────────────────────────────────────────────

    fun getChannelInfo(url: String): Single<ChannelInfo> = Single.fromCallable {
        ChannelInfo.getInfo(youtube, url)
    }.subscribeOn(Schedulers.io())

    // ── Playlist Info ─────────────────────────────────────────────

    fun getPlaylistInfo(url: String): Single<PlaylistInfo> = Single.fromCallable {
        PlaylistInfo.getInfo(youtube, url)
    }.subscribeOn(Schedulers.io())

    // ── Helpers ───────────────────────────────────────────────────

    fun extractVideoId(url: String): String {
        return try {
            youtube.streamLHFactory.getId(url)
        } catch (e: Exception) {
            // Fallback regex for youtu.be/ID or ?v=ID
            Regex("(?:v=|youtu\\.be/)([a-zA-Z0-9_-]{11})").find(url)?.groupValues?.get(1) ?: ""
        }
    }

    fun isYouTubeUrl(url: String): Boolean =
        url.contains("youtube.com") || url.contains("youtu.be")

    fun isSoundCloudUrl(url: String): Boolean = url.contains("soundcloud.com")
}
