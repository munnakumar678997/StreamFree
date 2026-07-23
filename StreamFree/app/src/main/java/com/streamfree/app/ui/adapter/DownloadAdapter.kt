package com.streamfree.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.streamfree.app.databinding.ItemDownloadBinding
import com.streamfree.app.database.entity.DownloadEntity
import com.streamfree.app.database.entity.DownloadStatus

class DownloadAdapter(
    private val onDelete: (DownloadEntity) -> Unit
) : ListAdapter<DownloadEntity, DownloadAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemDownloadBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: DownloadEntity) {
            b.tvTitle.text    = item.title
            b.tvQuality.text  = item.quality
            b.tvStatus.text   = item.status.name
            b.progressBar.progress = item.progress
            b.progressBar.visibility = if (item.status == DownloadStatus.DOWNLOADING) android.view.View.VISIBLE else android.view.View.GONE
            Glide.with(b.root).load(item.thumbnailUrl).into(b.ivThumbnail)
            b.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<DownloadEntity>() {
            override fun areItemsTheSame(a: DownloadEntity, b: DownloadEntity) = a.videoId == b.videoId
            override fun areContentsTheSame(a: DownloadEntity, b: DownloadEntity) = a == b
        }
    }
}
