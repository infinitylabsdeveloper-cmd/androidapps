package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.BuildConfig

/**
 * Dynamic Branding data model representing all visual identity assets,
 * colors, typography, and support links fetched from the central backend.
 */
data class AppBranding(
    val appName: String,
    val appCode: String = "",
    val shortName: String = "",
    val tagline: String = "",
    val description: String = "",
    val logoUrl: String = "",
    val iconUrl: String = "",
    val splashLogoUrl: String = "",
    val splashBackgroundUrl: String = "",
    val watermarkText: String = "",
    val primaryColorHex: Long = 0xFF2563EB,
    val secondaryColorHex: Long = 0xFF1D4ED8,
    val accentColorHex: Long = 0xFFF59E0B,
    val backgroundColorHex: Long = 0xFF080C18,
    val surfaceColorHex: Long = 0xFFFFFFFF,
    val cardCornerRadiusDp: Int = 14,
    val buttonCornerRadiusDp: Int = 10,
    val chipCornerRadiusDp: Int = 16,
    val fontPreset: String = "Modern",
    val supportEmail: String = "sobhadhanaraju0@gmail.com",
    val supportPhone: String = "+91 94935 12768",
    val whatsappNumber: String = "+91 94935 12768",
    val whatsappLink: String = "https://wa.me/919493512768",
    val telegramId: String = "@DHANARAJU0",
    val telegramLink: String = "https://t.me/DHANARAJU0",
    val websiteUrl: String = "",
    val copyrightText: String = ""
) {
    val primaryColor: Color get() = Color(primaryColorHex)
    val secondaryColor: Color get() = Color(secondaryColorHex)
    val accentColor: Color get() = Color(accentColorHex)
    val backgroundColor: Color get() = Color(backgroundColorHex)
    val surfaceColor: Color get() = Color(surfaceColorHex)
}

/**
 * Centralized Feature Flags data model allowing dynamic activation/deactivation
 * of individual functional modules per tenant without rebuilding the app.
 */
data class AppFeatureFlags(
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
 * Master AppConfig data model returned by the centralized backend API:
 * GET /api/apps/{appId}/configuration
 *
 * Encapsulates dynamic branding, feature flags, multi-tenant identity,
 * maintenance mode, and app versioning parameters.
 */
data class AppConfig(
    val appId: String,
    val tenantId: String = appId,
    val packageName: String,
    val category: String = "",
    val medium: String = "English",
    val allowedLanguages: List<String> = listOf("English", "Telugu"),
    val versionCode: Int = 1,
    val minSupportedVersionCode: Int = 1,
    val forceUpdate: Boolean = false,
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "",
    val apiBaseUrl: String = "https://api.aspirantportal.com",
    val razorpayKeyId: String = "", // Client-safe public key only (no secret!)
    val branding: AppBranding,
    val featureFlags: AppFeatureFlags = AppFeatureFlags(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun isFeatureEnabled(flagKey: String, defaultValue: Boolean = true): Boolean {
        return featureFlags.isEnabled(flagKey, defaultValue)
    }

    fun isFeatureEnabled(flag: TenantFeatureFlag): Boolean {
        return featureFlags.isEnabled(flag)
    }

    val primaryColor: Color get() = branding.primaryColor
    val secondaryColor: Color get() = branding.secondaryColor
    val accentColor: Color get() = branding.accentColor
    val backgroundColor: Color get() = branding.backgroundColor
    val surfaceColor: Color get() = branding.surfaceColor

    companion object {
        /**
         * Creates an AppConfig instance derived from local AppConfiguration.
         */
        fun fromAppConfiguration(config: AppConfiguration): AppConfig {
            return AppConfig(
                appId = config.appId,
                tenantId = config.appId,
                packageName = config.packageName,
                category = config.examCategory,
                medium = if (config.allowedLanguages.contains("Telugu")) "Telugu / English" else "English",
                allowedLanguages = config.allowedLanguages,
                apiBaseUrl = config.apiBaseUrl,
                branding = AppBranding(
                    appName = config.appName,
                    appCode = config.appCode,
                    shortName = config.appName.take(12),
                    tagline = config.tagline,
                    description = config.description,
                    logoUrl = "",
                    iconUrl = "",
                    splashLogoUrl = "",
                    splashBackgroundUrl = "",
                    watermarkText = config.watermarkText,
                    primaryColorHex = config.primaryColorHex,
                    secondaryColorHex = config.secondaryColorHex,
                    accentColorHex = config.accentColorHex,
                    surfaceColorHex = config.surfaceColorHex,
                    backgroundColorHex = config.backgroundColorHex,
                    cardCornerRadiusDp = config.cardCornerRadiusDp,
                    buttonCornerRadiusDp = config.buttonCornerRadiusDp,
                    chipCornerRadiusDp = config.chipCornerRadiusDp,
                    fontPreset = config.fontPreset.name,
                    supportEmail = config.supportEmail,
                    supportPhone = config.supportPhone,
                    whatsappNumber = config.whatsappNumber,
                    whatsappLink = config.whatsappLink,
                    telegramId = config.telegramId,
                    telegramLink = config.telegramLink,
                    websiteUrl = "https://aspirantportal.com",
                    copyrightText = "© 2026 Aspirant Portal. All rights reserved."
                ),
                featureFlags = AppFeatureFlags(
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
                )
            )
        }

        /**
         * Default fallback configuration for initial startup before remote fetch completes.
         */
        val DEFAULT = fromAppConfiguration(
            AppConfiguration.fromPackageOrAppId(
                try {
                    BuildConfig.APPLICATION_ID
                } catch (e: Exception) {
                    "com.aistudio.appscprep"
                }
            )
        )
    }
}
