package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.model.ContactUsConfig

object ContactSupportManager {
    private const val PREFS_NAME = "contact_us_support_prefs"
    
    private val _configState = mutableStateOf(ContactUsConfig())
    val configState: State<ContactUsConfig> = _configState

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            _configState.value = loadFromPrefs(context)
            isInitialized = true
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getConfig(context: Context): ContactUsConfig {
        if (!isInitialized) {
            init(context)
        }
        return _configState.value
    }

    private fun loadFromPrefs(context: Context): ContactUsConfig {
        val prefs = getPrefs(context)
        val default = ContactUsConfig()

        val rawEmail = prefs.getString("support_email", default.supportEmail) ?: default.supportEmail
        val rawWaNum = prefs.getString("whatsapp_number", default.whatsappNumber) ?: default.whatsappNumber
        val rawWaLink = prefs.getString("whatsapp_link", default.whatsappLink) ?: default.whatsappLink
        val rawTgLink = prefs.getString("telegram_link", default.telegramLink) ?: default.telegramLink
        val rawPhone = prefs.getString("phone_number", default.phoneNumber) ?: default.phoneNumber

        return ContactUsConfig(
            supportEmail = if (rawEmail == "support@infinitylabs.in" || rawEmail.isBlank()) default.supportEmail else rawEmail,
            emailTitle = prefs.getString("email_title", default.emailTitle) ?: default.emailTitle,
            emailSubtext = prefs.getString("email_subtext", default.emailSubtext) ?: default.emailSubtext,
            isEmailSupportEnabled = prefs.getBoolean("is_email_enabled", default.isEmailSupportEnabled),

            whatsappNumber = if (rawWaNum == "+91 98765 43210" || rawWaNum.isBlank()) default.whatsappNumber else rawWaNum,
            whatsappTitle = prefs.getString("whatsapp_title", default.whatsappTitle) ?: default.whatsappTitle,
            whatsappSubtext = prefs.getString("whatsapp_subtext", default.whatsappSubtext) ?: default.whatsappSubtext,
            whatsappLink = if (rawWaLink.contains("9876543210") || rawWaLink.isBlank()) default.whatsappLink else rawWaLink,
            isWhatsAppEnabled = prefs.getBoolean("is_whatsapp_enabled", default.isWhatsAppEnabled),

            telegramLink = if (rawTgLink.contains("infinitylabs") || rawTgLink.isBlank()) default.telegramLink else rawTgLink,
            telegramTitle = prefs.getString("telegram_title", default.telegramTitle) ?: default.telegramTitle,
            telegramSubtext = prefs.getString("telegram_subtext", default.telegramSubtext) ?: default.telegramSubtext,
            isTelegramEnabled = prefs.getBoolean("is_telegram_enabled", default.isTelegramEnabled),

            phoneNumber = if (rawPhone == "+91 98765 43210" || rawPhone.isBlank()) default.phoneNumber else rawPhone,

            supportTimingsTitle = prefs.getString("support_timings_title", default.supportTimingsTitle) ?: default.supportTimingsTitle,
            supportTimingsLine1 = prefs.getString("support_timings_line1", default.supportTimingsLine1) ?: default.supportTimingsLine1,
            supportTimingsLine2 = prefs.getString("support_timings_line2", default.supportTimingsLine2) ?: default.supportTimingsLine2,

            quickSupportTitle = prefs.getString("quick_support_title", default.quickSupportTitle) ?: default.quickSupportTitle,
            quickSupportSubtext = prefs.getString("quick_support_subtext", default.quickSupportSubtext) ?: default.quickSupportSubtext,

            headerTitle = prefs.getString("header_title", default.headerTitle) ?: default.headerTitle,
            headerSubtitle = prefs.getString("header_subtitle", default.headerSubtitle) ?: default.headerSubtitle,
            bannerTitle = prefs.getString("banner_title", default.bannerTitle) ?: default.bannerTitle,
            bannerSubtitle = prefs.getString("banner_subtitle", default.bannerSubtitle) ?: default.bannerSubtitle,
            fastSupportBadge = prefs.getString("fast_support_badge", default.fastSupportBadge) ?: default.fastSupportBadge,

            faqTitle = prefs.getString("faq_title", default.faqTitle) ?: default.faqTitle,
            faqSubtitle = prefs.getString("faq_subtitle", default.faqSubtitle) ?: default.faqSubtitle,
            faqUrl = prefs.getString("faq_url", default.faqUrl) ?: default.faqUrl,

            helpCenterTitle = prefs.getString("help_center_title", default.helpCenterTitle) ?: default.helpCenterTitle,
            helpCenterSubtitle = prefs.getString("help_center_subtitle", default.helpCenterSubtitle) ?: default.helpCenterSubtitle,
            helpCenterUrl = prefs.getString("help_center_url", default.helpCenterUrl) ?: default.helpCenterUrl,

            callUsTitle = prefs.getString("call_us_title", default.callUsTitle) ?: default.callUsTitle,

            communityTitle = prefs.getString("community_title", default.communityTitle) ?: default.communityTitle,
            communitySubtitle = prefs.getString("community_subtitle", default.communitySubtitle) ?: default.communitySubtitle,
            communityUrl = prefs.getString("community_url", default.communityUrl) ?: default.communityUrl,

            motivationalQuote = prefs.getString("motivational_quote", default.motivationalQuote) ?: default.motivationalQuote,

            screenBgHex = prefs.getString("screen_bg_hex", default.screenBgHex) ?: default.screenBgHex,

            bannerBgStartHex = prefs.getString("banner_bg_start_hex", default.bannerBgStartHex) ?: default.bannerBgStartHex,
            bannerBgEndHex = prefs.getString("banner_bg_end_hex", default.bannerBgEndHex) ?: default.bannerBgEndHex,
            bannerBorderHex = prefs.getString("banner_border_hex", default.bannerBorderHex) ?: default.bannerBorderHex,

            emailBgStartHex = prefs.getString("email_bg_start_hex", default.emailBgStartHex) ?: default.emailBgStartHex,
            emailBgEndHex = prefs.getString("email_bg_end_hex", default.emailBgEndHex) ?: default.emailBgEndHex,
            emailBorderHex = prefs.getString("email_border_hex", default.emailBorderHex) ?: default.emailBorderHex,
            emailIconContainerHex = prefs.getString("email_icon_container_hex", default.emailIconContainerHex) ?: default.emailIconContainerHex,
            emailTextAccentHex = prefs.getString("email_text_accent_hex", default.emailTextAccentHex) ?: default.emailTextAccentHex,

            whatsappBgStartHex = prefs.getString("whatsapp_bg_start_hex", default.whatsappBgStartHex) ?: default.whatsappBgStartHex,
            whatsappBgEndHex = prefs.getString("whatsapp_bg_end_hex", default.whatsappBgEndHex) ?: default.whatsappBgEndHex,
            whatsappBorderHex = prefs.getString("whatsapp_border_hex", default.whatsappBorderHex) ?: default.whatsappBorderHex,
            whatsappIconContainerHex = prefs.getString("whatsapp_icon_container_hex", default.whatsappIconContainerHex) ?: default.whatsappIconContainerHex,
            whatsappTextAccentHex = prefs.getString("whatsapp_text_accent_hex", default.whatsappTextAccentHex) ?: default.whatsappTextAccentHex,

            telegramBgStartHex = prefs.getString("telegram_bg_start_hex", default.telegramBgStartHex) ?: default.telegramBgStartHex,
            telegramBgEndHex = prefs.getString("telegram_bg_end_hex", default.telegramBgEndHex) ?: default.telegramBgEndHex,
            telegramBorderHex = prefs.getString("telegram_border_hex", default.telegramBorderHex) ?: default.telegramBorderHex,
            telegramIconContainerHex = prefs.getString("telegram_icon_container_hex", default.telegramIconContainerHex) ?: default.telegramIconContainerHex,
            telegramTextAccentHex = prefs.getString("telegram_text_accent_hex", default.telegramTextAccentHex) ?: default.telegramTextAccentHex,

            timingsBgStartHex = prefs.getString("timings_bg_start_hex", default.timingsBgStartHex) ?: default.timingsBgStartHex,
            timingsBgEndHex = prefs.getString("timings_bg_end_hex", default.timingsBgEndHex) ?: default.timingsBgEndHex,
            timingsBorderHex = prefs.getString("timings_border_hex", default.timingsBorderHex) ?: default.timingsBorderHex,

            quickSupportBgStartHex = prefs.getString("quick_support_bg_start_hex", default.quickSupportBgStartHex) ?: default.quickSupportBgStartHex,
            quickSupportBgEndHex = prefs.getString("quick_support_bg_end_hex", default.quickSupportBgEndHex) ?: default.quickSupportBgEndHex,
            quickSupportBorderHex = prefs.getString("quick_support_border_hex", default.quickSupportBorderHex) ?: default.quickSupportBorderHex,

            primaryTextHex = prefs.getString("primary_text_hex", default.primaryTextHex) ?: default.primaryTextHex,
            secondaryTextHex = prefs.getString("secondary_text_hex", default.secondaryTextHex) ?: default.secondaryTextHex,
            cardCornerRadiusDp = prefs.getInt("card_corner_radius_dp", default.cardCornerRadiusDp)
        )
    }

