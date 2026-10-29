package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.model.UnitModel
import com.example.ui.components.AppMotionSystem
import com.example.ui.components.SubjectFhdCardImage
import com.example.ui.components.TopicFhdCardImage
import com.example.ui.components.UnitFhdCardImage
import com.example.ui.components.cascadeDropItem
import com.example.util.OfflineSyncManager
import com.example.util.SyncStatus

/**
 * Subject Offline Hierarchy Drill-down Screen (Subject -> Unit -> Topic -> Sets -> MCQs)
 * 
 * Hierarchy Supported:
 * - Download Entire Subject
 * - Download a specific Unit
 * - Download a specific Topic
 * - Download individual Sets
 * 
 * Features:
 * - AES-256 Vault Encryption.
 * - Sticky bottom "Start Practicing" button (NO bottom Home or other irrelevant navigation buttons).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineSubjectDetailScreen(
    subject: Subject,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onStartPractice: (subjectId: String, unitId: String, topicId: String, setName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: Units, 1: Topics, 2: Sets
    val expandedUnitsState = remember { mutableStateMapOf<String, Boolean>() }

    val isSubjectDownloaded = remember(subject.id, OfflineSyncManager.downloadedPacks.size) {
        OfflineSyncManager.isSubjectDownloaded(subject.id)
    }

    val unitsList = remember(subject) {
        if (subject.units.isNotEmpty()) subject.units else listOf(
            UnitModel(
                id = "unit-1",
                name = "Core Foundations & Historical Background",
                unitNumber = 1,
                topics = listOf(
                    TopicModel(id = "top-1", name = "Syllabus Part 1", estimatedMcqs = "50 MCQs • 4 Sets"),
                    TopicModel(id = "top-2", name = "Syllabus Part 2", estimatedMcqs = "40 MCQs • 3 Sets")
                )
            ),
            UnitModel(
                id = "unit-2",
                name = "Applied Concepts & Case Studies",
                unitNumber = 2,
                topics = listOf(
                    TopicModel(id = "top-3", name = "Syllabus Part 3", estimatedMcqs = "40 MCQs • 3 Sets"),
                    TopicModel(id = "top-4", name = "Syllabus Part 4", estimatedMcqs = "60 MCQs • 3 Sets")
                )
            )
        )
    }

    // Auto-expand first unit initially
    LaunchedEffect(unitsList) {
        if (unitsList.isNotEmpty() && expandedUnitsState.isEmpty()) {
            expandedUnitsState[unitsList.first().id] = true
        }
    }

    val totalMcqsCount = remember(unitsList) {
        unitsList.sumOf { u ->
            u.topics.sumOf { t ->
                SampleData.getQuestionsForPractice(subject.id, u.id, t.id, "").size
            }
        }.let { if (it > 0) it else 320 }
    }

    val allSets = remember(unitsList) {
        unitsList.flatMap { u ->
            u.topics.flatMap { t ->
                listOf("Set 1", "Set 2", "Set 3", "Set 4").map { sName ->
                    Triple(u, t, sName)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF8FAFC),
        bottomBar = {
            // Sticky Start Practicing Bottom Bar (NO bottom Home/Mock tabs)
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isSubjectDownloaded) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = if (isSubjectDownloaded) Color(0xFF16A34A) else Color(0xFF2563EB),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (isSubjectDownloaded) "Offline Vault Ready" else "Download Available",
                                color = if (isSubjectDownloaded) Color(0xFF16A34A) else Color(0xFF2563EB),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "$totalMcqsCount MCQs • Zero Data Usage",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            val firstUnit = unitsList.firstOrNull()
                            val firstTopic = firstUnit?.topics?.firstOrNull()
                            onStartPractice(subject.id, firstUnit?.id ?: "", firstTopic?.id ?: "", "")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSubjectDownloaded) Color(0xFF16A34A) else Color(0xFF2563EB)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        modifier = Modifier.testTag("start_offline_practice_sticky_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Start Practicing",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Purple Gradient Subject Hero Header
            item {
                SubjectHeroHeader(
                    subject = subject,
                    totalMcqs = totalMcqsCount,
                    totalUnits = unitsList.size,
                    isDownloaded = isSubjectDownloaded,
                    onBack = onBack,
                    onNavigateToHome = onNavigateToHome,
                    onStartPractice = { onStartPractice(subject.id, "", "", "") },
                    onDownloadEntireSubject = {
                        OfflineSyncManager.downloadSubject(
                            context = context,
                            subject = subject,
                            onComplete = {
                                Toast.makeText(context, "${subject.name} downloaded successfully for offline use!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onRemoveEntireSubject = {
                        OfflineSyncManager.removeSubject(context, subject.id)
                        Toast.makeText(context, "${subject.name} offline data removed from device.", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // 2. Hierarchy Navigation Tabs (Units, Topics, Sets)
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    TabRow(
                        selectedTabIndex = selectedFilterTab,
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedFilterTab == 0,
                            onClick = { selectedFilterTab = 0 },
                            text = {
                                Text(
                                    "Units (${unitsList.size})",
                                    fontWeight = if (selectedFilterTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedFilterTab == 1,
                            onClick = { selectedFilterTab = 1 },
                            text = {
                                val totalTopics = unitsList.sumOf { it.topics.size }
                                Text(
                                    "Topics ($totalTopics)",
                                    fontWeight = if (selectedFilterTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedFilterTab == 2,
                            onClick = { selectedFilterTab = 2 },
                            text = {
                                Text(
                                    "Practice Sets",
                                    fontWeight = if (selectedFilterTab == 2) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            // 3. Tab Content Based on Selected Tab
            when (selectedFilterTab) {
                0 -> {
                    // Units & Hierarchical drill-down
                    itemsIndexed(unitsList, key = { _, u -> u.id }) { index, unit ->
                        val isExpanded = expandedUnitsState[unit.id] ?: false
                        val isUnitDownloaded = OfflineSyncManager.isUnitDownloaded(subject.id, unit.id)

                        UnitHierarchyCard(
                            subject = subject,
                            unit = unit,
                            unitIndex = index + 1,
                            isExpanded = isExpanded,
                            isUnitDownloaded = isUnitDownloaded,
                            onToggleExpand = {
                                if (isExpanded) {
                                    expandedUnitsState.remove(unit.id)
                                } else {
                                    expandedUnitsState.clear()
                                    expandedUnitsState[unit.id] = true
                                }
                            },
                            onDownloadUnit = {
                                OfflineSyncManager.downloadUnit(
                                    context = context,
                                    subject = subject,
                                    unit = unit,
                                    onComplete = {
                                        Toast.makeText(context, "${unit.name} downloaded offline!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onDownloadTopic = { topic ->
                                OfflineSyncManager.downloadTopic(
                                    context = context,
                                    subject = subject,
                                    unit = unit,
                                    topic = topic,
                                    onComplete = {
                                        Toast.makeText(context, "${topic.name} downloaded offline!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onDownloadSet = { topic, setName ->
                                OfflineSyncManager.downloadPracticeSet(
                                    context = context,
                                    subjectId = subject.id,
                                    unitId = unit.id,
                                    topicId = topic.id,
                                    setName = setName,
                                    onComplete = {
                                        Toast.makeText(context, "$setName ($topic.name) downloaded!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onStartPracticeTopic = { topic ->
                                onStartPractice(subject.id, unit.id, topic.id, "")
                            },
                            onStartPracticeSet = { topic, setName ->
                                onStartPractice(subject.id, unit.id, topic.id, setName)
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .cascadeDropItem(visible = true, index = index)
                        )
                    }
                }
                1 -> {
                    // All Topics Flat List
                    val allTopics = unitsList.flatMap { u -> u.topics.map { t -> Pair(u, t) } }
                    items(allTopics, key = { it.second.id }) { (unit, topic) ->
                        val isTopicDownloaded = OfflineSyncManager.isTopicDownloaded(subject.id, unit.id, topic.id)
                        TopicDirectCard(
                            subject = subject,
                            unit = unit,
                            unitName = unit.name,
                            topic = topic,
                            isDownloaded = isTopicDownloaded,
                            onDownload = {
                                OfflineSyncManager.downloadTopic(
                                    context = context,
                                    subject = subject,
                                    unit = unit,
                                    topic = topic,
                                    onComplete = {
                                        Toast.makeText(context, "${topic.name} downloaded offline!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onPractice = {
                                onStartPractice(subject.id, unit.id, topic.id, "")
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                2 -> {
                    // All Sets Flat View
                    items(allSets) { (unit, topic, setName) ->
                        val isSetDownloaded = OfflineSyncManager.isSetDownloaded(subject.id, unit.id, topic.id, setName)
                        PracticeSetDirectCard(
                            subjectName = subject.name,
                            unitName = unit.name,
                            topicName = topic.name,
                            setName = setName,
                            isDownloaded = isSetDownloaded,
                            onDownload = {
                                OfflineSyncManager.downloadPracticeSet(
                                    context = context,
                                    subjectId = subject.id,
                                    unitId = unit.id,
                                    topicId = topic.id,
                                    setName = setName,
                                    onComplete = {
                                        Toast.makeText(context, "$setName downloaded offline!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onPractice = {
                                onStartPractice(subject.id, unit.id, topic.id, setName)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Purple Gradient Subject Hero Header with Back, Stats, and Full Download Button
 */
