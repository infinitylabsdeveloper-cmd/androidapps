package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

enum class ShareChannel {
    NATIVE_CHOOSER,
    WHATSAPP,
    TELEGRAM,
    FACEBOOK,
    SMS,
    TWITTER_X,
    CLIPBOARD
}

/**
 * Enterprise-grade App Sharing & Deep-Link Utility for APPSC Exam Prep App.
 * Handles native Android share sheets, direct social app sharing (WhatsApp, Telegram, etc.),
 * official Google Play Store links, and deep-link generation.
 */
object AppShareManager {

    const val APPLICATION_ID = "com.aistudio.appscprep.fhdqkx"
    const val PLAY_STORE_WEB_URL = "https://play.google.com/store/apps/details?id=$APPLICATION_ID"
    const val PLAY_STORE_MARKET_URI = "market://details?id=$APPLICATION_ID"

    /**
     * Generates a high-converting, professional invitation message tailored for competitive exam aspirants.
     */
    fun buildAppShareMessage(appName: String = "Exam Preparation"): String {
        val region = com.example.data.SampleData.getActiveApp().region.ifBlank { "state" }
        return """
            📚 *${appName.uppercase()} Official Exam Preparation App* 🎯
            
            Accelerate your exam preparation with $region's premier study companion:
            
            ⭐ 10,000+ Topic-wise & Unit-wise MCQs with Detailed Solutions
            🏆 State-level Live Leaderboard & Real-time Ranks
            ⚡ 1v1 Speed Duel Quiz Arena (10-Q Sprint)
            📝 Full-Length Grand Mock Tests (Paper 1 & Paper 2)
            📰 Daily Current Affairs & Daily Quiz Challenge
            📥 100% Offline Mode (Practice anywhere without internet)
            
            📲 *Download Free on Google Play Store:*
            $PLAY_STORE_WEB_URL
            
            Join thousands of serious aspirants and secure your dream government post today! 🚀
        """.trimIndent()
    }

    /**
     * Generates a score/achievement sharing message for mock tests or speed duels.
     */
    fun buildScoreShareMessage(
        testTitle: String,
        score: Double,
        maxMarks: Double,
        accuracy: Double,
        appName: String = "APPSC Group-2"
    ): String {
        return """
            🎯 *I just completed: $testTitle* on $appName!
            
            📊 My Performance:
            • Score: ${"%.2f".format(score)} / ${maxMarks.toInt()} Marks
            • Accuracy: ${"%.1f".format(accuracy)}%
            
            Can you beat my score? Download the app and take the test:
            $PLAY_STORE_WEB_URL
            
            #APPSC #Group2 #ExamPrep #CompetitiveExams
        """.trimIndent()
    }

    /**
     * Shares the app link using either a specific channel or the native Android system share sheet.
     */
    fun shareApp(
        context: Context,
        appName: String = "APPSC Group-2",
        channel: ShareChannel = ShareChannel.NATIVE_CHOOSER
    ) {
        val shareText = buildAppShareMessage(appName)
        val subject = "$appName Preparation App - Free Download on Google Play"

        when (channel) {
            ShareChannel.NATIVE_CHOOSER -> launchNativeChooser(context, shareText, subject)
            ShareChannel.WHATSAPP -> shareToPackage(context, "com.whatsapp", shareText, subject)
            ShareChannel.TELEGRAM -> shareToPackage(context, "org.telegram.messenger", shareText, subject)
            ShareChannel.FACEBOOK -> launchNativeChooser(context, shareText, subject)
            ShareChannel.SMS -> shareViaSms(context, shareText)
            ShareChannel.TWITTER_X -> shareViaTwitter(context, shareText)
            ShareChannel.CLIPBOARD -> copyToClipboard(context, PLAY_STORE_WEB_URL, "Play Store link copied to clipboard!")
        }
    }

    /**
     * Shares a candidate's mock test / speed duel score with the official Play Store link.
     */
    fun shareTestScore(
        context: Context,
        testTitle: String,
        score: Double,
        maxMarks: Double,
        accuracy: Double,
        appName: String = "APPSC Group-2"
    ) {
        val shareText = buildScoreShareMessage(testTitle, score, maxMarks, accuracy, appName)
        val subject = "My $appName Test Score: $testTitle"
        launchNativeChooser(context, shareText, subject)
    }

    /**
     * Launches the native Android Share Sheet (`Intent.createChooser`).
     */
    fun launchNativeChooser(context: Context, shareText: String, subject: String) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooserIntent = Intent.createChooser(sendIntent, "Share App via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Unable to open share sheet: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Direct share to a specific package with automatic fallback to native chooser if app is not installed.
     */
    private fun shareToPackage(context: Context, packageName: String, shareText: String, subject: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, shareText)
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Target package not installed or blocked; fallback gracefully to native chooser
            launchNativeChooser(context, shareText, subject)
        }
    }

    /**
     * Direct SMS intent.
     */
    private fun shareViaSms(context: Context, shareText: String) {
        try {
            val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("smsto:")
                putExtra("sms_body", shareText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
        } catch (e: Exception) {
            launchNativeChooser(context, shareText, "Share App via SMS")
        }
    }

    /**
     * Direct Twitter / X share via web or app.
     */
    private fun shareViaTwitter(context: Context, shareText: String) {
        try {
            val twitterUrl = "https://twitter.com/intent/tweet?text=${Uri.encode(shareText)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(twitterUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            launchNativeChooser(context, shareText, "Share App")
        }
    }

    /**
     * Directly opens the app's Google Play Store listing page.
     * Tries the `market://` URI first for a native store experience, falling back to HTTPS Web URL.
     */
    fun openPlayStore(context: Context) {
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_MARKET_URI)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            // Google Play Store app not available on device or emulator, open in browser
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_WEB_URL)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Could not open Play Store: ${ex.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Copies the official link or custom text to clipboard with Toast confirmation.
     */
    fun copyToClipboard(context: Context, text: String, toastMessage: String = "Link copied to clipboard!") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("APPSC Prep Share Link", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to copy link", Toast.LENGTH_SHORT).show()
        }
    }
}
