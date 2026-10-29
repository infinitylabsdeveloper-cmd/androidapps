package com.example.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enterprise Firestore Collection names and subcollections for the Leaderboard System.
 * Adheres to standard Firestore multi-tenant, high-performance partitioning rules.
 */
object LeaderboardFirestoreCollections {
    const val LEADERBOARD_ENTRIES = "leaderboard_entries"
    const val USER_STATS = "user_stats"
    const val LEAGUES = "leagues"
    const val ACHIEVEMENTS = "achievements"
    const val USER_ACHIEVEMENTS = "user_achievements"
    const val LEADERBOARD_CONFIG = "leaderboard_config"
    const val XP_TRANSACTIONS = "xp_transactions"
    const val LEADERBOARD_PERIODS = "leaderboard_periods"
}

/**
 * Helper utility to generate deterministic period keys for high-performance indexing.
 */
object LeaderboardPeriodUtils {
    fun getWeekKey(date: Date = Date()): String {
        val cal = Calendar.getInstance().apply { time = date }
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return String.format(Locale.US, "%d-W%02d", year, week)
    }

    fun getMonthKey(date: Date = Date()): String {
        return SimpleDateFormat("yyyy-MM", Locale.US).format(date)
    }

    fun getGlobalKey(): String = "all-time"

    fun buildEntryDocumentId(examId: String, periodType: String, periodKey: String, userId: String): String {
        val cleanUser = userId.replace("/", "_").replace(".", "_")
        return "${examId}_${periodType}_${periodKey}_${cleanUser}"
    }

    fun buildUserStatsDocumentId(examId: String, userId: String): String {
        val cleanUser = userId.replace("/", "_").replace(".", "_")
        return "${examId}_${cleanUser}"
    }
}

/**
 * 1. LEADERBOARD ENTRY COLLECTION: 'leaderboard_entries'
 * 
 * High-performance ranking document optimized for indexed query execution across
 * Global, Weekly, Monthly, Exam, and Subject filters without table scans.
 * Links directly with existing user IDs and exam IDs.
 */
