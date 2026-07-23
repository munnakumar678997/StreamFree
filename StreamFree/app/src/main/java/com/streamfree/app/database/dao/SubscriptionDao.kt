package com.streamfree.app.database.dao

import androidx.room.*
import com.streamfree.app.database.entity.SubscriptionEntity
import io.reactivex.rxjava3.core.Flowable

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY channelName ASC")
    fun getAll(): Flowable<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE channelId = :channelId LIMIT 1")
    suspend fun get(channelId: String): SubscriptionEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM subscriptions WHERE channelId = :channelId)")
    suspend fun isSubscribed(channelId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SubscriptionEntity)

    @Delete
    suspend fun delete(entity: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE channelId = :channelId")
    suspend fun deleteById(channelId: String)

    @Query("SELECT COUNT(*) FROM subscriptions")
    suspend fun count(): Int
}
