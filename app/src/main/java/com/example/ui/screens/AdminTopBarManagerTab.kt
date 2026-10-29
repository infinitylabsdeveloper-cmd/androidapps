package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.TargetScopeType
import com.example.model.TopBarConfig
import com.example.ui.components.AppTargetSelector
import com.example.ui.components.PremiumTopBar
import com.example.ui.components.RecommendedImageSizeHelper
import com.example.ui.components.UploadImageType
import com.example.ui.components.parseHexColor
import com.example.util.FirebaseSyncManager
import com.example.util.ImageStorageManager
import kotlinx.coroutines.launch

@Composable
fun AdminTopBarManagerTab() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeApp = SampleData.getActiveApp()
    val currentSavedConfig = SampleData.getTopBarConfigForApp(activeApp.id)
    val appsList = SampleData.appsListState

    // Section Category Selection:
    // 0: Content & Texts, 1: Typography & Styling, 2: Icons & Action Buttons,
    // 3: Backgrounds & Gradients, 4: Borders, Shadow & Layout, 5: App Scope
    var activeCategoryIndex by remember { mutableIntStateOf(0) }

    // Live editing states
    var isEnabled by remember { mutableStateOf(currentSavedConfig.isEnabled) }
    var title by remember { mutableStateOf(currentSavedConfig.title) }
    var subtitle by remember { mutableStateOf(currentSavedConfig.subtitle) }
    var additionalText by remember { mutableStateOf(currentSavedConfig.additionalText) }
    var premiumBadgeText by remember { mutableStateOf(currentSavedConfig.premiumBadgeText) }

    // Visibility toggles
    var showMenu by remember { mutableStateOf(currentSavedConfig.showMenu) }
    var showAppIcon by remember { mutableStateOf(currentSavedConfig.showAppIcon) }
    var showTitle by remember { mutableStateOf(currentSavedConfig.showTitle) }
    var showSubtitle by remember { mutableStateOf(currentSavedConfig.showSubtitle) }
    var showAdditionalText by remember { mutableStateOf(currentSavedConfig.showAdditionalText) }
    var showTextSettings by remember { mutableStateOf(currentSavedConfig.showTextSettings) }
    var showPremium by remember { mutableStateOf(currentSavedConfig.showPremium) }
    var showNotifications by remember { mutableStateOf(currentSavedConfig.showNotifications) }

    // Typography: Title
    var titleFontFamily by remember { mutableStateOf(currentSavedConfig.titleFontFamily) }
    var titleFontSizeSp by remember { mutableIntStateOf(currentSavedConfig.titleFontSizeSp) }
    var titleFontWeight by remember { mutableStateOf(currentSavedConfig.titleFontWeight) }
    var titleFontStyle by remember { mutableStateOf(currentSavedConfig.titleFontStyle) }
    var titleColorHex by remember { mutableStateOf(currentSavedConfig.titleColorHex) }

    // Typography: Subtitle
    var subtitleFontFamily by remember { mutableStateOf(currentSavedConfig.subtitleFontFamily) }
    var subtitleFontSizeSp by remember { mutableIntStateOf(currentSavedConfig.subtitleFontSizeSp) }
    var subtitleFontWeight by remember { mutableStateOf(currentSavedConfig.subtitleFontWeight) }
    var subtitleFontStyle by remember { mutableStateOf(currentSavedConfig.subtitleFontStyle) }
    var subtitleColorHex by remember { mutableStateOf(currentSavedConfig.subtitleColorHex) }

    // Typography: Additional Text
    var additionalFontFamily by remember { mutableStateOf(currentSavedConfig.additionalFontFamily) }
    var additionalFontSizeSp by remember { mutableIntStateOf(currentSavedConfig.additionalFontSizeSp) }
    var additionalFontWeight by remember { mutableStateOf(currentSavedConfig.additionalFontWeight) }
    var additionalFontStyle by remember { mutableStateOf(currentSavedConfig.additionalFontStyle) }
    var additionalColorHex by remember { mutableStateOf(currentSavedConfig.additionalColorHex) }

    var textAlignment by remember { mutableStateOf(currentSavedConfig.textAlignment) }

    // Layout & Sizing
    var headerHeightDp by remember { mutableIntStateOf(currentSavedConfig.headerHeightDp) }
    var headerPaddingHorizontalDp by remember { mutableIntStateOf(currentSavedConfig.headerPaddingHorizontalDp) }
    var headerPaddingVerticalDp by remember { mutableIntStateOf(currentSavedConfig.headerPaddingVerticalDp) }
    var headerMarginHorizontalDp by remember { mutableIntStateOf(currentSavedConfig.headerMarginHorizontalDp) }
    var cornerRadiusDp by remember { mutableIntStateOf(currentSavedConfig.cornerRadiusDp) }
    var elevationDp by remember { mutableIntStateOf(currentSavedConfig.elevationDp) }
    var glowEffectEnabled by remember { mutableStateOf(currentSavedConfig.glowEffectEnabled) }
    var glowColorHex by remember { mutableStateOf(currentSavedConfig.glowColorHex) }

    // Colors & Gradients
    var backgroundColorStringHex by remember { mutableStateOf(currentSavedConfig.backgroundColorStringHex) }
    var secondaryBackgroundColorHex by remember { mutableStateOf(currentSavedConfig.secondaryBackgroundColorHex) }
    var backgroundType by remember { mutableStateOf(currentSavedConfig.backgroundType) }
    var useGradient by remember { mutableStateOf(currentSavedConfig.useGradient) }
    var gradientPreset by remember { mutableStateOf(currentSavedConfig.gradientPreset) }
    var gradientStartColorHex by remember { mutableStateOf(currentSavedConfig.gradientStartColorHex) }
    var gradientEndColorHex by remember { mutableStateOf(currentSavedConfig.gradientEndColorHex) }
    var gradientDirection by remember { mutableStateOf(currentSavedConfig.gradientDirection) }

    // Background Image
    var backgroundImageUrl by remember { mutableStateOf(currentSavedConfig.backgroundImageUrl) }
    var imageOpacity by remember { mutableFloatStateOf(currentSavedConfig.imageOpacity) }
    var imageScale by remember { mutableStateOf(currentSavedConfig.imageScale) }
    var imageOverlayColorHex by remember { mutableStateOf(currentSavedConfig.imageOverlayColorHex) }
    var overlayOpacity by remember { mutableFloatStateOf(currentSavedConfig.overlayOpacity) }

    // Borders & Dividers
    var headerBorderColorHex by remember { mutableStateOf(currentSavedConfig.headerBorderColorHex) }
    var headerBorderThicknessDp by remember { mutableIntStateOf(currentSavedConfig.headerBorderThicknessDp) }
    var dividerColorHex by remember { mutableStateOf(currentSavedConfig.dividerColorHex) }
    var dividerStyle by remember { mutableStateOf(currentSavedConfig.dividerStyle) }

    // Action Buttons & Icons Styling
    var buttonBgColorHex by remember { mutableStateOf(currentSavedConfig.buttonBgColorHex) }
    var buttonBorderColorHex by remember { mutableStateOf(currentSavedConfig.buttonBorderColorHex) }
    var buttonIconColorHex by remember { mutableStateOf(currentSavedConfig.buttonIconColorHex) }
    var buttonBorderThicknessDp by remember { mutableIntStateOf(currentSavedConfig.buttonBorderThicknessDp) }
    var buttonCornerRadiusDp by remember { mutableIntStateOf(currentSavedConfig.buttonCornerRadiusDp) }
    var buttonSizeDp by remember { mutableIntStateOf(currentSavedConfig.buttonSizeDp) }

    // Specific Action Icons
    var iconSelectionMenu by remember { mutableStateOf(currentSavedConfig.iconSelectionMenu) }
    var iconSelectionAppIcon by remember { mutableStateOf(currentSavedConfig.iconSelectionAppIcon) }
    var iconSelectionPremium by remember { mutableStateOf(currentSavedConfig.iconSelectionPremium) }
    var iconSelectionNotifications by remember { mutableStateOf(currentSavedConfig.iconSelectionNotifications) }

    // Target Scope
    var targetScope by remember { mutableStateOf(currentSavedConfig.targetScope) }
    var selectedAppIds by remember { mutableStateOf(currentSavedConfig.selectedAppIds.ifEmpty { listOf(activeApp.id) }) }

    // Live preview canvas background
    var previewCanvasMode by remember { mutableStateOf("DARK") } // DARK, LIGHT, LUMINOUS

    // Image Picker from Local Storage
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val savedPath = ImageStorageManager.persistPickedImage(context, uri, "top_bar")
                backgroundImageUrl = savedPath ?: uri.toString()
                backgroundType = "IMAGE"
                Toast.makeText(context, "📷 Top bar background image uploaded successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Build current live configuration for preview and saving
    val liveConfig = TopBarConfig(
        isEnabled = isEnabled,
        title = title,
        subtitle = subtitle,
        additionalText = additionalText,
        premiumBadgeText = premiumBadgeText,
        showMenu = showMenu,
        showAppIcon = showAppIcon,
        showTitle = showTitle,
        showSubtitle = showSubtitle,
        showAdditionalText = showAdditionalText,
        showTextSettings = showTextSettings,
        showPremium = showPremium,
        showNotifications = showNotifications,
        titleFontFamily = titleFontFamily,
        titleFontSizeSp = titleFontSizeSp,
        titleFontWeight = titleFontWeight,
        titleColorHex = titleColorHex,
        titleFontStyle = titleFontStyle,
        subtitleFontFamily = subtitleFontFamily,
        subtitleFontSizeSp = subtitleFontSizeSp,
        subtitleFontWeight = subtitleFontWeight,
        subtitleColorHex = subtitleColorHex,
        subtitleFontStyle = subtitleFontStyle,
        additionalFontFamily = additionalFontFamily,
        additionalFontSizeSp = additionalFontSizeSp,
        additionalFontWeight = additionalFontWeight,
        additionalColorHex = additionalColorHex,
        additionalFontStyle = additionalFontStyle,
        textAlignment = textAlignment,
        headerHeightDp = headerHeightDp,
        headerPaddingHorizontalDp = headerPaddingHorizontalDp,
        headerPaddingVerticalDp = headerPaddingVerticalDp,
        headerMarginHorizontalDp = headerMarginHorizontalDp,
        cornerRadiusDp = cornerRadiusDp,
        elevationDp = elevationDp,
        glowEffectEnabled = glowEffectEnabled,
        glowColorHex = glowColorHex,
        backgroundColorStringHex = backgroundColorStringHex,
        secondaryBackgroundColorHex = secondaryBackgroundColorHex,
        useGradient = useGradient,
        gradientPreset = gradientPreset,
        gradientStartColorHex = gradientStartColorHex,
        gradientEndColorHex = gradientEndColorHex,
        gradientDirection = gradientDirection,
        backgroundType = backgroundType,
        backgroundImageUrl = backgroundImageUrl,
        imageOpacity = imageOpacity,
        imageScale = imageScale,
        imageOverlayColorHex = imageOverlayColorHex,
        overlayOpacity = overlayOpacity,
        headerBorderColorHex = headerBorderColorHex,
        headerBorderThicknessDp = headerBorderThicknessDp,
        dividerColorHex = dividerColorHex,
        dividerStyle = dividerStyle,
        buttonBgColorHex = buttonBgColorHex,
        buttonBorderColorHex = buttonBorderColorHex,
        buttonIconColorHex = buttonIconColorHex,
        buttonBorderThicknessDp = buttonBorderThicknessDp,
        buttonCornerRadiusDp = buttonCornerRadiusDp,
        buttonSizeDp = buttonSizeDp,
        iconSelectionMenu = iconSelectionMenu,
        iconSelectionAppIcon = iconSelectionAppIcon,
        iconSelectionPremium = iconSelectionPremium,
        iconSelectionNotifications = iconSelectionNotifications,
        targetScope = targetScope,
        selectedAppIds = selectedAppIds
    )

    fun applyAndSaveConfig() {
        val scopeType = when (targetScope) {
            "ALL" -> TargetScopeType.ALL_APPS
            "SELECTED" -> TargetScopeType.SELECT_APPS
            else -> TargetScopeType.SINGLE_APP
        }
        SampleData.updateTopBarConfig(liveConfig, scopeType, selectedAppIds)
        FirebaseSyncManager.syncTopBarConfigToFirestore(activeApp.id, liveConfig)
        val scopeMessage = when (scopeType) {
            TargetScopeType.ALL_APPS -> "All Apps (${appsList.size})"
            TargetScopeType.SELECT_APPS -> "${selectedAppIds.size} Selected Apps"
            TargetScopeType.SINGLE_APP -> activeApp.name
        }
        Toast.makeText(context, "✨ Top Bar configuration deployed to $scopeMessage!", Toast.LENGTH_LONG).show()
    }

    fun discardChanges() {
        val saved = SampleData.getTopBarConfigForApp(activeApp.id)
        isEnabled = saved.isEnabled
        title = saved.title
        subtitle = saved.subtitle
        additionalText = saved.additionalText
        premiumBadgeText = saved.premiumBadgeText
        showMenu = saved.showMenu
        showAppIcon = saved.showAppIcon
        showTitle = saved.showTitle
        showSubtitle = saved.showSubtitle
        showAdditionalText = saved.showAdditionalText
        showTextSettings = saved.showTextSettings
        showPremium = saved.showPremium
        showNotifications = saved.showNotifications
        titleFontFamily = saved.titleFontFamily
        titleFontSizeSp = saved.titleFontSizeSp
        titleFontWeight = saved.titleFontWeight
        titleColorHex = saved.titleColorHex
        titleFontStyle = saved.titleFontStyle
        subtitleFontFamily = saved.subtitleFontFamily
        subtitleFontSizeSp = saved.subtitleFontSizeSp
        subtitleFontWeight = saved.subtitleFontWeight
        subtitleColorHex = saved.subtitleColorHex
        subtitleFontStyle = saved.subtitleFontStyle
        additionalFontFamily = saved.additionalFontFamily
        additionalFontSizeSp = saved.additionalFontSizeSp
        additionalFontWeight = saved.additionalFontWeight
        additionalColorHex = saved.additionalColorHex
        additionalFontStyle = saved.additionalFontStyle
        textAlignment = saved.textAlignment
        headerHeightDp = saved.headerHeightDp
        headerPaddingHorizontalDp = saved.headerPaddingHorizontalDp
        headerPaddingVerticalDp = saved.headerPaddingVerticalDp
        headerMarginHorizontalDp = saved.headerMarginHorizontalDp
        cornerRadiusDp = saved.cornerRadiusDp
        elevationDp = saved.elevationDp
        glowEffectEnabled = saved.glowEffectEnabled
        glowColorHex = saved.glowColorHex
        backgroundColorStringHex = saved.backgroundColorStringHex
        secondaryBackgroundColorHex = saved.secondaryBackgroundColorHex
        useGradient = saved.useGradient
        gradientPreset = saved.gradientPreset
        gradientStartColorHex = saved.gradientStartColorHex
        gradientEndColorHex = saved.gradientEndColorHex
        gradientDirection = saved.gradientDirection
        backgroundType = saved.backgroundType
        backgroundImageUrl = saved.backgroundImageUrl
        imageOpacity = saved.imageOpacity
        imageScale = saved.imageScale
        imageOverlayColorHex = saved.imageOverlayColorHex
        overlayOpacity = saved.overlayOpacity
        headerBorderColorHex = saved.headerBorderColorHex
        headerBorderThicknessDp = saved.headerBorderThicknessDp
        dividerColorHex = saved.dividerColorHex
        dividerStyle = saved.dividerStyle
        buttonBgColorHex = saved.buttonBgColorHex
        buttonBorderColorHex = saved.buttonBorderColorHex
        buttonIconColorHex = saved.buttonIconColorHex
        buttonBorderThicknessDp = saved.buttonBorderThicknessDp
        buttonCornerRadiusDp = saved.buttonCornerRadiusDp
        buttonSizeDp = saved.buttonSizeDp
        iconSelectionMenu = saved.iconSelectionMenu
        iconSelectionAppIcon = saved.iconSelectionAppIcon
        iconSelectionPremium = saved.iconSelectionPremium
        iconSelectionNotifications = saved.iconSelectionNotifications
        targetScope = saved.targetScope
        selectedAppIds = saved.selectedAppIds
        Toast.makeText(context, "Unsaved changes discarded.", Toast.LENGTH_SHORT).show()
    }

    fun resetToDefaults() {
        val scopeType = when (targetScope) {
            "ALL" -> TargetScopeType.ALL_APPS
            "SELECTED" -> TargetScopeType.SELECT_APPS
            else -> TargetScopeType.SINGLE_APP
        }
        SampleData.resetTopBarConfig(scopeType, activeApp.id, selectedAppIds)
        val fresh = TopBarConfig()
        isEnabled = fresh.isEnabled
        title = fresh.title
        subtitle = fresh.subtitle
        additionalText = fresh.additionalText
        premiumBadgeText = fresh.premiumBadgeText
        showMenu = fresh.showMenu
        showAppIcon = fresh.showAppIcon
        showTitle = fresh.showTitle
        showSubtitle = fresh.showSubtitle
        showAdditionalText = fresh.showAdditionalText
        showTextSettings = fresh.showTextSettings
        showPremium = fresh.showPremium
        showNotifications = fresh.showNotifications
        titleFontFamily = fresh.titleFontFamily
        titleFontSizeSp = fresh.titleFontSizeSp
        titleFontWeight = fresh.titleFontWeight
        titleColorHex = fresh.titleColorHex
        titleFontStyle = fresh.titleFontStyle
        subtitleFontFamily = fresh.subtitleFontFamily
        subtitleFontSizeSp = fresh.subtitleFontSizeSp
        subtitleFontWeight = fresh.subtitleFontWeight
        subtitleColorHex = fresh.subtitleColorHex
        subtitleFontStyle = fresh.subtitleFontStyle
        additionalFontFamily = fresh.additionalFontFamily
        additionalFontSizeSp = fresh.additionalFontSizeSp
        additionalFontWeight = fresh.additionalFontWeight
        additionalColorHex = fresh.additionalColorHex
        additionalFontStyle = fresh.additionalFontStyle
        textAlignment = fresh.textAlignment
        headerHeightDp = fresh.headerHeightDp
        headerPaddingHorizontalDp = fresh.headerPaddingHorizontalDp
        headerPaddingVerticalDp = fresh.headerPaddingVerticalDp
        headerMarginHorizontalDp = fresh.headerMarginHorizontalDp
        cornerRadiusDp = fresh.cornerRadiusDp
        elevationDp = fresh.elevationDp
        glowEffectEnabled = fresh.glowEffectEnabled
        glowColorHex = fresh.glowColorHex
        backgroundColorStringHex = fresh.backgroundColorStringHex
        secondaryBackgroundColorHex = fresh.secondaryBackgroundColorHex
        useGradient = fresh.useGradient
        gradientPreset = fresh.gradientPreset
        gradientStartColorHex = fresh.gradientStartColorHex
        gradientEndColorHex = fresh.gradientEndColorHex
        gradientDirection = fresh.gradientDirection
        backgroundType = fresh.backgroundType
        backgroundImageUrl = fresh.backgroundImageUrl
        imageOpacity = fresh.imageOpacity
        imageScale = fresh.imageScale
        imageOverlayColorHex = fresh.imageOverlayColorHex
        overlayOpacity = fresh.overlayOpacity
        headerBorderColorHex = fresh.headerBorderColorHex
        headerBorderThicknessDp = fresh.headerBorderThicknessDp
        dividerColorHex = fresh.dividerColorHex
        dividerStyle = fresh.dividerStyle
        buttonBgColorHex = fresh.buttonBgColorHex
        buttonBorderColorHex = fresh.buttonBorderColorHex
        buttonIconColorHex = fresh.buttonIconColorHex
        buttonBorderThicknessDp = fresh.buttonBorderThicknessDp
        buttonCornerRadiusDp = fresh.buttonCornerRadiusDp
        buttonSizeDp = fresh.buttonSizeDp
        iconSelectionMenu = fresh.iconSelectionMenu
        iconSelectionAppIcon = fresh.iconSelectionAppIcon
        iconSelectionPremium = fresh.iconSelectionPremium
        iconSelectionNotifications = fresh.iconSelectionNotifications
        Toast.makeText(context, "Top Bar restored to Approved Design defaults.", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // ==========================================
        // 1. HEADER HERO BANNER
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B192C)),
                border = BorderStroke(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E3E62))
                                    .border(1.dp, Color(0xFFFFD700), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DashboardCustomize,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Home Screen Top Bar Customizer",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "Design studio for titles, typography, icons, luxury gradients & multi-app deployment.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF132A4A),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "TARGET: ${activeApp.name}",
                                color = Color(0xFFFFD700),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            color = if (isEnabled) Color(0xFF065F46) else Color(0xFF7F1D1D),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isEnabled) "TOP BAR ACTIVE" else "TOP BAR DISABLED",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. LIVE INTERACTIVE TOP BAR PREVIEW
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "EXACT LIVE PREVIEW",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color(0xFF1E3E62)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFECFDF5),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "REAL COMPONENT",
                                    color = Color(0xFF065F46),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Preview Canvas Background Selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Canvas:", fontSize = 11.sp, color = Color(0xFF64748B))
                            listOf("DARK", "LUMINOUS", "LIGHT").forEach { mode ->
                                val isSel = previewCanvasMode == mode
                                Surface(
                                    onClick = { previewCanvasMode = mode },
                                    color = if (isSel) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = mode,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Render Real TopBar in Selected Canvas Mode
                    val canvasBg = when (previewCanvasMode) {
                        "DARK" -> Color(0xFF0F172A)
                        "LUMINOUS" -> Color(0xFF071224)
                        else -> Color(0xFFE2E8F0)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(canvasBg)
                            .padding(8.dp)
                    ) {
                        PremiumTopBar(
                            tenantConfig = activeApp,
                            config = liveConfig,
                            onMenuClick = { Toast.makeText(context, "Menu action clicked", Toast.LENGTH_SHORT).show() },
                            onAppIconClick = { Toast.makeText(context, "Logo/Exam icon clicked", Toast.LENGTH_SHORT).show() },
                            onTextSettingsClick = { Toast.makeText(context, "Text settings clicked", Toast.LENGTH_SHORT).show() },
                            onPremiumClick = { Toast.makeText(context, "Premium clicked", Toast.LENGTH_SHORT).show() },
                            onNotificationsClick = { Toast.makeText(context, "Notifications clicked", Toast.LENGTH_SHORT).show() },
                            unreadNotifications = 1
                        )
                    }

                    // Quick Scope Notice
                    val scopeNotice = when (targetScope) {
                        "ALL" -> "Deployment Scope: ALL APPS (${appsList.size})"
                        "SELECTED" -> "Deployment Scope: ${selectedAppIds.size} SELECTED APPS"
                        else -> "Deployment Scope: SINGLE APP (${activeApp.name})"
                    }
                    Text(
                        text = "ℹ️ $scopeNotice",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB)
                    )
                }
            }
        }

        // ==========================================
        // 3. CATEGORY NAVIGATION TABS
        // ==========================================
        item {
            val categories = listOf(
                "Texts & Titles",
                "Typography",
                "Icons & Buttons",
                "Backgrounds",
                "Layout & Borders",
                "Target Scope"
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories.size) { index ->
                    val isSelected = activeCategoryIndex == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeCategoryIndex = index },
                        label = {
                            Text(
                                text = categories[index],
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.5.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E3E62),
                            selectedLabelColor = Color(0xFFFFD700),
                            containerColor = Color.White,
                            labelColor = Color(0xFF334155)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color(0xFFFFD700) else Color(0xFFCBD5E1),
                            selectedBorderColor = Color(0xFFFFD700)
                        )
                    )
                }
            }
        }

        // ==========================================
        // 4. CATEGORY CONTENT ACCORDION
        // ==========================================
        when (activeCategoryIndex) {
            0 -> {
                // CATEGORY 0: CONTENT & TEXTS
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("Master & Content Settings", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF0F172A))

                            ToggleRow("Enable Top Bar in App", isEnabled) { isEnabled = it }

                            Divider(color = Color(0xFFF1F5F9))

                            // Main Title
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Main Title Text") },
                                placeholder = { Text("e.g. APPSC GROUP-2") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            ToggleRow("Show Main Title", showTitle) { showTitle = it }

                            Divider(color = Color(0xFFF1F5F9))

                            // Subtitle
                            OutlinedTextField(
                                value = subtitle,
                                onValueChange = { subtitle = it },
                                label = { Text("Subtitle Text") },
                                placeholder = { Text("e.g. EXECUTIVE & NON - EXECUTIVE") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            ToggleRow("Show Subtitle", showSubtitle) { showSubtitle = it }

                            Divider(color = Color(0xFFF1F5F9))

                            // Additional Text
                            OutlinedTextField(
                                value = additionalText,
                                onValueChange = { additionalText = it },
                                label = { Text("Additional / Slogan Text") },
                                placeholder = { Text("e.g. OFFICIAL EXAM PREPARATION") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            ToggleRow("Show Additional Text", showAdditionalText) { showAdditionalText = it }

                            Divider(color = Color(0xFFF1F5F9))

                            // Premium Badge Text
                            OutlinedTextField(
                                value = premiumBadgeText,
                                onValueChange = { premiumBadgeText = it },
                                label = { Text("Premium Badge Label") },
                                placeholder = { Text("PREMIUM / VIP / PRO") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            ToggleRow("Show Premium Crown Badge", showPremium) { showPremium = it }

                            Divider(color = Color(0xFFF1F5F9))

                            // Text Alignment
                            Text("Center Box Text Alignment", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("LEFT" to "Left", "CENTER" to "Center", "RIGHT" to "Right").forEach { (key, label) ->
                                    val isSel = textAlignment == key
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { textAlignment = key },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // CATEGORY 1: TYPOGRAPHY & STYLING
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Curated Typography & Fonts", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF0F172A))

                            // 1. MAIN TITLE TYPOGRAPHY
                            Text("1. Main Title Typography", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF1E3E62))
                            FontFamilySelector(selected = titleFontFamily) { titleFontFamily = it }
                            FontWeightSelector(selected = titleFontWeight) { titleFontWeight = it }
                            FontStyleSelector(selected = titleFontStyle) { titleFontStyle = it }

                            Text("Title Font Size: $titleFontSizeSp sp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Slider(
                                value = titleFontSizeSp.toFloat(),
                                onValueChange = { titleFontSizeSp = it.toInt() },
                                valueRange = 10f..20f,
                                steps = 10
                            )

                            ColorFieldWithSwatches(
                                label = "Title Color Hex",
                                hexValue = titleColorHex,
                                onHexChange = { titleColorHex = it },
                                swatches = listOf("#FFFBEB", "#FFFFFF", "#FFD700", "#38BDF8", "#F59E0B", "#F43F5E")
                            )

                            Divider(color = Color(0xFFE2E8F0))

                            // 2. SUBTITLE TYPOGRAPHY
                            Text("2. Subtitle Typography", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF1E3E62))
                            FontFamilySelector(selected = subtitleFontFamily) { subtitleFontFamily = it }
                            FontWeightSelector(selected = subtitleFontWeight) { subtitleFontWeight = it }
                            FontStyleSelector(selected = subtitleFontStyle) { subtitleFontStyle = it }

                            Text("Subtitle Font Size: $subtitleFontSizeSp sp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Slider(
                                value = subtitleFontSizeSp.toFloat(),
                                onValueChange = { subtitleFontSizeSp = it.toInt() },
                                valueRange = 7f..14f,
                                steps = 7
                            )

                            ColorFieldWithSwatches(
                                label = "Subtitle Color Hex",
                                hexValue = subtitleColorHex,
                                onHexChange = { subtitleColorHex = it },
                                swatches = listOf("#F1F5F9", "#FFFFFF", "#FFD700", "#94A3B8", "#E2E8F0")
                            )

                            Divider(color = Color(0xFFE2E8F0))

                            // 3. ADDITIONAL TEXT TYPOGRAPHY
                            Text("3. Additional Text Typography", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF1E3E62))
                            FontFamilySelector(selected = additionalFontFamily) { additionalFontFamily = it }
                            FontWeightSelector(selected = additionalFontWeight) { additionalFontWeight = it }
                            FontStyleSelector(selected = additionalFontStyle) { additionalFontStyle = it }

                            Text("Additional Font Size: $additionalFontSizeSp sp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Slider(
                                value = additionalFontSizeSp.toFloat(),
                                onValueChange = { additionalFontSizeSp = it.toInt() },
                                valueRange = 6f..12f,
                                steps = 6
                            )

                            ColorFieldWithSwatches(
                                label = "Additional Text Color Hex",
                                hexValue = additionalColorHex,
                                onHexChange = { additionalColorHex = it },
                                swatches = listOf("#FFD700", "#FFFBEB", "#F59E0B", "#10B981", "#38BDF8")
                            )
                        }
                    }
                }
            }

            2 -> {
                // CATEGORY 2: ICONS & ACTION BUTTONS
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Icons & Action Buttons Appearance", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF0F172A))

                            // Icon Selection & Toggles
                            Text("1. Icon Selections & Toggles", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF1E3E62))

                            // Menu
                            ToggleRow("Show Menu Button", showMenu) { showMenu = it }
                            if (showMenu) {
                                IconPickerRow(
                                    label = "Menu Icon",
                                    selected = iconSelectionMenu,
                                    options = listOf("Menu", "MenuOpen", "Widgets", "GridView", "Apps"),
                                    onSelect = { iconSelectionMenu = it }
                                )
                            }

                            Divider(color = Color(0xFFF1F5F9))

                            // App Logo / Emblem
                            ToggleRow("Show App Logo / Exam Emblem", showAppIcon) { showAppIcon = it }
                            if (showAppIcon) {
                                IconPickerRow(
                                    label = "Exam Emblem Fallback Icon",
                                    selected = iconSelectionAppIcon,
                                    options = listOf("School", "AutoAwesome", "WorkspacePremium", "Star", "Workspace"),
                                    onSelect = { iconSelectionAppIcon = it }
                                )
                            }

                            Divider(color = Color(0xFFF1F5F9))

                            // Text Settings
                            ToggleRow("Show Font & Text Settings Button (Tt)", showTextSettings) { showTextSettings = it }

                            Divider(color = Color(0xFFF1F5F9))

                            // Premium Crown Badge
                            ToggleRow("Show Subscription Crown Badge", showPremium) { showPremium = it }
                            if (showPremium) {
                                IconPickerRow(
                                    label = "Premium Badge Icon",
                                    selected = iconSelectionPremium,
                                    options = listOf("WorkspacePremium", "Crown", "Star"),
                                    onSelect = { iconSelectionPremium = it }
                                )
                            }

                            Divider(color = Color(0xFFF1F5F9))

                            // Notifications
                            ToggleRow("Show Notifications Bell", showNotifications) { showNotifications = it }
                            if (showNotifications) {
                                IconPickerRow(
                                    label = "Notification Bell Icon",
                                    selected = iconSelectionNotifications,
                                    options = listOf("Notifications", "NotificationsActive", "Bell"),
                                    onSelect = { iconSelectionNotifications = it }
                                )
                            }

                            Divider(color = Color(0xFFE2E8F0))

                            // Button Appearance
                            Text("2. Action Buttons Styling", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF1E3E62))

                            Text("Button Size: $buttonSizeDp dp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Slider(
                                value = buttonSizeDp.toFloat(),
                                onValueChange = { buttonSizeDp = it.toInt() },
                                valueRange = 26f..42f,
                                steps = 8
                            )

                            Text("Button Corner Radius: $buttonCornerRadiusDp dp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Slider(
                                value = buttonCornerRadiusDp.toFloat(),
                                onValueChange = { buttonCornerRadiusDp = it.toInt() },
                                valueRange = 0f..20f,
                                steps = 10
                            )

                            ColorFieldWithSwatches(
                                label = "Button Background Color Hex",
                                hexValue = buttonBgColorHex,
                                onHexChange = { buttonBgColorHex = it },
                                swatches = listOf("#132A4A", "#0B192C", "#1E3E62", "#FFFFFF", "#1E293B")
                            )

                            ColorFieldWithSwatches(
                                label = "Button Border Color Hex",
                                hexValue = buttonBorderColorHex,
                                onHexChange = { buttonBorderColorHex = it },
                                swatches = listOf("#FFD700", "#F59E0B", "#FFFFFF", "#38BDF8", "#64748B")
                            )

                            Text("Button Border Thickness: $buttonBorderThicknessDp dp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Slider(
                                value = buttonBorderThicknessDp.toFloat(),
                                onValueChange = { buttonBorderThicknessDp = it.toInt() },
                                valueRange = 0f..3f,
                                steps = 3
                            )

                            ColorFieldWithSwatches(
                                label = "Button Icon Color Hex",
                                hexValue = buttonIconColorHex,
                                onHexChange = { buttonIconColorHex = it },
                                swatches = listOf("#FFD700", "#FFFBEB", "#FFFFFF", "#38BDF8", "#0B192C")
                            )
                        }
                    }
                }
            }

            3 -> {
                // CATEGORY 3: BACKGROUNDS & GRADIENTS
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Background & Luxury Treatments", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF0F172A))

                            // Background Type
                            Text("Select Background Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF334155))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("GRADIENT" to "Gradient", "SOLID" to "Solid Color", "IMAGE" to "Image").forEach { (type, label) ->
                                    val isSel = backgroundType == type
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { backgroundType = type },
                                        label = { Text(label) }
                                    )
                                }
                            }

                            if (backgroundType == "GRADIENT") {
                                Text("Curated Gradient Presets", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E3E62))
                                val presets = listOf(
                                    "NAVY_GOLD" to "Navy & Gold",
                                    "ROYAL_GOLD" to "Royal Gold",
                                    "MIDNIGHT_PURPLE" to "Midnight Purple",
                                    "EMERALD_LUX" to "Emerald Lux",
                                    "CRIMSON_SUNSET" to "Crimson Sunset",
                                    "SUNSET_GOLD" to "Sunset Gold",
                                    "DARK_SLATE" to "Dark Slate",
                                    "CUSTOM" to "Custom"
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(presets) { (key, label) ->
                                        val isSel = gradientPreset == key
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { gradientPreset = key },
                                            label = { Text(label) }
                                        )
                                    }
                                }

                                Text("Gradient Direction", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF334155))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("HORIZONTAL" to "Horizontal", "VERTICAL" to "Vertical", "DIAGONAL" to "Diagonal").forEach { (dir, label) ->
                                        val isSel = gradientDirection == dir
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { gradientDirection = dir },
                                            label = { Text(label) }
                                        )
                                    }
                                }

                                if (gradientPreset == "CUSTOM") {
                                    ColorFieldWithSwatches(
                                        label = "Gradient Start Color",
                                        hexValue = gradientStartColorHex,
                                        onHexChange = { gradientStartColorHex = it },
                                        swatches = listOf("#071224", "#0B192C", "#1E1B4B", "#022C22", "#450A0A")
                                    )
                                    ColorFieldWithSwatches(
                                        label = "Gradient End Color",
                                        hexValue = gradientEndColorHex,
                                        onHexChange = { gradientEndColorHex = it },
                                        swatches = listOf("#1E3E62", "#4338CA", "#065F46", "#991B1B", "#334155")
                                    )
                                }
                            } else if (backgroundType == "SOLID") {
                                ColorFieldWithSwatches(
                                    label = "Primary Background Color Hex",
                                    hexValue = backgroundColorStringHex,
                                    onHexChange = { backgroundColorStringHex = it },
                                    swatches = listOf("#0B192C", "#071224", "#0F172A", "#1E1B4B", "#022C22", "#18181B")
                                )
                                ColorFieldWithSwatches(
                                    label = "Secondary Background Color Hex",
                                    hexValue = secondaryBackgroundColorHex,
                                    onHexChange = { secondaryBackgroundColorHex = it },
                                    swatches = listOf("#1E3E62", "#312E81", "#064E3B", "#7F1D1D", "#334155")
                                )
                            } else {
                                // Background Image
                                OutlinedTextField(
                                    value = backgroundImageUrl,
                                    onValueChange = { backgroundImageUrl = it },
                                    label = { Text("Background Image URL (Unsplash / Cloud Storage)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Button(
                                    onClick = {
                                        imagePickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3E62)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pick Image from Device Storage")
                                }

                                RecommendedImageSizeHelper(
                                    spec = UploadImageType.HEADER_BANNER,
                                    imageSource = backgroundImageUrl,
                                    showContainerPreview = true
                                )

                                Text("Image Scale Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF334155))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Crop", "Fit", "FillBounds", "Inside").forEach { scale ->
                                        val isSel = imageScale.equals(scale, ignoreCase = true)
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { imageScale = scale },
                                            label = { Text(scale) }
                                        )
                                    }
                                }

                                Text("Image Opacity: ${(imageOpacity * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                                Slider(
                                    value = imageOpacity,
                                    onValueChange = { imageOpacity = it },
                                    valueRange = 0.1f..1.0f
                                )

                                ColorFieldWithSwatches(
                                    label = "Image Overlay Color Hex",
                                    hexValue = imageOverlayColorHex,
                                    onHexChange = { imageOverlayColorHex = it },
                                    swatches = listOf("#071224", "#0B192C", "#000000", "#1E1B4B", "#022C22")
                                )

                                Text("Overlay Darkening Opacity: ${(overlayOpacity * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                                Slider(
                                    value = overlayOpacity,
                                    onValueChange = { overlayOpacity = it },
                                    valueRange = 0.0f..0.9f
                                )
                            }
                        }
                    }
                }
            }

            4 -> {
                // CATEGORY 4: BORDERS, SHADOW & LAYOUT
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Borders, Shadows & Layout Sizing", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF0F172A))

                            // Header Height
                            Text("Header Height (0 = Auto / Wrap Content): ${if (headerHeightDp == 0) "Auto" else "$headerHeightDp dp"}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Slider(
                                value = headerHeightDp.toFloat(),
                                onValueChange = { headerHeightDp = it.toInt() },
                                valueRange = 0f..80f,
                                steps = 8
                            )

                            // Padding & Margins
                            Text("Horizontal Padding: $headerPaddingHorizontalDp dp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Slider(
                                value = headerPaddingHorizontalDp.toFloat(),
                                onValueChange = { headerPaddingHorizontalDp = it.toInt() },
                                valueRange = 0f..20f,
                                steps = 10
                            )

                            Text("Vertical Padding: $headerPaddingVerticalDp dp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Slider(
                                value = headerPaddingVerticalDp.toFloat(),
                                onValueChange = { headerPaddingVerticalDp = it.toInt() },
                                valueRange = 0f..12f,
                                steps = 6
                            )

                            Text("Horizontal Margin: $headerMarginHorizontalDp dp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Slider(
                                value = headerMarginHorizontalDp.toFloat(),
                                onValueChange = { headerMarginHorizontalDp = it.toInt() },
                                valueRange = 0f..16f,
                                steps = 8
                            )

                            Divider(color = Color(0xFFE2E8F0))

                            // Corner Radius & Elevation
                            Text("Bottom Corner Radius: $cornerRadiusDp dp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Slider(
                                value = cornerRadiusDp.toFloat(),
                                onValueChange = { cornerRadiusDp = it.toInt() },
                                valueRange = 0f..32f,
                                steps = 16
                            )

                            Text("Shadow Elevation: $elevationDp dp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Slider(
                                value = elevationDp.toFloat(),
                                onValueChange = { elevationDp = it.toInt() },
                                valueRange = 0f..16f,
                                steps = 8
                            )

                            ToggleRow("Enable Ambient Gold Glow Effect", glowEffectEnabled) { glowEffectEnabled = it }
                            if (glowEffectEnabled) {
                                ColorFieldWithSwatches(
                                    label = "Glow Color Hex",
                                    hexValue = glowColorHex,
                                    onHexChange = { glowColorHex = it },
                                    swatches = listOf("#FFD700", "#F59E0B", "#38BDF8", "#A855F7", "#10B981")
                                )
                            }

                            Divider(color = Color(0xFFE2E8F0))

                            // Borders & Dividers
                            Text("Header Border Styling", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E3E62))
                            ColorFieldWithSwatches(
                                label = "Header Border Color Hex",
                                hexValue = headerBorderColorHex,
                                onHexChange = { headerBorderColorHex = it },
                                swatches = listOf("#FFD700", "#B45309", "#FFFFFF", "#38BDF8", "#334155")
                            )

                            Text("Header Border Thickness: $headerBorderThicknessDp dp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            Slider(
                                value = headerBorderThicknessDp.toFloat(),
                                onValueChange = { headerBorderThicknessDp = it.toInt() },
                                valueRange = 0f..4f,
                                steps = 4
                            )

                            Text("Title / Subtitle Divider Style", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E3E62))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("GRADIENT_HAIRLINE" to "Gradient Stars", "SOLID" to "Solid Line", "NONE" to "None").forEach { (style, label) ->
                                    val isSel = dividerStyle == style
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { dividerStyle = style },
                                        label = { Text(label) }
                                    )
                                }
                            }

                            ColorFieldWithSwatches(
                                label = "Divider Accent Color Hex",
                                hexValue = dividerColorHex,
                                onHexChange = { dividerColorHex = it },
                                swatches = listOf("#FFD700", "#F59E0B", "#FFFFFF", "#94A3B8")
                            )
                        }
                    }
                }
            }

            5 -> {
                // CATEGORY 5: TARGET APP SCOPE
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("TARGET APPLICATION SCOPE", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF0F172A))
                            Text(
                                "Configure whether your changes apply exclusively to the current active app (${activeApp.name}), specific chosen exams, or globally to all apps.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )

                            val scopes = listOf(
                                "CURRENT" to "Current App Only (${activeApp.name})",
                                "SELECTED" to "Multiple Selected Apps (${selectedAppIds.size})",
                                "ALL" to "All Apps Worldwide (${appsList.size})"
                            )

                            scopes.forEach { (key, label) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { targetScope = key }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = targetScope == key,
                                        onClick = { targetScope = key }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(label, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                                }
                            }

                            if (targetScope == "SELECTED") {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Select Target Competitive Apps:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                appsList.forEach { app ->
                                    val isChecked = selectedAppIds.contains(app.id)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedAppIds = if (isChecked) selectedAppIds - app.id else selectedAppIds + app.id
                                            }
                                            .padding(vertical = 2.dp, horizontal = 8.dp)
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                selectedAppIds = if (checked) selectedAppIds + app.id else selectedAppIds - app.id
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(app.name, fontSize = 13.sp, color = Color(0xFF334155))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. STICKY ACTION BUTTONS: SAVE / DISCARD / RESET
        // ==========================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Deployment & Actions",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { applyAndSaveConfig() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save & Publish", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { discardChanges() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                            border = BorderStroke(1.dp, Color(0xFF475569)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("Discard", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { resetToDefaults() },
                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Approved Design Defaults", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.5.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2563EB))
        )
    }
}

