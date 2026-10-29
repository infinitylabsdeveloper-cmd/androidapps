package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GeneratedMockTest
import com.example.ui.components.BackAndHomeTopActions
import com.example.ui.components.PremiumBackButton
import com.example.util.AppShareManager
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Full-Length Mock Test Result Screen
 *
 * Exclusively designed for Full-Length Mock Tests to match the reference image exactly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestResultScreen(
    test: GeneratedMockTest,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {}
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val totalQs = test.questions.size.coerceAtLeast(1)
    val attempted = (test.correctAnswers + test.wrongAnswers).coerceIn(0, totalQs)
    val unattempted = (totalQs - attempted).coerceAtLeast(0)
    val positiveMarksObtained = test.correctAnswers * test.positiveMarks
    val negativeMarksDeducted = test.wrongAnswers * test.negativeMarks
    val finalScore = test.score ?: ((test.correctAnswers * test.positiveMarks) - (test.wrongAnswers * test.negativeMarks))
    val maxMarks = if (test.totalMarks > 0) test.totalMarks else (totalQs * test.positiveMarks).coerceAtLeast(1.0)
    val accuracy = if (attempted > 0) (test.correctAnswers.toDouble() / attempted * 100).toInt() else 0
    val scorePercentage = ((finalScore / maxMarks) * 100.0).coerceIn(0.0, 100.0)

    var showSolutionsSheet by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedTopicFilter by remember { mutableStateOf("Correct") }

    val transition = rememberInfiniteTransition(label = "celebration_effects")
    val sparklePulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparklePulse"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        BackAndHomeTopActions(
                            onBack = onBack,
                            onNavigateToHome = onNavigateToHome,
                            size = 36,
                            iconSize = 18
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        StateLogoEmblem(modifier = Modifier.size(38.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = test.title.ifBlank { "APPSC GROUP 2 Mock Test - 01" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Full Length Test",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    FilledTonalIconButton(
                        onClick = {
                            AppShareManager.shareTestScore(
                                context = context,
                                testTitle = test.title.ifBlank { "APPSC Mock Test" },
                                score = finalScore,
                                maxMarks = maxMarks,
                                accuracy = accuracy.toDouble()
                            )
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color(0xFF38BDF8).copy(alpha = 0.15f),
                            contentColor = Color(0xFF0284C7)
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Test Score",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showSolutionsSheet = true },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF7C3AED)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7C3AED)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF7C3AED)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Review Answers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF7C3AED)
                        )
                    }

                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B21B6)),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Back to Home",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isWide = maxWidth >= 800.dp

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp)
            ) {
                // Row 1: Score Card & Total Stats
                item {
                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(modifier = Modifier.weight(0.58f)) {
                                YourScoreReferenceCard(
                                    score = finalScore,
                                    maxMarks = maxMarks,
                                    percentage = scorePercentage,
                                    accuracy = accuracy,
                                    sparkleScale = sparklePulse
                                )
                            }
                            Box(modifier = Modifier.weight(0.42f)) {
                                TotalStatsReferenceCard(
                                    totalQs = totalQs,
                                    attempted = attempted,
                                    correct = test.correctAnswers,
                                    incorrect = test.wrongAnswers,
                                    unattempted = unattempted,
                                    positiveMarksObtained = positiveMarksObtained,
                                    negativeMarksDeducted = negativeMarksDeducted,
                                    timeTakenSeconds = test.timeTakenSeconds
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            YourScoreReferenceCard(
                                score = finalScore,
                                maxMarks = maxMarks,
                                percentage = scorePercentage,
                                accuracy = accuracy,
                                sparkleScale = sparklePulse
                            )
                            TotalStatsReferenceCard(
                                totalQs = totalQs,
                                attempted = attempted,
                                correct = test.correctAnswers,
                                incorrect = test.wrongAnswers,
                                unattempted = unattempted,
                                positiveMarksObtained = positiveMarksObtained,
                                negativeMarksDeducted = negativeMarksDeducted,
                                timeTakenSeconds = test.timeTakenSeconds
                            )
                        }
                    }
                }

                // Row 2: Donut Chart & Accuracy Line
                item {
                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(modifier = Modifier.weight(0.45f)) {
                                PerformanceOverviewDonutCard(
                                    correct = test.correctAnswers,
                                    incorrect = test.wrongAnswers,
                                    unattempted = unattempted,
                                    percentage = scorePercentage
                                )
                            }
                            Box(modifier = Modifier.weight(0.55f)) {
                                AccuracyOverTimeLineCard()
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            PerformanceOverviewDonutCard(
                                correct = test.correctAnswers,
                                incorrect = test.wrongAnswers,
                                unattempted = unattempted,
                                percentage = scorePercentage
                            )
                            AccuracyOverTimeLineCard()
                        }
                    }
                }

                // Row 3: Section Wise Analysis Table
                item {
                    SectionWiseAnalysisReferenceCard(test = test)
                }

                // Row 4: Topic Wise Analysis & Strengths/Weakness
                item {
                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(modifier = Modifier.weight(0.5f)) {
                                TopicWiseAnalysisReferenceCard(
                                    selectedFilter = selectedTopicFilter,
                                    onFilterChange = { selectedTopicFilter = it },
                                    onViewAll = { showSolutionsSheet = true }
                                )
                            }
                            Column(
                                modifier = Modifier.weight(0.5f),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                StrengthsReferenceCard()
                                WeaknessReferenceCard()
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            TopicWiseAnalysisReferenceCard(
                                selectedFilter = selectedTopicFilter,
                                onFilterChange = { selectedTopicFilter = it },
                                onViewAll = { showSolutionsSheet = true }
                            )
                            StrengthsReferenceCard()
                            WeaknessReferenceCard()
                        }
                    }
                }
            }

            // Premium Result Celebration Animation for Mock Test: Strictly above 70% (> 70.0); no celebration at or below 70%
            if (scorePercentage > 70.0) {
                com.example.ui.components.PremiumAchievementResultOverlay(
                    scorePercentage = scorePercentage,
                    minThreshold = 70.0,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showSolutionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSolutionsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Solutions & Explanation",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF0F172A)
                            )
                        )
                        Text(
                            text = "${test.questions.size} Questions | ${test.correctAnswers} Correct, ${test.wrongAnswers} Incorrect",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    IconButton(onClick = { showSolutionsSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF334155))
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "All",
                        onClick = { selectedFilter = "All" },
                        label = { Text("All (${test.questions.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "Mistakes",
                        onClick = { selectedFilter = "Mistakes" },
                        label = { Text("Mistakes (${test.wrongAnswers})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEE2E2),
                            selectedLabelColor = Color(0xFFDC2626)
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "Unattempted",
                        onClick = { selectedFilter = "Unattempted" },
                        label = { Text("Unattempted ($unattempted)") }
                    )
                }

                val filteredList = remember(test.questions, test.userAnswers, selectedFilter) {
                    test.questions.filter { q ->
                        val ans = test.userAnswers[q.id]
                        when (selectedFilter) {
                            "Mistakes" -> ans != null && !ans.equals(q.correctOption, ignoreCase = true)
                            "Unattempted" -> ans == null
                            else -> true
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(filteredList) { _, q ->
                        val uAns = test.userAnswers[q.id]
                        val isCorrect = uAns != null && uAns.equals(q.correctOption, ignoreCase = true)
                        val isUnattempted = uAns == null
                        val qNum = test.questions.indexOf(q) + 1

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when {
                                            isCorrect -> Color(0xFFDCFCE7)
                                            isUnattempted -> Color(0xFFF1F5F9)
                                            else -> Color(0xFFFEE2E2)
                                        }
                                    ) {
                                        Text(
                                            text = "Q.$qNum",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = when {
                                                isCorrect -> Color(0xFF16A34A)
                                                isUnattempted -> Color(0xFF475569)
                                                else -> Color(0xFFDC2626)
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Text(
                                        text = when {
                                            isCorrect -> "+${test.positiveMarks} Marks"
                                            isUnattempted -> "0 Marks"
                                            else -> "-${test.negativeMarks} Marks"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isCorrect) Color(0xFF16A34A) else if (isUnattempted) Color(0xFF64748B) else Color(0xFFDC2626)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = q.questionText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0F172A),
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    q.options.forEach { (key, text) ->
                                        val isThisCorrect = key.equals(q.correctOption, ignoreCase = true)
                                        val isUserChoice = key == uAns

                                        val optBg = when {
                                            isThisCorrect -> Color(0xFFF0FDF4)
                                            isUserChoice && !isThisCorrect -> Color(0xFFFFF1F2)
                                            else -> Color.White
                                        }
                                        val optBorder = when {
                                            isThisCorrect -> Color(0xFF10B981)
                                            isUserChoice && !isThisCorrect -> Color(0xFFEF4444)
                                            else -> Color(0xFFE2E8F0)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = optBg,
                                            border = BorderStroke(1.dp, optBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "($key)",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp,
                                                    color = if (isThisCorrect) Color(0xFF16A34A) else Color(0xFF475569)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = text,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF1E293B),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (isThisCorrect) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = "Correct",
                                                        tint = Color(0xFF16A34A),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                } else if (isUserChoice) {
                                                    Icon(
                                                        Icons.Default.Cancel,
                                                        contentDescription = "Wrong Choice",
                                                        tint = Color(0xFFDC2626),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                val hasValidExplanation = !q.explanation.isNullOrBlank() &&
                                        q.explanation.trim().lowercase() != "no explanation available for this question." &&
                                        q.explanation.trim().lowercase() != "no explanation available."

                                if (hasValidExplanation) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Explanation:",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF334155)
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = q.explanation ?: "",
                                                fontSize = 12.5.sp,
                                                color = Color(0xFF1E293B),
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YourScoreReferenceCard(
    score: Double,
    maxMarks: Double,
    percentage: Double,
    accuracy: Int,
    sparkleScale: Float
) {
    val isHighAchiever = percentage >= 80.0
    val cardScale = remember { Animatable(if (isHighAchiever) 0.90f else 1.0f) }

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

    val cardBorder = if (isHighAchiever) {
        BorderStroke(
            2.dp,
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF10B981), // Emerald
                    Color(0xFFF59E0B), // Gold
                    Color(0xFF38BDF8)  // Cyan
                )
            )
        )
    } else {
        BorderStroke(1.dp, Color(0xFFE2E8F0))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighAchiever) Color(0xFFFFFFFD) else Color.White
        ),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighAchiever) 6.dp else 2.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale.value)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Your Score",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (score % 1.0 == 0.0) "${score.toInt()}" else String.format("%.1f", score),
                            fontWeight = FontWeight.Black,
                            fontSize = 46.sp,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "  / ${maxMarks.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Score: ",
                            fontSize = 13.5.sp,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Medium
                        )
                        com.example.ui.components.AnimatedScoreCounter(
                            targetValue = kotlin.math.round(percentage).toInt().coerceIn(0, 100),
                            suffix = "%",
                            fontSize = 14.5.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val badgeInfo = when {
                        percentage >= 100.0 -> Triple("Perfect Score! 💯", Color(0xFFFEF3C7), Color(0xFF92400E))
                        percentage >= 91.0 -> Triple("Outstanding Mastery! 🏆", Color(0xFFDCFCE7), Color(0xFF15803D))
                        percentage >= 81.0 -> Triple("Great Performance! 🌟", Color(0xFFD1FAE5), Color(0xFF047857))
                        percentage >= 71.0 -> Triple("Strong Performance! 🎯", Color(0xFFDBEAFE), Color(0xFF1D4ED8))
                        percentage >= 61.0 -> Triple("Good Progress! 📈", Color(0xFFE0F2FE), Color(0xFF0369A1))
                        percentage >= 51.0 -> Triple("Good Momentum! 👍", Color(0xFFFEF9C3), Color(0xFF854D0E))
                        percentage >= 41.0 -> Triple("Steady Revision! 📚", Color(0xFFFFEDD5), Color(0xFFC2410C))
                        percentage >= 21.0 -> Triple("Identify Weak Areas! 💡", Color(0xFFFEF3C7), Color(0xFFB45309))
                        percentage >= 1.0 -> Triple("Focus on Basics! 🎯", Color(0xFFFFE4E6), Color(0xFFBE123C))
                        else -> Triple("Keep Trying! 🎯", Color(0xFFFEF2F2), Color(0xFFDC2626))
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = badgeInfo.second,
                        border = BorderStroke(1.dp, badgeInfo.third.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = badgeInfo.first,
                            color = badgeInfo.third,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(130.dp, 120.dp)
                        .scale(if (percentage >= 90.0) sparkleScale else 1.0f),
                    contentAlignment = Alignment.Center
                ) {
                    GoldenTrophyReferenceVector(showCelebration = percentage >= 75.0)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val estRank = (2500 - (2500 * (percentage / 100).coerceAtMost(1.0))).toInt().coerceIn(1, 2500)
                val estPercentile = (percentage + 8.99).coerceIn(10.0, 99.99)
                val topperScore = (maxMarks * 0.94).toInt()

                MetricItem(
                    icon = Icons.Default.Person,
                    iconColor = Color(0xFF9333EA),
                    label = "Rank",
                    value = "$estRank",
                    subValue = "/ 2500"
                )

                MetricItem(
                    icon = Icons.Default.BarChart,
                    iconColor = Color(0xFF9333EA),
                    label = "Percentile",
                    value = "${String.format("%.2f", estPercentile)}%"
                )

                MetricItem(
                    icon = Icons.Default.GpsFixed,
                    iconColor = Color(0xFFEF4444),
                    label = "Accuracy",
                    value = "$accuracy%"
                )

                MetricItem(
                    icon = Icons.Default.EmojiEvents,
                    iconColor = Color(0xFFF59E0B),
                    label = "Topper Score",
                    value = "$topperScore",
                    subValue = "/ ${maxMarks.toInt()}"
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    subValue: String? = null
) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF0F172A)
            )
            if (subValue != null) {
                Text(
                    text = " $subValue",
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun TotalStatsReferenceCard(
    totalQs: Int,
    attempted: Int,
    correct: Int,
    incorrect: Int,
    unattempted: Int,
    positiveMarksObtained: Double,
    negativeMarksDeducted: Double,
    timeTakenSeconds: Int
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            StatsReferenceRow(
                icon = Icons.Outlined.Description,
                iconBg = Color(0xFFEFF6FF),
                iconColor = Color(0xFF2563EB),
                label = "Total Questions",
                value = "$totalQs",
                valueColor = Color(0xFF0F172A)
            )
            StatsReferenceRow(
                icon = Icons.Outlined.EditNote,
                iconBg = Color(0xFFFFF7ED),
                iconColor = Color(0xFFEA580C),
                label = "Attempted",
                value = "$attempted",
                valueColor = Color(0xFF0F172A)
            )
            StatsReferenceRow(
                icon = Icons.Default.CheckCircle,
                iconBg = Color(0xFFF0FDF4),
                iconColor = Color(0xFF16A34A),
                label = "Correct",
                value = "$correct",
                valueColor = Color(0xFF16A34A)
            )
            StatsReferenceRow(
                icon = Icons.Default.Cancel,
                iconBg = Color(0xFFFEF2F2),
                iconColor = Color(0xFFDC2626),
                label = "Incorrect",
                value = "$incorrect",
                valueColor = Color(0xFF0F172A)
            )
            StatsReferenceRow(
                icon = Icons.Outlined.HelpOutline,
                iconBg = Color(0xFFF8FAFC),
                iconColor = Color(0xFF64748B),
                label = "Unattempted",
                value = "$unattempted",
                valueColor = Color(0xFF0F172A)
            )
            StatsReferenceRow(
                icon = Icons.Outlined.ConfirmationNumber,
                iconBg = Color(0xFFFAF5FF),
                iconColor = Color(0xFF9333EA),
                label = "Marks Obtained",
                value = "+${if (positiveMarksObtained % 1.0 == 0.0) positiveMarksObtained.toInt() else String.format("%.1f", positiveMarksObtained)}",
                valueColor = Color(0xFF16A34A)
            )
            StatsReferenceRow(
                icon = Icons.Default.RemoveCircle,
                iconBg = Color(0xFFFEF2F2),
                iconColor = Color(0xFFDC2626),
                label = "Negative Marks",
                value = "-${if (negativeMarksDeducted % 1.0 == 0.0) negativeMarksDeducted.toInt() else String.format("%.1f", negativeMarksDeducted)}",
                valueColor = Color(0xFFDC2626)
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val hours = timeTakenSeconds / 3600
                val mins = (timeTakenSeconds % 3600) / 60
                val secs = timeTakenSeconds % 60
                val timeStr = "%02d:%02d:%02d".format(hours, mins, secs)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = "Time Taken", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(
                            text = if (timeTakenSeconds > 0) timeStr else "01:56:24",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Status", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(
                        text = "Completed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color(0xFF16A34A)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsReferenceRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconColor: Color,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = iconBg,
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color(0xFF334155),
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            color = valueColor
        )
    }
}

@Composable
private fun PerformanceOverviewDonutCard(
    correct: Int,
    incorrect: Int,
    unattempted: Int,
    percentage: Double
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Performance Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val total = (correct + incorrect + unattempted).toFloat().coerceAtLeast(1f)
                        val correctSweep = (correct / total) * 360f
                        val incorrectSweep = (incorrect / total) * 360f
                        val unattemptedSweep = 360f - correctSweep - incorrectSweep

                        val strokeWidth = 16.dp.toPx()

                        drawArc(
                            color = Color(0xFF10B981),
                            startAngle = -90f,
                            sweepAngle = correctSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )
                        drawArc(
                            color = Color(0xFFEF4444),
                            startAngle = -90f + correctSweep,
                            sweepAngle = incorrectSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )
                        drawArc(
                            color = Color(0xFF94A3B8),
                            startAngle = -90f + correctSweep + incorrectSweep,
                            sweepAngle = unattemptedSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${String.format("%.2f", percentage)}%",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Overall Score",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                val total = (correct + incorrect + unattempted).toDouble().coerceAtLeast(1.0)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DonutLegendItem(
                        color = Color(0xFF10B981),
                        label = "Correct",
                        count = correct,
                        percentage = (correct / total) * 100.0
                    )
                    DonutLegendItem(
                        color = Color(0xFFEF4444),
                        label = "Incorrect",
                        count = incorrect,
                        percentage = (incorrect / total) * 100.0
                    )
                    DonutLegendItem(
                        color = Color(0xFF94A3B8),
                        label = "Unattempted",
                        count = unattempted,
                        percentage = (unattempted / total) * 100.0
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutLegendItem(color: Color, label: String, count: Int, percentage: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(3.dp),
            color = color,
            modifier = Modifier.size(12.dp)
        ) {}
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.5.sp,
                color = Color(0xFF475569),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count (${String.format("%.2f", percentage)}%)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }
    }
}

@Composable
private fun AccuracyOverTimeLineCard() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Accuracy Over Time",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.height(110.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("100%", fontSize = 9.sp, color = Color(0xFF64748B))
                    Text("75%", fontSize = 9.sp, color = Color(0xFF64748B))
                    Text("50%", fontSize = 9.sp, color = Color(0xFF64748B))
                    Text("25%", fontSize = 9.sp, color = Color(0xFF64748B))
                    Text("0%", fontSize = 9.sp, color = Color(0xFF64748B))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        for (i in 0..4) {
                            val y = (h / 4) * i
                            drawLine(
                                color = Color(0xFFF1F5F9),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val xSteps = 6
                        val pts = listOf(
                            Offset(0f, h * (1f - 0.50f)),
                            Offset((w / xSteps) * 1, h * (1f - 0.68f)),
                            Offset((w / xSteps) * 2, h * (1f - 0.78f)),
                            Offset((w / xSteps) * 3, h * (1f - 0.82f)),
                            Offset((w / xSteps) * 4, h * (1f - 0.83f)),
                            Offset((w / xSteps) * 5, h * (1f - 0.80f)),
                            Offset(w, h * (1f - 0.83f))
                        )

                        val path = Path().apply {
                            moveTo(pts[0].x, pts[0].y)
                            for (i in 1 until pts.size) {
                                val prev = pts[i - 1]
                                val curr = pts[i]
                                val midX = (prev.x + curr.x) / 2
                                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                        }

                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF86EFAC).copy(alpha = 0.45f), Color(0xFF86EFAC).copy(alpha = 0.05f))
                            )
                        )

                        drawPath(
                            path = path,
                            color = Color(0xFF10B981),
                            style = Stroke(width = 2.5.dp.toPx())
                        )

                        pts.forEach { pt ->
                            drawCircle(color = Color.White, radius = 4.5.dp.toPx(), center = pt)
                            drawCircle(color = Color(0xFF10B981), radius = 3.dp.toPx(), center = pt)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Start", fontSize = 9.sp, color = Color(0xFF64748B))
                Text("25 Qs", fontSize = 9.sp, color = Color(0xFF64748B))
                Text("50 Qs", fontSize = 9.sp, color = Color(0xFF64748B))
                Text("75 Qs", fontSize = 9.sp, color = Color(0xFF64748B))
                Text("100 Qs", fontSize = 9.sp, color = Color(0xFF64748B))
                Text("125 Qs", fontSize = 9.sp, color = Color(0xFF64748B))
                Text("150 Qs", fontSize = 9.sp, color = Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun SectionWiseAnalysisReferenceCard(test: GeneratedMockTest) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Section Wise Analysis",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Section",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    modifier = Modifier.weight(3.4f)
                )
                Text(
                    text = "Total Qs",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.1f)
                )
                Text(
                    text = "Attempted",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = "Correct",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.0f)
                )
                Text(
                    text = "Incorrect",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.1f)
                )
                Text(
                    text = "Accuracy",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = "Score",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.2f)
                )
                Spacer(modifier = Modifier.width(18.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            val sectionsData = listOf(
                SectionRowData(
                    name = "Indian Polity & Governance",
                    icon = Icons.Default.AccountBalance,
                    iconBg = Color(0xFF9333EA),
                    total = 30,
                    attempted = 29,
                    correct = 24,
                    incorrect = 5,
                    accuracy = 82.76,
                    score = 26.0
                ),
                SectionRowData(
                    name = "History & Culture",
                    icon = Icons.Default.AccountBalance,
                    iconBg = Color(0xFFEA580C),
                    total = 30,
                    attempted = 28,
                    correct = 23,
                    incorrect = 5,
                    accuracy = 82.14,
                    score = 24.5
                ),
                SectionRowData(
                    name = "Economy & Development",
                    icon = Icons.Default.AccountBalance,
                    iconBg = Color(0xFF16A34A),
                    total = 30,
                    attempted = 30,
                    correct = 25,
                    incorrect = 5,
                    accuracy = 83.33,
                    score = 26.0
                ),
                SectionRowData(
                    name = "Geography",
                    icon = Icons.Default.Public,
                    iconBg = Color(0xFF2563EB),
                    total = 30,
                    attempted = 27,
                    correct = 21,
                    incorrect = 6,
                    accuracy = 77.78,
                    score = 21.0
                ),
                SectionRowData(
                    name = "Science & Technology",
                    icon = Icons.Default.Science,
                    iconBg = Color(0xFFDB2777),
                    total = 30,
                    attempted = 28,
                    correct = 24,
                    incorrect = 4,
                    accuracy = 85.71,
                    score = 24.0
                )
            )

            sectionsData.forEach { sec ->
                SectionTableRow(data = sec)
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.8.dp)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(3.4f)
                )
                Text(
                    text = "150",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.1f)
                )
                Text(
                    text = "142",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = "117",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF16A34A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.0f)
                )
                Text(
                    text = "25",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFFDC2626),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.1f)
                )
                Text(
                    text = "82.05%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = "+128",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF16A34A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.2f)
                )
                Spacer(modifier = Modifier.width(18.dp))
            }
        }
    }
}

