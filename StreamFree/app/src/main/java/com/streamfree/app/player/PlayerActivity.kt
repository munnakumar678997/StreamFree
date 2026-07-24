package com.streamfree.app.player

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.streamfree.app.databinding.ActivityPlayerBinding
import com.streamfree.app.extractor.InnerTubeClient
import com.streamfree.app.extractor.YouTubeExtractor
import com.streamfree.app.model.StreamQuality
import com.streamfree.app.sponsorblock.SponsorBlockRepository
import com.streamfree.app.sponsorblock.SponsorSegment
import com.streamfree.app.util.gone
import com.streamfree.app.util.show
import com.streamfree.app.util.toast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.android.ext.android.inject

@UnstableApi
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var player: ExoPlayer
    private val sponsorBlockRepo: SponsorBlockRepository by inject()
    private val disposables = CompositeDisposable()

    private var videoUrl: String = ""
    private var videoId: String = ""
    private var videoTitle: String = ""
    private var sponsorSegments: List<SponsorSegment> = emptyList()
    private var qualities: List<StreamQuality> = emptyList()
    private var isInPiP = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFullscreen()

        videoUrl = intent.getStringExtra(EXTRA_URL) ?: run { finish(); return }
        videoId  = YouTubeExtractor.extractVideoId(videoUrl)
        videoTitle = intent.getStringExtra(EXTRA_TITLE) ?: ""

        binding.tvTitle.text = videoTitle

        initPlayer()
        loadStreams()
        loadSponsorBlock()
        setupButtons()
    }

    private fun setupFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(this).build().also {
            binding.playerView.player = it
            it.addListener(playerListener)
        }
    }

    private fun loadStreams() {
        binding.progressBar.show()
        val disposable = YouTubeExtractor.getStreamInfo(videoUrl)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ streamInfo ->
                binding.progressBar.gone()
                // Map NewPipeExtractor streams to our model
                qualities = streamInfo.videoStreams.map { vs ->
                    StreamQuality(
                        label = vs.resolution ?: "Unknown",
                        url = vs.content ?: "",
                        mimeType = vs.format?.mimeType ?: "video/mp4",
                        bitrate = vs.bitrate,
                        height = vs.height,
                        width = vs.width,
                        fps = vs.fps,
                        isAdaptive = false,
                        itag = vs.itagItem?.id ?: 0
                    )
                }.filter { it.url.isNotEmpty() }
                  .sortedByDescending { it.height }

                val best = qualities.firstOrNull { it.isFullHD }
                    ?: qualities.firstOrNull { it.isHD }
                    ?: qualities.firstOrNull()

                best?.let { playUrl(it.url) }
                    ?: toast("No playable stream found")

            }, { error ->
                binding.progressBar.gone()
                toast("Failed to load video: ${error.message}")
                // Fallback to direct Innertube
                loadStreamsDirectly()
            })
        disposables.add(disposable)
    }

    /**
     * Fallback: use our direct InnerTubeClient (ANDROID client, no ads).
     * This runs if NewPipeExtractor fails.
     */
    private fun loadStreamsDirectly() {
        binding.progressBar.show()
        val disposable = io.reactivex.rxjava3.core.Single.fromCallable {
            val resp = InnerTubeClient.playerResponse(videoId)
            InnerTubeClient.extractStreams(resp)
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ streams ->
            binding.progressBar.gone()
            val videoStreams = streams.filter { it.isVideo && !it.isAdaptive }
                .sortedByDescending { it.bitrate }
            val best = videoStreams.firstOrNull { (it.height ?: 0) >= 1080 }
                ?: videoStreams.firstOrNull { (it.height ?: 0) >= 720 }
                ?: videoStreams.firstOrNull()
            best?.let { playUrl(it.url) } ?: toast("No streams available")
        }, { e ->
            binding.progressBar.gone()
            toast("Error: ${e.message}")
        })
        disposables.add(disposable)
    }

    private fun playUrl(url: String) {
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true
    }

    private fun loadSponsorBlock() {
        if (videoId.isEmpty()) return
        val disposable = io.reactivex.rxjava3.core.Single.fromCallable {
            kotlinx.coroutines.runBlocking { sponsorBlockRepo.getSegments(videoId) }
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ segments ->
            sponsorSegments = segments
            if (segments.isNotEmpty()) {
                binding.tvSponsorSkip.text = "SponsorBlock: ${segments.size} segments loaded"
            }
        }, {})
        disposables.add(disposable)
    }

    private fun setupButtons() {
        // Quality selector
        binding.btnQuality.setOnClickListener { showQualityDialog() }

        // PiP
        binding.btnPip.setOnClickListener { enterPiP() }

        // Background play
        binding.btnBackground.setOnClickListener {
            startService(Intent(this, PlayerService::class.java).apply {
                putExtra(PlayerService.EXTRA_URL, videoUrl)
                putExtra(PlayerService.EXTRA_TITLE, videoTitle)
            })
            toast("Playing in background")
            finish()
        }

        // Skip sponsor button
        binding.btnSkipSponsor.setOnClickListener {
            val pos = player.currentPosition
            val seg = SponsorBlockRepository.shouldSkip(pos, sponsorSegments)
            seg?.let { player.seekTo(it.endMs) }
        }
    }

    private fun showQualityDialog() {
        if (qualities.isEmpty()) return
        val labels = qualities.map { it.label }.toTypedArray()
        android.app.AlertDialog.Builder(this)
            .setTitle("Select Quality")
            .setItems(labels) { _, i ->
                val pos = player.currentPosition
                playUrl(qualities[i].url)
                player.seekTo(pos)
            }
            .show()
    }

    private fun enterPiP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(inPiP: Boolean, config: Configuration) {
        super.onPictureInPictureModeChanged(inPiP, config)
        isInPiP = inPiP
        binding.controlsOverlay.visibility = if (inPiP) View.GONE else View.VISIBLE
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (player.isPlaying) enterPiP()
    }

    override fun onStop() {
        super.onStop()
        if (!isInPiP) player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        disposables.clear()
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_BUFFERING -> binding.progressBar.show()
                Player.STATE_READY     -> {
                    binding.progressBar.gone()
                    checkSponsorSegment()
                }
                Player.STATE_ENDED     -> binding.progressBar.gone()
                else -> {}
            }
        }
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            window.apply {
                if (isPlaying) addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    private fun checkSponsorSegment() {
        if (sponsorSegments.isEmpty()) return
        player.addListener(object : Player.Listener {
            override fun onPositionDiscontinuity(
                oldPos: Player.PositionInfo, newPos: Player.PositionInfo, reason: Int
            ) {
                val seg = SponsorBlockRepository.shouldSkip(newPos.positionMs, sponsorSegments)
                if (seg != null) {
                    player.seekTo(seg.endMs)
                    binding.tvSponsorSkip.text = "Skipped: ${seg.category}"
                    binding.tvSponsorSkip.show()
                    binding.tvSponsorSkip.postDelayed({ binding.tvSponsorSkip.gone() }, 2000)
                }
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {}
        })
    }

    companion object {
        const val EXTRA_URL   = "extra_url"
        const val EXTRA_TITLE = "extra_title"
    }
}
