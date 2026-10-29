package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.AnimatableTarget

/**
 * Reusable Cascade-Drop Animation Composable for Subject -> Units -> Topics -> Sets/Subtopics accordions.
 *
 * Default Physics & Timing (configurable live via Admin Animation Studio):
 * - Enter: Alpha 0f -> 1f (200ms tween), Vertical translation -16.dp -> 0.dp, Scale 0.94f -> 1f
 *   using Spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
 *   staggered by 70ms * index.coerceAtMost(8).
 * - Exit: Alpha -> 0f, Vertical translation -> -8.dp in ~180ms without exit stagger.
 * - Uses hardware-accelerated `graphicsLayer` to prevent unnecessary recomposition.
 */
@Composable
fun CascadeItem(
    index: Int,
    visible: Boolean,
    modifier: Modifier = Modifier,
    target: AnimatableTarget = AnimatableTarget.UNIT_CARDS,
    screenKey: String? = null,
    triggerKey: Any = visible,
    initialOffsetY: Dp = 16.dp,
    initialScale: Float = 0.94f,
    staggerDelayMs: Int = 70,
    durationMs: Int = 260,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.cascadeDropItem(
            index = index,
            visible = visible,
            target = target,
            screenKey = screenKey,
            triggerKey = triggerKey,
            initialOffsetY = initialOffsetY,
            initialScale = initialScale,
            staggerDelayMs = staggerDelayMs,
            durationMs = durationMs
        )
    ) {
        content()
    }
}
