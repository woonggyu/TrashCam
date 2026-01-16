package com.example.db // 👈 패키지 이름 확인

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.UIDesign.R // 👈 R 파일 경로 확인
import com.example.UIDesign.databinding.ListItemStampBinding

/**
 * '성과 리포트' 화면의 스탬프 보드(RecyclerView)를 관리하는 어댑터입니다.
 */
class StampAdapter(
    private val context: Context,
    private val totalStamps: Int = 10, // 스탬프 보드의 총 칸 수 (예: 10개)
    private var filledStamps: Int        // DB에서 가져온, 실제로 찍힌 도장 개수
) : RecyclerView.Adapter<StampAdapter.StampViewHolder>() {

    /**
     * RecyclerView의 각 '칸(View)'을 보관하는 뷰 홀더
     */
    inner class StampViewHolder(val binding: ListItemStampBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * 'list_item_stamp.xml' 레이아웃을 '부풀려서' 뷰 홀더를 생성합니다.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StampViewHolder {
        val binding = ListItemStampBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StampViewHolder(binding)
    }

    /**
     * 각 '칸'에 어떤 이미지를 표시할지 결정합니다.
     */
    override fun onBindViewHolder(holder: StampViewHolder, position: Int) {
        // 'position'은 0부터 시작합니다 (0번째 칸, 1번째 칸...)

        if (position < filledStamps) {
            // 현재 칸(position)이 찍힌 도장 개수보다 작으면
            // '찍힌 도장' 이미지를 표시합니다.
            holder.binding.ivStamp.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.ic_soju)
            )
        } else {
            // 현재 칸이 찍힌 도장 개수보다 크거나 같으면
            // '빈 칸' 이미지를 표시합니다.
            holder.binding.ivStamp.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.ic_stamp_empty)
            )
        }
    }

    /**
     * 스탬프 보드의 총 칸 수를 반환합니다.
     */
    override fun getItemCount(): Int {
        return totalStamps
    }

    /**
     * (선택 사항) 통계 초기화 등으로 데이터가 변경되었을 때,
     * 스탬프 개수를 새로고침하는 함수
     */
    fun updateStamps(newCount: Int) {
        filledStamps = newCount
        notifyDataSetChanged() // RecyclerView 전체를 새로고침
    }
}

