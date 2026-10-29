package com.example.util

import com.example.model.Question
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.model.UnitModel
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

sealed class SyllabusParseResult {
    data class Subjects(val subjects: List<Subject>) : SyllabusParseResult()
    data class Units(val units: List<UnitModel>) : SyllabusParseResult()
    data class Topics(val topics: List<TopicModel>) : SyllabusParseResult()
    data class HierarchyMcqs(val packages: List<HierarchyMcqPackage>) : SyllabusParseResult()
    data class FlatMcqs(val questions: List<Question>) : SyllabusParseResult()
    data class Error(val message: String) : SyllabusParseResult()
}

data class HierarchyMcqPackage(
    val subjectName: String,
    val unitName: String,
    val unitNumber: Int = 1,
    val topicName: String,
    val language: String = "Telugu",
    val subtopicPackages: List<SubtopicMcqPackage>
)

data class SubtopicMcqPackage(
    val subtopicName: String,
    val questions: List<Question>
)

data class McqValidationResult(
    val isValid: Boolean,
    val totalMcqs: Int = 0,
    val totalSubtopics: Int = 0,
    val hierarchyInfo: String = "",
    val errorMessage: String? = null
)

object SyllabusJsonParser {

    // Core hierarchy and question keys that must ALWAYS be preserved
    private val CORE_MCQ_KEYS = setOf(
        "topic", "topic_name", "subtopics", "subtopic", "subtopic_name",
        "mcqs", "questions", "items", "question", "questiontext", "question_text", "q",
        "options", "optiona", "optionb", "optionc", "optiond",
        "a", "b", "c", "d", "e", "f", "answer", "right_answer", "correct_answer",
        "correctoption", "correct", "right_answer_index", "answerindex",
        "id", "mcq_id", "q_id", "question_id", "qid", "questionid", "code",
        "explanation", "explanationtext", "solution", "desc", "difficulty", "language",
        "subject", "subject_name", "unit", "unit_name", "unit_number", "unitnumber", "subjects", "units", "topics"
    )

    // Normalized metadata keys to recursively ignore anywhere in the JSON
    private val IGNORED_METADATA_KEYS = setOf(
        "coverage_method", "rules", "unique mcqs", "a-d options", "full-text answers",
        "no answer_option field", "no assertion & reason", "no assertion and reason",
        "no explanations", "descriptions", "description", "generation instructions",
        "generation_instructions", "ai instructions", "ai_instructions", "metadata",
        "validation instructions", "validation_instructions", "comments", "comment",
        "total_mcqs", "notes", "prompt", "system_instructions", "instructions",
        "guidelines", "meta", "summary", "coverage", "unique_mcqs", "a_d_options",
        "full_text_answers", "no_answer_option_field", "no_assertion_reason",
        "no_assertion_and_reason", "no_explanations", "system_rules", "admin_rules"
    )

    private fun isMetadataKey(key: String): Boolean {
        val cleanKey = key.trim().lowercase()
        val norm = cleanKey.replace("_", " ").replace("-", " ")
        if (CORE_MCQ_KEYS.contains(cleanKey) || CORE_MCQ_KEYS.contains(norm.replace(" ", ""))) {
            return false
        }
        if (IGNORED_METADATA_KEYS.contains(norm) || IGNORED_METADATA_KEYS.contains(cleanKey)) {
            return true
        }
        if (norm.startsWith("instruction") || norm.endsWith("instruction") || norm.contains("instructions")) return true
        if (norm.startsWith("rule") || norm.endsWith("rule") || norm.contains("rules")) return true
        if (norm.contains("coverage") || norm.contains("metadata")) return true
        if (norm.contains("ai rule") || norm.contains("system rule") || norm.contains("guideline")) return true
        return false
    }

