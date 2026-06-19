package com.example.hyeonsaengtime

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class RoomSimulationTest {
    private val seoul: TimeZone = TimeZone.getTimeZone("Asia/Seoul")
    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    @Test
    fun timeZoneStore_initializesPersonalTimeZoneOnce() {
        val prefs = RoomFakeSharedPreferences()

        val first = HyeonSaengTimeZoneStore.getOrCreatePersonalTimeZone(prefs, seoul)
        val second = HyeonSaengTimeZoneStore.getOrCreatePersonalTimeZone(prefs, utc)

        assertEquals("Asia/Seoul", first.id)
        assertEquals("Asia/Seoul", second.id)
        assertEquals(
            "Asia/Seoul",
            prefs.getString(HyeonSaengTimeZoneStore.KEY_PERSONAL_TIME_ZONE_ID, null)
        )
    }

    @Test
    fun roomStore_rejectsBlankRoomNameOrNickname() {
        val prefs = RoomFakeSharedPreferences()
        val store = RoomLocalStore(prefs)

        val result = store.createRoom(
            roomName = "",
            nickname = "루시",
            isAnonymous = true,
            hostTimeZone = seoul
        )

        assertTrue(result is RoomCreateResult.Invalid)
        assertFalse(store.isRoomCreated())
    }

    @Test
    fun roomStore_createsRoomWithHostTimeZoneAndStableSlot() {
        val now = millis(seoul, 2026, 6, 10, 12, 0)
        val prefs = RoomFakeSharedPreferences()
        val store = RoomLocalStore(prefs)

        val result = store.createRoom(
            roomName = "방 1",
            nickname = "루시",
            isAnonymous = true,
            nowMillis = now,
            hostTimeZone = seoul
        )

        val state = (result as RoomCreateResult.Created).state
        assertTrue(store.isRoomCreated())
        assertEquals("방 1", state.roomName)
        assertEquals("루시", state.nickname)
        assertEquals("Asia/Seoul", state.hostTimeZoneId)
        assertTrue(state.mySlot in 1..HyeonSaengRules.ROOM_MEMBER_COUNT)
        assertEquals("20260609", state.lastSettledDateKey)
        assertEquals(state.mySlot, prefs.getInt(RoomLocalStore.KEY_ROOM_MY_SLOT, 0))
    }

    @Test
    fun roomStore_leaveRoomClearsRoomStateOnly() {
        val prefs = roomPrefs(
            RoomLocalStore.roomTotalKey("20260610") to hours(2),
            RoomLocalStore.missionKey("20260610") to RoomMissionId.TOTAL_AT_LEAST_STREAK_TARGET.name
        )
        val store = RoomLocalStore(prefs)

        store.leaveRoom()

        assertFalse(store.isRoomCreated())
        assertFalse(prefs.contains(RoomLocalStore.KEY_ROOM_NAME))
        assertFalse(prefs.contains(RoomLocalStore.KEY_ROOM_NICKNAME))
        assertFalse(prefs.contains(RoomLocalStore.KEY_ROOM_ANONYMOUS))
        assertFalse(prefs.contains(RoomLocalStore.KEY_ROOM_LEVEL))
        assertFalse(prefs.contains(RoomLocalStore.KEY_ROOM_XP))
        assertFalse(prefs.contains(RoomLocalStore.KEY_ROOM_LAST_SETTLED_DATE))
        assertFalse(prefs.contains(RoomLocalStore.KEY_ROOM_MY_SLOT))
        assertFalse(prefs.contains(RoomLocalStore.KEY_ROOM_HOST_TIME_ZONE_ID))
        assertEquals(hours(2), prefs.getLong(RoomLocalStore.roomTotalKey("20260610"), 0L))
        assertEquals(
            RoomMissionId.TOTAL_AT_LEAST_STREAK_TARGET.name,
            prefs.getString(RoomLocalStore.missionKey("20260610"), null)
        )
    }

    @Test
    fun roomStore_returnsInvalidWithoutDeletingBrokenRoomData() {
        val prefs = RoomFakeSharedPreferences(
            mapOf<String, Any>(
                RoomLocalStore.KEY_ROOM_CREATED to true,
                RoomLocalStore.KEY_ROOM_NAME to "",
                RoomLocalStore.KEY_ROOM_NICKNAME to "루시",
                RoomLocalStore.KEY_ROOM_HOST_TIME_ZONE_ID to "Invalid/Zone"
            )
        )
        val store = RoomLocalStore(prefs)

        val result = store.loadRoomState()

        assertTrue(result is RoomLoadResult.Invalid)
        assertTrue(prefs.contains(RoomLocalStore.KEY_ROOM_CREATED))
        assertTrue(prefs.contains(RoomLocalStore.KEY_ROOM_NAME))
        val reasons = (result as RoomLoadResult.Invalid).reasons
        assertTrue(reasons.contains("${RoomLocalStore.KEY_ROOM_NAME} blank"))
        assertTrue(reasons.contains("${RoomLocalStore.KEY_ROOM_HOST_TIME_ZONE_ID} invalid"))
    }

    @Test
    fun roomStore_clampsPersistedXpWhenLoadingRoom() {
        val prefs = roomPrefs(
            RoomLocalStore.KEY_ROOM_XP to 999,
            RoomLocalStore.KEY_ROOM_LEVEL to 9
        )
        val store = RoomLocalStore(prefs)

        val result = store.loadRoomState()

        val state = (result as RoomLoadResult.Created).state
        assertEquals(500, state.xp)
        assertEquals(5, state.level)
        assertEquals(500, prefs.getInt(RoomLocalStore.KEY_ROOM_XP, -1))
        assertEquals(5, prefs.getInt(RoomLocalStore.KEY_ROOM_LEVEL, -1))
    }

    @Test
    fun roomMembers_keepAnonymousSlotsStable() {
        val first = RoomMemberGenerator.generate(
            dateKey = "20260610",
            mySlot = 3,
            myNickname = "루시",
            isAnonymous = true,
            myMillis = hours(5)
        )
        val second = RoomMemberGenerator.generate(
            dateKey = "20260610",
            mySlot = 3,
            myNickname = "루시",
            isAnonymous = true,
            myMillis = hours(5)
        )

        assertEquals(first, second)
        assertEquals((1..8).toList(), first.map { it.slot })
        assertEquals("익명 3 (나)", first[2].displayName)
        assertEquals(hours(5), first[2].hyeonsaengMillis)
        assertTrue(first[2].isMe)
        assertFalse(first[0].isMe)
    }

    @Test
    fun roomMissions_calculateEveryCandidate() {
        val members = successMembers()

        val missions = RoomMissionCalculator.calculate(members)

        assertTrue(missionIsMet(missions, RoomMissionId.ALL_MEMBERS_AT_LEAST_NINE_HOURS))
        assertTrue(missionIsMet(missions, RoomMissionId.TOTAL_AT_LEAST_STREAK_TARGET))
        assertTrue(missionIsMet(missions, RoomMissionId.MIN_MEMBER_AT_LEAST_STREAK_TARGET))
        assertTrue(missionIsMet(missions, RoomMissionId.TOP_MEMBER_AT_LEAST_EIGHTEEN_HOURS))
    }

    @Test
    fun roomMissions_useClearDisplayTitles() {
        val titles = RoomMissionCalculator.calculate(successMembers())
            .associate { it.id to it.title }

        assertEquals("전원 기록 9시간 이상", titles[RoomMissionId.ALL_MEMBERS_AT_LEAST_NINE_HOURS])
        assertEquals("방 합산 기록 128시간 이상", titles[RoomMissionId.TOTAL_AT_LEAST_STREAK_TARGET])
        assertEquals("개인 최저 기록 16시간 이상", titles[RoomMissionId.MIN_MEMBER_AT_LEAST_STREAK_TARGET])
        assertEquals("개인 최고 기록 18시간 이상", titles[RoomMissionId.TOP_MEMBER_AT_LEAST_EIGHTEEN_HOURS])
    }

    @Test
    fun roomSettlement_appliesOnlySelectedMissionXpOncePerDate() {
        val first = RoomMissionCalculator.settle(
            dateKey = "20260609",
            members = successMembers(),
            selectedMissionId = RoomMissionId.TOP_MEMBER_AT_LEAST_EIGHTEEN_HOURS,
            currentXp = 100,
            lastSettledDateKey = "20260608"
        )
        val second = RoomMissionCalculator.settle(
            dateKey = "20260609",
            members = successMembers(),
            selectedMissionId = RoomMissionId.TOP_MEMBER_AT_LEAST_EIGHTEEN_HOURS,
            currentXp = first.xpAfter,
            lastSettledDateKey = "20260609"
        )

        assertTrue(first.wasApplied)
        assertEquals(20, first.appliedXpDelta)
        assertEquals(120, first.xpAfter)
        assertEquals(2, first.levelAfter)
        assertFalse(second.wasApplied)
        assertEquals(0, second.appliedXpDelta)
        assertEquals(120, second.xpAfter)
    }

    @Test
    fun roomSettlement_clampsXpAtFiveHundredAndCanDecreaseFromMax() {
        val success = RoomMissionCalculator.settle(
            dateKey = "20260609",
            members = successMembers(),
            selectedMissionId = RoomMissionId.TOP_MEMBER_AT_LEAST_EIGHTEEN_HOURS,
            currentXp = 500,
            lastSettledDateKey = "20260608"
        )
        val miss = RoomMissionCalculator.settle(
            dateKey = "20260610",
            members = missedMembers(),
            selectedMissionId = RoomMissionId.TOP_MEMBER_AT_LEAST_EIGHTEEN_HOURS,
            currentXp = 500,
            lastSettledDateKey = "20260609"
        )

        assertEquals(500, success.xpAfter)
        assertEquals(5, success.levelAfter)
        assertEquals(490, miss.xpAfter)
        assertEquals(5, miss.levelAfter)
    }

    @Test
    fun roomLevelCalculator_usesXpZeroToFiveHundred() {
        assertEquals(1, RoomLevelCalculator.levelForXp(0))
        assertEquals(1, RoomLevelCalculator.levelForXp(99))
        assertEquals(2, RoomLevelCalculator.levelForXp(100))
        assertEquals(4, RoomLevelCalculator.levelForXp(399))
        assertEquals(5, RoomLevelCalculator.levelForXp(400))
        assertEquals(5, RoomLevelCalculator.levelForXp(500))
        assertEquals(100, RoomLevelCalculator.xpInCurrentLevel(500))
    }

    @Test
    fun roomOverview_keepsSelectedMissionStableForSameRoomDate() {
        val now = millis(seoul, 2026, 6, 10, 9, 0)
        val prefs = roomPrefs(
            RoomLocalStore.KEY_ROOM_LAST_SETTLED_DATE to "20260608",
            RoomLocalStore.KEY_ROOM_XP to 100
        )
        val store = RoomLocalStore(prefs)

        val first = store.getRoomOverview(now)
        val second = store.getRoomOverview(now)

        val firstOverview = (first as RoomOverviewResult.Created).overview
        val secondOverview = (second as RoomOverviewResult.Created).overview
        assertEquals(firstOverview.settlement.mission.id, secondOverview.settlement.mission.id)
        assertEquals(
            firstOverview.settlement.mission.id.name,
            prefs.getString(RoomLocalStore.missionKey("20260609"), null)
        )
        assertEquals(
            firstOverview.todayMission.id.name,
            prefs.getString(RoomLocalStore.missionKey("20260610"), null)
        )
        assertTrue(firstOverview.settlement.wasApplied)
        assertFalse(secondOverview.settlement.wasApplied)
    }

    @Test
    fun roomOverview_keepsTodayMissionSeparateFromYesterdaySettlementMission() {
        val now = millis(seoul, 2026, 6, 10, 9, 0)
        val prefs = roomPrefs(
            RoomLocalStore.KEY_ROOM_LAST_SETTLED_DATE to "20260608",
            RoomLocalStore.missionKey("20260609") to RoomMissionId.ALL_MEMBERS_AT_LEAST_NINE_HOURS.name,
            RoomLocalStore.missionKey("20260610") to RoomMissionId.TOP_MEMBER_AT_LEAST_EIGHTEEN_HOURS.name
        )
        val store = RoomLocalStore(prefs)

        val result = store.getRoomOverview(now)

        val overview = (result as RoomOverviewResult.Created).overview
        assertEquals(RoomMissionId.TOP_MEMBER_AT_LEAST_EIGHTEEN_HOURS, overview.todayMission.id)
        assertEquals(RoomMissionId.ALL_MEMBERS_AT_LEAST_NINE_HOURS, overview.settlement.mission.id)
    }

    @Test
    fun roomOverview_updatesSelectedMissionDateWhenRoomDateChanges() {
        val prefs = roomPrefs(
            RoomLocalStore.KEY_ROOM_LAST_SETTLED_DATE to "20260608"
        )
        val store = RoomLocalStore(prefs)

        store.getRoomOverview(millis(seoul, 2026, 6, 10, 9, 0))
        store.getRoomOverview(millis(seoul, 2026, 6, 11, 9, 0))

        assertTrue(prefs.contains(RoomLocalStore.missionKey("20260609")))
        assertTrue(prefs.contains(RoomLocalStore.missionKey("20260610")))
        assertTrue(prefs.contains(RoomLocalStore.missionKey("20260611")))
        assertEquals("20260610", prefs.getString(RoomLocalStore.KEY_ROOM_LAST_SETTLED_DATE, null))
    }

    @Test
    fun roomOverview_separatesPersonalAndRoomTodayMillis() {
        val now = millis(utc, 2026, 6, 8, 16, 0)
        val prefs = roomPrefs(
            HyeonSaengTimeZoneStore.KEY_PERSONAL_TIME_ZONE_ID to "UTC",
            RoomLocalStore.KEY_ROOM_HOST_TIME_ZONE_ID to "Asia/Seoul",
            RoomLocalStore.KEY_ROOM_LAST_SETTLED_DATE to "20260608",
            HyeonSaengLocalStore.totalKey("20260608") to hours(3),
            RoomLocalStore.roomTotalKey("20260609") to hours(4)
        )
        val store = RoomLocalStore(prefs)

        val result = store.getRoomOverview(now)

        val overview = (result as RoomOverviewResult.Created).overview
        assertEquals("20260608", overview.personalTodayDateKey)
        assertEquals("20260609", overview.roomTodayDateKey)
        assertEquals(hours(3), overview.personalTodayMillis)
        assertEquals(hours(4), overview.roomTodayMillis)
        assertEquals(hours(4), overview.todayMembers.first { it.isMe }.hyeonsaengMillis)
    }

    @Test
    fun roomOverview_fallsBackToPersonalTotalWhenRoomTotalDoesNotExist() {
        val now = millis(seoul, 2026, 6, 10, 9, 0)
        val prefs = roomPrefs(
            RoomLocalStore.KEY_ROOM_LAST_SETTLED_DATE to "20260609",
            HyeonSaengLocalStore.totalKey("20260610") to hours(6)
        )
        val store = RoomLocalStore(prefs)

        val result = store.getRoomOverview(now)

        val overview = (result as RoomOverviewResult.Created).overview
        assertEquals(hours(6), overview.roomTodayMillis)
        assertEquals(hours(6), overview.todayMembers.first { it.isMe }.hyeonsaengMillis)
    }

    @Test
    fun roomOverview_exposesYesterdayMembersForResultSummary() {
        val now = millis(seoul, 2026, 6, 10, 9, 0)
        val prefs = roomPrefs(
            RoomLocalStore.KEY_ROOM_LAST_SETTLED_DATE to "20260608",
            RoomLocalStore.roomTotalKey("20260609") to hours(4)
        )
        val store = RoomLocalStore(prefs)

        val result = store.getRoomOverview(now)

        val overview = (result as RoomOverviewResult.Created).overview
        assertEquals("20260609", overview.roomYesterdayDateKey)
        assertEquals(HyeonSaengRules.ROOM_MEMBER_COUNT, overview.yesterdayMembers.size)
        assertEquals(hours(4), overview.yesterdayMembers.first { it.isMe }.hyeonsaengMillis)
    }

    @Test
    fun roomResultSummary_ranksMembersAndClassifiesGoalStages() {
        val members = listOf(
            RoomMember(slot = 1, displayName = "나", hyeonsaengMillis = hours(18), isMe = true),
            RoomMember(slot = 2, displayName = "진행", hyeonsaengMillis = hours(10), isMe = false),
            RoomMember(slot = 3, displayName = "시작", hyeonsaengMillis = 0L, isMe = false),
            RoomMember(slot = 4, displayName = "낮음", hyeonsaengMillis = hours(5), isMe = false)
        )

        val summary = RoomResultSummaryCalculator.summarize(
            members = members,
            dailyGoalMillis = hours(16)
        )

        assertEquals(1, summary.myRank)
        assertEquals(4, summary.totalMembers)
        assertEquals(MemberGoalStage.ACHIEVED, summary.comparisons[0].stage)
        assertEquals(MemberGoalStage.IN_PROGRESS, summary.comparisons[1].stage)
        assertEquals(MemberGoalStage.START, summary.comparisons.last().stage)
    }

    private fun missionIsMet(
        missions: List<RoomMissionResult>,
        id: RoomMissionId
    ): Boolean {
        return missions.first { it.id == id }.isMet
    }

    private fun roomPrefs(vararg overrides: Pair<String, Any>): RoomFakeSharedPreferences {
        val baseValues = mutableMapOf<String, Any>(
            RoomLocalStore.KEY_ROOM_CREATED to true,
            RoomLocalStore.KEY_ROOM_NAME to "테스트방",
            RoomLocalStore.KEY_ROOM_NICKNAME to "루시",
            RoomLocalStore.KEY_ROOM_ANONYMOUS to false,
            RoomLocalStore.KEY_ROOM_LEVEL to 1,
            RoomLocalStore.KEY_ROOM_XP to 0,
            RoomLocalStore.KEY_ROOM_LAST_SETTLED_DATE to "20260608",
            RoomLocalStore.KEY_ROOM_MY_SLOT to 1,
            RoomLocalStore.KEY_ROOM_HOST_TIME_ZONE_ID to "Asia/Seoul"
        )
        overrides.forEach { (key, value) -> baseValues[key] = value }
        return RoomFakeSharedPreferences(baseValues)
    }

    private fun successMembers(): List<RoomMember> {
        return (1..8).map { slot ->
            RoomMember(
                slot = slot,
                displayName = "멤버 $slot",
                hyeonsaengMillis = if (slot == 8) hours(18) else hours(16),
                isMe = false
            )
        }
    }

    private fun missedMembers(): List<RoomMember> {
        return (1..8).map { slot ->
            RoomMember(
                slot = slot,
                displayName = "멤버 $slot",
                hyeonsaengMillis = hours(1),
                isMe = false
            )
        }
    }

    private fun millis(
        timeZone: TimeZone,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): Long {
        return Calendar.getInstance(timeZone).apply {
            clear()
            set(year, month - 1, day, hour, minute, 0)
        }.timeInMillis
    }

    private fun hours(value: Long): Long = value * 60L * 60L * 1000L
}

