package com.example.hyeonsaengtime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val ResultBackground = Color(0xFFD3DAD5)
private val ResultCardColor = Color(0xFFFFFCF7)
private val ResultPanelColor = Color(0xFFF7F4F0)
private val ResultAccent = Color(0xFF2F5E5A)
private val ResultMutedText = Color(0xFF667A78)
private val ResultBadgeColor = Color(0xFFF5C46C)
private val ResultSoftBadgeColor = Color(0xFFFCE6BA)

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
            .background(ResultBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        val result = dayResult
        if (result == null) {
            Text("결과를 불러오는 중", style = MaterialTheme.typography.bodyLarge)
            return@Box
        }

        val roomOverview = (roomResult as? RoomOverviewResult.Created)?.overview
        val roomSummary = roomOverview?.let { overview ->
            RoomResultSummaryCalculator.summarize(
                members = overview.yesterdayMembers,
                dailyGoalMillis = result.streakRequiredMillis
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(32.dp),
            color = ResultCardColor,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onBack) {
                        Text("닫기")
                    }
                }

                Text(
                    "어제의 현생 시간",
                    color = ResultMutedText,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    formatHourMinuteDuration(result.hyeonsaengMillis),
                    color = ResultAccent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(Modifier.height(12.dp))
                ResultBadge(
                    text = if (result.isStreakRequirementMet) {
                        "연속 ${result.streakCountAfterUpdate}일"
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
                        value = "${result.streakRequiredHours}:00",
                        badge = if (result.isStreakRequirementMet) "달성" else "진행",
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
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = ResultAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("오늘도 시작하기", style = MaterialTheme.typography.titleMedium)
                }
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
            .background(ResultPanelColor, RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = Color(0xFF2F3E3E),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            ResultBadge(text = badge)
        }
        Spacer(Modifier.height(18.dp))
        Text(
            value,
            color = Color(0xFF2F3E3E),
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
            .background(ResultPanelColor, RoundedCornerShape(24.dp))
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
                    color = Color(0xFF2F3E3E),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "$xpInLevel/${HyeonSaengRules.ROOM_XP_PER_LEVEL}",
                    color = ResultMutedText,
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
            color = ResultAccent,
            trackColor = Color(0xFFECE7DE),
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
            .background(ResultPanelColor, RoundedCornerShape(28.dp))
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
                color = Color(0xFF2F3E3E),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "${summary.myRank}위 /${summary.totalMembers}명 ${if (expanded) "접기" else "보기"}",
                color = ResultAccent,
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
            color = ResultAccent,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(44.dp)
        )
        Text(
            comparison.member.displayName,
            color = Color(0xFF2F3E3E),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            formatRemainingDuration(comparison.member.hyeonsaengMillis),
            color = ResultMutedText,
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
            .background(ResultPanelColor, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            title,
            color = Color(0xFF2F3E3E),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(text, color = ResultMutedText, style = MaterialTheme.typography.bodyMedium)
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
                color = if (soft) ResultSoftBadgeColor else ResultBadgeColor,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF263B3A),
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
