package com.example.ui.components

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.example.ui.theme.LocalWindowSizeClass
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.SampleData
import com.example.model.AppTenantModel
import com.example.model.TopBarConfig

// Helper functions for parsing
fun parseHexColor(hexString: String?, defaultColor: Color): Color {
    if (hexString.isNullOrBlank()) return defaultColor
    return try {
        val cleanHex = hexString.trim().removePrefix("#").removePrefix("0x").removePrefix("0X")
        val colorInt = when (cleanHex.length) {
            6 -> (0xFF000000 or cleanHex.toLong(16)).toInt()
            8 -> cleanHex.toLong(16).toInt()
            else -> return defaultColor
        }
        Color(colorInt)
    } catch (e: Exception) {
        defaultColor
    }
}

fun getTopBarFontFamilyByName(fontName: String?): FontFamily {
    return when (fontName?.lowercase()?.trim()) {
        "serif" -> FontFamily.Serif
        "monospace" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        "sans-serif", "sansserif" -> FontFamily.SansSerif
        else -> FontFamily.Default
    }
}

fun getTopBarFontWeightByName(weightName: String?): FontWeight {
    return when (weightName?.lowercase()?.trim()) {
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

fun getTopBarIconByName(iconName: String?, defaultIcon: ImageVector): ImageVector {
    return when (iconName?.lowercase()?.trim()) {
        "menu" -> Icons.Default.Menu
        "menuopen" -> Icons.Default.MenuOpen
        "widgets" -> Icons.Default.Widgets
        "gridview" -> Icons.Default.GridView
        "apps" -> Icons.Default.Apps
        "school" -> Icons.Default.School
        "autoawesome" -> Icons.Default.AutoAwesome
        "workspacepremium", "crown" -> Icons.Default.WorkspacePremium
        "star" -> Icons.Default.Star
        "workspace" -> Icons.Default.Work
        "notifications", "bell" -> Icons.Default.Notifications
        "notificationsactive" -> Icons.Default.NotificationsActive
        "settings" -> Icons.Default.Settings
        "formatsize" -> Icons.Default.FormatSize
        "fontdownload" -> Icons.Default.FontDownload
        else -> defaultIcon
    }
}

fun getGradientBrushForTopBar(config: TopBarConfig): Brush {
    val preset = config.gradientPreset.uppercase()
    val colors = when (preset) {
        "NAVY_GOLD" -> listOf(Color(0xFF071224), Color(0xFF0B192C), Color(0xFF1E3E62), Color(0xFF0B192C), Color(0xFF071224))
        "ROYAL_GOLD" -> listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4338CA), Color(0xFF312E81))
        "MIDNIGHT_PURPLE" -> listOf(Color(0xFF0F0728), Color(0xFF1E0A3C), Color(0xFF3B0764), Color(0xFF1E0A3C))
        "EMERALD_LUX" -> listOf(Color(0xFF022C22), Color(0xFF064E3B), Color(0xFF047857), Color(0xFF064E3B))
        "CRIMSON_SUNSET" -> listOf(Color(0xFF450A0A), Color(0xFF7F1D1D), Color(0xFF991B1B), Color(0xFF7F1D1D))
        "SUNSET_GOLD" -> listOf(Color(0xFF7C2D12), Color(0xFF9A3412), Color(0xFFC2410C), Color(0xFF9A3412))
        "DARK_SLATE" -> listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155), Color(0xFF1E293B))
        else -> {
            val start = parseHexColor(config.gradientStartColorHex, Color(0xFF071224))
            val end = parseHexColor(config.gradientEndColorHex, Color(0xFF1E3E62))
            listOf(start, end)
        }
    }

    return when (config.gradientDirection.uppercase()) {
        "VERTICAL" -> Brush.verticalGradient(colors)
        "DIAGONAL" -> Brush.linearGradient(colors)
        else -> Brush.horizontalGradient(colors)
    }
}

// Luxury Golden-Orange & Metallic Gold Color Palette matching the exact reference design
val AmberGoldDarkest = Color(0xFFE65100)
val AmberGoldDeep = Color(0xFFF57C00)
val AmberGoldMedium = Color(0xFFFFA000)
val AmberGoldLight = Color(0xFFFFB300)
val AmberGoldBright = Color(0xFFFFCA28)

val GoldMetallicLight = Color(0xFFFFF9C4)
val GoldMetallicBright = Color(0xFFFFE082)
val GoldMetallicPrimary = Color(0xFFFFB300)
val GoldMetallicDark = Color(0xFFFFA000)
val GoldMetallicShadow = Color(0xFFC67D00)

val PearlIvoryLight = Color(0xFFFFFFFF)
val PearlIvoryMedium = Color(0xFFFFFDF8)
val PearlIvoryEdge = Color(0xFFFFF8E7)
val PearlIvoryBorder = Color(0xFFFFC107)

val LuxuryHeaderBackgroundGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFFFF9800),
        Color(0xFFFFB300),
        Color(0xFFFFCA28),
        Color(0xFFFFB300),
        Color(0xFFF57C00)
    )
)

val HeaderGoldMetallicBorderGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFFFFD54F),
        Color(0xFFFFF9C4),
        Color(0xFFFFB300),
        Color(0xFFFFF9C4),
        Color(0xFFFFD54F)
    )
)

val ButtonGoldMetallicBorderGradient = Brush.verticalGradient(
    listOf(
        Color(0xFFFFE082),
        Color(0xFFFFB300),
        Color(0xFFFFA000)
    )
)

val IvoryButtonBackgroundGradient = Brush.verticalGradient(
    listOf(
        Color(0xFFFFFFFF),
        Color(0xFFFFFDF8),
        Color(0xFFFFF8EC)
    )
)

val WineMagentaPremiumGradient = Brush.verticalGradient(
    listOf(
        Color(0xFF9C27B0),
        Color(0xFF880E4F),
        Color(0xFF4A0028)
    )
)

/**
 * PremiumTopBar:
 * Matches the exact design reference for the APPSC GROUP-2 app header:
 * - Rich warm golden-amber-orange gradient background with metallic gold highlights
 * - Large rounded horizontal capsule with a layered gold border
 * - Left side: White circular Menu icon (orange lines) and Purple Education/Graduation Cap icon
 * - Center: Pure white glowing capsule with Gold Academic Laurel Emblem, "APPSC GROUP-2" in magenta-to-orange gradient, and "EXECUTIVE & NON-EXECUTIVE"
 * - Right side: Text Size (Tt), Premium (Wine/Magenta with Crown), and Notifications (with red badge)
 */
