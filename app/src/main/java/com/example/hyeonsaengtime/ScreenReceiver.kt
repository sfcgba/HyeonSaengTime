package com.example.hyeonsaengtime

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("hyeonsaeng", Context.MODE_PRIVATE)

        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                prefs.edit()
                    .putLong(KEY_ACTIVE_LOCK_START, System.currentTimeMillis())
                    .apply()
                Log.d("hyeonsaeng", "screen off")
            }

            Intent.ACTION_SCREEN_ON -> {
                // none = screen-on, swipe/PIN = USER_PRESENT.
                if (isKeyguardLocked(context)) {
                    Log.d("hyeonsaeng", "screen on - waiting for user present")
                } else {
                    finishActiveSession(prefs, "screen on")
                }
            }

            Intent.ACTION_USER_PRESENT -> {
                finishActiveSession(prefs, "user present")
            }
        }
    }

    private fun isKeyguardLocked(context: Context): Boolean {
        val keyguardManager = context.getSystemService(KeyguardManager::class.java)
        return keyguardManager.isKeyguardLocked
    }

    private fun finishActiveSession(
        prefs: android.content.SharedPreferences,
        reason: String
    ) {
        val lockStart = prefs.getLong(KEY_ACTIVE_LOCK_START, 0L)
        if (lockStart <= 0L) return

        val now = System.currentTimeMillis()
        val dailyDurations = UsageSessionCalculator.splitByLocalDate(lockStart, now)
        val editor = prefs.edit()

        dailyDurations.forEach { dailyDuration ->
            val key = "total_${dailyDuration.dateKey}"
            val accumulated = prefs.getLong(key, 0L)
            editor.putLong(key, accumulated + dailyDuration.durationMillis)
        }

        editor.remove(KEY_ACTIVE_LOCK_START).apply()
        Log.d("hyeonsaeng", "$reason - saved ${dailyDurations.size} day segment(s)")
    }

    // 전용 계산 helper가 필요할 수 있다. 자정을 넘길 시 알림표시를 정확하게 하기위해
    // 예시 getTodayLiveMillis(prefs, now)

    private companion object {
        const val KEY_ACTIVE_LOCK_START = "active_lock_start_millis"
    }
}
