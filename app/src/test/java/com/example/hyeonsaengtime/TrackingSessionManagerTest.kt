package com.example.hyeonsaengtime

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class TrackingSessionManagerTest {
    private val seoul: TimeZone = TimeZone.getTimeZone("Asia/Seoul")
    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(seoul)
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun handleScreenOff_storesActiveSessionStart() {
        val prefs = FakeSharedPreferences()
        val start = millis(2026, 6, 8, 10, 0)

        val result = TrackingSessionManager.handleScreenOff(prefs, start)

        assertEquals(TrackingSessionUpdate.STARTED, result.update)
        assertEquals(start, prefs.getLong(TrackingSessionManager.KEY_ACTIVE_LOCK_START, 0L))
    }

    @Test
    fun handleUserPresent_finalizesActiveSession() {
        val prefs = FakeSharedPreferences()
        val start = millis(2026, 6, 8, 10, 0)
        TrackingSessionManager.handleScreenOff(prefs, start)

        val result = TrackingSessionManager.handleUserPresent(
            prefs,
            millis(2026, 6, 8, 12, 30)
        )

        assertEquals(TrackingSessionUpdate.FINALIZED, result.update)
        assertEquals(hours(2) + minutes(30), prefs.getLong("total_20260608", 0L))
        assertFalse(prefs.contains(TrackingSessionManager.KEY_ACTIVE_LOCK_START))
    }

    @Test
    fun handleScreenOn_whenUnlocked_finalizesActiveSession() {
        val prefs = FakeSharedPreferences()
        val start = millis(2026, 6, 8, 10, 0)
        TrackingSessionManager.handleScreenOff(prefs, start)

        val result = TrackingSessionManager.handleScreenOn(
            prefs = prefs,
            isKeyguardLocked = false,
            eventAtMillis = millis(2026, 6, 8, 11, 0)
        )

        assertEquals(TrackingSessionUpdate.FINALIZED, result.update)
        assertEquals(hours(1), prefs.getLong("total_20260608", 0L))
        assertFalse(prefs.contains(TrackingSessionManager.KEY_ACTIVE_LOCK_START))
    }

    @Test
    fun handleScreenOn_whenLocked_waitsForUserPresent() {
        val prefs = FakeSharedPreferences()
        val start = millis(2026, 6, 8, 10, 0)
        TrackingSessionManager.handleScreenOff(prefs, start)

        val result = TrackingSessionManager.handleScreenOn(
            prefs = prefs,
            isKeyguardLocked = true,
            eventAtMillis = millis(2026, 6, 8, 11, 0)
        )

        assertEquals(TrackingSessionUpdate.WAITING_FOR_USER_PRESENT, result.update)
        assertEquals(0L, prefs.getLong("total_20260608", 0L))
        assertTrue(prefs.contains(TrackingSessionManager.KEY_ACTIVE_LOCK_START))
    }

    @Test
    fun handleUserPresent_withoutActiveSession_doesNothing() {
        val prefs = FakeSharedPreferences()

        val result = TrackingSessionManager.handleUserPresent(
            prefs,
            millis(2026, 6, 8, 11, 0)
        )

        assertEquals(TrackingSessionUpdate.IGNORED, result.update)
        assertEquals(0L, prefs.getLong("total_20260608", 0L))
    }

    @Test
    fun handleUserPresent_splitsTotalsAcrossMidnight() {
        val prefs = FakeSharedPreferences()
        TrackingSessionManager.handleScreenOff(prefs, millis(2026, 6, 8, 23, 50))

        val result = TrackingSessionManager.handleUserPresent(
            prefs,
            millis(2026, 6, 9, 0, 10)
        )

        assertEquals(TrackingSessionUpdate.FINALIZED, result.update)
        assertEquals(minutes(10), prefs.getLong("total_20260608", 0L))
        assertEquals(minutes(10), prefs.getLong("total_20260609", 0L))
    }

    @Test
    fun handleUserPresent_savesPersonalAndRoomTotalsWithSeparateTimeZones() {
        val utc = TimeZone.getTimeZone("UTC")
        val prefs = FakeSharedPreferences(
            mapOf<String, Any>(
                HyeonSaengTimeZoneStore.KEY_PERSONAL_TIME_ZONE_ID to "UTC",
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
        )
        TrackingSessionManager.handleScreenOff(prefs, millis(utc, 2026, 6, 8, 15, 30))

        val result = TrackingSessionManager.handleUserPresent(
            prefs,
            millis(utc, 2026, 6, 8, 16, 30)
        )

        assertEquals(TrackingSessionUpdate.FINALIZED, result.update)
        assertEquals(hours(1), prefs.getLong(HyeonSaengLocalStore.totalKey("20260608"), 0L))
        assertEquals(hours(1), prefs.getLong(RoomLocalStore.roomTotalKey("20260609"), 0L))
    }

    @Test
    fun recordScreenEvent_storesLastEventLog() {
        val prefs = FakeSharedPreferences()
        val eventAt = millis(2026, 6, 8, 10, 0)

        TrackingSessionManager.recordScreenEvent(prefs, "android.intent.action.SCREEN_OFF", eventAt)

        assertEquals(
            "android.intent.action.SCREEN_OFF",
            prefs.getString(TrackingSessionManager.KEY_LAST_SCREEN_EVENT_ACTION, null)
        )
        assertEquals(eventAt, prefs.getLong(TrackingSessionManager.KEY_LAST_SCREEN_EVENT_AT, 0L))
    }

    @Test
    fun unlockNotificationPolicy_onlyNotifiesForFinalizedSessionsWhenEnabled() {
        assertTrue(
            UnlockNotificationPolicy.shouldNotify(
                TrackingSessionResult(TrackingSessionUpdate.FINALIZED),
                extraNotificationsEnabled = true
            )
        )
        assertFalse(
            UnlockNotificationPolicy.shouldNotify(
                TrackingSessionResult(TrackingSessionUpdate.FINALIZED),
                extraNotificationsEnabled = false
            )
        )
        assertFalse(
            UnlockNotificationPolicy.shouldNotify(
                TrackingSessionResult(TrackingSessionUpdate.IGNORED),
                extraNotificationsEnabled = true
            )
        )
    }

    private fun millis(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): Long {
        return millis(seoul, year, month, day, hour, minute)
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

    private fun minutes(value: Long): Long = value * 60L * 1000L
}
