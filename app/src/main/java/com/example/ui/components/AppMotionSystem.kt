package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.AnimatableTarget
import com.example.model.MicroInteractionAnimationType
import com.example.util.AnimationConfigManager

/**
 * Enterprise-grade Motion & Micro-Interaction System for the entire app,
 * dynamically powered by [AnimationConfigManager] and [PremiumAnimationEngine].
 */
object AppMotionSystem {

    // Standard Animation Durations (ms)
    const val DURATION_PRESS: Int = 120
    const val DURATION_FAST: Int = 180
    const val DURATION_SCREEN_ENTER: Int = 280
    const val DURATION_SCREEN_EXIT: Int = 240
    const val DURATION_EXPAND: Int = 280

    // Standard High-Performance Easing Curves
    val StandardEasing: Easing = FastOutSlowInEasing
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val CascadeDropEasing: Easing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)

    // Cascade-Drop Accordion Timing Constants (70ms stagger, capped at 8 items = 560ms max)
    const val CASCADE_UNIT_STAGGER_MS: Int = 70
    const val CASCADE_TOPIC_STAGGER_MS: Int = 70
    const val CASCADE_DURATION_MS: Int = 260
    const val CASCADE_MAX_DELAY_MS: Int = 560

    /**
     * Smooth downward vertical expansion transition for Subject -> Units and Unit -> Topics accordions.
     * Dynamically respects Admin Animation Studio configuration for [AnimatableTarget.ACCORDION_EXPAND_COLLAPSE].
     */
    fun cascadeContainerEnterTransition(durationMs: Int = 280): EnterTransition {
        val spec = AnimationConfigManager.resolveSpec(AnimatableTarget.ACCORDION_EXPAND_COLLAPSE)
        val slowMo = AnimationConfigManager.sandboxSlowMoFactor.value
        return PremiumAnimationEngine.buildHierarchyContainerEnter(
            spec = spec.copy(durationMs = if (spec.durationMs > 0) spec.durationMs else durationMs),
            slowMoFactor = slowMo
        )
    }

    /**
     * Smooth upward vertical collapse transition for Subject -> Units and Unit -> Topics accordions.
     * Uses ~180ms non-staggered exit duration.
     */
    fun cascadeContainerExitTransition(durationMs: Int = 180): ExitTransition {
        val spec = AnimationConfigManager.resolveSpec(AnimatableTarget.ACCORDION_EXPAND_COLLAPSE)
        val slowMo = AnimationConfigManager.sandboxSlowMoFactor.value
        return PremiumAnimationEngine.buildHierarchyContainerExit(
            spec = spec.copy(exitDurationMs = if (spec.exitDurationMs > 0) spec.exitDurationMs else durationMs),
            slowMoFactor = slowMo
        )
    }

    /**
     * Standard Forward Enter Transition for Screen Navigation.
     */
    fun enterTransition(): EnterTransition {
        val spec = AnimationConfigManager.resolveSpec(AnimatableTarget.SCREEN_NAVIGATION)
        if (!spec.enabled) return EnterTransition.None
        val dur = spec.durationMs.coerceAtLeast(80)
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> (fullWidth * 0.12f).toInt() },
            animationSpec = tween(dur, easing = EmphasizedDecelerate)
        ) + fadeIn(
            animationSpec = tween(dur, easing = StandardEasing)
        ) + scaleIn(
            initialScale = spec.initialScale.coerceIn(0.90f, 1.0f),
            animationSpec = tween(dur, easing = EmphasizedDecelerate)
        )
    }

    /**
     * Standard Forward Exit Transition for Screen Navigation.
     */
    fun exitTransition(): ExitTransition {
        val spec = AnimationConfigManager.resolveSpec(AnimatableTarget.SCREEN_NAVIGATION)
        if (!spec.enabled) return ExitTransition.None
        val dur = spec.exitDurationMs.coerceAtLeast(60)
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> -(fullWidth * 0.08f).toInt() },
            animationSpec = tween(dur, easing = EmphasizedAccelerate)
        ) + fadeOut(
            animationSpec = tween(dur, easing = StandardEasing)
        )
    }

    /**
     * Standard Backward/Pop Enter Transition for Screen Navigation.
     */
    fun popEnterTransition(): EnterTransition {
        val spec = AnimationConfigManager.resolveSpec(AnimatableTarget.SCREEN_NAVIGATION)
        if (!spec.enabled) return EnterTransition.None
        val dur = spec.durationMs.coerceAtLeast(80)
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> -(fullWidth * 0.08f).toInt() },
            animationSpec = tween(dur, easing = EmphasizedDecelerate)
        ) + fadeIn(
            animationSpec = tween(dur, easing = StandardEasing)
        ) + scaleIn(
            initialScale = spec.initialScale.coerceIn(0.90f, 1.0f),
            animationSpec = tween(dur, easing = EmphasizedDecelerate)
        )
    }

    /**
     * Standard Backward/Pop Exit Transition for Screen Navigation.
     */
    fun popExitTransition(): ExitTransition {
        val spec = AnimationConfigManager.resolveSpec(AnimatableTarget.SCREEN_NAVIGATION)
        if (!spec.enabled) return ExitTransition.None
        val dur = spec.exitDurationMs.coerceAtLeast(60)
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> (fullWidth * 0.12f).toInt() },
            animationSpec = tween(dur, easing = EmphasizedAccelerate)
        ) + fadeOut(
            animationSpec = tween(dur, easing = StandardEasing)
        )
    }

    /**
     * Smoothly animates an accordion chevron icon between 0f (collapsed) and 180f (expanded).
     */
    @Composable
    fun animateAccordionChevronRotation(
        isExpanded: Boolean,
        durationMs: Int = 250,
        label: String = "accordionChevronRotation"
    ): Float {
        val spec = AnimationConfigManager.resolveSpec(AnimatableTarget.ACCORDION_EXPAND_COLLAPSE)
        val slowMo by AnimationConfigManager.sandboxSlowMoFactor
        if (!spec.enabled || spec.microInteractionType == MicroInteractionAnimationType.NONE) {
            return if (isExpanded) 180f else 0f
        }
        val effectiveDuration = ((if (spec.durationMs > 0) spec.durationMs else durationMs) / slowMo.coerceIn(0.15f, 2f)).toInt().coerceAtLeast(60)
        val rotation by animateFloatAsState(
            targetValue = if (isExpanded) 180f else 0f,
            animationSpec = tween(
                durationMillis = effectiveDuration,
                easing = CascadeDropEasing
            ),
            label = label
        )
        return rotation
    }
}

