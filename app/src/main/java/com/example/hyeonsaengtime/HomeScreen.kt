package com.example.hyeonsaengtime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hyeonsaengtime.ui.theme.HyeonSaengAccent
import com.example.hyeonsaengtime.ui.theme.HyeonSaengAccentSoft
import com.example.hyeonsaengtime.ui.theme.HyeonSaengBackground
import com.example.hyeonsaengtime.ui.theme.HyeonSaengBorder
import com.example.hyeonsaengtime.ui.theme.HyeonSaengPrimary
import com.example.hyeonsaengtime.ui.theme.HyeonSaengSurface
import com.example.hyeonsaengtime.ui.theme.HyeonSaengSurfaceSoft
import com.example.hyeonsaengtime.ui.theme.HyeonSaengText
import com.example.hyeonsaengtime.ui.theme.HyeonSaengTextMuted
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onCreateRoomClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localStore = remember(context) { HyeonSaengLocalStore(context) }
    val roomStore = remember(context) { RoomLocalStore(context) }
    var todayProgress by remember {
        mutableStateOf(
            HyeonSaengProgressCalculator.calculate(totalLockedMillis = 0L)
        )
    }
    var streakCount by remember { mutableStateOf(0) }
    var isFocusSessionActive by remember { mutableStateOf(localStore.isFocusSessionActive()) }
    var roomResult by remember {
        mutableStateOf<RoomOverviewResult>(roomStore.getRoomOverview())
    }

    LaunchedEffect(localStore, roomStore) {
        streakCount = localStore.updateStreakIfNeeded()
        while (true) {
            todayProgress = localStore.getTodayProgress()
            streakCount = localStore.getStreakCount()
            isFocusSessionActive = localStore.isFocusSessionActive()
            roomResult = roomStore.getRoomOverview()
            delay(1000L)
        }
    }

    val roomOverview = (roomResult as? RoomOverviewResult.Created)?.overview

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HyeonSaengBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 18.dp)
    ) {
        MainTopBar(
            title = roomOverview?.state?.roomName ?: "현생시간",
            onSettingsClick = onSettingsClick
        )
        Spacer(Modifier.height(22.dp))

        TimerCard(
            progress = todayProgress,
            streakCount = streakCount
        )

        if (roomOverview != null) {
            Spacer(Modifier.height(22.dp))
            MissionCard(roomOverview.todayMission)
        }

        Spacer(Modifier.height(18.dp))
        when (val result = roomResult) {
            RoomOverviewResult.NotCreated -> SoloRoomCard(
                progress = todayProgress,
                isFocusSessionActive = isFocusSessionActive,
                onCreateRoomClick = onCreateRoomClick
            )

            is RoomOverviewResult.Invalid -> InvalidRoomCard(result.reasons)

            is RoomOverviewResult.Created -> RoomMembersCard(
                overview = result.overview,
                currentTodayMillis = todayProgress.hyeonsaengMillis,
                isFocusSessionActive = isFocusSessionActive
            )
        }
    }
}

@Composable
private fun MainTopBar(
    title: String,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(64.dp))
        Text(
            text = title,
            color = HyeonSaengText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onSettingsClick, modifier = Modifier.width(64.dp)) {
            Text("설정")
        }
    }
}

