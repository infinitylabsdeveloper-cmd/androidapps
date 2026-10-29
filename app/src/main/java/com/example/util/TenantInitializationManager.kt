package com.example.util

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.model.*
import com.example.network.AppConfigStartupManager
import com.example.repository.AppConfigRepository
import com.example.repository.TenantConfig
import com.example.repository.TenantConfigRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Indicates the origin of the applied tenant configuration.
 */
enum class TenantConfigSource {
    DISK_CACHE,
    REMOTE_NETWORK,
    DEFAULT_FALLBACK
}

/**
 * Result bundle detailing the outcome of the tenant initialization process.
 */
data class TenantInitializationResult(
    val isSuccess: Boolean,
    val tenantId: String,
    val appId: String,
    val appName: String,
    val primaryColorHex: Long,
    val source: TenantConfigSource,
    val appliedFeatureFlags: Map<String, Boolean>,
    val latencyMs: Long,
    val errorMessage: String? = null
)

/**
 * Observable lifecycle state for tenant initialization.
 */
sealed class TenantInitializationState {
    object Idle : TenantInitializationState()
    data class Initializing(val stage: String, val progressFraction: Float) : TenantInitializationState()
    data class Initialized(val result: TenantInitializationResult) : TenantInitializationState()
    data class Failed(val error: Throwable, val fallbackResult: TenantInitializationResult) : TenantInitializationState()
}

/**
 * TenantInitializationManager:
 * Coordinates the fetch and application process for both AppConfig and TenantConfig
 * immediately upon app launch, ensuring that UI theme, branding, and feature flags
 * are guaranteed to be resolved and applied before the dashboard renders.
 *
 * Execution Pipeline:
 * 1. Zero-Latency Prewarm: Immediately loads local persistent disk cache for instant UI availability.
 * 2. Concurrent Network Synchronization: Fetches both AppConfig and TenantConfig in parallel via Retrofit/HTTP.
 * 3. Fail-Safe Timeout Protection: Employs a bounded window (default 2200ms) so slow or offline networks never deadlock splash transition.
 * 4. Atomic Configuration Application: Binds branding, primary/secondary colors, shapes, and feature flags into AppConfigRepository,
 *    TenantConfigRepository, SampleData, and DynamicAppTheme before signalling initialization completion.
 */
object TenantInitializationManager {

    private const val TAG = "TenantInitManager"
    private const val DEFAULT_TIMEOUT_MS = 2200L

    private val _state = MutableStateFlow<TenantInitializationState>(TenantInitializationState.Idle)
    val state: StateFlow<TenantInitializationState> = _state.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    @Volatile
    private var lastResult: TenantInitializationResult? = null

    /**
     * Coordinates the initialization process upon app launch.
     * Guaranteed to apply UI theme, branding, and feature flags before completing.
     *
     * @param context Application or Activity context.
     * @param timeoutMs Bounded maximum time to wait for network before falling back to local cache/defaults.
     */
    suspend fun initializeTenant(
        context: Context,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS
    ): TenantInitializationResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val appContext = context.applicationContext
        val targetTenantId = BuildConfig.TENANT_ID
        val targetAppId = BuildConfig.APP_ID

        Log.i(TAG, "Starting tenant initialization sequence for TENANT_ID: $targetTenantId, APP_ID: $targetAppId")
        _state.value = TenantInitializationState.Initializing("Pre-warming cached configuration", 0.1f)

        // 1. Instant Zero-Latency Cache Pre-warm
        val tenantRepo = TenantConfigRepository.getInstance(appContext)
        val appConfigRepo = AppConfigRepository.getInstance().apply {
            initializeWithContext(appContext)
        }

        // Apply cached or default configuration immediately to prevent any UI flicker
        var activeTenantConfig = tenantRepo.getCurrentConfig()
        applyConfigToRepositories(appConfigRepo, activeTenantConfig)

        _state.value = TenantInitializationState.Initializing("Synchronizing remote configuration", 0.4f)

        // 2. Concurrent Network Fetch with bounded timeout
        var resolvedSource = if (activeTenantConfig.isCached) TenantConfigSource.DISK_CACHE else TenantConfigSource.DEFAULT_FALLBACK
        var fetchError: Throwable? = null

