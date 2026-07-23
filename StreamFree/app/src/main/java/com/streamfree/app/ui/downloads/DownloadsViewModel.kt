package com.streamfree.app.ui.downloads

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import com.streamfree.app.database.entity.DownloadEntity
import com.streamfree.app.download.DownloadRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadsViewModel(private val repo: DownloadRepository) : ViewModel() {
    val downloads: LiveData<List<DownloadEntity>> =
        repo.getAll().observeOn(AndroidSchedulers.mainThread()).toLiveData()

    fun deleteDownload(videoId: String) = CoroutineScope(Dispatchers.IO).launch {
        repo.delete(videoId)
    }
}
