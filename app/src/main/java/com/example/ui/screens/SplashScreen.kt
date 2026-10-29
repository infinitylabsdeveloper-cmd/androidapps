package com.example.ui.screens

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AnimationConfigManager
import com.example.util.SplashQuoteProvider
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pastel Rainbow Luxury colour palette for Premium Exam Portal splash screen.
 * Strictly avoids black, grey, gold, or blue.
 */
object PastelSplashPalette {
    val WarmIvory = Color(0xFFFFF8F0)
    val PastelPeach = Color(0xFFFFD6B8)
    val SoftCoral = Color(0xFFFF8FA3)
    val PastelPink = Color(0xFFF7B7D2)
    val Lavender = Color(0xFFDCC6F5)
    val LightMint = Color(0xFFC8F0DC)
    val DeepPlum = Color(0xFF64205E)
}

/**
 * Single coordinated animation state for the 5-phase cinematic splash sequence.
 */
private class SplashTimelineState(reducedMotion: Boolean) {
    // Phase A: Background, waves, and central halo
    val bgGradientAlpha = Animatable(if (reducedMotion) 1f else 0f)
    val waveAlpha = Animatable(if (reducedMotion) 1f else 0f)
    val waveTranslationY = Animatable(if (reducedMotion) 0f else 22f)
    val haloScale = Animatable(if (reducedMotion) 1f else 0.92f)
    val haloAlpha = Animatable(if (reducedMotion) 1f else 0f)

    // Phase B: Logo container & subtle glow reveal
    val logoAlpha = Animatable(if (reducedMotion) 1f else 0f)
    val logoScale = Animatable(if (reducedMotion) 1f else 0.88f)
    val logoGlowAlpha = Animatable(if (reducedMotion) 0.45f else 0f)

    // Phase C: "PREMIUM EXAM PORTAL" badge reveal
    val badgeAlpha = Animatable(if (reducedMotion) 1f else 0f)
    val badgeTranslationY = Animatable(if (reducedMotion) 0f else 14f)

    // Phase D: Quote container & post-typing author reveal
    val quoteContainerAlpha = Animatable(if (reducedMotion) 1f else 0f)
    val quoteContainerScale = Animatable(if (reducedMotion) 1f else 0.97f)
    val authorAlpha = Animatable(if (reducedMotion) 1f else 0f)
    val authorTranslationY = Animatable(if (reducedMotion) 0f else 10f)
}

/**
 * Premium Pastel Rainbow Luxury Splash Screen with coordinated 5-phase motion,
 * custom flowing pastel wave artwork, luminous graduation-cap emblem,
 * zero-layout-jump character-by-character quote typing, and pastel progress pills.
 */
