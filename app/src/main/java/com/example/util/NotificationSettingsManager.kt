package com.example.util

import android.content.Context
import java.util.Locale

object NotificationSettingsManager {
    private const val PREFS_NAME = "notification_settings_prefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_NOTIFICATION_SOUND_ENABLED = "notification_sound_enabled"
    private const val KEY_NOTIFICATION_VIBRATION_ENABLED = "notification_vibration_enabled"

    private const val KEY_DAILY_PRACTICE_REMINDER_ENABLED = "daily_practice_reminder_enabled"
    private const val KEY_DAILY_QUESTION_TARGET = "daily_question_target"
    private const val KEY_REMINDER_HOUR = "reminder_hour"
    private const val KEY_REMINDER_MINUTE = "reminder_minute"
    private const val KEY_LAST_NOTIFICATION_TIMESTAMP = "last_notification_timestamp"

    private const val KEY_STUDY_MATERIALS_ENABLED = "study_materials_enabled"
    private const val KEY_MOCK_TESTS_ENABLED = "mock_tests_enabled"
    private const val KEY_EXAM_ALERTS_ENABLED = "exam_alerts_enabled"
    private const val KEY_SYSTEM_UPDATES_ENABLED = "system_updates_enabled"

    const val DEFAULT_DAILY_QUESTION_TARGET = 25
    const val DEFAULT_REMINDER_HOUR = 20 // 8:00 PM
    const val DEFAULT_REMINDER_MINUTE = 0

    fun isNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun isNotificationSoundEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATION_SOUND_ENABLED, true)
    }

    fun isNotificationVibrationEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATION_VIBRATION_ENABLED, true)
    }

    fun isDailyPracticeReminderEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DAILY_PRACTICE_REMINDER_ENABLED, true)
    }

    fun getDailyQuestionTarget(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_DAILY_QUESTION_TARGET, DEFAULT_DAILY_QUESTION_TARGET).coerceAtLeast(5)
    }

    fun getReminderHour(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_REMINDER_HOUR, DEFAULT_REMINDER_HOUR)
    }

    fun getReminderMinute(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_REMINDER_MINUTE, DEFAULT_REMINDER_MINUTE)
    }

    fun getFormattedReminderTime(context: Context): String {
        val hour = getReminderHour(context)
        val minute = getReminderMinute(context)
        return formatTime(hour, minute)
    }

    fun formatTime(hour: Int, minute: Int): String {
        val isAm = hour < 12
        val displayHour = when (hour) {
            0 -> 12
            in 1..12 -> hour
            else -> hour - 12
        }
        val amPm = if (isAm) "AM" else "PM"
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm)
    }

    fun saveNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun saveNotificationSoundEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFICATION_SOUND_ENABLED, enabled).apply()
    }

    fun saveNotificationVibrationEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFICATION_VIBRATION_ENABLED, enabled).apply()
    }

    fun saveDailyPracticeReminderEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DAILY_PRACTICE_REMINDER_ENABLED, enabled).apply()
    }

    fun saveDailyQuestionTarget(context: Context, target: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_DAILY_QUESTION_TARGET, target.coerceAtLeast(5)).apply()
    }

    fun saveReminderTime(context: Context, hour: Int, minute: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
    }

    fun recordNotificationSent(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_NOTIFICATION_TIMESTAMP, System.currentTimeMillis()).apply()
    }

    fun getLastNotificationTimestamp(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_NOTIFICATION_TIMESTAMP, 0L)
    }

    fun isStudyMaterialsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_STUDY_MATERIALS_ENABLED, true)
    }

    fun saveStudyMaterialsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_STUDY_MATERIALS_ENABLED, enabled).apply()
    }

    fun isMockTestsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MOCK_TESTS_ENABLED, true)
    }

    fun saveMockTestsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MOCK_TESTS_ENABLED, enabled).apply()
    }

    fun isExamAlertsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_EXAM_ALERTS_ENABLED, true)
    }

    fun saveExamAlertsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_EXAM_ALERTS_ENABLED, enabled).apply()
    }

    fun isSystemUpdatesEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SYSTEM_UPDATES_ENABLED, true)
    }

    fun saveSystemUpdatesEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SYSTEM_UPDATES_ENABLED, enabled).apply()
    }
}
