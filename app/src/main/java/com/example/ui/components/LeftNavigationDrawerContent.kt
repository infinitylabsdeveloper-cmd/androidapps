package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.*
import com.example.util.GamificationManager
import com.example.util.OfflineSyncManager
import com.example.util.PreviousYearPaperStore
import com.example.ui.theme.LuxuriousGold

fun getContentScaleByName(scaleType: String?): ContentScale {
    return when (scaleType?.lowercase()) {
        "crop" -> ContentScale.Crop
        "fit" -> ContentScale.Fit
        "fillbounds" -> ContentScale.FillBounds
        "fillwidth" -> ContentScale.FillWidth
        "inside" -> ContentScale.Inside
        else -> ContentScale.Crop
    }
}

fun getFontWeightByName(weightName: String?): FontWeight {
    return when (weightName?.lowercase()) {
        "light" -> FontWeight.Light
        "normal" -> FontWeight.Normal
        "medium" -> FontWeight.Medium
        "semibold" -> FontWeight.SemiBold
        "bold" -> FontWeight.Bold
        "extrabold" -> FontWeight.ExtraBold
        "black" -> FontWeight.Black
        else -> FontWeight.Bold
    }
}

fun getFontFamilyByName(fontName: String?): FontFamily {
    return when (fontName?.lowercase()?.trim()) {
        "serif" -> FontFamily.Serif
        "monospace" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        "sans-serif", "sansserif" -> FontFamily.SansSerif
        else -> FontFamily.Default
    }
}