@Composable
fun PremiumTopBar(
    modifier: Modifier = Modifier,
    tenantConfig: AppTenantModel? = null,
    config: TopBarConfig = SampleData.topBarConfigState.value,
    onMenuClick: () -> Unit = {},
    onAppIconClick: () -> Unit = {},
    onTextSettingsClick: () -> Unit = {},
    onLuxuryFontStudioClick: () -> Unit = {},
    onPremiumClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    unreadNotifications: Int = 1
) {
    if (!config.isEnabled) return

    val activeTenant = tenantConfig ?: SampleData.getActiveApp()

    val isGroup2 = activeTenant.id.contains("group2", ignoreCase = true) ||
            activeTenant.examCategory.contains("Group 2", ignoreCase = true) ||
            activeTenant.name.contains("Group 2", ignoreCase = true)

    val isGroup1 = activeTenant.id.contains("group1", ignoreCase = true) ||
            activeTenant.examCategory.contains("Group 1", ignoreCase = true)

    val resolvedTitle = remember(activeTenant.id, activeTenant.name, config.title) {
        if (config.title.isNotBlank() && config.title != "APPSC GROUP-2" && config.title != "Prep Portal" && config.title != "My Application") {
            config.title
        } else if (activeTenant.name.isNotBlank()) {
            activeTenant.name.uppercase()
        } else {
            "PREP PORTAL"
        }
    }

    val resolvedSubtitle = remember(activeTenant.id, activeTenant.shortName, activeTenant.description, config.subtitle) {
        if (config.subtitle.isNotBlank() && config.subtitle != "EXECUTIVE & NON - EXECUTIVE" && config.subtitle != "EXECUTIVE & NON-EXECUTIVE") {
            config.subtitle
        } else if (activeTenant.shortName.isNotBlank()) {
            activeTenant.shortName.uppercase()
        } else if (activeTenant.description.isNotBlank()) {
            activeTenant.description.take(30).uppercase()
        } else {
            "EXAMINATION PREPARATION"
        }
    }

    // Split title into prefix ("APPSC") and suffix ("GROUP-2") if formatted with a space
    val (titlePrefix, titleSuffix) = remember(resolvedTitle) {
        val parts = resolvedTitle.trim().split(" ", limit = 2)
        if (parts.size == 2) {
            Pair(parts[0], parts[1])
        } else {
            Pair(resolvedTitle, "")
        }
    }

    val windowSizeClass = LocalWindowSizeClass.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .premiumTargetAnimation(
                target = com.example.model.AnimatableTarget.TOP_APP_BAR_HEADER,
                index = 0
            )
    ) {
        val screenWidth = maxWidth
        val isCompactWidth = windowSizeClass == WindowWidthSizeClass.Compact
        val isMobileScreen = isCompactWidth || screenWidth < 480.dp
        val isVeryNarrow = screenWidth < 380.dp

        val headerHeight = when {
            isVeryNarrow -> 52.dp
            isMobileScreen -> 56.dp
            else -> 64.dp
        }
        val circleButtonSize = when {
            isVeryNarrow -> 32.dp
            isMobileScreen -> 36.dp
            else -> 42.dp
        }
        val squareButtonSize = when {
            isVeryNarrow -> 30.dp
            isMobileScreen -> 34.dp
            else -> 40.dp
        }
        val premiumButtonWidth = when {
            isVeryNarrow -> 38.dp
            isMobileScreen -> 44.dp
            else -> 52.dp
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = AmberGoldDarkest.copy(alpha = 0.4f),
                    spotColor = AmberGoldMedium.copy(alpha = 0.4f)
                )
                .border(
                    border = BorderStroke(1.5.dp, HeaderGoldMetallicBorderGradient),
                    shape = RoundedCornerShape(32.dp)
                ),
            color = AmberGoldMedium,
            shape = RoundedCornerShape(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LuxuryHeaderBackgroundGradient)
                    .padding(horizontal = if (isVeryNarrow) 4.dp else if (isMobileScreen) 6.dp else 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                // Subtle top specular edge highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.6f),
                                    Color.White.copy(alpha = 0.9f),
                                    Color.White.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            )
                        )
                        .align(Alignment.TopCenter)
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // ==========================================
                    // 1. LEFT SECTION: Menu & Education Cap
                    // ==========================================
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (isVeryNarrow) 3.dp else if (isMobileScreen) 4.dp else 6.dp)
                    ) {
                        // Menu Button (White/Cream circle with gold rim and 3 golden-orange horizontal bars)
                        if (config.showMenu) {
                            Box(
                                modifier = Modifier
                                    .size(circleButtonSize)
                                    .shadow(2.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(IvoryButtonBackgroundGradient)
                                    .border(
                                        BorderStroke(1.2.dp, ButtonGoldMetallicBorderGradient),
                                        CircleShape
                                    )
                                    .clickable { onMenuClick() }
                                    .testTag("premium_topbar_menu_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(if (isMobileScreen) 2.2.dp else 2.8.dp)
                                ) {
                                    repeat(3) {
                                        Box(
                                            modifier = Modifier
                                                .width(if (isVeryNarrow) 12.dp else if (isMobileScreen) 14.dp else 17.dp)
                                                .height(if (isMobileScreen) 2.dp else 2.4.dp)
                                                .clip(RoundedCornerShape(1.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(Color(0xFFFF8F00), Color(0xFFE65100))
                                                    )
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        // Education / Graduation Cap Button (White/Cream circle with purple cap icon)
                        if (config.showAppIcon && !isVeryNarrow) {
                            Box(
                                modifier = Modifier
                                    .size(circleButtonSize)
                                    .shadow(2.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(IvoryButtonBackgroundGradient)
                                    .border(
                                        BorderStroke(1.2.dp, ButtonGoldMetallicBorderGradient),
                                        CircleShape
                                    )
                                    .clickable { onAppIconClick() }
                                    .testTag("premium_topbar_logo_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "Education / Syllabus",
                                    tint = Color(0xFF6A1B9A),
                                    modifier = Modifier.size(if (isMobileScreen) 17.dp else 22.dp)
                                )
                            }
                        }

                        // Thin vertical gold divider
                        if (!isVeryNarrow) {
                            Box(
                                modifier = Modifier
                                    .width(1.2.dp)
                                    .height(if (isMobileScreen) 18.dp else 24.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color(0xFFFFB300).copy(alpha = 0.8f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    // ==========================================
                    // 2. CENTER SECTION: Glossy White Raised Pill with Emblem
                    // ==========================================
                    if (config.showTitle || config.showSubtitle) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(if (isVeryNarrow) 38.dp else if (isMobileScreen) 44.dp else 50.dp)
                                .padding(horizontal = if (isVeryNarrow) 2.dp else if (isMobileScreen) 3.dp else 5.dp)
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    ambientColor = Color(0xFFE65100).copy(alpha = 0.25f),
                                    spotColor = Color(0xFFFFB300).copy(alpha = 0.35f)
                                )
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFFFFFFFF),
                                            Color(0xFFFFFDF8),
                                            Color(0xFFFFFFFF)
                                        )
                                    )
                                )
                                .border(
                                    BorderStroke(
                                        1.3.dp,
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFFFD54F),
                                                Color(0xFFFFB300),
                                                Color(0xFFFFE082),
                                                Color(0xFFFFB300),
                                                Color(0xFFFFD54F)
                                            )
                                        )
                                    ),
                                    RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Top curve inner specular highlight
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color.White,
                                                Color.Transparent
                                            )
                                        )
                                    )
                                    .align(Alignment.TopCenter)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Academic Gold Emblem (Open Book + Laurel Wreath + Star)
                                Box(
                                    modifier = Modifier
                                        .size(if (isVeryNarrow) 22.dp else if (isMobileScreen) 26.dp else 32.dp)
                                        .padding(end = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Academic Emblem",
                                        tint = Color(0xFFE5A100),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Main Title: "APPSC GROUP-2" in rich Magenta-to-Orange Gradient
                                    if (config.showTitle) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = titlePrefix,
                                                color = Color(0xFF9C27B0),
                                                fontWeight = FontWeight.Black,
                                                fontSize = if (isVeryNarrow) 11.5.sp else if (isMobileScreen) 13.5.sp else 17.5.sp,
                                                letterSpacing = if (isMobileScreen) 0.3.sp else 0.8.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (titleSuffix.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(if (isVeryNarrow) 2.dp else 3.dp))
                                                Text(
                                                    text = titleSuffix,
                                                    color = Color(0xFFE65100),
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = if (isVeryNarrow) 11.5.sp else if (isMobileScreen) 13.5.sp else 17.5.sp,
                                                    letterSpacing = if (isMobileScreen) 0.3.sp else 0.8.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    // Subtitle: "— EXECUTIVE & NON-EXECUTIVE —"
                                    if (config.showSubtitle && resolvedSubtitle.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            if (!isVeryNarrow) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(if (isMobileScreen) 8.dp else 16.dp)
                                                        .height(1.dp)
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                listOf(Color.Transparent, Color(0xFFE5A100))
                                                            )
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                            }
                                            Text(
                                                text = resolvedSubtitle,
                                                color = Color(0xFF0F172A),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = if (isVeryNarrow) 6.5.sp else if (isMobileScreen) 7.5.sp else 8.5.sp,
                                                letterSpacing = if (isMobileScreen) 0.3.sp else 1.2.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (!isVeryNarrow) {
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .width(if (isMobileScreen) 8.dp else 16.dp)
                                                        .height(1.dp)
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                listOf(Color(0xFFE5A100), Color.Transparent)
                                                            )
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 3. RIGHT SECTION: Tt, Wine/Magenta PREMIUM & Bell
                    // ==========================================
                    Row(
                        modifier = Modifier.padding(end = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (isVeryNarrow) 2.dp else if (isMobileScreen) 3.dp else 5.dp)
                    ) {
                        // Text Size Settings Button ("Tt")
                        if (config.showTextSettings) {
                            Box(
                                modifier = Modifier
                                    .size(squareButtonSize)
                                    .shadow(2.dp, RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(IvoryButtonBackgroundGradient)
                                    .border(
                                        BorderStroke(1.2.dp, ButtonGoldMetallicBorderGradient),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onLuxuryFontStudioClick() }
                                    .testTag("premium_topbar_font_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tt",
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (isVeryNarrow) 11.sp else if (isMobileScreen) 13.sp else 15.sp
                                )
                            }
                        }

                        // PREMIUM Button (Luxurious Deep Wine/Magenta with Golden Crown + "PREMIUM")
                        if (config.showPremium) {
                            Box(
                                modifier = Modifier
                                    .size(width = premiumButtonWidth, height = squareButtonSize)
                                    .shadow(2.dp, RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(WineMagentaPremiumGradient)
                                    .border(
                                        BorderStroke(1.3.dp, ButtonGoldMetallicBorderGradient),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onPremiumClick() }
                                    .testTag("premium_topbar_subscription_badge"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 1.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "Premium Crown",
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(if (isVeryNarrow) 12.dp else if (isMobileScreen) 14.dp else 16.dp)
                                    )
                                    Text(
                                        text = "PREMIUM",
                                        color = Color(0xFFFFE082),
                                        fontWeight = FontWeight.Black,
                                        fontSize = if (isVeryNarrow) 6.sp else if (isMobileScreen) 7.sp else 8.5.sp,
                                        letterSpacing = 0.3.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Notifications Bell Button (White/Cream with Navy Bell & Red Dot Badge)
                        if (config.showNotifications) {
                            Box(
                                modifier = Modifier
                                    .size(squareButtonSize)
                                    .shadow(2.dp, RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(IvoryButtonBackgroundGradient)
                                    .border(
                                        BorderStroke(1.2.dp, ButtonGoldMetallicBorderGradient),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onNotificationsClick() }
                                    .testTag("premium_topbar_notification_bell"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(if (isVeryNarrow) 14.dp else if (isMobileScreen) 16.dp else 19.dp)
                                    )
                                    if (unreadNotifications > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(if (isMobileScreen) 7.dp else 8.dp)
                                                .align(Alignment.TopEnd)
                                                .offset(x = 3.dp, y = (-2).dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE53935))
                                                .border(BorderStroke(1.dp, Color.White), CircleShape)
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

