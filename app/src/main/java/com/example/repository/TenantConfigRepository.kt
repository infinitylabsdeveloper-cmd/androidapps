package com.example.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.model.*
import com.google.gson.Gson
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Network response payload for dynamic tenant configuration from the backend.
 * Captures branding, feature flags, UI theme properties, and metadata.
 */
data class TenantConfigNetworkDto(
    @Json(name = "tenantId") val tenantId: String? = null,
    @Json(name = "appId") val appId: String? = null,
    @Json(name = "appName") val appName: String? = null,
    @Json(name = "appCode") val appCode: String? = null,
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "examCategory") val examCategory: String? = null,
    @Json(name = "medium") val medium: String? = null,

    // Dynamic Branding Properties
    @Json(name = "logoUrl") val logoUrl: String? = null,
    @Json(name = "iconUrl") val iconUrl: String? = null,
    @Json(name = "splashLogoUrl") val splashLogoUrl: String? = null,
    @Json(name = "splashBackgroundUrl") val splashBackgroundUrl: String? = null,
    @Json(name = "watermarkText") val watermarkText: String? = null,
    @Json(name = "supportEmail") val supportEmail: String? = null,
    @Json(name = "supportPhone") val supportPhone: String? = null,
    @Json(name = "whatsappNumber") val whatsappNumber: String? = null,
    @Json(name = "whatsappLink") val whatsappLink: String? = null,
    @Json(name = "telegramId") val telegramId: String? = null,
    @Json(name = "telegramLink") val telegramLink: String? = null,
    @Json(name = "websiteUrl") val websiteUrl: String? = null,
    @Json(name = "copyrightText") val copyrightText: String? = null,

    // Dynamic UI Theme Properties
    @Json(name = "primaryColorHex") val primaryColorHex: Long? = null,
    @Json(name = "secondaryColorHex") val secondaryColorHex: Long? = null,
    @Json(name = "accentColorHex") val accentColorHex: Long? = null,
    @Json(name = "backgroundColorHex") val backgroundColorHex: Long? = null,
    @Json(name = "surfaceColorHex") val surfaceColorHex: Long? = null,
    @Json(name = "cardCornerRadiusDp") val cardCornerRadiusDp: Int? = null,
    @Json(name = "buttonCornerRadiusDp") val buttonCornerRadiusDp: Int? = null,
    @Json(name = "chipCornerRadiusDp") val chipCornerRadiusDp: Int? = null,
    @Json(name = "fontPreset") val fontPreset: String? = null,
    @Json(name = "fontScaleRatio") val fontScaleRatio: Float? = null,
    @Json(name = "isDarkModePreferred") val isDarkModePreferred: Boolean? = null,

    // Dynamic Feature Flags
    @Json(name = "mcqsEnabled") val mcqsEnabled: Boolean? = null,
    @Json(name = "mockTestsEnabled") val mockTestsEnabled: Boolean? = null,
    @Json(name = "currentAffairsEnabled") val currentAffairsEnabled: Boolean? = null,
    @Json(name = "leaderboardEnabled") val leaderboardEnabled: Boolean? = null,
    @Json(name = "subscriptionsEnabled") val subscriptionsEnabled: Boolean? = null,
    @Json(name = "paymentsEnabled") val paymentsEnabled: Boolean? = null,
    @Json(name = "notificationsEnabled") val notificationsEnabled: Boolean? = null,
    @Json(name = "offlineDownloadsEnabled") val offlineDownloadsEnabled: Boolean? = null,
    @Json(name = "aiMentorEnabled") val aiMentorEnabled: Boolean? = null,
    @Json(name = "scientificCalculatorEnabled") val scientificCalculatorEnabled: Boolean? = null,
    @Json(name = "savedQuestionsEnabled") val savedQuestionsEnabled: Boolean? = null,
    @Json(name = "studyMaterialsEnabled") val studyMaterialsEnabled: Boolean? = null,
    @Json(name = "dailyQuizEnabled") val dailyQuizEnabled: Boolean? = null,
    @Json(name = "customFlags") val customFlags: Map<String, Boolean>? = null,

    // Metadata & Versioning
    @Json(name = "minSupportedVersionCode") val minSupportedVersionCode: Int? = null,
    @Json(name = "forceUpdate") val forceUpdate: Boolean? = null,
    @Json(name = "maintenanceMode") val maintenanceMode: Boolean? = null,
    @Json(name = "maintenanceMessage") val maintenanceMessage: String? = null,
    @Json(name = "razorpayKeyId") val razorpayKeyId: String? = null,
    @Json(name = "timestamp") val timestamp: Long? = null
)

