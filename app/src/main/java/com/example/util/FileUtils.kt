package com.example.util


import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.*

object FileUtil { //갤러리 저장과 관련된 기능을 분리:

    fun createImageUri(context: Context): Uri? {
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Example")
        }

        return context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }
}
