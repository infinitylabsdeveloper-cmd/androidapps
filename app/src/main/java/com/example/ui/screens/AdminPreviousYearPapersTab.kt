package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.PreviousYearPaper
import com.example.model.PreviousYearQuestion
import com.example.ui.theme.*
import com.example.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPreviousYearPapersTab() {
    val context = LocalContext.current
    val currentAppId = SampleData.getActiveApp().id

    var activeAdminTab by remember { mutableStateOf("Feature Controls") }
    val adminTabs = listOf("Feature Controls", "Years & Papers", "Question Bank", "Exam Settings", "Analytics")

    // State for Config
    var currentConfig by remember {
        mutableStateOf(PYQConfigStore.getConfigForApp(context, currentAppId))
    }

    // State for Scope
    var applyScope by remember { mutableStateOf("THIS_APP") } // THIS_APP, SELECTED_APPS, ALL_APPS
    var selectedAppIds by remember { mutableStateOf<List<String>>(listOf(currentAppId)) }
    var showSelectAppsDialog by remember { mutableStateOf(false) }

    // State for Feature Controls
    var isPYQEnabled by remember { mutableStateOf(currentConfig.enabled) }
    var availabilityStatus by remember { mutableStateOf(currentConfig.availabilityStatus) }
    var disabledMsgType by remember { mutableStateOf(currentConfig.disabledMessageType) }
    var customDisabledMsg by remember { mutableStateOf(currentConfig.disabledCustomMessage) }
    var scheduledDate by remember { mutableStateOf(currentConfig.scheduledDateTime) }

    var fullPaperPractice by remember { mutableStateOf(currentConfig.featureToggles.fullPaperPractice) }
    var practiceMode by remember { mutableStateOf(currentConfig.featureToggles.practiceMode) }
    var examMode by remember { mutableStateOf(currentConfig.featureToggles.examMode) }
    var answerExplanations by remember { mutableStateOf(currentConfig.featureToggles.answerExplanations) }
    var bookmarks by remember { mutableStateOf(currentConfig.featureToggles.bookmarks) }
    var wrongAnswersPractice by remember { mutableStateOf(currentConfig.featureToggles.wrongAnswersPractice) }
    var reattempts by remember { mutableStateOf(currentConfig.featureToggles.reattempts) }
    var analytics by remember { mutableStateOf(currentConfig.featureToggles.analytics) }

    // State for Papers
    var papers by remember { mutableStateOf(PreviousYearPaperStore.getAllPapers(context)) }
    var showAddPaperDialog by remember { mutableStateOf(false) }

    // State for Question Bank
    var searchQuery by remember { mutableStateOf("") }
    var jsonInput by remember { mutableStateOf("") }
    var showAddQuestionDialog by remember { mutableStateOf(false) }

    // State for Exam Settings
    var defaultQuestionsCount by remember { mutableIntStateOf(currentConfig.defaultQuestionsCount) }
    var defaultTimeLimitMinutes by remember { mutableIntStateOf(currentConfig.defaultTimeLimitMinutes) }
    var marksPerCorrectAnswer by remember { mutableDoubleStateOf(currentConfig.marksPerCorrectAnswer) }
    var negativeMarks by remember { mutableDoubleStateOf(currentConfig.negativeMarks) }

    // Import Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val content = inputStream.bufferedReader().use { r -> r.readText() }
                    val result = PreviousYearPaperStore.parseJsonImport(content)
                    if (result.isSuccess) {
                        val newPaper = result.getOrNull()
                        if (newPaper != null) {
                            PreviousYearPaperStore.savePaper(context, newPaper)
                            papers = PreviousYearPaperStore.getAllPapers(context)
                            Toast.makeText(context, "✅ Paper imported & saved successfully!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "❌ Import failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "❌ Read error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("PYQs Management", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                Text("App: ${SampleData.getActiveApp().name}", fontSize = 12.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val updated = PYQAppConfig(
                        appId = currentAppId,
                        enabled = isPYQEnabled,
                        availabilityStatus = availabilityStatus,
                        scheduledDateTime = scheduledDate,
                        disabledMessageType = disabledMsgType,
                        disabledCustomMessage = customDisabledMsg,
                        featureToggles = PYQFeatureToggles(
                            fullPaperPractice = fullPaperPractice,
                            practiceMode = practiceMode,
                            examMode = examMode,
                            answerExplanations = answerExplanations,
                            bookmarks = bookmarks,
                            wrongAnswersPractice = wrongAnswersPractice,
                            reattempts = reattempts,
                            analytics = analytics
                        ),
                        defaultQuestionsCount = defaultQuestionsCount,
                        defaultTimeLimitMinutes = defaultTimeLimitMinutes,
                        marksPerCorrectAnswer = marksPerCorrectAnswer,
                        negativeMarks = negativeMarks
                    )

                    PYQConfigStore.saveConfig(context, updated, applyScope, selectedAppIds)
                    currentConfig = updated
                    Toast.makeText(context, "🎉 Settings saved successfully for $applyScope!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Settings")
            }
        }

        // Sub Tabs Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(adminTabs) { tab ->
                val isSel = activeAdminTab == tab
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) Color(0xFFEFF6FF) else Color.White,
                    border = BorderStroke(1.dp, if (isSel) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
                    modifier = Modifier.clickable { activeAdminTab = tab }
                ) {
                    Text(
                        tab,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Color(0xFF2563EB) else Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        when (activeAdminTab) {
            "Feature Controls" -> AdminFeatureControlsView(
                applyScope = applyScope,
                onScopeChange = { applyScope = it },
                onSelectAppsClick = { showSelectAppsDialog = true },
                isPYQEnabled = isPYQEnabled,
                onPYQEnabledChange = { isPYQEnabled = it },
                availabilityStatus = availabilityStatus,
                onStatusChange = { availabilityStatus = it },
                disabledMsgType = disabledMsgType,
                onMsgTypeChange = { disabledMsgType = it },
                customDisabledMsg = customDisabledMsg,
                onCustomMsgChange = { customDisabledMsg = it },
                scheduledDate = scheduledDate,
                onScheduledDateChange = { scheduledDate = it },
                fullPaperPractice = fullPaperPractice,
                onFullPaperChange = { fullPaperPractice = it },
                practiceMode = practiceMode,
                onPracticeModeChange = { practiceMode = it },
                examMode = examMode,
                onExamModeChange = { examMode = it },
                answerExplanations = answerExplanations,
                onAnswerExpChange = { answerExplanations = it },
                bookmarks = bookmarks,
                onBookmarksChange = { bookmarks = it },
                wrongAnswersPractice = wrongAnswersPractice,
                onWrongAnswersChange = { wrongAnswersPractice = it },
                reattempts = reattempts,
                onReattemptsChange = { reattempts = it },
                analytics = analytics,
                onAnalyticsChange = { analytics = it }
            )

            "Years & Papers" -> AdminYearsAndPapersView(
                papers = papers,
                onAddPaperClick = { showAddPaperDialog = true },
                onTogglePublish = { paperId ->
                    PreviousYearPaperStore.togglePublish(context, paperId)
                    papers = PreviousYearPaperStore.getAllPapers(context)
                },
                onDeletePaper = { paperId ->
                    PreviousYearPaperStore.deletePaper(context, paperId)
                    papers = PreviousYearPaperStore.getAllPapers(context)
                }
            )

            "Question Bank" -> AdminQuestionBankView(
                papers = papers,
                onImportJson = { filePickerLauncher.launch("application/json") },
                onAddQuestionClick = { showAddQuestionDialog = true }
            )

            "Exam Settings" -> AdminExamSettingsView(
                questionsCount = defaultQuestionsCount,
                onQuestionsCountChange = { defaultQuestionsCount = it },
                timeLimit = defaultTimeLimitMinutes,
                onTimeLimitChange = { defaultTimeLimitMinutes = it },
                marksPerCorrect = marksPerCorrectAnswer,
                onMarksChange = { marksPerCorrectAnswer = it },
                negativeMarks = negativeMarks,
                onNegativeChange = { negativeMarks = it }
            )

            "Analytics" -> AdminAnalyticsReportsView()
        }
    }

    // Dialogs
    if (showSelectAppsDialog) {
        AlertDialog(
            onDismissRequest = { showSelectAppsDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Select Apps to Apply Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SampleData.appsListState.forEach { app ->
                        val isChecked = selectedAppIds.contains(app.id)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                            selectedAppIds = if (isChecked) selectedAppIds - app.id else selectedAppIds + app.id
                        }) {
                            Checkbox(checked = isChecked, onCheckedChange = { chk ->
                                selectedAppIds = if (chk) selectedAppIds + app.id else selectedAppIds - app.id
                            })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(app.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSelectAppsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showAddPaperDialog) {
        var newExamName by remember { mutableStateOf("APPSC Group-2") }
        var newYear by remember { mutableStateOf("2025") }
        var newStage by remember { mutableStateOf("Prelims") }

        AlertDialog(
            onDismissRequest = { showAddPaperDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Add New Examination Year Paper", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newExamName,
                        onValueChange = { newExamName = it },
                        label = { Text("Exam Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newYear,
                        onValueChange = { newYear = it },
                        label = { Text("Year") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newStage,
                        onValueChange = { newStage = it },
                        label = { Text("Stage (Prelims/Mains)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val yearInt = newYear.toIntOrNull() ?: 2025
                    val paper = PreviousYearPaper(
                        id = "paper_${System.currentTimeMillis()}",
                        appId = currentAppId,
                        examName = newExamName,
                        year = yearInt,
                        examDate = "$yearInt-02-15",
                        shift = newStage,
                        language = "English",
                        subject = "General Studies",
                        isPublished = true,
                        questions = SampleData.questionsState.take(20).mapIndexed { idx, q -> PreviousYearQuestion(idx + 1, q.questionText, q.options, q.correctOption, q.explanation, q.difficulty) }
                    )
                    PreviousYearPaperStore.savePaper(context, paper)
                    papers = PreviousYearPaperStore.getAllPapers(context)
                    showAddPaperDialog = false
                    Toast.makeText(context, "✅ Paper added successfully!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Save Paper")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPaperDialog = false }) { Text("Cancel") }
            }
        )
    }
}

/**
 * Sub-Tab 1: Feature Controls (Image 2, Screens 1, 2, 3)
 */
@Composable
private fun AdminFeatureControlsView(
    applyScope: String,
    onScopeChange: (String) -> Unit,
    onSelectAppsClick: () -> Unit,
    isPYQEnabled: Boolean,
    onPYQEnabledChange: (Boolean) -> Unit,
    availabilityStatus: PYQAvailabilityStatus,
    onStatusChange: (PYQAvailabilityStatus) -> Unit,
    disabledMsgType: PYQDisabledMessageType,
    onMsgTypeChange: (PYQDisabledMessageType) -> Unit,
    customDisabledMsg: String,
    onCustomMsgChange: (String) -> Unit,
    scheduledDate: String,
    onScheduledDateChange: (String) -> Unit,
    fullPaperPractice: Boolean,
    onFullPaperChange: (Boolean) -> Unit,
    practiceMode: Boolean,
    onPracticeModeChange: (Boolean) -> Unit,
    examMode: Boolean,
    onExamModeChange: (Boolean) -> Unit,
    answerExplanations: Boolean,
    onAnswerExpChange: (Boolean) -> Unit,
    bookmarks: Boolean,
    onBookmarksChange: (Boolean) -> Unit,
    wrongAnswersPractice: Boolean,
    onWrongAnswersChange: (Boolean) -> Unit,
    reattempts: Boolean,
    onReattemptsChange: (Boolean) -> Unit,
    analytics: Boolean,
    onAnalyticsChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Apply Settings Scope Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Apply Settings To", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onScopeChange("THIS_APP") }) {
                        RadioButton(selected = applyScope == "THIS_APP", onClick = { onScopeChange("THIS_APP") })
                        Text("This App Only", fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onScopeChange("SELECTED_APPS"); onSelectAppsClick() }) {
                        RadioButton(selected = applyScope == "SELECTED_APPS", onClick = { onScopeChange("SELECTED_APPS"); onSelectAppsClick() })
                        Text("Selected Apps", fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onScopeChange("ALL_APPS") }) {
                        RadioButton(selected = applyScope == "ALL_APPS", onClick = { onScopeChange("ALL_APPS") })
                        Text("All Apps", fontSize = 13.sp)
                    }
                }
            }
        }

        // Main Feature Switches Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("PYQs Feature Controls", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))

                val toggles = listOf(
                    Triple("Enable Year-wise PYQs", "Master switch to show/hide Previous Year Questions section", isPYQEnabled to onPYQEnabledChange),
                    Triple("Full Paper Practice", "Allow full paper practice with timer", fullPaperPractice to onFullPaperChange),
                    Triple("Practice Mode", "Allow untimed learning practice", practiceMode to onPracticeModeChange),
                    Triple("Exam Mode", "Allow real exam mode with strict timer", examMode to onExamModeChange),
                    Triple("Answer Explanations", "Show solutions and detailed explanations after submit", answerExplanations to onAnswerExpChange),
                    Triple("Bookmarks", "Allow bookmarking difficult questions", bookmarks to onBookmarksChange),
                    Triple("Wrong Answers Practice", "Allow dedicated practice of wrong answers", wrongAnswersPractice to onWrongAnswersChange),
                    Triple("Reattempts", "Allow unlimited paper reattempts", reattempts to onReattemptsChange),
                    Triple("Analytics & Trends", "Show performance graphs and accuracy trends", analytics to onAnalyticsChange)
                )

                toggles.forEach { (title, sub, statePair) ->
                    val (state, onChg) = statePair
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                            Text(sub, fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Switch(checked = state, onCheckedChange = onChg)
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }

        // Availability Status & Message Config Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("PYQs Availability & Disabled Message", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val statuses = listOf("ENABLED" to PYQAvailabilityStatus.ENABLED, "DISABLED" to PYQAvailabilityStatus.DISABLED, "SCHEDULED" to PYQAvailabilityStatus.SCHEDULED)
                    statuses.forEach { (lbl, stat) ->
                        val isSel = availabilityStatus == stat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                            modifier = Modifier.weight(1f).clickable { onStatusChange(stat) }
                        ) {
                            Text(lbl, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color(0xFF475569), modifier = Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center)
                        }
                    }
                }

                if (availabilityStatus != PYQAvailabilityStatus.ENABLED) {
                    Text("Configured Message Text", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val msgTypes = listOf("Updating Soon..." to PYQDisabledMessageType.UPDATING_SOON, "Coming Soon..." to PYQDisabledMessageType.COMING_SOON, "Custom" to PYQDisabledMessageType.CUSTOM)
                        msgTypes.forEach { (lbl, t) ->
                            val isSel = disabledMsgType == t
                            FilterChip(
                                selected = isSel,
                                onClick = { onMsgTypeChange(t) },
                                label = { Text(lbl, fontSize = 11.sp) }
                            )
                        }
                    }

                    if (disabledMsgType == PYQDisabledMessageType.CUSTOM) {
                        OutlinedTextField(
                            value = customDisabledMsg,
                            onValueChange = onCustomMsgChange,
                            label = { Text("Custom Message") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (availabilityStatus == PYQAvailabilityStatus.SCHEDULED) {
                        OutlinedTextField(
                            value = scheduledDate,
                            onValueChange = onScheduledDateChange,
                            label = { Text("Scheduled Date & Time (e.g. 15 Nov 2024)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sub-Tab 2: Years & Papers Table (Image 2, Screen 4)
 */
@Composable
private fun AdminYearsAndPapersView(
    papers: List<PreviousYearPaper>,
    onAddPaperClick: () -> Unit,
    onTogglePublish: (String) -> Unit,
    onDeletePaper: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Examination Years & Papers", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Button(onClick = onAddPaperClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Paper")
            }
        }

        papers.forEach { paper ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${paper.examName} (${paper.year})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Stage: ${paper.shift} | Questions: ${paper.questions.size}", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (paper.isPublished) "Published" else "Draft", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (paper.isPublished) Color(0xFF10B981) else Color(0xFFF59E0B))
                        Switch(checked = paper.isPublished, onCheckedChange = { onTogglePublish(paper.id) })
                        IconButton(onClick = { onDeletePaper(paper.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sub-Tab 3: Question Bank View (Image 2, Screen 5)
 */
@Composable
private fun AdminQuestionBankView(
    papers: List<PreviousYearPaper>,
    onImportJson: () -> Unit,
    onAddQuestionClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Question Bank Management", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onImportJson) { Text("Import JSON") }
                Button(onClick = onAddQuestionClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))) { Text("Add Question") }
            }
        }

        val allQuestions = papers.flatMap { p -> p.questions.map { q -> p.examName to q } }
        allQuestions.take(10).forEachIndexed { idx, (exam, q) ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Q${idx + 1}. ${q.question}", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 2)
                    Text("Correct: ${q.correctAnswer} | Exam: $exam", fontSize = 11.sp, color = Color(0xFF10B981))
                }
            }
        }
    }
}

/**
 * Sub-Tab 4: Exam Settings View (Image 2, Screen 6)
 */
@Composable
private fun AdminExamSettingsView(
    questionsCount: Int,
    onQuestionsCountChange: (Int) -> Unit,
    timeLimit: Int,
    onTimeLimitChange: (Int) -> Unit,
    marksPerCorrect: Double,
    onMarksChange: (Double) -> Unit,
    negativeMarks: Double,
    onNegativeChange: (Double) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Default Paper Exam Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            OutlinedTextField(
                value = questionsCount.toString(),
                onValueChange = { onQuestionsCountChange(it.toIntOrNull() ?: 200) },
                label = { Text("Total Questions Count") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = timeLimit.toString(),
                onValueChange = { onTimeLimitChange(it.toIntOrNull() ?: 120) },
                label = { Text("Time Limit (Minutes)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = marksPerCorrect.toString(),
                onValueChange = { onMarksChange(it.toDoubleOrNull() ?: 1.0) },
                label = { Text("Marks per Correct Answer") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = negativeMarks.toString(),
                onValueChange = { onNegativeChange(it.toDoubleOrNull() ?: 0.33) },
                label = { Text("Negative Marking per Wrong Answer") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Sub-Tab 5: Admin Analytics & Reports View (Image 2, Screen 7)
 */
@Composable
private fun AdminAnalyticsReportsView() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFEFF6FF), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("980", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF2563EB))
                    Text("Total Questions", fontSize = 10.sp, color = Color(0xFF2563EB))
                }
            }
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFDCFCE7), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("820", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF15803D))
                    Text("Published", fontSize = 10.sp, color = Color(0xFF15803D))
                }
            }
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFEF3C7), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("160", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFFD97706))
                    Text("Unpublished", fontSize = 10.sp, color = Color(0xFFD97706))
                }
            }
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFCE4EC), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("12,450", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFFEC4899))
                    Text("Total Attempts", fontSize = 10.sp, color = Color(0xFFEC4899))
                }
            }
        }
    }
}