    fun saveConfig(context: Context, config: ContactUsConfig) {
        _configState.value = config
        val prefs = getPrefs(context)
        prefs.edit().apply {
            putString("support_email", config.supportEmail)
            putString("email_title", config.emailTitle)
            putString("email_subtext", config.emailSubtext)
            putBoolean("is_email_enabled", config.isEmailSupportEnabled)

            putString("whatsapp_number", config.whatsappNumber)
            putString("whatsapp_title", config.whatsappTitle)
            putString("whatsapp_subtext", config.whatsappSubtext)
            putString("whatsapp_link", config.whatsappLink)
            putBoolean("is_whatsapp_enabled", config.isWhatsAppEnabled)

            putString("telegram_link", config.telegramLink)
            putString("telegram_title", config.telegramTitle)
            putString("telegram_subtext", config.telegramSubtext)
            putBoolean("is_telegram_enabled", config.isTelegramEnabled)

            putString("phone_number", config.phoneNumber)

            putString("support_timings_title", config.supportTimingsTitle)
            putString("support_timings_line1", config.supportTimingsLine1)
            putString("support_timings_line2", config.supportTimingsLine2)

            putString("quick_support_title", config.quickSupportTitle)
            putString("quick_support_subtext", config.quickSupportSubtext)

            putString("header_title", config.headerTitle)
            putString("header_subtitle", config.headerSubtitle)
            putString("banner_title", config.bannerTitle)
            putString("banner_subtitle", config.bannerSubtitle)
            putString("fast_support_badge", config.fastSupportBadge)

            putString("faq_title", config.faqTitle)
            putString("faq_subtitle", config.faqSubtitle)
            putString("faq_url", config.faqUrl)

            putString("help_center_title", config.helpCenterTitle)
            putString("help_center_subtitle", config.helpCenterSubtitle)
            putString("help_center_url", config.helpCenterUrl)

            putString("call_us_title", config.callUsTitle)

            putString("community_title", config.communityTitle)
            putString("community_subtitle", config.communitySubtitle)
            putString("community_url", config.communityUrl)

            putString("motivational_quote", config.motivationalQuote)

            putString("screen_bg_hex", config.screenBgHex)

            putString("banner_bg_start_hex", config.bannerBgStartHex)
            putString("banner_bg_end_hex", config.bannerBgEndHex)
            putString("banner_border_hex", config.bannerBorderHex)

            putString("email_bg_start_hex", config.emailBgStartHex)
            putString("email_bg_end_hex", config.emailBgEndHex)
            putString("email_border_hex", config.emailBorderHex)
            putString("email_icon_container_hex", config.emailIconContainerHex)
            putString("email_text_accent_hex", config.emailTextAccentHex)

            putString("whatsapp_bg_start_hex", config.whatsappBgStartHex)
            putString("whatsapp_bg_end_hex", config.whatsappBgEndHex)
            putString("whatsapp_border_hex", config.whatsappBorderHex)
            putString("whatsapp_icon_container_hex", config.whatsappIconContainerHex)
            putString("whatsapp_text_accent_hex", config.whatsappTextAccentHex)

            putString("telegram_bg_start_hex", config.telegramBgStartHex)
            putString("telegram_bg_end_hex", config.telegramBgEndHex)
            putString("telegram_border_hex", config.telegramBorderHex)
            putString("telegram_icon_container_hex", config.telegramIconContainerHex)
            putString("telegram_text_accent_hex", config.telegramTextAccentHex)

            putString("timings_bg_start_hex", config.timingsBgStartHex)
            putString("timings_bg_end_hex", config.timingsBgEndHex)
            putString("timings_border_hex", config.timingsBorderHex)

            putString("quick_support_bg_start_hex", config.quickSupportBgStartHex)
            putString("quick_support_bg_end_hex", config.quickSupportBgEndHex)
            putString("quick_support_border_hex", config.quickSupportBorderHex)

            putString("primary_text_hex", config.primaryTextHex)
            putString("secondary_text_hex", config.secondaryTextHex)
            putInt("card_corner_radius_dp", config.cardCornerRadiusDp)
            apply()
        }
    }

