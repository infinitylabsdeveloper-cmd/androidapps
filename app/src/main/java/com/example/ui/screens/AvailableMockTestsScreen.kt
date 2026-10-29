package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.GeneratedMockTest
import com.example.model.MockTestAttemptRecord
import com.example.model.MockTestMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AvailableMockTestsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToGenerator: () -> Unit,
    onStartTest: (GeneratedMockTest) -> Unit,
    onResumeTest: (GeneratedMockTest) -> Unit,
    onReattemptTest: (GeneratedMockTest) -> Unit,
    onViewResult: (GeneratedMockTest) -> Unit,
    onNavigateToSubscription: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeApp = SampleData.getActiveApp()
    val userProfile = SampleData.userProfileState.value

    // Seed sample tests if empty for preview
    LaunchedEffect(activeApp.id) {
        SampleData.seedDefaultMockTestsIfEmpty(activeApp.id)
    }

    // Currently selected category (null = 1st screen with 3 category cards)
    var selectedCategory by remember { mutableStateOf<MockTestMode?>(null) }
    
    // Status filter state for the selected category
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    var testToDelete by remember { mutableStateOf<GeneratedMockTest?>(null) }
    var historyTestDialog by remember { mutableStateOf<GeneratedMockTest?>(null) }
    var testMenuExpandedId by remember { mutableStateOf<String?>(null) }

    // Intercept hardware back button
    BackHandler {
        if (selectedCategory != null) {
            selectedCategory = null
        } else {
            onNavigateBack()
        }
    }

    val appRepository = remember(activeApp.id) {
        com.example.repository.AppRepository(com.example.db.AppDatabase.getDatabase(context), activeApp.id)
    }

    // Dynamic user-specific mock test filtering (isolated by app & user)
    val allMockTestsSnapshot = SampleData.generatedMockTestsState.toList()
    val userTests = remember(allMockTestsSnapshot, activeApp.id, userProfile.email) {
        allMockTestsSnapshot.filter {
            it.appId == activeApp.id || it.appId.isBlank()
        }
    }

    // Category counts dynamically calculated from the database
    val fullLengthTests = remember(userTests) {
        userTests.filter { it.testMode == MockTestMode.FULL_LENGTH }
            .sortedBy { it.createdAt }
    }
    val unitWiseTests = remember(userTests) {
        userTests.filter { it.testMode == MockTestMode.UNIT_WISE || it.testMode == MockTestMode.SUBJECT_WISE }
            .sortedBy { it.createdAt }
    }
    val topicWiseTests = remember(userTests) {
        userTests.filter { it.testMode == MockTestMode.TOPIC_WISE }
            .sortedBy { it.createdAt }
    }

    // Active category tests list
    val currentCategoryTests = when (selectedCategory) {
        MockTestMode.FULL_LENGTH -> fullLengthTests
        MockTestMode.UNIT_WISE -> unitWiseTests
        MockTestMode.TOPIC_WISE -> topicWiseTests
        MockTestMode.SUBJECT_WISE -> unitWiseTests
        null -> emptyList()
    }

    // Filter tests by selected status
    val filteredCategoryTests = remember(selectedCategory, selectedStatusFilter, currentCategoryTests) {
        when (selectedStatusFilter) {
            "COMPLETED" -> currentCategoryTests.filter { it.status == "Completed" }
            "CONTINUING" -> currentCategoryTests.filter { 
                it.status == "In Progress" || it.status == "Continuing" || (it.status != "Completed" && it.userAnswers.isNotEmpty()) 
            }
            "NOT ATTEMPTED" -> currentCategoryTests.filter { 
                it.status != "Completed" && it.status != "In Progress" && it.status != "Continuing" && it.userAnswers.isEmpty() 
            }
            else -> currentCategoryTests
        }
    }

    // Dialog for displaying attempt history of a specific mock test
    if (historyTestDialog != null) {
        MockTestAttemptHistoryDialog(
            test = historyTestDialog!!,
            onDismiss = { historyTestDialog = null },
            onViewAttemptReport = { record: MockTestAttemptRecord ->
                val reviewTest = historyTestDialog!!.copy(
                    score = record.score,
                    correctAnswers = record.correctAnswers,
                    wrongAnswers = record.wrongAnswers,
                    unanswered = record.unanswered,
                    timeTakenSeconds = record.timeTakenSeconds
                )
                onViewResult(reviewTest)
            }
        )
    }

    // Confirm Delete Dialog
    if (testToDelete != null) {
        val test = testToDelete ?: return@AvailableMockTestsScreen
        AlertDialog(
            onDismissRequest = { testToDelete = null },
            title = { Text("Delete Mock Test?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove \"${test.title}\"? Attempt history will be retained.") },
            confirmButton = {
                Button(
                    onClick = {
                        SampleData.generatedMockTestsState.removeAll { it.id == test.id }
                        appRepository.deleteMockTest(test.id)
                        testToDelete = null
                        Toast.makeText(context, "Mock Test removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { testToDelete = null }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color(0xFFEFF6FF))
                )
            )
    ) {
        // Ambient background glowing orbs
        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.TopEnd)
                .offset(x = 120.dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFDBEAFE).copy(alpha = 0.45f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 100.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFEDE9FE).copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // TOP APP BAR
            TopMockTestsAppBar(
                selectedCategory = selectedCategory,
                onNavigateBack = {
                    if (selectedCategory != null) {
                        selectedCategory = null
                    } else {
                        onNavigateBack()
                    }
                },
                onNavigateToHome = onNavigateToHome,
                onNavigateToGenerator = onNavigateToGenerator
            )

            // BODY: SCREEN 1 (3 Categories) vs SCREEN 2 (Category Mock Test List)
            if (selectedCategory == null) {
                // ==========================================
                // 1. FIRST SCREEN — 3 MOCK TEST CATEGORIES
                // ==========================================
                MockTestCategoriesHomeScreen(
                    fullLengthCount = fullLengthTests.size,
                    unitWiseCount = unitWiseTests.size,
                    topicWiseCount = topicWiseTests.size,
                    onSelectCategory = { category ->
                        selectedCategory = category
                        selectedStatusFilter = "ALL" // Reset status filter on enter
                    },
                    onNavigateToGenerator = onNavigateToGenerator
                )
            } else {
                // ==========================================
                // 2. CATEGORY MOCK TEST LIST SCREEN
                // ==========================================
                CategoryMockTestListScreen(
                    category = selectedCategory!!,
                    categoryTests = currentCategoryTests,
                    filteredTests = filteredCategoryTests,
                    selectedStatusFilter = selectedStatusFilter,
                    onSelectStatusFilter = { selectedStatusFilter = it },
                    onStartTest = onStartTest,
                    onResumeTest = onResumeTest,
                    onReattemptTest = onReattemptTest,
                    onViewResult = onViewResult,
                    onDeleteTest = { testToDelete = it },
                    onViewHistory = { historyTestDialog = it },
                    onNavigateToGenerator = onNavigateToGenerator
                )
            }
        }
    }
}

