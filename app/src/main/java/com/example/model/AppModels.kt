package com.example.model

/**
 * Platform Multi-App Tenant Model
 * Single codebase powers multiple applications (e.g. APPSC Group 1, Group 2, SSC, Banking, etc.)
 */
data class AppTenantModel(
    val id: String, // immutable appId: app_001, app_002...
    val name: String,
    val shortName: String,
    val description: String = "",
    val category: String, // Competitive Exams, Teaching, etc.
    val examName: String, // APPSC Group 2, UPSC, etc.
    val orgName: String = "",
    val logoUrl: String = "",
    val iconUrl: String = "",
    val splashLogoUrl: String = "",
    val primaryLanguage: String = "English",
    val allowedLanguages: List<String> = listOf("English"),
    val country: String = "India",
    val region: String = "", // State/Region
    val contactEmail: String = "sobhadhanaraju0@gmail.com",
    val supportPhone: String = "+91 94935 12768",
    val supportEmail: String = "sobhadhanaraju0@gmail.com",
    val whatsappNumber: String = "+91 94935 12768",
    val whatsappLink: String = "https://wa.me/919493512768",
    val telegramId: String = "@DHANARAJU0",
    val telegramLink: String = "https://t.me/DHANARAJU0",
    val isWhatsAppEnabled: Boolean = true,
    val isTelegramEnabled: Boolean = true,
    val isEmailSupportEnabled: Boolean = true,
    val websiteUrl: String = "",
    val playStoreUrl: String = "",
    val packageName: String = "",
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val status: AppStatus = AppStatus.DRAFT,
    val primaryColorHex: Long = 0xFF2563EB,
    val secondaryColorHex: Long = 0xFF1E40AF,
    val accentColorHex: Long = 0xFFF59E0B,
    val backgroundColorHex: Long = 0xFFF8FAFC,
    val surfaceColorHex: Long = 0xFFFFFFFF,
    val textColorHex: Long = 0xFF0F172A,
    val buttonColorHex: Long = 0xFF2563EB,
    val errorColorHex: Long = 0xFFDC2626,
    val successColorHex: Long = 0xFF10B981,
    val fontFamily: String = "Roboto",
    val currencyCode: String = "INR",
    val currencySymbol: String = "₹",
    val featuresEnabled: List<String> = listOf("MCQs", "Current Affairs", "Monthly PDFs", "Posters", "Subscriptions", "Daily Quiz", "Mock Tests"),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val activeUsers: Int = 0,
    val subscriptionUsers: Int = 0,
    val contentCount: Int = 0,
    val medium: String = "English",
    val languageCode: String = "en",
    val isPrelimsCardEnabled: Boolean = true,
    val isMainsCardEnabled: Boolean = true,
    val isAllCardEnabled: Boolean = true
) {
    val code: String get() = id.replace("app_", "").uppercase()
    val examCategory: String get() = examName
    val isActive: Boolean get() = status == AppStatus.ACTIVE
}

