package com.example.hyeonsaengtime

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("hyeonsaeng", Context.MODE_PRIVATE)

    var sleepHours by remember { mutableStateOf(prefs.getInt("sleep_hours", 7).toFloat()) }
    var goalHours  by remember { mutableStateOf(prefs.getInt("goal_hours",  8).toFloat()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("설정", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        Text("수면시간: ${sleepHours.toInt()}시간", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Slider(
            value = sleepHours,
            onValueChange = { sleepHours = it },
            valueRange = 0f..12f,
            steps = 7,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        Text("하루 목표: ${goalHours.toInt()}시간", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Slider(
            value = goalHours,
            onValueChange = { goalHours = it },
            valueRange = 1f..16f,
            steps = 14,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("취소")
            }
            Button(
                onClick = {
                    prefs.edit()
                        .putInt("sleep_hours", sleepHours.toInt())
                        .putInt("goal_hours", goalHours.toInt())
                        .apply()
                    onBack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("저장")
            }
        }
    }
}