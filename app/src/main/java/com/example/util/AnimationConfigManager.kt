package com.example.util

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.example.data.SampleData
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Centralized Runtime Animation Configuration Manager.
 *
 * Implements the 5-tier configuration resolution hierarchy:
 * 1. Component override
 * 2. Screen override
 * 3. App-specific preset
 * 4. Global default preset
 * 5. Safe built-in fallback
 *
 * Supports real-time updates across the entire running app without restarting,
 * local SharedPreferences offline persistence, JSON import/export, version rollback,
 * and Cloud Firestore synchronization via FirebaseSyncManager.
 */
object AnimationConfigManager {
    private const val TAG = "AnimationConfigManager"
    private const val PREFS_NAME = "appsc_premium_animation_engine_prefs"
    private const val KEY_GLOBAL_CONFIG_JSON = "global_animation_config_json"
    private const val KEY_DRAFT_CONFIG_JSON = "draft_animation_config_json"
    private const val KEY_APP_CONFIG_PREFIX = "app_animation_config_"
    private const val KEY_HISTORY_JSON_LIST = "published_history_json_list"

    // 1. Reactive Global Default Configuration
    val globalConfigState = mutableStateOf(AnimationEngineConfig())

    // 2. Reactive Per-App Configurations (appId -> AnimationEngineConfig)
    val appConfigsState = mutableStateMapOf<String, AnimationEngineConfig>()

    // 3. Admin Draft / Local Preview Configuration
    val draftConfigState = mutableStateOf<AnimationEngineConfig?>(null)
    val isPreviewModeActive = mutableStateOf(false)

    // 4. Sandbox Slow-Motion Multiplier (1.0f = normal, 0.5f = half speed, 0.25f = quarter speed)
    val sandboxSlowMoFactor = mutableStateOf(1.0f)

    // 5. Monotonically increasing replay counter for triggering live replays
    val replayTriggerCounter = mutableIntStateOf(0)

    // 6. Version History for 1-Click Rollback (stores up to 10 previous published configs)
    val versionHistoryState = mutableStateListOf<AnimationEngineConfig>()

    // Cached system animator duration scale
    private var systemAnimatorScale: Float = 1.0f
    private var isInitialized: Boolean = false

