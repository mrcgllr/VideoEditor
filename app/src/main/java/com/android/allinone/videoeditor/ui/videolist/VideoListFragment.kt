package com.android.allinone.videoeditor.ui.videolist

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.android.allinone.videoeditor.R
import com.android.allinone.videoeditor.databinding.FragmentVideoListBinding
import com.android.allinone.videoeditor.model.Video
import com.android.allinone.videoeditor.ui.videocutter.VideoCutterActivity
import com.android.allinone.videoeditor.ui.videolist.adapter.VideoAdapter
import com.android.allinone.videoeditor.util.ContentUtil
import com.android.allinone.videoeditor.util.IntentExtras

class VideoListFragment : Fragment() {


    private lateinit var binding: FragmentVideoListBinding
    private val videoAdapter = VideoAdapter()
    private val videoList = mutableListOf<Video>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getVideosFromPhone()
        initListeners()
    }

    private fun initListeners() {
        binding.apply {
            rvVideos.adapter = videoAdapter
            videoAdapter.setData(videoList)
            videoAdapter.setItemClickListener {
                val intent = Intent(context, VideoCutterActivity::class.java)
                intent.flags = FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra(IntentExtras.PATH, it)
                requireContext().startActivity(intent)
            }
        }
    }

    private fun getVideosFromPhone(): Boolean {
        val managedQuery = requireActivity().managedQuery(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf("_data", "_id", "_display_name", "duration"),
            null,
            null,
            " _id DESC"
        )
        val count = managedQuery.count
        if (count <= 0) {
            return false
        }
        managedQuery.moveToFirst()
        for (i in 0 until count) {
            val withAppendedPath = Uri.withAppendedPath(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                ContentUtil.getLong(managedQuery)
            )
            videoList.add(
                Video(
                    managedQuery.getString(managedQuery.getColumnIndexOrThrow("_display_name")),
                    withAppendedPath,
                    managedQuery.getString(managedQuery.getColumnIndex("_data")),
                    ContentUtil.getTime(managedQuery, "duration")
                )
            )
            managedQuery.moveToNext()
        }
        return true
    }
}