package com.example.model

/**
 * Competitive League / Tier hierarchy for exam preparation candidates.
 */
enum class ExamLeague(
    val title: String,
    val iconEmoji: String,
    val minXp: Int,
    val colorHex: Long,
    val accentHex: Long
) {
    BRONZE("Bronze League", "🥉", 0, 0xFFCD7F32, 0xFFB45309),
    SILVER("Silver League", "🥈", 1000, 0xFF94A3B8, 0xFF64748B),
    GOLD("Gold League", "🥇", 3000, 0xFFF59E0B, 0xFFD97706),
    PLATINUM("Platinum League", "🏆", 6000, 0xFF0D9488, 0xFF0F766E),
    DIAMOND("Diamond League", "💎", 10000, 0xFF38BDF8, 0xFF0284C7);

    fun nextLeague(): ExamLeague? {
        return when (this) {
            BRONZE -> SILVER
            SILVER -> GOLD
            GOLD -> PLATINUM
            PLATINUM -> DIAMOND
            DIAMOND -> null
        }
    }
}

/**
 * Filter tabs available on the Leaderboard screen.
 */
enum class LeaderboardTab(val title: String) {
    OVERALL("Overall"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    EXAM("Exam"),
    SUBJECT("Subject")
}

/**
 * User privacy settings for leaderboard visibility.
 */
enum class LeaderboardPrivacyMode(val title: String, val description: String) {
    PUBLIC("Public", "Show full display name and avatar on leaderboard"),
    DISPLAY_NAME("Display Name Only", "Show only your chosen name, hide profile details"),
    ANONYMOUS("Anonymous", "Appear as 'Anonymous Candidate' with masked details")
}

/**
 * Official verified achievement badge definition.
 */
data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: String, // "ACCURACY", "STREAK", "QUESTIONS", "LEAGUE", "MOCK_TEST"
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

/**
 * Model representing a real candidate on the competitive leaderboard.
 */
data class LeaderboardCandidate(
    val userId: String = "",
    val rank: Int = 0,
    val previousRank: Int = 0,
    val displayName: String = "",
    val avatarColorHex: Long = 0xFF2563EB,
    val avatarUrl: String? = null,
    val appId: String = "appsc-group2",
    val targetExam: String = "APPSC Group-2",
    val district: String = "Visakhapatnam",
    val xp: Int = 0,
    val weeklyXp: Int = 0,
    val monthlyXp: Int = 0,
    val leaderboardScore: Double = 0.0,
    val accuracyPercentage: Float = 0.0f,
    val testsCompleted: Int = 0,
    val questionsAttempted: Int = 0,
    val correctAnswers: Int = 0,
    val streakDays: Int = 0,
    val longestStreak: Int = streakDays,
    val league: ExamLeague = ExamLeague.BRONZE,
    val badges: List<String> = emptyList(),
    val isCurrentUser: Boolean = false,
    val privacyMode: LeaderboardPrivacyMode = LeaderboardPrivacyMode.PUBLIC,
    val rankDelta: Int = previousRank - rank,
    val subjectXp: Map<String, Int> = emptyMap(),
    val lastActiveDate: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Sanitizes display name according to the candidate's chosen privacy mode.
     */
    fun getEffectiveDisplayName(isViewerSelf: Boolean = isCurrentUser): String {
        return when {
            isViewerSelf -> "$displayName (You)"
            privacyMode == LeaderboardPrivacyMode.ANONYMOUS -> "Anonymous Candidate"
            privacyMode == LeaderboardPrivacyMode.DISPLAY_NAME -> displayName.ifBlank { "Candidate #${rank}" }
            else -> displayName.ifBlank { "Candidate #${rank}" }
        }
    }
}

/**
 * Configurable leaderboard parameters managed via the Admin Panel.
 */
data class LeaderboardConfigModel(
    val isLeaderboardEnabled: Boolean = true,
    val isWeeklyEnabled: Boolean = true,
    val isMonthlyEnabled: Boolean = true,
    val isExamWiseEnabled: Boolean = true,
    val isSubjectWiseEnabled: Boolean = true,
    val xpPerCorrectAnswer: Int = 10,
    val xpPerMockTestCompletion: Int = 50,
    val xpPerDailyStreak: Int = 20,
    val xpPerTopicCompletion: Int = 25,
    val accuracyWeight: Double = 0.3,
    val mockTestWeight: Double = 0.4,
    val consistencyWeight: Double = 0.3,
    val silverThresholdXp: Int = 1000,
    val goldThresholdXp: Int = 3000,
    val platinumThresholdXp: Int = 6000,
    val diamondThresholdXp: Int = 10000,
    val weeklyResetDay: String = "SUNDAY_MIDNIGHT",
    val monthlyResetDay: String = "LAST_DAY_MIDNIGHT",
    val lastResetTimestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Idempotent XP transaction record for anti-cheat verification.
 */
data class XPTransaction(
    val id: String = "",
    val userId: String = "",
    val source: String = "", // "MOCK_TEST", "PRACTICE_SET", "CURRENT_AFFAIRS", "DAILY_STREAK", "TOPIC_COMPLETION"
    val sourceId: String = "", // Unique idempotency key (e.g. testId, attemptId)
    val xp: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Historical leaderboard record for previous weeks and months.
 */
data class LeaderboardHistoryEntry(
    val id: String = "",
    val userId: String = "",
    val periodType: String = "WEEKLY", // "WEEKLY", "MONTHLY"
    val periodLabel: String = "", // e.g. "Week 38 - Sep 2026"
    val rank: Int = 0,
    val xp: Int = 0,
    val accuracyPercentage: Float = 0f,
    val league: String = "BRONZE",
    val badgeEarned: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Real-time monitoring metrics for the Admin Dashboard.
 */
data class LeaderboardMonitoringStats(
    val totalParticipants: Int = 0,
    val activeParticipants: Int = 0,
    val topScore: Double = 0.0,
    val averageScore: Double = 0.0,
    val averageAccuracy: Float = 0f,
    val weeklyActiveUsers: Int = 0,
    val monthlyActiveUsers: Int = 0,
    val totalXpDistributed: Long = 0L,
    val totalAchievementsEarned: Int = 0,
    val leagueDistribution: Map<String, Int> = emptyMap()
)

/**
 * Weekly competitive challenge definition.
 */
data class WeeklyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val currentCount: Int,
    val xpReward: Int,
    val badgeReward: String? = null,
    val isCompleted: Boolean = currentCount >= targetCount
)
