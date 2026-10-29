package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.util.AnimationConfigManager
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Reusable Maximum Premium Animation Engine for Kotlin + Jetpack Compose.
 *
 * Uses hardware-accelerated `graphicsLayer` transforms (alpha, translationX/Y,
 * scaleX/Y, rotationX/Y/Z, cameraDistance, shadowElevation) and custom clip paths
 * to deliver 60fps animations across all 10 animation families.
 */
object PremiumAnimationEngine {

    /**
     * Converts [AnimationEasingCurve] to a Jetpack Compose [Easing] instance.
     */
    fun resolveEasing(curve: AnimationEasingCurve): Easing {
        return when (curve) {
            AnimationEasingCurve.STANDARD -> FastOutSlowInEasing
            AnimationEasingCurve.FAST_OUT_SLOW_IN -> FastOutSlowInEasing
            AnimationEasingCurve.LINEAR_OUT_SLOW_IN -> LinearOutSlowInEasing
            AnimationEasingCurve.EASE_OUT_CUBIC -> CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)
            AnimationEasingCurve.EASE_OUT_BACK -> CubicBezierEasing(0.34f, 1.45f, 0.64f, 1.0f)
            AnimationEasingCurve.SPRING -> CubicBezierEasing(0.20f, 1.15f, 0.35f, 1.0f)
            AnimationEasingCurve.BOUNCE -> CubicBezierEasing(0.28f, 1.55f, 0.52f, 1.0f)
            AnimationEasingCurve.OVERSHOOT -> CubicBezierEasing(0.25f, 1.40f, 0.50f, 1.0f)
        }
    }

    /**
     * Builds a float [AnimationSpec] (either Spring or Tween) from [ComponentAnimationSpec].
     */
    fun buildFloatAnimationSpec(
        spec: ComponentAnimationSpec,
        slowMoFactor: Float = 1.0f
    ): AnimationSpec<Float> {
        val duration = (spec.durationMs / slowMoFactor.coerceIn(0.1f, 2f)).toInt().coerceAtLeast(1)
        return when {
            spec.easingCurve == AnimationEasingCurve.SPRING ||
            spec.entranceType == EntranceAnimationType.CASCADE_ENTRANCE ||
            spec.entranceType == EntranceAnimationType.SPRING_SCALE ||
            spec.entranceType == EntranceAnimationType.BOUNCE_IN ||
            spec.entranceType == EntranceAnimationType.ELASTIC_ENTRANCE ||
            spec.entranceType == EntranceAnimationType.POP_IN ||
            spec.entranceType == EntranceAnimationType.OVERSHOOT_ENTRANCE ||
            spec.entranceType == EntranceAnimationType.HERO_EXPAND -> {
                val effectiveDamping = when (spec.entranceType) {
                    EntranceAnimationType.BOUNCE_IN -> (spec.dampingRatio * 0.78f).coerceIn(0.35f, 0.95f)
                    EntranceAnimationType.ELASTIC_ENTRANCE -> (spec.dampingRatio * 0.72f).coerceIn(0.32f, 0.95f)
                    EntranceAnimationType.OVERSHOOT_ENTRANCE -> (spec.dampingRatio * 0.82f).coerceIn(0.38f, 0.95f)
                    EntranceAnimationType.HERO_EXPAND -> (spec.dampingRatio * 0.88f).coerceIn(0.45f, 0.92f)
                    else -> spec.dampingRatio.coerceIn(0.3f, 1.0f)
                }
                val effectiveStiffness = (spec.stiffness * slowMoFactor.coerceIn(0.15f, 2f)).coerceIn(40f, 3000f)
                spring(
                    dampingRatio = effectiveDamping,
                    stiffness = effectiveStiffness
                )
            }
            else -> {
                tween(
                    durationMillis = duration,
                    easing = resolveEasing(spec.easingCurve)
                )
            }
        }
    }

    /**
     * Builds a screen navigation [ContentTransform] for [AnimatedContent] based on [GlobalAnimationMasterConfig].
     */
    fun buildScreenTransition(
        masterConfig: GlobalAnimationMasterConfig,
        isForward: Boolean = true,
        slowMoFactor: Float = 1.0f
    ): ContentTransform {
        if (!masterConfig.masterEnabled || masterConfig.screenTransitionType == ScreenTransitionAnimationType.NONE) {
            return EnterTransition.None togetherWith ExitTransition.None
        }

        val baseDuration = (masterConfig.screenTransitionDurationMs /
            (masterConfig.speedMultiplier.coerceIn(0.25f, 3f) * slowMoFactor.coerceIn(0.15f, 2f)))
            .toInt()
            .coerceIn(60, 1500)
        val exitDuration = (baseDuration * 0.8f).toInt().coerceAtLeast(50)
        val dirSign = if (isForward) 1 else -1

        return when (masterConfig.screenTransitionType) {
            ScreenTransitionAnimationType.NONE ->
                EnterTransition.None togetherWith ExitTransition.None

            ScreenTransitionAnimationType.CLASSIC_HORIZONTAL_SLIDE ->
                (slideInHorizontally(tween(baseDuration, easing = FastOutSlowInEasing)) { fullW -> dirSign * (fullW / 4) } +
                    fadeIn(tween(baseDuration))) togetherWith
                    (slideOutHorizontally(tween(exitDuration, easing = FastOutSlowInEasing)) { fullW -> -dirSign * (fullW / 6) } +
                        fadeOut(tween(exitDuration)))

            ScreenTransitionAnimationType.REVERSE_HORIZONTAL_SLIDE ->
                (slideInHorizontally(tween(baseDuration, easing = FastOutSlowInEasing)) { fullW -> -dirSign * (fullW / 4) } +
                    fadeIn(tween(baseDuration))) togetherWith
                    (slideOutHorizontally(tween(exitDuration, easing = FastOutSlowInEasing)) { fullW -> dirSign * (fullW / 6) } +
                        fadeOut(tween(exitDuration)))

            ScreenTransitionAnimationType.VERTICAL_SLIDE ->
                (slideInVertically(tween(baseDuration, easing = FastOutSlowInEasing)) { fullH -> dirSign * (fullH / 6) } +
                    fadeIn(tween(baseDuration))) togetherWith
                    (slideOutVertically(tween(exitDuration, easing = FastOutSlowInEasing)) { fullH -> -dirSign * (fullH / 8) } +
                        fadeOut(tween(exitDuration)))

            ScreenTransitionAnimationType.FADE_TRANSITION,
            ScreenTransitionAnimationType.CROSSFADE ->
                fadeIn(tween(baseDuration)) togetherWith fadeOut(tween(exitDuration))

            ScreenTransitionAnimationType.SCALE_AND_FADE_TRANSITION ->
                (scaleIn(tween(baseDuration, easing = FastOutSlowInEasing), initialScale = if (isForward) 0.94f else 1.04f) +
                    fadeIn(tween(baseDuration))) togetherWith
                    (scaleOut(tween(exitDuration), targetScale = if (isForward) 1.03f else 0.95f) +
                        fadeOut(tween(exitDuration)))

            ScreenTransitionAnimationType.SHARED_AXIS_X ->
                (slideInHorizontally(tween(baseDuration, easing = AppMotionSystem.EmphasizedDecelerate)) { fullW -> dirSign * (fullW * 0.14f).toInt() } +
                    fadeIn(tween(baseDuration)) +
                    scaleIn(tween(baseDuration), initialScale = 0.98f)) togetherWith
                    (slideOutHorizontally(tween(exitDuration, easing = AppMotionSystem.EmphasizedAccelerate)) { fullW -> -dirSign * (fullW * 0.09f).toInt() } +
                        fadeOut(tween(exitDuration)))

            ScreenTransitionAnimationType.SHARED_AXIS_Y ->
                (slideInVertically(tween(baseDuration, easing = AppMotionSystem.EmphasizedDecelerate)) { fullH -> dirSign * (fullH * 0.12f).toInt() } +
                    fadeIn(tween(baseDuration))) togetherWith
                    (slideOutVertically(tween(exitDuration, easing = AppMotionSystem.EmphasizedAccelerate)) { fullH -> -dirSign * (fullH * 0.08f).toInt() } +
                        fadeOut(tween(exitDuration)))

            ScreenTransitionAnimationType.SHARED_AXIS_Z,
            ScreenTransitionAnimationType.DEPTH_TRANSITION ->
                (scaleIn(tween(baseDuration, easing = AppMotionSystem.EmphasizedDecelerate), initialScale = if (isForward) 0.90f else 1.08f) +
                    fadeIn(tween(baseDuration))) togetherWith
                    (scaleOut(tween(exitDuration, easing = AppMotionSystem.EmphasizedAccelerate), targetScale = if (isForward) 1.06f else 0.92f) +
                        fadeOut(tween(exitDuration)))

            ScreenTransitionAnimationType.CARD_TO_SCREEN_EXPANSION ->
                (expandVertically(tween(baseDuration, easing = AppMotionSystem.CascadeDropEasing), expandFrom = Alignment.Top) +
                    scaleIn(tween(baseDuration), initialScale = 0.94f) +
                    fadeIn(tween(baseDuration))) togetherWith
                    (shrinkVertically(tween(exitDuration), shrinkTowards = Alignment.Top) +
                        fadeOut(tween(exitDuration)))

            ScreenTransitionAnimationType.ACCORDION_DRILL_DOWN ->
                (slideInVertically(tween(baseDuration, easing = AppMotionSystem.CascadeDropEasing)) { fullH -> -fullH / 10 } +
                    fadeIn(tween(baseDuration))) togetherWith
                    (slideOutVertically(tween(exitDuration)) { fullH -> -fullH / 14 } +
                        fadeOut(tween(exitDuration)))

            ScreenTransitionAnimationType.FLIP_SCREEN_TRANSITION ->
                (scaleIn(tween(baseDuration, easing = FastOutSlowInEasing), initialScale = 0.88f) +
                    fadeIn(tween(baseDuration))) togetherWith
                    (scaleOut(tween(exitDuration), targetScale = 0.88f) +
                        fadeOut(tween(exitDuration)))

            ScreenTransitionAnimationType.ZOOM_THROUGH_TRANSITION ->
                (scaleIn(tween(baseDuration, easing = FastOutSlowInEasing), initialScale = 0.84f) +
                    fadeIn(tween(baseDuration))) togetherWith
                    (scaleOut(tween(exitDuration), targetScale = 1.12f) +
                        fadeOut(tween(exitDuration)))

            ScreenTransitionAnimationType.HERO_EXPAND ->
                (expandVertically(
                    tween(baseDuration, easing = AppMotionSystem.EmphasizedDecelerate),
                    expandFrom = Alignment.Top
                ) + scaleIn(
                    spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
                    initialScale = if (isForward) 0.91f else 1.05f,
                    transformOrigin = TransformOrigin(0.5f, 0.18f)
                ) + fadeIn(tween((baseDuration * 0.75f).toInt().coerceAtLeast(60)))) togetherWith
                    (scaleOut(
                        tween(exitDuration, easing = AppMotionSystem.EmphasizedAccelerate),
                        targetScale = if (isForward) 1.04f else 0.93f,
                        transformOrigin = TransformOrigin(0.5f, 0.18f)
                    ) + fadeOut(tween(exitDuration)))
        }
    }

    /**
     * Builds container EnterTransition for accordion/hierarchy expand (Subject -> Units -> Topics).
     */
    fun buildHierarchyContainerEnter(
        spec: ComponentAnimationSpec,
        slowMoFactor: Float = 1.0f
    ): EnterTransition {
        if (!spec.enabled || spec.entranceType == EntranceAnimationType.NONE || spec.hierarchyType == HierarchyDrillDownAnimationType.NONE) {
            return EnterTransition.None
        }
        val dur = (spec.durationMs / slowMoFactor.coerceIn(0.15f, 2f)).toInt().coerceAtLeast(60)
        val alphaDur = (dur * 0.72f).toInt().coerceAtLeast(50)
        return when (spec.hierarchyType) {
            HierarchyDrillDownAnimationType.STEPPED_SLIDE_REVEAL,
            HierarchyDrillDownAnimationType.DRAWER_STYLE_SECTION_OPEN ->
                expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(dur, easing = FastOutSlowInEasing)
                ) + slideInHorizontally(
                    initialOffsetX = { -it / 14 },
                    animationSpec = tween(dur, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(alphaDur))

            HierarchyDrillDownAnimationType.CARD_STACK_UNFOLD,
            HierarchyDrillDownAnimationType.FAN_OUT_REVEAL,
            HierarchyDrillDownAnimationType.RIBBON_UNFOLD,
            HierarchyDrillDownAnimationType.HERO_EXPAND ->
                expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = spring(
                        dampingRatio = spec.dampingRatio.coerceIn(0.4f, 0.95f),
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + scaleIn(
                    initialScale = spec.initialScale.coerceIn(0.85f, 0.99f),
                    transformOrigin = TransformOrigin(0.5f, 0f),
                    animationSpec = tween(dur, easing = AppMotionSystem.CascadeDropEasing)
                ) + fadeIn(tween(alphaDur))

            else ->
                expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(dur, easing = AppMotionSystem.CascadeDropEasing)
                ) + fadeIn(
                    animationSpec = tween(alphaDur, easing = FastOutSlowInEasing)
                )
        }
    }

    /**
     * Builds container ExitTransition for accordion/hierarchy collapse (Subject -> Units -> Topics).
     * Never staggered; smooth ~180ms upward collapse.
     */
    fun buildHierarchyContainerExit(
        spec: ComponentAnimationSpec,
        slowMoFactor: Float = 1.0f
    ): ExitTransition {
        if (!spec.enabled || spec.exitType == ExitAnimationType.NONE || spec.hierarchyType == HierarchyDrillDownAnimationType.NONE) {
            return ExitTransition.None
        }
        val dur = (spec.exitDurationMs / slowMoFactor.coerceIn(0.15f, 2f)).toInt().coerceAtLeast(50)
        val alphaDur = (dur * 0.75f).toInt().coerceAtLeast(40)
        return shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(dur, easing = FastOutSlowInEasing)
        ) + fadeOut(
            animationSpec = tween(alphaDur, easing = FastOutSlowInEasing)
        )
    }
}

