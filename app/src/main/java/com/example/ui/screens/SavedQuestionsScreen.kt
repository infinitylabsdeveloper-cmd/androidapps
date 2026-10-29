package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
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
import com.example.model.Question

/**
 * Dedicated Luxury Palette for Bookmarked Questions:
 * Soft Champagne Pearl canvas with pure white cards, refined warm gold borders,
 * Imperial Emerald & Royal Amber jewel accents.
 * Strictly NO dark mode surfaces and NO cold grey tones.
 */
private object LuxuryTheme {
    val ScreenBg = Color(0xFFFAF7F2)          // Soft Champagne Pearl base
    val CardBg = Color(0xFFFFFFFF)            // Pure Crisp White
    val CardBorder = Color(0xFFE8D8B8)        // Refined Warm Gold border
    val CardBorderSubtle = Color(0xFFF2E6D4)  // Delicate Champagne divider
    val HighlightGold = Color(0xFFD4AF37)     // Metallic Warm Gold accent

    val TextPrimary = Color(0xFF2A1E17)       // Deep Warm Espresso Ink
    val TextSecondary = Color(0xFF7A6A5D)     // Warm Muted Bronze / Taupe
    val TextTertiary = Color(0xFF9E8B7C)      // Soft Warm Sand

    val AccentEmerald = Color(0xFF047857)     // Imperial Emerald Green
    val AccentEmeraldLight = Color(0xFFECFDF5)// Soft Mint Alabaster
    val AccentEmeraldBorder = Color(0xFFA7F3D0)// Emerald Mint Outline
    val AccentEmeraldDark = Color(0xFF064E3B) // Deep Forest Green

    val AccentAmber = Color(0xFFD97706)       // Royal Amber
    val AccentAmberLight = Color(0xFFFFFBEB)  // Soft Warm Buttercream
    val AccentAmberBorder = Color(0xFFFDE68A) // Amber Gold Outline
    val AccentAmberDark = Color(0xFFB45309)   // Deep Golden Bronze

    val DeleteRed = Color(0xFFDC2626)         // Crimson Rose for Delete
    val DeleteRedLight = Color(0xFFFEF2F2)
}

data class SubjectPalette(
    val gradientStart: Color,
    val gradientEnd: Color,
    val accentColor: Color,
    val textColor: Color,
    val icon: ImageVector,
    val iconBgColor: Color,
    val cardBgColor: Color
)

