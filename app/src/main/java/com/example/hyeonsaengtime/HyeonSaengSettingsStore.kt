package com.example.hyeonsaengtime

import android.content.Context
import android.content.SharedPreferences

data class HyeonSaengSettings(
    val dailyGoalHours: Int,
    val nickname: String,
    val isAnonymous: Boolean,
    val extraNotificationsEnabled: Boolean
)

class HyeonSaengSettingsStore(
    private val prefs: SharedPreferences
) {
    constructor(
        context: Context
    ) : this(context.getSharedPreferences(HyeonSaengLocalStore.PREFS_NAME, Context.MODE_PRIVATE))

    fun getSettings(): HyeonSaengSettings {
        return HyeonSaengSettings(
            dailyGoalHours = getDailyGoalHours(),
            nickname = getNickname(),
            isAnonymous = getIsAnonymous(),
            extraNotificationsEnabled = areExtraNotificationsEnabled()
        )
    }

    fun getDailyGoalHours(): Int {
        return sanitizeDailyGoalHours(
            prefs.getInt(KEY_DAILY_GOAL_HOURS, HyeonSaengRules.STREAK_REQUIRED_HOURS)
        )
    }

    fun getDailyGoalMillis(): Long {
        return getDailyGoalHours() * HyeonSaengRules.MILLIS_PER_HOUR
    }

    fun saveDailyGoalHours(hours: Int) {
        prefs.edit()
            .putInt(KEY_DAILY_GOAL_HOURS, sanitizeDailyGoalHours(hours))
            .apply()
    }

    fun saveProfile(nickname: String, isAnonymous: Boolean): Boolean {
        val normalizedNickname = nickname.trim()
        if (normalizedNickname.isBlank()) return false

        val editor = prefs.edit()
            .putString(KEY_USER_NICKNAME, normalizedNickname)
            .putBoolean(KEY_USER_ANONYMOUS, isAnonymous)

        if (prefs.getBoolean(RoomLocalStore.KEY_ROOM_CREATED, false)) {
            editor
                .putString(RoomLocalStore.KEY_ROOM_NICKNAME, normalizedNickname)
                .putBoolean(RoomLocalStore.KEY_ROOM_ANONYMOUS, isAnonymous)
        }

        editor.apply()
        return true
    }

    fun saveExtraNotificationsEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_EXTRA_NOTIFICATIONS_ENABLED, enabled)
            .apply()
    }

    fun areExtraNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_EXTRA_NOTIFICATIONS_ENABLED, true)
    }

    private fun getNickname(): String {
        val saved = prefs.getString(KEY_USER_NICKNAME, null)
        if (!saved.isNullOrBlank()) return saved

        val roomNickname = prefs.getString(RoomLocalStore.KEY_ROOM_NICKNAME, null)
        if (!roomNickname.isNullOrBlank()) return roomNickname

        return RoomLocalStore.INITIAL_NICKNAME
    }

    private fun getIsAnonymous(): Boolean {
        return if (prefs.contains(KEY_USER_ANONYMOUS)) {
            prefs.getBoolean(KEY_USER_ANONYMOUS, false)
        } else {
            prefs.getBoolean(RoomLocalStore.KEY_ROOM_ANONYMOUS, false)
        }
    }

    companion object {
        const val KEY_DAILY_GOAL_HOURS = "daily_goal_hours"
        const val KEY_USER_NICKNAME = "user_nickname"
        const val KEY_USER_ANONYMOUS = "user_anonymous"
        const val KEY_EXTRA_NOTIFICATIONS_ENABLED = "extra_notifications_enabled"

        const val MIN_DAILY_GOAL_HOURS = 1
        const val MAX_DAILY_GOAL_HOURS = 24

        fun sanitizeDailyGoalHours(hours: Int): Int {
            return hours.coerceIn(MIN_DAILY_GOAL_HOURS, MAX_DAILY_GOAL_HOURS)
        }
    }
}
