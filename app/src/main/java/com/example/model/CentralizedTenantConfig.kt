package com.example.model

/**
 * Data structure holding centralized branding properties for a tenant app.
 */
data class TenantBranding(
    val appName: String,
    val appCode: String = "",
    val shortName: String = "",
    val tagline: String = "",
    val description: String = "",
    val logoUrl: String = "",
    val appIconUrl: String = "",
    val splashLogoUrl: String = "",
    val splashBackgroundUrl: String = "",
    val watermarkText: String = "",
    val supportEmail: String = "sobhadhanaraju0@gmail.com",
    val supportPhone: String = "+91 94935 12768",
    val whatsappNumber: String = "+91 94935 12768",
    val whatsappLink: String = "https://wa.me/919493512768",
    val telegramId: String = "@DHANARAJU0",
    val telegramLink: String = "https://t.me/DHANARAJU0",
    val websiteUrl: String = "",
    val copyrightText: String = ""
)

/**
 * Data structure holding centralized feature flags for a tenant app.
 * Allows remote toggle of functional modules without rebuilding the app.
 */
data class TenantFeatureFlags(
    val mcqsEnabled: Boolean = true,
    val mockTestsEnabled: Boolean = true,
    val currentAffairsEnabled: Boolean = true,
    val leaderboardEnabled: Boolean = true,
    val subscriptionsEnabled: Boolean = true,
    val paymentsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val offlineDownloadsEnabled: Boolean = true,
    val aiMentorEnabled: Boolean = true,
    val scientificCalculatorEnabled: Boolean = true,
    val savedQuestionsEnabled: Boolean = true,
    val studyMaterialsEnabled: Boolean = true,
    val dailyQuizEnabled: Boolean = true,
    val customFlags: Map<String, Boolean> = emptyMap()
) {
    fun isEnabled(flagKey: String, defaultValue: Boolean = true): Boolean {
        return customFlags[flagKey] ?: when (flagKey) {
            "mcqsEnabled", "mcqs" -> mcqsEnabled
            "mockTestsEnabled", "mock_tests" -> mockTestsEnabled
            "currentAffairsEnabled", "current_affairs" -> currentAffairsEnabled
            "leaderboardEnabled", "leaderboard" -> leaderboardEnabled
            "subscriptionsEnabled", "subscriptions" -> subscriptionsEnabled
            "paymentsEnabled", "payments" -> paymentsEnabled
            "notificationsEnabled", "notifications" -> notificationsEnabled
            "offlineDownloadsEnabled", "offline_downloads" -> offlineDownloadsEnabled
            "aiMentorEnabled", "ai_mentor" -> aiMentorEnabled
            "scientificCalculatorEnabled", "scientific_calculator" -> scientificCalculatorEnabled
            "savedQuestionsEnabled", "saved_questions" -> savedQuestionsEnabled
            "studyMaterialsEnabled", "study_materials" -> studyMaterialsEnabled
            "dailyQuizEnabled", "daily_quiz" -> dailyQuizEnabled
            else -> defaultValue
        }
    }

    fun isEnabled(flag: TenantFeatureFlag): Boolean {
        return isEnabled(flag.key, flag.defaultEnabled)
    }
}

/**
 * Data structure holding centralized UI theme properties for a tenant app.
 */
data class TenantThemeProperties(
    val primaryColorHex: Long = 0xFF2563EB,
    val secondaryColorHex: Long = 0xFF1D4ED8,
    val accentColorHex: Long = 0xFFF59E0B,
    val backgroundColorHex: Long = 0xFF080C18,
    val surfaceColorHex: Long = 0xFFFFFFFF,
    val cardBackgroundColorHex: Long = 0xFFFFFFFF,
    val textPrimaryColorHex: Long = 0xFF0F172A,
    val textSecondaryColorHex: Long = 0xFF64748B,
    val cardCornerRadiusDp: Int = 14,
    val buttonCornerRadiusDp: Int = 10,
    val chipCornerRadiusDp: Int = 16,
    val fontPreset: String = "Modern",
    val fontScaleRatio: Float = 1.0f,
    val isDarkModePreferred: Boolean = false
) {
    val primaryColor: androidx.compose.ui.graphics.Color get() = androidx.compose.ui.graphics.Color(primaryColorHex)
    val secondaryColor: androidx.compose.ui.graphics.Color get() = androidx.compose.ui.graphics.Color(secondaryColorHex)
    val accentColor: androidx.compose.ui.graphics.Color get() = androidx.compose.ui.graphics.Color(accentColorHex)
    val surfaceColor: androidx.compose.ui.graphics.Color get() = androidx.compose.ui.graphics.Color(surfaceColorHex)
    val backgroundColor: androidx.compose.ui.graphics.Color get() = androidx.compose.ui.graphics.Color(backgroundColorHex)
}

