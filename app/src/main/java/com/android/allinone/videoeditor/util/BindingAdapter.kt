package com.android.allinone.videoeditor.util

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide


@BindingAdapter("bindImageFromUri")
fun ImageView.bindImageFromUri(uri: Uri) {
    Glide.with(context).load(uri).into(this)
}
