package com.example.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaderboardDao {

    @Query("SELECT * FROM leaderboard_entries WHERE appId = :appId ORDER BY leaderboardScore DESC LIMIT :limit")
    fun getOverallLeaderboard(appId: String, limit: Int = 100): Flow<List<LeaderboardEntryEntity>>

    @Query("SELECT * FROM leaderboard_entries WHERE appId = :appId ORDER BY weeklyXp DESC LIMIT :limit")
    fun getWeeklyLeaderboard(appId: String, limit: Int = 100): Flow<List<LeaderboardEntryEntity>>

    @Query("SELECT * FROM leaderboard_entries WHERE appId = :appId ORDER BY monthlyXp DESC LIMIT :limit")
    fun getMonthlyLeaderboard(appId: String, limit: Int = 100): Flow<List<LeaderboardEntryEntity>>

    @Query("SELECT * FROM leaderboard_entries WHERE appId = :appId AND (district = :district OR :district = 'All') ORDER BY leaderboardScore DESC LIMIT :limit")
    fun getDistrictLeaderboard(appId: String, district: String, limit: Int = 100): Flow<List<LeaderboardEntryEntity>>

    @Query("SELECT * FROM leaderboard_entries WHERE userId = :userId LIMIT 1")
    fun getUserEntry(userId: String): Flow<LeaderboardEntryEntity?>

    @Query("SELECT * FROM leaderboard_entries WHERE userId = :userId LIMIT 1")
    suspend fun getUserEntryDirect(userId: String): LeaderboardEntryEntity?

    @Query("SELECT * FROM leaderboard_entries WHERE appId = :appId ORDER BY leaderboardScore DESC LIMIT 3")
    fun getTopThree(appId: String): Flow<List<LeaderboardEntryEntity>>

    @Query("SELECT * FROM leaderboard_entries WHERE appId = :appId ORDER BY leaderboardScore DESC LIMIT :limit OFFSET :offset")
    fun getPagedCandidates(appId: String, limit: Int, offset: Int): Flow<List<LeaderboardEntryEntity>>

    @Query("SELECT COUNT(*) FROM leaderboard_entries WHERE appId = :appId")
    fun getTotalCandidatesCount(appId: String): Flow<Int>

    @Query("SELECT * FROM leaderboard_entries ORDER BY leaderboardScore DESC")
    suspend fun getAllCandidatesDirect(): List<LeaderboardEntryEntity>

    @Query("SELECT * FROM leaderboard_entries ORDER BY leaderboardScore DESC")
    fun getAllCandidates(): Flow<List<LeaderboardEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCandidate(candidate: LeaderboardEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCandidates(candidates: List<LeaderboardEntryEntity>)

    @Query("DELETE FROM leaderboard_entries WHERE userId = :userId")
    suspend fun deleteCandidate(userId: String)

    @Query("DELETE FROM leaderboard_entries")
    suspend fun clearAll()
}

@Dao
interface XPTransactionDao {

    @Query("SELECT EXISTS(SELECT 1 FROM xp_transactions WHERE sourceId = :sourceId)")
    suspend fun hasTransaction(sourceId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: XPTransactionEntity): Long

    @Query("SELECT SUM(xp) FROM xp_transactions WHERE userId = :userId")
    suspend fun getUserTotalXp(userId: String): Int?

    @Query("SELECT * FROM xp_transactions WHERE sourceId = :sourceId LIMIT 1")
    suspend fun getTransactionBySourceId(sourceId: String): XPTransactionEntity?

    @Query("SELECT SUM(xp) FROM xp_transactions WHERE userId = :userId AND timestamp >= :sinceTimestamp")
    suspend fun getXpEarnedSince(userId: String, sinceTimestamp: Long): Int?

    @Query("SELECT * FROM xp_transactions WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getUserRecentTransactions(userId: String, limit: Int = 50): Flow<List<XPTransactionEntity>>
}

@Dao
interface UserAchievementDao {

    @Query("SELECT * FROM user_achievements WHERE userId = :userId ORDER BY unlockedAt DESC")
    fun getUserAchievements(userId: String): Flow<List<UserAchievementEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM user_achievements WHERE userId = :userId AND badgeId = :badgeId)")
    suspend fun hasAchievement(userId: String, badgeId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: UserAchievementEntity)
}

@Dao
interface LeaderboardHistoryDao {

    @Query("SELECT * FROM leaderboard_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserHistory(userId: String): Flow<List<LeaderboardHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: LeaderboardHistoryEntity)
}

@Dao
interface LeaderboardConfigDao {

    @Query("SELECT * FROM leaderboard_config WHERE id = 'default_config' LIMIT 1")
    fun getConfig(): Flow<LeaderboardConfigEntity?>

    @Query("SELECT * FROM leaderboard_config WHERE id = 'default_config' LIMIT 1")
    suspend fun getConfigDirect(): LeaderboardConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(config: LeaderboardConfigEntity)
}
