package com.example.hyeonsaengtime

data class StreakUpdate(
    val streakCount: Int,
    val lastCheckDateKey: String,
    val shouldPersist: Boolean
)

object StreakCalculator {
    fun calculate(
        todayDateKey: String,
        yesterdayTotalMillis: Long,
        requiredMillis: Long,
        currentStreakCount: Int,
        lastCheckDateKey: String
    ): StreakUpdate {
        if (lastCheckDateKey == todayDateKey) {
            return StreakUpdate(
                streakCount = currentStreakCount,
                lastCheckDateKey = lastCheckDateKey,
                shouldPersist = false
            )
        }

        val newStreak = if (yesterdayTotalMillis >= requiredMillis) {
            currentStreakCount + 1
        } else {
            0
        }

        return StreakUpdate(
            streakCount = newStreak,
            lastCheckDateKey = todayDateKey,
            shouldPersist = true
        )
    }
}
