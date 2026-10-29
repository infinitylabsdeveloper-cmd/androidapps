package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.model.PreviousYearPaper
import com.example.model.PreviousYearQuestion
import com.example.model.Question
import com.example.ui.theme.*
import com.example.util.*
import java.util.Locale

private enum class PYQSubScreen {
    DASHBOARD,
    PAPER_OVERVIEW,
    PRACTICE_SESSION,
    SUBMIT_CONFIRMATION,
    TEST_RESULTS,
    ANSWER_REVIEW,
    EXPLANATION_DETAIL,
    WRONG_ANSWERS_PRACTICE,
    PROGRESS_HISTORY,
    COMPARE_YEARS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviousYearPapersScreen(
    onStartPaperAttempt: (String, List<Question>, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentAppId = SampleData.getActiveApp().id

    // Check Admin Configuration
    val config = remember(currentAppId) {
        PYQConfigStore.getConfigForApp(context, currentAppId)
    }

    // State Navigation
    var currentSubScreen by remember { mutableStateOf(PYQSubScreen.DASHBOARD) }
    var selectedPaper by remember { mutableStateOf<PreviousYearPaper?>(null) }
    var selectedMode by remember { mutableStateOf("Exam Mode") } // Exam Mode, Practice Mode, Full Paper Practice

    // Practice Session State
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var userAnswers by remember { mutableStateOf<Map<Int, String>>(emptyMap()) } // index -> "A"/"B"/"C"/"D"
    var reviewIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var timeRemainingSeconds by remember { mutableLongStateOf(config.defaultTimeLimitMinutes * 60L) }
    var isTimerPaused by remember { mutableStateOf(false) }

    // Results State
    var lastScore by remember { mutableDoubleStateOf(0.0) }
    var lastCorrectCount by remember { mutableIntStateOf(0) }
    var lastWrongCount by remember { mutableIntStateOf(0) }
    var lastUnansweredCount by remember { mutableIntStateOf(0) }
    var lastAccuracy by remember { mutableDoubleStateOf(0.0) }
    var selectedReviewQuestionIndex by remember { mutableIntStateOf(0) }
    var reviewFilter by remember { mutableStateOf("All") } // All, Correct, Incorrect, Unanswered

    // Year Filter in Dashboard
    var stageFilter by remember { mutableStateOf("All Years") } // All Years, Prelims, Mains

    val papers = remember(currentAppId) {
        PreviousYearPaperStore.getPapersForApp(context, currentAppId).ifEmpty {
            // Provide default fallback papers if empty
            listOf(
                PreviousYearPaper("pyp_2025", currentAppId, "APPSC Group-2", 2025, "2025-02-15", "Prelims", "English", "General Studies", true, SampleData.questionsState.take(20).mapIndexed { idx, q -> PreviousYearQuestion(idx + 1, q.questionText, q.options, q.correctOption, q.explanation, q.difficulty) }),
                PreviousYearPaper("pyp_2024", currentAppId, "APPSC Group-2", 2024, "2024-02-25", "Prelims", "English", "General Studies", true, SampleData.questionsState.take(20).mapIndexed { idx, q -> PreviousYearQuestion(idx + 1, q.questionText, q.options, q.correctOption, q.explanation, q.difficulty) }),
                PreviousYearPaper("pyp_2023", currentAppId, "APPSC Group-2", 2023, "2023-05-20", "Prelims", "English", "General Studies", true, SampleData.questionsState.take(20).mapIndexed { idx, q -> PreviousYearQuestion(idx + 1, q.questionText, q.options, q.correctOption, q.explanation, q.difficulty) }),
                PreviousYearPaper("pyp_2022", currentAppId, "APPSC Group-2", 2022, "2022-07-10", "Prelims", "English", "General Studies", true, SampleData.questionsState.take(20).mapIndexed { idx, q -> PreviousYearQuestion(idx + 1, q.questionText, q.options, q.correctOption, q.explanation, q.difficulty) }),
                PreviousYearPaper("pyp_2021", currentAppId, "APPSC Group-2", 2021, "2021-09-05", "Prelims", "English", "General Studies", true, SampleData.questionsState.take(20).mapIndexed { idx, q -> PreviousYearQuestion(idx + 1, q.questionText, q.options, q.correctOption, q.explanation, q.difficulty) })
            )
        }
    }

    val filteredPapers = remember(stageFilter, papers) {
        when (stageFilter) {
            "Prelims" -> papers.filter { it.shift.contains("Prelims", ignoreCase = true) || it.subject?.contains("Prelims", true) == true || true }
            "Mains" -> papers.filter { it.shift.contains("Mains", ignoreCase = true) || it.subject?.contains("Mains", true) == true }
            else -> papers
        }
    }

    // Back button handling
    BackHandler {
        when (currentSubScreen) {
            PYQSubScreen.DASHBOARD -> onBack()
            PYQSubScreen.PAPER_OVERVIEW -> currentSubScreen = PYQSubScreen.DASHBOARD
            PYQSubScreen.PRACTICE_SESSION -> currentSubScreen = PYQSubScreen.PAPER_OVERVIEW
            PYQSubScreen.SUBMIT_CONFIRMATION -> currentSubScreen = PYQSubScreen.PRACTICE_SESSION
            PYQSubScreen.TEST_RESULTS -> currentSubScreen = PYQSubScreen.PAPER_OVERVIEW
            PYQSubScreen.ANSWER_REVIEW -> currentSubScreen = PYQSubScreen.TEST_RESULTS
            PYQSubScreen.EXPLANATION_DETAIL -> currentSubScreen = PYQSubScreen.ANSWER_REVIEW
            PYQSubScreen.WRONG_ANSWERS_PRACTICE -> currentSubScreen = PYQSubScreen.TEST_RESULTS
            PYQSubScreen.PROGRESS_HISTORY -> currentSubScreen = PYQSubScreen.PAPER_OVERVIEW
            PYQSubScreen.COMPARE_YEARS -> currentSubScreen = PYQSubScreen.DASHBOARD
        }
    }

    // Handle Disabled/Scheduled State from Admin Config
    if (!config.enabled || config.availabilityStatus != PYQAvailabilityStatus.ENABLED) {
        PYQDisabledStateView(
            config = config,
            onBack = onBack
        )
        return
    }

    Scaffold(
        topBar = {
            if (currentSubScreen != PYQSubScreen.PRACTICE_SESSION) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentSubScreen) {
                                PYQSubScreen.DASHBOARD -> "Year-wise PYQs"
                                PYQSubScreen.PAPER_OVERVIEW -> selectedPaper?.let { "${it.examName} ${it.year}" } ?: "Paper Overview"
                                PYQSubScreen.TEST_RESULTS -> "Test Results"
                                PYQSubScreen.ANSWER_REVIEW -> "Answer Review"
                                PYQSubScreen.EXPLANATION_DETAIL -> "Question Explanation"
                                PYQSubScreen.WRONG_ANSWERS_PRACTICE -> "Wrong Answers Practice"
                                PYQSubScreen.PROGRESS_HISTORY -> "Your Progress"
                                PYQSubScreen.COMPARE_YEARS -> "Compare Years Performance"
                                else -> "Year-wise PYQs"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoal
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            when (currentSubScreen) {
                                PYQSubScreen.DASHBOARD -> onBack()
                                PYQSubScreen.PAPER_OVERVIEW -> currentSubScreen = PYQSubScreen.DASHBOARD
                                PYQSubScreen.TEST_RESULTS -> currentSubScreen = PYQSubScreen.PAPER_OVERVIEW
                                PYQSubScreen.ANSWER_REVIEW -> currentSubScreen = PYQSubScreen.TEST_RESULTS
                                PYQSubScreen.EXPLANATION_DETAIL -> currentSubScreen = PYQSubScreen.ANSWER_REVIEW
                                PYQSubScreen.WRONG_ANSWERS_PRACTICE -> currentSubScreen = PYQSubScreen.TEST_RESULTS
                                PYQSubScreen.PROGRESS_HISTORY -> currentSubScreen = PYQSubScreen.PAPER_OVERVIEW
                                PYQSubScreen.COMPARE_YEARS -> currentSubScreen = PYQSubScreen.DASHBOARD
                                else -> onBack()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepCharcoal)
                        }
                    },
                    actions = {
                        if (currentSubScreen == PYQSubScreen.DASHBOARD) {
                            IconButton(onClick = { currentSubScreen = PYQSubScreen.COMPARE_YEARS }) {
                                Icon(Icons.Default.Analytics, contentDescription = "Compare Performance", tint = ChampagneAccent)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Ivory)
                )
            }
        },
        containerColor = Ivory
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentSubScreen) {
                PYQSubScreen.DASHBOARD -> PYQDashboardView(
                    stageFilter = stageFilter,
                    onStageFilterChange = { stageFilter = it },
                    papers = filteredPapers,
                    onSelectPaper = { paper ->
                        selectedPaper = paper
                        currentSubScreen = PYQSubScreen.PAPER_OVERVIEW
                    }
                )

                PYQSubScreen.PAPER_OVERVIEW -> selectedPaper?.let { paper ->
                    PYQPaperOverviewView(
                        paper = paper,
                        config = config,
                        onStartMode = { mode ->
                            if ((mode == "Full Paper Practice" && !config.featureToggles.fullPaperPractice) ||
                                (mode == "Practice Mode" && !config.featureToggles.practiceMode) ||
                                (mode == "Exam Mode" && !config.featureToggles.examMode)
                            ) {
                                Toast.makeText(context, "$mode is currently disabled by Admin.", Toast.LENGTH_SHORT).show()
                                return@PYQPaperOverviewView
                            }
                            selectedMode = mode
                            userAnswers = emptyMap()
                            reviewIndices = emptySet()
                            currentQuestionIndex = 0
                            timeRemainingSeconds = (config.defaultTimeLimitMinutes * 60L)
                            currentSubScreen = PYQSubScreen.PRACTICE_SESSION
                        },
                        onViewAttempts = { currentSubScreen = PYQSubScreen.PROGRESS_HISTORY },
                        onViewBookmarks = {
                            if (!config.featureToggles.bookmarks) {
                                Toast.makeText(context, "Bookmarks feature is disabled by Admin.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Saved bookmarks loaded.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onViewWrongAnswers = {
                            if (!config.featureToggles.wrongAnswersPractice) {
                                Toast.makeText(context, "Wrong Answers Practice is disabled by Admin.", Toast.LENGTH_SHORT).show()
                            } else {
                                currentSubScreen = PYQSubScreen.WRONG_ANSWERS_PRACTICE
                            }
                        }
                    )
                }

                PYQSubScreen.PRACTICE_SESSION -> selectedPaper?.let { paper ->
                    PYQPracticeSessionView(
                        paper = paper,
                        mode = selectedMode,
                        config = config,
                        currentQuestionIndex = currentQuestionIndex,
                        onQuestionIndexChange = { currentQuestionIndex = it },
                        userAnswers = userAnswers,
                        onSelectAnswer = { qIdx, option ->
                            userAnswers = userAnswers + (qIdx to option)
                        },
                        reviewIndices = reviewIndices,
                        onToggleReview = { qIdx ->
                            reviewIndices = if (reviewIndices.contains(qIdx)) reviewIndices - qIdx else reviewIndices + qIdx
                        },
                        onClearAnswer = { qIdx ->
                            userAnswers = userAnswers - qIdx
                        },
                        timeRemainingSeconds = timeRemainingSeconds,
                        onRequestSubmit = {
                            currentSubScreen = PYQSubScreen.SUBMIT_CONFIRMATION
                        },
                        onBackToOverview = {
                            currentSubScreen = PYQSubScreen.PAPER_OVERVIEW
                        }
                    )
                }

                PYQSubScreen.SUBMIT_CONFIRMATION -> selectedPaper?.let { paper ->
                    PYQSubmitConfirmationDialog(
                        totalQuestions = paper.questions.size,
                        attemptedCount = userAnswers.size,
                        unattemptedCount = paper.questions.size - userAnswers.size,
                        timeTaken = "${(config.defaultTimeLimitMinutes * 60L - timeRemainingSeconds) / 60}m ${(config.defaultTimeLimitMinutes * 60L - timeRemainingSeconds) % 60}s",
                        onSubmit = {
                            // Calculate Score
                            var correct = 0
                            var wrong = 0
                            paper.questions.forEachIndexed { idx, q ->
                                val ans = userAnswers[idx]
                                if (ans != null) {
                                    if (ans.equals(q.correctAnswer, ignoreCase = true)) {
                                        correct++
                                    } else {
                                        wrong++
                                    }
                                }
                            }
                            val unanswered = paper.questions.size - (correct + wrong)
                            val score = (correct * config.marksPerCorrectAnswer) - (wrong * config.negativeMarks)
                            val accuracy = if (correct + wrong > 0) (correct.toDouble() / (correct + wrong)) * 100.0 else 0.0

                            lastScore = score.coerceAtLeast(0.0)
                            lastCorrectCount = correct
                            lastWrongCount = wrong
                            lastUnansweredCount = unanswered
                            lastAccuracy = accuracy

                            // Save user attempt record
                            PYQUserProgressStore.saveAttempt(
                                context,
                                PYQAttemptRecord(
                                    attemptId = "att_${System.currentTimeMillis()}",
                                    paperId = paper.id,
                                    appId = currentAppId,
                                    examName = paper.examName,
                                    year = paper.year,
                                    stage = paper.shift,
                                    score = lastScore,
                                    totalQuestions = paper.questions.size,
                                    correctCount = correct,
                                    wrongCount = wrong,
                                    unansweredCount = unanswered,
                                    timeTakenSeconds = (config.defaultTimeLimitMinutes * 60L - timeRemainingSeconds),
                                    accuracy = accuracy,
                                    timestamp = System.currentTimeMillis(),
                                    mode = selectedMode,
                                    userAnswersJson = userAnswers.toString(),
                                    bookmarkedIndicesJson = "[]",
                                    wrongIndicesJson = "[]"
                                )
                            )

                            currentSubScreen = PYQSubScreen.TEST_RESULTS
                        },
                        onCancel = {
                            currentSubScreen = PYQSubScreen.PRACTICE_SESSION
                        }
                    )
                }

                PYQSubScreen.TEST_RESULTS -> selectedPaper?.let { paper ->
                    PYQTestResultView(
                        paper = paper,
                        score = lastScore,
                        totalScore = paper.questions.size * config.marksPerCorrectAnswer,
                        accuracy = lastAccuracy,
                        correctCount = lastCorrectCount,
                        wrongCount = lastWrongCount,
                        unansweredCount = lastUnansweredCount,
                        timeTakenStr = "${(config.defaultTimeLimitMinutes * 60L - timeRemainingSeconds) / 60}m ${(config.defaultTimeLimitMinutes * 60L - timeRemainingSeconds) % 60}s",
                        onViewExplanations = {
                            if (!config.featureToggles.answerExplanations) {
                                Toast.makeText(context, "Answer Explanations disabled by Admin.", Toast.LENGTH_SHORT).show()
                            } else {
                                currentSubScreen = PYQSubScreen.ANSWER_REVIEW
                            }
                        },
                        onPracticeWrongAnswers = {
                            if (!config.featureToggles.wrongAnswersPractice) {
                                Toast.makeText(context, "Wrong Answers Practice disabled by Admin.", Toast.LENGTH_SHORT).show()
                            } else {
                                currentSubScreen = PYQSubScreen.WRONG_ANSWERS_PRACTICE
                            }
                        },
                        onReattempt = {
                            if (!config.featureToggles.reattempts) {
                                Toast.makeText(context, "Reattempts disabled by Admin.", Toast.LENGTH_SHORT).show()
                            } else {
                                userAnswers = emptyMap()
                                reviewIndices = emptySet()
                                currentQuestionIndex = 0
                                currentSubScreen = PYQSubScreen.PRACTICE_SESSION
                            }
                        },
                        onReturnToYearSelection = {
                            currentSubScreen = PYQSubScreen.DASHBOARD
                        }
                    )
                }

                PYQSubScreen.ANSWER_REVIEW -> selectedPaper?.let { paper ->
                    PYQAnswerReviewView(
                        paper = paper,
                        userAnswers = userAnswers,
                        filter = reviewFilter,
                        onFilterChange = { reviewFilter = it },
                        onSelectQuestion = { qIdx ->
                            selectedReviewQuestionIndex = qIdx
                            currentSubScreen = PYQSubScreen.EXPLANATION_DETAIL
                        }
                    )
                }

                PYQSubScreen.EXPLANATION_DETAIL -> selectedPaper?.let { paper ->
                    PYQExplanationDetailView(
                        paper = paper,
                        questionIndex = selectedReviewQuestionIndex,
                        userAnswer = userAnswers[selectedReviewQuestionIndex],
                        onNext = {
                            if (selectedReviewQuestionIndex < paper.questions.size - 1) {
                                selectedReviewQuestionIndex++
                            }
                        },
                        onPrev = {
                            if (selectedReviewQuestionIndex > 0) {
                                selectedReviewQuestionIndex--
                            }
                        }
                    )
                }

                PYQSubScreen.WRONG_ANSWERS_PRACTICE -> selectedPaper?.let { paper ->
                    PYQWrongAnswersPracticeView(
                        paper = paper,
                        wrongCount = lastWrongCount,
                        onStartPractice = {
                            userAnswers = emptyMap()
                            currentQuestionIndex = 0
                            currentSubScreen = PYQSubScreen.PRACTICE_SESSION
                        }
                    )
                }

                PYQSubScreen.PROGRESS_HISTORY -> selectedPaper?.let { paper ->
                    PYQProgressHistoryView(
                        paper = paper,
                        appId = currentAppId
                    )
                }

                PYQSubScreen.COMPARE_YEARS -> PYQCompareYearsView(
                    papers = papers,
                    onSelectPaper = { paper ->
                        selectedPaper = paper
                        currentSubScreen = PYQSubScreen.PAPER_OVERVIEW
                    }
                )

                else -> {}
            }
        }
    }
}

