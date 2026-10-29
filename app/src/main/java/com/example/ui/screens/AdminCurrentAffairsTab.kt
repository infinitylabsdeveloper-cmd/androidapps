package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.MonthlyCurrentAffairsModel
import com.example.model.HighlightRange
import com.example.ui.components.MathAndHighlightText
import com.example.ui.components.AppFhdImage
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCurrentAffairsTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val years = listOf("2024", "2025", "2026", "2027")
    
    // Multi-app selection state
    val allApps = SampleData.appsListState
    var selectedAppIds by remember {
        mutableStateOf(allApps.filter { it.isActive }.map { it.id }.toSet())
    }
    
    var selectedMonth by remember { mutableStateOf("September") }
    var selectedYear by remember { mutableStateOf("2026") }
    
    var jsonInput by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var validationSuccess by remember { mutableStateOf<String?>(null) }
    var parsedPreviewQuestions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    
    var uploadOption by remember { mutableStateOf(0) } // 0: Paste, 1: File
    var showEditAppsDialogForCA by remember { mutableStateOf<MonthlyCurrentAffairsModel?>(null) }

    // Activation State
    var isActive by remember { mutableStateOf(true) }
    var startDate by remember { mutableStateOf(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())) }
    var startTime by remember { mutableStateOf("00:00") }
    var endDate by remember { mutableStateOf("2030-12-31") }
    var endTime by remember { mutableStateOf("23:59") }
    var cardImageUrl by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val localPath = com.example.util.ImageStorageManager.persistPickedImage(context, it, "ca_wallpaper")
                    cardImageUrl = localPath
                } catch (e: Exception) {
                    Toast.makeText(context, "Error saving image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val content = inputStream.bufferedReader().use { reader -> reader.readText() }
                    jsonInput = content
                    validationError = null
                    validationSuccess = "File loaded successfully. Click 'Validate & Preview' to inspect."
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
                Text(
                    text = "Current Affairs Management",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Single upload • Multi-app distribution • Auto-partitioned sets (10 Qs/set)",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
        
        // 1. SELECT TARGET APPS (Multi-App Selection)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "1. Select Target Apps for Publishing",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Choose which exam apps will receive this Current Affairs compilation",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { selectedAppIds = allApps.map { it.id }.toSet() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Select All", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = { selectedAppIds = emptySet() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Clear", fontSize = 12.sp, color = Color(0xFFEF4444))
                        }
                    }
                }
                
                // App selection checkboxes / cards
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allApps.forEach { app ->
                        val isSelected = selectedAppIds.contains(app.id)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF3B82F6) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAppIds = if (isSelected) {
                                        selectedAppIds - app.id
                                    } else {
                                        selectedAppIds + app.id
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedAppIds = if (checked) {
                                                selectedAppIds + app.id
                                            } else {
                                                selectedAppIds - app.id
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = app.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF1E293B)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = Color(app.primaryColorHex).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = app.code,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(app.primaryColorHex),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = app.examCategory,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                                
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                if (selectedAppIds.isEmpty()) {
                    Text(
                        text = "⚠️ Please select at least one app to publish.",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "✓ ${selectedAppIds.size} Apps selected for publishing",
                        color = Color(0xFF16A34A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        
        // 2. TARGET MONTH & YEAR
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("2. Publication Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Month Dropdown
                    var monthExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = monthExpanded,
                        onExpandedChange = { monthExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedMonth,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Month") },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = monthExpanded,
                            onDismissRequest = { monthExpanded = false }
                        ) {
                            months.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m) },
                                    onClick = {
                                        selectedMonth = m
                                        monthExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Year Dropdown
                    var yearExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = yearExpanded,
                        onExpandedChange = { yearExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedYear,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Year") },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            years.forEach { y ->
                                DropdownMenuItem(
                                    text = { Text(y) },
                                    onClick = {
                                        selectedYear = y
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Activation Controls
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable Compilation Immediately", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                            Text("Visible to students after publishing", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                        )
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time") },
                            placeholder = { Text("HH:mm") },
                            modifier = Modifier.weight(0.7f),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End Time") },
                            placeholder = { Text("HH:mm") },
                            modifier = Modifier.weight(0.7f),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Wallpaper Selection
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Card Background Wallpaper (Optional)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF0F172A)
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cardImageUrl,
                                onValueChange = { cardImageUrl = it },
                                label = { Text("Wallpaper URL or Path") },
                                placeholder = { Text("https://example.com/image.jpg") },
                                modifier = Modifier.weight(1f),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                                trailingIcon = {
                                    if (cardImageUrl.isNotEmpty()) {
                                        IconButton(onClick = { cardImageUrl = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            )
                            
                            Button(
                                onClick = {
                                    imagePickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                modifier = Modifier.height(56.dp).padding(top = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569))
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gallery", fontSize = 12.sp)
                            }
                        }
                        
                        if (cardImageUrl.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (cardImageUrl.startsWith("http")) "Remote URL linked" else "Local image selected",
                                    fontSize = 11.sp,
                                    color = Color(0xFF16A34A),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 3. MCQ CONTENT SOURCE & PREVIEW
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("3. MCQ Content Source & Validation", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                
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
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Paste JSON")
                            }
                        }
                    )
                    Tab(
                        selected = uploadOption == 1,
                        onClick = { uploadOption = 1 },
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload JSON File")
                            }
                        }
                    )
                }

                if (uploadOption == 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = { filePickerLauncher.launch("application/json") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569))
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Select JSON File")
                            }
                            Text("Imports JSON array directly from local storage", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "JSON Array of MCQs:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        TextButton(
                            onClick = {
                                jsonInput = """
{
  "title": "August 2025 Current Affairs - 25 MCQs",
  "language": "English",
  "questions": [
    {
      "question": "What was the repo rate retained by the RBI Monetary Policy Committee in August 2025?",
      "options": [
        "5.00%",
        "5.25%",
        "5.50%",
        "5.75%"
      ],
      "explanation": "The RBI Monetary Policy Committee kept the repo rate unchanged at 5.50% in August 2025."
    },
    {
      "question": "What was India's real GDP growth rate in Q1 of FY 2025-26, according to the August 2025 data?",
      "options": [
        "6.5%",
        "7.0%",
        "7.8%",
        "8.4%"
      ],
      "explanation": "India's real GDP grew by 7.8% in the first quarter of FY 2025-26."
    },
    {
      "question": "On which date was National Space Day celebrated in India in 2025?",
      "options": [
        "15 August",
        "23 August",
        "24 August",
        "29 August"
      ],
      "explanation": "National Space Day was celebrated on 23 August 2025. The 2025 theme was 'Aryabhatta to Gaganyaan: Ancient Wisdom to Infinite Possibilities'."
    }
  ]
}
                                """.trimIndent()
                                validationError = null
                                validationSuccess = null
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Load Sample Template", fontSize = 11.sp)
                        }
                    }
                    
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { 
                            jsonInput = it
                            validationError = null
                            validationSuccess = null
                            parsedPreviewQuestions = emptyList()
                        },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        placeholder = { Text("Paste JSON array or questions list here...") },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )
                }

                // Validation Status Area
                if (validationError != null) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(validationError!!, color = Color(0xFFDC2626), fontSize = 12.5.sp)
                        }
                    }
                }

                if (validationSuccess != null) {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(validationSuccess!!, color = Color(0xFF16A34A), fontSize = 12.5.sp)
                        }
                    }
                }
                
                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (jsonInput.isBlank()) {
                                validationError = "Please enter or paste JSON content to validate."
                                return@OutlinedButton
                            }
                            try {
                                val trimmed = jsonInput.trim()
                                var extractedTitle = ""
                                val arr = if (trimmed.startsWith("[")) {
                                    JSONArray(trimmed)
                                } else if (trimmed.startsWith("{")) {
                                    val obj = JSONObject(trimmed)
                                    extractedTitle = obj.optString("title", "")
                                    if (extractedTitle.isNotBlank()) {
                                        for (m in months) {
                                            if (extractedTitle.contains(m, ignoreCase = true)) {
                                                selectedMonth = m
                                                break
                                            }
                                        }
                                        val yMatch = Regex("""\b(202\d|203\d)\b""").find(extractedTitle)
                                        if (yMatch != null && years.contains(yMatch.value)) {
                                            selectedYear = yMatch.value
                                        }
                                    }
                                    when {
                                        obj.has("questions") -> obj.getJSONArray("questions")
                                        obj.has("mcqs") -> obj.getJSONArray("mcqs")
                                        obj.has("data") -> obj.getJSONArray("data")
                                        obj.has("items") -> obj.getJSONArray("items")
                                        else -> throw IllegalArgumentException("Root JSON object must contain 'questions' or 'mcqs' array.")
                                    }
                                } else {
                                    throw IllegalArgumentException("Invalid format: Must be a JSON array or object.")
                                }
                                
                                val total = arr.length()
                                val totalSets = (total + 9) / 10
                                val optionLetters = listOf("A", "B", "C", "D", "E", "F")
                                val previewList = mutableListOf<Map<String, Any>>()
                                
                                for (i in 0 until minOf(3, total)) {
                                    val q = arr.getJSONObject(i)
                                    val text = q.optString("questionText", q.optString("question", "Question ${i + 1}"))
                                    val exp = q.optString("explanation", q.optString("expl", ""))
                                    
                                    val optionsMap = mutableMapOf<String, String>()
                                    if (q.has("options")) {
                                        val optRaw = q.get("options")
                                        if (optRaw is JSONArray) {
                                            for (idx in 0 until optRaw.length()) {
                                                optionsMap[optionLetters.getOrElse(idx) { ('A' + idx).toString() }] = optRaw.getString(idx)
                                            }
                                        } else if (optRaw is JSONObject) {
                                            val keys = optRaw.keys()
                                            while (keys.hasNext()) {
                                                val k = keys.next().uppercase()
                                                optionsMap[k] = optRaw.optString(k, "")
                                            }
                                        }
                                    }
                                    
                                    var ans = q.optString("correctOption", q.optString("answer", "")).trim().uppercase()
                                    if (ans.isEmpty() && exp.isNotBlank() && optionsMap.isNotEmpty()) {
                                        val matches = optionsMap.filter { (_, optText) ->
                                            optText.isNotBlank() && exp.contains(optText, ignoreCase = true)
                                        }
                                        ans = matches.maxByOrNull { it.value.length }?.key ?: "A"
                                    } else if (ans.isEmpty()) {
                                        ans = "A"
                                    }
                                    
                                    previewList.add(mapOf("text" to text, "ans" to ans, "exp" to exp))
                                }
                                parsedPreviewQuestions = previewList
                                validationError = null
                                val detectedMonthYear = if (extractedTitle.isNotBlank()) " | Detected: $selectedMonth $selectedYear" else ""
                                validationSuccess = "✓ Valid format: $total MCQs ready$detectedMonthYear. Will be organized into $totalSets Sets (max 10 Qs/set)."
                            } catch (e: Exception) {
                                validationError = "Invalid JSON: ${e.message}"
                                validationSuccess = null
                                parsedPreviewQuestions = emptyList()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Validate & Preview")
                    }

                    Button(
                        onClick = {
                            if (selectedAppIds.isEmpty()) {
                                validationError = "Please select at least one target app."
                                return@Button
                            }
                            if (jsonInput.isBlank()) {
                                validationError = "JSON content is empty."
                                return@Button
                            }
                            
                            val result = SampleData.processCurrentAffairsMcqs(
                                jsonString = jsonInput,
                                month = selectedMonth,
                                year = selectedYear,
                                targetAppIds = selectedAppIds.toList(),
                                isActive = isActive,
                                startDate = startDate,
                                startTime = startTime,
                                endDate = endDate,
                                endTime = endTime,
                                cardImageUrl = cardImageUrl
                            )
                            
                            if (result.isSuccess) {
                                val count = result.getOrNull() ?: 0
                                val targetNames = allApps.filter { selectedAppIds.contains(it.id) }.joinToString(", ") { it.name }
                                val setsCount = (count + 9) / 10
                                
                                validationSuccess = "🎉 Successfully published $count Current Affairs MCQs ($setsCount Sets) to: $targetNames!"
                                validationError = null
                                parsedPreviewQuestions = emptyList()
                                Toast.makeText(context, "Published to ${selectedAppIds.size} apps!", Toast.LENGTH_SHORT).show()
                                jsonInput = "" // Clear after success
                            } else {
                                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                                validationError = "❌ Publishing failed: $error"
                                validationSuccess = null
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Publish to (${selectedAppIds.size}) Apps")
                    }
                }
                
                // Parsed Questions Preview Snippets
                if (parsedPreviewQuestions.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Sample Question Preview (${parsedPreviewQuestions.size} shown):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        parsedPreviewQuestions.forEachIndexed { idx, q ->
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    MathAndHighlightText(
                                        text = "${idx + 1}. ${q["text"]}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Correct: Option ${q["ans"]}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A)
                                        )
                                        if ((q["exp"] as? String)?.isNotBlank() == true) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            MathAndHighlightText(
                                                text = "• Exp: ${q["exp"]}",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF64748B)
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
        
        // 4. EXISTING PUBLISHED CURRENT AFFAIRS LIST
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Published Monthly Compilations",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(top = 8.dp)
            )
            Surface(
                color = Color(0xFFE0E7FF),
                shape = CircleShape
            ) {
                Text(
                    text = "${SampleData.monthlyCurrentAffairsState.size} Months Active",
                    color = Color(0xFF3730A3),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        
        val monthlyCA = SampleData.monthlyCurrentAffairsState
        if (monthlyCA.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No Current Affairs data available yet.", color = Color.Gray)
            }
        } else {
            monthlyCA.forEach { ca ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Wallpaper Thumbnail
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.size(60.dp)
                            ) {
                                if (ca.cardImageUrl.isNotBlank()) {
                                    coil.compose.AsyncImage(
                                        model = if (ca.cardImageUrl.startsWith("http")) ca.cardImageUrl else java.io.File(ca.cardImageUrl),
                                        contentDescription = "Thumbnail",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(24.dp))
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ca.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A)
                                )
                                val status = ca.getStatus()
                                val statusColor = when (status) {
                                    com.example.model.PromotionStatus.ACTIVE -> Color(0xFF16A34A)
                                    com.example.model.PromotionStatus.SCHEDULED -> Color(0xFF2563EB)
                                    com.example.model.PromotionStatus.EXPIRED -> Color(0xFFDC2626)
                                    com.example.model.PromotionStatus.INACTIVE -> Color(0xFF94A3B8)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        color = statusColor.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = status.name,
                                            color = statusColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "${ca.sets.size} Sets • ${ca.sets.sumOf { it.questions.size }} Qs",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { showEditAppsDialogForCA = ca }) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "Edit Metadata & Apps",
                                        tint = Color(0xFF2563EB)
                                    )
                                }
                                IconButton(onClick = { SampleData.deleteMonthlyCurrentAffairs(ca.id) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete from all apps",
                                        tint = Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                        
                        // Associated Apps Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Published To:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                            if (ca.mappedAppIds.isEmpty()) {
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "All Apps (Global)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                ca.mappedAppIds.forEach { targetId ->
                                    val app = allApps.find { it.id == targetId }
                                    val label = app?.name ?: targetId
                                    Surface(
                                        color = Color(app?.primaryColorHex ?: 0xFF2563EB).copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(app?.primaryColorHex ?: 0xFF2563EB)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Unlink from $label",
                                                tint = Color(app?.primaryColorHex ?: 0xFF2563EB),
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clickable {
                                                        SampleData.unlinkMonthlyCurrentAffairFromApp(ca.id, targetId)
                                                        Toast.makeText(context, "Unlinked from $label", Toast.LENGTH_SHORT).show()
                                                    }
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

    // Edit Current Affairs Metadata Dialog
    if (showEditAppsDialogForCA != null) {
        val ca = showEditAppsDialogForCA!!
        var tempTitle by remember(ca.id) { mutableStateOf(ca.title) }
        var tempWallpaper by remember(ca.id) { mutableStateOf(ca.cardImageUrl) }
        var tempIsActive by remember(ca.id) { mutableStateOf(ca.isActive) }
        var tempMapped by remember(ca.id) { mutableStateOf(ca.mappedAppIds.toSet()) }
        var tempStartD by remember(ca.id) { mutableStateOf(ca.startDate) }
        var tempStartT by remember(ca.id) { mutableStateOf(ca.startTime) }
        var tempEndD by remember(ca.id) { mutableStateOf(ca.endDate) }
        var tempEndT by remember(ca.id) { mutableStateOf(ca.endTime) }

        val diagImagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            uri?.let {
                scope.launch {
                    try {
                        val localPath = com.example.util.ImageStorageManager.persistPickedImage(context, it, "ca_wallpaper_edit")
                        tempWallpaper = localPath
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error saving image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showEditAppsDialogForCA = null },
            title = {
                Text(
                    text = "Edit Compilation Metadata",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = tempTitle,
                        onValueChange = { tempTitle = it },
                        label = { Text("Compilation Title") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.5.sp)
                    )

                    // Wallpaper Edit
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Card Wallpaper", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = tempWallpaper,
                                onValueChange = { tempWallpaper = it },
                                label = { Text("URL or Local Path") },
                                modifier = Modifier.weight(1f),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                            )
                            IconButton(
                                onClick = {
                                    diagImagePicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Pick Image", tint = Color(0xFF2563EB))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Status: ${if (tempIsActive) "Active" else "Inactive"}", fontSize = 13.5.sp)
                        Switch(checked = tempIsActive, onCheckedChange = { tempIsActive = it })
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tempStartD,
                            onValueChange = { tempStartD = it },
                            label = { Text("Start Date") },
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                        )
                        OutlinedTextField(
                            value = tempStartT,
                            onValueChange = { tempStartT = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(0.7f),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Text("App Visibility", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    allApps.forEach { app ->
                        val isChecked = tempMapped.contains(app.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tempMapped = if (isChecked) tempMapped - app.id else tempMapped + app.id
                                }
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    tempMapped = if (checked) tempMapped + app.id else tempMapped - app.id
                                }
                            )
                            Text(text = app.name, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        SampleData.updateMonthlyCurrentAffairs(
                            id = ca.id,
                            title = tempTitle,
                            isActive = tempIsActive,
                            cardImageUrl = tempWallpaper,
                            mappedAppIds = tempMapped.toList(),
                            startDate = tempStartD,
                            startTime = tempStartT,
                            endDate = tempEndD,
                            endTime = tempEndT
                        )
                        showEditAppsDialogForCA = null
                        Toast.makeText(context, "Metadata updated!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditAppsDialogForCA = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
