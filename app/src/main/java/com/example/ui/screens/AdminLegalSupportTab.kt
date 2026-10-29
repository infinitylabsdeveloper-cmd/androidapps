package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.scale
import com.example.data.SampleData
import com.example.db.AppDatabase
import com.example.model.LegalDocType
import com.example.model.LegalDocument
import com.example.model.SupportFaq
import com.example.repository.AppRepository
import com.example.ui.theme.PremiumCardColorSystem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminLegalSupportTab() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val activeAppId = SampleData.currentActiveAppId.value
    val repository = remember(activeAppId) { AppRepository(database, activeAppId) }

    val allDocs by repository.allLegalDocuments.collectAsStateWithLifecycle()
    val allFaqs by repository.allFaqs.collectAsStateWithLifecycle()

    var subTab by remember { mutableIntStateOf(0) } // 0: Contact & Channels, 1: Legal Documents, 2: Support FAQs
    var showEditDocDialog by remember { mutableStateOf<LegalDocument?>(null) }
    var showEditFaqDialog by remember { mutableStateOf<SupportFaq?>(null) }
    var previewDoc by remember { mutableStateOf<LegalDocument?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (previewDoc != null) {
            LegalDocumentPreview(
                doc = previewDoc!!,
                onBack = { previewDoc = null }
            )
        } else {
            // Sub-navigation
            TabRow(
                selectedTabIndex = subTab,
                containerColor = Color(0xFFFFFDF9),
                contentColor = Color(0xFFB45309)
            ) {
                Tab(
                    selected = subTab == 0,
                    onClick = { subTab = 0 },
                    text = { Text("Contact & Channels", fontWeight = FontWeight.Bold, fontSize = 12.5.sp) }
                )
                Tab(
                    selected = subTab == 1,
                    onClick = { subTab = 1 },
                    text = { Text("Legal Documents", fontWeight = FontWeight.Bold, fontSize = 12.5.sp) }
                )
                Tab(
                    selected = subTab == 2,
                    onClick = { subTab = 2 },
                    text = { Text("Support FAQs", fontWeight = FontWeight.Bold, fontSize = 12.5.sp) }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (subTab) {
                    0 -> AdminContactChannelsEditor()
                    1 -> LegalDocsList(
                        docs = allDocs.sortedBy { it.order },
                        onEdit = { showEditDocDialog = it },
                        onPreview = { previewDoc = it },
                        onDelete = {
                            repository.deleteLegalDocument(it.id)
                            com.example.util.FirebaseSyncManager.deleteLegalDocumentFromFirestore(it.id)
                        },
                        onAddNew = {
                            showEditDocDialog = LegalDocument(
                                id = UUID.randomUUID().toString(),
                                type = LegalDocType.PRIVACY_POLICY,
                                title = "",
                                content = "",
                                order = allDocs.size,
                                isPublished = false
                            )
                        }
                    )
                    else -> FaqList(
                        faqs = allFaqs.sortedBy { it.order },
                        onEdit = { showEditFaqDialog = it },
                        onDelete = {
                            repository.deleteSupportFaq(it.id)
                            com.example.util.FirebaseSyncManager.deleteSupportFaqFromFirestore(it.id)
                        },
                        onAddNew = { showEditFaqDialog = SupportFaq(question = "", answer = "", order = allFaqs.size) }
                    )
                }
            }
        }
    }

    showEditDocDialog?.let { doc ->
        EditLegalDocDialog(
            doc = doc,
            allDocs = allDocs,
            onDismiss = { showEditDocDialog = null },
            onSave = { updatedDoc ->
                repository.saveLegalDocument(updatedDoc)
                com.example.util.FirebaseSyncManager.syncLegalDocumentToFirestore(
                    doc = updatedDoc,
                    onSuccess = { android.util.Log.d("AdminLegalSupportTab", "Synced LegalDoc ${updatedDoc.id} to Firestore") },
                    onFailure = { e -> android.util.Log.e("AdminLegalSupportTab", "Failed to sync ${updatedDoc.id} to Firestore", e) }
                )
                showEditDocDialog = null
                Toast.makeText(context, "Legal document saved successfully and synced to Firestore", Toast.LENGTH_SHORT).show()
            }
        )
    }

    showEditFaqDialog?.let { faq ->
        EditFaqDialog(
            faq = faq,
            onDismiss = { showEditFaqDialog = null },
            onSave = { updatedFaq ->
                repository.saveSupportFaq(updatedFaq)
                com.example.util.FirebaseSyncManager.syncSupportFaqToFirestore(
                    faq = updatedFaq,
                    onSuccess = { android.util.Log.d("AdminLegalSupportTab", "Synced FAQ ${updatedFaq.id} to Firestore") },
                    onFailure = { e -> android.util.Log.e("AdminLegalSupportTab", "Failed to sync FAQ ${updatedFaq.id} to Firestore", e) }
                )
                showEditFaqDialog = null
                Toast.makeText(context, "FAQ saved successfully and synced to Firestore", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun LegalDocsList(
    docs: List<LegalDocument>,
    onEdit: (LegalDocument) -> Unit,
    onPreview: (LegalDocument) -> Unit,
    onDelete: (LegalDocument) -> Unit,
    onAddNew: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onAddNew,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Legal Document")
            }
        }

        items(docs) { doc ->
            LegalDocItem(doc, onEdit, onPreview, onDelete)
        }

        if (docs.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Gavel,
                    title = "No Legal Documents",
                    subtitle = "Create privacy policies, terms, and support screens for your apps."
                )
            }
        }
    }
}

