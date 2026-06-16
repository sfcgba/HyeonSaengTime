package com.example.hyeonsaengtime

object RoomLevelCalculator {
    fun clampXp(xp: Int): Int {
        return xp.coerceIn(0, HyeonSaengRules.ROOM_MAX_XP)
    }

    fun levelForXp(xp: Int): Int {
        val clampedXp = clampXp(xp)
        if (clampedXp >= (HyeonSaengRules.ROOM_MAX_LEVEL - 1) * HyeonSaengRules.ROOM_XP_PER_LEVEL) {
            return HyeonSaengRules.ROOM_MAX_LEVEL
        }
        return clampedXp / HyeonSaengRules.ROOM_XP_PER_LEVEL + 1
    }

    fun applyDelta(currentXp: Int, delta: Int): Int {
        return clampXp(currentXp + delta)
    }

    fun xpInCurrentLevel(xp: Int): Int {
        val clampedXp = clampXp(xp)
        if (levelForXp(clampedXp) == HyeonSaengRules.ROOM_MAX_LEVEL) {
            return clampedXp - (HyeonSaengRules.ROOM_MAX_LEVEL - 1) * HyeonSaengRules.ROOM_XP_PER_LEVEL
        }
        return clampedXp % HyeonSaengRules.ROOM_XP_PER_LEVEL
    }
}
