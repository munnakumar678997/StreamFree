package com.streamfree.app.player

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.streamfree.app.R

/**
 * Floating popup player — watch video while using other apps.
 * Uses SYSTEM_ALERT_WINDOW permission to show overlay.
 */
@UnstableApi
class PopupPlayerService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var player: ExoPlayer? = null

    private var initialX = 0; private var initialY = 0
    private var initialTouchX = 0f; private var initialTouchY = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""

        showPopup(url, title)
        return START_STICKY
    }

    private fun showPopup(url: String, title: String) {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.popup_player, null)

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            600, 340,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100; y = 200
        }

        player = ExoPlayer.Builder(this).build()
        floatingView?.findViewById<PlayerView>(R.id.popup_player_view)?.player = player
        floatingView?.findViewById<TextView>(R.id.popup_title)?.text = title

        player?.apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }

        // Drag to move
        floatingView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y
                    initialTouchX = event.rawX; initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(floatingView, params)
                    true
                }
                else -> false
            }
        }

        // Close button
        floatingView?.findViewById<ImageButton>(R.id.popup_close)?.setOnClickListener { stopSelf() }

        windowManager?.addView(floatingView, params)
    }

    override fun onDestroy() {
        player?.release()
        floatingView?.let { windowManager?.removeView(it) }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_URL   = "extra_url"
        const val EXTRA_TITLE = "extra_title"
    }
}
