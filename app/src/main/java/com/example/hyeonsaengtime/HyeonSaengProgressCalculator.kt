package com.example.hyeonsaengtime

data class HyeonSaengProgress(
    val hyeonsaengMillis: Long,
    val streakRequiredHours: Int,
    val streakRequiredMillis: Long,
    val streakProgress: Float,
    val isStreakRequirementMet: Boolean,
    val remainingMillisForStreak: Long
)

object HyeonSaengProgressCalculator {
    fun calculate(
        totalLockedMillis: Long,
        streakRequiredHours: Int = HyeonSaengRules.STREAK_REQUIRED_HOURS
    ): HyeonSaengProgress {
        val hyeonsaengMillis = maxOf(0L, totalLockedMillis)
        val streakRequiredMillis = streakRequiredHours * HyeonSaengRules.MILLIS_PER_HOUR
        val streakProgress = if (streakRequiredMillis > 0L) {
            (hyeonsaengMillis.toFloat() / streakRequiredMillis).coerceAtMost(1f)
        } else {
            0f
        }

        return HyeonSaengProgress(
            hyeonsaengMillis = hyeonsaengMillis,
            streakRequiredHours = streakRequiredHours,
            streakRequiredMillis = streakRequiredMillis,
            streakProgress = streakProgress,
            isStreakRequirementMet = hyeonsaengMillis >= streakRequiredMillis,
            remainingMillisForStreak = maxOf(0L, streakRequiredMillis - hyeonsaengMillis)
        )
    }
}
