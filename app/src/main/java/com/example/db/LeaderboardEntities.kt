package com.example.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "leaderboard_entries",
    indices = [
        Index(value = ["appId", "leaderboardScore"]),
        Index(value = ["appId", "weeklyXp"]),
        Index(value = ["appId", "monthlyXp"]),
        Index(value = ["district"])
    ]
)
data class LeaderboardEntryEntity(
    @PrimaryKey val userId: String,
    val rank: Int = 0,
    val previousRank: Int = 0,
    val displayName: String,
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
    val league: String = "BRONZE",
    val badges: List<String> = emptyList(),
    val privacyMode: String = "PUBLIC",
    val subjectXpMap: Map<String, String> = emptyMap(),
    val lastActiveDate: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "xp_transactions",
    indices = [
        Index(value = ["sourceId"], unique = true),
        Index(value = ["userId", "timestamp"])
    ]
)
data class XPTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val source: String,
    val sourceId: String,
    val xp: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_achievements",
    indices = [
        Index(value = ["userId", "badgeId"], unique = true)
    ]
)
data class UserAchievementEntity(
    @PrimaryKey val id: String, // "${userId}_${badgeId}"
    val userId: String,
    val badgeId: String,
    val title: String,
    val description: String,
    val category: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "leaderboard_history",
    indices = [
        Index(value = ["userId", "periodType", "periodLabel"])
    ]
)
data class LeaderboardHistoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val periodType: String, // "WEEKLY", "MONTHLY"
    val periodLabel: String,
    val rank: Int,
    val xp: Int,
    val accuracyPercentage: Float,
    val league: String,
    val badgeEarned: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "leaderboard_config")
data class LeaderboardConfigEntity(
    @PrimaryKey val id: String = "default_config",
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