@Composable
fun LeftNavigationDrawerContent(
    drawerConfig: DrawerConfig = SampleData.drawerConfigState.value,
    userProfile: UserProfileModel = SampleData.userProfileState.value,
    isAdminLoggedIn: Boolean = false,
    isDarkMode: Boolean = false,
    onCloseDrawer: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToMockTestGenerator: () -> Unit,
    onNavigateToAvailableMockTests: () -> Unit,
    onNavigateToCurrentAffairs: () -> Unit,
    onNavigateToPerformance: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToSpeedDuel: () -> Unit = {},
    onNavigateToOfflineManager: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToScientificCalculator: () -> Unit,
    onNavigateToPrivacySection: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAdminPanel: () -> Unit,
    onShareApp: () -> Unit,
    onOpenDailyReminder: () -> Unit = {},
    onNavigateToAiMentor: () -> Unit = {},
    onNavigateToSavedQuestions: () -> Unit = {},
    onNavigateToAppSelection: () -> Unit = {},
    onReportIssue: () -> Unit = {},
    onNavigateToPreviousYearPapers: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeApp = remember { SampleData.getActiveApp() }
    val drawerBg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(drawerConfig.drawerWidthDp.dp),
        color = drawerBg,
        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // A. TOP CARDS (My Profile & Settings, Syllabus Home)
            val topCards = drawerConfig.items.filter { it.sectionKey == "MAIN" && it.isVisible }
                .sortedBy { it.order }

                topCards.forEach { item ->
                    if (item.id == "my_profile") {
                        val isSubActive = userProfile.isLoggedIn && (userProfile.isProSubscribed || userProfile.expiresAt > System.currentTimeMillis())
                        val resolvedItem = if (isSubActive) {
                            item.copy(
                                badgeText = "PRO ⭐",
                                badgeBgColorHex = 0xFFF59E0B,
                                badgeTextColorHex = 0xFFFFFFFF
                            )
                        } else {
                            item
                        }

                        // Apply premium multi-colored theme for subbed users
                        val premiumModifier = if (isSubActive) {
                            Modifier.background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF10B981), // Emerald
                                        Color(0xFF3B82F6), // Blue
                                        Color(0xFFF43F5E), // Red
                                        Color(0xFFF97316), // Orange
                                        Color(0xFFA855F7), // Violet
                                        Color(0xFF14B8A6)  // Teal
                                    )
                                ),
                                RoundedCornerShape(item.cornerRadiusDp.dp)
                            ).padding(1.5.dp) // Border thickness
                        } else {
                            Modifier
                        }

                        Box(modifier = premiumModifier) {
                            ExactDrawerCardItem(
                                item = resolvedItem,
                                isDarkMode = isDarkMode,
                                customTitle = if (userProfile.isLoggedIn && userProfile.name.isNotBlank()) "Profile • ${userProfile.name}" else "Profile & Settings",
                                customSubtitle = if (userProfile.isLoggedIn) {
                                    if (isSubActive) "PRO Member • ${userProfile.email}" else userProfile.email
                                } else item.subtitle,
                                onClick = {
                                    onCloseDrawer()
                                    onNavigateToProfile()
                                }
                            )
                        }
                    } else if (item.id == "syllabus_home") {
                        ExactDrawerCardItem(
                            item = item,
                            isDarkMode = isDarkMode,
                            onClick = {
                                onCloseDrawer()
                                onNavigateToHome()
                            }
                        )
                    } else {
                        ExactDrawerCardItem(
                            item = item,
                            isDarkMode = isDarkMode,
                            onClick = onCloseDrawer
                        )
                    }
                }

                // B. EXAM HUB & PRACTICE SECTION
                val examHubHeader = drawerConfig.sections.find { it.sectionKey == "EXAM_HUB" }
                if (examHubHeader != null && examHubHeader.isVisible) {
                    val filteredScript = examHubHeader.scriptText?.takeIf {
                        !it.contains("Practice Makes Progress", ignoreCase = true)
                    }
                    ExactDrawerSectionHeader(
                        section = examHubHeader,
                        title = examHubHeader.title,
                        icon = getIconByName(examHubHeader.iconName, Icons.Default.MenuBook),
                        scriptText = filteredScript,
                        isDarkMode = isDarkMode
                    )
                }

                val examHubItems = drawerConfig.items.filter { 
                    it.sectionKey == "EXAM_HUB" && it.isVisible
                }.sortedBy { it.order }

                val availableCount = SampleData.generatedMockTestsState.size
                val isOfflineMode by OfflineSyncManager.isOfflineModeSimulated
                val dailyRank by GamificationManager.currentUserRankDaily
                val displayRank = if (dailyRank > 0) dailyRank else 28

                examHubItems.forEach { item ->
                    val resolvedItem = when (item.id) {
                        "available_mock_tests" -> item.copy(badgeText = "$availableCount TESTS")
                        "state_leaderboard" -> item.copy(
                            title = "Leaderboard",
                            subtitle = "State Live Leaderboard",
                            badgeText = "LIVE",
                            badgeBgColorHex = 0xFFEAB308,
                            badgeTextColorHex = 0xFF0F172A
                        )
                        "offline_cloud_sync" -> item.copy(
                            title = "Downloaded Materials",
                            subtitle = if (isOfflineMode) "Offline Mode" else "Materials Ready",
                            badgeText = if (isOfflineMode) "OFFLINE" else "READY",
                            badgeBgColorHex = if (isOfflineMode) 0xFF0D9488 else 0xFF16A34A,
                            badgeTextColorHex = 0xFFFFFFFF
                        )
                        "previous_year_papers" -> {
                            val pypCount = PreviousYearPaperStore.getPapersForApp(context, activeApp.id).size
                            item.copy(badgeText = "$pypCount PAPERS")
                        }
                        else -> item
                    }

                    val clickAction: () -> Unit = when (item.id) {
                        "mock_test_generator" -> onNavigateToMockTestGenerator
                        "available_mock_tests", "mock_tests" -> onNavigateToAvailableMockTests
                        "current_affairs" -> onNavigateToCurrentAffairs
                        "performance_analytics" -> onNavigateToPerformance
                        "state_leaderboard" -> onNavigateToLeaderboard
                        "offline_cloud_sync" -> onNavigateToOfflineManager
                        "bookmarks" -> onNavigateToSavedQuestions
                        "previous_year_papers" -> onNavigateToPreviousYearPapers
                        else -> ({})
                    }

                    ExactDrawerCardItem(
                        item = resolvedItem,
                        isDarkMode = isDarkMode,
                        onClick = {
                            onCloseDrawer()
                            clickAction()
                        }
                    )
                }

                // C. EXAM TOOLS SECTION
                val examToolsHeader = drawerConfig.sections.find { it.sectionKey == "EXAM_TOOLS" }
                if (examToolsHeader != null && examToolsHeader.isVisible) {
                    ExactDrawerSectionHeader(
                        section = examToolsHeader,
                        title = examToolsHeader.title,
                        icon = getIconByName(examToolsHeader.iconName, Icons.Default.Settings),
                        scriptText = examToolsHeader.scriptText,
                        isDarkMode = isDarkMode
                    )
                }

                val examToolsItems = drawerConfig.items.filter { it.sectionKey == "EXAM_TOOLS" && it.isVisible }
                    .sortedBy { it.order }

                examToolsItems.forEach { item ->
                    val clickAction: () -> Unit = when (item.id) {
                        "subscriptions" -> onNavigateToSubscription
                        "scientific_calculator" -> onNavigateToScientificCalculator
                        "share_app" -> onShareApp
                        else -> ({})
                    }

                    ExactDrawerCardItem(
                        item = item,
                        isDarkMode = isDarkMode,
                        onClick = {
                            onCloseDrawer()
                            clickAction()
                        }
                    )
                }

                // D. LEGAL & SUPPORT SECTION
                val legalHeader = drawerConfig.sections.find { it.sectionKey == "LEGAL_SUPPORT" }
                if (legalHeader != null && legalHeader.isVisible) {
                    ExactDrawerSectionHeader(
                        section = legalHeader,
                        title = legalHeader.title,
                        icon = getIconByName(legalHeader.iconName, Icons.Default.VerifiedUser),
                        scriptText = legalHeader.scriptText,
                        isDarkMode = isDarkMode
                    )
                }

                val legalItems = drawerConfig.items.filter { it.sectionKey == "LEGAL_SUPPORT" && it.isVisible }
                    .sortedBy { it.order }

                legalItems.forEach { item ->
                    if (item.id == "report_issue") {
                        ExactDrawerCardItem(
                            item = item,
                            isDarkMode = isDarkMode,
                            onClick = {
                                onCloseDrawer()
                                onReportIssue()
                            }
                        )
                    } else {
                        val sectionKeyMapping = when (item.id) {
                            "legal_refund_policy" -> "refund_policy"
                            "legal_privacy_policy" -> "privacy_policy"
                            "legal_terms_conditions" -> "terms_conditions"
                            "legal_faq" -> "faq"
                            "legal_about_us" -> "about_us"
                            "legal_contact_us" -> "contact_us"
                            "legal_help_center" -> "help_center"
                            "account_deletion" -> "account_deletion"
                            else -> "privacy_policy"
                        }

                        ExactDrawerCardItem(
                            item = item,
                            isDarkMode = isDarkMode,
                            onClick = {
                                onCloseDrawer()
                                onNavigateToPrivacySection(sectionKeyMapping)
                            }
                        )
                    }
                }

                // Switch Exam Portal Shortcut (ONLY FOR ADMINS)
                if (isAdminLoggedIn) {
                    ExactDrawerCardItem(
                        item = com.example.model.DrawerMenuItemConfig(
                            id = "switch_portal",
                            sectionKey = "ACCOUNT",
                            title = "Switch Exam Portal",
                            subtitle = "Change your target exam",
                            iconName = "swap_horiz",
                            iconTintHex = 0xFF2563EB,
                            cardBgColorHex = 0xFFEFF6FF,
                            cardBorderColorHex = 0xFFBFDBFE,
                            iconBoxBgColorHex = 0xFFDBEAFE
                        ),
                        isDarkMode = isDarkMode,
                        onClick = {
                            onCloseDrawer()
                            onNavigateToAppSelection()
                        }
                    )
                }

                // E. ACCOUNT & ADMIN SECTION
                val accountItems = drawerConfig.items.filter { it.sectionKey == "ACCOUNT" && it.isVisible }
                    .sortedBy { it.order }

                accountItems.forEach { item ->
                    val label = if (userProfile.isLoggedIn) "Logout (${userProfile.name})" else "Candidate Account / Login"
                    val resolved = item.copy(
                        title = label,
                        iconName = if (userProfile.isLoggedIn) "logout" else "person"
                    )

                    ExactDrawerCardItem(
                        item = resolved,
                        isDarkMode = isDarkMode,
                        onClick = {
                            onCloseDrawer()
                            if (userProfile.isLoggedIn) {
                                SampleData.logout()
                            } else {
                                onNavigateToLogin()
                            }
                        }
                    )
                }

                // Admin Panel Shortcut Button if Admin is logged in
                if (isAdminLoggedIn) {
                    ExactDrawerCardItem(
                        item = DrawerMenuItemConfig(
                            id = "admin_panel",
                            sectionKey = "ACCOUNT",
                            title = "Admin Panel & Customizer",
                            subtitle = "Manage header, menu & global config",
                            iconName = "admin_panel_settings",
                            badgeText = "ADMIN",
                            badgeBgColorHex = 0xFFDC2626,
                            cardBgColorHex = 0xFFFEF2F2,
                            cardBorderColorHex = 0xFFFCA5A5,
                            iconBoxBgColorHex = 0xFFFEE2E2,
                            iconTintHex = 0xFFDC2626
                        ),
                        isDarkMode = isDarkMode,
                        onClick = {
                            onCloseDrawer()
                            onNavigateToAdminPanel()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // F. FOOTER DECORATION (Andhra Pradesh Map & Slogan Card)
                ExactDrawerFooterCard(isDarkMode = isDarkMode)
            }
        }
    }

