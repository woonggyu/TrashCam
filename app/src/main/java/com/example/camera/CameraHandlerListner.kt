package com.example.camera

import android.net.Uri

/**
 * CameraHandler가 자신의 상태나 결과를 CameraActivity에게 보고할 때 사용하는
 * 약속(인터페이스)입니다.
 */
interface CameraHandlerListener {
    /**
     * 사진이 성공적으로 저장되었을 때 호출됩니다.
     * @param uri 저장된 사진의 위치 정보
     */
    fun onPhotoSaved(uri: Uri)

    /**
     * 작업 중 오류가 발생했을 때 호출됩니다.
     * @param message 오류 메시지
     */
    fun onError(message: String)
}
