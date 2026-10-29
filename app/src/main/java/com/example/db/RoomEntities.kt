package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val appId: String = "appsc-group2",
    val subjectId: String,
    val unitId: String = "",
    val topicId: String = "",
    val subtopic: String = "",
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String,
    val explanation: String = "",
    val bookmarkedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exam_app_config")
data class ExamAppConfigEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isEnabled: Boolean,
    val metadata: String
)

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appId: String = "appsc-group2",
    val testId: String = "",
    val testTitle: String,
    val testType: String = "MOCK_TEST", // "MOCK_TEST", "PRACTICE_SET", "DAILY_QUIZ", "PYQ"
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val unanswered: Int,
    val score: Double = 0.0,
    val scorePercentage: Float,
    val accuracy: String,
    val timeTakenSeconds: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val userAnswers: Map<String, String> = emptyMap()
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val appId: String = "appsc-group2",
    val streakDays: Int = 1,
    val totalXp: Int = 150,
    val dailyTargetQuestions: Int = 30,
    val dailyQuestionsAnswered: Int = 0,
    val lastActiveDate: String = "2026-09-17",
    val completedQuestionsCount: Int = 0,
    val testsCompletedCount: Int = 0,
    val totalTimeSpentSeconds: Int = 0,
    val accuracyPercentage: Float = 0.0f,
    val completedTopicIds: List<String> = emptyList(),
    val completedUnitIds: List<String> = emptyList(),
    val savedTopicIds: List<String> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_streak")
data class UserStreakEntity(
    @PrimaryKey val id: Int = 1,
    val streakDays: Int = 14,
    val totalXp: Int = 1420,
    val dailyTargetQuestions: Int = 30,
    val lastActiveDate: String = "2026-09-09"
)

@Entity(
    tableName = "question_bank",
    indices = [
        Index(value = ["appId"]),
        Index(value = ["subjectId"]),
        Index(value = ["unitId"]),
        Index(value = ["topicId"])
    ]
)
data class QuestionEntity(
    @PrimaryKey val id: String,
    val appId: String = "appsc-group2",
    val subjectId: String,
    val unitId: String = "",
    val topicId: String = "",
    val subtopic: String = "",
    val questionText: String,
    val options: Map<String, String>,
    val correctOption: String,
    val explanation: String? = null,
    val setName: String = "",
    val difficulty: String = "Medium",
    val language: String = "English / Telugu",
    val imageUrl: String? = null,
    val year: String? = null,
    val exam: String? = null,
    val marks: Double = 1.0,
    val negativeMarks: Double = 0.33
)

@Entity(tableName = "generated_mock_tests")
data class GeneratedMockTestEntity(
    @PrimaryKey val id: String,
    val appId: String,
    val title: String,
    val createdAt: Long,
    val subjectConfigs: List<com.example.model.SubjectQuestionConfig>,
    val totalQuestions: Int,
    val positiveMarks: Double,
    val negativeMarks: Double,
    val durationMinutes: Int,
    val questions: List<com.example.model.Question>,
    val examType: String = "Custom",
    val difficulty: String = "Mixed",
    val selectionMode: String = "Balanced",
    val totalMarks: Double = 0.0,
    
    val status: String,
    val score: Double?,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val unanswered: Int,
    val timeTakenSeconds: Int,
    val percentage: Double?,
    val userAnswers: Map<String, String>,
    val testMode: String = "FULL_LENGTH",
    val markedForReview: Set<String> = emptySet(),
    val skippedQuestions: Set<String> = emptySet(),
    val currentIndex: Int = 0,
    val remainingSeconds: Int? = null,
    val attemptsHistory: List<com.example.model.MockTestAttemptRecord> = emptyList()
)

@Entity(tableName = "legal_documents")
data class LegalDocumentEntity(
    @PrimaryKey val id: String,
    val type: String, // Enum name
    val title: String,
    val content: String,
    val mappedAppIds: List<String>,
    val isEnabled: Boolean = true,
    val displayOrder: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val bgGradientStart: String? = null,
    val bgGradientEnd: String? = null,
    val accentColorHex: String? = null,
    val iconName: String? = null,
    val isPublished: Boolean = true,
    val sectionsJson: String? = null,
    val socialLinksJson: String? = null,
    val draftContent: String? = null,
    val draftSectionsJson: String? = null,
    val draftTitle: String? = null
)

@Entity(tableName = "support_faqs")
data class SupportFaqEntity(
    @PrimaryKey val id: String,
    val question: String,
    val answer: String,
    val mappedAppIds: List<String>,
    val isEnabled: Boolean = true,
    val displayOrder: Int = 0
)

