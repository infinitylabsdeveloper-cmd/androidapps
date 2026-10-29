package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room Entity tracking complete test session attempts, scores, durations, and answers.
 */
@Entity(
    tableName = "exam_attempts",
    indices = [
        Index(value = ["appId"]),
        Index(value = ["testType"]),
        Index(value = ["testReferenceId"]),
        Index(value = ["startedAt"])
    ]
)
data class ExamAttemptEntity(
    @PrimaryKey
    val attemptId: String = UUID.randomUUID().toString(),
    val appId: String = "appsc-group2",
    val testType: String, // "MOCK_TEST", "PRACTICE_SET", "DAILY_QUIZ", "PYQ_PAPER", "TOPIC_TEST"
    val testReferenceId: String, // Id of the mock test or practice set
    val testTitle: String,
    val subjectId: String? = null,
    val totalQuestions: Int,
    val attemptedCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val score: Double,
    val totalMarks: Double,
    val accuracyPercentage: Double,
    val timeTakenSeconds: Long,
    val userAnswers: Map<String, String> = emptyMap(), // questionId -> selectedOption
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long = System.currentTimeMillis()
)
