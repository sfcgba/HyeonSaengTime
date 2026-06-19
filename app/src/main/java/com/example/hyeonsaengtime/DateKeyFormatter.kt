package com.example.hyeonsaengtime

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateKeyFormatter {
    fun dateKey(
        millis: Long,
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        return SimpleDateFormat("yyyyMMdd", Locale.US).apply {
            this.timeZone = timeZone
        }.format(Date(millis))
    }

    fun todayKey(
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): String = dateKey(nowMillis, timeZone)

    fun yesterdayKey(
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        val calendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return dateKey(calendar.timeInMillis, timeZone)
    }
}
