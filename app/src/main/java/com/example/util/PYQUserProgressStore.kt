package com.example.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class PYQAttemptRecord(
    val attemptId: String,
    val paperId: String,
    val appId: String,
    val examName: String,
    val year: Int,
    val stage: String, // Prelims, Mains
    val score: Double,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unansweredCount: Int,
    val timeTakenSeconds: Long,
    val accuracy: Double,
    val timestamp: Long,
    val mode: String, // "Full Paper", "Practice", "Exam"
    val userAnswersJson: String, // JSON map of questionIndex -> chosenOption
    val bookmarkedIndicesJson: String, // JSON array of bookmarked question numbers
    val wrongIndicesJson: String // JSON array of wrong question numbers
)

object PYQUserProgressStore {
    private const val PREFS_NAME = "pyq_user_progress_prefs_v1"
    private const val KEY_ATTEMPTS = "pyq_attempts_history"
    private const val KEY_BOOKMARKS = "pyq_global_bookmarks"

    fun saveAttempt(context: Context, record: PYQAttemptRecord) {
        val attempts = getAttempts(context).toMutableList()
        attempts.add(record)
        persistAttempts(context, attempts)
    }

    fun getAttemptsForPaper(context: Context, paperId: String): List<PYQAttemptRecord> {
        return getAttempts(context).filter { it.paperId == paperId }
    }

    fun getAttemptsForApp(context: Context, appId: String): List<PYQAttemptRecord> {
        return getAttempts(context).filter { it.appId.equals(appId, ignoreCase = true) }
    }

    fun getAttempts(context: Context): List<PYQAttemptRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_ATTEMPTS, "[]") ?: "[]"
        val list = mutableListOf<PYQAttemptRecord>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    PYQAttemptRecord(
                        attemptId = obj.optString("attemptId", "att_$i"),
                        paperId = obj.optString("paperId", ""),
                        appId = obj.optString("appId", "default"),
                        examName = obj.optString("examName", "Exam"),
                        year = obj.optInt("year", 2025),
                        stage = obj.optString("stage", "Prelims"),
                        score = obj.optDouble("score", 0.0),
                        totalQuestions = obj.optInt("totalQuestions", 200),
                        correctCount = obj.optInt("correctCount", 0),
                        wrongCount = obj.optInt("wrongCount", 0),
                        unansweredCount = obj.optInt("unansweredCount", 0),
                        timeTakenSeconds = obj.optLong("timeTakenSeconds", 0L),
                        accuracy = obj.optDouble("accuracy", 0.0),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        mode = obj.optString("mode", "Exam Mode"),
                        userAnswersJson = obj.optString("userAnswersJson", "{}"),
                        bookmarkedIndicesJson = obj.optString("bookmarkedIndicesJson", "[]"),
                        wrongIndicesJson = obj.optString("wrongIndicesJson", "[]")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun persistAttempts(context: Context, list: List<PYQAttemptRecord>) {
        val arr = JSONArray()
        for (r in list) {
            val obj = JSONObject().apply {
                put("attemptId", r.attemptId)
                put("paperId", r.paperId)
                put("appId", r.appId)
                put("examName", r.examName)
                put("year", r.year)
                put("stage", r.stage)
                put("score", r.score)
                put("totalQuestions", r.totalQuestions)
                put("correctCount", r.correctCount)
                put("wrongCount", r.wrongCount)
                put("unansweredCount", r.unansweredCount)
                put("timeTakenSeconds", r.timeTakenSeconds)
                put("accuracy", r.accuracy)
                put("timestamp", r.timestamp)
                put("mode", r.mode)
                put("userAnswersJson", r.userAnswersJson)
                put("bookmarkedIndicesJson", r.bookmarkedIndicesJson)
                put("wrongIndicesJson", r.wrongIndicesJson)
            }
            arr.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ATTEMPTS, arr.toString()).apply()
    }

    fun isQuestionBookmarked(context: Context, paperId: String, questionIndex: Int): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_BOOKMARKS, "{}") ?: "{}"
        return try {
            val root = JSONObject(jsonStr)
            val arr = root.optJSONArray(paperId) ?: JSONArray()
            for (i in 0 until arr.length()) {
                if (arr.getInt(i) == questionIndex) return true
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    fun toggleBookmark(context: Context, paperId: String, questionIndex: Int): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_BOOKMARKS, "{}") ?: "{}"
        var isBookmarkedNow = false
        try {
            val root = JSONObject(jsonStr)
            val arr = root.optJSONArray(paperId) ?: JSONArray()
            val list = mutableListOf<Int>()
            for (i in 0 until arr.length()) {
                list.add(arr.getInt(i))
            }
            if (list.contains(questionIndex)) {
                list.remove(questionIndex)
                isBookmarkedNow = false
            } else {
                list.add(questionIndex)
                isBookmarkedNow = true
            }
            val newArr = JSONArray()
            list.forEach { newArr.put(it) }
            root.put(paperId, newArr)
            prefs.edit().putString(KEY_BOOKMARKS, root.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isBookmarkedNow
    }

    fun getBookmarkedIndicesForPaper(context: Context, paperId: String): List<Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_BOOKMARKS, "{}") ?: "{}"
        val list = mutableListOf<Int>()
        try {
            val root = JSONObject(jsonStr)
            val arr = root.optJSONArray(paperId) ?: JSONArray()
            for (i in 0 until arr.length()) {
                list.add(arr.getInt(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
