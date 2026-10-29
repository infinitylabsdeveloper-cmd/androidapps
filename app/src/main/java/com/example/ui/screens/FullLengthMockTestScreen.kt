package com.example.ui.screens
import com.example.ui.theme.LocalAppConfig
import com.example.ui.theme.LocalWindowSizeClass
import com.example.ui.components.MathAndHighlightText
import com.example.model.HighlightLocation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.SampleData
import com.example.model.GeneratedMockTest
import com.example.model.Question
import com.example.model.PracticeAppearanceSettings
import com.example.ui.components.PracticeAppearancePanel
import com.example.ui.theme.resolveFontFamily
import com.example.util.PracticeAppearanceManager
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Full-Length Mock Test Execution Screen
 *
 * Dedicated strictly to Full-Length Mock Tests to faithfully reproduce
 * the authentic competitive examination interface:
 * - Official Indian Flag Emblem & Clean Mock Test Top Bar
 * - Live Countdown Timer with automatic submit on expiry
 * - Sub-header Stat Card with Section, Question count (4/55), Marks (+1.00) and Negative (-0.33)
 * - Crisp Question Card with Q.X purple badge and Mark for Review button
 * - Clean Option Cards (A, B, C, D) with distinct colored letter circles and radio buttons
 * - Motivational quote box at bottom of question card
 * - Responsive Question Palette
 * - 3-Button Bottom Action Bar: Previous, Skip, and Save & Next
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullLengthMockTestScreen(
    test: GeneratedMockTest,
    onAnswerSelected: (Map<String, String>) -> Unit = {},
    onSubmitTest: (Int, Int, Double, Map<String, String>, Int) -> Unit,
    onPauseTest: (Int, Int, Int, Set<String>, Set<String>, Map<String, String>) -> Unit,
    initialBookmarkedIds: Set<String> = emptySet(),
    onToggleBookmark: (Question) -> Unit = {},
    onNavigateToSubscription: () -> Unit = {}
) {
    if (!SampleData.isUserPremium()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0F172A)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD700).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFFFD700)),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Premium Subscription Required",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mock test generation is free! To attempt, start, or submit full-length mock exams, upgrade to Premium subscription.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToSubscription,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Upgrade to Premium", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { onPauseTest(0, 0, 0, emptySet(), emptySet(), emptyMap()) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Go Back", color = Color.White)
                    }
                }
            }
        }
        return
    }

    val questions = test.questions
    if (questions.isEmpty()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8FAFC)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No questions found in this Mock Test.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onPauseTest(0, 0, 0, emptySet(), emptySet(), emptyMap()) }) {
                        Text("Go Back")
                    }
                }
            }
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(test.currentIndex) }
    val userAnswers = remember { mutableStateMapOf<String, String>().apply { putAll(test.userAnswers) } }
    var markedForReview by remember { mutableStateOf(test.markedForReview) }
    var visitedQuestions by remember { mutableStateOf(setOf(test.currentIndex)) }
    var skippedQuestions by remember { mutableStateOf(test.skippedQuestions.map { id -> test.questions.indexOfFirst { it.id == id } }.filter { it != -1 }.toSet()) }
    var bookmarkedQuestions by remember(initialBookmarkedIds) { mutableStateOf(initialBookmarkedIds) }

    // Palette state: Default is compact icon mode
    var isPaletteExpanded by remember { mutableStateOf(false) }

    // Dialog states
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showPracticeAppearanceSheet by remember { mutableStateOf(false) }

    // Appearance settings for Mock Tests (stored in independent mock_test_appearance_settings)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var mockTestAppearanceSettings by remember {
        mutableStateOf(PracticeAppearanceManager.getMockTestSettings(context))
    }
    var hasCustomAppearance by remember {
        mutableStateOf(PracticeAppearanceManager.hasCustomMockTestSettings(context))
    }

    val defaultMockTestBgBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFEDD5).copy(alpha = 0.55f), // Soft peach
                Color(0xFFE0F2FE).copy(alpha = 0.50f), // Soft sky blue
                Color(0xFFEDE9FE).copy(alpha = 0.45f), // Soft lavender
                Color(0xFFF8FAFC)                      // Clean light slate
            ),
            start = Offset(0f, 0f),
            end = Offset(1000f, 1500f)
        )
    }

    // Timer logic
    val totalSeconds = remember(test.durationMinutes) { test.durationMinutes * 60 }
    var remainingSeconds by remember { mutableIntStateOf(test.remainingSeconds ?: totalSeconds) }
    var timeTakenSeconds by remember { mutableIntStateOf(test.timeTakenSeconds) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
            timeTakenSeconds++
        }
        if (remainingSeconds <= 0 && isTimerRunning) {
            isTimerRunning = false
            val correct = questions.count { userAnswers[it.id] == it.correctOption }
            val wrong = questions.count { userAnswers.containsKey(it.id) && userAnswers[it.id] != it.correctOption }
            val score = (correct * test.positiveMarks) - (wrong * test.negativeMarks)
            onSubmitTest(correct, wrong, score, userAnswers.toMap(), timeTakenSeconds)
        }
    }

    BackHandler {
        showExitConfirmDialog = true
    }

    fun submitExamNow() {
        isTimerRunning = false
        val correct = questions.count { userAnswers[it.id] == it.correctOption }
        val wrong = questions.count { userAnswers.containsKey(it.id) && userAnswers[it.id] != it.correctOption }
        val score = (correct * test.positiveMarks) - (wrong * test.negativeMarks)
        onSubmitTest(correct, wrong, score, userAnswers.toMap(), timeTakenSeconds)
    }

    val currentQuestion = questions.getOrElse(currentIndex) { questions.first() }

    // Resolve human-readable Section Name
    val sectionName = remember(currentQuestion.subjectId) {
        val appSubjects = SampleData.getSubjectsForApp(test.appId)
        val matchedSubject = appSubjects.find { it.id.equals(currentQuestion.subjectId, ignoreCase = true) }
        matchedSubject?.name ?: when {
            currentQuestion.subjectId.contains("polity", ignoreCase = true) -> "Indian Polity"
            currentQuestion.subjectId.contains("history", ignoreCase = true) -> "Indian & AP History"
            currentQuestion.subjectId.contains("economy", ignoreCase = true) -> "Indian Economy"
            currentQuestion.subjectId.contains("geography", ignoreCase = true) -> "Geography"
            currentQuestion.subjectId.contains("science", ignoreCase = true) -> "Science & Tech"
            currentQuestion.subjectId.contains("ca", ignoreCase = true) || currentQuestion.subjectId.contains("current", ignoreCase = true) -> "Current Affairs"
            else -> "General Knowledge"
        }
    }

    val windowSizeClass = LocalWindowSizeClass.current
    val isTabletExpanded = windowSizeClass == WindowWidthSizeClass.Expanded
    val isMediumOrExpanded = windowSizeClass != WindowWidthSizeClass.Compact

    Scaffold(
        containerColor = if (hasCustomAppearance) Color(mockTestAppearanceSettings.screenBgColor) else Color(0xFFF8FAFC),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                // 1. Top Header Bar matching reference image
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button (rounded white card with subtle border)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { showExitConfirmDialog = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Title & Subtitle
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Full-Length Mock Test",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = SampleData.getActiveApp().name.uppercase(),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Timer Pill (⏱ 01:29:17)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF3E8FF),
                        border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        MockTimerDisplay(timeProvider = { remainingSeconds })
                    }

                    // Question Palette Toggle Icon
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPaletteExpanded) Color(0xFFEDE9FE) else Color.White,
                        border = BorderStroke(1.dp, if (isPaletteExpanded) Color(0xFF7C3AED) else Color(0xFFE2E8F0)),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { isPaletteExpanded = !isPaletteExpanded }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPaletteExpanded) Icons.Default.Close else Icons.Default.GridView,
                                contentDescription = if (isPaletteExpanded) "Close Question Palette" else "Open Question Palette",
                                tint = if (isPaletteExpanded) Color(0xFF6D28D9) else Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Appearance Settings Quick Action
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { showPracticeAppearanceSheet = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Practice Appearance Settings",
                                tint = Color(0xFF6D28D9),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Overflow Menu
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .size(38.dp)
                                .clickable { showOverflowMenu = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Appearance Settings", fontWeight = FontWeight.Medium) },
                                onClick = {
                                    showOverflowMenu = false
                                    showPracticeAppearanceSheet = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFF6D28D9))
                                }
                            )
                            HorizontalDivider()
                            val isCurrentBookmarked = bookmarkedQuestions.contains(currentQuestion.id)
                            DropdownMenuItem(
                                text = { Text(if (isCurrentBookmarked) "Unbookmark Question" else "Bookmark Question") },
                                onClick = {
                                    showOverflowMenu = false
                                    onToggleBookmark(currentQuestion)
                                    bookmarkedQuestions = if (isCurrentBookmarked) {
                                        bookmarkedQuestions - currentQuestion.id
                                    } else {
                                        bookmarkedQuestions + currentQuestion.id
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isCurrentBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = null,
                                        tint = Color(0xFF7C3AED)
                                    )
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Clear Current Response") },
                                onClick = {
                                    showOverflowMenu = false
                                    userAnswers.remove(currentQuestion.id)
                                    onAnswerSelected(userAnswers.toMap())
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color(0xFFEA580C))
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Submit Mock Test", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
                                onClick = {
                                    showOverflowMenu = false
                                    showSubmitConfirmDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFDC2626))
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                // 2. Sub-header Stat Bar matching reference layout
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Section info on left
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = "Section",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Section",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = sectionName,
                                    color = Color(0xFF0F172A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Vertical separator divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(30.dp)
                                .background(Color(0xFFE2E8F0))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Question counter on right
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFF7ED),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Assignment,
                                        contentDescription = "Question",
                                        tint = Color(0xFFEA580C),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Question",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${currentIndex + 1} / ${questions.size}",
                                    color = Color(0xFF0F172A),
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // 3-Button Bottom Action Bar matching reference image
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                val haptic = LocalHapticFeedback.current

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Previous Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable(enabled = currentIndex > 0) {
                                if (currentIndex > 0) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    currentIndex--
                                    visitedQuestions = visitedQuestions + currentIndex
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF0F9FF),
                        border = BorderStroke(1.2.dp, if (currentIndex > 0) Color(0xFFBAE6FD) else Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous",
                                tint = if (currentIndex > 0) Color(0xFF0284C7) else Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Previous",
                                color = if (currentIndex > 0) Color(0xFF0284C7) else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // 2. Skip Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                skippedQuestions = skippedQuestions + currentIndex
                                if (currentIndex < questions.size - 1) {
                                    currentIndex++
                                    visitedQuestions = visitedQuestions + currentIndex
                                } else {
                                    showSubmitConfirmDialog = true
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFF7ED),
                        border = BorderStroke(1.2.dp, Color(0xFFFFEDD5))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Skip",
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Skip",
                                color = Color(0xFFEA580C),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // 3. Save & Next Button
                    Surface(
                        modifier = Modifier
                            .weight(1.35f)
                            .height(48.dp)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                visitedQuestions = visitedQuestions + currentIndex
                                if (currentIndex < questions.size - 1) {
                                    currentIndex++
                                    visitedQuestions = visitedQuestions + currentIndex
                                } else {
                                    showSubmitConfirmDialog = true
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Transparent,
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF6D28D9), Color(0xFF7C3AED))
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (currentIndex == questions.size - 1) "Submit" else "Save & Next",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .then(
                    if (hasCustomAppearance) {
                        Modifier.background(Color(mockTestAppearanceSettings.screenBgColor))
                    } else {
                        Modifier.background(defaultMockTestBgBrush)
                    }
                )
        ) {
            val isWideScreen = (windowSizeClass == WindowWidthSizeClass.Expanded) || maxWidth >= 840.dp
            val selectedOption = userAnswers[currentQuestion.id]

            if (isWideScreen && isPaletteExpanded) {
                // Wide Screen Tablet Mode
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        QuestionContentCard(
                            question = currentQuestion,
                            questionIndex = currentIndex,
                            selectedOption = selectedOption,
                            isMarkedForReview = markedForReview.contains(currentQuestion.id),
                            userAnswers = userAnswers,
                            appearanceSettings = mockTestAppearanceSettings,
                            hasCustomAppearance = hasCustomAppearance,
                            onOptionSelected = { optionKey ->
                                userAnswers[currentQuestion.id] = optionKey
                                onAnswerSelected(userAnswers.toMap())
                            },
                            onToggleMarkForReview = {
                                markedForReview = if (markedForReview.contains(currentQuestion.id)) {
                                    markedForReview - currentQuestion.id
                                } else {
                                    markedForReview + currentQuestion.id
                                }
                            }
                        )
                    }

                    Box(modifier = Modifier.width(340.dp)) {
                        FullQuestionPaletteCard(
                            questions = questions,
                            currentIndex = currentIndex,
                            userAnswers = userAnswers,
                            markedForReview = markedForReview,
                            visitedQuestions = visitedQuestions,
                            skippedQuestions = skippedQuestions,
                            onQuestionSelected = { targetIndex ->
                                currentIndex = targetIndex
                                visitedQuestions = visitedQuestions + targetIndex
                            },
                            onCollapse = { isPaletteExpanded = false }
                        )
                    }
                }
            } else {
                // Standard Mobile View
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        QuestionContentCard(
                            question = currentQuestion,
                            questionIndex = currentIndex,
                            selectedOption = selectedOption,
                            isMarkedForReview = markedForReview.contains(currentQuestion.id),
                            userAnswers = userAnswers,
                            appearanceSettings = mockTestAppearanceSettings,
                            hasCustomAppearance = hasCustomAppearance,
                            onOptionSelected = { optionKey ->
                                userAnswers[currentQuestion.id] = optionKey
                                onAnswerSelected(userAnswers.toMap())
                            },
                            onToggleMarkForReview = {
                                markedForReview = if (markedForReview.contains(currentQuestion.id)) {
                                    markedForReview - currentQuestion.id
                                } else {
                                    markedForReview + currentQuestion.id
                                }
                            }
                        )
                    }

                    // Sliding Question Palette Drawer
                    AnimatedVisibility(
                        visible = isPaletteExpanded,
                        enter = slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300)
                        ) + fadeIn(),
                        exit = slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(250)
                        ) + fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.38f))
                                .clickable { isPaletteExpanded = false }
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight()
                                    .widthIn(max = 350.dp)
                                    .fillMaxWidth(0.88f)
                                    .clickable(enabled = false) {}
                                    .padding(12.dp)
                            ) {
                                FullQuestionPaletteCard(
                                    questions = questions,
                                    currentIndex = currentIndex,
                                    userAnswers = userAnswers,
                                    markedForReview = markedForReview,
                                    visitedQuestions = visitedQuestions,
                                    skippedQuestions = skippedQuestions,
                                    onQuestionSelected = { targetIndex ->
                                        currentIndex = targetIndex
                                        visitedQuestions = visitedQuestions + targetIndex
                                    },
                                    onCollapse = { isPaletteExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitConfirmDialog) {
        val answeredCount = userAnswers.size
        val unansweredCount = questions.size - answeredCount
        val reviewCount = markedForReview.size
        val ansAndReviewCount = markedForReview.count { id -> userAnswers.containsKey(id) }

        Dialog(onDismissRequest = { showSubmitConfirmDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AssignmentTurnedIn,
                        contentDescription = null,
                        tint = Color(0xFF6D28D9),
                        modifier = Modifier.size(44.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Submit Mock Test?",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Review your test progress summary below before final submission.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SubmitStatRow("Total Questions", "${questions.size}", Color(0xFF0F172A))
                            SubmitStatRow("Answered", "$answeredCount", Color(0xFF16A34A))
                            SubmitStatRow("Unanswered", "$unansweredCount", Color(0xFFDC2626))
                            SubmitStatRow("Marked for Review", "$reviewCount", Color(0xFF7C3AED))
                            SubmitStatRow("Answered & Review", "$ansAndReviewCount", Color(0xFFEA580C))
                            SubmitStatRow("Time Remaining", String.format(Locale.getDefault(), "%02d:%02d:%02d", remainingSeconds / 3600, (remainingSeconds % 3600) / 60, remainingSeconds % 60), Color(0xFF2563EB))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showSubmitConfirmDialog = false },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Text("Resume", color = Color(0xFF334155), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showSubmitConfirmDialog = false
                                submitExamNow()
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9))
                        ) {
                            Text("Submit Exam", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Exit Confirmation Dialog
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = {
                Text(
                    text = "Pause or Leave Exam?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = "Your progress will be saved so you can resume anytime.",
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        onPauseTest(currentIndex, remainingSeconds, timeTakenSeconds, markedForReview, skippedQuestions.map { questions[it].id }.toSet(), userAnswers)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Leave & Save", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExitConfirmDialog = false }) {
                    Text("Continue Exam")
                }
            }
        )
    }

    // Independent Mock Test Appearance Settings Dialog
    if (showPracticeAppearanceSheet) {
        Dialog(
            onDismissRequest = { showPracticeAppearanceSheet = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            PracticeAppearancePanel(
                currentSettings = mockTestAppearanceSettings,
                onSave = { newPa ->
                    PracticeAppearanceManager.saveMockTestSettings(context, newPa)
                    mockTestAppearanceSettings = newPa
                    hasCustomAppearance = true
                    showPracticeAppearanceSheet = false
                },
                onReset = {
                    PracticeAppearanceManager.resetMockTestDefault(context)
                    mockTestAppearanceSettings = PracticeAppearanceManager.getMockTestDefaultSettings()
                    hasCustomAppearance = false
                    showPracticeAppearanceSheet = false
                },
                onBack = { showPracticeAppearanceSheet = false }
            )
        }
    }
}

/**
 * Question Content Card
 * Recreates exact layout from reference:
 * - Top header with Q.4 bright purple pill and Mark for Review button
 * - Clean bold question text
 * - Option Cards (A, B, C, D) with:
 *     - Option A: Soft blue background, solid blue circle with white 'A', blue radio button
 *     - Option B: Soft pink/magenta background, solid pink circle with white 'B', pink radio button
 *     - Option C: Soft green background, solid green circle with white 'C', green radio button
 *     - Option D: Soft amber background, solid amber circle with white 'D', amber radio button
 * - Motivational quote box at bottom of question card
 * - Supports custom appearance settings if customized by the user via PracticeAppearancePanel
 */
@Composable
private fun QuestionContentCard(
    question: Question,
    questionIndex: Int,
    selectedOption: String?,
    isMarkedForReview: Boolean,
    userAnswers: Map<String, String>,
    appearanceSettings: PracticeAppearanceSettings,
    hasCustomAppearance: Boolean,
    onOptionSelected: (String) -> Unit,
    onToggleMarkForReview: () -> Unit
) {
    val optionKeys = remember { listOf("A", "B", "C", "D") }

    val cardBg = if (hasCustomAppearance) Color(appearanceSettings.questionBgColor) else Color.White
    val cardBorder = if (hasCustomAppearance) Color(appearanceSettings.questionBorderColor) else Color(0xFFE2E8F0)
    val cardCornerRadius = if (hasCustomAppearance) appearanceSettings.questionCornerRadius.dp else 20.dp

    Card(
        shape = RoundedCornerShape(cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Row: Q.4 Badge on Left, Mark for Review on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Purple Rounded Badge (Q.4)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF7C3AED),
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = "Q.${questionIndex + 1}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                // Mark for Review Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isMarkedForReview) Color(0xFFF3E8FF) else Color(0xFFFDF2F8),
                    border = BorderStroke(1.dp, if (isMarkedForReview) Color(0xFFDDD6FE) else Color(0xFFFBCFE8).copy(alpha = 0.7f)),
                    modifier = Modifier.clickable { onToggleMarkForReview() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isMarkedForReview) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Mark for Review",
                            tint = if (isMarkedForReview) Color(0xFF6D28D9) else Color(0xFFA21CAF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mark for Review",
                            color = if (isMarkedForReview) Color(0xFF6D28D9) else Color(0xFFA21CAF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question Text
            val qFontSize = if (hasCustomAppearance) appearanceSettings.questionFontSize.sp else 16.5.sp
            val qFontFamily = if (hasCustomAppearance) resolveFontFamily(appearanceSettings.questionFontFamily) else null
            val qTextColor = if (hasCustomAppearance) Color(appearanceSettings.questionTextColor) else Color(0xFF0F172A)

            MockTestStyledQuestionText(
                text = question.questionText,
                highlights = question.questionHighlights,
                fontSize = qFontSize,
                fontFamily = qFontFamily,
                textColor = qTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Options Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = null,
                        tint = Color(0xFF6D28D9),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF3E8FF)
                ) {
                    Text(
                        text = "Select one answer",
                        color = Color(0xFF7C3AED),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val haptic = LocalHapticFeedback.current

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                optionKeys.forEach { key ->
                    val optionText = question.options[key] ?: ""
                    if (optionText.isNotBlank()) {
                        val isSelected = selectedOption == key
                        MockTestOptionCard(
                            letter = key,
                            text = optionText,
                            isSelected = isSelected,
                            isCorrect = key == question.correctOption,
                            showFeedback = false,
                            appearanceSettings = appearanceSettings,
                            hasCustomAppearance = hasCustomAppearance,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onOptionSelected(key)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Motivational quote box at bottom of question card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF5F3FF),
                border = BorderStroke(1.dp, Color(0xFFEDE9FE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFDDD6FE)))
                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "\"Small steps in practice, big results in the exam.\"",
                        color = Color(0xFF4C1D95),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Individual Option Card (A, B, C, D)
 * Exact match to the reference image in Default Mode:
 * - Option A: Soft blue background (#F0F9FF), solid blue circle (#2563EB) with crisp white 'A', blue radio button
 * - Option B: Soft pink background (#FDF2F8), solid pink circle (#EC4899) with crisp white 'B', pink radio button
 * - Option C: Soft green background (#F0FDF4), solid green circle (#10B981) with crisp white 'C', green radio button
 * - Option D: Soft amber background (#FFFBEB), solid amber circle (#F59E0B) with crisp white 'D', amber radio button
 * If custom appearance settings are active, dynamically uses user-configured styles.
 */
@Composable
private fun MockTestOptionCard(
    letter: String,
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    showFeedback: Boolean,
    appearanceSettings: PracticeAppearanceSettings,
    hasCustomAppearance: Boolean,
    onClick: () -> Unit
) {
    if (hasCustomAppearance) {
        val targetBorderColor = if (isSelected) Color(0xFF6D28D9) else Color(appearanceSettings.optionBorderColor)
        val targetBgColor = if (isSelected) Color(0xFFF5F3FF) else Color(appearanceSettings.optionBgColor)
        val targetTextColor = if (isSelected) Color(0xFF6D28D9) else Color(appearanceSettings.optionTextColor)
        val optFontFamily = resolveFontFamily(appearanceSettings.optionFontFamily)
        val optFontSize = appearanceSettings.optionFontSize.sp
        val optCornerRadius = appearanceSettings.optionCornerRadius.dp

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(optCornerRadius))
                .clickable { onClick() },
            shape = RoundedCornerShape(optCornerRadius),
            color = targetBgColor,
            border = BorderStroke(if (isSelected) 1.75.dp else 1.dp, targetBorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) targetBorderColor else targetBorderColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        color = if (isSelected) Color.White else targetBorderColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Box(modifier = Modifier.weight(1f)) {
                    MathAndHighlightText(
                        text = text,
                        highlights = emptyList(),
                        location = HighlightLocation.MOCK_TEST_OPTION,
                        highlightConfig = LocalAppConfig.current.questionHighlightConfig,
                        color = targetTextColor,
                        fontSize = optFontSize,
                        fontFamily = optFontFamily ?: androidx.compose.ui.text.font.FontFamily.Default,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = targetBorderColor,
                        unselectedColor = targetBorderColor.copy(alpha = 0.5f)
                    )
                )
            }
        }
    } else {
        // EXACT DEFAULT MATCH TO UPLOADED REFERENCE DESIGN
        val primaryColor = when (letter) {
            "A" -> Color(0xFF2563EB) // Royal Blue
            "B" -> Color(0xFFEC4899) // Hot Pink / Magenta
            "C" -> Color(0xFF10B981) // Emerald Green
            else -> Color(0xFFF59E0B) // Bright Amber
        }

        val defaultBorderColor = when (letter) {
            "A" -> Color(0xFFBAE6FD) // Light Blue border
            "B" -> Color(0xFFFBCFE8) // Soft Pink border
            "C" -> Color(0xFFBBF7D0) // Soft Green border
            else -> Color(0xFFFDE68A) // Soft Amber border
        }

        val cardBgColor = when (letter) {
            "A" -> Color(0xFFF0F9FF) // Sky tint
            "B" -> Color(0xFFFDF2F8) // Pink tint
            "C" -> Color(0xFFF0FDF4) // Green tint
            else -> Color(0xFFFFFBEB) // Amber tint
        }

        val targetBorderColor = if (isSelected) primaryColor else defaultBorderColor
        val targetBorderWidth = if (isSelected) 2.dp else 1.25.dp

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            color = cardBgColor,
            border = BorderStroke(targetBorderWidth, targetBorderColor),
            shadowElevation = if (isSelected) 1.5.dp else 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Solid colored circle with bold white letter
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(primaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Box(modifier = Modifier.weight(1f)) {
                    MathAndHighlightText(
                        text = text,
                        highlights = emptyList(),
                        location = HighlightLocation.MOCK_TEST_OPTION,
                        highlightConfig = LocalAppConfig.current.questionHighlightConfig,
                        color = Color(0xFF1E293B),
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Custom matching radio button
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isSelected) 2.dp else 1.5.dp,
                            color = if (isSelected) primaryColor else primaryColor.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full Question Palette Card
 */
@Composable
private fun FullQuestionPaletteCard(
    questions: List<Question>,
    currentIndex: Int,
    userAnswers: Map<String, String>,
    markedForReview: Set<String>,
    visitedQuestions: Set<Int>,
    skippedQuestions: Set<Int>,
    onQuestionSelected: (Int) -> Unit,
    onCollapse: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Header Row: "Questions" + Chevron Up
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Questions",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = LocalAppConfig.current.advancedFontSettings.questionFontSize.sp,
                        color = Color(0xFF0F172A)
                    )
                )

                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Collapse Palette",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Legend (5 items)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PaletteLegendItem(
                    shapeType = LegendShapeType.CIRCLE,
                    color = Color(0xFFCBD5E1),
                    label = "Not\nVisited"
                )
                PaletteLegendItem(
                    shapeType = LegendShapeType.CIRCLE,
                    color = Color(0xFF16A34A),
                    label = "Answered"
                )
                PaletteLegendItem(
                    shapeType = LegendShapeType.CIRCLE,
                    color = Color(0xFF7C3AED),
                    label = "Review"
                )
                PaletteLegendItem(
                    shapeType = LegendShapeType.CIRCLE,
                    color = Color(0xFFEA580C),
                    label = "Ans. &\nReview"
                )
                PaletteLegendItem(
                    shapeType = LegendShapeType.SQUARE_BORDER,
                    color = Color(0xFF2563EB),
                    label = "Current"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // 4-Column Grid of Questions
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = questions,
                    key = { index, question -> "${question.id}_$index" }
                ) { index, question ->
                    val isCurrent = index == currentIndex
                    val isAnswered = userAnswers.containsKey(question.id) && userAnswers[question.id]?.isNotBlank() == true
                    val isMarked = markedForReview.contains(question.id)
                    val isVisited = visitedQuestions.contains(index)
                    val isSkipped = skippedQuestions.contains(index) && !isAnswered

                    PaletteQuestionCell(
                        number = index + 1,
                        isCurrent = isCurrent,
                        isAnswered = isAnswered,
                        isMarked = isMarked,
                        isVisited = isVisited,
                        isSkipped = isSkipped,
                        onClick = { onQuestionSelected(index) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total ${questions.size} Questions",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private enum class LegendShapeType {
    CIRCLE,
    SQUARE_BORDER
}

@Composable
private fun PaletteLegendItem(
    shapeType: LegendShapeType,
    color: Color,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(52.dp)
    ) {
        when (shapeType) {
            LegendShapeType.CIRCLE -> {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
            LegendShapeType.SQUARE_BORDER -> {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .border(1.5.dp, color, RoundedCornerShape(3.dp))
                        .background(Color.White)
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color(0xFF475569),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}

@Composable
private fun PaletteQuestionCell(
    number: Int,
    isCurrent: Boolean,
    isAnswered: Boolean,
    isMarked: Boolean,
    isVisited: Boolean,
    isSkipped: Boolean,
    onClick: () -> Unit
) {
    val bgColor: Color
    val textColor: Color
    val borderStroke: BorderStroke?

    when {
        isCurrent -> {
            bgColor = Color.White
            textColor = Color(0xFF2563EB)
            borderStroke = BorderStroke(2.dp, Color(0xFF2563EB))
        }
        isAnswered && isMarked -> {
            bgColor = Color(0xFFFEF3C7)
            textColor = Color(0xFFC2410C)
            borderStroke = null
        }
        isMarked -> {
            bgColor = Color(0xFFF3E8FF)
            textColor = Color(0xFF6B21A8)
            borderStroke = null
        }
        isAnswered -> {
            bgColor = Color(0xFFDCFCE7)
            textColor = Color(0xFF15803D)
            borderStroke = null
        }
        isSkipped || (isVisited && !isAnswered) -> {
            bgColor = Color.White
            textColor = Color(0xFFDC2626)
            borderStroke = BorderStroke(1.dp, Color(0xFFE2E8F0))
        }
        else -> {
            bgColor = Color.White
            textColor = Color(0xFF1E293B)
            borderStroke = BorderStroke(1.dp, Color(0xFFE2E8F0))
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = borderStroke
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "$number",
                color = textColor,
                fontSize = LocalAppConfig.current.advancedFontSettings.explanationFontSize.sp,
                fontWeight = if (isCurrent || isAnswered || isMarked) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Styled question text highlighting terms enclosed in single quotes ('...') or **bold** in red
 */
@Composable
private fun MockTestStyledQuestionText(
    text: String,
    highlights: List<com.example.model.HighlightRange> = emptyList(),
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontFamily: androidx.compose.ui.text.font.FontFamily? = null,
    textColor: Color = Color(0xFF0F172A)
) {
    MathAndHighlightText(
        text = text,
        highlights = highlights,
        location = HighlightLocation.MOCK_TEST_QUESTION,
        highlightConfig = LocalAppConfig.current.questionHighlightConfig,
        fontSize = fontSize,
        fontFamily = fontFamily ?: androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Indian Flag Circular Emblem
 */
@Composable
private fun IndianFlagEmblem(modifier: Modifier = Modifier.size(36.dp)) {
    Surface(
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stripeHeight = size.height / 3f
            val center = Offset(size.width / 2, size.height / 2)

            // Saffron Top
            drawRect(
                color = Color(0xFFFF9933),
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(size.width, stripeHeight)
            )
            // White Middle
            drawRect(
                color = Color.White,
                topLeft = Offset(0f, stripeHeight),
                size = androidx.compose.ui.geometry.Size(size.width, stripeHeight)
            )
            // Green Bottom
            drawRect(
                color = Color(0xFF138808),
                topLeft = Offset(0f, stripeHeight * 2),
                size = androidx.compose.ui.geometry.Size(size.width, stripeHeight)
            )
            // Navy Blue Ashoka Chakra Circle
            drawCircle(
                color = Color(0xFF000080),
                radius = stripeHeight / 2.3f,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF000080),
                radius = 1.2.dp.toPx(),
                center = center
            )
        }
    }
}

@Composable
private fun SubmitStatRow(title: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color(0xFF64748B), fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = valueColor, fontSize = LocalAppConfig.current.advancedFontSettings.explanationFontSize.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MockTimerDisplay(timeProvider: () -> Int) {
    val remainingSeconds = timeProvider()
    val hrs = remainingSeconds / 3600
    val mins = (remainingSeconds % 3600) / 60
    val secs = remainingSeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
    
    val color = if (remainingSeconds < 300) Color(0xFFDC2626) else Color(0xFF4338CA)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Timer,
            contentDescription = "Timer",
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = formattedTime,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
