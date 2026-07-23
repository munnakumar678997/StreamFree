package com.streamfree.app.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.streamfree.app.R
import com.streamfree.app.database.entity.DownloadStatus
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.android.ext.android.inject
import java.io.File

/**
 * Background download service.
 * Downloads video/audio files to external storage with progress tracking.
 */
class DownloadService : Service() {

    private val downloadRepo: DownloadRepository by inject()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val notifManager by lazy { getSystemService(NotificationManager::class.java) }
    private val activeJobs = mutableMapOf<String, Job>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Starting downloads...", 0))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DOWNLOAD -> {
                val videoId = intent.getStringExtra(EXTRA_VIDEO_ID) ?: return START_NOT_STICKY
                val url     = intent.getStringExtra(EXTRA_URL)      ?: return START_NOT_STICKY
                val title   = intent.getStringExtra(EXTRA_TITLE)    ?: "download"
                val quality = intent.getStringExtra(EXTRA_QUALITY)  ?: "best"
                startDownload(videoId, url, title, quality)
            }
            ACTION_CANCEL -> {
                val videoId = intent.getStringExtra(EXTRA_VIDEO_ID) ?: return START_NOT_STICKY
                activeJobs[videoId]?.cancel()
                scope.launch { downloadRepo.updateStatus(videoId, DownloadStatus.FAILED) }
            }
        }
        return START_STICKY
    }

    private fun startDownload(videoId: String, url: String, title: String, quality: String) {
        val job = scope.launch {
            try {
                downloadRepo.updateStatus(videoId, DownloadStatus.DOWNLOADING)
                val ext = if (url.contains("audio")) "m4a" else "mp4"
                val file = File(getExternalFilesDir(null), "StreamFree/${sanitize(title)}_$quality.$ext")
                file.parentFile?.mkdirs()

                val client = OkHttpClient()
                val resp = client.newCall(Request.Builder().url(url).build()).execute()
                val body = resp.body ?: throw Exception("Empty response")
                val total = body.contentLength()

                file.outputStream().use { out ->
                    body.byteStream().use { inp ->
                        val buf = ByteArray(8192)
                        var downloaded = 0L
                        var read: Int
                        while (inp.read(buf).also { read = it } != -1) {
                            out.write(buf, 0, read)
                            downloaded += read
                            val progress = if (total > 0) (downloaded * 100 / total).toInt() else 0
                            downloadRepo.updateProgress(videoId, progress)
                            updateNotification(title, progress)
                        }
                    }
                }

                downloadRepo.updateStatus(videoId, DownloadStatus.COMPLETED,
                    filePath = file.absolutePath, fileSize = file.length())
                updateNotification("$title — Complete", 100)

            } catch (e: CancellationException) {
                downloadRepo.updateStatus(videoId, DownloadStatus.FAILED)
            } catch (e: Exception) {
                downloadRepo.updateStatus(videoId, DownloadStatus.FAILED)
                updateNotification("$title — Failed", 0)
            } finally {
                activeJobs.remove(videoId)
                if (activeJobs.isEmpty()) stopSelf()
            }
        }
        activeJobs[videoId] = job
    }

    private fun sanitize(name: String) = name.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(80)

    private fun updateNotification(title: String, progress: Int) {
        notifManager.notify(NOTIF_ID, buildNotification(title, progress))
    }

    private fun buildNotification(title: String, progress: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading")
            .setContentText(title)
            .setSmallIcon(R.drawable.ic_download)
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_ID, "Downloads", NotificationManager.IMPORTANCE_LOW)
                .also { notifManager.createNotificationChannel(it) }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_DOWNLOAD = "com.streamfree.DOWNLOAD"
        const val ACTION_CANCEL   = "com.streamfree.CANCEL_DOWNLOAD"
        const val EXTRA_VIDEO_ID  = "extra_video_id"
        const val EXTRA_URL       = "extra_url"
        const val EXTRA_TITLE     = "extra_title"
        const val EXTRA_QUALITY   = "extra_quality"
        const val CHANNEL_ID      = "streamfree_download"
        const val NOTIF_ID        = 1002
    }
}