data class Subject(
    val id: String,
    val appId: String = "appsc-group2",
    val name: String,
    val category: String = "prelims", // "prelims", "mains", "general", "both", or comma-separated
    val categories: List<String> = emptyList(), // Multi-category assignment: ["prelims", "mains", "general"]
    val icon: String = "BookOpen",
    val imageUrl: String = "",
    val imageRes: Int? = null,
    val heroQuote: String = "Explore the Past\nBuild a Better Future",
    val color: String = "from-amber-600 to-orange-700",
    val units: List<UnitModel> = emptyList()
) {
    /**
     * Resolves all normalized categories assigned to this subject (prelims, mains, general).
     */
    fun getAssignedCategories(): List<String> {
        val result = linkedSetOf<String>()
        if (categories.isNotEmpty()) {
            categories.forEach { c ->
                val norm = c.trim().lowercase()
                if (norm == "both") {
                    result.add("prelims")
                    result.add("mains")
                } else if (norm.isNotBlank()) {
                    result.add(norm)
                }
            }
        }
        if (category.isNotBlank()) {
            category.split(",", "/", "&", "+", "|").forEach { part ->
                val norm = part.trim().lowercase()
                if (norm == "both") {
                    result.add("prelims")
                    result.add("mains")
                } else if (norm.isNotBlank()) {
                    result.add(norm)
                }
            }
        }
        if (result.isEmpty()) {
            result.add("prelims")
        }
        return result.toList()
    }

    /**
     * Checks whether this subject belongs to the requested category filter.
     * Supported filters: "ALL", "PRELIMS", "MAINS", "GENERAL".
     */
    fun matchesCategoryFilter(filter: String): Boolean {
        if (filter.equals("ALL", ignoreCase = true) || filter.isBlank()) return true
        val target = filter.trim().lowercase()
        val assigned = getAssignedCategories()
        return assigned.contains(target)
    }

    val displayCategoryLabel: String
        get() {
            val assigned = getAssignedCategories()
            return when {
                assigned.contains("prelims") && assigned.contains("mains") && assigned.contains("general") -> "PRELIMS • MAINS • GENERAL"
                assigned.contains("prelims") && assigned.contains("mains") -> "PRELIMS & MAINS"
                assigned.contains("prelims") && assigned.contains("general") -> "PRELIMS & GENERAL"
                assigned.contains("mains") && assigned.contains("general") -> "MAINS & GENERAL"
                assigned.contains("prelims") -> "PRELIMS"
                assigned.contains("mains") -> "MAINS"
                assigned.contains("general") -> "GENERAL"
                else -> category.uppercase()
            }
        }
}

data class UnitModel(
    val id: String,
    val appId: String = "appsc-group2",
    val name: String,
    val unitNumber: Int = 1,
    val subtitle: String? = null,
    val tagline: String? = null,
    val quote: String? = null,
    val slogan: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val imageRes: Int? = null,
    val estimatedMcqs: String? = null,
    val completionPercentage: Int? = null,
    val topics: List<TopicModel> = emptyList()
)

data class TopicModel(
    val id: String,
    val appId: String = "appsc-group2",
    val name: String,
    val subtitle: String? = null,
    val tagBadge: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val imageRes: Int? = null,
    val estimatedMcqs: String? = null,
    val practiceSetsCount: Int = 3,
    val difficulty: String = "Medium",
    val completionPercentage: Int = 0,
    val notesContent: String? = null,
    val subtopics: List<String> = emptyList()
)

data class PracticeSetModel(
    val id: String,
    val setNumber: Int = 1,
    val title: String = "Set 1",
    val subtitle: String = "10 Qs • Q1 – Q10",
    val questionsCount: Int = 10,
    val startQuestionNumber: Int = 1,
    val endQuestionNumber: Int = 10,
    val status: String = "Not Started", // "Completed", "In Progress", "Not Started"
    val isCompleted: Boolean = false,
    val durationMinutes: Int = 10,
    val totalMarks: Int = 10,
    val negativeMarks: Double = 0.33,
    val difficulty: String = "Medium",
    val badge: String = "High Yield",
    val bestScore: Int? = null,
    val questions: List<Question> = emptyList()
)

enum class ContentLevel(val value: String) {
    SUBJECT("SUBJECT"),
    UNIT("UNIT"),
    TOPIC("TOPIC")
}

