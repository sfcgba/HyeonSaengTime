package com.example.hyeonsaengtime

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var totalLockedMillis by remember { mutableStateOf(0L) }
    var sleepHours      by remember { mutableStateOf(7) }
    var goalHours       by remember { mutableStateOf(8) }
    var streakCount     by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        updateStreakIfNeeded(context)
        while (true) {
            val prefs = context.getSharedPreferences("hyeonsaeng", Context.MODE_PRIVATE)
            val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            totalLockedMillis = prefs.getLong("total_$today", 0L)
            sleepHours        = prefs.getInt("sleep_hours", 7)
            goalHours         = prefs.getInt("goal_hours", 8)
            streakCount       = prefs.getInt("streak_count", 0)
            delay(1000L)
        }
    }

    val sleepMillis      = sleepHours * 3600 * 1000L
    val hyeonsaengMillis = maxOf(0L, totalLockedMillis - sleepMillis)
    val goalMillis       = goalHours * 3600 * 1000L
    val goalProgress     = if (goalMillis > 0)
        (hyeonsaengMillis.toFloat() / goalMillis).coerceAtMost(1f) else 0f
    val goalMet          = hyeonsaengMillis >= goalMillis

    val hours   = hyeonsaengMillis / 1000 / 3600
    val minutes = (hyeonsaengMillis / 1000 % 3600) / 60
    val seconds = hyeonsaengMillis / 1000 % 60

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("오늘 현생시간", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "${hours}시간 ${minutes}분 ${seconds}초",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(Modifier.height(32.dp))

        Text("목표: ${goalHours}시간", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { goalProgress },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (goalMet) "목표 달성!"
            else "목표까지 ${formatRemaining(goalMillis - hyeonsaengMillis)} 남음",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(32.dp))

        Text("${streakCount}일 연속 달성", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(32.dp))

        Button(onClick = onSettingsClick) {
            Text("설정")
        }
    }
}

fun updateStreakIfNeeded(context: Context) {
    val prefs     = context.getSharedPreferences("hyeonsaeng", Context.MODE_PRIVATE)
    val today     = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    val lastCheck = prefs.getString("streak_last_date", "") ?: ""
    if (lastCheck == today) return

    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val yesterday     = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
    val yesterdayTotal = prefs.getLong("total_$yesterday", 0L)

    val sleepMillis = prefs.getInt("sleep_hours", 7) * 3600 * 1000L
    val goalMillis  = prefs.getInt("goal_hours", 8)  * 3600 * 1000L
    val yesterdayHs = maxOf(0L, yesterdayTotal - sleepMillis)

    val newStreak = if (yesterdayHs >= goalMillis)
        prefs.getInt("streak_count", 0) + 1 else 0

    prefs.edit()
        .putInt("streak_count", newStreak)
        .putString("streak_last_date", today)
        .apply()
}

fun formatRemaining(millis: Long): String {
    val h = millis / 1000 / 3600
    val m = (millis / 1000 % 3600) / 60
    return if (h > 0) "${h}시간 ${m}분" else "${m}분"
}