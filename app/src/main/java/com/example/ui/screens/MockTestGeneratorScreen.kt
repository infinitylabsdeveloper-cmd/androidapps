package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.SampleData
import com.example.model.*
import com.example.ui.components.AppMotionSystem
import com.example.ui.components.cascadeDropItem
import com.example.util.MockTestEngine
import java.text.SimpleDateFormat
import java.util.*

private fun parseHexColorSafe(hex: String, fallback: Color): Color {
    return try {
        val clean = hex.removePrefix("#").trim()
        val colorInt = when (clean.length) {
            6 -> (0xFF000000 or clean.toLong(16)).toInt()
            8 -> clean.toLong(16).toInt()
            else -> return fallback
        }
        Color(colorInt)
    } catch (_: Exception) {
        fallback
    }
}

enum class SyllabusCategory(val label: String) {
    PRELIMS("Prelims"),
    MAINS("Mains"),
    GENERAL("General")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestGeneratorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onTestGenerated: (GeneratedMockTest) -> Unit = {},
    pastTests: List<GeneratedMockTest> = emptyList(),
    onStartGeneratedTest: (GeneratedMockTest) -> Unit = {},
    onNavigateToAvailableTests: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {}
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val activeApp = SampleData.getActiveApp()
    val adminConfig = remember(activeApp.id) {
        SampleData.getMockTestGeneratorConfigForApp(activeApp.id)
    }

    // Parsed Themes
    val screenBgColor = remember(adminConfig.backgroundColorHex) {
        parseHexColorSafe(adminConfig.backgroundColorHex, Color(0xFFF8FAFC))
    }
    val cardBgColor = remember(adminConfig.cardBackgroundColorHex) {
        parseHexColorSafe(adminConfig.cardBackgroundColorHex, Color.White)
    }
    val mainActionBtnColor = remember(adminConfig.buttonColorHex) {
        parseHexColorSafe(adminConfig.buttonColorHex, Color(0xFF0D5C46))
    }
    val accentThemeColor = remember(adminConfig.accentColorHex) {
        parseHexColorSafe(adminConfig.accentColorHex, Color(0xFF10B981))
    }

    // Step-by-Step Selection States
    var selectedMockTestMode by remember { mutableStateOf(MockTestMode.FULL_LENGTH) }
    
    // Basic Settings
    var totalTimeMinutes by remember { mutableIntStateOf(90) }
    var totalMarksInput by remember { mutableStateOf("100") }
    var positiveMarksValue by remember { mutableDoubleStateOf(1.0) }
    var negativeMarksValue by remember { mutableDoubleStateOf(0.33) }

    // Category / Syllabus
    var selectedCategory by remember { mutableStateOf(SyllabusCategory.PRELIMS) }

    // Marking Scheme States
    var markingSchemeMode by remember { mutableStateOf("EXAM_PATTERN") } // "EXAM_PATTERN" or "CUSTOM"
    var selectedPositiveOption by remember { mutableStateOf("+1.00") } // "+1.00", "+1.25", "+1.50", "+2.00", "CUSTOM"
    var customPositiveInput by remember { mutableStateOf("1.00") }
    var selectedNegativeOption by remember { mutableStateOf("-0.33") } // "0", "-0.25", "-0.33", "-0.50", "CUSTOM"
    var customNegativeInput by remember { mutableStateOf("0.33") }

    // Exam pattern default lookup for active app and category
    val (examPatternPos, examPatternNeg) = remember(activeApp.id, selectedCategory) {
        MockTestEngine.getExamPatternMarkingScheme(selectedCategory.label, activeApp.id)
    }

    // Keep positiveMarksValue & negativeMarksValue in sync with selection
    LaunchedEffect(
        markingSchemeMode,
        examPatternPos,
        examPatternNeg,
        selectedPositiveOption,
        customPositiveInput,
        selectedNegativeOption,
        customNegativeInput
    ) {
        if (markingSchemeMode == "EXAM_PATTERN") {
            positiveMarksValue = examPatternPos
            negativeMarksValue = examPatternNeg
        } else {
            positiveMarksValue = when (selectedPositiveOption) {
                "+1.00" -> 1.0
                "+1.25" -> 1.25
                "+1.50" -> 1.5
                "+2.00" -> 2.0
                "CUSTOM" -> customPositiveInput.toDoubleOrNull() ?: 1.0
                else -> 1.0
            }
            negativeMarksValue = when (selectedNegativeOption) {
                "0" -> 0.0
                "-0.25" -> 0.25
                "-0.33" -> 0.33
                "-0.50" -> 0.5
                "CUSTOM" -> customNegativeInput.toDoubleOrNull() ?: 0.33
                else -> 0.33
            }
        }
    }

    // Questions pool and subjects
    val allQuestions = remember(activeApp.id, SampleData.questionsState.size) {
        SampleData.questionsState.filter { it.appId == activeApp.id || it.subjectId == "subj-current-affairs" }
    }
    val allSubjects = remember(activeApp.id, SampleData.subjectsState.size) {
        SampleData.subjectsState.filter { it.appId == activeApp.id }
    }

    // Collapsed/Expanded states for subject cards
    val expandedSubjects = remember { mutableStateMapOf<String, Boolean>() }
    val expandedUnits = remember { mutableStateMapOf<String, Boolean>() }

