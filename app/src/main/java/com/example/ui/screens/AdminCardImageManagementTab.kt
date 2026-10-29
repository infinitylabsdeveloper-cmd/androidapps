package com.example.ui.screens

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
import com.example.data.SampleData
import com.example.model.CardImageConfigModel
import com.example.ui.components.ImageUploadGuidelinesDialog
import com.example.ui.components.RecommendedImageSizeHelper
import com.example.ui.components.UploadImageType
import com.example.util.ImageStorageManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCardImageManagementTab() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cardImages = SampleData.cardImagesState
    val appsList = SampleData.appsListState

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showDialog by remember { mutableStateOf(false) }
    var editingCardImage by remember { mutableStateOf<CardImageConfigModel?>(null) }
    var imageToDelete by remember { mutableStateOf<CardImageConfigModel?>(null) }
    var showGlobalGuidelines by remember { mutableStateOf(false) }

    if (showGlobalGuidelines) {
        ImageUploadGuidelinesDialog(
            currentSpec = UploadImageType.CURRENT_AFFAIRS_CARD,
            onDismiss = { showGlobalGuidelines = false }
        )
    }

    // Dialog form state
    var formCardType by remember { mutableStateOf("CURRENT_AFFAIRS") }
    var formCardTitle by remember { mutableStateOf("") }
    var formImageUrl by remember { mutableStateOf("") }
    var formIsEnabled by remember { mutableStateOf(true) }
    var formSelectedAppIds by remember { mutableStateOf(listOf("appsc-group2")) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val savedPath = ImageStorageManager.persistPickedImage(context, uri, "card_image")
                formImageUrl = savedPath ?: uri.toString()
                Toast.makeText(context, "📷 Image uploaded successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val filteredCardImages = remember(cardImages.size, selectedCategoryFilter) {
        val list = if (selectedCategoryFilter == "ALL") cardImages
        else cardImages.filter { it.cardType == selectedCategoryFilter }
        list.sortedBy { it.order }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = Color(0xFF2563EB)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Card Image Management",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Manage and customize card images for Current Affairs, Mock Test Generator, and Mock Tests in real time.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Filter Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf(
                            "ALL" to "All Cards",
                            "CURRENT_AFFAIRS" to "Current Affairs",
                            "MOCK_TEST_GENERATOR" to "Test Generator",
                            "MOCK_TEST" to "Mock Test"
                        )
                        filters.forEach { (key, label) ->
                            val isSelected = selectedCategoryFilter == key
                            Surface(
                                onClick = { selectedCategoryFilter = key },
                                color = if (isSelected) Color(0xFF2563EB) else Color.White,
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF475569),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Bar (Add New Card Image Button)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configured Card Images (${filteredCardImages.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showGlobalGuidelines = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF2563EB))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Guidelines", fontSize = 12.sp, color = Color(0xFF2563EB))
                    }

                    Button(
                        onClick = {
                            editingCardImage = null
                            formCardType = if (selectedCategoryFilter != "ALL") selectedCategoryFilter else "CURRENT_AFFAIRS"
                            formCardTitle = ""
                            formImageUrl = ""
                            formIsEnabled = true
                            formSelectedAppIds = listOf("appsc-group2")
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Card Image", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Card Images List
        if (filteredCardImages.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ImageNotSupported,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No Card Images Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF475569)
                        )
                        Text(
                            "Tap 'Add Card Image' above to upload or link card graphics.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredCardImages, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail Preview
                        Card(
                            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            AsyncImage(
                                model = item.imageUrl.ifEmpty { "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=800" },
                                contentDescription = item.cardTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = when (item.cardType) {
                                        "CURRENT_AFFAIRS" -> Color(0xFFCCFBF1)
                                        "MOCK_TEST_GENERATOR" -> Color(0xFFFFEDD5)
                                        else -> Color(0xFFDBEAFE)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = when (item.cardType) {
                                            "CURRENT_AFFAIRS" -> "Current Affairs"
                                            "MOCK_TEST_GENERATOR" -> "Test Generator"
                                            else -> "Mock Test"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (item.cardType) {
                                            "CURRENT_AFFAIRS" -> Color(0xFF0F766E)
                                            "MOCK_TEST_GENERATOR" -> Color(0xFFC2410C)
                                            else -> Color(0xFF1D4ED8)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                if (item.isEnabled) {
                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Active",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Disabled",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Reorder Buttons
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { SampleData.reorderCardImageMoveUp(item.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Up", modifier = Modifier.size(12.dp), tint = Color(0xFF64748B))
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(12.dp).background(Color(0xFFE2E8F0)))
                                    IconButton(
                                        onClick = { SampleData.reorderCardImageMoveDown(item.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = "Down", modifier = Modifier.size(12.dp), tint = Color(0xFF64748B))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.cardTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Target Apps: ${item.mappedAppIds.joinToString(", ")}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Actions
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            IconButton(
                                onClick = {
                                    editingCardImage = item
                                    formCardType = item.cardType
                                    formCardTitle = item.cardTitle
                                    formImageUrl = item.imageUrl
                                    formIsEnabled = item.isEnabled
                                    formSelectedAppIds = item.mappedAppIds
                                    showDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = { imageToDelete = item },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.925f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (editingCardImage == null) "Upload / Add Card Image" else "Edit Card Image",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        IconButton(onClick = { showDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // Card Type Selector
                    Text("Select Card Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val types = listOf(
                            "CURRENT_AFFAIRS" to "Current Affairs",
                            "MOCK_TEST_GENERATOR" to "Test Generator",
                            "MOCK_TEST" to "Mock Test"
                        )
                        types.forEach { (typeKey, typeLabel) ->
                            val isSelected = formCardType == typeKey
                            Surface(
                                onClick = { formCardType = typeKey },
                                color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = typeLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF475569),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = formCardTitle,
                        onValueChange = { formCardTitle = it },
                        label = { Text("Card Title / Display Name") },
                        placeholder = { Text("e.g. Daily Current Affairs Capsule") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Image Upload Options (Local Storage or URL)
                    Text("Card Image (Local Upload or URL)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload from Phone", fontSize = 11.5.sp)
                        }
                    }

                    OutlinedTextField(
                        value = formImageUrl,
                        onValueChange = { formImageUrl = it },
                        label = { Text("Image URL or Local Storage Path") },
                        placeholder = { Text("https://images.unsplash.com/... or file path") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Image Size Helper, Recommended Specs & Preview
                    val cardSpec = when (formCardType) {
                        "CURRENT_AFFAIRS" -> UploadImageType.CURRENT_AFFAIRS_CARD
                        "MOCK_TEST_GENERATOR" -> UploadImageType.MOCK_TEST_IMAGE
                        "MOCK_TEST" -> UploadImageType.MOCK_TEST_IMAGE
                        else -> UploadImageType.SUBJECT_IMAGE
                    }

                    RecommendedImageSizeHelper(
                        spec = cardSpec,
                        imageSource = formImageUrl,
                        showContainerPreview = true
                    )

                    // App Targeting
                    Text("Target App(s)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val allAppIds = appsList.map { it.id }
                        val isAllSelected = formSelectedAppIds.containsAll(allAppIds)

                        OutlinedButton(
                            onClick = {
                                formSelectedAppIds = if (isAllSelected) listOf("appsc-group2") else allAppIds
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isAllSelected) Color(0xFFEFF6FF) else Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isAllSelected) "✓ Publish to All Apps" else "Publish to All Apps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Enable / Disable switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Card Image Immediately", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        Switch(
                            checked = formIsEnabled,
                            onCheckedChange = { formIsEnabled = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (formCardTitle.isBlank()) {
                                Toast.makeText(context, "⚠️ Please enter a card title", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (formImageUrl.isBlank()) {
                                Toast.makeText(context, "⚠️ Please upload or provide an image URL", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (editingCardImage == null) {
                                val newConfig = CardImageConfigModel(
                                    cardType = formCardType,
                                    cardTitle = formCardTitle,
                                    imageUrl = formImageUrl,
                                    isEnabled = formIsEnabled,
                                    mappedAppIds = formSelectedAppIds
                                )
                                SampleData.addCardImage(newConfig)
                                if (formCardType == "CURRENT_AFFAIRS") {
                                    SampleData.updateAllMonthlyCurrentAffairsWallpaper(formImageUrl)
                                }
                                Toast.makeText(context, "✅ Card image added & applied to Monthly Current Affairs!", Toast.LENGTH_SHORT).show()
                            } else {
                                val updated = editingCardImage!!.copy(
                                    cardType = formCardType,
                                    cardTitle = formCardTitle,
                                    imageUrl = formImageUrl,
                                    isEnabled = formIsEnabled,
                                    mappedAppIds = formSelectedAppIds
                                )
                                SampleData.updateCardImage(updated)
                                if (formCardType == "CURRENT_AFFAIRS") {
                                    SampleData.updateAllMonthlyCurrentAffairsWallpaper(formImageUrl)
                                }
                                Toast.makeText(context, "✅ Card image updated & applied to Monthly Current Affairs!", Toast.LENGTH_SHORT).show()
                            }
                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (editingCardImage == null) "Save & Publish Card Image" else "Update Card Image", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (imageToDelete != null) {
        AlertDialog(
            onDismissRequest = { imageToDelete = null },
            title = { Text("Delete Card Image?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${imageToDelete?.cardTitle}'? This will revert the card to default visual styling in the user app.") },
            confirmButton = {
                Button(
                    onClick = {
                        SampleData.deleteCardImage(imageToDelete!!.id)
                        Toast.makeText(context, "🗑️ Card image deleted", Toast.LENGTH_SHORT).show()
                        imageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { imageToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