/**
 * Top App Bar with back navigation, titles, and create button
 */
@Composable
private fun TopMockTestsAppBar(
    selectedCategory: MockTestMode?,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToGenerator: () -> Unit
) {
    val titleText = when (selectedCategory) {
        MockTestMode.FULL_LENGTH -> "Full-Length Mock Tests"
        MockTestMode.UNIT_WISE -> "Unit-Wise Mock Tests"
        MockTestMode.TOPIC_WISE -> "Topic-Wise Mock Tests"
        MockTestMode.SUBJECT_WISE -> "Unit-Wise Mock Tests"
        null -> "Mock Tests Available"
    }

    val subtitleText = when (selectedCategory) {
        null -> "Select Category to Practice"
        else -> "Official Exam Format & Evaluation"
    }

    Surface(
        color = Color.White.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onNavigateToHome,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column {
                    Text(
                        text = titleText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitleText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Create Test Button
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF2563EB), Color(0xFF4F46E5))
                        )
                    )
                    .clickable { onNavigateToGenerator() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Create",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Screen 1: Shows ONLY the 3 Main Categories:
 * 1. Full-Length Mock Tests
 * 2. Unit-Wise Mock Tests
 * 3. Topic-Wise Mock Tests
 */
@Composable
private fun MockTestCategoriesHomeScreen(
    fullLengthCount: Int,
    unitWiseCount: Int,
    topicWiseCount: Int,
    onSelectCategory: (MockTestMode) -> Unit,
    onNavigateToGenerator: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome / Info Banner
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2563EB),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MOCK TEST ARENA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1D4ED8),
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Comprehensive, Unit & Topic-Wise Examination Tests",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }

        // 1. FULL-LENGTH MOCK TESTS CARD
        item {
            CategorySelectionCard(
                categoryName = "FULL-LENGTH MOCK TESTS",
                testCount = fullLengthCount,
                description = "Complete syllabus mock tests aligned with actual exam pattern, multi-subject weightage, and negative marking.",
                icon = Icons.Default.Assignment,
                gradientColors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF3B82F6)),
                accentColor = Color(0xFF60A5FA),
                badgeColor = Color(0xFF93C5FD),
                onClick = { onSelectCategory(MockTestMode.FULL_LENGTH) }
            )
        }

        // 2. UNIT-WISE MOCK TESTS CARD
        item {
            CategorySelectionCard(
                categoryName = "UNIT-WISE MOCK TESTS",
                testCount = unitWiseCount,
                description = "Targeted assessments focusing on core syllabus units to strengthen conceptual mastery and unit-level accuracy.",
                icon = Icons.Default.AccountTree,
                gradientColors = listOf(Color(0xFF065F46), Color(0xFF059669), Color(0xFF10B981)),
                accentColor = Color(0xFF34D399),
                badgeColor = Color(0xFFA7F3D0),
                onClick = { onSelectCategory(MockTestMode.UNIT_WISE) }
            )
        }

        // 3. TOPIC-WISE MOCK TESTS CARD
        item {
            CategorySelectionCard(
                categoryName = "TOPIC-WISE MOCK TESTS",
                testCount = topicWiseCount,
                description = "High-precision practice dedicated to specific topics and key subtopics for pinpoint problem solving.",
                icon = Icons.Default.Psychology,
                gradientColors = listOf(Color(0xFF581C87), Color(0xFF7E22CE), Color(0xFF9333EA)),
                accentColor = Color(0xFFC084FC),
                badgeColor = Color(0xFFE9D5FF),
                onClick = { onSelectCategory(MockTestMode.TOPIC_WISE) }
            )
        }

        // Bottom CTA Card: Create New Custom Test
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToGenerator() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Need a custom test?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Generate with custom questions & timer",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Premium Category Card for the First Screen
 */