/**
 * Screen 1: Dashboard View
 */
@Composable
private fun PYQDashboardView(
    stageFilter: String,
    onStageFilterChange: (String) -> Unit,
    papers: List<PreviousYearPaper>,
    onSelectPaper: (PreviousYearPaper) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Luxury Header Banner (Image 1, Screen 1)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, ChampagneAccent.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFF3EE), Color(0xFFFCE4EC), Color(0xFFFFF3EE))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Year-wise PYQs", fontWeight = FontWeight.Black, fontSize = 22.sp, color = DeepCharcoal)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Practice Real Questions • Know the Exam Pattern\nBoost Your Confidence",
                            fontSize = 12.sp,
                            color = DeepCharcoal.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(BlushPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ChampagneAccent, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        // Filter Pills Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val pills = listOf("All Years", "Prelims", "Mains")
            pills.forEach { pill ->
                val isSel = stageFilter == pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSel) Color(0xFFEC4899) else Color.White,
                    border = BorderStroke(1.dp, if (isSel) Color(0xFFEC4899) else Lavender.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { onStageFilterChange(pill) }
                ) {
                    Text(
                        text = pill,
                        fontSize = 13.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Color.White else DeepCharcoal,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Year Cards List
        papers.forEachIndexed { idx, paper ->
            // Compute realistic stats or mock values for display
            val mockAccuracy = when (idx % 5) {
                0 -> 65
                1 -> 40
                2 -> 100
                3 -> 25
                else -> 0
            }
            val mockAttempted = (paper.questions.size * (mockAccuracy / 100.0)).toInt()

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, BlushPink.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPaper(paper) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SoftPeach),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "${paper.year} (${paper.shift})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepCharcoal
                            )
                            Text(
                                text = "${paper.questions.size} Questions",
                                fontSize = 12.sp,
                                color = DeepCharcoal.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("$mockAttempted Attempted", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = DeepCharcoal.copy(alpha = 0.7f))
                                Text("${(mockAttempted * 0.8).toInt()} Correct", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF10B981))
                            }
                        }
                    }

                    // Accuracy Progress Gauge Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color(0xFFF3E8FF),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 4.dp.toPx())
                                )
                                drawArc(
                                    color = if (mockAccuracy >= 60) Color(0xFF10B981) else Color(0xFFEC4899),
                                    startAngle = -90f,
                                    sweepAngle = (mockAccuracy / 100f) * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Text(
                                text = "$mockAccuracy%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepCharcoal
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ChampagneAccent)
                    }
                }
            }
        }
    }
}

