package com.example.hyeonsaengtime

data class TodayProgress(
    val todayHyeonsaengMillis: Long,
    val streakRequiredHours: Int,
    val streakRequiredMillis: Long,
    val streakProgress: Float,
    val isStreakRequirementMet: Boolean,
    val remainingMillisForStreak: Long
)

object TodayProgressCalculator {
    fun calculate(
        totalLockedMillis: Long,
        streakRequiredHours: Int = HyeonSaengRules.STREAK_REQUIRED_HOURS
    ): TodayProgress {
        val todayHyeonsaengMillis = maxOf(0L, totalLockedMillis)
        val streakRequiredMillis = streakRequiredHours * HyeonSaengRules.MILLIS_PER_HOUR
        val streakProgress = if (streakRequiredMillis > 0L) {
            (todayHyeonsaengMillis.toFloat() / streakRequiredMillis).coerceAtMost(1f)
        } else {
            0f
        }

        return TodayProgress(
            todayHyeonsaengMillis = todayHyeonsaengMillis,
            streakRequiredHours = streakRequiredHours,
            streakRequiredMillis = streakRequiredMillis,
            streakProgress = streakProgress,
            isStreakRequirementMet = todayHyeonsaengMillis >= streakRequiredMillis,
            remainingMillisForStreak = maxOf(0L, streakRequiredMillis - todayHyeonsaengMillis)
        )
    }
}