/**
 * Retrofit API interface for fetching centralized tenant configurations.
 */
interface TenantConfigApiService {

    @GET("api/v1/tenants/{tenantId}/config")
    suspend fun getTenantConfiguration(
        @Path("tenantId") tenantId: String,
        @Query("versionCode") versionCode: Int = 1,
        @Header("X-Tenant-ID") headerTenantId: String = tenantId
    ): TenantConfigNetworkDto

    @GET("api/apps/{tenantId}/configuration")
    suspend fun getAppConfigurationFallback(
        @Path("tenantId") tenantId: String,
        @Query("versionCode") versionCode: Int = 1,
        @Header("X-Tenant-ID") headerTenantId: String = tenantId
    ): TenantConfigNetworkDto
}

/**
 * Domain representation of Tenant Configuration used throughout the application.
 */
data class TenantConfig(
    val tenantId: String,
    val appId: String,
    val packageName: String,
    val branding: TenantBranding,
    val featureFlags: TenantFeatureFlags,
    val theme: TenantThemeProperties,
    val examCategory: String,
    val isMaintenanceMode: Boolean = false,
    val maintenanceMessage: String = "",
    val razorpayKeyId: String = "",
    val isCached: Boolean = false,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun createDefault(
            tenantId: String = BuildConfig.TENANT_ID,
            appId: String = BuildConfig.APP_ID
        ): TenantConfig {
            val appModel = com.example.data.SampleData.appsListState.find { it.id == appId }
            val appConfig = if (appModel != null) {
                AppConfiguration.fromTenantModel(appModel)
            } else {
                AppConfiguration.fromPackageOrAppId(appId)
            }
            val centralized = CentralizedTenantConfig.fromAppConfiguration(appConfig)

            return TenantConfig(
                tenantId = tenantId,
                appId = appId,
                packageName = BuildConfig.APPLICATION_ID,
                branding = centralized.branding,
                featureFlags = centralized.featureFlags,
                theme = centralized.themeProperties,
                examCategory = appConfig.examCategory,
                isMaintenanceMode = false,
                maintenanceMessage = "",
                razorpayKeyId = "",
                isCached = false,
                lastSyncTimestamp = System.currentTimeMillis()
            )
        }
    }
}

/**
 * TenantConfigRepository:
 * Fetches and caches the dynamic configuration (branding, feature flags, theme)
 * from the backend based on [BuildConfig.TENANT_ID] using Retrofit.
 *
 * Provides:
 * 1. Zero-latency instant startup via persistent local disk cache (SharedPreferences/JSON).
 * 2. Background asynchronous Retrofit fetching from central backend.
 * 3. Graceful fallback to bundled presets when offline.
 * 4. Reactive StateFlow for Jetpack Compose UI updates.
 */