@Composable
fun AppSplashScreen(
    onLoadingFinished: () -> Unit,
    modifier: Modifier = Modifier,
    typingIntervalMs: Long = 38L
) {
    val context = LocalContext.current

    // Check system & app reduced-motion settings
    val isReducedMotion = remember(context) {
        val systemScale = try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
        } catch (_: Exception) {
            1f
        }
        val masterConfig = AnimationConfigManager.getActiveEngineConfig().masterConfig
        !masterConfig.masterEnabled ||
            masterConfig.reducedMotionMode ||
            (masterConfig.respectSystemAnimatorScale && systemScale == 0f)
    }

    // Select one distinct inspirational quote for this session
    val sessionQuotePair = remember {
        SplashQuoteProvider.getQuoteForNextSession(context)
    }
    val selectedQuote = sessionQuotePair.second
    val totalProgressSteps = 5

    var typedCharCount by remember(selectedQuote) {
        mutableIntStateOf(if (isReducedMotion) selectedQuote.text.length else 0)
    }
    var activeProgressStep by remember {
        mutableIntStateOf(if (isReducedMotion) totalProgressSteps - 1 else 0)
    }

    val timeline = remember(isReducedMotion) { SplashTimelineState(isReducedMotion) }

    // Single coordinated animation timeline across Phases A -> E
    LaunchedEffect(selectedQuote, isReducedMotion) {
        if (isReducedMotion) {
            typedCharCount = selectedQuote.text.length
            activeProgressStep = totalProgressSteps - 1
            delay(900L)
            onLoadingFinished()
            return@LaunchedEffect
        }

        // Phase A — Background entrance (0ms - 450ms)
        activeProgressStep = 0
        coroutineScope {
            launch {
                timeline.bgGradientAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
                )
            }
            launch {
                timeline.waveAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 540, easing = LinearOutSlowInEasing)
                )
            }
            launch {
                timeline.waveTranslationY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 560, easing = FastOutSlowInEasing)
                )
            }
            launch {
                timeline.haloAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            }
            launch {
                timeline.haloScale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing)
                )
            }
        }

        // Phase B — Logo reveal (staggered slightly after background begins settling)
        activeProgressStep = 1
        coroutineScope {
            launch {
                timeline.logoAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
                )
            }
            launch {
                timeline.logoScale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
                )
            }
            launch {
                timeline.logoGlowAlpha.animateTo(
                    targetValue = 0.85f,
                    animationSpec = tween(durationMillis = 260, easing = LinearOutSlowInEasing)
                )
                timeline.logoGlowAlpha.animateTo(
                    targetValue = 0.48f,
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                )
            }
        }

        // Phase C — Brand badge ("PREMIUM EXAM PORTAL") gentle upward slide + fade
        activeProgressStep = 2
        coroutineScope {
            launch {
                timeline.badgeAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                )
            }
            launch {
                timeline.badgeTranslationY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)
                )
            }
            launch {
                timeline.quoteContainerAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing)
                )
            }
            launch {
                timeline.quoteContainerScale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
                )
            }
        }

        // Phase D — Inspirational quote character-by-character typing
        val quoteLength = selectedQuote.text.length
        val adaptiveInterval = when {
            quoteLength > 120 -> 24L
            quoteLength > 95 -> 28L
            else -> typingIntervalMs
        }
        val safeInterval = adaptiveInterval.coerceIn(24L, 50L)
        for (i in 1..quoteLength) {
            typedCharCount = i
            if (i == quoteLength / 2) {
                activeProgressStep = 3
            }
            delay(safeInterval)
        }

        // Reveal author only after quote finishes typing
        activeProgressStep = 4
        coroutineScope {
            launch {
                timeline.authorAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
                )
            }
            launch {
                timeline.authorTranslationY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
                )
            }
        }

        // Brief pause so user can comfortably read the completed quote & author
        delay(850L)
        onLoadingFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PastelSplashPalette.WarmIvory)
            .testTag("premium_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // =====================================================================
        // LAYER 1: PASTEL LUXURY AMBIENT GRADIENT + FLOWING EDGE WAVES + HALO
        // =====================================================================
        PastelLuxuryBackgroundCanvas(
            gradientAlpha = timeline.bgGradientAlpha.value,
            waveAlpha = timeline.waveAlpha.value,
            waveTranslationY = timeline.waveTranslationY.value,
            haloScale = timeline.haloScale.value,
            haloAlpha = timeline.haloAlpha.value
        )

        // =====================================================================
        // LAYER 2: FOREGROUND CONTENT (LOGO, BADGE, QUOTE, AUTHOR, INDICATORS)
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.9f))

            // PHASE B: Rounded-Square Graduation Cap Icon with Pink-Coral-Lavender Gradient
            PastelGraduationCapLogo(
                logoAlpha = timeline.logoAlpha.value,
                logoScale = timeline.logoScale.value,
                glowAlpha = timeline.logoGlowAlpha.value
            )

            Spacer(modifier = Modifier.height(26.dp))

            // PHASE C: Refined "PREMIUM EXAM PORTAL" Badge
            PastelBrandBadge(
                alpha = timeline.badgeAlpha.value,
                translationY = timeline.badgeTranslationY.value
            )

            Spacer(modifier = Modifier.height(28.dp))

            // PHASE D: Centrally Aligned Inspirational Quote & Post-Typing Author Reveal
            PastelInspirationalQuoteSection(
                quote = selectedQuote,
                typedCharCount = typedCharCount,
                containerAlpha = timeline.quoteContainerAlpha.value,
                containerScale = timeline.quoteContainerScale.value,
                authorAlpha = timeline.authorAlpha.value,
                authorTranslationY = timeline.authorTranslationY.value
            )

            Spacer(modifier = Modifier.weight(1.1f))

            // PHASE E: Minimal Pastel Loading / Progress Indicators near the bottom
            PastelProgressIndicators(
                totalSteps = totalProgressSteps,
                activeStep = activeProgressStep
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PastelLuxuryBackgroundCanvas(
    gradientAlpha: Float,
    waveAlpha: Float,
    waveTranslationY: Float,
    haloScale: Float,
    haloAlpha: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Soft cream base with pastel peach, pink, lavender, and mint ambient wash
        if (gradientAlpha > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to PastelSplashPalette.WarmIvory,
                        0.28f to PastelSplashPalette.PastelPeach.copy(alpha = 0.35f * gradientAlpha),
                        0.58f to PastelSplashPalette.PastelPink.copy(alpha = 0.32f * gradientAlpha),
                        0.82f to PastelSplashPalette.Lavender.copy(alpha = 0.38f * gradientAlpha),
                        1.0f to PastelSplashPalette.LightMint.copy(alpha = 0.42f * gradientAlpha)
                    )
                )
            )

            // Soft mint & lavender corner radial washes
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PastelSplashPalette.LightMint.copy(alpha = 0.48f * gradientAlpha),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.15f, h * 0.14f),
                    radius = w * 0.72f
                ),
                center = Offset(w * 0.15f, h * 0.14f),
                radius = w * 0.72f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PastelSplashPalette.Lavender.copy(alpha = 0.46f * gradientAlpha),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.88f, h * 0.22f),
                    radius = w * 0.75f
                ),
                center = Offset(w * 0.88f, h * 0.22f),
                radius = w * 0.75f
            )
        }

        // 2. Smooth flowing pastel waves near the top & bottom screen edges
        if (waveAlpha > 0f) {
            val topShift = -waveTranslationY * density
            val bottomShift = waveTranslationY * density

            // Top-left to top-right flowing peach & lavender wave
            val topOuterWave = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.15f + topShift)
                cubicTo(
                    w * 0.72f, h * 0.21f + topShift,
                    w * 0.34f, h * 0.08f + topShift,
                    0f, h * 0.18f + topShift
                )
                close()
            }
            drawPath(
                path = topOuterWave,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        PastelSplashPalette.PastelPeach.copy(alpha = 0.55f * waveAlpha),
                        PastelSplashPalette.PastelPink.copy(alpha = 0.48f * waveAlpha),
                        PastelSplashPalette.Lavender.copy(alpha = 0.52f * waveAlpha)
                    )
                )
            )

            // Top secondary delicate coral-mint wave ribbon
            val topInnerWave = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.09f + topShift * 0.7f)
                cubicTo(
                    w * 0.65f, h * 0.04f + topShift * 0.7f,
                    w * 0.30f, h * 0.14f + topShift * 0.7f,
                    0f, h * 0.09f + topShift * 0.7f
                )
                close()
            }
            drawPath(
                path = topInnerWave,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        PastelSplashPalette.LightMint.copy(alpha = 0.50f * waveAlpha),
                        PastelSplashPalette.PastelPeach.copy(alpha = 0.45f * waveAlpha)
                    )
                )
            )

            // Bottom back wave (Lavender -> Light Mint -> Pastel Peach)
            val bottomBackWave = Path().apply {
                moveTo(0f, h * 0.84f + bottomShift)
                cubicTo(
                    w * 0.28f, h * 0.77f + bottomShift,
                    w * 0.68f, h * 0.90f + bottomShift,
                    w, h * 0.81f + bottomShift
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = bottomBackWave,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        PastelSplashPalette.Lavender.copy(alpha = 0.56f * waveAlpha),
                        PastelSplashPalette.LightMint.copy(alpha = 0.52f * waveAlpha),
                        PastelSplashPalette.PastelPeach.copy(alpha = 0.58f * waveAlpha)
                    )
                )
            )

            // Bottom front wave (Pastel Peach -> Pastel Pink -> Soft Coral)
            val bottomFrontWave = Path().apply {
                moveTo(0f, h * 0.90f + bottomShift * 0.8f)
                cubicTo(
                    w * 0.36f, h * 0.96f + bottomShift * 0.8f,
                    w * 0.66f, h * 0.85f + bottomShift * 0.8f,
                    w, h * 0.91f + bottomShift * 0.8f
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = bottomFrontWave,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        PastelSplashPalette.PastelPeach.copy(alpha = 0.68f * waveAlpha),
                        PastelSplashPalette.PastelPink.copy(alpha = 0.64f * waveAlpha),
                        PastelSplashPalette.SoftCoral.copy(alpha = 0.48f * waveAlpha)
                    )
                )
            )
        }

        // 3. Subtle luminous circular halo behind the main graduation-cap logo
        if (haloAlpha > 0f) {
            val haloCenter = Offset(w * 0.5f, h * 0.33f)
            val outerHaloRadius = w * 0.44f * haloScale
            val innerHaloRadius = w * 0.24f * haloScale

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PastelSplashPalette.WarmIvory.copy(alpha = 0.92f * haloAlpha),
                        PastelSplashPalette.PastelPink.copy(alpha = 0.45f * haloAlpha),
                        PastelSplashPalette.PastelPeach.copy(alpha = 0.26f * haloAlpha),
                        Color.Transparent
                    ),
                    center = haloCenter,
                    radius = outerHaloRadius.coerceAtLeast(1f)
                ),
                center = haloCenter,
                radius = outerHaloRadius.coerceAtLeast(1f)
            )

            // Crisp luminous halo ring
            drawCircle(
                color = PastelSplashPalette.WarmIvory.copy(alpha = 0.78f * haloAlpha),
                center = haloCenter,
                radius = innerHaloRadius.coerceAtLeast(1f)
            )
        }
    }
}