/**
 * Screen 2: Paper Overview & Practice Mode Selection (Image 1, Screen 2)
 */
@Composable
private fun PYQPaperOverviewView(
    paper: PreviousYearPaper,
    config: PYQAppConfig,
    onStartMode: (String) -> Unit,
    onViewAttempts: () -> Unit,
    onViewBookmarks: () -> Unit,
    onViewWrongAnswers: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Card Header
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BlushPink.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, ChampagneAccent.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("${paper.examName} ${paper.year} ${paper.shift}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = DeepCharcoal)
                    Text("Explore • Practice • Analyse • Improve", fontSize = 12.sp, color = DeepCharcoal.copy(alpha = 0.7f))
                }
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ChampagneAccent, modifier = Modifier.size(36.dp))
            }
        }

        // Metrics Grid (Questions, Marks, Time, Negative)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val metrics = listOf(
                Triple("Questions", "${paper.questions.size}", Icons.Default.Quiz),
                Triple("Marks", "${paper.questions.size}", Icons.Default.Verified),
                Triple("Time", "${config.defaultTimeLimitMinutes} min", Icons.Default.Timer),
                Triple("Negative", "-${config.negativeMarks}", Icons.Default.RemoveCircleOutline)
            )
            metrics.forEach { (label, value, icon) ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Lavender.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(icon, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepCharcoal)
                        Text(label, fontSize = 10.sp, color = DeepCharcoal.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // Your Progress Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, SoftPeach),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Your Progress", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepCharcoal)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(Color(0xFFF3E8FF), 0f, 360f, false, style = Stroke(6.dp.toPx()))
                            drawArc(Color(0xFFEC4899), -90f, 144f, false, style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
                        }
                        Text("40%", fontSize = 13.sp, fontWeight = FontWeight.Black, color = DeepCharcoal)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("80", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepCharcoal)
                        Text("Attempted", fontSize = 11.sp, color = DeepCharcoal.copy(alpha = 0.6f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("58", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF10B981))
                        Text("Correct", fontSize = 11.sp, color = DeepCharcoal.copy(alpha = 0.6f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("72%", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFEC4899))
                        Text("Accuracy", fontSize = 11.sp, color = DeepCharcoal.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // Action Mode Buttons (Full Paper, Practice Mode, Exam Mode)
        val modes = listOf(
            Triple("Full Paper Practice", "Attempt all ${paper.questions.size} questions", Icons.Default.Assignment),
            Triple("Practice Mode", "No timer • Learn at your own pace", Icons.Default.AccessTime),
            Triple("Exam Mode", "Timer • Real exam experience", Icons.Default.Psychology)
        )

        modes.forEach { (modeTitle, modeSub, icon) ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.3f)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartMode(modeTitle) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFCE4EC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(modeTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepCharcoal)
                            Text(modeSub, fontSize = 12.sp, color = DeepCharcoal.copy(alpha = 0.6f))
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFEC4899))
                }
            }
        }

        // Quick Row (Previous Attempts, Bookmarks, Wrong Answers)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SoftPeach.copy(alpha = 0.5f),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onViewAttempts() }
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, tint = ChampagneAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Previous Attempts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SoftPeach.copy(alpha = 0.5f),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onViewBookmarks() }
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFF3B82F6))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Bookmarks", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SoftPeach.copy(alpha = 0.5f),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onViewWrongAnswers() }
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Wrong Answers", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                }
            }
        }
    }
}

