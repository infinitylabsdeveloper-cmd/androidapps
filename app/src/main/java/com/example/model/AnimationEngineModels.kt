package com.example.model

/**
 * All 24 individually controllable UI component targets across the application.
 */
enum class AnimatableTarget(
    val displayName: String,
    val category: String,
    val description: String
) {
    APP_LAUNCH_HOME_ENTRANCE(
        displayName = "App Launch / Home Entrance",
        category = "Home & Navigation",
        description = "Initial reveal of Home Screen sections on launch"
    ),
    TOP_APP_BAR_HEADER(
        displayName = "Top App Bar / Header",
        category = "Home & Navigation",
        description = "Header bar entrance and action icon micro-interactions"
    ),
    CATEGORY_CARDS(
        displayName = "Category Cards (Prelims, Mains, General)",
        category = "Home & Navigation",
        description = "Optional motion wrapper for Prelims, Mains, and General filter cards (Off by default to preserve exact static appearance)"
    ),
    LIVE_CONTENT_DASHBOARD(
        displayName = "Live Content Dashboard",
        category = "Home & Navigation",
        description = "Live Bank indicator pulse and metric counter cards"
    ),
    SUBJECT_CARDS(
        displayName = "Subject Cards",
        category = "Syllabus Hierarchy",
        description = "Home & Syllabus Explorer Subject cards entrance, press, and accordion header"
    ),
    UNIT_CARDS(
        displayName = "Unit Cards",
        category = "Syllabus Hierarchy",
        description = "Subject -> Units cascade drop-down and unit card interactions"
    ),
    TOPIC_CARDS(
        displayName = "Topic Cards",
        category = "Syllabus Hierarchy",
        description = "Unit -> Topics cascade drop-down and topic card interactions"
    ),
    SETS_CARDS(
        displayName = "Practice Sets Cards",
        category = "Syllabus Hierarchy",
        description = "Topic Sets 2-column grid entrance and press animations"
    ),
    SUBTOPIC_CARDS(
        displayName = "Subtopic Cards",
        category = "Syllabus Hierarchy",
        description = "Subtopic cards stagger entrance and press animations"
    ),
    ACCORDION_EXPAND_COLLAPSE(
        displayName = "Accordion Expand / Collapse",
        category = "Syllabus Hierarchy",
        description = "Container expansion, collapse, chevron rotation, and single vs multi-open mode"
    ),
    SCREEN_NAVIGATION(
        displayName = "Screen-to-Screen Navigation",
        category = "Home & Navigation",
        description = "Forward and backward screen transitions across the app"
    ),
    MODAL_DIALOGS(
        displayName = "Modal Dialogs",
        category = "Overlays & Menus",
        description = "Popup dialog entrance and dismiss transitions"
    ),
    BOTTOM_SHEETS(
        displayName = "Bottom Sheets",
        category = "Overlays & Menus",
        description = "Modal bottom sheet slide-up and dismiss transitions"
    ),
    NAVIGATION_DRAWER(
        displayName = "Navigation Drawer Items",
        category = "Overlays & Menus",
        description = "Left navigation drawer menu item reveal and press effects"
    ),
    FILTER_CHIPS_TABS(
        displayName = "Filter Chips & Tabs",
        category = "Micro-Interactions",
        description = "Tab bar indicator glide and chip selection bounce"
    ),
    STATISTICS_BADGES_COUNTERS(
        displayName = "Statistics Badges & Counters",
        category = "Micro-Interactions",
        description = "Units, Topics, and MCQs badges pulse and number count-up"
    ),
    MCQ_QUESTION_CARDS(
        displayName = "MCQ Question Cards",
        category = "Quiz & Exam Arena",
        description = "Question card transitions when switching between questions"
    ),
    MCQ_OPTION_CARDS(
        displayName = "MCQ Option Cards",
        category = "Quiz & Exam Arena",
        description = "Option cards staggered reveal and selection highlight"
    ),
    ANSWER_FEEDBACK(
        displayName = "Correct / Wrong Answer Feedback",
        category = "Quiz & Exam Arena",
        description = "Correct answer pulse/celebration and wrong answer horizontal shake"
    ),
    EXPLANATION_PANEL(
        displayName = "Explanation Panel",
        category = "Quiz & Exam Arena",
        description = "Solution and explanation drawer smooth expansion"
    ),
    RESULT_SUMMARY_SCORE(
        displayName = "Result Summary & Score Cards",
        category = "Quiz & Exam Arena",
        description = "Score counter roll-up, accuracy ring sweep, and rank slide"
    ),
    BUTTONS_CTAS(
        displayName = "Buttons & Floating Actions",
        category = "Micro-Interactions",
        description = "Primary CTA buttons, FABs, and icon button press bounce"
    ),
    LOADING_STATES(
        displayName = "Loading & Skeleton States",
        category = "System States",
        description = "Shimmer skeleton and pulse placeholder effects during sync"
    ),
    EMPTY_ERROR_STATES(
        displayName = "Empty & Error States",
        category = "System States",
        description = "Empty-state gentle float and error-state attention shake"
    )
}

/**
 * A. Entrance Animations (28 varieties)
 */
enum class EntranceAnimationType(val displayName: String) {
    NONE("None (Instant)"),
    FADE_IN("Fade In"),
    FADE_IN_SCALE("Fade In + Scale"),
    ZOOM_IN("Zoom In"),
    ZOOM_OUT_TO_NORMAL("Zoom Out to Normal"),
    SLIDE_UP("Slide Up"),
    SLIDE_DOWN("Slide Down"),
    SLIDE_LEFT("Slide Left"),
    SLIDE_RIGHT("Slide Right"),
    DIAGONAL_SLIDE("Diagonal Slide"),
    EXPAND_IN("Expand In"),
    SHRINK_IN("Shrink In"),
    POP_IN("Pop In"),
    BOUNCE_IN("Bounce In"),
    SPRING_SCALE("Spring Scale"),
    ELASTIC_ENTRANCE("Elastic Entrance"),
    OVERSHOOT_ENTRANCE("Overshoot Entrance"),
    STAGGERED_ENTRANCE("Staggered Entrance"),
    CASCADE_ENTRANCE("Cascade Entrance"),
    SEQUENTIAL_REVEAL("Sequential Reveal"),
    BLUR_TO_CLEAR("Blur-to-Clear"),
    REVEAL_WITH_CLIP("Reveal with Clip"),
    CIRCULAR_REVEAL("Circular Reveal"),
    DIRECTIONAL_WIPE("Directional Wipe"),
    FLIP_IN("Flip In"),
    FLIP_3D_Y("3D Y-Axis Flip"),
    FLIP_3D_X("3D X-Axis Flip"),
    PERSPECTIVE_ZOOM("Perspective Zoom"),
    DEPTH_BASED_ENTRANCE("Depth-Based Entrance"),
    HERO_EXPAND("Premium Hero Expand")
}

