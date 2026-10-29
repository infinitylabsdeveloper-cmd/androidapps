package com.example.repository

import com.example.BuildConfig
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import android.content.Context
import android.util.Log
import com.example.util.FontSettingsManager

/**
 * Centralized Repository that defines and provides tenant-specific branding,
 * feature flags, UI theme properties, and settings fetched from the backend
 * or local preset based on the current build's package name / App ID.
 */
class AppConfigRepository private constructor() {

    private val tag = "AppConfigRepository"

    // Automatically resolve configuration based on current build package name / application ID
    private val initialConfig: AppConfiguration = AppConfiguration.fromPackageOrAppId(
        try {
            BuildConfig.APPLICATION_ID
        } catch (e: Exception) {
            "com.aistudio.appscprep"
        }
    )

    private val _currentAppConfig = MutableStateFlow(initialConfig)
    val currentAppConfig: StateFlow<AppConfiguration> = _currentAppConfig.asStateFlow()

    // Centralized tenant configuration holding branding, feature flags, and UI theme properties
    private val _tenantConfig = MutableStateFlow(CentralizedTenantConfig.fromAppConfiguration(initialConfig))
    val tenantConfig: StateFlow<CentralizedTenantConfig> = _tenantConfig.asStateFlow()

    init {
        // Sync initial state with CentralAppConfigManager
        CentralAppConfigManager.switchActiveApp(initialConfig.appId)
    }

    /**
     * Initializes configuration with Context to load persistent settings like fonts and preferences.
     */
    fun initializeWithContext(context: Context, packageOrAppId: String = BuildConfig.APPLICATION_ID) {
        val fontSettings = FontSettingsManager.getSettings(context)
        val appearanceSettings = com.example.util.PracticeAppearanceManager.getSettings(context)
        val notifEnabled = com.example.util.NotificationSettingsManager.isNotificationsEnabled(context)
        val soundEnabled = com.example.util.NotificationSettingsManager.isNotificationSoundEnabled(context)
        val vibrationEnabled = com.example.util.NotificationSettingsManager.isNotificationVibrationEnabled(context)
        
        val config = AppConfiguration.fromPackageOrAppId(packageOrAppId).copy(
            advancedFontSettings = fontSettings.copy(practiceAppearance = appearanceSettings),
            isNotificationsEnabled = notifEnabled,
            isNotificationSoundEnabled = soundEnabled,
            isNotificationVibrationEnabled = vibrationEnabled
        )
        _currentAppConfig.value = config
        _tenantConfig.value = CentralizedTenantConfig.fromAppConfiguration(config)
        CentralAppConfigManager.switchActiveApp(config.appId)
    }

    /**
     * Retrieves the current snapshot of the centralized Tenant Configuration (Branding, Flags, Theme).
     */
    fun getCurrentTenantConfig(): CentralizedTenantConfig = _tenantConfig.value

    /**
     * Retrieves the current snapshot of the AppConfiguration (backward compatible).
     */
    fun getCurrentConfig(): AppConfiguration = _currentAppConfig.value

    /**
     * Quick accessor for tenant branding properties (appName, logo, contact, tagline).
     */
    fun getBranding(): TenantBranding = _tenantConfig.value.branding

    /**
     * Quick accessor for tenant feature flags (MCQs, Mock Tests, Subscriptions, AI Mentor, etc.).
     */
    fun getFeatureFlags(): TenantFeatureFlags = _tenantConfig.value.featureFlags

    /**
     * Quick accessor for tenant UI theme properties (Primary, Secondary, Accent, Background, Radii).
     */
    fun getThemeProperties(): TenantThemeProperties = _tenantConfig.value.themeProperties

    /**
     * Checks if a specific feature flag is enabled for the active tenant.
     */
    fun isFeatureEnabled(flagKey: String, defaultValue: Boolean = true): Boolean {
        return _tenantConfig.value.featureFlags.isEnabled(flagKey, defaultValue)
    }

    /**
     * Checks if a standardized TenantFeatureFlag enum is enabled for the active tenant.
     */
    fun isFeatureEnabled(flag: TenantFeatureFlag): Boolean {
        return _tenantConfig.value.featureFlags.isEnabled(flag)
    }

    /**
     * Initializes or re-aligns configuration specifically for a build package name or app ID.
     */
    fun initializeForBuild(packageOrAppId: String = BuildConfig.APPLICATION_ID) {
        val config = AppConfiguration.fromPackageOrAppId(packageOrAppId)
        _currentAppConfig.value = config
        _tenantConfig.value = CentralizedTenantConfig.fromAppConfiguration(config)
        CentralAppConfigManager.switchActiveApp(config.appId)
    }

    /**
     * Dynamically switches the active app tenant (e.g. for multi-exam prep, admin switching).
     */
    fun switchActiveAppTenant(appId: String) {
        // Try to find in dynamic apps list first to support admin-edited metadata
        val appModel = com.example.data.SampleData.appsListState.find { it.id == appId }
        val config = if (appModel != null) {
            AppConfiguration.fromTenantModel(appModel)
        } else {
            AppConfiguration.fromPackageOrAppId(appId)
        }
        _currentAppConfig.value = config
        _tenantConfig.value = CentralizedTenantConfig.fromAppConfiguration(config)
        CentralAppConfigManager.switchActiveApp(appId)
    }

