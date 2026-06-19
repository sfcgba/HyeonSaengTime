package com.example.hyeonsaengtime

object RoomMissionCalculator {
    fun calculate(members: List<RoomMember>): List<RoomMissionResult> {
        val totalMillis = members.sumOf { it.hyeonsaengMillis }
        val minMillis = members.minOfOrNull { it.hyeonsaengMillis } ?: 0L
        val maxMillis = members.maxOfOrNull { it.hyeonsaengMillis } ?: 0L

        return listOf(
            RoomMissionResult(
                id = RoomMissionId.ALL_MEMBERS_AT_LEAST_NINE_HOURS,
                title = "전원 기록 9시간 이상",
                isMet = members.isNotEmpty() &&
                    members.all { it.hyeonsaengMillis >= HyeonSaengRules.ROOM_ALL_MEMBER_REQUIRED_MILLIS }
            ),
            RoomMissionResult(
                id = RoomMissionId.TOTAL_AT_LEAST_STREAK_TARGET,
                title = "방 합산 기록 128시간 이상",
                isMet = totalMillis >= HyeonSaengRules.ROOM_TOTAL_REQUIRED_MILLIS
            ),
            RoomMissionResult(
                id = RoomMissionId.MIN_MEMBER_AT_LEAST_STREAK_TARGET,
                title = "개인 최저 기록 16시간 이상",
                isMet = members.isNotEmpty() &&
                    minMillis >= HyeonSaengRules.ROOM_MIN_MEMBER_REQUIRED_MILLIS
            ),
            RoomMissionResult(
                id = RoomMissionId.TOP_MEMBER_AT_LEAST_EIGHTEEN_HOURS,
                title = "개인 최고 기록 18시간 이상",
                isMet = members.isNotEmpty() &&
                    maxMillis >= HyeonSaengRules.ROOM_TOP_MEMBER_REQUIRED_MILLIS
            )
        )
    }

    fun selectMissionId(dateKey: String, roomName: String): RoomMissionId {
        val missions = RoomMissionId.values()
        return missions[stableIndex("$dateKey:$roomName", missions.size)]
    }

    fun calculateSelected(
        members: List<RoomMember>,
        selectedMissionId: RoomMissionId
    ): RoomMissionResult {
        return calculate(members).first { it.id == selectedMissionId }
    }

    fun settle(
        dateKey: String,
        members: List<RoomMember>,
        selectedMissionId: RoomMissionId,
        currentXp: Int,
        lastSettledDateKey: String
    ): RoomSettlement {
        val mission = calculateSelected(members, selectedMissionId)
        val rawDelta = if (mission.isMet) {
            HyeonSaengRules.ROOM_SUCCESS_XP
        } else {
            -HyeonSaengRules.ROOM_MISS_XP_PENALTY
        }
        val wasApplied = lastSettledDateKey != dateKey
        val xpAfter = if (wasApplied) {
            RoomLevelCalculator.applyDelta(currentXp, rawDelta)
        } else {
            RoomLevelCalculator.clampXp(currentXp)
        }

        return RoomSettlement(
            dateKey = dateKey,
            mission = mission,
            rawXpDelta = rawDelta,
            appliedXpDelta = if (wasApplied) rawDelta else 0,
            xpAfter = xpAfter,
            levelAfter = RoomLevelCalculator.levelForXp(xpAfter),
            wasApplied = wasApplied
        )
    }

    private fun stableIndex(seed: String, size: Int): Int {
        var hash = 1125899906842597L
        seed.forEach { char ->
            hash = 31L * hash + char.code
        }
        return ((hash ushr 1) % size).toInt()
    }
}
