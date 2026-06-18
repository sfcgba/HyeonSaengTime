package com.example.hyeonsaengtime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.hyeonsaengtime.ui.theme.HyeonSaengAccent
import com.example.hyeonsaengtime.ui.theme.HyeonSaengAccentSoft
import com.example.hyeonsaengtime.ui.theme.HyeonSaengBackground
import com.example.hyeonsaengtime.ui.theme.HyeonSaengPrimary
import com.example.hyeonsaengtime.ui.theme.HyeonSaengSurface
import com.example.hyeonsaengtime.ui.theme.HyeonSaengSurfaceSoft
import com.example.hyeonsaengtime.ui.theme.HyeonSaengText
import com.example.hyeonsaengtime.ui.theme.HyeonSaengTextMuted

@Composable
fun ResultScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localStore = remember(context) { HyeonSaengLocalStore(context) }
    val roomStore = remember(context) { RoomLocalStore(context) }

    var dayResult by remember { mutableStateOf<DayResult?>(null) }
    var roomResult by remember { mutableStateOf<RoomOverviewResult>(RoomOverviewResult.NotCreated) }

    LaunchedEffect(localStore, roomStore) {
        dayResult = localStore.getYesterdayResult()
        roomResult = roomStore.getRoomOverview()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HyeonSaengBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        val result = dayResult
        if (result == null) {
            Text("결과를 불러오는 중", style = MaterialTheme.typography.bodyLarge)
        } else {
            DailyRecapContent(
                dayResult = result,
                roomResult = roomResult,
                onDismiss = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DailyRecapDialog(
    dayResult: DayResult,
    roomResult: RoomOverviewResult,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        DailyRecapContent(
            dayResult = dayResult,
            roomResult = roomResult,
            onDismiss = onDismiss,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DailyRecapContent(
    dayResult: DayResult,
    roomResult: RoomOverviewResult,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val roomOverview = (roomResult as? RoomOverviewResult.Created)?.overview
    val roomSummary = roomOverview?.let { overview ->
        RoomResultSummaryCalculator.summarize(
            members = overview.yesterdayMembers,
            dailyGoalMillis = HyeonSaengRules.STREAK_REQUIRED_MILLIS
        )
    }

    Surface(
        modifier = modifier.heightIn(max = 720.dp),
        shape = RoundedCornerShape(32.dp),
        color = HyeonSaengSurface,
        shadowElevation = 14.dp
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("닫기")
                }
            }

            Text(
                "어제의 현생 시간",
                color = HyeonSaengTextMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                formatHourMinuteDuration(dayResult.hyeonsaengMillis),
                color = HyeonSaengPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(Modifier.height(12.dp))
            ResultBadge(
                text = if (dayResult.isStreakRequirementMet) {
                    "연속 ${dayResult.streakCountAfterUpdate}일"
                } else {
                    "연속 기록 쉬어감"
                },
                soft = true
            )
            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResultMetricPanel(
                    title = "개인 목표",
                    value = "${HyeonSaengRules.STREAK_REQUIRED_HOURS}:00",
                    badge = if (dayResult.isStreakRequirementMet) "달성" else "미달성",
                    modifier = Modifier.weight(1f)
                )
                ResultMetricPanel(
                    title = "방 미션",
                    value = roomOverview?.settlement?.mission?.title ?: "방 없음",
                    badge = when {
                        roomOverview == null -> "대기"
                        roomOverview.settlement.mission.isMet -> "달성"
                        else -> "미달성"
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(20.dp))

            if (roomOverview == null) {
                ResultPlaceholderPanel(
                    title = "방 Lv.",
                    text = "방을 만들면 진행도가 표시돼요"
                )
            } else {
                RoomLevelPanel(roomOverview)
            }
            Spacer(Modifier.height(20.dp))

            if (roomSummary == null) {
                ResultPlaceholderPanel(
                    title = "방 내 순위",
                    text = "방을 만들면 순위가 표시돼요"
                )
            } else {
                RoomRankPanel(roomSummary)
            }
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = HyeonSaengPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("오늘도 시작하기", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ResultMetricPanel(
    title: String,
    value: String,
    badge: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 112.dp)
            .background(HyeonSaengSurfaceSoft, RoundedCornerShape(28.dp))
            .padding(18.dp)
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
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            ResultBadge(text = badge)
        }
        Spacer(Modifier.height(18.dp))
        Text(
            value,
            color = HyeonSaengText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RoomLevelPanel(
    overview: RoomOverview
) {
    val xpInLevel = RoomLevelCalculator.xpInCurrentLevel(overview.state.xp)
    val progress = xpInLevel.toFloat() / HyeonSaengRules.ROOM_XP_PER_LEVEL
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HyeonSaengSurfaceSoft, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "방 Lv.${overview.state.level}",
                    color = HyeonSaengText,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "$xpInLevel/${HyeonSaengRules.ROOM_XP_PER_LEVEL}",
                    color = HyeonSaengTextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            ResultBadge(
                text = if (overview.settlement.appliedXpDelta == 0) {
                    "정산 완료"
                } else {
                    "${formatXpDelta(overview.settlement.appliedXpDelta)} XP"
                },
                soft = true
            )
        }
        Spacer(Modifier.height(14.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            color = HyeonSaengPrimary,
            trackColor = HyeonSaengBackground,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
private fun RoomRankPanel(
    summary: RoomResultSummary
) {
    var expanded by remember(summary) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HyeonSaengSurfaceSoft, RoundedCornerShape(28.dp))
            .clickable { expanded = !expanded }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "방 내 순위",
                color = HyeonSaengText,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "${summary.myRank}위 /${summary.totalMembers}명 ${if (expanded) "접기" else "보기"}",
                color = HyeonSaengPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (expanded) {
            Spacer(Modifier.height(16.dp))
            summary.comparisons.forEach { comparison ->
                RoomComparisonRow(comparison)
            }
        }
    }
}

@Composable
private fun RoomComparisonRow(
    comparison: RoomMemberComparison
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "${comparison.rank}위",
            color = HyeonSaengPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(44.dp)
        )
        Text(
            comparison.member.displayName,
            color = HyeonSaengText,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            formatRemainingDuration(comparison.member.hyeonsaengMillis),
            color = HyeonSaengTextMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(76.dp)
        )
        ResultBadge(text = comparison.stage.label, soft = comparison.stage != MemberGoalStage.ACHIEVED)
    }
}

@Composable
private fun ResultPlaceholderPanel(
    title: String,
    text: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HyeonSaengSurfaceSoft, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            title,
            color = HyeonSaengText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(text, color = HyeonSaengTextMuted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ResultBadge(
    text: String,
    soft: Boolean = false
) {
    Box(
        modifier = Modifier
            .background(
                color = if (soft) HyeonSaengAccentSoft else HyeonSaengAccent,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = HyeonSaengText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatXpDelta(value: Int): String {
    return if (value > 0) "+$value" else value.toString()
}
