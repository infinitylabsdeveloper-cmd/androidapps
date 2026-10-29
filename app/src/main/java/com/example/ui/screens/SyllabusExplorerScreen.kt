package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.model.UnitModel
import com.example.ui.components.*
import com.example.ui.theme.AppLuminousBackground

/**
 * Full-Screen Units Screen (Level 1 -> Level 2 of the Learning Hierarchy):
 * Displays the selected Subject's summary header and its full-screen list of Unit cards.
 * Clicking a Unit card opens the dedicated full-screen [UnitTopicsScreen] destination
 * rather than expanding topics inline underneath the clicked card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusExplorerScreen(
    subjectId: String,
    fallbackSubject: Subject? = null,
    onBack: () -> Unit,
    onNavigateToHome: (() -> Unit)? = null,
    onSelectUnit: (Subject, UnitModel) -> Unit = { _, _ -> },
    onNavigateToSubscription: () -> Unit = {}
) {
    // Resolve Subject by stable ID from SampleData state
    val currentSubject = remember(subjectId, SampleData.subjectsState.size) {
        SampleData.subjectsState.find { it.id == subjectId } ?: fallbackSubject
    }
    val isPremium = SampleData.isUserPremium()
    var upgradePromptFeature by remember { mutableStateOf<String?>(null) }
    val unitsListState = rememberLazyListState()

    BackHandler {
        onBack()
    }

    AppLuminousBackground(screenName = "Syllabus Explorer") {
        Scaffold(
            topBar = {
                PremiumSectionHeader(
                    title = currentSubject?.name ?: "Units",
                    level = HeaderLevel.SUBJECT,
                    onBack = onBack,
                    onNavigateToHome = onNavigateToHome,
                    rightAction = {
                        PremiumSubscriptionBadge(
                            onNavigateToSubscription = onNavigateToSubscription
                        )
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (currentSubject == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HierarchyEmptyOrErrorCard(
                        title = "Subject Not Found",
                        subtitle = "The requested subject could not be loaded. Please go back and select another subject.",
                        actionLabel = "Go Back",
                        onAction = onBack
                    )
                }
            } else {
                val totalUnits = currentSubject.units.size
                val totalTopics = currentSubject.units.sumOf { it.topics.size }
                val totalSets = totalTopics * 2
                val totalMcqs = remember(currentSubject.id, SampleData.questionsState.size) {
                    SampleData.getQuestionsCountForSubject(currentSubject.id)
                }
                val subjGradient = remember(currentSubject.id, currentSubject.name) {
                    com.example.ui.theme.PremiumCardColorSystem.getGradientForSubject(
                        currentSubject.name,
                        currentSubject.id
                    )
                }

                LazyColumn(
                    state = unitsListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ==========================================
                    // 1. SUBJECT SUMMARY HEADER CARD
                    // ==========================================
                    item(key = "subject_header_${currentSubject.id}") {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(2.dp, subjGradient.borderColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .premiumTargetAnimation(
                                    target = com.example.model.AnimatableTarget.SUBJECT_CARDS,
                                    index = 0,
                                    screenKey = "Syllabus Explorer"
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(168.dp)
                                    .background(Brush.linearGradient(colors = subjGradient.colors))
                            ) {
                                SubjectFhdCardImage(
                                    subject = currentSubject,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.2f),
                                                    Color.Black.copy(alpha = 0.5f),
                                                    Color.Black.copy(alpha = 0.85f)
                                                )
                                            )
                                        )
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currentSubject.name.uppercase(),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.Black.copy(alpha = 0.45f),
                                            border = BorderStroke(1.dp, Color(0xFFFDE68A).copy(alpha = 0.6f))
                                        ) {
                                            Text(
                                                text = "$totalUnits UNITS",
                                                color = Color(0xFFFDE68A),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        StatBadge("Units", "$totalUnits", Color(0xFF2563EB), Modifier.weight(1f))
                                        StatBadge("Topics", "$totalTopics", Color(0xFF7C3AED), Modifier.weight(1f))
                                        StatBadge("Sets", "$totalSets", Color(0xFFD97706), Modifier.weight(1f))
                                        StatBadge("MCQs", "$totalMcqs", Color(0xFF059669), Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 2. SECTION HEADER
                    // ==========================================
                    item(key = "select_unit_instruction") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SELECT A UNIT",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Tap any unit card to open its full-screen topics page",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // ==========================================
                    // 3. FULL-SCREEN UNITS LIST (NO INLINE EXPANSION)
                    // ==========================================
                    if (currentSubject.units.isEmpty()) {
                        item(key = "empty_units_state") {
                            HierarchyEmptyOrErrorCard(
                                title = "No Units Available",
                                subtitle = "Syllabus units for ${currentSubject.name} are being updated."
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = currentSubject.units,
                            key = { _, unit -> unit.id },
                            contentType = { _, _ -> "unit_card_item" }
                        ) { unitIndex, unit ->
                            val isUnitLocked = !isPremium && unitIndex > 0
                            val unitMcqCount = remember(currentSubject.id, unit.id, SampleData.questionsState.size) {
                                unit.topics.sumOf {
                                    SampleData.getQuestionsForPractice(currentSubject.id, unit.id, it.id, "").size
                                }
                            }
                            val unitSetsCount = remember(unit.topics.size) { unit.topics.size * 2 }
                            val unitGradient = remember(unit.id, currentSubject.name) {
                                com.example.ui.theme.PremiumCardColorSystem.getGradientForUnit(
                                    unit.unitNumber,
                                    currentSubject.name,
                                    unit.name
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(178.dp)
                                    .testTag("unit_card_${unit.id}")
                                    .cascadeDropItem(
                                        visible = true,
                                        index = unitIndex,
                                        target = com.example.model.AnimatableTarget.UNIT_CARDS,
                                        screenKey = "Syllabus Explorer"
                                    )
                                    .premiumPress(
                                        target = com.example.model.AnimatableTarget.UNIT_CARDS,
                                        screenKey = "Syllabus Explorer"
                                    ) {
                                        if (isUnitLocked) {
                                            upgradePromptFeature =
                                                "Unit ${unit.unitNumber.takeIf { it > 0 } ?: (unitIndex + 1)}: ${unit.name}"
                                        } else {
                                            onSelectUnit(currentSubject, unit)
                                        }
                                    },
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isUnitLocked) Color(0xFFFFD700).copy(alpha = 0.6f)
                                    else unitGradient.borderColor
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.linearGradient(colors = unitGradient.colors))
                                ) {
                                    UnitFhdCardImage(
                                        unit = unit,
                                        subjectName = currentSubject.name,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = if (isUnitLocked) 0.35f else 0.15f),
                                                        Color.Black.copy(alpha = if (isUnitLocked) 0.65f else 0.45f),
                                                        Color.Black.copy(alpha = if (isUnitLocked) 0.90f else 0.82f)
                                                    )
                                                )
                                            )
                                    )

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Top Badges
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = unitGradient.badgeBg
                                            ) {
                                                Text(
                                                    text = "UNIT ${unit.unitNumber.takeIf { it > 0 } ?: (unitIndex + 1)}",
                                                    color = unitGradient.badgeText,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            if (isUnitLocked) {
                                                PremiumLockBadge(text = "LOCKED (PRO)", isSmall = true)
                                            } else {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Open Topics",
                                                        color = Color(0xFFFDE68A),
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = "Open Topics",
                                                        tint = Color(0xFFFDE68A),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Bottom Information
                                        Column {
                                            Text(
                                                text = SampleData.formatUnitTitle(unit.unitNumber, unit.name),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${unit.topics.size} Topics • $unitSetsCount Sets" +
                                                    if (unitMcqCount > 0) " • $unitMcqCount MCQs" else "",
                                                color = unitGradient.textColorSecondary,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                fontSize = 11.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 4. TOPICS LIST (NO INLINE EXPANSION)
                    // ==========================================
                    // ... this is handled in TopicSetsScreen ...
                    // (Actually the user meant Topic cards in UnitTopicsScreen)
                    // Continuing in SyllabusExplorerScreen.kt ... (wait, topics are in UnitTopicsScreen, 
                    // and I have SyllabusExplorerScreen which renders units)
                    // The topic cards for the user are likely in SyllabusExplorerScreen if they meant the Topic sets,
                    // but they specifically said "Topic cards". Let me check where UnitTopicsScreen uses Topic cards.



                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }




    upgradePromptFeature?.let { feature ->
        PremiumUpgradePromptDialog(
            title = "Unlock Premium Content",
            featureName = feature,
            description = "Free candidates can access only the first Unit and first Topic Set. Upgrade to Premium for unrestricted access to all syllabus units, topic MCQs, and mock tests.",
            onDismiss = { upgradePromptFeature = null },
            onUpgradeClick = {
                upgradePromptFeature = null
                onNavigateToSubscription()
            }
        )
    }
}
}

/**
 * Backward-compatible overload accepting [Subject] directly.
 */
