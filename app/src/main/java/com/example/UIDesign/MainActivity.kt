package com.example.UIDesign

import android.Manifest // Manifest import 추가
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build // Build import 추가
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton // ImageButton import (언어 버튼용)
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate // 언어 변경
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat // 언어 변경
import androidx.core.view.GravityCompat // Drawer 제어
import androidx.drawerlayout.widget.DrawerLayout // DrawerLayout import
import com.example.camera.CameraActivity // 카메라 Activity import
import com.google.android.material.navigation.NavigationView // NavigationView import
import com.example.UIDesign.R
import com.example.camera.AlarmSettingsActivity
import com.example.db.ProfitReportActivity // ▼▼▼ '성과 리포트' 화면 import 추가 ▼▼▼
import com.example.db.StatisticsActivity // 통계 Activity import
import com.example.guideline.MistakeGuideActivity
import com.google.android.material.card.MaterialCardView // ▼▼▼ 카드뷰 import 추가 ▼▼▼
import java.util.Locale


class MainActivity : AppCompatActivity() {

    // --- DrawerLayout 변수 ---
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var toggle: ActionBarDrawerToggle

    // --- 팁 셔플 관련 변수 ---
    private lateinit var tipTextView: TextView
    private val tipHandler = Handler(Looper.getMainLooper())
    private lateinit var tipRunnable: Runnable
    private lateinit var currentTipList: Array<String>
    private val SHUFFLE_DELAY = 5000L

    // ▼▼▼ 1. '카메라 + 저장소' 모든 권한을 요청하는 런처 ▼▼▼
    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // 모든 권한이 허용되었는지 확인
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                openCamera() // 모든 권한을 받으면 카메라 열기
            } else {
                Toast.makeText(this, "카메라 및 저장소 권한이 모두 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    // ▼▼▼ 2. CameraActivity에 필요한 모든 권한 목록 (Android 버전에 따라 다름) ▼▼▼
    private val requiredPermissions: Array<String>
        get() = when {
            // Android 13 (TIRAMISU) 이상: READ_MEDIA_IMAGES 필요
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
                    // WRITE_EXTERNAL_STORAGE는 Q부터 필요 없음 (MediaStore 사용)
                )
            }
            // Android 10 (Q) ~ 12 (S)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
            // Android 9 (P) 이하
            else -> {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbarAndLanguageDrawer()
        setupButtons()
        setupTipShuffle()
    }

    // --- 툴바 및 언어 드로어 설정 ---
    private fun setupToolbarAndLanguageDrawer() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawerLayout = findViewById(R.id.drawer_layout)
        val languageButton: ImageButton = findViewById(R.id.language_button)
        languageButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val languageNavView: NavigationView = findViewById(R.id.language_nav_view)
        languageNavView.setNavigationItemSelectedListener { menuItem ->
            val langCode = when (menuItem.itemId) {
                R.id.nav_lang_ko -> "ko"
                R.id.nav_lang_en -> "en"
                R.id.nav_lang_zh -> "zh"
                R.id.nav_lang_ja -> "ja"
                else -> null
            }
            if (langCode != null) {
                val appLocale = LocaleListCompat.forLanguageTags(langCode)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    // --- 버튼 설정 ---
    private fun setupButtons() {
        val cameraButton: Button = findViewById(R.id.camera_button)
        cameraButton.setOnClickListener {
            // ▼▼▼ '모든' 권한을 확인하도록 수정 ▼▼▼
            checkAllPermissions()
        }

        val statisticsButton: Button = findViewById(R.id.statistics_button)
        statisticsButton.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }

        val alarmSettingsButton: Button = findViewById(R.id.alarm_settings_button)
        alarmSettingsButton.setOnClickListener {
            val intent = Intent(this, AlarmSettingsActivity::class.java)
            startActivity(intent)
        }

        // ▼▼▼ "성과 리포트" 카드 클릭 리스너 추가 ▼▼▼
        val sojuBottleButton: Button = findViewById(R.id.soju_bottle_button)
        sojuBottleButton.setOnClickListener {
            val intent = Intent(this, ProfitReportActivity::class.java)
            startActivity(intent)
        }
        val guideButton: Button = findViewById(R.id.guide_button)
        guideButton.setOnClickListener {
            val intent = Intent(this, MistakeGuideActivity::class.java)
            startActivity(intent)
        }
        // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲
    }

    // --- 팁 셔플 설정 ---
    private fun setupTipShuffle() {
        tipTextView = findViewById<TextView>(R.id.tip_shuffle_bar)
    }

    // --- 화면 생명주기 ---
    override fun onResume() {
        super.onResume()
        // strings.xml에서 팁 목록 로드 (언어 변경 시에도 적용됨)
        currentTipList = resources.getStringArray(R.array.recycling_tips)

        if (!::tipRunnable.isInitialized) {
            tipRunnable = Runnable {
                if (::currentTipList.isInitialized && currentTipList.isNotEmpty()) {
                    tipTextView.text = currentTipList.random()
                }
                tipHandler.postDelayed(tipRunnable, SHUFFLE_DELAY)
            }
        }
        tipHandler.removeCallbacks(tipRunnable)
        tipHandler.post(tipRunnable)
    }

    override fun onPause() {
        super.onPause()
        if (::tipRunnable.isInitialized) {
            tipHandler.removeCallbacks(tipRunnable)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (::toggle.isInitialized && toggle.onOptionsItemSelected(item)) return true
        return super.onOptionsItemSelected(item)
    }

    // --- 카메라 관련 함수 ---
    private fun checkAllPermissions() {
        val hasAllPermissions = requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (hasAllPermissions) {
            openCamera() // 모든 권한이 있으면 바로 카메라 열기
        } else {
            // 권한이 하나라도 없으면, 모든 권한을 다시 요청
            requestMultiplePermissionsLauncher.launch(requiredPermissions)
        }
    }

    private fun openCamera() {
        // 우리가 만든 CameraActivity를 호출합니다.
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }
}

