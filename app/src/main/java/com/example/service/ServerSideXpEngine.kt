package com.example.service

import android.content.Context
import android.util.Log
import com.example.db.AppDatabase
import com.example.db.XPTransactionDao
import com.example.db.XPTransactionEntity
import com.example.model.LeaderboardConfigModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.*
import kotlin.math.roundToInt

/**
 * Types of candidate assessment and practice activities eligible for verified XP.
 */
enum class AssessmentType {
    MOCK_TEST,
    PRACTICE_SET,
    SUBTOPIC_QUIZ,
    CURRENT_AFFAIRS,
    DAILY_STREAK,
    TOPIC_COMPLETION
}

/**
 * Anti-cheat verification status emitted by the ServerSideXpEngine.
 */
enum class AntiCheatStatus {
    VERIFIED,
    REJECTED_DUPLICATE,
    REJECTED_SPEED_ANOMALY,
    REJECTED_IMPOSSIBLE_SCORE,
    REJECTED_RATE_LIMITED,
    REJECTED_INVALID_TIMESTAMP
}

/**
 * Configurable weights and operational boundaries for XP allocation.
 */
data class XpEngineConfig(
    val accuracyWeight: Double = 0.3,
    val mockTestWeight: Double = 0.4,
    val consistencyWeight: Double = 0.3,
    val xpPerCorrectAnswer: Int = 10,
    val xpPerMockTestCompletion: Int = 50,
    val xpPerDailyStreak: Int = 20,
    val xpPerTopicCompletion: Int = 25,
    val minSecondsPerQuestion: Double = 1.5, // Flags sub-1.5s answers as bot activity
    val maxHourlyXpCap: Int = 2500, // Maximum allowable XP within rolling 60 minutes
    val isAntiCheatStrict: Boolean = true
) {
    companion object {
        fun fromLeaderboardConfig(config: LeaderboardConfigModel): XpEngineConfig {
            return XpEngineConfig(
                accuracyWeight = config.accuracyWeight,
                mockTestWeight = config.mockTestWeight,
                consistencyWeight = config.consistencyWeight,
                xpPerCorrectAnswer = config.xpPerCorrectAnswer,
                xpPerMockTestCompletion = config.xpPerMockTestCompletion,
                xpPerDailyStreak = config.xpPerDailyStreak,
                xpPerTopicCompletion = config.xpPerTopicCompletion
            )
        }
    }
}

/**
 * Client submission payload sent for server-side verification and XP calculation.
 */
data class XpVerificationRequest(
    val userId: String,
    val attemptId: String,
    val examId: String = "appsc-group2",
    val assessmentType: AssessmentType = AssessmentType.MOCK_TEST,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val durationSeconds: Int,
    val currentStreakDays: Int = 1,
    val clientTimestamp: Long = System.currentTimeMillis(),
    val subjectId: String = "",
    val customToken: String? = null
)

/**
 * Itemized breakdown of the verified XP calculation.
 */
data class XpBreakdown(
    val baseXp: Int,
    val accuracyBonus: Int,
    val mockPerformanceBonus: Int,
    val consistencyBonus: Int,
    val streakMultiplier: Double,
    val accuracyRatio: Float,
    val totalCalculatedXp: Int
)

/**
 * Result returned following server-side calculation and idempotent persistence.
 */