/**
 * Universal Component Target Animation Modifier.
 *
 * Automatically resolves the active [ComponentAnimationSpec] from [AnimationConfigManager]
 * for [target] and applies:
 * 1. Entrance & Exit animation (including Cascade Drop-Down with spring dampingRatio=0.6f,
 *    stiffness=Spring.StiffnessMediumLow, -16.dp -> 0.dp, scale 0.94f -> 1f, 200ms alpha tween,
 *    70ms * index.coerceAtMost(8) stagger, and 180ms non-staggered -8.dp exit).
 * 2. Optional Visual Polish Effect (Shimmer Sweep, Border Sweep, Soft Glow, Glass Highlight)
 *    when enabled in Admin Studio.
 */
fun Modifier.premiumTargetAnimation(
    target: AnimatableTarget,
    index: Int = 0,
    visible: Boolean = true,
    screenKey: String? = null,
    triggerKey: Any = visible,
    overrideInitialOffsetY: Dp? = null,
    overrideInitialScale: Float? = null,
    overrideStaggerDelayMs: Int? = null,
    overrideDurationMs: Int? = null
): Modifier = composed {
    val replayCount by AnimationConfigManager.replayTriggerCounter
    val slowMo by AnimationConfigManager.sandboxSlowMoFactor
    val spec = AnimationConfigManager.resolveSpec(target = target, screenKey = screenKey)

    if (!spec.enabled || (spec.entranceType == EntranceAnimationType.NONE && spec.exitType == ExitAnimationType.NONE)) {
        return@composed this
    }

    val effectiveSpec = remember(
        spec,
        overrideInitialOffsetY,
        overrideInitialScale,
        overrideStaggerDelayMs,
        overrideDurationMs
    ) {
        spec.copy(
            translationDistanceDp = overrideInitialOffsetY?.value ?: spec.translationDistanceDp,
            initialScale = overrideInitialScale ?: spec.initialScale,
            staggerDelayMs = overrideStaggerDelayMs ?: spec.staggerDelayMs,
            durationMs = overrideDurationMs ?: spec.durationMs
        )
    }

    this
        .premiumEntranceAndExit(
            spec = effectiveSpec,
            index = index,
            visible = visible,
            triggerKey = Pair(triggerKey, replayCount),
            slowMoFactor = slowMo
        )
        .then(
            if (effectiveSpec.continuousEffectEnabled && effectiveSpec.polishEffectType != VisualPolishEffectType.NONE) {
                Modifier.premiumVisualPolish(effectiveSpec.polishEffectType)
            } else {
                Modifier
            }
        )
}