/**
 * Screen 3: PYQ Practice Session View (Image 1, Screen 3)
 */
@Composable
private fun PYQPracticeSessionView(
    paper: PreviousYearPaper,
    mode: String,
    config: PYQAppConfig,
    currentQuestionIndex: Int,
    onQuestionIndexChange: (Int) -> Unit,
    userAnswers: Map<Int, String>,
    onSelectAnswer: (Int, String) -> Unit,
    reviewIndices: Set<Int>,
    onToggleReview: (Int) -> Unit,
    onClearAnswer: (Int) -> Unit,
    timeRemainingSeconds: Long,
    onRequestSubmit: () -> Unit,
    onBackToOverview: () -> Unit
) {
    val context = LocalContext.current
    val currentQuestion = paper.questions.getOrNull(currentQuestionIndex) ?: return

    var isBookmarked by remember(currentQuestionIndex) {
        mutableStateOf(PYQUserProgressStore.isQuestionBookmarked(context, paper.id, currentQuestionIndex))
    }

    val minutes = timeRemainingSeconds / 60
    val seconds = timeRemainingSeconds % 60
    val timerStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", minutes / 60, minutes % 60, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory)
    ) {
        // Top Header
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackToOverview) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepCharcoal)
                        }
                        Column {
                            Text("${paper.year} ${paper.shift}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepCharcoal)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BlushPink.copy(alpha = 0.5f)
                            ) {
                                Text(mode, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    if (mode == "Exam Mode" || mode == "Full Paper Practice") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF3EE),
                            border = BorderStroke(1.dp, ChampagneAccent)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(timerStr, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepCharcoal)
                            }
                        }
                    }

                    Text("${currentQuestionIndex + 1} / ${paper.questions.size}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepCharcoal)
                }
            }
        }

        // Question Content Scrollable
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Question Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BlushPink.copy(alpha = 0.5f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Question ${currentQuestionIndex + 1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFEC4899))
                        IconButton(onClick = {
                            isBookmarked = PYQUserProgressStore.toggleBookmark(context, paper.id, currentQuestionIndex)
                            Toast.makeText(context, if (isBookmarked) "Question bookmarked" else "Bookmark removed", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) Color(0xFFEC4899) else DeepCharcoal.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentQuestion.question,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = DeepCharcoal,
                        lineHeight = 22.sp
                    )
                }
            }

            // Options List (A, B, C, D)
            val selectedOption = userAnswers[currentQuestionIndex]
            val optionsList = currentQuestion.options.entries.toList()

            optionsList.forEach { (key, optionText) ->
                val isSelected = selectedOption == key
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFFFCE4EC) else Color.White,
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFEC4899) else Lavender.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectAnswer(currentQuestionIndex, key) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFFEC4899) else Color(0xFFF8FAFC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                key,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else DeepCharcoal
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            optionText,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = DeepCharcoal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Optional Immediate Explanation if Practice Mode
            if (mode == "Practice Mode" && selectedOption != null && config.featureToggles.answerExplanations) {
                val isCorrect = selectedOption.equals(currentQuestion.correctAnswer, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isCorrect) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, if (isCorrect) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isCorrect) " Correct Answer: ${currentQuestion.correctAnswer}" else " Incorrect! Correct Answer: ${currentQuestion.correctAnswer}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isCorrect) Color(0xFF15803D) else Color(0xFFB91C1C)
                        )
                        currentQuestion.explanation?.let { exp ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(exp, fontSize = 12.sp, color = DeepCharcoal.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }

        // Question Palette Quick Grid (1..N)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(paper.questions.size) { idx ->
                val isAns = userAnswers.containsKey(idx)
                val isRev = reviewIndices.contains(idx)
                val isCurrent = idx == currentQuestionIndex

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isCurrent -> Color(0xFFEC4899)
                                isAns -> Color(0xFF10B981)
                                isRev -> Color(0xFF8B5CF6)
                                else -> Color(0xFFF1F5F9)
                            }
                        )
                        .clickable { onQuestionIndexChange(idx) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${idx + 1}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent || isAns || isRev) Color.White else DeepCharcoal
                    )
                }
            }
        }

        // Bottom Actions Bar
        Surface(
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onToggleReview(currentQuestionIndex) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (reviewIndices.contains(currentQuestionIndex)) "Reviewed" else "Review", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { onClearAnswer(currentQuestionIndex) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Clear", fontSize = 12.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (currentQuestionIndex > 0) onQuestionIndexChange(currentQuestionIndex - 1)
                        },
                        enabled = currentQuestionIndex > 0,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftPeach)
                    ) {
                        Text("Previous", fontSize = 12.sp, color = DeepCharcoal)
                    }

                    if (currentQuestionIndex < paper.questions.size - 1) {
                        Button(
                            onClick = { onQuestionIndexChange(currentQuestionIndex + 1) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                        ) {
                            Text("Next >", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = onRequestSubmit,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Submit Test", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen 4: Submit Confirmation Dialog (Image 1, Screen 4)
 */
@Composable
private fun PYQSubmitConfirmationDialog(
    totalQuestions: Int,
    attemptedCount: Int,
    unattemptedCount: Int,
    timeTaken: String,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFCE4EC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Submit Test", fontWeight = FontWeight.Black, fontSize = 20.sp, color = DeepCharcoal)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Are you sure you want to submit the test?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DeepCharcoal,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "You have attempted $attemptedCount out of $totalQuestions questions.",
                    fontSize = 12.sp,
                    color = DeepCharcoal.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFDCFCE7), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$attemptedCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF15803D))
                            Text("Attempted", fontSize = 10.sp, color = Color(0xFF15803D))
                        }
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFEE2E2), modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$unattemptedCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFB91C1C))
                            Text("Unattempted", fontSize = 10.sp, color = Color(0xFFB91C1C))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Test", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = DeepCharcoal)
            }
        }
    )
}

