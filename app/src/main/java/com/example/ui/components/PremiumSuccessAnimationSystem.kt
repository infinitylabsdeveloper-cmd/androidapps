package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

// =========================================================================
// 1. REUSABLE MICRO-SPARKLE & AMBIENT DATA
// =========================================================================

private data class SubtleSparkle(
    val initialX: Float,
    val initialY: Float,
    val targetOffsetX: Float,
    val targetOffsetY: Float,
    val sizeDp: Float,
    val color: Color
)

private data class FloatingStardust(
    val xRatio: Float,
    val yRatio: Float,
    val speed: Float,
    val sizeDp: Float,
    val maxAlpha: Float,
    val color: Color
)

// =========================================================================
// 2. ELEGANT ANIMATED CHECKMARK (SVG STROKE DRAWING)
// =========================================================================

/**
 * Premium, smooth animated checkmark that dynamically draws its stroke from vertex
 * to completion with a subtle spring scale, giving a refined EdTech feel.
 */
@Composable
fun ElegantAnimatedCheckmark(
    visible: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    badgeColor: Color = Color(0xFF16A34A),
    checkmarkColor: Color = Color.White,
    strokeWidth: Dp = 2.4.dp
) {
    val scaleAnim = remember { Animatable(0.4f) }
    val drawProgress = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            scaleAnim.snapTo(0.4f)
            drawProgress.snapTo(0f)

            // Fast spring bounce for the badge
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = 500f
                )
            )

            // Smooth stroke draw
            drawProgress.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
        } else {
            scaleAnim.snapTo(0.4f)
            drawProgress.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(scaleAnim.value)
            .background(
                Brush.linearGradient(
                    listOf(badgeColor, badgeColor.copy(alpha = 0.90f))
                ),
                CircleShape
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.5f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.55f)) {
            val w = this.size.width
            val h = this.size.height

            // Standard checkmark path
            val path = Path().apply {
                moveTo(w * 0.15f, h * 0.50f)
                lineTo(w * 0.42f, h * 0.78f)
                lineTo(w * 0.88f, h * 0.22f)
            }

            if (drawProgress.value > 0f) {
                val measure = PathMeasure()
                measure.setPath(path, false)
                val totalLength = measure.length
                val drawLength = totalLength * drawProgress.value

                val segment = Path()
                measure.getSegment(0f, drawLength, segment, true)

                drawPath(
                    path = segment,
                    color = checkmarkColor,
                    style = Stroke(
                        width = strokeWidth.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

// =========================================================================
// 3. OPTION AMBIENT GLOW & SUBTLE SPARKLES (FOR MCQ OPTIONS)
// =========================================================================

/**
 * Non-intrusive soft glow and micro-sparkle aura placed around the selected
 * correct option in Subtopics and Sets MCQs.
 * Short, responsive (~450ms), and does NOT cover question text or buttons.
 */
@Composable
fun OptionCorrectSuccessAura(
    trigger: Boolean,
    modifier: Modifier = Modifier
) {
    if (!trigger) return

    val progress = remember { Animatable(0f) }

    val sparkles = remember {
        val rand = Random(42)
        List(8) { i ->
            val angle = (i * (2f * PI.toFloat() / 8f)) + (rand.nextFloat() * 0.2f - 0.1f)
            val dist = rand.nextFloat() * 16f + 12f // Subtle 12dp to 28dp drift
            SubtleSparkle(
                initialX = 0f,
                initialY = 0f,
                targetOffsetX = cos(angle) * dist,
                targetOffsetY = sin(angle) * dist,
                sizeDp = rand.nextFloat() * 2.5f + 2.5f, // 2.5 to 5.0 dp
                color = when (i % 3) {
                    0 -> Color(0xFFFBBF24) // Soft Gold
                    1 -> Color(0xFF34D399) // Mint Green
                    else -> Color(0xFFFFFFFF) // Pure White
                }
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
        )
    }

    val currentProg = progress.value
    if (currentProg <= 0f || currentProg >= 1f) return

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Soft Emerald Glow Aura behind right badge
        val auraAlpha = (sin(currentProg * PI.toFloat()) * 0.65f).coerceIn(0f, 0.65f)
        Box(
            modifier = Modifier
                .padding(end = 6.dp)
                .size(48.dp)
                .alpha(auraAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF34D399).copy(alpha = 0.55f),
                            Color(0xFFFBBF24).copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        // Micro-sparkles cluster around the right confirmation badge
        Canvas(
            modifier = Modifier
                .padding(end = 12.dp)
                .size(60.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val particleAlpha = (sin(currentProg * PI.toFloat()) * 1.2f).coerceIn(0f, 1f)

            sparkles.forEach { s ->
                val easeOut = 1f - (1f - currentProg) * (1f - currentProg)
                val posX = center.x + s.targetOffsetX.dp.toPx() * easeOut
                val posY = center.y + s.targetOffsetY.dp.toPx() * easeOut
                val currentSize = s.sizeDp.dp.toPx() * (1f - currentProg * 0.3f)

                // Delicate 4-point star sparkle
                drawMicroSparkle(
                    center = Offset(posX, posY),
                    radius = currentSize,
                    color = s.color.copy(alpha = particleAlpha)
                )
            }
        }
    }
}

private fun DrawScope.drawMicroSparkle(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val innerRadius = radius * 0.35f
    val numPoints = 4
    for (i in 0 until numPoints * 2) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = i * PI / numPoints - PI / 2
        val x = center.x + r * cos(angle).toFloat()
        val y = center.y + r * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

// =========================================================================
// 4. FLOATING MICRO-BANNER NOTIFICATION (SUBTLE, NON-DISTRACTING)
// =========================================================================

/**
 * Replaces the old full-screen blast with a sleek, compact success pill
 * that gently floats up and fades over 450ms.
 * Completely non-blocking and pass-through.
 */
@Composable
fun PremiumCorrectAnswerBanner(
    trigger: Int,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    if (trigger <= 0) return

    val haptic = LocalHapticFeedback.current
    val progress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {}

        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
        )
        onFinished()
    }

    val currentProg = progress.value
    if (currentProg <= 0f || currentProg >= 1f) return

    val bannerAlpha = when {
        currentProg < 0.20f -> (currentProg / 0.20f)
        currentProg > 0.70f -> ((1f - currentProg) / 0.30f).coerceIn(0f, 1f)
        else -> 1f
    }
    val bannerScale = 0.90f + 0.10f * (currentProg / 0.25f).coerceAtMost(1f)
    val bannerOffset = (-10.dp * currentProg)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .padding(top = 36.dp)
                .offset(y = bannerOffset)
                .scale(bannerScale)
                .alpha(bannerAlpha),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF064E3B).copy(alpha = 0.94f),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFF34D399), Color(0xFFFBBF24), Color(0xFF34D399))
                )
            ),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ElegantAnimatedCheckmark(
                    visible = true,
                    size = 18.dp,
                    badgeColor = Color(0xFF10B981),
                    checkmarkColor = Color.White,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Correct!",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.5.sp,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

// =========================================================================
// 5. ANIMATED PERCENTAGE & SCORE COUNTER
// =========================================================================

/**
 * Counts up smoothly from 0 to [targetPercentage] or score over [durationMillis].
 */
@Composable
fun AnimatedScoreCounter(
    targetValue: Int,
    suffix: String = "%",
    durationMillis: Int = 900,
    fontSize: TextUnit = 32.sp,
    fontWeight: FontWeight = FontWeight.Black,
    color: Color = Color(0xFF0F172A),
    fontFamily: FontFamily? = null,
    modifier: Modifier = Modifier
) {
    val countAnim = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        countAnim.snapTo(0f)
        countAnim.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
        )
    }

    Text(
        text = "${countAnim.value.roundToInt()}$suffix",
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        fontFamily = fontFamily,
        modifier = modifier
    )
}

