package com.android.allinone.videoeditor.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.allinone.videoeditor.R
import com.android.allinone.videoeditor.databinding.ActivityStartBinding
import com.android.allinone.videoeditor.ui.videolist.VideoListActivity

class StartActivity : AppCompatActivity() {

    lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_start)
        requestPermissions(
            arrayOf(
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
            ), 101
        )
        initListeners()
    }

    private fun initListeners() {
        binding.apply {
            ivVideoCutter.setOnClickListener {
                openVideosActivity()
            }
        }
    }

    private fun openVideosActivity(){
        Intent(this@StartActivity, VideoListActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(this)
            finish()
        }
    }
}