/**
 * Screen 5: Test Results View (Image 1, Screen 5)
 */
@Composable
private fun PYQTestResultView(
    paper: PreviousYearPaper,
    score: Double,
    totalScore: Double,
    accuracy: Double,
    correctCount: Int,
    wrongCount: Int,
    unansweredCount: Int,
    timeTakenStr: String,
    onViewExplanations: () -> Unit,
    onPracticeWrongAnswers: () -> Unit,
    onReattempt: () -> Unit,
    onReturnToYearSelection: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Score Header Banner
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, ChampagneAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFF3EE), Color(0xFFFCE4EC))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("${paper.year} ${paper.shift}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepCharcoal.copy(alpha = 0.7f))
                    Text("Test Results", fontSize = 22.sp, fontWeight = FontWeight.Black, color = DeepCharcoal)

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${score.toInt()} / ${totalScore.toInt()}", fontWeight = FontWeight.Black, fontSize = 24.sp, color = DeepCharcoal)
                            Text("Total Score", fontSize = 11.sp, color = DeepCharcoal.copy(alpha = 0.6f))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${accuracy.toInt()}%", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFFEC4899))
                            Text("Accuracy", fontSize = 11.sp, color = DeepCharcoal.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }

        // Summary Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFDCFCE7), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$correctCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF15803D))
                    Text("Correct", fontSize = 11.sp, color = Color(0xFF15803D))
                }
            }
            Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFFEE2E2), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$wrongCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFB91C1C))
                    Text("Incorrect", fontSize = 11.sp, color = Color(0xFFB91C1C))
                }
            }
            Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFF1F5F9), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$unansweredCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepCharcoal)
                    Text("Unanswered", fontSize = 11.sp, color = DeepCharcoal)
                }
            }
        }

        // Time Taken & Percentile
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, SoftPeach), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("⏱️ Time Taken: $timeTakenStr", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                Text("🏆 75th Percentile", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
            }
        }

        // Action Buttons Grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onViewExplanations,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Answers with Explanations", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onPracticeWrongAnswers,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Practice Wrong Answers ($wrongCount)", fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onReattempt,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reattempt Test", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onReturnToYearSelection,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Return to Years", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Screen 6: Answer Review List (Image 1, Screen 6)
 */
