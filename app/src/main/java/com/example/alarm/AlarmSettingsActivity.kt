package com.example.camera // 패키지 이름 확인

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri // 설정 URI
import android.os.Build
import android.os.Bundle
import android.provider.Settings // 설정 Intent
import android.widget.Button
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // 안내창
import androidx.appcompat.app.AppCompatActivity
// ▼▼▼ R 파일 import 경로 확인 ▼▼▼
import com.example.UIDesign.R
// ▼▼▼ AlarmReceiver import 경로 확인 ▼▼▼
import com.example.alarm.AlarmReceiver
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.* // Calendar 클래스 사용

class AlarmSettingsActivity : AppCompatActivity() {

    // SharedPreferences 이름 및 키 상수 정의
    private val PREFS_NAME = "AlarmSettings"
    private val KEY_ALARM_HOUR = "alarmHour"
    private val KEY_ALARM_MINUTE = "alarmMinute"
    private val KEY_ALARM_DAYS = "alarmDays"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_settings)

        // --- 뷰(View) 찾기 ---
        val timePicker: TimePicker = findViewById(R.id.time_picker)
        val dayChipGroup: ChipGroup = findViewById(R.id.day_chip_group)
        val saveButton: Button = findViewById(R.id.save_button)
        val deleteButton: Button = findViewById(R.id.delete_button)

        // --- 설정 불러오기 및 UI 초기화 ---
        loadSettings(timePicker, dayChipGroup)

        // --- 저장 버튼 클릭 리스너 ---
        saveButton.setOnClickListener {
            val selectedChipIds = dayChipGroup.checkedChipIds
            val hour = timePicker.hour
            val minute = timePicker.minute

            if (selectedChipIds.isEmpty()) {
                // ▼▼▼ [수정] Toast 메시지를 strings.xml 참조로 변경 ▼▼▼
                Toast.makeText(this, getString(R.string.select_day_warning), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedDaysOfWeek = mapChipIdsToCalendarDays(selectedChipIds)

            saveSettings(hour, minute, selectedDaysOfWeek)
            scheduleAlarmWithPermissionCheck(selectedDaysOfWeek, hour, minute) // 완료 메시지는 이 함수 내부에서 처리
        }

        // --- 삭제 버튼 클릭 리스너 ---
        deleteButton.setOnClickListener {
            cancelAllAlarmsAndSettings()
            loadSettings(timePicker, dayChipGroup)
            // ▼▼▼ [수정] Toast 메시지를 strings.xml 참조로 변경 ▼▼▼
            Toast.makeText(this, getString(R.string.alarm_deleted_message), Toast.LENGTH_SHORT).show()
        }
    }

    // --- 설정 불러오기 함수 ---
    private fun loadSettings(timePicker: TimePicker, dayChipGroup: ChipGroup) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedHour = prefs.getInt(KEY_ALARM_HOUR, 8)
        val savedMinute = prefs.getInt(KEY_ALARM_MINUTE, 0)
        val savedDays = prefs.getStringSet(KEY_ALARM_DAYS, setOf()) ?: setOf()

        timePicker.hour = savedHour
        timePicker.minute = savedMinute

        dayChipGroup.clearCheck()
        val dayMap = mapCalendarDaysToChipIds()
        savedDays.forEach { dayString ->
            val dayInt = dayString.toIntOrNull()
            if (dayInt != null && dayMap.containsKey(dayInt)) {
                dayChipGroup.findViewById<Chip>(dayMap[dayInt]!!)?.isChecked = true
            }
        }
    }

    // --- 설정 저장 함수 ---
    private fun saveSettings(hour: Int, minute: Int, daysOfWeek: Set<Int>) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        with(prefs.edit()) {
            putInt(KEY_ALARM_HOUR, hour)
            putInt(KEY_ALARM_MINUTE, minute)
            putStringSet(KEY_ALARM_DAYS, daysOfWeek.map { it.toString() }.toSet())
            apply()
        }
    }

    // --- Chip ID -> Calendar 요일 변환 함수 ---
    private fun mapChipIdsToCalendarDays(chipIds: List<Int>): Set<Int> {
        val days = mutableSetOf<Int>()
        chipIds.forEach { id ->
            when (id) {
                R.id.chip_mon -> days.add(Calendar.MONDAY)
                R.id.chip_tue -> days.add(Calendar.TUESDAY)
                R.id.chip_wed -> days.add(Calendar.WEDNESDAY)
                R.id.chip_thu -> days.add(Calendar.THURSDAY)
                R.id.chip_fri -> days.add(Calendar.FRIDAY)
                R.id.chip_sat -> days.add(Calendar.SATURDAY)
                R.id.chip_sun -> days.add(Calendar.SUNDAY)
            }
        }
        return days
    }

    // --- Calendar 요일 -> Chip ID 변환 함수 ---
    private fun mapCalendarDaysToChipIds(): Map<Int, Int> {
        return mapOf(
            Calendar.MONDAY to R.id.chip_mon,
            Calendar.TUESDAY to R.id.chip_tue,
            Calendar.WEDNESDAY to R.id.chip_wed,
            Calendar.THURSDAY to R.id.chip_thu,
            Calendar.FRIDAY to R.id.chip_fri,
            Calendar.SATURDAY to R.id.chip_sat,
            Calendar.SUNDAY to R.id.chip_sun
        )
    }

    // --- Calendar 요일 -> 문자열 변환 함수 ---
    private fun dayToString(calendarDay: Int): String {
        return when (calendarDay) {
            Calendar.MONDAY -> getString(R.string.chip_mon) // strings.xml 사용
            Calendar.TUESDAY -> getString(R.string.chip_tue)
            Calendar.WEDNESDAY -> getString(R.string.chip_wed)
            Calendar.THURSDAY -> getString(R.string.chip_thu)
            Calendar.FRIDAY -> getString(R.string.chip_fri)
            Calendar.SATURDAY -> getString(R.string.chip_sat)
            Calendar.SUNDAY -> getString(R.string.chip_sun)
            else -> ""
        }
    }

    // --- 권한 확인 로직 포함 알람 설정 함수 ---
    private fun scheduleAlarmWithPermissionCheck(daysOfWeek: Set<Int>, hour: Int, minute: Int) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                showExactAlarmPermissionDialog()
                return
            }
        }

        setAlarms(daysOfWeek, hour, minute, alarmManager)

        // ▼▼▼ [수정] Toast 메시지를 strings.xml 참조로 변경 ▼▼▼
        val daysString = daysOfWeek.joinToString(", ") { dayToString(it) }
        val timeString = String.format("%02d:%02d", hour, minute) // 시간 문자열 포맷팅
        Toast.makeText(this, getString(R.string.alarm_set_success_message, daysString, timeString), Toast.LENGTH_LONG).show()
        // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

        finish()
    }

    // --- 실제 알람 예약 로직 함수 ---
    private fun setAlarms(daysOfWeek: Set<Int>, hour: Int, minute: Int, alarmManager: AlarmManager) {
        val intent = Intent(this, AlarmReceiver::class.java)

        daysOfWeek.forEach { dayOfWeek ->
            val pendingIntent = PendingIntent.getBroadcast(
                this, dayOfWeek, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val calendar: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (System.currentTimeMillis() >= timeInMillis) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            alarmManager.cancel(pendingIntent) // 기존 알람 취소

            try { // 새 알람 설정 (setExactAndAllowWhileIdle)
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                Toast.makeText(this, "알람 설정 권한 오류 발생", Toast.LENGTH_SHORT).show()
            }
        }
        cancelUnselectedDayAlarms(daysOfWeek, alarmManager, intent) // 선택 안 된 요일 알람 취소
    }

    // --- 모든 알람 취소 및 설정 삭제 함수 ---
    private fun cancelAllAlarmsAndSettings() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)

        val allDays = setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
        allDays.forEach { dayOfWeek ->
            val pendingIntentToCancel = PendingIntent.getBroadcast(
                this, dayOfWeek, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntentToCancel)
        }

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        with(prefs.edit()) {
            remove(KEY_ALARM_HOUR)
            remove(KEY_ALARM_MINUTE)
            remove(KEY_ALARM_DAYS)
            apply()
        }
    }

    // --- 선택되지 않은 요일 알람 취소 로직 함수 ---
    private fun cancelUnselectedDayAlarms(selectedDays: Set<Int>, alarmManager: AlarmManager, intent: Intent) {
        val allDays = setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
        val daysToCancel = allDays - selectedDays
        daysToCancel.forEach { dayOfWeek ->
            val pendingIntentToCancel = PendingIntent.getBroadcast(
                this, dayOfWeek, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntentToCancel)
        }
    }

    // --- 정확한 알람 권한 안내 다이얼로그 함수 ---
    private fun showExactAlarmPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("정확한 알람 권한 필요")
            .setMessage("정확한 시간에 알림을 받으려면 '알람 및 리마인더' 권한이 필요합니다. 설정 화면으로 이동하여 권한을 허용해 주세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        try { startActivity(this) } catch (e: Exception) {
                            try { // fallback
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            } catch (e2: Exception) {
                                Toast.makeText(applicationContext, "설정 화면을 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else { /* Android 12 미만은 여기로 올 일 없음 */ }
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "권한이 없어 정확한 알람을 설정할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .show()
    }
}