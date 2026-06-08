package com.example.hyeonsaengtime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class TrackingService : Service() {

    private val screenReceiver = ScreenReceiver()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, buildNotification())
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        ContextCompat.registerReceiver(
            this, screenReceiver, filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        Log.d("현생", "서비스 시작됨")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
        Log.d("현생", "서비스 종료됨")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY = best-effort restart hint 서비스 시작전 힌트
        // process is killed. It does not recover force-stops, user-initiated stops,
        // 아직 위험성 있음. 일단 mvp단계에서는 이렇게만 구현한다.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "tracking_channel",
            "현생시간 추적",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, "tracking_channel")
            .setContentTitle("현생시간 측정 중")
            .setContentText("휴대폰을 내려놓는 시간을 기록하고 있어요")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
}
