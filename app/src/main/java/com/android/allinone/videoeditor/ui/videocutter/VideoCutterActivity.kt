package com.android.allinone.videoeditor.ui.videocutter

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentUris
import android.content.Intent
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.MediaStore.Images
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.allinone.videoeditor.R
import com.android.allinone.videoeditor.customview.SliceSeekBar
import com.android.allinone.videoeditor.databinding.ActivityVideoCutterBinding
import com.android.allinone.videoeditor.util.CommandUtil
import com.android.allinone.videoeditor.util.IntentExtras
import com.android.allinone.videoeditor.util.TimeUtils
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class VideoCutterActivity : AppCompatActivity(), MediaScannerConnectionClient {

    lateinit var binding: ActivityVideoCutterBinding
    private val viewModel: VideoCutterViewModel by viewModels()
    var mediaScannerConnection: MediaScannerConnection? = null

    private val videoHandler: VideoHandler = VideoHandler()

    inner class VideoHandler : Handler() {
        private var isInit = false
        private val runnable: Runnable

        fun run() {
            if (!isInit) {
                isInit = true
                sendEmptyMessage(0)
            }
        }

        override fun handleMessage(message: Message) {
            binding.apply {
                isInit = false
                seekBar.videoPlayingProgress(videoView.currentPosition)
                if (!videoView.isPlaying || videoView.currentPosition >= seekBar.rightProgress) {
                    if (videoView.isPlaying) {
                        videoView.pause()
                        btnPlay.setBackgroundResource(R.drawable.ic_play)
                    }
                    seekBar.setSliceBlocked(false)
                    seekBar.removeVideoStatusThumb()
                    return
                }
                postDelayed(runnable, 50)
            }
        }

        init {
            runnable = Runnable { this@VideoHandler.run() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_cutter)
        intent.getStringExtra(IntentExtras.PATH)?.let {
            viewModel.path = it
        }
        if (viewModel.path == "") {
            finish()
        }
        initListeners()
    }


    private fun initListeners() {
        with(viewModel) {
            binding.apply {

                videoView.setVideoPath(path)
                videoView.seekTo(100)
                tvFileName.text = File(path).name
                prepareVideoView()

                videoView.setOnCompletionListener {
                    btnPlay.setBackgroundResource(R.drawable.ic_play)
                }

                btnCute.setOnClickListener {
                    if (videoView.isPlaying) {
                        videoView.pause()
                        btnPlay.setBackgroundResource(R.drawable.ic_play)
                    }
                    cuteVideo()
                }

                btnPlay.setOnClickListener {
                    if (videoView.isPlaying) {
                        videoView.pause()
                        seekBar.setSliceBlocked(false)
                        btnPlay.setBackgroundResource(R.drawable.ic_play)
                        seekBar.removeVideoStatusThumb()
                        return@setOnClickListener
                    }
                    videoView.seekTo(seekBar.leftProgress)
                    videoView.start()
                    btnPlay.setBackgroundResource(R.drawable.ic_pause)
                    videoHandler.run()
                }

            }
        }
    }

    private fun prepareVideoView() {
        binding.apply {
            with(viewModel) {
                videoView.setOnPreparedListener { mediaPlayer ->
                    seekBar.setSeekBarChangeListener(object : SliceSeekBar.SeekBarChangeListener {
                        override fun SeekBarValueChanged(leftThumb: Int, rightThumb: Int) {
                            if (seekBar.selectedThumb == 1) {
                                mediaPlayer.seekTo(leftThumb)
                            }

                            tvLeft.text = TimeUtils.formatTimeUnit(leftThumb.toLong())
                            tvRight.text = TimeUtils.formatTimeUnit(rightThumb.toLong())
                            videoPlayerState.start = leftThumb
                            videoPlayerState.stop = rightThumb
                            videoStartTime = leftThumb / 1000
                            videoEndTime = rightThumb / 1000

                            tvDuration.text = getString(
                                R.string.video_duration,
                                Integer.valueOf((videoEndTime - videoStartTime) / 3600),
                                Integer.valueOf((videoEndTime - videoStartTime) % 3600 / 60),
                                Integer.valueOf((videoEndTime - videoStartTime) % 60)
                            )
                        }
                    })
                    btnPlay.setBackgroundResource(
                        R.drawable.ic_play
                    )
                    seekBar.setMaxValue(mediaPlayer.duration)
                    seekBar.leftProgress = 0
                    seekBar.rightProgress = mediaPlayer.duration
                    seekBar.setProgressMinDiff(0)
                }
            }
        }
    }

    override fun onScanCompleted(p0: String?, p1: Uri?) {
        mediaScannerConnection!!.disconnect()

    }

    override fun onMediaScannerConnected() {
        mediaScannerConnection!!.scanFile(viewModel.l, "video/*")
    }

    private fun cuteVideo() {
        viewModel.apply {
            val cuteStartTime = videoStartTime.toString()
            val cuteEndTime = (videoEndTime - videoStartTime).toString()
            val format = SimpleDateFormat("_HHmmss", Locale.US).format(Date())
            val sb = StringBuilder()
            sb.append(Environment.getExternalStorageDirectory().absoluteFile)
            sb.append("/")
            sb.append("Videooo")
            sb.append("/")
            sb.append("Video Cutter")
            val file = File(sb.toString())
            if (!file.exists()) {
                file.mkdirs()
            }
            val sb2 = StringBuilder()
            sb2.append(Environment.getExternalStorageDirectory().absoluteFile)
            sb2.append("/")
            sb2.append("Videooo")
            sb2.append("/")
            sb2.append("Video Cutter")
            sb2.append("/videocutter")
            sb2.append(format)
            sb2.append(".mp4")
            song = sb2.toString()

            cuteVideoFFmpeg(
                arrayOf(
                    "-ss",
                    cuteStartTime,
                    "-y",
                    "-i",
                    path,
                    "-t",
                    cuteEndTime,
                    "-vcodec",
                    "mpeg4",
                    "-b:v",
                    "2097152",
                    "-b:a",
                    "48000",
                    "-ac",
                    "2",
                    "-ar",
                    "22050",
                    song
                ), song
            )
        }

    }


    private fun cuteVideoFFmpeg(strArr: Array<String>, str: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Please Wait")
        progressDialog.show()
        FFmpeg.executeAsync(CommandUtil.main(strArr)) { executionId, returnCode ->
            Log.d("TAG", String.format("FFmpeg process exited with rc %d.", returnCode))
            Log.d("TAG", "FFmpeg process output:")
            Config.printLastCommandOutput(Log.INFO)
            progressDialog.dismiss()
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    progressDialog.dismiss()
                    val intent = Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE")
                    intent.data = Uri.fromFile(File(viewModel.song))
                    sendBroadcast(intent)
                }
                Config.RETURN_CODE_CANCEL -> {
                    Log.d("ffmpegfailure", str)
                    try {
                        File(str).delete()
                        deleteFromGallery(str)
                        Toast.makeText(this, "Error Creating Video", Toast.LENGTH_SHORT).show()
                    } catch (th: Throwable) {
                        th.printStackTrace()
                    }
                    Log.i(
                        Config.TAG,
                        "Async command execution cancelled by user."
                    )
                }
                else -> {
                    try {
                        File(str).delete()
                        deleteFromGallery(str)
                        Toast.makeText(this, "Error Creating Video", Toast.LENGTH_SHORT).show()
                    } catch (th: Throwable) {
                        th.printStackTrace()
                    }
                    Log.i(
                        Config.TAG,
                        String.format("Async command execution failed with rc=%d.", returnCode)
                    )
                }
            }
        }
        window.clearFlags(16)
    }

    fun showDeviceNotSupportedDialog() {
        AlertDialog.Builder(this).setTitle("Device not supported")
            .setMessage("FFmpeg is not supported on your device").setCancelable(false)
            .setPositiveButton(
                17039370
            ) { dialogInterface, i -> finish() }.create().show()
    }

    private fun deleteFromGallery(str: String) {
        val strArr = arrayOf("_id")
        val strArr2 = arrayOf(str)
        val uri = Images.Media.EXTERNAL_CONTENT_URI
        val contentResolver = contentResolver
        val query = contentResolver.query(uri, strArr, "_data = ?", strArr2, null)
        if (query!!.moveToFirst()) {
            try {
                contentResolver.delete(
                    ContentUris.withAppendedId(
                        Images.Media.EXTERNAL_CONTENT_URI, query.getLong(
                            query.getColumnIndexOrThrow("_id")
                        )
                    ), null, null
                )
            } catch (e2: IllegalArgumentException) {
                e2.printStackTrace()
            }
        } else {
            try {
                File(str).delete()
                refreshGallery(str)
            } catch (e3: Exception) {
                e3.printStackTrace()
            }
        }
        query.close()
    }

    private fun refreshGallery(str: String) {
        val intent = Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE")
        intent.data = Uri.fromFile(File(str))
        sendBroadcast(intent)
    }
}