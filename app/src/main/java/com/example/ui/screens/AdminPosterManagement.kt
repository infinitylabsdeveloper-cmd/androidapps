package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.SampleData
import com.example.model.PromotionalPosterModel
import com.example.model.PromotionStatus
import com.example.model.Subject
import com.example.ui.components.ImageUploadGuidelinesDialog
import com.example.ui.components.PosterFhdCardImage
import com.example.ui.components.RecommendedImageSizeHelper
import com.example.ui.components.UploadImageType
import com.example.util.ImageStorageManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Destination categories for promotional posters
 */
enum class PosterDestinationType(
    val title: String,
    val subtitle: String,
    val destinationKey: String,
    val posterTypeTag: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val primaryColor: Color,
    val lightBg: Color
) {
    MOCK_TESTS(
        title = "Mock Tests Series",
        subtitle = "Directs students directly to full-length & sectional mock tests",
        destinationKey = "mock_tests",
        posterTypeTag = "Exam Update",
        icon = Icons.Default.School,
        primaryColor = Color(0xFFEA580C),
        lightBg = Color(0xFFFFEDD5)
    ),
    SUBSCRIPTION(
        title = "Subscription & Pro Plans",
        subtitle = "Directs students to membership tiers, pass discounts & offers",
        destinationKey = "subscription",
        posterTypeTag = "Subscription Poster",
        icon = Icons.Default.WorkspacePremium,
        primaryColor = Color(0xFF2563EB),
        lightBg = Color(0xFFDBEAFE)
    ),
    DAILY_QUIZ(
        title = "Daily Speed Quiz",
        subtitle = "Directs students to today's speed quiz arena",
        destinationKey = "daily_quiz",
        posterTypeTag = "Daily Quiz",
        icon = Icons.Default.Quiz,
        primaryColor = Color(0xFF7C3AED),
        lightBg = Color(0xFFEDE9FE)
    ),
    CURRENT_AFFAIRS(
        title = "Current Affairs Capsule",
        subtitle = "Directs students to monthly & weekly current affairs digest",
        destinationKey = "current_affairs",
        posterTypeTag = "Current Affairs Poster",
        icon = Icons.Default.Article,
        primaryColor = Color(0xFF0D9488),
        lightBg = Color(0xFFCCFBF1)
    ),
    SUBJECT(
        title = "Subject Syllabus & MCQs",
        subtitle = "Directs students to a specific subject module",
        destinationKey = "subject",
        posterTypeTag = "Study Material Banner",
        icon = Icons.Default.Book,
        primaryColor = Color(0xFF059669),
        lightBg = Color(0xFFD1FAE5)
    )
}

/**
 * Modern, Full-Featured Poster Management System
 */