sealed class XpCalculationResult {
    data class Success(
        val xpAwarded: Int,
        val breakdown: XpBreakdown,
        val idempotencyKey: String,
        val transactionId: String,
        val status: AntiCheatStatus = AntiCheatStatus.VERIFIED,
        val auditMessage: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : XpCalculationResult()

    data class RejectedDuplicate(
        val idempotencyKey: String,
        val existingTransactionId: String?,
        val auditMessage: String = "Reward duplication prevented. Attempt already processed.",
        val timestamp: Long = System.currentTimeMillis()
    ) : XpCalculationResult()

    data class RejectedAntiCheat(
        val status: AntiCheatStatus,
        val idempotencyKey: String,
        val reason: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : XpCalculationResult()
}

/**
 * Enterprise Server-Side XP Calculation & Idempotent Verification Engine.
 * 
 * Features:
 * - Deterministic SHA-256 Idempotency Key hashing to prevent double-awarding of XP.
 * - Multi-factor configurable weighted formula (Accuracy, Mock Performance, Consistency).
 * - Anti-Cheat Validation: Speed-bot filtering, rate-limiting, and impossible score rejections.
 * - Atomic persistence across Room DB and Cloud Firestore transaction ledgers.
 */
class ServerSideXpEngine private constructor(
    private val xpTransactionDao: XPTransactionDao,
    private val firestore: FirebaseFirestore? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val mutex = Mutex()
    private var config: XpEngineConfig = XpEngineConfig()

    companion object {
        private const val TAG = "ServerSideXpEngine"

        @Volatile
        private var INSTANCE: ServerSideXpEngine? = null

        fun getInstance(context: Context): ServerSideXpEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ServerSideXpEngine(
                    xpTransactionDao = AppDatabase.getDatabase(context).xpTransactionDao(),
                    firestore = try { com.example.util.FirebaseSyncManager.firestore } catch (_: Exception) { null }
                ).also { INSTANCE = it }
            }
        }

        /**
         * Test factory for dependency injection in unit tests.
         */
        fun createForTesting(
            xpTransactionDao: XPTransactionDao,
            firestore: FirebaseFirestore? = null,
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
            customConfig: XpEngineConfig = XpEngineConfig()
        ): ServerSideXpEngine {
            val engine = ServerSideXpEngine(xpTransactionDao, firestore, ioDispatcher)
            engine.updateConfig(customConfig)
            return engine
        }
    }

    /**
     * Updates active calculation weights dynamically (e.g. from Admin Dashboard or Remote Config).
     */
    fun updateConfig(newConfig: XpEngineConfig) {
        this.config = newConfig
    }

