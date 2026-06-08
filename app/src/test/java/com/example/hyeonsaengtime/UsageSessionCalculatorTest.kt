package com.example.hyeonsaengtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class UsageSessionCalculatorTest {
    private val seoul: TimeZone = TimeZone.getTimeZone("Asia/Seoul")

    @Test
    fun splitByLocalDate_keepsSameDaySessionTogether() {
        val result = UsageSessionCalculator.splitByLocalDate(
            startMillis = millis(seoul, 2026, 6, 8, 10, 0),
            endMillis = millis(seoul, 2026, 6, 8, 12, 30),
            timeZone = seoul
        )

        assertEquals(
            listOf(DailyDuration("20260608", hours(2) + minutes(30))),
            result
        )
    }

    @Test
    fun splitByLocalDate_splitsSessionAcrossMidnight() {
        val result = UsageSessionCalculator.splitByLocalDate(
            startMillis = millis(seoul, 2026, 6, 8, 23, 50),
            endMillis = millis(seoul, 2026, 6, 9, 0, 10),
            timeZone = seoul
        )

        assertEquals(
            listOf(
                DailyDuration("20260608", minutes(10)),
                DailyDuration("20260609", minutes(10))
            ),
            result
        )
    }

    @Test
    fun splitByLocalDate_splitsLongSessionAcrossMultipleDates() {
        val result = UsageSessionCalculator.splitByLocalDate(
            startMillis = millis(seoul, 2026, 6, 8, 22, 0),
            endMillis = millis(seoul, 2026, 6, 10, 1, 30),
            timeZone = seoul
        )

        assertEquals(
            listOf(
                DailyDuration("20260608", hours(2)),
                DailyDuration("20260609", hours(24)),
                DailyDuration("20260610", hours(1) + minutes(30))
            ),
            result
        )
    }

    @Test
    fun splitByLocalDate_returnsEmptyWhenEndIsNotAfterStart() {
        val start = millis(seoul, 2026, 6, 8, 10, 0)

        assertTrue(
            UsageSessionCalculator.splitByLocalDate(start, start, seoul).isEmpty()
        )
        assertTrue(
            UsageSessionCalculator.splitByLocalDate(start, start - 1L, seoul).isEmpty()
        )
    }

    @Test
    fun splitByLocalDate_usesProvidedTimeZoneForDateKey() {
        val utc = TimeZone.getTimeZone("UTC")
        val start = millis(utc, 2026, 6, 7, 15, 30)

        val result = UsageSessionCalculator.splitByLocalDate(
            startMillis = start,
            endMillis = start + minutes(10),
            timeZone = seoul
        )

        assertEquals(listOf(DailyDuration("20260608", minutes(10))), result)
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
