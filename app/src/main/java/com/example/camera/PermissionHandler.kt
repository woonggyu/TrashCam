package com.example.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * 카메라 및 미디어 접근 권한을 Android 15까지 대응하여 처리하는 클래스
 */
class PermissionHandler(
    private val activity: AppCompatActivity,
    private val onPermissionsGranted: () -> Unit // 모든 권한이 허용됐을 때 실행할 작업
) {

    // Android 버전에 따른 필수 권한 배열
    private val requiredPermissions: Array<String>
        get() = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> { // Android 13 이상 (15 포함)
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> { // Android 10~12
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
            else -> { // Android 9 이하
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }

    // 권한 요청 런처
    private val requestPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val deniedPermissions = permissions.filter { !it.value }.keys
            if (deniedPermissions.isEmpty()) {
                // 모든 권한 허용됨
                onPermissionsGranted()
            } else {
                // 거부된 권한 안내
                deniedPermissions.forEach { perm ->
                    if (activity.shouldShowRequestPermissionRationale(perm)) {
                        Toast.makeText(activity, "권한이 필요합니다: $perm", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, "권한이 거부되었습니다. 설정에서 허용해주세요: $perm", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    /**
     * 필요한 모든 권한이 있는지 확인하고, 없으면 요청합니다.
     */
    fun checkPermissions() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            // 이미 모든 권한 허용됨
            onPermissionsGranted()
        } else {
            // 거부된 권한 요청
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}
