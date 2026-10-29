package com.example.util

import com.example.model.*
import com.example.data.SampleData

/**
 * QuestionImportValidator
 * Performs strict hierarchical validation of MCQ data before database insertion.
 * Ensures data integrity across App -> Subject -> Unit -> Topic -> Subtopic -> Set.
 */
object QuestionImportValidator {

    fun validate(
        questions: List<Question>,
        targetAppId: String,
        targetSubjectId: String = "",
        targetUnitId: String = "",
        targetTopicId: String = "",
        targetSubtopic: String = "",
        targetSetName: String = "",
        existingQuestions: List<Question>,
        subjects: List<Subject>
    ): ImportValidationReport {
        val errors = mutableListOf<String>()
        var validCount = 0
        var invalidCount = 0
        var duplicateCount = 0

        // Build hierarchy map for fast lookup
        val subjectMap = subjects.associateBy { it.id }
        val unitMap = subjects.flatMap { it.units }.associateBy { it.id }
        val topicMap = subjects.flatMap { it.units }.flatMap { it.topics }.associateBy { it.id }

        // Destination Context Validation
        val targetSubject = subjectMap[targetSubjectId]
        val targetUnit = unitMap[targetUnitId]
        val targetTopic = topicMap[targetTopicId]

        questions.forEachIndexed { index, q ->
            var isQValid = true
            val qId = q.id.ifBlank { "New-${index + 1}" }

            // 1. Basic Completeness
            if (q.questionText.isBlank()) {
                errors.add("[$qId] Missing question text.")
                isQValid = false
            }
            if (q.options.size < 2) {
                errors.add("[$qId] Insufficient options (min 2).")
                isQValid = false
            }
            if (q.correctOption.isBlank() || !q.options.containsKey(q.correctOption.uppercase())) {
                errors.add("[$qId] Correct option '${q.correctOption}' is invalid or missing.")
                isQValid = false
            }

            // 2. Strict Hierarchy Alignment (If targets are specified)
            
            // App ID Isolation
            if (q.appId.isNotBlank() && q.appId != targetAppId) {
                errors.add("[$qId] App ID mismatch: Embedded '${q.appId}' vs Target '$targetAppId'.")
                isQValid = false
            }

            // Subject ID Validation
            if (targetSubjectId.isNotBlank()) {
                if (q.subjectId.isNotBlank() && q.subjectId != targetSubjectId) {
                    errors.add("[$qId] Subject mismatch: Embedded '${q.subjectId}' vs Target '$targetSubjectId'.")
                    isQValid = false
                }
            }

            // Unit ID Validation
            if (targetUnitId.isNotBlank()) {
                if (q.unitId.isNotBlank() && q.unitId != targetUnitId) {
                    errors.add("[$qId] Unit mismatch: Embedded '${q.unitId}' vs Target '$targetUnitId'.")
                    isQValid = false
                }
            }

            // Topic ID Validation
            if (targetTopicId.isNotBlank()) {
                if (q.topicId.isNotBlank() && q.topicId != targetTopicId) {
                    errors.add("[$qId] Topic mismatch: Embedded '${q.topicId}' vs Target '$targetTopicId'.")
                    isQValid = false
                }
            }

            // Subtopic Validation
            if (targetSubtopic.isNotBlank()) {
                if (q.subtopic.isNotBlank() && q.subtopic != targetSubtopic) {
                    errors.add("[$qId] Subtopic mismatch: Embedded '${q.subtopic}' vs Target '$targetSubtopic'.")
                    isQValid = false
                }
            }

            // Set Name Validation
            if (targetSetName.isNotBlank()) {
                if (q.setName.isNotBlank() && q.setName != targetSetName) {
                    errors.add("[$qId] Set Name mismatch: Embedded '${q.setName}' vs Target '$targetSetName'.")
                    isQValid = false
                }
            }

            // Orphan Checks (if IDs are present but no specific target was forced)
            if (isQValid) {
                if (q.subjectId.isNotBlank() && !subjectMap.containsKey(q.subjectId)) {
                    errors.add("[$qId] Orphan Subject: ID '${q.subjectId}' not in DB.")
                    isQValid = false
                }
                if (q.unitId.isNotBlank() && !unitMap.containsKey(q.unitId)) {
                    errors.add("[$qId] Orphan Unit: ID '${q.unitId}' not in DB.")
                    isQValid = false
                }
                if (q.topicId.isNotBlank() && !topicMap.containsKey(q.topicId)) {
                    errors.add("[$qId] Orphan Topic: ID '${q.topicId}' not in DB.")
                    isQValid = false
                }
            }

            // 3. Duplicate Detection (Content Hash based)
            val hash = q.contentHash ?: SyllabusJsonParser.calculateQuestionHash(q.questionText, q.options)
            val isDuplicate = existingQuestions.any {
                it.appId == targetAppId && (it.id == q.id || (it.contentHash != null && it.contentHash == hash))
            }
            if (isDuplicate) {
                duplicateCount++
            }

            if (isQValid) validCount++ else invalidCount++
        }

        return ImportValidationReport(
            totalQuestions = questions.size,
            validQuestions = validCount,
            invalidQuestions = invalidCount,
            potentialDuplicates = duplicateCount,
            errors = errors,
            targetAppId = targetAppId,
            targetSubjectName = targetSubject?.name ?: "Dynamic",
            targetUnitName = targetUnit?.name ?: "Dynamic",
            targetTopicName = targetTopic?.name ?: "Dynamic"
        )
    }
}
