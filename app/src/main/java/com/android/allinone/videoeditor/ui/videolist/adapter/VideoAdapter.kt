package com.android.allinone.videoeditor.ui.videolist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.allinone.videoeditor.databinding.ItemVideoBinding
import com.android.allinone.videoeditor.model.Video

class VideoAdapter : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    private val videoList = mutableListOf<Video>()
    private var itemClickListener: (String) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(videoList[position])
    }

    override fun getItemCount(): Int = videoList.size

    fun setItemClickListener(clickListener: (String) -> Unit) {
        itemClickListener = clickListener
    }

    fun setData(list: MutableList<Video>) {
        videoList.clear()
        videoList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(video: Video) {
            binding.apply {
                item = video
                root.setOnClickListener {
                    itemClickListener.invoke(video.path)
                }
            }
        }
    }
}