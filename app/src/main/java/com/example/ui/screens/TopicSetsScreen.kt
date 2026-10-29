package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.PracticeAppearanceSettings
import com.example.model.PracticeSetModel
import com.example.model.Question
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.model.UnitModel
import com.example.repository.AppConfigRepository
import com.example.ui.components.HeaderLevel
import com.example.ui.components.PracticeAppearancePanel
import com.example.ui.components.PremiumSectionHeader
import com.example.ui.components.PremiumSubscriptionBadge
import com.example.ui.components.StatBadge
import com.example.ui.components.TopicFhdCardImage
import com.example.ui.components.cascadeDropItem
import com.example.ui.components.premiumPress
import com.example.ui.components.premiumTargetAnimation
import com.example.ui.theme.AppLuminousBackground
import com.example.ui.theme.PremiumCardColorSystem
import com.example.util.FontSettingsManager
import com.example.util.PracticeAppearanceManager
import com.example.util.PracticeSessionManager

data class TopicSubtopicItem(
    val id: String,
    val code: String,
    val title: String,
    val questions: List<Question>,
    val practiceSet: PracticeSetModel
)

// =========================================================================
// 1. PALETTES FOR EXACT RECREATION OF REFERENCE DESIGN
// =========================================================================

data class SetCardTheme(
    val accentColor: Color,
    val cardBgStart: Color,
    val cardBgEnd: Color,
    val cardBorder: Color,
    val buttonBg: Color,
    val chevronTint: Color
)

data class SubtopicCardTheme(
    val accentColor: Color,
    val pillBg: Color,
    val pillBorder: Color,
    val cardBgStart: Color,
    val cardBgEnd: Color,
    val cardBorder: Color
)

// =========================================================================
// 2. HELPER: CHUNK QUESTIONS INTO EXACT 10-QUESTION SETS
// =========================================================================

fun generateSetsForQuestionsList(
    questions: List<Question>,
    baseId: String = "set"
): List<PracticeSetModel> {
    if (questions.isEmpty()) return emptyList()

    val chunks = questions.chunked(10)
    return chunks.mapIndexed { index, chunkQuestions ->
        val setNumber = index + 1
        val startQ = index * 10 + 1
        val endQ = startQ + chunkQuestions.size - 1
        PracticeSetModel(
            id = "${baseId}-set-$setNumber",
            title = "Set %02d".format(setNumber),
            subtitle = "${chunkQuestions.size} Qs • Q$startQ – Q$endQ",
            setNumber = setNumber,
            questionsCount = chunkQuestions.size,
            startQuestionNumber = startQ,
            endQuestionNumber = endQ,
            questions = chunkQuestions,
            badge = ""
        )
    }
}

private fun resolveHierarchyContext(
    subjectId: String,
    unitId: String,
    topicId: String,
    fallbackSubject: Subject? = null,
    fallbackUnit: UnitModel? = null,
    fallbackTopic: TopicModel? = null
): Triple<Subject, UnitModel?, TopicModel> {
    val resolvedSubject = SampleData.subjectsState.find { it.id == subjectId }
        ?: fallbackSubject
        ?: SampleData.subjectsState.firstOrNull()
        ?: Subject(id = subjectId, name = "Syllabus Subject")

    val resolvedUnit = resolvedSubject.units.find { it.id == unitId }
        ?: fallbackUnit
        ?: resolvedSubject.units.firstOrNull()

    val resolvedTopic = resolvedUnit?.topics?.find { it.id == topicId }
        ?: fallbackTopic
        ?: resolvedUnit?.topics?.firstOrNull()
        ?: TopicModel(
            id = topicId.ifBlank { "default-topic" },
            name = resolvedUnit?.name ?: resolvedSubject.name,
            estimatedMcqs = "100"
        )

    return Triple(resolvedSubject, resolvedUnit, resolvedTopic)
}

private fun resolveTopicQuestions(
    subject: Subject,
    unit: UnitModel?,
    activeTopicVal: TopicModel
): List<Question> {
    val direct = SampleData.questionsState.filter {
        it.topicId == activeTopicVal.id &&
            (it.subjectId.isBlank() || it.subjectId == subject.id)
    }
    if (direct.isNotEmpty()) return direct

    val unitMatches = SampleData.questionsState.filter {
        it.subjectId == subject.id && (unit == null || it.unitId == unit.id || it.unitId.isBlank())
    }
    return if (unitMatches.isNotEmpty()) {
        unitMatches
    } else {
        SampleData.questionsState.filter { it.subjectId == subject.id }
    }
}

