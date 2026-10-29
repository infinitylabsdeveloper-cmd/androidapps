package com.example.util

import android.content.Context
import com.example.model.PreviousYearPaper
import com.example.model.PreviousYearQuestion
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object PreviousYearPaperStore {
    private const val PREFS_NAME = "pyp_secure_storage"
    private const val KEY_PAPERS = "pyp_papers_json"

    fun getAllPapers(context: Context): List<PreviousYearPaper> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_PAPERS, "[]") ?: "[]"
        val list = mutableListOf<PreviousYearPaper>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val questionsArr = obj.optJSONArray("questions") ?: JSONArray()
                val qList = mutableListOf<PreviousYearQuestion>()
                for (j in 0 until questionsArr.length()) {
                    val qObj = questionsArr.getJSONObject(j)
                    val optsObj = qObj.optJSONObject("options") ?: JSONObject()
                    val optsMap = mutableMapOf<String, String>()
                    val keys = optsObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        optsMap[k] = optsObj.optString(k, "")
                    }
                    qList.add(
                        PreviousYearQuestion(
                            questionNumber = qObj.optInt("questionNumber", j + 1),
                            question = qObj.optString("question", qObj.optString("text", "")),
                            options = optsMap,
                            correctAnswer = qObj.optString("correctAnswer", "A"),
                            explanation = qObj.optString("explanation").takeIf { it.isNotBlank() },
                            difficulty = qObj.optString("difficulty").takeIf { it.isNotBlank() }
                        )
                    )
                }
                list.add(
                    PreviousYearPaper(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        appId = obj.optString("appId", "default"),
                        examName = obj.optString("examName", "Exam"),
                        year = obj.optInt("year", 2025),
                        examDate = obj.optString("examDate", "2025-01-01"),
                        shift = obj.optString("shift", "Shift 1"),
                        language = obj.optString("language", "English"),
                        subject = obj.optString("subject").takeIf { it.isNotBlank() },
                        isPublished = obj.optBoolean("isPublished", true),
                        questions = qList
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getPapersForApp(context: Context, appId: String): List<PreviousYearPaper> {
        return getAllPapers(context).filter { it.appId.equals(appId, ignoreCase = true) && it.isPublished }
    }

    fun savePaper(context: Context, paper: PreviousYearPaper) {
        val list = getAllPapers(context).toMutableList()
        val existingIndex = list.indexOfFirst { it.id == paper.id }
        if (existingIndex >= 0) {
            list[existingIndex] = paper
        } else {
            list.add(paper)
        }
        persistList(context, list)
    }

    fun deletePaper(context: Context, id: String) {
        val list = getAllPapers(context).filter { it.id != id }
        persistList(context, list)
    }

    fun togglePublish(context: Context, id: String) {
        val list = getAllPapers(context).map {
            if (it.id == id) it.copy(isPublished = !it.isPublished) else it
        }
        persistList(context, list)
    }

    private fun persistList(context: Context, list: List<PreviousYearPaper>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        for (p in list) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("appId", p.appId)
                put("examName", p.examName)
                put("year", p.year)
                put("examDate", p.examDate)
                put("shift", p.shift)
                put("language", p.language)
                put("subject", p.subject ?: "")
                put("isPublished", p.isPublished)
                val qArr = JSONArray()
                for (q in p.questions) {
                    val qObj = JSONObject().apply {
                        put("questionNumber", q.questionNumber)
                        put("question", q.question)
                        val oObj = JSONObject()
                        for ((k, v) in q.options) {
                            oObj.put(k, v)
                        }
                        put("options", oObj)
                        put("correctAnswer", q.correctAnswer)
                        put("explanation", q.explanation ?: "")
                        put("difficulty", q.difficulty ?: "")
                    }
                    qArr.put(qObj)
                }
                put("questions", qArr)
            }
            arr.put(obj)
        }
        prefs.edit().putString(KEY_PAPERS, arr.toString()).apply()
    }

    fun parseJsonImport(jsonString: String): Result<PreviousYearPaper> {
        return try {
            val obj = JSONObject(jsonString.trim())
            val appId = obj.optString("appId", "").trim()
            val examName = obj.optString("examName", "").trim()
            val year = obj.optInt("year", 2025)
            val examDate = obj.optString("examDate", "2025-01-01").trim()
            val shift = obj.optString("shift", "Shift 1").trim()
            val language = obj.optString("language", "English").trim()
            val subject = obj.optString("subject").takeIf { it.isNotBlank() }

            if (appId.isEmpty()) return Result.failure(IllegalArgumentException("Missing required field: appId"))
            if (examName.isEmpty()) return Result.failure(IllegalArgumentException("Missing required field: examName"))

            val questionsArr = obj.optJSONArray("questions") ?: return Result.failure(IllegalArgumentException("Missing or invalid 'questions' array"))
            val qList = mutableListOf<PreviousYearQuestion>()

            for (i in 0 until questionsArr.length()) {
                val qObj = questionsArr.getJSONObject(i)
                val qNum = qObj.optInt("questionNumber", i + 1)
                val qText = qObj.optString("question", qObj.optString("text", "")).trim()
                if (qText.isEmpty()) return Result.failure(IllegalArgumentException("Question #${qNum} is missing question text"))

                val optsObj = qObj.optJSONObject("options") ?: return Result.failure(IllegalArgumentException("Question #${qNum} is missing options object"))
                val optsMap = mutableMapOf<String, String>()
                for (k in listOf("A", "B", "C", "D")) {
                    val optVal = optsObj.optString(k, "").trim()
                    if (optVal.isNotEmpty()) {
                        optsMap[k] = optVal
                    }
                }
                if (optsMap.isEmpty()) return Result.failure(IllegalArgumentException("Question #${qNum} has no valid options (A, B, C, D)"))

                val correct = qObj.optString("correctAnswer", "A").trim().uppercase()
                qList.add(
                    PreviousYearQuestion(
                        questionNumber = qNum,
                        question = qText,
                        options = optsMap,
                        correctAnswer = correct,
                        explanation = qObj.optString("explanation").takeIf { it.isNotBlank() },
                        difficulty = qObj.optString("difficulty").takeIf { it.isNotBlank() }
                    )
                )
            }

            val paper = PreviousYearPaper(
                id = UUID.randomUUID().toString(),
                appId = appId,
                examName = examName,
                year = year,
                examDate = examDate,
                shift = shift,
                language = language,
                subject = subject,
                isPublished = true,
                questions = qList
            )
            Result.success(paper)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
