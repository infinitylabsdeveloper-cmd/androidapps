package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.*

enum class LiveMetricsSheetType {
    SUBJECTS,
    UNITS,
    TOPICS,
    PRACTICE_SETS,
    TOTAL_MCQS
}

/**
 * Bottom Sheet displaying full breakdown when user taps any box
 * on the Live Content Dashboard (Subjects, Units, Topics, Practice Sets, Total MCQs).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMetricsDetailBottomSheet(
    sheetType: LiveMetricsSheetType,
    displaySubjects: List<Subject>,
    currentAppId: String,
    onDismissRequest: () -> Unit,
    onSelectSubject: (Subject) -> Unit,
    onSelectUnit: (Subject, UnitModel) -> Unit,
    onSelectTopic: (Subject, UnitModel, TopicModel) -> Unit,
    onNavigateToPracticeSets: () -> Unit,
    onNavigateToMockTests: () -> Unit,
    onNavigateToCurrentAffairs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf<String?>(null) }

    val sheetConfig = remember(sheetType) {
        when (sheetType) {
            LiveMetricsSheetType.SUBJECTS -> MetricSheetHeaderConfig(
                title = "Total Available Subjects",
                subtitle = "Complete curriculum syllabus categories for current exam",
                icon = Icons.AutoMirrored.Filled.Article,
                gradientStart = Color(0xFFFF526F),
                gradientEnd = Color(0xFFFA2552)
            )
            LiveMetricsSheetType.UNITS -> MetricSheetHeaderConfig(
                title = "Total Available Units",
                subtitle = "Module-level divisions organized across subjects",
                icon = Icons.Default.Layers,
                gradientStart = Color(0xFF00C853),
                gradientEnd = Color(0xFF00A844)
            )
            LiveMetricsSheetType.TOPICS -> MetricSheetHeaderConfig(
                title = "Total Available Topics",
                subtitle = "Granular syllabus topics with dedicated question banks",
                icon = Icons.Default.MenuBook,
                gradientStart = Color(0xFFFFB300),
                gradientEnd = Color(0xFFFF8F00)
            )
            LiveMetricsSheetType.PRACTICE_SETS -> MetricSheetHeaderConfig(
                title = "Total Practice Sets",
                subtitle = "Curated question bundles across subjects and current affairs",
                icon = Icons.Default.GridView,
                gradientStart = Color(0xFF8B5CF6),
                gradientEnd = Color(0xFF6D28D9)
            )
            LiveMetricsSheetType.TOTAL_MCQS -> MetricSheetHeaderConfig(
                title = "Total Question Bank (MCQs)",
                subtitle = "Active verified practice questions and simulated mock sets",
                icon = Icons.Default.HelpOutline,
                gradientStart = Color(0xFFFF2D87),
                gradientEnd = Color(0xFFE91E63)
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFFF8FAFC),
        scrimColor = Color.Black.copy(alpha = 0.55f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFCBD5E1))
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .navigationBarsPadding()
                .premiumTargetAnimation(
                    target = com.example.model.AnimatableTarget.BOTTOM_SHEETS,
                    index = 0
                )
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .premiumHeroExpand(
                        target = com.example.model.AnimatableTarget.LIVE_CONTENT_DASHBOARD,
                        isExpanded = true,
                        index = 0
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(sheetConfig.gradientStart, sheetConfig.gradientEnd)
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(36.dp),
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = sheetConfig.icon,
                                    contentDescription = null,
                                    tint = sheetConfig.gradientEnd,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = sheetConfig.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = sheetConfig.subtitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Search Bar (for Units, Topics, Sets, and MCQs)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        text = when (sheetType) {
                            LiveMetricsSheetType.SUBJECTS -> "Search subjects..."
                            LiveMetricsSheetType.UNITS -> "Search units or subjects..."
                            LiveMetricsSheetType.TOPICS -> "Search topics or subtopics..."
                            LiveMetricsSheetType.PRACTICE_SETS -> "Search practice sets..."
                            LiveMetricsSheetType.TOTAL_MCQS -> "Filter MCQs by subject..."
                        },
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = sheetConfig.gradientEnd,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            // Subject Filter Chips (when viewing Units, Topics or Sets)
            if (sheetType in setOf(LiveMetricsSheetType.UNITS, LiveMetricsSheetType.TOPICS, LiveMetricsSheetType.PRACTICE_SETS)) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedSubjectFilter == null,
                            onClick = { selectedSubjectFilter = null },
                            label = { Text("All (${displaySubjects.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = sheetConfig.gradientEnd,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF475569)
                            ),
                            border = BorderStroke(1.dp, if (selectedSubjectFilter == null) sheetConfig.gradientEnd else Color(0xFFE2E8F0))
                        )
                    }
                    items(displaySubjects) { subject ->
                        FilterChip(
                            selected = selectedSubjectFilter == subject.id,
                            onClick = {
                                selectedSubjectFilter = if (selectedSubjectFilter == subject.id) null else subject.id
                            },
                            label = {
                                Text(
                                    text = subject.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = sheetConfig.gradientEnd,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF475569)
                            ),
                            border = BorderStroke(1.dp, if (selectedSubjectFilter == subject.id) sheetConfig.gradientEnd else Color(0xFFE2E8F0))
                        )
                    }
                }
            }

            // Body Content based on Sheet Type
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (sheetType) {
                    LiveMetricsSheetType.SUBJECTS -> {
                        SubjectsSheetContent(
                            subjects = displaySubjects,
                            currentAppId = currentAppId,
                            searchQuery = searchQuery,
                            onSelectSubject = {
                                onDismissRequest()
                                onSelectSubject(it)
                            },
                            onSelectCurrentAffairs = {
                                onDismissRequest()
                                onNavigateToCurrentAffairs()
                            }
                        )
                    }
                    LiveMetricsSheetType.UNITS -> {
                        UnitsSheetContent(
                            subjects = displaySubjects,
                            searchQuery = searchQuery,
                            selectedSubjectId = selectedSubjectFilter,
                            onSelectUnit = { subject, unit ->
                                onDismissRequest()
                                onSelectUnit(subject, unit)
                            }
                        )
                    }
                    LiveMetricsSheetType.TOPICS -> {
                        TopicsSheetContent(
                            subjects = displaySubjects,
                            searchQuery = searchQuery,
                            selectedSubjectId = selectedSubjectFilter,
                            onSelectTopic = { subject, unit, topic ->
                                onDismissRequest()
                                onSelectTopic(subject, unit, topic)
                            }
                        )
                    }
                    LiveMetricsSheetType.PRACTICE_SETS -> {
                        PracticeSetsSheetContent(
                            subjects = displaySubjects,
                            searchQuery = searchQuery,
                            selectedSubjectId = selectedSubjectFilter,
                            onSelectTopicSet = { subject, unit, topic ->
                                onDismissRequest()
                                onSelectTopic(subject, unit, topic)
                            }
                        )
                    }
                    LiveMetricsSheetType.TOTAL_MCQS -> {
                        TotalMcqsSheetContent(
                            subjects = displaySubjects,
                            currentAppId = currentAppId,
                            searchQuery = searchQuery,
                            onSelectSubject = {
                                onDismissRequest()
                                onSelectSubject(it)
                            },
                            onNavigateToMockTests = {
                                onDismissRequest()
                                onNavigateToMockTests()
                            },
                            onNavigateToCurrentAffairs = {
                                onDismissRequest()
                                onNavigateToCurrentAffairs()
                            }
                        )
                    }
                }
            }
        }
    }
}

private data class MetricSheetHeaderConfig(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradientStart: Color,
    val gradientEnd: Color
)

/**
 * 1. SUBJECTS SHEET CONTENT
 */
