package com.example.repository

import android.content.Context
import com.example.db.AppDatabase
import com.example.db.UserProgressEntity
import com.example.model.AppConfiguration
import com.example.model.MockTestPerformanceSummary
import com.example.model.UserProgress
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Room Database Repository dedicated to persisting User Progress,
 * Study Streaks, Daily Practice Quotas, and Mock Test Analytics.
 * Aligns with the AppConfiguration pattern for multi-app / multi-exam tenants.
 */
class UserProgressRepository(
    private val database: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val _milestoneEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val milestoneEvents: SharedFlow<String> = _milestoneEvents.asSharedFlow()

    private val progressDao = database.userProgressDao()
    private val testResultDao = database.testResultDao()

    /**
     * Observes real-time user progress for the specified app ID or active AppConfiguration.
     */
    fun getUserProgress(appId: String = AppConfigRepository.getInstance().getCurrentConfig().appId): Flow<UserProgress> {
        return progressDao.getUserProgress(appId).map { entity ->
            entity?.toDomainModel() ?: UserProgress(appId = appId)
        }.flowOn(ioDispatcher)
    }

    /**
     * Records an answered question, calculating XP, daily target progression, and accuracy.
     */
    suspend fun recordQuestionAnswered(
        appId: String,
        isCorrect: Boolean,
        xpEarned: Int = if (isCorrect) 15 else 5,
        timeSpentSeconds: Int = 30
    ) = withContext(ioDispatcher) {
        val todayStr = getTodayDateString()
        val current = progressDao.getUserProgressDirect(appId) ?: UserProgressEntity(appId = appId, lastActiveDate = todayStr)

        val isNewDay = current.lastActiveDate != todayStr
        val updatedStreak = when {
            isNewDay && isConsecutiveDay(current.lastActiveDate, todayStr) -> current.streakDays + 1
            isNewDay -> 1
            else -> current.streakDays
        }

        if (updatedStreak == 7 && current.streakDays < 7) {
            _milestoneEvents.tryEmit("STREAK_7_DAYS")
        }

        val updatedDailyCount = if (isNewDay) 1 else current.dailyQuestionsAnswered + 1
        val updatedTotalQuestions = current.completedQuestionsCount + 1
        val updatedXp = current.totalXp + xpEarned
        val updatedTimeSpent = current.totalTimeSpentSeconds + timeSpentSeconds

        val updatedEntity = current.copy(
            streakDays = updatedStreak,
            totalXp = updatedXp,
            dailyQuestionsAnswered = updatedDailyCount,
            completedQuestionsCount = updatedTotalQuestions,
            totalTimeSpentSeconds = updatedTimeSpent,
            lastActiveDate = todayStr,
            updatedAt = System.currentTimeMillis()
        )

        progressDao.upsertProgress(updatedEntity)
    }

    /**
     * Marks a study topic as completed.
     */
    suspend fun completeTopic(appId: String, topicId: String) = withContext(ioDispatcher) {
        val current = progressDao.getUserProgressDirect(appId) ?: UserProgressEntity(appId = appId)
        if (!current.completedTopicIds.contains(topicId)) {
            val updatedList = current.completedTopicIds + topicId
            val updatedEntity = current.copy(
                completedTopicIds = updatedList,
                totalXp = current.totalXp + 50,
                updatedAt = System.currentTimeMillis()
            )
            progressDao.upsertProgress(updatedEntity)
        }
    }

    /**
     * Marks a study unit as completed.
     */
    suspend fun completeUnit(appId: String, unitId: String) = withContext(ioDispatcher) {
        val current = progressDao.getUserProgressDirect(appId) ?: UserProgressEntity(appId = appId)
        if (!current.completedUnitIds.contains(unitId)) {
            val updatedList = current.completedUnitIds + unitId
            val updatedEntity = current.copy(
                completedUnitIds = updatedList,
                totalXp = current.totalXp + 150,
                updatedAt = System.currentTimeMillis()
            )
            progressDao.upsertProgress(updatedEntity)
        }
    }

    /**
     * Updates daily question goal for the specified app configuration.
     */
    suspend fun updateDailyTarget(appId: String, targetQuestions: Int) = withContext(ioDispatcher) {
        val current = progressDao.getUserProgressDirect(appId) ?: UserProgressEntity(appId = appId)
        val updated = current.copy(
            dailyTargetQuestions = targetQuestions.coerceAtLeast(5),
            updatedAt = System.currentTimeMillis()
        )
        progressDao.upsertProgress(updated)
    }

    /**
     * Records mock test completion and updates comprehensive stats in Room.
     */
    suspend fun recordMockTestCompletion(
        appId: String,
        scorePercentage: Float,
        questionsCount: Int,
        correctCount: Int,
        timeSpentSeconds: Int
    ) = withContext(ioDispatcher) {
        val todayStr = getTodayDateString()
        val current = progressDao.getUserProgressDirect(appId) ?: UserProgressEntity(appId = appId, lastActiveDate = todayStr)

        val xpAwarded = (correctCount * 10) + (if (scorePercentage >= 70f) 100 else 40)
        val isNewDay = current.lastActiveDate != todayStr
        val updatedStreak = when {
            isNewDay && isConsecutiveDay(current.lastActiveDate, todayStr) -> current.streakDays + 1
            isNewDay -> 1
            else -> current.streakDays
        }

        val updatedTestsCount = current.testsCompletedCount + 1
        val updatedTotalQuestions = current.completedQuestionsCount + questionsCount
        val updatedDailyCount = if (isNewDay) questionsCount else current.dailyQuestionsAnswered + questionsCount
        val updatedXp = current.totalXp + xpAwarded
        val updatedTimeSpent = current.totalTimeSpentSeconds + timeSpentSeconds

        val updatedEntity = current.copy(
            streakDays = updatedStreak,
            totalXp = updatedXp,
            dailyQuestionsAnswered = updatedDailyCount,
            completedQuestionsCount = updatedTotalQuestions,
            testsCompletedCount = updatedTestsCount,
            totalTimeSpentSeconds = updatedTimeSpent,
            lastActiveDate = todayStr,
            updatedAt = System.currentTimeMillis()
        )

        progressDao.upsertProgress(updatedEntity)
    }

    /**
     * Returns an aggregate summary of mock test performance for the active AppConfiguration.
     */
    fun getPerformanceSummary(appId: String): Flow<MockTestPerformanceSummary> {
        return combine(
            testResultDao.getTestCount(appId),
            testResultDao.getAverageScorePercentage(appId),
            testResultDao.getMaxScorePercentage(appId),
            testResultDao.getTotalQuestionsAttempted(appId),
            testResultDao.getTotalCorrectAnswers(appId)
        ) { testCount, avgScore, maxScore, totalQuestions, totalCorrect ->
            val attempts = totalQuestions ?: 0
            val corrects = (totalCorrect ?: 0).coerceIn(0, attempts.coerceAtLeast(1))
            val accuracyPct = if (attempts > 0) ((corrects.toDouble() / attempts) * 100).toInt().coerceIn(0, 100) else 0
            val accuracy = "$accuracyPct%"

            MockTestPerformanceSummary(
                totalTestsTaken = testCount,
                averageScorePercentage = avgScore ?: 0f,
                bestScorePercentage = maxScore ?: 0f,
                totalQuestionsAttempted = attempts,
                totalCorrectAnswers = corrects,
                overallAccuracy = accuracy
            )
        }.flowOn(ioDispatcher)
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun isConsecutiveDay(previousDate: String, currentDate: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val prev = sdf.parse(previousDate)?.time ?: return false
            val curr = sdf.parse(currentDate)?.time ?: return false
            val diffDays = (curr - prev) / (1000 * 60 * 60 * 24)
            diffDays == 1L
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserProgressRepository? = null

        fun getInstance(context: Context): UserProgressRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                INSTANCE ?: UserProgressRepository(db).also { INSTANCE = it }
            }
        }
    }
}