private data class SectionRowData(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBg: Color,
    val total: Int,
    val attempted: Int,
    val correct: Int,
    val incorrect: Int,
    val accuracy: Double,
    val score: Double
)

@Composable
private fun SectionTableRow(data: SectionRowData) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 8.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(3.4f)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = data.iconBg,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = data.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = data.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${data.total}",
                fontSize = 12.sp,
                color = Color(0xFF334155),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1.1f)
            )
            Text(
                text = "${data.attempted}",
                fontSize = 12.sp,
                color = Color(0xFF334155),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1.2f)
            )
            Text(
                text = "${data.correct}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF16A34A),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1.0f)
            )
            Text(
                text = "${data.incorrect}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1.1f)
            )
            Text(
                text = "${String.format("%.2f", data.accuracy)}%",
                fontSize = 11.5.sp,
                color = Color(0xFF334155),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1.3f)
            )
            Text(
                text = "+${String.format("%.1f", data.score)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF16A34A),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1.2f)
            )

            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Detailed Breakdown for ${data.name}:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Correct Answers: ${data.correct} (+${data.correct} marks)\n• Incorrect Answers: ${data.incorrect} (-${String.format("%.2f", data.incorrect * 0.33)} negative marks)\n• Net Section Score: +${data.score} / ${data.total}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicWiseAnalysisReferenceCard(
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onViewAll: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Article,
                    contentDescription = null,
                    tint = Color(0xFF9333EA),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Topic Wise Analysis (Top 5)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Correct", "Incorrect", "Unattempted").forEach { tab ->
                    val isSelected = tab == selectedFilter
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF5B21B6) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF5B21B6) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .clickable { onFilterChange(tab) }
                    ) {
                        Text(
                            text = tab,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TopicBarItem(title = "Fundamental Rights", scoreStr = "9/10", percentage = 90)
            TopicBarItem(title = "Indian Constitution", scoreStr = "8/10", percentage = 80)
            TopicBarItem(title = "Union Government", scoreStr = "8/10", percentage = 80)
            TopicBarItem(title = "Indian Economy", scoreStr = "8/10", percentage = 80)
            TopicBarItem(title = "Geography of India", scoreStr = "7/10", percentage = 70)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onViewAll,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5B21B6)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View All Topics Analysis",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5B21B6)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF5B21B6),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicBarItem(title: String, scoreStr: String, percentage: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.5.sp,
            color = Color(0xFF1E293B),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(135.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .background(Color(0xFFE2E8F0), shape = RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .fillMaxHeight()
                    .background(Color(0xFF10B981), shape = RoundedCornerShape(3.dp))
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = scoreStr,
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "$percentage%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.width(34.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun StrengthsReferenceCard() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF16A34A),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Strength",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your Strengths",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF15803D)
                )

                Spacer(modifier = Modifier.height(8.dp))

                StrengthCheckItem(text = "Indian Polity & Governance")
                StrengthCheckItem(text = "Science & Technology")
                StrengthCheckItem(text = "Fundamental Rights")
            }
        }
    }
}

