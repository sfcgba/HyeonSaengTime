package com.example.hyeonsaengtime

data class DayResult(
    val dateKey: String,
    val hyeonsaengMillis: Long,
    val streakRequiredHours: Int,
    val streakRequiredMillis: Long,
    val streakProgress: Float,
    val isStreakRequirementMet: Boolean,
    val remainingMillisForStreak: Long,
    val streakCountAfterUpdate: Int
) {
    constructor(
        dateKey: String,
        progress: HyeonSaengProgress,
        streakCountAfterUpdate: Int
    ) : this(
        dateKey = dateKey,
        hyeonsaengMillis = progress.hyeonsaengMillis,
        streakRequiredHours = progress.streakRequiredHours,
        streakRequiredMillis = progress.streakRequiredMillis,
        streakProgress = progress.streakProgress,
        isStreakRequirementMet = progress.isStreakRequirementMet,
        remainingMillisForStreak = progress.remainingMillisForStreak,
        streakCountAfterUpdate = streakCountAfterUpdate
    )
}