/**
 * Core Entrance + Exit Modifier supporting all 28 Entrance animations, 15 Exit animations,
 * and 13 Hierarchy Drill-Down modes using GPU-accelerated `graphicsLayer`.
 */
fun Modifier.premiumEntranceAndExit(
    spec: ComponentAnimationSpec,
    index: Int = 0,
    visible: Boolean = true,
    triggerKey: Any = visible,
    slowMoFactor: Float = 1.0f
): Modifier = composed {
    if (!spec.enabled || spec.entranceType == EntranceAnimationType.NONE) {
        return@composed this
    }

    val entryCacheKey = remember(spec.target, index, triggerKey) {
        "${spec.target.name}_${index}_${triggerKey.hashCode()}"
    }
    var isEntered by remember(entryCacheKey) {
        mutableStateOf(PremiumEntranceCache.hasEntered(entryCacheKey))
    }

    LaunchedEffect(entryCacheKey, visible, spec.staggerDelayMs, spec.startDelayMs, spec.maxStaggerItems) {
        if (!visible) {
            // Exit immediately without stagger delay
            isEntered = false
            return@LaunchedEffect
        }
        if (PremiumEntranceCache.hasEntered(entryCacheKey)) {
            isEntered = true
            return@LaunchedEffect
        }
        isEntered = false
        val cappedIndex = index.coerceIn(0, spec.maxStaggerItems.coerceAtLeast(1))
        val staggerMs = cappedIndex * spec.staggerDelayMs
        val totalDelay = ((spec.startDelayMs + staggerMs) / slowMoFactor.coerceIn(0.15f, 2f)).toLong()
        if (totalDelay > 0L) {
            delay(totalDelay)
        }
        PremiumEntranceCache.markEntered(entryCacheKey)
        isEntered = true
    }

    val exitDuration = (spec.exitDurationMs / slowMoFactor.coerceIn(0.15f, 2f)).toInt().coerceIn(50, 1000)
    val enterAlphaDuration = (200 / slowMoFactor.coerceIn(0.15f, 2f)).toInt().coerceIn(80, 1000)

    // 1. Main progress (0f -> 1f on enter with Spring/Tween, 1f -> 0f on exit with ~180ms Tween)
    val mainProgress by animateFloatAsState(
        targetValue = if (isEntered && visible) 1f else 0f,
        animationSpec = if (isEntered && visible) {
            PremiumAnimationEngine.buildFloatAnimationSpec(spec, slowMoFactor)
        } else {
            tween(durationMillis = exitDuration, easing = FastOutSlowInEasing)
        },
        label = "premiumMainProgress_${spec.target.name}_$index"
    )

    // 2. Alpha progress (short 200ms tween on enter rather than a spring; 180ms tween on exit)
    val alphaProgress by animateFloatAsState(
        targetValue = if (isEntered && visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isEntered && visible) enterAlphaDuration else exitDuration,
            easing = FastOutSlowInEasing
        ),
        label = "premiumAlphaProgress_${spec.target.name}_$index"
    )

    val isEnteringOrVisible = isEntered && visible
    val entranceType = spec.entranceType
    val hierarchyType = spec.hierarchyType

    this
        .graphicsLayer {
            if (isEnteringOrVisible && mainProgress >= 0.995f && alphaProgress >= 0.995f) {
                alpha = 1f
                translationX = 0f
                translationY = 0f
                scaleX = 1f
                scaleY = 1f
                rotationX = 0f
                rotationY = 0f
                rotationZ = 0f
                return@graphicsLayer
            }

            val enterDistPx = spec.translationDistanceDp.dp.toPx()
            val exitDistPx = spec.exitTranslationDistanceDp.dp.toPx()
            val activeDistPx = if (isEnteringOrVisible) enterDistPx else exitDistPx
            val inv = (1f - mainProgress)

            // Alpha (0f -> 1f on enter, 1f -> 0f on exit)
            alpha = (spec.initialAlpha + (1f - spec.initialAlpha) * alphaProgress).coerceIn(0f, 1f)

            // Default translations
            var tx = 0f
            var ty = 0f
            var sx = 1f
            var sy = 1f
            var rx = 0f
            var ry = 0f
            var rz = 0f

            // Direction vector
            val dirY = when (spec.direction) {
                AnimationDirection.TOP -> -1f    // Begins above (-16.dp) and drops down to 0.dp
                AnimationDirection.BOTTOM -> 1f  // Begins below (+16.dp) and rises up to 0.dp
                AnimationDirection.DIAGONAL -> -0.8f
                else -> 0f
            }
            val dirX = when (spec.direction) {
                AnimationDirection.LEFT -> -1f
                AnimationDirection.RIGHT -> 1f
                AnimationDirection.DIAGONAL -> -0.8f
                else -> 0f
            }

            when (entranceType) {
                EntranceAnimationType.NONE -> {}
                EntranceAnimationType.FADE_IN -> {
                    ty = if (!isEnteringOrVisible) -exitDistPx * inv else 0f
                }
                EntranceAnimationType.FADE_IN_SCALE,
                EntranceAnimationType.SPRING_SCALE,
                EntranceAnimationType.CASCADE_ENTRANCE,
                EntranceAnimationType.STAGGERED_ENTRANCE,
                EntranceAnimationType.SEQUENTIAL_REVEAL -> {
                    // Cascade Drop-Down & Spring Scale:
                    // Enter: Vertical translation -16.dp -> 0.dp, Scale 0.94f -> 1f
                    // Exit: Vertical translation -> -8.dp, Alpha -> 0f
                    ty = if (isEnteringOrVisible) {
                        val effectiveDirY = if (dirY == 0f && dirX == 0f) -1f else dirY
                        effectiveDirY * enterDistPx * inv
                    } else {
                        -exitDistPx * inv
                    }
                    tx = if (isEnteringOrVisible) dirX * enterDistPx * inv else 0f
                    val s = spec.initialScale + (1f - spec.initialScale) * mainProgress
                    sx = s
                    sy = s
                }
                EntranceAnimationType.ZOOM_IN,
                EntranceAnimationType.POP_IN,
                EntranceAnimationType.BOUNCE_IN,
                EntranceAnimationType.ELASTIC_ENTRANCE,
                EntranceAnimationType.OVERSHOOT_ENTRANCE -> {
                    val startScale = spec.initialScale.coerceAtMost(0.92f)
                    val s = startScale + (1f - startScale) * mainProgress
                    sx = s
                    sy = s
                    ty = (if (dirY != 0f) dirY else -1f) * activeDistPx * 0.65f * inv
                }
                EntranceAnimationType.ZOOM_OUT_TO_NORMAL,
                EntranceAnimationType.SHRINK_IN -> {
                    val startScale = 1.10f
                    val s = startScale - (startScale - 1f) * mainProgress
                    sx = s
                    sy = s
                }
                EntranceAnimationType.SLIDE_UP -> {
                    ty = activeDistPx * inv
                }
                EntranceAnimationType.SLIDE_DOWN -> {
                    ty = -activeDistPx * inv
                }
                EntranceAnimationType.SLIDE_LEFT -> {
                    tx = activeDistPx * 1.4f * inv
                }
                EntranceAnimationType.SLIDE_RIGHT -> {
                    tx = -activeDistPx * 1.4f * inv
                }
                EntranceAnimationType.DIAGONAL_SLIDE -> {
                    tx = -activeDistPx * inv
                    ty = -activeDistPx * inv
                    val s = spec.initialScale + (1f - spec.initialScale) * mainProgress
                    sx = s
                    sy = s
                }
                EntranceAnimationType.EXPAND_IN -> {
                    sx = 0.96f + 0.04f * mainProgress
                    sy = (0.82f + 0.18f * mainProgress).coerceAtLeast(0.01f)
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
                EntranceAnimationType.FLIP_IN,
                EntranceAnimationType.FLIP_3D_X -> {
                    rx = -spec.rotationDegrees.coerceAtLeast(18f) * inv
                    ty = -activeDistPx * 0.5f * inv
                    val s = spec.initialScale + (1f - spec.initialScale) * mainProgress
                    sx = s
                    sy = s
                    cameraDistance = spec.cameraDistance * density
                }
                EntranceAnimationType.FLIP_3D_Y -> {
                    ry = spec.rotationDegrees.coerceAtLeast(20f) * inv
                    val s = spec.initialScale + (1f - spec.initialScale) * mainProgress
                    sx = s
                    sy = s
                    cameraDistance = spec.cameraDistance * density
                }
                EntranceAnimationType.PERSPECTIVE_ZOOM,
                EntranceAnimationType.DEPTH_BASED_ENTRANCE -> {
                    rx = 10f * inv
                    ty = -activeDistPx * inv
                    val s = (spec.initialScale.coerceAtMost(0.92f)) + (1f - spec.initialScale.coerceAtMost(0.92f)) * mainProgress
                    sx = s
                    sy = s
                    cameraDistance = spec.cameraDistance * density
                }
                EntranceAnimationType.BLUR_TO_CLEAR,
                EntranceAnimationType.REVEAL_WITH_CLIP,
                EntranceAnimationType.CIRCULAR_REVEAL,
                EntranceAnimationType.DIRECTIONAL_WIPE -> {
                    ty = -activeDistPx * 0.5f * inv
                    val s = spec.initialScale + (1f - spec.initialScale) * mainProgress
                    sx = s
                    sy = s
                }
                EntranceAnimationType.HERO_EXPAND -> {
                    // Signature Premium Hero Expand:
                    // Smooth origin-anchored expansion with subtle 3D depth pitch and vertical lift
                    val startScale = spec.initialScale.coerceIn(0.86f, 0.96f)
                    val s = startScale + (1f - startScale) * mainProgress
                    sx = s
                    sy = (startScale * 0.94f + (1f - startScale * 0.94f) * mainProgress).coerceAtLeast(0.01f)
                    ty = -activeDistPx * 0.85f * inv
                    rx = 6f * inv
                    transformOrigin = TransformOrigin(0.5f, 0.15f)
                    cameraDistance = spec.cameraDistance * density
                }
            }

            // Additional Hierarchy Drill-Down flavoring when active
            when (hierarchyType) {
                HierarchyDrillDownAnimationType.STEPPED_SLIDE_REVEAL -> {
                    tx += -(12.dp.toPx()) * inv
                }
                HierarchyDrillDownAnimationType.TREE_BRANCH_REVEAL -> {
                    tx += -(16.dp.toPx()) * inv
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                HierarchyDrillDownAnimationType.CARD_STACK_UNFOLD -> {
                    rx += -12f * inv
                    cameraDistance = spec.cameraDistance * density
                }
                HierarchyDrillDownAnimationType.FAN_OUT_REVEAL -> {
                    rz = -3.5f * inv * (if (index % 2 == 0) 1f else -1f)
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
                HierarchyDrillDownAnimationType.RIBBON_UNFOLD -> {
                    rx += -16f * inv
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    cameraDistance = spec.cameraDistance * density
                }
                HierarchyDrillDownAnimationType.HERO_EXPAND -> {
                    rx += 5f * inv
                    transformOrigin = TransformOrigin(0.5f, 0.1f)
                    cameraDistance = spec.cameraDistance * density
                }
                else -> {}
            }

            translationX = tx
            translationY = ty
            scaleX = sx
            scaleY = sy
            rotationX = rx
            rotationY = ry
            rotationZ = rz
        }
        .then(
            if (entranceType == EntranceAnimationType.CIRCULAR_REVEAL && mainProgress < 0.99f) {
                Modifier.drawWithContent {
                    val maxRadius = hypot(size.width, size.height) * mainProgress.coerceIn(0.01f, 1f)
                    val path = Path().apply {
                        addOval(
                            Rect(
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = maxRadius
                            )
                        )
                    }
                    clipPath(path) {
                        this@drawWithContent.drawContent()
                    }
                }
            } else if (
                (entranceType == EntranceAnimationType.DIRECTIONAL_WIPE ||
                    entranceType == EntranceAnimationType.REVEAL_WITH_CLIP) &&
                mainProgress < 0.99f
            ) {
                Modifier.drawWithContent {
                    val p = mainProgress.coerceIn(0.01f, 1f)
                    val clipRect = when (spec.direction) {
                        AnimationDirection.LEFT -> Rect(0f, 0f, size.width * p, size.height)
                        AnimationDirection.RIGHT -> Rect(size.width * (1f - p), 0f, size.width, size.height)
                        AnimationDirection.BOTTOM -> Rect(0f, size.height * (1f - p), size.width, size.height)
                        else -> Rect(0f, 0f, size.width, size.height * p)
                    }
                    val path = Path().apply { addRect(clipRect) }
                    clipPath(path) {
                        this@drawWithContent.drawContent()
                    }
                }
            } else {
                Modifier
            }
        )
}

/**
 * Interactive Card Press / Flip / Tilt / Highlight Modifier.
 * Supports all 22 Card Interaction animations from Section 2C.
 */
fun Modifier.premiumCardInteraction(
    target: AnimatableTarget = AnimatableTarget.SUBJECT_CARDS,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val spec = AnimationConfigManager.resolveSpec(target)
    val currentInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by currentInteractionSource.collectIsPressedAsState()

    if (!spec.enabled || spec.interactionType == CardInteractionAnimationType.NONE) {
        return@composed if (onClick != null) {
            this.clickable(
                interactionSource = currentInteractionSource,
                indication = null,
                onClick = onClick
            )
        } else {
            this
        }
    }

    val interactionType = spec.interactionType
    var flipToggled by remember { mutableStateOf(false) }

    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = spec.dampingRatio.coerceIn(0.4f, 0.95f),
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardInteractionPress_${target.name}"
    )

    val flipRotation by animateFloatAsState(
        targetValue = if (flipToggled) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardFlipRotation_${target.name}"
    )

    this
        .graphicsLayer {
            val targetPressScale = spec.pressScale.coerceIn(0.88f, 1.06f)
            val currentScale = 1f - (1f - targetPressScale) * pressProgress
            cameraDistance = spec.cameraDistance * density

            when (interactionType) {
                CardInteractionAnimationType.PRESS_SCALE,
                CardInteractionAnimationType.RELEASE_SPRING,
                CardInteractionAnimationType.TAP_RIPPLE,
                CardInteractionAnimationType.SEQUENTIAL_CARD_REVEAL,
                CardInteractionAnimationType.CARD_STACK_REVEAL,
                CardInteractionAnimationType.EXPAND_AND_COLLAPSE,
                CardInteractionAnimationType.SHARED_ELEMENT_LIKE -> {
                    scaleX = currentScale
                    scaleY = currentScale
                    alpha = 1f - (0.05f * pressProgress)
                }
                CardInteractionAnimationType.LIFT_ON_PRESS,
                CardInteractionAnimationType.ELEVATION_CHANGE,
                CardInteractionAnimationType.HERO_EXPAND -> {
                    val liftScale = if (interactionType == CardInteractionAnimationType.HERO_EXPAND) {
                        1f + 0.024f * pressProgress
                    } else {
                        1f + 0.018f * pressProgress
                    }
                    scaleX = liftScale
                    scaleY = liftScale
                    translationY = -(4.dp.toPx()) * pressProgress
                }
                CardInteractionAnimationType.GENTLE_TILT,
                CardInteractionAnimationType.PERSPECTIVE_TILT -> {
                    scaleX = currentScale
                    scaleY = currentScale
                    rotationX = 5.5f * pressProgress
                    rotationY = -4.0f * pressProgress
                }
                CardInteractionAnimationType.Y_AXIS_ROTATION,
                CardInteractionAnimationType.HORIZONTAL_CARD_FLIP,
                CardInteractionAnimationType.CARD_FLIP_3D,
                CardInteractionAnimationType.FLIP_WITH_BACK_FACE -> {
                    scaleX = currentScale
                    scaleY = currentScale
                    rotationY = (10f * pressProgress) + (if (flipRotation <= 90f) flipRotation else 180f - flipRotation)
                }
                CardInteractionAnimationType.X_AXIS_ROTATION,
                CardInteractionAnimationType.VERTICAL_CARD_FLIP -> {
                    scaleX = currentScale
                    scaleY = currentScale
                    rotationX = (10f * pressProgress)
                }
                CardInteractionAnimationType.SWIPE_TO_REVEAL,
                CardInteractionAnimationType.SWIPE_TO_DISMISS,
                CardInteractionAnimationType.SUBTLE_PARALLAX -> {
                    scaleX = currentScale
                    scaleY = currentScale
                    translationX = (4.dp.toPx()) * pressProgress
                }
                CardInteractionAnimationType.BORDER_HIGHLIGHT,
                CardInteractionAnimationType.GLOW_PULSE -> {
                    scaleX = currentScale
                    scaleY = currentScale
                }
                CardInteractionAnimationType.NONE -> {}
            }
        }
        .then(
            if ((interactionType == CardInteractionAnimationType.BORDER_HIGHLIGHT ||
                    interactionType == CardInteractionAnimationType.GLOW_PULSE) &&
                pressProgress > 0.02f
            ) {
                Modifier.drawWithContent {
                    drawContent()
                    drawRect(
                        color = Color(0xFFFDE68A).copy(alpha = 0.28f * pressProgress),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            } else {
                Modifier
            }
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = currentInteractionSource,
                    indication = null
                ) {
                    if (interactionType == CardInteractionAnimationType.CARD_FLIP_3D ||
                        interactionType == CardInteractionAnimationType.FLIP_WITH_BACK_FACE
                    ) {
                        flipToggled = !flipToggled
                    }
                    onClick()
                }
            } else {
                Modifier
            }
        )
}

/**
 * Optional Visual Polish Effect Modifier (Section 2J).
 * Only runs when explicitly enabled for a target or in the Admin Preview Sandbox.
 */
fun Modifier.premiumVisualPolish(
    effectType: VisualPolishEffectType
): Modifier = composed {
    if (effectType == VisualPolishEffectType.NONE) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "visualPolish_${effectType.name}")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "visualPolishPhase"
    )

    this.drawWithContent {
        drawContent()
        when (effectType) {
            VisualPolishEffectType.SUBTLE_SHIMMER_SWEEP,
            VisualPolishEffectType.GLASS_CARD_HIGHLIGHT -> {
                val startX = (phase * 1.6f - 0.3f) * size.width
                val brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    start = Offset(startX, 0f),
                    end = Offset(startX + size.width * 0.35f, size.height)
                )
                drawRect(brush = brush)
            }
            VisualPolishEffectType.SOFT_GLOW_HIGHLIGHT,
            VisualPolishEffectType.SPOTLIGHT_REVEAL -> {
                val pulseAlpha = (0.06f + 0.10f * sin(phase * 2f * PI.toFloat())).coerceIn(0f, 0.22f)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFDE68A).copy(alpha = pulseAlpha),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = size.maxDimension * 0.7f
                    )
                )
            }
            VisualPolishEffectType.BORDER_SWEEP,
            VisualPolishEffectType.GRADIENT_SHIFT,
            VisualPolishEffectType.CARD_DEPTH_SHADOW_TRANSITION -> {
                val borderAlpha = (0.20f + 0.25f * sin(phase * 2f * PI.toFloat())).coerceIn(0.08f, 0.55f)
                drawRect(
                    color = Color(0xFFFBBF24).copy(alpha = borderAlpha),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            VisualPolishEffectType.NONE -> {}
        }
    }
}