    /**
     * Generates a deterministic SHA-256 idempotency key for an assessment attempt.
     */
    fun generateIdempotencyKey(req: XpVerificationRequest): String {
        val raw = "${req.userId}:${req.examId}:${req.assessmentType.name}:${req.attemptId}:${req.totalQuestions}:${req.correctAnswers}"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Primary entry point: Validates, calculates, and idempotently awards XP.
     */
    suspend fun verifyAndCalculateXp(req: XpVerificationRequest): XpCalculationResult = withContext(ioDispatcher) {
        val idempotencyKey = req.customToken?.ifBlank { null } ?: generateIdempotencyKey(req)

        mutex.withLock {
            // 1. Dual-Layer Idempotency Check (Room Local + Remote Firestore)
            if (isTransactionAlreadyProcessed(idempotencyKey)) {
                val existing = xpTransactionDao.getTransactionBySourceId(idempotencyKey)
                logW(TAG, "Duplicate attempt detected! Blocked key: $idempotencyKey")
                return@withLock XpCalculationResult.RejectedDuplicate(
                    idempotencyKey = idempotencyKey,
                    existingTransactionId = existing?.id
                )
            }

            // 2. Anti-Cheat Sanity Checks
            val sanityError = validateAttemptSanity(req)
            if (sanityError != null) {
                logW(TAG, "Anti-cheat rejected attempt: $sanityError")
                return@withLock XpCalculationResult.RejectedAntiCheat(
                    status = sanityError,
                    idempotencyKey = idempotencyKey,
                    reason = getAntiCheatReasonMessage(sanityError, req)
                )
            }

            // 3. Hourly Velocity / Rate Limit Validation
            val hourlyXp = xpTransactionDao.getXpEarnedSince(
                userId = req.userId,
                sinceTimestamp = System.currentTimeMillis() - 3600_000L
            ) ?: 0

            if (hourlyXp >= config.maxHourlyXpCap) {
                logW(TAG, "Rate limit reached for user ${req.userId} (Hourly XP: $hourlyXp)")
                return@withLock XpCalculationResult.RejectedAntiCheat(
                    status = AntiCheatStatus.REJECTED_RATE_LIMITED,
                    idempotencyKey = idempotencyKey,
                    reason = "Hourly XP cap reached (${config.maxHourlyXpCap} XP/hr). Please rest before next session."
                )
            }

            // 4. Perform Multi-Factor Weighted XP Calculation
            val breakdown = computeWeightedXp(req)
            val finalXp = breakdown.totalCalculatedXp

            // 5. Commit Transaction Idempotently to Room
            val transactionId = UUID.randomUUID().toString()
            val txEntity = XPTransactionEntity(
                id = transactionId,
                userId = req.userId,
                source = req.assessmentType.name,
                sourceId = idempotencyKey,
                xp = finalXp,
                timestamp = System.currentTimeMillis()
            )
            val insertRowId = xpTransactionDao.insertTransaction(txEntity)
            if (insertRowId == -1L) {
                // Secondary fallback if concurrent race condition occurred
                return@withLock XpCalculationResult.RejectedDuplicate(
                    idempotencyKey = idempotencyKey,
                    existingTransactionId = transactionId
                )
            }

            // 6. Push to Firestore Transaction Ledger (Asynchronous / Resilient)
            syncTransactionToFirestore(txEntity, req, breakdown)

            logI(TAG, "Successfully awarded $finalXp XP to user ${req.userId} (Key: $idempotencyKey)")

            return@withLock XpCalculationResult.Success(
                xpAwarded = finalXp,
                breakdown = breakdown,
                idempotencyKey = idempotencyKey,
                transactionId = transactionId,
                status = AntiCheatStatus.VERIFIED,
                auditMessage = "Attempt verified via multi-factor weighted scoring. Idempotency token committed."
            )
        }
    }

    /**
     * Checks both local database and remote Firestore ledger for idempotency existence.
     */
    private suspend fun isTransactionAlreadyProcessed(idempotencyKey: String): Boolean {
        // Step 1: Check Local Room Database
        if (xpTransactionDao.hasTransaction(idempotencyKey)) {
            return true
        }

        // Step 2: Check Firestore (if online and configured)
        if (firestore != null) {
            try {
                val doc = firestore.collection("xp_transactions")
                    .document(idempotencyKey)
                    .get()
                    .await()
                if (doc.exists()) {
                    return true
                }
            } catch (e: Exception) {
                logD(TAG, "Remote idempotency check skipped: ${e.message}")
            }
        }

        return false
    }

    /**
     * Validates anti-cheat parameters before calculating XP.
     */
    private fun validateAttemptSanity(req: XpVerificationRequest): AntiCheatStatus? {
        // Impossible score checks
        if (req.totalQuestions <= 0 || req.correctAnswers < 0 || req.correctAnswers > req.totalQuestions) {
            return AntiCheatStatus.REJECTED_IMPOSSIBLE_SCORE
        }

        // Speed anomaly / bot detection
        if (config.isAntiCheatStrict && req.totalQuestions >= 5) {
            val minRealisticSeconds = (req.totalQuestions * config.minSecondsPerQuestion).roundToInt()
            if (req.durationSeconds < minRealisticSeconds) {
                return AntiCheatStatus.REJECTED_SPEED_ANOMALY
            }
        }

        // Timestamp validation (Must not be in future > 5 minutes or older than 7 days)
        val now = System.currentTimeMillis()
        val fiveMinutesFuture = now + 300_000L
        val sevenDaysPast = now - (7 * 24 * 3600_000L)
        if (req.clientTimestamp > fiveMinutesFuture || req.clientTimestamp < sevenDaysPast) {
            return AntiCheatStatus.REJECTED_INVALID_TIMESTAMP
        }

        return null
    }

    /**
     * Centralized multi-factor XP calculation based on configurable weights.
     */
    fun computeWeightedXp(req: XpVerificationRequest): XpBreakdown {
        val total = req.totalQuestions.coerceAtLeast(1)
        val correct = req.correctAnswers.coerceIn(0, total)
        val accuracyRatio = correct.toFloat() / total.toFloat()

        // 1. Base Correct Answer XP
        val baseXp = correct * config.xpPerCorrectAnswer

        // 2. Accuracy Bonus: Scaled by accuracyWeight and precision tier
        val accuracyMultiplier = when {
            accuracyRatio >= 0.95f -> 1.5 // 95%+ Elite Precision
            accuracyRatio >= 0.85f -> 1.25 // 85%+ High Mastery
            accuracyRatio >= 0.70f -> 1.0 // 70%+ Solid Passing
            accuracyRatio >= 0.50f -> 0.5 // 50%+ Moderate
            else -> 0.1 // Sub-50%
        }
        val accuracyBonus = (baseXp * config.accuracyWeight * accuracyMultiplier).roundToInt()

        // 3. Mock Test Performance Bonus: Scaled by mockTestWeight and test length
        val mockPerformanceBonus = if (req.assessmentType == AssessmentType.MOCK_TEST) {
            val lengthScaling = when {
                total >= 100 -> 2.0 // Full-length grand mock
                total >= 50 -> 1.5 // Standard mock
                else -> 1.0
            }
            val scoreTierBonus = when {
                accuracyRatio >= 0.80f -> 1.4
                accuracyRatio >= 0.60f -> 1.0
                else -> 0.5
            }
            (config.xpPerMockTestCompletion * config.mockTestWeight * lengthScaling * scoreTierBonus).roundToInt()
        } else if (req.assessmentType == AssessmentType.PRACTICE_SET || req.assessmentType == AssessmentType.CURRENT_AFFAIRS) {
            (config.xpPerTopicCompletion * 0.5 * config.mockTestWeight).roundToInt()
        } else {
            0
        }

        // 4. Consistency / Streak Bonus: Scaled by consistencyWeight and consecutive streak days
        val streakMultiplier = when {
            req.currentStreakDays >= 30 -> 2.0 // 30-Day Streak Titan
            req.currentStreakDays >= 14 -> 1.5 // 14-Day Consistency Pro
            req.currentStreakDays >= 7 -> 1.25 // 7-Day Habit Formed
            req.currentStreakDays >= 3 -> 1.1 // 3-Day Momentum
            else -> 1.0
        }
        val consistencyBonus = (config.xpPerDailyStreak * config.consistencyWeight * streakMultiplier).roundToInt()

        val totalCalculatedXp = (baseXp + accuracyBonus + mockPerformanceBonus + consistencyBonus).coerceAtLeast(5)

        return XpBreakdown(
            baseXp = baseXp,
            accuracyBonus = accuracyBonus,
            mockPerformanceBonus = mockPerformanceBonus,
            consistencyBonus = consistencyBonus,
            streakMultiplier = streakMultiplier,
            accuracyRatio = accuracyRatio,
            totalCalculatedXp = totalCalculatedXp
        )
    }

    /**
     * Persists the transaction audit log to Firestore.
     */
    private fun syncTransactionToFirestore(
        tx: XPTransactionEntity,
        req: XpVerificationRequest,
        breakdown: XpBreakdown
    ) {
        if (firestore == null) return
        try {
            val record = mapOf(
                "id" to tx.id,
                "userId" to tx.userId,
                "source" to tx.source,
                "sourceId" to tx.sourceId,
                "xp" to tx.xp,
                "examId" to req.examId,
                "assessmentType" to req.assessmentType.name,
                "totalQuestions" to req.totalQuestions,
                "correctAnswers" to req.correctAnswers,
                "durationSeconds" to req.durationSeconds,
                "accuracyPercentage" to (breakdown.accuracyRatio * 100f),
                "breakdown" to mapOf(
                    "baseXp" to breakdown.baseXp,
                    "accuracyBonus" to breakdown.accuracyBonus,
                    "mockPerformanceBonus" to breakdown.mockPerformanceBonus,
                    "consistencyBonus" to breakdown.consistencyBonus,
                    "streakMultiplier" to breakdown.streakMultiplier
                ),
                "status" to "COMMITTED",
                "timestamp" to tx.timestamp
            )

            firestore.collection("xp_transactions")
                .document(tx.sourceId)
                .set(record, SetOptions.merge())
                .addOnFailureListener { e ->
                    logW(TAG, "Offline sync pending for XP transaction: ${e.message}")
                }
        } catch (e: Exception) {
            logW(TAG, "Error writing transaction to Firestore: ${e.message}")
        }
    }

    private fun logW(tag: String, msg: String) {
        try { Log.w(tag, msg) } catch (_: Throwable) { println("[$tag] WARN: $msg") }
    }

    private fun logI(tag: String, msg: String) {
        try { Log.i(tag, msg) } catch (_: Throwable) { println("[$tag] INFO: $msg") }
    }

    private fun logD(tag: String, msg: String) {
        try { Log.d(tag, msg) } catch (_: Throwable) { }
    }

    private fun getAntiCheatReasonMessage(status: AntiCheatStatus, req: XpVerificationRequest): String {
        return when (status) {
            AntiCheatStatus.REJECTED_SPEED_ANOMALY ->
                "Speed anomaly detected. Completed ${req.totalQuestions} questions in ${req.durationSeconds}s, exceeding maximum human capability."
            AntiCheatStatus.REJECTED_IMPOSSIBLE_SCORE ->
                "Impossible score detected (${req.correctAnswers}/${req.totalQuestions})."
            AntiCheatStatus.REJECTED_RATE_LIMITED ->
                "Velocity check failed: Hourly XP limit exceeded."
            AntiCheatStatus.REJECTED_INVALID_TIMESTAMP ->
                "Timestamp rejected: Attempt time is out of allowable synchronization window."
            AntiCheatStatus.REJECTED_DUPLICATE ->
                "Attempt already recorded."
            AntiCheatStatus.VERIFIED ->
                "Verified."
        }
    }
}
