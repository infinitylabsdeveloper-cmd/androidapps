package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class PracticeSession(
    val sessionId: String,
    val startTimestamp: Long,
    val elapsedTime: Long, // cumulative seconds
    val questionIdsOrder: List<String>,
    val currentQuestionIndex: Int,
    val userAnswers: Map<String, String>,
    val markedForReviewIds: Set<String>,
    val completedTimestamp: Long = 0L,
    val isCompleted: Boolean = false
)

object PracticeSessionManager {
    private const val PREFS_NAME = "practice_sessions_prefs"
    private const val KEY_SESSIONS_PREFIX = "session_"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(context: Context, session: PracticeSession) {
        val prefs = getPrefs(context)
        val json = gson.toJson(session)
        prefs.edit().putString(KEY_SESSIONS_PREFIX + session.sessionId, json).apply()
    }

    fun getSession(context: Context, sessionId: String): PracticeSession? {
        val prefs = getPrefs(context)
        val json = prefs.getString(KEY_SESSIONS_PREFIX + sessionId, null) ?: return null
        return try {
            gson.fromJson(json, PracticeSession::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun deleteSession(context: Context, sessionId: String) {
        val prefs = getPrefs(context)
        prefs.edit().remove(KEY_SESSIONS_PREFIX + sessionId).apply()
    }

    fun getSessionStatus(context: Context, sessionId: String, hasDbCompleted: Boolean): String {
        if (hasDbCompleted) return "Completed"
        val session = getSession(context, sessionId) ?: return "Not Attempted"
        return if (session.isCompleted) {
            "Completed"
        } else if (session.userAnswers.isNotEmpty() || session.elapsedTime > 0) {
            "Attempting"
        } else {
            "Not Attempted"
        }
    }
}