/**
 * B. Exit Animations (15 varieties)
 */
enum class ExitAnimationType(val displayName: String) {
    NONE("None (Instant)"),
    FADE_OUT("Fade Out"),
    SHRINK_OUT("Shrink Out"),
    ZOOM_OUT("Zoom Out"),
    SLIDE_UP_OUT("Slide Up and Out"),
    SLIDE_DOWN_OUT("Slide Down and Out"),
    SLIDE_LEFT_OUT("Slide Left and Out"),
    SLIDE_RIGHT_OUT("Slide Right and Out"),
    EXPAND_AND_FADE("Expand and Fade"),
    COLLAPSE_AND_FADE("Collapse and Fade"),
    FLIP_OUT("Flip Out"),
    ROTATE_3D_OUT("3D Rotate Out"),
    DIRECTIONAL_WIPE_OUT("Directional Wipe Out"),
    SCALE_AND_FADE("Scale and Fade"),
    SMOOTH_DISMISS("Smooth Dismiss"),
    DIALOG_SHEET_DISMISS("Dialog & Sheet Dismiss")
}

/**
 * C. Card Interaction Animations (22 varieties)
 */
enum class CardInteractionAnimationType(val displayName: String) {
    NONE("None"),
    PRESS_SCALE("Press Scale"),
    LIFT_ON_PRESS("Lift on Press"),
    RELEASE_SPRING("Release Spring"),
    TAP_RIPPLE("Tap Ripple"),
    GENTLE_TILT("Gentle Tilt"),
    PERSPECTIVE_TILT("Perspective Tilt"),
    CARD_FLIP_3D("3D Card Flip"),
    HORIZONTAL_CARD_FLIP("Horizontal Card Flip"),
    VERTICAL_CARD_FLIP("Vertical Card Flip"),
    Y_AXIS_ROTATION("Y-Axis Rotation"),
    X_AXIS_ROTATION("X-Axis Rotation"),
    BORDER_HIGHLIGHT("Border Highlight"),
    GLOW_PULSE("Glow Pulse"),
    ELEVATION_CHANGE("Elevation Change"),
    SUBTLE_PARALLAX("Subtle Parallax"),
    FLIP_WITH_BACK_FACE("Flip with Back-Face Content"),
    SEQUENTIAL_CARD_REVEAL("Sequential Card Reveal"),
    CARD_STACK_REVEAL("Card Stack Reveal"),
    SWIPE_TO_REVEAL("Swipe-to-Reveal"),
    SWIPE_TO_DISMISS("Swipe-to-Dismiss"),
    EXPAND_AND_COLLAPSE("Expand and Collapse"),
    SHARED_ELEMENT_LIKE("Shared-Element-Like Transition"),
    HERO_EXPAND("Premium Hero Expand")
}

/**
 * D. Screen Navigation Transitions (14 varieties)
 */
enum class ScreenTransitionAnimationType(val displayName: String) {
    NONE("None (Instant)"),
    CLASSIC_HORIZONTAL_SLIDE("Classic Horizontal Slide"),
    REVERSE_HORIZONTAL_SLIDE("Reverse Horizontal Slide"),
    VERTICAL_SLIDE("Vertical Slide"),
    FADE_TRANSITION("Fade Transition"),
    CROSSFADE("Crossfade"),
    SCALE_AND_FADE_TRANSITION("Scale + Fade Transition"),
    SHARED_AXIS_X("Shared Axis X"),
    SHARED_AXIS_Y("Shared Axis Y"),
    SHARED_AXIS_Z("Shared Axis Z"),
    DEPTH_TRANSITION("Depth Transition"),
    CARD_TO_SCREEN_EXPANSION("Card-to-Screen Expansion"),
    ACCORDION_DRILL_DOWN("Accordion Drill-Down"),
    FLIP_SCREEN_TRANSITION("Flip Screen Transition"),
    ZOOM_THROUGH_TRANSITION("Zoom-Through Transition"),
    HERO_EXPAND("Premium Hero Expand")
}

/**
 * E. Hierarchy and Drill-Down Animations (13 varieties)
 */
enum class HierarchyDrillDownAnimationType(val displayName: String) {
    NONE("None (Instant)"),
    ACCORDION_EXPAND_COLLAPSE("Accordion Expand / Collapse"),
    CASCADE_DROP_DOWN("Cascade Drop-Down"),
    STEPPED_SLIDE_REVEAL("Stepped Slide Reveal"),
    TREE_BRANCH_REVEAL("Tree Branch Reveal"),
    DRAWER_STYLE_SECTION_OPEN("Drawer-Style Section Open"),
    CARD_STACK_UNFOLD("Card Stack Unfold"),
    FAN_OUT_REVEAL("Fan-Out Reveal"),
    GRID_STAGGER_REVEAL("Grid Stagger Reveal"),
    RIBBON_UNFOLD("Ribbon Unfold"),
    SMOOTH_HEIGHT_EXPANSION("Smooth Height Expansion"),
    PARENT_LOCK_CHILD_REVEAL("Parent Lock + Child Reveal"),
    SINGLE_OPEN_ACCORDION("Single-Open Accordion Mode"),
    MULTI_OPEN_ACCORDION("Multi-Open Accordion Mode"),
    HERO_EXPAND("Premium Hero Expand")
}

/**
 * F. List, Grid, and Scroll Animations (10 varieties)
 */
