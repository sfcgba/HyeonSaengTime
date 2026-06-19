package com.example.hyeonsaengtime

import android.content.Context
import android.content.SharedPreferences

class HyeonSaengLocalStore(
    private val prefs: SharedPreferences
) {
    constructor(
        context: Context
    ) : this(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    fun getTodayProgress(nowMillis: Long = System.currentTimeMillis()): HyeonSaengProgress {
        val personalTimeZone = HyeonSaengTimeZoneStore.getOrCreatePersonalTimeZone(prefs)
        val todayDateKey = DateKeyFormatter.todayKey(nowMillis, personalTimeZone)
        return HyeonSaengProgressCalculator.calculate(
            totalLockedMillis = getTotalMillis(todayDateKey)
        )
    }

    fun getYesterdayResult(nowMillis: Long = System.currentTimeMillis()): DayResult {
        val streakCountAfterUpdate = updateStreakIfNeeded(nowMillis)
        val personalTimeZone = HyeonSaengTimeZoneStore.getOrCreatePersonalTimeZone(prefs)
        val yesterdayDateKey = DateKeyFormatter.yesterdayKey(nowMillis, personalTimeZone)
        val progress = HyeonSaengProgressCalculator.calculate(
            totalLockedMillis = getTotalMillis(yesterdayDateKey)
        )

        return DayResult(
            dateKey = yesterdayDateKey,
            progress = progress,
            streakCountAfterUpdate = streakCountAfterUpdate
        )
    }

    fun getStreakCount(): Int = prefs.getInt(KEY_STREAK_COUNT, 0)

    fun isFocusSessionActive(): Boolean {
        return prefs.getLong(TrackingSessionManager.KEY_ACTIVE_LOCK_START, 0L) > 0L
    }

    fun getPendingDailyRecap(nowMillis: Long = System.currentTimeMillis()): DayResult? {
        val personalTimeZone = HyeonSaengTimeZoneStore.getOrCreatePersonalTimeZone(prefs)
        val todayDateKey = DateKeyFormatter.todayKey(nowMillis, personalTimeZone)
        val lastAppSeenDateKey = prefs.getString(KEY_LAST_APP_SEEN_DATE, "") ?: ""
        val lastRecapShownDateKey = prefs.getString(KEY_LAST_DAILY_RECAP_SHOWN_DATE, "") ?: ""

        if (lastAppSeenDateKey.isBlank()) {
            saveLastAppSeenDateKey(todayDateKey)
            return null
        }
        if (lastAppSeenDateKey == todayDateKey || lastRecapShownDateKey == todayDateKey) {
            saveLastAppSeenDateKey(todayDateKey)
            return null
        }

        return getYesterdayResult(nowMillis)
    }

    fun markDailyRecapShown(nowMillis: Long = System.currentTimeMillis()) {
        val personalTimeZone = HyeonSaengTimeZoneStore.getOrCreatePersonalTimeZone(prefs)
        val todayDateKey = DateKeyFormatter.todayKey(nowMillis, personalTimeZone)
        prefs.edit()
            .putString(KEY_LAST_DAILY_RECAP_SHOWN_DATE, todayDateKey)
            .putString(KEY_LAST_APP_SEEN_DATE, todayDateKey)
            .apply()
    }

    fun updateStreakIfNeeded(nowMillis: Long = System.currentTimeMillis()): Int {
        val personalTimeZone = HyeonSaengTimeZoneStore.getOrCreatePersonalTimeZone(prefs)
        val todayDateKey = DateKeyFormatter.todayKey(nowMillis, personalTimeZone)
        val yesterdayDateKey = DateKeyFormatter.yesterdayKey(nowMillis, personalTimeZone)

        val update = StreakCalculator.calculate(
            todayDateKey = todayDateKey,
            yesterdayTotalMillis = getTotalMillis(yesterdayDateKey),
            requiredMillis = HyeonSaengRules.STREAK_REQUIRED_MILLIS,
            currentStreakCount = getStreakCount(),
            lastCheckDateKey = getStreakLastDateKey()
        )

        if (update.shouldPersist) {
            saveStreak(update.streakCount, update.lastCheckDateKey)
        }

        return update.streakCount
    }

    private fun getTotalMillis(dateKey: String): Long {
        return prefs.getLong(totalKey(dateKey), 0L)
    }

    private fun getStreakLastDateKey(): String {
        return prefs.getString(KEY_STREAK_LAST_DATE, "") ?: ""
    }

    private fun saveStreak(streakCount: Int, lastDateKey: String) {
        prefs.edit()
            .putInt(KEY_STREAK_COUNT, streakCount)
            .putString(KEY_STREAK_LAST_DATE, lastDateKey)
            .apply()
    }

    private fun saveLastAppSeenDateKey(dateKey: String) {
        prefs.edit()
            .putString(KEY_LAST_APP_SEEN_DATE, dateKey)
            .apply()
    }

    companion object {
        const val PREFS_NAME = "hyeonsaeng"
        const val KEY_STREAK_COUNT = "streak_count"
        const val KEY_STREAK_LAST_DATE = "streak_last_date"
        const val KEY_LAST_APP_SEEN_DATE = "last_app_seen_date"
        const val KEY_LAST_DAILY_RECAP_SHOWN_DATE = "last_daily_recap_shown_date"

        fun totalKey(dateKey: String): String = "total_$dateKey"
    }
}