/**
 * Smoothly animates an accordion chevron icon between 0f (collapsed) and 180f (expanded).
 */
@Composable
fun animateAccordionChevronRotation(
    isExpanded: Boolean,
    durationMs: Int = 250
): State<Float> {
    val spec = AnimationConfigManager.resolveSpec(AnimatableTarget.ACCORDION_EXPAND_COLLAPSE)
    val slowMo by AnimationConfigManager.sandboxSlowMoFactor
    val effectiveDuration = if (!spec.enabled || spec.microInteractionType == MicroInteractionAnimationType.NONE) {
        0
    } else {
        ((if (spec.durationMs > 0) spec.durationMs else durationMs) / slowMo.coerceIn(0.15f, 2f)).toInt().coerceAtLeast(60)
    }
    return animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = if (effectiveDuration == 0) snap() else tween(
            durationMillis = effectiveDuration,
            easing = AppMotionSystem.CascadeDropEasing
        ),
        label = "accordionChevronRotation"
    )
}

/**
 * Premium Cascade-Drop Opening Animation Modifier for Subject -> Units -> Topics -> Sets/Subtopics cards.
 *
 * Connected to [AnimationConfigManager] and [PremiumAnimationEngine]:
 * - Default Enter: Alpha 0f -> 1f (200ms tween), Vertical translation -16.dp -> 0.dp,
 *   Scale 0.94f -> 1f with Spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
 *   staggered by 70ms * index.coerceAtMost(8).
 * - Default Exit: Alpha -> 0f, Vertical translation -> -8.dp in ~180ms without exit stagger.
 * - Dynamically switches to any Entrance, Exit, or Hierarchy Drill-Down effect selected in Admin Studio.
 */
fun Modifier.cascadeDropItem(
    index: Int,
    visible: Boolean = true,
    target: AnimatableTarget = AnimatableTarget.UNIT_CARDS,
    screenKey: String? = null,
    triggerKey: Any = visible,
    initialOffsetY: Dp = 16.dp,
    initialScale: Float = 0.94f,
    staggerDelayMs: Int = AppMotionSystem.CASCADE_UNIT_STAGGER_MS,
    durationMs: Int = AppMotionSystem.CASCADE_DURATION_MS,
    maxTotalDelayMs: Int = AppMotionSystem.CASCADE_MAX_DELAY_MS
): Modifier = composed {
    this.premiumTargetAnimation(
        target = target,
        index = index,
        visible = visible,
        screenKey = screenKey,
        triggerKey = triggerKey
    )
}

/**
 * Universal tactile press feedback for cards, list items, dialog options, and actionable surfaces.
 * Connected to [AnimationConfigManager] via [premiumCardInteraction].
 */
fun Modifier.premiumPress(
    targetScale: Float = 0.97f,
    durationMs: Int = AppMotionSystem.DURATION_PRESS,
    target: AnimatableTarget = AnimatableTarget.SUBJECT_CARDS,
    screenKey: String? = null,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    this.premiumCardInteraction(
        target = target,
        interactionSource = interactionSource,
        onClick = onClick
    )
}

/**
 * Bouncy, responsive micro-interaction specifically for primary buttons, FABs, and action chips.
 * Connected to [AnimationConfigManager] for [AnimatableTarget.BUTTONS_CTAS].
 */
fun Modifier.buttonBounce(
    targetScale: Float = 0.94f,
    durationMs: Int = AppMotionSystem.DURATION_PRESS,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val spec = AnimationConfigManager.resolveSpec(AnimatableTarget.BUTTONS_CTAS)
    val currentInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by currentInteractionSource.collectIsPressedAsState()

    val effectiveScaleTarget = if (spec.enabled) spec.pressScale.coerceIn(0.85f, 1.0f) else 1f
    val scale by animateFloatAsState(
        targetValue = if (isPressed && spec.enabled) effectiveScaleTarget else 1f,
        animationSpec = spring(
            dampingRatio = spec.dampingRatio.coerceIn(0.35f, 0.95f),
            stiffness = spec.stiffness.coerceIn(200f, 1500f)
        ),
        label = "buttonBounceScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
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
 * Smooth entrance animation for lists and cards on initial render.
 */
fun Modifier.smoothStaggeredEntrance(
    index: Int,
    delayPerItemMs: Int = 35,
    durationMs: Int = 260,
    target: AnimatableTarget = AnimatableTarget.APP_LAUNCH_HOME_ENTRANCE
): Modifier = composed {
    this.premiumTargetAnimation(
        target = target,
        index = index,
        visible = true
    )
}