@Composable
private fun SubjectHeroHeader(
    subject: Subject,
    totalMcqs: Int,
    totalUnits: Int,
    isDownloaded: Boolean,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onStartPractice: () -> Unit = {},
    onDownloadEntireSubject: () -> Unit,
    onRemoveEntireSubject: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // High-Resolution Thematic Card Image in Background
        SubjectFhdCardImage(
            subject = subject,
            modifier = Modifier.matchParentSize()
        )

        // Gradient Scrim Overlay for contrast & accessibility
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color(0xDD1E1B4B),
                            Color(0xF50F172A)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            // Top App Bar row with Back and Home buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("offline_detail_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onNavigateToHome) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = Color(0xFF93C5FD)
                        )
                    }
                }

                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFDE68A),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "AES-256 Vault",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = subject.name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$totalUnits Units • $totalMcqs MCQs • Multi-Level Offline Sync",
                color = Color(0xFFE0E7FF),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button: Download Entire Subject vs Subject Downloaded
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isDownloaded) {
                    Button(
                        onClick = onStartPractice,
                        enabled = true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Practice Subject MCQs", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onRemoveEntireSubject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                } else {
                    Button(
                        onClick = onDownloadEntireSubject,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF4338CA)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Download Entire Subject ($totalMcqs MCQs)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Expandable Unit Card with Topics and Sets
 */
@Composable
private fun UnitHierarchyCard(
    subject: Subject,
    unit: UnitModel,
    unitIndex: Int,
    isExpanded: Boolean,
    isUnitDownloaded: Boolean,
    onToggleExpand: () -> Unit,
    onDownloadUnit: () -> Unit,
    onDownloadTopic: (TopicModel) -> Unit,
    onDownloadSet: (TopicModel, String) -> Unit,
    onStartPracticeTopic: (TopicModel) -> Unit,
    onStartPracticeSet: (TopicModel, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalUnitMcqs = remember(unit) {
        unit.topics.size * 50
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            1.dp,
            if (isUnitDownloaded) Color(0xFF86EFAC) else Color(0xFFE2E8F0)
        ),
        modifier = modifier.shadow(2.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Unit Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 44.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        UnitFhdCardImage(
                            unit = unit,
                            subjectName = subject.name,
                            subjectId = subject.id,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.50f)
                                        )
                                    )
                                )
                        )
                        Surface(
                            shape = RoundedCornerShape(bottomEnd = 6.dp),
                            color = if (isUnitDownloaded) Color(0xFF16A34A) else Color(0xFF4338CA),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "U$unitIndex",
                                fontWeight = FontWeight.Black,
                                fontSize = 9.5.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = unit.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${unit.topics.size} Topics • $totalUnitMcqs MCQs",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isUnitDownloaded) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF0FDF4)
                        ) {
                            Text(
                                text = "SAVED",
                                color = Color(0xFF15803D),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onDownloadUnit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Unit",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    val chevronRotation = AppMotionSystem.animateAccordionChevronRotation(
                        isExpanded = isExpanded,
                        label = "OfflineUnitChevron_${unit.id}"
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Toggle",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.rotate(chevronRotation)
                    )
                }
            }

            // Expanded Child Content (Topics & Sets)
            AnimatedVisibility(
                visible = isExpanded,
                enter = AppMotionSystem.cascadeContainerEnterTransition(),
                exit = AppMotionSystem.cascadeContainerExitTransition()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    unit.topics.forEachIndexed { tIdx, topic ->
                        val isTopicSaved = isUnitDownloaded || OfflineSyncManager.isTopicDownloaded("", unit.id, topic.id)

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .cascadeDropItem(visible = isExpanded, index = tIdx)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Topic ${tIdx + 1}: ${topic.name}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = (topic.estimatedMcqs ?: "").ifBlank { "50 MCQs • 4 Sets" },
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (!isTopicSaved) {
                                            IconButton(
                                                onClick = { onDownloadTopic(topic) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = "Download Topic",
                                                    tint = Color(0xFF4F46E5),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        TextButton(
                                            onClick = { onStartPracticeTopic(topic) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                "Practice",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2563EB)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Individual Sets Badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Set 1", "Set 2", "Set 3", "Set 4").forEach { sName ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF1F5F9),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onStartPracticeSet(topic, sName) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = sName,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF334155)
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
}

/**
 * Topic Flat Card for Topic Tab
 */
@Composable
private fun TopicDirectCard(
    subject: Subject? = null,
    unit: UnitModel? = null,
    unitName: String,
    topic: TopicModel,
    isDownloaded: Boolean,
    onDownload: () -> Unit,
    onPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TopicFhdCardImage(
                topic = topic,
                unitId = unit?.id.orEmpty(),
                subjectId = subject?.id.orEmpty(),
                modifier = Modifier
                    .size(width = 52.dp, height = 52.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = unitName,
                    fontSize = 10.5.sp,
                    color = Color(0xFF6366F1),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = topic.name,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = (topic.estimatedMcqs ?: "").ifBlank { "50 MCQs • 4 Sets" },
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isDownloaded) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "OFFLINE",
                            color = Color(0xFF166534),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    IconButton(onClick = onDownload) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Topic",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Button(
                    onClick = onPractice,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Practice", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Practice Set Direct Card for Sets Tab
 */
@Composable
private fun PracticeSetDirectCard(
    subjectName: String,
    unitName: String,
    topicName: String,
    setName: String,
    isDownloaded: Boolean,
    onDownload: () -> Unit,
    onPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$unitName • $topicName",
                    fontSize = 10.5.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = setName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "25 MCQs • Multi-Format",
                    fontSize = 11.sp,
                    color = Color(0xFF059669),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isDownloaded) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Saved",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    IconButton(onClick = onDownload) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Set",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Button(
                    onClick = onPractice,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Start", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
