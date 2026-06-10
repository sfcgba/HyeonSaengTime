package com.example.hyeonsaengtime

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localStore = remember(context) { HyeonSaengLocalStore(context) }
    var todayProgress by remember {
        mutableStateOf(
            TodayProgressCalculator.calculate(totalLockedMillis = 0L)
        )
    }
    var streakCount by remember { mutableStateOf(0) }

    LaunchedEffect(localStore) {
        streakCount = localStore.updateStreakIfNeeded()
        while (true) {
            todayProgress = localStore.getTodayProgress()
            streakCount = localStore.getStreakCount()
            delay(1000L)
        }
    }

    val hyeonsaengMillis = todayProgress.todayHyeonsaengMillis

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

        Text(
            "streak 기준: ${todayProgress.streakRequiredHours}시간",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { todayProgress.streakProgress },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (todayProgress.isStreakRequirementMet) "오늘 streak 달성!"
            else "streak까지 ${formatRemaining(todayProgress.remainingMillisForStreak)} 남음",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(32.dp))

        Text("${streakCount}일 연속 달성", style = MaterialTheme.typography.titleMedium)
    }
}

fun formatRemaining(millis: Long): String {
    val h = millis / 1000 / 3600
    val m = (millis / 1000 % 3600) / 60
    return if (h > 0) "${h}시간 ${m}분" else "${m}분"
}
