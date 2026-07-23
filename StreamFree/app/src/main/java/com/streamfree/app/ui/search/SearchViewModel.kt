package com.streamfree.app.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.streamfree.app.extractor.YouTubeExtractor
import com.streamfree.app.model.VideoItem
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

class SearchViewModel(query: String) : ViewModel() {

    private val _results = MutableLiveData<List<VideoItem>>()
    private val _loading = MutableLiveData<Boolean>()
    private val _error   = MutableLiveData<String?>()

    val results: LiveData<List<VideoItem>> = _results
    val loading: LiveData<Boolean> = _loading
    val error: LiveData<String?> = _error

    private val disposables = CompositeDisposable()

    init {
        search(query)
    }

    private fun search(query: String) {
        _loading.value = true
        _error.value = null
        val d = YouTubeExtractor.search(query)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ items ->
                _results.value = items
                _loading.value = false
            }, { err ->
                _error.value = "Search failed: ${err.message}"
                _loading.value = false
            })
        disposables.add(d)
    }

    override fun onCleared() { disposables.clear(); super.onCleared() }
}
