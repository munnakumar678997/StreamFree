package com.streamfree.app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChannelItem(
    val channelId: String,
    val name: String,
    val avatarUrl: String,
    val subscriberCount: Long = -1,
    val description: String = "",
    val url: String = "",
    val isSubscribed: Boolean = false
) : Parcelable {
    val subscriberCountFormatted: String get() = when {
        subscriberCount < 0            -> ""
        subscriberCount >= 1_000_000   -> "${subscriberCount / 1_000_000}M subscribers"
        subscriberCount >= 1_000       -> "${subscriberCount / 1_000}K subscribers"
        else                           -> "$subscriberCount subscribers"
    }
}
