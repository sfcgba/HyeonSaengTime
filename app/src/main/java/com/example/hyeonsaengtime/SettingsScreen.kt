package com.example.hyeonsaengtime

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsStore = remember(context) { HyeonSaengSettingsStore(context) }
    val initialSettings = remember(settingsStore) { settingsStore.getSettings() }

    var dailyGoalText by remember { mutableStateOf(initialSettings.dailyGoalHours.toString()) }
    var nickname by remember { mutableStateOf(initialSettings.nickname) }
    var isAnonymous by remember { mutableStateOf(initialSettings.isAnonymous) }
    var extraNotificationsEnabled by remember {
        mutableStateOf(initialSettings.extraNotificationsEnabled)
    }
    var message by remember { mutableStateOf("") }

    val parsedDailyGoal = dailyGoalText.toIntOrNull()
    val canSave = parsedDailyGoal != null && nickname.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("설정", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = dailyGoalText,
            onValueChange = { value -> dailyGoalText = value.filter { it.isDigit() } },
            label = { Text("하루 목표") },
            suffix = { Text("시간") },
            singleLine = true,
            isError = parsedDailyGoal == null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            supportingText = {
                Text("${HyeonSaengSettingsStore.MIN_DAILY_GOAL_HOURS}-${HyeonSaengSettingsStore.MAX_DAILY_GOAL_HOURS}시간 사이로 저장돼요")
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("닉네임") },
            singleLine = true,
            isError = nickname.isBlank(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        SettingSwitchRow(
            title = "익명 표시",
            checked = isAnonymous,
            onCheckedChange = { isAnonymous = it }
        )
        Spacer(Modifier.height(8.dp))
        SettingSwitchRow(
            title = "알림",
            checked = extraNotificationsEnabled,
            onCheckedChange = { extraNotificationsEnabled = it }
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val goal = parsedDailyGoal ?: HyeonSaengRules.STREAK_REQUIRED_HOURS
                settingsStore.saveDailyGoalHours(goal)
                val profileSaved = settingsStore.saveProfile(nickname, isAnonymous)
                settingsStore.saveExtraNotificationsEnabled(extraNotificationsEnabled)
                dailyGoalText = settingsStore.getDailyGoalHours().toString()
                message = if (profileSaved) "설정을 저장했어요" else "닉네임을 확인해 주세요"
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("저장")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("홈으로")
        }

        if (message.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
