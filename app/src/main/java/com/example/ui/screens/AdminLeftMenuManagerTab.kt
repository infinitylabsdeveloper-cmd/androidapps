package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.*
import com.example.ui.components.AppTargetSelector
import com.example.ui.components.ImageUploadGuidelinesDialog
import com.example.ui.components.LeftNavigationDrawerContent
import com.example.ui.components.RecommendedImageSizeHelper
import com.example.ui.components.UploadImageType
import com.example.ui.components.getIconByName
import com.example.util.ImageStorageManager
import kotlinx.coroutines.launch

private fun parseHexToLong(hex: String, default: Long): Long {
    return try {
        val clean = hex.trim().removePrefix("#").removePrefix("0x").removePrefix("0X")
        when (clean.length) {
            6 -> ("FF" + clean).toLong(16)
            8 -> clean.toLong(16)
            else -> default
        }
    } catch (e: Exception) {
        default
    }
}

private fun longToHexStr(color: Long): String {
    return String.format("#%06X", color and 0xFFFFFFL)
}

@Composable
fun AdminLeftMenuManagerTab() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentActiveAppId = SampleData.currentActiveAppId.value
    var targetScope by remember { mutableStateOf(TargetScopeType.SINGLE_APP) }
    var selectedAppIds by remember { mutableStateOf(listOf(currentActiveAppId)) }

    val savedConfig = remember(targetScope, selectedAppIds, currentActiveAppId) {
        SampleData.getDrawerConfigForApp(selectedAppIds.firstOrNull() ?: currentActiveAppId)
    }

    var drawerHeader by remember(savedConfig) { mutableStateOf(savedConfig.header) }
    var drawerSections by remember(savedConfig) { mutableStateOf(savedConfig.sections) }
    var menuItems by remember(savedConfig) { mutableStateOf(savedConfig.items) }

    var showLivePreview by remember { mutableStateOf(true) }
    var previewDarkMode by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(2) } // 0: Header, 1: Sections, 2: Menu Items
    var searchQuery by remember { mutableStateOf("") }
    var filterSectionKey by remember { mutableStateOf("ALL") }
    var expandedItemKey by remember { mutableStateOf<String?>(null) }
    var imageTargetItemIndex by remember { mutableIntStateOf(-1) }
    var showGlobalGuidelines by remember { mutableStateOf(false) }

    if (showGlobalGuidelines) {
        ImageUploadGuidelinesDialog(
            currentSpec = UploadImageType.HEADER_BANNER,
            onDismiss = { showGlobalGuidelines = false }
        )
    }

    // Launcher for Header background image
    val headerImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val savedPath = ImageStorageManager.persistPickedImage(context, uri, "drawer_header")
                val finalUrl = savedPath ?: uri.toString()
                drawerHeader = drawerHeader.copy(
                    backgroundImageUrl = finalUrl,
                    showBackgroundImage = true
                )
                Toast.makeText(context, "📷 Header background image updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher for Menu Card background image
    val cardImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && imageTargetItemIndex in menuItems.indices) {
            coroutineScope.launch {
                val item = menuItems[imageTargetItemIndex]
                val savedPath = ImageStorageManager.persistPickedImage(context, uri, "drawer_card_${item.id}")
                val finalUrl = savedPath ?: uri.toString()
                val mutable = menuItems.toMutableList()
                mutable[imageTargetItemIndex] = item.copy(
                    cardImageUrl = finalUrl,
                    showCardImage = true
                )
                menuItems = mutable
                Toast.makeText(context, "🖼️ Card background image updated for '${item.title}'!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val pendingConfig = DrawerConfig(
        isEnabled = true,
        targetScope = when (targetScope) {
            TargetScopeType.ALL_APPS -> "ALL"
            TargetScopeType.SELECT_APPS -> "SELECTED"
            TargetScopeType.SINGLE_APP -> "CURRENT"
        },
        selectedAppIds = selectedAppIds,
        header = drawerHeader,
        sections = drawerSections,
        items = menuItems
    )

    fun saveConfiguration() {
        SampleData.updateDrawerConfig(
            config = pendingConfig,
            scope = targetScope,
            selectedAppIds = selectedAppIds
        )
        val targetAppId = selectedAppIds.firstOrNull() ?: currentActiveAppId
        com.example.util.FirebaseSyncManager.syncDrawerConfigToFirestore(targetAppId, pendingConfig)
        val scopeLabel = when (targetScope) {
            TargetScopeType.ALL_APPS -> "ALL APPS (Global)"
            TargetScopeType.SELECT_APPS -> "${selectedAppIds.size} Selected Apps"
            TargetScopeType.SINGLE_APP -> "Selected App"
        }
        Toast.makeText(context, "✅ Left Menu Appearance saved & synchronized with User App ($scopeLabel)!", Toast.LENGTH_SHORT).show()
    }

    fun restoreDefaultReferenceDesign() {
        SampleData.resetDrawerConfig(
            scope = targetScope,
            targetAppId = selectedAppIds.firstOrNull() ?: currentActiveAppId,
            selectedAppIds = selectedAppIds
        )
        val defaultConfig = DrawerConfig()
        drawerHeader = defaultConfig.header
        drawerSections = defaultConfig.sections
        menuItems = defaultConfig.items
        Toast.makeText(context, "🔄 Restored 100% Original Reference Design Defaults!", Toast.LENGTH_LONG).show()
    }

    fun discardChanges() {
        drawerHeader = savedConfig.header
        drawerSections = savedConfig.sections
        menuItems = savedConfig.items
        Toast.makeText(context, "↩️ Unsaved changes discarded!", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. TOP HEADER & QUICK ACTIONS
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Premium Menu Appearance & Content Manager",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Full typography, colors, gradients, images, borders, icons & badges",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981)
                        ) {
                            Text(
                                text = "SUPER ADMIN",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Scope Indicator Banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Hub, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (targetScope) {
                                        TargetScopeType.ALL_APPS -> "TARGET SCOPE: ALL APPS (GLOBAL OVERRIDE)"
                                        TargetScopeType.SELECT_APPS -> "TARGET SCOPE: ${selectedAppIds.size} SELECTED APPS"
                                        TargetScopeType.SINGLE_APP -> "TARGET SCOPE: INDIVIDUAL APP (${selectedAppIds.firstOrNull() ?: currentActiveAppId})"
                                    },
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { saveConfiguration() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save & Apply", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showGlobalGuidelines = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF38BDF8))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guidelines", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showLivePreview = !showLivePreview },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Icon(if (showLivePreview) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showLivePreview) "Hide Preview" else "Show Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { discardChanges() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF1F5F9)),
                            border = BorderStroke(1.dp, Color(0xFF64748B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                        }

                        OutlinedButton(
                            onClick = { restoreDefaultReferenceDesign() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // 2. SCOPE SELECTOR COMPONENT
        item {
            AppTargetSelector(
                scope = targetScope,
                selectedAppIds = selectedAppIds,
                onScopeChange = { newScope ->
                    targetScope = newScope
                    if (newScope == TargetScopeType.ALL_APPS) {
                        selectedAppIds = SampleData.appsListState.map { it.id }
                    }
                },
                onAppToggled = { appId, isChecked ->
                    val mutable = selectedAppIds.toMutableList()
                    if (isChecked) {
                        if (!mutable.contains(appId)) mutable.add(appId)
                    } else {
                        mutable.remove(appId)
                    }
                    selectedAppIds = if (mutable.isEmpty()) listOf(currentActiveAppId) else mutable
                },
                onSelectAll = {
                    selectedAppIds = SampleData.appsListState.map { it.id }
                },
                onDeselectAll = {
                    selectedAppIds = listOf(currentActiveAppId)
                },
                onSingleAppSelected = { appId ->
                    selectedAppIds = listOf(appId)
                },
                title = "1. Choose Application Apply Scope",
                subtitle = "Select whether edits apply to an Individual App, Multiple Apps, or All Apps globally"
            )
        }

        // 3. LIVE INTERACTIVE PREVIEW PANEL
        item {
            AnimatedVisibility(
                visible = showLivePreview,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color(0xFF0284C7)),
                    elevation = CardDefaults.cardElevation(3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Live Interactive Preview",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Dark Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.width(4.dp))
                                Switch(
                                    checked = previewDarkMode,
                                    onCheckedChange = { previewDarkMode = it },
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }

                        // Live Drawer Frame Container
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (previewDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(460.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            LeftNavigationDrawerContent(
                                drawerConfig = pendingConfig,
                                isDarkMode = previewDarkMode,
                                onCloseDrawer = {},
                                onNavigateToHome = {},
                                onNavigateToProfile = {},
                                onNavigateToMockTestGenerator = {},
                                onNavigateToAvailableMockTests = {},
                                onNavigateToCurrentAffairs = {},
                                onNavigateToPerformance = {},
                                onNavigateToLeaderboard = {},
                                onNavigateToOfflineManager = {},
                                onNavigateToSubscription = {},
                                onNavigateToScientificCalculator = {},
                                onNavigateToPrivacySection = {},
                                onNavigateToLogin = {},
                                onNavigateToAdminPanel = {},
                                onShareApp = {}
                            )
                        }
                    }
                }
            }
        }

        // 4. NAVIGATION TABS FOR EDITOR SECTIONS
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF0284C7),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Drawer Header", fontWeight = FontWeight.Bold, fontSize = 12.5.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Section Headers", fontWeight = FontWeight.Bold, fontSize = 12.5.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Menu Options (${menuItems.size})", fontWeight = FontWeight.Bold, fontSize = 12.5.sp) }
                )
            }
        }

        // =========================================================================
        // TAB 0: DRAWER HEADER CUSTOMIZER
        // =========================================================================
        if (selectedTab == 0) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Header Branding & Typography", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF0F172A))

                        OutlinedTextField(
                            value = drawerHeader.title,
                            onValueChange = { drawerHeader = drawerHeader.copy(title = it) },
                            label = { Text("Header Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FontSelectorDropdown(
                                selectedFont = drawerHeader.fontFamily,
                                onFontSelected = { drawerHeader = drawerHeader.copy(fontFamily = it) },
                                modifier = Modifier.weight(1f)
                            )
                            FontWeightSelectorDropdown(
                                selectedWeight = drawerHeader.titleFontWeight,
                                onWeightSelected = { drawerHeader = drawerHeader.copy(titleFontWeight = it) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        HexColorPickerRow(
                            label = "Title Text Color",
                            currentHex = longToHexStr(drawerHeader.titleTextColorHex),
                            onHexChanged = { drawerHeader = drawerHeader.copy(titleTextColorHex = parseHexToLong(it, 0xFFFEF08A)) }
                        )

                        OutlinedTextField(
                            value = drawerHeader.quotation,
                            onValueChange = { drawerHeader = drawerHeader.copy(quotation = it) },
                            label = { Text("Quotation Text") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = drawerHeader.sloganTagline,
                            onValueChange = { drawerHeader = drawerHeader.copy(sloganTagline = it) },
                            label = { Text("Watermark Script Tagline") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        Text("Header Background & Overlay", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF0F172A))

                        OutlinedTextField(
                            value = drawerHeader.backgroundImageUrl,
                            onValueChange = { drawerHeader = drawerHeader.copy(backgroundImageUrl = it, showBackgroundImage = it.isNotBlank()) },
                            label = { Text("Image URL or Uploaded Path") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    headerImagePickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Background Image")
                            }

                            if (drawerHeader.backgroundImageUrl.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { drawerHeader = drawerHeader.copy(backgroundImageUrl = "", showBackgroundImage = false) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Remove")
                                }
                            }
                        }

                        // Recommended Size Spec & Preview for Drawer Header
                        RecommendedImageSizeHelper(
                            spec = UploadImageType.HEADER_BANNER,
                            imageSource = drawerHeader.backgroundImageUrl,
                            showContainerPreview = true
                        )

                        Text("Header Height: ${drawerHeader.headerHeightDp} dp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Slider(
                            value = drawerHeader.headerHeightDp.toFloat(),
                            onValueChange = { drawerHeader = drawerHeader.copy(headerHeightDp = it.toInt()) },
                            valueRange = 120f..220f
                        )

                        Text("Dark Overlay Opacity: ${(drawerHeader.overlayOpacity * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Slider(
                            value = drawerHeader.overlayOpacity,
                            onValueChange = { drawerHeader = drawerHeader.copy(overlayOpacity = it) },
                            valueRange = 0.0f..0.8f
                        )
                    }
                }
            }
        }

        // =========================================================================
        // TAB 1: SECTION HEADERS CUSTOMIZER
        // =========================================================================
        if (selectedTab == 1) {
            itemsIndexed(drawerSections) { sIndex, section ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Section: ${section.sectionKey}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0284C7))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Visible", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = section.isVisible,
                                    onCheckedChange = { checked ->
                                        val mutable = drawerSections.toMutableList()
                                        mutable[sIndex] = section.copy(isVisible = checked)
                                        drawerSections = mutable
                                    }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = section.title,
                            onValueChange = { newTitle ->
                                val mutable = drawerSections.toMutableList()
                                mutable[sIndex] = section.copy(title = newTitle)
                                drawerSections = mutable
                            },
                            label = { Text("Section Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = section.scriptText ?: "",
                            onValueChange = { newScript ->
                                val mutable = drawerSections.toMutableList()
                                mutable[sIndex] = section.copy(scriptText = if (newScript.isBlank()) null else newScript)
                                drawerSections = mutable
                            },
                            label = { Text("Right Script Tagline") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FontSelectorDropdown(
                                selectedFont = section.fontFamily,
                                onFontSelected = { font ->
                                    val mutable = drawerSections.toMutableList()
                                    mutable[sIndex] = section.copy(fontFamily = font)
                                    drawerSections = mutable
                                },
                                modifier = Modifier.weight(1f)
                            )
                            IconPickerDropdown(
                                selectedIcon = section.iconName,
                                onIconSelected = { ic ->
                                    val mutable = drawerSections.toMutableList()
                                    mutable[sIndex] = section.copy(iconName = ic)
                                    drawerSections = mutable
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB 2: MENU ITEMS CUSTOMIZER (ALL 22+ OPTIONS)
        // =========================================================================
        if (selectedTab == 2) {
            // Search & Filter Bar
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search menu option by title or subtitle...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = null)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val sectionsList = listOf("ALL", "MAIN", "EXAM_HUB", "EXAM_TOOLS", "LEGAL_SUPPORT")
                            items(sectionsList.size) { idx ->
                                val sec = sectionsList[idx]
                                FilterChip(
                                    selected = filterSectionKey == sec,
                                    onClick = { filterSectionKey = sec },
                                    label = { Text(sec) }
                                )
                            }
                        }
                    }
                }
            }

            val filteredItems = menuItems.filter { item ->
                val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                        (item.subtitle?.contains(searchQuery, ignoreCase = true) == true)
                val matchesSection = filterSectionKey == "ALL" || item.sectionKey == filterSectionKey
                matchesSearch && matchesSection
            }

            itemsIndexed(filteredItems) { _, item ->
                val actualIndex = menuItems.indexOfFirst { it.id == item.id }
                val isExpanded = expandedItemKey == item.id

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.2.dp, if (isExpanded) Color(0xFF0284C7) else Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(if (isExpanded) 4.dp else 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Summary Bar (Always Visible)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedItemKey = if (isExpanded) null else item.id }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(item.iconBoxBgColorHex),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = getIconByName(item.iconName, Icons.Default.Circle),
                                            contentDescription = null,
                                            tint = Color(item.iconTintHex),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                    Text(
                                        text = "${item.sectionKey} • Font: ${item.fontFamily} • ${item.fontWeight}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Switch(
                                    checked = item.isVisible,
                                    onCheckedChange = { checked ->
                                        if (actualIndex != -1) {
                                            val mutable = menuItems.toMutableList()
                                            mutable[actualIndex] = item.copy(isVisible = checked)
                                            menuItems = mutable
                                        }
                                    }
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B)
                                )
                            }
                        }

                        // Expanded Editor Form
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HorizontalDivider(color = Color(0xFFE2E8F0))

                                // Reordering Bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Reorder Index: ${item.order}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                if (actualIndex > 0) {
                                                    val mutable = menuItems.toMutableList()
                                                    val prev = mutable[actualIndex - 1]
                                                    mutable[actualIndex - 1] = item.copy(order = prev.order)
                                                    mutable[actualIndex] = prev.copy(order = item.order)
                                                    menuItems = mutable
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Text("Up", fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                if (actualIndex < menuItems.size - 1) {
                                                    val mutable = menuItems.toMutableList()
                                                    val next = mutable[actualIndex + 1]
                                                    mutable[actualIndex + 1] = item.copy(order = next.order)
                                                    mutable[actualIndex] = next.copy(order = item.order)
                                                    menuItems = mutable
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Text("Down", fontSize = 11.sp)
                                        }
                                    }
                                }

                                // 1. Text Editing
                                Text("Text Content & Subtitle", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF0284C7))
                                OutlinedTextField(
                                    value = item.title,
                                    onValueChange = { newTitle ->
                                        if (actualIndex != -1) {
                                            val mutable = menuItems.toMutableList()
                                            mutable[actualIndex] = item.copy(title = newTitle)
                                            menuItems = mutable
                                        }
                                    },
                                    label = { Text("Menu Title") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = item.subtitle ?: "",
                                    onValueChange = { newSub ->
                                        if (actualIndex != -1) {
                                            val mutable = menuItems.toMutableList()
                                            mutable[actualIndex] = item.copy(subtitle = if (newSub.isBlank()) null else newSub)
                                            menuItems = mutable
                                        }
                                    },
                                    label = { Text("Subtitle") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // 2. Typography Customization
                                Text("Typography & Font Styling", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF0284C7))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FontSelectorDropdown(
                                        selectedFont = item.fontFamily,
                                        onFontSelected = { newFont ->
                                            if (actualIndex != -1) {
                                                val mutable = menuItems.toMutableList()
                                                mutable[actualIndex] = item.copy(fontFamily = newFont)
                                                menuItems = mutable
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FontWeightSelectorDropdown(
                                        selectedWeight = item.fontWeight,
                                        onWeightSelected = { newWeight ->
                                            if (actualIndex != -1) {
                                                val mutable = menuItems.toMutableList()
                                                mutable[actualIndex] = item.copy(fontWeight = newWeight)
                                                menuItems = mutable
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Title Size: ${item.titleFontSizeSp}sp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Slider(
                                            value = item.titleFontSizeSp,
                                            onValueChange = { newSz ->
                                                if (actualIndex != -1) {
                                                    val mutable = menuItems.toMutableList()
                                                    mutable[actualIndex] = item.copy(titleFontSizeSp = newSz)
                                                    menuItems = mutable
                                                }
                                            },
                                            valueRange = 10f..22f
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Subtitle Size: ${item.subtitleFontSizeSp}sp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Slider(
                                            value = item.subtitleFontSizeSp,
                                            onValueChange = { newSz ->
                                                if (actualIndex != -1) {
                                                    val mutable = menuItems.toMutableList()
                                                    mutable[actualIndex] = item.copy(subtitleFontSizeSp = newSz)
                                                    menuItems = mutable
                                                }
                                            },
                                            valueRange = 8f..18f
                                        )
                                    }
                                }

                                HexColorPickerRow(
                                    label = "Title Color (Optional)",
                                    currentHex = item.titleTextColorHex?.let { longToHexStr(it) } ?: "#0F172A",
                                    onHexChanged = { hex ->
                                        if (actualIndex != -1) {
                                            val mutable = menuItems.toMutableList()
                                            mutable[actualIndex] = item.copy(titleTextColorHex = parseHexToLong(hex, 0xFF0F172A))
                                            menuItems = mutable
                                        }
                                    }
                                )

                                // 3. Card Background & Premium Gradients
                                Text("Card Background & Gradients", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF0284C7))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Use Premium Gradient Background", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Switch(
                                        checked = item.useGradient,
                                        onCheckedChange = { useGrad ->
                                            if (actualIndex != -1) {
                                                val mutable = menuItems.toMutableList()
                                                mutable[actualIndex] = item.copy(useGradient = useGrad)
                                                menuItems = mutable
                                            }
                                        }
                                    )
                                }

                                if (item.useGradient) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            HexColorPickerRow(
                                                label = "Gradient Start",
                                                currentHex = longToHexStr(item.gradientStartColorHex),
                                                onHexChanged = { h ->
                                                    if (actualIndex != -1) {
                                                        val mutable = menuItems.toMutableList()
                                                        mutable[actualIndex] = item.copy(gradientStartColorHex = parseHexToLong(h, 0xFF3B82F6))
                                                        menuItems = mutable
                                                    }
                                                }
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            HexColorPickerRow(
                                                label = "Gradient End",
                                                currentHex = longToHexStr(item.gradientEndColorHex),
                                                onHexChanged = { h ->
                                                    if (actualIndex != -1) {
                                                        val mutable = menuItems.toMutableList()
                                                        mutable[actualIndex] = item.copy(gradientEndColorHex = parseHexToLong(h, 0xFF1D4ED8))
                                                        menuItems = mutable
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    // Gradient Direction Selection
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Diagonal", "Horizontal", "Vertical").forEach { dir ->
                                            FilterChip(
                                                selected = item.gradientDirection == dir,
                                                onClick = {
                                                    if (actualIndex != -1) {
                                                        val mutable = menuItems.toMutableList()
                                                        mutable[actualIndex] = item.copy(gradientDirection = dir)
                                                        menuItems = mutable
                                                    }
                                                },
                                                label = { Text(dir, fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                } else {
                                    HexColorPickerRow(
                                        label = "Solid Background Color",
                                        currentHex = longToHexStr(item.cardBgColorHex),
                                        onHexChanged = { h ->
                                            if (actualIndex != -1) {
                                                val mutable = menuItems.toMutableList()
                                                mutable[actualIndex] = item.copy(cardBgColorHex = parseHexToLong(h, 0xFFFFFFFF))
                                                menuItems = mutable
                                            }
                                        }
                                    )
                                }

                                // 4. Background Image Upload & URL
                                Text("Card Background Image (Optional)", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF0284C7))
                                OutlinedTextField(
                                    value = item.cardImageUrl,
                                    onValueChange = { url ->
                                        if (actualIndex != -1) {
                                            val mutable = menuItems.toMutableList()
                                            mutable[actualIndex] = item.copy(cardImageUrl = url, showCardImage = url.isNotBlank())
                                            menuItems = mutable
                                        }
                                    },
                                    label = { Text("Image URL or File Path") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = {
                                            imageTargetItemIndex = actualIndex
                                            cardImagePickerLauncher.launch(
                                                androidx.activity.result.PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Upload Image", fontSize = 12.sp)
                                    }

                                    if (item.cardImageUrl.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                if (actualIndex != -1) {
                                                    val mutable = menuItems.toMutableList()
                                                    mutable[actualIndex] = item.copy(cardImageUrl = "", showCardImage = false)
                                                    menuItems = mutable
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Clear Image", fontSize = 12.sp)
                                        }
                                    }
                                }

                                // Recommended Size Spec & Preview for Menu Card
                                Spacer(modifier = Modifier.height(4.dp))
                                RecommendedImageSizeHelper(
                                    spec = UploadImageType.MENU_FEATURE_IMAGE,
                                    imageSource = item.cardImageUrl,
                                    showContainerPreview = true
                                )

                                if (item.showCardImage && item.cardImageUrl.isNotBlank()) {
                                    Text("Image Opacity: ${(item.cardImageOpacity * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Slider(
                                        value = item.cardImageOpacity,
                                        onValueChange = { op ->
                                            if (actualIndex != -1) {
                                                val mutable = menuItems.toMutableList()
                                                mutable[actualIndex] = item.copy(cardImageOpacity = op)
                                                menuItems = mutable
                                            }
                                        },
                                        valueRange = 0.05f..1.0f
                                    )
                                }

                                // 5. Icon & Icon Box Customization
                                Text("Icon & Icon Box Customization", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF0284C7))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconPickerDropdown(
                                        selectedIcon = item.iconName,
                                        onIconSelected = { ic ->
                                            if (actualIndex != -1) {
                                                val mutable = menuItems.toMutableList()
                                                mutable[actualIndex] = item.copy(iconName = ic)
                                                menuItems = mutable
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    HexColorPickerRow(
                                        label = "Icon Tint",
                                        currentHex = longToHexStr(item.iconTintHex),
                                        onHexChanged = { h ->
                                            if (actualIndex != -1) {
                                                val mutable = menuItems.toMutableList()
                                                mutable[actualIndex] = item.copy(iconTintHex = parseHexToLong(h, 0xFF2563EB))
                                                menuItems = mutable
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    HexColorPickerRow(
                                        label = "Icon Box BG",
                                        currentHex = longToHexStr(item.iconBoxBgColorHex),
                                        onHexChanged = { h ->
                                            if (actualIndex != -1) {
                                                val mutable = menuItems.toMutableList()
                                                mutable[actualIndex] = item.copy(iconBoxBgColorHex = parseHexToLong(h, 0xFFEFF6FF))
                                                menuItems = mutable
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Box Corner Radius: ${item.iconBoxCornerRadiusDp}dp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Slider(
                                            value = item.iconBoxCornerRadiusDp.toFloat(),
                                            onValueChange = { rad ->
                                                if (actualIndex != -1) {
                                                    val mutable = menuItems.toMutableList()
                                                    mutable[actualIndex] = item.copy(iconBoxCornerRadiusDp = rad.toInt())
                                                    menuItems = mutable
                                                }
                                            },
                                            valueRange = 0f..20f
                                        )
                                    }
                                }

                                // 6. Border & Corner Radius
                                Text("Borders & Card Radius", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF0284C7))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    HexColorPickerRow(
                                        label = "Border Color",
                                        currentHex = longToHexStr(item.cardBorderColorHex),
                                        onHexChanged = { h ->
                                            if (actualIndex != -1) {
                                                val mutable = menuItems.toMutableList()
                                                mutable[actualIndex] = item.copy(cardBorderColorHex = parseHexToLong(h, 0xFFE2E8F0))
                                                menuItems = mutable
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Border Width: ${item.borderThicknessDp}dp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Slider(
                                            value = item.borderThicknessDp.toFloat(),
                                            onValueChange = { w ->
                                                if (actualIndex != -1) {
                                                    val mutable = menuItems.toMutableList()
                                                    mutable[actualIndex] = item.copy(borderThicknessDp = w.toInt())
                                                    menuItems = mutable
                                                }
                                            },
                                            valueRange = 0f..6f
                                        )
                                    }
                                }

                                Text("Card Corner Radius: ${item.cornerRadiusDp}dp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = item.cornerRadiusDp.toFloat(),
                                    onValueChange = { r ->
                                        if (actualIndex != -1) {
                                            val mutable = menuItems.toMutableList()
                                            mutable[actualIndex] = item.copy(cornerRadiusDp = r.toInt())
                                            menuItems = mutable
                                        }
                                    },
                                    valueRange = 0f..32f
                                )

                                // 7. Badge / Status Pill
                                Text("Badge Pill & Status Text", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF0284C7))
                                OutlinedTextField(
                                    value = item.badgeText ?: "",
                                    onValueChange = { b ->
                                        if (actualIndex != -1) {
                                            val mutable = menuItems.toMutableList()
                                            mutable[actualIndex] = item.copy(badgeText = if (b.isBlank()) null else b)
                                            menuItems = mutable
                                        }
                                    },
                                    label = { Text("Badge Text (e.g. LIVE, NEW, PRO, HOT)") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (!item.badgeText.isNullOrBlank()) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        HexColorPickerRow(
                                            label = "Badge BG Color",
                                            currentHex = longToHexStr(item.badgeBgColorHex),
                                            onHexChanged = { h ->
                                                if (actualIndex != -1) {
                                                    val mutable = menuItems.toMutableList()
                                                    mutable[actualIndex] = item.copy(badgeBgColorHex = parseHexToLong(h, 0xFF2563EB))
                                                    menuItems = mutable
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )

                                        HexColorPickerRow(
                                            label = "Badge Text Color",
                                            currentHex = longToHexStr(item.badgeTextColorHex),
                                            onHexChanged = { h ->
                                                if (actualIndex != -1) {
                                                    val mutable = menuItems.toMutableList()
                                                    mutable[actualIndex] = item.copy(badgeTextColorHex = parseHexToLong(h, 0xFFFFFFFF))
                                                    menuItems = mutable
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
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

// =========================================================================
// HELPER COMPOSABLES FOR ADMIN UI
// =========================================================================

@Composable
fun HexColorPickerRow(
    label: String,
    currentHex: String,
    onHexChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textVal by remember(currentHex) { mutableStateOf(currentHex) }

    val presetColors = listOf(
        "#2563EB", "#1D4ED8", "#4F46E5", "#7C3AED",
        "#059669", "#D97706", "#DC2626", "#0F172A",
        "#FEF08A", "#E2E8F0", "#FFFFFF"
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = textVal,
            onValueChange = {
                textVal = it
                onHexChanged(it)
            },
            label = { Text(label, fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(presetColors.size) { idx ->
                val hex = presetColors[idx]
                val colorLong = parseHexToLong(hex, 0xFFFFFFFF)
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(colorLong))
                        .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                        .clickable {
                            textVal = hex
                            onHexChanged(hex)
                        }
                )
            }
        }
    }
}

@Composable
fun FontSelectorDropdown(
    selectedFont: String,
    onFontSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val fontOptions = listOf(
        "Inter", "Outfit", "Poppins", "Lexend", "Roboto",
        "Playfair Display", "Plus Jakarta Sans", "Cinzel",
        "Space Grotesk", "Serif", "Monospace", "Cursive"
    )

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedFont,
            onValueChange = {},
            readOnly = true,
            label = { Text("Font Family", fontSize = 11.sp) },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            fontOptions.forEach { font ->
                DropdownMenuItem(
                    text = { Text(font, fontWeight = if (font == selectedFont) FontWeight.Bold else FontWeight.Normal) },
                    onClick = {
                        onFontSelected(font)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FontWeightSelectorDropdown(
    selectedWeight: String,
    onWeightSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val weightOptions = listOf("Light", "Normal", "Medium", "SemiBold", "Bold", "ExtraBold", "Black")

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedWeight,
            onValueChange = {},
            readOnly = true,
            label = { Text("Font Weight", fontSize = 11.sp) },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            weightOptions.forEach { w ->
                DropdownMenuItem(
                    text = { Text(w, fontWeight = if (w == selectedWeight) FontWeight.Bold else FontWeight.Normal) },
                    onClick = {
                        onWeightSelected(w)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun IconPickerDropdown(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val iconOptions = listOf(
        "person", "home", "auto_awesome", "assignment", "newspaper",
        "bar_chart", "emoji_events", "bolt", "cloud_sync", "workspace_premium",
        "calculate", "share", "alarm", "receipt_long", "security",
        "description", "quiz", "info", "contact_support", "help",
        "school", "menu_book", "settings", "verified_user", "star"
    )

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedIcon,
            onValueChange = {},
            readOnly = true,
            label = { Text("Icon Name", fontSize = 11.sp) },
            leadingIcon = {
                Icon(
                    imageVector = getIconByName(selectedIcon, Icons.Default.Circle),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            iconOptions.forEach { ic ->
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = getIconByName(ic, Icons.Default.Circle),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    text = { Text(ic) },
                    onClick = {
                        onIconSelected(ic)
                        expanded = false
                    }
                )
            }
        }
    }
}