@Composable
private fun PYQAnswerReviewView(
    paper: PreviousYearPaper,
    userAnswers: Map<Int, String>,
    filter: String,
    onFilterChange: (String) -> Unit,
    onSelectQuestion: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filter Pills
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val filters = listOf("All", "Correct", "Incorrect", "Unanswered")
            filters.forEach { f ->
                val isSel = filter == f
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSel) Color(0xFFEC4899) else Color.White,
                    border = BorderStroke(1.dp, Lavender),
                    modifier = Modifier.clickable { onFilterChange(f) }
                ) {
                    Text(
                        f,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else DeepCharcoal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Question List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(paper.questions.size) { idx ->
                val q = paper.questions[idx]
                val userAns = userAnswers[idx]
                val isCorrect = userAns != null && userAns.equals(q.correctAnswer, ignoreCase = true)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Lavender.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectQuestion(idx) }
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        userAns == null -> Color(0xFFF1F5F9)
                                        isCorrect -> Color(0xFFDCFCE7)
                                        else -> Color(0xFFFEE2E2)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(q.question, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold, color = DeepCharcoal)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Your Ans: ${userAns ?: "None"}", fontSize = 11.sp, color = if (isCorrect) Color(0xFF15803D) else Color(0xFFB91C1C))
                                Text("Correct: ${q.correctAnswer}", fontSize = 11.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ChampagneAccent)
                    }
                }
            }
        }
    }
}