@Composable
private fun PastelGraduationCapLogo(
    logoAlpha: Float,
    logoScale: Float,
    glowAlpha: Float
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .graphicsLayer {
                alpha = logoAlpha
                scaleX = logoScale
                scaleY = logoScale
            }
            .testTag("splash_logo_container")
    ) {
        // Outer luminous pastel aura ring
        Box(
            modifier = Modifier
                .size(122.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PastelSplashPalette.SoftCoral.copy(alpha = 0.38f * glowAlpha),
                            PastelSplashPalette.PastelPink.copy(alpha = 0.28f * glowAlpha),
                            PastelSplashPalette.Lavender.copy(alpha = 0.16f * glowAlpha),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Rounded-square luxury icon card with Pink, Coral, and Lavender gradient
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PastelSplashPalette.SoftCoral,
                            PastelSplashPalette.PastelPink,
                            PastelSplashPalette.Lavender
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(260f, 260f)
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PastelSplashPalette.WarmIvory.copy(alpha = 0.95f),
                            PastelSplashPalette.PastelPeach.copy(alpha = 0.80f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            // Subtle inner sheen highlight
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PastelSplashPalette.WarmIvory.copy(alpha = 0.28f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Premium Exam Portal Graduation Cap Logo",
                tint = PastelSplashPalette.DeepPlum,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun PastelBrandBadge(
    alpha: Float,
    translationY: Float
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = PastelSplashPalette.WarmIvory.copy(alpha = 0.88f),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    PastelSplashPalette.SoftCoral.copy(alpha = 0.65f),
                    PastelSplashPalette.PastelPink.copy(alpha = 0.75f),
                    PastelSplashPalette.Lavender.copy(alpha = 0.75f)
                )
            )
        ),
        modifier = Modifier
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = translationY * density
            }
            .testTag("splash_brand_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = PastelSplashPalette.SoftCoral,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "PREMIUM EXAM PORTAL",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PastelSplashPalette.DeepPlum,
                letterSpacing = 2.2.sp
            )
        }
    }
}

