package com.example.hyeonsaengtime

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class DailyDuration(
    val dateKey: String,
    val durationMillis: Long
)

object UsageSessionCalculator {
    // cursor = startMillis -> nextLocalMidnight 자정 기준으로 나눠주는 체크포인트
    // segmentEnd = cursor로 나눈 날짜별 시간 값들이 들어감
    fun splitByLocalDate(
        startMillis: Long,
        endMillis: Long,
        timeZone: TimeZone = TimeZone.getDefault()
    ): List<DailyDuration> {
        if (endMillis <= startMillis) return emptyList()

        val sessions = mutableListOf<DailyDuration>()
        var cursor = startMillis

        while (cursor < endMillis) {
            val segmentEnd = minOf(endMillis, nextLocalMidnight(cursor, timeZone))
            val durationMillis = segmentEnd - cursor

            if (durationMillis > 0L) {
                sessions += DailyDuration(
                    dateKey = dateKey(cursor, timeZone),
                    durationMillis = durationMillis
                )
            }

            cursor = segmentEnd
        }

        return sessions
    }

    private fun nextLocalMidnight(millis: Long, timeZone: TimeZone): Long {
        val calendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = millis
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun dateKey(millis: Long, timeZone: TimeZone): String {
        return SimpleDateFormat("yyyyMMdd", Locale.US).apply {
            this.timeZone = timeZone
        }.format(Date(millis))
    }
}
