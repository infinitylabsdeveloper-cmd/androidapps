package com.example.util

import com.example.model.GeneratedMockTest
import com.example.model.Question
import com.example.model.SubjectQuestionConfig
import java.util.UUID

object MockTestEngine {

    /**
     * Resolves exam-specific positive and negative marking values based on exam category / app.
     */
    fun getExamPatternMarkingScheme(examCategoryOrType: String, appId: String): Pair<Double, Double> {
        val cleanApp = appId.lowercase()
        val cleanExam = examCategoryOrType.lowercase()
        return when {
            cleanExam.contains("ssc") || cleanApp.contains("ssc") -> Pair(2.0, 0.50)
            cleanExam.contains("upsc") || cleanApp.contains("upsc") -> Pair(2.0, 0.66)
            cleanExam.contains("bank") || cleanExam.contains("ibps") || cleanExam.contains("sbi") || cleanApp.contains("bank") -> Pair(1.0, 0.25)
            cleanExam.contains("railway") || cleanExam.contains("rrb") || cleanApp.contains("rrb") -> Pair(1.0, 0.33)
            // APPSC, TSPSC, State PSC, Group 2, Prelims, Mains, General:
            else -> Pair(1.0, 0.33)
        }
    }

    data class ValidationIssue(
        val sourceName: String,
        val requested: Int,
        val available: Int,
        val shortage: Int,
        val detailMessage: String = ""
    )

    data class EngineValidationResult(
        val isValid: Boolean,
        val totalAvailable: Int = 0,
        val totalRequested: Int = 0,
        val issues: List<ValidationIssue> = emptyList()
    )

    /**
     * Filters candidate questions for a given source config and optional difficulty/set criteria.
     */
    fun getCandidateQuestionsForConfig(
        appId: String,
        config: SubjectQuestionConfig,
        allQuestions: List<Question>,
        difficulty: String = "Mixed"
    ): List<Question> {
        val baseQuestions = allQuestions.filter { q ->
            (q.appId == appId || q.subjectId == "subj-current-affairs" || q.appId == "ALL") &&
            q.subjectId == config.subjectId &&
            (
                (config.unitIds.isEmpty() && config.topicIds.isEmpty()) ||
                (config.unitIds.isNotEmpty() && q.unitId in config.unitIds) ||
                (config.topicIds.isNotEmpty() && q.topicId in config.topicIds)
            ) &&
            (config.setNames.isEmpty() || q.setName in config.setNames) &&
            q.isActive
        }

        if (difficulty.equals("Mixed", ignoreCase = true) || difficulty.isBlank()) {
            return baseQuestions
        }

        val difficultyFiltered = baseQuestions.filter {
            it.difficulty.equals(difficulty, ignoreCase = true)
        }

        // If specific difficulty has enough questions, use it; otherwise fallback to base pool
        return if (difficultyFiltered.isNotEmpty()) difficultyFiltered else baseQuestions
    }

    /**
     * Real-time validation confirming Requested <= Available for each source,
     * accounting for hierarchy overlaps (Topics -> Units -> Subjects).
     */
    fun validateConfigs(
        appId: String,
        configs: List<SubjectQuestionConfig>,
        allQuestions: List<Question>,
        difficulty: String = "Mixed",
        selectionMode: String = "Balanced",
        targetTotalQuestions: Int = 0
    ): EngineValidationResult {
        if (configs.isEmpty()) {
            return EngineValidationResult(isValid = false, issues = emptyList())
        }

        val issues = mutableListOf<ValidationIssue>()

        // Sort configs by hierarchy: Topics first, then Units, then Subjects
        val sortedConfigs = configs.sortedWith(
            compareBy { config ->
                when {
                    config.topicIds.isNotEmpty() -> 0
                    config.unitIds.isNotEmpty() -> 1
                    else -> 2
                }
            }
        )

        val reservedQuestionIds = mutableSetOf<String>()
        var totalAvailableSum = 0

        for (config in sortedConfigs) {
            val matchingQuestions = getCandidateQuestionsForConfig(appId, config, allQuestions, difficulty)
            val totalAvailableForSource = matchingQuestions.size
            val unreservedMatching = matchingQuestions.filter { it.id !in reservedQuestionIds }
            val availableCount = unreservedMatching.size
            totalAvailableSum += availableCount

            // In Balanced or Random mode, individual requestedCount might be automatically distributed later,
            // but if manual requestedCount is > available, we flag it.
            if (selectionMode.equals("Manual", ignoreCase = true)) {
                if (config.requestedCount <= 0 || config.requestedCount > availableCount) {
                    val detail = if (availableCount < totalAvailableForSource) {
                        "Only $availableCount unassigned MCQs available ($totalAvailableForSource total, but ${totalAvailableForSource - availableCount} assigned to other selections). Requested ${config.requestedCount}."
                    } else {
                        "Only $availableCount MCQs available, but ${config.requestedCount} requested."
                    }
                    issues.add(
                        ValidationIssue(
                            sourceName = config.subjectName,
                            requested = config.requestedCount,
                            available = availableCount,
                            shortage = (config.requestedCount - availableCount).coerceAtLeast(0),
                            detailMessage = detail
                        )
                    )
                } else {
                    reservedQuestionIds.addAll(unreservedMatching.map { it.id }.take(config.requestedCount))
                }
            }
        }

        val totalRequested = if (selectionMode.equals("Manual", ignoreCase = true)) {
            configs.sumOf { it.requestedCount }
        } else {
            targetTotalQuestions
        }

        if (!selectionMode.equals("Manual", ignoreCase = true) && targetTotalQuestions > totalAvailableSum) {
            issues.add(
                ValidationIssue(
                    sourceName = "Total Pool Across Selected Subjects",
                    requested = targetTotalQuestions,
                    available = totalAvailableSum,
                    shortage = targetTotalQuestions - totalAvailableSum,
                    detailMessage = "Total requested questions ($targetTotalQuestions) exceeds total available unique questions in selected subjects ($totalAvailableSum)."
                )
            )
        }

        return EngineValidationResult(
            isValid = issues.isEmpty() && totalRequested > 0 && totalAvailableSum >= totalRequested,
            totalAvailable = totalAvailableSum,
            totalRequested = totalRequested,
            issues = issues
        )
    }

