package com.streamfree.app.ui.detail

import android.content.Context
import android.content.Intent
import android.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamfree.app.database.dao.BookmarkDao
import com.streamfree.app.database.dao.HistoryDao
import com.streamfree.app.database.dao.SubscriptionDao
import com.streamfree.app.database.entity.BookmarkEntity
import com.streamfree.app.database.entity.HistoryEntity
import com.streamfree.app.database.entity.SubscriptionEntity
import com.streamfree.app.database.entity.DownloadEntity
import com.streamfree.app.download.DownloadRepository
import com.streamfree.app.download.DownloadService
import com.streamfree.app.extractor.YouTubeExtractor
import com.streamfree.app.extractor.InnerTubeClient
import com.streamfree.app.model.StreamQuality
import com.streamfree.app.sponsorblock.ReturnYouTubeDislikeApi
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.stream.StreamInfo

class VideoDetailViewModel(
    private val videoUrl: String,
    private val historyDao: HistoryDao,
    private val subscriptionDao: SubscriptionDao,
    private val bookmarkDao: BookmarkDao,
    private val downloadRepo: DownloadRepository
) : ViewModel() {

    private val _loading      = MutableLiveData(true)
    private val _title        = MutableLiveData<String>()
    private val _uploader     = MutableLiveData<String>()
    private val _channelId    = MutableLiveData<String>()
    private val _channelUrl   = MutableLiveData<String>()
    private val _viewCount    = MutableLiveData<String>()
    private val _likes        = MutableLiveData<String>()
    private val _dislikes     = MutableLiveData<String>()
    private val _description  = MutableLiveData<String>()
    private val _thumbnailUrl = MutableLiveData<String>()
    private val _isSubscribed = MutableLiveData(false)
    private val _isBookmarked = MutableLiveData(false)
    private val _error        = MutableLiveData<String?>()

    val loading: LiveData<Boolean>      = _loading
    val title: LiveData<String>         = _title
    val uploader: LiveData<String>      = _uploader
    val viewCount: LiveData<String>     = _viewCount
    val likes: LiveData<String>         = _likes
    val dislikes: LiveData<String>      = _dislikes
    val description: LiveData<String>   = _description
    val thumbnailUrl: LiveData<String>  = _thumbnailUrl
    val isSubscribed: LiveData<Boolean> = _isSubscribed
    val isBookmarked: LiveData<Boolean> = _isBookmarked
    val error: LiveData<String?>        = _error

    private var streamInfo: StreamInfo? = null
    private var videoId: String = YouTubeExtractor.extractVideoId(videoUrl)
    private val disposables = CompositeDisposable()

    init { loadInfo() }

    private fun loadInfo() {
        _loading.value = true
        val d = YouTubeExtractor.getStreamInfo(videoUrl)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ info ->
                streamInfo = info
                _loading.value = false
                _title.value        = info.name
                _uploader.value     = info.uploaderName
                _channelId.value    = info.uploaderUrl
                _channelUrl.value   = info.uploaderUrl
                _viewCount.value    = formatViews(info.viewCount)
                _likes.value        = formatCount(info.likeCount)
                _description.value  = info.description?.content ?: ""
                _thumbnailUrl.value = info.thumbnails.maxByOrNull { it.height }?.url ?: "https://i.ytimg.com/vi/$videoId/maxresdefault.jpg"

                checkSubscription()
                checkBookmark()
                saveToHistory(info)
                loadDislikes()
            }, { err ->
                _loading.value = false
                _error.value = "Failed to load video: ${err.message}"
            })
        disposables.add(d)
    }

    private fun loadDislikes() {
        viewModelScope.launch {
            val data = ReturnYouTubeDislikeApi.getVotes(videoId)
            data?.let {
                _dislikes.postValue(formatCount(it.dislikes))
                _likes.postValue(formatCount(it.likes))
            }
        }
    }

    private fun checkSubscription() = viewModelScope.launch {
        val cid = _channelId.value ?: return@launch
        _isSubscribed.postValue(subscriptionDao.isSubscribed(cid))
    }

    private fun checkBookmark() = viewModelScope.launch {
        _isBookmarked.postValue(bookmarkDao.isBookmarked(videoId))
    }

    private fun saveToHistory(info: StreamInfo) = viewModelScope.launch {
        historyDao.insert(HistoryEntity(
            videoId = videoId,
            title = info.name,
            thumbnailUrl = info.thumbnails.maxByOrNull { it.height }?.url ?: "",
            uploaderName = info.uploaderName,
            duration = info.duration
        ))
    }

    fun toggleSubscription() = viewModelScope.launch {
        val cid = _channelId.value ?: return@launch
        val channelUrl = _channelUrl.value ?: return@launch
        if (subscriptionDao.isSubscribed(cid)) {
            subscriptionDao.deleteById(cid)
            _isSubscribed.postValue(false)
        } else {
            subscriptionDao.insert(SubscriptionEntity(
                channelId = cid,
                channelName = _uploader.value ?: "",
                channelUrl = channelUrl,
                avatarUrl = ""
            ))
            _isSubscribed.postValue(true)
        }
    }

    fun toggleBookmark() = viewModelScope.launch {
        if (bookmarkDao.isBookmarked(videoId)) {
            bookmarkDao.deleteById(videoId)
            _isBookmarked.postValue(false)
        } else {
            bookmarkDao.insert(BookmarkEntity(
                videoId = videoId,
                title = _title.value ?: "",
                thumbnailUrl = _thumbnailUrl.value ?: "",
                uploaderName = _uploader.value ?: "",
                duration = streamInfo?.duration ?: 0
            ))
            _isBookmarked.postValue(true)
        }
    }

    fun showDownloadDialog(context: Context) {
        val info = streamInfo ?: return
        val qualities = mutableListOf<Pair<String, String>>()

        // Add video qualities
        info.videoStreams.sortedByDescending { it.height }.forEach { vs ->
            if (!vs.content.isNullOrEmpty()) {
                qualities.add("${vs.resolution} (Video)" to vs.content!!)
            }
        }
        // Add audio-only option
        info.audioStreams.firstOrNull()?.let { audio ->
            if (!audio.content.isNullOrEmpty()) {
                qualities.add("Audio only (M4A)" to audio.content!!)
            }
        }

        if (qualities.isEmpty()) { return }

        val labels = qualities.map { it.first }.toTypedArray()
        AlertDialog.Builder(context)
            .setTitle("Download Quality")
            .setItems(labels) { _, i ->
                val (label, url) = qualities[i]
                startDownload(context, videoId, url, _title.value ?: "video", label)
            }
            .show()
    }

    private fun startDownload(context: Context, videoId: String, url: String, title: String, quality: String) {
        viewModelScope.launch {
            downloadRepo.enqueue(DownloadEntity(
                videoId = videoId, title = title,
                thumbnailUrl = _thumbnailUrl.value ?: "",
                uploaderName = _uploader.value ?: "",
                filePath = "", quality = quality
            ))
        }
        context.startService(Intent(context, DownloadService::class.java).apply {
            action = DownloadService.ACTION_DOWNLOAD
            putExtra(DownloadService.EXTRA_VIDEO_ID, videoId)
            putExtra(DownloadService.EXTRA_URL, url)
            putExtra(DownloadService.EXTRA_TITLE, title)
            putExtra(DownloadService.EXTRA_QUALITY, quality)
        })
    }

    private fun formatViews(count: Long) = when {
        count < 0          -> ""
        count >= 1_000_000 -> "${count / 1_000_000}M views"
        count >= 1_000     -> "${count / 1_000}K views"
        else               -> "$count views"
    }

    private fun formatCount(count: Long) = when {
        count < 0          -> "0"
        count >= 1_000_000 -> "${count / 1_000_000}M"
        count >= 1_000     -> "${count / 1_000}K"
        else               -> count.toString()
    }

    override fun onCleared() { disposables.clear(); super.onCleared() }
}