@Composable
private fun FontFamilySelector(selected: String, onSelect: (String) -> Unit) {
    val families = listOf("Montserrat", "Inter", "Outfit", "Poppins", "Lexend", "Roboto", "Playfair Display", "Cinzel", "Serif", "Monospace", "Sans-Serif")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Font Family", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(families) { font ->
                val isSel = selected.equals(font, ignoreCase = true)
                FilterChip(
                    selected = isSel,
                    onClick = { onSelect(font) },
                    label = { Text(font, fontSize = 11.5.sp) }
                )
            }
        }
    }
}

@Composable
private fun FontWeightSelector(selected: String, onSelect: (String) -> Unit) {
    val weights = listOf("Normal", "Medium", "SemiBold", "Bold", "ExtraBold", "Black")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Font Weight", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(weights) { weight ->
                val isSel = selected.equals(weight, ignoreCase = true)
                FilterChip(
                    selected = isSel,
                    onClick = { onSelect(weight) },
                    label = { Text(weight, fontSize = 11.5.sp) }
                )
            }
        }
    }
}

@Composable
private fun FontStyleSelector(selected: String, onSelect: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Font Style:", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
        listOf("Normal", "Italic").forEach { style ->
            val isSel = selected.equals(style, ignoreCase = true)
            FilterChip(
                selected = isSel,
                onClick = { onSelect(style) },
                label = { Text(style, fontSize = 11.5.sp) }
            )
        }
    }
}

@Composable
private fun ColorFieldWithSwatches(
    label: String,
    hexValue: String,
    onHexChange: (String) -> Unit,
    swatches: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = hexValue,
            onValueChange = onHexChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Presets:", fontSize = 11.sp, color = Color(0xFF64748B))
            swatches.forEach { colorHex ->
                val parsed = parseHexColor(colorHex, Color.DarkGray)
                val isSel = hexValue.equals(colorHex, ignoreCase = true)
                Surface(
                    onClick = { onHexChange(colorHex) },
                    color = parsed,
                    shape = CircleShape,
                    modifier = Modifier.size(26.dp),
                    border = BorderStroke(if (isSel) 2.5.dp else 1.dp, if (isSel) Color(0xFF2563EB) else Color.LightGray)
                ) {}
            }
        }
    }
}

@Composable
private fun IconPickerRow(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(options) { opt ->
                val isSel = selected.equals(opt, ignoreCase = true)
                FilterChip(
                    selected = isSel,
                    onClick = { onSelect(opt) },
                    label = { Text(opt, fontSize = 11.5.sp) }
                )
            }
        }
    }
}