    fun resetToDefaults(context: Context) {
        val default = ContactUsConfig()
        saveConfig(context, default)
    }

    fun buildPrefilledMessage(
        issueCategory: String,
        customDetails: String = "",
        extraContext: String = ""
    ): String {
        val activeApp = com.example.data.SampleData.getActiveApp()
        val profile = com.example.data.SampleData.sampleUserProfile
        val stateLabel = activeApp.region.ifBlank { activeApp.category }.ifBlank { "Central / National" }
        return buildString {
            appendLine("Hello Support Team,")
            appendLine()
            appendLine("• App Name: ${activeApp.name}")
            appendLine("• Exam / State: ${activeApp.examName} ($stateLabel)")
            appendLine("• Issue Category: $issueCategory")
            if (profile.phone.isNotBlank()) {
                appendLine("• User Mobile: ${profile.phone}")
            }
            if (profile.email.isNotBlank()) {
                appendLine("• User Email: ${profile.email}")
            }
            if (extraContext.isNotBlank()) {
                appendLine("• Context: $extraContext")
            }
            if (customDetails.isNotBlank()) {
                appendLine()
                appendLine("Details:")
                appendLine(customDetails.trim())
            } else {
                appendLine()
                appendLine("Please assist me with this issue.")
            }
        }
    }

