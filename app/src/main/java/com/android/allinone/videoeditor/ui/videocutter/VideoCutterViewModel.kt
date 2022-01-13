package com.android.allinone.videoeditor.ui.videocutter

import androidx.lifecycle.ViewModel
import com.android.allinone.videoeditor.model.VideoPlayerState

class VideoCutterViewModel : ViewModel() {

    var path: String = ""
    var song: String = ""
    var videoEndTime = 0
    var videoStartTime = 0
    val l = ""
    var videoPlayerState: VideoPlayerState = VideoPlayerState()
    var isSeekBarInit = false
}