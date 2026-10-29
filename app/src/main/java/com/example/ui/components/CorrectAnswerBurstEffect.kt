package com.example.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import com.example.model.BurstEffectType
import kotlinx.coroutines.isActive
import kotlin.math.*
import kotlin.random.Random

/**
 * High-performance 60fps Canvas engine rendering all 10 authentic correct-answer burst effects:
 *
 * 1. Confetti Pop (Default for all sections)
 * 2. Star Burst
 * 3. Ripple Rings
 * 4. Fireworks
 * 5. Emoji Burst
 * 6. Sparkle Shine
 * 7. Hearts Rise
 * 8. Coin Shower
 * 9. Big Check Pop
 * 10. Shatter Flash
 */

private val CONFETTI_PALETTE = listOf(
    Color(0xFFFF595E),
    Color(0xFFFFCA3A),
    Color(0xFF8AC926),
    Color(0xFF1982C4),
    Color(0xFF6A4C93),
    Color(0xFFFF924C)
)

private val STAR_PALETTE = listOf(
    Color(0xFFFFD60A),
    Color(0xFFFFC300),
    Color(0xFFFF9F1C)
)

private val SPARKLE_PALETTE = listOf(
    Color(0xFFFFC800),
    Color(0xFF22C55E),
    Color(0xFFFF9F1C),
    Color(0xFF4EA8FF)
)

private val SHARD_PALETTE = listOf(
    Color(0xFF16A34A),
    Color(0xFF22C55E),
    Color(0xFF86EFAC),
    Color(0xFFFACC15)
)

private val EMOJI_BURST_SET = listOf("🎉", "✨", "⭐", "👏", "🎊", "💯")
private val HEARTS_SET = listOf("💚", "💖", "💛", "💙", "🧡")

private data class EffectParticle(
    val type: String,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val g: Float = 0f,
    val drag: Float = 0.98f,
    val size: Float = 10f,
    val color: Color = Color(0xFF22C55E),
    var rot: Float = 0f,
    val vr: Float = 0f,
    val life: Float = 60f,
    var age: Float = 0f,
    var delay: Float = 0f,
    val wob: Float = 0f,
    val phase: Float = Random.nextFloat() * 6.28f,
    val emoji: String = "✨",
    val speed: Float = 3.4f
)

