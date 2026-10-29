package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.util.NotificationSettingsManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Enterprise WorkManager scheduler for APPSC Exam Prep daily notifications.
 * Manages periodic daily reminder scheduling, dynamic time-of-day offsets,
 * cancellation, and on-demand one-time test triggering.
 */
object NotificationScheduler {

    const val WORK_NAME_DAILY_PRACTICE = "daily_practice_question_reminder"
    const val TAG_DAILY_PRACTICE = "tag_daily_practice_reminder"

    /**
     * Schedules or updates the periodic daily practice reminder worker.
     * Computes the precise initial delay until the candidate's preferred study time (e.g. 8:00 PM).
     */
    fun scheduleDailyPracticeReminder(context: Context, replaceExisting: Boolean = true) {
        val isEnabled = NotificationSettingsManager.isDailyPracticeReminderEnabled(context) &&
                NotificationSettingsManager.isNotificationsEnabled(context)

        if (!isEnabled) {
            Log.d("NotificationScheduler", "Daily practice reminder is disabled. Cancelling any scheduled work.")
            cancelDailyPracticeReminder(context)
            return
        }

        val hour = NotificationSettingsManager.getReminderHour(context)
        val minute = NotificationSettingsManager.getReminderMinute(context)
        val initialDelayMs = calculateInitialDelayMs(hour, minute)

        Log.d("NotificationScheduler", "Scheduling daily practice reminder for $hour:$minute (Initial delay: ${initialDelayMs / 1000 / 60} minutes)")

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<DailyPracticeReminderWorker>(
            24, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // 15-minute flex window
        )
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(TAG_DAILY_PRACTICE)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                15, TimeUnit.MINUTES
            )
            .build()

        val workPolicy = if (replaceExisting) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_DAILY_PRACTICE,
            workPolicy,
            periodicWorkRequest
        )
    }

    /**
     * Backward-compatible alias for initializing daily notifications on app start.
     */
    fun scheduleDailyReminder(context: Context) {
        scheduleDailyPracticeReminder(context, replaceExisting = false)
    }

    /**
     * Cancels the scheduled daily practice reminder periodic work.
     */
    fun cancelDailyPracticeReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_DAILY_PRACTICE)
    }

    /**
     * Enqueues an immediate OneTimeWorkRequest to trigger the practice notification instantly.
     * Useful for testing, verifying notification channels, and manual goal preview.
     */
    fun triggerImmediatePracticeReminder(context: Context) {
        val oneTimeWork = OneTimeWorkRequestBuilder<DailyPracticeReminderWorker>()
            .addTag("test_immediate_practice_reminder")
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueue(oneTimeWork)
    }

    /**
     * Computes milliseconds from now until the next occurrence of [targetHour]:[targetMinute].
     */
    fun calculateInitialDelayMs(targetHour: Int, targetMinute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If time has already passed today, schedule for tomorrow
            if (before(now) || timeInMillis <= now.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
    }
}

