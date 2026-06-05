package com.example.hyeonsaengtime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("hyeonsaeng", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                prefs.edit()
                    .putLong("lock_start_$today", System.currentTimeMillis())
                    .apply()
                Log.d("hyeonsaeng", "screen off")
            }
            Intent.ACTION_SCREEN_ON -> {
                val lockStart = prefs.getLong("lock_start_$today", 0L)
                if (lockStart > 0L) {
                    val elapsed = System.currentTimeMillis() - lockStart
                    val accumulated = prefs.getLong("total_$today", 0L)
                    prefs.edit()
                        .putLong("total_$today", accumulated + elapsed)
                        .putLong("lock_start_$today", 0L)
                        .apply()
                    val totalMin = (accumulated + elapsed) / 1000 / 60
                    Log.d("hyeonsaeng", "screen on - today total: ${totalMin}min")
                }
            }
        }
    }
}