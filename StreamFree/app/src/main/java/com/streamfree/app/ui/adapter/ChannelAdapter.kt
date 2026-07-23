package com.streamfree.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.streamfree.app.databinding.ItemChannelBinding
import com.streamfree.app.database.entity.SubscriptionEntity

class ChannelAdapter(
    private val onClick: (SubscriptionEntity) -> Unit
) : ListAdapter<SubscriptionEntity, ChannelAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemChannelBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: SubscriptionEntity) {
            b.tvChannelName.text = item.channelName
            Glide.with(b.root).load(item.avatarUrl)
                .transform(CircleCrop())
                .placeholder(android.R.drawable.ic_menu_myplaces)
                .into(b.ivAvatar)
            b.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<SubscriptionEntity>() {
            override fun areItemsTheSame(a: SubscriptionEntity, b: SubscriptionEntity) = a.channelId == b.channelId
            override fun areContentsTheSame(a: SubscriptionEntity, b: SubscriptionEntity) = a == b
        }
    }
}