// Mapper extension function
fun UserProgressEntity.toDomainModel(): UserProgress {
    return UserProgress(
        appId = appId,
        streakDays = streakDays,
        totalXp = totalXp,
        dailyTargetQuestions = dailyTargetQuestions,
        dailyQuestionsAnswered = dailyQuestionsAnswered,
        lastActiveDate = lastActiveDate,
        completedQuestionsCount = completedQuestionsCount,
        testsCompletedCount = testsCompletedCount,
        totalTimeSpentSeconds = totalTimeSpentSeconds,
        accuracyPercentage = accuracyPercentage,
        completedTopicIds = completedTopicIds,
        completedUnitIds = completedUnitIds,
        savedTopicIds = savedTopicIds,
        updatedAt = updatedAt
    )
}

fun UserProgress.toEntity(): UserProgressEntity {
    return UserProgressEntity(
        appId = appId,
        streakDays = streakDays,
        totalXp = totalXp,
        dailyTargetQuestions = dailyTargetQuestions,
        dailyQuestionsAnswered = dailyQuestionsAnswered,
        lastActiveDate = lastActiveDate,
        completedQuestionsCount = completedQuestionsCount,
        testsCompletedCount = testsCompletedCount,
        totalTimeSpentSeconds = totalTimeSpentSeconds,
        accuracyPercentage = accuracyPercentage,
        completedTopicIds = completedTopicIds,
        completedUnitIds = completedUnitIds,
        savedTopicIds = savedTopicIds,
        updatedAt = updatedAt
    )
}