fun getSubjectPalette(subjectId: String): SubjectPalette {
    return when (subjectId.lowercase()) {
        "subj-history", "history", "subj-ap-history", "ap-history", "subj-indian-history" -> SubjectPalette(
            gradientStart = Color(0xFFFFF7ED),
            gradientEnd = Color(0xFFFFEDD5),
            accentColor = Color(0xFFC2410C),
            textColor = Color(0xFF9A3412),
            icon = Icons.Default.History,
            iconBgColor = Color(0xFFFFEDD5),
            cardBgColor = LuxuryTheme.CardBg
        )
        "subj-polity", "polity", "subj-constitution", "constitution" -> SubjectPalette(
            gradientStart = Color(0xFFF0F7FF),
            gradientEnd = Color(0xFFE0EFFF),
            accentColor = Color(0xFF1D4ED8),
            textColor = Color(0xFF1E40AF),
            icon = Icons.Default.Gavel,
            iconBgColor = Color(0xFFDBEAFE),
            cardBgColor = LuxuryTheme.CardBg
        )
        "subj-geography", "geography" -> SubjectPalette(
            gradientStart = Color(0xFFF0FDF4),
            gradientEnd = Color(0xFFDCFCE7),
            accentColor = Color(0xFF047857),
            textColor = Color(0xFF065F46),
            icon = Icons.Default.Public,
            iconBgColor = Color(0xFFD1FAE5),
            cardBgColor = LuxuryTheme.CardBg
        )
        "subj-economy", "economy" -> SubjectPalette(
            gradientStart = Color(0xFFFAF5FF),
            gradientEnd = Color(0xFFF3E8FF),
            accentColor = Color(0xFF7C3AED),
            textColor = Color(0xFF5B21B6),
            icon = Icons.Default.TrendingUp,
            iconBgColor = Color(0xFFEDE9FE),
            cardBgColor = LuxuryTheme.CardBg
        )
        "subj-science", "science", "subj-science-tech", "science-tech" -> SubjectPalette(
            gradientStart = Color(0xFFF0FDFA),
            gradientEnd = Color(0xFFCCFBF1),
            accentColor = Color(0xFF0F766E),
            textColor = Color(0xFF115E59),
            icon = Icons.Default.Science,
            iconBgColor = Color(0xFF99F6E4),
            cardBgColor = LuxuryTheme.CardBg
        )
        "subj-mental-ability", "mental-ability" -> SubjectPalette(
            gradientStart = Color(0xFFFFFBEB),
            gradientEnd = Color(0xFFFEF3C7),
            accentColor = Color(0xFFD97706),
            textColor = Color(0xFF92400E),
            icon = Icons.Default.Psychology,
            iconBgColor = Color(0xFFFDE68A),
            cardBgColor = LuxuryTheme.CardBg
        )
        "subj-current-affairs", "current-affairs" -> SubjectPalette(
            gradientStart = Color(0xFFFFF1F2),
            gradientEnd = Color(0xFFFFE4E6),
            accentColor = Color(0xFFBE123C),
            textColor = Color(0xFF9F1239),
            icon = Icons.Default.Newspaper,
            iconBgColor = Color(0xFFFECDD3),
            cardBgColor = LuxuryTheme.CardBg
        )
        "subj-society", "society" -> SubjectPalette(
            gradientStart = Color(0xFFF0FDF4),
            gradientEnd = Color(0xFFDCFCE7),
            accentColor = Color(0xFF15803D),
            textColor = Color(0xFF166534),
            icon = Icons.Default.People,
            iconBgColor = Color(0xFFBBF7D0),
            cardBgColor = LuxuryTheme.CardBg
        )
        else -> SubjectPalette(
            gradientStart = Color(0xFFFFFBEB),
            gradientEnd = Color(0xFFFEF3C7),
            accentColor = Color(0xFFB45309),
            textColor = Color(0xFF78350F),
            icon = Icons.Default.MenuBook,
            iconBgColor = Color(0xFFFDE68A),
            cardBgColor = LuxuryTheme.CardBg
        )
    }
}

