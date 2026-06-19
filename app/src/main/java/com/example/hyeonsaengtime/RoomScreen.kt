package com.example.hyeonsaengtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.example.hyeonsaengtime.ui.theme.HyeonSaengBorder
import com.example.hyeonsaengtime.ui.theme.HyeonSaengPrimary
import com.example.hyeonsaengtime.ui.theme.HyeonSaengSurface
import com.example.hyeonsaengtime.ui.theme.HyeonSaengText
import com.example.hyeonsaengtime.ui.theme.HyeonSaengTextMuted

@Composable
fun RoomScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val roomStore = remember(context) { RoomLocalStore(context) }
    val settingsStore = remember(context) { HyeonSaengSettingsStore(context) }
    val settings = remember(settingsStore) { settingsStore.getSettings() }

    if (roomStore.isRoomCreated()) {
        RoomAlreadyCreatedContent(onBack = onBack, modifier = modifier)
        return
    }

    RoomCreateContent(
        nickname = settings.nickname,
        onBack = onBack,
        onCreate = { roomName, isAnonymous ->
            when (roomStore.createRoom(roomName, settings.nickname, isAnonymous)) {
                is RoomCreateResult.Created -> onCreated()
                is RoomCreateResult.Invalid -> Unit
            }
        },
        modifier = modifier
    )
}

@Composable
private fun RoomCreateContent(
    nickname: String,
    onBack: () -> Unit,
    onCreate: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var roomName by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    val canCreate = roomName.isNotBlank() && nickname.isNotBlank()

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
                "방 만들기",
                color = HyeonSaengText,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(64.dp))
        }

        Spacer(Modifier.height(30.dp))
        Text(
            "방 이름",
            color = HyeonSaengTextMuted,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = roomName,
            onValueChange = { roomName = it },
            singleLine = true,
            placeholder = { Text("방 이름") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(18.dp))
        AnonymousSettingCard(
            checked = isAnonymous,
            onCheckedChange = { isAnonymous = it }
        )

        Spacer(Modifier.height(30.dp))
        InviteCodeCard()

        Spacer(Modifier.height(120.dp))
        Button(
            onClick = { onCreate(roomName, isAnonymous) },
            enabled = canCreate,
            colors = ButtonDefaults.buttonColors(containerColor = HyeonSaengPrimary),
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("방 만들기", style = MaterialTheme.typography.titleMedium)
        }
        TextButton(
            onClick = {},
            enabled = false,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("이미 코드를 받았다면 참여하기")
        }
    }
}

@Composable
private fun AnonymousSettingCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HyeonSaengSurface, RoundedCornerShape(24.dp))
            .border(1.dp, HyeonSaengBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "방 익명 모드",
                color = HyeonSaengText,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "모든 멤버를 익명으로 표시해요",
                color = HyeonSaengTextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun InviteCodeCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HyeonSaengSurface, RoundedCornerShape(24.dp))
            .border(1.dp, HyeonSaengBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "초대 코드",
                color = HyeonSaengTextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "QUIET",
                color = HyeonSaengText,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.headlineMedium
            )
        }
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = HyeonSaengBackground),
            shape = RoundedCornerShape(999.dp)
        ) {
            Text("복사", color = HyeonSaengPrimary)
        }
    }
}

@Composable
private fun RoomAlreadyCreatedContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HyeonSaengBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HyeonSaengSurface, RoundedCornerShape(28.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "이미 방이 있어요",
                color = HyeonSaengText,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "홈에서 방 현황을 볼 수 있어요",
                color = HyeonSaengTextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = HyeonSaengPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("홈으로")
            }
        }
    }
}