/**
 * Screen 7: Explanation Detail View (Image 1, Screen 7)
 */
@Composable
private fun PYQExplanationDetailView(
    paper: PreviousYearPaper,
    questionIndex: Int,
    userAnswer: String?,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    val q = paper.questions.getOrNull(questionIndex) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev, enabled = questionIndex > 0) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev")
            }
            Text("Question ${questionIndex + 1} of ${paper.questions.size}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepCharcoal)
            IconButton(onClick = onNext, enabled = questionIndex < paper.questions.size - 1) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next")
            }
        }

        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, Lavender), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(q.question, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepCharcoal)
            }
        }

        q.options.forEach { (key, text) ->
            val isCorrect = key.equals(q.correctAnswer, ignoreCase = true)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isCorrect) Color(0xFFDCFCE7) else Color.White,
                border = BorderStroke(1.dp, if (isCorrect) Color(0xFF86EFAC) else Lavender.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("$key. $text", fontSize = 14.sp, fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal)
                    if (isCorrect) {
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D))
                    }
                }
            }
        }

        Surface(shape = RoundedCornerShape(16.dp), color = SoftPeach, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Correct Answer: ${q.correctAnswer}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF15803D))
                q.explanation?.let { exp ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(exp, fontSize = 13.sp, color = DeepCharcoal.copy(alpha = 0.8f))
                }
            }
        }
    }
}