    /**
     * Initializes local cache and reads Android system animator scale.
     */
    fun initialize(context: Context) {
        try {
            refreshSystemAnimatorScale(context)
            if (!isInitialized) {
                loadFromLocalPreferences(context)
                isInitialized = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize AnimationConfigManager, using safe defaults: ${e.message}")
        }
    }

    fun refreshSystemAnimatorScale(context: Context) {
        systemAnimatorScale = try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            ).coerceIn(0f, 5f)
        } catch (e: Exception) {
            1.0f
        }
    }

    /**
     * Resolves the active [AnimationEngineConfig] for the given [appId].
     */
    fun getActiveEngineConfig(appId: String = SampleData.currentActiveAppId.value): AnimationEngineConfig {
        if (isPreviewModeActive.value) {
            draftConfigState.value?.let { return it }
        }
        return appConfigsState[appId] ?: globalConfigState.value
    }

    /**
     * Resolves the effective [ComponentAnimationSpec] for a specific [target] and optional [screenKey]
     * following the strict 5-tier priority hierarchy:
     * 1. Component override (in active config)
     * 2. Screen override (screenOverrides[screenKey][target.name])
     * 3. App-specific preset (appConfigsState[appId])
     * 4. Global default preset (globalConfigState)
     * 5. Safe built-in fallback (AnimationPresets.getDefaultSpecForTarget(target))
     */
    fun resolveSpec(
        target: AnimatableTarget,
        screenKey: String? = null,
        appId: String = SampleData.currentActiveAppId.value
    ): ComponentAnimationSpec {
        val fallback = AnimationPresets.getDefaultSpecForTarget(target)
        val activeConfig = getActiveEngineConfig(appId)
        val master = activeConfig.masterConfig

        // Check if master animations or system animator scale disables animations
        if (!master.masterEnabled ||
            master.activePresetId == AnimationPresetId.NO_ANIMATION_INSTANT ||
            (master.respectSystemAnimatorScale && systemAnimatorScale <= 0.01f)
        ) {
            return fallback.copy(
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

        // Priority 1 & 2: Screen-specific override -> Component-specific override -> App config -> Global config -> Fallback
        val rawSpec = (if (!screenKey.isNullOrBlank()) {
            activeConfig.screenOverrides[screenKey]?.get(target.name)
        } else null)
            ?: activeConfig.componentSpecs[target.name]
            ?: appConfigsState[appId]?.componentSpecs?.get(target.name)
            ?: globalConfigState.value.componentSpecs[target.name]
            ?: fallback

        if (!rawSpec.enabled) {
            return rawSpec.copy(
                entranceType = EntranceAnimationType.NONE,
                exitType = ExitAnimationType.NONE,
                interactionType = CardInteractionAnimationType.NONE,
                polishEffectType = VisualPolishEffectType.NONE,
                durationMs = 0,
                exitDurationMs = 0,
                staggerDelayMs = 0
            )
        }

        // Apply Reduced Motion & Low-End Performance adjustments if enabled
        if (master.reducedMotionMode) {
            return rawSpec.copy(
                entranceType = if (rawSpec.entranceType == EntranceAnimationType.NONE) EntranceAnimationType.NONE else EntranceAnimationType.FADE_IN,
                exitType = ExitAnimationType.FADE_OUT,
                interactionType = CardInteractionAnimationType.PRESS_SCALE,
                polishEffectType = VisualPolishEffectType.NONE,
                durationMs = 150,
                exitDurationMs = 120,
                staggerDelayMs = 20,
                maxStaggerItems = 4,
                translationDistanceDp = 0f,
                exitTranslationDistanceDp = 0f,
                initialScale = 1f,
                pressScale = 0.98f,
                continuousEffectEnabled = false
            )
        }

        val speedFactor = master.speedMultiplier.coerceIn(0.25f, 3.0f)
        val sysScale = if (master.respectSystemAnimatorScale) systemAnimatorScale.coerceIn(0.5f, 2.0f) else 1.0f
        val effectiveDurationScale = (sysScale / speedFactor).coerceIn(0.2f, 4.0f)

        val intensity = master.intensityLevel
        val scaledDuration = (rawSpec.durationMs * effectiveDurationScale).toInt().coerceIn(60, 1800)
        val scaledExitDuration = (rawSpec.exitDurationMs * effectiveDurationScale).toInt().coerceIn(50, 1200)
        val scaledStagger = (rawSpec.staggerDelayMs * effectiveDurationScale).toInt().coerceIn(0, 300)
        val scaledStartDelay = (rawSpec.startDelayMs * effectiveDurationScale).toInt().coerceIn(0, 1000)

        val scaledDistance = (rawSpec.translationDistanceDp * intensity.distanceMultiplier).coerceIn(0f, 80f)
        val scaleDelta = (1f - rawSpec.initialScale) * intensity.scaleDeltaMultiplier
        val scaledInitialScale = (1f - scaleDelta).coerceIn(0.75f, 1.25f)

        return if (master.lowEndPerformanceMode) {
            rawSpec.copy(
                durationMs = (scaledDuration * 0.8f).toInt().coerceAtLeast(80),
                exitDurationMs = (scaledExitDuration * 0.8f).toInt().coerceAtLeast(60),
                staggerDelayMs = (scaledStagger * 0.65f).toInt(),
                maxStaggerItems = rawSpec.maxStaggerItems.coerceAtMost(5),
                translationDistanceDp = scaledDistance * 0.75f,
                initialScale = scaledInitialScale.coerceAtLeast(0.95f),
                polishEffectType = VisualPolishEffectType.NONE,
                continuousEffectEnabled = false
            )
        } else {
            rawSpec.copy(
                durationMs = scaledDuration,
                exitDurationMs = scaledExitDuration,
                staggerDelayMs = scaledStagger,
                startDelayMs = scaledStartDelay,
                translationDistanceDp = scaledDistance,
                initialScale = scaledInitialScale
            )
        }
    }

    /**
     * Applies a Ready-Made Preset to the given config and returns the updated config.
     */
    fun applyPreset(
        current: AnimationEngineConfig,
        preset: AnimationPresetId
    ): AnimationEngineConfig {
        return current.copy(
            masterConfig = AnimationPresets.buildMasterConfigForPreset(preset),
            componentSpecs = AnimationPresets.buildPresetSpecs(preset),
            updatedAt = System.currentTimeMillis(),
            notes = "Preset: ${preset.displayName}"
        )
    }

    /**
     * Saves a draft configuration locally without publishing to live Firestore users.
     */
    fun saveDraft(context: Context, draft: AnimationEngineConfig) {
        draftConfigState.value = draft
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_DRAFT_CONFIG_JSON, serializeConfigToJson(draft)).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed saving draft config: ${e.message}")
        }
    }

    /**
     * Publishes configuration to Current App, Selected Apps, or All Apps,
     * records version history for rollback, persists locally, and syncs to Firestore.
     */
    fun publishConfig(
        context: Context?,
        config: AnimationEngineConfig,
        scope: String = config.targetScope, // "CURRENT", "SELECTED", "ALL"
        selectedAppIds: List<String> = config.selectedAppIds,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        val currentAppId = SampleData.currentActiveAppId.value
        val previousSnapshot = getActiveEngineConfig(currentAppId)
        if (versionHistoryState.isEmpty() || versionHistoryState.first().updatedAt != previousSnapshot.updatedAt) {
            versionHistoryState.add(0, previousSnapshot)
            if (versionHistoryState.size > 10) {
                versionHistoryState.removeAt(versionHistoryState.lastIndex)
            }
        }

        val nextVersion = (previousSnapshot.versionNumber + 1).coerceAtLeast(1)
        val published = config.copy(
            targetScope = scope,
            selectedAppIds = selectedAppIds,
            versionNumber = nextVersion,
            updatedAt = System.currentTimeMillis()
        )

        draftConfigState.value = published

        when (scope) {
            "ALL" -> {
                globalConfigState.value = published
                SampleData.appsListState.forEach { app ->
                    appConfigsState[app.id] = published
                }
            }
            "SELECTED" -> {
                val targets = selectedAppIds.ifEmpty { listOf(currentAppId) }
                targets.forEach { id ->
                    appConfigsState[id] = published
                }
                if (targets.contains(currentAppId)) {
                    globalConfigState.value = published
                }
            }
            else -> {
                appConfigsState[currentAppId] = published
                globalConfigState.value = published
            }
        }

        if (context != null) {
            saveToLocalPreferences(context, published, scope, selectedAppIds)
        }

        // Sync to Cloud Firestore via FirebaseSyncManager
        FirebaseSyncManager.syncAnimationEngineConfigToFirestore(
            appId = currentAppId,
            config = published,
            onSuccess = {
                onComplete?.invoke(true, "Published Animation Studio v$nextVersion ($scope) & synced live!")
            },
            onFailure = { e ->
                Log.w(TAG, "Firestore sync warning (saved locally): ${e.message}")
                onComplete?.invoke(true, "Saved & applied Animation Studio v$nextVersion locally!")
            }
        )
    }

    /**
     * Rolls back to the most recent version in [versionHistoryState].
     */
    fun rollbackToPreviousVersion(context: Context?): AnimationEngineConfig? {
        if (versionHistoryState.isEmpty()) return null
        val previous = versionHistoryState.removeAt(0)
        globalConfigState.value = previous
        appConfigsState[SampleData.currentActiveAppId.value] = previous
        draftConfigState.value = previous
        if (context != null) {
            saveToLocalPreferences(context, previous, previous.targetScope, previous.selectedAppIds)
        }
        FirebaseSyncManager.syncAnimationEngineConfigToFirestore(
            appId = SampleData.currentActiveAppId.value,
            config = previous
        )
        replayTriggerCounter.intValue++
        return previous
    }

    /**
     * Resets a single component target to its built-in default specification.
     */
    fun resetComponentToDefault(
        config: AnimationEngineConfig,
        target: AnimatableTarget
    ): AnimationEngineConfig {
        val updatedSpecs = config.componentSpecs.toMutableMap()
        updatedSpecs[target.name] = AnimationPresets.getDefaultSpecForTarget(target)
        return config.copy(
            componentSpecs = updatedSpecs,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Resets the entire animation configuration to the default Cascade Accordion Pro preset.
     */
    fun resetAllToDefault(context: Context?): AnimationEngineConfig {
        val defaultConfig = AnimationEngineConfig(
            masterConfig = AnimationPresets.buildMasterConfigForPreset(AnimationPresetId.CASCADE_ACCORDION_PRO),
            componentSpecs = AnimationPresets.buildPresetSpecs(AnimationPresetId.CASCADE_ACCORDION_PRO),
            versionNumber = globalConfigState.value.versionNumber + 1,
            updatedAt = System.currentTimeMillis(),
            notes = "Reset to Default Cascade Accordion Pro"
        )
        globalConfigState.value = defaultConfig
        appConfigsState.clear()
        draftConfigState.value = defaultConfig
        if (context != null) {
            saveToLocalPreferences(context, defaultConfig, "ALL", emptyList())
        }
        FirebaseSyncManager.syncAnimationEngineConfigToFirestore(
            appId = SampleData.currentActiveAppId.value,
            config = defaultConfig
        )
        replayTriggerCounter.intValue++
        return defaultConfig
    }

    fun triggerReplay() {
        replayTriggerCounter.intValue++
    }

    private fun saveToLocalPreferences(
        context: Context,
        config: AnimationEngineConfig,
        scope: String,
        selectedAppIds: List<String>
    ) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = serializeConfigToJson(config)
            val editor = prefs.edit()
            editor.putString(KEY_GLOBAL_CONFIG_JSON, json)
            editor.putString(KEY_DRAFT_CONFIG_JSON, json)
            val currentAppId = SampleData.currentActiveAppId.value
            editor.putString(KEY_APP_CONFIG_PREFIX + currentAppId, json)
            if (scope == "SELECTED") {
                selectedAppIds.forEach { id ->
                    editor.putString(KEY_APP_CONFIG_PREFIX + id, json)
                }
            }
            val historyArray = JSONArray()
            versionHistoryState.take(8).forEach { item ->
                historyArray.put(serializeConfigToJson(item))
            }
            editor.putString(KEY_HISTORY_JSON_LIST, historyArray.toString())
            editor.apply()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving animation config to SharedPreferences: ${e.message}")
        }
    }

    private fun loadFromLocalPreferences(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val globalJson = prefs.getString(KEY_GLOBAL_CONFIG_JSON, null)
            if (!globalJson.isNullOrBlank()) {
                parseConfigFromJson(globalJson)?.let { parsed ->
                    globalConfigState.value = parsed
                }
            }
            val draftJson = prefs.getString(KEY_DRAFT_CONFIG_JSON, null)
            if (!draftJson.isNullOrBlank()) {
                parseConfigFromJson(draftJson)?.let { parsed ->
                    draftConfigState.value = parsed
                }
            }
            val currentAppId = SampleData.currentActiveAppId.value
            val appJson = prefs.getString(KEY_APP_CONFIG_PREFIX + currentAppId, null)
            if (!appJson.isNullOrBlank()) {
                parseConfigFromJson(appJson)?.let { parsed ->
                    appConfigsState[currentAppId] = parsed
                }
            }
            val historyRaw = prefs.getString(KEY_HISTORY_JSON_LIST, null)
            if (!historyRaw.isNullOrBlank()) {
                val arr = JSONArray(historyRaw)
                versionHistoryState.clear()
                for (i in 0 until arr.length()) {
                    parseConfigFromJson(arr.optString(i))?.let { versionHistoryState.add(it) }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error loading animation config from SharedPreferences, using default: ${e.message}")
        }
    }

    /**
     * Exports [config] to a formatted JSON string for Admin backup/sharing.
     */
    fun serializeConfigToJson(config: AnimationEngineConfig, pretty: Boolean = false): String {
        return try {
            val root = JSONObject()
            root.put("versionNumber", config.versionNumber)
            root.put("targetScope", config.targetScope)
            root.put("updatedAt", config.updatedAt)
            root.put("updatedBy", config.updatedBy)
            root.put("notes", config.notes)

            val appsArray = JSONArray()
            config.selectedAppIds.forEach { appsArray.put(it) }
            root.put("selectedAppIds", appsArray)

            // Master config
            val m = config.masterConfig
            val masterObj = JSONObject().apply {
                put("masterEnabled", m.masterEnabled)
                put("reducedMotionMode", m.reducedMotionMode)
                put("lowEndPerformanceMode", m.lowEndPerformanceMode)
                put("respectSystemAnimatorScale", m.respectSystemAnimatorScale)
                put("speedMultiplier", m.speedMultiplier.toDouble())
                put("intensityLevel", m.intensityLevel.name)
                put("activePresetId", m.activePresetId.name)
                put("screenTransitionType", m.screenTransitionType.name)
                put("screenTransitionDurationMs", m.screenTransitionDurationMs)
            }
            root.put("masterConfig", masterObj)

            // Component specs
            val specsObj = JSONObject()
            config.componentSpecs.forEach { (key, spec) ->
                specsObj.put(key, serializeSpecToJson(spec))
            }
            root.put("componentSpecs", specsObj)

            // Screen overrides
            val screensObj = JSONObject()
            config.screenOverrides.forEach { (screenName, map) ->
                val sObj = JSONObject()
                map.forEach { (k, spec) ->
                    sObj.put(k, serializeSpecToJson(spec))
                }
                screensObj.put(screenName, sObj)
            }
            root.put("screenOverrides", screensObj)

            if (pretty) root.toString(2) else root.toString()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to serialize AnimationEngineConfig: ${e.message}")
            "{}"
        }
    }

    private fun serializeSpecToJson(spec: ComponentAnimationSpec): JSONObject {
        return JSONObject().apply {
            put("target", spec.target.name)
            put("enabled", spec.enabled)
            put("entranceType", spec.entranceType.name)
            put("exitType", spec.exitType.name)
            put("interactionType", spec.interactionType.name)
            put("hierarchyType", spec.hierarchyType.name)
            put("listScrollType", spec.listScrollType.name)
            put("microInteractionType", spec.microInteractionType.name)
            put("quizAnimationType", spec.quizAnimationType.name)
            put("loadingAnimationType", spec.loadingAnimationType.name)
            put("polishEffectType", spec.polishEffectType.name)
            put("direction", spec.direction.name)
            put("durationMs", spec.durationMs)
            put("exitDurationMs", spec.exitDurationMs)
            put("startDelayMs", spec.startDelayMs)
            put("staggerDelayMs", spec.staggerDelayMs)
            put("maxStaggerItems", spec.maxStaggerItems)
            put("dampingRatio", spec.dampingRatio.toDouble())
            put("stiffness", spec.stiffness.toDouble())
            put("initialScale", spec.initialScale.toDouble())
            put("pressScale", spec.pressScale.toDouble())
            put("translationDistanceDp", spec.translationDistanceDp.toDouble())
            put("exitTranslationDistanceDp", spec.exitTranslationDistanceDp.toDouble())
            put("rotationDegrees", spec.rotationDegrees.toDouble())
            put("cameraDistance", spec.cameraDistance.toDouble())
            put("initialAlpha", spec.initialAlpha.toDouble())
            put("easingCurve", spec.easingCurve.name)
            put("singleOpenAccordion", spec.singleOpenAccordion)
            put("continuousEffectEnabled", spec.continuousEffectEnabled)
        }
    }

    /**
     * Safely parses [json] into an [AnimationEngineConfig].
     * Never throws; falls back to safe defaults for any missing or invalid enum/field.
     */
    fun parseConfigFromJson(json: String): AnimationEngineConfig? {
        if (json.isBlank()) return null
        return try {
            val root = JSONObject(json)
            val mObj = root.optJSONObject("masterConfig")
            val presetId = safeEnum(mObj?.optString("activePresetId"), AnimationPresetId.CASCADE_ACCORDION_PRO)
            val masterConfig = if (mObj != null) {
                GlobalAnimationMasterConfig(
                    masterEnabled = mObj.optBoolean("masterEnabled", true),
                    reducedMotionMode = mObj.optBoolean("reducedMotionMode", false),
                    lowEndPerformanceMode = mObj.optBoolean("lowEndPerformanceMode", false),
                    respectSystemAnimatorScale = mObj.optBoolean("respectSystemAnimatorScale", true),
                    speedMultiplier = mObj.optDouble("speedMultiplier", 1.0).toFloat().coerceIn(0.25f, 3.0f),
                    intensityLevel = safeEnum(mObj.optString("intensityLevel"), AnimationIntensityLevel.BALANCED),
                    activePresetId = presetId,
                    screenTransitionType = safeEnum(mObj.optString("screenTransitionType"), ScreenTransitionAnimationType.SHARED_AXIS_X),
                    screenTransitionDurationMs = mObj.optInt("screenTransitionDurationMs", 280).coerceIn(0, 2000)
                )
            } else {
                GlobalAnimationMasterConfig()
            }

            val defaultSpecs = AnimationPresets.buildPresetSpecs(presetId).toMutableMap()
            val specsObj = root.optJSONObject("componentSpecs")
            if (specsObj != null) {
                val keys = specsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val target = safeEnum<AnimatableTarget>(key, null)
                    val sObj = specsObj.optJSONObject(key)
                    if (target != null && sObj != null) {
                        defaultSpecs[key] = parseSpecFromJson(target, sObj)
                    }
                }
            }

            val screenOverrides = mutableMapOf<String, Map<String, ComponentAnimationSpec>>()
            val screensObj = root.optJSONObject("screenOverrides")
            if (screensObj != null) {
                val sKeys = screensObj.keys()
                while (sKeys.hasNext()) {
                    val screenName = sKeys.next()
                    val innerObj = screensObj.optJSONObject(screenName)
                    if (innerObj != null) {
                        val innerMap = mutableMapOf<String, ComponentAnimationSpec>()
                        val tKeys = innerObj.keys()
                        while (tKeys.hasNext()) {
                            val tKey = tKeys.next()
                            val target = safeEnum<AnimatableTarget>(tKey, null)
                            val specJson = innerObj.optJSONObject(tKey)
                            if (target != null && specJson != null) {
                                innerMap[tKey] = parseSpecFromJson(target, specJson)
                            }
                        }
                        screenOverrides[screenName] = innerMap
                    }
                }
            }

            val selectedIds = mutableListOf<String>()
            val appsArr = root.optJSONArray("selectedAppIds")
            if (appsArr != null) {
                for (i in 0 until appsArr.length()) {
                    val id = appsArr.optString(i)
                    if (id.isNotBlank()) selectedIds.add(id)
                }
            }

            AnimationEngineConfig(
                masterConfig = masterConfig,
                componentSpecs = defaultSpecs,
                screenOverrides = screenOverrides,
                targetScope = root.optString("targetScope", "ALL"),
                selectedAppIds = selectedIds,
                versionNumber = root.optInt("versionNumber", 1),
                updatedAt = root.optLong("updatedAt", System.currentTimeMillis()),
                updatedBy = root.optString("updatedBy", "sobhadhanaraju0@gmail.com"),
                notes = root.optString("notes", "")
            )
        } catch (e: Exception) {
            Log.w(TAG, "Invalid JSON in parseConfigFromJson: ${e.message}")
            null
        }
    }

    private fun parseSpecFromJson(target: AnimatableTarget, obj: JSONObject): ComponentAnimationSpec {
        val def = AnimationPresets.getDefaultSpecForTarget(target)
        return ComponentAnimationSpec(
            target = target,
            enabled = obj.optBoolean("enabled", def.enabled),
            entranceType = safeEnum(obj.optString("entranceType"), def.entranceType),
            exitType = safeEnum(obj.optString("exitType"), def.exitType),
            interactionType = safeEnum(obj.optString("interactionType"), def.interactionType),
            hierarchyType = safeEnum(obj.optString("hierarchyType"), def.hierarchyType),
            listScrollType = safeEnum(obj.optString("listScrollType"), def.listScrollType),
            microInteractionType = safeEnum(obj.optString("microInteractionType"), def.microInteractionType),
            quizAnimationType = safeEnum(obj.optString("quizAnimationType"), def.quizAnimationType),
            loadingAnimationType = safeEnum(obj.optString("loadingAnimationType"), def.loadingAnimationType),
            polishEffectType = safeEnum(obj.optString("polishEffectType"), def.polishEffectType),
            direction = safeEnum(obj.optString("direction"), def.direction),
            durationMs = obj.optInt("durationMs", def.durationMs).coerceIn(0, 2500),
            exitDurationMs = obj.optInt("exitDurationMs", def.exitDurationMs).coerceIn(0, 2000),
            startDelayMs = obj.optInt("startDelayMs", def.startDelayMs).coerceIn(0, 2000),
            staggerDelayMs = obj.optInt("staggerDelayMs", def.staggerDelayMs).coerceIn(0, 500),
            maxStaggerItems = obj.optInt("maxStaggerItems", def.maxStaggerItems).coerceIn(1, 24),
            dampingRatio = obj.optDouble("dampingRatio", def.dampingRatio.toDouble()).toFloat().coerceIn(0.2f, 1.0f),
            stiffness = obj.optDouble("stiffness", def.stiffness.toDouble()).toFloat().coerceIn(50f, 3000f),
            initialScale = obj.optDouble("initialScale", def.initialScale.toDouble()).toFloat().coerceIn(0.5f, 1.5f),
            pressScale = obj.optDouble("pressScale", def.pressScale.toDouble()).toFloat().coerceIn(0.8f, 1.15f),
            translationDistanceDp = obj.optDouble("translationDistanceDp", def.translationDistanceDp.toDouble()).toFloat().coerceIn(0f, 120f),
            exitTranslationDistanceDp = obj.optDouble("exitTranslationDistanceDp", def.exitTranslationDistanceDp.toDouble()).toFloat().coerceIn(0f, 120f),
            rotationDegrees = obj.optDouble("rotationDegrees", def.rotationDegrees.toDouble()).toFloat().coerceIn(0f, 180f),
            cameraDistance = obj.optDouble("cameraDistance", def.cameraDistance.toDouble()).toFloat().coerceIn(4f, 48f),
            initialAlpha = obj.optDouble("initialAlpha", def.initialAlpha.toDouble()).toFloat().coerceIn(0f, 1f),
            easingCurve = safeEnum(obj.optString("easingCurve"), def.easingCurve),
            singleOpenAccordion = obj.optBoolean("singleOpenAccordion", def.singleOpenAccordion),
            continuousEffectEnabled = obj.optBoolean("continuousEffectEnabled", def.continuousEffectEnabled)
        )
    }

    private inline fun <reified T : Enum<T>> safeEnum(value: String?, fallback: T): T {
        if (value.isNullOrBlank()) return fallback
        return try {
            enumValueOf<T>(value)
        } catch (_: Exception) {
            fallback
        }
    }

    @JvmName("safeEnumNullable")
    private inline fun <reified T : Enum<T>> safeEnum(value: String?, fallback: T?): T? {
        if (value.isNullOrBlank()) return fallback
        return try {
            enumValueOf<T>(value)
        } catch (_: Exception) {
            fallback
        }
    }
}
