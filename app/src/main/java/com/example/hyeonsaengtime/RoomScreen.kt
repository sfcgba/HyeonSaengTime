package com.example.hyeonsaengtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import com.example.hyeonsaengtime.ui.theme.HyeonSaengPrimarySoft
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
        onCreate = { roomName ->
            when (roomStore.createRoom(roomName, settings.nickname, isAnonymous = false)) {
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
    onCreate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var roomName by remember { mutableStateOf("저녁 9시 같이 자기") }
    var selectedPromise by remember { mutableStateOf(RoomPromise.EveningTogether) }
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

        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HyeonSaengPrimarySoft, RoundedCornerShape(30.dp))
                .padding(28.dp)
        ) {
            Text(
                "함께 쌓기",
                color = HyeonSaengPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "랭킹 없이,\n같은 시간을 나란히 쌓아요.",
                color = HyeonSaengText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
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
            isError = roomName.isBlank(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))
        Text(
            "오늘의 작은 약속",
            color = HyeonSaengTextMuted,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(12.dp))
        RoomPromiseGrid(
            selected = selectedPromise,
            onSelected = { selectedPromise = it }
        )

        Spacer(Modifier.height(30.dp))
        InviteCodeCard()

        Spacer(Modifier.height(120.dp))
        Button(
            onClick = { onCreate(roomName) },
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
private fun RoomPromiseGrid(
    selected: RoomPromise,
    onSelected: (RoomPromise) -> Unit
) {
    RoomPromise.values().toList().chunked(2).forEach { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rowItems.forEach { promise ->
                PromiseChip(
                    promise = promise,
                    selected = selected == promise,
                    onClick = { onSelected(promise) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PromiseChip(
    promise: RoomPromise,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) HyeonSaengPrimarySoft else HyeonSaengSurface,
            contentColor = if (selected) HyeonSaengPrimary else HyeonSaengTextMuted
        ),
        modifier = modifier
            .height(62.dp)
            .border(
                width = 1.dp,
                color = if (selected) HyeonSaengPrimary else HyeonSaengBorder,
                shape = RoundedCornerShape(999.dp)
            )
    ) {
        Text(
            promise.label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
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

private enum class RoomPromise(val label: String) {
    EveningTogether("저녁 함께 1h"),
    BeforeSleep("잠들기 전 2h"),
    WeekendMorning("주말 오전 3h"),
    Custom("직접 정하기")
}