    /**
     * Refreshes the current configuration from the source (e.g. SampleData) 
     * without switching the active app ID. Useful when an app's basic metadata 
     * like name or branding changes at runtime.
     */
    fun refreshCurrentConfig() {
        val currentAppId = _currentAppConfig.value.appId
        val appModel = com.example.data.SampleData.appsListState.find { it.id == currentAppId }
        val updatedConfig = if (appModel != null) {
            AppConfiguration.fromTenantModel(appModel)
        } else {
            AppConfiguration.fromPackageOrAppId(currentAppId)
        }
        
        // Merge with existing session-specific settings (like fonts)
        val mergedConfig = updatedConfig.copy(
            advancedFontSettings = _currentAppConfig.value.advancedFontSettings
        )
        _currentAppConfig.value = mergedConfig
        _tenantConfig.value = CentralizedTenantConfig.fromAppConfiguration(mergedConfig)
    }

    /**
     * Updates and broadcasts a custom modified AppConfiguration.
     */
    fun updateAppConfig(config: AppConfiguration) {
        _currentAppConfig.value = config
        _tenantConfig.value = CentralizedTenantConfig.fromAppConfiguration(config)
    }

    /**
     * Updates and broadcasts a customized CentralizedTenantConfig directly.
     */
    fun updateTenantConfig(config: CentralizedTenantConfig) {
        _tenantConfig.value = config
        // Also keep _currentAppConfig aligned
        val current = _currentAppConfig.value
        _currentAppConfig.value = current.copy(
            appName = config.branding.appName,
            appCode = config.branding.appCode,
            tagline = config.branding.tagline,
            description = config.branding.description,
            supportPhone = config.branding.supportPhone,
            supportEmail = config.branding.supportEmail,
            whatsappNumber = config.branding.whatsappNumber,
            whatsappLink = config.branding.whatsappLink,
            telegramId = config.branding.telegramId,
            telegramLink = config.branding.telegramLink,
            primaryColorHex = config.themeProperties.primaryColorHex,
            secondaryColorHex = config.themeProperties.secondaryColorHex,
            accentColorHex = config.themeProperties.accentColorHex,
            surfaceColorHex = config.themeProperties.surfaceColorHex,
            backgroundColorHex = config.themeProperties.backgroundColorHex,
            cardCornerRadiusDp = config.themeProperties.cardCornerRadiusDp,
            buttonCornerRadiusDp = config.themeProperties.buttonCornerRadiusDp,
            isMockTestActive = config.featureFlags.mockTestsEnabled,
            isCurrentAffairsActive = config.featureFlags.currentAffairsEnabled,
            isSubscriptionActive = config.featureFlags.subscriptionsEnabled,
            isAiDoubtSolverActive = config.featureFlags.aiMentorEnabled,
            isScientificCalculatorEnabled = config.featureFlags.scientificCalculatorEnabled
        )
    }

    /**
     * Simulates or executes fetching centralized tenant configuration from the backend API:
     * GET /api/apps/{appId}/configuration
     * Updates in-memory StateFlow and caches configuration.
     */
    suspend fun fetchRemoteConfiguration(appId: String = BuildConfig.APP_ID): Result<CentralizedTenantConfig> {
        return try {
            // Check dynamic apps catalog or fall back to bundled preset
            val appModel = com.example.data.SampleData.appsListState.find { it.id == appId }
            val resolvedAppConfig = if (appModel != null) {
                AppConfiguration.fromTenantModel(appModel)
            } else {
                AppConfiguration.fromPackageOrAppId(appId)
            }

            val centralizedConfig = CentralizedTenantConfig.fromAppConfiguration(resolvedAppConfig)
            _tenantConfig.value = centralizedConfig
            _currentAppConfig.value = resolvedAppConfig
            Log.d(tag, "Fetched remote centralized configuration for tenant: $appId")
            Result.success(centralizedConfig)
        } catch (e: Exception) {
            Log.w(tag, "Failed to fetch remote config for $appId, using cached/fallback: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Fetches configuration for a specific App ID.
     */
    fun fetchAppConfig(appId: String): Result<AppConfiguration> {
        return try {
            val config = AppConfiguration.fromPackageOrAppId(appId)
            Result.success(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches configuration for a specific package name.
     */
    fun getConfigurationForPackage(packageName: String): AppConfiguration {
        return AppConfiguration.fromPackageOrAppId(packageName)
    }

    /**
     * Retrieves all available pre-configured app configurations.
     */
    fun getAvailableAppConfigs(): List<AppConfiguration> {
        return ExamPreset.values().map { AppConfiguration.fromPreset(it) }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppConfigRepository? = null

        fun getInstance(): AppConfigRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppConfigRepository().also { INSTANCE = it }
            }
        }
    }
}

