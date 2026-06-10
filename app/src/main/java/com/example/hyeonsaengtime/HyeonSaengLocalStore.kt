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
        val todayDateKey = DateKeyFormatter.todayKey(nowMillis)
        return HyeonSaengProgressCalculator.calculate(
            totalLockedMillis = getTotalMillis(todayDateKey)
        )
    }

    fun getYesterdayResult(nowMillis: Long = System.currentTimeMillis()): DayResult {
        val streakCountAfterUpdate = updateStreakIfNeeded(nowMillis)
        val yesterdayDateKey = DateKeyFormatter.yesterdayKey(nowMillis)
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

    fun updateStreakIfNeeded(nowMillis: Long = System.currentTimeMillis()): Int {
        val todayDateKey = DateKeyFormatter.todayKey(nowMillis)
        val yesterdayDateKey = DateKeyFormatter.yesterdayKey(nowMillis)

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

    companion object {
        const val PREFS_NAME = "hyeonsaeng"
        const val KEY_STREAK_COUNT = "streak_count"
        const val KEY_STREAK_LAST_DATE = "streak_last_date"

        fun totalKey(dateKey: String): String = "total_$dateKey"
    }
}
