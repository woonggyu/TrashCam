package com.example.camera // 패키지 이름 확인

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.example.alarm.AlarmReceiver
import java.util.* // Calendar 클래스 사용
import kotlin.apply
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.isNullOrEmpty
import kotlin.collections.mapNotNull
import kotlin.collections.toSet
import kotlin.jvm.java
import kotlin.text.toIntOrNull

class BootReceiver : BroadcastReceiver() {

    // 휴대폰 부팅이 완료되면 이 함수가 호출됨
    override fun onReceive(context: Context, intent: Intent) {
        // 부팅 완료 액션인지 확인 (필수)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // SharedPreferences에서 저장된 알람 설정 불러오기
            val prefs = context.getSharedPreferences("AlarmSettings", Context.MODE_PRIVATE)
            val hour = prefs.getInt("alarmHour", -1) // 저장된 시간 (없으면 -1)
            val minute = prefs.getInt("alarmMinute", -1) // 저장된 분 (없으면 -1)
            val daysStringSet = prefs.getStringSet("alarmDays", null) // 저장된 요일 Set<String>

            // 저장된 설정이 있고, 요일이 선택되어 있다면 알람 재등록
            if (hour != -1 && minute != -1 && !daysStringSet.isNullOrEmpty()) {
                val daysOfWeek = daysStringSet.mapNotNull { it.toIntOrNull() }.toSet()
                if (daysOfWeek.isNotEmpty()) {
                    scheduleAlarm(context, daysOfWeek, hour, minute) // 알람 재등록 함수 호출
                    // (선택 사항) 재등록 완료 메시지 (디버깅용)
                    // Toast.makeText(context, "부팅 완료: 알람 재등록됨", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // AlarmSettingsActivity의 scheduleAlarm 함수와 거의 동일한 로직
    // Context를 파라미터로 받도록 수정됨
    private fun scheduleAlarm(context: Context, daysOfWeek: Set<Int>, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)

        daysOfWeek.forEach { dayOfWeek ->
            val pendingIntent = PendingIntent.getBroadcast(
                context, dayOfWeek, intent,
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

            // --- 기존 알람 취소 (혹시 모를 중복 방지) ---
            alarmManager.cancel(pendingIntent)

            // --- 새 알람 설정 (매주 반복) ---
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    // 부팅 시에는 사용자 인터랙션이 없으므로 권한 요청 불가
                    // 앱 실행 시 권한을 미리 받아두어야 함
                    return
                }

                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                // 권한 오류 처리 (부팅 시에는 특별히 할 수 있는게 없음)
            }
        }
        // 선택되지 않은 요일 알람 취소 로직은 BootReceiver에서는 생략해도 무방함
        // (앱 실행 시 설정 저장할 때 이미 처리됨)
    }
}