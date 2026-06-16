package com.example.hyeonsaengtime

import android.content.SharedPreferences
import java.util.TimeZone

object HyeonSaengTimeZoneStore {
    const val KEY_PERSONAL_TIME_ZONE_ID = "personal_time_zone_id"

    fun getOrCreatePersonalTimeZone(
        prefs: SharedPreferences,
        fallbackTimeZone: TimeZone = TimeZone.getDefault()
    ): TimeZone {
        val storedTimeZoneId = prefs.getString(KEY_PERSONAL_TIME_ZONE_ID, null)
        val storedTimeZone = storedTimeZoneId?.let(::timeZoneOrNull)
        if (storedTimeZone != null) return storedTimeZone

        prefs.edit()
            .putString(KEY_PERSONAL_TIME_ZONE_ID, fallbackTimeZone.id)
            .apply()
        return fallbackTimeZone
    }

    fun timeZoneOrNull(timeZoneId: String): TimeZone? {
        if (timeZoneId.isBlank()) return null
        return if (timeZoneId in availableTimeZoneIds) {
            TimeZone.getTimeZone(timeZoneId)
        } else {
            null
        }
    }

    private val availableTimeZoneIds: Set<String> = TimeZone.getAvailableIDs().toSet()
}
