package com.example.util

import android.content.Context
import com.example.model.AdvancedFontSettings
import com.example.model.PracticeAppearanceSettings
import com.example.model.PracticeSectionAppearance

object FontSettingsManager {
    private const val PREFS_NAME = "advanced_font_settings_prefs"
    
    fun getSettings(context: Context): AdvancedFontSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val practiceAppearance = PracticeAppearanceManager.getSettings(context)

        return AdvancedFontSettings(
            fontFamily = prefs.getString("fontFamily", "Roboto") ?: "Roboto",
            headingFontFamily = prefs.getString("headingFontFamily", "Roboto") ?: "Roboto",
            bodyFontFamily = prefs.getString("bodyFontFamily", "Roboto") ?: "Roboto",
            titleFontSize = prefs.getFloat("titleFontSize", 22f),
            headingFontSize = prefs.getFloat("headingFontSize", 18f),
            bodyFontSize = prefs.getFloat("bodyFontSize", 15f),
            questionFontSize = prefs.getFloat("questionFontSize", 16f),
            optionFontSize = prefs.getFloat("optionFontSize", 15f),
            explanationFontSize = prefs.getFloat("explanationFontSize", 14f),
            currentAffairsFontSize = prefs.getFloat("currentAffairsFontSize", 15f),
            buttonFontSize = prefs.getFloat("buttonFontSize", 14f),
            labelFontSize = prefs.getFloat("labelFontSize", 12f),
            fontWeight = prefs.getString("fontWeight", "Regular") ?: "Regular",
            headingFontWeight = prefs.getString("headingFontWeight", "Bold") ?: "Bold",
            bodyFontWeight = prefs.getString("bodyFontWeight", "Regular") ?: "Regular",
            typographyScale = prefs.getFloat("typographyScale", 1.0f),
            lineSpacing = prefs.getString("lineSpacing", "Normal") ?: "Normal",
            textWidthMode = prefs.getString("textWidthMode", "Normal") ?: "Normal",
            letterSpacingScale = prefs.getFloat("letterSpacingScale", 0.0f),
            cornerRadiusStyle = prefs.getString("cornerRadiusStyle", "Balanced") ?: "Balanced",
            uiAestheticTheme = prefs.getString("uiAestheticTheme", "Royal Indigo") ?: "Royal Indigo",
            backgroundWallpaperTheme = prefs.getString("backgroundWallpaperTheme", "Midnight Royal Prestige") ?: "Midnight Royal Prestige",
            practiceAppearance = practiceAppearance
        )
    }

    fun saveSettings(context: Context, settings: AdvancedFontSettings) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("fontFamily", settings.fontFamily)
            putString("headingFontFamily", settings.headingFontFamily)
            putString("bodyFontFamily", settings.bodyFontFamily)
            putFloat("titleFontSize", settings.titleFontSize)
            putFloat("headingFontSize", settings.headingFontSize)
            putFloat("bodyFontSize", settings.bodyFontSize)
            putFloat("questionFontSize", settings.questionFontSize)
            putFloat("optionFontSize", settings.optionFontSize)
            putFloat("explanationFontSize", settings.explanationFontSize)
            putFloat("currentAffairsFontSize", settings.currentAffairsFontSize)
            putFloat("buttonFontSize", settings.buttonFontSize)
            putFloat("labelFontSize", settings.labelFontSize)
            putString("fontWeight", settings.fontWeight)
            putString("headingFontWeight", settings.headingFontWeight)
            putString("bodyFontWeight", settings.bodyFontWeight)
            putFloat("typographyScale", settings.typographyScale)
            putString("lineSpacing", settings.lineSpacing)
            putString("textWidthMode", settings.textWidthMode)
            putFloat("letterSpacingScale", settings.letterSpacingScale)
            putString("cornerRadiusStyle", settings.cornerRadiusStyle)
            putString("uiAestheticTheme", settings.uiAestheticTheme)
            putString("backgroundWallpaperTheme", settings.backgroundWallpaperTheme)
        }.apply()
        // Note: PracticeAppearance is managed strictly via PracticeAppearanceManager
    }

    fun resetGlobalSettings(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            remove("fontFamily")
            remove("headingFontFamily")
            remove("bodyFontFamily")
            remove("titleFontSize")
            remove("headingFontSize")
            remove("bodyFontSize")
            remove("questionFontSize")
            remove("optionFontSize")
            remove("explanationFontSize")
            remove("currentAffairsFontSize")
            remove("buttonFontSize")
            remove("labelFontSize")
            remove("fontWeight")
            remove("headingFontWeight")
            remove("bodyFontWeight")
            remove("typographyScale")
            remove("lineSpacing")
            remove("textWidthMode")
            remove("letterSpacingScale")
            remove("cornerRadiusStyle")
            remove("uiAestheticTheme")
        }.apply()
    }
}