    // Selection matrices
    val selectedSubjectIds = remember { mutableStateOf(setOf<String>()) }
    val selectedEntireSubjects = remember { mutableStateOf(setOf<String>()) }
    val selectedUnitIds = remember { mutableStateOf(setOf<String>()) }
    val selectedTopicIds = remember { mutableStateOf(setOf<String>()) }

    // Current Affairs Selection State
    var selectedEntireCurrentAffairs by remember { mutableStateOf(false) }
    val selectedCAContractMonths = remember { mutableStateOf(setOf<String>()) }

    // User-requested question count (Default 150 for APPSC standard format)
    var requestedQuestionsCount by remember { mutableIntStateOf(150) }
    var customTotalInput by remember { mutableStateOf("150") }

    // Manual Question Allocation per selected Subject and for Current Affairs
    val subjectManualAllocations = remember { mutableStateMapOf<String, Int>() }
    var caManualAllocation by remember { mutableIntStateOf(0) }

    fun isCaSelected(): Boolean = selectedEntireCurrentAffairs || selectedCAContractMonths.value.isNotEmpty()

    fun distributeQuestionsEvenly(targetTotal: Int) {
        val hasCa = isCaSelected()
        val sourcesCount = selectedSubjectIds.value.size + (if (hasCa) 1 else 0)
        if (sourcesCount == 0) return
        val perSource = targetTotal / sourcesCount
        val remainder = targetTotal % sourcesCount

        var assigned = 0
        selectedSubjectIds.value.toList().sorted().forEachIndexed { idx, subId ->
            val extra = if (idx < remainder) 1 else 0
            val amount = perSource + extra
            subjectManualAllocations[subId] = amount
            assigned += amount
        }
        if (hasCa) {
            caManualAllocation = targetTotal - assigned
        } else {
            caManualAllocation = 0
        }
    }

    // Keep allocations synced whenever selection changes
    LaunchedEffect(selectedSubjectIds.value, selectedEntireCurrentAffairs, selectedCAContractMonths.value) {
        val currentKeys = subjectManualAllocations.keys.toList()
        currentKeys.forEach { key ->
            if (key !in selectedSubjectIds.value) {
                subjectManualAllocations.remove(key)
            }
        }
        val unassignedSubjects = selectedSubjectIds.value.filter { it !in subjectManualAllocations }
        if (unassignedSubjects.isNotEmpty() || (isCaSelected() && caManualAllocation == 0)) {
            distributeQuestionsEvenly(requestedQuestionsCount)
        }
    }

