package com.example.camera

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

object GalleryHandler {//사진 저장 삭제 기능

    fun savePhoto(activity: AppCompatActivity, imageCapture: ImageCapture, onSaved: (Uri) -> Unit) {
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GarbageSortingApp")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(activity.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(activity),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onSaved(outputFileResults.savedUri!!)
                }

                override fun onError(exception: ImageCaptureException) {
                    // 에러 처리를 위해 콜백을 추가할 수도 있습니다.
                }
            })
    }

    fun deletePhoto(activity: AppCompatActivity, uri: Uri) {
        try {
            activity.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}