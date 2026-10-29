package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Premium localized correct-answer particle burst animation.
 * Emits 45 refined colorful particles with a 1.4-second smooth fade-out,
 * subtle glow, and non-disruptive overlay behaviour.
 */
@Composable
fun CorrectAnswerCelebration(
    trigger: Boolean,
    modifier: Modifier = Modifier
) {
    if (!trigger) return

    val particles = remember {
        List(45) {
            Particle(
                color = listOf(
                    Color(0xFF10B981), // Emerald
                    Color(0xFF3B82F6), // Blue
                    Color(0xFFF59E0B), // Gold
                    Color(0xFF8B5CF6), // Purple
                    Color(0xFFEC4899), // Pink
                    Color(0xFF06B6D4)  // Cyan
                ).random(),
                angle = Random.nextDouble(0.0, 2.0 * Math.PI),
                velocity = Random.nextDouble(3.0, 7.0),
                rotation = Random.nextDouble(0.0, 360.0),
                size = Random.nextDouble(3.0, 6.5),
                type = Random.nextInt(0, 3) // 0: Square, 1: Rect, 2: Diamond
            )
        }
    }

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
            )
        }
    }

    if (animationProgress.value > 0f && animationProgress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val progress = animationProgress.value
            
            // Subtle gravity acceleration
            val gravity = 4f * progress * progress
            
            particles.forEach { particle ->
                val distance = (particle.velocity * progress * 55.0).toFloat()
                val x = center.x + cos(particle.angle).toFloat() * distance
                val y = center.y + sin(particle.angle).toFloat() * distance + gravity
                
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val pSize = particle.size.toFloat() * (1.2f - 0.2f * progress)
                
                rotate(particle.rotation.toFloat() + (progress * 540f), pivot = Offset(x, y)) {
                    when (particle.type) {
                        0 -> drawRect(
                            particle.color.copy(alpha = alpha),
                            Offset(x - pSize / 2f, y - pSize / 2f),
                            size = androidx.compose.ui.geometry.Size(pSize, pSize)
                        )
                        1 -> drawRect(
                            particle.color.copy(alpha = alpha),
                            Offset(x - pSize * 1.5f / 2f, y - pSize / 2f),
                            size = androidx.compose.ui.geometry.Size(pSize * 1.5f, pSize)
                        )
                        2 -> {
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(x, y - pSize * 1.2f)
                                lineTo(x + pSize * 1.2f, y)
                                lineTo(x, y + pSize * 1.2f)
                                lineTo(x - pSize * 1.2f, y)
                                close()
                            }
                            drawPath(path, particle.color.copy(alpha = alpha))
                        }
                    }
                }
            }
        }
    }
}

private data class Particle(
    val color: Color,
    val angle: Double,
    val velocity: Double,
    val rotation: Double,
    val size: Double,
    val type: Int
)