// =========================================================================
// COMPONENT: EXACT DRAWER CARD ITEM
// =========================================================================
@Composable
private fun ExactDrawerCardItem(
    item: DrawerMenuItemConfig,
    isDarkMode: Boolean = false,
    customTitle: String? = null,
    customSubtitle: String? = null,
    onClick: () -> Unit
) {
    val displayTitle = customTitle ?: item.title
    val displaySubtitle = customSubtitle ?: item.subtitle

    val titleFontFamily = getFontFamilyByName(item.fontFamily)
    val titleFontWeight = getFontWeightByName(item.fontWeight)

    val iconTint = Color(item.iconTintHex)

    val rawBgColor = Color(item.cardBgColorHex)
    val containerColor = if (isDarkMode) {
        iconTint.copy(alpha = 0.12f).compositeOver(Color(0xFF1E293B))
    } else {
        // Prevent black or grey background colors in navigation menu options
        val isBlackOrGrey = rawBgColor == Color.Black || rawBgColor == Color.DarkGray || rawBgColor == Color.Gray || rawBgColor == Color.LightGray ||
                (rawBgColor.red == rawBgColor.green && rawBgColor.green == rawBgColor.blue && rawBgColor.red < 0.85f) ||
                (rawBgColor.red < 0.15f && rawBgColor.green < 0.15f && rawBgColor.blue < 0.15f)
        if (isBlackOrGrey) Color(0xFFEFF6FF) else rawBgColor
    }

    val borderColor = if (isDarkMode) {
        iconTint.copy(alpha = 0.28f)
    } else {
        Color(item.cardBorderColorHex)
    }

    val iconBoxBg = if (isDarkMode) {
        iconTint.copy(alpha = 0.16f)
    } else {
        Color(item.iconBoxBgColorHex)
    }

    // Manual luminance calculation
    val luminance = (containerColor.red * 0.2126f + containerColor.green * 0.7152f + containerColor.blue * 0.0722f)
    val isLightBackground = luminance > 0.5f

    // Premium colors for the profile text
    val isProfileItem = item.id == "my_profile"
    val premiumTitleColor = if (isLightBackground) Color(0xFF047857) else Color(0xFF10B981) // Emerald
    val premiumSubtitleColor = if (isLightBackground) Color(0xFF1D4ED8) else Color(0xFF3B82F6) // Blue

    val titleColor = item.titleTextColorHex?.let { Color(it) }
        ?: (if (isProfileItem) LuxuriousGold else (if (isLightBackground) Color(0xFF0F172A) else Color.White))
    val subtitleColor = item.subtitleTextColorHex?.let { Color(it) }
        ?: (if (isProfileItem) premiumSubtitleColor else (if (isLightBackground) Color(0xFF64748B) else Color(0xFF94A3B8)))

    val cardShape = RoundedCornerShape(item.cornerRadiusDp.dp)
    val gradientBrush = if (item.useGradient) {
        val start = Color(item.gradientStartColorHex)
        val end = Color(item.gradientEndColorHex)
        when (item.gradientDirection) {
            "Horizontal" -> Brush.horizontalGradient(listOf(start, end))
            "Vertical" -> Brush.verticalGradient(listOf(start, end))
            else -> Brush.linearGradient(listOf(start, end))
        }
    } else null

    val profileTextBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFE11D48), // Rose Red
                Color(0xFF9333EA), // Royal Violet
                Color(0xFF2563EB), // Electric Blue
                Color(0xFF059669), // Emerald Green
                Color(0xFFD97706)  // Luxurious Gold
            )
        )
    }

    Surface(
        onClick = onClick,
        shape = cardShape,
        color = if (gradientBrush != null) Color.Transparent else containerColor,
        border = BorderStroke(item.borderThicknessDp.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .premiumTargetAnimation(
                target = com.example.model.AnimatableTarget.NAVIGATION_DRAWER,
                index = item.order.coerceIn(0, 8)
            )
            .premiumCardInteraction(
                target = com.example.model.AnimatableTarget.NAVIGATION_DRAWER
            )
            .shadow(0.5.dp, cardShape)
            .then(
                if (gradientBrush != null) Modifier.background(gradientBrush, cardShape)
                else Modifier
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (item.showCardImage && item.cardImageUrl.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = item.cardImageUrl,
                    contentDescription = null,
                    contentScale = getContentScaleByName(item.cardImageScaleType),
                    alpha = item.cardImageOpacity,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(item.cardImageOverlayColorHex))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (item.hasLeftBarAccent || isProfileItem) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isProfileItem) profileTextBrush else Brush.verticalGradient(listOf(Color(item.leftBarColorHex), Color(item.leftBarColorHex)))
                                )
                        )
                    }

                    if (item.showIcon) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(item.iconBoxCornerRadiusDp.dp))
                                .background(iconBoxBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconByName(item.iconName, Icons.Default.Circle),
                                contentDescription = displayTitle,
                                tint = if (isProfileItem) Color.White else iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isProfileItem) {
                            Text(
                                text = displayTitle,
                                style = TextStyle(
                                    brush = profileTextBrush,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = titleFontFamily,
                                    fontSize = (item.titleFontSizeSp + 0.5f).sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = displayTitle,
                                color = titleColor,
                                fontWeight = titleFontWeight,
                                fontFamily = titleFontFamily,
                                fontSize = item.titleFontSizeSp.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (!displaySubtitle.isNullOrEmpty()) {
                            Text(
                                text = displaySubtitle,
                                color = subtitleColor,
                                fontWeight = FontWeight.Medium,
                                fontFamily = titleFontFamily,
                                fontSize = item.subtitleFontSizeSp.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!item.badgeText.isNullOrEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(item.badgeBgColorHex)
                        ) {
                            Text(
                                text = item.badgeText,
                                color = Color(item.badgeTextColorHex),
                                fontSize = item.badgeFontSizeSp.sp,
                                fontWeight = getFontWeightByName(item.badgeFontWeight),
                                fontFamily = titleFontFamily,
                                letterSpacing = 0.3.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = if (isDarkMode) Color(0xFF64748B) else iconTint.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// COMPONENT: EXACT DRAWER SECTION HEADER
// =========================================================================
@Composable
private fun ExactDrawerSectionHeader(
    section: DrawerSectionHeaderConfig? = null,
    title: String = section?.title ?: "",
    icon: ImageVector = getIconByName(section?.iconName ?: "", Icons.Default.MenuBook),
    scriptText: String? = section?.scriptText,
    isDarkMode: Boolean = false
) {
    if (section != null && !section.isVisible) return

    val fontFamily = getFontFamilyByName(section?.fontFamily ?: "Inter")
    val titleWeight = getFontWeightByName(section?.fontWeight ?: "ExtraBold")
    val titleColor = section?.titleColorHex?.let { Color(it) }
        ?: if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF1E3A8A)
    val iconTint = section?.iconTintHex?.let { Color(it) }
        ?: if (isDarkMode) Color(0xFF60A5FA) else Color(0xFF1E3A8A)
    val scriptColor = section?.scriptTextColorHex?.let { Color(it) }
        ?: if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF2563EB)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = title,
                    color = titleColor,
                    fontSize = (section?.fontSizeSp ?: 12f).sp,
                    fontWeight = titleWeight,
                    fontFamily = fontFamily,
                    letterSpacing = 0.8.sp
                )
            }

            val cleanScriptText = scriptText?.takeIf {
                !it.contains("Practice Makes Progress", ignoreCase = true)
            }
            if (!cleanScriptText.isNullOrEmpty()) {
                Text(
                    text = cleanScriptText,
                    color = scriptColor,
                    fontSize = (section?.scriptFontSizeSp ?: 10f).sp,
                    fontWeight = getFontWeightByName(section?.scriptFontWeight ?: "Bold"),
                    fontFamily = fontFamily,
                    fontStyle = FontStyle.Italic
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        HorizontalDivider(
            color = if (isDarkMode) Color(0xFF334155) else Color(0xFFCBD5E1),
            thickness = 1.dp
        )
    }
}

// =========================================================================
// COMPONENT: EXACT DRAWER FOOTER CARD (MOTIVATIONAL QUOTATION SECTION)
// =========================================================================
@Composable
private fun ExactDrawerFooterCard(
    isDarkMode: Boolean = false
) {
    val wordColors = if (isDarkMode) {
        listOf(
            Color(0xFF10B981), // Emerald Green
            Color(0xFF3B82F6), // Royal Blue
            Color(0xFFF43F5E), // Ruby Red
            Color(0xFFF97316), // Vibrant Orange
            Color(0xFFA855F7), // Rich Violet
            Color(0xFF14B8A6)  // Deep Teal
        )
    } else {
        listOf(
            Color(0xFF047857), // Emerald Green
            Color(0xFF1D4ED8), // Royal Blue
            Color(0xFFBE123C), // Ruby Red
            Color(0xFFEA580C), // Vibrant Orange
            Color(0xFF6D28D9), // Rich Violet
            Color(0xFF0F766E)  // Deep Teal
        )
    }

    val quoteWords = listOf("“Dream", "Big.", "Study", "Hard.", "Achieve", "Greatness.”")

    val styledQuote = remember(isDarkMode) {
        buildAnnotatedString {
            quoteWords.forEachIndexed { index, word ->
                withStyle(
                    style = SpanStyle(
                        color = wordColors[index % wordColors.size],
                        fontWeight = FontWeight.Black
                    )
                ) {
                    append(word)
                }
                if (index < quoteWords.lastIndex) {
                    append(if (index == 3) "\n" else " ")
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
        ),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top subtle decorative divider pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(wordColors[0])
                )
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(wordColors[1])
                )
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(wordColors[2])
                )
            }

            // Large, high-contrast, multi-colored motivational quote
            Text(
                text = styledQuote,
                fontSize = 18.sp,
                lineHeight = 25.sp,
                letterSpacing = 0.3.sp,
                textAlign = TextAlign.Center,
                softWrap = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Bottom subtle decorative divider pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(wordColors[3])
                )
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(wordColors[4])
                )
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(wordColors[5])
                )
            }
        }
    }
}