private fun getSubjectDisplayName(subjectId: String): String {
    return when (subjectId.lowercase()) {
        "subj-history", "history", "subj-indian-history" -> "Indian History"
        "subj-society", "society" -> "Indian Society"
        "subj-geography", "geography" -> "Geography"
        "subj-current-affairs", "current-affairs" -> "Current Affairs"
        "subj-polity", "polity", "subj-constitution", "constitution" -> "Indian Polity"
        "subj-economy", "economy" -> "Indian Economy"
        "subj-science", "science", "subj-science-tech", "science-tech" -> "Science & Tech"
        "subj-ap-history" -> "AP History"
        "subj-mental-ability" -> "Mental Ability"
        "all" -> "All Subjects"
        else -> subjectId.replace("subj-", "").replace("-", " ").split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedQuestionsScreen(
    savedQuestions: List<Question>,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onRemoveBookmark: (String) -> Unit,
    onPracticeSaved: (List<Question>, String, Int) -> Unit,
    onNavigateToExplore: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf("all") }
    var selectedTopicName by remember { mutableStateOf("all") }
    var viewMode by remember { mutableStateOf("hierarchy") } // "hierarchy" or "flat"
    var expandedQuestionIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var expandedSubjects by remember { mutableStateOf<Set<String>>(emptySet()) }
    var expandedTopics by remember { mutableStateOf<Set<String>>(emptySet()) }
    var questionToDelete by remember { mutableStateOf<Question?>(null) }

    val subjectsInBookmarks = remember(savedQuestions) {
        savedQuestions.map { it.subjectId }.distinct().filter { it.isNotBlank() }
    }

    val topicsInSelectedSubject = remember(savedQuestions, selectedSubjectId) {
        savedQuestions.filter { q ->
            selectedSubjectId == "all" || q.subjectId.equals(selectedSubjectId, ignoreCase = true)
        }.map { q ->
            if (q.subtopic.isNotBlank()) q.subtopic 
            else if (q.topicId.isNotBlank()) q.topicId 
            else "General Bookmarks"
        }.distinct().filter { it.isNotBlank() }
    }

    // Reset selected topic if it doesn't belong to the newly selected subject
    LaunchedEffect(selectedSubjectId) {
        selectedTopicName = "all"
    }

    val filteredQuestions = remember(savedQuestions, searchQuery, selectedSubjectId, selectedTopicName) {
        savedQuestions.filter { q ->
            val matchesSubject = selectedSubjectId == "all" || q.subjectId.equals(selectedSubjectId, ignoreCase = true)
            val topicName = if (q.subtopic.isNotBlank()) q.subtopic else if (q.topicId.isNotBlank()) q.topicId else "General Bookmarks"
            val matchesTopic = selectedTopicName == "all" || topicName.equals(selectedTopicName, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    q.questionText.contains(searchQuery, ignoreCase = true) ||
                    q.subtopic.contains(searchQuery, ignoreCase = true) ||
                    q.topicId.contains(searchQuery, ignoreCase = true) ||
                    q.options.values.any { it.contains(searchQuery, ignoreCase = true) }
            matchesSubject && matchesTopic && matchesSearch
        }
    }

    // Expand all subjects and topics on load/search change automatically for instant view
    LaunchedEffect(filteredQuestions) {
        expandedSubjects = filteredQuestions.map { it.subjectId }.toSet()
        expandedTopics = filteredQuestions.map { "${it.subjectId}_${if (it.subtopic.isNotBlank()) it.subtopic else if (it.topicId.isNotBlank()) it.topicId else "General Bookmarks"}" }.toSet()
    }

    if (questionToDelete != null) {
        val target = questionToDelete!!
        AlertDialog(
            onDismissRequest = { questionToDelete = null },
            title = {
                Text(
                    text = "Remove Bookmark?",
                    fontWeight = FontWeight.Black,
                    color = LuxuryTheme.TextPrimary
                )
            },
            text = {
                Text(
                    text = "Remove this high-yield MCQ from your revision bank?",
                    color = LuxuryTheme.TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveBookmark(target.id)
                        questionToDelete = null
                        Toast.makeText(context, "Bookmark removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryTheme.DeleteRed)
                ) {
                    Text("Remove", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { questionToDelete = null }) {
                    Text("Cancel", color = LuxuryTheme.TextSecondary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = LuxuryTheme.CardBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryTheme.ScreenBg)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = LuxuryTheme.AccentAmberLight,
                                    border = BorderStroke(1.dp, LuxuryTheme.AccentAmberBorder),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = LuxuryTheme.AccentAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Premium Revision Hub",
                                    color = LuxuryTheme.TextPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    letterSpacing = (-0.3).sp
                                )
                            }
                            Text(
                                text = "${savedQuestions.size} Bookmarked Questions • 100% Offline Ready",
                                color = LuxuryTheme.AccentEmerald,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = LuxuryTheme.TextPrimary
                                )
                            }
                            IconButton(onClick = onNavigateToHome) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home",
                                    tint = LuxuryTheme.AccentEmerald
                                )
                            }
                        }
                    },
                    actions = {
                        Surface(
                            onClick = {
                                viewMode = if (viewMode == "hierarchy") "flat" else "hierarchy"
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (viewMode == "hierarchy") LuxuryTheme.AccentEmerald else LuxuryTheme.CardBg,
                            border = BorderStroke(1.dp, if (viewMode == "hierarchy") LuxuryTheme.AccentEmerald else LuxuryTheme.CardBorder),
                            shadowElevation = 1.dp,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (viewMode == "hierarchy") Icons.Default.AccountTree else Icons.Default.List,
                                    contentDescription = null,
                                    tint = if (viewMode == "hierarchy") Color.White else LuxuryTheme.AccentEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (viewMode == "hierarchy") "Hierarchy" else "Flat List",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (viewMode == "hierarchy") Color.White else LuxuryTheme.AccentEmerald
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = LuxuryTheme.ScreenBg)
                )
            },
            bottomBar = {
                if (filteredQuestions.isNotEmpty()) {
                    Surface(
                        color = LuxuryTheme.CardBg,
                        tonalElevation = 6.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, LuxuryTheme.CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${filteredQuestions.size} Questions Selected",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.5.sp,
                                    color = LuxuryTheme.TextPrimary
                                )
                                Text(
                                    text = "Ready for focused revision",
                                    fontSize = 11.sp,
                                    color = LuxuryTheme.TextSecondary
                                )
                            }

                            Button(
                                onClick = {
                                    onPracticeSaved(filteredQuestions, "Bookmarked Questions Practice", 0)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryTheme.AccentEmerald),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 9.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = LuxuryTheme.AccentAmberLight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Practice Selected", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }
                }
            },
            containerColor = LuxuryTheme.ScreenBg
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(LuxuryTheme.ScreenBg)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                item(key = "header_controls") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // LUXURIOUS HEADER BANNER
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = LuxuryTheme.CardBg),
                            border = BorderStroke(1.2.dp, LuxuryTheme.CardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFFFFDF9), Color(0xFFFBF4E8))
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "🎯 MCQ REVISION HUB",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp,
                                                color = LuxuryTheme.TextPrimary,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "Tap any question to launch full-screen practice with complete explanations.",
                                            fontSize = 11.5.sp,
                                            color = LuxuryTheme.TextSecondary,
                                            lineHeight = 16.sp
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = LuxuryTheme.AccentEmeraldLight,
                                        border = BorderStroke(1.dp, LuxuryTheme.AccentEmeraldBorder),
                                        modifier = Modifier.padding(start = 12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "${savedQuestions.size}",
                                                fontSize = 19.sp,
                                                fontWeight = FontWeight.Black,
                                                color = LuxuryTheme.AccentEmeraldDark
                                            )
                                            Text(
                                                text = "SAVED",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Black,
                                                color = LuxuryTheme.AccentEmerald
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // SEARCH BAR
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search among bookmarked questions...",
                                    fontSize = 13.sp,
                                    color = LuxuryTheme.TextTertiary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = LuxuryTheme.AccentAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = LuxuryTheme.TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LuxuryTheme.CardBg,
                                unfocusedContainerColor = LuxuryTheme.CardBg,
                                focusedBorderColor = LuxuryTheme.AccentAmber,
                                unfocusedBorderColor = LuxuryTheme.CardBorder,
                                focusedTextColor = LuxuryTheme.TextPrimary,
                                unfocusedTextColor = LuxuryTheme.TextPrimary
                            ),
                            singleLine = true
                        )

                        // SUBJECT HORIZONTAL FILTER ROW
                        Text(
                            text = "FILTER BY SUBJECT",
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            color = LuxuryTheme.TextSecondary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedSubjectId == "all",
                                    onClick = { selectedSubjectId = "all" },
                                    label = { Text("All Subjects", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LuxuryTheme.AccentEmerald,
                                        selectedLabelColor = Color.White,
                                        containerColor = LuxuryTheme.CardBg,
                                        labelColor = LuxuryTheme.TextSecondary
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selectedSubjectId == "all") LuxuryTheme.AccentEmerald else LuxuryTheme.CardBorder
                                    )
                                )
                            }

                            items(subjectsInBookmarks) { subjectId ->
                                val palette = getSubjectPalette(subjectId)
                                val isSelected = selectedSubjectId.equals(subjectId, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSubjectId = subjectId },
                                    label = { Text(getSubjectDisplayName(subjectId), fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = palette.accentColor,
                                        selectedLabelColor = Color.White,
                                        containerColor = palette.gradientStart,
                                        labelColor = if (isSelected) Color.White else palette.textColor
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) palette.accentColor else palette.accentColor.copy(alpha = 0.35f)
                                    )
                                )
                            }
                        }

                        // TOPIC HORIZONTAL FILTER ROW (Visible if any subject has topics)
                        if (topicsInSelectedSubject.isNotEmpty()) {
                            Text(
                                text = "FILTER BY TOPIC",
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                color = LuxuryTheme.TextSecondary,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedTopicName == "all",
                                        onClick = { selectedTopicName = "all" },
                                        label = { Text("All Topics", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = LuxuryTheme.AccentAmberDark,
                                            selectedLabelColor = Color.White,
                                            containerColor = LuxuryTheme.CardBg,
                                            labelColor = LuxuryTheme.TextSecondary
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (selectedTopicName == "all") LuxuryTheme.AccentAmberDark else LuxuryTheme.CardBorder
                                        )
                                    )
                                }

                                items(topicsInSelectedSubject) { topicName ->
                                    val isSelected = selectedTopicName.equals(topicName, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedTopicName = topicName },
                                        label = { Text(topicName, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = LuxuryTheme.AccentEmerald,
                                            selectedLabelColor = Color.White,
                                            containerColor = LuxuryTheme.AccentEmeraldLight,
                                            labelColor = if (isSelected) Color.White else LuxuryTheme.AccentEmeraldDark
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) LuxuryTheme.AccentEmerald else LuxuryTheme.AccentEmeraldBorder
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // CONTENT AREA
                if (filteredQuestions.isEmpty()) {
                    item(key = "empty_state") {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = LuxuryTheme.CardBg),
                            border = BorderStroke(1.2.dp, LuxuryTheme.CardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = LuxuryTheme.AccentAmberLight,
                                    border = BorderStroke(1.dp, LuxuryTheme.AccentAmberBorder),
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = LuxuryTheme.AccentAmber,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No bookmarked questions match your criteria.",
                                    fontWeight = FontWeight.Black,
                                    color = LuxuryTheme.TextPrimary,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Try adjusting your search query or selecting another subject filter.",
                                    fontSize = 12.5.sp,
                                    color = LuxuryTheme.TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    if (viewMode == "hierarchy") {
                        // SUBJECTS > TOPICS > QUESTIONS HIERARCHY VIEW
                        val questionsBySubject = filteredQuestions.groupBy { it.subjectId }

                        questionsBySubject.forEach { (subjectId, subjectQuestions) ->
                            val subjectName = getSubjectDisplayName(subjectId)
                            val isSubjectExpanded = expandedSubjects.contains(subjectId)
                            val palette = getSubjectPalette(subjectId)

                            item(key = "subj_$subjectId") {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = LuxuryTheme.CardBg),
                                    border = BorderStroke(1.2.dp, LuxuryTheme.CardBorder),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        // Subject Header Row
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    expandedSubjects = if (isSubjectExpanded) {
                                                        expandedSubjects - subjectId
                                                    } else {
                                                        expandedSubjects + subjectId
                                                    }
                                                }
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = listOf(palette.gradientStart, palette.gradientEnd)
                                                    )
                                                )
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = palette.iconBgColor,
                                                    border = BorderStroke(1.dp, palette.accentColor.copy(alpha = 0.3f)),
                                                    modifier = Modifier.size(38.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = palette.icon,
                                                            contentDescription = null,
                                                            tint = palette.accentColor,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = subjectName,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 15.sp,
                                                        color = LuxuryTheme.TextPrimary
                                                    )
                                                    Text(
                                                        text = "${subjectQuestions.size} Bookmarks Available",
                                                        fontSize = 11.5.sp,
                                                        color = palette.textColor,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                OutlinedButton(
                                                    onClick = {
                                                        onPracticeSaved(subjectQuestions, "$subjectName Revision Practice", 0)
                                                    },
                                                    border = BorderStroke(1.dp, palette.accentColor),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.accentColor),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Practice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = if (isSubjectExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = null,
                                                    tint = LuxuryTheme.TextSecondary
                                                )
                                            }
                                        }

                                        // Topics under Subject
                                        AnimatedVisibility(visible = isSubjectExpanded) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(LuxuryTheme.ScreenBg)
                                                    .padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                val questionsByTopic = subjectQuestions.groupBy { 
                                                    if (it.subtopic.isNotBlank()) it.subtopic 
                                                    else if (it.topicId.isNotBlank()) it.topicId 
                                                    else "General Bookmarks" 
                                                }

                                                questionsByTopic.forEach { (topicName, topicQuestions) ->
                                                    val topicKey = "${subjectId}_$topicName"
                                                    val isTopicExpanded = expandedTopics.contains(topicKey)

                                                    Card(
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = CardDefaults.cardColors(containerColor = LuxuryTheme.CardBg),
                                                        border = BorderStroke(1.dp, LuxuryTheme.CardBorderSubtle),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Column {
                                                            // Topic Header Row
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clickable {
                                                                        expandedTopics = if (isTopicExpanded) {
                                                                            expandedTopics - topicKey
                                                                        } else {
                                                                            expandedTopics + topicKey
                                                                        }
                                                                    }
                                                                    .padding(12.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    modifier = Modifier.weight(1f)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Bookmark,
                                                                        contentDescription = null,
                                                                        tint = palette.accentColor,
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                    Text(
                                                                        text = topicName,
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 13.sp,
                                                                        color = LuxuryTheme.TextPrimary
                                                                    )
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Surface(
                                                                        shape = RoundedCornerShape(6.dp),
                                                                        color = palette.iconBgColor,
                                                                        border = BorderStroke(1.dp, palette.accentColor.copy(alpha = 0.3f))
                                                                    ) {
                                                                        Text(
                                                                            text = "${topicQuestions.size}",
                                                                            fontSize = 10.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            color = palette.textColor,
                                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                        )
                                                                    }
                                                                }

                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    TextButton(
                                                                        onClick = {
                                                                            onPracticeSaved(topicQuestions, "$topicName Revision", 0)
                                                                        },
                                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                                    ) {
                                                                        Text(
                                                                            "Practice (${topicQuestions.size})",
                                                                            fontSize = 11.sp,
                                                                            fontWeight = FontWeight.Black,
                                                                            color = palette.accentColor
                                                                        )
                                                                    }
                                                                    Icon(
                                                                        imageVector = if (isTopicExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                                        contentDescription = null,
                                                                        tint = LuxuryTheme.TextSecondary,
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }

                                                            // Questions under Topic
                                                            AnimatedVisibility(visible = isTopicExpanded) {
                                                                Column(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .padding(8.dp),
                                                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                                                ) {
                                                                    topicQuestions.forEach { question ->
                                                                        val isExpanded = expandedQuestionIds.contains(question.id)
                                                                        val targetIdx = filteredQuestions.indexOfFirst { it.id == question.id }.coerceAtLeast(0)
                                                                        SavedQuestionCard(
                                                                            question = question,
                                                                            isExpanded = isExpanded,
                                                                            onToggleExpand = {
                                                                                expandedQuestionIds = if (isExpanded) {
                                                                                    expandedQuestionIds - question.id
                                                                                } else {
                                                                                    expandedQuestionIds + question.id
                                                                                }
                                                                            },
                                                                            onPracticeSingle = {
                                                                                onPracticeSaved(filteredQuestions, "Bookmarked Questions Practice", targetIdx)
                                                                            },
                                                                            onDeleteClick = {
                                                                                questionToDelete = question
                                                                            },
                                                                            onShareClick = {
                                                                                shareQuestion(question, context)
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
                    } else {
                        // FLAT LIST VIEW
                        items(filteredQuestions, key = { it.id }) { question ->
                            val isExpanded = expandedQuestionIds.contains(question.id)
                            val targetIdx = filteredQuestions.indexOfFirst { it.id == question.id }.coerceAtLeast(0)

                            SavedQuestionCard(
                                question = question,
                                isExpanded = isExpanded,
                                onToggleExpand = {
                                    expandedQuestionIds = if (isExpanded) {
                                        expandedQuestionIds - question.id
                                    } else {
                                        expandedQuestionIds + question.id
                                    }
                                },
                                onPracticeSingle = {
                                    onPracticeSaved(filteredQuestions, "Bookmarked Questions Practice", targetIdx)
                                },
                                onDeleteClick = {
                                    questionToDelete = question
                                },
                                onShareClick = {
                                    shareQuestion(question, context)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun shareQuestion(question: Question, context: android.content.Context) {
    val shareBody = buildString {
        append("📌 APPSC Group 2 MCQ Practice\n\n")
        append(question.questionText)
        append("\n\n")
        append("A) ${question.options["A"]}\n")
        append("B) ${question.options["B"]}\n")
        append("C) ${question.options["C"]}\n")
        append("D) ${question.options["D"]}\n\n")
        append("✅ Correct Answer: Option ${question.correctOption}\n")
        if (!question.explanation.isNullOrBlank()) {
            append("📖 Explanation: ${question.explanation}\n")
        }
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "APPSC Group 2 Saved MCQ")
        putExtra(Intent.EXTRA_TEXT, shareBody)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Question"))
}

@Composable
private fun SavedQuestionCard(
    question: Question,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onPracticeSingle: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val palette = getSubjectPalette(question.subjectId)

    Card(
        onClick = onPracticeSingle,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LuxuryTheme.CardBg),
        border = BorderStroke(1.2.dp, LuxuryTheme.CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val label = if (question.subtopic.isNotBlank()) question.subtopic else getSubjectDisplayName(question.subjectId)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = palette.gradientStart,
                        border = BorderStroke(1.dp, palette.accentColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = label,
                            color = palette.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = LuxuryTheme.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Remove Bookmark",
                            tint = LuxuryTheme.AccentAmberDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = question.questionText,
                color = LuxuryTheme.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("A", "B", "C", "D").forEach { optKey ->
                    val optText = question.options[optKey] ?: ""
                    if (optText.isNotBlank()) {
                        val isCorrect = isExpanded && optKey.equals(question.correctOption, ignoreCase = true)
                        
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCorrect) LuxuryTheme.AccentEmeraldLight else LuxuryTheme.CardBg,
                            border = BorderStroke(
                                if (isCorrect) 1.5.dp else 1.dp,
                                if (isCorrect) LuxuryTheme.AccentEmerald else LuxuryTheme.CardBorderSubtle
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isCorrect) LuxuryTheme.AccentEmerald else Color(0xFFFBF4E8),
                                    border = BorderStroke(1.dp, if (isCorrect) LuxuryTheme.AccentEmerald else LuxuryTheme.CardBorder),
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = optKey,
                                            color = if (isCorrect) Color.White else LuxuryTheme.TextPrimary,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = optText,
                                    color = if (isCorrect) LuxuryTheme.AccentEmeraldDark else LuxuryTheme.TextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Correct Answer",
                                        tint = LuxuryTheme.AccentEmerald,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val hasValidExplanation = !question.explanation.isNullOrBlank() &&
                    question.explanation.trim().lowercase() != "no explanation available for this question." &&
                    question.explanation.trim().lowercase() != "no explanation available."

            if (hasValidExplanation) {
                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(LuxuryTheme.AccentEmeraldLight)
                            .border(1.dp, LuxuryTheme.AccentEmeraldBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = LuxuryTheme.AccentAmber,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Detailed Explanation / వివరణ",
                                color = LuxuryTheme.AccentEmeraldDark,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = question.explanation,
                            color = LuxuryTheme.TextPrimary,
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = if (hasValidExplanation) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasValidExplanation) {
                    TextButton(
                        onClick = onToggleExpand,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = LuxuryTheme.AccentAmberDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isExpanded) "Hide Solution" else "View Solution",
                            fontSize = 11.5.sp,
                            color = LuxuryTheme.AccentAmberDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = onPracticeSingle,
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryTheme.AccentEmerald),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = LuxuryTheme.AccentAmberLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Practice", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