        try {
            withTimeoutOrNull(timeoutMs) {
                coroutineScope {
                    val tenantConfigDeferred = async {
                        try {
                            tenantRepo.fetchAndCacheConfig(targetTenantId)
                        } catch (e: Exception) {
                            Log.w(TAG, "TenantConfig fetch note: ${e.message}")
                            Result.failure(e)
                        }
                    }

                    val appConfigDeferred = async {
                        try {
                            AppConfigStartupManager.fetchStartupConfiguration(appContext, targetAppId)
                        } catch (e: Exception) {
                            Log.w(TAG, "AppConfig fetch note: ${e.message}")
                            null
                        }
                    }

                    val tenantResult = tenantConfigDeferred.await()
                    val remoteAppConfig = appConfigDeferred.await()

                    if (tenantResult.isSuccess) {
                        activeTenantConfig = tenantResult.getOrThrow()
                        resolvedSource = TenantConfigSource.REMOTE_NETWORK
                        Log.i(TAG, "Successfully retrieved remote TenantConfig for $targetTenantId")
                    } else {
                        Log.w(TAG, "TenantConfig fetch failed or timed out, maintaining cached/default")
                    }

                    if (remoteAppConfig != null) {
                        Log.i(TAG, "Successfully retrieved remote AppConfig for $targetAppId")
                    }
                }
            } ?: run {
                Log.w(TAG, "Remote config fetch exceeded ${timeoutMs}ms limit; continuing with cached/bundled configuration.")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Exception during remote configuration synchronization: ${t.message}", t)
            fetchError = t
        }

        _state.value = TenantInitializationState.Initializing("Applying theme, branding & feature flags", 0.8f)

        // 3. Atomically apply final resolved configuration to UI theme, branding, and feature flags
        applyConfigToRepositories(appConfigRepo, activeTenantConfig)
        // Synchronize active app in SampleData
        try {
            com.example.data.SampleData.setActiveApp(targetAppId)
        } catch (ex: Exception) {
            Log.w(TAG, "SampleData.setActiveApp note: ${ex.message}")
        }

        val latency = System.currentTimeMillis() - startTime
        val appliedFlags = mapOf(
            "mcqsEnabled" to activeTenantConfig.featureFlags.mcqsEnabled,
            "mockTestsEnabled" to activeTenantConfig.featureFlags.mockTestsEnabled,
            "currentAffairsEnabled" to activeTenantConfig.featureFlags.currentAffairsEnabled,
            "leaderboardEnabled" to activeTenantConfig.featureFlags.leaderboardEnabled,
            "subscriptionsEnabled" to activeTenantConfig.featureFlags.subscriptionsEnabled,
            "paymentsEnabled" to activeTenantConfig.featureFlags.paymentsEnabled,
            "notificationsEnabled" to activeTenantConfig.featureFlags.notificationsEnabled,
            "offlineDownloadsEnabled" to activeTenantConfig.featureFlags.offlineDownloadsEnabled,
            "aiMentorEnabled" to activeTenantConfig.featureFlags.aiMentorEnabled,
            "scientificCalculatorEnabled" to activeTenantConfig.featureFlags.scientificCalculatorEnabled
        )

        val result = TenantInitializationResult(
            isSuccess = fetchError == null || resolvedSource != TenantConfigSource.DEFAULT_FALLBACK,
            tenantId = activeTenantConfig.tenantId,
            appId = activeTenantConfig.appId,
            appName = activeTenantConfig.branding.appName,
            primaryColorHex = activeTenantConfig.theme.primaryColorHex,
            source = resolvedSource,
            appliedFeatureFlags = appliedFlags,
            latencyMs = latency,
            errorMessage = fetchError?.message
        )

        lastResult = result
        _isReady.value = true
        _state.value = TenantInitializationState.Initialized(result)

        Log.i(
            TAG,
            "TenantInitializationManager finished in ${latency}ms! " +
            "Source: $resolvedSource, App: ${result.appName}, ThemePrimary: 0x${result.primaryColorHex.toString(16)}"
        )

        result
    }