@Composable
fun CorrectAnswerBurstEffect(
    trigger: Int,
    effectName: String = BurstEffectType.DEFAULT.displayName,
    modifier: Modifier = Modifier,
    originXFraction: Float = 0.5f,
    originYFraction: Float = 0.5f,
    onFinished: () -> Unit = {}
) {
    if (trigger <= 0) return

    val density = LocalDensity.current
    val dp = density.density.coerceAtLeast(1.0f)

    val particles = remember { mutableStateListOf<EffectParticle>() }
    var screenFlashAlpha by remember { mutableFloatStateOf(0f) }
    var bigCheckProgress by remember { mutableFloatStateOf(0f) }
    var isRunning by remember { mutableStateOf(false) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val resolvedEffect = remember(effectName) {
        BurstEffectType.fromDisplayName(effectName)
    }

    // Auto-dismiss timer: Automatically reset and clear particles after 1.5 seconds
    LaunchedEffect(trigger, effectName) {
        if (trigger > 0) {
            kotlinx.coroutines.delay(1500L)
            particles.clear()
            screenFlashAlpha = 0f
            bigCheckProgress = 0f
            isRunning = false
            onFinished()
        }
    }

    // Drive physics and smooth frame loop
    LaunchedEffect(trigger, effectName) {
        if (trigger <= 0) {
            particles.clear()
            screenFlashAlpha = 0f
            bigCheckProgress = 0f
            isRunning = false
            return@LaunchedEffect
        }

        particles.clear()
        screenFlashAlpha = 0f
        bigCheckProgress = 0f
        isRunning = true

        when (resolvedEffect) {
            BurstEffectType.CONFETTI_POP -> {
                // 70 rectangular confetti tumbling with 3D rotation and upward fountain arc
                for (i in 0 until 70) {
                    val angle = Random.nextFloat() * (-0.14f - (-3f)) + (-3f)
                    val speed = (Random.nextFloat() * (13f - 5.5f) + 5.5f) * dp
                    particles.add(
                        EffectParticle(
                            type = "rect",
                            x = (Random.nextFloat() - 0.5f) * 60f * dp,
                            y = (Random.nextFloat() - 0.5f) * 20f * dp,
                            vx = cos(angle) * speed,
                            vy = sin(angle) * speed,
                            g = 0.32f * dp,
                            drag = 0.985f,
                            size = (Random.nextFloat() * (14f - 8f) + 8f) * dp,
                            color = CONFETTI_PALETTE.random(),
                            vr = (Random.nextFloat() - 0.5f) * 0.35f,
                            life = Random.nextFloat() * (110f - 75f) + 75f
                        )
                    )
                }
            }

            BurstEffectType.STAR_BURST -> {
                // 22 radiating golden stars flying outward with drag and rotation
                val count = 22
                for (i in 0 until count) {
                    val angle = (2f * PI.toFloat() * i / count) + (Random.nextFloat() - 0.5f) * 0.25f
                    val speed = (Random.nextFloat() * (9.5f - 4.5f) + 4.5f) * dp
                    particles.add(
                        EffectParticle(
                            type = "star",
                            x = 0f,
                            y = 0f,
                            vx = cos(angle) * speed,
                            vy = sin(angle) * speed,
                            drag = 0.95f,
                            size = (Random.nextFloat() * (15f - 9f) + 9f) * dp,
                            color = STAR_PALETTE.random(),
                            vr = 0.12f,
                            life = 60f
                        )
                    )
                }
            }

            BurstEffectType.RIPPLE_RINGS -> {
                // 4 expanding concentric emerald shockwave rings
                for (i in 0 until 4) {
                    particles.add(
                        EffectParticle(
                            type = "ring",
                            x = 0f,
                            y = 0f,
                            size = 12f * dp,
                            speed = 4.2f * dp,
                            life = 65f,
                            delay = i * 10f,
                            color = if (i % 2 == 1) Color(0xFF86EFAC) else Color(0xFF16A34A)
                        )
                    )
                }
            }

            BurstEffectType.FIREWORKS -> {
                // 3 staged aerial firework bursts staggered by delay
                val burstOffsets = listOf(
                    Offset(0f, -40f * dp) to 0f,
                    Offset(-70f * dp, -90f * dp) to 16f,
                    Offset(70f * dp, -80f * dp) to 32f
                )
                burstOffsets.forEach { (offset, burstDelay) ->
                    val bx = offset.x
                    val by = offset.y
                    val baseCol = CONFETTI_PALETTE.random()
                    val secCol = CONFETTI_PALETTE.random()
                    val count = 30
                    for (i in 0 until count) {
                        val angle = (2f * PI.toFloat() * i / count) + (Random.nextFloat() - 0.5f) * 0.2f
                        val speed = (Random.nextFloat() * (8.5f - 3.2f) + 3.2f) * dp
                        particles.add(
                            EffectParticle(
                                type = "spark",
                                x = bx,
                                y = by,
                                vx = cos(angle) * speed,
                                vy = sin(angle) * speed,
                                g = 0.12f * dp,
                                drag = 0.955f,
                                size = 4f * dp,
                                color = if (Random.nextFloat() < 0.35f) secCol else baseCol,
                                life = Random.nextFloat() * (70f - 50f) + 50f,
                                delay = burstDelay
                            )
                        )
                    }
                }
            }

            BurstEffectType.EMOJI_BURST -> {
                // 16 celebration emojis bursting radially outward with gentle gravity
                val count = 16
                for (i in 0 until count) {
                    val angle = (2f * PI.toFloat() * i / count) + (Random.nextFloat() - 0.5f) * 0.2f
                    val speed = (Random.nextFloat() * (9f - 4.5f) + 4.5f) * dp
                    particles.add(
                        EffectParticle(
                            type = "emoji",
                            emoji = EMOJI_BURST_SET.random(),
                            x = 0f,
                            y = 0f,
                            vx = cos(angle) * speed,
                            vy = sin(angle) * speed,
                            g = 0.22f * dp,
                            drag = 0.96f,
                            size = (Random.nextFloat() * (28f - 20f) + 20f) * dp,
                            vr = (Random.nextFloat() - 0.5f) * 0.18f,
                            life = 80f
                        )
                    )
                }
            }

            BurstEffectType.SPARKLE_SHINE -> {
                // 24 diamond sparkles scattered over the area with staggered twinkle
                for (i in 0 until 24) {
                    particles.add(
                        EffectParticle(
                            type = "sparkle",
                            x = (Random.nextFloat() - 0.5f) * 220f * dp,
                            y = (Random.nextFloat() - 0.5f) * 160f * dp,
                            size = (Random.nextFloat() * (16f - 10f) + 10f) * dp,
                            rot = Random.nextFloat(),
                            vr = 0.04f,
                            life = 55f,
                            delay = Random.nextFloat() * 25f,
                            color = SPARKLE_PALETTE.random()
                        )
                    )
                }
            }

            BurstEffectType.HEARTS_RISE -> {
                // 18 floating colorful hearts rising upward with gentle sine wobble
                for (i in 0 until 18) {
                    particles.add(
                        EffectParticle(
                            type = "emoji",
                            emoji = HEARTS_SET.random(),
                            x = (Random.nextFloat() - 0.5f) * 160f * dp,
                            y = (Random.nextFloat() * 40f) * dp,
                            vy = -(Random.nextFloat() * (4.2f - 2.2f) + 2.2f) * dp,
                            drag = 1.0f,
                            size = (Random.nextFloat() * (26f - 18f) + 18f) * dp,
                            wob = (Random.nextFloat() * (1.2f - 0.6f) + 0.6f) * dp,
                            life = Random.nextFloat() * (100f - 75f) + 75f,
                            delay = Random.nextFloat() * 25f
                        )
                    )
                }
            }

            BurstEffectType.COIN_SHOWER -> {
                // 28 gleaming gold coins showering down like a winning jackpot
                for (i in 0 until 28) {
                    particles.add(
                        EffectParticle(
                            type = "coin",
                            x = (Random.nextFloat() - 0.5f) * 50f * dp,
                            y = 0f,
                            vx = (Random.nextFloat() - 0.5f) * 8f * dp,
                            vy = -(Random.nextFloat() * (14f - 8f) + 8f) * dp,
                            g = 0.38f * dp,
                            drag = 0.99f,
                            size = (Random.nextFloat() * (13f - 9f) + 9f) * dp,
                            life = Random.nextFloat() * (100f - 80f) + 80f
                        )
                    )
                }
            }

            BurstEffectType.BIG_CHECK_POP -> {
                // Centered large emerald checkmark badge + 3 shockwave rings
                bigCheckProgress = 1f
                for (i in 0 until 3) {
                    particles.add(
                        EffectParticle(
                            type = "ring",
                            x = 0f,
                            y = 0f,
                            size = 36f * dp,
                            speed = 3.6f * dp,
                            life = 60f,
                            delay = i * 12f,
                            color = Color(0xFF16A34A)
                        )
                    )
                }
            }

            BurstEffectType.SHATTER_FLASH -> {
                // Emerald flash + 48 crystal triangle shards
                screenFlashAlpha = 0.45f
                for (i in 0 until 48) {
                    val angle = Random.nextFloat() * 2f * PI.toFloat()
                    val speed = (Random.nextFloat() * (9.5f - 3.5f) + 3.5f) * dp
                    particles.add(
                        EffectParticle(
                            type = "shard",
                            x = (Random.nextFloat() - 0.5f) * 80f * dp,
                            y = (Random.nextFloat() - 0.5f) * 40f * dp,
                            vx = cos(angle) * speed,
                            vy = sin(angle) * speed - 1.8f * dp,
                            g = 0.28f * dp,
                            drag = 0.97f,
                            size = (Random.nextFloat() * (12f - 6f) + 6f) * dp,
                            color = SHARD_PALETTE.random(),
                            vr = (Random.nextFloat() - 0.5f) * 0.4f,
                            life = Random.nextFloat() * (80f - 55f) + 55f
                        )
                    )
                }
            }
        }

        // 60fps VSYNC frame loop
        while (isActive && isRunning) {
            withFrameMillis { frameTime ->
                frameTick = frameTime
                var activeCount = 0

                if (screenFlashAlpha > 0f) {
                    screenFlashAlpha = (screenFlashAlpha - 0.022f).coerceAtLeast(0f)
                }

                if (bigCheckProgress > 0f) {
                    bigCheckProgress = (bigCheckProgress - 0.016f).coerceAtLeast(0f)
                }

                for (i in particles.indices) {
                    val p = particles[i]
                    if (p.delay > 0f) {
                        p.delay -= 1f
                        activeCount++
                        continue
                    }

                    p.age += 1f
                    if (p.age < p.life) {
                        activeCount++
                        p.vx *= p.drag
                        p.vy = p.vy * p.drag + p.g
                        p.x += p.vx + if (p.wob > 0f) sin(p.age * 0.12f + p.phase) * p.wob else 0f
                        p.y += p.vy
                        p.rot += p.vr
                    }
                }

                if (activeCount == 0 && screenFlashAlpha <= 0f && bigCheckProgress <= 0f) {
                    isRunning = false
                    particles.clear()
                    onFinished()
                }
            }
        }
    }

    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            color = android.graphics.Color.WHITE
        }
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Read frameTick to continuously invalidate DrawScope on every VSYNC frame while running
            if (!isRunning && particles.isEmpty() && screenFlashAlpha <= 0f && bigCheckProgress <= 0f) {
                return@Canvas
            }
            // Active read ensures Compose dependency tracking on frame updates
            val tick = frameTick
            if (tick < 0L) return@Canvas

            val w = size.width
            val h = size.height
            val ox = w * originXFraction
            val oy = h * originYFraction

            // 1. Screen flash (Shatter Flash)
            if (screenFlashAlpha > 0f) {
                drawRect(
                    color = Color(0xFF22C55E).copy(alpha = screenFlashAlpha.coerceIn(0f, 1f)),
                    size = Size(w, h)
                )
            }

            // 2. Big Check Pop centered badge
            if (bigCheckProgress > 0f) {
                val popScale = when {
                    bigCheckProgress > 0.8f -> (1f - (bigCheckProgress - 0.8f) / 0.2f) * 1.15f
                    bigCheckProgress > 0.7f -> 1.15f - (0.8f - bigCheckProgress) / 0.1f * 0.15f
                    else -> bigCheckProgress / 0.7f
                }
                val checkAlpha = (bigCheckProgress / 0.3f).coerceIn(0f, 1f)

                if (popScale > 0.01f && checkAlpha > 0.01f) {
                    val badgeR = 48f * dp
                    scale(scale = popScale, pivot = Offset(ox, oy)) {
                        drawCircle(
                            color = Color(0xFF16A34A).copy(alpha = checkAlpha),
                            radius = badgeR,
                            center = Offset(ox, oy)
                        )
                        drawCircle(
                            color = Color(0xFF86EFAC).copy(alpha = checkAlpha * 0.7f),
                            radius = badgeR * 1.12f,
                            center = Offset(ox, oy),
                            style = Stroke(width = 3.5f * dp)
                        )
                        val checkPath = Path().apply {
                            moveTo(ox - 18f * dp, oy + 2f * dp)
                            lineTo(ox - 6f * dp, oy + 15f * dp)
                            lineTo(ox + 20f * dp, oy - 14f * dp)
                        }
                        drawPath(
                            path = checkPath,
                            color = Color.White.copy(alpha = checkAlpha),
                            style = Stroke(width = 6.5f * dp, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }
                }
            }

            // 3. Render all active particles centered around (ox, oy)
            for (i in particles.indices) {
                val p = particles[i]
                if (p.delay > 0f || p.age >= p.life) continue

                val progress = p.age / p.life
                val alpha = if (progress < 0.65f) 1f else (1f - progress) / 0.35f
                val px = ox + p.x
                val py = oy + p.y

                when (p.type) {
                    "rect" -> {
                        // Confetti tumbling 3D
                        val cosY = cos(p.age * 0.25f + p.phase)
                        val safeScaleY = if (abs(cosY) < 0.08f) 0.08f * (if (cosY >= 0f) 1f else -1f) else cosY
                        rotate(degrees = p.rot * 57.3f, pivot = Offset(px, py)) {
                            scale(scaleX = 1f, scaleY = safeScaleY, pivot = Offset(px, py)) {
                                drawRect(
                                    color = p.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                                    topLeft = Offset(px - p.size / 2f, py - p.size / 3f),
                                    size = Size(p.size, p.size * 0.66f)
                                )
                            }
                        }
                    }

                    "star" -> {
                        // 5-pointed star
                        rotate(degrees = p.rot * 57.3f, pivot = Offset(px, py)) {
                            drawStar(
                                cx = px,
                                cy = py,
                                radius = p.size,
                                points = 5,
                                innerRatio = 0.42f,
                                color = p.color.copy(alpha = alpha.coerceIn(0f, 1f))
                            )
                        }
                    }

                    "spark" -> {
                        // Aerial firework trail spark
                        val sparkW = (p.size * (1f - progress * 0.4f)).coerceAtLeast(2f)
                        val tailX = px - p.vx * 3f
                        val tailY = py - p.vy * 3f
                        drawLine(
                            color = p.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                            start = Offset(tailX, tailY),
                            end = Offset(px, py),
                            strokeWidth = sparkW,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }

                    "ring" -> {
                        // Shockwave ring
                        val currentRadius = p.size + p.age * p.speed
                        val strokeW = (1.5f + 4.5f * (1f - progress)).coerceAtLeast(1.2f)
                        drawCircle(
                            color = p.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                            radius = currentRadius,
                            center = Offset(px, py),
                            style = Stroke(width = strokeW * dp)
                        )
                    }

                    "emoji" -> {
                        // Celebration & Heart emojis
                        rotate(degrees = p.rot * 57.3f, pivot = Offset(px, py)) {
                            drawIntoCanvas { canvas ->
                                textPaint.textSize = p.size
                                textPaint.color = android.graphics.Color.WHITE
                                textPaint.alpha = (alpha.coerceIn(0f, 1f) * 255).toInt()
                                canvas.nativeCanvas.drawText(p.emoji, px, py + p.size * 0.35f, textPaint)
                            }
                        }
                    }

                    "sparkle" -> {
                        // 4-pointed diamond star sparkle
                        val pulse = sin(progress * PI.toFloat()).coerceAtLeast(0f)
                        val sparkleRadius = p.size * pulse
                        if (sparkleRadius > 0.5f) {
                            rotate(degrees = p.rot * 57.3f, pivot = Offset(px, py)) {
                                drawStar(
                                    cx = px,
                                    cy = py,
                                    radius = sparkleRadius,
                                    points = 4,
                                    innerRatio = 0.20f,
                                    color = p.color.copy(alpha = alpha.coerceIn(0f, 1f))
                                )
                            }
                        }
                    }

                    "coin" -> {
                        // 3D Spinning Gold Coin
                        val cosY = cos(p.age * 0.20f + p.phase)
                        val safeScaleY = if (abs(cosY) < 0.08f) 0.08f * (if (cosY >= 0f) 1f else -1f) else cosY
                        scale(scaleX = 1f, scaleY = safeScaleY, pivot = Offset(px, py)) {
                            drawCircle(
                                color = Color(0xFFF59E0B).copy(alpha = alpha.coerceIn(0f, 1f)),
                                radius = p.size,
                                center = Offset(px, py)
                            )
                            drawCircle(
                                color = Color(0xFFFFD95A).copy(alpha = alpha.coerceIn(0f, 1f)),
                                radius = p.size * 0.65f,
                                center = Offset(px, py)
                            )
                        }
                    }

                    "shard" -> {
                        // Crystal triangular shard
                        rotate(degrees = p.rot * 57.3f, pivot = Offset(px, py)) {
                            val path = Path().apply {
                                moveTo(px, py - p.size)
                                lineTo(px + p.size * 0.7f, py + p.size * 0.6f)
                                lineTo(px - p.size * 0.7f, py + p.size * 0.6f)
                                close()
                            }
                            drawPath(
                                path = path,
                                color = p.color.copy(alpha = alpha.coerceIn(0f, 1f))
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws an n-pointed geometric star path
 */
private fun DrawScope.drawStar(
    cx: Float,
    cy: Float,
    radius: Float,
    points: Int,
    innerRatio: Float,
    color: Color
) {
    val path = Path()
    val totalPoints = points * 2
    for (i in 0 until totalPoints) {
        val angle = i * PI.toFloat() / points - PI.toFloat() / 2f
        val d = if (i % 2 == 1) radius * innerRatio else radius
        val x = cx + cos(angle) * d
        val y = cy + sin(angle) * d
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path = path, color = color)
}
