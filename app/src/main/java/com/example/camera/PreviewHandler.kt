package com.example.camera

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * Glide를 사용하여 ImageView에 이미지를 표시하고 지우는 역할을 하는 간단한 객체입니다.
 */
object PreviewHandler {
    fun show(imageView: ImageView, uri: Uri?) {
        Glide.with(imageView.context)
            .load(uri)
            .into(imageView)
    }

    fun clear(imageView: ImageView) {
        Glide.with(imageView.context).clear(imageView)
    }
}
