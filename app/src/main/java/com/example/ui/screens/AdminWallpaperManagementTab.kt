package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.model.AppWallpaperModel
import com.example.model.AppTenantModel
import com.example.ui.components.ImageUploadGuidelinesDialog
import com.example.ui.components.RecommendedImageSizeHelper
import com.example.ui.components.UploadImageType
import com.example.util.ImageStorageManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWallpaperManagementTab() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val wallpapers = SampleData.appWallpapersState

    var showCreateOrEditDialog by remember { mutableStateOf(false) }
    var wallpaperToEdit by remember { mutableStateOf<AppWallpaperModel?>(null) }
    var wallpaperToDelete by remember { mutableStateOf<AppWallpaperModel?>(null) }
    var showGlobalGuidelines by remember { mutableStateOf(false) }

    if (showGlobalGuidelines) {
        ImageUploadGuidelinesDialog(
            currentSpec = UploadImageType.APP_WALLPAPER,
            onDismiss = { showGlobalGuidelines = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Section Description Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Wallpaper,
                                contentDescription = null,
                                tint = Color(0xFF2563EB)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "App Screen Wallpapers",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Customize backgrounds for individual student-facing screens in real time.",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }
        }

        // Action controls bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configured Wallpapers (${wallpapers.size})",
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
                            wallpaperToEdit = null
                            showCreateOrEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Wallpaper", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Wallpapers List
        if (wallpapers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Wallpaper,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No Custom Wallpapers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF475569)
                        )
                        Text(
                            "Using default luminous glowing vectors vector canvas backgrounds.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        } else {
            itemsIndexed(
                items = wallpapers.sortedBy { it.order },
                key = { _, item -> item.id }
            ) { index, wallpaper ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail Preview
                        Card(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            AsyncImage(
                                model = wallpaper.url,
                                contentDescription = "Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Details Column
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (wallpaper.screenName == "Global Default") Color(0xFFFEF3C7) else Color(0xFFECFDF5),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = wallpaper.screenName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (wallpaper.screenName == "Global Default") Color(0xFFD97706) else Color(0xFF059669)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "#${wallpaper.order + 1}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Targeted Apps
                            if (wallpaper.mappedAppIds.isEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color(0xFF64748B))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("All Apps", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color(0xFF2563EB))
                                    Text(
                                        text = wallpaper.mappedAppIds.map { id -> SampleData.appsListState.find { it.id == id }?.name ?: id }.joinToString(", "),
                                        fontSize = 10.sp,
                                        color = Color(0xFF2563EB),
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = wallpaper.url,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Switch Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Switch(
                                    checked = wallpaper.isEnabled,
                                    onCheckedChange = { isChecked ->
                                        SampleData.updateAppWallpaper(wallpaper.copy(isEnabled = isChecked))
                                    },
                                    modifier = Modifier.scale(0.7f)
                                )
                                Text(
                                    text = if (wallpaper.isEnabled) "Active" else "Disabled",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (wallpaper.isEnabled) Color(0xFF10B981) else Color(0xFF64748B)
                                )
                            }
                        }

                        // Actions
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Reorder Arrows
                            Row {
                                IconButton(
                                    onClick = { SampleData.reorderWallpaperMoveUp(wallpaper.id) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ArrowUpward,
                                        contentDescription = "Move Up",
                                        tint = if (index > 0) Color(0xFF2563EB) else Color(0xFFCBD5E1),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { SampleData.reorderWallpaperMoveDown(wallpaper.id) },
                                    enabled = index < wallpapers.size - 1,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ArrowDownward,
                                        contentDescription = "Move Down",
                                        tint = if (index < wallpapers.size - 1) Color(0xFF2563EB) else Color(0xFFCBD5E1),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Edit & Delete Buttons
                            Row {
                                IconButton(
                                    onClick = {
                                        wallpaperToEdit = wallpaper
                                        showCreateOrEditDialog = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { wallpaperToDelete = wallpaper },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dedicated state holder for the dialog to avoid capture issues with launchers
    val dialogState = remember { mutableStateOf<DialogState?>(null) }

    // PHOTO PICKER LAUNCHERS
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    try {
                        val localPath = ImageStorageManager.persistPickedImage(context, uri, "wallpaper")
                        dialogState.value = dialogState.value?.copy(localImageUriString = localPath, isLocalMode = true)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error saving image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )

    if (showCreateOrEditDialog && dialogState.value == null) {
        dialogState.value = DialogState(
            screenName = wallpaperToEdit?.screenName ?: "Global Default",
            mappedAppIds = wallpaperToEdit?.mappedAppIds ?: emptyList(),
            isLocalMode = wallpaperToEdit != null && !wallpaperToEdit!!.url.startsWith("http"),
            url = if (wallpaperToEdit != null && wallpaperToEdit!!.url.startsWith("http")) wallpaperToEdit!!.url else "",
            localImageUriString = if (wallpaperToEdit != null && !wallpaperToEdit!!.url.startsWith("http")) wallpaperToEdit!!.url else "",
            isEnabled = wallpaperToEdit?.isEnabled ?: true,
            appTargetMode = if (wallpaperToEdit?.mappedAppIds?.isEmpty() == true) "All Apps" else if (wallpaperToEdit?.mappedAppIds?.size == 1) "One App" else "Multiple Apps"
        )
    }

    // CREATE OR EDIT DIALOG
    if (showCreateOrEditDialog && dialogState.value != null) {
        val state = dialogState.value!!
        var screenDropdownExpanded by remember { mutableStateOf(false) }
        val apps = SampleData.appsListState
        val screens = listOf(
            "Entire App", "Global Default", "Login Screen", "OTP Screen", "Home Screen",
            "Syllabus Explorer", "Mock Test Series", "Current Affairs",
            "Saved Questions", "Performance Board", "Topic Sets"
        )

        Dialog(
            onDismissRequest = { 
                showCreateOrEditDialog = false
                dialogState.value = null
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.95f).wrapContentHeight().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = if (wallpaperToEdit == null) "Configure New Background" else "Update Background Target",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Target Apps
                    Text("1. Target Apps", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("One App", "Multiple Apps", "All Apps").forEach { mode ->
                            val isSelected = state.appTargetMode == mode
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9))
                                    .clickable { 
                                        dialogState.value = state.copy(
                                            appTargetMode = mode,
                                            mappedAppIds = if (mode == "All Apps") emptyList() else state.mappedAppIds
                                        )
                                    }.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(mode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFF475569))
                            }
                        }
                    }

                    if (state.appTargetMode != "All Apps") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)).padding(8.dp)) {
                            apps.forEach { app ->
                                val isChecked = state.mappedAppIds.contains(app.id)
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        val newList = if (state.appTargetMode == "One App") listOf(app.id) else {
                                            if (isChecked) state.mappedAppIds.filter { it != app.id } else state.mappedAppIds + app.id
                                        }
                                        dialogState.value = state.copy(mappedAppIds = newList)
                                    }.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = isChecked, onCheckedChange = { checked ->
                                        val newList = if (state.appTargetMode == "One App") listOf(app.id) else {
                                            if (checked) state.mappedAppIds + app.id else state.mappedAppIds.filter { it != app.id }
                                        }
                                        dialogState.value = state.copy(mappedAppIds = newList)
                                    }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB)))
                                    Text(app.name, fontSize = 13.sp, color = Color(0xFF334155))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Target Screen
                    Text("2. Target Screen", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = state.screenName, onValueChange = {}, readOnly = true,
                            trailingIcon = { IconButton(onClick = { screenDropdownExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth().clickable { screenDropdownExpanded = true },
                            shape = RoundedCornerShape(10.dp)
                        )
                        DropdownMenu(expanded = screenDropdownExpanded, onDismissRequest = { screenDropdownExpanded = false }, modifier = Modifier.fillMaxWidth(0.8f)) {
                            screens.forEach { s -> DropdownMenuItem(text = { Text(s, fontSize = 14.sp) }, onClick = { dialogState.value = state.copy(screenName = s); screenDropdownExpanded = false }) }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Source Selection
                    Text("3. Background Source", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(4.dp)) {
                        listOf("Image URL" to false, "Local Storage" to true).forEach { (label, isLocal) ->
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp))
                                    .background(if (state.isLocalMode == isLocal) Color.White else Color.Transparent)
                                    .clickable { dialogState.value = state.copy(isLocalMode = isLocal) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (state.isLocalMode == isLocal) Color(0xFF1E293B) else Color(0xFF64748B))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    if (!state.isLocalMode) {
                        OutlinedTextField(value = state.url, onValueChange = { dialogState.value = state.copy(url = it) }, label = { Text("Enter Image URL") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                    } else {
                        Button(
                            onClick = { photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF334155)),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)), shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Upload, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (state.localImageUriString.isNotEmpty()) "Change Image" else "Select from Device")
                        }
                    }

                    // 4. Recommended Dimensions, Helper & Preview
                    val activeUrl = if (state.isLocalMode) state.localImageUriString else state.url
                    val wallpaperSpec = when {
                        state.screenName.contains("Tablet", ignoreCase = true) -> UploadImageType.TABLET_WALLPAPER
                        state.screenName.contains("Mobile", ignoreCase = true) -> UploadImageType.MOBILE_WALLPAPER
                        else -> UploadImageType.APP_WALLPAPER
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    RecommendedImageSizeHelper(
                        spec = wallpaperSpec,
                        imageSource = activeUrl,
                        showContainerPreview = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Status", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF334155))
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(checked = state.isEnabled, onCheckedChange = { dialogState.value = state.copy(isEnabled = it) })
                        }
                        Row {
                            TextButton(onClick = { showCreateOrEditDialog = false; dialogState.value = null }) { Text("Cancel", color = Color(0xFF64748B)) }
                            Button(
                                onClick = {
                                    val targetUrl = if (state.isLocalMode) state.localImageUriString else state.url
                                    if (targetUrl.isEmpty()) { Toast.makeText(context, "Provide an image", Toast.LENGTH_SHORT).show(); return@Button }
                                    val newWallpaper = (wallpaperToEdit?.copy(url = targetUrl, screenName = state.screenName, isEnabled = state.isEnabled, mappedAppIds = state.mappedAppIds)
                                        ?: AppWallpaperModel(url = targetUrl, screenName = state.screenName, isEnabled = state.isEnabled, mappedAppIds = state.mappedAppIds))
                                    if (wallpaperToEdit == null) SampleData.addAppWallpaper(newWallpaper) else SampleData.updateAppWallpaper(newWallpaper)
                                    showCreateOrEditDialog = false; dialogState.value = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)), shape = RoundedCornerShape(8.dp)
                            ) { Text("Apply Change") }
                        }
                    }
                }
            }
        }
    }

    // DELETE CONFIRMATION DIALOG
    if (wallpaperToDelete != null) {
        AlertDialog(
            onDismissRequest = { wallpaperToDelete = null }, containerColor = Color.White, shape = RoundedCornerShape(16.dp),
            title = { Row { Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444)); Spacer(modifier = Modifier.width(8.dp)); Text("Delete Wallpaper?", fontWeight = FontWeight.Bold, fontSize = 16.sp) } },
            text = { Text("Are you sure you want to delete the wallpaper for \"${wallpaperToDelete?.screenName}\"?") },
            confirmButton = {
                Button(onClick = { SampleData.deleteAppWallpaper(wallpaperToDelete!!.id); wallpaperToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)), shape = RoundedCornerShape(8.dp)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { wallpaperToDelete = null }) { Text("Cancel", color = Color(0xFF64748B)) } }
        )
    }
}

data class DialogState(
    val screenName: String,
    val mappedAppIds: List<String>,
    val isLocalMode: Boolean,
    val url: String,
    val localImageUriString: String,
    val isEnabled: Boolean,
    val appTargetMode: String
)
