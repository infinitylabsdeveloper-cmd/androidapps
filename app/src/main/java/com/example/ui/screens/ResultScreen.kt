package com.example.ui.screens

import com.example.ui.components.MathAndHighlightText
import com.example.ui.components.premiumHeroExpand
import com.example.ui.components.premiumPress
import com.example.ui.components.premiumTargetAnimation
import com.example.model.HighlightLocation
import com.example.ui.theme.LocalAppConfig

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Question
import kotlin.math.*


private val colorBackground = Color(0xFFFAF7F2)
private val colorQuestionCard = Color(0xFFFFFFFF)
private val colorQuestionText = Color(0xFF292524)
private val colorOptionBackground = Color(0xFFF0F4FA)
private val colorOptionText = Color(0xFF334155)
private val colorExplanationBackground = Color(0xFFFFF3E8)
private val colorExplanationText = Color(0xFF633F2D)
private val colorPremium = Color(0xFFD4AF37)
private val colorSecondaryText = Color(0xFF64748B)

private fun parseHexColor(hex: String, defaultColor: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
    } catch (e: Exception) {
        defaultColor
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    questions: List<Question>,
    userAnswers: Map<String, String>,
    correctCount: Int,
    wrongCount: Int,
    score: Double,
    initialBookmarkedIds: Set<String> = emptySet(),
    onToggleBookmark: (Question) -> Unit = {},
    onPracticeAgain: () -> Unit,
    onBackToPrevious: () -> Unit = {},
    onBackToHome: () -> Unit,
    onNextSet: (() -> Unit)? = null,
    nextItemTitle: String? = null,
    isSubtopicPractice: Boolean = false,
    hasNextItem: Boolean = true
) {
    BackHandler { onBackToPrevious() }
    val context = LocalContext.current
    var bookmarkedIds by remember(initialBookmarkedIds) { mutableStateOf(initialBookmarkedIds) }
    val totalQs = questions.size.coerceAtLeast(1)
    val safeCorrectCount = correctCount.coerceIn(0, totalQs)
    val attempted = (safeCorrectCount + wrongCount).coerceIn(0, totalQs)
    val unattempted = (totalQs - attempted).coerceAtLeast(0)
    val accuracy = if (attempted > 0) ((safeCorrectCount.toDouble() / attempted) * 100).roundToInt().coerceIn(0, 100) else 0
    val scorePercentage = if (totalQs > 0) ((safeCorrectCount.toDouble() * 100.0) / totalQs).roundToInt().coerceIn(0, 100) else 0

    val suggestionConfig = remember(scorePercentage) {
        getAiSuggestionConfig(scorePercentage)
    }

    var selectedFilter by remember { mutableStateOf("All") }

    val questionIndexMap = remember(questions) {
        questions.mapIndexed { idx, q -> q.id to idx }.toMap()
    }

    val filteredQs = remember(questions, userAnswers, selectedFilter, questionIndexMap) {
        questions.filterIndexed { idx, q ->
            val ans = userAnswers[q.id]
                ?: userAnswers[q.questionText]
                ?: userAnswers[idx.toString()]
            when (selectedFilter) {
                "Mistakes" -> ans != null && !ans.equals(q.correctOption, ignoreCase = true)
                "Skipped" -> ans == null
                else -> true
            }
        }
    }

    val appConfig = LocalAppConfig.current
    val pa = appConfig.advancedFontSettings.practiceAppearance
    val isDark = isSystemInDarkTheme()
    val screenBg = colorBackground

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg),
        contentAlignment = Alignment.TopCenter
    ) {
        val isTablet = maxWidth >= 600.dp

        // Champagne/Peach Hero Background smoothly fading into screenBg without a hard horizontal seam
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFEFE6), // Soft peach cream
                            Color(0xFFFFF8F0), // Warm ivory transition
                            screenBg           // Seamless blend into screen background
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxHeight()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Navigation Action buttons: Back (Return to Sets) + Direct Home Shortcut
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Back button: Return to previous Sets/Subtopics screen
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            shadowElevation = 2.dp,
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .premiumPress(target = com.example.model.AnimatableTarget.BUTTONS_CTAS) {
                                    onBackToPrevious()
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Return to Sets / Subtopics",
                                    tint = colorQuestionText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Back",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = colorQuestionText
                                )
                            }
                        }

                        // 2. Direct Home Shortcut
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colorQuestionCard,
                            shadowElevation = 2.dp,
                            border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .premiumPress(target = com.example.model.AnimatableTarget.BUTTONS_CTAS) {
                                    onBackToHome()
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Go to Home",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Home",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = colorQuestionText
                                )
                            }
                        }
                    }

                    // Set Pill & Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Set Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colorQuestionCard.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = colorSecondaryText
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val setName = questions.firstOrNull()?.setName?.takeIf { it.isNotBlank() } ?: "Set 1"
                                Text(
                                    text = setName,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorQuestionText
                                )
                            }
                        }

                        // Share Button
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable {
                                    try {
                                        val sendIntent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            putExtra(
                                                android.content.Intent.EXTRA_TEXT,
                                                "I scored $scorePercentage% in practice! Correct: $correctCount, Wrong: $wrongCount."
                                            )
                                            type = "text/plain"
                                        }
                                        context.startActivity(
                                            android.content.Intent.createChooser(sendIntent, "Share Result")
                                        )
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Score: $scorePercentage%", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color(0xFF4338CA),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Hero Performance Card
            item {
                val isHighAchiever = scorePercentage >= 70
                val cardScale = remember { Animatable(if (isHighAchiever) 0.92f else 1.0f) }

                LaunchedEffect(isHighAchiever) {
                    if (isHighAchiever) {
                        cardScale.animateTo(
                            targetValue = 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                }

                val heroBorder = if (isHighAchiever) {
                    BorderStroke(
                        2.dp,
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF10B981),
                                Color(0xFFF59E0B),
                                Color(0xFF38BDF8)
                            )
                        )
                    )
                } else {
                    BorderStroke(1.dp, Color(0xFFE2E8F0))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .scale(cardScale.value)
                        .premiumHeroExpand(
                            target = com.example.model.AnimatableTarget.RESULT_SUMMARY_SCORE,
                            isExpanded = true,
                            index = 0
                        ),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorQuestionCard
                    ),
                    border = BorderStroke(1.dp, colorPremium),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isHighAchiever) 6.dp else 2.dp)
                ) {
                    if (isTablet) {
                        // Tablet: Spacious horizontal row layout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Donut Chart on Left
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(136.dp)
                            ) {
                                Canvas(modifier = Modifier.size(118.dp)) {
                                    drawArc(
                                        color = Color(0xFFF1F5F9),
                                        startAngle = 0f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        brush = Brush.sweepGradient(suggestionConfig.donutColors),
                                        startAngle = -90f,
                                        sweepAngle = if (scorePercentage > 0) 360f * (scorePercentage / 100f) else 12f,
                                        useCenter = false,
                                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    com.example.ui.components.AnimatedScoreCounter(
                                        targetValue = scorePercentage,
                                        suffix = "%",
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = suggestionConfig.iconBg,
                                        modifier = Modifier.offset(y = 2.dp)
                                    ) {
                                        Text(
                                            text = suggestionConfig.badgeText,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            // Details on Right
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = suggestionConfig.heroTitle,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "You answered $correctCount out of $totalQs questions correctly",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF475569)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEFF6FF)) {
                                        Text(
                                            text = "Accuracy: $accuracy%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2563EB),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF0FDF4)) {
                                        Text(
                                            text = "Score: ${score.toInt()}/$totalQs",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Mobile: Centered, clean layout
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Donut Chart
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(126.dp)
                            ) {
                                Canvas(modifier = Modifier.size(112.dp)) {
                                    drawArc(
                                        color = Color(0xFFF1F5F9),
                                        startAngle = 0f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 13.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        brush = Brush.sweepGradient(suggestionConfig.donutColors),
                                        startAngle = -90f,
                                        sweepAngle = if (scorePercentage > 0) 360f * (scorePercentage / 100f) else 12f,
                                        useCenter = false,
                                        style = Stroke(width = 13.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    com.example.ui.components.AnimatedScoreCounter(
                                        targetValue = scorePercentage,
                                        suffix = "%",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = suggestionConfig.iconBg,
                                        modifier = Modifier.offset(y = 2.dp)
                                    ) {
                                        Text(
                                            text = suggestionConfig.badgeText,
                                            color = Color.White,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                                        )
                                    }
                                }
                            }

                            // Summary Text
                            Text(
                                text = "You answered $correctCount out of $totalQs questions correctly",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                textAlign = TextAlign.Center
                            )

                            // Status Pills
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEFF6FF)
                                ) {
                                    Text(
                                        text = "Accuracy: $accuracy%",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2563EB),
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF0FDF4)
                                ) {
                                    Text(
                                        text = "Score: ${score.toInt()}/$totalQs",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF16A34A),
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Next Target Milestone
            item {
                val nextTargetPercentage = when {
                    scorePercentage >= 100 -> 100
                    scorePercentage >= 90 -> 100
                    scorePercentage >= 70 -> 90
                    else -> 70
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = if (scorePercentage >= 100) Color(0xFFFEF3C7) else Color(0xFFF0F9FF),
                    border = BorderStroke(1.dp, if (scorePercentage >= 100) Color(0xFFFDE68A) else Color(0xFFBAE6FD))
                ) {
                    if (isTablet) {
                        // Tablet: single row
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (scorePercentage >= 100) Color(0xFFD97706) else Color(0xFF3B82F6),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (scorePercentage >= 100) Icons.Default.EmojiEvents else Icons.Default.AdsClick,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (scorePercentage >= 100) "Milestone Reached! 🏆" else "Next Target Milestone",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (scorePercentage >= 100) Color(0xFF92400E) else Color(0xFF1E3A8A)
                                )
                                Text(
                                    text = if (scorePercentage >= 100) "100% Perfect!" else "$nextTargetPercentage%+",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.2f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .height(8.dp)
                                            .weight(1f)
                                            .background(Color(0xFFBFDBFE), RoundedCornerShape(4.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth((scorePercentage / 100f).coerceIn(0f, 1f))
                                                .background(
                                                    if (scorePercentage >= 100) Color(0xFFD97706) else Color(0xFF3B82F6),
                                                    RoundedCornerShape(4.dp)
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$scorePercentage%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    } else {
                        // Mobile: 2-row layout without horizontal squishing
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (scorePercentage >= 100) Color(0xFFD97706) else Color(0xFF3B82F6),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (scorePercentage >= 100) Icons.Default.EmojiEvents else Icons.Default.AdsClick,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (scorePercentage >= 100) "Milestone Reached! 🏆" else "Next Target Milestone",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (scorePercentage >= 100) Color(0xFF92400E) else Color(0xFF1E3A8A)
                                        )
                                        Text(
                                            text = if (scorePercentage >= 100) "100% Perfect!" else "$nextTargetPercentage%+",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (scorePercentage >= 100) Color(0xFFDCFCE7) else Color(0xFFFEF08A)
                                ) {
                                    Text(
                                        text = if (scorePercentage >= 100) "Top Rank!" else "Target: $nextTargetPercentage%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (scorePercentage >= 100) Color(0xFF15803D) else Color(0xFF713F12),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Progress bar
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .height(8.dp)
                                        .weight(1f)
                                        .background(Color(0xFFBFDBFE), RoundedCornerShape(4.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth((scorePercentage / 100f).coerceIn(0f, 1f))
                                            .background(
                                                if (scorePercentage >= 100) Color(0xFFD97706) else Color(0xFF3B82F6),
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$scorePercentage%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }
            }

            // Stat Cards Grid
            item {
                val nextTargetVal = when {
                    scorePercentage >= 100 -> "100% 🏆"
                    scorePercentage >= 90 -> "100%"
                    scorePercentage >= 70 -> "90%+"
                    else -> "70%+"
                }
                val wrongSub = if (wrongCount == 0) "0 Mistakes! 🎯" else "To revise"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isTablet) {
                        // Tablet: 4 cards per row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.CheckCircle, iconTint = Color(0xFF16A34A), iconBg = Color(0xFFDCFCE7), title = "Correct", value = "$correctCount", subtitle = "+1 Mark each", bg = Color(0xFFF0FDF4), border = Color(0xFFBBF7D0))
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Cancel, iconTint = Color(0xFFDC2626), iconBg = Color(0xFFFFE4E6), title = "Wrong", value = "$wrongCount", subtitle = wrongSub, bg = Color(0xFFFFF1F2), border = Color(0xFFFECDD3))
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.GpsFixed, iconTint = Color(0xFF2563EB), iconBg = Color(0xFFDBEAFE), title = "Accuracy", value = "$accuracy%", subtitle = "Accuracy rate", bg = Color(0xFFEFF6FF), border = Color(0xFFBFDBFE))
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.FormatListBulleted, iconTint = Color(0xFF7C3AED), iconBg = Color(0xFFEDE9FE), title = "Total Questions", value = "$totalQs", subtitle = "In this session", bg = Color(0xFFFAF5FF), border = Color(0xFFE9D5FF))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.AccessTimeFilled, iconTint = Color(0xFFD97706), iconBg = Color(0xFFFEF3C7), title = "Time Taken", value = "00:16", subtitle = "Pace & Speed", bg = Color(0xFFFFFBEB), border = Color(0xFFFDE68A))
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.InsertChart, iconTint = Color(0xFF0D9488), iconBg = Color(0xFFCCFBF1), title = "Attempted", value = "$attempted / $totalQs", subtitle = "Questions", bg = Color(0xFFF0FDFA), border = Color(0xFF99F6E4))
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.SkipNext, iconTint = Color(0xFFDB2777), iconBg = Color(0xFFFCE7F3), title = "Unattempted", value = "$unattempted", subtitle = "Skipped", bg = Color(0xFFFDF2F8), border = Color(0xFFFBCFE8))
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.TrackChanges, iconTint = Color(0xFFEA580C), iconBg = Color(0xFFFFEDD5), title = "Next Target", value = nextTargetVal, subtitle = if (scorePercentage >= 100) "Mastered" else "Keep Practicing", bg = Color(0xFFFFF7ED), border = Color(0xFFFED7AA))
                        }
                    } else {
                        // Mobile: 2 cards per row so all text & numbers fit without truncation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.CheckCircle, iconTint = Color(0xFF16A34A), iconBg = Color(0xFFDCFCE7), title = "Correct", value = "$correctCount", subtitle = "+1 Mark each", bg = Color(0xFFF0FDF4), border = Color(0xFFBBF7D0))
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Cancel, iconTint = Color(0xFFDC2626), iconBg = Color(0xFFFFE4E6), title = "Wrong", value = "$wrongCount", subtitle = wrongSub, bg = Color(0xFFFFF1F2), border = Color(0xFFFECDD3))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.GpsFixed, iconTint = Color(0xFF2563EB), iconBg = Color(0xFFDBEAFE), title = "Accuracy", value = "$accuracy%", subtitle = "Accuracy rate", bg = Color(0xFFEFF6FF), border = Color(0xFFBFDBFE))
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.FormatListBulleted, iconTint = Color(0xFF7C3AED), iconBg = Color(0xFFEDE9FE), title = "Total Questions", value = "$totalQs", subtitle = "In this session", bg = Color(0xFFFAF5FF), border = Color(0xFFE9D5FF))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.AccessTimeFilled, iconTint = Color(0xFFD97706), iconBg = Color(0xFFFEF3C7), title = "Time Taken", value = "00:16", subtitle = "Pace & Speed", bg = Color(0xFFFFFBEB), border = Color(0xFFFDE68A))
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.InsertChart, iconTint = Color(0xFF0D9488), iconBg = Color(0xFFCCFBF1), title = "Attempted", value = "$attempted / $totalQs", subtitle = "Questions", bg = Color(0xFFF0FDFA), border = Color(0xFF99F6E4))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.SkipNext, iconTint = Color(0xFFDB2777), iconBg = Color(0xFFFCE7F3), title = "Unattempted", value = "$unattempted", subtitle = "Skipped", bg = Color(0xFFFDF2F8), border = Color(0xFFFBCFE8))
                            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.TrackChanges, iconTint = Color(0xFFEA580C), iconBg = Color(0xFFFFEDD5), title = "Next Target", value = nextTargetVal, subtitle = if (scorePercentage >= 100) "Mastered" else "Keep Practicing", bg = Color(0xFFFFF7ED), border = Color(0xFFFED7AA))
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPracticeAgain,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A00))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Practice Again",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    if (onNextSet != null || !hasNextItem) {
                        if (hasNextItem && onNextSet != null) {
                            val buttonText = if (isSubtopicPractice) {
                                if (!nextItemTitle.isNullOrBlank()) "Next Subtopic: $nextItemTitle" else "Next Subtopic"
                            } else {
                                if (!nextItemTitle.isNullOrBlank()) "Next Set: $nextItemTitle" else "Next Set"
                            }
                            val buttonColor = if (isSubtopicPractice) Color(0xFF2563EB) else Color(0xFF3B82F6)

                            Button(
                                onClick = { onNextSet() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                            ) {
                                Icon(
                                    imageVector = if (isSubtopicPractice) Icons.Default.Category else Icons.Default.RocketLaunch,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSubtopicPractice) "Next Subtopic" else "Next Set",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            // End of Content - No next Set or Subtopic exists
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable { onBackToPrevious() }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "End",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp,
                                        color = Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Detailed Solutions Header & Filters
            item {
                if (isTablet) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF4F46E5),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Detailed Solutions & Concept Review",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterPill(label = "All ($totalQs)", selected = selectedFilter == "All", onClick = { selectedFilter = "All" }, color = Color(0xFF3B82F6))
                            FilterPill(label = "Mistakes ($wrongCount)", selected = selectedFilter == "Mistakes", onClick = { selectedFilter = "Mistakes" }, color = Color(0xFFE11D48))
                            FilterPill(label = "Skipped ($unattempted)", selected = selectedFilter == "Skipped", onClick = { selectedFilter = "Skipped" }, color = Color(0xFF475569))
                        }
                    }
                } else {
                    // Mobile: Title row + filter pills row below
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF4F46E5),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Detailed Solutions & Concept Review",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterPill(label = "All ($totalQs)", selected = selectedFilter == "All", onClick = { selectedFilter = "All" }, color = Color(0xFF3B82F6))
                            FilterPill(label = "Mistakes ($wrongCount)", selected = selectedFilter == "Mistakes", onClick = { selectedFilter = "Mistakes" }, color = Color(0xFFE11D48))
                            FilterPill(label = "Skipped ($unattempted)", selected = selectedFilter == "Skipped", onClick = { selectedFilter = "Skipped" }, color = Color(0xFF475569))
                        }
                    }
                }
            }

            // Questions List
            itemsIndexed(
                items = filteredQs,
                key = { index, question -> "${question.id}_$index" }
            ) { index, question ->
                val origIdx = questionIndexMap[question.id] ?: index
                val isBm = bookmarkedIds.contains(question.id)
                val selectedOpt = userAnswers[question.id]
                    ?: userAnswers[question.questionText]
                    ?: userAnswers[origIdx.toString()]
                    ?: userAnswers[index.toString()]

                QuestionSolutionCard(
                    question = question,
                    index = origIdx + 1,
                    selectedOption = selectedOpt,
                    isBookmarked = isBm,
                    onToggleBookmark = { q ->
                        onToggleBookmark(q)
                        bookmarkedIds = if (isBm) bookmarkedIds - q.id else bookmarkedIds + q.id
                    }
                )
            }
        }

        // Premium Achievement Celebration Overlay on Result Screen (Sets, Subtopics, Current Affairs)
        // Strictly displayed ONLY when score is 70% or higher (>= 70.0)
        if (scorePercentage >= 70) {
            com.example.ui.components.PremiumAchievementResultOverlay(
                scorePercentage = scorePercentage.toDouble(),
                minThreshold = 70.0,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Color(0xFFC76B91) else Color(0xFFFFF1E8),
        border = BorderStroke(1.dp, if (selected) Color(0xFFC76B91) else Color(0xFFF2D3DC)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else Color(0xFF786575),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun StatCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color, iconBg: Color, title: String, value: String, subtitle: String, bg: Color, border: Color) {
    Surface(
        modifier = modifier.wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Surface(shape = CircleShape, color = iconBg, modifier = Modifier.size(26.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = iconTint, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF49333F), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, fontSize = 8.sp, color = Color(0xFF786575), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun QuestionSolutionCard(
    question: Question,
    index: Int,
    selectedOption: String?,
    isBookmarked: Boolean = false,
    onToggleBookmark: (Question) -> Unit = {}
) {
    val context = LocalContext.current
    val isCorrect = selectedOption != null && selectedOption.equals(question.correctOption, ignoreCase = true)
    val isSkipped = selectedOption == null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0C7545), // Rich Jade Green container from reference image
        border = BorderStroke(2.dp, Color(0xFFFCEA74)), // Sunshine Yellow border from reference image
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Q# Badge, Status Badge (Correct / Incorrect / Skipped), Bookmark, Topic/Subtopic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Question Number Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = "Q${index.toString().padStart(2, '0')}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    // Status Badge (Correct / Incorrect / Skipped)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isSkipped -> Color(0xFFF8FAFC)
                            isCorrect -> Color(0xFFDCFCE7)
                            else -> Color(0xFFFEE2E2)
                        },
                        border = BorderStroke(
                            1.5.dp,
                            when {
                                isSkipped -> Color(0xFFCBD5E1)
                                isCorrect -> Color(0xFF16A34A)
                                else -> Color(0xFFDC2626)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    isSkipped -> Icons.Default.RemoveCircleOutline
                                    isCorrect -> Icons.Default.CheckCircle
                                    else -> Icons.Default.Cancel
                                },
                                contentDescription = null,
                                tint = when {
                                    isSkipped -> Color(0xFF334155)
                                    isCorrect -> Color(0xFF166534)
                                    else -> Color(0xFF991B1B)
                                },
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = when {
                                    isSkipped -> "Skipped"
                                    isCorrect -> "Correct"
                                    else -> "Incorrect"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isSkipped -> Color(0xFF334155)
                                    isCorrect -> Color(0xFF166534)
                                    else -> Color(0xFF991B1B)
                                }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(start = 8.dp)
                ) {
                    IconButton(
                        onClick = {
                            onToggleBookmark(question)
                            Toast.makeText(
                                context,
                                if (isBookmarked) "Bookmark removed" else "Question saved to Room Database!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark Question",
                            tint = if (isBookmarked) Color(0xFFFCEA74) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    val labelText = if (question.subtopic.isNotBlank()) question.subtopic
                    else if (question.topicId.isNotBlank()) question.topicId
                    else question.subjectId
                    Text(
                        text = labelText,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.95f),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent Question Text Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEF7D7), // Pale Sunshine Yellow from reference image
                border = BorderStroke(1.5.dp, Color(0xFFFCEA74)), // Sunshine Yellow accent border
                modifier = Modifier.fillMaxWidth()
            ) {
                MathAndHighlightText(
                    text = question.questionText,
                    highlights = question.questionHighlights,
                    location = HighlightLocation.OTHER,
                    highlightConfig = LocalAppConfig.current.questionHighlightConfig,
                    fontSize = 18.sp, // Prominent 17-19sp spec
                    color = Color(0xFF07141D), // Dark slate/navy text for high contrast readability
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Answer Options List
            val optionEntries = question.options.entries.toList()

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                optionEntries.forEach { (key, value) ->
                    val isThisCorrect = key.equals(question.correctOption, ignoreCase = true)
                    val isThisSelected = key.equals(selectedOption, ignoreCase = true)

                    val bgColor = when {
                        isThisCorrect -> Color(0xFFFDEA75) // Sunshine Yellow highlight from reference image
                        isThisSelected && !isThisCorrect -> Color(0xFFFEE2E2) // Wrong answer color
                        else -> Color.White // Clean white options cards from reference image
                    }
                    val borderColor = when {
                        isThisCorrect -> Color(0xFF0C7545) // Rich Jade Green border for correct answer
                        isThisSelected && !isThisCorrect -> Color(0xFFDC2626) // Ruby red border for wrong answer
                        else -> Color(0xFFE2E8F0) // Clean subtle border for neutral options
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        border = BorderStroke(if (isThisCorrect || isThisSelected) 2.dp else 1.5.dp, borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circle label (A, B, C, D)
                            Surface(
                                shape = CircleShape,
                                color = when {
                                    isThisCorrect -> Color(0xFF0C7545) // Jade Green badge for correct answer
                                    isThisSelected && !isThisCorrect -> Color(0xFFDC2626) // Red badge for wrong answer
                                    else -> Color(0xFFDCE5ED) // Soft blue-gray badge from reference image
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key.uppercase(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isThisCorrect -> Color.White
                                            isThisSelected && !isThisCorrect -> Color.White
                                            else -> Color(0xFF1E293B)
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                MathAndHighlightText(
                                    text = value,
                                    highlights = emptyList(),
                                    location = HighlightLocation.OTHER,
                                    highlightConfig = LocalAppConfig.current.questionHighlightConfig,
                                    fontSize = 16.sp, // 15-17sp spec
                                    color = Color(0xFF07141D),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (isThisCorrect) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(shape = CircleShape, color = Color(0xFF0C7545), modifier = Modifier.size(18.dp)) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(2.dp))
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Correct Answer", fontSize = 9.sp, color = Color(0xFF0C7545), fontWeight = FontWeight.Bold)
                                }
                            } else if (isThisSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(shape = CircleShape, color = Color(0xFFDC2626), modifier = Modifier.size(18.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.padding(2.dp))
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Your Answer", fontSize = 9.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Explanation Card
            val hasValidExplanation = !question.explanation.isNullOrBlank() &&
                    question.explanation.trim().lowercase() != "no explanation available for this question." &&
                    question.explanation.trim().lowercase() != "no explanation available."

            if (hasValidExplanation) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFD8FDCC), // Soft light mint / pale Jade Green from reference image
                    border = BorderStroke(2.dp, Color(0xFF025935)), // Deep Jade Green border from reference image
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = Color(0xFF0E623D), modifier = Modifier.size(26.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Explanation",
                                    fontSize = 17.sp, // Distinct heading weight & color
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF025935) // Deep Jade Green heading text from reference image
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        MathAndHighlightText(
                            text = question.explanation ?: "",
                            highlights = question.explanationHighlights,
                            location = HighlightLocation.OTHER,
                            highlightConfig = LocalAppConfig.current.questionHighlightConfig,
                            fontSize = 15.5.sp, // 15-16sp spec with comfortable line spacing
                            color = Color(0xFF064E3B), // Deep Jade text for crystal clarity and reference image fidelity
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

data class AiSuggestionConfig(
    val badgeText: String,
    val badgeBg: Color,
    val badgeTextCol: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val iconBg: Color,
    val message: String,
    val heroTitle: String = "Great Effort!",
    val heroSubtitle: String = "Practice Today",
    val donutColors: List<Color> = listOf(Color(0xFFE11D48), Color(0xFFFB7185))
)

fun getAiSuggestionConfig(scorePercentage: Int): AiSuggestionConfig {
    return when {
        scorePercentage >= 100 -> AiSuggestionConfig(
            badgeText = "Perfect Score! 💯",
            badgeBg = Color(0xFFFEF3C7),
            badgeTextCol = Color(0xFF92400E),
            cardBg = Color(0xFFFFFDF2),
            cardBorder = Color(0xFFFDE68A),
            iconBg = Color(0xFFD97706),
            message = "Flawless performance! You scored a perfect 100%! You have mastered every question in this set with complete precision. Outstanding achievement!",
            heroTitle = "Perfect 100%!",
            heroSubtitle = "Flawless Mastery",
            donutColors = listOf(Color(0xFFF59E0B), Color(0xFF10B981))
        )
        scorePercentage in 91..99 -> AiSuggestionConfig(
            badgeText = "Outstanding Mastery",
            badgeBg = Color(0xFFDCFCE7),
            badgeTextCol = Color(0xFF15803D),
            cardBg = Color(0xFFF0FDF4),
            cardBorder = Color(0xFFBBF7D0),
            iconBg = Color(0xFF16A34A),
            message = "Outstanding mastery! Exceptional accuracy and deep subject knowledge. Maintain this consistency to secure a top rank!",
            heroTitle = "Outstanding!",
            heroSubtitle = "Top Tier Accuracy",
            donutColors = listOf(Color(0xFF16A34A), Color(0xFF4ADE80))
        )
        scorePercentage in 81..90 -> AiSuggestionConfig(
            badgeText = "Great Performance",
            badgeBg = Color(0xFFD1FAE5),
            badgeTextCol = Color(0xFF047857),
            cardBg = Color(0xFFECFDF5),
            cardBorder = Color(0xFFA7F3D0),
            iconBg = Color(0xFF059669),
            message = "Great performance! You are operating at an advanced level. Challenge yourself with exam-level mock tests to sharpen your competitive edge.",
            heroTitle = "Great Performance!",
            heroSubtitle = "Exam Ready Level",
            donutColors = listOf(Color(0xFF059669), Color(0xFF34D399))
        )
        scorePercentage in 71..80 -> AiSuggestionConfig(
            badgeText = "Strong Performance",
            badgeBg = Color(0xFFDBEAFE),
            badgeTextCol = Color(0xFF1D4ED8),
            cardBg = Color(0xFFEFF6FF),
            cardBorder = Color(0xFFBFDBFE),
            iconBg = Color(0xFF2563EB),
            message = "Strong performance! You are demonstrating clear conceptual clarity. Keep maintaining high accuracy across all question types.",
            heroTitle = "Strong Effort!",
            heroSubtitle = "High Accuracy Maintained",
            donutColors = listOf(Color(0xFF2563EB), Color(0xFF60A5FA))
        )
        scorePercentage in 61..70 -> AiSuggestionConfig(
            badgeText = "Good Progress",
            badgeBg = Color(0xFFE0F2FE),
            badgeTextCol = Color(0xFF0369A1),
            cardBg = Color(0xFFF0F9FF),
            cardBorder = Color(0xFFBAE6FD),
            iconBg = Color(0xFF0284C7),
            message = "Great progress! You have a solid grasp of many key concepts. Maintain consistent practice to reach higher accuracy.",
            heroTitle = "Good Progress!",
            heroSubtitle = "Consistent Practice Wins",
            donutColors = listOf(Color(0xFF0284C7), Color(0xFF38BDF8))
        )
        scorePercentage in 51..60 -> AiSuggestionConfig(
            badgeText = "Good Momentum",
            badgeBg = Color(0xFFFEF9C3),
            badgeTextCol = Color(0xFF854D0E),
            cardBg = Color(0xFFFEFCE8),
            cardBorder = Color(0xFFFEF08A),
            iconBg = Color(0xFFCA8A04),
            message = "Good job! You've crossed the halfway mark. Keep practicing to strengthen your accuracy and eliminate minor mistakes.",
            heroTitle = "Good Effort!",
            heroSubtitle = "Strengthening Accuracy",
            donutColors = listOf(Color(0xFFCA8A04), Color(0xFFFACC15))
        )
        scorePercentage in 41..50 -> AiSuggestionConfig(
            badgeText = "Steady Revision",
            badgeBg = Color(0xFFFFEDD5),
            badgeTextCol = Color(0xFFC2410C),
            cardBg = Color(0xFFFFF7ED),
            cardBorder = Color(0xFFFED7AA),
            iconBg = Color(0xFFEA580C),
            message = "Promising effort! Regular revision of key topics and consistent daily practice will help you push past the halfway mark.",
            heroTitle = "Keep Pushing!",
            heroSubtitle = "Regular Revision Needed",
            donutColors = listOf(Color(0xFFEA580C), Color(0xFFFB923C))
        )
        scorePercentage in 21..40 -> AiSuggestionConfig(
            badgeText = "Identify Weak Areas",
            badgeBg = Color(0xFFFEF3C7),
            badgeTextCol = Color(0xFFB45309),
            cardBg = Color(0xFFFFFBEB),
            cardBorder = Color(0xFFFDE68A),
            iconBg = Color(0xFFD97706),
            message = "You're making progress! Review the questions you missed, identify your weak areas, and engage in targeted practice to build confidence.",
            heroTitle = "Keep Practicing!",
            heroSubtitle = "Target Weak Areas",
            donutColors = listOf(Color(0xFFD97706), Color(0xFFFBBF24))
        )
        scorePercentage in 1..20 -> AiSuggestionConfig(
            badgeText = "Focus on Basics",
            badgeBg = Color(0xFFFFE4E6),
            badgeTextCol = Color(0xFFBE123C),
            cardBg = Color(0xFFFFF1F2),
            cardBorder = Color(0xFFFECDD3),
            iconBg = Color(0xFFE11D48),
            message = "Good start on taking this practice set! Focus on strengthening your core fundamentals and revising basic concepts before retrying.",
            heroTitle = "Build Basics!",
            heroSubtitle = "Focus on Fundamentals",
            donutColors = listOf(Color(0xFFE11D48), Color(0xFFFB7185))
        )
        else -> AiSuggestionConfig( // 0%
            badgeText = "Keep Trying",
            badgeBg = Color(0xFFFEE2E2),
            badgeTextCol = Color(0xFF991B1B),
            cardBg = Color(0xFFFEF2F2),
            cardBorder = Color(0xFFFCA5A5),
            iconBg = Color(0xFFDC2626),
            message = "Every practice attempt is a learning opportunity. Review the explanations carefully, build your foundations, and try again!",
            heroTitle = "Keep Learning!",
            heroSubtitle = "Every Step Counts",
            donutColors = listOf(Color(0xFFDC2626), Color(0xFFF87171))
        )
    }
}

@Composable
fun AiStudySuggestionCard(
    scorePercentage: Int,
    modifier: Modifier = Modifier
) {
    val config = remember(scorePercentage) { getAiSuggestionConfig(scorePercentage) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = config.cardBg,
        border = BorderStroke(1.dp, config.cardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = config.iconBg,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("AI Study Suggestion", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = config.badgeBg
                    ) {
                        Text(config.badgeText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = config.badgeTextCol, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = config.message,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A),
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(28.dp).align(Alignment.CenterVertically)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