@Entity(tableName = "user_entitlements")
data class UserEntitlementEntity(
    @PrimaryKey val transactionId: String,
    val appId: String = "appsc-group2",
    val userId: String = "guest_user",
    val planId: String = "12_months",
    val planTitle: String,
    val provider: String = "GOOGLE_PLAY", // "GOOGLE_PLAY" or "RAZORPAY"
    val gateway: String = "GOOGLE_PLAY_BILLING",
    val paymentMode: String = "",
    val providerSubscriptionId: String? = null,
    val providerPaymentId: String? = null,
    val providerOrderId: String? = null,
    val amountRupees: Int = 999,
    val amount: Double = amountRupees.toDouble(),
    val currency: String = "INR",
    val isVerified: Boolean = true,
    val purchaseTimestamp: Long = System.currentTimeMillis(),
    val startDate: Long = purchaseTimestamp,
    val expiryTimestamp: Long = purchaseTimestamp + (365L * 24 * 3600 * 1000L),
    val expiryDate: Long = expiryTimestamp,
    val status: String = "ACTIVE", // "INITIATED", "PENDING", "ACTIVE", "FAILED", "CANCELLED", "REFUNDED", "EXPIRED"
    val autoRenew: Boolean = false,
    val createdAt: Long = purchaseTimestamp,
    val updatedAt: Long = purchaseTimestamp,
    val isAdminGranted: Boolean = false,
    val reportingToken: String? = null
)

@Entity(tableName = "offline_content_packs")
data class OfflineContentPackEntity(
    @PrimaryKey val id: String,
    val appId: String = "appsc-group2",
    val title: String,
    val level: String = "SUBJECT", // "SUBJECT", "UNIT", "TOPIC", "SET"
    val subjectId: String,
    val unitId: String = "",
    val topicId: String = "",
    val setName: String = "",
    val questionCount: Int,
    val sizeBytes: Long,
    val downloadStatus: String = "DOWNLOADED", // "DOWNLOADED", "DOWNLOADING", "AVAILABLE"
    val syncStatus: String = "SYNCED", // "SYNCED", "SYNCING", "PENDING_SYNC", "SYNC_REQUIRED"
    val encryptedPayload: String = "",
    val lastSyncedTimestamp: Long = System.currentTimeMillis(),
    val downloadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mock_test_configs")
data class MockTestConfigEntity(
    @PrimaryKey val appId: String,
    val examType: String,
    val testMode: String,
    val selectedSubjectIds: List<String>,
    val selectedTopicMap: Map<String, List<String>>,
    val totalQuestions: Int,
    val durationMinutes: Int,
    val positiveMarks: Double,
    val negativeMarks: Double,
    val questionSelectionMode: String,
    val difficultyLevel: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey val id: String,
    val appId: String = "appsc-group2",
    val title: String,
    val content: String,
    val category: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "current_affairs")
data class CurrentAffairsEntity(
    @PrimaryKey val id: String,
    val appId: String = "appsc-group2",
    val mappedAppIds: List<String> = listOf("appsc-group2"),
    val title: String,
    val description: String = "",
    val content: String = "",
    val date: String = "",
    val month: String = "",
    val category: String = "AP Current Affairs",
    val sourceUrl: String? = null,
    val imageUrl: String? = null,
    val explanation: String? = null,
    val publishedDate: String = "",
    val status: String = "PUBLISHED",
    val isSharedMultiApp: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val appId: String = "appsc-group2",
    val name: String,
    val category: String = "prelims",
    val categories: List<String> = emptyList(),
    val icon: String = "BookOpen",
    val imageUrl: String = "",
    val heroQuote: String = "Explore the Past\nBuild a Better Future",
    val color: String = "from-amber-600 to-orange-700",
    val units: List<com.example.model.UnitModel> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_roles")
data class UserRoleEntity(
    @PrimaryKey val uid: String,
    val role: String // "USER", "ADMIN"
)

fun CurrentAffairsEntity.toDomain(): com.example.model.CurrentAffair {
    return com.example.model.CurrentAffair(
        id = id,
        appId = appId,
        mappedAppIds = mappedAppIds,
        month = month.ifBlank { date },
        category = category,
        title = title,
        content = content.ifBlank { description },
        sourceUrl = sourceUrl,
        imageUrl = imageUrl,
        explanation = explanation,
        publishedDate = publishedDate.ifBlank { date },
        status = status,
        isSharedMultiApp = isSharedMultiApp
    )
}

fun com.example.model.CurrentAffair.toEntity(): CurrentAffairsEntity {
    return CurrentAffairsEntity(
        id = id,
        appId = appId,
        mappedAppIds = mappedAppIds,
        title = title,
        description = content.take(120),
        content = content,
        date = publishedDate.ifBlank { month },
        month = month,
        category = category,
        sourceUrl = sourceUrl,
        imageUrl = imageUrl,
        explanation = explanation,
        publishedDate = publishedDate,
        status = status,
        isSharedMultiApp = isSharedMultiApp,
        lastUpdated = System.currentTimeMillis()
    )
}

fun SubjectEntity.toDomain(): com.example.model.Subject {
    return com.example.model.Subject(
        id = id,
        appId = appId,
        name = name,
        category = category,
        categories = categories,
        icon = icon,
        imageUrl = imageUrl,
        heroQuote = heroQuote,
        color = color,
        units = units
    )
}

fun com.example.model.Subject.toEntity(): SubjectEntity {
    return SubjectEntity(
        id = id,
        appId = appId,
        name = name,
        category = category,
        categories = categories,
        icon = icon,
        imageUrl = imageUrl,
        heroQuote = heroQuote,
        color = color,
        units = units,
        lastUpdated = System.currentTimeMillis()
    )
}
