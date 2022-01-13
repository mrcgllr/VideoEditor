package com.android.allinone.videoeditor.ui.videoplayer

import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.ParseException
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.allinone.videoeditor.R
import com.android.allinone.videoeditor.databinding.ActivityVideoPlayerBinding
import com.android.allinone.videoeditor.util.TimeUtils
import java.io.File

class VideoPlayerActivity : AppCompatActivity() {

    lateinit var binding: ActivityVideoPlayerBinding

    var duration = 0
    var handler = Handler()
    var isVideoPlaying = false
    var position = 0
    val r = true
    var bundle: Bundle? = null
    var song = ""
    var videoPath: Uri? = null

    lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_player)

        runnable = object : Runnable {
            override fun run() {
                binding.apply {
                    if (videoView.isPlaying) {
                        val currentPosition: Int = videoView.currentPosition
                        seekBar.progress = currentPosition
                        try {
                            tvLeft.text = TimeUtils.formatTimeUnit(
                                currentPosition.toLong()
                            )
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }
                        if (currentPosition == duration) {
                            seekBar.progress = 0
                            tvLeft.text = "00:00"
                            handler.removeCallbacks(runnable)
                            return
                        }
                        handler.postDelayed(runnable, 200)
                        return
                    }
                    seekBar.progress = duration
                    try {
                        tvLeft.text = TimeUtils.formatTimeUnit(
                            duration.toLong()
                        )
                    } catch (e2: ParseException) {
                        e2.printStackTrace()
                    }
                    handler.removeCallbacks(runnable)
                }
            }
        }

        initListener()
    }

    private fun initListener() {
        binding.apply {
            btnPlay.setOnClickListener {
                if (isVideoPlaying) {
                    try {
                        videoView.pause()
                        handler.removeCallbacks(runnable)
                        btnPlay.setBackgroundResource(R.drawable.ic_play)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    try {
                        videoView.seekTo(seekBar.progress)
                        videoView.start()
                        handler.postDelayed(runnable, 200)
                        videoView.visibility = View.VISIBLE
                        btnPlay.setBackgroundResource(R.drawable.ic_pause)
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                    }
                }
                isVideoPlaying = isVideoPlaying xor r
            }

            imgClear.setOnClickListener {
                if (videoView.isPlaying) {
                    try {
                        videoView.pause()
                        handler.removeCallbacks(runnable)
                        btnPlay.setBackgroundResource(R.drawable.ic_play)
                        isVideoPlaying = false
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                    }
                }
                deleteVideo()
            }

            if (r || supportActionBar != null) {
                supportActionBar!!.setDisplayHomeAsUpEnabled(r)
                supportActionBar!!.setDisplayShowTitleEnabled(false)
                bundle = intent.extras
                bundle?.let{
                    song = it.getString("song").toString()
                    position = it.getInt("position", 0)
                }
                try {
                    getVideo(applicationContext, song)
                } catch (unused: Exception) {
                }
                videoView.setVideoPath(song)
                videoView.seekTo(100)
                videoView.setOnErrorListener { mediaPlayer, i, i2 ->
                    Toast.makeText(
                        applicationContext,
                        "Video Player Not Supproting",
                        Toast.LENGTH_SHORT
                    ).show()
                   r
                }
                videoView.setOnPreparedListener {
                    duration = videoView.duration
                    seekBar.max = duration
                    tvLeft.text = "00:00"
                    try {
                        val textView: TextView = tvDuration
                        val sb = StringBuilder()
                        sb.append("duration : ")
                        sb.append(TimeUtils.formatTimeUnit(duration.toLong()))
                        textView.text = sb.toString()
                        tvRight.text = TimeUtils.formatTimeUnit(
                            duration.toLong()
                        )
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                }
                videoView.setOnCompletionListener {
                    videoView.visibility = View.VISIBLE
                    btnPlay.setBackgroundResource(R.drawable.ic_play)
                    videoView.seekTo(0)
                    seekBar.progress = 0
                    tvLeft.text = "00:00"
                    handler.removeCallbacks(runnable)
                    isVideoPlaying = isVideoPlaying xor r
                }
            } else {
                throw AssertionError()
            }
        }
    }

    fun isOnline(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (connectivityManager.activeNetworkInfo == null || !connectivityManager.activeNetworkInfo!!.isConnectedOrConnecting
        ) {
            false
        } else r
    }

    fun onProgressChanged(seekBar: SeekBar?, i2: Int, z: Boolean) {
        binding.apply {
            if (z) {
                videoView.seekTo(i2)
                try {
                    tvLeft.text = TimeUtils.formatTimeUnit(i2.toLong())
                } catch (e2: ParseException) {
                    e2.printStackTrace()
                }
            }
        }
    }

    private fun getVideo(context: Context?, str: String?) {
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val strArr =
            arrayOf("_id", "_data", "_display_name", "_size", "duration", "date_added", "album")
        val sb = StringBuilder()
        sb.append("%")
        sb.append(str)
        sb.append("%")
        val strArr2 = arrayOf(sb.toString())
        val managedQuery = managedQuery(uri, strArr, "_data  like ?", strArr2, " _id DESC")
        if (managedQuery.moveToFirst()) {
            try {
                videoPath = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, getLong(managedQuery)
                )
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    private fun getLong(cursor: Cursor): String? {
        return cursor.getLong(cursor.getColumnIndexOrThrow("_id")).toString()
    }

    private fun deleteVideo() {
        AlertDialog.Builder(this).setMessage("Are you sure you want to delete this file ?")
            .setPositiveButton(
                "delete"
            ) { dialogInterface, i ->
                val file = File(song)
                if (file.exists()) {
                    file.delete()
                    try {
                        val contentResolver: ContentResolver = contentResolver
                        val uri: Uri? = videoPath
                        val sb = StringBuilder()
                        sb.append("_data=\"")
                        sb.append(song)
                        sb.append("\"")
                        if (uri != null) {
                            contentResolver.delete(uri, sb.toString(), null)
                        }
                    } catch (unused: Exception) {
                    }
                }
                onBackPressed()
            }.setNegativeButton(
                "Cancel"
            ) { dialogInterface, i -> }.setCancelable(r)
            .show()
    }
}