/**
 * MCQ / Quiz Answer Feedback Modifier (Section 2H):
 * - Correct answer pulse (scale pop + subtle glow)
 * - Wrong answer horizontal shake
 */
fun Modifier.premiumAnswerFeedback(
    isSelected: Boolean,
    isCorrect: Boolean?,
    triggerKey: Any = isSelected
): Modifier = composed {
    val spec = AnimationConfigManager.resolveSpec(AnimatableTarget.ANSWER_FEEDBACK)
    if (!spec.enabled || !isSelected || isCorrect == null) return@composed this

    val animProgress = remember(triggerKey, isCorrect) { Animatable(0f) }
    LaunchedEffect(triggerKey, isCorrect) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = spec.durationMs.coerceIn(180, 600), easing = FastOutSlowInEasing)
        )
    }

    this.graphicsLayer {
        // Maintain exactly the same scale and position without zooming or shaking
        scaleX = 1f
        scaleY = 1f
        translationX = 0f
        translationY = 0f
    }
}

/**
 * Reusable Animated Count-Up Number Composable for Statistics Badges, Score Cards, and Live Metrics.
 */
@Composable
fun rememberAnimatedCountUp(
    targetValue: Int,
    target: AnimatableTarget = AnimatableTarget.STATISTICS_BADGES_COUNTERS,
    durationMs: Int = 550
): Int {
    val spec = AnimationConfigManager.resolveSpec(target)
    if (!spec.enabled) return targetValue

    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(
            durationMillis = durationMs.coerceIn(120, 1500),
            easing = FastOutSlowInEasing
        ),
        label = "animatedCountUp_${target.name}"
    )
    return animatedValue
}

