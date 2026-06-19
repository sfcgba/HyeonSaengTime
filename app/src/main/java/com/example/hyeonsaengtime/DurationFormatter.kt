package com.example.hyeonsaengtime

import java.util.Locale

fun formatHyeonSaengDuration(millis: Long): String {
    val h = millis / 1000 / 3600
    val m = (millis / 1000 % 3600) / 60
    val s = millis / 1000 % 60
    return "${h}시간 ${m}분 ${s}초"
}

fun formatHourMinuteDuration(millis: Long): String {
    val h = millis / 1000 / 3600
    val m = (millis / 1000 % 3600) / 60
    return "${h}h ${m}m"
}

fun formatRemainingDuration(millis: Long): String {
    val h = millis / 1000 / 3600
    val m = (millis / 1000 % 3600) / 60
    return if (h > 0) "${h}시간 ${m}분" else "${m}분"
}

fun formatDigitalDuration(millis: Long): String {
    val totalSeconds = millis.coerceAtLeast(0L) / 1000L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}

fun formatClockHourMinuteDuration(millis: Long): String {
    val totalMinutes = millis.coerceAtLeast(0L) / 1000L / 60L
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return String.format(Locale.US, "%02d:%02d", hours, minutes)
}
