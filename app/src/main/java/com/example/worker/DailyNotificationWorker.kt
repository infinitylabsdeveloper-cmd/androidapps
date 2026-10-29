package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R

class DailyNotificationWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val worker = DailyPracticeReminderWorker(context, workerParams)
        return worker.doWork()
    }

    private fun showNotification() {
        val soundEnabled = com.example.util.NotificationSettingsManager.isNotificationSoundEnabled(context)
        val vibrationEnabled = com.example.util.NotificationSettingsManager.isNotificationVibrationEnabled(context)
        val notificationsEnabled = com.example.util.NotificationSettingsManager.isNotificationsEnabled(context)

        if (!notificationsEnabled) return

        val channelId = "daily_study_reminder_channel_s${if (soundEnabled) "1" else "0"}_v${if (vibrationEnabled) "1" else "0"}"
        val notificationId = 1001

        // Create Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Study Reminders"
            val descriptionText = "Daily notifications to review pending study sets"
            // We use IMPORTANCE_DEFAULT to ensure the notification is visible in the status bar
            // Sound and vibration are configured specifically on the channel
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                if (soundEnabled) {
                    setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
                } else {
                    setSound(null, null)
                }
                enableVibration(vibrationEnabled)
                if (vibrationEnabled) {
                    vibrationPattern = longArrayOf(0, 250, 250, 250)
                } else {
                    vibrationPattern = null
                }
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Create Intent to open MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Build Notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Use app icon as small icon
            .setContentTitle("Time to Study! 📚")
            .setContentText("You have pending practice sets to review. Keep your streak going!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            
        if (!soundEnabled) {
            builder.setSilent(true)
            builder.setSound(null)
        } else {
            builder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
        }

        if (!vibrationEnabled) {
            builder.setVibrate(longArrayOf(0))
        } else {
            builder.setVibrate(longArrayOf(0, 250, 250, 250))
        }

        // Show Notification
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            ) {
                notify(notificationId, builder.build())
            }
        }
    }
}
