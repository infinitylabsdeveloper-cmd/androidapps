package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp

@Composable
fun LuxuryFontStudioTheme(content: @Composable () -> Unit) {
    val config by LuxuryFontStudioManager.fontConfig.collectAsState()

    val fontWeight = when (config.fontWeight) {
        "Light" -> FontWeight.Light
        "Medium" -> FontWeight.Medium
        "Semi Bold" -> FontWeight.SemiBold
        "Bold" -> FontWeight.Bold
        else -> FontWeight.Normal
    }

    val fontStyle = if (config.fontStyle == "Italic") FontStyle.Italic else FontStyle.Normal
    val safeLineMultiplier = config.lineSpacing.coerceAtLeast(1.45f)
    val platformTextStyle = PlatformTextStyle(includeFontPadding = true)

    val customTypography = Typography(
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(
            fontSize = config.fontSizeSp.sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            letterSpacing = 0.sp,
            lineHeight = (config.fontSizeSp * safeLineMultiplier).sp,
            platformStyle = platformTextStyle
        ),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(
            fontSize = (config.fontSizeSp * 0.9f).sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            letterSpacing = 0.sp,
            lineHeight = (config.fontSizeSp * 0.9f * safeLineMultiplier).sp,
            platformStyle = platformTextStyle
        ),
        titleLarge = MaterialTheme.typography.titleLarge.copy(
            fontSize = (config.fontSizeSp * 1.3f).sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            letterSpacing = 0.sp,
            lineHeight = (config.fontSizeSp * 1.3f * safeLineMultiplier).sp,
            platformStyle = platformTextStyle
        ),
        titleMedium = MaterialTheme.typography.titleMedium.copy(
            fontSize = (config.fontSizeSp * 1.1f).sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            letterSpacing = 0.sp,
            lineHeight = (config.fontSizeSp * 1.1f * safeLineMultiplier).sp,
            platformStyle = platformTextStyle
        )
    )

    MaterialTheme(
        typography = customTypography,
        content = content
    )
}
