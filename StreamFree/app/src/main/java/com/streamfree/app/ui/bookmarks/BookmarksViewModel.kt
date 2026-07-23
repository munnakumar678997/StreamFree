package com.streamfree.app.ui.bookmarks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import com.streamfree.app.database.dao.BookmarkDao
import com.streamfree.app.database.entity.BookmarkEntity
import com.streamfree.app.model.VideoItem
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookmarksViewModel(private val dao: BookmarkDao) : ViewModel() {
    val bookmarks: LiveData<List<VideoItem>> = dao.getAll()
        .map { list -> list.map { b ->
            VideoItem(b.videoId, b.title, b.thumbnailUrl, b.uploaderName, duration = b.duration,
                url = "https://www.youtube.com/watch?v=${b.videoId}")
        }}
        .observeOn(AndroidSchedulers.mainThread())
        .toLiveData()

    fun removeBookmark(videoId: String) = CoroutineScope(Dispatchers.IO).launch {
        dao.deleteById(videoId)
    }
}