data class Question(
    val id: String,
    val appId: String = "appsc-group2", // Exam ID
    val subjectId: String,
    val unitId: String = "",
    val topicId: String = "",
    val contentLevel: String = "TOPIC", // "SUBJECT", "UNIT", "TOPIC"
    val subtopic: String = "",
    val questionText: String,
    val options: Map<String, String>, // "A" -> text, "B" -> text, etc.
    val correctOption: String, // "A", "B", "C", "D"
    val explanation: String? = null,
    val setName: String = "",
    val difficulty: String = "Medium",
    val questionType: String = "SINGLE_CHOICE",
    val source: String? = null,
    val tags: List<String> = emptyList(),
    val language: String = "English / Telugu",
    val imageUrl: String? = null,
    val imageRes: Int? = null,
    val year: String? = null,
    val exam: String? = null,
    val marks: Double = 1.0,
    val negativeMarks: Double = 0.33,
    val customFields: Map<String, String> = emptyMap(),
    val contentHash: String? = null,
    val importBatchId: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val questionHighlights: List<HighlightRange> = emptyList(),
    val explanationHighlights: List<HighlightRange> = emptyList()
) {
    fun shuffledOptions(): Question {
        val keys = listOf("A", "B", "C", "D").filter { options.containsKey(it) }
        if (keys.size <= 1) return this
        val correctText = options[correctOption] ?: ""
        val secureRandom = java.security.SecureRandom()
        val shuffledValues = keys.mapNotNull { options[it] }.toMutableList().apply {
            repeat(3) {
                for (i in indices.reversed()) {
                    val j = secureRandom.nextInt(i + 1)
                    val temp = this[i]
                    this[i] = this[j]
                    this[j] = temp
                }
            }
        }
        val newOptions = mutableMapOf<String, String>()
        var newCorrectOption = correctOption
        keys.forEachIndexed { index, key ->
            val text = shuffledValues.getOrElse(index) { "" }
            newOptions[key] = text
            if (text == correctText) {
                newCorrectOption = key
            }
        }
        return this.copy(options = newOptions, correctOption = newCorrectOption)
    }
}

data class CurrentAffair(
    val id: String,
    val appId: String = "appsc-group2",
    val mappedAppIds: List<String> = listOf("appsc-group2"),
    val month: String,
    val category: String = "AP Current Affairs", // "AP Current Affairs", "National", "International", "Economy", "Polity", "Science & Technology", "Environment", "Sports", "Schemes", "Defence", "Reports & Indexes"
    val title: String,
    val content: String,
    val sourceUrl: String? = null,
    val imageUrl: String? = null,
    val explanation: String? = null,
    val publishedDate: String,
    val status: String = "PUBLISHED", // "PUBLISHED", "DRAFT", "SCHEDULED"
    val isSharedMultiApp: Boolean = false,
    val attachedMcqs: List<Question> = emptyList()
)

data class SetAppearanceConfig(
    val backgroundColor: String? = null,
    val gradientColors: List<String>? = null,
    val accentColor: String? = null,
    val textColor: String? = null,
    val iconColor: String? = null,
    val iconBackgroundColor: String? = null,
    val borderColor: String? = null,
    val cornerRadiusDp: Int = 16,
    val shadowElevationDp: Int = 2,
    val iconName: String? = null,
    val backgroundPattern: String? = null
)

data class MonthlyMcqSetModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String, // e.g., "Set 1", "Set 2"
    val month: String,
    val year: String,
    val questions: List<Question> = emptyList(),
    val appearanceConfig: SetAppearanceConfig? = null
)

data class MonthlyCurrentAffairsModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val appId: String = "appsc-group2",
    val mappedAppIds: List<String> = listOf("appsc-group2"),
    val month: String,
    val year: String = "2026",
    val title: String, // e.g., "September 2026 Current Affairs"
    val sets: List<MonthlyMcqSetModel> = emptyList(),
    val publishedDate: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
    val isActive: Boolean = true,
    val startDate: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
    val startTime: String = "00:00",
    val endDate: String = "2030-12-31",
    val endTime: String = "23:59",
    val cardImageUrl: String = ""
) {
    fun getStatus(
        currentDateStr: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
        currentTimeStr: String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
    ): PromotionStatus {
        if (!isActive) return PromotionStatus.INACTIVE
        if (currentDateStr < startDate || (currentDateStr == startDate && currentTimeStr < startTime)) {
            return PromotionStatus.SCHEDULED
        }
        if (currentDateStr > endDate || (currentDateStr == endDate && currentTimeStr > endTime)) {
            return PromotionStatus.EXPIRED
        }
        return PromotionStatus.ACTIVE
    }
}

