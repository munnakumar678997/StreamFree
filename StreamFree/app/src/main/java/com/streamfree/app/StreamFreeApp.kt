package com.streamfree.app

import android.app.Application
import com.streamfree.app.di.appModule
import com.streamfree.app.extractor.DownloaderImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.schabi.newpipe.extractor.NewPipe

class StreamFreeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize NewPipeExtractor with our ad-free downloader.
        // DownloaderImpl uses ANDROID Innertube client — streams returned
        // have no adPlacements. We never request or parse ad metadata.
        NewPipe.init(DownloaderImpl.getInstance())

        // Koin dependency injection
        startKoin {
            androidContext(this@StreamFreeApp)
            modules(appModule)
        }
    }

    companion object {
        lateinit var instance: StreamFreeApp
            private set
    }
}
