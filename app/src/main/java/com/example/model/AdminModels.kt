package com.example.model

enum class AppStatus {
    DRAFT, ACTIVE, INACTIVE, MAINTENANCE, ARCHIVED
}

data class AppAuditLogModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val adminId: String,
    val adminName: String,
    val action: String, // "CREATED", "EDITED", "ACTIVATED", "DEACTIVATED", "DUPLICATED", "ARCHIVED", "DELETED"
    val appId: String,
    val appName: String,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class UserAccountStatus {
    ACTIVE, BLOCKED, PENDING
}

enum class SubscriptionSource {
    PAID,
    ADMIN_GRANTED,
    NONE
}

data class AdminUserModel(
    val id: String,
    val appId: String = "appsc-group2",
    val name: String,
    val email: String,
    val phone: String,
    val registeredAt: Long,
    val lastActiveAt: Long,
    val isPremium: Boolean,
    val subscriptionPlan: String?,
    val status: UserAccountStatus,
    val totalMcqsAttempted: Int,
    val totalMockTestsAttempted: Int,
    val averageAccuracy: Double,
    val subscriptionSource: SubscriptionSource = if (isPremium) SubscriptionSource.PAID else SubscriptionSource.NONE,
    val activatedAt: Long? = if (isPremium) System.currentTimeMillis() - (15L * 24 * 60 * 60 * 1000) else null,
    val expiresAt: Long? = if (isPremium) System.currentTimeMillis() + (75L * 24 * 60 * 60 * 1000) else null,
    val grantReason: String? = null,
    val grantedByAdmin: String? = null
) {
    fun isSubscriptionActive(currentTime: Long = System.currentTimeMillis()): Boolean {
        if (!isPremium) return false
        if (expiresAt == null || expiresAt == 0L) return true
        return currentTime < expiresAt
    }
}

data class AdminGrantAuditLogModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userPhone: String = "",
    val appId: String,
    val appName: String,
    val planId: String,
    val planName: String,
    val previousStatus: String, // "FREE" or "PREMIUM"
    val previousPlanName: String? = null,
    val previousExpiresAt: Long? = null,
    val newStatus: String = "PREMIUM",
    val grantAction: String = "NEW", // "NEW", "EXTEND", "REPLACE"
    val activatedAt: Long,
    val expiresAt: Long,
    val adminId: String,
    val adminName: String,
    val grantReason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class NotificationCategory(val displayName: String, val iconPrefix: String) {
    IMPORTANT("Important Updates", "🚨"),
    EXAM_UPDATES("Exam Updates", "📢"),
    MOCK_TEST("Mock Tests", "🧪"),
    CURRENT_AFFAIRS("Current Affairs", "📰"),
    SUBSCRIPTION("Subscriptions", "⭐"),
    OFFERS("Offers & Discounts", "🎁"),
    SYSTEM("System Updates", "⚙️"),
    GENERAL("General Announcement", "🔔"),
    NEW_MCQS("New MCQs/Sets", "📝"),
    COUPON("Coupon", "🎟️"),
    LEADERBOARD("Leaderboard", "🏆")
}

enum class NotificationAudience {
    ALL, FREE, PREMIUM, INACTIVE, SPECIFIC
}

enum class NotificationStatus {
    DRAFT, SCHEDULED, SENDING, SENT, CANCELLED, FAILED
}

data class AdminNotificationModel(
    val id: String,
    val title: String,
    val message: String,
    val imageUrl: String? = null,
    val deepLink: String? = null,
    val category: NotificationCategory = NotificationCategory.GENERAL,
    val audience: NotificationAudience = NotificationAudience.ALL,
    val status: NotificationStatus = NotificationStatus.SENT,
    val targetAppIds: List<String> = listOf("ALL"),
    val scheduledAt: Long? = null,
    val sentAt: Long? = null,
    val expiresAt: Long? = null,
    val priority: String = "HIGH", // HIGH, MEDIUM, LOW
    val iconName: String = "bell", // bell, siren, flask, newspaper, star, gift, gear, trophy
    val isFeatured: Boolean = false,
    val isRead: Boolean = false,
    val actionText: String? = null,
    val deliveryCount: Int = 0,
    val openCount: Int = 0,
    val failedCount: Int = 0
)

enum class DocumentStatus { DRAFT, PUBLISHED }

data class LegalDocumentModel(
    val id: String,
    val title: String,
    val content: String,
    val status: DocumentStatus,
    val targetAppIds: List<String> = listOf("ALL"),
    val lastUpdated: Long
)

data class FAQModel(
    val id: String,
    val question: String,
    val answer: String,
    val isEnabled: Boolean,
    val orderIndex: Int,
    val targetAppIds: List<String> = listOf("ALL")
)

data class SocialMediaLinkModel(
    val id: String,
    val platform: String,
    val url: String,
    val targetAppIds: List<String> = listOf("ALL")
)

data class SupportContactModel(
    val email: String,
    val phone: String,
    val targetAppIds: List<String> = listOf("ALL")
)

data class QuestionReportModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val appId: String = "appsc-group2",
    val questionId: String,
    val subjectName: String,
    val unitName: String,
    val topicName: String,
    val questionText: String,
    val options: Map<String, String>,
    val correctOption: String,
    val reportReason: String,
    val reporterEmail: String,
    val status: String = "PENDING", // PENDING, UNDER_REVIEW, RESOLVED, DISMISSED
    val reportedAt: Long = System.currentTimeMillis(),
    val reportDate: String = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
)

