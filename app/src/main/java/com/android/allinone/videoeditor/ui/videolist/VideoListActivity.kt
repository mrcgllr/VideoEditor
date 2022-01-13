package com.android.allinone.videoeditor.ui.videolist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.allinone.videoeditor.R
import com.android.allinone.videoeditor.databinding.ActivityVideoListBinding

class VideoListActivity : AppCompatActivity() {

    lateinit var binding: ActivityVideoListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_list)
    }

}