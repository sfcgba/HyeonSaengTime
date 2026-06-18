package com.example.hyeonsaengtime

import android.Manifest
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences(HyeonSaengLocalStore.PREFS_NAME, Context.MODE_PRIVATE)
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
        showUnlockNotificationIfNeeded(context, result)
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

    private fun showUnlockNotificationIfNeeded(
        context: Context,
        result: TrackingSessionResult
    ) {
        val prefs = context.getSharedPreferences(HyeonSaengLocalStore.PREFS_NAME, Context.MODE_PRIVATE)
        val settingsStore = HyeonSaengSettingsStore(prefs)
        if (!UnlockNotificationPolicy.shouldNotify(result, settingsStore.areExtraNotificationsEnabled())) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        createUnlockNotificationChannel(context)
        val notification = NotificationCompat.Builder(context, UnlockNotificationPolicy.CHANNEL_ID)
            .setContentTitle(UnlockNotificationPolicy.TITLE)
            .setContentText(UnlockNotificationPolicy.TEXT)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            UnlockNotificationPolicy.NOTIFICATION_ID,
            notification
        )
    }

    private fun createUnlockNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            UnlockNotificationPolicy.CHANNEL_ID,
            UnlockNotificationPolicy.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}