@Composable
private fun LegalDocItem(
    doc: LegalDocument,
    onEdit: (LegalDocument) -> Unit,
    onPreview: (LegalDocument) -> Unit,
    onDelete: (LegalDocument) -> Unit
) {
    val isDarkMode = isSystemInDarkTheme()
    val palette = remember(isDarkMode) { PremiumCardColorSystem.getSetPalette(1, isDarkMode) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (doc.isEnabled) palette.borderColor else Color(0xFFCBD5E1))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (doc.isEnabled) Color(0xFFEFF6FF) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                val itemIcon = doc.iconName?.let { getIconForName(it) } ?: getIconForType(doc.type)
                Icon(
                    imageVector = itemIcon,
                    contentDescription = null,
                    tint = if (doc.isEnabled) Color(0xFF2563EB) else Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = doc.title.ifBlank { doc.type.title },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (doc.isEnabled) Color(0xFF1E293B) else Color(0xFF64748B)
                    )
                    if (!doc.isEnabled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "DISABLED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (!doc.isPublished) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "DRAFT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${doc.type.title} • Order: ${doc.order}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(4.dp))

                val appsText = if (doc.mappedAppIds.isEmpty()) "All Apps" else "${doc.mappedAppIds.size} Apps"
                Surface(
                    color = if (doc.isEnabled) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Target: $appsText",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (doc.isEnabled) Color(0xFF2563EB) else Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(onClick = { onPreview(doc) }) {
                Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = Color(0xFF2563EB))
            }
            IconButton(onClick = { onEdit(doc) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF64748B))
            }
            IconButton(onClick = { onDelete(doc) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
            }
        }
    }
}

@Composable
private fun FaqList(
    faqs: List<SupportFaq>,
    onEdit: (SupportFaq) -> Unit,
    onDelete: (SupportFaq) -> Unit,
    onAddNew: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onAddNew,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New FAQ Question")
            }
        }

        items(faqs) { faq ->
            FaqItem(faq, onEdit, onDelete)
        }

        if (faqs.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Quiz,
                    title = "No FAQs Found",
                    subtitle = "Help your users with common questions and answers."
                )
            }
        }
    }
}