@Composable
private fun PastelInspirationalQuoteSection(
    quote: QuoteItem,
    typedCharCount: Int,
    containerAlpha: Float,
    containerScale: Float,
    authorAlpha: Float,
    authorTranslationY: Float
) {
    // Build AnnotatedString with pre-allocated transparent suffix so the text layout
    // never reflows or jumps vertically/horizontally as each character appears.
    val safeTypedCount = typedCharCount.coerceIn(0, quote.text.length)
    val revealedText = quote.text.substring(0, safeTypedCount)
    val unrevealedText = quote.text.substring(safeTypedCount)

    val typedAnnotatedQuote = remember(quote.text, safeTypedCount) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = PastelSplashPalette.DeepPlum)) {
                append("“")
                append(revealedText)
            }
            if (unrevealedText.isNotEmpty()) {
                withStyle(SpanStyle(color = Color.Transparent)) {
                    append(unrevealedText)
                    append("”")
                }
            } else {
                withStyle(SpanStyle(color = PastelSplashPalette.DeepPlum)) {
                    append("”")
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = PastelSplashPalette.WarmIvory.copy(alpha = 0.72f),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    PastelSplashPalette.PastelPeach.copy(alpha = 0.70f),
                    PastelSplashPalette.PastelPink.copy(alpha = 0.65f),
                    PastelSplashPalette.Lavender.copy(alpha = 0.70f)
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = containerAlpha
                scaleX = containerScale
                scaleY = containerScale
            }
            .semantics {
                contentDescription = "${quote.text} by ${quote.author}"
            }
            .testTag("splash_quote_container")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 22.dp)
        ) {
            Text(
                text = typedAnnotatedQuote,
                fontSize = 16.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                lineHeight = 25.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("splash_quote_text")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Author remains hidden until typing completes, then slides and fades in
            Text(
                text = "— ${quote.author}",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = PastelSplashPalette.DeepPlum.copy(alpha = 0.86f),
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = authorAlpha
                        translationY = authorTranslationY * density
                    }
                    .testTag("splash_quote_author")
            )
        }
    }
}

