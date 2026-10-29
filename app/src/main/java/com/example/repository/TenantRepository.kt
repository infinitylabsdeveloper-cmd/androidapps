package com.example.repository

import android.content.Context
import com.example.model.RemoteTenantConfiguration
import com.example.model.TenantContext
import com.example.model.TenantFeatureFlag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * OkHttp Network Interceptor that strictly enforces Multi-Tenant Data Isolation.
 * Automatically injects X-App-ID and X-Tenant-ID headers into every network request.
 */
class TenantHeaderInterceptor(
    private val tenantContextProvider: () -> String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val currentAppId = tenantContextProvider()
        val originalRequest = chain.request()
        val authenticatedRequest = originalRequest.newBuilder()
            .header("X-App-ID", currentAppId)
            .header("X-Tenant-ID", currentAppId)
            .header("Accept", "application/json")
            .build()
        return chain.proceed(authenticatedRequest)
    }
}

/**
 * Retrofit API Service for centralized backend communication:
 * GET /api/apps/{appId}/configuration
 */
interface CentralTenantApiService {
    @GET("api/apps/{appId}/configuration")
    suspend fun getTenantConfiguration(
        @Path("appId") appId: String,
        @Query("versionCode") versionCode: Int = 1
    ): RemoteTenantConfiguration
}

/**
 * Centralized Repository for Tenant Configuration, Isolation, and Dynamic Feature Flags.
 * Acts as the single source of truth for app branding, identity, and features.
 */
class TenantRepository private constructor(
    private val context: Context
) {
    private val _currentTenantContext = MutableStateFlow(TenantContext.DEFAULT)
    val currentTenantContext: StateFlow<TenantContext> = _currentTenantContext.asStateFlow()

    private val _remoteConfig = MutableStateFlow<RemoteTenantConfiguration?>(null)
    val remoteConfig: StateFlow<RemoteTenantConfiguration?> = _remoteConfig.asStateFlow()

    fun initialize(appId: String, tenantId: String, appName: String, packageName: String) {
        _currentTenantContext.value = TenantContext(
            appId = appId,
            tenantId = tenantId,
            appName = appName,
            packageName = packageName
        )
    }

    fun isFeatureEnabled(flag: TenantFeatureFlag): Boolean {
        return _remoteConfig.value?.isFeatureEnabled(flag) ?: flag.defaultEnabled
    }

    fun updateRemoteConfig(config: RemoteTenantConfiguration) {
        _remoteConfig.value = config
    }

    companion object {
        @Volatile
        private var INSTANCE: TenantRepository? = null

        fun getInstance(context: Context): TenantRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TenantRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