/**
 * Screen 8: Wrong Answers Practice View (Image 1, Screen 8)
 */
@Composable
private fun PYQWrongAnswersPracticeView(
    paper: PreviousYearPaper,
    wrongCount: Int,
    onStartPractice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFEE2E2), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFB91C1C), modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Wrong Answers Practice", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFFB91C1C))
                Text("Focus on your weak areas • Improve Faster", fontSize = 12.sp, color = Color(0xFFB91C1C).copy(alpha = 0.8f))
            }
        }

        Button(
            onClick = onStartPractice,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Start Practice ($wrongCount Questions)", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Screen 9: Progress & History View (Image 1, Screen 9)
 */
@Composable
private fun PYQProgressHistoryView(
    paper: PreviousYearPaper,
    appId: String
) {
    val context = LocalContext.current
    val attempts = remember { PYQUserProgressStore.getAttemptsForPaper(context, paper.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, SoftPeach), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("${paper.year} ${paper.shift} Progress", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepCharcoal)
                Text("Total Attempts: ${attempts.size}", fontSize = 12.sp, color = DeepCharcoal.copy(alpha = 0.6f))
            }
        }
    }
}

/**
 * Screen 10: Year-wise Compare Performance View (Image 1, Screen 10)
 */
@Composable
private fun PYQCompareYearsView(
    papers: List<PreviousYearPaper>,
    onSelectPaper: (PreviousYearPaper) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, ChampagneAccent), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Year-wise Performance", fontWeight = FontWeight.Black, fontSize = 18.sp, color = DeepCharcoal)
                Text("Compare your accuracy across examination years", fontSize = 12.sp, color = DeepCharcoal.copy(alpha = 0.6f))
            }
        }

        papers.forEach { p ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Lavender),
                modifier = Modifier.fillMaxWidth().clickable { onSelectPaper(p) }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("${p.year} (${p.shift})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepCharcoal)
                        Text("${p.questions.size} Questions", fontSize = 12.sp, color = DeepCharcoal.copy(alpha = 0.6f))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ChampagneAccent)
                }
            }
        }
    }
}

/**
 * Disabled / Scheduled State View (Image 2, Screens 8, 9, 10)
 */
@Composable
private fun PYQDisabledStateView(
    config: PYQAppConfig,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val titleStr = when (config.disabledMessageType) {
        PYQDisabledMessageType.COMING_SOON -> "Coming Soon..."
        PYQDisabledMessageType.UPDATING_SOON -> "Updating Soon..."
        PYQDisabledMessageType.CUSTOM -> "Notice"
    }

    val bodyMsg = if (config.disabledMessageType == PYQDisabledMessageType.CUSTOM && config.disabledCustomMessage.isNotBlank()) {
        config.disabledCustomMessage
    } else if (config.availabilityStatus == PYQAvailabilityStatus.SCHEDULED) {
        "Previous Year Questions will be available from ${config.scheduledDateTime}"
    } else {
        "We're working hard to bring Previous Year Questions for you. Stay tuned!"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFCE4EC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (config.availabilityStatus == PYQAvailabilityStatus.SCHEDULED) Icons.Default.CalendarMonth else Icons.Default.Campaign,
                    contentDescription = null,
                    tint = Color(0xFFEC4899),
                    modifier = Modifier.size(52.dp)
                )
            }

            Text(titleStr, fontWeight = FontWeight.Black, fontSize = 24.sp, color = DeepCharcoal)
            Text(
                bodyMsg,
                fontSize = 14.sp,
                color = DeepCharcoal.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "🔔 You will be notified as soon as PYQs go live!", Toast.LENGTH_LONG).show()
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notify Me", fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onBack) {
                Text("Back to Home", color = DeepCharcoal)
            }
        }
    }
}
