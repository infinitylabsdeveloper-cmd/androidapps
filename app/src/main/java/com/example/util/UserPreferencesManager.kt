package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

/**
 * Manages user UI preferences such as visibility of AI Mentor on the home screen,
 * quick toggles, and personalization flags.
 */
object UserPreferencesManager {
    private const val PREF_NAME = "user_app_preferences"
    private const val KEY_SHOW_AI_MENTOR_HOME = "show_ai_mentor_on_home"
    private const val KEY_SHOW_DAILY_GOAL_BANNER = "show_daily_goal_banner"

    private val _aiMentorHomeVisibleState = mutableStateOf(true)
    val aiMentorHomeVisibleState: State<Boolean> = _aiMentorHomeVisibleState

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Initializes preferences state from persistent storage.
     */
    fun initialize(context: Context) {
        val prefs = getPrefs(context)
        val isVisible = prefs.getBoolean(KEY_SHOW_AI_MENTOR_HOME, true)
        _aiMentorHomeVisibleState.value = isVisible
    }

    /**
     * Checks if AI Mentor floating shortcut should be visible on the Home Screen.
     */
    fun isAiMentorHomeVisible(context: Context): Boolean {
        val prefs = getPrefs(context)
        val isVisible = prefs.getBoolean(KEY_SHOW_AI_MENTOR_HOME, true)
        _aiMentorHomeVisibleState.value = isVisible
        return isVisible
    }

    /**
     * Updates visibility of AI Mentor shortcut on the Home Screen.
     */
    fun setAiMentorHomeVisible(context: Context, visible: Boolean) {
        val prefs = getPrefs(context)
        prefs.edit().putBoolean(KEY_SHOW_AI_MENTOR_HOME, visible).apply()
        _aiMentorHomeVisibleState.value = visible
    }
}
