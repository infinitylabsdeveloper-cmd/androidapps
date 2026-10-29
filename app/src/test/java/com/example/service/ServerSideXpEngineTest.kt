package com.example.service

import com.example.db.XPTransactionDao
import com.example.db.XPTransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class FakeXPTransactionDao : XPTransactionDao {
    private val transactions = mutableListOf<XPTransactionEntity>()

    override suspend fun hasTransaction(sourceId: String): Boolean {
        return transactions.any { it.sourceId == sourceId }
    }

    override suspend fun insertTransaction(transaction: XPTransactionEntity): Long {
        if (hasTransaction(transaction.sourceId)) return -1L
        transactions.add(transaction)
        return transactions.size.toLong()
    }

    override suspend fun getUserTotalXp(userId: String): Int? {
        return transactions.filter { it.userId == userId }.sumOf { it.xp }
    }

    override suspend fun getTransactionBySourceId(sourceId: String): XPTransactionEntity? {
        return transactions.find { it.sourceId == sourceId }
    }

    override suspend fun getXpEarnedSince(userId: String, sinceTimestamp: Long): Int? {
        return transactions.filter { it.userId == userId && it.timestamp >= sinceTimestamp }.sumOf { it.xp }
    }

    override fun getUserRecentTransactions(userId: String, limit: Int): Flow<List<XPTransactionEntity>> {
        return flowOf(transactions.filter { it.userId == userId }.takeLast(limit))
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ServerSideXpEngineTest {

    private lateinit var fakeDao: FakeXPTransactionDao
    private lateinit var xpEngine: ServerSideXpEngine

    @Before
    fun setUp() {
        fakeDao = FakeXPTransactionDao()
        xpEngine = ServerSideXpEngine.createForTesting(
            xpTransactionDao = fakeDao,
            firestore = null,
            customConfig = XpEngineConfig(
                accuracyWeight = 0.3,
                mockTestWeight = 0.4,
                consistencyWeight = 0.3,
                xpPerCorrectAnswer = 10,
                xpPerMockTestCompletion = 50,
                xpPerDailyStreak = 20,
                minSecondsPerQuestion = 1.0,
                maxHourlyXpCap = 2500,
                isAntiCheatStrict = true
            )
        )
    }

    @Test
    fun testWeightedXpCalculation_HighAccuracyAndMockBonus() {
        val request = XpVerificationRequest(
            userId = "test_user_01",
            attemptId = "mock_attempt_101",
            examId = "appsc-group2",
            assessmentType = AssessmentType.MOCK_TEST,
            totalQuestions = 50,
            correctAnswers = 48, // 96% accuracy
            durationSeconds = 1800, // 30 mins
            currentStreakDays = 7 // 7-day habit
        )

        val breakdown = xpEngine.computeWeightedXp(request)

        // Base XP: 48 * 10 = 480
        assertEquals(480, breakdown.baseXp)
        // Accuracy ratio: 0.96
        assertEquals(0.96f, breakdown.accuracyRatio, 0.01f)
        // Accuracy Bonus: 480 * 0.3 * 1.5 = 216
        assertEquals(216, breakdown.accuracyBonus)
        // Mock Performance Bonus: 50 * 0.4 * 1.5 (length >= 50) * 1.4 (accuracy >= 80%) = 42
        assertEquals(42, breakdown.mockPerformanceBonus)
        // Consistency Bonus: 20 * 0.3 * 1.25 (7 days) = 8
        assertEquals(8, breakdown.consistencyBonus)
        // Total: 480 + 216 + 42 + 8 = 746
        assertEquals(746, breakdown.totalCalculatedXp)
    }

    @Test
    fun testIdempotency_DuplicateAttemptIsRejected() = runBlocking {
        val request = XpVerificationRequest(
            userId = "test_user_02",
            attemptId = "attempt_unique_99",
            examId = "appsc-group2",
            assessmentType = AssessmentType.MOCK_TEST,
            totalQuestions = 25,
            correctAnswers = 20,
            durationSeconds = 600,
            currentStreakDays = 3
        )

        // First attempt: Must succeed
        val firstResult = xpEngine.verifyAndCalculateXp(request)
        assertTrue("First attempt should be Success", firstResult is XpCalculationResult.Success)
        val success = firstResult as XpCalculationResult.Success
        assertTrue(success.xpAwarded > 0)

        // Second attempt with exact same parameters: Must be rejected as duplicate
        val secondResult = xpEngine.verifyAndCalculateXp(request)
        assertTrue("Second identical attempt should be RejectedDuplicate", secondResult is XpCalculationResult.RejectedDuplicate)
        val duplicate = secondResult as XpCalculationResult.RejectedDuplicate
        assertEquals(success.idempotencyKey, duplicate.idempotencyKey)
    }

    @Test
    fun testAntiCheat_SpeedBotAnomalyRejected() = runBlocking {
        val botRequest = XpVerificationRequest(
            userId = "bot_user_01",
            attemptId = "bot_attempt_01",
            examId = "appsc-group2",
            assessmentType = AssessmentType.MOCK_TEST,
            totalQuestions = 50,
            correctAnswers = 50,
            durationSeconds = 5, // Humanly impossible: 5 seconds for 50 questions
            currentStreakDays = 1
        )

        val result = xpEngine.verifyAndCalculateXp(botRequest)
        assertTrue("Speed anomaly should be rejected", result is XpCalculationResult.RejectedAntiCheat)
        val antiCheat = result as XpCalculationResult.RejectedAntiCheat
        assertEquals(AntiCheatStatus.REJECTED_SPEED_ANOMALY, antiCheat.status)
    }

    @Test
    fun testAntiCheat_ImpossibleScoreRejected() = runBlocking {
        val impossibleRequest = XpVerificationRequest(
            userId = "cheater_01",
            attemptId = "attempt_cheat_01",
            examId = "appsc-group2",
            assessmentType = AssessmentType.MOCK_TEST,
            totalQuestions = 30,
            correctAnswers = 35, // Impossible: correct > total
            durationSeconds = 900
        )

        val result = xpEngine.verifyAndCalculateXp(impossibleRequest)
        assertTrue(result is XpCalculationResult.RejectedAntiCheat)
        val antiCheat = result as XpCalculationResult.RejectedAntiCheat
        assertEquals(AntiCheatStatus.REJECTED_IMPOSSIBLE_SCORE, antiCheat.status)
    }

    @Test
    fun testConfigurableWeightsDynamicUpdate() {
        val request = XpVerificationRequest(
            userId = "user_weights_test",
            attemptId = "attempt_weights",
            assessmentType = AssessmentType.MOCK_TEST,
            totalQuestions = 20,
            correctAnswers = 18,
            durationSeconds = 400,
            currentStreakDays = 14
        )

        // Default weights: accuracyWeight = 0.3, consistencyWeight = 0.3
        val initial = xpEngine.computeWeightedXp(request)

        // Update weights dynamically: prioritize consistency over accuracy
        xpEngine.updateConfig(
            XpEngineConfig(
                accuracyWeight = 0.1,
                consistencyWeight = 0.8,
                mockTestWeight = 0.1
            )
        )

        val updated = xpEngine.computeWeightedXp(request)

        assertTrue(updated.accuracyBonus < initial.accuracyBonus)
        assertTrue(updated.consistencyBonus > initial.consistencyBonus)
    }
}