@Composable
private fun CategorySelectionCard(
    categoryName: String,
    testCount: Int,
    description: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    accentColor: Color,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(20.dp)
        ) {
            // Background subtle ambient light
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row: Category Badge + Test Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = categoryName,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Dynamic Count Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = "$testCount Mock Tests",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                Text(
                    text = description,
                    fontSize = 12.5.sp,
                    color = Color.White.copy(alpha = 0.88f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (testCount > 0) "TAP TO EXPLORE TESTS" else "TAP TO GENERATE",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        letterSpacing = 0.6.sp
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = gradientColors.first(),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen 2: Dedicated Mock Test List page for the chosen category with status filters
 */
@Composable
private fun CategoryMockTestListScreen(
    category: MockTestMode,
    categoryTests: List<GeneratedMockTest>,
    filteredTests: List<GeneratedMockTest>,
    selectedStatusFilter: String,
    onSelectStatusFilter: (String) -> Unit,
    onStartTest: (GeneratedMockTest) -> Unit,
    onResumeTest: (GeneratedMockTest) -> Unit,
    onReattemptTest: (GeneratedMockTest) -> Unit,
    onViewResult: (GeneratedMockTest) -> Unit,
    onDeleteTest: (GeneratedMockTest) -> Unit,
    onViewHistory: (GeneratedMockTest) -> Unit,
    onNavigateToGenerator: () -> Unit
) {
    val filterTabs = listOf("ALL", "COMPLETED", "CONTINUING", "NOT ATTEMPTED")

    // Dynamic counts for each filter chip
    val completedCount = remember(categoryTests) { categoryTests.count { it.status == "Completed" } }
    val continuingCount = remember(categoryTests) { 
        categoryTests.count { it.status == "In Progress" || it.status == "Continuing" || (it.status != "Completed" && it.userAnswers.isNotEmpty()) } 
    }
    val notAttemptedCount = remember(categoryTests) { 
        categoryTests.count { it.status != "Completed" && it.status != "In Progress" && it.status != "Continuing" && it.userAnswers.isEmpty() } 
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // STATUS FILTER BAR: [ ALL ] [ COMPLETED ] [ CONTINUING ] [ NOT ATTEMPTED ]
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterTabs) { tab ->
                val isSelected = selectedStatusFilter == tab
                val countForTab = when (tab) {
                    "ALL" -> categoryTests.size
                    "COMPLETED" -> completedCount
                    "CONTINUING" -> continuingCount
                    "NOT ATTEMPTED" -> notAttemptedCount
                    else -> 0
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF1E293B) else Color.White,
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectStatusFilter(tab) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF475569)
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color.White.copy(alpha = 0.2f) else Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "$countForTab",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // LIST OF MOCK TESTS OR EMPTY STATE
        if (filteredTests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (selectedStatusFilter) {
                                        "COMPLETED" -> Icons.Default.CheckCircleOutline
                                        "CONTINUING" -> Icons.Default.PendingActions
                                        "NOT ATTEMPTED" -> Icons.Default.HourglassEmpty
                                        else -> Icons.Default.FolderOpen
                                    },
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val emptyHeading = when (selectedStatusFilter) {
                            "COMPLETED" -> "No Completed Mock Tests"
                            "CONTINUING" -> "No Continuing Mock Tests"
                            "NOT ATTEMPTED" -> "No Not Attempted Mock Tests"
                            else -> "No Mock Tests Available"
                        }

                        val emptyDescription = when (selectedStatusFilter) {
                            "COMPLETED" -> "You haven't completed any mock tests in this category yet. Start a test to see your scores here."
                            "CONTINUING" -> "You have no mock tests currently in progress. Start a new test or resume an existing one."
                            "NOT ATTEMPTED" -> "All available mock tests in this category have already been attempted or completed."
                            else -> "Generate a mock test to see it here."
                        }

                        Text(
                            text = emptyHeading,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = emptyDescription,
                            fontSize = 12.5.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onNavigateToGenerator,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CREATE MOCK TEST", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                itemsIndexed(filteredTests, key = { _, test -> test.id }) { index, test ->
                    // Calculate stable display sequential index within this category
                    val sequentialIndex = categoryTests.indexOfFirst { it.id == test.id }.let { if (it >= 0) it + 1 else index + 1 }
                    
                    MockTestItemCard(
                        sequentialNumber = sequentialIndex,
                        test = test,
                        onStartTest = { onStartTest(test) },
                        onResumeTest = { onResumeTest(test) },
                        onReattemptTest = { onReattemptTest(test) },
                        onViewResult = { onViewResult(test) },
                        onDeleteTest = { onDeleteTest(test) },
                        onViewHistory = { onViewHistory(test) }
                    )
                }
            }
        }
    }
}

/**
 * Premium Card for individual generated mock test
 */
@Composable
private fun MockTestItemCard(
    sequentialNumber: Int,
    test: GeneratedMockTest,
    onStartTest: () -> Unit,
    onResumeTest: () -> Unit,
    onReattemptTest: () -> Unit,
    onViewResult: () -> Unit,
    onDeleteTest: () -> Unit,
    onViewHistory: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val activeApp = SampleData.getActiveApp()
    val firstGeneratedTestId = remember(SampleData.generatedMockTestsState.size, activeApp.id) {
        SampleData.generatedMockTestsState
            .filter { it.appId == activeApp.id || it.appId.isBlank() }
            .minByOrNull { it.createdAt }?.id
    }
    val isFree = test.id == firstGeneratedTestId

    val isCompleted = test.status == "Completed"
    val isInProgress = test.status == "In Progress" || test.status == "Continuing" || (test.status != "Completed" && test.userAnswers.isNotEmpty())
    val isNotAttempted = !isCompleted && !isInProgress

    val categoryTitle = when (test.testMode) {
        MockTestMode.FULL_LENGTH -> "Full-Length Mock Test"
        MockTestMode.UNIT_WISE -> "Unit-Wise Mock Test"
        MockTestMode.TOPIC_WISE -> "Topic-Wise Mock Test"
        MockTestMode.SUBJECT_WISE -> "Unit-Wise Mock Test"
    }

    val totalMarks = test.totalMarks.let { if (it > 0) it.toInt() else (test.totalQuestions * test.positiveMarks).toInt() }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(
            1.dp,
            when {
                isCompleted -> Color(0xFF86EFAC)
                isInProgress -> Color(0xFFFDE68A)
                else -> Color(0xFFE2E8F0)
            }
        ),
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ROW 1: Sequential Mock Test Title + Category Badge + More Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mock Test $sequentialNumber",
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = categoryTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Access Badge (FREE / PREMIUM / LOCKED)
                    val isPro = SampleData.isUserPremium()
                    val accessLabel = when {
                        isFree -> "FREE"
                        isPro -> "PREMIUM"
                        else -> "LOCKED"
                    }
                    val accessColor = when {
                        isFree -> Color(0xFF16A34A) // Green
                        isPro -> Color(0xFFD97706) // Amber
                        else -> Color(0xFFEF4444) // Red
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accessColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, accessColor),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (!isFree && !isPro) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = accessColor,
                                    modifier = Modifier.size(10.dp)
                                )
                            } else if (isPro && !isFree) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = accessColor,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                            Text(
                                text = accessLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accessColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Status Badge Top-Right
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            isCompleted -> Color(0xFFDCFCE7)
                            isInProgress -> Color(0xFFFEF3C7)
                            else -> Color(0xFFF1F5F9)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isCompleted -> Color(0xFF86EFAC)
                                isInProgress -> Color(0xFFFCD34D)
                                else -> Color(0xFFE2E8F0)
                            }
                        )
                    ) {
                        Text(
                            text = when {
                                isCompleted -> "COMPLETED"
                                isInProgress -> "CONTINUING"
                                else -> "NOT ATTEMPTED"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isCompleted -> Color(0xFF15803D)
                                isInProgress -> Color(0xFFB45309)
                                else -> Color(0xFF475569)
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Three-dot Options Menu
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            if (test.attemptsHistory.isNotEmpty() || isCompleted) {
                                DropdownMenuItem(
                                    text = { Text("Attempt History") },
                                    onClick = {
                                        menuExpanded = false
                                        onViewHistory()
                                    },
                                    leadingIcon = { Icon(Icons.Default.History, contentDescription = null) }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Delete Test") },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteTest()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626)) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ROW 2: STATS ROW (Questions | Marks | Duration)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MockTestSpecStat(
                        icon = Icons.Default.Quiz,
                        label = "Questions",
                        value = "${test.totalQuestions}"
                    )
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp)
                            .background(Color(0xFFE2E8F0))
                    )
                    MockTestSpecStat(
                        icon = Icons.Default.Score,
                        label = "Marks",
                        value = "$totalMarks"
                    )
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp)
                            .background(Color(0xFFE2E8F0))
                    )
                    MockTestSpecStat(
                        icon = Icons.Default.Timer,
                        label = "Duration",
                        value = "${test.durationMinutes}m"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ROW 3: CONDITIONAL DETAILS & ACTIONS ACCORDING TO STATUS
            when {
                isCompleted -> {
                    // COMPLETED STATE: Show Score & Accuracy, View Result + Retest buttons
                    val lastRecord = test.attemptsHistory.lastOrNull()
                    val accuracy = lastRecord?.accuracyStr ?: if (test.correctAnswers + test.wrongAnswers > 0) {
                        val totalAtt = (test.correctAnswers + test.wrongAnswers).coerceAtLeast(1)
                        val accVal = ((test.correctAnswers * 100.0) / totalAtt).toInt().coerceIn(0, 100)
                        "$accVal%"
                    } else "100%"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Score: ${test.score ?: lastRecord?.score ?: 0.0} / $totalMarks",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Accuracy: $accuracy • Correct: ${test.correctAnswers}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF16A34A)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // View Result Button
                            OutlinedButton(
                                onClick = onViewResult,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF2563EB)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = "VIEW RESULT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB)
                                )
                            }

                            // Retest Button
                            Button(
                                onClick = onReattemptTest,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "RETEST",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                isInProgress -> {
                    // CONTINUING STATE: Show Progress count + Resume button
                    val answeredCount = test.userAnswers.size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Progress: $answeredCount/${test.totalQuestions} Questions",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                            Text(
                                text = "Test in progress • Saved state ready",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Button(
                            onClick = onResumeTest,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(
                                text = "RESUME TEST",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                else -> {
                    // NOT ATTEMPTED STATE: Show Start Test Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Not started yet",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )

                        Button(
                            onClick = onStartTest,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(
                                text = "START TEST",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
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
private fun MockTestSpecStat(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun MockTestAttemptHistoryDialog(
    test: GeneratedMockTest,
    onDismiss: () -> Unit,
    onViewAttemptReport: (MockTestAttemptRecord) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF2563EB))
                Text(
                    text = "Attempt History",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            if (test.attemptsHistory.isEmpty()) {
                Text(
                    text = "No past attempts recorded for this test.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(test.attemptsHistory) { index, record ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onViewAttemptReport(record) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Attempt #${record.attemptNumber}",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = record.accuracyStr,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dateFormat.format(Date(record.completedAt)),
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Score: ${record.score} / ${record.totalMarks.toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2563EB)
                                    )
                                    Text(
                                        text = "Time: ${record.timeTakenSeconds / 60}m",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}