enum class ListScrollAnimationType(val displayName: String) {
    NONE("None"),
    LAZY_COLUMN_ITEM_ENTRANCE("LazyColumn Item Entrance"),
    LAZY_VERTICAL_GRID_STAGGER("LazyVerticalGrid Stagger"),
    SCROLL_TRIGGERED_REVEAL("Scroll-Triggered Reveal"),
    DIRECTIONAL_SCROLL_REVEAL("Directional Scroll Reveal"),
    STICKY_HEADER_TRANSITION("Sticky Header Transition"),
    COLLAPSING_HEADER_EFFECT("Collapsing Header Effect"),
    PARALLAX_HEADER_BANNER("Parallax Header / Banner"),
    FLOATING_FILTER_BAR("Floating Filter Bar Transition"),
    REORDER_PLACEMENT("Reorder Placement Animation"),
    ITEM_INSERT_REMOVE("Item Insert / Remove Animation")
}

/**
 * G. Micro-Interactions (13 varieties)
 */
enum class MicroInteractionAnimationType(val displayName: String) {
    NONE("None"),
    BUTTON_PRESS_BOUNCE("Button Press Bounce"),
    ICON_ROTATION("Icon Rotation"),
    CHEVRON_ROTATION("Chevron Rotation"),
    BOOKMARK_POP("Bookmark Pop"),
    FAVORITE_HEART_BURST("Favorite / Heart Burst"),
    BADGE_PULSE("Badge Pulse"),
    COUNT_UP_NUMBER("Count-Up Number Animation"),
    PROGRESS_BAR_FILL("Progress Bar Fill Animation"),
    CIRCULAR_PROGRESS_SWEEP("Circular Progress Sweep"),
    CHIP_SELECTION_SLIDE("Chip Selection Slide"),
    TAB_INDICATOR_GLIDE("Tab Indicator Glide"),
    TOGGLE_SWITCH_SPRING("Toggle Switch Spring"),
    LOCK_UNLOCK_SHAKE("Lock / Unlock Shake or Pop")
}

/**
 * H. Quiz / MCQ / Exam-Specific Animations (12 varieties)
 */
enum class QuizExamAnimationType(val displayName: String) {
    NONE("None"),
    QUESTION_CARD_TRANSITION("Question Card Transition"),
    OPTION_STAGGER_REVEAL("Option Stagger Reveal"),
    OPTION_SELECT_HIGHLIGHT("Option Select Highlight"),
    CORRECT_ANSWER_PULSE("Correct Answer Pulse"),
    WRONG_ANSWER_SHAKE("Wrong Answer Shake"),
    EXPLANATION_DRAWER_EXPAND("Explanation Drawer Expand"),
    SCORE_COUNTER_ROLL_UP("Score Counter Roll-Up"),
    ACCURACY_RING_ANIMATION("Accuracy Ring Animation"),
    STREAK_FLAME_PULSE("Streak / Flame Pulse"),
    RANK_IMPROVEMENT_SLIDE("Rank Improvement Slide"),
    TIMER_URGENCY_PULSE("Timer Urgency Pulse"),
    CELEBRATION_CONFETTI_BURST("Celebration / Confetti Burst")
}

/**
 * I. Loading, Skeleton, and State Animations (7 varieties)
 */
enum class LoadingStateAnimationType(val displayName: String) {
    NONE("None"),
    SHIMMER_SKELETON("Shimmer Skeleton Loading"),
    PULSE_PLACEHOLDER("Pulse Placeholder"),
    EMPTY_STATE_GENTLE_FLOAT("Empty-State Gentle Float"),
    ERROR_STATE_SHAKE("Error-State Shake"),
    OFFLINE_BANNER_SLIDE("Offline Banner Slide"),
    SYNC_COMPLETE_CHECKMARK("Sync Complete Checkmark"),
    LIVE_CONTENT_INDICATOR_PULSE("Live Content Indicator Pulse")
}

/**
 * J. Premium Visual Polish Effects (7 varieties)
 */
enum class VisualPolishEffectType(val displayName: String) {
    NONE("None"),
    SUBTLE_SHIMMER_SWEEP("Subtle Shimmer Sweep"),
    SOFT_GLOW_HIGHLIGHT("Soft Glow Highlight"),
    BORDER_SWEEP("Border Sweep"),
    GRADIENT_SHIFT("Gradient Shift"),
    SPOTLIGHT_REVEAL("Spotlight Reveal"),
    GLASS_CARD_HIGHLIGHT("Glass Card Highlight"),
    CARD_DEPTH_SHADOW_TRANSITION("Card Depth Shadow Transition")
}

/**
 * Direction options for directional entrance/exit/wipe animations.
 */
enum class AnimationDirection(val displayName: String) {
    TOP("Top (Downward Drop)"),
    BOTTOM("Bottom (Upward Rise)"),
    LEFT("Left to Right"),
    RIGHT("Right to Left"),
    CENTER("Center Radial"),
    DIAGONAL("Diagonal")
}

/**
 * Easing curve options for configurable motion feel.
 */
enum class AnimationEasingCurve(val displayName: String) {
    STANDARD("Standard Material"),
    FAST_OUT_SLOW_IN("FastOutSlowIn"),
    LINEAR_OUT_SLOW_IN("LinearOutSlowIn"),
    EASE_OUT_CUBIC("EaseOutCubic"),
    EASE_OUT_BACK("EaseOutBack"),
    SPRING("Spring Physics"),
    BOUNCE("Bounce"),
    OVERSHOOT("Overshoot")
}

/**
 * Global intensity levels.
 */
enum class AnimationIntensityLevel(
    val displayName: String,
    val distanceMultiplier: Float,
    val scaleDeltaMultiplier: Float
) {
    SUBTLE("Subtle", 0.6f, 0.55f),
    BALANCED("Balanced", 1.0f, 1.0f),
    EXPRESSIVE("Expressive", 1.3f, 1.35f),
    ULTRA_PREMIUM("Ultra Premium", 1.5f, 1.55f),
    CUSTOM("Custom", 1.0f, 1.0f)
}

/**
 * 12 Ready-Made Premium Presets.
 */