@Composable
fun SyllabusExplorerScreen(
    subject: Subject,
    initialUnit: UnitModel? = null,
    onBack: () -> Unit,
    onNavigateToHome: (() -> Unit)? = null,
    onSelectUnit: (Subject, UnitModel) -> Unit = { _, _ -> },
    onSelectTopic: (UnitModel, TopicModel) -> Unit = { _, _ -> },
    onSelectSubjectSets: (Subject) -> Unit = { _ -> },
    onSelectUnitSets: (UnitModel) -> Unit = { _ -> },
    onNavigateToSubscription: () -> Unit = {}
) {
    SyllabusExplorerScreen(
        subjectId = subject.id,
        fallbackSubject = subject,
        onBack = onBack,
        onNavigateToHome = onNavigateToHome,
        onSelectUnit = onSelectUnit,
        onNavigateToSubscription = onNavigateToSubscription
    )
}

/**
 * Full-Screen Topics Screen for a Selected Unit (Level 2 -> Level 3 of the Learning Hierarchy):
 * Displays the selected Unit's summary header card and its full-screen list of Topic cards.
 * Clicking a Topic card opens the dedicated full-screen [TopicSetsScreen] destination.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitTopicsScreen(
    subjectId: String,
    unitId: String,
    fallbackSubject: Subject? = null,
    fallbackUnit: UnitModel? = null,
    onBack: () -> Unit,
    onNavigateToHome: (() -> Unit)? = null,
    onSelectTopic: (Subject, UnitModel, TopicModel) -> Unit = { _, _, _ -> },
    onNavigateToSubscription: () -> Unit = {}
) {
    val currentSubject = remember(subjectId, SampleData.subjectsState.size) {
        SampleData.subjectsState.find { it.id == subjectId } ?: fallbackSubject
    }
    val currentUnit = remember(currentSubject, unitId) {
        currentSubject?.units?.find { it.id == unitId } ?: fallbackUnit
    }
    val isPremium = SampleData.isUserPremium()
    var upgradePromptFeature by remember { mutableStateOf<String?>(null) }
    val topicsListState = rememberLazyListState()

    BackHandler {
        onBack()
    }

    AppLuminousBackground(screenName = "Unit Topics Screen") {
        Scaffold(
            topBar = {
                PremiumSectionHeader(
                    title = currentUnit?.name ?: "Unit Topics",
                    level = HeaderLevel.UNIT,
                    onBack = onBack,
                    onNavigateToHome = onNavigateToHome,
                    rightAction = {
                        PremiumSubscriptionBadge(
                            onNavigateToSubscription = onNavigateToSubscription
                        )
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (currentSubject == null || currentUnit == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HierarchyEmptyOrErrorCard(
                        title = "Unit Not Found",
                        subtitle = "The selected unit could not be found. Please return to the Units screen.",
                        actionLabel = "Back to Units",
                        onAction = onBack
                    )
                }
            } else {
                val unitIndex = remember(currentSubject.units, currentUnit.id) {
                    currentSubject.units.indexOfFirst { it.id == currentUnit.id }.coerceAtLeast(0)
                }
                val unitMcqCount = remember(currentSubject.id, currentUnit.id, SampleData.questionsState.size) {
                    currentUnit.topics.sumOf {
                        SampleData.getQuestionsForPractice(currentSubject.id, currentUnit.id, it.id, "").size
                    }
                }
                val unitSetsCount = remember(currentUnit.topics.size) { currentUnit.topics.size * 2 }
                val unitGradient = remember(currentUnit.id, currentSubject.name) {
                    com.example.ui.theme.PremiumCardColorSystem.getGradientForUnit(
                        currentUnit.unitNumber,
                        currentSubject.name,
                        currentUnit.name
                    )
                }

                LazyColumn(
                    state = topicsListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ==========================================
                    // 1. UNIT HERO SUMMARY HEADER CARD
                    // ==========================================
                    item(key = "unit_header_${currentUnit.id}") {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(2.dp, unitGradient.borderColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .premiumTargetAnimation(
                                    target = com.example.model.AnimatableTarget.UNIT_CARDS,
                                    index = 0,
                                    screenKey = "Unit Topics Screen"
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(148.dp)
                                    .background(Brush.linearGradient(colors = unitGradient.colors))
                            ) {
                                UnitFhdCardImage(
                                    unit = currentUnit,
                                    subjectName = currentSubject.name,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.2f),
                                                    Color.Black.copy(alpha = 0.5f),
                                                    Color.Black.copy(alpha = 0.85f)
                                                )
                                            )
                                        )
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = unitGradient.badgeBg
                                        ) {
                                            Text(
                                                text = "UNIT ${currentUnit.unitNumber.takeIf { it > 0 } ?: (unitIndex + 1)} • ${currentSubject.name.uppercase()}",
                                                color = unitGradient.badgeText,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = SampleData.formatUnitTitle(currentUnit.unitNumber, currentUnit.name),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color.White,
                                        fontSize = 15.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        StatBadge("Topics", "${currentUnit.topics.size}", Color(0xFF7C3AED), Modifier.weight(1f), isCompact = true)
                                        StatBadge("Sets", "$unitSetsCount", Color(0xFFD97706), Modifier.weight(1f), isCompact = true)
                                        StatBadge("MCQs", "$unitMcqCount", Color(0xFF059669), Modifier.weight(1f), isCompact = true)
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 2. SECTION INSTRUCTION
                    // ==========================================
                    item(key = "select_topic_instruction") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "UNIT TOPICS (${currentUnit.topics.size})",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Tap any topic card to open its full-screen practice sets page",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // ==========================================
                    // 3. FULL-SCREEN TOPICS LIST
                    // ==========================================
                    if (currentUnit.topics.isEmpty()) {
                        item(key = "empty_topics_state") {
                            HierarchyEmptyOrErrorCard(
                                title = "No Topics Available",
                                subtitle = "Topics for ${currentUnit.name} are being prepared."
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = currentUnit.topics,
                            key = { _, topic -> topic.id },
                            contentType = { _, _ -> "topic_card_item" }
                        ) { topicIndex, topic ->
                            val isTopicLocked = !isPremium && (unitIndex > 0 || topicIndex > 0)
                            val topicSets = remember(SampleData.questionsState.size, currentSubject.id, currentUnit.id, topic.id) {
                                SampleData.getPracticeSetsForTopic(currentSubject.id, currentUnit, topic)
                            }
                            val totalSetsCount = remember(topicSets, topic.practiceSetsCount) {
                                if (topicSets.isNotEmpty()) topicSets.size else maxOf(topic.practiceSetsCount, 2)
                            }
                            val totalMcqCount = remember(topicSets, currentSubject.id, currentUnit.id, topic.id, SampleData.questionsState.size) {
                                val fromSets = topicSets.sumOf { it.questionsCount }
                                if (fromSets > 0) fromSets else SampleData.getQuestionsForPractice(currentSubject.id, currentUnit.id, topic.id, "").size
                            }
                            val topicGradient = remember(topic.id, topic.name) {
                                com.example.ui.theme.PremiumCardColorSystem.getGradientForTopic(
                                    topicIndex,
                                    topic.name,
                                    currentSubject.name
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(182.dp)
                                    .testTag("topic_card_${topic.id}")
                                    .cascadeDropItem(
                                        visible = true,
                                        index = topicIndex,
                                        target = com.example.model.AnimatableTarget.TOPIC_CARDS,
                                        screenKey = "Unit Topics Screen"
                                    )
                                    .premiumPress(
                                        target = com.example.model.AnimatableTarget.TOPIC_CARDS,
                                        screenKey = "Unit Topics Screen"
                                    ) {
                                        if (isTopicLocked) {
                                            upgradePromptFeature = "Topic: ${topic.name}"
                                        } else {
                                            onSelectTopic(currentSubject, currentUnit, topic)
                                        }
                                    },
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isTopicLocked) Color(0xFFFFD700).copy(alpha = 0.6f)
                                    else topicGradient.borderColor
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.linearGradient(colors = topicGradient.colors))
                                ) {
                                    TopicFhdCardImage(
                                        topic = topic,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = if (isTopicLocked) 0.35f else 0.18f),
                                                        Color.Black.copy(alpha = if (isTopicLocked) 0.60f else 0.45f),
                                                        Color.Black.copy(alpha = if (isTopicLocked) 0.82f else 0.72f),
                                                        Color.Black.copy(alpha = if (isTopicLocked) 0.94f else 0.88f)
                                                    )
                                                )
                                            )
                                    )

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Top Row: Topic Badge + Sets/MCQs or Pro Lock
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = topicGradient.badgeBg
                                            ) {
                                                Text(
                                                    text = "TOPIC ${topicIndex + 1}",
                                                    color = topicGradient.badgeText,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 10.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                            if (isTopicLocked) {
                                                PremiumLockBadge(text = "LOCKED (PRO)", isSmall = true)
                                            } else {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = "$totalSetsCount Sets",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.5.sp
                                                        ),
                                                        color = Color(0xFFFDE68A)
                                                    )
                                                    Text(
                                                        text = "•",
                                                        color = Color.White.copy(alpha = 0.6f),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                                    )
                                                    Text(
                                                        text = "$totalMcqCount MCQs",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.5.sp
                                                        ),
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        // Bottom Row: Topic Name & Subtitle + Action Pill
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp)
                                            ) {
                                                 Text(
                                                    text = topic.name,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    ),
                                                    color = Color.White,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (!topic.subtitle.isNullOrBlank()) {
                                                    Text(
                                                        text = topic.subtitle,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                                        color = Color(0xFFFDE68A).copy(alpha = 0.9f),
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isTopicLocked) Color(0xFF0F172A) else Color(0xFF10B981),
                                                border = if (isTopicLocked) BorderStroke(1.dp, Color(0xFFFFD700)) else null
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (isTopicLocked) {
                                                        Icon(
                                                            imageVector = Icons.Default.Lock,
                                                            contentDescription = null,
                                                            tint = Color(0xFFFFD700),
                                                            modifier = Modifier.size(11.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = "Unlock",
                                                            color = Color(0xFFFFD700),
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    } else {
                                                        Text(
                                                            text = "Open Sets",
                                                            color = Color.White,
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(11.dp)
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

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    upgradePromptFeature?.let { feature ->
        PremiumUpgradePromptDialog(
            title = "Unlock Premium Content",
            featureName = feature,
            description = "Free candidates can access only the first Unit and first Topic Set. Upgrade to Premium for unrestricted access to all syllabus units, topic MCQs, and mock tests.",
            onDismiss = { upgradePromptFeature = null },
            onUpgradeClick = {
                upgradePromptFeature = null
                onNavigateToSubscription()
            }
        )
    }
}

@Composable
fun HierarchyEmptyOrErrorCard(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .premiumTargetAnimation(
                target = com.example.model.AnimatableTarget.EMPTY_ERROR_STATES,
                index = 0
            )
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
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onAction,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF2563EB))
                ) {
                    Text(
                        text = actionLabel,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