@Composable
private fun FaqItem(
    faq: SupportFaq,
    onEdit: (SupportFaq) -> Unit,
    onDelete: (SupportFaq) -> Unit
) {
    val isDarkMode = isSystemInDarkTheme()
    val palette = remember(isDarkMode) { PremiumCardColorSystem.getSetPalette(1, isDarkMode) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, palette.borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = faq.question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = faq.answer,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                val appsText = if (faq.mappedAppIds.isEmpty()) "All Apps" else "${faq.mappedAppIds.size} Apps"
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Target: $appsText",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(onClick = { onEdit(faq) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF64748B))
            }
            IconButton(onClick = { onDelete(faq) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditLegalDocDialog(
    doc: LegalDocument,
    allDocs: List<LegalDocument>,
    onDismiss: () -> Unit,
    onSave: (LegalDocument) -> Unit
) {
    // Basic Fields
    var type by remember { mutableStateOf(doc.type) }
    var mappedAppIds by remember { mutableStateOf(doc.mappedAppIds) }
    var isEnabled by remember { mutableStateOf(doc.isEnabled) }
    var order by remember { mutableIntStateOf(doc.order) }

    // Draft Workspaces
    var currentTitle by remember { mutableStateOf(doc.draftTitle ?: doc.title) }
    var currentContent by remember { mutableStateOf(doc.draftContent ?: doc.content) }

    // Premium Theme Configs
    var bgGradientStart by remember { mutableStateOf(doc.bgGradientStart ?: "") }
    var bgGradientEnd by remember { mutableStateOf(doc.bgGradientEnd ?: "") }
    var accentColorHex by remember { mutableStateOf(doc.accentColorHex ?: "") }
    var iconName by remember { mutableStateOf(doc.iconName ?: "Shield") }
    var typographyStyle by remember { mutableStateOf(doc.draftTitle ?: "Normal") }

    // Sections Editor
    val initialSections: List<DocSection> = remember(doc.draftSectionsJson, doc.sectionsJson) {
        val targetJson = doc.draftSectionsJson ?: doc.sectionsJson
        if (!targetJson.isNullOrBlank()) {
            try {
                Gson().fromJson(targetJson, object : TypeToken<List<DocSection>>() {}.type)
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()
    }
    var sectionsList by remember { mutableStateOf(initialSections) }
    var showAddSectionDialog by remember { mutableStateOf<DocSection?>(null) }

    // Contact/Social Settings Editor (only relevant for CONTACT_US or HELP_CENTER)
    val initialContactSettings = remember(doc.socialLinksJson) {
        if (!doc.socialLinksJson.isNullOrBlank()) {
            try {
                Gson().fromJson(doc.socialLinksJson, AdminContactSettings::class.java)
            } catch (e: Exception) {
                AdminContactSettings()
            }
        } else AdminContactSettings()
    }
    var supportEmail by remember { mutableStateOf(initialContactSettings.supportEmail ?: "") }
    var supportPhone by remember { mutableStateOf(initialContactSettings.supportPhone ?: "") }
    var whatsappNumber by remember { mutableStateOf(initialContactSettings.whatsappNumber ?: "") }
    var whatsappLink by remember { mutableStateOf(initialContactSettings.whatsappLink ?: "") }
    var telegramId by remember { mutableStateOf(initialContactSettings.telegramId ?: "") }
    var telegramLink by remember { mutableStateOf(initialContactSettings.telegramLink ?: "") }
    var supportWebsite by remember { mutableStateOf(initialContactSettings.supportWebsite ?: "") }
    var customSocialLinks by remember { mutableStateOf(initialContactSettings.socialLinks) }
    var showAddSocialLinkDialog by remember { mutableStateOf<SocialLink?>(null) }

    // Preview Pane Toggle
    var showDialogPreview by remember { mutableStateOf(false) }

    val appsList = SampleData.appsListState
    val iconPresets = listOf("Shield", "Assignment", "CurrencyExchange", "Star", "Delete", "Info", "ContactSupport", "Quiz", "Help", "Business", "Email", "Phone", "Chat", "Send")
    val typographyPresets = listOf("Normal", "Serif", "Monospace", "SansSerif", "Bold", "Italic")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configure Legal/Support Document",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enabled", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        modifier = Modifier.scale(0.7f)
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showDialogPreview) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("LIVE PREVIEW MODE", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 12.sp)
                                    IconButton(onClick = { showDialogPreview = false }) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red)
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = currentTitle.ifBlank { "Sample Document Title" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (sectionsList.isNotEmpty()) {
                                    sectionsList.forEach { section ->
                                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                            Text(text = "• ${section.title}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                                            Text(text = section.content, fontSize = 12.sp, color = Color.DarkGray)
                                        }
                                    }
                                } else {
                                    Text(text = currentContent.ifBlank { "No content written yet." }, fontSize = 13.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Select Document Type", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LegalDocType.values().forEach { docType ->
                            FilterChip(
                                selected = type == docType,
                                onClick = {
                                    type = docType
                                    if (currentTitle.isBlank()) currentTitle = docType.title
                                },
                                label = { Text(docType.title, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = currentTitle,
                            onValueChange = { currentTitle = it },
                            label = { Text("Page Custom Title") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = order.toString(),
                            onValueChange = { order = it.toIntOrNull() ?: 0 },
                            label = { Text("Display Order") },
                            modifier = Modifier.width(90.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Premium Style Config Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Page Visual Styles & Layout", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = bgGradientStart,
                                    onValueChange = { bgGradientStart = it },
                                    label = { Text("BG Gradient Start (Hex)") },
                                    placeholder = { Text("#0F172A") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = bgGradientEnd,
                                    onValueChange = { bgGradientEnd = it },
                                    label = { Text("BG Gradient End (Hex)") },
                                    placeholder = { Text("#1E293B") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = accentColorHex,
                                    onValueChange = { accentColorHex = it },
                                    label = { Text("Accent Color (Hex)") },
                                    placeholder = { Text("#3B82F6") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Header Icon Select", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    var expandIconDropdown by remember { mutableStateOf(false) }
                                    Box {
                                        OutlinedButton(
                                            onClick = { expandIconDropdown = true },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(iconName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        DropdownMenu(expanded = expandIconDropdown, onDismissRequest = { expandIconDropdown = false }) {
                                            iconPresets.forEach { preset ->
                                                DropdownMenuItem(
                                                    text = { Text(preset) },
                                                    onClick = {
                                                        iconName = preset
                                                        expandIconDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("Typography Style", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                var expandTyDropdown by remember { mutableStateOf(false) }
                                Box {
                                    OutlinedButton(
                                        onClick = { expandTyDropdown = true },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(typographyStyle)
                                    }
                                    DropdownMenu(expanded = expandTyDropdown, onDismissRequest = { expandTyDropdown = false }) {
                                        typographyPresets.forEach { ty ->
                                            DropdownMenuItem(
                                                text = { Text(ty) },
                                                onClick = {
                                                    typographyStyle = ty
                                                    expandTyDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Dynamic Sections Editor
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
                        border = BorderStroke(1.dp, Color(0xFFCCFBF1))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Document Page Sections", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF115E59))
                                Button(
                                    onClick = { showAddSectionDialog = DocSection(title = "", content = "", order = sectionsList.size) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF134E4A)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+ Section", fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            if (sectionsList.isEmpty()) {
                                Text("No custom sections added yet. Page will fall back to single raw content field.", fontSize = 12.sp, color = Color(0xFF0F766E))
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    sectionsList.forEachIndexed { index, section ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.White, RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(section.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                                Text(section.content, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            Row {
                                                IconButton(
                                                    onClick = {
                                                        if (index > 0) {
                                                            val reordered = sectionsList.toMutableList()
                                                            val temp = reordered[index]
                                                            reordered[index] = reordered[index - 1]
                                                            reordered[index - 1] = temp
                                                            sectionsList = reordered.mapIndexed { idx, sec -> sec.copy(order = idx) }
                                                        }
                                                    },
                                                    enabled = index > 0
                                                ) {
                                                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = {
                                                        if (index < sectionsList.size - 1) {
                                                            val reordered = sectionsList.toMutableList()
                                                            val temp = reordered[index]
                                                            reordered[index] = reordered[index + 1]
                                                            reordered[index + 1] = temp
                                                            sectionsList = reordered.mapIndexed { idx, sec -> sec.copy(order = idx) }
                                                        }
                                                    },
                                                    enabled = index < sectionsList.size - 1
                                                ) {
                                                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(onClick = { showAddSectionDialog = section }) {
                                                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Blue, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(onClick = { sectionsList = sectionsList.filter { it.id != section.id }.mapIndexed { idx, sec -> sec.copy(order = idx) } }) {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Raw Fallback content editor (used if sections are empty)
                if (sectionsList.isEmpty()) {
                    item {
                        OutlinedTextField(
                            value = currentContent,
                            onValueChange = { currentContent = it },
                            label = { Text("Standard Web Content (Raw Fallback)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // CONTACT SUPPORT CHANNELS (Only visible for Contact Us screen)
                if (type == LegalDocType.CONTACT_US) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                            border = BorderStroke(1.dp, Color(0xFFFFE4E6))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Academic Support Contact Channels", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF9F1239))

                                OutlinedTextField(
                                    value = supportEmail,
                                    onValueChange = { supportEmail = it },
                                    label = { Text("Candidate Helpdesk Email") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = supportPhone,
                                    onValueChange = { supportPhone = it },
                                    label = { Text("Candidate Support Helpline") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = whatsappNumber,
                                        onValueChange = { whatsappNumber = it },
                                        label = { Text("WhatsApp Number") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = whatsappLink,
                                        onValueChange = { whatsappLink = it },
                                        label = { Text("WhatsApp Link Override") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = telegramId,
                                        onValueChange = { telegramId = it },
                                        label = { Text("Telegram Username") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = telegramLink,
                                        onValueChange = { telegramLink = it },
                                        label = { Text("Telegram Link Override") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                OutlinedTextField(
                                    value = supportWebsite,
                                    onValueChange = { supportWebsite = it },
                                    label = { Text("Official Portal Web link") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                // Custom Social links editor
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Custom Social Community Communities", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF9F1239))
                                    TextButton(onClick = { showAddSocialLinkDialog = SocialLink(platform = "YouTube", url = "", value = "") }) {
                                        Text("+ Community")
                                    }
                                }

                                customSocialLinks.forEach { link ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("${link.platform}: ${link.value}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(link.url, fontSize = 10.sp, color = Color.Gray)
                                        }
                                        Row {
                                            IconButton(onClick = { showAddSocialLinkDialog = link }) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = { customSocialLinks = customSocialLinks.filter { it.url != link.url } }) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Target Applications", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = mappedAppIds.isEmpty(),
                            onCheckedChange = { if (it) mappedAppIds = emptyList() }
                        )
                        Text("All Applications", fontSize = 13.sp)
                    }

                    appsList.forEach { app ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = mappedAppIds.contains(app.id),
                                onCheckedChange = { checked ->
                                    val current = mappedAppIds.toMutableList()
                                    if (checked) {
                                        if (!current.contains(app.id)) current.add(app.id)
                                    } else {
                                        current.remove(app.id)
                                    }
                                    mappedAppIds = current
                                }
                            )
                            Text(app.name, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // RESET TO DEFAULT BUTTON
                TextButton(
                    onClick = {
                        val fallback = SampleData.getDefaultLegalDocs().find { it.type == type }
                        if (fallback != null) {
                            currentTitle = fallback.title
                            currentContent = fallback.content
                            sectionsList = emptyList()
                            bgGradientStart = ""
                            bgGradientEnd = ""
                            accentColorHex = ""
                            iconName = type.icon
                            typographyStyle = "Normal"
                        }
                    }
                ) {
                    Text("Reset Defaults", color = Color.Red)
                }

                // TOGGLE PREVIEW
                TextButton(onClick = { showDialogPreview = !showDialogPreview }) {
                    Text(if (showDialogPreview) "Hide Preview" else "Preview Pane")
                }

                // SAVE DRAFT
                FilledTonalButton(
                    onClick = {
                        val sectionsJsonStr = if (sectionsList.isNotEmpty()) Gson().toJson(sectionsList) else null
                        val socialLinksJsonStr = if (type == LegalDocType.CONTACT_US) {
                            Gson().toJson(AdminContactSettings(
                                supportEmail = supportEmail,
                                supportPhone = supportPhone,
                                whatsappNumber = whatsappNumber,
                                whatsappLink = whatsappLink,
                                telegramId = telegramId,
                                telegramLink = telegramLink,
                                supportWebsite = supportWebsite,
                                socialLinks = customSocialLinks
                            ))
                        } else null

                        onSave(doc.copy(
                            type = type,
                            order = order,
                            mappedAppIds = mappedAppIds,
                            isEnabled = isEnabled,
                            bgGradientStart = bgGradientStart.ifBlank { null },
                            bgGradientEnd = bgGradientEnd.ifBlank { null },
                            accentColorHex = accentColorHex.ifBlank { null },
                            iconName = iconName,
                            draftTitle = currentTitle,
                            draftContent = currentContent,
                            draftSectionsJson = sectionsJsonStr,
                            socialLinksJson = socialLinksJsonStr,
                            isPublished = false,
                            lastUpdated = System.currentTimeMillis()
                        ))
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Draft")
                }

                // PUBLISH LIVE
                Button(
                    onClick = {
                        val sectionsJsonStr = if (sectionsList.isNotEmpty()) Gson().toJson(sectionsList) else null
                        val socialLinksJsonStr = if (type == LegalDocType.CONTACT_US) {
                            Gson().toJson(AdminContactSettings(
                                supportEmail = supportEmail,
                                supportPhone = supportPhone,
                                whatsappNumber = whatsappNumber,
                                whatsappLink = whatsappLink,
                                telegramId = telegramId,
                                telegramLink = telegramLink,
                                supportWebsite = supportWebsite,
                                socialLinks = customSocialLinks
                            ))
                        } else null

                        onSave(doc.copy(
                            title = currentTitle,
                            content = currentContent,
                            sectionsJson = sectionsJsonStr,
                            type = type,
                            order = order,
                            mappedAppIds = mappedAppIds,
                            isEnabled = isEnabled,
                            bgGradientStart = bgGradientStart.ifBlank { null },
                            bgGradientEnd = bgGradientEnd.ifBlank { null },
                            accentColorHex = accentColorHex.ifBlank { null },
                            iconName = iconName,
                            socialLinksJson = socialLinksJsonStr,
                            draftTitle = null,
                            draftContent = null,
                            draftSectionsJson = null,
                            isPublished = true,
                            lastUpdated = System.currentTimeMillis()
                        ))
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Publish Live")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Sub dialog for Custom Section Add/Edit
    if (showAddSectionDialog != null) {
        var secTitle by remember { mutableStateOf(showAddSectionDialog!!.title) }
        var secContent by remember { mutableStateOf(showAddSectionDialog!!.content) }

        AlertDialog(
            onDismissRequest = { showAddSectionDialog = null },
            title = { Text(if (showAddSectionDialog!!.title.isBlank()) "Add Web Section" else "Edit Section", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = secTitle,
                        onValueChange = { secTitle = it },
                        label = { Text("Section Headline") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = secContent,
                        onValueChange = { secContent = it },
                        label = { Text("Section Details Body") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = sectionsList.toMutableList()
                        val existingIndex = updated.indexOfFirst { it.id == showAddSectionDialog!!.id }
                        if (existingIndex >= 0) {
                            updated[existingIndex] = showAddSectionDialog!!.copy(title = secTitle, content = secContent)
                        } else {
                            updated.add(DocSection(id = showAddSectionDialog!!.id, title = secTitle, content = secContent, order = sectionsList.size))
                        }
                        sectionsList = updated.sortedBy { it.order }
                        showAddSectionDialog = null
                    },
                    enabled = secTitle.isNotBlank() && secContent.isNotBlank()
                ) {
                    Text("Apply Section")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSectionDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Sub dialog for Custom Social Link
    if (showAddSocialLinkDialog != null) {
        var platformName by remember { mutableStateOf(showAddSocialLinkDialog!!.platform) }
        var linkUrl by remember { mutableStateOf(showAddSocialLinkDialog!!.url) }
        var linkVal by remember { mutableStateOf(showAddSocialLinkDialog!!.value) }

        AlertDialog(
            onDismissRequest = { showAddSocialLinkDialog = null },
            title = { Text("Social Link Community", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = platformName,
                        onValueChange = { platformName = it },
                        label = { Text("Platform Name (e.g. YouTube, Instagram, Twitter)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = linkVal,
                        onValueChange = { linkVal = it },
                        label = { Text("Community Handle Value (e.g. @appsc_group2)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = linkUrl,
                        onValueChange = { linkUrl = it },
                        label = { Text("Community Hyperlink URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = customSocialLinks.toMutableList()
                        val existingIndex = updated.indexOfFirst { it.url == showAddSocialLinkDialog!!.url }
                        if (existingIndex >= 0) {
                            updated[existingIndex] = showAddSocialLinkDialog!!.copy(platform = platformName, url = linkUrl, value = linkVal)
                        } else {
                            updated.add(SocialLink(platform = platformName, url = linkUrl, value = linkVal))
                        }
                        customSocialLinks = updated
                        showAddSocialLinkDialog = null
                    },
                    enabled = platformName.isNotBlank() && linkUrl.isNotBlank() && linkVal.isNotBlank()
                ) {
                    Text("Add Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSocialLinkDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalDocumentPreview(
    doc: LegalDocument,
    onBack: () -> Unit
) {
    val activeApp = SampleData.getActiveApp()
    val processedContent = remember(doc.content, activeApp.name) {
        doc.content.replace("{{APP_NAME}}", activeApp.name)
            .replace("{{COMPANY_NAME}}", "${activeApp.name} Learning")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Preview: ${doc.title}") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = doc.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Last Updated: ${java.text.SimpleDateFormat("dd MMM yyyy").format(java.util.Date(doc.lastUpdated))}",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )
            HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = processedContent,
                fontSize = 15.sp,
                color = Color(0xFF334155),
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun EditFaqDialog(
    faq: SupportFaq,
    onDismiss: () -> Unit,
    onSave: (SupportFaq) -> Unit
) {
    var question by remember { mutableStateOf(faq.question) }
    var answer by remember { mutableStateOf(faq.answer) }
    var mappedAppIds by remember { mutableStateOf(faq.mappedAppIds) }
    var order by remember { mutableIntStateOf(faq.order) }

    val appsList = SampleData.appsListState

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit FAQ", fontWeight = FontWeight.Black) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = question,
                            onValueChange = { question = it },
                            label = { Text("Question") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = order.toString(),
                            onValueChange = { order = it.toIntOrNull() ?: 0 },
                            label = { Text("Order") },
                            modifier = Modifier.width(80.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        label = { Text("Answer") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Text("Target Applications", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    appsList.forEach { app ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = mappedAppIds.contains(app.id) || mappedAppIds.isEmpty(),
                                onCheckedChange = { checked ->
                                    val current = mappedAppIds.toMutableList()
                                    if (checked) {
                                        if (!current.contains(app.id)) current.add(app.id)
                                    } else {
                                        current.remove(app.id)
                                    }
                                    mappedAppIds = current
                                }
                            )
                            Text(app.name, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(faq.copy(
                        question = question,
                        answer = answer,
                        mappedAppIds = mappedAppIds,
                        order = order
                    ))
                },
                enabled = question.isNotBlank() && answer.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save FAQ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFCBD5E1)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569)
        )
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = Color(0xFF94A3B8),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun AdminContactChannelsEditor() {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        com.example.util.ContactSupportManager.init(context)
    }
    val currentConfig by com.example.util.ContactSupportManager.configState

    var supportEmail by remember(currentConfig) { mutableStateOf(currentConfig.supportEmail) }
    var isEmailEnabled by remember(currentConfig) { mutableStateOf(currentConfig.isEmailSupportEnabled) }

    var whatsappNumber by remember(currentConfig) { mutableStateOf(currentConfig.whatsappNumber) }
    var whatsappLink by remember(currentConfig) { mutableStateOf(currentConfig.whatsappLink) }
    var isWhatsAppEnabled by remember(currentConfig) { mutableStateOf(currentConfig.isWhatsAppEnabled) }

    var communityUrl by remember(currentConfig) { mutableStateOf(currentConfig.communityUrl) }
    var isTelegramEnabled by remember(currentConfig) { mutableStateOf(currentConfig.isTelegramEnabled) }

    var supportHours by remember(currentConfig) { mutableStateOf(currentConfig.supportTimingsLine1) }
    var responseTimeText by remember(currentConfig) { mutableStateOf(currentConfig.fastSupportBadge) }
    var bannerTitle by remember(currentConfig) { mutableStateOf(currentConfig.bannerTitle) }
    var bannerSubtext by remember(currentConfig) { mutableStateOf(currentConfig.bannerSubtitle) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F3))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFFFFDF9),
            border = BorderStroke(1.2.dp, Color(0xFFD4AF37).copy(alpha = 0.6f)),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFFBEB), Color(0xFFFFFDF9))
                        )
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Support & Contact Channels",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Manage WhatsApp, Email, Telegram, Hours & Support Banner",
                            fontSize = 11.5.sp,
                            color = Color(0xFF9A7B2C),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 1. WhatsApp Support Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFFDF9),
            border = BorderStroke(1.dp, Color(0xFFE8DCC4)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WhatsApp Support Channel", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                    }
                    Switch(
                        checked = isWhatsAppEnabled,
                        onCheckedChange = { isWhatsAppEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF059669))
                    )
                }

                OutlinedTextField(
                    value = whatsappNumber,
                    onValueChange = { whatsappNumber = it },
                    label = { Text("WhatsApp Phone Number") },
                    placeholder = { Text("+91 94935 12768") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = whatsappLink,
                    onValueChange = { whatsappLink = it },
                    label = { Text("WhatsApp Direct Link (wa.me)") },
                    placeholder = { Text("https://wa.me/919493512768") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // 2. Email Support Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFFDF9),
            border = BorderStroke(1.dp, Color(0xFFE8DCC4)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Official Email Support", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                    }
                    Switch(
                        checked = isEmailEnabled,
                        onCheckedChange = { isEmailEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFB45309))
                    )
                }

                OutlinedTextField(
                    value = supportEmail,
                    onValueChange = { supportEmail = it },
                    label = { Text("Official Support Email Address") },
                    placeholder = { Text("sobhadhanaraju0@gmail.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // 3. Telegram Community & Support Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFFDF9),
            border = BorderStroke(1.dp, Color(0xFFE8DCC4)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Telegram Support & Channel", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                    }
                    Switch(
                        checked = isTelegramEnabled,
                        onCheckedChange = { isTelegramEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF0284C7))
                    )
                }

                OutlinedTextField(
                    value = communityUrl,
                    onValueChange = { communityUrl = it },
                    label = { Text("Telegram Channel / Support URL") },
                    placeholder = { Text("https://t.me/DHANARAJU0") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // 4. Support Hours & Banner Configuration
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFFDF9),
            border = BorderStroke(1.dp, Color(0xFFE8DCC4)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF9A7B2C), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Support Hours & Banner Message", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                }

                OutlinedTextField(
                    value = supportHours,
                    onValueChange = { supportHours = it },
                    label = { Text("Support Operating Hours") },
                    placeholder = { Text("Monday – Saturday: 9:00 AM – 8:00 PM IST") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = responseTimeText,
                    onValueChange = { responseTimeText = it },
                    label = { Text("Response Time Badge Text") },
                    placeholder = { Text("Usually responds within 2–4 hours") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = bannerTitle,
                    onValueChange = { bannerTitle = it },
                    label = { Text("Support Banner Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = bannerSubtext,
                    onValueChange = { bannerSubtext = it },
                    label = { Text("Support Banner Subtext") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Save & Reset Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    com.example.util.ContactSupportManager.resetToDefaults(context)
                    Toast.makeText(context, "Reset to official default contact details", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFD4AF37)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF92400E)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Defaults", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val updated = currentConfig.copy(
                        supportEmail = supportEmail.trim().ifBlank { "sobhadhanaraju0@gmail.com" },
                        isEmailSupportEnabled = isEmailEnabled,
                        whatsappNumber = whatsappNumber.trim().ifBlank { "+91 94935 12768" },
                        whatsappLink = whatsappLink.trim().ifBlank { "https://wa.me/919493512768" },
                        isWhatsAppEnabled = isWhatsAppEnabled,
                        telegramLink = communityUrl.trim().ifBlank { "https://t.me/DHANARAJU0" },
                        communityUrl = communityUrl.trim().ifBlank { "https://t.me/DHANARAJU0" },
                        isTelegramEnabled = isTelegramEnabled,
                        supportTimingsLine1 = supportHours.trim().ifBlank { "Monday – Saturday: 9:00 AM – 8:00 PM IST" },
                        fastSupportBadge = responseTimeText.trim().ifBlank { "Usually responds within 2–4 hours" },
                        bannerTitle = bannerTitle.trim().ifBlank { "Have a question or facing an issue?" },
                        bannerSubtitle = bannerSubtext.trim().ifBlank { "Reach out to our support team anytime." }
                    )
                    com.example.util.ContactSupportManager.saveConfig(context, updated)
                    Toast.makeText(context, "✅ Support & Contact settings saved across all screens!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Contact Config", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
