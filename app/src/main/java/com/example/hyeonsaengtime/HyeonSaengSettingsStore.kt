package com.example.hyeonsaengtime

import android.content.Context
import android.content.SharedPreferences

data class HyeonSaengSettings(
    val nickname: String,
    val isRoomCreated: Boolean,
    val isRoomAnonymous: Boolean,
    val extraNotificationsEnabled: Boolean
)

class HyeonSaengSettingsStore(
    private val prefs: SharedPreferences
) {
    constructor(
        context: Context
    ) : this(context.getSharedPreferences(HyeonSaengLocalStore.PREFS_NAME, Context.MODE_PRIVATE))

    fun getSettings(): HyeonSaengSettings {
        cleanupLegacySettings()
        return HyeonSaengSettings(
            nickname = getNickname(),
            isRoomCreated = prefs.getBoolean(RoomLocalStore.KEY_ROOM_CREATED, false),
            isRoomAnonymous = getRoomAnonymous(),
            extraNotificationsEnabled = areExtraNotificationsEnabled()
        )
    }

    fun cleanupLegacySettings() {
        prefs.edit()
            .remove(LEGACY_KEY_DAILY_GOAL_HOURS)
            .remove(LEGACY_KEY_USER_ANONYMOUS)
            .apply()
    }

    fun saveNickname(nickname: String): Boolean {
        val normalizedNickname = nickname.trim()
        if (normalizedNickname.isBlank()) return false

        val editor = prefs.edit()
            .putString(KEY_USER_NICKNAME, normalizedNickname)

        if (prefs.getBoolean(RoomLocalStore.KEY_ROOM_CREATED, false)) {
            editor.putString(RoomLocalStore.KEY_ROOM_NICKNAME, normalizedNickname)
        }

        editor.apply()
        return true
    }

    fun saveRoomAnonymous(isAnonymous: Boolean): Boolean {
        if (!prefs.getBoolean(RoomLocalStore.KEY_ROOM_CREATED, false)) return false

        prefs.edit()
            .putBoolean(RoomLocalStore.KEY_ROOM_ANONYMOUS, isAnonymous)
            .apply()
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

    private fun getRoomAnonymous(): Boolean {
        return prefs.getBoolean(RoomLocalStore.KEY_ROOM_ANONYMOUS, false)
    }

    companion object {
        const val LEGACY_KEY_DAILY_GOAL_HOURS = "daily_goal_hours"
        const val LEGACY_KEY_USER_ANONYMOUS = "user_anonymous"
        const val KEY_USER_NICKNAME = "user_nickname"
        const val KEY_EXTRA_NOTIFICATIONS_ENABLED = "extra_notifications_enabled"
    }
}
