package com.example.ui.screens
import com.example.ui.theme.LocalAppConfig
import com.example.ui.theme.LocalWindowSizeClass
import com.example.ui.theme.AppLuminousBackground
import com.example.ui.components.MathAndHighlightText
import com.example.ui.components.premiumAnswerFeedback
import com.example.ui.components.premiumTargetAnimation
import com.example.model.HighlightLocation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import kotlinx.coroutines.launch

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import com.example.util.PracticeSession
import com.example.util.PracticeSessionManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Question
import com.example.ui.components.*
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.repository.AppConfigRepository
import com.example.util.FontSettingsManager
import com.example.model.PracticeSectionType
import kotlinx.coroutines.delay

import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun StateLogoEmblem(modifier: Modifier = Modifier.size(36.dp)) {
    val ringColor = remember { Color(0XFF15803D) }
    val bgColor = remember { Color(0XFFFEF08A) }
    val innerRingColor = remember { Color(0XFFEA580C) }
    val coreColor = remember { Color(0XFF1D4ED8) }

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)
        // Outer green ring representing the elegant border
        drawCircle(
            color = ringColor,
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
        // Inner gold background circle
        drawCircle(
            color = bgColor,
            radius = radius - 3.dp.toPx(),
            center = center
        )
        // Concentric orange inner rings
        drawCircle(
            color = innerRingColor,
            radius = radius - 6.dp.toPx(),
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
        // Center blue emblem seal core
        drawCircle(
            color = coreColor,
            radius = radius - 10.dp.toPx(),
            center = center
        )
    }
}

