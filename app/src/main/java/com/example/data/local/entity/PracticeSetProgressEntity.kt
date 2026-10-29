package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity tracking completion and score metrics for individual practice sets.
 */
@Entity(
    tableName = "practice_set_progress",
    indices = [
        Index(value = ["appId"]),
        Index(value = ["subjectId"]),
        Index(value = ["unitId"]),
        Index(value = ["topicId"]),
        Index(value = ["isCompleted"])
    ]
)
data class PracticeSetProgressEntity(
    @PrimaryKey
    val setId: String, // e.g., "set-ancient-1" or composite key
    val appId: String = "appsc-group2",
    val subjectId: String,
    val unitId: String = "",
    val topicId: String = "",
    val title: String = "",
    val status: String = "Not Started", // "Not Started", "In Progress", "Completed"
    val totalQuestions: Int = 10,
    val attemptedQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val unattemptedAnswers: Int = 0,
    val score: Double = 0.0,
    val bestScore: Double = 0.0,
    val accuracyPercentage: Double = 0.0,
    val isCompleted: Boolean = false,
    val lastAttemptedTimestamp: Long = System.currentTimeMillis()
)