    // Success generation modal
    var generatedTestResult by remember { mutableStateOf<GeneratedMockTest?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Helper for bucketed CA questions
    fun getQuestionsForMonth(month: MonthlyCurrentAffairsModel, questions: List<Question>): List<Question> {
        val fromModel = month.sets.flatMap { it.questions }
        val fromPool = questions.filter { q ->
            q.subjectId == "subj-current-affairs" && (
                (q.id.contains(month.month.take(3).lowercase()) && q.id.contains(month.year)) ||
                (q.subtopic.contains(month.month, ignoreCase = true) && q.subtopic.contains(month.year))
            )
        }
        return (fromModel + fromPool).distinctBy { it.id }
    }

    // Filter subjects dynamically according to Syllabus Category Selection
    val filteredSubjects = remember(allSubjects, selectedCategory) {
        allSubjects.filter { subject ->
            subject.matchesCategoryFilter(selectedCategory.label)
        }
    }

    // Calculate dynamic pools
    val eligiblePool = remember(
        allQuestions,
        selectedSubjectIds.value,
        selectedEntireSubjects.value,
        selectedUnitIds.value,
        selectedTopicIds.value,
        selectedEntireCurrentAffairs,
        selectedCAContractMonths.value
    ) {
        val pool = mutableListOf<Question>()

        // 1. Regular subjects
        val regularQuestions = allQuestions.filter { it.subjectId != "subj-current-affairs" }
        regularQuestions.forEach { q ->
            if (q.subjectId in selectedSubjectIds.value) {
                if (q.subjectId in selectedEntireSubjects.value) {
                    pool.add(q)
                } else if (q.unitId in selectedUnitIds.value) {
                    pool.add(q)
                } else if (q.topicId in selectedTopicIds.value) {
                    pool.add(q)
                }
            }
        }

        // 2. Current affairs
        if (selectedEntireCurrentAffairs) {
            val allCa = allQuestions.filter { it.subjectId == "subj-current-affairs" }
            val allModelCa = SampleData.getMonthlyCurrentAffairs().flatMap { it.sets.flatMap { s -> s.questions } }
            pool.addAll((allCa + allModelCa).distinctBy { it.id })
        } else if (selectedCAContractMonths.value.isNotEmpty()) {
            SampleData.getMonthlyCurrentAffairs().forEach { m ->
                val monthId = "ca-month-${m.id}"
                if (monthId in selectedCAContractMonths.value) {
                    val mQuestions = getQuestionsForMonth(m, allQuestions)
                    pool.addAll(mQuestions)
                }
            }
        }

        pool.distinctBy { it.id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mock Test Generator",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF0F172A)
                            )
                        }
                        IconButton(onClick = onNavigateToHome) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = Color(0xFF2563EB)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = screenBgColor
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header Intro
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(mainActionBtnColor.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = mainActionBtnColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Smart Custom Generator",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Configure time, marks, syllabus categories, and drill down to choose specific sources.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            // 1. MOCK TEST TYPES (Step 1)
            item {
                Text(
                    text = "1. Select Mock Test Type",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MockTestMode.values().forEach { mode ->
                        val secConfig = adminConfig.sections[mode.name] ?: MockTestSectionConfig(mode)
                        if (secConfig.isEnabled) {
                            val startColor = parseHexColorSafe(secConfig.backgroundGradientStart, Color(0xFF1E3A8A))
                            val endColor = parseHexColorSafe(secConfig.backgroundGradientEnd, Color(0xFF3B82F6))
                            val isSelected = selectedMockTestMode == mode

                            Card(
                                onClick = {
                                    selectedMockTestMode = mode
                                    // Auto-adjust default question count based on mode selection
                                    requestedQuestionsCount = when (mode) {
                                        MockTestMode.FULL_LENGTH -> 100
                                        MockTestMode.SUBJECT_WISE -> 50
                                        MockTestMode.UNIT_WISE -> 25
                                        MockTestMode.TOPIC_WISE -> 15
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) startColor.copy(alpha = 0.04f) else cardBgColor
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) startColor else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Brush.linearGradient(listOf(startColor, endColor))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (mode) {
                                                MockTestMode.FULL_LENGTH -> Icons.Default.Assignment
                                                MockTestMode.SUBJECT_WISE -> Icons.Default.MenuBook
                                                MockTestMode.UNIT_WISE -> Icons.Default.AccountTree
                                                MockTestMode.TOPIC_WISE -> Icons.Default.Psychology
                                            },
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = secConfig.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = secConfig.description,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedMockTestMode = mode },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = startColor
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. BASIC TEST SETTINGS (Step 2)
            item {
                Text(
                    text = "2. Basic Test Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Total Duration Picker
                        Text(
                            text = "Total Duration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(30, 60, 90, 120).forEach { mins ->
                                val isSelected = totalTimeMinutes == mins
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { totalTimeMinutes = mins },
                                    label = { Text("$mins Mins", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = mainActionBtnColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        
                        // Custom Duration Slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Slider(
                                value = totalTimeMinutes.toFloat(),
                                onValueChange = { totalTimeMinutes = it.toInt() },
                                valueRange = 10f..180f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    activeTrackColor = mainActionBtnColor,
                                    thumbColor = mainActionBtnColor
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$totalTimeMinutes Min",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.width(60.dp),
                                textAlign = TextAlign.End
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                        // Total Marks Input
                        Text(
                            text = "Total Marks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = totalMarksInput,
                            onValueChange = { totalMarksInput = it.filter { char -> char.isDigit() } },
                            placeholder = { Text("e.g. 100") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = mainActionBtnColor
                            )
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                        // Flexible Exam-Specific Marking Scheme
                        Text(
                            text = "Marking Scheme",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Segmented Mode Selector: Use Exam Pattern vs Custom Marking
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isExamPatternSelected = markingSchemeMode == "EXAM_PATTERN"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isExamPatternSelected) mainActionBtnColor else Color(0xFFF1F5F9),
                                border = BorderStroke(
                                    1.dp,
                                    if (isExamPatternSelected) mainActionBtnColor else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { markingSchemeMode = "EXAM_PATTERN" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = if (isExamPatternSelected) Color.White else Color(0xFF475569),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Use Exam Pattern",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isExamPatternSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }

                            val isCustomSelected = markingSchemeMode == "CUSTOM"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCustomSelected) mainActionBtnColor else Color(0xFFF1F5F9),
                                border = BorderStroke(
                                    1.dp,
                                    if (isCustomSelected) mainActionBtnColor else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { markingSchemeMode = "CUSTOM" }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = if (isCustomSelected) Color.White else Color(0xFF475569),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Custom Marking",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isCustomSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (markingSchemeMode == "EXAM_PATTERN") {
                            // Display Exam Pattern Scheme Summary Banner
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${activeApp.name} (${selectedCategory.label}) Configured Scheme",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = Color(0xFF1E40AF)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• Correct Answer: +${String.format(java.util.Locale.US, "%.2f", examPatternPos)}",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF1E3A8A),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "• Incorrect Penalty: -${String.format(java.util.Locale.US, "%.2f", examPatternNeg)}",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF991B1B),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        } else {
                            // Custom Marking Sub-sections
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Positive Marks Row Options
                                Column {
                                    Text(
                                        text = "Positive Marks",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        listOf("+1.00", "+1.25", "+1.50", "+2.00", "Custom").forEach { opt ->
                                            val isSel = selectedPositiveOption == opt
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSel) mainActionBtnColor else Color(0xFFF1F5F9))
                                                    .clickable { selectedPositiveOption = opt }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = opt,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (isSel) Color.White else Color(0xFF475569)
                                                )
                                            }
                                        }
                                    }

                                    if (selectedPositiveOption == "Custom") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = customPositiveInput,
                                            onValueChange = { customPositiveInput = it },
                                            label = { Text("Custom Positive Marks") },
                                            placeholder = { Text("e.g. 2.50") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = mainActionBtnColor)
                                        )
                                    }
                                }

                                // Negative Marks Row Options
                                Column {
                                    Text(
                                        text = "Negative Marks",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        listOf("0", "-0.25", "-0.33", "-0.50", "Custom").forEach { opt ->
                                            val isSel = selectedNegativeOption == opt
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSel) Color(0xFFDC2626) else Color(0xFFF1F5F9))
                                                    .clickable { selectedNegativeOption = opt }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = opt,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (isSel) Color.White else Color(0xFF475569)
                                                )
                                            }
                                        }
                                    }

                                    if (selectedNegativeOption == "Custom") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = customNegativeInput,
                                            onValueChange = { customNegativeInput = it },
                                            label = { Text("Custom Negative Penalty Marks") },
                                            placeholder = { Text("e.g. 0.20") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFDC2626))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. SELECT EXAM CATEGORY (Step 3)
            item {
                Text(
                    text = "3. Select Syllabus Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SyllabusCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Button(
                            onClick = { selectedCategory = cat },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) mainActionBtnColor else Color(0xFFE2E8F0),
                                contentColor = if (isSelected) Color.White else Color(0xFF475569)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text(
                                text = cat.label,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // 4. DISPLAY SUBJECTS WITH QUESTION COUNTS (Step 4 & 5)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "4. Select Question Sources",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Category: ${selectedCategory.label}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentThemeColor
                    )
                }
            }

            // Normal subjects lists
            if (filteredSubjects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No subjects found in category ${selectedCategory.label}.",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(filteredSubjects) { subject ->
                    val isSubjectSelected = subject.id in selectedSubjectIds.value
                    val isEntireSelected = subject.id in selectedEntireSubjects.value
                    val isExpanded = expandedSubjects[subject.id] == true

                    val totalAvailableInSub = remember(allQuestions, subject.id) {
                        allQuestions.count { it.subjectId == subject.id && it.isActive }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        border = BorderStroke(
                            width = if (isSubjectSelected) 1.5.dp else 1.dp,
                            color = if (isSubjectSelected) mainActionBtnColor else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Column {
                            // Subject Card Top row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isExpanded) {
                                            expandedSubjects.remove(subject.id)
                                            expandedUnits.clear()
                                        } else {
                                            expandedSubjects.clear()
                                            expandedUnits.clear()
                                            expandedSubjects[subject.id] = true
                                        }
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Subject icon placeholder
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(mainActionBtnColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = mainActionBtnColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = subject.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "Available MCQs: $totalAvailableInSub",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isSubjectSelected,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                selectedSubjectIds.value = selectedSubjectIds.value + subject.id
                                                // Default to select entire subject
                                                selectedEntireSubjects.value = selectedEntireSubjects.value + subject.id
                                            } else {
                                                selectedSubjectIds.value = selectedSubjectIds.value - subject.id
                                                selectedEntireSubjects.value = selectedEntireSubjects.value - subject.id
                                                // Deselect all unit and topic IDs in this subject
                                                val unitIdsToRemove = subject.units.map { it.id }.toSet()
                                                val topicIdsToRemove = subject.units.flatMap { it.topics }.map { it.id }.toSet()
                                                selectedUnitIds.value = selectedUnitIds.value - unitIdsToRemove
                                                selectedTopicIds.value = selectedTopicIds.value - topicIdsToRemove
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = mainActionBtnColor
                                        )
                                    )
                                    val subjChevronRotation = AppMotionSystem.animateAccordionChevronRotation(
                                        isExpanded = isExpanded,
                                        label = "MockGenSubjectChevron_${subject.id}"
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = "Expand",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.rotate(subjChevronRotation)
                                    )
                                }
                            }

                            // Subject Expand/Drill-Down Options
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = AppMotionSystem.cascadeContainerEnterTransition(),
                                exit = AppMotionSystem.cascadeContainerExitTransition()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC))
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Option A / Option B segment buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                selectedSubjectIds.value = selectedSubjectIds.value + subject.id
                                                selectedEntireSubjects.value = selectedEntireSubjects.value + subject.id
                                                val unitIdsToRemove = subject.units.map { it.id }.toSet()
                                                val topicIdsToRemove = subject.units.flatMap { it.topics }.map { it.id }.toSet()
                                                selectedUnitIds.value = selectedUnitIds.value - unitIdsToRemove
                                                selectedTopicIds.value = selectedTopicIds.value - topicIdsToRemove
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isEntireSelected) mainActionBtnColor else Color(0xFFE2E8F0),
                                                contentColor = if (isEntireSelected) Color.White else Color(0xFF475569)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 6.dp)
                                        ) {
                                            Text("Option A: Entire Subject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                selectedSubjectIds.value = selectedSubjectIds.value + subject.id
                                                selectedEntireSubjects.value = selectedEntireSubjects.value - subject.id
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSubjectSelected && !isEntireSelected) mainActionBtnColor else Color(0xFFE2E8F0),
                                                contentColor = if (isSubjectSelected && !isEntireSelected) Color.White else Color(0xFF475569)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 6.dp)
                                        ) {
                                            Text("Option B: Selective Units", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Units List drill-down
                                    if (isSubjectSelected && !isEntireSelected) {
                                        Text(
                                            text = "Units under ${subject.name}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF334155),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )

                                        subject.units.forEachIndexed { unitIdx, unit ->
                                            val isUnitSelected = unit.id in selectedUnitIds.value
                                            val isUnitExpanded = expandedUnits[unit.id] == true
                                            val totalInUnit = remember(allQuestions, subject.id, unit.id) {
                                                allQuestions.count { it.subjectId == subject.id && it.unitId == unit.id && it.isActive }
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .cascadeDropItem(visible = isExpanded, index = unitIdx)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color.White)
                                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            if (isUnitExpanded) {
                                                                expandedUnits.remove(unit.id)
                                                            } else {
                                                                expandedUnits.clear()
                                                                expandedUnits[unit.id] = true
                                                            }
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = unit.name,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF1E293B)
                                                        )
                                                        Text(
                                                            text = "Available Qs: $totalInUnit",
                                                            fontSize = 10.sp,
                                                            color = Color(0xFF64748B)
                                                        )
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Checkbox(
                                                            checked = isUnitSelected,
                                                            onCheckedChange = { checked ->
                                                                if (checked) {
                                                                    selectedUnitIds.value = selectedUnitIds.value + unit.id
                                                                } else {
                                                                    selectedUnitIds.value = selectedUnitIds.value - unit.id
                                                                    // Deselect topics of this unit
                                                                    val topicsToRemove = unit.topics.map { it.id }.toSet()
                                                                    selectedTopicIds.value = selectedTopicIds.value - topicsToRemove
                                                                }
                                                            },
                                                            colors = CheckboxDefaults.colors(
                                                                checkedColor = mainActionBtnColor
                                                            )
                                                        )
                                                        val unitChevronRotation = AppMotionSystem.animateAccordionChevronRotation(
                                                            isExpanded = isUnitExpanded,
                                                            label = "MockGenUnitChevron_${unit.id}"
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Default.ExpandMore,
                                                            contentDescription = "Expand Unit",
                                                            tint = Color(0xFF64748B),
                                                            modifier = Modifier
                                                                .size(18.dp)
                                                                .rotate(unitChevronRotation)
                                                        )
                                                    }
                                                }

                                                // Topics Level Drill-down
                                                AnimatedVisibility(
                                                    visible = isUnitExpanded,
                                                    enter = AppMotionSystem.cascadeContainerEnterTransition(),
                                                    exit = AppMotionSystem.cascadeContainerExitTransition()
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color(0xFFFAFBFD))
                                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = "Topics under ${unit.name}",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF475569)
                                                        )

                                                        unit.topics.forEachIndexed { topicIdx, topic ->
                                                            val isTopicSelected = topic.id in selectedTopicIds.value
                                                            val totalInTopic = remember(allQuestions, subject.id, unit.id, topic.id) {
                                                                allQuestions.count { it.subjectId == subject.id && it.unitId == unit.id && it.topicId == topic.id && it.isActive }
                                                            }

                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .cascadeDropItem(visible = isUnitExpanded, index = topicIdx)
                                                                    .padding(start = 6.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Column(modifier = Modifier.weight(1f)) {
                                                                    Text(
                                                                        text = topic.name,
                                                                        fontSize = 11.sp,
                                                                        color = Color(0xFF334155)
                                                                    )
                                                                    Text(
                                                                        text = "Available Qs: $totalInTopic",
                                                                        fontSize = 9.sp,
                                                                        color = Color(0xFF94A3B8)
                                                                    )
                                                                }

                                                                Checkbox(
                                                                    checked = isTopicSelected,
                                                                    onCheckedChange = { checked ->
                                                                        if (checked) {
                                                                            selectedTopicIds.value = selectedTopicIds.value + topic.id
                                                                            // Ensure parent unit is not fully unselected if topic selected
                                                                            if (unit.id !in selectedUnitIds.value) {
                                                                                selectedUnitIds.value = selectedUnitIds.value + unit.id
                                                                            }
                                                                        } else {
                                                                            selectedTopicIds.value = selectedTopicIds.value - topic.id
                                                                        }
                                                                    },
                                                                    colors = CheckboxDefaults.colors(
                                                                        checkedColor = mainActionBtnColor
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else if (!isSubjectSelected) {
                                        Text(
                                            text = "Please check this Subject's header box to enable selections.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. CURRENT AFFAIRS DRILL DOWN (Step 10)
            item {
                Text(
                    text = "Current Affairs Selection",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(top = 8.dp)
                )

                val caMonths = remember { SampleData.getMonthlyCurrentAffairs() }
                val totalCaQuestionsCount = remember(allQuestions) {
                    allQuestions.count { it.subjectId == "subj-current-affairs" && it.isActive }
                }

                var isCaExpanded by remember { mutableStateOf(false) }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    border = BorderStroke(
                        width = if (selectedEntireCurrentAffairs || selectedCAContractMonths.value.isNotEmpty()) 1.5.dp else 1.dp,
                        color = if (selectedEntireCurrentAffairs || selectedCAContractMonths.value.isNotEmpty()) mainActionBtnColor else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCaExpanded = !isCaExpanded }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(mainActionBtnColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Newspaper,
                                    contentDescription = null,
                                    tint = mainActionBtnColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Current Affairs",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Available Qs: $totalCaQuestionsCount",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedEntireCurrentAffairs,
                                    onCheckedChange = { checked ->
                                        selectedEntireCurrentAffairs = checked
                                        if (checked) {
                                            selectedCAContractMonths.value = emptySet()
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = mainActionBtnColor
                                    )
                                )
                                Icon(
                                    imageVector = if (isCaExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand CA",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        }

                        AnimatedVisibility(visible = isCaExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            selectedEntireCurrentAffairs = true
                                            selectedCAContractMonths.value = emptySet()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedEntireCurrentAffairs) mainActionBtnColor else Color(0xFFE2E8F0),
                                            contentColor = if (selectedEntireCurrentAffairs) Color.White else Color(0xFF475569)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Text("Option A: All Months", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            selectedEntireCurrentAffairs = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!selectedEntireCurrentAffairs) mainActionBtnColor else Color(0xFFE2E8F0),
                                            contentColor = if (!selectedEntireCurrentAffairs) Color.White else Color(0xFF475569)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Text("Option B: Select Months", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (!selectedEntireCurrentAffairs) {
                                    Text(
                                        text = "Select Months",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    caMonths.forEach { month ->
                                        val monthId = "ca-month-${month.id}"
                                        val isMonthSelected = monthId in selectedCAContractMonths.value
                                        val monthQuestionsCount = remember(allQuestions) {
                                            getQuestionsForMonth(month, allQuestions).size
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color.White)
                                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = month.title,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF1E293B)
                                                )
                                                Text(
                                                    text = "Available Qs: $monthQuestionsCount",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }

                                            Checkbox(
                                                checked = isMonthSelected,
                                                onCheckedChange = { checked ->
                                                    if (checked) {
                                                        selectedCAContractMonths.value = selectedCAContractMonths.value + monthId
                                                    } else {
                                                        selectedCAContractMonths.value = selectedCAContractMonths.value - monthId
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = mainActionBtnColor
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. QUESTION COUNT SELECTION & VALIDATION (Step 13 & 14)
            item {
                Text(
                    text = "5. Question Count & Pattern Allocation",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(top = 8.dp)
                )

                val availablePoolCount = eligiblePool.size
                val isInsufficient = availablePoolCount < requestedQuestionsCount
                val noSelection = !selectedEntireCurrentAffairs && selectedCAContractMonths.value.isEmpty() && selectedSubjectIds.value.isEmpty()
                val hasCa = isCaSelected()
                val totalAllocated = selectedSubjectIds.value.sumOf { subjectManualAllocations[it] ?: 0 } + (if (hasCa) caManualAllocation else 0)
                val isAllocationMatched = (totalAllocated == requestedQuestionsCount) && (totalAllocated > 0)

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isInsufficient || (!isAllocationMatched && !noSelection)) Color(0xFFFBBF24) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Question Pool Available:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF334155)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isInsufficient) Color(0xFFEF4444) else accentThemeColor)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$availablePoolCount Unique Qs",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Target Total Number of Questions setting
                        Text(
                            text = "Set Total Number of Questions:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(50, 100, 150, 200).forEach { amount ->
                                val isSelected = requestedQuestionsCount == amount
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        requestedQuestionsCount = amount
                                        customTotalInput = amount.toString()
                                        distributeQuestionsEvenly(amount)
                                    },
                                    label = { Text(if (amount == 150) "150 (APPSC Std)" else "$amount Qs", fontSize = 11.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = mainActionBtnColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom Total Questions Input Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = customTotalInput,
                                onValueChange = { newVal ->
                                    customTotalInput = newVal.filter { it.isDigit() }
                                    val parsed = customTotalInput.toIntOrNull()
                                    if (parsed != null && parsed in 5..500) {
                                        requestedQuestionsCount = parsed
                                    }
                                },
                                label = { Text("Custom Total Questions", fontSize = 11.sp) },
                                placeholder = { Text("e.g. 150, 200") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    val parsed = customTotalInput.toIntOrNull() ?: requestedQuestionsCount
                                    requestedQuestionsCount = parsed
                                    distributeQuestionsEvenly(parsed)
                                    Toast.makeText(context, "Evenly distributed across selections", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto-Distribute", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Subject-wise & Current Affairs Manual Allocations List
                        Text(
                            text = "Manual Subject-wise Question Allocation:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Specify exact questions per subject and current affairs to match your exam pattern.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (selectedSubjectIds.value.isEmpty() && !hasCa) {
                            Text(
                                text = "Select at least one Subject or Current Affairs above to allocate questions.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                selectedSubjectIds.value.forEach { subId ->
                                    val subj = SampleData.subjectsState.find { it.id == subId }
                                    val count = subjectManualAllocations[subId] ?: 0
                                    val subPoolSize = eligiblePool.count { it.subjectId == subId }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = subj?.name ?: "Subject",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp,
                                                    color = Color(0xFF1E293B)
                                                )
                                                Text(
                                                    text = "Pool Available: $subPoolSize Qs",
                                                    fontSize = 10.5.sp,
                                                    color = if (count > subPoolSize) Color(0xFFDC2626) else Color(0xFF64748B)
                                                )
                                            }

                                            // Stepper & Direct Editable Input
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        if (count > 0) {
                                                            subjectManualAllocations[subId] = count - 5.coerceAtMost(count)
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = Color(0xFF64748B))
                                                }

                                                var textVal by remember(count) { mutableStateOf(count.toString()) }
                                                OutlinedTextField(
                                                    value = textVal,
                                                    onValueChange = { nv ->
                                                        val filtered = nv.filter { it.isDigit() }
                                                        textVal = filtered
                                                        val num = filtered.toIntOrNull() ?: 0
                                                        subjectManualAllocations[subId] = num
                                                    },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    textStyle = androidx.compose.ui.text.TextStyle(
                                                        textAlign = TextAlign.Center,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    ),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.width(62.dp).height(44.dp)
                                                )

                                                IconButton(
                                                    onClick = {
                                                        subjectManualAllocations[subId] = count + 5
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = mainActionBtnColor)
                                                }
                                            }
                                        }
                                    }
                                }

                                // Current Affairs Manual Input
                                if (hasCa) {
                                    val caPoolSize = eligiblePool.count { it.subjectId == "subj-current-affairs" }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFFFBEB),
                                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Current Affairs",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp,
                                                    color = Color(0xFF92400E)
                                                )
                                                Text(
                                                    text = "Pool Available: $caPoolSize Qs",
                                                    fontSize = 10.5.sp,
                                                    color = if (caManualAllocation > caPoolSize) Color(0xFFDC2626) else Color(0xFFB45309)
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        if (caManualAllocation > 0) {
                                                            caManualAllocation = caManualAllocation - 5.coerceAtMost(caManualAllocation)
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = Color(0xFFB45309))
                                                }

                                                var caTextVal by remember(caManualAllocation) { mutableStateOf(caManualAllocation.toString()) }
                                                OutlinedTextField(
                                                    value = caTextVal,
                                                    onValueChange = { nv ->
                                                        val filtered = nv.filter { it.isDigit() }
                                                        caTextVal = filtered
                                                        val num = filtered.toIntOrNull() ?: 0
                                                        caManualAllocation = num
                                                    },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    textStyle = androidx.compose.ui.text.TextStyle(
                                                        textAlign = TextAlign.Center,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    ),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.width(62.dp).height(44.dp)
                                                )

                                                IconButton(
                                                    onClick = {
                                                        caManualAllocation = caManualAllocation + 5
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = Color(0xFFD97706))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Automated Live Validation Banner
                        if (noSelection) {
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Please select at least one subject or current affairs to configure test.", fontSize = 11.5.sp, color = Color(0xFF92400E))
                                }
                            }
                        } else if (isAllocationMatched) {
                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "✓ Perfect Match: $totalAllocated / $requestedQuestionsCount Questions",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = Color(0xFF15803D)
                                        )
                                        Text(
                                            text = "Subject-wise & Current Affairs quantities match the total test pattern 100%.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF166534)
                                        )
                                    }
                                }
                            }
                        } else {
                            Surface(
                                color = if (totalAllocated < requestedQuestionsCount) Color(0xFFFFFBEB) else Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (totalAllocated < requestedQuestionsCount) Color(0xFFFCD34D) else Color(0xFFFCA5A5)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (totalAllocated < requestedQuestionsCount) Icons.Default.Info else Icons.Default.Error,
                                            contentDescription = null,
                                            tint = if (totalAllocated < requestedQuestionsCount) Color(0xFFD97706) else Color(0xFFDC2626),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (totalAllocated < requestedQuestionsCount)
                                                "⚠️ Under-allocated: $totalAllocated / $requestedQuestionsCount Qs (Need ${requestedQuestionsCount - totalAllocated} more)"
                                            else
                                                "❌ Over-allocated: $totalAllocated / $requestedQuestionsCount Qs (Reduce by ${totalAllocated - requestedQuestionsCount})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (totalAllocated < requestedQuestionsCount) Color(0xFFB45309) else Color(0xFFB91C1C)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                distributeQuestionsEvenly(requestedQuestionsCount)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("Balance Evenly", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                requestedQuestionsCount = totalAllocated
                                                customTotalInput = totalAllocated.toString()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = mainActionBtnColor),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("Set Total to $totalAllocated Qs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        if (isInsufficient) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Available pool has only $availablePoolCount questions. Select additional units/topics or lower the question target.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 7. SUMMARY & GENERATE (Step 17)
            item {
                Text(
                    text = "6. Review Mock Test Summary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(top = 8.dp)
                )

                val availablePoolCount = eligiblePool.size
                val isInsufficient = availablePoolCount < requestedQuestionsCount
                val noSelection = !selectedEntireCurrentAffairs && selectedCAContractMonths.value.isEmpty() && selectedSubjectIds.value.isEmpty()
                val hasCa = isCaSelected()
                val totalAllocated = selectedSubjectIds.value.sumOf { subjectManualAllocations[it] ?: 0 } + (if (hasCa) caManualAllocation else 0)
                val isAllocationMatched = (totalAllocated == requestedQuestionsCount) && (totalAllocated > 0)
                val isGenerationEnabled = !isInsufficient && !noSelection && isAllocationMatched

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Generated Mock Test Configuration Summary",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Details list
                        listOf(
                            "Test Type" to selectedMockTestMode.displayName,
                            "Category" to selectedCategory.label,
                            "Total Questions" to "$requestedQuestionsCount Qs",
                            "Total Marks" to "${totalMarksInput.ifEmpty { "100" }} Marks",
                            "Positive Marks" to "+$positiveMarksValue",
                            "Negative Marks" to if (negativeMarksValue == 0.0) "0" else "-$negativeMarksValue",
                            "Duration" to "$totalTimeMinutes Minutes",
                            "Selected Subjects" to "${selectedSubjectIds.value.size} Subjects",
                            "Current Affairs" to (if (hasCa) "$caManualAllocation Qs" else "Not Included"),
                            "Allocation Status" to if (isAllocationMatched) "✓ Validated ($totalAllocated/$requestedQuestionsCount)" else "⚠️ Mismatch ($totalAllocated/$requestedQuestionsCount)",
                            "Eligible Pool" to "$availablePoolCount Questions"
                        ).forEach { (label, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                // Generate logic with exact manual question allocation per subject and current affairs
                                val testTitle = "${selectedMockTestMode.displayName} (${selectedCategory.label}) - ${requestedQuestionsCount}Q Pattern"
                                val finalMarks = totalMarksInput.ifEmpty { "100" }.toDoubleOrNull() ?: (requestedQuestionsCount * positiveMarksValue)

                                val finalSubjectConfigs = selectedSubjectIds.value.map { subId ->
                                    val subj = SampleData.subjectsState.find { it.id == subId }
                                    val unitIds = selectedUnitIds.value.filter { uid -> subj?.units?.any { it.id == uid } == true }
                                    val topicIds = selectedTopicIds.value.filter { tid -> subj?.units?.flatMap { it.topics }?.any { it.id == tid } == true }
                                    val count = subjectManualAllocations[subId] ?: (requestedQuestionsCount / selectedSubjectIds.value.size.coerceAtLeast(1))

                                    SubjectQuestionConfig(
                                        subjectId = subId,
                                        subjectName = subj?.name ?: "Subject",
                                        requestedCount = count,
                                        unitIds = unitIds,
                                        topicIds = topicIds
                                    )
                                } + if (hasCa && caManualAllocation > 0) listOf(
                                    SubjectQuestionConfig(
                                        subjectId = "subj-current-affairs",
                                        subjectName = "Current Affairs",
                                        requestedCount = caManualAllocation
                                    )
                                ) else emptyList()

                                // Sample exact specified counts from each subject pool and CA pool
                                val generatedQuestionsList = mutableListOf<Question>()
                                selectedSubjectIds.value.forEach { subId ->
                                    val countNeeded = subjectManualAllocations[subId] ?: 0
                                    if (countNeeded > 0) {
                                        val subPool = eligiblePool.filter { it.subjectId == subId }
                                        val taken = subPool.shuffled().take(countNeeded)
                                        generatedQuestionsList.addAll(taken)
                                    }
                                }

                                if (hasCa && caManualAllocation > 0) {
                                    val caPool = eligiblePool.filter { it.subjectId == "subj-current-affairs" }
                                    val takenCa = caPool.shuffled().take(caManualAllocation)
                                    generatedQuestionsList.addAll(takenCa)
                                }

                                // If any remaining due to small pool sizes, top up from eligible pool
                                if (generatedQuestionsList.size < requestedQuestionsCount) {
                                    val existingIds = generatedQuestionsList.map { it.id }.toSet()
                                    val extraNeeded = requestedQuestionsCount - generatedQuestionsList.size
                                    val topUps = eligiblePool.filter { it.id !in existingIds }.shuffled().take(extraNeeded)
                                    generatedQuestionsList.addAll(topUps)
                                }

                                val finalGeneratedQuestions = generatedQuestionsList.shuffled().take(requestedQuestionsCount)

                                val mockTest = GeneratedMockTest(
                                    id = UUID.randomUUID().toString(),
                                    appId = activeApp.id,
                                    title = testTitle,
                                    createdAt = System.currentTimeMillis(),
                                    subjectConfigs = finalSubjectConfigs,
                                    totalQuestions = requestedQuestionsCount,
                                    positiveMarks = positiveMarksValue,
                                    negativeMarks = negativeMarksValue,
                                    durationMinutes = totalTimeMinutes,
                                    totalMarks = finalMarks,
                                    questions = finalGeneratedQuestions,
                                    examType = selectedCategory.label,
                                    testMode = selectedMockTestMode,
                                    selectedSubjectNames = selectedSubjectIds.value.mapNotNull { subId -> SampleData.subjectsState.find { it.id == subId }?.name },
                                    selectedUnitNames = selectedUnitIds.value.mapNotNull { uid -> SampleData.subjectsState.flatMap { it.units }.find { it.id == uid }?.name },
                                    selectedTopicNames = selectedTopicIds.value.mapNotNull { tid -> SampleData.subjectsState.flatMap { it.units }.flatMap { it.topics }.find { it.id == tid }?.name },
                                    selectedCurrentAffairsMonths = selectedCAContractMonths.value.mapNotNull { mid -> SampleData.getMonthlyCurrentAffairs().find { "ca-month-${it.id}" == mid }?.title },
                                    availableQuestionPoolCount = availablePoolCount,
                                    status = "Not Started"
                                )

                                onTestGenerated(mockTest)
                                generatedTestResult = mockTest
                                showSuccessDialog = true
                            },
                            enabled = isGenerationEnabled,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = mainActionBtnColor,
                                disabledContainerColor = Color(0xFFCBD5E1)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = adminConfig.generateButtonText,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // SUCCESS DIALOG GENERATED (Step 18 & 19)
    if (showSuccessDialog && generatedTestResult != null) {
        val test = generatedTestResult!!
        Dialog(onDismissRequest = { showSuccessDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(accentThemeColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = accentThemeColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Mock Test Generated!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Successfully compiled and generated your custom ${test.totalQuestions}-question mock test with real syllabus items. It has been saved to your dashboard.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Options Grid
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onStartGeneratedTest(test)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = mainActionBtnColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Mock Test Now", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            showSuccessDialog = false
                            onNavigateToAvailableTests()
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Dashboard, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Saved Tests", fontWeight = FontWeight.SemiBold, color = Color(0xFF475569), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
