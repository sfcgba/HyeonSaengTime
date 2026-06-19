package com.example.hyeonsaengtime

data class RoomState(
    val roomName: String,
    val nickname: String,
    val isAnonymous: Boolean,
    val level: Int,
    val xp: Int,
    val lastSettledDateKey: String,
    val mySlot: Int,
    val hostTimeZoneId: String
)

sealed interface RoomLoadResult {
    data object NotCreated : RoomLoadResult
    data class Created(val state: RoomState) : RoomLoadResult
    data class Invalid(val reasons: List<String>) : RoomLoadResult
}

sealed interface RoomCreateResult {
    data class Created(val state: RoomState) : RoomCreateResult
    data class Invalid(val reasons: List<String>) : RoomCreateResult
}

sealed interface RoomOverviewResult {
    data object NotCreated : RoomOverviewResult
    data class Created(val overview: RoomOverview) : RoomOverviewResult
    data class Invalid(val reasons: List<String>) : RoomOverviewResult
}

data class RoomMember(
    val slot: Int,
    val displayName: String,
    val hyeonsaengMillis: Long,
    val isMe: Boolean
)

enum class RoomMissionId {
    ALL_MEMBERS_AT_LEAST_NINE_HOURS,
    TOTAL_AT_LEAST_STREAK_TARGET,
    MIN_MEMBER_AT_LEAST_STREAK_TARGET,
    TOP_MEMBER_AT_LEAST_EIGHTEEN_HOURS
}

data class RoomMissionResult(
    val id: RoomMissionId,
    val title: String,
    val isMet: Boolean
)

data class RoomSettlement(
    val dateKey: String,
    val mission: RoomMissionResult,
    val rawXpDelta: Int,
    val appliedXpDelta: Int,
    val xpAfter: Int,
    val levelAfter: Int,
    val wasApplied: Boolean
)

data class RoomOverview(
    val state: RoomState,
    val personalTimeZoneId: String,
    val personalTodayDateKey: String,
    val roomTodayDateKey: String,
    val roomYesterdayDateKey: String,
    val personalTodayMillis: Long,
    val roomTodayMillis: Long,
    val todayMembers: List<RoomMember>,
    val yesterdayMembers: List<RoomMember>,
    val todayMission: RoomMissionResult,
    val settlement: RoomSettlement
)
