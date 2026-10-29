package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.model.UnitModel
import com.example.ui.theme.AppLuminousBackground
import com.example.ui.theme.AvailableFontFamilies
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.util.DownloadHierarchyType
import com.example.util.OfflineSyncManager
import com.example.util.SyncStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadedMaterialsScreen(
    onBack: () -> Unit,
    onStartPractice: (subjectId: String, unitId: String, topicId: String, setName: String) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current

    // First-time Offline Mode Onboarding Tutorial Dialog state
    var showOfflineOnboarding by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("offline_onboarding_prefs", android.content.Context.MODE_PRIVATE)
        val seen = prefs.getBoolean("has_seen_offline_onboarding", false)
        if (!seen) {
            showOfflineOnboarding = true
        }
    }

    if (showOfflineOnboarding) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFFEFF6FF),
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📥", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Offline Mode & ₹99 Pass",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E3A8A)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Welcome to your Offline MCQ Vault! Here you can access downloaded study materials and practice without an internet connection.",
                        fontSize = 13.sp,
                        color = Color(0xFF1E40AF),
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFF93C5FD))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "✨ Why the ₹99 Offline Pass?",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8),
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = "• Unlocks seamless offline practice through Sets, Subtopics, and Downloaded Materials.\n• Validity aligns with your active plan duration.\n• Online practice remains 100% free for everyone.",
                                fontSize = 12.sp,
                                color = Color(0xFF1E293B),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val prefs = context.getSharedPreferences("offline_onboarding_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("has_seen_offline_onboarding", true).apply()
                        showOfflineOnboarding = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Got It • Explore Vault", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Search and Filter State
    var searchQuery by remember { mutableStateOf("") }

    // Expandable Hierarchy States
    val expandedSubjectIds = remember { mutableStateListOf<String>() }
    val expandedUnitIds = remember { mutableStateListOf<String>() }
    val expandedTopicIds = remember { mutableStateListOf<String>() }

    // Font Options State
    var selectedFontName by remember { mutableStateOf("Cinzel") }
    var fontScaleMultiplier by remember { mutableFloatStateOf(1.0f) }
    var showFontOptionsPanel by remember { mutableStateOf(false) }

    val activeHeaderFont = remember(selectedFontName) {
        AvailableFontFamilies[selectedFontName] ?: CinzelFontFamily
    }

    // Access Downloaded Packs State from OfflineSyncManager
    val downloadedPacks = OfflineSyncManager.downloadedPacks

    // Calculated Stats
    val totalMcqsCount = remember(downloadedPacks.size) {
        if (downloadedPacks.isEmpty()) 0
        else downloadedPacks.sumOf { it.questionCount }
    }

    val totalStorageBytes = remember(downloadedPacks.size) {
        if (downloadedPacks.isEmpty()) 0L
        else downloadedPacks.sumOf { it.sizeBytes }
    }

    val formattedStorageText = remember(totalStorageBytes) {
        if (totalStorageBytes > 1024 * 1024 * 1024) {
            "%.2f GB".format(totalStorageBytes / (1024.0 * 1024.0 * 1024.0))
        } else {
            "%.1f MB".format(totalStorageBytes / (1024.0 * 1024.0))
        }
    }

    // Filter Subjects based on search or downloaded content
    val allSubjects = SampleData.subjectsState.toList()
    val filteredSubjects = remember(allSubjects, searchQuery, downloadedPacks.size) {
        allSubjects.filter { subject ->
            val matchesQuery = searchQuery.isBlank() ||
                    subject.name.contains(searchQuery, ignoreCase = true) ||
                    subject.units.any { u ->
                        u.name.contains(searchQuery, ignoreCase = true) ||
                                u.topics.any { t -> t.name.contains(searchQuery, ignoreCase = true) }
                    }

            val isDownloadedOrContainsDownloads = OfflineSyncManager.isSubjectDownloaded(subject.id) ||
                    subject.units.any { u ->
                        OfflineSyncManager.isUnitDownloaded(subject.id, u.id) ||
                                u.topics.any { t -> OfflineSyncManager.isTopicDownloaded(subject.id, u.id, t.id) }
                    }

            matchesQuery && (searchQuery.isNotBlank() || isDownloadedOrContainsDownloads || downloadedPacks.isEmpty())
        }
    }

    AppLuminousBackground(screenName = "Downloaded Materials Vault") {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Downloaded Materials",
                                fontFamily = activeHeaderFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = (20 * fontScaleMultiplier).sp,
                                color = Color.White
                            )
                            Text(
                                text = "Offline MCQ Vault • APPSC Exam Prep",
                                fontSize = 11.sp,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFontOptionsPanel = !showFontOptionsPanel }) {
                            Icon(
                                imageVector = Icons.Default.FontDownload,
                                contentDescription = "Typography Options",
                                tint = if (showFontOptionsPanel) Color(0xFFF59E0B) else Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0F172A).copy(alpha = 0.95f)
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Font Styling Options Expandable Bar
                AnimatedVisibility(
                    visible = showFontOptionsPanel,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Luxury Typography Options",
                                fontFamily = activeHeaderFont,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Header Font:", fontSize = 12.sp, color = Color.LightGray)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Cinzel", "Playfair Display", "Montserrat").forEach { fontName ->
                                        FilterChip(
                                            selected = selectedFontName == fontName,
                                            onClick = { selectedFontName = fontName },
                                            label = {
                                                Text(
                                                    fontName.take(8),
                                                    fontSize = 11.sp,
                                                    fontWeight = if (selectedFontName == fontName) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFFF59E0B),
                                                selectedLabelColor = Color.Black,
                                                containerColor = Color(0xFF0F172A),
                                                labelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Font Size Scale:", fontSize = 12.sp, color = Color.LightGray)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(0.9f to "Compact", 1.0f to "Normal", 1.15f to "Large").forEach { (scale, label) ->
                                        OutlinedButton(
                                            onClick = { fontScaleMultiplier = scale },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (fontScaleMultiplier == scale) Color(0xFF3B82F6) else Color.Transparent,
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(label, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Vault Stats Overview Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1E1B4B),
                                        Color(0xFF312E81),
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.FolderZip,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Offline Content Vault",
                                            fontFamily = activeHeaderFont,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = (15 * fontScaleMultiplier).sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Encrypted Local AES-256 Storage",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Ready Offline",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MetricStatPill(
                                    icon = Icons.Default.Quiz,
                                    label = "Downloaded MCQs",
                                    value = "$totalMcqsCount Qs",
                                    accentColor = Color(0xFFF59E0B)
                                )
                                MetricStatPill(
                                    icon = Icons.Default.Storage,
                                    label = "Vault Size",
                                    value = formattedStorageText,
                                    accentColor = Color(0xFF3B82F6)
                                )
                                MetricStatPill(
                                    icon = Icons.Default.Category,
                                    label = "Downloaded Packs",
                                    value = "${downloadedPacks.size} Items",
                                    accentColor = Color(0xFFA855F7)
                                )
                            }
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("Search downloaded subjects, units, topics...", fontSize = 13.sp, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.8f),
                        unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.6f),
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Empty State Handler
                if (filteredSubjects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No Matching Downloaded Content" else "No Materials Downloaded Yet",
                                    fontFamily = activeHeaderFont,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Download subjects, units, subtopics, and practice sets from the Syllabus Explorer to access MCQs offline anytime.",
                                    fontSize = 12.5.sp,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = onBack,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Browse Syllabus to Download", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Hierarchical LazyColumn list of Subject -> Unit -> Topic -> Sets
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredSubjects, key = { it.id }) { subject ->
                            val isSubjectExpanded = expandedSubjectIds.contains(subject.id)
                            val isSubjectDownloaded = remember(subject.id, downloadedPacks.size) {
                                OfflineSyncManager.isSubjectDownloaded(subject.id)
                            }

                            // Subject Item
                            SubjectVaultCard(
                                subject = subject,
                                isExpanded = isSubjectExpanded,
                                isDownloaded = isSubjectDownloaded,
                                activeHeaderFont = activeHeaderFont,
                                fontScale = fontScaleMultiplier,
                                onToggleExpand = {
                                    if (isSubjectExpanded) expandedSubjectIds.remove(subject.id)
                                    else expandedSubjectIds.add(subject.id)
                                },
                                onRemovePack = {
                                    OfflineSyncManager.removeSubject(context, subject.id)
                                    Toast.makeText(context, "${subject.name} removed from offline vault", Toast.LENGTH_SHORT).show()
                                },
                                onStartPractice = {
                                    onStartPractice(subject.id, "", "", "")
                                }
                            )

                            // Expanded Units under Subject
                            if (isSubjectExpanded) {
                                subject.units.forEach { unit ->
                                    val unitKey = "${subject.id}_${unit.id}"
                                    val isUnitExpanded = expandedUnitIds.contains(unitKey)
                                    val isUnitDownloaded = remember(subject.id, unit.id, downloadedPacks.size) {
                                        OfflineSyncManager.isUnitDownloaded(subject.id, unit.id)
                                    }

                                    UnitVaultCard(
                                        unit = unit,
                                        isExpanded = isUnitExpanded,
                                        isDownloaded = isUnitDownloaded,
                                        activeHeaderFont = activeHeaderFont,
                                        fontScale = fontScaleMultiplier,
                                        onToggleExpand = {
                                            if (isUnitExpanded) expandedUnitIds.remove(unitKey)
                                            else expandedUnitIds.add(unitKey)
                                        },
                                        onStartPractice = {
                                            onStartPractice(subject.id, unit.id, "", "")
                                        }
                                    )

                                    // Expanded Topics under Unit
                                    if (isUnitExpanded) {
                                        unit.topics.forEach { topic ->
                                            val topicKey = "${subject.id}_${unit.id}_${topic.id}"
                                            val isTopicExpanded = expandedTopicIds.contains(topicKey)
                                            val isTopicDownloaded = remember(subject.id, unit.id, topic.id, downloadedPacks.size) {
                                                OfflineSyncManager.isTopicDownloaded(subject.id, unit.id, topic.id)
                                            }

                                            TopicVaultCard(
                                                subjectId = subject.id,
                                                unitId = unit.id,
                                                topic = topic,
                                                isExpanded = isTopicExpanded,
                                                isDownloaded = isTopicDownloaded,
                                                activeHeaderFont = activeHeaderFont,
                                                fontScale = fontScaleMultiplier,
                                                onToggleExpand = {
                                                    if (isTopicExpanded) expandedTopicIds.remove(topicKey)
                                                    else expandedTopicIds.add(topicKey)
                                                },
                                                onDownloadClick = {
                                                    OfflineSyncManager.downloadTopic(
                                                        context = context,
                                                        subject = subject,
                                                        unit = unit,
                                                        topic = topic,
                                                        onComplete = {
                                                            Toast.makeText(context, "${topic.name} downloaded for offline practice!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    )
                                                },
                                                onStartPractice = {
                                                    onStartPractice(subject.id, unit.id, topic.id, "")
                                                }
                                            )

                                            // Expanded Practice Sets under Topic
                                            if (isTopicExpanded) {
                                                val setsList = listOf("Set 1", "Set 2", "Set 3", "Set 4")
                                                setsList.forEach { setName ->
                                                    val isSetDownloaded = remember(subject.id, unit.id, topic.id, setName, downloadedPacks.size) {
                                                        OfflineSyncManager.isSetDownloaded(subject.id, unit.id, topic.id, setName)
                                                    }

                                                    PracticeSetVaultCard(
                                                        subjectId = subject.id,
                                                        unitId = unit.id,
                                                        topicId = topic.id,
                                                        setName = setName,
                                                        isDownloaded = isSetDownloaded,
                                                        fontScale = fontScaleMultiplier,
                                                        onDownloadClick = {
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
                                                        onStartPractice = {
                                                            onStartPractice(subject.id, unit.id, topic.id, setName)
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
        }
    }
}

@Composable
private fun MetricStatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Text(text = label, fontSize = 9.5.sp, color = Color(0xFF94A3B8))
    }
}

@Composable
private fun SubjectVaultCard(
    subject: Subject,
    isExpanded: Boolean,
    isDownloaded: Boolean,
    activeHeaderFont: FontFamily,
    fontScale: Float,
    onToggleExpand: () -> Unit,
    onRemovePack: () -> Unit,
    onStartPractice: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, if (isExpanded) Color(0xFFF59E0B) else Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = subject.name,
                            fontFamily = activeHeaderFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = (15 * fontScale).sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${subject.units.size} Units • ${subject.units.sumOf { it.topics.size * 25 }} MCQs",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDownloaded) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF3B82F6).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (isDownloaded) "✓ Subject Vault Ready" else "Partial Offline",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDownloaded) Color(0xFF10B981) else Color(0xFF60A5FA),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand Units",
                            tint = Color.White
                        )
                    }
                    if (isDownloaded) {
                        IconButton(onClick = onRemovePack) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Remove Pack",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onStartPractice,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Practice Entire Subject Offline", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
            }
        }
    }
}

@Composable
private fun UnitVaultCard(
    unit: UnitModel,
    isExpanded: Boolean,
    isDownloaded: Boolean,
    activeHeaderFont: FontFamily,
    fontScale: Float,
    onToggleExpand: () -> Unit,
    onStartPractice: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 16.dp, top = 2.dp, bottom = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Unit ${unit.unitNumber}: ${unit.name}",
                            fontFamily = activeHeaderFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = (13.5 * fontScale).sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${unit.topics.size} Topics • ${unit.topics.size * 25} Questions",
                            fontSize = 10.5.sp,
                            color = Color.Gray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = if (isDownloaded) Color(0xFF10B981) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand Topics",
                            tint = Color.LightGray
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = onStartPractice,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF60A5FA)),
                    border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f))
                ) {
                    Text("Practice Unit ${unit.unitNumber} MCQs", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TopicVaultCard(
    subjectId: String,
    unitId: String,
    topic: TopicModel,
    isExpanded: Boolean,
    isDownloaded: Boolean,
    activeHeaderFont: FontFamily,
    fontScale: Float,
    onToggleExpand: () -> Unit,
    onDownloadClick: () -> Unit,
    onStartPractice: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 16.dp, top = 2.dp, bottom = 2.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, Color(0xFF475569))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.name,
                        fontFamily = activeHeaderFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = (12.5 * fontScale).sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Subtopic • 25 MCQs Offline Available",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDownloadClick) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                            contentDescription = "Download Subtopic",
                            tint = if (isDownloaded) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand Sets",
                            tint = Color.LightGray
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onStartPractice,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Practice Subtopic MCQs", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun PracticeSetVaultCard(
    subjectId: String,
    unitId: String,
    topicId: String,
    setName: String,
    isDownloaded: Boolean,
    fontScale: Float,
    onDownloadClick: () -> Unit,
    onStartPractice: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 64.dp, end = 16.dp, top = 2.dp, bottom = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.9f)),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Quiz,
                    contentDescription = null,
                    tint = Color(0xFFA855F7),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = setName,
                        fontWeight = FontWeight.Bold,
                        fontSize = (11.5 * fontScale).sp,
                        color = Color.White
                    )
                    Text(
                        text = "10 Qs • Offline Ready",
                        fontSize = 9.5.sp,
                        color = Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDownloadClick) {
                    Icon(
                        imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                        contentDescription = "Download Set",
                        tint = if (isDownloaded) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.clickable { onStartPractice() }
                ) {
                    Text(
                        text = "Practice",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