// =========================================================================
// 6. PREMIUM ACHIEVEMENT RESULT OVERLAY (> 70% ONLY)
// =========================================================================

/**
 * Replaces the old bursting/confetti explosions on Result Screens.
 *
 * Principles:
 * - Strictly appears ONLY when [scorePercentage] is ABOVE 70.0% (> 70.0).
 * - For scores <= 70.0%: Strictly does NOT trigger celebration.
 * - Sequence:
 *   1. Card smoothly appears with gentle spring scale & fade.
 *   2. Soft emerald-gold ambient radial glow radiates behind the score card.
 *   3. Delicate, ambient floating stardust specks (10-12 particles) drift gently upwards with twinkling alpha.
 *   4. Premium "Excellent Performance — High Mastery" badge treatment.
 *   5. Completely non-blocking and pass-through so students can immediately interact with the analysis.
 */
@Composable
fun PremiumAchievementResultOverlay(
    scorePercentage: Double,
    minThreshold: Double = 70.0,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    // Strictly do not show celebration for below 70%
    if (scorePercentage < minThreshold) return

    val haptic = LocalHapticFeedback.current
    var hasTriggeredHaptic by remember { mutableStateOf(false) }
    var isOverlayActive by remember(scorePercentage) { mutableStateOf(true) }
    val celebrationProgress = remember(scorePercentage) { Animatable(0f) }

    val stardustParticles = remember {
        val rand = Random(777)
        List(12) { i ->
            FloatingStardust(
                xRatio = rand.nextFloat() * 0.84f + 0.08f,
                yRatio = rand.nextFloat() * 0.40f + 0.05f,
                speed = rand.nextFloat() * 0.4f + 0.8f,
                sizeDp = rand.nextFloat() * 3.5f + 2.5f,
                maxAlpha = rand.nextFloat() * 0.45f + 0.35f,
                color = when (i % 3) {
                    0 -> Color(0xFFFBBF24) // Warm Gold
                    1 -> Color(0xFF34D399) // Mint Green
                    else -> Color(0xFFFFFFFF) // Diamond White
                }
            )
        }
    }

    LaunchedEffect(scorePercentage) {
        if (!hasTriggeredHaptic) {
            hasTriggeredHaptic = true
            try {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                delay(120)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
        }
        celebrationProgress.snapTo(0f)
        celebrationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3200, easing = LinearEasing)
        )
        isOverlayActive = false
    }

    if (!isOverlayActive) return

    val p = celebrationProgress.value
    val envelopeAlpha = when {
        p < 0.15f -> (p / 0.15f).coerceIn(0f, 1f)
        p > 0.72f -> ((1f - p) / 0.28f).coerceIn(0f, 1f)
        else -> 1f
    }
    val glowPulse = (0.30f + 0.20f * sin(p * PI.toFloat() * 2f)) * envelopeAlpha

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        // 1. Subtle, mesmerizing ambient background glow & floating stardust
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 1f || h <= 1f) return@Canvas

            // Soft radial ambient light behind the top card area
            val glowCenter = Offset(w / 2f, h * 0.22f)
            val glowRadius = (w * 0.55f).coerceAtLeast(1f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF59E0B).copy(alpha = (glowPulse * 0.35f).coerceIn(0f, 1f)),
                        Color(0xFF10B981).copy(alpha = (glowPulse * 0.20f).coerceIn(0f, 1f)),
                        Color.Transparent
                    ),
                    center = glowCenter,
                    radius = glowRadius
                ),
                center = glowCenter,
                radius = glowRadius
            )

            // Delicate floating stardust specks
            stardustParticles.forEach { s ->
                val progress = (p * s.speed) % 1f
                val pX = w * s.xRatio + sin(progress * 2f * PI.toFloat()) * 12.dp.toPx()
                val pY = (h * s.yRatio) - (progress * 110.dp.toPx())

                val twinkle = (sin(progress * PI.toFloat()) * s.maxAlpha * envelopeAlpha).coerceIn(0f, 1f)

                drawMicroSparkle(
                    center = Offset(pX, pY),
                    radius = s.sizeDp.dp.toPx(),
                    color = s.color.copy(alpha = twinkle)
                )
            }
        }
    }
}
