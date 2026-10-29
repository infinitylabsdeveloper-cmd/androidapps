package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.repository.AppConfigRepository
import com.example.repository.UserProgressRepository
import com.example.util.NotificationSettingsManager

/**
 * Enterprise WorkManager CoroutineWorker that runs daily to remind aspirants
 * to practice their customized target quota of test questions.
 *
 * Dynamically queries local Room database for today's answered questions,
 * calculates remaining question quota, and presents an interactive, expandable
 * Material 3 notification with direct action triggers to start testing immediately.
 */
class DailyPracticeReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "daily_practice_reminder_channel"
        const val CHANNEL_NAME = "Daily Practice Quota Reminders"
        const val NOTIFICATION_ID = 2024
        const val EXTRA_NAVIGATE_TO = "EXTRA_NAVIGATE_TO"
        const val NAV_DEST_DAILY_QUIZ = "daily_quiz"
        const val NAV_DEST_PRACTICE = "practice"
        const val NAV_DEST_ANALYTICS = "profile"
    }

    override suspend fun doWork(): Result {
        return try {
            val notificationsMasterEnabled = NotificationSettingsManager.isNotificationsEnabled(context)
            val practiceReminderEnabled = NotificationSettingsManager.isDailyPracticeReminderEnabled(context)

            if (!notificationsMasterEnabled || !practiceReminderEnabled) {
                Log.d("PracticeReminderWorker", "Notifications or Practice Reminder disabled in settings. Skipping.")
                return Result.success()
            }

            sendPracticeReminderNotification()
            NotificationSettingsManager.recordNotificationSent(context)
            Result.success()
        } catch (e: Exception) {
            Log.e("PracticeReminderWorker", "Error executing daily practice reminder worker", e)
            Result.retry()
        }
    }

    private suspend fun sendPracticeReminderNotification() {
        val soundEnabled = NotificationSettingsManager.isNotificationSoundEnabled(context)
        val vibrationEnabled = NotificationSettingsManager.isNotificationVibrationEnabled(context)
        val targetQuestions = NotificationSettingsManager.getDailyQuestionTarget(context)

        val activeConfig = AppConfigRepository.getInstance().getCurrentConfig()
        val appId = activeConfig.appId.ifBlank { "appsc-group2" }
        val appName = activeConfig.appName.ifBlank { "APPSC Group-2" }

        // Fetch user progress from Room
        val progressRepo = UserProgressRepository.getInstance(context)
        val db = com.example.db.AppDatabase.getDatabase(context)
        val userProgressEntity = db.userProgressDao().getUserProgressDirect(appId)
        val answeredToday = userProgressEntity?.dailyQuestionsAnswered ?: 0
        val streakDays = (userProgressEntity?.streakDays ?: 1).coerceAtLeast(1)
        val remaining = (targetQuestions - answeredToday).coerceAtLeast(0)

        // Notification Channel Setup (Android 8.0+)
        createNotificationChannel(soundEnabled, vibrationEnabled)

        // Main Tap Intent -> Opens App at Daily Quiz / Practice
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TO, NAV_DEST_DAILY_QUIZ)
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: "Start Practice 🎯"
        val practiceIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TO, NAV_DEST_PRACTICE)
        }
        val practicePendingIntent = PendingIntent.getActivity(
            context,
            1,
            practiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: "View Progress 📊"
        val statsIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TO, NAV_DEST_ANALYTICS)
        }
        val statsPendingIntent = PendingIntent.getActivity(
            context,
            2,
            statsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dynamic Header & Body Content based on question completion status
        val title: String
        val shortContent: String
        val bigTextSummary: String

        if (answeredToday == 0) {
            title = "Daily Practice Reminder: Solve $targetQuestions MCQs Today 🎯"
            shortContent = "You haven't practiced any questions today. Start now to keep your $streakDays-day streak alive!"
            bigTextSummary = """
                📚 Exam: $appName
                🎯 Daily Question Target: $targetQuestions MCQs
                📝 Questions Completed Today: 0 / $targetQuestions
                ⏳ Remaining Today: $targetQuestions MCQs
                🔥 Current Study Streak: $streakDays Days
                
                💡 Consistent daily MCQ practice is proven to boost state-level APPSC exam rankings! Tap below to start your sprint.
            """.trimIndent()
        } else if (remaining > 0) {
            title = "Keep It Up! 🔥 $remaining more questions to reach goal"
            shortContent = "You've solved $answeredToday of $targetQuestions questions today. $remaining to go!"
            bigTextSummary = """
                📚 Exam: $appName
                🎯 Daily Question Target: $targetQuestions MCQs
                ✅ Completed Today: $answeredToday / $targetQuestions MCQs
                ⏳ Remaining for Today's Goal: $remaining MCQs
                🔥 Current Study Streak: $streakDays Days
                
                ⚡ You're more than halfway there! Complete the remaining $remaining questions to secure today's XP points.
            """.trimIndent()
        } else {
            title = "Daily Goal Accomplished! 🏆 ($answeredToday/$targetQuestions MCQs)"
            shortContent = "Awesome job! You reached your $targetQuestions question target for today. Keep practicing for bonus XP!"
            bigTextSummary = """
                📚 Exam: $appName
                🏆 Daily Target Status: COMPLETED ($answeredToday / $targetQuestions MCQs)
                🔥 Current Study Streak: $streakDays Days
                ⭐ Bonus XP Unlocked!
                
                🚀 Want to push your APPSC rank even higher? Take a Grand Mock Test or 1v1 Speed Duel!
            """.trimIndent()
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigTextSummary))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .setColor(0xFF0284C7.toInt())
            .addAction(
                android.R.drawable.ic_media_play,
                "Start Practice 🚀",
                practicePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_agenda,
                "My Progress 📊",
                statsPendingIntent
            )

        if (!soundEnabled) {
            builder.setSilent(true)
            builder.setSound(null)
        } else {
            builder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
        }

        if (vibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 300, 200, 300))
        } else {
            builder.setVibrate(longArrayOf(0))
        }

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            ) {
                notify(NOTIFICATION_ID, builder.build())
            }
        }
    }

    private fun createNotificationChannel(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Daily scheduled reminders for practice question quotas and study streak maintenance"
                enableLights(true)
                lightColor = Color.YELLOW
                if (soundEnabled) {
                    setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
                } else {
                    setSound(null, null)
                }
                enableVibration(vibrationEnabled)
                if (vibrationEnabled) {
                    vibrationPattern = longArrayOf(0, 300, 200, 300)
                }
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
