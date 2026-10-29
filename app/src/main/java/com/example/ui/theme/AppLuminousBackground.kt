package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.SampleData
import com.example.repository.AppConfigRepository

// Ultra-Luxurious Royal Midnight & Obsidian Palette
val MidnightObsidianDeep = Color(0xFF080C18)
val MidnightRoyalSapphire = Color(0xFF0C1427)
val MidnightElevatedSlate = Color(0xFF0E1A36)
val MidnightAbyssalBase = Color(0xFF060913)

val RoyalGoldRadiance = Color(0xFFF59E0B)
val AmberAuraGlow = Color(0xFFD97706)
val GoldenSunBeam = Color(0xFFFBBF24)
val EtherealGoldLight = Color(0xFFFDE68A)

val ElectricCyanGlow = Color(0xFF0284C7)
val SkyCyanLuminous = Color(0xFF38BDF8)
val SapphireAura = Color(0xFF2563EB)
val RoyalIndigoAura = Color(0xFF4F46E5)
val EmeraldPrestigeGlow = Color(0xFF0D9488)

// Classic warm tones for light themes
val LuminousCreamTop = Color(0xFFFFFDF9)
val LuminousCreamCenter = Color(0xFFFFF7ED)
val LuminousTealTopLeft = Color(0xFF7EE8FA)

/**
 * App-wide dynamic wallpaper background supporting customizable wallpapers per-screen,
 * rendering an ultra-premium executive luminous atmosphere with golden radiance,
 * deep obsidian sapphire depth, ethereal ribbon waves, and subtle star dust.
 */
