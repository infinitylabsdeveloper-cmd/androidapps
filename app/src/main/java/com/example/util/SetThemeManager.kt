package com.example.util

import androidx.compose.ui.graphics.Color
import com.example.model.MonthlyMcqSetModel
import com.example.model.SetAppearanceConfig

object SetThemeManager {

    // Premium Color Palettes for automatic generation
    private val palettes = listOf(
        // Blue/Indigo
        SetAppearanceConfig(
            backgroundColor = "0xFFEFF6FF",
            gradientColors = listOf("0xFF1E3A8A", "0xFF2563EB"),
            accentColor = "0xFF60A5FA",
            textColor = "0xFF1E3A8A",
            iconColor = "0xFFFFFFFF",
            iconBackgroundColor = "0xFF2563EB",
            borderColor = "0xFF93C5FD"
        ),
        // Emerald/Green
        SetAppearanceConfig(
            backgroundColor = "0xFFF0FDF4",
            gradientColors = listOf("0xFF065F46", "0xFF059669"),
            accentColor = "0xFF4ADE80",
            textColor = "0xFF065F46",
            iconColor = "0xFFFFFFFF",
            iconBackgroundColor = "0xFF059669",
            borderColor = "0xFF86EFAC"
        ),
        // Violet/Purple
        SetAppearanceConfig(
            backgroundColor = "0xFFF5F3FF",
            gradientColors = listOf("0xFF4C1D95", "0xFF7C3AED"),
            accentColor = "0xFFA78BFA",
            textColor = "0xFF4C1D95",
            iconColor = "0xFFFFFFFF",
            iconBackgroundColor = "0xFF7C3AED",
            borderColor = "0xFFC4B5FD"
        ),
        // Amber/Orange
        SetAppearanceConfig(
            backgroundColor = "0xFFFFFBEB",
            gradientColors = listOf("0xFF92400E", "0xFFD97706"),
            accentColor = "0xFFFBBF24",
            textColor = "0xFF92400E",
            iconColor = "0xFFFFFFFF",
            iconBackgroundColor = "0xFFD97706",
            borderColor = "0xFCD34D"
        )
    )

    fun getSetAppearance(set: MonthlyMcqSetModel): SetAppearanceConfig {
        // Return admin configuration if available
        if (set.appearanceConfig != null) {
            return set.appearanceConfig
        }

        // Otherwise generate a premium palette based on the set ID
        val index = (set.id.hashCode() and 0x7FFFFFFF) % palettes.size
        return palettes[index]
    }
}
