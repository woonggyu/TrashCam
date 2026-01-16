package com.example.db

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.UIDesign.databinding.ActivityProfitReportBinding
import java.text.NumberFormat
import com.example.UIDesign.R

class ProfitReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfitReportBinding
    private lateinit var dbHelper: GarbageDatabaseHelper
    private lateinit var stampAdapter: StampAdapter

    private val SOJU_PRICE = 100
    private val STAMPS_PER_COUPON = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfitReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = GarbageDatabaseHelper(this)

        setupStampBoard()
    }

    override fun onResume() {
        super.onResume()
        loadProfitData()
    }

    private fun setupStampBoard() {
        binding.stampRecyclerView.layoutManager = GridLayoutManager(this, 5)
        stampAdapter = StampAdapter(this, STAMPS_PER_COUPON, 0)
        binding.stampRecyclerView.adapter = stampAdapter
    }

    private fun loadProfitData() {
        val sojuCount = dbHelper.getSpecificItemCount("Soju")
        val totalProfit = sojuCount * SOJU_PRICE
        val formattedProfit = NumberFormat.getNumberInstance().format(totalProfit)

        // 다국어 지원 텍스트 설정
        binding.tvTitle.text = getString(R.string.profit_report_title)
        binding.tvTotalProfit.text = getString(R.string.total_profit, formattedProfit)

        val stampsFilled = sojuCount % STAMPS_PER_COUPON
        val stampsRemaining = STAMPS_PER_COUPON - stampsFilled

        binding.tvStampStatus.text = if (stampsFilled == 0 && sojuCount > 0) {
            getString(R.string.stamp_coupon_achieved, STAMPS_PER_COUPON, STAMPS_PER_COUPON)
        } else {
            getString(R.string.stamp_remaining, stampsRemaining, stampsFilled, STAMPS_PER_COUPON)
        }

        stampAdapter.updateStamps(stampsFilled)
    }
}
