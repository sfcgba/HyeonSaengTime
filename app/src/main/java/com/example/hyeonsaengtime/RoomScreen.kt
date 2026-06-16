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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun RoomScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val roomStore = remember(context) { RoomLocalStore(context) }
    var roomResult by remember {
        mutableStateOf<RoomOverviewResult>(roomStore.getRoomOverview())
    }

    LaunchedEffect(roomStore, roomResult::class) {
        if (roomResult !is RoomOverviewResult.Created) return@LaunchedEffect

        while (true) {
            roomResult = roomStore.getRoomOverview()
            delay(1000L)
        }
    }

    when (val result = roomResult) {
        RoomOverviewResult.NotCreated -> RoomCreateContent(
            onBack = onBack,
            onCreate = { roomName, nickname, isAnonymous ->
                roomResult = when (roomStore.createRoom(roomName, nickname, isAnonymous)) {
                    is RoomCreateResult.Created -> roomStore.getRoomOverview()
                    is RoomCreateResult.Invalid -> roomStore.getRoomOverview()
                }
            },
            modifier = modifier
        )

        is RoomOverviewResult.Invalid -> RoomInvalidContent(
            reasons = result.reasons,
            onBack = onBack,
            modifier = modifier
        )

        is RoomOverviewResult.Created -> RoomOverviewContent(
            overview = result.overview,
            onBack = onBack,
            modifier = modifier
        )
    }
}

@Composable
private fun RoomCreateContent(
    onBack: () -> Unit,
    onCreate: (String, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var roomName by remember { mutableStateOf(RoomLocalStore.INITIAL_ROOM_NAME) }
    var nickname by remember { mutableStateOf(RoomLocalStore.INITIAL_NICKNAME) }
    var isAnonymous by remember { mutableStateOf(false) }
    val canCreate = roomName.isNotBlank() && nickname.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("방 만들기", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = roomName,
            onValueChange = { roomName = it },
            label = { Text("방 이름") },
            isError = roomName.isBlank(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("닉네임") },
            isError = nickname.isBlank(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isAnonymous,
                onCheckedChange = { isAnonymous = it }
            )
            Text("익명 방")
        }
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onCreate(roomName, nickname, isAnonymous) },
            enabled = canCreate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("방 생성")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("홈으로")
        }
    }
}

@Composable
private fun RoomInvalidContent(
    reasons: List<String>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("방 데이터 확인 필요", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            reasons.joinToString(", "),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onBack) {
            Text("홈으로")
        }
    }
}

@Composable
private fun RoomOverviewContent(
    overview: RoomOverview,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(overview.state.roomName, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "오늘 내 현생시간 ${formatHyeonSaengDuration(overview.personalTodayMillis)}",
            style = MaterialTheme.typography.bodyLarge
        )
        if (overview.personalTimeZoneId != overview.state.hostTimeZoneId) {
            Spacer(Modifier.height(4.dp))
            Text(
                "방 기준 ${formatHyeonSaengDuration(overview.roomTodayMillis)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Lv.${overview.state.level} / XP ${RoomLevelCalculator.xpInCurrentLevel(overview.state.xp)}/${HyeonSaengRules.ROOM_XP_PER_LEVEL}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(24.dp))

        Text("오늘 미션", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "${if (overview.todayMission.isMet) "현재 달성" else "진행 중"} - ${overview.todayMission.title}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))

        Text("어제 정산", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "${if (overview.settlement.mission.isMet) "달성" else "미달성"} - ${overview.settlement.mission.title}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (overview.settlement.wasApplied) {
                "어제 정산 XP ${formatXpDelta(overview.settlement.appliedXpDelta)}"
            } else {
                "어제 정산 완료"
            },
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))

        Text("오늘 방 현황", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        overview.todayMembers.chunked(4).forEach { rowMembers ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowMembers.forEach { member ->
                    RoomMemberCard(
                        member = member,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("홈으로")
        }
    }
}

@Composable
private fun RoomMemberCard(
    member: RoomMember,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                member.displayName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            Text(
                formatRemainingDuration(member.hyeonsaengMillis),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatXpDelta(value: Int): String {
    return if (value > 0) "+$value" else value.toString()
}
