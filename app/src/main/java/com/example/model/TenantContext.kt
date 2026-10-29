package com.example.model

/**
 * Core tenant identification and runtime context for the 662+ multi-app platform.
 */
data class TenantContext(
    val appId: String,
    val tenantId: String,
    val appName: String,
    val packageName: String,
    val apiBaseUrl: String = "https://api.aspirantportal.com",
    val isStandaloneBuild: Boolean = true
) {
    companion object {
        val DEFAULT = TenantContext(
            appId = "andhra-pradesh-appsc-group-2",
            tenantId = "APP001",
            appName = "APPSC Group-2",
            packageName = "com.aistudio.appscprep.fhdqkx",
            isStandaloneBuild = false
        )
    }
}

/**
 * Standardized feature flags across all 662+ tenant applications.
 * Can be dynamically toggled per app from the Central Admin Panel or remote configuration.
 */
enum class TenantFeatureFlag(val key: String, val displayName: String, val defaultEnabled: Boolean) {
    MCQS("mcqsEnabled", "MCQ Practice Arena", true),
    MOCK_TESTS("mockTestsEnabled", "Full-Length Mock Tests", true),
    CURRENT_AFFAIRS("currentAffairsEnabled", "Daily & Monthly Current Affairs", true),
    LEADERBOARD("leaderboardEnabled", "Competitive Leaderboard", true),
    SUBSCRIPTIONS("subscriptionsEnabled", "Subscription & Paywall", true),
    PAYMENTS("paymentsEnabled", "Razorpay / Google Play Billing", true),
    NOTIFICATIONS("notificationsEnabled", "Local & Push Notifications", true),
    OFFLINE_DOWNLOADS("offlineDownloadsEnabled", "Offline Question Banks", true),
    AI_MENTOR("aiMentorEnabled", "AI Doubt Resolution", true),
    SCIENTIFIC_CALCULATOR("scientificCalculatorEnabled", "Scientific Calculator", true);

    companion object {
        fun fromKey(key: String): TenantFeatureFlag? = entries.find { it.key.equals(key, ignoreCase = true) }
    }
}

/**
 * Remote Tenant Configuration payload matching the centralized backend API:
 * GET /api/apps/{appId}/configuration
 */
data class RemoteTenantConfiguration(
    val appId: String,
    val tenantId: String = appId,
    val appName: String,
    val appCode: String = "",
    val examCategory: String = "",
    val description: String = "",
    val logoUrl: String = "",
    val iconUrl: String = "",
    val splashLogoUrl: String = "",
    val primaryColorHex: Long = 0xFF2563EB,
    val secondaryColorHex: Long = 0xFF1D4ED8,
    val accentColorHex: Long = 0xFFF59E0B,
    val backgroundColorHex: Long = 0xFF080C18,
    val surfaceColorHex: Long = 0xFFFFFFFF,
    val medium: String = "English",
    val languageCode: String = "en",
    val allowedLanguages: List<String> = listOf("English"),
    val minSupportedVersionCode: Int = 1,
    val forceUpdate: Boolean = false,
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "",
    val featureFlags: Map<String, Boolean> = emptyMap(),
    val razorpayKeyId: String = "",
    val supportEmail: String = "sobhadhanaraju0@gmail.com",
    val supportPhone: String = "+91 94935 12768",
    val whatsappNumber: String = "+91 94935 12768",
    val telegramLink: String = "https://t.me/DHANARAJU0",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun isFeatureEnabled(flag: TenantFeatureFlag): Boolean {
        return featureFlags[flag.key] ?: flag.defaultEnabled
    }
}
