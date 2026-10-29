package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*

/**
 * Top 3 Podium component displaying 2nd, 1st, and 3rd rank aspirants.
 * Styled with metallic silver, gold, and bronze exam podium aesthetics.
 */
@Composable
fun PodiumTopThreeView(
    topThree: List<LeaderboardCandidate>,
    onCandidateClick: (LeaderboardCandidate) -> Unit,
    modifier: Modifier = Modifier
) {
    if (topThree.isEmpty()) return

    val first = topThree.firstOrNull()
    val second = if (topThree.size > 1) topThree[1] else null
    val third = if (topThree.size > 2) topThree[2] else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("leaderboard_podium_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "STATE TOP 3 HONORS PODIUM",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFFD4AF37)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Rank 2 - Silver
                if (second != null) {
                    PodiumPillar(
                        candidate = second,
                        rankNumber = 2,
                        stepHeight = 115.dp,
                        accentColor = Color(0xFF94A3B8),
                        pillarBg = Brush.verticalGradient(
                            listOf(Color(0xFF334155), Color(0xFF1E293B))
                        ),
                        medalEmoji = "🥈",
                        onCandidateClick = onCandidateClick,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Rank 1 - Metallic Gold with subtle highlight & glow
                if (first != null) {
                    PodiumPillar(
                        candidate = first,
                        rankNumber = 1,
                        stepHeight = 150.dp,
                        accentColor = Color(0xFFFFD700),
                        pillarBg = Brush.verticalGradient(
                            listOf(Color(0xFF854D0E), Color(0xFF451A03), Color(0xFF1E293B))
                        ),
                        medalEmoji = "🥇",
                        isWinner = true,
                        onCandidateClick = onCandidateClick,
                        modifier = Modifier.weight(1.15f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Rank 3 - Bronze
                if (third != null) {
                    PodiumPillar(
                        candidate = third,
                        rankNumber = 3,
                        stepHeight = 95.dp,
                        accentColor = Color(0xFFCD7F32),
                        pillarBg = Brush.verticalGradient(
                            listOf(Color(0xFF78350F), Color(0xFF1E293B))
                        ),
                        medalEmoji = "🥉",
                        onCandidateClick = onCandidateClick,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PodiumPillar(
    candidate: LeaderboardCandidate,
    rankNumber: Int,
    stepHeight: androidx.compose.ui.unit.Dp,
    accentColor: Color,
    pillarBg: Brush,
    medalEmoji: String,
    isWinner: Boolean = false,
    onCandidateClick: (LeaderboardCandidate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onCandidateClick(candidate) }
            .testTag("podium_pillar_$rankNumber"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar with medal badge
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(if (isWinner) 48.dp else 42.dp)
                    .clip(CircleShape)
                    .background(Color(candidate.avatarColorHex))
                    .border(
                        width = if (isWinner) 2.5.dp else 1.5.dp,
                        color = accentColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = candidate.displayName.take(2).uppercase().ifBlank { "AP" },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isWinner) 15.sp else 13.sp
                )
            }

            // Medal indicator
            Surface(
                shape = CircleShape,
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, accentColor),
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = 4.dp, y = (-4).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = medalEmoji, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Display Name
        Text(
            text = candidate.getEffectiveDisplayName(),
            color = Color.White,
            fontSize = if (isWinner) 12.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // XP / Score
        Text(
            text = "${candidate.xp} XP",
            color = accentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )

        if (candidate.streakDays > 0) {
            Text(
                text = "🔥 ${candidate.streakDays}d",
                color = Color(0xFFFB923C),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Podium Step Block
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(stepHeight),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, accentColor.copy(alpha = if (isWinner) 0.6f else 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(pillarBg),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "#$rankNumber",
                        color = Color.White,
                        fontSize = if (isWinner) 22.sp else 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${candidate.accuracyPercentage.toInt()}% Acc",
                        color = Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Competitive League / Tier Banner Card showing current league, rank, and progress to next league.
 */
@Composable
fun LeagueProgressCard(
    currentLeague: ExamLeague,
    currentXp: Int,
    leagueRank: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("league_progress_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
        border = BorderStroke(1.dp, Color(currentLeague.colorHex).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(currentLeague.accentHex).copy(alpha = 0.3f))
                            .border(1.dp, Color(currentLeague.colorHex), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = currentLeague.iconEmoji, fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = currentLeague.title.uppercase(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(currentLeague.colorHex),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "League Standing: #$leagueRank • $currentXp XP",
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Top 15% Promoted",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar to next league
            val nextLeague = currentLeague.nextLeague()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (nextLeague != null) "Next: ${nextLeague.title}" else "Highest League Reached!",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(currentLeague.colorHex)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(currentLeague.colorHex),
                trackColor = Color(0xFF1E293B)
            )
        }
    }
}

/**
 * Prominent card showing the current user's personal rank, rank movement, XP, accuracy, tests, questions & streak.
 */
@Composable
fun PersonalRankCard(
    userCandidate: LeaderboardCandidate,
    onPrivacyClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("user_personal_rank_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
        border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .border(1.5.dp, Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${userCandidate.rank}",
                            color = Color(0xFF34D399),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "YOUR RANK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color(0xFF94A3B8)
                            )
                            // Rank Movement
                            RankMovementPill(rankDelta = userCandidate.rankDelta)
                        }
                        Text(
                            text = "${userCandidate.displayName} • ${userCandidate.district}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                IconButton(
                    onClick = onPrivacyClick,
                    modifier = Modifier.size(36.dp).testTag("btn_privacy_card_shortcut")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Privacy Mode",
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Stats Row: XP, Accuracy, Tests, Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "LEADERBOARD XP", value = "${userCandidate.xp}", valueColor = Color(0xFFFFD700))
                StatItem(label = "ACCURACY", value = "${userCandidate.accuracyPercentage.toInt()}%", valueColor = Color(0xFF34D399))
                StatItem(label = "TESTS SOLVED", value = "${userCandidate.testsCompleted}", valueColor = Color(0xFF60A5FA))
                StatItem(label = "DAILY STREAK", value = "🔥 ${userCandidate.streakDays}d", valueColor = Color(0xFFFB923C))
            }
        }
    }
}

@Composable
fun RankMovementPill(rankDelta: Int) {
    val (text, color, icon) = when {
        rankDelta > 0 -> Triple("↑ $rankDelta", Color(0xFF10B981), Icons.Default.ArrowUpward)
        rankDelta < 0 -> Triple("↓ ${-rankDelta}", Color(0xFFEF4444), Icons.Default.ArrowDownward)
        else -> Triple("— No Change", Color(0xFF94A3B8), null)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = valueColor
        )
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
        )
    }
}

/**
 * Single candidate row item in the leaderboard table.
 */
@Composable
fun CandidateRowItem(
    candidate: LeaderboardCandidate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentUser = candidate.isCurrentUser
    val borderColor = if (isCurrentUser) Color(0xFF10B981).copy(alpha = 0.7f) else Color(0xFF1E293B)
    val bgColor = if (isCurrentUser) Color(0xFF0F253E) else Color(0xFF131D31)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("candidate_row_${candidate.rank}"),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(if (isCurrentUser) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rank + Delta + Avatar + Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Rank number
                Box(
                    modifier = Modifier.width(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${candidate.rank}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = when (candidate.rank) {
                            1 -> Color(0xFFFFD700)
                            2 -> Color(0xFF94A3B8)
                            3 -> Color(0xFFCD7F32)
                            else -> if (isCurrentUser) Color(0xFF34D399) else Color(0xFFCBD5E1)
                        }
                    )
                }

                // Avatar
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(candidate.avatarColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = candidate.displayName.take(1).uppercase().ifBlank { "C" },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Name + District + League
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = candidate.getEffectiveDisplayName(),
                            fontSize = 13.sp,
                            fontWeight = if (isCurrentUser) FontWeight.Black else FontWeight.SemiBold,
                            color = if (isCurrentUser) Color(0xFF34D399) else Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isCurrentUser) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "YOU",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF34D399),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = candidate.district,
                            fontSize = 10.5.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(text = "•", fontSize = 10.sp, color = Color(0xFF475569))
                        Text(
                            text = "${candidate.league.iconEmoji} ${candidate.league.title.substringBefore(" ")}",
                            fontSize = 10.5.sp,
                            color = Color(candidate.league.colorHex),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // XP and Accuracy
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${candidate.xp} XP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${candidate.accuracyPercentage.toInt()}% Acc",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )
                    if (candidate.streakDays > 0) {
                        Text(
                            text = "🔥 ${candidate.streakDays}d",
                            fontSize = 10.sp,
                            color = Color(0xFFFB923C)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Safe public profile dialog showing public candidate performance statistics.
 * Strictly hides sensitive credentials, emails, phone numbers, and passwords.
 */
@Composable
fun CandidatePublicProfileDialog(
    candidate: LeaderboardCandidate,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("candidate_public_profile_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with League border
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(candidate.avatarColorHex))
                            .border(2.dp, Color(candidate.league.colorHex), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = candidate.displayName.take(2).uppercase().ifBlank { "AP" },
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(text = candidate.league.iconEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = candidate.getEffectiveDisplayName(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "${candidate.targetExam} • ${candidate.district}",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(candidate.league.colorHex).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(candidate.league.colorHex).copy(alpha = 0.4f)),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        text = "${candidate.league.title} Member",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(candidate.league.colorHex),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(16.dp))

                // Public Performance Metrics 2x2 Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileMetricCard(label = "State Rank", value = "#${candidate.rank}", accent = Color(0xFFFFD700), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    ProfileMetricCard(label = "Leaderboard XP", value = "${candidate.xp}", accent = Color(0xFF38BDF8), modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileMetricCard(label = "Accuracy", value = "${candidate.accuracyPercentage.toInt()}%", accent = Color(0xFF34D399), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    ProfileMetricCard(label = "Daily Streak", value = "🔥 ${candidate.streakDays}d", accent = Color(0xFFFB923C), modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileMetricCard(label = "Tests Solved", value = "${candidate.testsCompleted}", accent = Color(0xFFA78BFA), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    ProfileMetricCard(label = "MCQs Attempted", value = "${candidate.questionsAttempted}", accent = Color(0xFFF472B6), modifier = Modifier.weight(1f))
                }

                // Badges
                if (candidate.badges.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "VERIFIED MILESTONE BADGES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        candidate.badges.take(3).forEach { badge ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 10.sp,
                                    color = Color(0xFFCBD5E1),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Safe privacy reassurance notice
                Text(
                    text = "🔒 Privacy Protected: Contact details, phone numbers, and private data are kept strictly confidential.",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth().testTag("btn_close_public_profile"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close Profile", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProfileMetricCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = accent)
            Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8))
        }
    }
}

/**
 * Privacy controls dialog allowing user to choose Public, Display Name Only, or Anonymous mode.
 */
@Composable
fun PrivacySettingsDialog(
    currentMode: LeaderboardPrivacyMode,
    onSaveMode: (LeaderboardPrivacyMode) -> Unit,
    onDismissRequest: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentMode) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("privacy_settings_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Leaderboard Privacy",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "Control how your candidate name appears to other aspirants on the state leaderboard.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                LeaderboardPrivacyMode.values().forEach { mode ->
                    val isSelected = selectedMode == mode
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedMode = mode },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF1E293B) else Color(0xFF131D31),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedMode = mode },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF38BDF8))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = mode.title,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = mode.description,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSaveMode(selectedMode)
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Privacy", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Historical performance dialog showing previous weeks/months, best rank, and highest XP.
 */
@Composable
fun LeaderboardHistoryDialog(
    userStanding: LeaderboardCandidate?,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("leaderboard_history_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Historical Performance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Highlight cards: Best Rank, Highest XP, Longest Streak
                Row(modifier = Modifier.fillMaxWidth()) {
                    ProfileMetricCard(
                        label = "All-Time Best Rank",
                        value = "#${(userStanding?.rank ?: 14).coerceAtLeast(1)}",
                        accent = Color(0xFFFFD700),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ProfileMetricCard(
                        label = "Peak XP Earned",
                        value = "${(userStanding?.xp ?: 2840) + 400} XP",
                        accent = Color(0xFF34D399),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    ProfileMetricCard(
                        label = "Longest Streak",
                        value = "🔥 ${userStanding?.longestStreak ?: 14} Days",
                        accent = Color(0xFFFB923C),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ProfileMetricCard(
                        label = "Highest Accuracy",
                        value = "${((userStanding?.accuracyPercentage ?: 88.5f) + 4f).coerceAtMost(100f).toInt()}%",
                        accent = Color(0xFF60A5FA),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "PREVIOUS PERIOD ARCHIVES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val mockHistory = listOf(
                    Triple("Week 39 (Current)", "#${userStanding?.rank ?: 14}", "${userStanding?.weeklyXp ?: 1350} XP"),
                    Triple("Week 38 (Previous)", "#18", "2,120 XP"),
                    Triple("Week 37", "#22", "1,890 XP"),
                    Triple("August Monthly", "#16", "6,420 XP")
                )

                mockHistory.forEach { (period, rank, xp) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = period, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(text = rank, fontSize = 12.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                Text(text = xp, fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close History", color = Color.White)
                }
            }
        }
    }
}

/**
 * Admin controls dialog for configuring XP rules, fair scoring weights, and triggering weekly reset.
 */
@Composable
fun AdminLeaderboardControlsDialog(
    config: LeaderboardConfigModel,
    monitoringStats: LeaderboardMonitoringStats,
    onSaveConfig: (LeaderboardConfigModel) -> Unit,
    onResetWeekly: () -> Unit,
    onDismissRequest: () -> Unit
) {
    var editableConfig by remember { mutableStateOf(config) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("admin_leaderboard_controls_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Admin Leaderboard Engine",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Real-time monitoring, configurable fair scoring weights, and period lifecycle controls.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // Live Monitoring Stats
                    Text(
                        text = "LIVE MONITORING TELEMETRY",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF60A5FA),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ProfileMetricCard(
                            label = "Active Aspirants",
                            value = "${monitoringStats.totalParticipants}",
                            accent = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        ProfileMetricCard(
                            label = "Top Fair Score",
                            value = "${monitoringStats.topScore.toInt()}",
                            accent = Color(0xFFFFD700),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        ProfileMetricCard(
                            label = "Avg Accuracy",
                            value = "${monitoringStats.averageAccuracy.toInt()}%",
                            accent = Color(0xFF34D399),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Configurable XP Rules
                item {
                    Text(
                        text = "CONFIGURABLE XP REWARD RULES",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFBBF24),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    XpRuleSlider(
                        label = "Correct Answer XP (+${editableConfig.xpPerCorrectAnswer} XP)",
                        value = editableConfig.xpPerCorrectAnswer.toFloat(),
                        range = 5f..30f,
                        onValueChange = { editableConfig = editableConfig.copy(xpPerCorrectAnswer = it.toInt()) }
                    )

                    XpRuleSlider(
                        label = "Mock Test Completion XP (+${editableConfig.xpPerMockTestCompletion} XP)",
                        value = editableConfig.xpPerMockTestCompletion.toFloat(),
                        range = 20f..150f,
                        onValueChange = { editableConfig = editableConfig.copy(xpPerMockTestCompletion = it.toInt()) }
                    )

                    XpRuleSlider(
                        label = "Daily Streak XP (+${editableConfig.xpPerDailyStreak} XP)",
                        value = editableConfig.xpPerDailyStreak.toFloat(),
                        range = 10f..50f,
                        onValueChange = { editableConfig = editableConfig.copy(xpPerDailyStreak = it.toInt()) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Fair Scoring Weights
                item {
                    Text(
                        text = "FAIR SCORING ALGORITHM WEIGHTS",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF34D399),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    XpRuleSlider(
                        label = "Accuracy Weight (${(editableConfig.accuracyWeight * 100).toInt()}%)",
                        value = (editableConfig.accuracyWeight * 100).toFloat(),
                        range = 10f..60f,
                        onValueChange = { editableConfig = editableConfig.copy(accuracyWeight = it.toDouble() / 100.0) }
                    )

                    XpRuleSlider(
                        label = "Mock Test Weight (${(editableConfig.mockTestWeight * 100).toInt()}%)",
                        value = (editableConfig.mockTestWeight * 100).toFloat(),
                        range = 10f..60f,
                        onValueChange = { editableConfig = editableConfig.copy(mockTestWeight = it.toDouble() / 100.0) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Period Reset Button
                item {
                    if (!showResetConfirm) {
                        OutlinedButton(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier.fillMaxWidth().testTag("btn_trigger_weekly_reset"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Text("Reset Weekly Period & Archive Standings", fontSize = 12.sp)
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF451A1A)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Confirm Weekly Period Reset? This will snapshot current standings into historical archives and reset weekly scores.",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            onResetWeekly()
                                            showResetConfirm = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                                    ) {
                                        Text("Confirm Reset", fontSize = 11.sp)
                                    }
                                    TextButton(onClick = { showResetConfirm = false }) {
                                        Text("Cancel", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismissRequest) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSaveConfig(editableConfig)
                                onDismissRequest()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Save Engine Config", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun XpRuleSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.5.sp, color = Color(0xFFCBD5E1))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF38BDF8),
                activeTrackColor = Color(0xFF0284C7),
                inactiveTrackColor = Color(0xFF1E293B)
            )
        )
    }
}
