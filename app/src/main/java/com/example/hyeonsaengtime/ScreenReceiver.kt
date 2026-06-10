package com.example.hyeonsaengtime

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("hyeonsaeng", Context.MODE_PRIVATE)
        val action = intent.action ?: return
        val eventAtMillis = System.currentTimeMillis()

        TrackingSessionManager.recordScreenEvent(prefs, action, eventAtMillis)

        val result = when (action) {
            Intent.ACTION_SCREEN_OFF -> {
                TrackingSessionManager.handleScreenOff(prefs, eventAtMillis)
            }

            Intent.ACTION_SCREEN_ON -> {
                // none = screen-on, swipe/PIN = USER_PRESENT.
                TrackingSessionManager.handleScreenOn(
                    prefs = prefs,
                    isKeyguardLocked = isKeyguardLocked(context),
                    eventAtMillis = eventAtMillis
                )
            }

            Intent.ACTION_USER_PRESENT -> {
                TrackingSessionManager.handleUserPresent(prefs, eventAtMillis)
            }

            else -> return
        }

        logResult(action, result)
    }

    private fun isKeyguardLocked(context: Context): Boolean {
        val keyguardManager = context.getSystemService(KeyguardManager::class.java)
        return keyguardManager.isKeyguardLocked
    }

    private fun logResult(action: String, result: TrackingSessionResult) {
        val message = when (result.update) {
            TrackingSessionUpdate.STARTED -> "$action - active session started"
            TrackingSessionUpdate.WAITING_FOR_USER_PRESENT -> {
                "$action - waiting for user present"
            }
            TrackingSessionUpdate.FINALIZED -> {
                "$action - saved ${result.dailyDurations.size} day segment(s)"
            }
            TrackingSessionUpdate.IGNORED -> "$action - ignored"
        }
        Log.d("hyeonsaeng", message)
    }
}
