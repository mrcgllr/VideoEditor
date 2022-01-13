package com.android.allinone.videoeditor.model

data class VideoPlayerState(
    val currentTime: Int = 0,
    val fileName: String = "",
    val messageText: String = "",
    var start: Int = 0,
    var stop: Int = 0
)
