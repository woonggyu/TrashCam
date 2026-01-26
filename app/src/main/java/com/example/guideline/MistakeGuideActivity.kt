package com.example.guideline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog // 팝업창 사용을 위해 import
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.UIDesign.R
import com.example.UIDesign.databinding.MistakeguideBinding // 바인딩 이름 수정됨
import com.example.UIDesign.databinding.ItemMistakeBinding

/**
 * 헷갈리는 '일반 쓰레기' 목록을 보여주는 화면
 * (이미지 클릭 시 상세 설명 팝업 표시)
 */
class MistakeGuideActivity : AppCompatActivity() {

    // 바인딩 이름을 MistakeguideBinding으로 설정
    private lateinit var binding: MistakeguideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 인플레이터도 MistakeguideBinding 사용
        binding = MistakeguideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 데이터 준비
        val mistakeList = listOf(
            MistakeItem(
                "오염된 용기",
                R.drawable.ic_dirty_ramen,
                "오염된 용기: 기름이나 양념이 심하게 묻어 닦아도 지워지지 않는 용기는 재활용이 불가능하므로 쓰레기(종량제 봉투)로 버립니다."
            ),
            MistakeItem(
                "영수증",
                R.drawable.ic_receipt,
                "영수증은 재활용이 불가능한 감열지이므로, 일반 쓰레기(종량제 봉투)에 버려야 합니다."
            ),
            MistakeItem(
                "치킨 뼈",
                R.drawable.ic_chicken_bone,
                "치킨 뼈, 생선 가시 등은 딱딱하여 비료나 사료로 만들 수 없으므로 일반 쓰레기(종량제 봉투)입니다."
            ),
            MistakeItem(
                "빨대",
                R.drawable.ic_straws,
                "빨대는 재활용이 어려운 작은 크기의 플라스틱 또는 오염된 종이이므로, 일반 쓰레기(종량제 봉투)로 버려야 합니다."
            ),
            MistakeItem(
                "깨진 유리",
                R.drawable.ic_broken_glass,
                "깨진 유리는 재활용이 안 됩니다. 신문지로 감싸고 종량제 봉투에 담아 버리세요. 양이 많으면 특수규격마대(불연성)를 사용하세요."
            ),
            MistakeItem(
                "전단지",
                R.drawable.ic_coated_paper,
                "전단지는 대부분 비닐 코팅된 혼합 용지로, 재활용이 불가능해 일반 쓰레기(종량제 봉투)로 버려야 합니다."
            )
        )

        // 2. 리사이클러뷰 설정 (2열 그리드)
        binding.rvMistakes.layoutManager = GridLayoutManager(this, 2)
        binding.rvMistakes.adapter = MistakeAdapter(mistakeList)
    }
}

// --- 데이터 클래스 ---
data class MistakeItem(
    val name: String,
    val imageResId: Int,
    val description: String
)

// --- 어댑터 클래스 ---
class MistakeAdapter(private val items: List<MistakeItem>) :
    RecyclerView.Adapter<MistakeAdapter.MistakeViewHolder>() {

    class MistakeViewHolder(val binding: ItemMistakeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MistakeViewHolder {
        val binding = ItemMistakeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MistakeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MistakeViewHolder, position: Int) {
        val item = items[position]

        // 1. 기본 정보 연결
        holder.binding.tvMistakeName.text = item.name
        holder.binding.ivMistakeImage.setImageResource(item.imageResId)

        // 2. [클릭 이벤트] 이미지 클릭 시 설명 팝업 띄우기
        holder.binding.ivMistakeImage.setOnClickListener {
            val context = holder.itemView.context

            AlertDialog.Builder(context)
                .setTitle(item.name)           // 팝업 제목
                .setMessage(item.description)  // 팝업 내용 (긴 설명)
                .setPositiveButton("확인") { dialog, _ ->
                    dialog.dismiss() // 닫기
                }
                .show() // 팝업 표시
        }
    }

    override fun getItemCount() = items.size
}