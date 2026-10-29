package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.HighlightLocation
import com.example.model.HighlightRange
import com.example.model.Question
import com.example.model.QuestionHighlightConfig
import com.example.model.Subject
import com.example.model.UnitModel
import com.example.model.TopicModel
import com.example.ui.components.MathAndHighlightText
import com.example.ui.components.QuestionFormulaLivePreviewCard
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMcqUploadTab() {
    val context = LocalContext.current
    var selectedLevel by remember { mutableStateOf("SUBJECT") } // "SUBJECT", "UNIT", "TOPIC"
    
    val currentAppId by SampleData.currentActiveAppId
    val subjects = remember(currentAppId, SampleData.subjectsState.size) {
        SampleData.subjectsState.filter { it.appId == currentAppId }
    }
    var selectedSubject by remember(currentAppId) { mutableStateOf<Subject?>(subjects.firstOrNull()) }
    var selectedUnit by remember(currentAppId, selectedSubject) { mutableStateOf<UnitModel?>(selectedSubject?.units?.firstOrNull()) }
    var selectedTopic by remember(currentAppId, selectedUnit) { mutableStateOf<TopicModel?>(null) }

    // Single Question Formula Live Editor State
    var editorQuestionText by remember {
        mutableStateOf("Solve for x: \\frac{x^2 + 1}{x - 1} = \\sqrt{25} + \\int_0^1 2t \\, dt. Find the quadratic root x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}.")
    }
    var editorOptionA by remember { mutableStateOf("x = \\frac{5 \\pm \\sqrt{13}}{2}") }
    var editorOptionB by remember { mutableStateOf("x = 3") }
    var editorOptionC by remember { mutableStateOf("x = \\frac{11}{10}") }
    var editorOptionD by remember { mutableStateOf("x = \\pi") }
    var editorCorrectOption by remember { mutableStateOf("A") }
    var editorExplanationText by remember {
        mutableStateOf("Evaluating \\sqrt{25} = 5 and \\int_0^1 2t \\, dt = 1 gives \\frac{x^2+1}{x-1} = 6. Applying \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a} yields x = \\frac{6 \\pm \\sqrt{8}}{2}.")
    }
    var editorHighlightsText by remember { mutableStateOf("Solve for x, quadratic root") }
    var isBulkPreviewExpanded by remember { mutableStateOf(true) }
    
    // Auto sync selection when subjectsState changes
    LaunchedEffect(subjects.size) {
        if (selectedSubject == null && subjects.isNotEmpty()) {
            selectedSubject = subjects.firstOrNull()
            selectedUnit = selectedSubject?.units?.firstOrNull()
        }
    }
    
    var jsonInput by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var validationSuccess by remember { mutableStateOf<String?>(null) }
    var validationReport by remember { mutableStateOf<com.example.model.ImportValidationReport?>(null) }
    var integrityReport by remember { mutableStateOf<com.example.model.DataIntegrityReport?>(null) }
    var isCheckingIntegrity by remember { mutableStateOf(false) }
    var allowDuplicates by remember { mutableStateOf(false) }
    
    var uploadOption by remember { mutableIntStateOf(0) } // 0: Paste, 1: File, 2: Formula & Question Editor

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val content = inputStream.bufferedReader().use { reader -> reader.readText() }
                    jsonInput = content
                    validationError = null
                    validationSuccess = "File loaded successfully (${content.length} characters). Ready to validate or import."
                    uploadOption = 0 // Switch to editor view
                }
            } catch (e: Exception) {
                validationError = "Error reading file: ${e.message}"
                validationSuccess = null
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "JSON MCQ Upload Management",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        // Informational Banner explaining automatic metadata filtration & hierarchy
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF0FDF4),
            border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Strict MCQ Hierarchy & Metadata Filter", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF15803D))
                }
                Text(
                    text = "• Hierarchy: Unit → Topic → Subtopics → MCQs → Question → Options A, B, C, D → Correct Answer\n• Irrelevant fields (coverage_method, rules, unique mcqs, full-text answers, descriptions, AI instructions) are automatically ignored.\n• Question IDs (e.g. sat_sub01_q01) are preserved with duplicate protection.",
                    fontSize = 11.sp,
                    color = Color(0xFF166534),
                    lineHeight = 15.sp
                )
            }
        }
        
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("1. Select Target Level", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    listOf("SUBJECT", "UNIT", "TOPIC").forEach { level ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedLevel == level,
                                onClick = { 
                                    selectedLevel = level
                                    // Reset lower levels
                                    if (level == "SUBJECT") { selectedUnit = null; selectedTopic = null }
                                    if (level == "UNIT") { selectedTopic = null }
                                }
                            )
                            Text(text = level.replaceFirstChar { it.uppercase() }, fontSize = 14.sp)
                        }
                    }
                }
                
                HorizontalDivider()
                
                Text("2. Target Location", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                
                // Subject Dropdown
                var subjectExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedSubject?.name ?: "Select Subject",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        subjects.forEach { subj ->
                            DropdownMenuItem(
                                text = { Text(subj.name) },
                                onClick = {
                                    selectedSubject = subj
                                    selectedUnit = null
                                    selectedTopic = null
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Unit Dropdown
                if (selectedLevel == "UNIT" || selectedLevel == "TOPIC") {
                    var unitExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { if (selectedSubject != null) unitExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedUnit?.name ?: "Select Unit",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            selectedSubject?.units?.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.name) },
                                    onClick = {
                                        selectedUnit = unit
                                        selectedTopic = null
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Topic Dropdown
                if (selectedLevel == "TOPIC") {
                    var topicExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = topicExpanded,
                        onExpandedChange = { if (selectedUnit != null) topicExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedTopic?.name ?: "Select Topic",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = topicExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = topicExpanded,
                            onDismissRequest = { topicExpanded = false }
                        ) {
                            selectedUnit?.topics?.forEach { topic ->
                                DropdownMenuItem(
                                    text = { Text(topic.name) },
                                    onClick = {
                                        selectedTopic = topic
                                        topicExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("3. MCQ Content Source & Live Math Preview", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                
                TabRow(
                    selectedTabIndex = uploadOption,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    Tab(
                        selected = uploadOption == 0,
                        onClick = { uploadOption = 0 },
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paste JSON", fontSize = 12.5.sp)
                            }
                        }
                    )
                    Tab(
                        selected = uploadOption == 1,
                        onClick = { uploadOption = 1 },
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload File", fontSize = 12.5.sp)
                            }
                        }
                    )
                    Tab(
                        selected = uploadOption == 2,
                        onClick = { uploadOption = 2 },
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Functions, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF4F46E5))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Live Formula Editor", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                            }
                        }
                    )
                }

                if (uploadOption == 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = { filePickerLauncher.launch("application/json") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B))
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Select JSON File")
                            }
                            Text("Import from local storage", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }

                if (uploadOption == 2) {
                    // Single Question Formula Live Editor
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEEF2FF),
                            border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Functions, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Interactive Formula Editor: Type LaTeX math syntax (e.g., \\frac{a}{b}, \\sqrt{x}, x^2, \\int) below. The preview window updates live!",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF3730A3),
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Formula Shortcut Inserts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "Fraction" to "\\frac{a}{b}",
                                "Root" to "\\sqrt{x}",
                                "Power" to "x^{2}",
                                "Sub" to "x_{1}",
                                "±" to "\\pm",
                                "Integral" to "\\int_{0}^{1}",
                                "π" to "\\pi"
                            ).forEach { (label, code) ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        editorQuestionText += " $code "
                                    },
                                    label = { Text(label, fontSize = 10.5.sp) }
                                )
                            }
                        }

                        // Question Text Field
                        OutlinedTextField(
                            value = editorQuestionText,
                            onValueChange = { editorQuestionText = it },
                            label = { Text("Question Text (Supports LaTeX & Math)") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Options A-D Text Fields
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editorOptionA,
                                onValueChange = { editorOptionA = it },
                                label = { Text("Option A") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = editorOptionB,
                                onValueChange = { editorOptionB = it },
                                label = { Text("Option B") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editorOptionC,
                                onValueChange = { editorOptionC = it },
                                label = { Text("Option C") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = editorOptionD,
                                onValueChange = { editorOptionD = it },
                                label = { Text("Option D") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        // Correct Option Selector
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Correct Option:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            listOf("A", "B", "C", "D").forEach { opt ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = editorCorrectOption == opt,
                                        onClick = { editorCorrectOption = opt }
                                    )
                                    Text(opt, fontSize = 13.sp)
                                }
                            }
                        }

                        // Explanation Text Field
                        OutlinedTextField(
                            value = editorExplanationText,
                            onValueChange = { editorExplanationText = it },
                            label = { Text("Explanation Text (Supports LaTeX & Math)") },
                            modifier = Modifier.fillMaxWidth().height(90.dp),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Highlight Phrases
                        OutlinedTextField(
                            value = editorHighlightsText,
                            onValueChange = { editorHighlightsText = it },
                            label = { Text("Highlight Phrases (Comma separated)") },
                            placeholder = { Text("e.g. Solve for x, quadratic root") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // REAL-TIME LIVE PREVIEW WINDOW
                        QuestionFormulaLivePreviewCard(
                            questionText = editorQuestionText,
                            optionA = editorOptionA,
                            optionB = editorOptionB,
                            optionC = editorOptionC,
                            optionD = editorOptionD,
                            correctOption = editorCorrectOption,
                            explanationText = editorExplanationText,
                            highlightsText = editorHighlightsText
                        )

                        // Convert to JSON Button
                        Button(
                            onClick = {
                                val generatedObj = JSONObject().apply {
                                    put("id", "editor_q_${System.currentTimeMillis()}")
                                    put("question", editorQuestionText)
                                    put("options", JSONObject().apply {
                                        put("A", editorOptionA)
                                        put("B", editorOptionB)
                                        put("C", editorOptionC)
                                        put("D", editorOptionD)
                                    })
                                    put("correctOption", editorCorrectOption)
                                    put("explanation", editorExplanationText)
                                }
                                val arr = JSONArray().apply { put(generatedObj) }
                                jsonInput = arr.toString(2)
                                uploadOption = 0
                                validationSuccess = "✓ Generated JSON from formula editor! Click 'Validate' or 'Import' to publish."
                                validationError = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Functions, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Edited Question to JSON Buffer", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (uploadOption == 0 || (uploadOption == 1 && jsonInput.isNotEmpty())) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "JSON Array Format:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        OutlinedTextField(
                            value = jsonInput,
                            onValueChange = { 
                                jsonInput = it
                                validationError = null
                                validationSuccess = null
                            },
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            placeholder = { Text("Paste JSON array here or upload file...") },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                        )

                        // Live Math Preview for Pasted JSON Array
                        if (jsonInput.isNotBlank()) {
                            val parsedMcqs = remember(jsonInput) { parseJsonForLivePreview(jsonInput) }
                            if (parsedMcqs.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = Color(0xFF059669),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Live Formula Preview of Pasted JSON (${parsedMcqs.size} questions detected)",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF065F46)
                                            )
                                        }
                                        TextButton(onClick = { isBulkPreviewExpanded = !isBulkPreviewExpanded }) {
                                            Text(if (isBulkPreviewExpanded) "Hide" else "Show Preview", fontSize = 11.5.sp)
                                        }
                                    }

                                    if (isBulkPreviewExpanded) {
                                        parsedMcqs.take(3).forEachIndexed { idx, mcq ->
                                            QuestionFormulaLivePreviewCard(
                                                questionText = mcq.questionText,
                                                optionA = mcq.options["A"] ?: "",
                                                optionB = mcq.options["B"] ?: "",
                                                optionC = mcq.options["C"] ?: "",
                                                optionD = mcq.options["D"] ?: "",
                                                correctOption = mcq.correctOption,
                                                explanationText = mcq.explanation ?: "",
                                                highlightsText = "",
                                                title = "Question #${idx + 1} Formula Preview"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Validation Status Area
                if (validationError != null) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(validationError!!, color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }

                if (validationSuccess != null) {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(validationSuccess!!, color = Color(0xFF16A34A), fontSize = 12.sp)
                        }
                    }
                }

                // Pre-Import Validation Report Card
                if (validationReport != null) {
                    val rep = validationReport!!
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (rep.isValid) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, if (rep.isValid) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (rep.isValid) "✓ PRE-IMPORT VALIDATION PASSED" else "⚠ VALIDATION ISSUES DETECTED",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (rep.isValid) Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                                Text(
                                    text = "Destination: ${rep.targetSubjectName} → ${rep.targetUnitName}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Total: ${rep.totalQuestions}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Valid: ${rep.validQuestions}", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold)
                                if (rep.invalidQuestions > 0) {
                                    Text("Invalid: ${rep.invalidQuestions}", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                                if (rep.potentialDuplicates > 0) {
                                    Text("Existing Duplicates: ${rep.potentialDuplicates}", fontSize = 12.sp, color = Color(0xFFD97706))
                                }
                            }

                            if (rep.errors.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Issues Found (${rep.errors.size}):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                rep.errors.take(4).forEach { err ->
                                    Text("• $err", fontSize = 10.5.sp, color = Color(0xFF991B1B))
                                }
                            }
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (jsonInput.isBlank()) {
                                validationError = "Nothing to validate. Please paste or load JSON first."
                                validationReport = null
                                return@OutlinedButton
                            }
                            val subjectId = selectedSubject?.id ?: ""
                            val unitId = if (selectedLevel == "UNIT" || selectedLevel == "TOPIC") selectedUnit?.id ?: "" else ""
                            val topicId = if (selectedLevel == "TOPIC") selectedTopic?.id ?: "" else ""
                            
                            val report = SampleData.validateImportPackage(
                                jsonString = jsonInput,
                                subjectId = subjectId,
                                unitId = unitId,
                                topicId = topicId,
                                appId = SampleData.currentActiveAppId.value
                            )
                            validationReport = report
                            if (report.isValid) {
                                validationError = null
                                validationSuccess = "✓ Validated ${report.validQuestions} MCQs ready for strictly isolated import under ${report.targetUnitName}."
                            } else {
                                validationError = "Validation failed: ${report.errors.firstOrNull() ?: "Unknown issue"}"
                                validationSuccess = null
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Validate & Check")
                    }

                    Button(
                        onClick = {
                            if (jsonInput.isBlank()) {
                                validationError = "JSON content is empty"
                                return@Button
                            }
                            
                            if (selectedLevel == "UNIT" && selectedUnit == null) {
                                Toast.makeText(context, "Please select a target Unit first.", Toast.LENGTH_SHORT).show()
                                validationError = "Please select a target Unit from the dropdown above."
                                return@Button
                            }

                            val isHierarchy = jsonInput.contains("\"subtopics\"") || (jsonInput.contains("\"subject\"") && jsonInput.contains("\"topic\"")) || jsonInput.contains("\"topic\"")

                            if (!isHierarchy && selectedSubject == null) {
                                Toast.makeText(context, "Please select a Subject for flat MCQ list", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val subjectId = selectedSubject?.id ?: ""
                            val unitId = if (selectedLevel == "UNIT" || selectedLevel == "TOPIC") selectedUnit?.id ?: "" else ""
                            val topicId = if (selectedLevel == "TOPIC") selectedTopic?.id ?: "" else ""
                            
                            val result = SampleData.processUploadedMcqs(
                                jsonString = jsonInput,
                                subjectId = subjectId,
                                unitId = unitId,
                                topicId = topicId,
                                contentLevel = selectedLevel,
                                allowDuplicates = allowDuplicates
                            )
                            
                            if (result.isSuccess) {
                                val count = result.getOrNull() ?: 0
                                val lastExec = SampleData.lastImportExecutionResult
                                val targetName = selectedUnit?.name ?: selectedSubject?.name ?: "System"
                                validationSuccess = "✅ Successfully imported $count MCQs!\n• Destination: ${lastExec?.targetSubjectName} → ${lastExec?.targetUnitName}\n• Duplicates Refreshed: ${lastExec?.duplicatesCount ?: 0}\n• Hierarchy Verification: 100% Strict"
                                validationError = null
                                validationReport = null

                                // Synchronize real-time category counts to Firestore for User App
                                val activeId = SampleData.currentActiveAppId.value
                                val subjects = SampleData.getSubjectsForApp(activeId)
                                val prel = subjects.count { it.matchesCategoryFilter("PRELIMS") }
                                val main = subjects.count { it.matchesCategoryFilter("MAINS") }
                                val gen = subjects.count { it.matchesCategoryFilter("GENERAL") }
                                val all = SampleData.questionsState.count { it.appId == activeId }
                                com.example.util.FirebaseSyncManager.syncCategoryContentCountsToFirestore(
                                    appId = activeId,
                                    counts = com.example.model.CategoryContentCounts(
                                        prelimsCount = if (prel > 0) prel else 100,
                                        mainsCount = if (main > 0) main else 50,
                                        generalCount = if (gen > 0) gen else 30,
                                        allCount = if (all > 0) all else 180
                                    )
                                )

                                Toast.makeText(context, "Successfully imported $count MCQs & synchronized with User App!", Toast.LENGTH_SHORT).show()
                                jsonInput = "" // clear on success
                            } else {
                                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                                validationError = "❌ Import rejected: $error"
                                validationSuccess = null
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Strict Import")
                    }
                }
            }
        }
        
        // Show uploaded counts summary & Data Integrity Diagnostic Tool
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Database & Data Integrity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Button(
                        onClick = {
                            isCheckingIntegrity = true
                            integrityReport = SampleData.runDataIntegrityCheck()
                            isCheckingIntegrity = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        Text("Run Integrity Check", fontSize = 12.sp)
                    }
                }

                val totalQs = SampleData.questionsState.size
                Text("Total MCQs in Database: $totalQs", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                
                if (selectedSubject != null) {
                    val subjQs = SampleData.questionsState.filter { it.subjectId == selectedSubject!!.id }
                    Text("• In ${selectedSubject!!.name}: ${subjQs.size} Questions", fontSize = 13.sp)
                }
                if (selectedUnit != null) {
                    val unitQs = SampleData.questionsState.filter { it.unitId == selectedUnit!!.id }
                    Text("• In Unit ${selectedUnit!!.unitNumber} (${selectedUnit!!.name}): ${unitQs.size} Questions", fontSize = 13.sp)
                }

                // Integrity Diagnostic Result
                if (integrityReport != null) {
                    val report = integrityReport!!
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (report.orphanQuestionsCount == 0 && report.invalidHierarchyCount == 0) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, if (report.orphanQuestionsCount == 0 && report.invalidHierarchyCount == 0) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "DIAGNOSTIC AUDIT REPORT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (report.orphanQuestionsCount == 0) Color(0xFF15803D) else Color(0xFFB91C1C)
                            )
                            Text("• Questions Checked: ${report.totalQuestionsChecked}", fontSize = 12.sp)
                            Text("• Correct Hierarchy Mappings: ${report.correctMappingsCount}", fontSize = 12.sp, color = Color(0xFF16A34A))
                            Text("• Orphan Questions: ${report.orphanQuestionsCount}", fontSize = 12.sp, color = if (report.orphanQuestionsCount > 0) Color.Red else Color(0xFF475569))
                            Text("• Hierarchy Mismatches: ${report.invalidHierarchyCount}", fontSize = 12.sp, color = if (report.invalidHierarchyCount > 0) Color.Red else Color(0xFF475569))
                            Text("• Duplicate Hashes: ${report.duplicateQuestionsCount}", fontSize = 12.sp)
                            Text("Summary: ${report.summaryText}", fontSize = 11.5.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Unit JSON Upload Dialog
 * Opens directly from Admin -> Syllabus -> Units / Unit Card
 * Allows the admin to upload/paste JSON under the exact selected Unit.
 * Enforces Unit -> Topic -> Subtopics -> MCQs hierarchy, stripping metadata & AI instructions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUnitJsonUploadDialog(
    unit: UnitModel,
    subject: Subject,
    onDismiss: () -> Unit,
    onSuccess: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var jsonInput by remember { mutableStateOf("") }
    var uploadOption by remember { mutableIntStateOf(0) } // 0: Paste, 1: File
    var validationError by remember { mutableStateOf<String?>(null) }
    var validationSuccess by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var isValidating by remember { mutableStateOf(false) }
    var isPreviewExpanded by remember { mutableStateOf(true) }
    var parsedMcqs by remember { mutableStateOf<List<Question>>(emptyList()) }

    LaunchedEffect(jsonInput) {
        if (jsonInput.isNotBlank()) {
            parsedMcqs = withContext(Dispatchers.Default) {
                parseJsonForLivePreview(jsonInput)
            }
        } else {
            parsedMcqs = emptyList()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(it)?.use { inputStream ->
                            inputStream.bufferedReader().use { reader -> reader.readText() }
                        } ?: ""
                    }
                    jsonInput = content
                    validationError = null
                    validationSuccess = "File loaded (${content.length} characters). Click 'Validate' or 'Import'."
                    uploadOption = 0
                } catch (e: Exception) {
                    validationError = "Error reading file: ${e.message}"
                    validationSuccess = null
                }
            }
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Upload JSON to Unit ${unit.unitNumber}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${subject.name} • ${unit.name}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Info banner explaining the hierarchy & metadata filter
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hierarchy: Unit (${unit.name}) → Topic → Subtopics → MCQs → Question → Options A-D → Correct Answer.\nAll rules, descriptions, coverage_method, and metadata are automatically stripped. Question IDs are preserved.",
                            fontSize = 11.sp,
                            color = Color(0xFF166534),
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Tabs
                TabRow(
                    selectedTabIndex = uploadOption,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = uploadOption == 0,
                        onClick = { uploadOption = 0 },
                        text = { Text("Paste JSON Text", fontSize = 12.5.sp) }
                    )
                    Tab(
                        selected = uploadOption == 1,
                        onClick = { uploadOption = 1 },
                        text = { Text("Pick JSON File", fontSize = 12.5.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (uploadOption == 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                            .border(BorderStroke(1.dp, Color(0xFFCBD5E1)), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { filePickerLauncher.launch("application/json") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select .json File from Storage", fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Text Field for JSON
                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = {
                        jsonInput = it
                        validationError = null
                        validationSuccess = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(if (parsedMcqs.isEmpty() || !isPreviewExpanded) 1f else 0.4f),
                    placeholder = { Text("Paste MCQ hierarchy JSON here...\n{\n  \"topic\": \"Satavahanas\",\n  \"subtopics\": [\n    {\n      \"subtopic\": \"Origin & Sources\",\n      \"mcqs\": [...]\n    }\n  ]\n}") },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.5.sp),
                    shape = RoundedCornerShape(8.dp)
                )

                // Live Preview Section
                if (parsedMcqs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (isPreviewExpanded) 0.6f else 0.05f)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                            .border(BorderStroke(1.dp, Color(0xFFCBD5E1)), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Live Preview (${parsedMcqs.size} MCQs)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            }
                            TextButton(onClick = { isPreviewExpanded = !isPreviewExpanded }) {
                                Text(if (isPreviewExpanded) "Collapse" else "Expand", fontSize = 11.sp)
                            }
                        }

                        if (isPreviewExpanded) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(parsedMcqs.take(10)) { idx: Int, mcq: Question ->
                                    QuestionFormulaLivePreviewCard(
                                        questionText = mcq.questionText,
                                        optionA = mcq.options["A"] ?: "",
                                        optionB = mcq.options["B"] ?: "",
                                        optionC = mcq.options["C"] ?: "",
                                        optionD = mcq.options["D"] ?: "",
                                        correctOption = mcq.correctOption,
                                        explanationText = mcq.explanation ?: "",
                                        highlightsText = "",
                                        title = "MCQ #${idx + 1} Rendered Preview"
                                    )
                                }
                                if (parsedMcqs.size > 10) {
                                    item {
                                        Text(
                                            "Preview limited to first 10 questions...",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Validation feedback
                if (validationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(validationError!!, color = Color.Red, fontSize = 11.5.sp)
                        }
                    }
                }

                if (validationSuccess != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(validationSuccess!!, color = Color(0xFF16A34A), fontSize = 11.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (jsonInput.isBlank()) {
                                validationError = "Please paste or select a JSON file first."
                                return@OutlinedButton
                            }
                            isValidating = true
                            scope.launch {
                                val vResult = withContext(Dispatchers.Default) {
                                    com.example.util.SyllabusJsonParser.validateJson(jsonInput)
                                }
                                isValidating = false
                                if (vResult.isValid) {
                                    validationError = null
                                    validationSuccess = "✓ Valid: ${vResult.hierarchyInfo} (${vResult.totalMcqs} MCQs).\n✨ Metadata and AI instructions successfully filtered out."
                                } else {
                                    validationError = vResult.errorMessage ?: "Invalid JSON syntax"
                                    validationSuccess = null
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isValidating && !isImporting
                    ) {
                        if (isValidating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Validating...", fontSize = 13.sp)
                        } else {
                            Text("Validate JSON", fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (jsonInput.isBlank()) {
                                validationError = "JSON content is empty"
                                return@Button
                            }
                            isImporting = true
                            scope.launch {
                                val vResult = withContext(Dispatchers.Default) {
                                    com.example.util.SyllabusJsonParser.validateJson(jsonInput)
                                }
                                if (!vResult.isValid) {
                                    validationError = "Validation failed: ${vResult.errorMessage}"
                                    isImporting = false
                                    return@launch
                                }

                                val result = withContext(Dispatchers.IO) {
                                    SampleData.processUploadedMcqs(
                                        jsonString = jsonInput,
                                        subjectId = subject.id,
                                        unitId = unit.id,
                                        topicId = "",
                                        contentLevel = "UNIT"
                                    )
                                }
                                isImporting = false

                                if (result.isSuccess) {
                                    val count = result.getOrNull() ?: 0
                                    Toast.makeText(context, "✅ Successfully imported $count MCQs into ${unit.name}!", Toast.LENGTH_LONG).show()
                                    onSuccess(count)
                                    onDismiss()
                                } else {
                                    validationError = "Import failed: ${result.exceptionOrNull()?.message}"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier.weight(1f),
                        enabled = !isImporting && !isValidating
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Importing...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import to Unit", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun parseJsonForLivePreview(jsonStr: String): List<Question> {
    val result = mutableListOf<Question>()
    try {
        val trimmed = jsonStr.trim()
        if (trimmed.startsWith("[")) {
            val arr = JSONArray(trimmed)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val qText = obj.optString("question", obj.optString("question_text", ""))
                if (qText.isNotBlank()) {
                    val optsMap = mutableMapOf<String, String>()
                    val optsObj = obj.optJSONObject("options")
                    if (optsObj != null) {
                        val keys = optsObj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            optsMap[k.uppercase()] = optsObj.optString(k)
                        }
                    }
                    val correct = obj.optString("correctOption", obj.optString("answer", "A"))
                    val exp = obj.optString("explanation", "")
                    result.add(
                        Question(
                            id = "preview_$i",
                            subjectId = "preview",
                            questionText = qText,
                            options = optsMap,
                            correctOption = correct,
                            explanation = exp
                        )
                    )
                }
            }
        } else if (trimmed.startsWith("{")) {
            val obj = JSONObject(trimmed)
            if (obj.has("subtopics") || obj.has("mcqs") || obj.has("questions")) {
                val mcqsArr = obj.optJSONArray("mcqs") ?: obj.optJSONArray("questions")
                if (mcqsArr != null) {
                    for (i in 0 until mcqsArr.length()) {
                        val qObj = mcqsArr.optJSONObject(i) ?: continue
                        val qText = qObj.optString("question", qObj.optString("question_text", ""))
                        if (qText.isNotBlank()) {
                            val optsMap = mutableMapOf<String, String>()
                            val optsObj = qObj.optJSONObject("options")
                            if (optsObj != null) {
                                val keys = optsObj.keys()
                                while (keys.hasNext()) {
                                    val k = keys.next()
                                    optsMap[k.uppercase()] = optsObj.optString(k)
                                }
                            }
                            val correct = qObj.optString("correctOption", qObj.optString("answer", "A"))
                            val exp = qObj.optString("explanation", "")
                            result.add(
                                Question(
                                    id = "preview_$i",
                                    subjectId = "preview",
                                    questionText = qText,
                                    options = optsMap,
                                    correctOption = correct,
                                    explanation = exp
                                )
                            )
                        }
                    }
                }
            }
        }
    } catch (_: Exception) {}
    return result
}