// =========================================================================
// 3. FULL-SCREEN SETS SCREEN FOR A TOPIC (Level 3 -> Level 4)
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicSetsScreen(
    subjectId: String,
    unitId: String,
    topicId: String,
    fallbackSubject: Subject? = null,
    fallbackUnit: UnitModel? = null,
    fallbackTopic: TopicModel? = null,
    onBack: () -> Unit,
    onNavigateToHome: (() -> Unit)? = null,
    onSelectSet: (PracticeSetModel, List<PracticeSetModel>) -> Unit,
    onSelectSubtopic: (PracticeSetModel, String) -> Unit = { pSet, _ -> onSelectSet(pSet, emptyList()) },
    testResults: List<com.example.model.TestResult> = emptyList(),
    onNavigateToSubscription: () -> Unit = {}
) {
    val context = LocalContext.current
    val setsListState = rememberLazyListState()
    val (subject, unit, activeTopicVal) = remember(
        subjectId,
        unitId,
        topicId,
        SampleData.subjectsState.size
    ) {
        resolveHierarchyContext(subjectId, unitId, topicId, fallbackSubject, fallbackUnit, fallbackTopic)
    }
    val topicQuestions = remember(activeTopicVal.id, subject.id, unit?.id, SampleData.questionsState.size) {
        resolveTopicQuestions(subject, unit, activeTopicVal)
    }
    val subtopicsList = remember(activeTopicVal.subtopics, topicQuestions) {
        val explicit = activeTopicVal.subtopics.filter { it.isNotBlank() }
        val fromQuestions = topicQuestions.map { it.subtopic }.filter { it.isNotBlank() }.distinct()
        (explicit + fromQuestions).distinct().ifEmpty {
            if (topicQuestions.isNotEmpty()) listOf("${activeTopicVal.name} – Core Concepts") else emptyList()
        }
    }
    val setsList = remember(topicQuestions, subject.id, unit?.id, activeTopicVal.id) {
        generateSetsForQuestionsList(
            questions = topicQuestions,
            baseId = "set-${subject.id}-${unit?.id ?: ""}-${activeTopicVal.id}-all"
        )
    }

    val topicSubtopics = remember(subtopicsList, topicQuestions, activeTopicVal) {
        subtopicsList.mapIndexed { index, subTitle ->
            val assignedQuestions = topicQuestions.filter { it.subtopic.equals(subTitle, ignoreCase = true) }.ifEmpty {
                topicQuestions
            }
            val subId = "topic-sub-${activeTopicVal.id}-${index + 1}-${subTitle.hashCode()}"
            val code = "%02d".format(index + 1)
            TopicSubtopicItem(
                id = subId,
                code = code,
                title = subTitle,
                questions = assignedQuestions,
                practiceSet = PracticeSetModel(
                    id = subId,
                    title = subTitle,
                    subtitle = "${assignedQuestions.size} Questions",
                    setNumber = index + 1,
                    questionsCount = assignedQuestions.size,
                    startQuestionNumber = 1,
                    endQuestionNumber = assignedQuestions.size.coerceAtLeast(1),
                    questions = assignedQuestions,
                    badge = activeTopicVal.name
                )
            )
        }
    }

    var selectedTab by remember { mutableStateOf("SETS") }

    val topicHeaderGradient = remember(activeTopicVal.id, activeTopicVal.name, subject.name) {
        val topicIdx = unit?.topics?.indexOfFirst { it.id == activeTopicVal.id }?.takeIf { it >= 0 } ?: 0
        PremiumCardColorSystem.getGradientForTopic(topicIdx, activeTopicVal.name, subject.name)
    }

    BackHandler {
        onBack()
    }

    val configRepository = remember { AppConfigRepository.getInstance() }
    val currentAppConfig by configRepository.currentAppConfig.collectAsState()
    var showPracticeAppearanceSheet by remember { mutableStateOf(false) }

    AppLuminousBackground(screenName = "Topic Sets Screen") {
        Scaffold(
            topBar = {
                PremiumSectionHeader(
                    title = activeTopicVal.name,
                    level = HeaderLevel.TOPIC,
                    onBack = onBack,
                    onNavigateToHome = onNavigateToHome,
                    rightAction = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { showPracticeAppearanceSheet = true }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "Practice Appearance Settings",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            PremiumSubscriptionBadge(
                                onNavigateToSubscription = onNavigateToSubscription
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                state = setsListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. TOPIC HERO SUMMARY CARD
                item(key = "topic_header_${activeTopicVal.id}") {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.5.dp, topicHeaderGradient.borderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .premiumTargetAnimation(
                                target = com.example.model.AnimatableTarget.TOPIC_CARDS,
                                index = 0,
                                screenKey = "Topic Sets"
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(148.dp)
                                .background(Brush.linearGradient(colors = topicHeaderGradient.colors))
                        ) {
                            TopicFhdCardImage(
                                topic = activeTopicVal,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.15f),
                                                Color.Black.copy(alpha = 0.35f),
                                                Color.Black.copy(alpha = 0.55f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = activeTopicVal.name.uppercase(),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!activeTopicVal.subtitle.isNullOrBlank()) {
                                        Text(
                                            text = activeTopicVal.subtitle,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                            color = Color(0xFFFDE68A).copy(alpha = 0.9f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    StatBadge(
                                        label = "Sets",
                                        value = "${setsList.size}",
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.weight(1f),
                                        isCompact = true
                                    )
                                    StatBadge(
                                        label = "MCQs",
                                        value = "${topicQuestions.size}",
                                        color = Color(0xFF059669),
                                        modifier = Modifier.weight(1f),
                                        isCompact = true
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. TOP ROW: TWO OPTIONS SIDE BY SIDE (SETS & SUBTOPICS)
                item(key = "topic_tabs_bar") {
                    TopicSideBySideTabsBar(
                        selectedTab = selectedTab,
                        setsCount = setsList.size,
                        subtopicsCount = topicSubtopics.size,
                        onTabSelected = { selectedTab = it }
                    )
                }

                if (selectedTab == "SETS") {
                    // SECTION HEADER FOR PRACTICE SETS
                    item(key = "sets_section_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "PRACTICE SETS (${setsList.size})",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color(0xFF000080)
                                )
                                Text(
                                    text = "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFFFFFF)
                                )
                            }
                        }
                    }

                    // 2-COLUMN GRID DISPLAY FOR SETS
                    if (setsList.isEmpty()) {
                        item(key = "empty_sets_state") {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassEmpty,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No Sets Available",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Practice questions are being prepared for this topic.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    } else {
                        // DESIGN 4 palette: premium orange, cyan, rose and emerald cards.
                        val solidColors = listOf(
                            Color(0xFFFFF0C2), Color(0xFFD9F5FF),
                            Color(0xFFFFE0E8), Color(0xFFDDFBE8),
                            Color(0xFFFFF0C2), Color(0xFFD9F5FF)
                        )
                        val chunkedSets = setsList.chunked(2)
                        itemsIndexed(chunkedSets, key = { rowIndex, _ -> "set_row_$rowIndex" }) { rowIndex, rowSets ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowSets.forEachIndexed { colIndex, pSet ->
                                    val globalIndex = rowIndex * 2 + colIndex
                                    val bgCol = solidColors[globalIndex % solidColors.size]
                                    val serialNumber = "%02d".format(pSet.setNumber)
                                    val setSubtitle = "${pSet.questionsCount} Questions • Q${pSet.startQuestionNumber}–Q${pSet.endQuestionNumber}"
                                    val hasDbCompleted = testResults.any { it.testName.contains(pSet.title, ignoreCase = true) }
                                    val status = PracticeSessionManager.getSessionStatus(context, pSet.id, hasDbCompleted)

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("set_card_${pSet.id}")
                                            .cascadeDropItem(
                                                visible = true,
                                                index = globalIndex,
                                                target = com.example.model.AnimatableTarget.SETS_CARDS,
                                                screenKey = "Topic Sets"
                                            )
                                    ) {
                                        val isSetDownloaded = com.example.util.OfflineSyncManager.isSetDownloaded(subjectId, unitId, topicId, "Set $serialNumber")
                                        CompactGridSetCard(
                                            serialNumber = serialNumber,
                                            title = "Set $serialNumber",
                                            subtitle = setSubtitle,
                                            status = status,
                                            backgroundColor = bgCol,
                                            isDownloaded = isSetDownloaded,
                                            onDownloadClick = {
                                                com.example.util.OfflineSyncManager.downloadPracticeSet(
                                                    context = context,
                                                    subjectId = subjectId,
                                                    unitId = unitId,
                                                    topicId = topicId,
                                                    setName = "Set $serialNumber",
                                                    onComplete = {
                                                        Toast.makeText(context, "Set $serialNumber downloaded offline!", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            },
                                            onClick = { onSelectSet(pSet, setsList) }
                                        )
                                    }
                                }
                                if (rowSets.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    // SECTION HEADER FOR SUBTOPICS
                    item(key = "subtopics_section_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SUBTOPICS (${topicSubtopics.size})",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Two-column grid of topic subtopics",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // 2-COLUMN GRID DISPLAY FOR SUBTOPICS
                    if (topicSubtopics.isEmpty()) {
                        item(key = "empty_subtopics_state") {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassEmpty,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No Subtopics Available",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Subtopic content for this topic is currently being updated.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    } else {
                        // DESIGN 4 pastel palette keeps dark text clearly readable.
                        val solidColors = listOf(
                            Color(0xFFFFF0C2), Color(0xFFD9F5FF),
                            Color(0xFFFFE0E8), Color(0xFFDDFBE8),
                            Color(0xFFE6F4FF), Color(0xFFE7F9F0)
                        )
                        itemsIndexed(topicSubtopics, key = { _, subItem -> subItem.id }) { globalIndex, subItem ->
                            val bgCol = solidColors[globalIndex % solidColors.size]
                            val hasDbCompletedSub = testResults.any {
                                it.testName.contains(subItem.title, ignoreCase = true)
                            }
                            val status = PracticeSessionManager.getSessionStatus(
                                context, subItem.id, hasDbCompletedSub
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("subtopic_card_${subItem.id}")
                                    .cascadeDropItem(
                                        visible = true,
                                        index = globalIndex,
                                        target = com.example.model.AnimatableTarget.SUBTOPIC_CARDS,
                                        screenKey = "Topic Sets"
                                    )
                            ) {
                                val isSubtopicDownloaded = com.example.util.OfflineSyncManager.isTopicDownloaded(subjectId, unitId, subItem.id)
                                CompactGridSubtopicCard(
                                    code = subItem.code,
                                    title = subItem.title,
                                    questionsCount = subItem.questions.size,
                                    status = status,
                                    backgroundColor = bgCol,
                                    isDownloaded = isSubtopicDownloaded,
                                    onDownloadClick = {
                                        val (resSubject, resUnit, resTopic) = resolveHierarchyContext(
                                            subjectId = subjectId,
                                            unitId = unitId,
                                            topicId = topicId,
                                            fallbackSubject = subject,
                                            fallbackUnit = unit,
                                            fallbackTopic = activeTopicVal
                                        )
                                        com.example.util.OfflineSyncManager.downloadTopic(
                                            context = context,
                                            subject = resSubject,
                                            unit = resUnit ?: com.example.model.UnitModel(id = unitId, name = "Unit"),
                                            topic = resTopic,
                                            onComplete = {
                                                Toast.makeText(context, "${subItem.title} downloaded offline!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    },
                                    onClick = {
                                        onSelectSubtopic(subItem.practiceSet, subItem.title)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showPracticeAppearanceSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPracticeAppearanceSheet = false },
                containerColor = Color.White
            ) {
                PracticeAppearancePanel(
                    currentSettings = currentAppConfig.advancedFontSettings.practiceAppearance,
                    onSave = { newPa ->
                        PracticeAppearanceManager.saveSettings(context, newPa)
                        val updatedAdvanced = currentAppConfig.advancedFontSettings.copy(
                            practiceAppearance = newPa
                        )
                        FontSettingsManager.saveSettings(context, updatedAdvanced)
                        configRepository.updateAppConfig(
                            currentAppConfig.copy(advancedFontSettings = updatedAdvanced)
                        )
                        showPracticeAppearanceSheet = false
                        Toast.makeText(context, "Practice appearance applied!", Toast.LENGTH_SHORT).show()
                    },
                    onReset = {
                        PracticeAppearanceManager.resetToDefault(context)
                        val defPa = PracticeAppearanceSettings()
                        val updatedAdvanced = currentAppConfig.advancedFontSettings.copy(
                            practiceAppearance = defPa
                        )
                        FontSettingsManager.saveSettings(context, updatedAdvanced)
                        configRepository.updateAppConfig(
                            currentAppConfig.copy(advancedFontSettings = updatedAdvanced)
                        )
                        showPracticeAppearanceSheet = false
                        Toast.makeText(context, "Practice appearance reset to default", Toast.LENGTH_SHORT).show()
                    },
                    onBack = { showPracticeAppearanceSheet = false }
                )
            }
        }
    }
}

/**
 * Backward-compatible overload accepting [Subject], [UnitModel], [TopicModel] directly.
 */
@Composable
fun TopicSetsScreen(
    subject: Subject,
    unit: UnitModel?,
    topic: TopicModel?,
    onBack: () -> Unit,
    onNavigateToHome: (() -> Unit)? = null,
    onStartSet: (PracticeSetModel, List<PracticeSetModel>) -> Unit,
    onSelectSubtopic: (PracticeSetModel, String) -> Unit = { pSet, _ -> onStartSet(pSet, emptyList()) },
    testResults: List<com.example.model.TestResult> = emptyList(),
    onNavigateToSubscription: () -> Unit = {}
) {
    TopicSetsScreen(
        subjectId = subject.id,
        unitId = unit?.id ?: "",
        topicId = topic?.id ?: "",
        fallbackSubject = subject,
        fallbackUnit = unit,
        fallbackTopic = topic,
        onBack = onBack,
        onNavigateToHome = onNavigateToHome,
        onSelectSet = onStartSet,
        onSelectSubtopic = onSelectSubtopic,
        testResults = testResults,
        onNavigateToSubscription = onNavigateToSubscription
    )
}

// =========================================================================
// 4. FULL-SCREEN SUBTOPICS SCREEN FOR A SELECTED SET (Level 4 -> Level 5)
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetSubtopicsScreen(
    subjectId: String,
    unitId: String,
    topicId: String,
    setId: String,
    setNumber: Int = 1,
    setTitle: String = "",
    fallbackSubject: Subject? = null,
    fallbackUnit: UnitModel? = null,
    fallbackTopic: TopicModel? = null,
    onBack: () -> Unit,
    onNavigateToHome: (() -> Unit)? = null,
    onSelectSubtopic: (PracticeSetModel, String) -> Unit,
    testResults: List<com.example.model.TestResult> = emptyList(),
    onNavigateToSubscription: () -> Unit = {}
) {
    val context = LocalContext.current
    val subtopicsListState = rememberLazyListState()

    val (subject, unit, activeTopicVal) = remember(
        subjectId,
        unitId,
        topicId,
        SampleData.subjectsState.size
    ) {
        resolveHierarchyContext(subjectId, unitId, topicId, fallbackSubject, fallbackUnit, fallbackTopic)
    }

    val topicQuestions = remember(activeTopicVal.id, subject.id, unit?.id, SampleData.questionsState.size) {
        resolveTopicQuestions(subject, unit, activeTopicVal)
    }

    val setsList = remember(topicQuestions, subject.id, unit?.id, activeTopicVal.id) {
        generateSetsForQuestionsList(
            questions = topicQuestions,
            baseId = "set-${subject.id}-${unit?.id ?: ""}-${activeTopicVal.id}-all"
        )
    }

    val activeSet = remember(setsList, setId, setNumber, setTitle, topicQuestions) {
        setsList.find {
            it.id == setId ||
                it.setNumber == setNumber ||
                (setTitle.isNotBlank() && it.title.equals(setTitle, ignoreCase = true))
        } ?: setsList.firstOrNull() ?: PracticeSetModel(
            id = setId.ifBlank { "set-1" },
            title = setTitle.ifBlank { "Set %02d".format(setNumber.coerceAtLeast(1)) },
            subtitle = "${topicQuestions.size} Qs",
            setNumber = setNumber.coerceAtLeast(1),
            questionsCount = topicQuestions.size,
            startQuestionNumber = 1,
            endQuestionNumber = topicQuestions.size.coerceAtLeast(1),
            questions = topicQuestions
        )
    }

    data class ResolvedSubtopicItem(
        val id: String,
        val code: String,
        val title: String,
        val questions: List<Question>,
        val practiceSet: PracticeSetModel
    )

    val resolvedSubtopics = remember(activeSet, activeTopicVal, topicQuestions) {
        val setQuestions = activeSet.questions.ifEmpty { topicQuestions }
        val fromSetQuestions = setQuestions.map { it.subtopic }.filter { it.isNotBlank() }.distinct()
        val explicitTopicSubs = activeTopicVal.subtopics.filter { it.isNotBlank() }
        val combinedTitles = (fromSetQuestions + explicitTopicSubs).distinct().ifEmpty {
            if (setQuestions.isNotEmpty()) {
                listOf("${activeTopicVal.name} – Core Concepts")
            } else {
                emptyList()
            }
        }

        combinedTitles.mapIndexed { index, subTitle ->
            val matchedInSet = setQuestions.filter { it.subtopic.equals(subTitle, ignoreCase = true) }
            val matchedInTopic = topicQuestions.filter { it.subtopic.equals(subTitle, ignoreCase = true) }
            val assignedQuestions = when {
                matchedInSet.isNotEmpty() -> matchedInSet
                matchedInTopic.isNotEmpty() -> matchedInTopic
                setQuestions.isNotEmpty() -> {
                    if (combinedTitles.size <= 1) {
                        setQuestions
                    } else {
                        val chunkSize = (setQuestions.size / combinedTitles.size).coerceAtLeast(1)
                        val startIdx = (index * chunkSize).coerceAtMost(setQuestions.lastIndex)
                        val endIdx = if (index == combinedTitles.lastIndex) {
                            setQuestions.size
                        } else {
                            ((index + 1) * chunkSize).coerceAtMost(setQuestions.size)
                        }
                        setQuestions.subList(startIdx, endIdx.coerceAtLeast(startIdx + 1))
                    }
                }
                else -> emptyList()
            }

            val subId = "subtopic-${activeSet.id}-${index + 1}-${subTitle.hashCode()}"
            val code = "S%02d".format(index + 1)
            ResolvedSubtopicItem(
                id = subId,
                code = code,
                title = subTitle,
                questions = assignedQuestions,
                practiceSet = PracticeSetModel(
                    id = subId,
                    title = subTitle,
                    subtitle = "${assignedQuestions.size} Questions",
                    setNumber = index + 1,
                    questionsCount = assignedQuestions.size,
                    startQuestionNumber = 1,
                    endQuestionNumber = assignedQuestions.size.coerceAtLeast(1),
                    questions = assignedQuestions,
                    badge = activeSet.title
                )
            )
        }
    }

    val topicHeaderGradient = remember(activeTopicVal.id, activeTopicVal.name, subject.name) {
        val topicIdx = unit?.topics?.indexOfFirst { it.id == activeTopicVal.id }?.takeIf { it >= 0 } ?: 0
        PremiumCardColorSystem.getGradientForTopic(topicIdx, activeTopicVal.name, subject.name)
    }

    BackHandler {
        onBack()
    }

    val configRepository = remember { AppConfigRepository.getInstance() }
    val currentAppConfig by configRepository.currentAppConfig.collectAsState()
    var showPracticeAppearanceSheet by remember { mutableStateOf(false) }

    AppLuminousBackground(screenName = "Set Subtopics Screen") {
        Scaffold(
            topBar = {
                PremiumSectionHeader(
                    title = "${activeSet.title} • Subtopics",
                    level = HeaderLevel.SET,
                    onBack = onBack,
                    onNavigateToHome = onNavigateToHome,
                    rightAction = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { showPracticeAppearanceSheet = true }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "Practice Appearance Settings",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            PremiumSubscriptionBadge(
                                onNavigateToSubscription = onNavigateToSubscription
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                state = subtopicsListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. SET HERO SUMMARY CARD
                item(key = "set_header_${activeSet.id}") {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.5.dp, topicHeaderGradient.borderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .premiumTargetAnimation(
                                target = com.example.model.AnimatableTarget.SETS_CARDS,
                                index = 0,
                                screenKey = "Set Subtopics Screen"
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(104.dp)
                                .background(Brush.linearGradient(colors = topicHeaderGradient.colors))
                        ) {
                            TopicFhdCardImage(
                                topic = activeTopicVal,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.18f),
                                                Color.Black.copy(alpha = 0.42f),
                                                Color.Black.copy(alpha = 0.72f)
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${activeSet.title.uppercase()} • ${activeTopicVal.name.uppercase()}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = activeSet.subtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                        color = Color(0xFFFDE68A).copy(alpha = 0.9f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    StatBadge(
                                        label = "Subtopics",
                                        value = "${resolvedSubtopics.size}",
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.weight(1f),
                                        isCompact = true
                                    )
                                    StatBadge(
                                        label = "MCQs",
                                        value = "${activeSet.questionsCount}",
                                        color = Color(0xFF059669),
                                        modifier = Modifier.weight(1f),
                                        isCompact = true
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. SECTION HEADER FOR SUBTOPICS
                item(key = "set_subtopics_section_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SUBTOPICS (${resolvedSubtopics.size})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Select a subtopic to practice targeted questions",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                // 3. 2-COLUMN GRID OF SUBTOPICS
                if (resolvedSubtopics.isEmpty()) {
                    item(key = "empty_set_subtopics_state") {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No Subtopics Available",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF334155)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Subtopic content for this set is currently being updated.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                } else {
                    val solidColors = listOf(
                        Color(0xFF2563EB), Color(0xFF059669), Color(0xFF7C3AED),
                        Color(0xFFEA580C), Color(0xFFE11D48), Color(0xFF0891B2)
                    )
                    itemsIndexed(resolvedSubtopics, key = { _, subItem -> subItem.id }) { globalIndex, subItem ->
                        val bgCol = solidColors[globalIndex % solidColors.size]
                        val hasDbCompletedSub = testResults.any {
                            it.testName.contains(subItem.title, ignoreCase = true)
                        }
                        val status = PracticeSessionManager.getSessionStatus(
                            context, subItem.id, hasDbCompletedSub
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("set_subtopic_card_${subItem.id}")
                                .cascadeDropItem(
                                    visible = true,
                                    index = globalIndex,
                                    target = com.example.model.AnimatableTarget.SUBTOPIC_CARDS,
                                    screenKey = "Set Subtopics Screen"
                                )
                        ) {
                            CompactGridSubtopicCard(
                                code = subItem.code,
                                title = subItem.title,
                                questionsCount = subItem.questions.size,
                                status = status,
                                backgroundColor = bgCol,
                                onClick = {
                                    onSelectSubtopic(subItem.practiceSet, subItem.title)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showPracticeAppearanceSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPracticeAppearanceSheet = false },
                containerColor = Color.White
            ) {
                PracticeAppearancePanel(
                    currentSettings = currentAppConfig.advancedFontSettings.practiceAppearance,
                    onSave = { newPa ->
                        PracticeAppearanceManager.saveSettings(context, newPa)
                        val updatedAdvanced = currentAppConfig.advancedFontSettings.copy(
                            practiceAppearance = newPa
                        )
                        FontSettingsManager.saveSettings(context, updatedAdvanced)
                        configRepository.updateAppConfig(
                            currentAppConfig.copy(advancedFontSettings = updatedAdvanced)
                        )
                        showPracticeAppearanceSheet = false
                        Toast.makeText(context, "Practice appearance applied!", Toast.LENGTH_SHORT).show()
                    },
                    onReset = {
                        PracticeAppearanceManager.resetToDefault(context)
                        val defPa = PracticeAppearanceSettings()
                        val updatedAdvanced = currentAppConfig.advancedFontSettings.copy(
                            practiceAppearance = defPa
                        )
                        FontSettingsManager.saveSettings(context, updatedAdvanced)
                        configRepository.updateAppConfig(
                            currentAppConfig.copy(advancedFontSettings = updatedAdvanced)
                        )
                        showPracticeAppearanceSheet = false
                        Toast.makeText(context, "Practice appearance reset to default", Toast.LENGTH_SHORT).show()
                    },
                    onBack = { showPracticeAppearanceSheet = false }
                )
            }
        }
    }
}

@Composable
fun TopicSideBySideTabsBar(
    selectedTab: String,
    setsCount: Int,
    subtopicsCount: Int,
    onTabSelected: (String) -> Unit
) {
    val isSetsActive = selectedTab == "SETS"
    val isSubtopicsActive = selectedTab == "SUBTOPICS"

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Tab 1: SETS
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSetsActive) Color(0xFF0052CC) else Color.Transparent,
                shadowElevation = if (isSetsActive) 2.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onTabSelected("SETS") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = if (isSetsActive) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SETS",
                        color = if (isSetsActive) Color.White else Color(0xFF1E293B),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = if (isSetsActive) Color.White.copy(alpha = 0.25f) else Color(0xFFE2E8F0),
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$setsCount",
                                color = if (isSetsActive) Color.White else Color(0xFF1E293B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
            }

            // Tab 2: SUBTOPICS
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSubtopicsActive) Color(0xFF059669) else Color.Transparent,
                shadowElevation = if (isSubtopicsActive) 2.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onTabSelected("SUBTOPICS") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = if (isSubtopicsActive) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SUBTOPICS",
                        color = if (isSubtopicsActive) Color.White else Color(0xFF1E293B),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = if (isSubtopicsActive) Color.White.copy(alpha = 0.25f) else Color(0xFFE2E8F0),
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$subtopicsCount",
                                color = if (isSubtopicsActive) Color.White else Color(0xFF1E293B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private object PremiumStartButtonPalette {
    val DeepEmeraldGreen = Color(0xFF005B35)
    val DarkNavyBlue = Color(0xFF082B70)
    val RichBurgundy = Color(0xFF800020)
    val DeepTeal = Color(0xFF005F60)
    val DarkChocolateBrown = Color(0xFF4A2608)
    val MidnightPurple = Color(0xFF3B087A)
    val DeepForestGreen = Color(0xFF07552E)
    val RoyalBlue = Color(0xFF172FA8)
    val DeepPlum = Color(0xFF650044)
    val DeepMaroon = Color(0xFF720018)

    fun getContrastingColor(cardBg: Color, index: Int): Color {
        val r = cardBg.red
        val g = cardBg.green
        val b = cardBg.blue

        return when {
            // Mint / Completed Green or Emerald or Teal
            g > r && g >= b -> when (index % 3) {
                0 -> RichBurgundy
                1 -> DeepMaroon
                else -> DeepPlum
            }
            // Blue dominant (e.g. Royal Blue)
            b > r && b > g -> when (index % 3) {
                0 -> DeepMaroon
                1 -> DarkChocolateBrown
                else -> RichBurgundy
            }
            // Violet / Purple dominant
            r > 0.4f && b > 0.6f -> when (index % 3) {
                0 -> DeepEmeraldGreen
                1 -> DeepForestGreen
                else -> DarkChocolateBrown
            }
            // Orange / Amber dominant (e.g. In Progress or Orange card)
            r > 0.8f && g > 0.3f && b < 0.5f -> when (index % 3) {
                0 -> DarkNavyBlue
                1 -> MidnightPurple
                else -> RoyalBlue
            }
            // Ruby Red dominant
            r > 0.7f && g < 0.35f -> when (index % 3) {
                0 -> DeepEmeraldGreen
                1 -> DeepTeal
                else -> DarkNavyBlue
            }
            // Fallback cycling through the 10 premium solid colors
            else -> when (index % 10) {
                0 -> DeepEmeraldGreen
                1 -> DarkNavyBlue
                2 -> RichBurgundy
                3 -> DeepTeal
                4 -> DarkChocolateBrown
                5 -> MidnightPurple
                6 -> DeepForestGreen
                7 -> RoyalBlue
                8 -> DeepPlum
                else -> DeepMaroon
            }
        }
    }
}

@Composable
fun CompactGridSetCard(
    serialNumber: String,
    title: String,
    subtitle: String,
    status: String,
    backgroundColor: Color = Color.Unspecified,
    isDownloaded: Boolean = false,
    onDownloadClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val isCompleted = status.equals("Completed", true)
    val isInProgress = !isCompleted && (status.equals("Attempting", true) || status.equals("In Progress", true))

    val setNumInt = serialNumber.toIntOrNull() ?: 1
    val vibrantBg = PremiumCardColorSystem.vibrantReferenceColors[(setNumInt - 1) % PremiumCardColorSystem.vibrantReferenceColors.size]

    val cardBg = when {
        isCompleted -> Color(0xFF6EE7B7) // Rich mint green
        isInProgress -> Color(0xFFFDBA74) // Rich amber-orange
        else -> vibrantBg
    }

    val borderColor = when {
        isCompleted -> Color(0xFF059669)
        isInProgress -> Color(0xFFEA580C)
        else -> Color.White.copy(alpha = 0.5f)
    }

    val textColor = when {
        isCompleted -> Color(0xFF064E3B)
        isInProgress -> Color(0xFF7C2D12)
        else -> Color.White
    }

    val badgeBg = when {
        isCompleted -> Color(0xFF34D399)
        isInProgress -> Color(0xFFFB923C)
        else -> Color(0xFFA78BFA)
    }

    val badgeText = when {
        isCompleted -> "Completed"
        isInProgress -> "In Progress"
        else -> "Not Started"
    }

    val progress = when {
        isCompleted -> 1f
        isInProgress -> 0.5f
        else -> 0f
    }

    val progressPercentText = when {
        isCompleted -> "100%"
        isInProgress -> "50%"
        else -> "0%"
    }

    val progressBarColor = when {
        isCompleted -> Color(0xFF059669)
        isInProgress -> Color(0xFFEA580C)
        else -> Color(0xFF7C3AED)
    }

    val progressTrackColor = when {
        isCompleted -> Color(0xFF34D399).copy(alpha = 0.4f)
        isInProgress -> Color(0xFFFB923C).copy(alpha = 0.4f)
        else -> Color(0xFFA78BFA).copy(alpha = 0.4f)
    }

    val actionButtonText = when {
        isCompleted -> "Review"
        isInProgress -> "Continue"
        else -> "Start"
    }

    val actionButtonBg = PremiumStartButtonPalette.getContrastingColor(cardBg, setNumInt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(195.dp)
            .premiumPress(
                target = com.example.model.AnimatableTarget.SETS_CARDS,
                screenKey = "Topic Sets",
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(width = 1.8.dp, color = borderColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = when {
                        isCompleted -> Color(0xFF059669).copy(alpha = 0.15f)
                        isInProgress -> Color(0xFFEA580C).copy(alpha = 0.15f)
                        else -> Color(0xFF7C3AED).copy(alpha = 0.15f)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = serialNumber,
                            color = textColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onDownloadClick != null) {
                        IconButton(
                            onClick = onDownloadClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                                contentDescription = "Download Set",
                                tint = if (isDownloaded) Color(0xFF059669) else Color(0xFF2563EB),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = badgeBg
                    ) {
                        Text(
                            text = if (isDownloaded) "✓ Downloaded" else "• $badgeText",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = textColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    color = textColor.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = progressPercentText,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = progressBarColor,
                    trackColor = progressTrackColor
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(12.dp),
                color = actionButtonBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = actionButtonText,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun CompactGridSubtopicCard(
    code: String,
    title: String,
    questionsCount: Int,
    status: String,
    backgroundColor: Color = Color.Unspecified,
    isDownloaded: Boolean = false,
    onDownloadClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val isCompleted = status.equals("Completed", true)
    val isInProgress = !isCompleted && (status.equals("Attempting", true) || status.equals("In Progress", true))

    val codeInt = code.toIntOrNull() ?: 1
    val vibrantBg = PremiumCardColorSystem.vibrantReferenceColors[(codeInt - 1) % PremiumCardColorSystem.vibrantReferenceColors.size]

    val cardBg = when {
        isCompleted -> Color(0xFF6EE7B7) // Rich mint green
        isInProgress -> Color(0xFFFDBA74) // Rich amber-orange
        else -> vibrantBg
    }

    val borderColor = when {
        isCompleted -> Color(0xFF059669)
        isInProgress -> Color(0xFFEA580C)
        else -> Color.White.copy(alpha = 0.5f)
    }

    val textColor = when {
        isCompleted -> Color(0xFF064E3B)
        isInProgress -> Color(0xFF7C2D12)
        else -> Color.White
    }

    val badgeBg = when {
        isCompleted -> Color(0xFF34D399)
        isInProgress -> Color(0xFFFB923C)
        else -> Color(0xFFA78BFA)
    }

    val badgeText = when {
        isCompleted -> "Completed"
        isInProgress -> "In Progress"
        else -> "Not Started"
    }

    val progress = when {
        isCompleted -> 1f
        isInProgress -> 0.5f
        else -> 0f
    }

    val progressPercentText = when {
        isCompleted -> "100%"
        isInProgress -> "50%"
        else -> "0%"
    }

    val progressBarColor = when {
        isCompleted -> Color(0xFF059669)
        isInProgress -> Color(0xFFEA580C)
        else -> Color(0xFF7C3AED)
    }

    val progressTrackColor = when {
        isCompleted -> Color(0xFF34D399).copy(alpha = 0.4f)
        isInProgress -> Color(0xFFFB923C).copy(alpha = 0.4f)
        else -> Color(0xFFA78BFA).copy(alpha = 0.4f)
    }

    val actionButtonText = when {
        isCompleted -> "Review"
        isInProgress -> "Continue"
        else -> "Start"
    }

    val actionButtonBg = PremiumStartButtonPalette.getContrastingColor(cardBg, codeInt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(195.dp)
            .premiumPress(
                target = com.example.model.AnimatableTarget.SUBTOPIC_CARDS,
                screenKey = "Topic Sets",
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(width = 1.8.dp, color = borderColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = when {
                        isCompleted -> Color(0xFF059669).copy(alpha = 0.15f)
                        isInProgress -> Color(0xFFEA580C).copy(alpha = 0.15f)
                        else -> Color(0xFF7C3AED).copy(alpha = 0.15f)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = code,
                            color = textColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onDownloadClick != null) {
                        IconButton(
                            onClick = onDownloadClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                                contentDescription = "Download Subtopic",
                                tint = if (isDownloaded) Color(0xFF059669) else Color(0xFF2563EB),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = badgeBg
                    ) {
                        Text(
                            text = if (isDownloaded) "✓ Downloaded" else "• $badgeText",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = textColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$questionsCount Questions",
                    color = textColor.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = progressPercentText,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = progressBarColor,
                    trackColor = progressTrackColor
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(12.dp),
                color = actionButtonBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = actionButtonText,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF475569)
        )
    }
}