enum class AnimationPresetId(
    val displayName: String,
    val badge: String,
    val description: String
) {
    DEFAULT_SMOOTH(
        displayName = "Default Smooth",
        badge = "Recommended",
        description = "Balanced spring cascade-drop for accordions with smooth 60fps transitions"
    ),
    ULTRA_PREMIUM(
        displayName = "Ultra Premium",
        badge = "Flagship",
        description = "Rich spring physics, depth-based entrances, lift-on-press, and subtle border polish"
    ),
    CASCADE_ACCORDION_PRO(
        displayName = "Cascade Accordion Pro",
        badge = "Syllabus Focus",
        description = "Tuned downward cascade drop for Subjects -> Units -> Topics -> Sets drill-down"
    ),
    CARD_3D_SHOWCASE(
        displayName = "3D Card Showcase",
        badge = "3D Depth",
        description = "Perspective 3D Y/X-axis flips, gentle card tilt on press, and camera depth"
    ),
    FAST_COMPETITIVE_EXAM(
        displayName = "Fast Competitive Exam Mode",
        badge = "High Speed",
        description = "Snappy 140ms transitions and zero distraction for rapid MCQ solving"
    ),
    MINIMAL_CLEAN(
        displayName = "Minimal Clean",
        badge = "Subtle",
        description = "Clean opacity fades and micro-scale presses with minimal translation"
    ),
    BOUNCY_MODERN(
        displayName = "Bouncy Modern",
        badge = "Energetic",
        description = "Expressive spring damping (0.52f) with pop-in cards and bouncy buttons"
    ),
    CINEMATIC_DEPTH(
        displayName = "Cinematic Depth",
        badge = "Immersive",
        description = "Shared-axis Z depth transitions, perspective zoom, and smooth parallax"
    ),
    HIGH_FPS_LIGHTWEIGHT(
        displayName = "High FPS Lightweight",
        badge = "60/120Hz",
        description = "Ultra-light GPU graphicsLayer transforms optimized for budget devices"
    ),
    NO_ANIMATION_INSTANT(
        displayName = "No Animation / Instant",
        badge = "Zero Motion",
        description = "Immediate state changes with all decorative and transition animations disabled"
    ),
    ROYAL_GOLD_EDITION(
        displayName = "Royal Gold Edition",
        badge = "Luxury",
        description = "Graceful cascade reveals paired with soft glow and border highlight on press"
    ),
    PLAYFUL_SPRING_STUDIO(
        displayName = "Playful Spring Studio",
        badge = "Dynamic",
        description = "Elastic entrances, overshoot accordions, and responsive tactile feedback"
    ),
    HERO_EXPAND_PRO(
        displayName = "Premium Hero Expand",
        badge = "Hero Motion",
        description = "Signature Hero Expand container & card morphing with spring depth and staggered content reveal"
    )
}

/**
 * Per-component animation specification configurable by the Admin.
 */
data class ComponentAnimationSpec(
    val target: AnimatableTarget = AnimatableTarget.SUBJECT_CARDS,
    val enabled: Boolean = true,
    val entranceType: EntranceAnimationType = EntranceAnimationType.CASCADE_ENTRANCE,
    val exitType: ExitAnimationType = ExitAnimationType.SMOOTH_DISMISS,
    val interactionType: CardInteractionAnimationType = CardInteractionAnimationType.PRESS_SCALE,
    val hierarchyType: HierarchyDrillDownAnimationType = HierarchyDrillDownAnimationType.CASCADE_DROP_DOWN,
    val listScrollType: ListScrollAnimationType = ListScrollAnimationType.LAZY_COLUMN_ITEM_ENTRANCE,
    val microInteractionType: MicroInteractionAnimationType = MicroInteractionAnimationType.CHEVRON_ROTATION,
    val quizAnimationType: QuizExamAnimationType = QuizExamAnimationType.OPTION_STAGGER_REVEAL,
    val loadingAnimationType: LoadingStateAnimationType = LoadingStateAnimationType.SHIMMER_SKELETON,
    val polishEffectType: VisualPolishEffectType = VisualPolishEffectType.NONE,
    val direction: AnimationDirection = AnimationDirection.TOP,
    val durationMs: Int = 260,
    val exitDurationMs: Int = 180,
    val startDelayMs: Int = 0,
    val staggerDelayMs: Int = 70,
    val maxStaggerItems: Int = 8,
    val dampingRatio: Float = 0.6f,
    val stiffness: Float = 400f, // Spring.StiffnessMediumLow
    val initialScale: Float = 0.94f,
    val pressScale: Float = 0.97f,
    val translationDistanceDp: Float = 16f,
    val exitTranslationDistanceDp: Float = 8f,
    val rotationDegrees: Float = 12f,
    val cameraDistance: Float = 12f,
    val initialAlpha: Float = 0f,
    val easingCurve: AnimationEasingCurve = AnimationEasingCurve.SPRING,
    val singleOpenAccordion: Boolean = true,
    val continuousEffectEnabled: Boolean = false
)

/**
 * Global Master Controls for the Animation Engine.
 */
data class GlobalAnimationMasterConfig(
    val masterEnabled: Boolean = true,
    val reducedMotionMode: Boolean = false,
    val lowEndPerformanceMode: Boolean = false,
    val respectSystemAnimatorScale: Boolean = true,
    val speedMultiplier: Float = 1.0f, // 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x
    val intensityLevel: AnimationIntensityLevel = AnimationIntensityLevel.BALANCED,
    val activePresetId: AnimationPresetId = AnimationPresetId.CASCADE_ACCORDION_PRO,
    val screenTransitionType: ScreenTransitionAnimationType = ScreenTransitionAnimationType.HERO_EXPAND,
    val screenTransitionDurationMs: Int = 320
)

/**
 * Complete Animation Engine Configuration supporting Global, App-Specific,
 * Screen-Specific, and Component-Specific scopes plus versioning and rollback.
 */
data class AnimationEngineConfig(
    val masterConfig: GlobalAnimationMasterConfig = GlobalAnimationMasterConfig(),
    val componentSpecs: Map<String, ComponentAnimationSpec> = AnimationPresets.buildPresetSpecs(AnimationPresetId.CASCADE_ACCORDION_PRO),
    val screenOverrides: Map<String, Map<String, ComponentAnimationSpec>> = emptyMap(),
    val targetScope: String = "ALL", // "CURRENT", "SELECTED", "ALL"
    val selectedAppIds: List<String> = emptyList(),
    val versionNumber: Int = 1,
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "sobhadhanaraju0@gmail.com",
    val notes: String = "Default Cascade Accordion Pro configuration"
)

/**
 * Factory for the 12 Ready-Made Premium Presets and safe per-component defaults.
 */
