package com.example.hyeonsaengtime

data class RoomMemberComparison(
    val member: RoomMember,
    val rank: Int,
    val stage: MemberGoalStage
)

enum class MemberGoalStage(
    val label: String
) {
    START("시작"),
    IN_PROGRESS("진행"),
    ACHIEVED("달성")
}

data class RoomResultSummary(
    val myRank: Int,
    val totalMembers: Int,
    val comparisons: List<RoomMemberComparison>
)

object RoomResultSummaryCalculator {
    fun summarize(
        members: List<RoomMember>,
        dailyGoalMillis: Long
    ): RoomResultSummary {
        val sorted = members
            .sortedWith(
                compareByDescending<RoomMember> { it.hyeonsaengMillis }
                    .thenBy { it.slot }
            )
        val comparisons = sorted.mapIndexed { index, member ->
            RoomMemberComparison(
                member = member,
                rank = index + 1,
                stage = stageFor(member.hyeonsaengMillis, dailyGoalMillis)
            )
        }
        val myRank = comparisons.firstOrNull { it.member.isMe }?.rank ?: 0

        return RoomResultSummary(
            myRank = myRank,
            totalMembers = members.size,
            comparisons = comparisons
        )
    }

    fun stageFor(
        millis: Long,
        dailyGoalMillis: Long
    ): MemberGoalStage {
        if (millis <= 0L) return MemberGoalStage.START
        return if (millis >= dailyGoalMillis) {
            MemberGoalStage.ACHIEVED
        } else {
            MemberGoalStage.IN_PROGRESS
        }
    }
}
