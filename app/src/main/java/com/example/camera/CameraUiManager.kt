package com.example.camera

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.UIDesign.databinding.ActivityCameraBinding

/**
 * CameraActivity의 모든 UI 변경을 전문적으로 처리하는 '무대 감독' 클래스입니다.
 */
class CameraUiManager(
    private val activity: AppCompatActivity,
    private val binding: ActivityCameraBinding
) {
    // 1. 촬영 전 초기 상태
    fun showInitialState() {
        binding.viewFinder.visibility = View.VISIBLE
        binding.btnCapture.visibility = View.VISIBLE


        binding.imagePreview.visibility = View.GONE
        binding.tvDetectionResult.visibility = View.GONE
        binding.confirmationLayout.visibility = View.GONE
        binding.loadingTextView.visibility = View.GONE
        binding.btnDetails.visibility = View.GONE // ▼ [상세 보기] 버튼 숨기기

        // Glide로 로드된 이미지/비트맵 캐시 지우기 (초기화)
        Glide.with(activity).clear(binding.imagePreview)
    }

    // 2. 분석 중 로딩 상태 (서버 전송 중)
    fun showLoadingState(message: String = "처리 중...") {
        binding.viewFinder.visibility = View.VISIBLE // 로딩 중에도 카메라 화면 유지
        binding.loadingTextView.visibility = View.VISIBLE

        // TODO: 로딩 메시지를 표시할 TextView가 있다면 여기에 코드 추가


        binding.btnCapture.visibility = View.GONE
        binding.imagePreview.visibility = View.GONE
        binding.confirmationLayout.visibility = View.GONE
        binding.tvDetectionResult.visibility = View.GONE
        binding.btnDetails.visibility = View.GONE // ▼ [상세 보기] 버튼 숨기기
    }

    // 3. 촬영 후 확인 상태 (Bitmap과 텍스트를 받음)
    fun showConfirmationState(imageUri: Uri?, resultText: String, resultBitmap: Bitmap?) {

        if (resultBitmap != null) {
            // 사각형이 그려진 Bitmap이 있다면 그것을 직접 ImageView에 표시
            binding.imagePreview.setImageBitmap(resultBitmap)
        } else {
            // Bitmap이 null이면 (예: 그리기 오류) 원본 Uri를 로드 (백업)
            Glide.with(activity).load(imageUri).into(binding.imagePreview)
        }

        binding.tvDetectionResult.text = resultText // 분석 결과 텍스트 설정

        // UI 요소 상태 변경
        binding.imagePreview.visibility = View.VISIBLE
        binding.tvDetectionResult.visibility = View.VISIBLE
        binding.confirmationLayout.visibility = View.VISIBLE // (저장/삭제/다시찍기) 버튼 그룹
        binding.btnDetails.visibility = View.VISIBLE // ▼ [상세 보기] 버튼 보이기

        binding.viewFinder.visibility = View.GONE
        binding.btnCapture.visibility = View.GONE
        binding.loadingTextView.visibility = View.GONE
    }
}
