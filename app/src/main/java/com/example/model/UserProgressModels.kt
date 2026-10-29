package com.example.model

/**
 * Domain model representing user learning progress, streaks, daily quotas,
 * and mastery levels for the active App / Exam configuration.
 */
data class UserProgress(
    val appId: String = "appsc-group2",
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
) {
    val dailyProgressRatio: Float
        get() = if (dailyTargetQuestions > 0) {
            (dailyQuestionsAnswered.toFloat() / dailyTargetQuestions).coerceIn(0f, 1f)
        } else 0f

    val totalHoursSpent: Float
        get() = totalTimeSpentSeconds / 3600f

    val level: Int
        get() = (totalXp / 250) + 1

    val xpForCurrentLevel: Int
        get() = totalXp % 250

    val xpNeededForNextLevel: Int
        get() = 250
}

/**
 * Summary statistics of mock test performance
 */
data class MockTestPerformanceSummary(
    val totalTestsTaken: Int = 0,
    val averageScorePercentage: Float = 0f,
    val bestScorePercentage: Float = 0f,
    val totalQuestionsAttempted: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val overallAccuracy: String = "0%",
    val totalTimeSpentSeconds: Int = 0
)