private class RoomFakeSharedPreferences(
    initialValues: Map<String, Any> = emptyMap()
) : SharedPreferences {
    private val values = initialValues.toMutableMap()

    override fun getAll(): MutableMap<String, *> = values.toMutableMap()

    override fun getString(key: String?, defValue: String?): String? {
        return values[key] as? String ?: defValue
    }

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(
        key: String?,
        defValues: MutableSet<String>?
    ): MutableSet<String>? {
        return (values[key] as? Set<String>)?.toMutableSet() ?: defValues
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return values[key] as? Int ?: defValue
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return values[key] as? Long ?: defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return values[key] as? Float ?: defValue
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return values[key] as? Boolean ?: defValue
    }

    override fun contains(key: String?): Boolean = values.containsKey(key)

    override fun edit(): SharedPreferences.Editor = FakeEditor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) = Unit

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) = Unit

    private inner class FakeEditor : SharedPreferences.Editor {
        private val changes = mutableMapOf<String, Any?>()
        private var shouldClear = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            if (key != null) changes[key] = value
            return this
        }

        override fun putStringSet(
            key: String?,
            values: MutableSet<String>?
        ): SharedPreferences.Editor {
            if (key != null) changes[key] = values?.toSet()
            return this
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            if (key != null) changes[key] = value
            return this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            if (key != null) changes[key] = value
            return this
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            if (key != null) changes[key] = value
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            if (key != null) changes[key] = value
            return this
        }

        override fun remove(key: String?): SharedPreferences.Editor {
            if (key != null) changes[key] = null
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            shouldClear = true
            return this
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (shouldClear) values.clear()
            changes.forEach { (key, value) ->
                if (value == null) {
                    values.remove(key)
                } else {
                    values[key] = value
                }
            }
        }
    }
}