// Helper to map string icon names to ImageVectors
fun getIconByName(iconName: String, fallback: ImageVector = Icons.Default.Circle): ImageVector {
    return when (iconName.lowercase()) {
        "person", "account" -> Icons.Default.Person
        "home" -> Icons.Default.Home
        "auto_awesome", "sparkle" -> Icons.Default.AutoAwesome
        "assignment", "test", "quiz_list", "history_edu", "history", "pyp", "previous_year_papers" -> Icons.Default.Assignment
        "newspaper", "article" -> Icons.Default.Newspaper
        "bar_chart", "analytics" -> Icons.Default.BarChart
        "emoji_events", "trophy" -> Icons.Default.EmojiEvents
        "flash_on", "bolt", "lightning" -> Icons.Default.Bolt
        "cloud_sync", "sync" -> Icons.Default.CloudSync
        "workspace_premium", "crown", "pro" -> Icons.Default.WorkspacePremium
        "calculate", "calculator" -> Icons.Default.Calculate
        "share" -> Icons.Default.Share
        "alarm", "reminder" -> Icons.Default.Alarm
        "receipt_long", "refund" -> Icons.Default.ReceiptLong
        "security", "privacy" -> Icons.Default.Security
        "description", "terms" -> Icons.Default.Description
        "quiz", "faq" -> Icons.Default.Quiz
        "info", "about" -> Icons.Default.Info
        "contact_support", "contact" -> Icons.Default.ContactSupport
        "help", "help_center" -> Icons.Default.Help
        "delete_forever", "delete" -> Icons.Default.DeleteForever
        "bug_report", "bug", "report" -> Icons.Default.BugReport
        "logout" -> Icons.Default.Logout
        "school", "cap" -> Icons.Default.School
        "menu_book", "book" -> Icons.Default.MenuBook
        "settings", "gear" -> Icons.Default.Settings
        "verified_user" -> Icons.Default.VerifiedUser
        "admin_panel_settings", "admin" -> Icons.Default.AdminPanelSettings
        else -> fallback
    }
}