/**
 * Centralized Tenant Configuration payload matching the backend API:
 * GET /api/apps/{appId}/configuration
 * Encapsulates branding, feature flags, UI theme properties, and runtime parameters.
 */
data class CentralizedTenantConfig(
    val appId: String,
    val tenantId: String = appId,
    val packageName: String,
    val examCategory: String = "",
    val medium: String = "English",
    val allowedLanguages: List<String> = listOf("English", "Telugu"),
    val minSupportedVersionCode: Int = 1,
    val forceUpdate: Boolean = false,
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "",
    val apiBaseUrl: String = "https://api.aspirantportal.com",
    val razorpayKeyId: String = com.example.BuildConfig.RAZORPAY_KEY_ID, // Client-safe public key only
    val branding: TenantBranding,
    val featureFlags: TenantFeatureFlags = TenantFeatureFlags(),
    val themeProperties: TenantThemeProperties = TenantThemeProperties(),
    val lastFetchedTimestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromAppConfiguration(config: AppConfiguration): CentralizedTenantConfig {
            return CentralizedTenantConfig(
                appId = config.appId,
                tenantId = config.appId,
                packageName = config.packageName,
                examCategory = config.examCategory,
                medium = if (config.allowedLanguages.contains("Telugu")) "Telugu / English" else "English",
                allowedLanguages = config.allowedLanguages,
                apiBaseUrl = config.apiBaseUrl,
                branding = TenantBranding(
                    appName = config.appName,
                    appCode = config.appCode,
                    shortName = config.appName.take(12),
                    tagline = config.tagline,
                    description = config.description,
                    logoUrl = "",
                    appIconUrl = "",
                    splashLogoUrl = "",
                    splashBackgroundUrl = "",
                    watermarkText = config.watermarkText,
                    supportEmail = config.supportEmail,
                    supportPhone = config.supportPhone,
                    whatsappNumber = config.whatsappNumber,
                    whatsappLink = config.whatsappLink,
                    telegramId = config.telegramId,
                    telegramLink = config.telegramLink,
                    websiteUrl = "https://aspirantportal.com",
                    copyrightText = "© 2026 Aspirant Portal. All rights reserved."
                ),
                featureFlags = TenantFeatureFlags(
                    mcqsEnabled = config.featuresEnabled.contains("MCQs"),
                    mockTestsEnabled = config.isMockTestActive,
                    currentAffairsEnabled = config.isCurrentAffairsActive,
                    leaderboardEnabled = true,
                    subscriptionsEnabled = config.isSubscriptionActive,
                    paymentsEnabled = true,
                    notificationsEnabled = config.isNotificationsEnabled,
                    offlineDownloadsEnabled = true,
                    aiMentorEnabled = config.isAiDoubtSolverActive,
                    scientificCalculatorEnabled = config.isScientificCalculatorEnabled
                ),
                themeProperties = TenantThemeProperties(
                    primaryColorHex = config.primaryColorHex,
                    secondaryColorHex = config.secondaryColorHex,
                    accentColorHex = config.accentColorHex,
                    surfaceColorHex = config.surfaceColorHex,
                    backgroundColorHex = config.backgroundColorHex,
                    cardCornerRadiusDp = config.cardCornerRadiusDp,
                    buttonCornerRadiusDp = config.buttonCornerRadiusDp,
                    chipCornerRadiusDp = config.chipCornerRadiusDp,
                    fontPreset = config.fontPreset.name,
                    fontScaleRatio = config.fontScaleRatio
                )
            )
        }
    }
}
