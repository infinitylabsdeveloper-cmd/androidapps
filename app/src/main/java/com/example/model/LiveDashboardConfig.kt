package com.example.model

import androidx.compose.ui.graphics.Color

data class LiveDashboardMetricCardConfig(
    val title: String,
    val badgeText: String = "",
    val bgGradientStart: String,
    val bgGradientEnd: String,
    val borderColor: String,
    val accentGlowColor: String,
    val textColor: String = "#FFFFFF",
    val badgeBgColor: String = "#FFFFFF",
    val badgeTextColor: String,
    val overrideValue: Int? = null // Optional manual override for count
)

data class LiveDashboardConfig(
    val headerTitlePrefix: String = "LIVE CONTENT",
    val headerTitleSuffix: String = "DASHBOARD",
    val headerSubtitle: String = "Real-time question bank & syllabus metrics",
    val liveIndicatorColor: String = "#00C853", // Vibrant emerald beacon
    val liveBankButtonText: String = "Live Bank",
    val liveBankButtonColor: String = "#00A844",
    val containerBgStart: String = "#FFF3EE",
    val containerBgEnd: String = "#FCE4EC",
    val containerBorderColor: String = "#FED7AA",

    // 5 Individual Metric Cards with rich orange gradient (#FF9800 to #FF6D00)
    val subjectsCard: LiveDashboardMetricCardConfig = LiveDashboardMetricCardConfig(
        title = "Subjects",
        badgeText = "",
        bgGradientStart = "#FF9800",
        bgGradientEnd = "#FF6D00",
        borderColor = "#FF9800",
        accentGlowColor = "#FF9800",
        textColor = "#FFFFFF",
        badgeBgColor = "#FFFFFF",
        badgeTextColor = "#FF6D00"
    ),
    val unitsCard: LiveDashboardMetricCardConfig = LiveDashboardMetricCardConfig(
        title = "Units",
        badgeText = "",
        bgGradientStart = "#FF9800",
        bgGradientEnd = "#FF6D00",
        borderColor = "#FF9800",
        accentGlowColor = "#FF9800",
        textColor = "#FFFFFF",
        badgeBgColor = "#FFFFFF",
        badgeTextColor = "#FF6D00"
    ),
    val topicsCard: LiveDashboardMetricCardConfig = LiveDashboardMetricCardConfig(
        title = "Topics",
        badgeText = "",
        bgGradientStart = "#FF9800",
        bgGradientEnd = "#FF6D00",
        borderColor = "#FF9800",
        accentGlowColor = "#FF9800",
        textColor = "#FFFFFF",
        badgeBgColor = "#FFFFFF",
        badgeTextColor = "#FF6D00"
    ),
    val practiceSetsCard: LiveDashboardMetricCardConfig = LiveDashboardMetricCardConfig(
        title = "Practice Sets",
        badgeText = "",
        bgGradientStart = "#FF9800",
        bgGradientEnd = "#FF6D00",
        borderColor = "#FF9800",
        accentGlowColor = "#FF9800",
        textColor = "#FFFFFF",
        badgeBgColor = "#FFFFFF",
        badgeTextColor = "#FF6D00"
    ),
    val totalMcqsCard: LiveDashboardMetricCardConfig = LiveDashboardMetricCardConfig(
        title = "Total MCQs",
        badgeText = "",
        bgGradientStart = "#FF9800",
        bgGradientEnd = "#FF6D00",
        borderColor = "#FF9800",
        accentGlowColor = "#FF9800",
        textColor = "#FFFFFF",
        badgeBgColor = "#FFFFFF",
        badgeTextColor = "#FF6D00"
    )
)

fun parseHexColor(hexString: String, defaultColor: Color): Color {
    return try {
        val clean = hexString.trim().removePrefix("#")
        val longVal = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000 or longVal)
        } else {
            Color(longVal)
        }
    } catch (e: Exception) {
        defaultColor
    }
}
