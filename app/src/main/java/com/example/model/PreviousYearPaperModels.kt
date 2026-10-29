package com.example.model

data class PreviousYearQuestion(
    val questionNumber: Int,
    val question: String,
    val options: Map<String, String>, // A, B, C, D
    val correctAnswer: String,
    val explanation: String? = null,
    val difficulty: String? = null
)

data class PreviousYearPaper(
    val id: String,
    val appId: String,
    val examName: String,
    val year: Int,
    val examDate: String,
    val shift: String,
    val language: String,
    val subject: String? = null,
    val isPublished: Boolean = true,
    val questions: List<PreviousYearQuestion>
)