@Composable
fun AppLuminousBackground(
    modifier: Modifier = Modifier,
    screenName: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val wallpapers = SampleData.appWallpapersState
    val activeWallpaper = remember(wallpapers.toList(), screenName, SampleData.currentActiveAppId.value) {
        val currentAppId = SampleData.currentActiveAppId.value
        val enabledList = wallpapers.filter { it.isEnabled }
            .filter { it.mappedAppIds.isEmpty() || it.mappedAppIds.contains(currentAppId) }
            .sortedBy { it.order }
            
        val specific = if (screenName != null) enabledList.find { it.screenName == screenName } else null
        specific ?: enabledList.find { it.screenName == "Global Default" }
    }

    val configRepository = remember { AppConfigRepository.getInstance() }
    val currentConfig by configRepository.currentAppConfig.collectAsState()
    val themeName = currentConfig.advancedFontSettings.backgroundWallpaperTheme

    val isDark = when (themeName.lowercase()) {
        "royal parchment", "subtle heritage", "emerald breeze", "golden glow", "clean slate" -> false
        else -> true // Midnight Royal Prestige, Twilight Slate, Deep Azure are dark luxury themes
    }

    val themeColors = when (themeName.lowercase()) {
        "midnight royal prestige", "imperial obsidian gold" -> listOf(
            MidnightObsidianDeep,
            MidnightRoyalSapphire,
            MidnightElevatedSlate,
            MidnightAbyssalBase
        )
        "twilight slate" -> listOf(
            Color(0xFF0B0F19),
            Color(0xFF111827),
            Color(0xFF1F2937),
            Color(0xFF030712)
        )
        "deep azure" -> listOf(
            Color(0xFF0A192F),
            Color(0xFF0D2342),
            Color(0xFF13325B),
            Color(0xFF071120)
        )
        "royal parchment" -> listOf(
            Color(0xFFFAF7F2),
            Color(0xFFF5EFE6),
            Color(0xFFEADBCE),
            Color(0xFFDFCEBC).copy(alpha = 0.35f)
        )
        "golden glow" -> listOf(
            Color(0xFFFFFBEB),
            Color(0xFFFEF3C7),
            Color(0xFFFDE68A),
            Color(0xFFF59E0B).copy(alpha = 0.25f)
        )
        "emerald breeze" -> listOf(
            Color(0xFFF0FDF4),
            Color(0xFFDCFCE7),
            Color(0xFFBBF7D0),
            Color(0xFF86EFAC).copy(alpha = 0.3f)
        )
        "deep purple majesty" -> listOf(
            Color(0xFF2E1065), // Deepest Purple
            Color(0xFF4C1D95), // Royal Purple
            Color(0xFF6D28D9), // Vibrant Purple
            Color(0xFF0F172A)  // Dark Blue Base
        )
        "subtle heritage" -> listOf(
            Color(0xFFFFFBEB),
            Color(0xFFF3E8FF),
            Color(0xFFE9D5FF),
            Color(0xFFDDD6FE)
        )
        "clean slate" -> listOf(
            Color(0xFFF8FAFC),
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0),
            Color(0xFFCBD5E1).copy(alpha = 0.3f)
        )
        else -> listOf(
            MidnightObsidianDeep,
            MidnightRoyalSapphire,
            MidnightElevatedSlate,
            MidnightAbyssalBase
        ) // Default to Midnight Royal Prestige
    }

    // Deterministic constellation stardust particles for an elite executive ambiance
    val stardustParticles = remember {
        listOf(
            floatArrayOf(0.12f, 0.05f, 1.6f, 0.45f),
            floatArrayOf(0.86f, 0.09f, 2.2f, 0.50f),
            floatArrayOf(0.70f, 0.03f, 1.4f, 0.35f),
            floatArrayOf(0.26f, 0.20f, 1.5f, 0.30f),
            floatArrayOf(0.92f, 0.26f, 2.0f, 0.40f),
            floatArrayOf(0.06f, 0.38f, 1.3f, 0.28f),
            floatArrayOf(0.88f, 0.62f, 1.8f, 0.35f),
            floatArrayOf(0.14f, 0.74f, 2.0f, 0.32f),
            floatArrayOf(0.80f, 0.85f, 1.6f, 0.40f),
            floatArrayOf(0.48f, 0.08f, 1.2f, 0.30f),
            floatArrayOf(0.35f, 0.92f, 1.7f, 0.35f)
        )
    }

    val activeWallpaperUrl = remember(activeWallpaper, screenName) {
        activeWallpaper?.url ?: com.example.R.drawable.premium_pink_wallpaper
    }

    val wallpaperModel: Any? = remember(activeWallpaperUrl) {
        when (activeWallpaperUrl) {
            is Int -> activeWallpaperUrl
            is String -> activeWallpaperUrl.toIntOrNull() ?: activeWallpaperUrl
            else -> activeWallpaperUrl
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (wallpaperModel != null) {
            if (wallpaperModel is Int) {
                // High-performance direct native Compose Image with ContentScale.Crop
                Image(
                    painter = painterResource(id = wallpaperModel),
                    contentDescription = "Background Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val context = LocalContext.current
                val wallpaperRequest = remember(wallpaperModel, context) {
                    ImageRequest.Builder(context)
                        .data(wallpaperModel)
                        .crossfade(true)
                        .allowHardware(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build()
                }
                // Render Custom Wallpaper background asynchronously with hardware bitmap caching
                AsyncImage(
                    model = wallpaperRequest,
                    contentDescription = "Background Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (wallpaperModel != com.example.R.drawable.premium_pink_wallpaper) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDark) Color.Black.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.35f))
                )
            }
        } else {
            // Render breathtaking luxury luminous canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val width = size.width
                        val height = size.height

                        // 1. BASE VERTICAL MULTI-STOP LUXURY GRADIENT
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = themeColors
                            )
                        )

                        // 2. TOP-RIGHT ROYAL GOLD CORONA (Prestige Ambient Light)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    RoyalGoldRadiance.copy(alpha = if (isDark) 0.26f else 0.18f),
                                    AmberAuraGlow.copy(alpha = if (isDark) 0.14f else 0.10f),
                                    GoldenSunBeam.copy(alpha = if (isDark) 0.06f else 0.04f),
                                    Color.Transparent
                                ),
                                center = Offset(width * 0.88f, height * 0.08f),
                                radius = width * 0.85f
                            ),
                            radius = width * 0.85f,
                            center = Offset(width * 0.88f, height * 0.08f)
                        )

                        // 3. TOP-LEFT SAPPHIRE & ELECTRIC CYAN GLOW (Intellectual Brilliance)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ElectricCyanGlow.copy(alpha = if (isDark) 0.22f else 0.15f),
                                    SkyCyanLuminous.copy(alpha = if (isDark) 0.12f else 0.08f),
                                    SapphireAura.copy(alpha = if (isDark) 0.06f else 0.03f),
                                    Color.Transparent
                                ),
                                center = Offset(width * 0.10f, height * 0.18f),
                                radius = width * 0.75f
                            ),
                            radius = width * 0.75f,
                            center = Offset(width * 0.10f, height * 0.18f)
                        )

                        // 4. BOTTOM-CENTER MAJESTIC INDIGO & TEAL AURA (Grounded Stability)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    RoyalIndigoAura.copy(alpha = if (isDark) 0.20f else 0.10f),
                                    EmeraldPrestigeGlow.copy(alpha = if (isDark) 0.12f else 0.06f),
                                    Color.Transparent
                                ),
                                center = Offset(width * 0.50f, height * 0.94f),
                                radius = width * 0.95f
                            ),
                            radius = width * 0.95f,
                            center = Offset(width * 0.50f, height * 0.94f)
                        )

                        // 5. UPPER FLOWING ETHEREAL CURVE (Smooth Bezier with Golden Light Stroke)
                        val upperWavePath = Path().apply {
                            moveTo(-50f, height * 0.32f)
                            cubicTo(
                                width * 0.25f, height * 0.22f,
                                width * 0.40f, height * 0.10f,
                                width * 0.48f, -20f
                            )
                            lineTo(-50f, -20f)
                            close()
                        }
                        drawPath(
                            path = upperWavePath,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    RoyalGoldRadiance.copy(alpha = if (isDark) 0.08f else 0.06f),
                                    SkyCyanLuminous.copy(alpha = if (isDark) 0.04f else 0.02f),
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(width * 0.48f, height * 0.32f)
                            ),
                            style = Fill
                        )

                        // Upper Wave Golden Accent Stroke
                        val upperWaveEdge = Path().apply {
                            moveTo(-50f, height * 0.32f)
                            cubicTo(
                                width * 0.25f, height * 0.22f,
                                width * 0.40f, height * 0.10f,
                                width * 0.48f, -20f
                            )
                        }
                        drawPath(
                            path = upperWaveEdge,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    EtherealGoldLight.copy(alpha = if (isDark) 0.70f else 0.50f),
                                    RoyalGoldRadiance.copy(alpha = if (isDark) 0.85f else 0.65f),
                                    SkyCyanLuminous.copy(alpha = if (isDark) 0.40f else 0.30f)
                                ),
                                start = Offset(-50f, height * 0.32f),
                                end = Offset(width * 0.48f, -20f)
                            ),
                            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                        )

                        // 6. BOTTOM SWEEPING PANORAMIC CREST WAVE (Dark Glass & Gold Horizon)
                        val midWavePath = Path().apply {
                            moveTo(-50f, height * 0.70f)
                            cubicTo(
                                width * 0.20f, height * 0.55f,
                                width * 0.48f, height * 0.50f,
                                width * 0.80f, height * 0.64f
                            )
                            cubicTo(
                                width * 0.94f, height * 0.70f,
                                width * 0.99f, height * 0.78f,
                                width + 50f, height * 0.86f
                            )
                            lineTo(width + 50f, height + 50f)
                            lineTo(-50f, height + 50f)
                            close()
                        }
                        drawPath(
                            path = midWavePath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    if (isDark) Color(0xFF1E293B).copy(alpha = 0.25f) else SkyCyanLuminous.copy(alpha = 0.12f),
                                    if (isDark) Color(0xFF0F172A).copy(alpha = 0.45f) else ElectricCyanGlow.copy(alpha = 0.20f),
                                    if (isDark) Color(0xFF080C18).copy(alpha = 0.75f) else SapphireAura.copy(alpha = 0.30f)
                                ),
                                startY = height * 0.50f,
                                endY = height
                            ),
                            style = Fill
                        )

                        // Lower Wave Multi-Tone Glowing Edge Stroke
                        val midWaveEdge = Path().apply {
                            moveTo(-50f, height * 0.70f)
                            cubicTo(
                                width * 0.20f, height * 0.55f,
                                width * 0.48f, height * 0.50f,
                                width * 0.80f, height * 0.64f
                            )
                            cubicTo(
                                width * 0.94f, height * 0.70f,
                                width * 0.99f, height * 0.78f,
                                width + 50f, height * 0.86f
                            )
                        }
                        drawPath(
                            path = midWaveEdge,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    GoldenSunBeam.copy(alpha = if (isDark) 0.80f else 0.60f),
                                    SkyCyanLuminous.copy(alpha = if (isDark) 0.75f else 0.55f),
                                    ElectricCyanGlow.copy(alpha = if (isDark) 0.60f else 0.45f),
                                    RoyalGoldRadiance.copy(alpha = if (isDark) 0.85f else 0.65f)
                                ),
                                start = Offset(-50f, height * 0.70f),
                                end = Offset(width + 50f, height * 0.86f)
                            ),
                            style = Stroke(width = 3.0f, cap = StrokeCap.Round)
                        )

                        // 7. SACRED CONSTELLATION STARDUST (Elite Civil Service Academic Aura)
                        if (isDark) {
                            stardustParticles.forEach { p ->
                                val px = width * p[0]
                                val py = height * p[1]
                                val pr = p[2] * density
                                val pa = p[3]

                                // Inner bright star nucleus
                                drawCircle(
                                    color = EtherealGoldLight.copy(alpha = pa),
                                    radius = pr,
                                    center = Offset(px, py)
                                )
                                // Subtle outer halo
                                drawCircle(
                                    color = RoyalGoldRadiance.copy(alpha = pa * 0.4f),
                                    radius = pr * 2.2f,
                                    center = Offset(px, py)
                                )
                            }
                        }

                        // 8. SUBTLE VIGNETTE AT EDGES (Enhanced Focus on Content)
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    if (isDark) Color(0xFF04060C).copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.05f)
                                )
                            )
                        )
                    }
            )
        }
    }

    // Render the child components cleanly above the luxury background
    Box(modifier = modifier.fillMaxSize()) {
        content()
    }
}

