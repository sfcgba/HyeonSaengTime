package com.example.hyeonsaengtime

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ResultScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localStore = remember(context) { HyeonSaengLocalStore(context) }
    var dayResult by remember { mutableStateOf<DayResult?>(null) }

    LaunchedEffect(localStore) {
        dayResult = localStore.getYesterdayResult()
    }

    val result = dayResult

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (result == null) {
            Text("결과를 불러오는 중", style = MaterialTheme.typography.bodyLarge)
            return@Column
        }

        Text("어제 결과", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            formatHyeonSaengDuration(result.hyeonsaengMillis),
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(Modifier.height(32.dp))

        Text(
            "streak 기준: ${result.streakRequiredHours}시간",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { result.streakProgress },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (result.isStreakRequirementMet) {
                "어제 streak 기준을 채웠어요"
            } else {
                "어제는 기준까지 ${formatRemainingDuration(result.remainingMillisForStreak)} 남았어요"
            },
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(32.dp))

        Text(
            "${result.streakCountAfterUpdate}일 연속 달성",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(32.dp))

        Button(onClick = onBack) {
            Text("홈으로")
        }
    }
}
