package com.android.allinone.videoeditor.model

import android.net.Uri

data class Video(
    val name:String,
    val uri:Uri,
    val path:String,
    val duration:String,
)