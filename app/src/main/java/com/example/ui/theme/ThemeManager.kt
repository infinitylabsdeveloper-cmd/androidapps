package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

data class AppThemeConfig(
    val id: String = "candy",
    val name: String = "Candy",
    val primaryBackground: Color,
    val secondaryBackground: Color,
    val surfaceColor: Color,
    val onBackground: Color,
    val onSurface: Color
) {
    companion object {
        val Candy = AppThemeConfig(
            id = "candy",
            name = "Candy Sweet",
            primaryBackground = Color(0xFFFFF0F5), // Lavender Blush / Candy Sweet Pink
            secondaryBackground = Color(0xFFFFE4E6), // Soft Candy Rose / Peach
            surfaceColor = Color(0xFFFFFFFF), // Crisp Clean White for cards & content so images are untouched
            onBackground = Color(0xFF4A154B), // Deep Berry Plum
            onSurface = Color(0xFF1E293B)
        )

        val Slate = AppThemeConfig(
            id = "slate",
            name = "Slate Blue",
            primaryBackground = Color(0xFFF1F5F9), // Slate 100
            secondaryBackground = Color(0xFFE2E8F0), // Slate 200
            surfaceColor = Color(0xFFFFFFFF),
            onBackground = Color(0xFF0F172A), // Slate 900
            onSurface = Color(0xFF0F172A)
        )
        
        val Teal = AppThemeConfig(
            id = "teal",
            name = "Emerald Teal",
            primaryBackground = Color(0xFFF0FDF4), // Green 50
            secondaryBackground = Color(0xFFDCFCE7), // Green 100
            surfaceColor = Color(0xFFFFFFFF),
            onBackground = Color(0xFF064E3B), // Emerald 900
            onSurface = Color(0xFF064E3B)
        )
        
        val SoftCharcoal = AppThemeConfig(
            id = "charcoal",
            name = "Dark Charcoal",
            primaryBackground = Color(0xFF1E293B), // Slate 800
            secondaryBackground = Color(0xFF334155), // Slate 700
            surfaceColor = Color(0xFF0F172A), // Slate 900
            onBackground = Color(0xFFF8FAFC), // Slate 50
            onSurface = Color(0xFFF8FAFC)
        )
    }
}

object ThemeManager {
    var currentTheme = androidx.compose.runtime.mutableStateOf(AppThemeConfig.Candy)

    /**
     * Applies the premium background hues to the given ColorScheme.
     */
    fun applyTheme(baseScheme: ColorScheme, config: AppThemeConfig = currentTheme.value): ColorScheme {
        return baseScheme.copy(
            background = config.primaryBackground,
            surface = config.surfaceColor,
            surfaceVariant = config.secondaryBackground,
            onBackground = config.onBackground,
            onSurface = config.onSurface
        )
    }
}
