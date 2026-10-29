package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.MathAndHighlightText
import com.example.ui.components.QuestionFormulaLivePreviewCard
import com.example.model.HighlightRange
import androidx.compose.ui.window.Dialog
import com.example.data.SampleData
import com.example.model.PracticeSetModel
import com.example.model.Question
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.ui.theme.PremiumCardColorSystem
import com.example.model.UnitModel
import com.example.ui.components.AppFhdImage
import com.example.ui.components.ImageUploadGuidelinesDialog
import com.example.ui.components.RecommendedImageSizeHelper
import com.example.ui.components.UploadImageType
import com.example.util.ImageStorageManager
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Admin Syllabus Hub
 * Implements strict hierarchical management:
 * 1. Subjects (with Prelims/Mains/Both category, name, and Image URL)
 * 2. Units (strictly created under an existing Subject, with unit number, name, and Image URL)
 * 3. Topics (strictly created under an existing Unit of a Subject, with name, Image URL, and MCQs count)
 */
@Composable
fun AdminSyllabusManagementView() {
    val context = LocalContext.current

    // App Tenant Context for Admin Management
    var selectedAdminAppId by remember { mutableStateOf(SampleData.currentActiveAppId.value) }
    var showAppDropdown by remember { mutableStateOf(false) }
    val currentSelectedApp = SampleData.appsListState.find { it.id == selectedAdminAppId } ?: SampleData.getActiveApp()

    // Hierarchy Navigation State
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var selectedUnitId by remember { mutableStateOf<String?>(null) }
    var selectedTopicId by remember { mutableStateOf<String?>(null) }

    // Dynamic Reactive lookup from SampleData (strictly isolated to selectedAdminAppId)
    val selectedSubject = SampleData.subjectsState.find { it.id == selectedSubjectId && it.appId == selectedAdminAppId }
    val selectedUnit = selectedSubject?.units?.find { it.id == selectedUnitId }
    val selectedTopic = selectedUnit?.topics?.find { it.id == selectedTopicId }

    // If a selected subject/unit/topic was deleted, reset selection
    LaunchedEffect(SampleData.subjectsState.size, selectedSubject?.units?.size, selectedUnit?.topics?.size) {
        if (selectedSubjectId != null && selectedSubject == null) {
            selectedSubjectId = null
            selectedUnitId = null
            selectedTopicId = null
        } else if (selectedUnitId != null && selectedUnit == null) {
            selectedUnitId = null
            selectedTopicId = null
        } else if (selectedTopicId != null && selectedTopic == null) {
            selectedTopicId = null
        }
    }

    // Dialog States
    var showSubjectDialog by remember { mutableStateOf(false) }
    var subjectToEdit by remember { mutableStateOf<Subject?>(null) }

    var showUnitDialog by remember { mutableStateOf(false) }
    var unitToEdit by remember { mutableStateOf<UnitModel?>(null) }

    var showTopicDialog by remember { mutableStateOf(false) }
    var topicToEdit by remember { mutableStateOf<TopicModel?>(null) }

    var questionToEdit by remember { mutableStateOf<Question?>(null) }
    var questionToDelete by remember { mutableStateOf<Question?>(null) }

    // Direct JSON Upload to selected Unit
    var uploadJsonUnit by remember { mutableStateOf<UnitModel?>(null) }

    // Quick Image URL Update Dialog State
    var quickImageTarget by remember { mutableStateOf<ImageUpdateTarget?>(null) }

    // Delete Confirmation Dialog State
    var deleteConfirmation by remember { mutableStateOf<DeleteTarget?>(null) }

    // Search and Category Filter State (for Subjects)
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") } // "ALL", "PRELIMS", "MAINS", "BOTH"
    var showGlobalGuidelines by remember { mutableStateOf(false) }

    if (showGlobalGuidelines) {
        ImageUploadGuidelinesDialog(
            currentSpec = UploadImageType.SUBJECT_IMAGE,
            onDismiss = { showGlobalGuidelines = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Individual Question Editor Dialog
        questionToEdit?.let { q ->
            QuestionEditDialog(
                question = q,
                onDismiss = { questionToEdit = null },
                onSave = { updated ->
                    SampleData.updateQuestion(updated)
                    Toast.makeText(context, "Question updated successfully", Toast.LENGTH_SHORT).show()
                    questionToEdit = null
                }
            )
        }

        // Individual Question Delete Confirmation
        questionToDelete?.let { q ->
            QuestionDeleteConfirmDialog(
                question = q,
                onDismiss = { questionToDelete = null },
                onConfirm = {
                    SampleData.deleteQuestion(q.id)
                    Toast.makeText(context, "Question deleted", Toast.LENGTH_SHORT).show()
                    questionToDelete = null
                }
            )
        }
        // =========================================================================
        // TOP BREADCRUMB / HIERARCHY NAVIGATION BAR
        // =========================================================================
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // 1. Independent App Selector & Current Config Indicator
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "SELECT APP TO CONFIGURE SYLLABUS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = currentSelectedApp.name,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Box {
                            OutlinedButton(
                                onClick = { showAppDropdown = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Switch App", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }

                            DropdownMenu(
                                expanded = showAppDropdown,
                                onDismissRequest = { showAppDropdown = false },
                                modifier = Modifier.widthIn(min = 250.dp)
                            ) {
                                Text(
                                    text = "Available Apps",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                                HorizontalDivider(color = Color(0xFFE2E8F0))
                                SampleData.appsListState.forEach { app ->
                                    val isSelected = app.id == selectedAdminAppId
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = app.name,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B)
                                                    )
                                                    Text(
                                                        text = "ID: ${app.id}",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF94A3B8)
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color(0xFF2563EB),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedAdminAppId = app.id
                                            selectedSubjectId = null
                                            selectedUnitId = null
                                            selectedTopicId = null
                                            showAppDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Individual Dashboard Card Visibility Controls (All, Prelims, Mains)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Dashboard Card Visibility",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Text(
                                text = "App: ${currentSelectedApp.shortName}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 3 Toggle Controls: All, Prelims, Mains
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // All Card Toggle
                            AdminCardVisibilityToggleItem(
                                title = "All",
                                icon = Icons.Default.Apps,
                                isEnabled = currentSelectedApp.isAllCardEnabled,
                                onToggle = { isChecked ->
                                    SampleData.updateAppCardVisibility(
                                        appId = selectedAdminAppId,
                                        isPrelims = currentSelectedApp.isPrelimsCardEnabled,
                                        isMains = currentSelectedApp.isMainsCardEnabled,
                                        isAll = isChecked
                                    )
                                    Toast.makeText(context, "${currentSelectedApp.shortName}: 'All' card ${if (isChecked) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Prelims Card Toggle
                            AdminCardVisibilityToggleItem(
                                title = "Prelims",
                                icon = Icons.Default.School,
                                isEnabled = currentSelectedApp.isPrelimsCardEnabled,
                                onToggle = { isChecked ->
                                    SampleData.updateAppCardVisibility(
                                        appId = selectedAdminAppId,
                                        isPrelims = isChecked,
                                        isMains = currentSelectedApp.isMainsCardEnabled,
                                        isAll = currentSelectedApp.isAllCardEnabled
                                    )
                                    Toast.makeText(context, "${currentSelectedApp.shortName}: 'Prelims' card ${if (isChecked) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Mains Card Toggle
                            AdminCardVisibilityToggleItem(
                                title = "Mains",
                                icon = Icons.Default.WorkspacePremium,
                                isEnabled = currentSelectedApp.isMainsCardEnabled,
                                onToggle = { isChecked ->
                                    SampleData.updateAppCardVisibility(
                                        appId = selectedAdminAppId,
                                        isPrelims = currentSelectedApp.isPrelimsCardEnabled,
                                        isMains = isChecked,
                                        isAll = currentSelectedApp.isAllCardEnabled
                                    )
                                    Toast.makeText(context, "${currentSelectedApp.shortName}: 'Mains' card ${if (isChecked) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Top Level Title & Add Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedSubjectId != null) {
                            IconButton(
                                onClick = {
                                    if (selectedTopicId != null) {
                                        selectedTopicId = null // Back to Topics level
                                    } else if (selectedUnitId != null) {
                                        selectedUnitId = null // Back to Units level
                                    } else {
                                        selectedSubjectId = null // Back to Subjects level
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFF1E293B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            val stageTitle = when {
                                selectedTopic != null -> "Topic Sets & Subtopics"
                                selectedUnit != null -> "Topic Management (Level 3)"
                                selectedSubject != null -> "Unit Management (Level 2)"
                                else -> "Subject Management (Level 1)"
                            }
                            Text(
                                text = stageTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = when {
                                    selectedTopic != null -> selectedTopic.name
                                    selectedUnit != null -> "Topics Management"
                                    selectedSubject != null -> "Units Management"
                                    else -> "Syllabus Structure & Coverage"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Action Buttons (Guidelines + Dynamically adapts to hierarchy)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { showGlobalGuidelines = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF2563EB))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Guidelines", 
                                fontSize = 11.sp, 
                                color = Color(0xFF2563EB),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (selectedTopic == null) {
                            Button(
                                onClick = {
                                    when {
                                        selectedUnit != null && selectedSubject != null -> {
                                            // Add Topic strictly under this Unit
                                            topicToEdit = null
                                            showTopicDialog = true
                                        }
                                        selectedSubject != null -> {
                                            // Add Unit strictly under this Subject
                                            unitToEdit = null
                                            showUnitDialog = true
                                        }
                                        else -> {
                                            // Add Subject
                                            subjectToEdit = null
                                            showSubjectDialog = true
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when {
                                        selectedUnit != null -> Color(0xFF059669) // Emerald for topics
                                        selectedSubject != null -> Color(0xFFD97706) // Amber for units
                                        else -> Color(0xFF2563EB) // Royal Blue for subjects
                                    }
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = when {
                                        selectedUnit != null -> "New Topic"
                                        selectedSubject != null -> "New Unit"
                                        else -> "New Subject"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Interactive Breadcrumb Trail
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Breadcrumb 1: All Subjects
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (selectedSubjectId == null) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (selectedSubjectId == null) Color(0xFFBFDBFE) else Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable {
                            selectedSubjectId = null
                            selectedUnitId = null
                            selectedTopicId = null
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = if (selectedSubjectId == null) Color(0xFF2563EB) else Color(0xFF64748B),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "All Subjects",
                                fontSize = 11.sp,
                                fontWeight = if (selectedSubjectId == null) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedSubjectId == null) Color(0xFF2563EB) else Color(0xFF64748B)
                            )
                        }
                    }

                    if (selectedSubject != null) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(12.dp)
                        )

                        // Breadcrumb 2: Selected Subject
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedUnitId == null) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (selectedUnitId == null) Color(0xFFFDE68A) else Color(0xFFE2E8F0)),
                            modifier = Modifier.clickable {
                                selectedUnitId = null
                                selectedTopicId = null
                            }
                        ) {
                            Text(
                                text = selectedSubject.name,
                                fontSize = 11.sp,
                                fontWeight = if (selectedUnitId == null) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedUnitId == null) Color(0xFFB45309) else Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (selectedUnit != null) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(12.dp)
                        )

                        // Breadcrumb 3: Selected Unit
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedTopicId == null) Color(0xFFD1FAE5) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (selectedTopicId == null) Color(0xFFA7F3D0) else Color(0xFFE2E8F0)),
                            modifier = Modifier.clickable {
                                selectedTopicId = null
                            }
                        ) {
                            Text(
                                text = "Unit ${selectedUnit.unitNumber} • ${selectedUnit.name}",
                                fontSize = 11.sp,
                                fontWeight = if (selectedTopicId == null) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTopicId == null) Color(0xFF047857) else Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (selectedTopic != null) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(12.dp)
                        )

                        // Breadcrumb 4: Selected Topic
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEDE9FE),
                            border = BorderStroke(1.dp, Color(0xFFDDD6FE))
                        ) {
                            Text(
                                text = selectedTopic.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // BODY CONTENT BASED ON DRILL-DOWN LEVEL
        // =========================================================================
        when {
            // ---------------------------------------------------------------------
            // LEVEL 4: TOPIC DETAILS (SETS | SUBTOPICS SIDE-BY-SIDE)
            // ---------------------------------------------------------------------
            selectedSubject != null && selectedUnit != null && selectedTopic != null -> {
                AdminTopicDetailsView(
                    subject = selectedSubject,
                    unit = selectedUnit,
                    topic = selectedTopic,
                    onBack = { selectedTopicId = null },
                    onEditQuestion = { questionToEdit = it },
                    onDeleteQuestion = { questionToDelete = it }
                )
            }

            // ---------------------------------------------------------------------
            // LEVEL 3: TOPICS OF THE SELECTED UNIT
            // ---------------------------------------------------------------------
            selectedSubject != null && selectedUnit != null -> {
                AdminTopicsLevelView(
                    subject = selectedSubject,
                    unit = selectedUnit,
                    onSelectTopic = { topic ->
                        selectedTopicId = topic.id
                    },
                    onAddTopic = {
                        topicToEdit = null
                        showTopicDialog = true
                    },
                    onEditTopic = { topic ->
                        topicToEdit = topic
                        showTopicDialog = true
                    },
                    onDeleteTopic = { topic ->
                        deleteConfirmation = DeleteTarget.Topic(selectedSubject.id, selectedUnit.id, topic)
                    },
                    onQuickUpdateImage = { topic ->
                        quickImageTarget = ImageUpdateTarget.Topic(
                            subjectId = selectedSubject.id,
                            unitId = selectedUnit.id,
                            topicId = topic.id,
                            title = topic.name,
                            currentUrl = topic.imageUrl ?: ""
                        )
                    },
                    onUploadJson = {
                        uploadJsonUnit = selectedUnit
                    }
                )
            }

            // ---------------------------------------------------------------------
            // LEVEL 2: UNITS OF THE SELECTED SUBJECT
            // ---------------------------------------------------------------------
            selectedSubject != null -> {
                AdminUnitsLevelView(
                    subject = selectedSubject,
                    onSelectUnit = { unit ->
                        selectedUnitId = unit.id
                    },
                    onAddUnit = {
                        unitToEdit = null
                        showUnitDialog = true
                    },
                    onEditUnit = { unit ->
                        unitToEdit = unit
                        showUnitDialog = true
                    },
                    onDeleteUnit = { unit ->
                        deleteConfirmation = DeleteTarget.Unit(selectedSubject.id, unit)
                    },
                    onQuickUpdateImage = { unit ->
                        quickImageTarget = ImageUpdateTarget.Unit(
                            subjectId = selectedSubject.id,
                            unitId = unit.id,
                            title = unit.name,
                            currentUrl = unit.imageUrl ?: ""
                        )
                    },
                    onEditSubject = {
                        subjectToEdit = selectedSubject
                        showSubjectDialog = true
                    },
                    onUploadJson = { unit ->
                        uploadJsonUnit = unit
                    }
                )
            }

            // ---------------------------------------------------------------------
            // LEVEL 1: ALL SUBJECTS
            // ---------------------------------------------------------------------
            else -> {
                AdminSubjectsLevelView(
                    selectedAppId = selectedAdminAppId,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    categoryFilter = selectedCategoryFilter,
                    onCategoryFilterChange = { selectedCategoryFilter = it },
                    onSelectSubject = { subject ->
                        selectedSubjectId = subject.id
                    },
                    onAddSubject = {
                        subjectToEdit = null
                        showSubjectDialog = true
                    },
                    onEditSubject = { subject ->
                        subjectToEdit = subject
                        showSubjectDialog = true
                    },
                    onDeleteSubject = { subject ->
                        deleteConfirmation = DeleteTarget.Subject(subject)
                    },
                    onQuickUpdateImage = { subject ->
                        quickImageTarget = ImageUpdateTarget.Subject(
                            subjectId = subject.id,
                            title = subject.name,
                            currentUrl = subject.imageUrl
                        )
                    }
                )
            }
        }
    }

    // =============================================================================
    // MODAL DIALOGS
    // =============================================================================

    // Dedicated Unit JSON MCQ Upload Dialog
    if (uploadJsonUnit != null && selectedSubject != null) {
        AdminUnitJsonUploadDialog(
            unit = uploadJsonUnit!!,
            subject = selectedSubject,
            onDismiss = { uploadJsonUnit = null },
            onSuccess = { _ ->
                uploadJsonUnit = null
            }
        )
    }

    // 1. Subject Dialog (Add / Edit) with Prelims / Mains / General multi-category + Image URL
    if (showSubjectDialog) {
        SubjectEditDialog(
            initialSubject = subjectToEdit,
            onDismiss = { showSubjectDialog = false },
            onSave = { name, category, categories, imageUrl, heroQuote ->
                if (subjectToEdit != null) {
                    val updated = subjectToEdit!!.copy(
                        name = name,
                        category = category,
                        categories = categories,
                        imageUrl = imageUrl,
                        heroQuote = heroQuote
                    )
                    SampleData.updateSubject(updated)
                    Toast.makeText(context, "Subject '${name}' updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    val newSubject = Subject(
                        id = "subj-${UUID.randomUUID().toString().take(8)}",
                        appId = selectedAdminAppId,
                        name = name,
                        category = category,
                        categories = categories,
                        imageUrl = imageUrl,
                        heroQuote = heroQuote.ifBlank { "Comprehensive study sets for ${categories.joinToString(" & ") { it.uppercase() }}" },
                        units = emptyList()
                    )
                    SampleData.addSubject(newSubject)
                    Toast.makeText(context, "Subject '${name}' created for ${currentSelectedApp.shortName}.", Toast.LENGTH_LONG).show()
                }
                showSubjectDialog = false
            }
        )
    }

    // 2. Unit Dialog (Add / Edit strictly under selected Subject)
    if (showUnitDialog && selectedSubject != null) {
        UnitEditDialog(
            subjectName = selectedSubject.name,
            initialUnit = unitToEdit,
            suggestedUnitNumber = (selectedSubject.units.maxOfOrNull { it.unitNumber } ?: 0) + 1,
            onDismiss = { showUnitDialog = false },
            onSave = { unitNumber, name, imageUrl, subtitle ->
                if (unitToEdit != null) {
                    val updated = unitToEdit!!.copy(
                        unitNumber = unitNumber,
                        name = name,
                        imageUrl = imageUrl.ifBlank { null },
                        subtitle = subtitle.ifBlank { null }
                    )
                    SampleData.updateUnitInSubject(selectedSubject.id, updated)
                    Toast.makeText(context, "Unit '${name}' updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    val newUnit = UnitModel(
                        id = "unit-${UUID.randomUUID().toString().take(8)}",
                        appId = selectedSubject.appId,
                        name = name,
                        unitNumber = unitNumber,
                        imageUrl = imageUrl.ifBlank { null },
                        subtitle = subtitle.ifBlank { null },
                        topics = emptyList()
                    )
                    SampleData.addUnitToSubject(selectedSubject.id, newUnit)
                    Toast.makeText(context, "Unit ${unitNumber} created. You can now add topics to it!", Toast.LENGTH_LONG).show()
                }
                showUnitDialog = false
            }
        )
    }

    // 3. Topic Dialog (Add / Edit strictly under selected Unit under Subject)
    if (showTopicDialog && selectedSubject != null && selectedUnit != null) {
        TopicEditDialog(
            subjectName = selectedSubject.name,
            unitName = selectedUnit.name,
            unitNumber = selectedUnit.unitNumber,
            initialTopic = topicToEdit,
            onDismiss = { showTopicDialog = false },
            onSave = { name, imageUrl, estimatedMcqs, difficulty, subtitle ->
                if (topicToEdit != null) {
                    val updated = topicToEdit!!.copy(
                        name = name,
                        imageUrl = imageUrl.ifBlank { null },
                        estimatedMcqs = estimatedMcqs.ifBlank { null },
                        difficulty = difficulty,
                        subtitle = subtitle.ifBlank { null }
                    )
                    SampleData.updateTopicInUnit(selectedSubject.id, selectedUnit.id, updated)
                    Toast.makeText(context, "Topic '${name}' updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    val newTopic = TopicModel(
                        id = "topic-${UUID.randomUUID().toString().take(8)}",
                        appId = selectedSubject.appId,
                        name = name,
                        imageUrl = imageUrl.ifBlank { null },
                        estimatedMcqs = estimatedMcqs.ifBlank { "50 MCQs" },
                        difficulty = difficulty,
                        subtitle = subtitle.ifBlank { null },
                        practiceSetsCount = 3
                    )
                    SampleData.addTopicToUnit(selectedSubject.id, selectedUnit.id, newTopic)
                    Toast.makeText(context, "Topic '${name}' added to Unit ${selectedUnit.unitNumber}!", Toast.LENGTH_LONG).show()
                }
                showTopicDialog = false
            }
        )
    }

    // 4. Quick Image URL Update Dialog
    quickImageTarget?.let { target ->
        val entityType = when (target) {
            is ImageUpdateTarget.Subject -> "subject"
            is ImageUpdateTarget.Unit -> "unit"
            is ImageUpdateTarget.Topic -> "topic"
        }
        QuickImageUpdateDialog(
            targetTitle = target.title,
            currentUrl = target.currentUrl,
            entityType = entityType,
            onDismiss = { quickImageTarget = null },
            onSave = { newUrl ->
                when (target) {
                    is ImageUpdateTarget.Subject -> {
                        SampleData.updateSubjectImage(target.subjectId, newUrl)
                        Toast.makeText(context, "Subject image URL updated", Toast.LENGTH_SHORT).show()
                    }
                    is ImageUpdateTarget.Unit -> {
                        SampleData.updateUnitImage(target.subjectId, target.unitId, newUrl.ifBlank { null })
                        Toast.makeText(context, "Unit image URL updated", Toast.LENGTH_SHORT).show()
                    }
                    is ImageUpdateTarget.Topic -> {
                        SampleData.updateTopicImage(target.subjectId, target.unitId, target.topicId, newUrl.ifBlank { null })
                        Toast.makeText(context, "Topic image URL updated", Toast.LENGTH_SHORT).show()
                    }
                }
                quickImageTarget = null
            }
        )
    }

    // 5. Delete Confirmation Dialog
    deleteConfirmation?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteConfirmation = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = when (target) {
                        is DeleteTarget.Subject -> "Delete Subject '${target.subject.name}'?"
                        is DeleteTarget.Unit -> "Delete Unit '${target.unit.name}'?"
                        is DeleteTarget.Topic -> "Delete Topic '${target.topic.name}'?"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = when (target) {
                        is DeleteTarget.Subject -> "Are you sure? Deleting this subject will permanently remove all ${target.subject.units.size} unit(s) and their associated topics."
                        is DeleteTarget.Unit -> "Are you sure? Deleting this unit will permanently remove all ${target.unit.topics.size} topic(s) inside it."
                        is DeleteTarget.Topic -> "Are you sure you want to delete this topic from the syllabus?"
                    },
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (target) {
                            is DeleteTarget.Subject -> {
                                SampleData.deleteSubject(target.subject.id)
                                Toast.makeText(context, "Subject deleted", Toast.LENGTH_SHORT).show()
                            }
                            is DeleteTarget.Unit -> {
                                SampleData.deleteUnitFromSubject(target.subjectId, target.unit.id)
                                Toast.makeText(context, "Unit deleted", Toast.LENGTH_SHORT).show()
                            }
                            is DeleteTarget.Topic -> {
                                SampleData.deleteTopicFromUnit(target.subjectId, target.unitId, target.topic.id)
                                Toast.makeText(context, "Topic deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                        deleteConfirmation = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Yes, Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { deleteConfirmation = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// =============================================================================
// DASHBOARD CARD VISIBILITY TOGGLE ITEM
// =============================================================================
@Composable
private fun AdminCardVisibilityToggleItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isEnabled) Color.White else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (isEnabled) Color(0xFF10B981) else Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEnabled) Color(0xFF0F172A) else Color(0xFF94A3B8),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) Color(0xFF0F172A) else Color(0xFF64748B)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                modifier = Modifier
                    .scale(0.75f)
                    .height(26.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF10B981),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFCBD5E1)
                )
            )
            Text(
                text = if (isEnabled) "ENABLED" else "DISABLED",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = if (isEnabled) Color(0xFF059669) else Color(0xFF94A3B8)
            )
        }
    }
}

// =============================================================================
// LEVEL 1: ALL SUBJECTS VIEW
// =============================================================================
@Composable
private fun AdminSubjectsLevelView(
    selectedAppId: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    categoryFilter: String,
    onCategoryFilterChange: (String) -> Unit,
    onSelectSubject: (Subject) -> Unit,
    onAddSubject: () -> Unit,
    onEditSubject: (Subject) -> Unit,
    onDeleteSubject: (Subject) -> Unit,
    onQuickUpdateImage: (Subject) -> Unit
) {
    val subjects = SampleData.subjectsState.filter { it.appId == selectedAppId }

    // Filter by search query and category (Prelims, Mains, General, All)
    val filteredSubjects = remember(subjects.toList(), searchQuery, categoryFilter) {
        subjects.filter { subj ->
            val matchesCategory = subj.matchesCategoryFilter(categoryFilter)
            val matchesSearch = searchQuery.isBlank() ||
                    subj.name.contains(searchQuery, ignoreCase = true) ||
                    subj.getAssignedCategories().any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Search & Filter Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search subjects by name...", fontSize = 13.5.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF64748B))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                // Category Filter Chips: All, Prelims, Mains, General
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "All (${subjects.size})",
                        "PRELIMS" to "Prelims (${subjects.count { it.matchesCategoryFilter("PRELIMS") }})",
                        "MAINS" to "Mains (${subjects.count { it.matchesCategoryFilter("MAINS") }})",
                        "GENERAL" to "General (${subjects.count { it.matchesCategoryFilter("GENERAL") }})"
                    )
                    filters.forEach { (key, label) ->
                        val isSelected = categoryFilter == key
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF2563EB) else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable { onCategoryFilterChange(key) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Info Banner: Sequential Creation Rule
        item {
            Surface(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Hierarchy: First create Subjects > then create Units > then create Topics under those Units. Tap 'Manage Units' on any subject to drill down.",
                        fontSize = 12.sp,
                        color = Color(0xFF1E40AF),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Empty State
        if (filteredSubjects.isEmpty()) {
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LibraryBooks,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Subjects Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No subjects match '$searchQuery'" else "Create your first subject to begin building syllabus units and topics.",
                            fontSize = 12.5.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddSubject,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Subject", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Subject Cards
            items(filteredSubjects, key = { it.id }) { subject ->
                AdminSubjectCard(
                    subject = subject,
                    onSelect = { onSelectSubject(subject) },
                    onEdit = { onEditSubject(subject) },
                    onDelete = { onDeleteSubject(subject) },
                    onQuickUpdateImage = { onQuickUpdateImage(subject) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AdminSubjectCard(
    subject: Subject,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickUpdateImage: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF2563EB)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Thumbnail + Info + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail with quick image edit overlay
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable { onQuickUpdateImage() }
                ) {
                    AppFhdImage(
                        imageUrl = subject.imageUrl,
                        imageRes = subject.imageRes,
                        contentDescription = subject.name,
                        themeCategory = subject.name,
                        title = subject.name,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Camera icon badge
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Edit Image",
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title + Badges
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Display badges for all assigned categories
                        val assignedCategories = subject.getAssignedCategories()
                        assignedCategories.forEach { cat ->
                            val (catText, catBg, catColor) = when (cat.lowercase()) {
                                "prelims" -> Triple("PRELIMS", Color(0xFFFEF3C7), Color(0xFFB45309))
                                "mains" -> Triple("MAINS", Color(0xFFEFF6FF), Color(0xFF1D4ED8))
                                "general" -> Triple("GENERAL", Color(0xFFECFDF5), Color(0xFF059669))
                                else -> Triple(cat.uppercase(), Color(0xFFF5F3FF), Color(0xFF6D28D9))
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = catBg
                            ) {
                                Text(
                                    text = catText,
                                    color = catColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.5.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Units count badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "${subject.units.size} Units",
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subject.name,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.5.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    val totalTopics = subject.units.sumOf { it.topics.size }
                    Text(
                        text = if (totalTopics > 0) "$totalTopics topics organized across ${subject.units.size} units" else "No topics added yet",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // Edit & Delete Icon Buttons
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Primary Drill-Down Action: "Manage Units"
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Manage Units (${subject.units.size})",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Open Units",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2563EB)
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
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

// =============================================================================
// LEVEL 2: UNITS OF SELECTED SUBJECT
// =============================================================================
@Composable
private fun AdminUnitsLevelView(
    subject: Subject,
    onSelectUnit: (UnitModel) -> Unit,
    onAddUnit: () -> Unit,
    onEditUnit: (UnitModel) -> Unit,
    onDeleteUnit: (UnitModel) -> Unit,
    onQuickUpdateImage: (UnitModel) -> Unit,
    onEditSubject: () -> Unit,
    onUploadJson: (UnitModel) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Parent Subject Summary Card
        item {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        AppFhdImage(
                            imageUrl = subject.imageUrl,
                            contentDescription = subject.name,
                            themeCategory = "history",
                            title = subject.name,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ACTIVE SUBJECT (LEVEL 1 PARENT)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEA580C)
                        )
                        Text(
                            text = subject.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${subject.category.uppercase()} • ${subject.units.size} Units total",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    OutlinedButton(
                        onClick = onEditSubject,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Subject", fontSize = 11.sp)
                    }
                }
            }
        }

        // Subheader: Units count & Add Unit
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Subject Units (${subject.units.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Units belonging exclusively to '${subject.name}'",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Button(
                    onClick = onAddUnit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Unit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Empty state for Units
        if (subject.units.isEmpty()) {
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LayersClear,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Units in ${subject.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Create units first before you can add topics. For example: 'Unit 1: Ancient History'.",
                            fontSize = 12.5.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddUnit,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Unit 1", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Units list
            items(subject.units, key = { it.id }) { unit ->
                AdminUnitCard(
                    unit = unit,
                    onSelect = { onSelectUnit(unit) },
                    onEdit = { onEditUnit(unit) },
                    onDelete = { onDeleteUnit(unit) },
                    onQuickUpdateImage = { onQuickUpdateImage(unit) },
                    onUploadJson = { onUploadJson(unit) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AdminUnitCard(
    unit: UnitModel,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickUpdateImage: () -> Unit,
    onUploadJson: () -> Unit = {}
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF7C3AED)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail with edit image badge
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable { onQuickUpdateImage() }
                ) {
                    AppFhdImage(
                        imageUrl = unit.imageUrl,
                        imageRes = unit.imageRes,
                        contentDescription = unit.name,
                        themeCategory = unit.name,
                        title = unit.name,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(3.dp)
                            .size(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Edit Image",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Text(
                                text = "UNIT ${unit.unitNumber}",
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFF0FDF4)
                        ) {
                            Text(
                                text = "${unit.topics.size} Topics",
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = unit.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    unit.subtitle?.let {
                        Text(
                            text = it,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Edit & Delete Icon Buttons
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Action: "Manage Topics" + "Upload JSON"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.FormatListBulleted,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Manage Topics (${unit.topics.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.clickable { onUploadJson() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Upload JSON",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// LEVEL 3: TOPICS OF SELECTED UNIT
// =============================================================================
@Composable
private fun AdminTopicsLevelView(
    subject: Subject,
    unit: UnitModel,
    onSelectTopic: (TopicModel) -> Unit,
    onAddTopic: () -> Unit,
    onEditTopic: (TopicModel) -> Unit,
    onDeleteTopic: (TopicModel) -> Unit,
    onQuickUpdateImage: (TopicModel) -> Unit,
    onUploadJson: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Parent Context Card (Subject + Unit)
        item {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AppFhdImage(
                            imageUrl = unit.imageUrl,
                            contentDescription = unit.name,
                            themeCategory = "history",
                            title = unit.name,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${subject.name.uppercase()} • UNIT ${unit.unitNumber}",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF059669)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = unit.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${unit.topics.size} Topics configured for MCQs",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Subheader: Topics count & Add Topic
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Topics (${unit.topics.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Click a topic to view auto-generated Sets & Subtopics",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onUploadJson,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF2563EB))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Upload JSON", fontSize = 11.5.sp, color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onAddTopic,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Topic", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Empty state for Topics
        if (unit.topics.isEmpty()) {
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Topics in Unit ${unit.unitNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Create the first topic for '${unit.name}' or upload JSON in MCQ Upload tab.",
                            fontSize = 12.5.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = onUploadJson,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF2563EB))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload JSON", fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                            }

                            Button(
                                onClick = onAddTopic,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create First Topic", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Topics list
            items(unit.topics, key = { it.id }) { topic ->
                AdminTopicCard(
                    topic = topic,
                    onSelect = { onSelectTopic(topic) },
                    onEdit = { onEditTopic(topic) },
                    onDelete = { onDeleteTopic(topic) },
                    onQuickUpdateImage = { onQuickUpdateImage(topic) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AdminTopicCard(
    topic: TopicModel,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickUpdateImage: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail with edit image badge
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable { onQuickUpdateImage() }
                ) {
                    AppFhdImage(
                        imageUrl = topic.imageUrl,
                        contentDescription = topic.name,
                        themeCategory = "history",
                        title = topic.name,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .size(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Edit Image",
                                tint = Color.White,
                                modifier = Modifier.size(9.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Estimated MCQs badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = topic.estimatedMcqs ?: "50 MCQs",
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Difficulty badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = topic.difficulty,
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (topic.subtopics.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFECFDF5)
                            ) {
                                Text(
                                    text = "${topic.subtopics.size} Subtopics",
                                    color = Color(0xFF059669),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Edit & Delete actions
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(6.dp))

            // Footer row to view side-by-side Sets & Subtopics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Sets & Subtopics", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(14.dp))
            }
        }
    }
}

// =============================================================================
// LEVEL 4: TOPIC DETAILS SCREEN (SETS | SUBTOPICS SIDE-BY-SIDE)
// =============================================================================
@Composable
private fun AdminTopicDetailsView(
    subject: Subject,
    unit: UnitModel,
    topic: TopicModel,
    onBack: () -> Unit,
    onEditQuestion: (Question) -> Unit,
    onDeleteQuestion: (Question) -> Unit
) {
    val sets: List<PracticeSetModel> = remember(topic.id, SampleData.questionsState.size) {
        val topicSets = SampleData.getPracticeSetsForTopic(subject.id, unit, topic)
        topicSets
    }

    val topicQuestions: List<Question> = remember(topic.id, SampleData.questionsState.size) {
        SampleData.questionsState.filter { it.topicId == topic.id }
    }

    // Determine subtopics as List<String>
    val subtopicsList: List<String> = remember(topic.subtopics, topicQuestions) {
        if (topic.subtopics.isNotEmpty()) {
            topic.subtopics
        } else {
            val distinctSubNames = topicQuestions.map { it.subtopic }.filter { it.isNotBlank() }.distinct()
            if (distinctSubNames.isNotEmpty()) {
                distinctSubNames
            } else {
                listOf("${topic.name} Core Concepts")
            }
        }
    }

    var selectedSetForDetail by remember { mutableStateOf<String?>(null) }
    var selectedSubtopicForDetail by remember { mutableStateOf<String?>(null) }
    var adminSelectedTab by remember { mutableStateOf("SETS") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // TOPIC CONTEXT HEADER CARD
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    AppFhdImage(
                        imageUrl = topic.imageUrl,
                        contentDescription = topic.name,
                        themeCategory = "history",
                        title = topic.name,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${subject.name.uppercase()} • UNIT ${unit.unitNumber}",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF7C3AED)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = topic.name,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEFF6FF)) {
                            Text(
                                text = "${topicQuestions.size} Total MCQs",
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFEF3C7)) {
                            Text(
                                text = "${sets.size} Auto Sets",
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFECFDF5)) {
                            Text(
                                text = "${subtopicsList.size} Subtopics",
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // SIDE-BY-SIDE TAB BUTTONS: "SETS" & "SUBTOPICS" (ONLY ONE VISIBLE AT A TIME)
        // =========================================================================
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color(0xFFEFF6FF),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // TAB 1: SETS
                val isSetsActive = adminSelectedTab == "SETS"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSetsActive) Color(0xFF2563EB) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { adminSelectedTab = "SETS" }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "SETS",
                            tint = if (isSetsActive) Color.White else Color(0xFF1E40AF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SETS",
                            color = if (isSetsActive) Color.White else Color(0xFF1E40AF),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (isSetsActive) Color.White.copy(alpha = 0.25f) else Color(0xFFDBEAFE),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "${sets.size}",
                                color = if (isSetsActive) Color.White else Color(0xFF1D4ED8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // TAB 2: SUBTOPICS
                val isSubtopicsActive = adminSelectedTab == "SUBTOPICS"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSubtopicsActive) Color(0xFF059669) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { adminSelectedTab = "SUBTOPICS" }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = "SUBTOPICS",
                            tint = if (isSubtopicsActive) Color.White else Color(0xFF065F46),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SUBTOPICS",
                            color = if (isSubtopicsActive) Color.White else Color(0xFF065F46),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (isSubtopicsActive) Color.White.copy(alpha = 0.25f) else Color(0xFFA7F3D0),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "${subtopicsList.size}",
                                color = if (isSubtopicsActive) Color.White else Color(0xFF065F46),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // ONLY ONE TAB'S CONTENT IS DISPLAYED AT A TIME
        if (adminSelectedTab == "SETS") {
            // ---------------------------------------------------------------------
            // DISPLAY ONLY SETS (MAX 10 MCQS EACH • DYNAMIC RANGE)
            // ---------------------------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sets", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF1E40AF))
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2563EB)
                        ) {
                            Text(
                                text = "${sets.size}",
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (sets.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Sets Generated", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF475569))
                            Text("Upload MCQs in Admin panel to auto-generate sets.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(sets, key = { _, itemSet -> itemSet.id }) { index, set ->
                            val isExpanded = selectedSetForDetail == set.id
                            val isDarkMode = isSystemInDarkTheme()
                            val palette = remember(index, isDarkMode) { PremiumCardColorSystem.getSetPalette(index + 1, isDarkMode) }
                            val setQuestions: List<Question> = remember(set.id, set.questions.size) {
                                if (set.questions.isNotEmpty()) set.questions else SampleData.questionsState.take(set.questionsCount)
                            }

                            Surface(
                                color = palette.cardBgGradientStart,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, if (isExpanded) palette.mainAccent else palette.borderColor),
                                shadowElevation = if (isDarkMode) 0.dp else 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedSetForDetail = if (isExpanded) null else set.id
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = palette.mainAccent
                                            ) {
                                                Text(
                                                    text = "Set ${index + 1}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 11.5.sp,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${set.questionsCount} Questions",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = palette.textPrimary
                                            )
                                        }

                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = palette.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Q${set.startQuestionNumber} – Q${set.endQuestionNumber}  •  ${set.durationMinutes} Mins",
                                        fontSize = 10.5.sp,
                                        color = palette.textSecondary
                                    )

                                    if (isExpanded) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = palette.borderColor)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            setQuestions.forEachIndexed { qIdx, q ->
                                                Surface(
                                                    color = palette.pillBg.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        Row(
                                                            modifier = Modifier.padding(8.dp),
                                                            verticalAlignment = Alignment.Top,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                MathAndHighlightText(
                                                                    text = "Q${qIdx + 1}. ${q.questionText}",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = palette.textPrimary
                                                                )
                                                                Spacer(modifier = Modifier.height(2.dp))
                                                                Text(
                                                                    text = "Ans: Option ${q.correctOption} (${q.options[q.correctOption] ?: ""})",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = palette.mainAccent
                                                                )
                                                            }
                                                            
                                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                IconButton(
                                                                    onClick = { onEditQuestion(q) },
                                                                    modifier = Modifier.size(24.dp)
                                                                ) {
                                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = palette.mainAccent, modifier = Modifier.size(14.dp))
                                                                }
                                                                IconButton(
                                                                    onClick = { onDeleteQuestion(q) },
                                                                    modifier = Modifier.size(24.dp)
                                                                ) {
                                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
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
            }
        } else {
            // ---------------------------------------------------------------------
            // DISPLAY ONLY SUBTOPICS (NO DURATION TEXT)
            // ---------------------------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountTree, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Subtopics", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF065F46))
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF059669)
                        ) {
                            Text(
                                text = "${subtopicsList.size}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (subtopicsList.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Subtopics Added", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF475569))
                            Text("Subtopics are automatically mapped from JSON hierarchy.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(subtopicsList, key = { index, sub -> "sub_${index}_$sub" }) { index, subtopicName ->
                            val isExpanded = selectedSubtopicForDetail == subtopicName
                            val isDarkMode = isSystemInDarkTheme()
                            val palette = remember(index, subtopicName, isDarkMode) { PremiumCardColorSystem.getSubtopicPalette(index + 1, isDarkMode, subtopicName) }
                            val subQuestions: List<Question> = remember(subtopicName, topicQuestions) {
                                topicQuestions.filter { 
                                    it.subtopic.equals(subtopicName, ignoreCase = true) ||
                                    (it.subtopic.isBlank() && index == 0)
                                }
                            }

                            Surface(
                                color = palette.cardBgGradientStart,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, if (isExpanded) palette.mainAccent else palette.borderColor),
                                shadowElevation = if (isDarkMode) 0.dp else 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedSubtopicForDetail = if (isExpanded) null else subtopicName
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = palette.mainAccent
                                            ) {
                                                Text(
                                                    text = "Subtopic ${index + 1}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = subtopicName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = palette.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = palette.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${subQuestions.size} Questions",
                                        fontSize = 10.5.sp,
                                        color = palette.textSecondary,
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (isExpanded) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = palette.borderColor)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (subQuestions.isEmpty()) {
                                            Text(
                                                text = "No direct MCQs attached to this subtopic yet.",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        } else {
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                subQuestions.forEachIndexed { qIdx, q ->
                                                    Surface(
                                                        color = palette.pillBg.copy(alpha = 0.5f),
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(8.dp),
                                                            verticalAlignment = Alignment.Top,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                MathAndHighlightText(
                                                                    text = "Q${qIdx + 1}. ${q.questionText}",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = palette.textPrimary
                                                                )
                                                                Spacer(modifier = Modifier.height(2.dp))
                                                                Text(
                                                                    text = "Correct: Option ${q.correctOption}",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = palette.mainAccent
                                                                )
                                                            }

                                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                IconButton(
                                                                    onClick = { onEditQuestion(q) },
                                                                    modifier = Modifier.size(24.dp)
                                                                ) {
                                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = palette.mainAccent, modifier = Modifier.size(14.dp))
                                                                }
                                                                IconButton(
                                                                    onClick = { onDeleteQuestion(q) },
                                                                    modifier = Modifier.size(24.dp)
                                                                ) {
                                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
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
                }
            }
        }
    }
}

// =============================================================================
// REUSABLE IMAGE THUMBNAIL SELECTOR & LIVE PREVIEW COMPONENT
// =============================================================================
@Composable
private fun AdminImageThumbnailSelector(
    label: String,
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    entityType: String,
    titleForArtwork: String,
    themeCategory: String = "history"
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isUploading = true
                try {
                    val persistedUri = ImageStorageManager.persistPickedImage(context, uri, entityType)
                    onImageUrlChange(persistedUri)
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to load image from storage", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploading = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )

            // Button to upload / pick from device gallery
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Upload from device",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Upload Image",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Small Thumbnail Preview Card (Before Saving / Updating)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Small Thumbnail (60dp x 60dp)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2E8F0))
                        .border(1.5.dp, if (imageUrl.isNotBlank()) Color(0xFF2563EB) else Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF2563EB)
                        )
                    } else if (imageUrl.isNotBlank()) {
                        AppFhdImage(
                            imageUrl = imageUrl,
                            contentDescription = "Thumbnail Preview",
                            themeCategory = themeCategory,
                            title = titleForArtwork.ifBlank { "Preview" },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add image",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Add Image",
                                fontSize = 8.5.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Metadata and Preview Status
                Column(modifier = Modifier.weight(1f)) {
                    val (statusText, statusBg, statusColor) = when {
                        isUploading -> Triple("Saving image...", Color(0xFFFEF3C7), Color(0xFFD97706))
                        imageUrl.startsWith("file://") -> Triple("Local Storage Image", Color(0xFFECFDF5), Color(0xFF059669))
                        imageUrl.startsWith("http") -> Triple("Web Image URL", Color(0xFFEFF6FF), Color(0xFF2563EB))
                        imageUrl.isNotBlank() -> Triple("Custom Image Set", Color(0xFFEFF6FF), Color(0xFF2563EB))
                        else -> Triple("No Image Selected", Color(0xFFF1F5F9), Color(0xFF64748B))
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (imageUrl.isNotBlank()) "Thumbnail preview ready to save" else "Tap thumbnail or button to pick photo",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (imageUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Change",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB),
                                modifier = Modifier.clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                            Text(
                                text = "Remove",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.clickable { onImageUrlChange("") }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Image URL input field with "Choose from Device" button right next to it
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = onImageUrlChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("https://... or choose photo", fontSize = 12.sp) },
                trailingIcon = {
                    if (imageUrl.isNotEmpty()) {
                        IconButton(onClick = { onImageUrlChange("") }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Dedicated "Choose from Device" / "Upload Image" Button right next to the Image URL field
            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("btn_choose_from_device")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Upload Image from Device",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Choose from",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFDBEAFE),
                            lineHeight = 11.sp
                        )
                        Text(
                            text = "Device",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }

        // Recommended Dimensions Helper & Container Preview
        Spacer(modifier = Modifier.height(10.dp))
        val uploadSpec = when (entityType.lowercase()) {
            "subject" -> UploadImageType.SUBJECT_IMAGE
            "unit" -> UploadImageType.UNIT_IMAGE
            "topic" -> UploadImageType.TOPIC_IMAGE
            else -> UploadImageType.SUBJECT_IMAGE
        }
        RecommendedImageSizeHelper(
            spec = uploadSpec,
            imageSource = imageUrl,
            showContainerPreview = true
        )
    }
}

// =============================================================================
// SUBJECT EDIT / CREATE DIALOG
// =============================================================================
@Composable
private fun SubjectEditDialog(
    initialSubject: Subject?,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, categories: List<String>, imageUrl: String, heroQuote: String) -> Unit
) {
    var name by remember { mutableStateOf(initialSubject?.name ?: "") }
    var selectedCategories by remember {
        val initialList = initialSubject?.getAssignedCategories() ?: listOf("prelims")
        mutableStateOf(initialList.toSet())
    }
    var imageUrl by remember { mutableStateOf(initialSubject?.imageUrl ?: "") }
    var heroQuote by remember { mutableStateOf(initialSubject?.heroQuote ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialSubject != null) "Edit Subject" else "Create New Subject",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                // Subject Name
                Column {
                    Text("Subject Name *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Indian History / General Science") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Multi-Category Selection: Prelims, Mains, General
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Assign Categories (Select 1 or more) *",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Text(
                            "${selectedCategories.size} selected",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedCategories.isNotEmpty()) Color(0xFF2563EB) else Color(0xFFDC2626)
                        )
                    }
                    Text(
                        "Subject will appear in the selected filter tabs in the User App.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                    )

                    val categoryOptions = listOf(
                        Triple("prelims", "Prelims", Color(0xFFD97706)),
                        Triple("mains", "Mains", Color(0xFF2563EB)),
                        Triple("general", "General", Color(0xFF059669))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryOptions.forEach { (catKey, catLabel, catAccent) ->
                            val isChecked = selectedCategories.contains(catKey)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isChecked) catAccent.copy(alpha = 0.12f) else Color(0xFFF8FAFC),
                                border = BorderStroke(
                                    if (isChecked) 1.5.dp else 1.dp,
                                    if (isChecked) catAccent else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val newSet = selectedCategories.toMutableSet()
                                        if (isChecked) {
                                            if (newSet.size > 1) {
                                                newSet.remove(catKey)
                                            }
                                        } else {
                                            newSet.add(catKey)
                                        }
                                        selectedCategories = newSet
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isChecked) catAccent else Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = catLabel,
                                        fontWeight = if (isChecked) FontWeight.Black else FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = if (isChecked) catAccent else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }

                    // Quick Select All preset
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = if (selectedCategories.size == 3) "Clear Selection" else "Select All (Prelims + Mains + General)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB),
                            modifier = Modifier
                                .clickable {
                                    if (selectedCategories.size == 3) {
                                        selectedCategories = setOf("prelims")
                                    } else {
                                        selectedCategories = setOf("prelims", "mains", "general")
                                    }
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Subject Image & Small Thumbnail Preview
                AdminImageThumbnailSelector(
                    label = "Subject Image / Artwork",
                    imageUrl = imageUrl,
                    onImageUrlChange = { imageUrl = it },
                    entityType = "subject",
                    titleForArtwork = name.ifBlank { "Subject" },
                    themeCategory = "history"
                )

                // Hero Quote / Subtitle
                Column {
                    Text("Tagline / Subtitle", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = heroQuote,
                        onValueChange = { heroQuote = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Master APPSC History with 1,000+ MCQs") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && selectedCategories.isNotEmpty()) {
                                val catList = selectedCategories.toList()
                                val primaryCategory = catList.joinToString(",")
                                onSave(name.trim(), primaryCategory, catList, imageUrl.trim(), heroQuote.trim())
                            }
                        },
                        enabled = name.isNotBlank() && selectedCategories.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (initialSubject != null) "Update Subject" else "Create Subject", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =============================================================================
// UNIT EDIT / CREATE DIALOG
// =============================================================================
@Composable
private fun UnitEditDialog(
    subjectName: String,
    initialUnit: UnitModel?,
    suggestedUnitNumber: Int,
    onDismiss: () -> Unit,
    onSave: (unitNumber: Int, name: String, imageUrl: String, subtitle: String) -> Unit
) {
    var unitNumberText by remember { mutableStateOf((initialUnit?.unitNumber ?: suggestedUnitNumber).toString()) }
    var name by remember { mutableStateOf(initialUnit?.name ?: "") }
    var imageUrl by remember { mutableStateOf(initialUnit?.imageUrl ?: "") }
    var subtitle by remember { mutableStateOf(initialUnit?.subtitle ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialUnit != null) "Edit Unit" else "Create New Unit",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Parent: $subjectName",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD97706)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                // Unit Number
                Column {
                    Text("Unit Number *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = unitNumberText,
                        onValueChange = { unitNumberText = it.filter { ch -> ch.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. 1, 2, 3") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Unit Name
                Column {
                    Text("Unit Name *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Ancient Indian History") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Unit Image & Small Thumbnail Preview
                AdminImageThumbnailSelector(
                    label = "Unit Image / Artwork",
                    imageUrl = imageUrl,
                    onImageUrlChange = { imageUrl = it },
                    entityType = "unit",
                    titleForArtwork = name.ifBlank { "Unit $unitNumberText" },
                    themeCategory = "history"
                )

                // Subtitle
                Column {
                    Text("Subtitle / Description", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Indus Valley, Vedic Era, and Mauryan Empire") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val uNum = unitNumberText.toIntOrNull() ?: 1
                            if (name.isNotBlank()) {
                                onSave(uNum, name.trim(), imageUrl.trim(), subtitle.trim())
                            }
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (initialUnit != null) "Update Unit" else "Create Unit", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =============================================================================
// TOPIC EDIT / CREATE DIALOG
// =============================================================================
@Composable
private fun TopicEditDialog(
    subjectName: String,
    unitName: String,
    unitNumber: Int,
    initialTopic: TopicModel?,
    onDismiss: () -> Unit,
    onSave: (name: String, imageUrl: String, estimatedMcqs: String, difficulty: String, subtitle: String) -> Unit
) {
    var name by remember { mutableStateOf(initialTopic?.name ?: "") }
    var imageUrl by remember { mutableStateOf(initialTopic?.imageUrl ?: "") }
    var estimatedMcqs by remember { mutableStateOf(initialTopic?.estimatedMcqs ?: "50 MCQs") }
    var difficulty by remember { mutableStateOf(initialTopic?.difficulty ?: "Medium") }
    var subtitle by remember { mutableStateOf(initialTopic?.subtitle ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialTopic != null) "Edit Topic" else "Create New Topic",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "$subjectName  •  Unit $unitNumber",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF059669)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                // Topic Name
                Column {
                    Text("Topic Name *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Indus Valley Civilization & Harappan Culture") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Topic Image & Small Thumbnail Preview
                AdminImageThumbnailSelector(
                    label = "Topic Image / Artwork",
                    imageUrl = imageUrl,
                    onImageUrlChange = { imageUrl = it },
                    entityType = "topic",
                    titleForArtwork = name.ifBlank { "Topic" },
                    themeCategory = "history"
                )

                // Estimated MCQs & Difficulty Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Estimated MCQs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = estimatedMcqs,
                            onValueChange = { estimatedMcqs = it },
                            placeholder = { Text("e.g. 60 MCQs") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Difficulty", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Easy", "Medium", "Hard").forEach { diff ->
                                val isSelected = difficulty.equals(diff, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) Color(0xFF059669) else Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { difficulty = diff }
                                ) {
                                    Text(
                                        text = diff,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF475569),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name.trim(), imageUrl.trim(), estimatedMcqs.trim(), difficulty, subtitle.trim())
                            }
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (initialTopic != null) "Update Topic" else "Create Topic", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =============================================================================
// QUICK IMAGE URL UPDATE DIALOG
// =============================================================================
@Composable
private fun QuickImageUpdateDialog(
    targetTitle: String,
    currentUrl: String,
    entityType: String,
    onDismiss: () -> Unit,
    onSave: (newUrl: String) -> Unit
) {
    var urlInput by remember { mutableStateOf(currentUrl) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Update Image",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = targetTitle,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                // Interactive Image Selector & Small Thumbnail Preview
                AdminImageThumbnailSelector(
                    label = "Image Source & Live Preview",
                    imageUrl = urlInput,
                    onImageUrlChange = { urlInput = it },
                    entityType = entityType,
                    titleForArtwork = targetTitle,
                    themeCategory = "history"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(urlInput.trim()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Image", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Helper Sealed Classes for State Management
private sealed class ImageUpdateTarget(val title: String, val currentUrl: String) {
    class Subject(val subjectId: String, title: String, currentUrl: String) : ImageUpdateTarget(title, currentUrl)
    class Unit(val subjectId: String, val unitId: String, title: String, currentUrl: String) : ImageUpdateTarget(title, currentUrl)
    class Topic(val subjectId: String, val unitId: String, val topicId: String, title: String, currentUrl: String) : ImageUpdateTarget(title, currentUrl)
}

private sealed class DeleteTarget {
    class Subject(val subject: com.example.model.Subject) : DeleteTarget()
    class Unit(val subjectId: String, val unit: com.example.model.UnitModel) : DeleteTarget()
    class Topic(val subjectId: String, val unitId: String, val topic: com.example.model.TopicModel) : DeleteTarget()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionEditDialog(
    question: Question,
    onDismiss: () -> Unit,
    onSave: (Question) -> Unit
) {
    var questionText by remember { mutableStateOf(question.questionText) }
    var optionA by remember { mutableStateOf(question.options["A"] ?: "") }
    var optionB by remember { mutableStateOf(question.options["B"] ?: "") }
    var optionC by remember { mutableStateOf(question.options["C"] ?: "") }
    var optionD by remember { mutableStateOf(question.options["D"] ?: "") }
    var correctOption by remember { mutableStateOf(question.correctOption) }
    var explanationText by remember { mutableStateOf(question.explanation ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Question",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question Text (Supports LaTeX)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = optionA,
                        onValueChange = { optionA = it },
                        label = { Text("Option A") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = optionB,
                        onValueChange = { optionB = it },
                        label = { Text("Option B") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = optionC,
                        onValueChange = { optionC = it },
                        label = { Text("Option C") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = optionD,
                        onValueChange = { optionD = it },
                        label = { Text("Option D") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Text("Correct Option:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("A", "B", "C", "D").forEach { opt ->
                        FilterChip(
                            selected = correctOption == opt,
                            onClick = { correctOption = opt },
                            label = { Text(opt) }
                        )
                    }
                }

                OutlinedTextField(
                    value = explanationText,
                    onValueChange = { explanationText = it },
                    label = { Text("Explanation (Supports LaTeX)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                QuestionFormulaLivePreviewCard(
                    questionText = questionText,
                    optionA = optionA,
                    optionB = optionB,
                    optionC = optionC,
                    optionD = optionD,
                    correctOption = correctOption,
                    explanationText = explanationText,
                    highlightsText = "",
                    title = "✨ Live Mathematical Preview"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val updated = question.copy(
                                questionText = questionText,
                                options = mapOf("A" to optionA, "B" to optionB, "C" to optionC, "D" to optionD),
                                correctOption = correctOption,
                                explanation = explanationText
                            )
                            onSave(updated)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionDeleteConfirmDialog(
    question: Question,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Question?") },
        text = { Text("Are you sure you want to delete this question? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
