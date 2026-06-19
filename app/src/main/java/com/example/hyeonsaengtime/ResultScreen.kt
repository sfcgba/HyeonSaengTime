package com.example.hyeonsaengtime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hyeonsaengtime.ui.theme.HyeonSaengAccent
import com.example.hyeonsaengtime.ui.theme.HyeonSaengAccentSoft
import com.example.hyeonsaengtime.ui.theme.HyeonSaengBackground
import com.example.hyeonsaengtime.ui.theme.HyeonSaengBorder
import com.example.hyeonsaengtime.ui.theme.HyeonSaengPrimary
import com.example.hyeonsaengtime.ui.theme.HyeonSaengSurface
import com.example.hyeonsaengtime.ui.theme.HyeonSaengSurfaceSoft
import com.example.hyeonsaengtime.ui.theme.HyeonSaengText
import com.example.hyeonsaengtime.ui.theme.HyeonSaengTextMuted
import java.util.Locale

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
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 430.dp)
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            DailyRecapContent(
                dayResult = dayResult,
                roomResult = roomResult,
                onDismiss = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 430.dp)
            )
        }
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
        modifier = modifier.heightIn(max = 760.dp),
        shape = RoundedCornerShape(32.dp),
        color = HyeonSaengSurface,
        shadowElevation = 14.dp
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RecapHero(dayResult = dayResult, onDismiss = onDismiss)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResultMetricPanel(
                        title = "개인 목표",
                        value = "${HyeonSaengRules.STREAK_REQUIRED_HOURS}:00",
                        badge = if (dayResult.isStreakRequirementMet) "달성" else "실패",
                        badgeTone = if (dayResult.isStreakRequirementMet) {
                            ResultBadgeTone.Accent
                        } else {
                            ResultBadgeTone.Muted
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ResultMetricPanel(
                        title = "방 미션",
                        value = roomOverview?.settlement?.mission?.title ?: "방 없음",
                        badge = when {
                            roomOverview == null -> "대기"
                            roomOverview.settlement.mission.isMet -> "달성"
                            else -> "실패"
                        },
                        badgeTone = when {
                            roomOverview == null -> ResultBadgeTone.Muted
                            roomOverview.settlement.mission.isMet -> ResultBadgeTone.Accent
                            else -> ResultBadgeTone.Muted
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (roomOverview == null) {
                    ResultPlaceholderPanel(
                        title = "방 Lv.",
                        text = "방을 만들면 진행도가 표시돼요"
                    )
                } else {
                    RoomLevelPanel(roomOverview)
                }

                if (roomSummary == null) {
                    ResultPlaceholderPanel(
                        title = "방 내 순위",
                        text = "방을 만들면 순위가 표시돼요"
                    )
                } else {
                    RoomRankPanel(roomSummary)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = HyeonSaengPrimary),
                    shape = RoundedCornerShape(999.dp),
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
private fun RecapHero(
    dayResult: DayResult,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 26.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(34.dp)
                .background(Color.Transparent, CircleShape)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "X",
                color = HyeonSaengTextMuted,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "어제의 현생 시간",
                color = HyeonSaengTextMuted,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = resultTwoDigitHourPart(dayResult.hyeonsaengMillis),
                    color = HyeonSaengPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 64.sp,
                        lineHeight = 68.sp
                    )
                )
                Text(
                    text = "h",
                    color = HyeonSaengTextMuted,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 2.dp, end = 8.dp, bottom = 7.dp)
                )
                Text(
                    text = resultTwoDigitMinutePart(dayResult.hyeonsaengMillis),
                    color = HyeonSaengPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 64.sp,
                        lineHeight = 68.sp
                    )
                )
                Text(
                    text = "m",
                    color = HyeonSaengTextMuted,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 2.dp, bottom = 7.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .background(HyeonSaengAccentSoft, RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_streak_leaf),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (dayResult.isStreakRequirementMet) {
                        "연속 ${dayResult.streakCountAfterUpdate}일"
                    } else {
                        "연속 기록 쉬어감"
                    },
                    color = HyeonSaengText,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun ResultMetricPanel(
    title: String,
    value: String,
    badge: String,
    badgeTone: ResultBadgeTone,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 92.dp)
            .background(HyeonSaengSurfaceSoft, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp)
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
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            ResultBadge(text = badge, tone = badgeTone)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            value,
            color = HyeonSaengText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
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
            .background(HyeonSaengSurfaceSoft, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
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
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "$xpInLevel/${HyeonSaengRules.ROOM_XP_PER_LEVEL}",
                    color = HyeonSaengTextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            ResultBadge(
                text = if (overview.settlement.appliedXpDelta == 0) {
                    "정산 완료"
                } else {
                    "${formatXpDelta(overview.settlement.appliedXpDelta)} XP"
                },
                tone = ResultBadgeTone.Soft
            )
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            color = HyeonSaengPrimary,
            trackColor = HyeonSaengBackground,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
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
            .background(HyeonSaengSurfaceSoft, RoundedCornerShape(20.dp))
            .clickable { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "방 내 순위",
                color = HyeonSaengText,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${summary.myRank}위",
                    color = HyeonSaengPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "/${summary.totalMembers}명",
                    color = HyeonSaengTextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (expanded) "접기" else "보기",
                    color = HyeonSaengTextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(HyeonSaengBorder)
            )
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                summary.comparisons.forEach { comparison ->
                    RoomComparisonRow(comparison)
                }
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
            .background(
                color = if (comparison.member.isMe) HyeonSaengAccentSoft else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            comparison.rank.toString(),
            color = if (comparison.rank <= 3) HyeonSaengPrimary else HyeonSaengTextMuted,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(28.dp)
        )
        Text(
            comparison.member.displayName,
            color = HyeonSaengText,
            fontWeight = if (comparison.member.isMe) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            formatClockHourMinuteDuration(comparison.member.hyeonsaengMillis),
            color = HyeonSaengTextMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(52.dp)
        )
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
            .background(HyeonSaengSurfaceSoft, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            title,
            color = HyeonSaengText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(6.dp))
        Text(text, color = HyeonSaengTextMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ResultBadge(
    text: String,
    tone: ResultBadgeTone = ResultBadgeTone.Accent
) {
    val background = when (tone) {
        ResultBadgeTone.Accent -> HyeonSaengAccent
        ResultBadgeTone.Soft -> HyeonSaengAccentSoft
        ResultBadgeTone.Muted -> HyeonSaengBackground
    }
    val textColor = when (tone) {
        ResultBadgeTone.Muted -> HyeonSaengTextMuted
        else -> HyeonSaengText
    }

    Box(
        modifier = Modifier
            .background(
                color = background,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private enum class ResultBadgeTone {
    Accent,
    Soft,
    Muted
}

private fun formatXpDelta(value: Int): String {
    return if (value > 0) "+$value" else value.toString()
}

private fun resultTwoDigitHourPart(millis: Long): String {
    val hours = millis.coerceAtLeast(0L) / 1000L / 3600L
    return String.format(Locale.US, "%02d", hours)
}

private fun resultTwoDigitMinutePart(millis: Long): String {
    val minutes = (millis.coerceAtLeast(0L) / 1000L % 3600L) / 60L
    return String.format(Locale.US, "%02d", minutes)
}