@Composable
private fun PastelProgressIndicators(
    totalSteps: Int,
    activeStep: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .semantics {
                contentDescription = "Splash progress step ${activeStep + 1} of $totalSteps"
            }
            .testTag("splash_progress_indicators")
    ) {
        for (index in 0 until totalSteps) {
            val isActive = index == activeStep
            val isCompleted = index < activeStep

            val indicatorWidth by animateDpAsState(
                targetValue = if (isActive) 28.dp else 9.dp,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = "IndicatorWidth_$index"
            )
            val indicatorAlpha by animateFloatAsState(
                targetValue = when {
                    isActive -> 1f
                    isCompleted -> 0.85f
                    else -> 0.55f
                },
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = "IndicatorAlpha_$index"
            )

            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(indicatorWidth)
                    .clip(RoundedCornerShape(50))
                    .graphicsLayer { alpha = indicatorAlpha }
                    .background(
                        brush = if (isActive) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    PastelSplashPalette.SoftCoral,
                                    PastelSplashPalette.PastelPink
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    PastelSplashPalette.PastelPeach,
                                    PastelSplashPalette.PastelPink.copy(alpha = 0.75f)
                                )
                            )
                        }
                    )
            )
        }
    }
}

data class QuoteItem(
    val text: String,
    val author: String
)