object AnimationPresets {

    fun getDefaultSpecForTarget(target: AnimatableTarget): ComponentAnimationSpec {
        return when (target) {
            AnimatableTarget.CATEGORY_CARDS -> ComponentAnimationSpec(
                target = target,
                enabled = false, // CRITICAL: Prelims, Mains, General untouched by default
                entranceType = EntranceAnimationType.NONE,
                exitType = ExitAnimationType.NONE,
                interactionType = CardInteractionAnimationType.NONE,
                polishEffectType = VisualPolishEffectType.NONE
            )
            AnimatableTarget.SUBJECT_CARDS -> ComponentAnimationSpec(
                target = target,
                enabled = true,
                entranceType = EntranceAnimationType.STAGGERED_ENTRANCE,
                exitType = ExitAnimationType.SMOOTH_DISMISS,
                interactionType = CardInteractionAnimationType.PRESS_SCALE,
                hierarchyType = HierarchyDrillDownAnimationType.SINGLE_OPEN_ACCORDION,
                direction = AnimationDirection.BOTTOM,
                durationMs = 340,
                exitDurationMs = 180,
                staggerDelayMs = 45,
                maxStaggerItems = 8,
                dampingRatio = 0.65f,
                stiffness = 400f,
                initialScale = 0.96f,
                pressScale = 0.98f,
                translationDistanceDp = 18f,
                easingCurve = AnimationEasingCurve.FAST_OUT_SLOW_IN,
                singleOpenAccordion = true
            )
            AnimatableTarget.UNIT_CARDS -> ComponentAnimationSpec(
                target = target,
                enabled = true,
                entranceType = EntranceAnimationType.CASCADE_ENTRANCE,
                exitType = ExitAnimationType.SMOOTH_DISMISS,
                interactionType = CardInteractionAnimationType.PRESS_SCALE,
                hierarchyType = HierarchyDrillDownAnimationType.CASCADE_DROP_DOWN,
                direction = AnimationDirection.TOP,
                durationMs = 280,
                exitDurationMs = 180,
                staggerDelayMs = 70,
                maxStaggerItems = 8,
                dampingRatio = 0.6f,
                stiffness = 400f, // Spring.StiffnessMediumLow
                initialScale = 0.94f,
                pressScale = 0.97f,
                translationDistanceDp = 16f,
                exitTranslationDistanceDp = 8f,
                easingCurve = AnimationEasingCurve.SPRING,
                singleOpenAccordion = true
            )
            AnimatableTarget.TOPIC_CARDS -> ComponentAnimationSpec(
                target = target,
                enabled = true,
                entranceType = EntranceAnimationType.CASCADE_ENTRANCE,
                exitType = ExitAnimationType.SMOOTH_DISMISS,
                interactionType = CardInteractionAnimationType.PRESS_SCALE,
                hierarchyType = HierarchyDrillDownAnimationType.CASCADE_DROP_DOWN,
                direction = AnimationDirection.TOP,
                durationMs = 260,
                exitDurationMs = 180,
                staggerDelayMs = 70,
                maxStaggerItems = 8,
                dampingRatio = 0.6f,
                stiffness = 400f,
                initialScale = 0.94f,
                pressScale = 0.97f,
                translationDistanceDp = 16f,
                exitTranslationDistanceDp = 8f,
                easingCurve = AnimationEasingCurve.SPRING,
                singleOpenAccordion = true
            )
            AnimatableTarget.SETS_CARDS -> ComponentAnimationSpec(
                target = target,
                enabled = true,
                entranceType = EntranceAnimationType.CASCADE_ENTRANCE,
                exitType = ExitAnimationType.SCALE_AND_FADE,
                interactionType = CardInteractionAnimationType.PRESS_SCALE,
                hierarchyType = HierarchyDrillDownAnimationType.GRID_STAGGER_REVEAL,
                listScrollType = ListScrollAnimationType.LAZY_VERTICAL_GRID_STAGGER,
                direction = AnimationDirection.TOP,
                durationMs = 260,
                exitDurationMs = 160,
                staggerDelayMs = 55,
                maxStaggerItems = 8,
                dampingRatio = 0.62f,
                stiffness = 420f,
                initialScale = 0.94f,
                pressScale = 0.96f,
                translationDistanceDp = 16f,
                easingCurve = AnimationEasingCurve.SPRING
            )
            AnimatableTarget.SUBTOPIC_CARDS -> ComponentAnimationSpec(
                target = target,
                enabled = true,
                entranceType = EntranceAnimationType.CASCADE_ENTRANCE,
                exitType = ExitAnimationType.SMOOTH_DISMISS,
                interactionType = CardInteractionAnimationType.PRESS_SCALE,
                hierarchyType = HierarchyDrillDownAnimationType.CASCADE_DROP_DOWN,
                direction = AnimationDirection.TOP,
                durationMs = 260,
                exitDurationMs = 160,
                staggerDelayMs = 60,
                maxStaggerItems = 8,
                dampingRatio = 0.62f,
                stiffness = 420f,
                initialScale = 0.94f,
                pressScale = 0.97f,
                translationDistanceDp = 16f,
                easingCurve = AnimationEasingCurve.SPRING
            )
            AnimatableTarget.ACCORDION_EXPAND_COLLAPSE -> ComponentAnimationSpec(
                target = target,
                enabled = true,
                entranceType = EntranceAnimationType.EXPAND_IN,
                exitType = ExitAnimationType.COLLAPSE_AND_FADE,
                hierarchyType = HierarchyDrillDownAnimationType.CASCADE_DROP_DOWN,
                microInteractionType = MicroInteractionAnimationType.CHEVRON_ROTATION,
                direction = AnimationDirection.TOP,
                durationMs = 280,
                exitDurationMs = 180,
                staggerDelayMs = 70,
                maxStaggerItems = 8,
                dampingRatio = 0.6f,
                stiffness = 400f,
                initialScale = 0.94f,
                translationDistanceDp = 16f,
                exitTranslationDistanceDp = 8f,
                easingCurve = AnimationEasingCurve.SPRING,
                singleOpenAccordion = true
            )
            AnimatableTarget.SCREEN_NAVIGATION -> ComponentAnimationSpec(
                target = target,
                enabled = true,
                entranceType = EntranceAnimationType.FADE_IN_SCALE,
                exitType = ExitAnimationType.SCALE_AND_FADE,
                direction = AnimationDirection.RIGHT,
                durationMs = 280,
                exitDurationMs = 220,
                initialScale = 0.97f,
                translationDistanceDp = 32f,
                easingCurve = AnimationEasingCurve.FAST_OUT_SLOW_IN
            )
            AnimatableTarget.MCQ_QUESTION_CARDS -> ComponentAnimationSpec(
                target = target,
                enabled = true,
                entranceType = EntranceAnimationType.SLIDE_LEFT,
                exitType = ExitAnimationType.SLIDE_LEFT_OUT,
                quizAnimationType = QuizExamAnimationType.QUESTION_CARD_TRANSITION,
                direction = AnimationDirection.RIGHT,
                durationMs = 240,
                exitDurationMs = 180,
                initialScale = 0.98f,
                translationDistanceDp = 24f,
                easingCurve = AnimationEasingCurve.FAST_OUT_SLOW_IN
            )
            AnimatableTarget.MCQ_OPTION_CARDS -> ComponentAnimationSpec(
                target = target,
                enabled = true,
                entranceType = EntranceAnimationType.STAGGERED_ENTRANCE,
                exitType = ExitAnimationType.FADE_OUT,
                interactionType = CardInteractionAnimationType.PRESS_SCALE,
                quizAnimationType = QuizExamAnimationType.OPTION_STAGGER_REVEAL,
                direction = AnimationDirection.BOTTOM,
                durationMs = 220,
                staggerDelayMs = 45,
                maxStaggerItems = 5,
                dampingRatio = 0.68f,
                stiffness = 450f,
                initialScale = 0.96f,
                pressScale = 0.98f,
                translationDistanceDp = 12f,
                easingCurve = AnimationEasingCurve.FAST_OUT_SLOW_IN
            )
            AnimatableTarget.ANSWER_FEEDBACK -> ComponentAnimationSpec(
                target = target,
                enabled = true,
                entranceType = EntranceAnimationType.POP_IN,
                quizAnimationType = QuizExamAnimationType.CORRECT_ANSWER_PULSE,
                durationMs = 300,
                dampingRatio = 0.55f,
                stiffness = 500f,
                initialScale = 0.92f
            )
            AnimatableTarget.EXPLANATION_PANEL -> ComponentAnimationSpec(
                target = target,
                enabled = true,
                entranceType = EntranceAnimationType.EXPAND_IN,
                exitType = ExitAnimationType.COLLAPSE_AND_FADE,
                quizAnimationType = QuizExamAnimationType.EXPLANATION_DRAWER_EXPAND,
                direction = AnimationDirection.TOP,
                durationMs = 280,
                exitDurationMs = 200,
                translationDistanceDp = 14f
            )
            AnimatableTarget.RESULT_SUMMARY_SCORE -> ComponentAnimationSpec(
                target = target,
                enabled = true,
                entranceType = EntranceAnimationType.SPRING_SCALE,
                quizAnimationType = QuizExamAnimationType.SCORE_COUNTER_ROLL_UP,
                microInteractionType = MicroInteractionAnimationType.COUNT_UP_NUMBER,
                durationMs = 650,
                staggerDelayMs = 80,
                dampingRatio = 0.6f,
                stiffness = 380f,
                initialScale = 0.9f
            )
            AnimatableTarget.BUTTONS_CTAS -> ComponentAnimationSpec(
                target = target,
                enabled = true,
                entranceType = EntranceAnimationType.FADE_IN_SCALE,
                interactionType = CardInteractionAnimationType.RELEASE_SPRING,
                microInteractionType = MicroInteractionAnimationType.BUTTON_PRESS_BOUNCE,
                durationMs = 150,
                dampingRatio = 0.58f,
                stiffness = 550f,
                pressScale = 0.94f
            )
            else -> ComponentAnimationSpec(
                target = target,
                enabled = true,
                entranceType = EntranceAnimationType.FADE_IN_SCALE,
                exitType = ExitAnimationType.SMOOTH_DISMISS,
                interactionType = CardInteractionAnimationType.PRESS_SCALE,
                direction = AnimationDirection.BOTTOM,
                durationMs = 250,
                exitDurationMs = 180,
                staggerDelayMs = 50,
                maxStaggerItems = 8,
                dampingRatio = 0.65f,
                stiffness = 420f,
                initialScale = 0.96f,
                pressScale = 0.97f,
                translationDistanceDp = 14f,
                easingCurve = AnimationEasingCurve.FAST_OUT_SLOW_IN
            )
        }
    }

