package com.streamfree.app.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val channelId: String,
    val channelName: String,
    val channelUrl: String,
    val avatarUrl: String,
    val subscriberCount: Long = -1,
    val subscribedAt: Long = System.currentTimeMillis(),
    val lastChecked: Long = 0
)
