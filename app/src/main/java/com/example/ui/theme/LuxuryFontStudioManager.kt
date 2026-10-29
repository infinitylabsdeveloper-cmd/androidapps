package com.example.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val Context.fontStudioDataStore: DataStore<Preferences> by preferencesDataStore(name = "luxury_font_studio_settings")

data class LuxuryFontConfig(
    val fontFamily: String = "Poppins",
    val fontSizeSp: Float = 18f,
    val fontWeight: String = "Regular",
    val fontStyle: String = "Normal",
    val textColor: String = "#292524",
    val backgroundColor: String = "#FFF1E6",
    val letterSpacing: Float = 0.5f,
    val lineSpacing: Float = 1.2f,
    val applyScope: String = "Whole App"
)

object LuxuryFontStudioManager {
    private val _fontConfig = MutableStateFlow(LuxuryFontConfig())
    val fontConfig: StateFlow<LuxuryFontConfig> = _fontConfig

    private val KEY_FONT_FAMILY = stringPreferencesKey("font_family")
    private val KEY_FONT_SIZE = floatPreferencesKey("font_size")
    private val KEY_FONT_WEIGHT = stringPreferencesKey("font_weight")
    private val KEY_FONT_STYLE = stringPreferencesKey("font_style")
    private val KEY_TEXT_COLOR = stringPreferencesKey("text_color")
    private val KEY_BG_COLOR = stringPreferencesKey("background_color")
    private val KEY_LETTER_SPACING = floatPreferencesKey("letter_spacing")
    private val KEY_LINE_SPACING = floatPreferencesKey("line_spacing")
    private val KEY_APPLY_SCOPE = stringPreferencesKey("apply_scope")

    fun init(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            context.fontStudioDataStore.data.map { prefs ->
                LuxuryFontConfig(
                    fontFamily = prefs[KEY_FONT_FAMILY] ?: "Poppins",
                    fontSizeSp = prefs[KEY_FONT_SIZE] ?: 18f,
                    fontWeight = prefs[KEY_FONT_WEIGHT] ?: "Regular",
                    fontStyle = prefs[KEY_FONT_STYLE] ?: "Normal",
                    textColor = prefs[KEY_TEXT_COLOR] ?: "#292524",
                    backgroundColor = prefs[KEY_BG_COLOR] ?: "#FFF1E6",
                    letterSpacing = prefs[KEY_LETTER_SPACING] ?: 0.5f,
                    lineSpacing = prefs[KEY_LINE_SPACING] ?: 1.2f,
                    applyScope = prefs[KEY_APPLY_SCOPE] ?: "Whole App"
                )
            }.collect {
                _fontConfig.value = it
            }
        }
    }

    fun updateConfig(context: Context, newConfig: LuxuryFontConfig) {
        CoroutineScope(Dispatchers.IO).launch {
            context.fontStudioDataStore.edit { prefs ->
                prefs[KEY_FONT_FAMILY] = newConfig.fontFamily
                prefs[KEY_FONT_SIZE] = newConfig.fontSizeSp
                prefs[KEY_FONT_WEIGHT] = newConfig.fontWeight
                prefs[KEY_FONT_STYLE] = newConfig.fontStyle
                prefs[KEY_TEXT_COLOR] = newConfig.textColor
                prefs[KEY_BG_COLOR] = newConfig.backgroundColor
                prefs[KEY_LETTER_SPACING] = newConfig.letterSpacing
                prefs[KEY_LINE_SPACING] = newConfig.lineSpacing
                prefs[KEY_APPLY_SCOPE] = newConfig.applyScope
            }
            _fontConfig.value = newConfig
        }
    }

    fun resetConfig(context: Context) {
        val defaultConfig = LuxuryFontConfig()
        updateConfig(context, defaultConfig)
    }
}
