package com.example.hyeonsaengtime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hyeonsaengtime.ui.theme.HyeonSaengBackground
import com.example.hyeonsaengtime.ui.theme.HyeonSaengPrimary
import com.example.hyeonsaengtime.ui.theme.HyeonSaengSurface
import com.example.hyeonsaengtime.ui.theme.HyeonSaengText
import com.example.hyeonsaengtime.ui.theme.HyeonSaengTextMuted

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsStore = remember(context) { HyeonSaengSettingsStore(context) }
    val roomStore = remember(context) { RoomLocalStore(context) }
    val initialSettings = remember(settingsStore) { settingsStore.getSettings() }

    var nickname by remember { mutableStateOf(initialSettings.nickname) }
    var isRoomAnonymous by remember { mutableStateOf(initialSettings.isRoomAnonymous) }
    var extraNotificationsEnabled by remember {
        mutableStateOf(initialSettings.extraNotificationsEnabled)
    }
    var message by remember { mutableStateOf("") }

    val canSave = nickname.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HyeonSaengBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack, modifier = Modifier.width(64.dp)) {
                Text("뒤로")
            }
            Text(
                "설정",
                color = HyeonSaengText,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(64.dp))
        }

        Spacer(Modifier.height(28.dp))
        SettingsSectionTitle("개인")
        SettingsCard {
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("닉네임") },
                singleLine = true,
                isError = nickname.isBlank(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "목표 시간은 16시간으로 고정돼요",
                color = HyeonSaengTextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(28.dp))
        SettingsSectionTitle("방")
        SettingsCard {
            SettingSwitchRow(
                title = "방 익명 모드",
                description = if (initialSettings.isRoomCreated) {
                    "모든 멤버를 익명으로 표시해요"
                } else {
                    "방을 만들면 설정할 수 있어요"
                },
                checked = isRoomAnonymous,
                enabled = initialSettings.isRoomCreated,
                onCheckedChange = { isRoomAnonymous = it }
            )
            if (initialSettings.isRoomCreated) {
                Spacer(Modifier.height(14.dp))
                TextButton(
                    onClick = {
                        roomStore.leaveRoom()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        "방 나가기",
                        color = HyeonSaengTextMuted,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        SettingsSectionTitle("알림")
        SettingsCard {
            SettingSwitchRow(
                title = "복귀 알림",
                description = "잠금해제 후 조용한 격려",
                checked = extraNotificationsEnabled,
                enabled = true,
                onCheckedChange = { extraNotificationsEnabled = it }
            )
        }

        Spacer(Modifier.height(36.dp))
        Button(
            onClick = {
                val nicknameSaved = settingsStore.saveNickname(nickname)
                if (nicknameSaved) {
                    if (initialSettings.isRoomCreated) {
                        settingsStore.saveRoomAnonymous(isRoomAnonymous)
                    }
                    settingsStore.saveExtraNotificationsEnabled(extraNotificationsEnabled)
                    onBack()
                } else {
                    message = "닉네임을 확인해 주세요"
                }
            },
            enabled = canSave,
            colors = ButtonDefaults.buttonColors(containerColor = HyeonSaengPrimary),
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Text("저장", style = MaterialTheme.typography.titleMedium)
        }

        if (message.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                color = HyeonSaengTextMuted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        color = HyeonSaengTextMuted,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = HyeonSaengSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            content = content
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = HyeonSaengText,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                description,
                color = HyeonSaengTextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}