data class SubscriptionPackage(
    val id: String,
    val appId: String = "appsc-group2",
    val name: String,
    val price: Double,
    val currencySymbol: String = "₹",
    val billingCycle: String = "Monthly",
    val features: List<String> = emptyList(),
    val isPopular: Boolean = false
)

data class TestAttemptHistory(
    val id: String,
    val title: String,
    val scoreText: String,
    val accuracy: String,
    val date: String
)

data class TestResult(
    val id: String,
    val testName: String,
    val totalQuestions: Int,
    val score: Int,
    val percentage: Int,
    val accuracy: String,
    val date: String
)

data class PyqPaper(
    val id: String,
    val appId: String = "appsc-group2",
    val year: String,
    val title: String,
    val examStage: String, // "Prelims", "Mains Paper 1", etc.
    val totalQuestions: Int,
    val durationMinutes: Int,
    val description: String,
    val questions: List<Question> = emptyList()
)

data class MockTestModel(
    val id: String,
    val appId: String = "appsc-group2",
    val title: String,
    val paperType: String, // "Paper I - GS", "Paper II - AP History & Polity", etc.
    val durationMinutes: Int,
    val totalQuestions: Int,
    val totalMarks: Int,
    val difficulty: String, // "Easy", "Medium", "Hard", "Exam Standard"
    val description: String,
    val isFree: Boolean = true
)

data class StudyNoteModel(
    val id: String,
    val appId: String = "appsc-group2",
    val subjectId: String,
    val title: String,
    val category: String, // "AP Schemes", "History Table", "Act Summary", "Econ Data"
    val readTime: String,
    val content: String,
    val highlights: List<String> = emptyList()
)

enum class UserRole {
    USER, ADMIN
}

data class UserProfileModel(
    val name: String,
    val email: String,
    val phone: String,
    val district: String = "Visakhapatnam",
    val targetExam: String,
    val targetAppId: String = "appsc-group2",
    val preferredLanguage: String,
    val dailyGoalQuestions: Int,
    val streakDays: Int,
    val isProSubscribed: Boolean,
    val proPlanName: String,
    val activatedAt: Long = 0L,
    val expiresAt: Long = 0L,
    val subscriptionSource: String = "DIRECT_PURCHASE",
    val isLoggedIn: Boolean = false,
    val profileImageUrl: String? = null,
    val activeSessionId: String? = null,
    val isSubscriptionCancelled: Boolean = false,
    val cancellationReason: String? = null,
    val cancellationDetails: String? = null,
    val isPhoneVerified: Boolean = true,
    val isEmailVerified: Boolean = true,
    val role: UserRole = UserRole.USER
)

enum class CouponDiscountType {
    PERCENTAGE, FIXED
}

enum class PromotionStatus {
    ACTIVE, SCHEDULED, EXPIRED, INACTIVE
}

data class CouponModel(
    val id: String,
    val appId: String = "appsc-group2",
    val mappedAppIds: List<String> = listOf("appsc-group2"),
    val code: String,
    val description: String,
    val discountType: CouponDiscountType = CouponDiscountType.PERCENTAGE,
    val discountValue: Double,
    val minPurchaseAmount: Double = 0.0,
    val startDate: String,
    val startTime: String = "00:00",
    val expiryDate: String,
    val expiryTime: String = "23:59",
    val isActive: Boolean = true,
    val usageLimitTotal: Int = -1,
    val usageLimitPerUser: Int = 1,
    val timesUsed: Int = 0,
    val restrictedPlanIds: List<String> = emptyList(),
    val restrictedUserGroups: List<String> = listOf("All Users"),
    val festivalTag: String = "General Offer"
) {
    fun getStatus(
        currentDateStr: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
        currentTimeStr: String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
    ): PromotionStatus {
        if (!isActive) return PromotionStatus.INACTIVE
        if (usageLimitTotal > 0 && timesUsed >= usageLimitTotal) return PromotionStatus.EXPIRED
        if (currentDateStr < startDate || (currentDateStr == startDate && currentTimeStr < startTime)) {
            return PromotionStatus.SCHEDULED
        }
        if (currentDateStr > expiryDate || (currentDateStr == expiryDate && currentTimeStr > expiryTime)) {
            return PromotionStatus.EXPIRED
        }
        return PromotionStatus.ACTIVE
    }

    fun calculateDiscount(cartAmount: Double): Double {
        if (cartAmount < minPurchaseAmount) return 0.0
        return when (discountType) {
            CouponDiscountType.PERCENTAGE -> (cartAmount * (discountValue / 100.0)).coerceAtMost(cartAmount)
            CouponDiscountType.FIXED -> discountValue.coerceAtMost(cartAmount)
        }
    }
}

