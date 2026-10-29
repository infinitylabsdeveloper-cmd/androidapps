package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object SubscriptionNotificationHelper {
    fun sendCancellationConfirmation(context: Context, email: String, expiryDateStr: String) {
        // 1. Simulate Confirmation Email
        Log.i("SubscriptionEmail", "📧 [CONFIRMATION EMAIL SENT] To: $email | Subject: Subscription Cancellation Confirmed | Body: Your auto-renewal has been successfully cancelled. Premium access remains active until $expiryDateStr. Thank you for using our app.")
        
        // 2. Trigger Push Notification
        val channelId = "subscription_cancellation_channel"
        val notificationId = 2002

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Subscription Alerts"
            val descriptionText = "Notifications regarding subscription status and renewals"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Subscription Cancelled ⚠️")
            .setContentText("Access active until $expiryDateStr. Confirmation email sent.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Your subscription auto-renewal has been successfully cancelled. You will continue to enjoy full premium access until $expiryDateStr. A confirmation email has been dispatched to $email."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notify(notificationId, builder.build())
                }
            } catch (e: Exception) {
                Log.e("SubscriptionNotificationHelper", "Failed to send notification", e)
            }
        }
    }
}
