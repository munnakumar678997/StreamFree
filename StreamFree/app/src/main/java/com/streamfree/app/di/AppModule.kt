package com.streamfree.app.di

import com.streamfree.app.database.AppDatabase
import com.streamfree.app.download.DownloadRepository
import com.streamfree.app.sponsorblock.SponsorBlockApi
import com.streamfree.app.sponsorblock.SponsorBlockRepository
import com.streamfree.app.ui.home.HomeViewModel
import com.streamfree.app.ui.search.SearchViewModel
import com.streamfree.app.ui.detail.VideoDetailViewModel
import com.streamfree.app.ui.subscriptions.SubscriptionsViewModel
import com.streamfree.app.ui.history.HistoryViewModel
import com.streamfree.app.ui.downloads.DownloadsViewModel
import com.streamfree.app.ui.bookmarks.BookmarksViewModel
import com.google.gson.Gson
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {

    // ── Database ──────────────────────────────────────────────────
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().historyDao() }
    single { get<AppDatabase>().subscriptionDao() }
    single { get<AppDatabase>().bookmarkDao() }
    single { get<AppDatabase>().downloadDao() }

    // ── Networking ────────────────────────────────────────────────
    single {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    single { Gson() }

    // ── SponsorBlock ──────────────────────────────────────────────
    single {
        Retrofit.Builder()
            .baseUrl("https://sponsor.ajay.app/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
            .create(SponsorBlockApi::class.java)
    }
    single { SponsorBlockRepository(get()) }

    // ── Repositories ──────────────────────────────────────────────
    single { DownloadRepository(get(), androidContext()) }

    // ── ViewModels ────────────────────────────────────────────────
    viewModel { HomeViewModel(get()) }
    viewModel { (query: String) -> SearchViewModel(query) }
    viewModel { (url: String) -> VideoDetailViewModel(url, get(), get(), get(), get()) }
    viewModel { SubscriptionsViewModel(get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { DownloadsViewModel(get()) }
    viewModel { BookmarksViewModel(get()) }
}
