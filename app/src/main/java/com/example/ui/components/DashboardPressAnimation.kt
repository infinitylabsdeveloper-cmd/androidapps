package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable press animation for dashboard rectangular cards and interactive boxes:
 * - Scales down to 0.975f (approx 0.97f–0.98f) when pressed
 * - Subtle elevation/translation shift for tactile feel
 * - Fast, smooth 140ms FastOutSlowInEasing animation
 * - Restores smoothly on release without bouncing
 */
fun Modifier.dashboardPressAnimation(
    targetScale: Float = 0.975f,
    durationMs: Int = 140,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val currentInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by currentInteractionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec = tween(
            durationMillis = durationMs,
            easing = FastOutSlowInEasing
        ),
        label = "dashboardPressScale"
    )

    val translateY by animateFloatAsState(
        targetValue = if (isPressed) 1.2f else 0f,
        animationSpec = tween(
            durationMillis = durationMs,
            easing = FastOutSlowInEasing
        ),
        label = "dashboardPressTranslateY"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.translationY = translateY
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = currentInteractionSource,
                    indication = null
                ) {
                    onClick()
                }
            } else {
                Modifier
            }
        )
}

/**
 * State helper holding animated scale and dynamic elevation for Surfaces/Cards.
 */
class CardPressAnimationState(
    val isPressed: Boolean,
    val scale: Float,
    val elevation: Dp,
    val interactionSource: MutableInteractionSource
)

@Composable
fun rememberCardPressAnimationState(
    targetScale: Float = 0.975f,
    defaultElevation: Dp = 2.dp,
    pressedElevation: Dp = 0.5.dp,
    durationMs: Int = 140
): CardPressAnimationState {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "cardPressScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) pressedElevation else defaultElevation,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "cardPressElevation"
    )

    return remember(isPressed, scale, elevation, interactionSource) {
        CardPressAnimationState(
            isPressed = isPressed,
            scale = scale,
            elevation = elevation,
            interactionSource = interactionSource
        )
    }
}