class TenantConfigRepository private constructor(
    private val context: Context
) {
    private val tag = "TenantConfigRepository"
    private val gson = Gson()
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // In-memory reactive state
    private val _configState = MutableStateFlow(loadInitialConfig())
    val configState: StateFlow<TenantConfig> = _configState.asStateFlow()

    // Retrofit API Service
    private val apiService: TenantConfigApiService by lazy {
        createRetrofitService()
    }

    init {
        Log.i(tag, "TenantConfigRepository initialized for TENANT_ID: ${BuildConfig.TENANT_ID}")
    }

    /**
     * Retrieves the current snapshot of active tenant configuration.
     */
    fun getCurrentConfig(): TenantConfig = _configState.value

    /**
     * Accessor for active tenant branding properties (appName, logo, tagline, support links).
     */
    fun getBranding(): TenantBranding = _configState.value.branding

    /**
     * Accessor for active tenant feature flags (MCQs, Mock Tests, AI Mentor, etc.).
     */
    fun getFeatureFlags(): TenantFeatureFlags = _configState.value.featureFlags

    /**
     * Accessor for active tenant theme properties (Primary, Secondary, Accent, Radii).
     */
    fun getTheme(): TenantThemeProperties = _configState.value.theme

    /**
     * Quick check if a feature flag is enabled for the active tenant.
     */
    fun isFeatureEnabled(flagKey: String, defaultValue: Boolean = true): Boolean {
        return _configState.value.featureFlags.isEnabled(flagKey, defaultValue)
    }

    /**
     * Quick check using TenantFeatureFlag enum.
     */
    fun isFeatureEnabled(flag: TenantFeatureFlag): Boolean {
        return _configState.value.featureFlags.isEnabled(flag)
    }

    /**
     * Fetches dynamic configuration from the backend using Retrofit based on [BuildConfig.TENANT_ID]
     * (or specified tenant ID) and caches the result locally.
     *
     * @param tenantId The tenant ID to fetch configuration for. Defaults to [BuildConfig.TENANT_ID].
     * @param forceRefresh If true, forces a network query even if cache is fresh.
     */
    suspend fun fetchAndCacheConfig(
        tenantId: String = BuildConfig.TENANT_ID,
        forceRefresh: Boolean = false
    ): Result<TenantConfig> = withContext(Dispatchers.IO) {
        try {
            Log.d(tag, "Fetching remote configuration via Retrofit for Tenant: $tenantId (force=$forceRefresh)")

            val dto = try {
                apiService.getTenantConfiguration(tenantId = tenantId)
            } catch (e: Exception) {
                Log.w(tag, "Primary endpoint returned ${e.message}, attempting fallback endpoint...")
                apiService.getAppConfigurationFallback(tenantId = tenantId)
            }

            val domainConfig = mapDtoToDomain(dto, tenantId)

            // Cache successfully fetched configuration to persistent storage
            saveConfigToCache(domainConfig)

            // Update in-memory reactive flow
            _configState.value = domainConfig

            // Synchronize with AppConfigRepository for backward compatibility
            try {
                AppConfigRepository.getInstance().updateTenantConfig(
                    CentralizedTenantConfig(
                        appId = domainConfig.appId,
                        tenantId = domainConfig.tenantId,
                        packageName = domainConfig.packageName,
                        examCategory = domainConfig.examCategory,
                        branding = domainConfig.branding,
                        featureFlags = domainConfig.featureFlags,
                        themeProperties = domainConfig.theme,
                        maintenanceMode = domainConfig.isMaintenanceMode,
                        maintenanceMessage = domainConfig.maintenanceMessage,
                        razorpayKeyId = domainConfig.razorpayKeyId
                    )
                )
            } catch (syncEx: Exception) {
                Log.w(tag, "Notice syncing to AppConfigRepository: ${syncEx.message}")
            }

            Log.i(tag, "Successfully fetched and cached configuration for Tenant: $tenantId (${domainConfig.branding.appName})")
            Result.success(domainConfig)
        } catch (e: Exception) {
            Log.w(tag, "Retrofit fetch failed for Tenant: $tenantId (${e.message}). Falling back to cached or default configuration.")
            val cachedOrFallback = _configState.value
            Result.failure(e)
        }
    }

    /**
     * Loads the initial configuration upon startup:
     * 1. Reads disk cache first for instant zero-latency UI rendering.
     * 2. Falls back to default preset derived from BuildConfig.TENANT_ID and BuildConfig.APP_ID.
     */
    private fun loadInitialConfig(): TenantConfig {
        val cachedJson = prefs.getString(KEY_CACHED_CONFIG_JSON, null)
        val cachedTenantId = prefs.getString(KEY_CACHED_TENANT_ID, null)

        if (!cachedJson.isNullOrEmpty() && cachedTenantId == BuildConfig.TENANT_ID) {
            try {
                val cachedConfig = gson.fromJson(cachedJson, TenantConfig::class.java)
                if (cachedConfig != null) {
                    Log.d(tag, "Loaded tenant config from local persistent cache for ${cachedConfig.tenantId}")
                    return cachedConfig.copy(isCached = true)
                }
            } catch (e: Exception) {
                Log.w(tag, "Error parsing cached tenant config: ${e.message}")
            }
        }

        // Default configuration
        return TenantConfig.createDefault()
    }

    /**
     * Persists the given configuration to SharedPreferences cache.
     */
    private fun saveConfigToCache(config: TenantConfig) {
        try {
            val json = gson.toJson(config)
            prefs.edit()
                .putString(KEY_CACHED_CONFIG_JSON, json)
                .putString(KEY_CACHED_TENANT_ID, config.tenantId)
                .putLong(KEY_LAST_SYNC_TIMESTAMP, System.currentTimeMillis())
                .apply()
            Log.d(tag, "Saved configuration for ${config.tenantId} to persistent cache")
        } catch (e: Exception) {
            Log.e(tag, "Failed to save configuration to cache: ${e.message}")
        }
    }

    /**
     * Clears cached tenant configuration (e.g. for testing or logout).
     */
    fun clearCache() {
        prefs.edit()
            .remove(KEY_CACHED_CONFIG_JSON)
            .remove(KEY_CACHED_TENANT_ID)
            .remove(KEY_LAST_SYNC_TIMESTAMP)
            .apply()
        _configState.value = TenantConfig.createDefault()
        Log.i(tag, "Persistent tenant config cache cleared.")
    }

    /**
     * Maps the network DTO to clean domain TenantConfig model.
     */
    private fun mapDtoToDomain(dto: TenantConfigNetworkDto, fallbackTenantId: String): TenantConfig {
        val default = TenantConfig.createDefault()

        val branding = TenantBranding(
            appName = dto.appName ?: default.branding.appName,
            appCode = dto.appCode ?: default.branding.appCode,
            shortName = (dto.appName ?: default.branding.appName).take(12),
            tagline = dto.tagline ?: default.branding.tagline,
            description = dto.description ?: default.branding.description,
            logoUrl = dto.logoUrl ?: default.branding.logoUrl,
            appIconUrl = dto.iconUrl ?: default.branding.appIconUrl,
            splashLogoUrl = dto.splashLogoUrl ?: default.branding.splashLogoUrl,
            splashBackgroundUrl = dto.splashBackgroundUrl ?: default.branding.splashBackgroundUrl,
            watermarkText = dto.watermarkText ?: default.branding.watermarkText,
            supportEmail = dto.supportEmail ?: default.branding.supportEmail,
            supportPhone = dto.supportPhone ?: default.branding.supportPhone,
            whatsappNumber = dto.whatsappNumber ?: default.branding.whatsappNumber,
            whatsappLink = dto.whatsappLink ?: default.branding.whatsappLink,
            telegramId = dto.telegramId ?: default.branding.telegramId,
            telegramLink = dto.telegramLink ?: default.branding.telegramLink,
            websiteUrl = dto.websiteUrl ?: default.branding.websiteUrl,
            copyrightText = dto.copyrightText ?: default.branding.copyrightText
        )

        val featureFlags = TenantFeatureFlags(
            mcqsEnabled = dto.mcqsEnabled ?: default.featureFlags.mcqsEnabled,
            mockTestsEnabled = dto.mockTestsEnabled ?: default.featureFlags.mockTestsEnabled,
            currentAffairsEnabled = dto.currentAffairsEnabled ?: default.featureFlags.currentAffairsEnabled,
            leaderboardEnabled = dto.leaderboardEnabled ?: default.featureFlags.leaderboardEnabled,
            subscriptionsEnabled = dto.subscriptionsEnabled ?: default.featureFlags.subscriptionsEnabled,
            paymentsEnabled = dto.paymentsEnabled ?: default.featureFlags.paymentsEnabled,
            notificationsEnabled = dto.notificationsEnabled ?: default.featureFlags.notificationsEnabled,
            offlineDownloadsEnabled = dto.offlineDownloadsEnabled ?: default.featureFlags.offlineDownloadsEnabled,
            aiMentorEnabled = dto.aiMentorEnabled ?: default.featureFlags.aiMentorEnabled,
            scientificCalculatorEnabled = dto.scientificCalculatorEnabled ?: default.featureFlags.scientificCalculatorEnabled,
            savedQuestionsEnabled = dto.savedQuestionsEnabled ?: default.featureFlags.savedQuestionsEnabled,
            studyMaterialsEnabled = dto.studyMaterialsEnabled ?: default.featureFlags.studyMaterialsEnabled,
            dailyQuizEnabled = dto.dailyQuizEnabled ?: default.featureFlags.dailyQuizEnabled,
            customFlags = dto.customFlags ?: default.featureFlags.customFlags
        )

        val theme = TenantThemeProperties(
            primaryColorHex = dto.primaryColorHex ?: default.theme.primaryColorHex,
            secondaryColorHex = dto.secondaryColorHex ?: default.theme.secondaryColorHex,
            accentColorHex = dto.accentColorHex ?: default.theme.accentColorHex,
            backgroundColorHex = dto.backgroundColorHex ?: default.theme.backgroundColorHex,
            surfaceColorHex = dto.surfaceColorHex ?: default.theme.surfaceColorHex,
            cardCornerRadiusDp = dto.cardCornerRadiusDp ?: default.theme.cardCornerRadiusDp,
            buttonCornerRadiusDp = dto.buttonCornerRadiusDp ?: default.theme.buttonCornerRadiusDp,
            chipCornerRadiusDp = dto.chipCornerRadiusDp ?: default.theme.chipCornerRadiusDp,
            fontPreset = dto.fontPreset ?: default.theme.fontPreset,
            fontScaleRatio = dto.fontScaleRatio ?: default.theme.fontScaleRatio,
            isDarkModePreferred = dto.isDarkModePreferred ?: default.theme.isDarkModePreferred
        )

        return TenantConfig(
            tenantId = dto.tenantId ?: fallbackTenantId,
            appId = dto.appId ?: BuildConfig.APP_ID,
            packageName = BuildConfig.APPLICATION_ID,
            branding = branding,
            featureFlags = featureFlags,
            theme = theme,
            examCategory = dto.examCategory ?: default.examCategory,
            isMaintenanceMode = dto.maintenanceMode ?: false,
            maintenanceMessage = dto.maintenanceMessage ?: "",
            razorpayKeyId = dto.razorpayKeyId ?: "",
            isCached = true,
            lastSyncTimestamp = System.currentTimeMillis()
        )
    }

    /**
     * Builds and configures the Retrofit instance with multi-tenant isolation headers.
     */
    private fun createRetrofitService(): TenantConfigApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
        }

        val tenantInterceptor = Interceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("X-Tenant-ID", BuildConfig.TENANT_ID)
                .header("X-App-ID", BuildConfig.APP_ID)
                .header("Accept", "application/json")
                .header("User-Agent", "MultiTenantExamApp/${BuildConfig.APP_VERSION_NAME} Android")
                .build()
            chain.proceed(request)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(tenantInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(DEFAULT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TenantConfigApiService::class.java)
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://api.aspirantportal.com/"
        private const val PREFS_NAME = "tenant_dynamic_config_cache"
        private const val KEY_CACHED_CONFIG_JSON = "key_cached_tenant_config_json"
        private const val KEY_CACHED_TENANT_ID = "key_cached_tenant_id"
        private const val KEY_LAST_SYNC_TIMESTAMP = "key_last_sync_timestamp"

        @Volatile
        private var INSTANCE: TenantConfigRepository? = null

        fun getInstance(context: Context): TenantConfigRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TenantConfigRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun getInstance(): TenantConfigRepository? {
            return INSTANCE
        }
    }
}
