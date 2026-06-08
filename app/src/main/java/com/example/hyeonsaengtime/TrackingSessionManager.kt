package com.example.hyeonsaengtime

import android.content.SharedPreferences

enum class TrackingSessionUpdate {
    STARTED,
    WAITING_FOR_USER_PRESENT,
    FINALIZED,
    IGNORED
}

data class TrackingSessionResult(
    val update: TrackingSessionUpdate,
    val dailyDurations: List<DailyDuration> = emptyList()
)

object TrackingSessionManager {
    const val KEY_ACTIVE_LOCK_START = "active_lock_start_millis"
    const val KEY_LAST_SCREEN_EVENT_ACTION = "last_screen_event_action"
    const val KEY_LAST_SCREEN_EVENT_AT = "last_screen_event_at_millis"

    fun recordScreenEvent(
        prefs: SharedPreferences,
        action: String,
        eventAtMillis: Long
    ) {
        prefs.edit()
            .putString(KEY_LAST_SCREEN_EVENT_ACTION, action)
            .putLong(KEY_LAST_SCREEN_EVENT_AT, eventAtMillis)
            .apply()
    }

    fun handleScreenOff(
        prefs: SharedPreferences,
        eventAtMillis: Long
    ): TrackingSessionResult {
        val activeStart = prefs.getLong(KEY_ACTIVE_LOCK_START, 0L)
        if (activeStart > 0L) {
            return TrackingSessionResult(TrackingSessionUpdate.IGNORED)
        }

        prefs.edit()
            .putLong(KEY_ACTIVE_LOCK_START, eventAtMillis)
            .apply()
        return TrackingSessionResult(TrackingSessionUpdate.STARTED)
    }

    fun handleScreenOn(
        prefs: SharedPreferences,
        isKeyguardLocked: Boolean,
        eventAtMillis: Long
    ): TrackingSessionResult {
        return if (isKeyguardLocked) {
            TrackingSessionResult(TrackingSessionUpdate.WAITING_FOR_USER_PRESENT)
        } else {
            finalizeActiveSession(prefs, eventAtMillis)
        }
    }

    fun handleUserPresent(
        prefs: SharedPreferences,
        eventAtMillis: Long
    ): TrackingSessionResult = finalizeActiveSession(prefs, eventAtMillis)

    private fun finalizeActiveSession(
        prefs: SharedPreferences,
        eventAtMillis: Long
    ): TrackingSessionResult {
        val lockStart = prefs.getLong(KEY_ACTIVE_LOCK_START, 0L)
        if (lockStart <= 0L) {
            return TrackingSessionResult(TrackingSessionUpdate.IGNORED)
        }

        val dailyDurations = UsageSessionCalculator.splitByLocalDate(lockStart, eventAtMillis)
        val editor = prefs.edit()

        dailyDurations.forEach { dailyDuration ->
            val key = "total_${dailyDuration.dateKey}"
            val accumulated = prefs.getLong(key, 0L)
            editor.putLong(key, accumulated + dailyDuration.durationMillis)
        }

        editor.remove(KEY_ACTIVE_LOCK_START).apply()
        return TrackingSessionResult(
            update = TrackingSessionUpdate.FINALIZED,
            dailyDurations = dailyDurations
        )
    }
}
