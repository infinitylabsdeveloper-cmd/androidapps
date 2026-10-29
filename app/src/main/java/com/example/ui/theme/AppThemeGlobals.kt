package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.example.model.DynamicAppConfig

val LocalAppConfig = staticCompositionLocalOf { DynamicAppConfig() }
val LocalWindowSizeClass = staticCompositionLocalOf { WindowWidthSizeClass.Compact }

val AppTextDark = Color(0xFF1E293B)
val AppTextSecondary = Color(0xFF475569)
val AppTextMuted = Color(0xFF64748B)

fun resolveFontFamily(fontName: String?): FontFamily {
    return FontFamily.Default
}

@Composable
fun DynamicAppTheme(
    appConfig: DynamicAppConfig = DynamicAppConfig(),
    content: @Composable () -> Unit
) {
    SyllabusProTheme {
        CompositionLocalProvider(
            LocalAppConfig provides appConfig
        ) {
            content()
        }
    }
}
