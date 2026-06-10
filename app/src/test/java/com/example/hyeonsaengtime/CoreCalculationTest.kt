package com.example.hyeonsaengtime

import android.content.SharedPreferences
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class CoreCalculationTest {
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
    fun dateKey_usesProvidedTimeZone() {
        val utc = TimeZone.getTimeZone("UTC")
        val millis = millis(utc, 2026, 6, 9, 15, 30)

        assertEquals("20260610", DateKeyFormatter.dateKey(millis, seoul))
    }

    @Test
    fun yesterdayKey_usesProvidedTimeZone() {
        val now = millis(seoul, 2026, 6, 10, 0, 10)

        assertEquals("20260609", DateKeyFormatter.yesterdayKey(now, seoul))
    }

    @Test
    fun todayProgress_usesTotalLockedMillisDirectly() {
        val progress = TodayProgressCalculator.calculate(totalLockedMillis = hours(9))

        assertEquals(hours(9), progress.todayHyeonsaengMillis)
        assertEquals(HyeonSaengRules.STREAK_REQUIRED_HOURS, progress.streakRequiredHours)
        assertEquals(HyeonSaengRules.STREAK_REQUIRED_MILLIS, progress.streakRequiredMillis)
        assertEquals(1f, progress.streakProgress, 0.0001f)
        assertTrue(progress.isStreakRequirementMet)
        assertEquals(0L, progress.remainingMillisForStreak)
    }

    @Test
    fun todayProgress_calculatesProgressAndRemainingTime() {
        val progress = TodayProgressCalculator.calculate(totalLockedMillis = hours(2))

        assertEquals(0.25f, progress.streakProgress, 0.0001f)
        assertFalse(progress.isStreakRequirementMet)
        assertEquals(hours(6), progress.remainingMillisForStreak)
    }

    @Test
    fun streakCalculator_incrementsWhenYesterdayMeetsGoal() {
        val update = StreakCalculator.calculate(
            todayDateKey = "20260610",
            yesterdayTotalMillis = hours(8),
            requiredMillis = HyeonSaengRules.STREAK_REQUIRED_MILLIS,
            currentStreakCount = 2,
            lastCheckDateKey = "20260609"
        )

        assertEquals(3, update.streakCount)
        assertEquals("20260610", update.lastCheckDateKey)
        assertTrue(update.shouldPersist)
    }

    @Test
    fun streakCalculator_resetsWhenYesterdayMissesGoal() {
        val update = StreakCalculator.calculate(
            todayDateKey = "20260610",
            yesterdayTotalMillis = hours(7),
            requiredMillis = HyeonSaengRules.STREAK_REQUIRED_MILLIS,
            currentStreakCount = 2,
            lastCheckDateKey = "20260609"
        )

        assertEquals(0, update.streakCount)
        assertEquals("20260610", update.lastCheckDateKey)
        assertTrue(update.shouldPersist)
    }

    @Test
    fun streakCalculator_doesNotPersistWhenAlreadyCheckedToday() {
        val update = StreakCalculator.calculate(
            todayDateKey = "20260610",
            yesterdayTotalMillis = hours(8),
            requiredMillis = HyeonSaengRules.STREAK_REQUIRED_MILLIS,
            currentStreakCount = 2,
            lastCheckDateKey = "20260610"
        )

        assertEquals(2, update.streakCount)
        assertEquals("20260610", update.lastCheckDateKey)
        assertFalse(update.shouldPersist)
    }

    @Test
    fun localStore_readsTodayProgressFromPreferences() {
        val now = millis(seoul, 2026, 6, 10, 12, 0)
        val prefs = CoreFakeSharedPreferences(
            mapOf<String, Any>(
                HyeonSaengLocalStore.totalKey("20260610") to hours(9)
            )
        )
        val store = HyeonSaengLocalStore(prefs)

        val progress = store.getTodayProgress(now)

        assertEquals(hours(9), progress.todayHyeonsaengMillis)
        assertEquals(HyeonSaengRules.STREAK_REQUIRED_HOURS, progress.streakRequiredHours)
        assertTrue(progress.isStreakRequirementMet)
    }

    @Test
    fun localStore_updatesStreakOncePerDay() {
        val now = millis(seoul, 2026, 6, 10, 9, 0)
        val prefs = CoreFakeSharedPreferences(
            mapOf<String, Any>(
                HyeonSaengLocalStore.totalKey("20260609") to hours(8),
                HyeonSaengLocalStore.KEY_STREAK_COUNT to 2
            )
        )
        val store = HyeonSaengLocalStore(prefs)

        assertEquals(3, store.updateStreakIfNeeded(now))
        assertEquals(3, prefs.getInt(HyeonSaengLocalStore.KEY_STREAK_COUNT, 0))
        assertEquals(
            "20260610",
            prefs.getString(HyeonSaengLocalStore.KEY_STREAK_LAST_DATE, null)
        )
    }

    @Test
    fun localStore_keepsStreakWhenAlreadyCheckedToday() {
        val now = millis(seoul, 2026, 6, 10, 9, 0)
        val prefs = CoreFakeSharedPreferences(
            mapOf<String, Any>(
                HyeonSaengLocalStore.totalKey("20260609") to hours(0),
                HyeonSaengLocalStore.KEY_STREAK_COUNT to 3,
                HyeonSaengLocalStore.KEY_STREAK_LAST_DATE to "20260610"
            )
        )
        val store = HyeonSaengLocalStore(prefs)

        assertEquals(3, store.updateStreakIfNeeded(now))
        assertEquals(3, prefs.getInt(HyeonSaengLocalStore.KEY_STREAK_COUNT, 0))
        assertEquals(
            "20260610",
            prefs.getString(HyeonSaengLocalStore.KEY_STREAK_LAST_DATE, null)
        )
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

private class CoreFakeSharedPreferences(
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
