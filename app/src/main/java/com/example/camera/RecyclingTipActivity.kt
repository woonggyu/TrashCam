package com.example.camera

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.UIDesign.R
import com.example.UIDesign.databinding.ActivityRecycleTipBinding
import com.example.video.VideoTipAdapter

/**
 * 재활용 팁 동영상 목록을 보여주는 Activity (다국어 지원)
 */
class RecycleTipActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecycleTipBinding

    data class VideoInfo(val title: String, val videoId: String)

    private lateinit var tipData: Map<String, Triple<String, String, List<VideoInfo>>>
    private lateinit var defaultTip: Triple<String, String, List<VideoInfo>>

    companion object {
        const val EXTRA_CLASS_NAME = "extra_class_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecycleTipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 1. 다국어 문자열 로드
        tipData = mapOf(
            "Plastic bottle" to Triple(
                getString(R.string.tip_title_plastic_bottle),
                getString(R.string.tip_desc_plastic_bottle),
                listOf(
                    VideoInfo(getString(R.string.tip_video1_plastic_bottle), "e_Cu-QvO5MM"),
                    VideoInfo(getString(R.string.tip_video2_plastic_bottle), "hRDjZf8I3Kc"),
                    VideoInfo(getString(R.string.tip_video3_plastic_bottle), "MK3HLKiKDkY")
                )
            ),
            "Poly bag" to Triple(
                getString(R.string.tip_title_poly_bag),
                getString(R.string.tip_desc_poly_bag),
                listOf(
                    VideoInfo(getString(R.string.tip_video1_poly_bag), "f54c0TJvqGE"),
                    VideoInfo(getString(R.string.tip_video2_poly_bag), "evDKM7OZrho"),
                    VideoInfo(getString(R.string.tip_video3_poly_bag), "uY0btN30dM4")
                )
            ),
            "Paper" to Triple(
                getString(R.string.tip_title_paper),
                getString(R.string.tip_desc_paper),
                listOf(
                    VideoInfo(getString(R.string.tip_video1_paper), "HJ9Y46Wnp3Y"),
                    VideoInfo(getString(R.string.tip_video2_paper), "QyG4MYdG8dE"),
                    VideoInfo(getString(R.string.tip_video3_paper), "3pQRao1phVo")
                )
            )
        )

        // 🔹 기본값
        defaultTip = Triple(
            getString(R.string.tip_title_default),
            getString(R.string.tip_desc_default),
            listOf(
                VideoInfo(getString(R.string.tip_video1_default), "XUlVtdJxSI4"),
                VideoInfo(getString(R.string.tip_video2_default), "vUihMYKtHWI"),
                VideoInfo(getString(R.string.tip_video3_default), "o0CbvjoLd8U")
            )
        )

        // 🔹 2. 데이터 가져오기
        val className = intent.getStringExtra(EXTRA_CLASS_NAME) ?: "Unknown"
        val (title, description, videoList) = tipData[className] ?: defaultTip

        // 🔹 3. UI 설정
        binding.tvTipTitle.text = title
        binding.tvTipDescription.text = description

        val videoAdapter = VideoTipAdapter(this, videoList)
        binding.videoRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecycleTipActivity)
            adapter = videoAdapter
        }
    }
}