    fun buildPresetSpecs(preset: AnimationPresetId): Map<String, ComponentAnimationSpec> {
        val baseMap = AnimatableTarget.entries.associate { target ->
            target.name to getDefaultSpecForTarget(target)
        }.toMutableMap()

        when (preset) {
            AnimationPresetId.DEFAULT_SMOOTH,
            AnimationPresetId.CASCADE_ACCORDION_PRO -> {
                // Uses the carefully tuned defaults above
            }
            AnimationPresetId.ULTRA_PREMIUM -> {
                AnimatableTarget.entries.forEach { target ->
                    if (target != AnimatableTarget.CATEGORY_CARDS) {
                        val base = baseMap[target.name] ?: getDefaultSpecForTarget(target)
                        baseMap[target.name] = base.copy(
                            entranceType = when (target) {
                                AnimatableTarget.UNIT_CARDS,
                                AnimatableTarget.TOPIC_CARDS,
                                AnimatableTarget.SETS_CARDS,
                                AnimatableTarget.SUBTOPIC_CARDS -> EntranceAnimationType.CASCADE_ENTRANCE
                                AnimatableTarget.SUBJECT_CARDS -> EntranceAnimationType.DEPTH_BASED_ENTRANCE
                                else -> EntranceAnimationType.SPRING_SCALE
                            },
                            interactionType = CardInteractionAnimationType.LIFT_ON_PRESS,
                            dampingRatio = 0.58f,
                            stiffness = 380f,
                            initialScale = 0.93f,
                            translationDistanceDp = 20f,
                            easingCurve = AnimationEasingCurve.SPRING
                        )
                    }
                }
            }
            AnimationPresetId.CARD_3D_SHOWCASE -> {
                listOf(
                    AnimatableTarget.SUBJECT_CARDS,
                    AnimatableTarget.UNIT_CARDS,
                    AnimatableTarget.TOPIC_CARDS,
                    AnimatableTarget.SETS_CARDS,
                    AnimatableTarget.SUBTOPIC_CARDS,
                    AnimatableTarget.MCQ_QUESTION_CARDS
                ).forEach { target ->
                    val base = baseMap[target.name] ?: getDefaultSpecForTarget(target)
                    baseMap[target.name] = base.copy(
                        entranceType = if (target == AnimatableTarget.SUBJECT_CARDS) EntranceAnimationType.FLIP_3D_X else EntranceAnimationType.PERSPECTIVE_ZOOM,
                        interactionType = CardInteractionAnimationType.PERSPECTIVE_TILT,
                        hierarchyType = HierarchyDrillDownAnimationType.CARD_STACK_UNFOLD,
                        rotationDegrees = 14f,
                        cameraDistance = 14f,
                        durationMs = 340,
                        dampingRatio = 0.62f,
                        easingCurve = AnimationEasingCurve.SPRING
                    )
                }
            }
            AnimationPresetId.FAST_COMPETITIVE_EXAM -> {
                AnimatableTarget.entries.forEach { target ->
                    if (target != AnimatableTarget.CATEGORY_CARDS) {
                        val base = baseMap[target.name] ?: getDefaultSpecForTarget(target)
                        baseMap[target.name] = base.copy(
                            durationMs = 140,
                            exitDurationMs = 110,
                            staggerDelayMs = 25,
                            maxStaggerItems = 5,
                            dampingRatio = 0.82f,
                            stiffness = 700f,
                            initialScale = 0.98f,
                            translationDistanceDp = 8f,
                            easingCurve = AnimationEasingCurve.FAST_OUT_SLOW_IN
                        )
                    }
                }
            }
            AnimationPresetId.MINIMAL_CLEAN -> {
                AnimatableTarget.entries.forEach { target ->
                    if (target != AnimatableTarget.CATEGORY_CARDS) {
                        val base = baseMap[target.name] ?: getDefaultSpecForTarget(target)
                        baseMap[target.name] = base.copy(
                            entranceType = EntranceAnimationType.FADE_IN,
                            exitType = ExitAnimationType.FADE_OUT,
                            interactionType = CardInteractionAnimationType.PRESS_SCALE,
                            durationMs = 200,
                            exitDurationMs = 150,
                            staggerDelayMs = 30,
                            initialScale = 0.99f,
                            translationDistanceDp = 4f,
                            easingCurve = AnimationEasingCurve.STANDARD
                        )
                    }
                }
            }
            AnimationPresetId.BOUNCY_MODERN -> {
                AnimatableTarget.entries.forEach { target ->
                    if (target != AnimatableTarget.CATEGORY_CARDS) {
                        val base = baseMap[target.name] ?: getDefaultSpecForTarget(target)
                        baseMap[target.name] = base.copy(
                            entranceType = EntranceAnimationType.BOUNCE_IN,
                            interactionType = CardInteractionAnimationType.RELEASE_SPRING,
                            dampingRatio = 0.48f,
                            stiffness = 420f,
                            initialScale = 0.90f,
                            pressScale = 0.94f,
                            translationDistanceDp = 22f,
                            easingCurve = AnimationEasingCurve.BOUNCE
                        )
                    }
                }
            }
            AnimationPresetId.CINEMATIC_DEPTH -> {
                AnimatableTarget.entries.forEach { target ->
                    if (target != AnimatableTarget.CATEGORY_CARDS) {
                        val base = baseMap[target.name] ?: getDefaultSpecForTarget(target)
                        baseMap[target.name] = base.copy(
                            entranceType = EntranceAnimationType.DEPTH_BASED_ENTRANCE,
                            exitType = ExitAnimationType.SCALE_AND_FADE,
                            interactionType = CardInteractionAnimationType.ELEVATION_CHANGE,
                            durationMs = 360,
                            exitDurationMs = 220,
                            staggerDelayMs = 65,
                            initialScale = 0.92f,
                            translationDistanceDp = 24f,
                            cameraDistance = 16f,
                            easingCurve = AnimationEasingCurve.EASE_OUT_CUBIC
                        )
                    }
                }
            }
            AnimationPresetId.HIGH_FPS_LIGHTWEIGHT -> {
                AnimatableTarget.entries.forEach { target ->
                    if (target != AnimatableTarget.CATEGORY_CARDS) {
                        val base = baseMap[target.name] ?: getDefaultSpecForTarget(target)
                        baseMap[target.name] = base.copy(
                            entranceType = EntranceAnimationType.SLIDE_UP,
                            exitType = ExitAnimationType.FADE_OUT,
                            interactionType = CardInteractionAnimationType.PRESS_SCALE,
                            durationMs = 170,
                            exitDurationMs = 120,
                            staggerDelayMs = 20,
                            maxStaggerItems = 4,
                            dampingRatio = 0.85f,
                            stiffness = 650f,
                            initialScale = 0.98f,
                            translationDistanceDp = 10f,
                            easingCurve = AnimationEasingCurve.FAST_OUT_SLOW_IN
                        )
                    }
                }
            }
            AnimationPresetId.NO_ANIMATION_INSTANT -> {
                AnimatableTarget.entries.forEach { target ->
                    val base = baseMap[target.name] ?: getDefaultSpecForTarget(target)
                    baseMap[target.name] = base.copy(
                        enabled = false,
                        entranceType = EntranceAnimationType.NONE,
                        exitType = ExitAnimationType.NONE,
                        interactionType = CardInteractionAnimationType.NONE,
                        polishEffectType = VisualPolishEffectType.NONE,
                        durationMs = 0,
                        exitDurationMs = 0,
                        staggerDelayMs = 0
                    )
                }
            }
            AnimationPresetId.ROYAL_GOLD_EDITION -> {
                AnimatableTarget.entries.forEach { target ->
                    if (target != AnimatableTarget.CATEGORY_CARDS) {
                        val base = baseMap[target.name] ?: getDefaultSpecForTarget(target)
                        baseMap[target.name] = base.copy(
                            entranceType = EntranceAnimationType.CASCADE_ENTRANCE,
                            interactionType = CardInteractionAnimationType.BORDER_HIGHLIGHT,
                            polishEffectType = if (target == AnimatableTarget.SUBJECT_CARDS) VisualPolishEffectType.GLASS_CARD_HIGHLIGHT else VisualPolishEffectType.NONE,
                            durationMs = 310,
                            staggerDelayMs = 65,
                            dampingRatio = 0.6f,
                            stiffness = 390f,
                            initialScale = 0.94f,
                            translationDistanceDp = 18f,
                            easingCurve = AnimationEasingCurve.SPRING
                        )
                    }
                }
            }
            AnimationPresetId.PLAYFUL_SPRING_STUDIO -> {
                AnimatableTarget.entries.forEach { target ->
                    if (target != AnimatableTarget.CATEGORY_CARDS) {
                        val base = baseMap[target.name] ?: getDefaultSpecForTarget(target)
                        baseMap[target.name] = base.copy(
                            entranceType = EntranceAnimationType.ELASTIC_ENTRANCE,
                            interactionType = CardInteractionAnimationType.GENTLE_TILT,
                            hierarchyType = HierarchyDrillDownAnimationType.FAN_OUT_REVEAL,
                            durationMs = 330,
                            staggerDelayMs = 60,
                            dampingRatio = 0.52f,
                            stiffness = 360f,
                            initialScale = 0.91f,
                            translationDistanceDp = 20f,
                            easingCurve = AnimationEasingCurve.OVERSHOOT
                        )
                    }
                }
            }
            AnimationPresetId.HERO_EXPAND_PRO -> {
                AnimatableTarget.entries.forEach { target ->
                    if (target != AnimatableTarget.CATEGORY_CARDS) {
                        val base = baseMap[target.name] ?: getDefaultSpecForTarget(target)
                        baseMap[target.name] = base.copy(
                            entranceType = EntranceAnimationType.HERO_EXPAND,
                            interactionType = CardInteractionAnimationType.HERO_EXPAND,
                            hierarchyType = HierarchyDrillDownAnimationType.CASCADE_DROP_DOWN,
                            polishEffectType = if (
                                target == AnimatableTarget.SUBJECT_CARDS ||
                                target == AnimatableTarget.UNIT_CARDS ||
                                target == AnimatableTarget.TOPIC_CARDS
                            ) VisualPolishEffectType.GLASS_CARD_HIGHLIGHT else VisualPolishEffectType.NONE,
                            durationMs = 340,
                            exitDurationMs = 190,
                            staggerDelayMs = 60,
                            dampingRatio = 0.62f,
                            stiffness = 410f,
                            initialScale = 0.92f,
                            pressScale = 0.96f,
                            translationDistanceDp = 18f,
                            cameraDistance = 14f,
                            easingCurve = AnimationEasingCurve.SPRING
                        )
                    }
                }
            }
        }
        return baseMap
    }