data class PromotionalPosterModel(
    val id: String,
    val appId: String = "appsc-group2",
    val mappedAppIds: List<String> = listOf("appsc-group2"),
    val posterType: String = "Home Screen Poster", // "Home Screen Poster", "Current Affairs Poster", "Subscription Poster", "Exam Update", "Festival Poster", "Announcement", "Promotional Banner", "Study Material Banner"
    val title: String,
    val imageUrl: String,
    val promotionalText: String,
    val ctaText: String = "Claim Offer",
    val offerDetails: String = "",
    val destinationPage: String = "subscription",
    val targetPages: List<String> = listOf("Home Screen", "Subscription Screen"),
    val targetPlans: List<String> = listOf("All Plans"),
    val targetUserTypes: List<String> = listOf("All Users"),
    val startDate: String = "2026-09-01",
    val startTime: String = "00:00",
    val endDate: String = "2026-12-31",
    val endTime: String = "23:59",
    val priorityOrder: Int = 1,
    val isActive: Boolean = true,
    val isSharedMultiApp: Boolean = false
) {
    fun getStatus(
        currentDateStr: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
        currentTimeStr: String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
    ): PromotionStatus {
        if (!isActive) return PromotionStatus.INACTIVE
        if (currentDateStr < startDate || (currentDateStr == startDate && currentTimeStr < startTime)) {
            return PromotionStatus.SCHEDULED
        }
        if (currentDateStr > endDate || (currentDateStr == endDate && currentTimeStr > endTime)) {
            return PromotionStatus.EXPIRED
        }
        return PromotionStatus.ACTIVE
    }
}

data class SubscriptionPlanModel(
    val id: String,
    val appId: String = "appsc-group2",
    val name: String,
    val price: Double,
    val promotionalPrice: Double? = null,
    val currencySymbol: String = "₹",
    val duration: String = "Monthly",
    val durationDays: Int = 30,
    val features: List<String> = emptyList(),
    val benefits: String = "",
    val introductoryOfferText: String? = null,
    val linkedCouponCodes: List<String> = emptyList(),
    val isPopular: Boolean = false,
    val isActive: Boolean = true
)

data class LoginBgImageModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val url: String,
    val title: String = "",
    val subtitle: String = "",
    val targetScreen: String = "BOTH", // "LOGIN", "OTP", or "BOTH"
    val isEnabled: Boolean = true,
    val order: Int = 0
)

data class LoginQuotationModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val quoteText: String,
    val authorOrSource: String = "",
    val targetScreen: String = "BOTH", // "LOGIN", "OTP", or "BOTH"
    val themeColor: String = "GOLD", // "GOLD", "EMERALD", "SAPPHIRE", "AMBER", "SUNSET", "PURPLE", "LUXURY_CREAM", "SLATE", "CUSTOM_HEX"
    val customBgHex: String = "",
    val customTextHex: String = "",
    val isEnabled: Boolean = true,
    val order: Int = 0
)

data class AppWallpaperModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val url: String,
    val screenName: String, // e.g., "Home Screen", "Syllabus Explorer", "Mock Test Series", "Current Affairs", "Saved Questions", "Performance Board", "Topic Sets", "Global Default", "Login Screen", "OTP Screen"
    val isEnabled: Boolean = true,
    val order: Int = 0,
    val mappedAppIds: List<String> = emptyList() // Empty list means "All Apps"
)


