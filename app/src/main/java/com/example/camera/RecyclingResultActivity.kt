package com.example.camera

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.UIDesign.R
import com.example.UIDesign.databinding.ActivityTrashResultBinding
import com.example.camera.RecyclingGuide

/**
 * AI 분석 후, 분리수거 지침과 관련 유튜브 썸네일을 보여주는 화면입니다.
 * 썸네일 클릭 시 유튜브로 이동합니다.
 */
class RecyclingResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrashResultBinding

    private var photoUri: Uri? = null
    private var className: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrashResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 데이터 수신
        photoUri = intent.getParcelableExtra(EXTRA_URI)
        className = intent.getStringExtra(EXTRA_CLASS_NAME)

        // 2. 데이터 확인
        if (photoUri == null || className == null) {
            Toast.makeText(this, getString(R.string.error_no_analysis_data), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 3. 가이드 정보 가져오기
        val guide = RecyclingGuide.getGuide(this, className!!)

        // 4. UI 텍스트 설정
        binding.tvCategory.text = guide.category
        binding.tvDisposalArea.text = guide.disposalArea
        binding.tvInstruction.text = guide.instruction

        // 5. 촬영된 사진 로드
        Glide.with(this)
            .load(photoUri)
            .into(binding.resultImageView)

        // 6. 유튜브 썸네일 로드 및 클릭 이벤트 설정
        loadYoutubeThumbnail(guide.videoId)

        // 7. [활용 방법] 버튼 클릭 리스너
        binding.btnRecycleTip.setOnClickListener {
            val intent = Intent(this, RecycleTipActivity::class.java).apply {
                putExtra(RecycleTipActivity.EXTRA_CLASS_NAME, className)
            }
            startActivity(intent)
        }
    }

    /**
     * 유튜브 영상 ID를 이용해 썸네일 이미지를 로드하고, 클릭 시 이동 기능을 설정합니다.
     */
    private fun loadYoutubeThumbnail(videoId: String) {
        // 공백 제거
        val cleanVideoId = videoId.trim()

        // 유튜브 고화질 썸네일 URL
        val thumbnailUrl = "https://img.youtube.com/vi/$cleanVideoId/hqdefault.jpg"

        // Glide로 썸네일 이미지 로드
        Glide.with(this)
            .load(thumbnailUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(binding.youtubeThumbnailImageView)

        // ★ 클릭 이벤트 추가: 썸네일 영역 전체를 누르면 유튜브로 이동
        binding.videoContainer.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$cleanVideoId"))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "영상을 열 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        const val EXTRA_URI = "extra_photo_uri"
        const val EXTRA_CLASS_NAME = "extra_class_name"
    }
}