/**
 * Signature Premium Hero Expand Modifier:
 * Applies smooth origin-anchored hero expansion and elevation depth when a Hero Card,
 * Subject Header, Topic Banner, or Expandable Section transitions between collapsed and expanded states
 * or enters the screen. Respects Admin Panel configuration and Reduced Motion settings.
 */
fun Modifier.premiumHeroExpand(
    target: AnimatableTarget = AnimatableTarget.SUBJECT_CARDS,
    isExpanded: Boolean = true,
    screenKey: String? = null,
    index: Int = 0
): Modifier = composed {
    val spec = AnimationConfigManager.resolveSpec(target, screenKey)
    val slowMo by AnimationConfigManager.sandboxSlowMoFactor
    if (!spec.enabled) return@composed this

    val expandProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = spec.dampingRatio.coerceIn(0.45f, 0.92f),
            stiffness = (spec.stiffness * slowMo.coerceIn(0.15f, 2f)).coerceIn(80f, 1500f)
        ),
        label = "premiumHeroExpand_${target.name}_$index"
    )

    this
        .premiumTargetAnimation(
            target = target,
            index = index,
            visible = true,
            screenKey = screenKey
        )
        .graphicsLayer {
            // Subtle transient scale pulse only while actively transitioning (0 < expandProgress < 1),
            // settling back to exact 1.0f so card borders and Telugu text never clip or blur.
            val transitionPulse = 4f * expandProgress * (1f - expandProgress)
            val heroScaleBoost = if (spec.entranceType == EntranceAnimationType.HERO_EXPAND ||
                spec.interactionType == CardInteractionAnimationType.HERO_EXPAND
            ) {
                0.008f * transitionPulse
            } else {
                0.003f * transitionPulse
            }
            scaleX = 1f + heroScaleBoost
            scaleY = 1f + heroScaleBoost
        }
}

private object PremiumEntranceCache {
    private const val MAX_ENTRIES = 512
    private val enteredKeys = LinkedHashSet<String>()

    @Synchronized
    fun hasEntered(key: String): Boolean = enteredKeys.contains(key)

    @Synchronized
    fun markEntered(key: String) {
        if (enteredKeys.size >= MAX_ENTRIES) {
            val first = enteredKeys.iterator()
            if (first.hasNext()) {
                first.next()
                first.remove()
            }
        }
        enteredKeys.add(key)
    }
}

/**
 * Reusable Container for Hero Expand / Accordion Section Reveal.
 * Connects directly to [AnimationConfigManager] for the specified [target] and [screenKey].
 */
@Composable
fun PremiumHeroExpandContainer(
    visible: Boolean,
    target: AnimatableTarget = AnimatableTarget.ACCORDION_EXPAND_COLLAPSE,
    screenKey: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val spec = AnimationConfigManager.resolveSpec(target, screenKey)
    val slowMo by AnimationConfigManager.sandboxSlowMoFactor
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = PremiumAnimationEngine.buildHierarchyContainerEnter(spec, slowMo),
        exit = PremiumAnimationEngine.buildHierarchyContainerExit(spec, slowMo),
        content = content
    )
}