/**
 * Reusable Target Scope for Universal / Global Features
 */
enum class TargetScopeType {
    ALL_APPS,
    SINGLE_APP,
    SELECT_APPS
}

/**
 * Global Configuration Priority Architecture
 * GLOBAL CONFIGURATION -> Applied to all apps
 * APP-SPECIFIC OVERRIDE -> Overrides global configuration for that specific app
 */
data class AppSpecificConfigOverride(
    val appId: String,
    val loginBackgroundUrl: String? = null,
    val loginHeaderQuote: String? = null,
    val setsCardBgColorHex: Long? = null,
    val subtopicsCardBgColorHex: Long? = null,
    val wallpaperUrl: String? = null,
    val minAppVersion: String? = null,
    val latestAppVersion: String? = null,
    val isForceUpdate: Boolean? = null
)

data class GlobalAppConfigSystem(
    val globalLoginBackgroundUrl: String = "https://images.unsplash.com/photo-1590050752117-238cb0fb12b1?w=800",
    val globalLoginHeaderQuote: String = "Success belongs to the relentless. Master your preparation every single day.",
    val globalSetsCardBgColorHex: Long = 0xFFFFC000,
    val globalSubtopicsCardBgColorHex: Long = 0xFFF43F5E,
    val globalWallpaperUrl: String = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=800",
    val globalMinAppVersion: String = "2.4.0",
    val globalLatestAppVersion: String = "3.1.0",
    val globalForceUpdate: Boolean = false,
    val questionHighlightConfig: QuestionHighlightConfig = QuestionHighlightConfig(),
    val overrides: Map<String, AppSpecificConfigOverride> = emptyMap()
) {
    val loginBackgroundUrl: String get() = globalLoginBackgroundUrl
    val loginHeaderQuote: String get() = globalLoginHeaderQuote
    val appVersionName: String get() = globalLatestAppVersion
    val forceUpdate: Boolean get() = globalForceUpdate

    fun resolveLoginBg(appId: String): String =
        overrides[appId]?.loginBackgroundUrl ?: globalLoginBackgroundUrl

    fun resolveQuote(appId: String): String =
        overrides[appId]?.loginHeaderQuote ?: globalLoginHeaderQuote

    fun resolveSetsColor(appId: String): Long =
        overrides[appId]?.setsCardBgColorHex ?: globalSetsCardBgColorHex

    fun resolveSubtopicsColor(appId: String): Long =
        overrides[appId]?.subtopicsCardBgColorHex ?: globalSubtopicsCardBgColorHex

    fun resolveWallpaper(appId: String): String =
        overrides[appId]?.wallpaperUrl ?: globalWallpaperUrl

    fun resolveMinVersion(appId: String): String =
        overrides[appId]?.minAppVersion ?: globalMinAppVersion

    fun resolveLatestVersion(appId: String): String =
        overrides[appId]?.latestAppVersion ?: globalLatestAppVersion

    fun resolveForceUpdate(appId: String): Boolean =
        overrides[appId]?.isForceUpdate ?: globalForceUpdate
}

data class CardImageConfigModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val cardType: String, // "CURRENT_AFFAIRS", "MOCK_TEST_GENERATOR", "MOCK_TEST"
    val cardTitle: String,
    val imageUrl: String,
    val isEnabled: Boolean = true,
    val order: Int = 0,
    val mappedAppIds: List<String> = listOf("appsc-group2")
)

/**
 * Strict Hierarchy & Import Validation Models
 */
data class ImportValidationReport(
    val totalQuestions: Int = 0,
    val validQuestions: Int = 0,
    val invalidQuestions: Int = 0,
    val potentialDuplicates: Int = 0,
    val errors: List<String> = emptyList(),
    val hierarchySummary: String = "",
    val targetAppId: String = "",
    val targetSubjectName: String = "",
    val targetUnitName: String = "",
    val targetTopicName: String = ""
) {
    val isValid: Boolean get() = totalQuestions > 0 && invalidQuestions == 0
}

data class ImportExecutionResult(
    val isSuccess: Boolean,
    val totalProcessed: Int,
    val importedCount: Int,
    val skippedCount: Int,
    val rejectedCount: Int,
    val duplicatesCount: Int,
    val targetAppId: String,
    val targetSubjectName: String,
    val targetUnitName: String,
    val targetTopicName: String,
    val message: String,
    val jsonHash: String = ""
)

data class QuestionHierarchyDiagnostic(
    val questionId: String,
    val questionText: String,
    val issueType: String, // "ORPHAN_SUBJECT", "ORPHAN_UNIT", "ORPHAN_TOPIC", "CROSS_APP_MISMATCH", "DUPLICATE_HASH"
    val details: String,
    val currentAppId: String,
    val currentSubjectId: String,
    val currentUnitId: String,
    val currentTopicId: String
)

data class DataIntegrityReport(
    val totalQuestionsChecked: Int,
    val correctMappingsCount: Int,
    val orphanQuestionsCount: Int,
    val invalidHierarchyCount: Int,
    val duplicateQuestionsCount: Int,
    val issues: List<QuestionHierarchyDiagnostic> = emptyList(),
    val summaryText: String = ""
)


