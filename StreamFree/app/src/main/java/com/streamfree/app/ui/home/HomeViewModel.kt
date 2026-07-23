package com.streamfree.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.streamfree.app.database.dao.HistoryDao
import com.streamfree.app.extractor.YouTubeExtractor
import com.streamfree.app.model.VideoItem
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

class HomeViewModel(private val historyDao: HistoryDao) : ViewModel() {

    private val _videos  = MutableLiveData<List<VideoItem>>()
    private val _loading = MutableLiveData<Boolean>()
    private val _error   = MutableLiveData<String?>()

    val videos: LiveData<List<VideoItem>> = _videos
    val loading: LiveData<Boolean> = _loading
    val error: LiveData<String?> = _error

    private val disposables = CompositeDisposable()

    fun loadTrending() {
        _loading.value = true
        _error.value = null
        val d = YouTubeExtractor.getTrending()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ items ->
                _videos.value = items
                _loading.value = false
            }, { err ->
                _error.value = "Failed to load trending: ${err.message}"
                _loading.value = false
            })
        disposables.add(d)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