@Composable
fun AdminPosterManagement(
    onNavigateToSubscription: () -> Unit = {},
    onSwitchToUserHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val posters = SampleData.postersState

    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    var showCreateOrEditDialog by remember { mutableStateOf(false) }
    var posterToEdit by remember { mutableStateOf<PromotionalPosterModel?>(null) }
    var posterToPreview by remember { mutableStateOf<PromotionalPosterModel?>(null) }
    var posterToDelete by remember { mutableStateOf<PromotionalPosterModel?>(null) }
    var showGlobalGuidelines by remember { mutableStateOf(false) }

    if (showGlobalGuidelines) {
        ImageUploadGuidelinesDialog(
            currentSpec = UploadImageType.POSTER_BANNER,
            onDismiss = { showGlobalGuidelines = false }
        )
    }

    // Filter posters based on selected tab and search, sorted by priority
    val filteredPosters = remember(posters.size, selectedFilter, searchQuery, posters.map { it.isActive }, posters.map { it.priorityOrder }) {
        posters.filter { poster ->
            val matchesFilter = when (selectedFilter) {
                "Active" -> poster.isActive
                "Inactive" -> !poster.isActive
                "Mock Tests" -> poster.destinationPage.contains("mock", ignoreCase = true) || poster.destinationPage.contains("test", ignoreCase = true)
                "Subscription" -> poster.destinationPage.contains("sub", ignoreCase = true) || poster.destinationPage.contains("plan", ignoreCase = true)
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                poster.title.contains(searchQuery, ignoreCase = true) ||
                        poster.promotionalText.contains(searchQuery, ignoreCase = true) ||
                        poster.offerDetails.contains(searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }.sortedBy { it.priorityOrder }
    }

    val totalCount = posters.size
    val activeCount = posters.count { it.isActive }
    val mockTestsCount = posters.count { it.destinationPage.contains("mock", ignoreCase = true) || it.destinationPage.contains("test", ignoreCase = true) }
    val subscriptionCount = posters.count { it.destinationPage.contains("sub", ignoreCase = true) || it.destinationPage.contains("plan", ignoreCase = true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header with Stats & "Create New Poster" Button
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEFF6FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Poster Management",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = (-0.5).sp
                                )
                            }
                            Text(
                                text = "Centralized hub to manage all banners and promotional posters across all apps",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showGlobalGuidelines = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF2563EB))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Guidelines", fontSize = 12.sp, color = Color(0xFF2563EB))
                            }

                            Button(
                                onClick = {
                                    posterToEdit = null
                                    showCreateOrEditDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Poster", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metrics Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricMiniCard(
                            title = "Total",
                            count = totalCount.toString(),
                            color = Color(0xFF475569),
                            bgColor = Color(0xFFF1F5F9),
                            modifier = Modifier.weight(1f)
                        )
                        MetricMiniCard(
                            title = "Active",
                            count = activeCount.toString(),
                            color = Color(0xFF16A34A),
                            bgColor = Color(0xFFDCFCE7),
                            modifier = Modifier.weight(1f)
                        )
                        MetricMiniCard(
                            title = "Mock Tests",
                            count = mockTestsCount.toString(),
                            color = Color(0xFFEA580C),
                            bgColor = Color(0xFFFFEDD5),
                            modifier = Modifier.weight(1f)
                        )
                        MetricMiniCard(
                            title = "Pro Pass",
                            count = subscriptionCount.toString(),
                            color = Color(0xFF2563EB),
                            bgColor = Color(0xFFDBEAFE),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Search & Filter Tabs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search banners by title, coupon or description...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Filter Chips
                val filterOptions = listOf("All", "Active", "Mock Tests", "Subscription", "Inactive")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterOptions) { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = when (filter) {
                                        "All" -> "All Posters ($totalCount)"
                                        "Active" -> "Active ($activeCount)"
                                        "Mock Tests" -> "Mock Tests ($mockTestsCount)"
                                        "Subscription" -> "Subscription ($subscriptionCount)"
                                        "Inactive" -> "Disabled (${totalCount - activeCount})"
                                        else -> filter
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1E293B),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF475569)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFF1E293B) else Color(0xFFCBD5E1)
                            )
                        )
                    }
                }
            }
        }

        // 3. Posters List
        if (filteredPosters.isEmpty()) {
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Banners Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try changing your search query" else "Click '+ New Poster' above to add one",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        } else {
            items(filteredPosters, key = { it.id }) { poster ->
                PosterManagementCard(
                    poster = poster,
                    onToggleActive = { isChecked ->
                        val idx = SampleData.postersState.indexOfFirst { it.id == poster.id }
                        if (idx != -1) {
                            SampleData.postersState[idx] = poster.copy(isActive = isChecked)
                            Toast.makeText(
                                context,
                                if (isChecked) "Banner activated on Home carousel" else "Banner deactivated",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onPreview = {
                        posterToPreview = poster
                    },
                    onEdit = {
                        posterToEdit = poster
                        showCreateOrEditDialog = true
                    },
                    onDelete = {
                        posterToDelete = poster
                    },
                    onMoveUp = {
                        SampleData.reorderPosterMoveUp(poster.id)
                    },
                    onMoveDown = {
                        SampleData.reorderPosterMoveDown(poster.id)
                    }
                )
            }
        }
    }

    // =========================================================================
    // DIALOGS: Create/Edit Poster, Live Preview, and Delete Confirmation
    // =========================================================================

    // 1. Create or Edit Poster Dialog
    if (showCreateOrEditDialog) {
        PosterEditorDialog(
            existingPoster = posterToEdit,
            onDismiss = {
                showCreateOrEditDialog = false
                posterToEdit = null
            },
            onSave = { updatedPoster ->
                SampleData.publishPosterToApps(updatedPoster, updatedPoster.mappedAppIds)
                Toast.makeText(context, "Poster published to ${updatedPoster.mappedAppIds.size} app(s)! 🚀", Toast.LENGTH_SHORT).show()
                showCreateOrEditDialog = false
                posterToEdit = null
            },
            onTestRedirection = { destination ->
                when {
                    destination.contains("mock", ignoreCase = true) -> {
                        Toast.makeText(context, "Note: Mock Test management has been removed from Admin Panel. This will work in User App.", Toast.LENGTH_LONG).show()
                    }
                    destination.contains("sub", ignoreCase = true) -> onNavigateToSubscription()
                    else -> onSwitchToUserHome()
                }
            }
        )
    }

    // 2. Full Student Carousel Live Preview Modal
    if (posterToPreview != null) {
        val previewItem = posterToPreview!!
        Dialog(
            onDismissRequest = { posterToPreview = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .padding(vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Student Preview", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                                Text("Exact view rendered on Home Screen", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }

                        IconButton(onClick = { posterToPreview = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                        }
                    }

                    // Render the simulated Student Home Screen card
                    InteractiveStudentPosterCard(
                        poster = previewItem,
                        onTestClick = {
                            val destName = when {
                                previewItem.destinationPage.contains("mock", ignoreCase = true) -> "🎯 Mock Tests"
                                previewItem.destinationPage.contains("sub", ignoreCase = true) -> "👑 Subscription & Pro Plans"
                                else -> "📚 ${previewItem.destinationPage}"
                            }
                            Toast.makeText(context, "✅ Verified! Clicking this poster redirects to: $destName", Toast.LENGTH_LONG).show()
                        }
                    )

                    // Destination Info Card
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Target Redirection Details", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Destination Key: ", fontSize = 12.sp, color = Color(0xFF64748B))
                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        previewItem.destinationPage,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2563EB),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Target Screen: ", fontSize = 12.sp, color = Color(0xFF64748B))
                                val screenLabel = when {
                                    previewItem.destinationPage.contains("mock", ignoreCase = true) -> "Mock Tests Arena 🎯"
                                    previewItem.destinationPage.contains("sub", ignoreCase = true) -> "Subscription & Pricing Plans 👑"
                                    else -> "Subject / Feature Screen 📚"
                                }
                                Text(screenLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { posterToPreview = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Dismiss")
                        }

                        Button(
                            onClick = {
                                posterToPreview = null
                                when {
                                    previewItem.destinationPage.contains("mock", ignoreCase = true) -> {
                                        Toast.makeText(context, "Note: Mock Test management has been removed from Admin Panel. This will work in User App.", Toast.LENGTH_LONG).show()
                                    }
                                    previewItem.destinationPage.contains("sub", ignoreCase = true) -> onNavigateToSubscription()
                                    else -> onSwitchToUserHome()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Jump to Screen", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 3. Delete Confirmation Dialog
    if (posterToDelete != null) {
        val toDelete = posterToDelete!!
        AlertDialog(
            onDismissRequest = { posterToDelete = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                }
            },
            title = {
                Text("Delete Banner Poster?", fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${toDelete.title}\"? This will permanently remove it from the home carousel.",
                    fontSize = 13.5.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        SampleData.deletePoster(toDelete.id)
                        posterToDelete = null
                        Toast.makeText(context, "Banner deleted successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Yes, Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { posterToDelete = null }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

/**
 * Individual Poster Card in Admin List
 */
@Composable
private fun PosterManagementCard(
    poster: PromotionalPosterModel,
    onToggleActive: (Boolean) -> Unit,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, if (poster.isActive) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
        shadowElevation = if (poster.isActive) 4.dp else 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Status Badge + Reorder Buttons + Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Status Badge
                    val status = poster.getStatus()
                    val statusColor = when (status) {
                        PromotionStatus.ACTIVE -> Color(0xFF16A34A)
                        PromotionStatus.SCHEDULED -> Color(0xFF2563EB)
                        PromotionStatus.EXPIRED -> Color(0xFFDC2626)
                        PromotionStatus.INACTIVE -> Color(0xFF94A3B8)
                    }
                    val statusBgColor = when (status) {
                        PromotionStatus.ACTIVE -> Color(0xFFDCFCE7)
                        PromotionStatus.SCHEDULED -> Color(0xFFDBEAFE)
                        PromotionStatus.EXPIRED -> Color(0xFFFEE2E2)
                        PromotionStatus.INACTIVE -> Color(0xFFF1F5F9)
                    }

                    Surface(
                        color = statusBgColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = status.name,
                                color = statusColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Reorder Buttons
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onMoveUp,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(16.dp), tint = Color(0xFF475569))
                        }
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0xFFCBD5E1)))
                        IconButton(
                            onClick = onMoveDown,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(16.dp), tint = Color(0xFF475569))
                        }
                    }
                }

                // Switch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (poster.isActive) "ACTIVE" else "DISABLED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (poster.isActive) Color(0xFF16A34A) else Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = poster.isActive,
                        onCheckedChange = onToggleActive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF16A34A),
                            uncheckedTrackColor = Color(0xFFE2E8F0)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body: Thumbnail Image + Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Poster Visual Thumbnail
                Box(
                    modifier = Modifier
                        .size(width = 130.dp, height = 85.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                ) {
                    if (poster.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = poster.imageUrl,
                            contentDescription = poster.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Priority Overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        color = Color(0xCC0F172A),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "#${poster.priorityOrder}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Content Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = poster.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = poster.promotionalText,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        maxLines = 2,
                        lineHeight = 16.sp,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Target Apps Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF64748B))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${poster.mappedAppIds.size} App(s) Targeted",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Strip (Dates + Destination)
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dates
                    Column {
                        Text("Duration", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        Text(
                            "${poster.startDate} → ${poster.endDate}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }

                    // Destination Indicator Pill
                    val isMock = poster.destinationPage.contains("mock", ignoreCase = true) || poster.destinationPage.contains("test", ignoreCase = true)
                    val isSub = poster.destinationPage.contains("sub", ignoreCase = true) || poster.destinationPage.contains("plan", ignoreCase = true)

                    val destTagColor = when {
                        isMock -> Color(0xFFEA580C)
                        isSub -> Color(0xFF2563EB)
                        else -> Color(0xFF059669)
                    }
                    val destBgColor = when {
                        isMock -> Color(0xFFFFEDD5)
                        isSub -> Color(0xFFDBEAFE)
                        else -> Color(0xFFD1FAE5)
                    }
                    val destLabel = when {
                        isMock -> "Mock Test"
                        isSub -> "Subscription"
                        else -> poster.destinationPage.replaceFirstChar { it.uppercase() }
                    }

                    Surface(
                        color = destBgColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isMock) Icons.Default.Quiz else if (isSub) Icons.Default.Stars else Icons.Default.Link,
                                contentDescription = null,
                                tint = destTagColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = destLabel,
                                color = destTagColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row (Preview, Edit, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Preview Action
                Button(
                    onClick = onPreview,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEFF6FF),
                        contentColor = Color(0xFF2563EB)
                    ),
                    elevation = null
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Preview", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Edit Button
                Button(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1F5F9),
                        contentColor = Color(0xFF334155)
                    ),
                    elevation = null
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Delete Button
                Button(
                    onClick = onDelete,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(44.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFEF2F2),
                        contentColor = Color(0xFFDC2626)
                    ),
                    elevation = null
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/**
 * Comprehensive Create / Edit Poster Dialog with Image URL / Local Storage Upload and Live Preview
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PosterEditorDialog(
    existingPoster: PromotionalPosterModel?,
    onDismiss: () -> Unit,
    onSave: (PromotionalPosterModel) -> Unit,
    onTestRedirection: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf(existingPoster?.title ?: "") }
    var promotionalText by remember { mutableStateOf(existingPoster?.promotionalText ?: "") }
    var offerDetails by remember { mutableStateOf(existingPoster?.offerDetails ?: "") }
    var ctaText by remember { mutableStateOf(existingPoster?.ctaText ?: "Claim Offer") }
    var imageUrl by remember {
        mutableStateOf(
            existingPoster?.imageUrl ?: "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80"
        )
    }
    var priorityOrder by remember { mutableIntStateOf(existingPoster?.priorityOrder ?: 1) }

    // Activation Type: 0 = Instant, 1 = Scheduled
    var activationType by remember { mutableIntStateOf(if (existingPoster?.startDate.isNullOrBlank()) 0 else 1) }
    var startDate by remember { mutableStateOf(existingPoster?.startDate ?: "") }
    var startTime by remember { mutableStateOf(existingPoster?.startTime ?: "00:00") }
    var endDate by remember { mutableStateOf(existingPoster?.endDate ?: "") }
    var endTime by remember { mutableStateOf(existingPoster?.endTime ?: "23:59") }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // Image source mode: 0 = URL, 1 = Local Device Storage
    var imageSourceTab by remember { mutableIntStateOf(if (existingPoster?.imageUrl?.startsWith("file://") == true) 1 else 0) }

    // Destination Selection
    var selectedDestinationType by remember {
        mutableStateOf(
            when {
                existingPoster?.destinationPage?.contains("mock", ignoreCase = true) == true -> PosterDestinationType.MOCK_TESTS
                existingPoster?.destinationPage?.contains("sub", ignoreCase = true) == true -> PosterDestinationType.SUBSCRIPTION
                existingPoster?.destinationPage?.contains("daily", ignoreCase = true) == true -> PosterDestinationType.DAILY_QUIZ
                existingPoster?.destinationPage?.contains("current", ignoreCase = true) == true -> PosterDestinationType.CURRENT_AFFAIRS
                else -> PosterDestinationType.MOCK_TESTS
            }
        )
    }

    // Selected Subject if SUBJECT destination chosen
    var selectedSubject by remember { mutableStateOf(SampleData.defaultSubjects.firstOrNull()?.id ?: "subj-ap-history") }

    // Local Storage Image Picker using standard Android Photo Picker (zero runtime permissions needed)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val persistedFileUri = ImageStorageManager.persistPickedImage(context, uri, "poster")
                    imageUrl = persistedFileUri
                    Toast.makeText(context, "Local image loaded successfully! 📸", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    imageUrl = uri.toString()
                }
            }
        }
    }

    // Current computed target destination key
    val finalDestinationPage = remember(selectedDestinationType, selectedSubject) {
        if (selectedDestinationType == PosterDestinationType.SUBJECT) selectedSubject else selectedDestinationType.destinationKey
    }

    // Active Editor Tab: 0 = Form, 1 = Live Preview Before Confirm
    var activeEditorTab by remember { mutableIntStateOf(0) }

    // Curated High-Yield Presets for 1-Tap URL Loading
    val curatedPresets = listOf(
        Pair("🎯 Mock Exam Desk", "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?auto=format&fit=crop&w=1200&q=80"),
        Pair("👑 Gold Pro Pass", "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?auto=format&fit=crop&w=1200&q=80"),
        Pair("📚 Study Library", "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=1200&q=80"),
        Pair("📰 Current Affairs", "https://images.unsplash.com/photo-1504711434969-e33886168f5c?auto=format&fit=crop&w=1200&q=80"),
        Pair("🎆 Festival Dhamaka", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=1200&q=80")
    )

    // Target App Distribution State (Shared + Individual Upload)
    val allAvailableApps = SampleData.appsListState
    var selectedAppIdsForPoster by remember {
        mutableStateOf(
            existingPoster?.mappedAppIds?.toSet() ?: setOf(SampleData.currentActiveAppId.value)
        )
    }

    // Construct preview poster object
    val previewPoster = remember(title, promotionalText, offerDetails, ctaText, imageUrl, finalDestinationPage, priorityOrder, selectedDestinationType, activationType, startDate, startTime, endDate, endTime, selectedAppIdsForPoster) {
        val finalStartDate = if (activationType == 0) "" else startDate
        val finalStartTime = if (activationType == 0) "00:00" else startTime
        val finalEndDate = if (activationType == 0) "2099-12-31" else endDate
        val finalEndTime = if (activationType == 0) "23:59" else endTime
        val targetList = selectedAppIdsForPoster.toList().ifEmpty { listOf(SampleData.currentActiveAppId.value) }

        PromotionalPosterModel(
            id = existingPoster?.id ?: "banner-${UUID.randomUUID().toString().take(6)}",
            appId = targetList.first(),
            mappedAppIds = targetList,
            isSharedMultiApp = targetList.size > 1,
            posterType = selectedDestinationType.posterTypeTag,
            title = title.ifBlank { "✨ Enter Poster Title" },
            imageUrl = imageUrl,
            promotionalText = promotionalText.ifBlank { "Enter promotional description or syllabus details" },
            ctaText = ctaText.ifBlank { "Claim Offer" },
            offerDetails = offerDetails,
            destinationPage = finalDestinationPage,
            priorityOrder = priorityOrder,
            startDate = finalStartDate,
            startTime = finalStartTime,
            endDate = finalEndDate,
            endTime = finalEndTime,
            isActive = existingPoster?.isActive ?: true
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (existingPoster != null) "Edit Banner Poster" else "Create New Banner Poster",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Target Mock Tests, Subscriptions or Subjects with full preview",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Switcher: "Details & Image" vs "Live Preview Before Confirm"
                TabRow(
                    selectedTabIndex = activeEditorTab,
                    containerColor = Color(0xFFF1F5F9),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .fillMaxWidth()
                ) {
                    Tab(
                        selected = activeEditorTab == 0,
                        onClick = { activeEditorTab = 0 },
                        text = { Text("1. Details & Image", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeEditorTab == 1,
                        onClick = { activeEditorTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("2. Preview & Confirm", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content Area
                Box(modifier = Modifier.weight(1f)) {
                    if (activeEditorTab == 0) {
                        // Form Tab
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 0. Target Apps Distribution Mode (Upload to All Apps / Selective / Individual)
                            Surface(
                                color = Color(0xFFF0F9FF),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "🌐 Poster Distribution Scope",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF0369A1)
                                            )
                                        }

                                        Text(
                                            text = "${selectedAppIdsForPoster.size}/${allAvailableApps.size} Apps",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0284C7)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Upload to All Apps Button
                                        Button(
                                            onClick = {
                                                selectedAppIdsForPoster = allAvailableApps.map { it.id }.toSet()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (selectedAppIdsForPoster.size == allAvailableApps.size) Color(0xFF0284C7) else Color(0xFFE0F2FE)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                "Upload to All",
                                                fontSize = 11.sp,
                                                color = if (selectedAppIdsForPoster.size == allAvailableApps.size) Color.White else Color(0xFF0369A1),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Individual Upload Button (Selected App Only)
                                        Button(
                                            onClick = {
                                                selectedAppIdsForPoster = setOf(SampleData.currentActiveAppId.value)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (selectedAppIdsForPoster.size == 1 && selectedAppIdsForPoster.contains(SampleData.currentActiveAppId.value)) Color(0xFF0284C7) else Color(0xFFE0F2FE)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                "Current App Only",
                                                fontSize = 11.sp,
                                                color = if (selectedAppIdsForPoster.size == 1 && selectedAppIdsForPoster.contains(SampleData.currentActiveAppId.value)) Color.White else Color(0xFF0369A1),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Selective Upload (Check target apps):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        allAvailableApps.forEach { appItem ->
                                            val isChecked = selectedAppIdsForPoster.contains(appItem.id)
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isChecked) Color.White else Color(0xFFF1F5F9),
                                                border = BorderStroke(1.dp, if (isChecked) Color(0xFF0284C7) else Color(0xFFCBD5E1)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedAppIdsForPoster = if (isChecked) {
                                                            if (selectedAppIdsForPoster.size > 1) selectedAppIdsForPoster - appItem.id else selectedAppIdsForPoster
                                                        } else {
                                                            selectedAppIdsForPoster + appItem.id
                                                        }
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = isChecked,
                                                        onCheckedChange = { checked ->
                                                            selectedAppIdsForPoster = if (checked) {
                                                                selectedAppIdsForPoster + appItem.id
                                                            } else {
                                                                if (selectedAppIdsForPoster.size > 1) selectedAppIdsForPoster - appItem.id else selectedAppIdsForPoster
                                                            }
                                                        },
                                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0284C7))
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = appItem.name,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 1. Destination / Concerned Redirection Type (CORE USER REQUIREMENT)
                            Text(
                                text = "🎯 Target Destination (Concerned Page When Clicked)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )

                            // Quick Choice Chips
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                PosterDestinationType.values().forEach { destType ->
                                    val isSelected = selectedDestinationType == destType
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) destType.lightBg else Color(0xFFF8FAFC),
                                        border = BorderStroke(1.2.dp, if (isSelected) destType.primaryColor else Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedDestinationType = destType }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedDestinationType = destType },
                                                colors = RadioButtonDefaults.colors(selectedColor = destType.primaryColor)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = destType.icon,
                                                contentDescription = null,
                                                tint = destType.primaryColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = destType.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (isSelected) destType.primaryColor else Color(0xFF0F172A)
                                                )
                                                Text(
                                                    text = destType.subtitle,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Title and Description
                            Text(
                                text = "📝 Title & Description",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )

                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Poster Title *") },
                                placeholder = {
                                    Text(
                                        if (selectedDestinationType == PosterDestinationType.MOCK_TESTS)
                                            "e.g. 🎯 Grand All-Andhra APPSC Group 2 Mock Test"
                                        else
                                            "e.g. 👑 Flat 50% Off 1-Year Unlimited Pro Pass"
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = promotionalText,
                                onValueChange = { promotionalText = it },
                                label = { Text("Promotional Description / Subtitle *") },
                                placeholder = { Text("e.g. 150 Full-Length Simulation MCQs with Instant State Ranking & Analytics") },
                                maxLines = 3,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = offerDetails,
                                    onValueChange = { offerDetails = it },
                                    label = { Text("Offer Badge (Optional)") },
                                    placeholder = { Text("e.g. FLAT 50% OFF") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = ctaText,
                                    onValueChange = { ctaText = it },
                                    label = { Text("Button Text") },
                                    placeholder = { Text("e.g. Attempt Test") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // 3. Image Upload: Local Storage vs URL (USER REQUIREMENT)
                            Text(
                                text = "🖼️ Poster Image (Upload or Web URL)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )

                            // Image Source Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (imageSourceTab == 1) Color(0xFFDBEAFE) else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (imageSourceTab == 1) Color(0xFF2563EB) else Color(0xFFCBD5E1)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { imageSourceTab = 1 }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            tint = if (imageSourceTab == 1) Color(0xFF2563EB) else Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Local Storage Image",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (imageSourceTab == 1) Color(0xFF2563EB) else Color(0xFF475569)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (imageSourceTab == 0) Color(0xFFDBEAFE) else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (imageSourceTab == 0) Color(0xFF2563EB) else Color(0xFFCBD5E1)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { imageSourceTab = 0 }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Link,
                                            contentDescription = null,
                                            tint = if (imageSourceTab == 0) Color(0xFF2563EB) else Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Web Image URL",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (imageSourceTab == 0) Color(0xFF2563EB) else Color(0xFF475569)
                                        )
                                    }
                                }
                            }

                            if (imageSourceTab == 1) {
                                // Option A: Pick from Local Device Storage
                                Surface(
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "Upload Image from Device Storage / Gallery",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF334155)
                                        )

                                        Button(
                                            onClick = {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Choose from Device Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        if (imageUrl.isNotBlank() && imageUrl.startsWith("file://")) {
                                            Surface(
                                                color = Color(0xFFDCFCE7),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Local Image Attached & Saved", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Option B: Paste Web URL + Presets
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = imageUrl,
                                        onValueChange = { imageUrl = it },
                                        label = { Text("Image Web URL (https://...)") },
                                        placeholder = { Text("Paste banner image URL here") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Text(
                                        text = "⚡ Quick Curated Presets (1-Tap):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(curatedPresets) { (label, presetUrl) ->
                                            SuggestionChip(
                                                onClick = { imageUrl = presetUrl },
                                                label = { Text(label, fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                }
                            }

                            // Recommended Dimensions Helper & App Container Preview
                            Spacer(modifier = Modifier.height(10.dp))
                            RecommendedImageSizeHelper(
                                spec = UploadImageType.POSTER_BANNER,
                                imageSource = imageUrl,
                                showContainerPreview = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // 4. Activation Settings (Instant vs Scheduled)
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Activation Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            onClick = { activationType = 0 },
                                            color = if (activationType == 0) Color(0xFFEFF6FF) else Color.White,
                                            border = BorderStroke(1.dp, if (activationType == 0) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.FlashOn, contentDescription = null, tint = if (activationType == 0) Color(0xFF2563EB) else Color(0xFF64748B))
                                                Text("Instant", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (activationType == 0) Color(0xFF2563EB) else Color(0xFF64748B))
                                            }
                                        }

                                        Surface(
                                            onClick = { activationType = 1 },
                                            color = if (activationType == 1) Color(0xFFEDE9FE) else Color.White,
                                            border = BorderStroke(1.dp, if (activationType == 1) Color(0xFF7C3AED) else Color(0xFFE2E8F0)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.Schedule, contentDescription = null, tint = if (activationType == 1) Color(0xFF7C3AED) else Color(0xFF64748B))
                                                Text("Scheduled", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (activationType == 1) Color(0xFF7C3AED) else Color(0xFF64748B))
                                            }
                                        }
                                    }

                                    if (activationType == 1) {
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text("Start Date & Time", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = startDate,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Start Date") },
                                                modifier = Modifier.weight(1f).clickable { showStartDatePicker = true },
                                                enabled = false,
                                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color(0xFFE2E8F0), disabledLabelColor = Color(0xFF64748B))
                                            )
                                            OutlinedTextField(
                                                value = startTime,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Start Time") },
                                                modifier = Modifier.weight(1f).clickable { showStartTimePicker = true },
                                                enabled = false,
                                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color(0xFFE2E8F0), disabledLabelColor = Color(0xFF64748B))
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text("End Date & Time", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = endDate,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("End Date") },
                                                modifier = Modifier.weight(1f).clickable { showEndDatePicker = true },
                                                enabled = false,
                                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color(0xFFE2E8F0), disabledLabelColor = Color(0xFF64748B))
                                            )
                                            OutlinedTextField(
                                                value = endTime,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("End Time") },
                                                modifier = Modifier.weight(1f).clickable { showEndTimePicker = true },
                                                enabled = false,
                                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color(0xFFE2E8F0), disabledLabelColor = Color(0xFF64748B))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Live Preview Before Confirm Tab (USER REQUIREMENT)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFFDE047)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "This is an exact preview of how your banner will appear to students on the Home carousel. Test the redirection button below!",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }

                            // Interactive Student Card Preview
                            InteractiveStudentPosterCard(
                                poster = previewPoster,
                                onTestClick = {
                                    val dest = selectedDestinationType.title
                                    Toast.makeText(context, "✅ Redirection Verified! Tapping this card redirects students to: $dest", Toast.LENGTH_LONG).show()
                                }
                            )

                            // Verification Summary
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Verification Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))

                                    Row {
                                        Text("• Target Destination: ", fontSize = 12.sp, color = Color(0xFF64748B))
                                        Text(selectedDestinationType.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = selectedDestinationType.primaryColor)
                                    }
                                    Row {
                                        Text("• Action on Click: ", fontSize = 12.sp, color = Color(0xFF64748B))
                                        Text("Redirects to ${finalDestinationPage}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                    }
                                    Row {
                                        Text("• Carousel Priority: ", fontSize = 12.sp, color = Color(0xFF64748B))
                                        Text("#$priorityOrder (Immediate Top Banner)", fontSize = 12.sp, color = Color(0xFF0F172A))
                                    }
                                }
                            }

                            // Test Jump Button
                            OutlinedButton(
                                onClick = { onTestRedirection(finalDestinationPage) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2563EB)),
                                border = BorderStroke(1.dp, Color(0xFF93C5FD))
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Immediate Redirection Jump to Target Screen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Bar (Cancel, Switch to Preview, Confirm & Publish)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    if (activeEditorTab == 0) {
                        OutlinedButton(
                            onClick = { activeEditorTab = 1 },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2563EB)),
                            border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview First", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(previewPoster)
                            } else {
                                Toast.makeText(context, "Please enter a banner title", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (existingPoster != null) "Update Banner" else "Confirm & Publish",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Exact replica of the Student Home Carousel Card with simulated click interaction
 */
@Composable
private fun InteractiveStudentPosterCard(
    poster: PromotionalPosterModel,
    onTestClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .clickable { onTestClick() },
        border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background image (full cover and fit)
            if (poster.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = poster.imageUrl,
                    contentDescription = poster.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PosterFhdCardImage(
                    poster = poster,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color(0x99000000),
                                Color(0xFF000000)
                            )
                        )
                    )
            )

            // 3. Top Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF59E0B)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = poster.posterType.uppercase(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Priority #${poster.priorityOrder}",
                        color = Color(0xFFFDE68A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // 4. Bottom Offer Text & Call-To-Action Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = poster.title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = poster.promotionalText,
                    color = Color(0xFFFDE047),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (poster.offerDetails.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = poster.offerDetails,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Tap to Test Redirection",
                            color = Color(0xFFFDE68A),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFF7A00),
                        shadowElevation = 2.dp,
                        modifier = Modifier.clickable { onTestClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = poster.ctaText.ifBlank { "CLAIM OFFER" },
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Metric summary card for admin dashboard
 */
@Composable
private fun MetricMiniCard(
    title: String,
    count: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}
