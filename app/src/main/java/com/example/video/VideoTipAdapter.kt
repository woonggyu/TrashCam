package com.example.video

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.UIDesign.R // R 파일 경로 확인
import com.example.UIDesign.databinding.ListItemVideoBinding // 뷰 바인딩 import
import com.example.camera.RecycleTipActivity
/**
 * RecycleTipActivity의 RecyclerView를 위한 어댑터
 */
class VideoTipAdapter(
    private val context: Context,
    private val videoList: List<RecycleTipActivity.VideoInfo> // Activity에 정의된 VideoInfo 사용
) : RecyclerView.Adapter<VideoTipAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(private val binding: ListItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(videoInfo: RecycleTipActivity.VideoInfo) {
            // 1. 텍스트 설정
            binding.videoTitle.text = videoInfo.title

            // 2. 썸네일 이미지 로드 (Glide 사용)
            // 유튜브 비디오 ID로 썸네일 URL을 만듭니다. (0.jpg = 고화질 썸네일)
            val thumbnailUrl = "https://img.youtube.com/vi/${videoInfo.videoId}/0.jpg"
            Glide.with(context)
                .load(thumbnailUrl)
                .placeholder(R.drawable.ic_launcher_background) // 로딩 중 표시할 임시 이미지
                .error(R.drawable.ic_launcher_foreground) // 로딩 실패 시 표시할 이미지
                .into(binding.videoThumbnail)

            // 3. 클릭 리스너 설정
            binding.root.setOnClickListener {
                // 클릭 시 유튜브 앱/웹으로 이동하는 Intent 생성
                val youtubeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:${videoInfo.videoId}"))
                // 유튜브 앱이 없을 경우를 대비해 웹 브라우저 URL도 준비
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=${videoInfo.videoId}"))

                try {
                    // 유튜브 앱 실행 시도
                    context.startActivity(youtubeIntent)
                } catch (e: Exception) {
                    // 실패 시 웹 브라우저 실행
                    context.startActivity(webIntent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ListItemVideoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VideoViewHolder(binding)
    }

    override fun getItemCount(): Int = videoList.size

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoList[position])
    }
}