@Composable
fun StyledQuestionText(text: String, fontSize: androidx.compose.ui.unit.TextUnit, isDarkMode: Boolean) {
    val annotatedString = remember(text) {
        androidx.compose.ui.text.buildAnnotatedString {
            var cursor = 0
            // Parses text inside single quotes 'something' or **something** to bold red
            val regex = "'([^']+)'|\\*\\*([^*]+)\\*\\*".toRegex()
            val matches = regex.findAll(text)
            for (match in matches) {
                val start = match.range.first
                val end = match.range.last + 1
                if (start > cursor) {
                    append(text.substring(cursor, start))
                }
                val word = match.groupValues[1].takeIf { it.isNotEmpty() } ?: match.groupValues[2]
                pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0XFFDC2626), fontWeight = FontWeight.Bold))
                append(word)
                pop()
                cursor = end
            }
            if (cursor < text.length) {
                append(text.substring(cursor))
            }
        }
    }
    Text(
        text = if (annotatedString.isNotEmpty()) annotatedString else androidx.compose.ui.text.AnnotatedString(text),
        color = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeArenaScreen(
    practiceSessionId: String = "practice-session-default",
    questions: List<Question> = emptyList(),
    title: String = "Practice Arena",
    durationMinutes: Int = 30,
    positiveMarks: Double = 1.0,
    negativeMarks: Double = 1.0,
    initialUserAnswers: Map<String, String> = emptyMap(),
    onAnswerSelected: (Map<String, String>) -> Unit = {},
    onSubmitTest: (Int, Int, Double, Map<String, String>, Int) -> Unit = { _, _, _, _, _ -> },
    isMockTest: Boolean = false,
    showCelebration: Boolean = false,
    initialBookmarkedIds: Set<String> = emptySet(),
    onToggleBookmark: (Question) -> Unit = {},
    onNavigateToHome: (() -> Unit)? = null,
    sectionType: PracticeSectionType? = null,
    initialQuestionIndex: Int = 0,
    preserveQuestionOrder: Boolean = false,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appConfig = LocalAppConfig.current
    val practiceScope = rememberCoroutineScope()
    var isDarkMode by remember { mutableStateOf(false) }
    var pendingExitDestination by remember { mutableStateOf("previous") }
    LaunchedEffect(Unit) {
        isDarkMode = false
    }
    val appSettings = appConfig.advancedFontSettings
    val pa = appSettings.practiceAppearance

    val currentSectionType = remember(sectionType, practiceSessionId, title) {
        sectionType ?: when {
            com.example.util.OfflineSyncManager.isOfflineModeSimulated.value ||
            practiceSessionId.contains("offline", ignoreCase = true) ||
            title.contains("offline", ignoreCase = true) -> PracticeSectionType.OFFLINE

            practiceSessionId.contains("current-affairs", ignoreCase = true) ||
            title.contains("Current Affairs", ignoreCase = true) -> PracticeSectionType.CURRENT_AFFAIRS

            practiceSessionId.startsWith("subtopic") ||
            title.contains("subtopic", ignoreCase = true) -> PracticeSectionType.SUBTOPICS

            else -> PracticeSectionType.SETS
        }
    }
    val activeBurstEffect = remember(pa, currentSectionType) {
        pa.getEffectForSection(currentSectionType)
    }

    val savedSession = remember(practiceSessionId) {
        val s = PracticeSessionManager.getSession(context, practiceSessionId)
        if (s != null && s.isCompleted) {
            PracticeSessionManager.deleteSession(context, practiceSessionId)
            null
        } else {
            s
        }
    }

    if (questions.isEmpty()) {
        Surface(modifier = Modifier.fillMaxSize(), color = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No questions available in this set.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack) {
                        Text("Go Back")
                    }
                }
            }
        }
        return
    }

    val restoredQuestions = remember(questions, preserveQuestionOrder) {
        if (preserveQuestionOrder) {
            questions
        } else {
            // Highly randomize questions and multiple choice options with cryptographic multi-pass shuffle
            questions.map { it.shuffledOptions() }.highlyShuffled()
        }
    }

    var activeQuestions by remember(restoredQuestions) {
        mutableStateOf(
            restoredQuestions.ifEmpty {
                if (preserveQuestionOrder) questions else questions.map { it.shuffledOptions() }.highlyShuffled()
            }
        )
    }
    var currentIndex by remember(practiceSessionId, initialQuestionIndex, activeQuestions.size) {
        mutableStateOf(initialQuestionIndex.coerceIn(0, (activeQuestions.size - 1).coerceAtLeast(0)))
    }
    var isShuffled by remember(practiceSessionId) { mutableStateOf(!preserveQuestionOrder) }
    val userAnswers = remember(practiceSessionId) {
        mutableStateMapOf<String, String>().apply {
            putAll(initialUserAnswers)
        }
    }
    var markedForReviewIds by remember(practiceSessionId) { mutableStateOf(setOf<String>()) }
    var bookmarkedQuestionIds by remember(initialBookmarkedIds, practiceSessionId) { mutableStateOf(initialBookmarkedIds) }
    var visitedIndices by remember(practiceSessionId) { mutableStateOf(setOf(currentIndex)) }
    var showExitDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var correctAnswerTrigger by remember { mutableStateOf(0) }
    var isInputReady by remember(practiceSessionId) { mutableStateOf(false) }

    LaunchedEffect(practiceSessionId) {
        delay(300L)
        isInputReady = true
    }

    // Immediately reset and clear burst animation effect whenever current question changes (Next / Previous / Palette)
    LaunchedEffect(currentIndex) {
        correctAnswerTrigger = 0
    }

    fun shuffleQuestions() {
        if (activeQuestions.size > 1) {
            // Thoroughly re-randomize both question sequence and option order with maximum entropy
            activeQuestions = activeQuestions.map { it.shuffledOptions() }.highlyShuffled()
            currentIndex = 0
            visitedIndices = setOf(0)
            isShuffled = true
            Toast.makeText(context, "🔀 Questions & options deeply randomized!", Toast.LENGTH_SHORT).show()
        }
    }

    var questionFontSizeScale by remember { mutableStateOf(1.0f) }
    var autoShowExplanation by remember { mutableStateOf(true) }
    var showScientificCalculator by remember { mutableStateOf(false) }
    var showFlagDialog by remember { mutableStateOf<Question?>(null) }
    var showAppearanceSettings by remember { mutableStateOf(false) }

    var tts by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
    DisposableEffect(context) {
        var ttsInstance: android.speech.tts.TextToSpeech? = null
        ttsInstance = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                ttsInstance?.language = java.util.Locale.getDefault()
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        }
    }

    fun speakText(text: String) {
        try {
            tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    BackHandler {
        pendingExitDestination = "previous"
        showExitDialog = true
    }

    val view = LocalView.current
    val performSelectionHaptic = {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        Unit
    }

    val performResultHaptic = { isCorrect: Boolean ->
        if (isCorrect) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        Unit
    }
    val totalTimeSeconds = (durationMinutes * 60).coerceAtLeast(30)
    val sessionStartTimestamp = remember { System.currentTimeMillis() }
    val accumulatedElapsedTime = remember(savedSession) { savedSession?.elapsedTime ?: 0L }
    var secondsDelta by remember { mutableStateOf(0L) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            val localStart = System.currentTimeMillis() - (secondsDelta * 1000)
            while (isTimerRunning) {
                delay(1000L)
                val nextDelta = (System.currentTimeMillis() - localStart) / 1000
                if (nextDelta != secondsDelta) {
                    secondsDelta = nextDelta
                }
            }
        }
    }

    // Autosave session on changes
    LaunchedEffect(currentIndex, userAnswers.size, markedForReviewIds, activeQuestions) {
        if (!isMockTest && activeQuestions.isNotEmpty()) {
            val currentElapsed = (accumulatedElapsedTime + secondsDelta).coerceAtLeast(0L)
            val updatedSession = PracticeSession(
                sessionId = practiceSessionId,
                startTimestamp = sessionStartTimestamp,
                elapsedTime = currentElapsed,
                questionIdsOrder = activeQuestions.map { it.id },
                currentQuestionIndex = currentIndex,
                userAnswers = userAnswers.toMap(),
                markedForReviewIds = markedForReviewIds,
                isCompleted = false
            )
            PracticeSessionManager.saveSession(context, updatedSession)
        }
    }

    val currentQuestion = activeQuestions.getOrNull(currentIndex) ?: activeQuestions[0]
    val currentSelectedOption = userAnswers[currentQuestion.id]
    val isMarkedForReview = markedForReviewIds.contains(currentQuestion.id)
    val isSaved = currentSelectedOption != null

    // Submission handler
    fun triggerSubmit() {
        if (isSubmitting) return
        isSubmitting = true
        var correct = 0
        var wrong = 0
        var score = 0.0
        activeQuestions.forEach { q ->
            val ans = userAnswers[q.id]
            if (ans != null) {
                if (ans.equals(q.correctOption, ignoreCase = true)) {
                    correct++
                    score += positiveMarks
                } else {
                    wrong++
                    score -= negativeMarks
                }
            }
        }
        val currentElapsed = (accumulatedElapsedTime + secondsDelta).coerceAtLeast(0L)
        val elapsedSeconds = currentElapsed.coerceAtLeast(1L).toInt()

        if (!isMockTest) {
            val completedSession = PracticeSession(
                sessionId = practiceSessionId,
                startTimestamp = sessionStartTimestamp,
                elapsedTime = currentElapsed,
                questionIdsOrder = activeQuestions.map { it.id },
                currentQuestionIndex = currentIndex,
                userAnswers = userAnswers.toMap(),
                markedForReviewIds = markedForReviewIds,
                completedTimestamp = System.currentTimeMillis(),
                isCompleted = true
            )
            PracticeSessionManager.saveSession(context, completedSession)
        }

        onSubmitTest(
            correct,
            wrong,
            score,
            userAnswers,
            elapsedSeconds
        )
    }

    LaunchedEffect(isTimerRunning, totalTimeSeconds, accumulatedElapsedTime) {
        while (isTimerRunning) {
            val currentElapsed = (accumulatedElapsedTime + secondsDelta).coerceAtLeast(0L)
            val rem = (totalTimeSeconds - currentElapsed).coerceAtLeast(0L).toInt()
            if (rem <= 0) {
                isTimerRunning = false
                Toast.makeText(context, "Time is up! Submitting...", Toast.LENGTH_SHORT).show()
                triggerSubmit()
                break
            }
            delay(1000L)
        }
    }

    val windowSizeClass = LocalWindowSizeClass.current
    val isTabletLayout = windowSizeClass == WindowWidthSizeClass.Expanded || 
                         windowSizeClass == WindowWidthSizeClass.Medium

    AppLuminousBackground(screenName = "Practice Arena Screen") {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                // Floating Card Header with 2 clean rows
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, Lavender.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Row 1: Primary Controls Header (Back, Title, Actions)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Two Back Buttons (Home + Previous Sets) + Subtopic Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // 1. Back Button: Return to immediately previous Sets screen
                                PremiumBackButton(
                                    onClick = {
                                        pendingExitDestination = "previous"
                                        showExitDialog = true
                                    },
                                    size = 36,
                                    iconSize = 18
                                )

                                // 2. Direct Home Shortcut
                                if (onNavigateToHome != null) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isDarkMode) Color(0xFF334155) else Color.White,
                                        shadowElevation = 2.dp,
                                        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1)),
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clickable {
                                                pendingExitDestination = "home"
                                                showExitDialog = true
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = "Go to Home",
                                                tint = Color(0xFF2563EB),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Right Tools: Bookmark, Palette, Theme Toggle, Submit Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Bookmark Button
                                val isBookmarked = activeQuestions.getOrNull(currentIndex)?.id in bookmarkedQuestionIds
                                Surface(
                                    shape = CircleShape,
                                    color = if (isBookmarked) Color(0xFFFEF3C7) else if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isBookmarked) Color(0xFFF59E0B) else if (isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1)),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable {
                                            val currentQ = activeQuestions.getOrNull(currentIndex)
                                            if (currentQ != null) {
                                                onToggleBookmark(currentQ)
                                                if (isBookmarked) {
                                                    bookmarkedQuestionIds = bookmarkedQuestionIds - currentQ.id
                                                    Toast.makeText(context, "Bookmark removed", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    bookmarkedQuestionIds = bookmarkedQuestionIds + currentQ.id
                                                    Toast.makeText(context, "Question saved to Room Database!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Bookmark Question",
                                            tint = if (isBookmarked) Color(0xFFD97706) else if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }

                                // Appearance Customization Palette Button
                                Surface(
                                    shape = CircleShape,
                                    color = if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1)),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable { showAppearanceSettings = true }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = "Appearance Settings",
                                            tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF2563EB),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }

                                // Dark / Light Theme Toggle Button
                                Surface(
                                    shape = CircleShape,
                                    color = if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1)),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable { isDarkMode = !isDarkMode }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = "Toggle Dark/Light Mode",
                                            tint = if (isDarkMode) Color(0xFFFDE047) else Color(0xFF475569),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }

                                // Submit Exam Button
                                Button(
                                    onClick = { triggerSubmit() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = "Submit",
                                        color = Color.Black,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = if (isDarkMode) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0),
                            thickness = 0.8.dp
                        )

                        // Row 2: Exam Metrics & Practice Controls Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val currentTotalElapsed = (accumulatedElapsedTime + secondsDelta).coerceAtLeast(0L)
                            val remainingSeconds = (totalTimeSeconds - currentTotalElapsed).coerceAtLeast(0L).toInt()
                            // Timer Pill (Interactive Play/Pause on Tap)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (remainingSeconds < 120 && remainingSeconds > 0) Color(0xFFFEE2E2) else if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (remainingSeconds < 120 && remainingSeconds > 0) Color(0xFFEF4444) else if (isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable { isTimerRunning = !isTimerRunning }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isTimerRunning) Icons.Outlined.AccessTime else Icons.Default.PlayArrow,
                                        contentDescription = "Timer",
                                        tint = if (remainingSeconds < 120 && remainingSeconds > 0) Color(0xFFDC2626) else if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF2563EB),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = String.format("%02d:%02d:%02d", remainingSeconds / 3600, (remainingSeconds % 3600) / 60, remainingSeconds % 60),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (remainingSeconds < 120 && remainingSeconds > 0) Color(0xFF991B1B) else if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                                    )
                                }
                            }

                            // Progress Count Pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FormatListNumbered,
                                        contentDescription = "Progress",
                                        tint = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "${currentIndex + 1}/${activeQuestions.size}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                                    )
                                }
                            }

                            // Shuffle Pill Icon (Text removed)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isShuffled) Color(0xFFEEF2FF) else if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (isShuffled) Color(0xFF6366F1) else if (isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable { shuffleQuestions() }
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "Randomize questions",
                                        tint = if (isShuffled) Color(0xFF4F46E5) else if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // Marks Pill (+1.0 & -0.25)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "+${positiveMarks}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color(0xFF16A34A)
                                    )
                                    Text(
                                        text = "/",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "-${negativeMarks}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
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
            val isWide = isTabletLayout || maxWidth >= 600.dp
            
            Column(modifier = Modifier.fillMaxSize()) {

                // Main Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (isMockTest) {
                        if (isWide) {
                            // Desktop side-by-side view exactly like the reference for Mock Tests
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Left Question Details (65%)
                                val tabletScrollState = rememberScrollState()
                                LaunchedEffect(currentIndex) {
                                    tabletScrollState.scrollTo(0)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(0.65f)
                                        .fillMaxHeight()
                                        .verticalScroll(tabletScrollState)
                                ) {
                                    AnimatedContent(
                                        targetState = currentIndex,
                                        transitionSpec = {
                                            val isForward = targetState >= initialState
                                            val dir = if (isForward) 1 else -1
                                            (slideInHorizontally(
                                                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                                            ) { fullWidth -> dir * (fullWidth * 0.35f).toInt() } + fadeIn(
                                                animationSpec = tween(durationMillis = 200)
                                            )) togetherWith (slideOutHorizontally(
                                                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                                            ) { fullWidth -> -dir * (fullWidth * 0.25f).toInt() } + fadeOut(
                                                animationSpec = tween(durationMillis = 180)
                                            ))
                                        },
                                        label = "MockTestTabletQuestionTransition"
                                    ) { targetIndex ->
                                        val q = activeQuestions.getOrNull(targetIndex) ?: currentQuestion
                                        val targetSelectedOption = userAnswers[q.id]
                                        val targetIsMarked = markedForReviewIds.contains(q.id)
                                        QuestionDetailPanel(
                                            currentQuestion = q,
                                            currentIndex = targetIndex,
                                            currentSelectedOption = targetSelectedOption,
                                            isMarkedForReview = targetIsMarked,
                                            questionFontSizeScale = questionFontSizeScale,
                                            autoShowExplanation = autoShowExplanation,
                                            isMockTest = isMockTest,
                                            onOptionSelected = { key ->
                                                val wasAlreadyAnswered = userAnswers.containsKey(q.id)
                                                if (isMockTest || !wasAlreadyAnswered) {
                                                    userAnswers[q.id] = key
                                                    onAnswerSelected(userAnswers.toMap())
                                                    val isCorrect = key.equals(q.correctOption, ignoreCase = true)
                                                    performResultHaptic(isCorrect)
                                                    if (!isMockTest) {
                                                        if (!wasAlreadyAnswered) {
                                                            practiceScope.launch {
                                                                com.example.repository.UserProgressRepository.getInstance(context)
                                                                    .recordQuestionAnswered(appConfig.appId, isCorrect)
                                                            }
                                                        }
                                                        if (isCorrect) {
                                                            correctAnswerTrigger++
                                                        }
                                                    }
                                                }
                                            },
                                            onHapticSelection = performSelectionHaptic,
                                            onToggleReview = {
                                                markedForReviewIds = if (targetIsMarked) {
                                                    markedForReviewIds - q.id
                                                } else {
                                                    markedForReviewIds + q.id
                                                }
                                            },
                                            onFlagQuestion = { flgQ -> showFlagDialog = flgQ },
                                            speakText = ::speakText,
                                            onBack = onBack,
                                            isDarkMode = isDarkMode
                                        )
                                    }
                                }

                                // Right Questions list palette (35%)
                                Box(modifier = Modifier.weight(0.35f)) {
                                    QuestionsPalettePanel(
                                        questions = activeQuestions,
                                        currentIndex = currentIndex,
                                        userAnswers = userAnswers,
                                        markedForReviewIds = markedForReviewIds,
                                        visitedIndices = visitedIndices,
                                        onQuestionSelected = { idx ->
                                            currentIndex = idx
                                            visitedIndices = visitedIndices + idx
                                        },
                                        onSubmitClick = { triggerSubmit() }
                                    )
                                }
                            }
                        } else {
                            // Mobile View for Mock Tests: Stacks the panes or allows viewing palette via scroll
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AnimatedContent(
                                    targetState = currentIndex,
                                    transitionSpec = {
                                        val isForward = targetState >= initialState
                                        val dir = if (isForward) 1 else -1
                                        (slideInHorizontally(
                                            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                                        ) { fullWidth -> dir * (fullWidth * 0.35f).toInt() } + fadeIn(
                                            animationSpec = tween(durationMillis = 200)
                                        )) togetherWith (slideOutHorizontally(
                                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                                        ) { fullWidth -> -dir * (fullWidth * 0.25f).toInt() } + fadeOut(
                                            animationSpec = tween(durationMillis = 180)
                                        ))
                                    },
                                    label = "MockTestMobileQuestionTransition"
                                ) { targetIndex ->
                                    val q = activeQuestions.getOrNull(targetIndex) ?: currentQuestion
                                    val targetSelectedOption = userAnswers[q.id]
                                    val targetIsMarked = markedForReviewIds.contains(q.id)
                                    QuestionDetailPanel(
                                        currentQuestion = q,
                                        currentIndex = targetIndex,
                                        currentSelectedOption = targetSelectedOption,
                                        isMarkedForReview = targetIsMarked,
                                        questionFontSizeScale = questionFontSizeScale,
                                        autoShowExplanation = autoShowExplanation,
                                        isMockTest = isMockTest,
                                        isInputReady = isInputReady,
                                        onOptionSelected = { key ->
                                            if (!isInputReady) return@QuestionDetailPanel
                                            val wasAlreadyAnswered = userAnswers.containsKey(q.id)
                                            if (isMockTest || !wasAlreadyAnswered) {
                                                userAnswers[q.id] = key
                                                onAnswerSelected(userAnswers.toMap())
                                                val isCorrect = key.equals(q.correctOption, ignoreCase = true)
                                                performResultHaptic(isCorrect)
                                                if (!isMockTest) {
                                                    if (!wasAlreadyAnswered) {
                                                        practiceScope.launch {
                                                            com.example.repository.UserProgressRepository.getInstance(context)
                                                                .recordQuestionAnswered(appConfig.appId, isCorrect)
                                                        }
                                                    }
                                                    if (isCorrect) {
                                                        correctAnswerTrigger++
                                                    }
                                                }
                                            }
                                        },
                                        onToggleReview = {
                                            markedForReviewIds = if (targetIsMarked) {
                                                markedForReviewIds - q.id
                                            } else {
                                                markedForReviewIds + q.id
                                            }
                                        },
                                        onFlagQuestion = { flgQ -> showFlagDialog = flgQ },
                                        speakText = ::speakText,
                                        onBack = onBack,
                                        onHapticSelection = performSelectionHaptic,
                                        isDarkMode = isDarkMode
                                    )
                                }
                                
                                QuestionsPalettePanel(
                                    questions = activeQuestions,
                                    currentIndex = currentIndex,
                                    userAnswers = userAnswers,
                                    markedForReviewIds = markedForReviewIds,
                                    visitedIndices = visitedIndices,
                                    onQuestionSelected = { idx ->
                                        currentIndex = idx
                                        visitedIndices = visitedIndices + idx
                                    },
                                    onSubmitClick = { triggerSubmit() }
                                )
                            }
                        }
                    } else {
                        // Standard MCQ Practice Screen: Centered, clean single card view without palette/matrix
                        val scrollState = rememberScrollState()
                        LaunchedEffect(currentIndex) {
                            scrollState.scrollTo(0)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 680.dp)
                                    .verticalScroll(scrollState),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AnimatedContent(
                                    targetState = currentIndex,
                                    transitionSpec = {
                                        val isForward = targetState >= initialState
                                        val dir = if (isForward) 1 else -1
                                        (slideInHorizontally(
                                            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                                        ) { fullWidth -> dir * (fullWidth * 0.35f).toInt() } + fadeIn(
                                            animationSpec = tween(durationMillis = 200)
                                        )) togetherWith (slideOutHorizontally(
                                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                                        ) { fullWidth -> -dir * (fullWidth * 0.25f).toInt() } + fadeOut(
                                            animationSpec = tween(durationMillis = 180)
                                        ))
                                    },
                                    label = "StandardMCQQuestionTransition"
                                ) { targetIndex ->
                                    val q = activeQuestions.getOrNull(targetIndex) ?: currentQuestion
                                    val targetSelectedOption = userAnswers[q.id]
                                    val targetIsMarked = markedForReviewIds.contains(q.id)
                                    QuestionDetailPanel(
                                        currentQuestion = q,
                                        currentIndex = targetIndex,
                                        currentSelectedOption = targetSelectedOption,
                                        isMarkedForReview = targetIsMarked,
                                        questionFontSizeScale = questionFontSizeScale,
                                        autoShowExplanation = autoShowExplanation,
                                        isMockTest = isMockTest,
                                        isInputReady = isInputReady,
                                        onOptionSelected = { key ->
                                            if (!isInputReady) return@QuestionDetailPanel
                                            val wasAlreadyAnswered = userAnswers.containsKey(q.id)
                                            if (isMockTest || !wasAlreadyAnswered) {
                                                userAnswers[q.id] = key
                                                onAnswerSelected(userAnswers.toMap())
                                                val isCorrect = key.equals(q.correctOption, ignoreCase = true)
                                                performResultHaptic(isCorrect)
                                                if (!isMockTest) {
                                                    if (!wasAlreadyAnswered) {
                                                        practiceScope.launch {
                                                            com.example.repository.UserProgressRepository.getInstance(context)
                                                                .recordQuestionAnswered(appConfig.appId, isCorrect)
                                                        }
                                                    }
                                                    if (isCorrect) {
                                                        correctAnswerTrigger++
                                                    }
                                                }
                                            }
                                        },
                                        onToggleReview = {
                                            markedForReviewIds = if (targetIsMarked) {
                                                markedForReviewIds - q.id
                                            } else {
                                                markedForReviewIds + q.id
                                            }
                                        },
                                        onFlagQuestion = { flgQ -> showFlagDialog = flgQ },
                                        speakText = ::speakText,
                                        onBack = onBack,
                                        onHapticSelection = performSelectionHaptic,
                                        isDarkMode = isDarkMode
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Action Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isDarkMode) Color(0xFF0F172A) else Color.White,
                    shadowElevation = 12.dp,
                    border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                ) {
                    if (isMockTest) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous Button
                            OutlinedButton(
                                onClick = {
                                    if (currentIndex > 0) {
                                        currentIndex--
                                        visitedIndices = visitedIndices + currentIndex
                                    }
                                },
                                enabled = currentIndex > 0,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                                    contentColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                    disabledContainerColor = if (isDarkMode) Color(0xFF0F172A).copy(alpha = 0.5f) else Color(0xFFF8FAFC),
                                    disabledContentColor = if (isDarkMode) Color(0xFF475569) else Color(0xFF94A3B8)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (currentIndex > 0) (if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1))
                                    else (if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Prev",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Mark for Review Button
                            OutlinedButton(
                                onClick = {
                                    markedForReviewIds = if (isMarkedForReview) {
                                        markedForReviewIds - currentQuestion.id
                                    } else {
                                        markedForReviewIds + currentQuestion.id
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(44.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isMarkedForReview) Color(0xFF7C3AED).copy(alpha = 0.15f) else if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                                    contentColor = if (isMarkedForReview) Color(0xFFA855F7) else if (isDarkMode) Color(0xFFC084FC) else Color(0xFF7C3AED)
                                ),
                                border = BorderStroke(1.dp, if (isMarkedForReview) Color(0xFFA855F7) else if (isDarkMode) Color(0xFF4C1D95) else Color(0xFFDDD6FE)),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMarkedForReview) Icons.Default.Flag else Icons.Outlined.Flag,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isMarkedForReview) "Unmark" else "Review",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Skip Button
                            OutlinedButton(
                                onClick = {
                                    if (currentIndex < activeQuestions.size - 1) {
                                        currentIndex++
                                        visitedIndices = visitedIndices + currentIndex
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFF7ED),
                                    contentColor = if (isDarkMode) Color(0xFFFB923C) else Color(0xFFEA580C)
                                ),
                                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF9A3412) else Color(0xFFFFEDD5)),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.DoubleArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Skip",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Save & Next Button
                            Button(
                                onClick = {
                                    if (currentIndex < activeQuestions.size - 1) {
                                        currentIndex++
                                        visitedIndices = visitedIndices + currentIndex
                                    } else {
                                        triggerSubmit()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = if (currentIndex < activeQuestions.size - 1) "Save & Next" else "Submit",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.5.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = if (currentIndex < activeQuestions.size - 1) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    } else {
                        // MCQ Practice: Balanced Previous and Save & Next layout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous Button
                            OutlinedButton(
                                onClick = {
                                    if (currentIndex > 0) {
                                        currentIndex--
                                        visitedIndices = visitedIndices + currentIndex
                                    }
                                },
                                enabled = currentIndex > 0,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                    contentColor = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                                    disabledContainerColor = if (isDarkMode) Color(0xFF0F172A).copy(alpha = 0.5f) else Color(0xFFF8FAFC),
                                    disabledContentColor = if (isDarkMode) Color(0xFF475569) else Color(0xFF94A3B8)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (currentIndex > 0) (if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1))
                                    else (if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Previous",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Save & Next Button
                            Button(
                                onClick = {
                                    if (currentIndex < activeQuestions.size - 1) {
                                        currentIndex++
                                        visitedIndices = visitedIndices + currentIndex
                                    } else {
                                        triggerSubmit()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = if (currentIndex < activeQuestions.size - 1) "Save & Next" else "Submit",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (currentIndex < activeQuestions.size - 1) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Floating Scientific Calculator Icon
            val isMathsPractice = title.contains("Math", ignoreCase = true) || 
                                 title.contains("Arithmetic", ignoreCase = true) ||
                                 title.contains("Ability", ignoreCase = true) ||
                                 title.contains("Reasoning", ignoreCase = true)

            if (appConfig.isScientificCalculatorEnabled && isMathsPractice) {
                var offset by remember { mutableStateOf(Offset(0f, 0f)) }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp, end = 16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Surface(
                        modifier = Modifier
                            .offset { androidx.compose.ui.unit.IntOffset(offset.x.toInt(), offset.y.toInt()) }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    offset += dragAmount
                                }
                            }
                            .size(56.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .clickable { showScientificCalculator = true },
                        color = Color(0xFF2563EB)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Scientific Calculator",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Premium Success Burst Animation for Correct Answer (Sets, Subtopics, Current Affairs, Offline)
            if (!isMockTest) {
                CorrectAnswerBurstEffect(
                    trigger = correctAnswerTrigger,
                    effectName = activeBurstEffect,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showAppearanceSettings) {
        ModalBottomSheet(
            onDismissRequest = { showAppearanceSettings = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            PracticeAppearancePanel(
                currentSettings = appConfig.advancedFontSettings.practiceAppearance,
                onSave = { newPracticeSettings ->
                    val updatedAdvanced = appConfig.advancedFontSettings.copy(
                        practiceAppearance = newPracticeSettings
                    )
                    FontSettingsManager.saveSettings(context, updatedAdvanced)
                    com.example.util.PracticeAppearanceManager.saveSettings(context, newPracticeSettings)
                    
                    val currentConfig = AppConfigRepository.getInstance().getCurrentConfig()
                    AppConfigRepository.getInstance().updateAppConfig(
                        currentConfig.copy(advancedFontSettings = updatedAdvanced)
                    )
                    showAppearanceSettings = false
                },
                onReset = {
                    com.example.util.PracticeAppearanceManager.resetToDefault(context)
                    val defaultPractice = com.example.model.PracticeAppearanceSettings()
                    val updatedAdvanced = appConfig.advancedFontSettings.copy(
                        practiceAppearance = defaultPractice
                    )
                    FontSettingsManager.saveSettings(context, updatedAdvanced)
                    
                    val currentConfig = AppConfigRepository.getInstance().getCurrentConfig()
                    AppConfigRepository.getInstance().updateAppConfig(
                        currentConfig.copy(advancedFontSettings = updatedAdvanced)
                    )
                },
                onBack = { showAppearanceSettings = false }
            )
        }
    }

    if (showFlagDialog != null) {
        val question = showFlagDialog!!
        FlagQuestionDialog(
            question = question,
            onDismiss = { showFlagDialog = null },
            onSubmitReport = { reason ->
                val report = com.example.model.QuestionReportModel(
                    questionId = question.id,
                    subjectName = com.example.data.SampleData.getSubjectName(question.subjectId),
                    unitName = com.example.data.SampleData.getUnitName(question.subjectId, question.unitId),
                    topicName = com.example.data.SampleData.getTopicName(question.subjectId, question.unitId, question.topicId),
                    questionText = question.questionText,
                    options = question.options,
                    correctOption = question.correctOption,
                    reportReason = reason,
                    reporterEmail = com.example.data.SampleData.sampleUserProfile.email
                )
                com.example.data.SampleData.questionReportsState.add(report)
                
                // Simulate notification to Admin Login Email
                val adminEmail = "sobhadhanaraju0@gmail.com"
                Toast.makeText(context, "Report submitted! Notification sent to $adminEmail", Toast.LENGTH_LONG).show()
                showFlagDialog = null
            }
        )
    }

    if (showScientificCalculator) {
        ScientificCalculatorDialog(onDismiss = { showScientificCalculator = false })
    }

    // Exit confirmation Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White,
            title = {
                Text(
                    "Exit Practice Arena?",
                    fontWeight = FontWeight.Bold,
                    fontSize = LocalAppConfig.current.advancedFontSettings.questionFontSize.sp,
                    color = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    "You have answered ${userAnswers.size} out of ${questions.size} questions. Would you like to submit your answers now or leave?",
                    fontSize = LocalAppConfig.current.advancedFontSettings.explanationFontSize.sp,
                    color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        triggerSubmit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0XFF2563EB)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Submit Test")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showExitDialog = false
                        if (pendingExitDestination == "home" && onNavigateToHome != null) {
                            onNavigateToHome()
                        } else {
                            onBack()
                        }
                    }) {
                        Text("Exit", color = Color(0XFFE11D48))
                    }
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Cancel", color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B))
                    }
                }
            }
        )
    }
    }
}

@Composable
fun QuestionDetailPanel(
    currentQuestion: Question,
    currentIndex: Int,
    currentSelectedOption: String?,
    isMarkedForReview: Boolean,
    questionFontSizeScale: Float = 1.0f,
    autoShowExplanation: Boolean = true,
    isMockTest: Boolean = false,
    isInputReady: Boolean = true,
    onOptionSelected: (String) -> Unit,
    onHapticSelection: () -> Unit,
    onToggleReview: () -> Unit,
    onFlagQuestion: (Question) -> Unit = {},
    speakText: (String) -> Unit = {},
    onBack: () -> Unit = {},
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val appConfig = LocalAppConfig.current
    val appSettings = appConfig.advancedFontSettings
    val pa = appSettings.practiceAppearance
    val isSets = currentQuestion.setName.isNotBlank()
    
    val containerBg = if (isDarkMode) {
        Color(0xFF1E293B)
    } else if (isMockTest) {
        Color.White
    } else {
        Color(pa.screenBgColor)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0XFFF3E8FF)) // Light lavender border
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Multi-color thin gradient strip at the top border of the card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0XFF3B82F6), // Blue
                                Color(0XFF8B5CF6), // Purple
                                Color(0XFFEC4899), // Pink
                                Color(0XFFF59E0B)  // Orange
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 18.dp)
            ) {
                // Question Header Card
                val qCardBg = if (isDarkMode) {
                    Color(pa.questionBgColor)
                } else {
                    val isBgDark = pa.questionBgColor == 0xFF1E3A8AL || pa.questionBgColor == 0xFF0F172AL || pa.questionBgColor == 0xFF312E81L
                    if (isBgDark) Color(0xFFFEF6E1) else Color(pa.questionBgColor)
                }
                val qCardTextColor = if (isDarkMode) {
                    Color(pa.questionTextColor)
                } else {
                    val isBgDark = pa.questionBgColor == 0xFF1E3A8AL || pa.questionBgColor == 0xFF0F172AL || pa.questionBgColor == 0xFF312E81L
                    if (isBgDark) Color(0xFF0C192E) else Color(pa.questionTextColor)
                }
                val qCardBorderColor = if (isDarkMode) {
                    Color(pa.questionBorderColor)
                } else {
                    val isBgDark = pa.questionBgColor == 0xFF1E3A8AL || pa.questionBgColor == 0xFF0F172AL || pa.questionBgColor == 0xFF312E81L
                    if (isBgDark) Color(0xFFD97706) else Color(pa.questionBorderColor)
                }

                Surface(
                    shape = RoundedCornerShape(pa.questionCornerRadius.dp),
                    color = qCardBg,
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(qCardBorderColor, qCardBorderColor.copy(alpha = 0.6f), Color(0xFFFDE68A)))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .premiumTargetAnimation(
                            target = com.example.model.AnimatableTarget.MCQ_QUESTION_CARDS,
                            screenKey = "Practice Arena",
                            visible = true,
                            index = 0
                        ),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Orange "Q" badge matching reference image
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFF9800), Color(0xFFF57C00))
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Q",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(30.dp)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.4f))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        MathAndHighlightText(
                            text = currentQuestion.questionText,
                            highlights = currentQuestion.questionHighlights,
                            location = if (isSets) HighlightLocation.SETS_QUESTION else HighlightLocation.SUBTOPICS_QUESTION,
                            highlightConfig = LocalAppConfig.current.questionHighlightConfig,
                            color = qCardTextColor,
                            fontSize = (pa.questionFontSize * questionFontSizeScale).sp,
                            fontFamily = getFontFamily(pa.questionFontFamily),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Options List
                val optionKeys = remember { listOf("A", "B", "C", "D") }
                val hasAnswered = currentSelectedOption != null

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    optionKeys.forEach { key ->
                        val optionText = currentQuestion.options[key] ?: ""
                        if (optionText.isNotEmpty()) {
                            val isCorrectOption = key.equals(currentQuestion.correctOption, ignoreCase = true)
                            val isUserSelection = currentSelectedOption == key

                            // High contrast colors ensuring unselected options remain 100% visible and readable
                            val defaultOptionBg = if (isDarkMode) Color(0xFF1E293B) else Color(pa.optionBgColor)
                            val defaultOptionText = if (isDarkMode) Color(0xFFF1F5F9) else Color(pa.optionTextColor)
                            val defaultBorder = if (isDarkMode) Color(0xFF475569) else Color(pa.optionBorderColor)

                            val targetBgColor = when {
                                !hasAnswered -> defaultOptionBg
                                isMockTest && isUserSelection -> if (isDarkMode) Color(0xFF1E3A8A) else Color(0xFFEFF6FF)
                                isMockTest && !isUserSelection -> defaultOptionBg
                                isCorrectOption -> if (isDarkMode) Color(0xFF064E3B) else Color(0xFFDCFCE7)
                                isUserSelection -> if (isDarkMode) Color(0xFF7F1D1D) else Color(0xFFFEE2E2)
                                else -> defaultOptionBg
                            }

                            val targetBorderColor = when {
                                isMockTest && isUserSelection -> if (isDarkMode) Color(0xFF3B82F6) else Color(0xFF2563EB)
                                isMockTest && !isUserSelection -> defaultBorder
                                !hasAnswered -> defaultBorder
                                isCorrectOption -> if (isDarkMode) Color(0xFF10B981) else Color(0xFF16A34A)
                                isUserSelection -> if (isDarkMode) Color(0xFFEF4444) else Color(0xFFDC2626)
                                else -> defaultBorder
                            }

                            val targetTextColor = when {
                                isMockTest && isUserSelection -> if (isDarkMode) Color.White else Color(0xFF1E40AF)
                                isMockTest && !isUserSelection -> defaultOptionText
                                !hasAnswered -> defaultOptionText
                                isCorrectOption -> if (isDarkMode) Color(0xFFECFDF5) else Color(0xFF14532D)
                                isUserSelection -> if (isDarkMode) Color(0xFFFEF2F2) else Color(0xFF991B1B)
                                else -> defaultOptionText // 100% opacity, crisp & readable
                            }

                            val targetBorderWidth = when {
                                isMockTest && isUserSelection -> 2.dp
                                !isMockTest && hasAnswered && (isCorrectOption || isUserSelection) -> 2.dp
                                else -> pa.optionBorderWidth.dp
                            }

                            val rightIcon = when {
                                isMockTest -> null
                                !hasAnswered -> null
                                isCorrectOption -> Icons.Default.Check
                                isUserSelection -> Icons.Default.Close
                                else -> null
                            }

                            // Smooth color transitions for options without layout shifts or dimension changes
                            val animatedBgColor by animateColorAsState(targetValue = targetBgColor, animationSpec = tween(220), label = "BgAnim")
                            val animatedBorderColor by animateColorAsState(targetValue = targetBorderColor, animationSpec = tween(220), label = "BorderAnim")
                            val animatedTextColor by animateColorAsState(targetValue = targetTextColor, animationSpec = tween(220), label = "TextAnim")
                            val fixedBorderWidth = pa.optionBorderWidth.dp.coerceAtLeast(1.5.dp)

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    shape = RoundedCornerShape(pa.optionCornerRadius.dp),
                                    color = animatedBgColor,
                                    border = BorderStroke(
                                        width = fixedBorderWidth,
                                        brush = Brush.linearGradient(listOf(animatedBorderColor, animatedBorderColor.copy(alpha = 0.85f), Color(0xFFE2E8F0)))
                                    ),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            enabled = isInputReady && (isMockTest || !hasAnswered),
                                            interactionSource = remember(currentQuestion.id, key) { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                            indication = ripple()
                                        ) {
                                            if (isInputReady && (isMockTest || !hasAnswered)) {
                                                onHapticSelection()
                                                onOptionSelected(key)
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Option Badge (A, B, C, D)
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    when {
                                                        !isMockTest && hasAnswered && isCorrectOption -> Brush.linearGradient(listOf(Color(0xFF16A34A), Color(0xFF15803D)))
                                                        !isMockTest && hasAnswered && isUserSelection -> Brush.linearGradient(listOf(Color(0xFFDC2626), Color(0xFFB91C1C)))
                                                        isMockTest && isUserSelection -> Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)))
                                                        else -> Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)))
                                                    },
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = key,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Vertical Divider
                                        Box(
                                            modifier = Modifier
                                                .width(1.5.dp)
                                                .height(28.dp)
                                                .background(Color(0xFF38BDF8).copy(alpha = 0.4f))
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Box(modifier = Modifier.weight(1f)) {
                                            MathAndHighlightText(
                                                text = optionText,
                                                highlights = emptyList(),
                                                location = if (isSets) HighlightLocation.SETS_OPTION else HighlightLocation.SUBTOPICS_OPTION,
                                                highlightConfig = LocalAppConfig.current.questionHighlightConfig,
                                                fontSize = pa.optionFontSize.sp,
                                                fontFamily = getFontFamily(pa.optionFontFamily),
                                                fontWeight = FontWeight.Bold,
                                                color = animatedTextColor,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Right indicator matching reference in fixed 26.dp container
                                        Box(
                                            modifier = Modifier.size(26.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (rightIcon != null) {
                                                if (isCorrectOption && !isMockTest) {
                                                    com.example.ui.components.ElegantAnimatedCheckmark(
                                                        visible = true,
                                                        size = 26.dp,
                                                        badgeColor = Color(0xFF16A34A),
                                                        checkmarkColor = Color.White
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(26.dp)
                                                            .background(
                                                                Color(0xFFDC2626),
                                                                CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = rightIcon,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .border(2.dp, Color(0xFF0284C7), CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }

                                
                            }
                        }
                    }
                }

                // Explanation Box: Visible only if user has answered the question and a valid explanation exists
                val hasValidExplanation = !currentQuestion.explanation.isNullOrBlank() &&
                        currentQuestion.explanation?.trim()?.lowercase() != "no explanation available for this question." &&
                        currentQuestion.explanation?.trim()?.lowercase() != "no explanation available."

                if (!isMockTest && hasAnswered && autoShowExplanation && hasValidExplanation) {
                    Spacer(modifier = Modifier.height(20.dp))

                    var isExplanationVisible by remember { mutableStateOf(true) }

                    Surface(
                        shape = RoundedCornerShape(pa.explanationCornerRadius.dp),
                        color = Color(pa.explanationBgColor),
                        border = BorderStroke(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFFF59E0B)))
                        ),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .premiumTargetAnimation(
                                target = com.example.model.AnimatableTarget.EXPLANATION_PANEL,
                                screenKey = "Practice Arena",
                                visible = true,
                                index = 0
                            )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Orange document/bulb badge
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFFFF9800), Color(0xFFF57C00))
                                                ),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Lightbulb,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Vertical Divider
                                    Box(
                                        modifier = Modifier
                                            .width(1.5.dp)
                                            .height(28.dp)
                                            .background(Color(0xFFF59E0B).copy(alpha = 0.4f))
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = "Explanation:",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        fontFamily = getFontFamily(pa.explanationFontFamily),
                                        color = if (isDarkMode) Color(pa.explanationTextColor) else Color(0xFFEA580C)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Listen button
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.clickable {
                                            val explanationToSpeak = currentQuestion.explanation ?: "Explanation."
                                            speakText(explanationToSpeak)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Listen Explanation",
                                            tint = Color(pa.explanationTextColor),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Listen",
                                            fontSize = (pa.explanationFontSize - 1).coerceAtLeast(11f).sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = getFontFamily(pa.explanationFontFamily),
                                            color = Color(pa.explanationTextColor)
                                        )
                                    }

                                    // Hide/Show toggle button
                                    Text(
                                        text = if (isExplanationVisible) "Hide ^" else "Show v",
                                        fontSize = (pa.explanationFontSize - 1).coerceAtLeast(11f).sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = getFontFamily(pa.explanationFontFamily),
                                        color = Color(pa.explanationTextColor),
                                        modifier = Modifier.clickable {
                                            isExplanationVisible = !isExplanationVisible
                                        }
                                    )
                                }
                            }

                            if (isExplanationVisible) {
                                Spacer(modifier = Modifier.height(10.dp))
                                RichVisualExplanationView(
                                    explanationText = currentQuestion.explanation ?: "No explanation available for this question.",
                                    highlights = currentQuestion.explanationHighlights,
                                    fontSize = pa.explanationFontSize.sp,
                                    fontFamily = getFontFamily(pa.explanationFontFamily),
                                    textColor = Color(pa.explanationTextColor),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                Spacer(modifier = Modifier.height(18.dp))

                // Links Row at bottom of question card
                if (!isMockTest) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Flag Question button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.clickable {
                                onFlagQuestion(currentQuestion)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.OutlinedFlag,
                                contentDescription = "Flag",
                                tint = Color(0XFFE11D48),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Flag Question",
                                fontSize = appSettings.explanationFontSize.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0XFFE11D48)
                            )
                        }
                    }
                }
            }
        }
    }
}
}

private fun getFontFamily(fontName: String?): androidx.compose.ui.text.font.FontFamily {
    return when (fontName?.lowercase()?.trim()) {
        "serif" -> androidx.compose.ui.text.font.FontFamily.Serif
        "monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
        "cursive" -> androidx.compose.ui.text.font.FontFamily.Cursive
        "sans-serif", "sansserif" -> androidx.compose.ui.text.font.FontFamily.SansSerif
        else -> androidx.compose.ui.text.font.FontFamily.Default
    }
}

/**
 * Cryptographically randomizes list elements using multiple Fisher-Yates rounds with SecureRandom
 * to produce maximum entropy and prevent predictable question sequences.
 */
private fun <T> List<T>.highlyShuffled(): List<T> {
    if (size <= 1) return this
    val list = this.toMutableList()
    val secureRandom = java.security.SecureRandom()
    repeat(5) {
        for (i in list.indices.reversed()) {
            val j = secureRandom.nextInt(i + 1)
            val temp = list[i]
            list[i] = list[j]
            list[j] = temp
        }
    }
    return list
}