    /**
     * Generates a Full-Length / Custom Mock Test with intelligent question selection,
     * deduplication, difficulty balancing, and exact configuration persistence.
     */
    fun generateTest(
        appId: String,
        title: String,
        examType: String = "Custom",
        testMode: com.example.model.MockTestMode = com.example.model.MockTestMode.FULL_LENGTH,
        configs: List<SubjectQuestionConfig>,
        totalQuestions: Int = configs.sumOf { it.requestedCount }.coerceAtLeast(1),
        positiveMarks: Double = 1.0,
        negativeMarks: Double = 0.33,
        durationMinutes: Int = 90,
        totalMarks: Double = totalQuestions * positiveMarks,
        difficulty: String = "Mixed",
        selectionMode: String = "Balanced",
        selectedSubjectNames: List<String> = emptyList(),
        selectedUnitNames: List<String> = emptyList(),
        selectedTopicNames: List<String> = emptyList(),
        selectedCurrentAffairsMonths: List<String> = emptyList(),
        availableQuestionPoolCount: Int = 0,
        allQuestions: List<Question>,
        pastTests: List<GeneratedMockTest> = emptyList()
    ): GeneratedMockTest {
        val selectedQuestions = mutableListOf<Question>()
        val chosenInThisTestIds = mutableSetOf<String>()
        val previouslyUsedIds = pastTests.flatMap { test -> test.questions.map { it.id } }.toSet()

        // 1. Resolve Effective Configs with Allocations
        val effectiveConfigs: List<SubjectQuestionConfig> = when {
            selectionMode.equals("Manual", ignoreCase = true) -> {
                configs
            }
            selectionMode.equals("Random", ignoreCase = true) -> {
                // In random mode, pool candidate questions and pick overall
                configs
            }
            else -> {
                // Balanced mode: Distribute totalQuestions evenly / proportionally across selected configs
                distributeBalancedCounts(appId, configs, totalQuestions, allQuestions, difficulty)
            }
        }

        if (selectionMode.equals("Random", ignoreCase = true)) {
            // Pool all matching candidate questions from selected configs
            val pool = mutableListOf<Question>()
            for (config in configs) {
                val candidates = getCandidateQuestionsForConfig(appId, config, allQuestions, difficulty)
                pool.addAll(candidates)
            }
            val uniquePool = pool.distinctBy { it.id }
            val freshCandidates = uniquePool.filter { it.id !in previouslyUsedIds }
            val usedCandidates = uniquePool.filter { it.id in previouslyUsedIds }

            if (freshCandidates.size >= totalQuestions) {
                selectedQuestions.addAll(freshCandidates.shuffled().take(totalQuestions))
            } else {
                selectedQuestions.addAll(freshCandidates.shuffled())
                val needed = totalQuestions - freshCandidates.size
                selectedQuestions.addAll(usedCandidates.shuffled().take(needed))
            }

            if (selectedQuestions.size < totalQuestions && uniquePool.size >= totalQuestions) {
                val remaining = uniquePool.filter { it.id !in selectedQuestions.map { q -> q.id } }
                selectedQuestions.addAll(remaining.take(totalQuestions - selectedQuestions.size))
            }
        } else {
            // Granular generation per subject/unit/topic
            val sortedConfigs = effectiveConfigs.sortedWith(
                compareBy { config ->
                    when {
                        config.topicIds.isNotEmpty() -> 0
                        config.unitIds.isNotEmpty() -> 1
                        else -> 2
                    }
                }
            )

            for (config in sortedConfigs) {
                val matching = getCandidateQuestionsForConfig(appId, config, allQuestions, difficulty)
                val unchosen = matching.filter { it.id !in chosenInThisTestIds }
                val fresh = unchosen.filter { it.id !in previouslyUsedIds }
                val used = unchosen.filter { it.id in previouslyUsedIds }

                val needed = config.requestedCount
                val chosenForThis = mutableListOf<Question>()

                if (fresh.size >= needed) {
                    chosenForThis.addAll(fresh.shuffled().take(needed))
                } else {
                    chosenForThis.addAll(fresh.shuffled())
                    val rem = (needed - fresh.size).coerceAtMost(used.size)
                    chosenForThis.addAll(used.shuffled().take(rem))
                }

                if (chosenForThis.size < needed) {
                    // Fallback to any matching in subject if still shortage
                    val fallback = allQuestions.filter { 
                        (it.appId == appId || it.subjectId == "subj-current-affairs" || it.appId == "ALL") &&
                        it.subjectId == config.subjectId && 
                        it.id !in chosenInThisTestIds && 
                        it.id !in chosenForThis.map { q -> q.id }
                    }
                    val rem2 = (needed - chosenForThis.size).coerceAtMost(fallback.size)
                    chosenForThis.addAll(fallback.shuffled().take(rem2))
                }

                chosenInThisTestIds.addAll(chosenForThis.map { it.id })
                selectedQuestions.addAll(chosenForThis)
            }
        }

        // Deduplication Safety Guarantee
        val distinctSelected = selectedQuestions.distinctBy { it.id }.toMutableList()

        // If slight shortage due to overlaps, top up from eligible pools
        if (distinctSelected.size < totalQuestions) {
            val allEligible = configs.flatMap { getCandidateQuestionsForConfig(appId, it, allQuestions, difficulty) }
                .distinctBy { it.id }
                .filter { it.id !in distinctSelected.map { q -> q.id } }
            
            val needed = totalQuestions - distinctSelected.size
            distinctSelected.addAll(allEligible.shuffled().take(needed))
        }

        // Randomize question display sequence
        distinctSelected.shuffle()

        val finalTotalQuestions = distinctSelected.size.coerceAtLeast(1)
        val finalTotalMarks = if (totalMarks > 0) totalMarks else finalTotalQuestions * positiveMarks

        return GeneratedMockTest(
            id = UUID.randomUUID().toString(),
            appId = appId,
            title = title.ifBlank {
                "${examType.trim()} Mock Test #${pastTests.size + 1}"
            },
            createdAt = System.currentTimeMillis(),
            subjectConfigs = effectiveConfigs,
            totalQuestions = finalTotalQuestions,
            positiveMarks = positiveMarks,
            negativeMarks = negativeMarks,
            durationMinutes = durationMinutes,
            questions = distinctSelected,
            examType = examType,
            testMode = testMode,
            selectedSubjectNames = selectedSubjectNames,
            selectedUnitNames = selectedUnitNames,
            selectedTopicNames = selectedTopicNames,
            selectedCurrentAffairsMonths = selectedCurrentAffairsMonths,
            availableQuestionPoolCount = if (availableQuestionPoolCount > 0) availableQuestionPoolCount else distinctSelected.size,
            difficulty = difficulty,
            selectionMode = selectionMode,
            totalMarks = finalTotalMarks,
            status = "Not Started"
        )
    }

    /**
     * Distributes totalQuestions evenly/proportionally across configs based on available pool.
     */
    private fun distributeBalancedCounts(
        appId: String,
        configs: List<SubjectQuestionConfig>,
        totalQuestions: Int,
        allQuestions: List<Question>,
        difficulty: String
    ): List<SubjectQuestionConfig> {
        if (configs.isEmpty() || totalQuestions <= 0) return configs

        val numConfigs = configs.size
        val baseCount = totalQuestions / numConfigs
        var remainder = totalQuestions % numConfigs

        return configs.mapIndexed { index, cfg ->
            val extra = if (index < remainder) 1 else 0
            val allocated = baseCount + extra
            cfg.copy(requestedCount = allocated)
        }
    }
}