@IgnoreExtraProperties
data class FirestoreLeaderboardEntry(
    @DocumentId
    val entryId: String = "",

    @PropertyName("userId")
    val userId: String = "",

    @PropertyName("examId")
    val examId: String = "appsc-group2",

    @PropertyName("targetExam")
    val targetExam: String = "APPSC Group-2",

    @PropertyName("periodType")
    val periodType: String = "GLOBAL", // "GLOBAL", "WEEKLY", "MONTHLY", "SUBJECT"

    @PropertyName("periodKey")
    val periodKey: String = "all-time", // "all-time", "2026-W40", "2026-09", "subject_history"

    @PropertyName("district")
    val district: String = "Visakhapatnam",

    @PropertyName("subjectId")
    val subjectId: String? = null,

    @PropertyName("displayName")
    val displayName: String = "",

    @PropertyName("avatarColorHex")
    val avatarColorHex: Long = 0xFF2563EB,

    @PropertyName("avatarUrl")
    val avatarUrl: String? = null,

    @PropertyName("privacyMode")
    val privacyMode: String = "PUBLIC", // "PUBLIC", "DISPLAY_NAME", "ANONYMOUS"

    @PropertyName("xp")
    val xp: Int = 0,

    @PropertyName("leaderboardScore")
    val leaderboardScore: Double = 0.0,

    @PropertyName("rank")
    val rank: Int = 0,

    @PropertyName("previousRank")
    val previousRank: Int = 0,

    @PropertyName("rankDelta")
    val rankDelta: Int = 0,

    @PropertyName("accuracyPercentage")
    val accuracyPercentage: Float = 0.0f,

    @PropertyName("testsCompleted")
    val testsCompleted: Int = 0,

    @PropertyName("questionsAttempted")
    val questionsAttempted: Int = 0,

    @PropertyName("correctAnswers")
    val correctAnswers: Int = 0,

    @PropertyName("streakDays")
    val streakDays: Int = 0,

    @PropertyName("league")
    val league: String = "BRONZE", // "BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND"

    @PropertyName("badges")
    val badges: List<String> = emptyList(),

    @PropertyName("isVerified")
    val isVerified: Boolean = true,

    @PropertyName("lastVerifiedTxId")
    val lastVerifiedTxId: String = "",

    @PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @PropertyName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomainCandidate(currentUserId: String): LeaderboardCandidate {
        val domainLeague = try {
            ExamLeague.valueOf(league)
        } catch (_: Exception) {
            ExamLeague.BRONZE
        }
        val domainPrivacy = try {
            LeaderboardPrivacyMode.valueOf(privacyMode)
        } catch (_: Exception) {
            LeaderboardPrivacyMode.PUBLIC
        }

        return LeaderboardCandidate(
            userId = userId,
            rank = rank,
            previousRank = previousRank,
            displayName = displayName,
            avatarColorHex = avatarColorHex,
            avatarUrl = avatarUrl,
            appId = examId,
            targetExam = targetExam,
            district = district,
            xp = xp,
            weeklyXp = if (periodType == "WEEKLY") xp else 0,
            monthlyXp = if (periodType == "MONTHLY") xp else 0,
            leaderboardScore = leaderboardScore,
            accuracyPercentage = accuracyPercentage,
            testsCompleted = testsCompleted,
            questionsAttempted = questionsAttempted,
            correctAnswers = correctAnswers,
            streakDays = streakDays,
            longestStreak = streakDays,
            league = domainLeague,
            badges = badges,
            isCurrentUser = userId == currentUserId,
            privacyMode = domainPrivacy,
            rankDelta = rankDelta,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromCandidate(
            candidate: LeaderboardCandidate,
            periodType: String = "GLOBAL",
            periodKey: String = "all-time",
            subjectId: String? = null
        ): FirestoreLeaderboardEntry {
            val docId = LeaderboardPeriodUtils.buildEntryDocumentId(
                examId = candidate.appId,
                periodType = periodType,
                periodKey = periodKey,
                userId = candidate.userId
            )
            val periodXp = when (periodType) {
                "WEEKLY" -> candidate.weeklyXp.takeIf { it > 0 } ?: candidate.xp
                "MONTHLY" -> candidate.monthlyXp.takeIf { it > 0 } ?: candidate.xp
                else -> candidate.xp
            }

            return FirestoreLeaderboardEntry(
                entryId = docId,
                userId = candidate.userId,
                examId = candidate.appId,
                targetExam = candidate.targetExam,
                periodType = periodType,
                periodKey = periodKey,
                district = candidate.district,
                subjectId = subjectId,
                displayName = candidate.displayName,
                avatarColorHex = candidate.avatarColorHex,
                avatarUrl = candidate.avatarUrl,
                privacyMode = candidate.privacyMode.name,
                xp = periodXp,
                leaderboardScore = candidate.leaderboardScore,
                rank = candidate.rank,
                previousRank = candidate.previousRank,
                rankDelta = candidate.rankDelta,
                accuracyPercentage = candidate.accuracyPercentage,
                testsCompleted = candidate.testsCompleted,
                questionsAttempted = candidate.questionsAttempted,
                correctAnswers = candidate.correctAnswers,
                streakDays = candidate.streakDays,
                league = candidate.league.name,
                badges = candidate.badges,
                isVerified = true,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}

/**
 * 2. USER STATS COLLECTION: 'user_stats'
 * 
 * Cumulative, verified lifetime & cyclical performance statistics for a specific candidate.
 * Document ID format: '{examId}_{userId}' ensures 1:1 linkage to existing user IDs and exam data.
 */
@IgnoreExtraProperties
data class FirestoreUserStats(
    @DocumentId
    val statsId: String = "",

    @PropertyName("userId")
    val userId: String = "",

    @PropertyName("examId")
    val examId: String = "appsc-group2",

    @PropertyName("targetExam")
    val targetExam: String = "APPSC Group-2",

    @PropertyName("district")
    val district: String = "Visakhapatnam",

    @PropertyName("totalXp")
    val totalXp: Int = 0,

    @PropertyName("weeklyXp")
    val weeklyXp: Int = 0,

    @PropertyName("monthlyXp")
    val monthlyXp: Int = 0,

    @PropertyName("currentWeekKey")
    val currentWeekKey: String = LeaderboardPeriodUtils.getWeekKey(),

    @PropertyName("currentMonthKey")
    val currentMonthKey: String = LeaderboardPeriodUtils.getMonthKey(),

    @PropertyName("questionsAttempted")
    val questionsAttempted: Int = 0,

    @PropertyName("correctAnswers")
    val correctAnswers: Int = 0,

    @PropertyName("overallAccuracy")
    val overallAccuracy: Float = 0.0f,

    @PropertyName("testsCompleted")
    val testsCompleted: Int = 0,

    @PropertyName("mockTestsCompleted")
    val mockTestsCompleted: Int = 0,

    @PropertyName("practiceSetsCompleted")
    val practiceSetsCompleted: Int = 0,

    @PropertyName("currentStreak")
    val currentStreak: Int = 0,

    @PropertyName("longestStreak")
    val longestStreak: Int = 0,

    @PropertyName("lastActiveDate")
    val lastActiveDate: String = "",

    @PropertyName("lastActiveTimestamp")
    val lastActiveTimestamp: Long = System.currentTimeMillis(),

    @PropertyName("currentLeague")
    val currentLeague: String = "BRONZE",

    @PropertyName("leagueProgress")
    val leagueProgress: Float = 0.0f,

    @PropertyName("subjectXp")
    val subjectXp: Map<String, Int> = emptyMap(),

    @PropertyName("subjectAccuracy")
    val subjectAccuracy: Map<String, Float> = emptyMap(),

    @PropertyName("unlockedBadgeIds")
    val unlockedBadgeIds: List<String> = emptyList(),

    @PropertyName("antiCheatAuditFlags")
    val antiCheatAuditFlags: Int = 0,

    @PropertyName("privacyMode")
    val privacyMode: String = "PUBLIC",

    @PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @PropertyName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 3. LEAGUE COLLECTION: 'leagues'
 * 
 * Competitive Tier configuration and status.
 * Document ID format: 'bronze', 'silver', 'gold', 'platinum', 'diamond'.
 */
@IgnoreExtraProperties
data class FirestoreLeague(
    @DocumentId
    val leagueId: String = "",

    @PropertyName("tier")
    val tier: String = "BRONZE", // "BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND"

    @PropertyName("title")
    val title: String = "Bronze League",

    @PropertyName("iconEmoji")
    val iconEmoji: String = "🥉",

    @PropertyName("minXp")
    val minXp: Int = 0,

    @PropertyName("maxXp")
    val maxXp: Int? = 999,

    @PropertyName("colorHex")
    val colorHex: Long = 0xFFCD7F32,

    @PropertyName("accentHex")
    val accentHex: Long = 0xFFB45309,

    @PropertyName("orderIndex")
    val orderIndex: Int = 1,

    @PropertyName("promotionThresholdPercentile")
    val promotionThresholdPercentile: Float = 0.15f, // Top 15% get promoted

    @PropertyName("demotionThresholdPercentile")
    val demotionThresholdPercentile: Float = 0.10f, // Bottom 10% get demoted

    @PropertyName("totalCandidatesCount")
    val totalCandidatesCount: Int = 0,

    @PropertyName("description")
    val description: String = "Foundation competitive division for all state aspirants.",

    @PropertyName("perks")
    val perks: List<String> = listOf("Standard ranking access", "Weekly challenge participation"),

    @PropertyName("isActive")
    val isActive: Boolean = true,

    @PropertyName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun getDefaultLeagues(): List<FirestoreLeague> = listOf(
            FirestoreLeague(
                leagueId = "BRONZE",
                tier = "BRONZE",
                title = "Bronze League",
                iconEmoji = "🥉",
                minXp = 0,
                maxXp = 999,
                colorHex = 0xFFCD7F32,
                accentHex = 0xFFB45309,
                orderIndex = 1,
                description = "Foundation competitive division for all state exam aspirants.",
                perks = listOf("Daily test ranking", "Basic achievement badges")
            ),
            FirestoreLeague(
                leagueId = "SILVER",
                tier = "SILVER",
                title = "Silver League",
                iconEmoji = "🥈",
                minXp = 1000,
                maxXp = 2999,
                colorHex = 0xFF94A3B8,
                accentHex = 0xFF64748B,
                orderIndex = 2,
                description = "Consistent performers maintaining daily practice habits.",
                perks = listOf("District rank breakdown", "Silver league badge")
            ),
            FirestoreLeague(
                leagueId = "GOLD",
                tier = "GOLD",
                title = "Gold League",
                iconEmoji = "🥇",
                minXp = 3000,
                maxXp = 5999,
                colorHex = 0xFFF59E0B,
                accentHex = 0xFFD97706,
                orderIndex = 3,
                description = "High-performing contenders with proven accuracy.",
                perks = listOf("State-wide podium access", "Gold profile border")
            ),
            FirestoreLeague(
                leagueId = "PLATINUM",
                tier = "PLATINUM",
                title = "Platinum League",
                iconEmoji = "🏆",
                minXp = 6000,
                maxXp = 9999,
                colorHex = 0xFF0D9488,
                accentHex = 0xFF0F766E,
                orderIndex = 4,
                description = "Elite aspirants demonstrating mock test mastery.",
                perks = listOf("Advanced performance diagnostics", "Elite challenge rewards")
            ),
            FirestoreLeague(
                leagueId = "DIAMOND",
                tier = "DIAMOND",
                title = "Diamond League",
                iconEmoji = "💎",
                minXp = 10000,
                maxXp = null,
                colorHex = 0xFF38BDF8,
                accentHex = 0xFF0284C7,
                orderIndex = 5,
                description = "The pinnacle tier of competitive state service candidates.",
                perks = listOf("Top 1% State Aspirant recognition", "Permanent hall of fame listing")
            )
        )
    }
}

/**
 * 4. ACHIEVEMENT COLLECTION: 'achievements'
 * 
 * Server-verified official badges awarded to candidates.
 * Document ID format: '{badgeId}' (e.g. 'ACC_95', 'STREAK_30', 'MOCK_CHAMP').
 */
@IgnoreExtraProperties
data class FirestoreAchievement(
    @DocumentId
    val achievementId: String = "",

    @PropertyName("badgeId")
    val badgeId: String = "",

    @PropertyName("title")
    val title: String = "",

    @PropertyName("description")
    val description: String = "",

    @PropertyName("icon")
    val icon: String = "🏅",

    @PropertyName("category")
    val category: String = "COMPETITIVE", // "ACCURACY", "STREAK", "QUESTIONS", "LEAGUE", "MOCK_TEST"

    @PropertyName("criteriaType")
    val criteriaType: String = "", // "ACCURACY_PERCENTAGE", "STREAK_DAYS", "QUESTIONS_COUNT", "LEAGUE_TIER", "TESTS_COUNT"

    @PropertyName("thresholdValue")
    val thresholdValue: Double = 0.0,

    @PropertyName("xpReward")
    val xpReward: Int = 100,

    @PropertyName("rarity")
    val rarity: String = "COMMON", // "COMMON", "RARE", "EPIC", "LEGENDARY"

    @PropertyName("totalUnlockedCount")
    val totalUnlockedCount: Int = 0,

    @PropertyName("isLive")
    val isLive: Boolean = true,

    @PropertyName("orderIndex")
    val orderIndex: Int = 1,

    @PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun getDefaultAchievements(): List<FirestoreAchievement> = listOf(
            FirestoreAchievement(
                achievementId = "ACC_95",
                badgeId = "ACC_95",
                title = "🎯 95% Accuracy Master",
                description = "Maintain 95% or higher accuracy across 50+ attempted questions",
                icon = "🎯",
                category = "ACCURACY",
                criteriaType = "ACCURACY_PERCENTAGE",
                thresholdValue = 95.0,
                xpReward = 150,
                rarity = "EPIC",
                orderIndex = 1
            ),
            FirestoreAchievement(
                achievementId = "STREAK_30",
                badgeId = "STREAK_30",
                title = "🔥 30-Day Streak Titan",
                description = "Practice continuously for 30 consecutive calendar days",
                icon = "🔥",
                category = "STREAK",
                criteriaType = "STREAK_DAYS",
                thresholdValue = 30.0,
                xpReward = 300,
                rarity = "LEGENDARY",
                orderIndex = 2
            ),
            FirestoreAchievement(
                achievementId = "STREAK_14",
                badgeId = "STREAK_14",
                title = "⚡ 14-Day Consistency Pro",
                description = "Complete practice sessions for 14 days without breaking streak",
                icon = "⚡",
                category = "STREAK",
                criteriaType = "STREAK_DAYS",
                thresholdValue = 14.0,
                xpReward = 100,
                rarity = "RARE",
                orderIndex = 3
            ),
            FirestoreAchievement(
                achievementId = "QUESTIONS_1000",
                badgeId = "QUESTIONS_1000",
                title = "📚 1,000 Questions Solved",
                description = "Solve and verify 1,000 high-yield questions in syllabus",
                icon = "📚",
                category = "QUESTIONS",
                criteriaType = "QUESTIONS_COUNT",
                thresholdValue = 1000.0,
                xpReward = 250,
                rarity = "EPIC",
                orderIndex = 4
            ),
            FirestoreAchievement(
                achievementId = "MOCK_CHAMP",
                badgeId = "MOCK_CHAMP",
                title = "🏆 Mock Test Champion",
                description = "Complete 15+ full-length verified exam mock tests",
                icon = "🏆",
                category = "MOCK_TEST",
                criteriaType = "TESTS_COUNT",
                thresholdValue = 15.0,
                xpReward = 200,
                rarity = "RARE",
                orderIndex = 5
            ),
            FirestoreAchievement(
                achievementId = "DIAMOND_LEAGUE",
                badgeId = "DIAMOND_LEAGUE",
                title = "💎 Diamond League Elite",
                description = "Reach the coveted Diamond League tier with 10,000+ XP",
                icon = "💎",
                category = "LEAGUE",
                criteriaType = "LEAGUE_TIER",
                thresholdValue = 10000.0,
                xpReward = 500,
                rarity = "LEGENDARY",
                orderIndex = 6
            ),
            FirestoreAchievement(
                achievementId = "TOP_1_PERCENT",
                badgeId = "TOP_1_PERCENT",
                title = "🏅 Top 1% State Aspirant",
                description = "Achieve a Top 10 rank in the overall state competitive leaderboard",
                icon = "🏅",
                category = "LEAGUE",
                criteriaType = "RANK_THRESHOLD",
                thresholdValue = 10.0,
                xpReward = 400,
                rarity = "LEGENDARY",
                orderIndex = 7
            )
        )
    }
}

/**
 * 5. USER ACHIEVEMENT COLLECTION / SUBCOLLECTION: 'user_achievements'
 * 
 * Records the unlock event for a specific user and badge.
 * Document ID format: '{userId}_{badgeId}'
 */
@IgnoreExtraProperties
data class FirestoreUserAchievement(
    @DocumentId
    val id: String = "",

    @PropertyName("userId")
    val userId: String = "",

    @PropertyName("badgeId")
    val badgeId: String = "",

    @PropertyName("title")
    val title: String = "",

    @PropertyName("category")
    val category: String = "COMPETITIVE",

    @PropertyName("unlockedAt")
    val unlockedAt: Long = System.currentTimeMillis(),

    @PropertyName("verifiedTransactionId")
    val verifiedTransactionId: String = "",

    @PropertyName("xpAwarded")
    val xpAwarded: Int = 0
)
