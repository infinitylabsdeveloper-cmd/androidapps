package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.db.AppDatabase
import com.example.db.TestResultEntity
import com.example.model.*
import com.example.repository.UserProgressRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// Design 5 — Coral Pink & Peach Luxury Palette
private val CoralPinkPrimary = Color(0xFFE91E63)
private val CoralPinkDark = Color(0xFFC2185B)
private val PeachAccent = Color(0xFFFF7043)
private val WarmChampagne = Color(0xFFFFA726)
private val SoftBlushBgStart = Color(0xFFFFF0F5)
private val SoftPeachBgMid = Color(0xFFFFF3E0)
private val SoftIvoryBgEnd = Color(0xFFFFF8F0)
private val LuxuryCardBg = Color(0xFFFFFFFF)
private val LuxuryBorderColor = Color(0xFFF8BBD0)
private val PrimaryTextColor = Color(0xFF49333F)
private val SubtitleTextColor = Color(0xFF786575)
private val EmeraldSuccess = Color(0xFF2E7D32)
private val RoseWarning = Color(0xFFD32F2F)

/**
 * Performance & Analytics Screen
 * Restored with Design 5 — Coral Pink & Peach Luxury theme.
 * Features:
 * - Animated Progress Ring (Overall Accuracy & Mastery)
 * - 4-Key Analytics KPI Stat Cards (Accuracy, Goal & Streak, Tests Taken, Total XP)
 * - Weekly Activity Bar Chart (Mon-Sun question attempt volume)
 * - Weak-Topics Targeted Diagnostics & Improvement Action Cards
 * - Subject-wise Syllabus Progress & Mastery Breakdown
 * - Recent Test Attempt History with real Room DB data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceBoardScreen(
    results: List<TestResult> = emptyList(),
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToPractice: ((String) -> Unit)? = null,
    onNavigateToSubject: ((Subject) -> Unit)? = null,
    onNavigateToTopic: ((Subject, UnitModel, TopicModel) -> Unit)? = null,
    onNavigateToMockTests: (() -> Unit)? = null
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val currentAppId = SampleData.currentActiveAppId.value
    val db = remember { AppDatabase.getInstance(context) }
    val userProgressRepo = remember { UserProgressRepository(db) }

    val userProgress by userProgressRepo.getUserProgress(currentAppId)
        .collectAsState(initial = UserProgress(appId = currentAppId))
    val performanceSummary by userProgressRepo.getPerformanceSummary(currentAppId)
        .collectAsState(initial = MockTestPerformanceSummary())

    val dbResultsFlow = remember(currentAppId) {
        db.testResultDao().getAllResults(currentAppId)
    }
    val dbResults by dbResultsFlow.collectAsState(initial = emptyList())

    // Time filter pills
    var selectedPeriod by remember { mutableStateOf("Overall") }

    val periodFilteredResults = remember(dbResults, selectedPeriod) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        when (selectedPeriod) {
            "Today" -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfDay = cal.timeInMillis
                dbResults.filter { it.timestamp >= startOfDay }
            }
            "This Week" -> {
                val sevenDaysAgo = now - 7L * 24L * 60L * 60L * 1000L
                dbResults.filter { it.timestamp >= sevenDaysAgo }
            }
            "This Month" -> {
                val thirtyDaysAgo = now - 30L * 24L * 60L * 60L * 1000L
                dbResults.filter { it.timestamp >= thirtyDaysAgo }
            }
            else -> dbResults
        }
    }

    // Aggregate derived analytics metrics from real database records
    val totalAttempted = remember(periodFilteredResults, performanceSummary, userProgress, selectedPeriod) {
        val fromFiltered = periodFilteredResults.sumOf { it.correctAnswers + it.wrongAnswers }
        when {
            selectedPeriod != "Overall" -> fromFiltered
            fromFiltered > 0 -> maxOf(fromFiltered, userProgress.completedQuestionsCount)
            performanceSummary.totalQuestionsAttempted > 0 -> performanceSummary.totalQuestionsAttempted
            else -> userProgress.completedQuestionsCount
        }
    }

    val totalCorrect = remember(periodFilteredResults, performanceSummary, userProgress, selectedPeriod) {
        val fromFiltered = periodFilteredResults.sumOf { it.correctAnswers }
        val estimatedFromProgress = ((userProgress.completedQuestionsCount * userProgress.accuracyPercentage) / 100f).roundToInt()
        when {
            selectedPeriod != "Overall" -> fromFiltered
            fromFiltered > 0 -> maxOf(fromFiltered, performanceSummary.totalCorrectAnswers)
            performanceSummary.totalCorrectAnswers > 0 -> performanceSummary.totalCorrectAnswers
            else -> estimatedFromProgress
        }
    }

    val accuracyInt = remember(totalAttempted, totalCorrect, performanceSummary, userProgress, selectedPeriod) {
        when {
            totalAttempted > 0 -> ((totalCorrect.toFloat() / totalAttempted.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
            selectedPeriod == "Overall" && userProgress.accuracyPercentage > 0f -> userProgress.accuracyPercentage.roundToInt().coerceIn(0, 100)
            else -> 0
        }
    }

    val testsCount = remember(periodFilteredResults, performanceSummary, userProgress, selectedPeriod) {
        when {
            selectedPeriod != "Overall" -> periodFilteredResults.size
            periodFilteredResults.isNotEmpty() -> maxOf(periodFilteredResults.size, userProgress.testsCompletedCount)
            performanceSummary.totalTestsTaken > 0 -> performanceSummary.totalTestsTaken
            else -> userProgress.testsCompletedCount
        }
    }

    val streakDays = remember(userProgress) {
        userProgress.streakDays.coerceAtLeast(0)
    }

    val totalXp = remember(userProgress, totalCorrect, testsCount) {
        if (userProgress.totalXp > 0) userProgress.totalXp
        else (totalCorrect * 10) + (testsCount * 50)
    }

    val avgScoreInt = remember(periodFilteredResults, performanceSummary) {
        if (periodFilteredResults.isNotEmpty()) {
            periodFilteredResults.map { it.scorePercentage }.average().roundToInt().coerceIn(0, 100)
        } else if (performanceSummary.averageScorePercentage > 0f) {
            performanceSummary.averageScorePercentage.roundToInt().coerceIn(0, 100)
        } else {
            0
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SoftBlushBgStart,
                        SoftPeachBgMid,
                        SoftIvoryBgEnd
                    )
                )
            )
    ) {
        val isTablet = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back Arrow + Screen Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(1.dp, LuxuryBorderColor),
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable { onBack() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = CoralPinkPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Analytics Icon Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFEBF2),
                                border = BorderStroke(1.dp, CoralPinkPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = "Analytics",
                                        tint = CoralPinkPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "Performance & Analytics",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Study Diagnostics & Accuracy Insights",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SubtitleTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Right: Home Shortcut
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, LuxuryBorderColor),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { onNavigateToHome() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home",
                                    tint = CoralPinkPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .widthIn(max = if (isTablet) 860.dp else 600.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 36.dp)
            ) {
                // 1. Time Filter Tabs (Overall, This Month, This Week, Today)
                item {
                    val periods = listOf("Overall", "This Month", "This Week", "Today")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        periods.forEach { period ->
                            val isSel = selectedPeriod == period
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSel) CoralPinkPrimary else Color.White,
                                border = BorderStroke(1.dp, if (isSel) CoralPinkPrimary else LuxuryBorderColor),
                                shadowElevation = if (isSel) 2.dp else 0.5.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedPeriod = period }
                            ) {
                                Text(
                                    text = period,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else PrimaryTextColor,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                // 2. HERO PROGRESS RING CARD (Overall Mastery & Accuracy)
                item {
                    ProgressRingMasteryCard(
                        accuracy = accuracyInt,
                        attempted = totalAttempted,
                        correct = totalCorrect,
                        testsTaken = testsCount,
                        streak = streakDays
                    )
                }

                // 3. 4-KEY ANALYTICS KPI CARDS GRID
                item {
                    AnalyticsStatGrid(
                        accuracy = accuracyInt,
                        attempted = totalAttempted,
                        streak = streakDays,
                        xp = totalXp,
                        avgScore = avgScoreInt,
                        isTablet = isTablet
                    )
                }

                // 4. WEEKLY ACTIVITY CHART (Mon - Sun Questions Solved)
                item {
                    WeeklyActivityChartCard(
                        userProgress = userProgress,
                        dbResults = dbResults
                    )
                }

                // 5. WEAK-TOPICS TARGETED DIAGNOSTICS & ACTION CARD
                item {
                    WeakTopicsSectionCard(
                        currentAppId = currentAppId,
                        dbResults = dbResults,
                        onNavigateToTopic = onNavigateToTopic,
                        onNavigateToSubject = onNavigateToSubject,
                        onNavigateToMockTests = onNavigateToMockTests
                    )
                }

                // 6. SUBJECT-WISE SYLLABUS PERFORMANCE BREAKDOWN
                item {
                    SubjectWisePerformanceCard(
                        currentAppId = currentAppId,
                        dbResults = dbResults,
                        onNavigateToSubject = onNavigateToSubject
                    )
                }

                // 7. RECENT TESTS & MOCK PAPERS HISTORY
                item {
                    RecentTestResultsCard(
                        dbResults = periodFilteredResults,
                        onNavigateToMockTests = onNavigateToMockTests
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLE 1: Progress Ring Mastery Card
// ----------------------------------------------------------------------------
@Composable
private fun ProgressRingMasteryCard(
    accuracy: Int,
    attempted: Int,
    correct: Int,
    testsTaken: Int,
    streak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LuxuryCardBg),
        border = BorderStroke(1.5.dp, LuxuryBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = CoralPinkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Overall Study Mastery",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTextColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF0F5),
                    border = BorderStroke(1.dp, LuxuryBorderColor)
                ) {
                    Text(
                        text = "State Benchmark: High",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralPinkPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Animated Progress Ring Donut
                val animatedSweep = remember { Animatable(0f) }
                LaunchedEffect(accuracy) {
                    animatedSweep.animateTo(
                        targetValue = (accuracy / 100f) * 360f,
                        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
                    )
                }

                Box(
                    modifier = Modifier.size(116.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(108.dp)) {
                        val strokeW = 12.dp.toPx()
                        // Background track arc (Blush cream)
                        drawArc(
                            color = Color(0xFFFFEBEE),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                        // Gradient fill arc (Coral Pink -> Warm Peach)
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    CoralPinkPrimary,
                                    PeachAccent,
                                    WarmChampagne,
                                    CoralPinkPrimary
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = animatedSweep.value,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$accuracy%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = CoralPinkPrimary
                        )
                        Text(
                            text = "Accuracy",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SubtitleTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Stats breakdown list on right
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatMetricLine(
                        icon = "🎯",
                        label = "Attempted",
                        value = "$attempted MCQs"
                    )
                    StatMetricLine(
                        icon = "✅",
                        label = "Correct Answers",
                        value = "$correct (${accuracy}%)"
                    )
                    StatMetricLine(
                        icon = "📝",
                        label = "Tests Solved",
                        value = "$testsTaken Papers"
                    )
                    StatMetricLine(
                        icon = "🔥",
                        label = "Daily Consistency",
                        value = "$streak Days Streak"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatMetricLine(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = SubtitleTextColor,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryTextColor
        )
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLE 2: 4-Key Analytics KPI Grid
// ----------------------------------------------------------------------------
@Composable
private fun AnalyticsStatGrid(
    accuracy: Int,
    attempted: Int,
    streak: Int,
    xp: Int,
    avgScore: Int,
    isTablet: Boolean
) {
    val cards = listOf(
        KpiCardModel(
            title = "Overall Accuracy",
            value = "$accuracy%",
            subtitle = "+4.2% vs target average",
            icon = Icons.Default.Verified,
            badgeColor = Color(0xFFE8F5E9),
            accentColor = EmeraldSuccess
        ),
        KpiCardModel(
            title = "Study Streak",
            value = "$streak Days",
            subtitle = "Daily Practice Maintained",
            icon = Icons.Default.LocalFireDepartment,
            badgeColor = Color(0xFFFFF3E0),
            accentColor = PeachAccent
        ),
        KpiCardModel(
            title = "Average Score",
            value = "$avgScore%",
            subtitle = "Based on Mock Series",
            icon = Icons.Default.Grade,
            badgeColor = Color(0xFFFFF8E1),
            accentColor = WarmChampagne
        ),
        KpiCardModel(
            title = "Exam Readiness XP",
            value = "$xp pts",
            subtitle = "Rank Milestone Tier 1",
            icon = Icons.Default.MilitaryTech,
            badgeColor = Color(0xFFFCE4EC),
            accentColor = CoralPinkPrimary
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) { KpiTileItem(cards[0]) }
            Box(modifier = Modifier.weight(1f)) { KpiTileItem(cards[1]) }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) { KpiTileItem(cards[2]) }
            Box(modifier = Modifier.weight(1f)) { KpiTileItem(cards[3]) }
        }
    }
}

private data class KpiCardModel(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeColor: Color,
    val accentColor: Color
)

@Composable
private fun KpiTileItem(model: KpiCardModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LuxuryCardBg),
        border = BorderStroke(1.dp, LuxuryBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = model.title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SubtitleTextColor
                )
                Surface(
                    shape = CircleShape,
                    color = model.badgeColor,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = model.icon,
                            contentDescription = null,
                            tint = model.accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = model.value,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                color = PrimaryTextColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = model.subtitle,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = model.accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLE 3: Weekly Activity Bar Chart
// ----------------------------------------------------------------------------
@Composable
private fun WeeklyActivityChartCard(
    userProgress: UserProgress,
    dbResults: List<TestResultEntity>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LuxuryCardBg),
        border = BorderStroke(1.dp, LuxuryBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = CoralPinkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Weekly Activity Trend",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTextColor
                    )
                }

                Text(
                    text = "Goal: ${SampleData.userProfileState.value.dailyGoalQuestions.coerceAtLeast(30)} MCQs/day",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PeachAccent
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7 Days of Activity computed from real TestResultEntity timestamps + today's progress
            val daysData = remember(dbResults, userProgress) {
                val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val countsByDay = IntArray(7)
                val sevenDaysAgo = System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L
                val cal = Calendar.getInstance()
                dbResults.filter { it.timestamp >= sevenDaysAgo }.forEach { res ->
                    cal.timeInMillis = res.timestamp
                    val dow = cal.get(Calendar.DAY_OF_WEEK) // Sun=1 .. Sat=7
                    val idx = if (dow == Calendar.SUNDAY) 6 else dow - 2
                    if (idx in 0..6) {
                        countsByDay[idx] += (res.correctAnswers + res.wrongAnswers)
                    }
                }
                // Include direct practice questions answered today if higher than logged test results today
                val todayCal = Calendar.getInstance()
                val todayDow = todayCal.get(Calendar.DAY_OF_WEEK)
                val todayIdx = if (todayDow == Calendar.SUNDAY) 6 else todayDow - 2
                if (countsByDay.sum() == 0 && userProgress.completedQuestionsCount > 0 && todayIdx in 0..6) {
                    countsByDay[todayIdx] = userProgress.completedQuestionsCount
                }
                labels.mapIndexed { i, label -> label to countsByDay[i] }
            }
            val maxQuestions = (daysData.maxOfOrNull { it.second }?.toFloat() ?: 0f).coerceAtLeast(30f)
            val totalWeekQuestions = daysData.sumOf { it.second }
            val peakEntry = daysData.maxByOrNull { it.second }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                daysData.forEach { (day, count) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "$count",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralPinkPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val barHeightRatio = if (count > 0) (count / maxQuestions).coerceIn(0.12f, 1f) else 0.06f

                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height((85 * barHeightRatio).dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(CoralPinkPrimary, PeachAccent)
                                    ),
                                    RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = day,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SubtitleTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LuxuryBorderColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (peakEntry != null && peakEntry.second > 0) {
                        "⚡ Peak Day: ${peakEntry.first} (${peakEntry.second} MCQs solved)"
                    } else {
                        "⚡ Start practicing today to build your weekly trend"
                    },
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryTextColor
                )
                Text(
                    text = "$totalWeekQuestions Total this week",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoralPinkPrimary
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLE 4: Weak-Topics Targeted Diagnostics & Improvement
// ----------------------------------------------------------------------------
@Composable
private fun WeakTopicsSectionCard(
    currentAppId: String,
    dbResults: List<TestResultEntity>,
    onNavigateToTopic: ((Subject, UnitModel, TopicModel) -> Unit)?,
    onNavigateToSubject: ((Subject) -> Unit)?,
    onNavigateToMockTests: (() -> Unit)?
) {
    val appSubjects = remember(currentAppId, SampleData.subjectsState.size) {
        SampleData.getSubjectsForApp(currentAppId)
    }

    val weakTopics = remember(dbResults, appSubjects) {
        val resultsWithErrors = dbResults.filter { it.wrongAnswers > 0 }
            .sortedBy { it.scorePercentage }
            .take(3)
        if (resultsWithErrors.isNotEmpty()) {
            resultsWithErrors.map { res ->
                val matchedSub = appSubjects.firstOrNull { subj ->
                    res.testTitle.contains(subj.name.take(6), ignoreCase = true) ||
                        subj.units.any { u -> u.topics.any { t -> res.testTitle.contains(t.name, ignoreCase = true) } }
                } ?: appSubjects.firstOrNull()
                val attempted = (res.correctAnswers + res.wrongAnswers).coerceAtLeast(1)
                val acc = ((res.correctAnswers.toFloat() / attempted) * 100f).roundToInt()
                Triple(
                    res.testTitle.ifBlank { "Practice Set" },
                    matchedSub?.name ?: "Syllabus Module",
                    "$acc% Accuracy • ${res.wrongAnswers} Errors"
                )
            }
        } else {
            // Recommend actual unattempted syllabus topics from the app's subjects
            appSubjects.flatMap { subj ->
                subj.units.flatMap { u ->
                    u.topics.map { t ->
                        val qCount = SampleData.getQuestionsCountForTopic(subj.id, u.id, t.id)
                        Triple(
                            t.name,
                            subj.name,
                            "Not Attempted • $qCount MCQs Available"
                        )
                    }
                }
            }.take(3)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LuxuryCardBg),
        border = BorderStroke(1.5.dp, RoseWarning.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = RoseWarning,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Weak Areas Diagnostics",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTextColor
                        )
                        Text(
                            text = "Focus here to rapidly boost your score",
                            fontSize = 10.5.sp,
                            color = SubtitleTextColor
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = "${weakTopics.size} Focus Areas",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoseWarning,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                weakTopics.forEach { (topicTitle, subjectName, statStr) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF8F9), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, LuxuryBorderColor.copy(alpha = 0.6f)), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topicTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$subjectName • $statStr",
                                fontSize = 11.sp,
                                color = RoseWarning,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Targeted Practice Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CoralPinkPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    val matchedSub = appSubjects.firstOrNull {
                                        it.name.equals(subjectName, ignoreCase = true) ||
                                            subjectName.contains(it.name.take(6), ignoreCase = true)
                                    } ?: appSubjects.firstOrNull()

                                    if (matchedSub != null) {
                                        val matchedPair = matchedSub.units.flatMap { u ->
                                            u.topics.map { t -> u to t }
                                        }.firstOrNull { (_, t) ->
                                            t.name.equals(topicTitle, ignoreCase = true) ||
                                                topicTitle.contains(t.name, ignoreCase = true)
                                        }
                                        val u = matchedPair?.first ?: matchedSub.units.firstOrNull()
                                        val t = matchedPair?.second ?: u?.topics?.firstOrNull()
                                        if (u != null && t != null && onNavigateToTopic != null) {
                                            onNavigateToTopic(matchedSub, u, t)
                                        } else {
                                            onNavigateToSubject?.invoke(matchedSub)
                                        }
                                    }
                                }
                        ) {
                            Text(
                                text = "Practice",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLE 5: Subject-wise Performance Breakdown
// ----------------------------------------------------------------------------
@Composable
private fun SubjectWisePerformanceCard(
    currentAppId: String,
    dbResults: List<TestResultEntity>,
    onNavigateToSubject: ((Subject) -> Unit)?
) {
    val appSubjects = remember(currentAppId, SampleData.subjectsState.size) {
        SampleData.getSubjectsForApp(currentAppId)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LuxuryCardBg),
        border = BorderStroke(1.dp, LuxuryBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = CoralPinkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Subject-wise Accuracy & Mastery",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTextColor
                    )
                }

                Text(
                    text = "${appSubjects.size} Subjects",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubtitleTextColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subject rows
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                appSubjects.forEach { subject ->
                    val subjectResults = remember(dbResults, subject.id, subject.name) {
                        val topicNames = subject.units.flatMap { it.topics }.map { it.name.lowercase() }
                        dbResults.filter { res ->
                            val titleLower = res.testTitle.lowercase()
                            titleLower.contains(subject.name.lowercase()) ||
                                topicNames.any { tName -> tName.isNotBlank() && titleLower.contains(tName) }
                        }
                    }
                    val acc = remember(subjectResults) {
                        val totalAtt = subjectResults.sumOf { it.correctAnswers + it.wrongAnswers }
                        val totalCorr = subjectResults.sumOf { it.correctAnswers }
                        if (totalAtt > 0) ((totalCorr.toFloat() / totalAtt.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
                        else 0
                    }
                    val questionsCount = remember(subject.id, SampleData.questionsState.size) {
                        SampleData.getQuestionsCountForSubject(subject.id)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF8F9), RoundedCornerShape(12.dp))
                            .clickable { onNavigateToSubject?.invoke(subject) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subject.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTextColor,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$acc% Acc",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (acc >= 85) EmeraldSuccess else CoralPinkPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { acc / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (acc >= 85) EmeraldSuccess else CoralPinkPrimary,
                            trackColor = Color(0xFFFFEBEE)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${subject.units.size} Units • $questionsCount MCQs Bank",
                                fontSize = 10.5.sp,
                                color = SubtitleTextColor
                            )
                            Text(
                                text = "Explore Syllabus ➔",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralPinkPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// COMPOSABLE 6: Recent Test Performance History
// ----------------------------------------------------------------------------
@Composable
private fun RecentTestResultsCard(
    dbResults: List<TestResultEntity>,
    onNavigateToMockTests: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LuxuryCardBg),
        border = BorderStroke(1.dp, LuxuryBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = CoralPinkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recent Test Attempts",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTextColor
                    )
                }

                if (dbResults.isNotEmpty()) {
                    Text(
                        text = "${dbResults.size} Recorded",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubtitleTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (dbResults.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📊", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No Mock Tests Attempted Yet",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTextColor
                    )
                    Text(
                        text = "Take a full-length or topic practice test to generate diagnostics.",
                        fontSize = 11.5.sp,
                        color = SubtitleTextColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onNavigateToMockTests?.invoke() },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPinkPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start First Test", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dbResults.take(5).forEach { result ->
                        val pct = result.scorePercentage.roundToInt().coerceIn(0, 100)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF8F9), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = result.testTitle.ifBlank { "APPSC Mock Test" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${dateFormat.format(Date(result.timestamp))} • ${result.correctAnswers}/${result.totalQuestions} Correct",
                                    fontSize = 11.sp,
                                    color = SubtitleTextColor
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (pct >= 70) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ) {
                                Text(
                                    text = "$pct%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (pct >= 70) EmeraldSuccess else RoseWarning,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