    fun buildMasterConfigForPreset(preset: AnimationPresetId): GlobalAnimationMasterConfig {
        return when (preset) {
            AnimationPresetId.NO_ANIMATION_INSTANT -> GlobalAnimationMasterConfig(
                masterEnabled = false,
                activePresetId = preset,
                screenTransitionType = ScreenTransitionAnimationType.NONE,
                screenTransitionDurationMs = 0
            )
            AnimationPresetId.FAST_COMPETITIVE_EXAM -> GlobalAnimationMasterConfig(
                masterEnabled = true,
                speedMultiplier = 1.5f,
                intensityLevel = AnimationIntensityLevel.SUBTLE,
                activePresetId = preset,
                screenTransitionType = ScreenTransitionAnimationType.FADE_TRANSITION,
                screenTransitionDurationMs = 160
            )
            AnimationPresetId.ULTRA_PREMIUM -> GlobalAnimationMasterConfig(
                masterEnabled = true,
                speedMultiplier = 1.0f,
                intensityLevel = AnimationIntensityLevel.ULTRA_PREMIUM,
                activePresetId = preset,
                screenTransitionType = ScreenTransitionAnimationType.DEPTH_TRANSITION,
                screenTransitionDurationMs = 320
            )
            AnimationPresetId.CARD_3D_SHOWCASE -> GlobalAnimationMasterConfig(
                masterEnabled = true,
                speedMultiplier = 1.0f,
                intensityLevel = AnimationIntensityLevel.EXPRESSIVE,
                activePresetId = preset,
                screenTransitionType = ScreenTransitionAnimationType.FLIP_SCREEN_TRANSITION,
                screenTransitionDurationMs = 340
            )
            AnimationPresetId.CINEMATIC_DEPTH -> GlobalAnimationMasterConfig(
                masterEnabled = true,
                speedMultiplier = 1.0f,
                intensityLevel = AnimationIntensityLevel.EXPRESSIVE,
                activePresetId = preset,
                screenTransitionType = ScreenTransitionAnimationType.SHARED_AXIS_Z,
                screenTransitionDurationMs = 320
            )
            AnimationPresetId.HIGH_FPS_LIGHTWEIGHT -> GlobalAnimationMasterConfig(
                masterEnabled = true,
                lowEndPerformanceMode = true,
                speedMultiplier = 1.25f,
                intensityLevel = AnimationIntensityLevel.SUBTLE,
                activePresetId = preset,
                screenTransitionType = ScreenTransitionAnimationType.CLASSIC_HORIZONTAL_SLIDE,
                screenTransitionDurationMs = 200
            )
            AnimationPresetId.HERO_EXPAND_PRO -> GlobalAnimationMasterConfig(
                masterEnabled = true,
                speedMultiplier = 1.0f,
                intensityLevel = AnimationIntensityLevel.EXPRESSIVE,
                activePresetId = preset,
                screenTransitionType = ScreenTransitionAnimationType.HERO_EXPAND,
                screenTransitionDurationMs = 320
            )
            else -> GlobalAnimationMasterConfig(
                masterEnabled = true,
                speedMultiplier = 1.0f,
                intensityLevel = AnimationIntensityLevel.BALANCED,
                activePresetId = preset,
                screenTransitionType = ScreenTransitionAnimationType.HERO_EXPAND,
                screenTransitionDurationMs = 320
            )
        }
    }
}
