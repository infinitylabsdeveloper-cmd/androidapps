package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.model.PracticeAppearanceSettings
import com.example.model.PracticeSectionAppearance
import com.example.model.PracticeSectionType
import com.example.model.BurstEffectType

object PracticeAppearanceManager {
    private const val PREF_NAME = "practice_appearance_settings"
    private const val MOCK_TEST_PREF_NAME = "mock_test_appearance_settings"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun getMockTestPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(MOCK_TEST_PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getMockTestDefaultSettings(): PracticeAppearanceSettings {
        return PracticeAppearanceSettings(
            presetName = "MockTestDefault",
            screenBackgroundColor = "#F8FAFC",
            questionCard = PracticeSectionAppearance(
                backgroundColor = "#FFFDF8",
                textColor = "#0F172A",
                fontFamily = "Poppins",
                fontSize = 16,
                borderColor = "#E2E8F0",
                borderWidth = 1,
                cornerRadius = 20
            ),
            options = PracticeSectionAppearance(
                backgroundColor = "#F0F9FF",
                textColor = "#1E293B",
                fontFamily = "Poppins",
                fontSize = 15,
                borderColor = "#BAE6FD",
                borderWidth = 1,
                cornerRadius = 16
            ),
            explanation = PracticeSectionAppearance(
                backgroundColor = "#F5F3FF",
                textColor = "#4C1D95",
                fontFamily = "Poppins",
                fontSize = 14,
                borderColor = "#EDE9FE",
                borderWidth = 1,
                cornerRadius = 16
            )
        )
    }

    fun hasCustomMockTestSettings(context: Context): Boolean {
        val prefs = getMockTestPrefs(context)
        return prefs.contains("has_custom_settings") && prefs.getBoolean("has_custom_settings", false)
    }

    fun getMockTestSettings(context: Context): PracticeAppearanceSettings {
        val prefs = getMockTestPrefs(context)
        val defaultSettings = getMockTestDefaultSettings()
        if (!hasCustomMockTestSettings(context)) {
            return defaultSettings
        }
        return PracticeAppearanceSettings(
            presetName = prefs.getString("preset_name", defaultSettings.presetName) ?: defaultSettings.presetName,
            screenBackgroundColor = prefs.getString("screen_bg_hex", defaultSettings.screenBackgroundColor) ?: defaultSettings.screenBackgroundColor,
            questionCard = PracticeSectionAppearance(
                backgroundColor = prefs.getString("q_bg_hex", defaultSettings.questionCard.backgroundColor) ?: defaultSettings.questionCard.backgroundColor,
                textColor = prefs.getString("q_text_hex", defaultSettings.questionCard.textColor) ?: defaultSettings.questionCard.textColor,
                fontFamily = prefs.getString("q_font", defaultSettings.questionCard.fontFamily) ?: defaultSettings.questionCard.fontFamily,
                fontSize = prefs.getInt("q_size", defaultSettings.questionCard.fontSize),
                borderColor = prefs.getString("q_border_hex", defaultSettings.questionCard.borderColor) ?: defaultSettings.questionCard.borderColor,
                borderWidth = prefs.getInt("q_border_width", defaultSettings.questionCard.borderWidth),
                cornerRadius = prefs.getInt("q_corner_radius", defaultSettings.questionCard.cornerRadius)
            ),
            options = PracticeSectionAppearance(
                backgroundColor = prefs.getString("o_bg_hex", defaultSettings.options.backgroundColor) ?: defaultSettings.options.backgroundColor,
                textColor = prefs.getString("o_text_hex", defaultSettings.options.textColor) ?: defaultSettings.options.textColor,
                fontFamily = prefs.getString("o_font", defaultSettings.options.fontFamily) ?: defaultSettings.options.fontFamily,
                fontSize = prefs.getInt("o_size", defaultSettings.options.fontSize),
                borderColor = prefs.getString("o_border_hex", defaultSettings.options.borderColor) ?: defaultSettings.options.borderColor,
                borderWidth = prefs.getInt("o_border_width", defaultSettings.options.borderWidth),
                cornerRadius = prefs.getInt("o_corner_radius", defaultSettings.options.cornerRadius)
            ),
            explanation = PracticeSectionAppearance(
                backgroundColor = prefs.getString("e_bg_hex", defaultSettings.explanation.backgroundColor) ?: defaultSettings.explanation.backgroundColor,
                textColor = prefs.getString("e_text_hex", defaultSettings.explanation.textColor) ?: defaultSettings.explanation.textColor,
                fontFamily = prefs.getString("e_font", defaultSettings.explanation.fontFamily) ?: defaultSettings.explanation.fontFamily,
                fontSize = prefs.getInt("e_size", defaultSettings.explanation.fontSize),
                borderColor = prefs.getString("e_border_hex", defaultSettings.explanation.borderColor) ?: defaultSettings.explanation.borderColor,
                borderWidth = prefs.getInt("e_border_width", defaultSettings.explanation.borderWidth),
                cornerRadius = prefs.getInt("e_corner_radius", defaultSettings.explanation.cornerRadius)
            ),
            lastUpdated = prefs.getLong("updated_at", System.currentTimeMillis())
        )
    }

    fun saveMockTestSettings(context: Context, settings: PracticeAppearanceSettings) {
        val prefs = getMockTestPrefs(context)
        prefs.edit().apply {
            putBoolean("has_custom_settings", true)
            putString("preset_name", settings.presetName)
            putString("screen_bg_hex", settings.screenBackgroundColor)
            putString("q_bg_hex", settings.questionCard.backgroundColor)
            putString("q_text_hex", settings.questionCard.textColor)
            putString("q_font", settings.questionCard.fontFamily)
            putInt("q_size", settings.questionCard.fontSize)
            putString("q_border_hex", settings.questionCard.borderColor)
            putInt("q_border_width", settings.questionCard.borderWidth)
            putInt("q_corner_radius", settings.questionCard.cornerRadius)
            
            putString("o_bg_hex", settings.options.backgroundColor)
            putString("o_text_hex", settings.options.textColor)
            putString("o_font", settings.options.fontFamily)
            putInt("o_size", settings.options.fontSize)
            putString("o_border_hex", settings.options.borderColor)
            putInt("o_border_width", settings.options.borderWidth)
            putInt("o_corner_radius", settings.options.cornerRadius)
            
            putString("e_bg_hex", settings.explanation.backgroundColor)
            putString("e_text_hex", settings.explanation.textColor)
            putString("e_font", settings.explanation.fontFamily)
            putInt("e_size", settings.explanation.fontSize)
            putString("e_border_hex", settings.explanation.borderColor)
            putInt("e_border_width", settings.explanation.borderWidth)
            putInt("e_corner_radius", settings.explanation.cornerRadius)
            
            putLong("updated_at", System.currentTimeMillis())
            apply()
        }
    }

    fun resetMockTestDefault(context: Context) {
        getMockTestPrefs(context).edit().clear().apply()
    }

    fun getSettings(context: Context): PracticeAppearanceSettings {
        val prefs = getPrefs(context)
        val defaultSettings = PracticeAppearanceSettings()
        return PracticeAppearanceSettings(
            presetName = prefs.getString("preset_name", defaultSettings.presetName) ?: defaultSettings.presetName,
            screenBackgroundColor = prefs.getString("screen_bg_hex", defaultSettings.screenBackgroundColor) ?: defaultSettings.screenBackgroundColor,
            questionCard = PracticeSectionAppearance(
                backgroundColor = prefs.getString("q_bg_hex", defaultSettings.questionCard.backgroundColor) ?: defaultSettings.questionCard.backgroundColor,
                textColor = prefs.getString("q_text_hex", defaultSettings.questionCard.textColor) ?: defaultSettings.questionCard.textColor,
                fontFamily = prefs.getString("q_font", defaultSettings.questionCard.fontFamily) ?: defaultSettings.questionCard.fontFamily,
                fontSize = prefs.getInt("q_size", defaultSettings.questionCard.fontSize),
                borderColor = prefs.getString("q_border_hex", defaultSettings.questionCard.borderColor) ?: defaultSettings.questionCard.borderColor,
                borderWidth = prefs.getInt("q_border_width", defaultSettings.questionCard.borderWidth),
                cornerRadius = prefs.getInt("q_corner_radius", defaultSettings.questionCard.cornerRadius)
            ),
            options = PracticeSectionAppearance(
                backgroundColor = prefs.getString("o_bg_hex", defaultSettings.options.backgroundColor) ?: defaultSettings.options.backgroundColor,
                textColor = prefs.getString("o_text_hex", defaultSettings.options.textColor) ?: defaultSettings.options.textColor,
                fontFamily = prefs.getString("o_font", defaultSettings.options.fontFamily) ?: defaultSettings.options.fontFamily,
                fontSize = prefs.getInt("o_size", defaultSettings.options.fontSize),
                borderColor = prefs.getString("o_border_hex", defaultSettings.options.borderColor) ?: defaultSettings.options.borderColor,
                borderWidth = prefs.getInt("o_border_width", defaultSettings.options.borderWidth),
                cornerRadius = prefs.getInt("o_corner_radius", defaultSettings.options.cornerRadius)
            ),
            explanation = PracticeSectionAppearance(
                backgroundColor = prefs.getString("e_bg_hex", defaultSettings.explanation.backgroundColor) ?: defaultSettings.explanation.backgroundColor,
                textColor = prefs.getString("e_text_hex", defaultSettings.explanation.textColor) ?: defaultSettings.explanation.textColor,
                fontFamily = prefs.getString("e_font", defaultSettings.explanation.fontFamily) ?: defaultSettings.explanation.fontFamily,
                fontSize = prefs.getInt("e_size", defaultSettings.explanation.fontSize),
                borderColor = prefs.getString("e_border_hex", defaultSettings.explanation.borderColor) ?: defaultSettings.explanation.borderColor,
                borderWidth = prefs.getInt("e_border_width", defaultSettings.explanation.borderWidth),
                cornerRadius = prefs.getInt("e_corner_radius", defaultSettings.explanation.cornerRadius)
            ),
            answerAnimationEffect = prefs.getString("answer_animation_effect", defaultSettings.answerAnimationEffect) ?: defaultSettings.answerAnimationEffect,
            setsEffect = prefs.getString(PracticeSectionType.SETS.prefKey, defaultSettings.setsEffect) ?: defaultSettings.setsEffect,
            subtopicsEffect = prefs.getString(PracticeSectionType.SUBTOPICS.prefKey, defaultSettings.subtopicsEffect) ?: defaultSettings.subtopicsEffect,
            currentAffairsEffect = prefs.getString(PracticeSectionType.CURRENT_AFFAIRS.prefKey, defaultSettings.currentAffairsEffect) ?: defaultSettings.currentAffairsEffect,
            offlineEffect = prefs.getString(PracticeSectionType.OFFLINE.prefKey, defaultSettings.offlineEffect) ?: defaultSettings.offlineEffect,
            lastUpdated = prefs.getLong("updated_at", System.currentTimeMillis())
        )
    }

    fun saveSettings(context: Context, settings: PracticeAppearanceSettings) {
        val prefs = getPrefs(context)
        prefs.edit().apply {
            putString("preset_name", settings.presetName)
            putString("screen_bg_hex", settings.screenBackgroundColor)
            putString("q_bg_hex", settings.questionCard.backgroundColor)
            putString("q_text_hex", settings.questionCard.textColor)
            putString("q_font", settings.questionCard.fontFamily)
            putInt("q_size", settings.questionCard.fontSize)
            putString("q_border_hex", settings.questionCard.borderColor)
            putInt("q_border_width", settings.questionCard.borderWidth)
            putInt("q_corner_radius", settings.questionCard.cornerRadius)
            
            putString("o_bg_hex", settings.options.backgroundColor)
            putString("o_text_hex", settings.options.textColor)
            putString("o_font", settings.options.fontFamily)
            putInt("o_size", settings.options.fontSize)
            putString("o_border_hex", settings.options.borderColor)
            putInt("o_border_width", settings.options.borderWidth)
            putInt("o_corner_radius", settings.options.cornerRadius)
            
            putString("e_bg_hex", settings.explanation.backgroundColor)
            putString("e_text_hex", settings.explanation.textColor)
            putString("e_font", settings.explanation.fontFamily)
            putInt("e_size", settings.explanation.fontSize)
            putString("e_border_hex", settings.explanation.borderColor)
            putInt("e_border_width", settings.explanation.borderWidth)
            putInt("e_corner_radius", settings.explanation.cornerRadius)
            
            putString("answer_animation_effect", settings.answerAnimationEffect)
            putString(PracticeSectionType.SETS.prefKey, settings.setsEffect)
            putString(PracticeSectionType.SUBTOPICS.prefKey, settings.subtopicsEffect)
            putString(PracticeSectionType.CURRENT_AFFAIRS.prefKey, settings.currentAffairsEffect)
            putString(PracticeSectionType.OFFLINE.prefKey, settings.offlineEffect)
            putLong("updated_at", System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Retrieves the persisted correct-answer burst effect for a specific practice section.
     * Defaults to "Confetti Pop" if not explicitly set.
     */
    fun getEffectForSection(context: Context, section: PracticeSectionType): String {
        val prefs = getPrefs(context)
        return prefs.getString(section.prefKey, BurstEffectType.DEFAULT.displayName)
            ?.takeIf { it.isNotBlank() } ?: BurstEffectType.DEFAULT.displayName
    }

    /**
     * Persistently saves the selected burst effect for a specific practice section.
     */
    fun saveEffectForSection(context: Context, section: PracticeSectionType, effectName: String) {
        val prefs = getPrefs(context)
        prefs.edit().putString(section.prefKey, effectName).apply()
    }

    /**
     * Returns a snapshot map of currently saved effects across all 4 practice sections.
     */
    fun getAllSectionEffects(context: Context): Map<PracticeSectionType, String> {
        return PracticeSectionType.entries.associateWith { getEffectForSection(context, it) }
    }

    /**
     * Resets all 4 practice sections to the default "Confetti Pop" effect.
     */
    fun resetEffectsToDefault(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().apply {
            PracticeSectionType.entries.forEach { section ->
                putString(section.prefKey, BurstEffectType.DEFAULT.displayName)
            }
            putString("answer_animation_effect", BurstEffectType.DEFAULT.displayName)
            apply()
        }
    }

    fun resetToDefault(context: Context) {
        getPrefs(context).edit().clear().apply()
        resetEffectsToDefault(context)
    }
}
