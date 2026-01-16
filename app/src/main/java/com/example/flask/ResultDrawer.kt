package com.example.flask

import android.graphics.*
// Context, Uri, Build, MediaStore import는 이제 필요 없습니다.
import android.util.Log
import com.example.flask.Detection // Detection 클래스 import (패키지 경로 확인!)

object ResultDrawer {

    // 사각형 그리기 펜
    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 10f // 사각형 두께
    }

    // 텍스트 그리기 펜
    private val textPaint = Paint().apply {
        color = Color.GREEN
        textSize = 50f
        style = Paint.Style.FILL
        setShadowLayer(5f, 0f, 0f, Color.BLACK) // 텍스트 가독성을 위한 그림자
    }

    /**
     * 원본 Bitmap과 탐지된 객체 목록을 받아, 사각형이 그려진 Bitmap을 반환합니다.
     * @param originalBitmap 원본 사진 Bitmap
     * @param detections 서버에서 받은 YOLO 탐지 결과 목록
     * @return 사각형과 라벨이 그려진 Bitmap
     */
    // ▼▼▼ 함수 정의를 CameraActivity의 호출에 맞게 수정했습니다. (Context 제거) ▼▼▼
    fun drawDetections(originalBitmap: Bitmap, detections: List<Detection>): Bitmap {

        // 원본 Bitmap을 수정할 수 있도록 복사본을 만듭니다.
        val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        val width = bitmap.width
        val height = bitmap.height

        try {
            // 2. 탐지된 객체를 순회하며 그리기
            for (detection in detections) {
                // 정규화된 좌표(0.0 ~ 1.0)를 실제 픽셀 값으로 변환
                val box = detection.boxNormalized
                if (box.size == 4) {
                    val xmin = (box[0] * width)
                    val ymin = (box[1] * height)
                    val xmax = (box[2] * width)
                    val ymax = (box[3] * height)

                    // 사각형 그리기
                    canvas.drawRect(xmin, ymin, xmax, ymax, boxPaint)

                    // 라벨 텍스트 생성
                    val labelText = "${detection.className} (${"%.0f".format(detection.confidence * 100)}%)"

                    // 텍스트 그리기 (사각형 상단)
                    canvas.drawText(
                        labelText,
                        xmin + 10f, // 사각형에서 약간 떨어진 위치
                        ymin - 10f,
                        textPaint
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("ResultDrawer", "Error drawing detections", e)
        }
        return bitmap // 사각형이 그려진 Bitmap 반환
    }

    // URI에서 Bitmap을 로드하는 함수는 CameraActivity.kt의 uriToBitmap이 담당하므로 제거합니다.
    // private fun loadBitmapFromUri(...) { ... }
}

