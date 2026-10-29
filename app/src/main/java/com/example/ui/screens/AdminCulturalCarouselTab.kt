package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.CulturalCarouselItem
import com.example.repository.CulturalCarouselRepository
import com.example.ui.components.AndhraCulturalCarousel
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Admin Cultural Carousel Management Screen.
 * Allows administrators to add, edit, reorder, toggle, and synchronize
 * Andhra Pradesh cultural hero carousel images dynamically without requiring app updates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCulturalCarouselTab(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { CulturalCarouselRepository.getInstance(context) }

    val allItems by repository.allItems.collectAsState()
    val enabledItems by repository.enabledItems.collectAsState()
    val isLoading by repository.isLoading.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<CulturalCarouselItem?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<CulturalCarouselItem?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var previewMode by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .padding(16.dp)
            .testTag("admin_cultural_carousel_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. HEADER CARD: Title + Actions
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0B132B),
            border = BorderStroke(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF38BDF8))
                )
            ),
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Cultural Carousel Manager",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF065F46)
                            ) {
                                Text(
                                    text = "LIVE CLOUD SYNC",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6EE7B7),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "ఆంధ్రప్రదేశ్ సాంస్కృతిక దృశ్యమాల నిర్వహణ • Login & OTP Hero",
                            fontSize = 12.sp,
                            color = Color(0xFFFCD34D)
                        )
                    }

                    // Total Active Slides counter
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "${enabledItems.size}/${allItems.size} Active",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            editingItem = CulturalCarouselItem(id = UUID.randomUUID().toString())
                            showEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_add_slide_button")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Slide", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { previewMode = !previewMode },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                    ) {
                        Icon(
                            imageVector = if (previewMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (previewMode) "Hide Preview" else "Show Preview", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.4f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1))
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset AP Defaults", fontSize = 12.sp)
                    }
                }
            }
        }

        // 2. LIVE INTERACTIVE CAROUSEL PREVIEW (Collapsible)
        AnimatedVisibility(visible = previewMode && enabledItems.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE CANDIDATE VIEW PREVIEW",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Auto-advancing 5.5s • Ken Burns",
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B)
                    )
                }

                AndhraCulturalCarousel(
                    items = enabledItems,
                    height = 190.dp,
                    compactMode = false
                )
            }
        }

        // 3. SLIDES LIST
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MANAGE SLIDES & REORDER (${allItems.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFBBF24),
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Instant Backend Sync",
                fontSize = 11.sp,
                color = Color(0xFF10B981)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(allItems, key = { _, item -> item.id }) { index, item ->
                AdminCulturalSlideCard(
                    item = item,
                    index = index,
                    totalCount = allItems.size,
                    onToggleEnabled = { enabled ->
                        coroutineScope.launch {
                            repository.toggleItemEnabled(item.id, enabled)
                            Toast.makeText(context, "${item.title} ${if (enabled) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onMoveUp = {
                        if (index > 0) {
                            coroutineScope.launch {
                                val mutable = allItems.toMutableList()
                                java.util.Collections.swap(mutable, index, index - 1)
                                repository.reorderItems(mutable)
                            }
                        }
                    },
                    onMoveDown = {
                        if (index < allItems.size - 1) {
                            coroutineScope.launch {
                                val mutable = allItems.toMutableList()
                                java.util.Collections.swap(mutable, index, index + 1)
                                repository.reorderItems(mutable)
                            }
                        }
                    },
                    onEdit = {
                        editingItem = item
                        showEditDialog = true
                    },
                    onDelete = {
                        showDeleteConfirmDialog = item
                    }
                )
            }
        }
    }

    // 4. ADD / EDIT DIALOG
    if (showEditDialog && editingItem != null) {
        AdminSlideEditDialog(
            initialItem = editingItem!!,
            onDismiss = {
                showEditDialog = false
                editingItem = null
            },
            onSave = { updated ->
                coroutineScope.launch {
                    val exists = allItems.any { it.id == updated.id }
                    if (exists) {
                        repository.updateItem(updated)
                        Toast.makeText(context, "✅ Slide Updated & Synced to Cloud", Toast.LENGTH_SHORT).show()
                    } else {
                        repository.addItem(updated)
                        Toast.makeText(context, "✅ New Slide Added & Synced to Cloud", Toast.LENGTH_SHORT).show()
                    }
                    showEditDialog = false
                    editingItem = null
                }
            }
        )
    }

    // 5. DELETE CONFIRMATION DIALOG
    if (showDeleteConfirmDialog != null) {
        val itemToDelete = showDeleteConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Cultural Slide?", color = Color.White) },
            text = {
                Text(
                    "Are you sure you want to remove \"${itemToDelete.title}\" from the candidate carousel? This action will remove it from both local cache and Cloud Firestore.",
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.deleteItem(itemToDelete.id)
                            Toast.makeText(context, "Slide deleted successfully", Toast.LENGTH_SHORT).show()
                            showDeleteConfirmDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // 6. RESET TO DEFAULTS CONFIRMATION DIALOG
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset to Andhra Heritage Defaults?", color = Color.White) },
            text = {
                Text(
                    "This will restore the 8 official curated Andhra Pradesh cultural slides (Araku Valley, Dhimsa Tribal Dance, Eastern Ghats, Lepakshi, Borra Caves, Tirumala Seshachalam, Prakasam Barrage, Kalamkari Heritage).",
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.resetToDefaults()
                            Toast.makeText(context, "✅ Restored official Andhra cultural slides!", Toast.LENGTH_SHORT).show()
                            showResetConfirmDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Confirm Reset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

/**
 * Individual Slide Management Card
 */
@Composable
private fun AdminCulturalSlideCard(
    item: CulturalCarouselItem,
    index: Int,
    totalCount: Int,
    onToggleEnabled: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(
            width = 1.dp,
            color = if (item.isEnabled) Color(0xFF334155) else Color(0xFF1E293B)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_slide_card_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reorder Column (Move Up / Down)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = index > 0,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Move Up",
                        tint = if (index > 0) Color(0xFF38BDF8) else Color(0xFF475569)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCD34D)
                        )
                    }
                }

                IconButton(
                    onClick = onMoveDown,
                    enabled = index < totalCount - 1,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Move Down",
                        tint = if (index < totalCount - 1) Color(0xFF38BDF8) else Color(0xFF475569)
                    )
                }
            }

            // Thumbnail Image
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF020617),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Landscape, contentDescription = null, tint = Color(0xFF64748B))
                    }
                }
            }

            // Slide Info Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Badge + Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF022C22),
                        border = BorderStroke(0.8.dp, Color(0xFF10B981))
                    ) {
                        Text(
                            text = item.badgeText.uppercase(),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6EE7B7),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = item.tag,
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // English Title
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isEnabled) Color.White else Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Telugu Cultural Title
                if (item.teluguTitle.isNotBlank()) {
                    Text(
                        text = item.teluguTitle,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (item.isEnabled) Color(0xFFFCD34D) else Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Subtitle
                Text(
                    text = item.subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Enable Toggle + Edit + Delete Column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Enable/Disable Switch
                Switch(
                    checked = item.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF10B981),
                        checkedTrackColor = Color(0xFF064E3B),
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.scale(0.8f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Slide",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Slide",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog for Adding or Editing an Andhra Cultural Carousel Slide
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminSlideEditDialog(
    initialItem: CulturalCarouselItem,
    onDismiss: () -> Unit,
    onSave: (CulturalCarouselItem) -> Unit
) {
    var title by remember { mutableStateOf(initialItem.title) }
    var teluguTitle by remember { mutableStateOf(initialItem.teluguTitle) }
    var subtitle by remember { mutableStateOf(initialItem.subtitle) }
    var badgeText by remember { mutableStateOf(initialItem.badgeText) }
    var tag by remember { mutableStateOf(initialItem.tag) }
    var imageUrl by remember { mutableStateOf(initialItem.imageUrl) }
    var description by remember { mutableStateOf(initialItem.description) }
    var isEnabled by remember { mutableStateOf(initialItem.isEnabled) }

    // Zero-permission Android Photo Picker (Google Play Policy Compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUrl = uri.toString()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0B132B),
            border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialItem.title.isBlank()) "Add Cultural Carousel Slide" else "Edit Cultural Slide",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "ఆంధ్రప్రదేశ్ సాంస్కృతిక దృశ్య స్లయిడ్ సవరణ",
                            fontSize = 12.sp,
                            color = Color(0xFFFCD34D)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Image Preview & Upload Row
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF020617),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        "No Image Selected • Enter URL or Pick from Gallery",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            // Pick button overlay
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose Photo", fontSize = 12.sp)
                            }
                        }
                    }

                    // Image URL Input
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image High-Res URL (or pick from gallery)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )

                    // Title (English)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Slide Title (English)") },
                        placeholder = { Text("e.g. Araku Valley & Coffee Plantations") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )

                    // Telugu Title
                    OutlinedTextField(
                        value = teluguTitle,
                        onValueChange = { teluguTitle = it },
                        label = { Text("తెలుగు శీర్షిక (Telugu Cultural Title)") },
                        placeholder = { Text("e.g. అరకు లోయ & కాఫీ తోటలు") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )

                    // Subtitle / Location
                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        label = { Text("Subtitle / Geographic Location") },
                        placeholder = { Text("e.g. Lush Terraced Hills • Eastern Ghats, Visakhapatnam") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )

                    // Badge Text & Category Tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = badgeText,
                            onValueChange = { badgeText = it },
                            label = { Text("Badge Text") },
                            placeholder = { Text("e.g. NATURAL WONDER") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFF334155)
                            )
                        )

                        OutlinedTextField(
                            value = tag,
                            onValueChange = { tag = it },
                            label = { Text("Tag Category") },
                            placeholder = { Text("e.g. Landscape, Dance") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155)
                            )
                        )
                    }

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Cultural Description (Optional)") },
                        placeholder = { Text("Brief historical or cultural context for aspirants...") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )

                    // Enabled Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Active in Candidate Carousel", fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text("When turned on, this slide appears on Login and OTP screens.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF10B981),
                                checkedTrackColor = Color(0xFF064E3B)
                            )
                        )
                    }
                }

                // Action Buttons (Cancel / Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF475569))
                    ) {
                        Text("Cancel", color = Color.White)
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    initialItem.copy(
                                        title = title.trim(),
                                        teluguTitle = teluguTitle.trim(),
                                        subtitle = subtitle.trim(),
                                        badgeText = badgeText.trim().ifBlank { "ANDHRA HERITAGE" },
                                        tag = tag.trim().ifBlank { "Culture" },
                                        imageUrl = imageUrl.trim(),
                                        description = description.trim(),
                                        isEnabled = isEnabled,
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Sync", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
