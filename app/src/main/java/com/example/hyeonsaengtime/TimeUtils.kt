package com.example.hyeonsaengtime

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    /**
     * 시작 시간과 종료 시간을 받아 날짜별(yyyyMMdd) 누적 밀리초로 분할합니다.
     */
    fun splitTimeByDays(startTime: Long, endTime: Long): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        if (startTime >= endTime) return result

        var currentStart = startTime
        while (currentStart < endTime) {
            val calendar = Calendar.getInstance().apply { timeInMillis = currentStart }
            val currentDayStr = dateFormat.format(calendar.time)

            // 현재 날짜의 다음 날 자정(00:00:00.000) 구하기
            val nextMidnightCal = Calendar.getInstance().apply {
                timeInMillis = currentStart
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val nextMidnight = nextMidnightCal.timeInMillis

            // 이번 구간의 끝은 전체 종료 시간과 다음 날 자정 중 더 작은 값
            val currentEnd = minOf(endTime, nextMidnight)
            val elapsed = currentEnd - currentStart

            result[currentDayStr] = (result[currentDayStr] ?: 0L) + elapsed
            currentStart = currentEnd
        }
        return result
    }

    /**
     * 오늘의 시작 시점(00:00:00.000)의 타임스탬프를 가져옵니다.
     */
    fun getTodayStartMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}