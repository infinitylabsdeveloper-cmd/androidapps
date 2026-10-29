package com.example.model

import java.util.UUID

enum class MockTestMode(val displayName: String, val description: String) {
    FULL_LENGTH("Full-Length Mock Test", "Comprehensive exam covering complete syllabus"),
    SUBJECT_WISE("Subject-Wise Mock Test", "In-depth practice dedicated to selected subjects"),
    UNIT_WISE("Unit-Wise Mock Test", "Targeted test focusing on selected syllabus units"),
    TOPIC_WISE("Topic-Wise Mock Test", "Precision practice covering specific topics")
}

data class MockTestSectionConfig(
    val mode: MockTestMode,
    val name: String = mode.displayName,
    val description: String = mode.description,
    val iconName: String = when (mode) {
        MockTestMode.FULL_LENGTH -> "Assignment"
        MockTestMode.SUBJECT_WISE -> "MenuBook"
        MockTestMode.UNIT_WISE -> "AccountTree"
        MockTestMode.TOPIC_WISE -> "Psychology"
    },
    val backgroundGradientStart: String = when (mode) {
        MockTestMode.FULL_LENGTH -> "#1E3A8A"
        MockTestMode.SUBJECT_WISE -> "#065F46"
        MockTestMode.UNIT_WISE -> "#7C2D12"
        MockTestMode.TOPIC_WISE -> "#581C87"
    },
    val backgroundGradientEnd: String = when (mode) {
        MockTestMode.FULL_LENGTH -> "#3B82F6"
        MockTestMode.SUBJECT_WISE -> "#10B981"
        MockTestMode.UNIT_WISE -> "#F97316"
        MockTestMode.TOPIC_WISE -> "#A855F7"
    },
    val accentColor: String = when (mode) {
        MockTestMode.FULL_LENGTH -> "#60A5FA"
        MockTestMode.SUBJECT_WISE -> "#34D399"
        MockTestMode.UNIT_WISE -> "#FB923C"
        MockTestMode.TOPIC_WISE -> "#C084FC"
    },
    val isEnabled: Boolean = true,
    val displayOrder: Int = when (mode) {
        MockTestMode.FULL_LENGTH -> 1
        MockTestMode.SUBJECT_WISE -> 2
        MockTestMode.UNIT_WISE -> 3
        MockTestMode.TOPIC_WISE -> 4
    }
)

data class MockTestGeneratorAdminConfig(
    val sections: Map<String, MockTestSectionConfig> = MockTestMode.values().associate { it.name to MockTestSectionConfig(it) },
    val backgroundColorHex: String = "#F8FAFC",
    val cardBackgroundColorHex: String = "#FFFFFF",
    val buttonColorHex: String = "#2563EB",
    val accentColorHex: String = "#10B981",
    val headerDesignStyle: String = "PREMIUM_GRADIENT", // "PREMIUM_GRADIENT", "MINIMAL", "ROYAL_EMERALD"
    val generateButtonText: String = "Generate Mock Test",
    val targetScope: String = "CURRENT", // "CURRENT", "SELECTED", "ALL"
    val selectedAppIds: List<String> = emptyList()
)

data class SubjectQuestionConfig(
    val subjectId: String,
    val subjectName: String,
    val requestedCount: Int,
    val unitIds: List<String> = emptyList(),
    val topicIds: List<String> = emptyList(),
    val setNames: List<String> = emptyList()
)

data class MockTestAttemptRecord(
    val attemptId: String = UUID.randomUUID().toString(),
    val attemptNumber: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val score: Double,
    val totalMarks: Double,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val unanswered: Int,
    val timeTakenSeconds: Int,
    val percentage: Double,
    val accuracyStr: String,
    val userAnswers: Map<String, String> = emptyMap()
)

data class GeneratedMockTest(
    val id: String = UUID.randomUUID().toString(),
    val appId: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val subjectConfigs: List<SubjectQuestionConfig>,
    val totalQuestions: Int,
    val positiveMarks: Double,
    val negativeMarks: Double,
    val durationMinutes: Int,
    val questions: List<Question>,
    
    // Exact Test Configuration
    val examType: String = "Custom", // "Prelims", "Mains", "General", "Custom"
    val testMode: MockTestMode = MockTestMode.FULL_LENGTH,
    val selectedSubjectNames: List<String> = emptyList(),
    val selectedUnitNames: List<String> = emptyList(),
    val selectedTopicNames: List<String> = emptyList(),
    val selectedCurrentAffairsMonths: List<String> = emptyList(),
    val availableQuestionPoolCount: Int = 0,
    val difficulty: String = "Mixed", // "Easy", "Medium", "Hard", "Mixed"
    val selectionMode: String = "Balanced", // "Random", "Balanced", "Manual"
    val totalMarks: Double = totalQuestions * positiveMarks,
    
    // Attempt tracking
    var status: String = "Not Started", // "Not Started", "In Progress", "Completed"
    var score: Double? = null,
    var correctAnswers: Int = 0,
    var wrongAnswers: Int = 0,
    var unanswered: Int = 0,
    var timeTakenSeconds: Int = 0,
    var percentage: Double? = null,
    var userAnswers: Map<String, String> = emptyMap(), // Question ID -> Selected Option
    var markedForReview: Set<String> = emptySet(), // Question ID set
    var skippedQuestions: Set<String> = emptySet(), // Question ID set
    var currentIndex: Int = 0,
    var remainingSeconds: Int? = null,
    var attemptsHistory: List<MockTestAttemptRecord> = emptyList()
)
