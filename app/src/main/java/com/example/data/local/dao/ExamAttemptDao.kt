package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ExamAttemptEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Mock Test, PYQ, and practice exam attempts history.
 */
@Dao
interface ExamAttemptDao {

    @Query("SELECT * FROM exam_attempts WHERE attemptId = :attemptId LIMIT 1")
    fun getAttemptByIdFlow(attemptId: String): Flow<ExamAttemptEntity?>

    @Query("SELECT * FROM exam_attempts WHERE attemptId = :attemptId LIMIT 1")
    suspend fun getAttemptById(attemptId: String): ExamAttemptEntity?

    @Query("SELECT * FROM exam_attempts WHERE appId = :appId ORDER BY startedAt DESC")
    fun getAllAttemptsFlow(appId: String = "appsc-group2"): Flow<List<ExamAttemptEntity>>

    @Query("SELECT * FROM exam_attempts WHERE appId = :appId AND testType = :testType ORDER BY startedAt DESC")
    fun getAttemptsByTypeFlow(appId: String, testType: String): Flow<List<ExamAttemptEntity>>

    @Query("SELECT * FROM exam_attempts WHERE testReferenceId = :testReferenceId ORDER BY startedAt DESC")
    fun getAttemptsForTestFlow(testReferenceId: String): Flow<List<ExamAttemptEntity>>

    @Query("SELECT * FROM exam_attempts WHERE appId = :appId ORDER BY startedAt DESC LIMIT :limit")
    suspend fun getRecentAttempts(appId: String = "appsc-group2", limit: Int = 10): List<ExamAttemptEntity>

    @Query("SELECT AVG(accuracyPercentage) FROM exam_attempts WHERE appId = :appId")
    fun getAverageAccuracyFlow(appId: String = "appsc-group2"): Flow<Double?>

    @Query("SELECT COUNT(*) FROM exam_attempts WHERE appId = :appId")
    fun getTotalAttemptsCountFlow(appId: String = "appsc-group2"): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: ExamAttemptEntity)

    @Query("DELETE FROM exam_attempts WHERE attemptId = :attemptId")
    suspend fun deleteAttemptById(attemptId: String)

    @Query("DELETE FROM exam_attempts WHERE appId = :appId")
    suspend fun clearAllAttempts(appId: String)
}
