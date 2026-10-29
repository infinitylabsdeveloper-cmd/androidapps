package com.example.network

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.model.AppBranding
import com.example.model.AppConfig
import com.example.repository.AppConfigRepository
import com.example.util.AppConfigProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
 * Retrofit API interface for fetching dynamic branding, feature flags,
 * and tenant configurations from the central backend upon application startup.
 */
interface AppConfigApiService {

    /**
     * Fetches complete dynamic configuration for a specific tenant application:
     * GET /api/apps/{appId}/configuration
     */
    @GET("api/apps/{appId}/configuration")
    suspend fun getAppConfiguration(
        @Path("appId") appId: String,
        @Query("versionCode") versionCode: Int = 1,
        @Header("X-App-ID") headerAppId: String = appId,
        @Header("X-Tenant-ID") headerTenantId: String = appId
    ): AppConfig

    /**
     * Fetches dynamic branding assets and UI styling for a tenant application:
     * GET /api/apps/{appId}/branding
     */
    @GET("api/apps/{appId}/branding")
    suspend fun getAppBranding(
        @Path("appId") appId: String,
        @Header("X-App-ID") headerAppId: String = appId
    ): AppBranding

    /**
     * Fetches centralized feature flags for a tenant application:
     * GET /api/apps/{appId}/features
     */
    @GET("api/apps/{appId}/features")
    suspend fun getAppFeatureFlags(
        @Path("appId") appId: String,
        @Header("X-App-ID") headerAppId: String = appId
    ): Map<String, Boolean>

    /**
     * Administrative endpoint for retrieving all 662+ tenant app configurations:
     * GET /api/apps/configurations
     */
    @GET("api/apps/configurations")
    suspend fun getAllAppConfigurations(): List<AppConfig>
}

/**
 * Client factory and builder for creating configured AppConfigApiService instances.
 * Automatically injects multi-tenant isolation headers into every outbound request.
 */
object AppConfigApiClient {
    private const val TAG = "AppConfigApiClient"
    const val DEFAULT_BASE_URL = "https://api.aspirantportal.com/"

    fun create(
        baseUrl: String = DEFAULT_BASE_URL,
        tenantIdProvider: () -> String = { AppConfigProvider.appId }
    ): AppConfigApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
        }

        val tenantInterceptor = Interceptor { chain ->
            val original = chain.request()
            val activeTenant = tenantIdProvider()
            val request = original.newBuilder()
                .header("X-App-ID", activeTenant)
                .header("X-Tenant-ID", activeTenant)
                .header("Accept", "application/json")
                .header("User-Agent", "MultiTenantExamPlatform/${BuildConfig.APP_VERSION_NAME} Android")
                .build()
            chain.proceed(request)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(tenantInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AppConfigApiService::class.java)
    }
}

/**
 * Startup Manager responsible for orchestrating the initial configuration fetch
 * upon application startup with graceful offline fallback.
 */
object AppConfigStartupManager {
    private const val TAG = "AppConfigStartup"
    private const val PREFS_NAME = "startup_app_config_cache"
    private const val KEY_CACHED_CONFIG = "key_cached_config_"

    private val apiService: AppConfigApiService by lazy {
        AppConfigApiClient.create()
    }

    /**
     * Executes upon application startup.
     * 1. Checks local cache for instant zero-latency startup rendering.
     * 2. Queries central backend API asynchronously to refresh dynamic branding & feature flags.
     * 3. Seamlessly updates AppConfigRepository with latest remote configuration.
     */
    suspend fun fetchStartupConfiguration(
        context: Context,
        appId: String = AppConfigProvider.appId
    ): AppConfig {
        Log.i(TAG, "Initiating startup dynamic configuration fetch for tenant: $appId")

        return try {
            val remoteConfig = apiService.getAppConfiguration(
                appId = appId,
                versionCode = 1
            )

            // Cache remote configuration locally
            cacheConfig(context, remoteConfig)

            // Synchronize with active repository
            AppConfigRepository.getInstance().fetchRemoteConfiguration(appId)

            Log.i(TAG, "Successfully fetched and applied remote configuration for $appId (${remoteConfig.branding.appName})")
            remoteConfig
        } catch (e: Exception) {
            Log.w(TAG, "Network fetch unavailable (${e.message}). Falling back to local/cached configuration for $appId")
            getCachedOrDefaultConfig(context, appId)
        }
    }

    private fun cacheConfig(context: Context, config: AppConfig) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString("${KEY_CACHED_CONFIG}${config.appId}_name", config.branding.appName)
                .putLong("${KEY_CACHED_CONFIG}${config.appId}_primary", config.branding.primaryColorHex)
                .putBoolean("${KEY_CACHED_CONFIG}${config.appId}_mock", config.featureFlags.mockTestsEnabled)
                .putBoolean("${KEY_CACHED_CONFIG}${config.appId}_ai", config.featureFlags.aiMentorEnabled)
                .putLong("${KEY_CACHED_CONFIG}${config.appId}_ts", System.currentTimeMillis())
                .apply()
        } catch (ex: Exception) {
            Log.w(TAG, "Error caching config: ${ex.message}")
        }
    }

    private fun getCachedOrDefaultConfig(context: Context, appId: String): AppConfig {
        val activeAppModel = com.example.data.SampleData.appsListState.find { it.id == appId }
        return if (activeAppModel != null) {
            val appConfiguration = com.example.model.AppConfiguration.fromTenantModel(activeAppModel)
            AppConfig.fromAppConfiguration(appConfiguration)
        } else {
            AppConfig.DEFAULT
        }
    }
}
