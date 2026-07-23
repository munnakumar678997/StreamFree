package com.streamfree.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.streamfree.app.databinding.ItemVideoBinding
import com.streamfree.app.model.VideoItem

class VideoAdapter(
    private val onClick: (VideoItem) -> Unit
) : ListAdapter<VideoItem, VideoAdapter.VideoViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VideoViewHolder(ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class VideoViewHolder(private val binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VideoItem) {
            binding.tvTitle.text       = item.title
            binding.tvUploader.text    = item.uploaderName
            binding.tvViews.text       = item.viewCountFormatted
            binding.tvDuration.text    = item.durationFormatted
            Glide.with(binding.root)
                .load(item.thumbnailUrl)
                .placeholder(android.R.drawable.ic_media_play)
                .into(binding.ivThumbnail)
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<VideoItem>() {
            override fun areItemsTheSame(a: VideoItem, b: VideoItem) = a.videoId == b.videoId
            override fun areContentsTheSame(a: VideoItem, b: VideoItem) = a == b
        }
    }
}
