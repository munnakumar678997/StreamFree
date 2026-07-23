package com.streamfree.app.ui.subscriptions

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import com.google.gson.Gson
import com.streamfree.app.database.dao.SubscriptionDao
import com.streamfree.app.database.entity.SubscriptionEntity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SubscriptionsViewModel(private val dao: SubscriptionDao) : ViewModel() {

    val subscriptions: LiveData<List<SubscriptionEntity>> =
        dao.getAll().observeOn(AndroidSchedulers.mainThread()).toLiveData()

    fun exportSubscriptions(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val subs = subscriptions.value ?: return@launch
            val json = Gson().toJson(subs)
            val file = File(context.getExternalFilesDir(null), "StreamFree/subscriptions_export.json")
            file.parentFile?.mkdirs()
            file.writeText(json)
        }
    }

    fun importSubscriptions(context: Context) {
        // TODO: Open file picker, parse JSON, insert subscriptions
        // Implementation requires ActivityResultLauncher in Fragment
    }
}
