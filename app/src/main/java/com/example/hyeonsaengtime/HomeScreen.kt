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
    onResultClick: () -> Unit,
    onRoomClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localStore = remember(context) { HyeonSaengLocalStore(context) }
    var todayProgress by remember {
        mutableStateOf(
            HyeonSaengProgressCalculator.calculate(totalLockedMillis = 0L)
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
            formatHyeonSaengDuration(todayProgress.hyeonsaengMillis),
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
            else "streak까지 ${formatRemainingDuration(todayProgress.remainingMillisForStreak)} 남음",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(32.dp))

        Text("${streakCount}일 연속 달성", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(32.dp))

        Button(onClick = onResultClick) {
            Text("어제 결과 보기")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRoomClick) {
            Text("방 보기")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onSettingsClick) {
            Text("설정")
        }
    }
}