@Composable
private fun SubjectsSheetContent(
    subjects: List<Subject>,
    currentAppId: String,
    searchQuery: String,
    onSelectSubject: (Subject) -> Unit,
    onSelectCurrentAffairs: () -> Unit
) {
    val filteredSubjects = remember(subjects, searchQuery) {
        if (searchQuery.isBlank()) subjects
        else subjects.filter { it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Featured Current Affairs Subject Box
        if (searchQuery.isBlank() || "current affairs".contains(searchQuery, ignoreCase = true)) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .dashboardPressAnimation(onClick = { onSelectCurrentAffairs() }),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.2.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Current Affairs & Live News",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFDC2626)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Daily articles, monthly compilations & high-yield MCQs",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val caCount = SampleData.getCurrentAffairsForApp(currentAppId).size
                                MetricSmallPill(text = "$caCount Articles", color = Color(0xFFDC2626))
                                MetricSmallPill(text = "Monthly Bundles", color = Color(0xFFE11D48))
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        items(filteredSubjects) { subject ->
            val totalUnits = subject.units.size
            val totalTopics = subject.units.sumOf { it.topics.size }
            val mcqCount = SampleData.getQuestionsCountForSubject(subject.id)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .dashboardPressAnimation(onClick = { onSelectSubject(subject) }),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Article,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = subject.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFDBEAFE)
                            ) {
                                Text(
                                    text = subject.displayCategoryLabel,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D4ED8),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = subject.heroQuote.replace("\n", " • "),
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MetricSmallPill(text = "$totalUnits Units", color = Color(0xFF059669))
                            MetricSmallPill(text = "$totalTopics Topics", color = Color(0xFFD97706))
                            MetricSmallPill(text = "$mcqCount MCQs", color = Color(0xFF7C3AED))
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * 2. UNITS SHEET CONTENT
 */
@Composable
private fun UnitsSheetContent(
    subjects: List<Subject>,
    searchQuery: String,
    selectedSubjectId: String?,
    onSelectUnit: (Subject, UnitModel) -> Unit
) {
    data class FlattenedUnit(val subject: Subject, val unit: UnitModel)

    val allUnits = remember(subjects) {
        subjects.flatMap { subject ->
            subject.units.map { unit -> FlattenedUnit(subject, unit) }
        }
    }

    val filteredUnits = remember(allUnits, searchQuery, selectedSubjectId) {
        allUnits.filter { item ->
            val matchSubject = selectedSubjectId == null || item.subject.id == selectedSubjectId
            val matchSearch = searchQuery.isBlank() ||
                    item.unit.name.contains(searchQuery, ignoreCase = true) ||
                    item.subject.name.contains(searchQuery, ignoreCase = true) ||
                    (item.unit.subtitle?.contains(searchQuery, ignoreCase = true) == true)
            matchSubject && matchSearch
        }
    }

    if (filteredUnits.isEmpty()) {
        EmptyStateNotice(message = "No units found matching your search.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredUnits) { item ->
                val unitQuestions = SampleData.questionsState.count {
                    it.subjectId == item.subject.id && it.unitId == item.unit.id && it.isActive
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .dashboardPressAnimation(onClick = { onSelectUnit(item.subject, item.unit) }),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "U${item.unit.unitNumber}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.unit.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.subject.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                MetricSmallPill(text = "${item.unit.topics.size} Topics", color = Color(0xFF15803D))
                                if (unitQuestions > 0) {
                                    MetricSmallPill(text = "$unitQuestions MCQs", color = Color(0xFF2563EB))
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. TOPICS SHEET CONTENT
 */
@Composable
private fun TopicsSheetContent(
    subjects: List<Subject>,
    searchQuery: String,
    selectedSubjectId: String?,
    onSelectTopic: (Subject, UnitModel, TopicModel) -> Unit
) {
    data class FlattenedTopic(val subject: Subject, val unit: UnitModel, val topic: TopicModel)

    val allTopics = remember(subjects) {
        subjects.flatMap { subject ->
            subject.units.flatMap { unit ->
                unit.topics.map { topic -> FlattenedTopic(subject, unit, topic) }
            }
        }
    }

    val filteredTopics = remember(allTopics, searchQuery, selectedSubjectId) {
        allTopics.filter { item ->
            val matchSubject = selectedSubjectId == null || item.subject.id == selectedSubjectId
            val matchSearch = searchQuery.isBlank() ||
                    item.topic.name.contains(searchQuery, ignoreCase = true) ||
                    item.unit.name.contains(searchQuery, ignoreCase = true) ||
                    item.subject.name.contains(searchQuery, ignoreCase = true) ||
                    item.topic.subtopics.any { it.contains(searchQuery, ignoreCase = true) }
            matchSubject && matchSearch
        }
    }

    if (filteredTopics.isEmpty()) {
        EmptyStateNotice(message = "No topics found matching your search.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredTopics) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .dashboardPressAnimation(onClick = { onSelectTopic(item.subject, item.unit, item.topic) }),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.topic.name,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.subject.name} • Unit ${item.unit.unitNumber}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                MetricSmallPill(text = "${item.topic.practiceSetsCount} Sets", color = Color(0xFFD97706))
                                MetricSmallPill(text = item.topic.difficulty, color = Color(0xFF2563EB))
                                item.topic.tagBadge?.let {
                                    MetricSmallPill(text = it, color = Color(0xFF10B981))
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 4. PRACTICE SETS SHEET CONTENT
 */
@Composable
private fun PracticeSetsSheetContent(
    subjects: List<Subject>,
    searchQuery: String,
    selectedSubjectId: String?,
    onSelectTopicSet: (Subject, UnitModel, TopicModel) -> Unit
) {
    data class DisplayPracticeSet(
        val title: String,
        val subtitle: String,
        val contextName: String,
        val questionsCount: Int,
        val difficulty: String,
        val subjectId: String,
        val subject: Subject,
        val unit: UnitModel,
        val topic: TopicModel
    )

    val setsList = remember(subjects) {
        val list = mutableListOf<DisplayPracticeSet>()
        subjects.forEach { subject ->
            subject.units.forEach { unit ->
                unit.topics.forEach { topic ->
                    val sets = SampleData.getPracticeSetsForTopic(subject.id, unit, topic)
                    if (sets.isNotEmpty()) {
                        sets.forEach { s ->
                            list.add(
                                DisplayPracticeSet(
                                    title = "${topic.name} – ${s.title}",
                                    subtitle = s.subtitle,
                                    contextName = "${subject.name} • Unit ${unit.unitNumber}",
                                    questionsCount = s.questionsCount,
                                    difficulty = s.difficulty,
                                    subjectId = subject.id,
                                    subject = subject,
                                    unit = unit,
                                    topic = topic
                                )
                            )
                        }
                    } else {
                        // Syllabus configured sets
                        val count = maxOf(topic.practiceSetsCount, 2)
                        for (i in 1..count) {
                            list.add(
                                DisplayPracticeSet(
                                    title = "${topic.name} – Practice Set $i",
                                    subtitle = "10 Qs • High Yield",
                                    contextName = "${subject.name} • Unit ${unit.unitNumber}",
                                    questionsCount = 10,
                                    difficulty = topic.difficulty,
                                    subjectId = subject.id,
                                    subject = subject,
                                    unit = unit,
                                    topic = topic
                                )
                            )
                        }
                    }
                }
            }
        }
        list
    }

    val filteredSets = remember(setsList, searchQuery, selectedSubjectId) {
        setsList.filter { item ->
            val matchSubject = selectedSubjectId == null || item.subjectId == selectedSubjectId
            val matchSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.contextName.contains(searchQuery, ignoreCase = true)
            matchSubject && matchSearch
        }
    }

    if (filteredSets.isEmpty()) {
        EmptyStateNotice(message = "No practice sets found matching your search.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredSets) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .dashboardPressAnimation(onClick = { onSelectTopicSet(item.subject, item.unit, item.topic) }),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEDE9FE),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.contextName,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                MetricSmallPill(text = "${item.questionsCount} MCQs", color = Color(0xFF7C3AED))
                                MetricSmallPill(text = item.difficulty, color = Color(0xFF0284C7))
                            }
                        }

                        Button(
                            onClick = { onSelectTopicSet(item.subject, item.unit, item.topic) },
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text("Start", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 5. TOTAL MCQS SHEET CONTENT
 */
@Composable
private fun TotalMcqsSheetContent(
    subjects: List<Subject>,
    currentAppId: String,
    searchQuery: String,
    onSelectSubject: (Subject) -> Unit,
    onNavigateToMockTests: () -> Unit,
    onNavigateToCurrentAffairs: () -> Unit
) {
    val totalSubjectMcqs = remember(subjects) {
        subjects.sumOf { SampleData.getQuestionsCountForSubject(it.id) }
    }
    val caMcqs = remember(currentAppId) {
        SampleData.getCurrentAffairsForApp(currentAppId).sumOf { it.attachedMcqs.size } +
                SampleData.getMonthlyCurrentAffairs().sumOf { m -> m.sets.sumOf { it.questions.size } }
    }
    val grandTotal = totalSubjectMcqs + caMcqs

    val filteredSubjects = remember(subjects, searchQuery) {
        if (searchQuery.isBlank()) subjects
        else subjects.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Grand Total Question Bank Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Grand Total Question Bank",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "$grandTotal Active Questions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE11D48)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFE4E6),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFE11D48),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Distribution summary pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$totalSubjectMcqs", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text("Syllabus MCQs", fontSize = 9.sp, color = Color(0xFF64748B))
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$caMcqs", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text("Current Affairs", fontSize = 9.sp, color = Color(0xFF64748B))
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToCurrentAffairs,
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE11D48)),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text("Current Affairs", fontSize = 11.sp, color = Color(0xFFE11D48), fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = onNavigateToMockTests,
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text("Mock Generator", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Section Title: Breakdown by Subject
        item {
            Text(
                text = "Question Distribution by Subject",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
            )
        }

        items(filteredSubjects) { subject ->
            val mcqCount = SampleData.getQuestionsCountForSubject(subject.id)
            val progress = if (grandTotal > 0) (mcqCount.toFloat() / grandTotal.toFloat()).coerceIn(0.05f, 1f) else 0f

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .dashboardPressAnimation(onClick = { onSelectSubject(subject) }),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            color = Color(0xFF0F172A),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$mcqCount MCQs",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE11D48)
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFE11D48),
                        trackColor = Color(0xFFFFE4E6)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${subject.units.size} Units • ${subject.units.sumOf { it.topics.size }} Topics",
                            fontSize = 10.5.sp,
                            color = Color(0xFF64748B)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Explore Practice",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2563EB)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricSmallPill(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun EmptyStateNotice(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )
    }
}
