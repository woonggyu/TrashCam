package com.example.camera

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

/**
 * CameraX API를 사용하여 카메라 미리보기를 켜고, 고품질 사진을 촬영하는
 * '사진작가'의 역할만 전문적으로 수행하는 클래스입니다.
 */
class CameraHandler(
    private val lifecycleOwner: LifecycleOwner,
    private val activity: AppCompatActivity,
    private val previewView: PreviewView,
    private val listener: CameraHandlerListener
) {
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9) // 경고 해결
                .build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                listener.onError("카메라 연결 실패: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(activity))
    }

    fun takePhoto() {
        val imageCapture = this.imageCapture ?: return
        GalleryHandler.savePhoto(activity, imageCapture) { uri ->
            listener.onPhotoSaved(uri)
        }
    }

    fun shutdownCamera() {
        cameraProvider?.unbindAll()
    }
}