@Composable
private fun TimerCard(
    progress: HyeonSaengProgress,
    streakCount: Int
) {
    Card(
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = HyeonSaengSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 162.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_streak_leaf),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "연속 ${streakCount}일",
                    color = HyeonSaengText,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = "오늘의 현생 시간",
                color = HyeonSaengTextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatDigitalDuration(progress.hyeonsaengMillis),
                color = HyeonSaengPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "오늘의 목표까지 ${formatClockHourMinuteDuration(progress.hyeonsaengMillis)}/${formatClockHourMinuteDuration(HyeonSaengRules.STREAK_REQUIRED_MILLIS)}",
                color = HyeonSaengTextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MissionCard(mission: RoomMissionResult) {
    Card(
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = HyeonSaengSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(63.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(33.dp)
                    .clip(CircleShape)
                    .background(HyeonSaengAccentSoft),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_mission_target),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "오늘의 미션",
                    color = HyeonSaengTextMuted,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    mission.title,
                    color = HyeonSaengText,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SoloRoomCard(
    progress: HyeonSaengProgress,
    isFocusSessionActive: Boolean,
    onCreateRoomClick: () -> Unit
) {
    RoomCardFrame(minHeight = 282.dp) {
        RoomCardHeader(
            title = "방 인원",
            trailing = "1명 집중 중 · 1/1",
            subtitle = "잘하고 있어요"
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            MemberBubble(
                name = "나",
                timeText = formatClockHourMinuteDuration(progress.hyeonsaengMillis),
                active = isFocusSessionActive,
                self = true
            )
        }
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(HyeonSaengBorder)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "함께하면 더 오래 집중할 수 있어요",
            color = HyeonSaengTextMuted,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onCreateRoomClick,
            colors = ButtonDefaults.buttonColors(containerColor = HyeonSaengPrimary),
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(52.dp)
        ) {
            Text("+ 방 만들기", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun RoomMembersCard(
    overview: RoomOverview,
    currentTodayMillis: Long,
    isFocusSessionActive: Boolean
) {
    val members = overview.todayMembers.map { member ->
        if (member.isMe) {
            member.copy(hyeonsaengMillis = currentTodayMillis)
        } else {
            member
        }
    }
    val activeCount = if (isFocusSessionActive) 1 else 0
    val xpInLevel = RoomLevelCalculator.xpInCurrentLevel(overview.state.xp)

    RoomCardFrame(minHeight = 282.dp) {
        RoomCardHeader(
            title = "방 인원",
            trailing = "Lv.${overview.state.level}  $xpInLevel/${HyeonSaengRules.ROOM_XP_PER_LEVEL}",
            subtitle = "현재 ${activeCount}명이 열심히 살고 있어요 · ${members.size}/${HyeonSaengRules.ROOM_MEMBER_COUNT}"
        )
        Spacer(Modifier.height(22.dp))
        members.chunked(4).forEach { rowMembers ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowMembers.forEach { member ->
                    MemberBubble(
                        name = displayMemberName(member),
                        timeText = formatClockHourMinuteDuration(member.hyeonsaengMillis),
                        active = member.isMe && isFocusSessionActive,
                        self = member.isMe,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(HyeonSaengRules.ROOM_MEMBER_COUNT / 2 - rowMembers.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun InvalidRoomCard(reasons: List<String>) {
    RoomCardFrame {
        Text(
            "방 데이터 확인 필요",
            color = HyeonSaengText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            reasons.joinToString(", "),
            color = HyeonSaengTextMuted,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun RoomCardFrame(
    minHeight: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = HyeonSaengSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            content = content
        )
    }
}

@Composable
private fun RoomCardHeader(
    title: String,
    trailing: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = HyeonSaengText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            trailing,
            color = HyeonSaengPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
    Spacer(Modifier.height(8.dp))
    Text(
        subtitle,
        color = HyeonSaengTextMuted,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun MemberBubble(
    name: String,
    timeText: String,
    active: Boolean,
    self: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            if (active) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(
                            elevation = 11.dp,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = HyeonSaengAccent.copy(alpha = 0.4f),
                            spotColor = HyeonSaengAccent.copy(alpha = 0.4f)
                        )
                        .clip(CircleShape)
                        .background(HyeonSaengAccent)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(HyeonSaengSurfaceSoft)
                )
            }
            if (self) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(HyeonSaengPrimary)
                )
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (active) HyeonSaengAccent else HyeonSaengSurfaceSoft)
                        .border(2.dp, HyeonSaengSurface, CircleShape)
                )
            }
            Text(
                text = if (self) "나" else "",
                color = HyeonSaengText,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = name,
            color = HyeonSaengText,
            fontWeight = if (self) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = timeText,
            color = HyeonSaengTextMuted,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

private fun displayMemberName(member: RoomMember): String {
    return if (member.isMe) member.displayName.removeSuffix(" (나)") else member.displayName
}