    fun openWhatsAppSupport(
        context: Context,
        issueCategory: String = "General Assistance",
        customDetails: String = "",
        extraContext: String = ""
    ) {
        val cfg = getConfig(context)
        val cleanNumber = cfg.whatsappNumber.replace(Regex("[^0-9]"), "").ifBlank { "919493512768" }
        val message = buildPrefilledMessage(issueCategory, customDetails, extraContext)
        val encodedText = android.net.Uri.encode(message)
        val baseUrl = if (cfg.whatsappLink.isNotBlank() && cfg.whatsappLink.startsWith("http")) {
            cfg.whatsappLink.substringBefore("?")
        } else {
            "https://wa.me/$cleanNumber"
        }
        val finalUrl = "$baseUrl?text=$encodedText"
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(finalUrl)).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "WhatsApp Support: ${cfg.whatsappNumber}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    fun openEmailSupport(
        context: Context,
        issueCategory: String = "General Assistance",
        customDetails: String = "",
        extraContext: String = ""
    ) {
        val cfg = getConfig(context)
        val activeApp = com.example.data.SampleData.getActiveApp()
        val email = cfg.supportEmail.trim().ifBlank { "sobhadhanaraju0@gmail.com" }
        val subject = "[${activeApp.name}] Support: $issueCategory"
        val body = buildPrefilledMessage(issueCategory, customDetails, extraContext)
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:$email")
                putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
                putExtra(android.content.Intent.EXTRA_TEXT, body)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Support Email: $email", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    fun openTelegramSupport(context: Context) {
        val cfg = getConfig(context)
        val rawLink = cfg.telegramLink.trim().ifBlank { "https://t.me/DHANARAJU0" }
        val targetUrl = if (rawLink.startsWith("http://") || rawLink.startsWith("https://")) rawLink else "https://$rawLink"
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(targetUrl)).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Telegram Support: $targetUrl", android.widget.Toast.LENGTH_LONG).show()
        }
    }
}