    /**
     * Atomically propagates the resolved TenantConfig into AppConfigRepository
     * so that DynamicAppTheme and Compose components recompose before the dashboard appears.
     */
    private fun applyConfigToRepositories(
        appConfigRepo: AppConfigRepository,
        tenantConfig: TenantConfig
    ) {
        val centralizedConfig = CentralizedTenantConfig(
            appId = tenantConfig.appId,
            tenantId = tenantConfig.tenantId,
            packageName = tenantConfig.packageName,
            examCategory = tenantConfig.examCategory,
            branding = tenantConfig.branding,
            featureFlags = tenantConfig.featureFlags,
            themeProperties = tenantConfig.theme,
            maintenanceMode = tenantConfig.isMaintenanceMode,
            maintenanceMessage = tenantConfig.maintenanceMessage,
            razorpayKeyId = tenantConfig.razorpayKeyId,
            lastFetchedTimestamp = tenantConfig.lastSyncTimestamp
        )

        // Update centralized configuration in AppConfigRepository
        appConfigRepo.updateTenantConfig(centralizedConfig)

        // Also ensure legacy AppConfiguration model reflects the new theme and flags
        val current = appConfigRepo.getCurrentConfig()
        val updatedAppConfig = current.copy(
            appName = tenantConfig.branding.appName,
            appCode = tenantConfig.branding.appCode,
            tagline = tenantConfig.branding.tagline,
            description = tenantConfig.branding.description,
            primaryColorHex = tenantConfig.theme.primaryColorHex,
            secondaryColorHex = tenantConfig.theme.secondaryColorHex,
            accentColorHex = tenantConfig.theme.accentColorHex,
            surfaceColorHex = tenantConfig.theme.surfaceColorHex,
            backgroundColorHex = tenantConfig.theme.backgroundColorHex,
            cardCornerRadiusDp = tenantConfig.theme.cardCornerRadiusDp,
            buttonCornerRadiusDp = tenantConfig.theme.buttonCornerRadiusDp,
            supportEmail = tenantConfig.branding.supportEmail,
            supportPhone = tenantConfig.branding.supportPhone,
            whatsappNumber = tenantConfig.branding.whatsappNumber,
            whatsappLink = tenantConfig.branding.whatsappLink,
            telegramId = tenantConfig.branding.telegramId,
            telegramLink = tenantConfig.branding.telegramLink,
            isMockTestActive = tenantConfig.featureFlags.mockTestsEnabled,
            isCurrentAffairsActive = tenantConfig.featureFlags.currentAffairsEnabled,
            isSubscriptionActive = tenantConfig.featureFlags.subscriptionsEnabled,
            isAiDoubtSolverActive = tenantConfig.featureFlags.aiMentorEnabled,
            isScientificCalculatorEnabled = tenantConfig.featureFlags.scientificCalculatorEnabled
        )
        appConfigRepo.updateAppConfig(updatedAppConfig)
    }

    /**
     * Returns the latest initialization result, if completed.
     */
    fun getLastResult(): TenantInitializationResult? = lastResult

    /**
     * Quick accessor for the resolved branding properties.
     */
    fun getActiveBranding(): TenantBranding {
        return try {
            TenantConfigRepository.getInstance()?.getBranding()
                ?: AppConfigRepository.getInstance().getBranding()
        } catch (e: Exception) {
            AppConfigRepository.getInstance().getBranding()
        }
    }

    /**
     * Quick accessor for the resolved UI theme properties.
     */
    fun getActiveTheme(): TenantThemeProperties {
        return try {
            TenantConfigRepository.getInstance()?.getTheme()
                ?: AppConfigRepository.getInstance().getThemeProperties()
        } catch (e: Exception) {
            AppConfigRepository.getInstance().getThemeProperties()
        }
    }

    /**
     * Quick accessor for the resolved feature flags.
     */
    fun getActiveFeatureFlags(): TenantFeatureFlags {
        return try {
            TenantConfigRepository.getInstance()?.getFeatureFlags()
                ?: AppConfigRepository.getInstance().getFeatureFlags()
        } catch (e: Exception) {
            AppConfigRepository.getInstance().getFeatureFlags()
        }
    }

    /**
     * Checks if a specific feature flag is currently enabled.
     */
    fun isFeatureEnabled(flagKey: String, defaultValue: Boolean = true): Boolean {
        return try {
            TenantConfigRepository.getInstance()?.isFeatureEnabled(flagKey, defaultValue)
                ?: AppConfigRepository.getInstance().isFeatureEnabled(flagKey, defaultValue)
        } catch (e: Exception) {
            AppConfigRepository.getInstance().isFeatureEnabled(flagKey, defaultValue)
        }
    }

    /**
     * Checks if a standard TenantFeatureFlag enum is currently enabled.
     */
    fun isFeatureEnabled(flag: TenantFeatureFlag): Boolean {
        return try {
            TenantConfigRepository.getInstance()?.isFeatureEnabled(flag)
                ?: AppConfigRepository.getInstance().isFeatureEnabled(flag)
        } catch (e: Exception) {
            AppConfigRepository.getInstance().isFeatureEnabled(flag)
        }
    }
}
