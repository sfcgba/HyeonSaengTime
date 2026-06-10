package com.example.hyeonsaengtime

fun formatHyeonSaengDuration(millis: Long): String {
    val h = millis / 1000 / 3600
    val m = (millis / 1000 % 3600) / 60
    val s = millis / 1000 % 60
    return "${h}시간 ${m}분 ${s}초"
}

fun formatRemainingDuration(millis: Long): String {
    val h = millis / 1000 / 3600
    val m = (millis / 1000 % 3600) / 60
    return if (h > 0) "${h}시간 ${m}분" else "${m}분"
}
