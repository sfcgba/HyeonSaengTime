package com.example.hyeonsaengtime

import android.content.Context
import android.content.SharedPreferences
import java.util.TimeZone

class RoomLocalStore(
    private val prefs: SharedPreferences
) {
    constructor(
        context: Context
    ) : this(context.getSharedPreferences(HyeonSaengLocalStore.PREFS_NAME, Context.MODE_PRIVATE))

    fun isRoomCreated(): Boolean = prefs.getBoolean(KEY_ROOM_CREATED, false)

    fun leaveRoom() {
        prefs.edit()
            .remove(KEY_ROOM_CREATED)
            .remove(KEY_ROOM_NAME)
            .remove(KEY_ROOM_NICKNAME)
            .remove(KEY_ROOM_ANONYMOUS)
            .remove(KEY_ROOM_LEVEL)
            .remove(KEY_ROOM_XP)
            .remove(KEY_ROOM_LAST_SETTLED_DATE)
            .remove(KEY_ROOM_MY_SLOT)
            .remove(KEY_ROOM_HOST_TIME_ZONE_ID)
            .apply()
    }

    fun createRoom(
        roomName: String,
        nickname: String,
        isAnonymous: Boolean,
        nowMillis: Long = System.currentTimeMillis(),
        hostTimeZone: TimeZone = TimeZone.getDefault()
    ): RoomCreateResult {
        val normalizedRoomName = roomName.trim()
        val normalizedNickname = nickname.trim()
        val reasons = buildList {
            if (normalizedRoomName.isBlank()) add("$KEY_ROOM_NAME blank")
            if (normalizedNickname.isBlank()) add("$KEY_ROOM_NICKNAME blank")
        }
        if (reasons.isNotEmpty()) return RoomCreateResult.Invalid(reasons)

        val lastSettledDateKey = DateKeyFormatter.yesterdayKey(nowMillis, hostTimeZone)
        val state = RoomState(
            roomName = normalizedRoomName,
            nickname = normalizedNickname,
            isAnonymous = isAnonymous,
            level = 1,
            xp = 0,
            lastSettledDateKey = lastSettledDateKey,
            mySlot = RoomMemberGenerator.stableSlot(normalizedRoomName, normalizedNickname),
            hostTimeZoneId = hostTimeZone.id
        )

        saveRoomState(state)
        return RoomCreateResult.Created(state)
    }

    fun loadRoomState(): RoomLoadResult {
        if (!isRoomCreated()) return RoomLoadResult.NotCreated

        val reasons = mutableListOf<String>()
        val roomName = readRequiredString(KEY_ROOM_NAME, reasons)
        val nickname = readRequiredString(KEY_ROOM_NICKNAME, reasons)
        val hostTimeZoneId = readRequiredString(KEY_ROOM_HOST_TIME_ZONE_ID, reasons)
        if (hostTimeZoneId != null && HyeonSaengTimeZoneStore.timeZoneOrNull(hostTimeZoneId) == null) {
            reasons += "$KEY_ROOM_HOST_TIME_ZONE_ID invalid"
        }

        if (reasons.isNotEmpty()) {
            return RoomLoadResult.Invalid(reasons)
        }

        val normalizedRoomName = normalizeLoadedRoomName(requireNotNull(roomName))
        val xp = RoomLevelCalculator.clampXp(prefs.getInt(KEY_ROOM_XP, 0))
        val level = RoomLevelCalculator.levelForXp(xp)
        persistXpAndLevelIfNeeded(xp, level)

        return RoomLoadResult.Created(
            RoomState(
                roomName = normalizedRoomName,
                nickname = requireNotNull(nickname),
                isAnonymous = prefs.getBoolean(KEY_ROOM_ANONYMOUS, false),
                level = level,
                xp = xp,
                lastSettledDateKey = prefs.getString(KEY_ROOM_LAST_SETTLED_DATE, "") ?: "",
                mySlot = prefs.getInt(KEY_ROOM_MY_SLOT, 1)
                    .coerceIn(1, HyeonSaengRules.ROOM_MEMBER_COUNT),
                hostTimeZoneId = requireNotNull(hostTimeZoneId)
            )
        )
    }

    fun getRoomOverview(nowMillis: Long = System.currentTimeMillis()): RoomOverviewResult {
        val stateBeforeSettlement = when (val result = loadRoomState()) {
            RoomLoadResult.NotCreated -> return RoomOverviewResult.NotCreated
            is RoomLoadResult.Invalid -> return RoomOverviewResult.Invalid(result.reasons)
            is RoomLoadResult.Created -> result.state
        }
        val roomTimeZone = HyeonSaengTimeZoneStore.timeZoneOrNull(stateBeforeSettlement.hostTimeZoneId)
            ?: return RoomOverviewResult.Invalid(listOf("$KEY_ROOM_HOST_TIME_ZONE_ID invalid"))
        val personalTimeZone = HyeonSaengTimeZoneStore.getOrCreatePersonalTimeZone(prefs)

        val personalTodayDateKey = DateKeyFormatter.todayKey(nowMillis, personalTimeZone)
        val roomTodayDateKey = DateKeyFormatter.todayKey(nowMillis, roomTimeZone)
        val roomYesterdayDateKey = DateKeyFormatter.yesterdayKey(nowMillis, roomTimeZone)
        val todayMembersBeforeSettlement = getMembers(
            dateKey = roomTodayDateKey,
            state = stateBeforeSettlement,
            useRoomTotal = true
        )
        val todayMissionId = getOrCreateMissionId(
            dateKey = roomTodayDateKey,
            roomName = stateBeforeSettlement.roomName
        )
        val todayMission = RoomMissionCalculator.calculateSelected(
            members = todayMembersBeforeSettlement,
            selectedMissionId = todayMissionId
        )
        val yesterdayMembers = getMembers(
            dateKey = roomYesterdayDateKey,
            state = stateBeforeSettlement,
            useRoomTotal = true
        )
        val yesterdayMissionId = getOrCreateMissionId(
            dateKey = roomYesterdayDateKey,
            roomName = stateBeforeSettlement.roomName
        )
        val settlement = RoomMissionCalculator.settle(
            dateKey = roomYesterdayDateKey,
            members = yesterdayMembers,
            selectedMissionId = yesterdayMissionId,
            currentXp = stateBeforeSettlement.xp,
            lastSettledDateKey = stateBeforeSettlement.lastSettledDateKey
        )
        val stateAfterSettlement = if (settlement.wasApplied) {
            stateBeforeSettlement.copy(
                level = settlement.levelAfter,
                xp = settlement.xpAfter,
                lastSettledDateKey = settlement.dateKey
            ).also { saveRoomState(it) }
        } else {
            stateBeforeSettlement.copy(
                level = settlement.levelAfter,
                xp = settlement.xpAfter
            )
        }

        return RoomOverviewResult.Created(
            RoomOverview(
                state = stateAfterSettlement,
                personalTimeZoneId = personalTimeZone.id,
                personalTodayDateKey = personalTodayDateKey,
                roomTodayDateKey = roomTodayDateKey,
                roomYesterdayDateKey = roomYesterdayDateKey,
                personalTodayMillis = getPersonalTotalMillis(personalTodayDateKey),
                roomTodayMillis = getRoomTotalMillis(roomTodayDateKey),
                todayMembers = getMembers(
                    dateKey = roomTodayDateKey,
                    state = stateAfterSettlement,
                    useRoomTotal = true
                ),
                yesterdayMembers = yesterdayMembers,
                todayMission = todayMission,
                settlement = settlement
            )
        )
    }

    fun addRoomDurations(
        durations: List<DailyDuration>,
        editor: SharedPreferences.Editor
    ) {
        durations.forEach { dailyDuration ->
            val key = roomTotalKey(dailyDuration.dateKey)
            val accumulated = prefs.getLong(key, 0L)
            editor.putLong(key, accumulated + dailyDuration.durationMillis)
        }
    }

    private fun getMembers(
        dateKey: String,
        state: RoomState,
        useRoomTotal: Boolean
    ): List<RoomMember> {
        val myMillis = if (useRoomTotal) getRoomTotalMillis(dateKey) else getPersonalTotalMillis(dateKey)
        return RoomMemberGenerator.generate(
            dateKey = dateKey,
            mySlot = state.mySlot,
            myNickname = state.nickname,
            isAnonymous = state.isAnonymous,
            myMillis = myMillis
        )
    }

    private fun getOrCreateMissionId(
        dateKey: String,
        roomName: String
    ): RoomMissionId {
        val key = missionKey(dateKey)
        val parsedMissionId = (prefs.getString(key, "") ?: "").toRoomMissionIdOrNull()
        if (parsedMissionId != null) {
            return parsedMissionId
        }

        val newMissionId = RoomMissionCalculator.selectMissionId(dateKey, roomName)
        prefs.edit()
            .putString(key, newMissionId.name)
            .apply()
        return newMissionId
    }

    private fun getPersonalTotalMillis(dateKey: String): Long {
        return prefs.getLong(HyeonSaengLocalStore.totalKey(dateKey), 0L)
    }

    private fun getRoomTotalMillis(dateKey: String): Long {
        val roomTotal = prefs.getLong(roomTotalKey(dateKey), -1L)
        if (roomTotal >= 0L) return roomTotal
        return getPersonalTotalMillis(dateKey)
    }

    private fun readRequiredString(
        key: String,
        reasons: MutableList<String>
    ): String? {
        if (!prefs.contains(key)) {
            reasons += "$key missing"
            return null
        }

        val value = prefs.getString(key, null)
        if (value.isNullOrBlank()) {
            reasons += "$key blank"
            return null
        }
        return value
    }

    private fun normalizeLoadedRoomName(roomName: String): String {
        if (roomName != LEGACY_DEFAULT_ROOM_NAME) return roomName

        prefs.edit()
            .putString(KEY_ROOM_NAME, DEFAULT_ROOM_NAME)
            .apply()
        return DEFAULT_ROOM_NAME
    }

    private fun persistXpAndLevelIfNeeded(xp: Int, level: Int) {
        val storedXp = prefs.getInt(KEY_ROOM_XP, 0)
        val storedLevel = prefs.getInt(KEY_ROOM_LEVEL, 1)
        if (storedXp == xp && storedLevel == level) return

        prefs.edit()
            .putInt(KEY_ROOM_XP, xp)
            .putInt(KEY_ROOM_LEVEL, level)
            .apply()
    }

    private fun saveRoomState(state: RoomState) {
        val xp = RoomLevelCalculator.clampXp(state.xp)
        val level = RoomLevelCalculator.levelForXp(xp)
        prefs.edit()
            .putBoolean(KEY_ROOM_CREATED, true)
            .putString(KEY_ROOM_NAME, state.roomName)
            .putString(KEY_ROOM_NICKNAME, state.nickname)
            .putBoolean(KEY_ROOM_ANONYMOUS, state.isAnonymous)
            .putInt(KEY_ROOM_LEVEL, level)
            .putInt(KEY_ROOM_XP, xp)
            .putString(KEY_ROOM_LAST_SETTLED_DATE, state.lastSettledDateKey)
            .putInt(KEY_ROOM_MY_SLOT, state.mySlot)
            .putString(KEY_ROOM_HOST_TIME_ZONE_ID, state.hostTimeZoneId)
            .apply()
    }

    private fun String.toRoomMissionIdOrNull(): RoomMissionId? {
        return RoomMissionId.values().firstOrNull { it.name == this }
    }

    companion object {
        const val KEY_ROOM_CREATED = "room_created"
        const val KEY_ROOM_NAME = "room_name"
        const val KEY_ROOM_NICKNAME = "room_nickname"
        const val KEY_ROOM_ANONYMOUS = "room_anonymous"
        const val KEY_ROOM_LEVEL = "room_level"
        const val KEY_ROOM_XP = "room_xp"
        const val KEY_ROOM_LAST_SETTLED_DATE = "room_last_settled_date"
        const val KEY_ROOM_MY_SLOT = "room_my_slot"
        const val KEY_ROOM_HOST_TIME_ZONE_ID = "room_host_time_zone_id"

        const val INITIAL_NICKNAME = "나"
        const val DEFAULT_ROOM_NAME = "방 이름"
        private const val LEGACY_DEFAULT_ROOM_NAME = "저녁 9시 같이 자기"

        fun roomTotalKey(dateKey: String): String = "room_total_$dateKey"
        fun missionKey(dateKey: String): String = "room_mission_$dateKey"
    }
}