    /**
     * SHA-256 Hashing Utility
     */
    fun calculateHash(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "error-hashing"
        }
    }

    /**
     * Calculates a unique hash for a question based on its text and options.
     */
    fun calculateQuestionHash(questionText: String, options: Map<String, String>): String {
        val normalizedOptions = options.toSortedMap().values.joinToString("|") { 
            it.trim().lowercase().replace(Regex("\\s+"), " ")
        }
        val normalizedText = questionText.trim().lowercase().replace(Regex("\\s+"), " ")
        return calculateHash("$normalizedText||$normalizedOptions")
    }

    /**
     * Removes irrelevant metadata and instruction fields from JSON before processing or validation.
     */
    fun removeMetadata(jsonString: String): String {
        return try {
            val trimmed = jsonString.trim()
            if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                cleanJsonObject(obj)
                obj.toString()
            } else if (trimmed.startsWith("[")) {
                val arr = JSONArray(trimmed)
                cleanJsonArray(arr)
                arr.toString()
            } else {
                jsonString
            }
        } catch (e: Exception) {
            jsonString
        }
    }

    private fun cleanJsonObject(obj: JSONObject) {
        val keysToRemove = mutableListOf<String>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            if (isMetadataKey(k)) {
                keysToRemove.add(k)
            } else {
                val v = obj.get(k)
                if (v is JSONObject) {
                    cleanJsonObject(v)
                } else if (v is JSONArray) {
                    cleanJsonArray(v)
                }
            }
        }
        keysToRemove.forEach { obj.remove(it) }
    }

    private fun cleanJsonArray(arr: JSONArray) {
        for (i in 0 until arr.length()) {
            val v = arr.get(i)
            if (v is JSONObject) {
                cleanJsonObject(v)
            } else if (v is JSONArray) {
                cleanJsonArray(v)
            }
        }
    }

    /**
     * Validates an uploaded JSON string strictly before importing.
     * Enforces:
     * - Topic exists
     * - Subtopics exists and non-empty
     * - Questions exist
     * - Options A-D exist
     * - Correct Answer exists and matches options
     * If an MCQ is invalid, returns a clear error message identifying the record without crashing.
     */
    fun validateJson(jsonString: String): McqValidationResult {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) {
            return McqValidationResult(isValid = false, errorMessage = "JSON content is empty.")
        }

        try {
            val cleanedJson = removeMetadata(trimmed)
            if (cleanedJson.startsWith("{")) {
                val obj = JSONObject(cleanedJson)
                if (obj.has("subtopics") || obj.has("topic") || obj.has("subject")) {
                    return validateSingleHierarchyPackage(obj)
                }
                if (obj.has("subjects")) {
                    return McqValidationResult(isValid = true, hierarchyInfo = "Full Syllabus Tree with Subjects")
                }
                if (obj.has("units")) {
                    return McqValidationResult(isValid = true, hierarchyInfo = "Units Syllabus Structure")
                }
                if (obj.has("topics")) {
                    return McqValidationResult(isValid = true, hierarchyInfo = "Topics Syllabus Structure")
                }
                // Check if it's a single MCQ object
                if (obj.has("question") || obj.has("questionText")) {
                    val err = validateSingleMcqObj(obj, 1)
                    return if (err == null) {
                        McqValidationResult(isValid = true, totalMcqs = 1, hierarchyInfo = "Single MCQ Item")
                    } else {
                        McqValidationResult(isValid = false, errorMessage = err)
                    }
                }
                return McqValidationResult(isValid = false, errorMessage = "Unrecognized JSON object structure. Expected 'topic', 'subtopics', or 'subjects'.")
            } else if (cleanedJson.startsWith("[")) {
                val array = JSONArray(cleanedJson)
                if (array.length() == 0) {
                    return McqValidationResult(isValid = false, errorMessage = "JSON array is empty.")
                }
                val first = array.getJSONObject(0)
                if (first.has("subtopics") || first.has("topic") || first.has("subject")) {
                    var totalQ = 0
                    var totalS = 0
                    for (i in 0 until array.length()) {
                        val vRes = validateSingleHierarchyPackage(array.getJSONObject(i))
                        if (!vRes.isValid) return vRes
                        totalQ += vRes.totalMcqs
                        totalS += vRes.totalSubtopics
                    }
                    return McqValidationResult(
                        isValid = true,
                        totalMcqs = totalQ,
                        totalSubtopics = totalS,
                        hierarchyInfo = "Multiple Topics Hierarchy ($totalS subtopics, $totalQ MCQs)"
                    )
                } else if (first.has("question") || first.has("questionText")) {
                    for (i in 0 until array.length()) {
                        val qObj = array.getJSONObject(i)
                        val err = validateSingleMcqObj(qObj, i + 1)
                        if (err != null) return McqValidationResult(isValid = false, errorMessage = err)
                    }
                    return McqValidationResult(isValid = true, totalMcqs = array.length(), hierarchyInfo = "Flat MCQ List (${array.length()} MCQs)")
                }
                return McqValidationResult(isValid = true, hierarchyInfo = "JSON Array structure")
            }
            return McqValidationResult(isValid = false, errorMessage = "Invalid JSON syntax. Must start with '{' or '['.")
        } catch (e: Exception) {
            return McqValidationResult(isValid = false, errorMessage = "JSON Syntax Error: ${e.message}")
        }
    }

    private fun validateSingleHierarchyPackage(obj: JSONObject): McqValidationResult {
        val topic = obj.optString("topic", obj.optString("topic_name", obj.optString("name", ""))).trim()
        if (topic.isEmpty()) {
            return McqValidationResult(isValid = false, errorMessage = "Missing required 'topic' field in hierarchy JSON.")
        }

        if (!obj.has("subtopics")) {
            return McqValidationResult(isValid = false, errorMessage = "Topic '$topic': Missing required 'subtopics' array.")
        }

        val subtopicsArr = obj.optJSONArray("subtopics")
        if (subtopicsArr == null || subtopicsArr.length() == 0) {
            return McqValidationResult(isValid = false, errorMessage = "Topic '$topic': 'subtopics' array is empty.")
        }

        var totalMcqs = 0
        for (s in 0 until subtopicsArr.length()) {
            val subItem = subtopicsArr.opt(s)
            if (subItem !is JSONObject) {
                return McqValidationResult(isValid = false, errorMessage = "Topic '$topic': Subtopic at index ${s + 1} is not a valid JSON object.")
            }
            val subName = subItem.optString("subtopic", subItem.optString("subtopic_name", subItem.optString("name", ""))).trim()
            if (subName.isEmpty()) {
                return McqValidationResult(isValid = false, errorMessage = "Topic '$topic': Subtopic #${s + 1} is missing a subtopic name.")
            }

            val mcqsArr = when {
                subItem.has("mcqs") -> subItem.optJSONArray("mcqs")
                subItem.has("questions") -> subItem.optJSONArray("questions")
                subItem.has("items") -> subItem.optJSONArray("items")
                else -> null
            }

            if (mcqsArr == null || mcqsArr.length() == 0) {
                return McqValidationResult(isValid = false, errorMessage = "Topic '$topic' → Subtopic '$subName': Has no MCQs (empty or missing 'mcqs' array).")
            }

            for (m in 0 until mcqsArr.length()) {
                val mcqItem = mcqsArr.opt(m)
                if (mcqItem !is JSONObject) {
                    return McqValidationResult(isValid = false, errorMessage = "Topic '$topic' → Subtopic '$subName' - MCQ #${m + 1}: Not a valid JSON object.")
                }
                val err = validateSingleMcqObj(mcqItem, m + 1, subtopicName = subName, topicName = topic)
                if (err != null) {
                    return McqValidationResult(isValid = false, errorMessage = err)
                }
                totalMcqs++
            }
        }

        val hierarchyText = buildString {
            append("Topic: $topic → ${subtopicsArr.length()} Subtopics ($totalMcqs MCQs)")
        }

        return McqValidationResult(
            isValid = true,
            totalMcqs = totalMcqs,
            totalSubtopics = subtopicsArr.length(),
            hierarchyInfo = hierarchyText
        )
    }

    private fun validateSingleMcqObj(
        obj: JSONObject,
        index: Int,
        subtopicName: String? = null,
        topicName: String? = null
    ): String? {
        val topicPrefix = if (!topicName.isNullOrBlank()) "Topic '$topicName' → " else ""
        val prefix = if (!subtopicName.isNullOrBlank()) "${topicPrefix}Subtopic '$subtopicName' - MCQ #$index" else "${topicPrefix}MCQ #$index"
        val qId = obj.optString("id", obj.optString("mcq_id", "")).trim()
        val idSuffix = if (qId.isNotBlank()) " (ID: $qId)" else ""

        val qText = obj.optString("question", obj.optString("questionText", obj.optString("question_text", obj.optString("q", "")))).trim()
        if (qText.isEmpty()) {
            return "$prefix$idSuffix: Missing or empty 'question' field."
        }

        // Check options
        val optionsMap = linkedMapOf<String, String>()
        if (obj.has("options")) {
            val optVal = obj.get("options")
            if (optVal is JSONObject) {
                listOf("A", "B", "C", "D").forEach { k ->
                    val v = optVal.optString(k, optVal.optString(k.lowercase(), "")).trim()
                    if (v.isNotEmpty()) optionsMap[k] = v
                }
            } else if (optVal is JSONArray) {
                val labels = listOf("A", "B", "C", "D")
                for (k in 0 until optVal.length()) {
                    val label = if (k < labels.size) labels[k] else ('A'.code + k).toChar().toString()
                    val v = optVal.optString(k, "").trim()
                    if (v.isNotEmpty()) optionsMap[label] = v
                }
            }
        } else {
            listOf("A", "B", "C", "D").forEach { label ->
                val text = obj.optString("option$label", obj.optString("opt$label", obj.optString(label, obj.optString(label.lowercase(), "")))).trim()
                if (text.isNotEmpty()) optionsMap[label] = text
            }
        }

        if (optionsMap.size < 2) {
            return "$prefix$idSuffix: Must provide options A-D, found ${optionsMap.size}."
        }

        val missingOptions = mutableListOf<String>()
        listOf("A", "B", "C", "D").forEach { key ->
            if (!optionsMap.containsKey(key) || optionsMap[key]?.isBlank() == true) {
                missingOptions.add(key)
            }
        }
        if (missingOptions.isNotEmpty() && (obj.optJSONObject("options") != null || obj.optJSONArray("options")?.length() == 4)) {
            return "$prefix$idSuffix: Incomplete options. Missing: ${missingOptions.joinToString(", ")}."
        }

        // Check answer
        val rawAnswer = obj.optString(
            "answer",
            obj.optString("right_answer", obj.optString("correct_answer", obj.optString("correctOption", obj.optString("correct", ""))))
        ).trim()

        if (rawAnswer.isEmpty() && !obj.has("right_answer_index") && !obj.has("answerIndex")) {
            return "$prefix$idSuffix: Missing 'answer' field."
        }

        if (rawAnswer.isNotEmpty()) {
            val upper = rawAnswer.uppercase()
            val matchesKey = optionsMap.containsKey(upper)
            val normalizedRaw = rawAnswer.replace(Regex("""\s+"""), " ").trimEnd('.', ';', ',')
            val matchesText = optionsMap.values.any { v ->
                v.replace(Regex("""\s+"""), " ").trimEnd('.', ';', ',').equals(normalizedRaw, ignoreCase = true)
            }
            val isIndex = rawAnswer.toIntOrNull() != null
            val isPrefixed = Regex("""(?i)^(?:option|opt)?\s*[\(\[]?([A-D])[\)\]\.]?""").matches(rawAnswer)

            if (!matchesKey && !matchesText && !isIndex && !isPrefixed) {
                return "$prefix$idSuffix: Correct answer '$rawAnswer' does not match any of options A-D."
            }
        }

        return null
    }

    fun parseJson(jsonString: String, targetAppId: String): SyllabusParseResult {
        try {
            val json = removeMetadata(jsonString.trim())
            if (json.startsWith("{")) {
                val jsonObj = JSONObject(json)
                if (jsonObj.has("subtopics") || (jsonObj.has("subject") && jsonObj.has("topic")) || jsonObj.has("topic")) {
                    val pkg = parseSingleHierarchyPackage(jsonObj, targetAppId)
                    return SyllabusParseResult.HierarchyMcqs(listOf(pkg))
                }
                if (jsonObj.has("subjects")) {
                    return parseSubjects(jsonObj.getJSONArray("subjects"), targetAppId)
                } else if (jsonObj.has("units")) {
                    return parseUnits(jsonObj.getJSONArray("units"), targetAppId)
                } else if (jsonObj.has("topics")) {
                    return parseTopics(jsonObj.getJSONArray("topics"), targetAppId)
                } else {
                    if (jsonObj.has("units")) return SyllabusParseResult.Subjects(listOf(parseSingleSubject(jsonObj, targetAppId)))
                    if (jsonObj.has("topics")) return SyllabusParseResult.Units(listOf(parseSingleUnit(jsonObj, targetAppId)))
                    return SyllabusParseResult.Topics(listOf(parseSingleTopic(jsonObj, targetAppId)))
                }
            } else if (json.startsWith("[")) {
                val jsonArray = JSONArray(json)
                if (jsonArray.length() == 0) return SyllabusParseResult.Error("Empty array")

                val firstObj = jsonArray.getJSONObject(0)
                if (firstObj.has("subtopics") || (firstObj.has("subject") && firstObj.has("topic")) || firstObj.has("topic")) {
                    val pkgs = (0 until jsonArray.length()).map {
                        parseSingleHierarchyPackage(jsonArray.getJSONObject(it), targetAppId)
                    }
                    return SyllabusParseResult.HierarchyMcqs(pkgs)
                } else if (firstObj.has("units")) {
                    return parseSubjects(jsonArray, targetAppId)
                } else if (firstObj.has("topics")) {
                    return parseUnits(jsonArray, targetAppId)
                } else if (firstObj.has("question") || firstObj.has("questionText")) {
                    val flatQuestions = (0 until jsonArray.length()).mapNotNull {
                        parseSingleMcqObject(jsonArray.getJSONObject(it), targetAppId, "", "", "", "")
                    }
                    return SyllabusParseResult.FlatMcqs(flatQuestions)
                } else {
                    return parseTopics(jsonArray, targetAppId)
                }
            }
            return SyllabusParseResult.Error("Invalid JSON format")
        } catch (e: Exception) {
            return SyllabusParseResult.Error("JSON Parsing Error: ${e.message}")
        }
    }

    fun parseSingleHierarchyPackage(obj: JSONObject, appId: String): HierarchyMcqPackage {
        val subjectName = obj.optString("subject", obj.optString("subject_name", obj.optString("name", "General Studies"))).trim()
        val unitName = obj.optString("unit", obj.optString("unit_name", "Unit 1")).trim()
        val unitNumber = obj.optInt("unit_number", obj.optInt("unitNumber", 1))
        val topicName = obj.optString("topic", obj.optString("topic_name", "General Topic")).trim()
        val language = obj.optString("language", "English").trim()

        val subtopicsList = mutableListOf<SubtopicMcqPackage>()
        if (obj.has("subtopics")) {
            val subArray = obj.getJSONArray("subtopics")
            for (s in 0 until subArray.length()) {
                val subObj = subArray.getJSONObject(s)
                val subName = subObj.optString("subtopic", subObj.optString("subtopic_name", subObj.optString("name", "Subtopic ${s + 1}"))).trim()
                
                val mcqsArray = when {
                    subObj.has("mcqs") -> subObj.getJSONArray("mcqs")
                    subObj.has("questions") -> subObj.getJSONArray("questions")
                    subObj.has("items") -> subObj.getJSONArray("items")
                    else -> null
                }

                val questionsList = mutableListOf<Question>()
                if (mcqsArray != null) {
                    for (m in 0 until mcqsArray.length()) {
                        val mcqObj = mcqsArray.getJSONObject(m)
                        val q = parseSingleMcqObject(
                            obj = mcqObj,
                            appId = appId,
                            subjectName = subjectName,
                            unitName = unitName,
                            topicName = topicName,
                            subtopicName = subName,
                            defaultLanguage = language
                        )
                        if (q != null) {
                            questionsList.add(q)
                        }
                    }
                }
                subtopicsList.add(SubtopicMcqPackage(subtopicName = subName, questions = questionsList))
            }
        }

        return HierarchyMcqPackage(
            subjectName = subjectName,
            unitName = unitName,
            unitNumber = unitNumber,
            topicName = topicName,
            language = language,
            subtopicPackages = subtopicsList
        )
    }

    fun parseSingleMcqObject(
        obj: JSONObject,
        appId: String,
        subjectName: String,
        unitName: String,
        topicName: String,
        subtopicName: String,
        defaultLanguage: String = "English"
    ): Question? {
        val questionText = obj.optString(
            "question",
            obj.optString("questionText", obj.optString("question_text", obj.optString("q", "")))
        ).trim()
        if (questionText.isEmpty()) return null

        val subtopic = if (subtopicName.isNotBlank()) subtopicName else obj.optString("subtopic", obj.optString("subtopic_name", "")).trim()
        val language = obj.optString("language", defaultLanguage)
        var explanation = obj.optString(
            "explanation",
            obj.optString("explanationText", obj.optString("solution", obj.optString("desc", "")))
        ).trim()

        val optionsMap = linkedMapOf<String, String>()
        if (obj.has("options")) {
            val optVal = obj.get("options")
            if (optVal is JSONArray) {
                val labels = listOf("A", "B", "C", "D", "E", "F")
                for (k in 0 until optVal.length()) {
                    val label = if (k < labels.size) labels[k] else ('A'.code + k).toChar().toString()
                    optionsMap[label] = optVal.optString(k, "").trim()
                }
            } else if (optVal is JSONObject) {
                listOf("A", "B", "C", "D").forEach { label ->
                    val v = optVal.optString(label, optVal.optString(label.lowercase(), "")).trim()
                    if (v.isNotEmpty()) {
                        optionsMap[label] = v
                    }
                }
                val keys = optVal.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val normalizedKey = k.uppercase().trim()
                    if (!optionsMap.containsKey(normalizedKey)) {
                        optionsMap[normalizedKey] = optVal.optString(k, "").trim()
                    }
                }
            }
        } else {
            // Explicit options: optA / optionA / A
            listOf("A", "B", "C", "D").forEach { label ->
                val text = obj.optString("option$label", obj.optString("opt$label", obj.optString(label, obj.optString(label.lowercase(), "")))).trim()
                if (text.isNotEmpty()) {
                    optionsMap[label] = text
                }
            }
        }

        // Parse right answer
        val rawAnswer = obj.optString(
            "answer",
            obj.optString("right_answer", obj.optString("correct_answer", obj.optString("correctOption", obj.optString("correct", ""))))
        ).trim()

        var correctOptionKey = "A"
        var fullTextAnswer = rawAnswer

        if (rawAnswer.isNotEmpty()) {
            val upper = rawAnswer.uppercase()
            if (optionsMap.containsKey(upper)) {
                correctOptionKey = upper
                fullTextAnswer = optionsMap[upper] ?: rawAnswer
            } else {
                // Try matching exact text of options (ignoring case, internal whitespace differences, and trailing punctuation)
                val normalizedRaw = rawAnswer.replace(Regex("""\s+"""), " ").trimEnd('.', ';', ',')
                val matchedEntry = optionsMap.entries.find { entry ->
                    val normalizedOpt = entry.value.replace(Regex("""\s+"""), " ").trimEnd('.', ';', ',')
                    normalizedOpt.equals(normalizedRaw, ignoreCase = true)
                }
                if (matchedEntry != null) {
                    correctOptionKey = matchedEntry.key
                    fullTextAnswer = matchedEntry.value
                } else {
                    // Try regex match for "Option A" or "(A)"
                    val letterMatch = Regex("""(?i)^(?:option|opt)?\s*[\(\[]?([A-D])[\)\]\.]?""").find(rawAnswer)
                    val extractedLetter = letterMatch?.groupValues?.getOrNull(1)?.uppercase()
                    if (extractedLetter != null && optionsMap.containsKey(extractedLetter)) {
                        correctOptionKey = extractedLetter
                        fullTextAnswer = optionsMap[extractedLetter] ?: rawAnswer
                    } else {
                        // Try numeric index (e.g. 1 -> A, 2 -> B)
                        val num = rawAnswer.toIntOrNull()
                        if (num != null) {
                            correctOptionKey = when (num) {
                                0, 1 -> "A"
                                2 -> "B"
                                3 -> "C"
                                4 -> "D"
                                else -> "A"
                            }
                            fullTextAnswer = optionsMap[correctOptionKey] ?: rawAnswer
                        }
                    }
                }
            }
        } else if (obj.has("right_answer_index") || obj.has("answerIndex")) {
            val idx = obj.optInt("right_answer_index", obj.optInt("answerIndex", 0))
            correctOptionKey = when (idx) {
                0 -> "A"
                1 -> "B"
                2 -> "C"
                3 -> "D"
                else -> "A"
            }
            fullTextAnswer = optionsMap[correctOptionKey] ?: ""
        }

        // Preserve original question ID exactly as given in the JSON (e.g. sat_sub01_q01)
        val rawId = obj.optString("id", obj.optString("mcq_id", obj.optString("q_id", obj.optString("question_id", obj.optString("qid", obj.optString("questionId", obj.optString("code", ""))))))).trim()
        val questionId = if (rawId.isNotBlank()) rawId else "mcq-${UUID.randomUUID().toString().take(8)}"

        val contentHash = calculateQuestionHash(questionText, optionsMap)

        if (explanation.isBlank() && fullTextAnswer.isNotBlank() && fullTextAnswer != correctOptionKey) {
            explanation = "Answer: Option $correctOptionKey ($fullTextAnswer)"
        }

        return Question(
            id = questionId,
            appId = appId,
            subjectId = obj.optString("subjectId", ""),
            unitId = obj.optString("unitId", ""),
            topicId = obj.optString("topicId", ""),
            contentLevel = "SUBTOPIC",
            subtopic = subtopic,
            questionText = questionText,
            options = optionsMap,
            correctOption = correctOptionKey,
            explanation = explanation.ifBlank { null },
            customFields = mapOf("fullTextAnswer" to fullTextAnswer),
            contentHash = contentHash,
            setName = if (subtopic.isNotBlank()) subtopic else "Set 1",
            difficulty = obj.optString("difficulty", "Medium"),
            language = language,
            isActive = true
        )
    }

    private fun parseSubjects(array: JSONArray, appId: String): SyllabusParseResult.Subjects {
        val list = mutableListOf<Subject>()
        for (i in 0 until array.length()) {
            list.add(parseSingleSubject(array.getJSONObject(i), appId))
        }
        return SyllabusParseResult.Subjects(list)
    }

    private fun parseUnits(array: JSONArray, appId: String): SyllabusParseResult.Units {
        val list = mutableListOf<UnitModel>()
        for (i in 0 until array.length()) {
            list.add(parseSingleUnit(array.getJSONObject(i), appId))
        }
        return SyllabusParseResult.Units(list)
    }

    private fun parseTopics(array: JSONArray, appId: String): SyllabusParseResult.Topics {
        val list = mutableListOf<TopicModel>()
        for (i in 0 until array.length()) {
            list.add(parseSingleTopic(array.getJSONObject(i), appId))
        }
        return SyllabusParseResult.Topics(list)
    }

    private fun parseSingleSubject(obj: JSONObject, appId: String): Subject {
        val units = if (obj.has("units")) {
            val uArray = obj.getJSONArray("units")
            (0 until uArray.length()).map { parseSingleUnit(uArray.getJSONObject(it), appId) }
        } else emptyList()

        val parsedCategories = mutableListOf<String>()
        if (obj.has("categories")) {
            val catArr = obj.optJSONArray("categories")
            if (catArr != null) {
                for (k in 0 until catArr.length()) {
                    parsedCategories.add(catArr.optString(k, ""))
                }
            }
        }

        return Subject(
            id = obj.optString("id", "subj-${UUID.randomUUID()}"),
            appId = appId,
            name = obj.optString("name", "Untitled Subject"),
            category = obj.optString("category", "prelims"),
            categories = parsedCategories.filter { it.isNotBlank() },
            icon = obj.optString("icon", "BookOpen"),
            imageUrl = obj.optString("imageUrl", ""),
            heroQuote = obj.optString("heroQuote", ""),
            color = obj.optString("color", "from-blue-600 to-blue-800"),
            units = units
        )
    }

    private fun parseSingleUnit(obj: JSONObject, appId: String): UnitModel {
        val topics = if (obj.has("topics")) {
            val tArray = obj.getJSONArray("topics")
            (0 until tArray.length()).map { parseSingleTopic(tArray.getJSONObject(it), appId) }
        } else emptyList()

        return UnitModel(
            id = obj.optString("id", "unit-${UUID.randomUUID()}"),
            appId = appId,
            name = obj.optString("name", "Untitled Unit"),
            unitNumber = obj.optInt("unitNumber", 1),
            subtitle = obj.optString("subtitle", ""),
            tagline = obj.optString("tagline", ""),
            quote = obj.optString("quote", ""),
            slogan = obj.optString("slogan", ""),
            description = obj.optString("description", ""),
            imageUrl = obj.optString("imageUrl", ""),
            estimatedMcqs = obj.optString("estimatedMcqs", ""),
            completionPercentage = if (obj.has("completionPercentage")) obj.optInt("completionPercentage") else 0,
            topics = topics
        )
    }

    private fun parseSingleTopic(obj: JSONObject, appId: String): TopicModel {
        val subtopics = if (obj.has("subtopics")) {
            val sArray = obj.getJSONArray("subtopics")
            (0 until sArray.length()).map { sArray.getString(it) }
        } else emptyList()

        return TopicModel(
            id = obj.optString("id", "topic-${UUID.randomUUID()}"),
            appId = appId,
            name = obj.optString("name", "Untitled Topic"),
            subtitle = obj.optString("subtitle", ""),
            tagBadge = obj.optString("tagBadge", ""),
            description = obj.optString("description", ""),
            imageUrl = obj.optString("imageUrl", ""),
            estimatedMcqs = obj.optString("estimatedMcqs", ""),
            practiceSetsCount = obj.optInt("practiceSetsCount", 3),
            difficulty = obj.optString("difficulty", "Medium"),
            completionPercentage = if (obj.has("completionPercentage")) obj.optInt("completionPercentage") else 0,
            notesContent = obj.optString("notesContent", ""),
            subtopics = subtopics
        )
    }
}

