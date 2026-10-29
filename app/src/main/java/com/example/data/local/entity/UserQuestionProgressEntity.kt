package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity tracking granular user interactions and progress per MCQ question.
 * Records whether the question has been attempted, chosen answer, accuracy, bookmarks,
 * time spent, and revision flags.
 */
@Entity(
    tableName = "user_question_progress",
    indices = [
        Index(value = ["appId"]),
        Index(value = ["subjectId"]),
        Index(value = ["unitId"]),
        Index(value = ["topicId"]),
        Index(value = ["isBookmarked"]),
        Index(value = ["isAttempted"]),
        Index(value = ["isCorrect"]),
        Index(value = ["isMarkedForReview"])
    ]
)
data class UserQuestionProgressEntity(
    @PrimaryKey
    val questionId: String,
    val appId: String = "appsc-group2",
    val subjectId: String = "",
    val unitId: String = "",
    val topicId: String = "",
    val isAttempted: Boolean = false,
    val selectedOption: String? = null, // "A", "B", "C", "D"
    val isCorrect: Boolean = false,
    val isBookmarked: Boolean = false,
    val isMarkedForReview: Boolean = false,
    val notes: String? = null,
    val attemptCount: Int = 0,
    val lastAttemptedAt: Long = System.currentTimeMillis(),
    val timeSpentSeconds: Long = 0L
)
