package com.example.db

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.example.UIDesign.R // R 파일 import (프로젝트 패키지 이름 확인!)
import com.example.UIDesign.databinding.ActivityStatisticsBinding
private lateinit var binding: ActivityStatisticsBinding

    class StatisticsActivity : AppCompatActivity() {

        private lateinit var dbHelper: GarbageDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = GarbageDatabaseHelper(this)



        // ▼▼▼ '초기화' 버튼 클릭 리스너 ▼▼▼
//        binding.btnResetStats.setOnClickListener {
//            // 사용자에게 정말 삭제할 것인지 확인받습니다. (실수 방지)
//            showDeleteConfirmDialog()
//        }

        // onCreate에서는 차트 설정을 호출하지 않습니다.
    }

    // ▼▼▼ 화면이 나타날 때마다 차트를 새로 그리도록 onResume()으로 이동 ▼▼▼
    override fun onResume() {
        super.onResume()
        // DB에서 최신 데이터를 가져와 차트를 그립니다.
        setupChart()
    }

    private fun setupChart() {
        // 1. DB에서 통계 데이터 가져오기
        val stats: List<GarbageStat> = dbHelper.getStatistics()

        if (stats.isNotEmpty()) {
            // 2. DB 데이터를 차트 데이터(BarEntry)로 변환
            val entries = ArrayList<BarEntry>()
            val labels = ArrayList<String>()
            stats.forEachIndexed { index, stat ->
                entries.add(BarEntry(index.toFloat(), stat.count.toFloat()))
                labels.add(stat.name)
            }

            // 3. 데이터 세트로 묶고 디자인 적용 (R.string 사용)
            val dataSet = BarDataSet(entries, getString(R.string.chart_label_garbage_type)).apply {
                color = Color.CYAN // 막대 색상
                valueTextColor = Color.BLACK
                valueTextSize = 14f
            }

            // 4. 최종 차트 데이터 생성
            val barData = BarData(dataSet)
            barData.barWidth = 0.5f

            // 5. 차트에 데이터를 적용하고 옵션 설정
            binding.barChart.apply {
                data = barData
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    valueFormatter = IndexAxisValueFormatter(labels)
                    setDrawGridLines(false)
                    granularity = 1f
                    textSize = 12f
                }
                axisLeft.axisMinimum = 0f
                axisRight.isEnabled = false
                animateY(1000)
                invalidate() // 차트 새로고침
            }
        } else {
            // 6. 데이터가 없으면 차트를 깨끗하게 비웁니다.
            binding.barChart.clear()
            binding.barChart.invalidate()
        }
    }

    // ▼▼▼ 확인 대화상자를 보여주는 함수 (R.string 사용) ▼▼▼
//    private fun showDeleteConfirmDialog() {
//        AlertDialog.Builder(this)
//            .setTitle(getString(R.string.dialog_title_reset_stats))
//            .setMessage(getString(R.string.dialog_message_reset_stats))
//            .setPositiveButton(getString(R.string.dialog_button_delete)) { _, _ ->
//                // "삭제" 버튼을 누르면
//                dbHelper.clearAllEntries() // DB 전문가에게 전체 삭제를 지시
//                Toast.makeText(this, getString(R.string.toast_stats_reset), Toast.LENGTH_SHORT).show()
//                // 차트를 다시 그려서 빈 화면으로 업데이트
//                setupChart()
//            }
//            .setNegativeButton(getString(R.string.dialog_button_cancel), null) // "취소" 버튼은 아무것도 하지 않음
//            .show()
//    }
}