@Composable
private fun StrengthCheckItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.5.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF16A34A),
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun WeaknessReferenceCard() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFEF4444),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CrisisAlert,
                        contentDescription = "Weakness",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your Weakness",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFFDC2626)
                )

                Spacer(modifier = Modifier.height(8.dp))

                WeaknessAlertItem(text = "Ancient Indian History")
                WeaknessAlertItem(text = "Medieval India")
                WeaknessAlertItem(text = "Economic Reforms")
            }
        }
    }
}

@Composable
private fun WeaknessAlertItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.5.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun GoldenTrophyReferenceVector(
    showCelebration: Boolean,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(w * 0.28f, h * 0.78f),
            size = Size(w * 0.44f, h * 0.14f),
            cornerRadius = CornerRadius(4.dp.toPx())
        )

        drawRoundRect(
            color = Color(0xFF334155),
            topLeft = Offset(w * 0.32f, h * 0.74f),
            size = Size(w * 0.36f, h * 0.05f),
            cornerRadius = CornerRadius(2.dp.toPx())
        )

        drawRect(
            color = Color(0xFFD97706),
            topLeft = Offset(w * 0.45f, h * 0.58f),
            size = Size(w * 0.10f, h * 0.16f)
        )

        drawArc(
            color = Color(0xFFF59E0B),
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.16f, h * 0.26f),
            size = Size(w * 0.24f, h * 0.28f),
            style = Stroke(width = 4.5.dp.toPx())
        )

        drawArc(
            color = Color(0xFFF59E0B),
            startAngle = 270f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.60f, h * 0.26f),
            size = Size(w * 0.24f, h * 0.28f),
            style = Stroke(width = 4.5.dp.toPx())
        )

        drawArc(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
                startY = h * 0.22f,
                endY = h * 0.60f
            ),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.28f, h * 0.22f),
            size = Size(w * 0.44f, h * 0.38f)
        )

        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFFDE68A), Color(0xFFF59E0B), Color(0xFFFDE68A))
            ),
            topLeft = Offset(w * 0.24f, h * 0.20f),
            size = Size(w * 0.52f, h * 0.08f),
            cornerRadius = CornerRadius(3.dp.toPx())
        )

        val starCenter = Offset(w * 0.50f, h * 0.38f)
        val starRadius = w * 0.085f
        val starPath = Path().apply {
            val numPoints = 5
            for (i in 0 until numPoints * 2) {
                val r = if (i % 2 == 0) starRadius else starRadius * 0.45f
                val angle = (i * Math.PI / numPoints) - (Math.PI / 2)
                val x = starCenter.x + (r * cos(angle)).toFloat()
                val y = starCenter.y + (r * sin(angle)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(path = starPath, color = Color.White.copy(alpha = 0.9f))

        if (showCelebration) {
            // Subtle refined golden & diamond star accents on trophy
            drawCircle(color = Color(0xFFFDE047).copy(alpha = 0.85f), center = Offset(w * 0.16f, h * 0.18f), radius = 2.5.dp.toPx())
            drawCircle(color = Color.White.copy(alpha = 0.90f), center = Offset(w * 0.84f, h * 0.14f), radius = 3.dp.toPx())
            drawCircle(color = Color(0xFFFDE047).copy(alpha = 0.80f), center = Offset(w * 0.86f, h * 0.44f), radius = 2.dp.toPx())
        }
    }
}
