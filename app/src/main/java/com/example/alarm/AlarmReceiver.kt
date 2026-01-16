package com.example.alarm // 👈 [확인] 이 파일이 실제로 있는 패키지 이름이 맞는지 확인하세요!

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes // 소리 설정
import android.media.RingtoneManager // 기본 알림음
import android.net.Uri // 알림음 URI
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
// ▼▼▼ MainActivity import 경로 확인 ▼▼▼
import com.example.UIDesign.MainActivity
// ▼▼▼ R 파일 import 경로 확인 ▼▼▼
import com.example.UIDesign.R
import java.util.* // Calendar 클래스 사용
// 'kotlin.apply'와 'kotlin.jvm.java' import는 필요 없으므로 제거했습니다.

class AlarmReceiver : BroadcastReceiver() {

    // AlarmManager가 알람을 발생시키면 이 함수가 호출됨
    override fun onReceive(context: Context, intent: Intent) {
        // 알람 받은 후 다음 주 알람 재예약
        rescheduleAlarm(context)

        // --- 알림 클릭 시 MainActivity 열기 설정 ---
        // ▼▼▼ "Intent.setFlags ="를 "flags ="로 수정 ▼▼▼
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // --- 알림 채널 ID 및 알림 고유 ID ---
        val channelId = "recycling_alarm_channel"
        val notificationId = System.currentTimeMillis().toInt() // 알림 ID (고유해야 함)

        // --- 알림 내용 생성 ---
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // 앱 아이콘 사용
            .setContentTitle("분리수거 알림 ⏰")
            .setContentText("오늘은 분리수거 하는 날입니다! 잊지 마세요~")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 중요도 높게 설정
            .setContentIntent(pendingIntent) // 알림 클릭 시 실행될 동작
            .setAutoCancel(true) // 알림 클릭 시 자동으로 사라짐
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE) // 기본 소리/진동 (Oreo 미만용)

        // --- Android 8.0 이상 알림 채널 생성 (소리/진동 설정) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "분리수거 알림"
            val descriptionText = "지정된 요일과 시간에 분리수거 알림을 보냅니다."
            val importance = NotificationManager.IMPORTANCE_HIGH // 중요도 높게 설정
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = descriptionText
                enableVibration(true) // 진동 활성화
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500) // 진동 패턴
                // 기본 알림 소리 설정
                val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM) // 용도를 알람으로 지정
                    .build()
                setSound(soundUri, audioAttributes)
            }
            // 시스템에 알림 채널 등록
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // --- 알림 표시 ---
        with(NotificationManagerCompat.from(context)) {
            // 알림 권한 확인 (Android 13 이상)
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한 없으면 Toast 메시지 표시 후 종료 (MainActivity에서 권한 요청해야 함)
                Toast.makeText(context, "알림 표시 권한이 없어 알림을 보낼 수 없습니다.", Toast.LENGTH_SHORT).show()
                return
            }
            // 권한 있으면 알림 표시
            notify(notificationId, builder.build())
        }
    }

    // --- 다음 주 알람을 재예약하는 함수 ---
    private fun rescheduleAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // 알람 발생 시 다시 이 AlarmReceiver를 실행할 Intent
        val intent = Intent(context, AlarmReceiver::class.java)

        // SharedPreferences에서 저장된 알람 시간 가져오기
        val prefs = context.getSharedPreferences("AlarmSettings", Context.MODE_PRIVATE)
        val hour = prefs.getInt("alarmHour", -1) // 저장된 시간 (없으면 -1)
        val minute = prefs.getInt("alarmMinute", -1) // 저장된 분 (없으면 -1)

        // 저장된 시간이 없으면 재예약 중단
        if (hour == -1 || minute == -1) return

        // 현재 시간을 기준으로 다음 주 같은 요일, 저장된 시간으로 Calendar 설정
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.WEEK_OF_YEAR, 1) // 다음 주로 설정
            set(Calendar.HOUR_OF_DAY, hour) // 저장된 시간으로 설정
            set(Calendar.MINUTE, minute) // 저장된 분으로 설정
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // 다음 알람 요일 (PendingIntent ID로 사용)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // PendingIntent 생성 (requestCode를 요일 상수로 사용하여 요일별 알람 구분)
        val pendingIntent = PendingIntent.getBroadcast(
            context, dayOfWeek, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 다음 알람 예약 (setExactAndAllowWhileIdle 사용)
        try {
            // Android 12 이상 정확한 알람 권한 확인
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    // 권한 없으면 예약 불가 (앱 실행 시 권한 받아야 함)
                    return
                }
            }
            // 정확한 시간에 다음 알람 예약
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, // 절전 모드에서도 깨워서 실행
                calendar.timeInMillis, // 다음 알람 시간
                pendingIntent // 실행할 Intent
            )
        } catch (e: SecurityException) {
            // 권한 관련 오류 발생 시 (일반적으로 발생하지 않음)
        }
    }
}
