package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.HighlightLocation
import com.example.model.HighlightPreset
import com.example.model.HighlightRange
import com.example.model.Question
import com.example.model.QuestionHighlightConfig
import com.example.ui.components.MathAndHighlightText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuestionHighlightTab() {
    val context = LocalContext.current
    var config by remember { mutableStateOf(SampleData.globalConfigState.value.questionHighlightConfig) }

    // Sample question & explanation for live admin preview
    var sampleQuestionText by remember {
        mutableStateOf("Which Article of the Indian Constitution provides for Fundamental Rights? Solve the quadratic roots x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a} and compound ratio \\frac{121}{100} = \\left(\\frac{11}{10}\\right)^2.")
    }
    var sampleQuestionHighlights by remember {
        mutableStateOf(
            listOf(
                HighlightRange(text = "Article", type = "exceptions"),
                HighlightRange(text = "Fundamental Rights", type = "key_concepts"),
                HighlightRange(text = "quadratic roots", type = "formulas"),
                HighlightRange(text = "121/100", type = "numbers_dates")
            )
        )
    }

    var sampleExplanationText by remember {
        mutableStateOf("Article 13 protects Fundamental Rights. The formula yields \\frac{121}{100} where r = 10% and compound formula A = P\\left(1 + \\frac{r}{100}\\right)^n. Therefore, the final compound yield matches 121.")
    }
    var sampleExplanationHighlights by remember {
        mutableStateOf(
            listOf(
                HighlightRange(text = "Article 13", type = "names_places"),
                HighlightRange(text = "Fundamental Rights", type = "key_concepts"),
                HighlightRange(text = "10%", type = "numbers_dates"),
                HighlightRange(text = "121", type = "answers")
            )
        )
    }

    var newHighlightPhrase by remember { mutableStateOf("") }
    var selectedHighlightType by remember { mutableStateOf("key_concepts") }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "✨ Question & Explanation Highlighting",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Globally configure important word highlighting and mathematical formula rendering across Subtopics, Sets, and Mock Tests.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Section 2: Master Switch & App Scope
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "QUESTION HIGHLIGHTING",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (config.isMasterEnabled) "HIGHLIGHTING IS GLOBALLY ACTIVE" else "DISABLED GLOBALLY",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (config.isMasterEnabled) Color(0xFF16A34A) else Color(0xFFDC2626)
                            )
                        }
                        Switch(
                            checked = config.isMasterEnabled,
                            onCheckedChange = { isChecked ->
                                config = config.copy(isMasterEnabled = isChecked)
                            }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "App Scope Targeting:",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("THIS_APP" to "This App", "SELECTED_APPS" to "Selected Apps", "ALL_APPS" to "All Apps").forEach { (key, label) ->
                            FilterChip(
                                selected = config.targetAppScope == key,
                                onClick = { config = config.copy(targetAppScope = key) },
                                label = { Text(label, fontSize = 11.5.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Location Controls
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LOCATION-SPECIFIC DISPLAY CONTROLS",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LocationCheckRow("Subtopics Questions", config.isSubtopicsQuestionsEnabled) {
                        config = config.copy(isSubtopicsQuestionsEnabled = it)
                    }
                    LocationCheckRow("Sets Questions", config.isSetsQuestionsEnabled) {
                        config = config.copy(isSetsQuestionsEnabled = it)
                    }
                    LocationCheckRow("Mock Test Questions", config.isMockTestQuestionsEnabled) {
                        config = config.copy(isMockTestQuestionsEnabled = it)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    LocationCheckRow("Subtopics Explanations", config.isSubtopicsExplanationsEnabled) {
                        config = config.copy(isSubtopicsExplanationsEnabled = it)
                    }
                    LocationCheckRow("Sets Explanations", config.isSetsExplanationsEnabled) {
                        config = config.copy(isSetsExplanationsEnabled = it)
                    }
                }
            }
        }

        // Section 4: Style System & Presets
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HIGHLIGHT STYLE PRESETS",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(HighlightPreset.ALL_PRESETS) { preset ->
                            val isSelected = config.presetName == preset.name
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(preset.bgColorHex),
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color(preset.borderColorHex)
                                ),
                                modifier = Modifier.clickable {
                                    config = config.copy(
                                        presetName = preset.name,
                                        bgColorHex = preset.bgColorHex,
                                        textColorHex = preset.textColorHex,
                                        borderColorHex = preset.borderColorHex
                                    )
                                }
                            ) {
                                Text(
                                    text = preset.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(preset.textColorHex),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Font Weight:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Normal", "Medium", "SemiBold", "Bold").forEach { weight ->
                            FilterChip(
                                selected = config.fontWeightName == weight,
                                onClick = { config = config.copy(fontWeightName = weight) },
                                label = { Text(weight, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Section 5: Question & Explanation Highlight Phrase Editor
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HIGHLIGHT IMPORTANT WORDS EDITOR",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = sampleQuestionText,
                        onValueChange = { sampleQuestionText = it },
                        label = { Text("Question Text") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Select Highlight Color Type Category:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val types = listOf(
                            "key_concepts" to "Amber (Concept)",
                            "numbers_dates" to "Blue (Number/Date)",
                            "formulas" to "Purple (Formula)",
                            "answers" to "Green (Result)",
                            "exceptions" to "Pink (Negative/Exception)",
                            "names_places" to "Teal (Name/Place)"
                        )
                        items(types) { (typeKey, label) ->
                            val isSelected = selectedHighlightType == typeKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedHighlightType = typeKey },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newHighlightPhrase,
                            onValueChange = { newHighlightPhrase = it },
                            placeholder = { Text("Word or Phrase") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (newHighlightPhrase.isNotBlank()) {
                                    sampleQuestionHighlights = sampleQuestionHighlights + HighlightRange(
                                        text = newHighlightPhrase.trim(),
                                        type = selectedHighlightType
                                    )
                                    newHighlightPhrase = ""
                                }
                            }
                        ) {
                            Text("Add Q")
                        }
                        Button(
                            onClick = {
                                if (newHighlightPhrase.isNotBlank()) {
                                    sampleExplanationHighlights = sampleExplanationHighlights + HighlightRange(
                                        text = newHighlightPhrase.trim(),
                                        type = selectedHighlightType
                                    )
                                    newHighlightPhrase = ""
                                }
                            }
                        ) {
                            Text("Add Exp")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Active Question Highlights:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        sampleQuestionHighlights.forEach { hl ->
                            InputChip(
                                selected = true,
                                onClick = {
                                    sampleQuestionHighlights = sampleQuestionHighlights.filter { it.id != hl.id }
                                },
                                label = { Text("${hl.text} (${hl.type.replace("_", " ")})", fontSize = 11.sp) },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = sampleExplanationText,
                        onValueChange = { sampleExplanationText = it },
                        label = { Text("Explanation Text") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Active Explanation Highlights:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        sampleExplanationHighlights.forEach { hl ->
                            InputChip(
                                selected = true,
                                onClick = {
                                    sampleExplanationHighlights = sampleExplanationHighlights.filter { it.id != hl.id }
                                },
                                label = { Text("${hl.text} (${hl.type.replace("_", " ")})", fontSize = 11.sp) },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                }
            }
        }

        // Section 6: Live Admin Preview
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE ADMIN PREVIEW",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PreviewSectionBox("A. SUBTOPIC QUESTION PREVIEW") {
                        MathAndHighlightText(
                            text = sampleQuestionText,
                            highlights = sampleQuestionHighlights,
                            location = HighlightLocation.SUBTOPICS_QUESTION,
                            highlightConfig = config,
                            color = Color.White
                        )
                    }

                    PreviewSectionBox("B. SET QUESTION PREVIEW") {
                        MathAndHighlightText(
                            text = sampleQuestionText,
                            highlights = sampleQuestionHighlights,
                            location = HighlightLocation.SETS_QUESTION,
                            highlightConfig = config,
                            color = Color.White
                        )
                    }

                    PreviewSectionBox("C. MOCK TEST QUESTION PREVIEW") {
                        MathAndHighlightText(
                            text = sampleQuestionText,
                            highlights = sampleQuestionHighlights,
                            location = HighlightLocation.MOCK_TEST_QUESTION,
                            highlightConfig = config,
                            color = Color.White
                        )
                    }

                    PreviewSectionBox("D. SUBTOPIC EXPLANATION PREVIEW") {
                        MathAndHighlightText(
                            text = sampleExplanationText,
                            highlights = sampleExplanationHighlights,
                            location = HighlightLocation.SUBTOPICS_EXPLANATION,
                            highlightConfig = config,
                            color = Color(0xFFE2E8F0)
                        )
                    }

                    PreviewSectionBox("E. SET EXPLANATION PREVIEW") {
                        MathAndHighlightText(
                            text = sampleExplanationText,
                            highlights = sampleExplanationHighlights,
                            location = HighlightLocation.SETS_EXPLANATION,
                            highlightConfig = config,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        // Section 7: Save Settings
        item {
            Button(
                onClick = {
                    val updatedGlobal = SampleData.globalConfigState.value.copy(
                        questionHighlightConfig = config
                    )
                    SampleData.updateGlobalConfig(updatedGlobal)
                    Toast.makeText(context, "✅ Highlighting & Math Settings Saved Successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE HIGHLIGHTING & MATH CONFIGURATION", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }
        }
    }
}

@Composable
private fun LocationCheckRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PreviewSectionBox(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF38BDF8)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(10.dp)) {
                content()
            }
        }
    }
}
