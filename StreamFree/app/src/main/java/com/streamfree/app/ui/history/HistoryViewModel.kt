package com.streamfree.app.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import com.streamfree.app.database.dao.HistoryDao
import com.streamfree.app.database.entity.HistoryEntity
import com.streamfree.app.model.VideoItem
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel(private val dao: HistoryDao) : ViewModel() {

    val history: LiveData<List<VideoItem>> = dao.getAll()
        .map { list -> list.map { h ->
            VideoItem(h.videoId, h.title, h.thumbnailUrl, h.uploaderName, duration = h.duration,
                url = "https://www.youtube.com/watch?v=${h.videoId}")
        }}
        .observeOn(AndroidSchedulers.mainThread())
        .toLiveData()

    fun clearHistory() = CoroutineScope(Dispatchers.IO).launch { dao.clearAll() }
}
