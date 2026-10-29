package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * Premium Card Color System for Sets, Subtopics, and Learning Cards.
 * Provides rich, modern, distinct background colors, accents, borders, and typography styling
 * with high visual contrast and zero pure-white / pure-black backgrounds.
 */
data class PremiumSetColor(
    val mainAccent: Color,
    val cardBgGradientStart: Color,
    val cardBgGradientEnd: Color,
    val borderColor: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val pillBg: Color,
    val startButtonText: Color = Color.White
)

/**
 * Converts HSL values (Hue: 0..360, Saturation: 0..1, Lightness: 0..1) to Compose Color.
 */
fun hslToColor(hue: Float, saturation: Float, lightness: Float, alpha: Float = 1f): Color {
    val h = (hue % 360f + 360f) % 360f
    val c = (1f - abs(2f * lightness - 1f)) * saturation
    val x = c * (1f - abs((h / 60f) % 2f - 1f))
    val m = lightness - c / 2f

    val (r1, g1, b1) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f),
        alpha = alpha.coerceIn(0f, 1f)
    )
}

object PremiumCardColorSystem {

    // Reference image rich vibrant solid colors
    val vibrantReferenceColors = listOf(
        Color(0xFFEA580C), // Rich Orange
        Color(0xFF2563EB), // Royal Blue
        Color(0xFFE11D48), // Ruby Red
        Color(0xFF0D9488), // Teal Green
        Color(0xFF059669), // Emerald Green
        Color(0xFF7C3AED)  // Violet
    )

    // 24 Handcrafted, distinctive, high-vibrancy palettes tailored for Education/Exam apps
    private val curatedSetsLight = listOf(
        // 0: Sapphire Blue
        PremiumSetColor(
            mainAccent = Color(0xFF2563EB),
            cardBgGradientStart = Color(0xFFEBF3FF),
            cardBgGradientEnd = Color(0xFFDCEBFF),
            borderColor = Color(0xFF93C5FD),
            textPrimary = Color(0xFF1E3A8A),
            textSecondary = Color(0xFF2563EB),
            pillBg = Color(0xFFBFDBFE)
        ),
        // 1: Royal Purple
        PremiumSetColor(
            mainAccent = Color(0xFF7C3AED),
            cardBgGradientStart = Color(0xFFF5F3FF),
            cardBgGradientEnd = Color(0xFFECE7FE),
            borderColor = Color(0xFFC4B5FD),
            textPrimary = Color(0xFF4C1D95),
            textSecondary = Color(0xFF6D28D9),
            pillBg = Color(0xFFDDD6FE)
        ),
        // 2: Emerald Green
        PremiumSetColor(
            mainAccent = Color(0xFF059669),
            cardBgGradientStart = Color(0xFFECFDF5),
            cardBgGradientEnd = Color(0xFFD1FAE5),
            borderColor = Color(0xFF6EE7B7),
            textPrimary = Color(0xFF064E3B),
            textSecondary = Color(0xFF047857),
            pillBg = Color(0xFFA7F3D0)
        ),
        // 3: Vibrant Amber / Gold
        PremiumSetColor(
            mainAccent = Color(0xFFD97706),
            cardBgGradientStart = Color(0xFFFFFBEB),
            cardBgGradientEnd = Color(0xFFFEF3C7),
            borderColor = Color(0xFFFCD34D),
            textPrimary = Color(0xFF78350F),
            textSecondary = Color(0xFFB45309),
            pillBg = Color(0xFFFDE68A)
        ),
        // 4: Ruby Rose
        PremiumSetColor(
            mainAccent = Color(0xFFE11D48),
            cardBgGradientStart = Color(0xFFFFF1F2),
            cardBgGradientEnd = Color(0xFFFFE4E6),
            borderColor = Color(0xFFFDA4AF),
            textPrimary = Color(0xFF881337),
            textSecondary = Color(0xFFBE123C),
            pillBg = Color(0xFFFECDD3)
        ),
        // 5: Ocean Teal
        PremiumSetColor(
            mainAccent = Color(0xFF0D9488),
            cardBgGradientStart = Color(0xFFF0FDFA),
            cardBgGradientEnd = Color(0xFFCCFBF1),
            borderColor = Color(0xFF5EEAD4),
            textPrimary = Color(0xFF115E59),
            textSecondary = Color(0xFF0F766E),
            pillBg = Color(0xFF99F6E4)
        ),
        // 6: Electric Indigo
        PremiumSetColor(
            mainAccent = Color(0xFF4F46E5),
            cardBgGradientStart = Color(0xFFEEF2FF),
            cardBgGradientEnd = Color(0xFFE0E7FF),
            borderColor = Color(0xFFA5B4FC),
            textPrimary = Color(0xFF312E81),
            textSecondary = Color(0xFF4338CA),
            pillBg = Color(0xFFC7D2FE)
        ),
        // 7: Sunset Tangerine
        PremiumSetColor(
            mainAccent = Color(0xFFEA580C),
            cardBgGradientStart = Color(0xFFFFF7ED),
            cardBgGradientEnd = Color(0xFFFFEDD5),
            borderColor = Color(0xFFFDBA74),
            textPrimary = Color(0xFF7C2D12),
            textSecondary = Color(0xFFC2410C),
            pillBg = Color(0xFFFED7AA)
        ),
        // 8: Deep Cyan
        PremiumSetColor(
            mainAccent = Color(0xFF0891B2),
            cardBgGradientStart = Color(0xFFECFEFF),
            cardBgGradientEnd = Color(0xFFCFFAFE),
            borderColor = Color(0xFF67E8F9),
            textPrimary = Color(0xFF164E63),
            textSecondary = Color(0xFF0E7490),
            pillBg = Color(0xFFA5F3FC)
        ),
        // 9: Magenta Fuchsia
        PremiumSetColor(
            mainAccent = Color(0xFFC026D3),
            cardBgGradientStart = Color(0xFFFDF4FF),
            cardBgGradientEnd = Color(0xFFFAE8FF),
            borderColor = Color(0xFFE879F9),
            textPrimary = Color(0xFF701A75),
            textSecondary = Color(0xFFA21CAF),
            pillBg = Color(0xFFF5D0FE)
        ),
        // 10: Lime Apple
        PremiumSetColor(
            mainAccent = Color(0xFF65A30D),
            cardBgGradientStart = Color(0xFFF7FEE7),
            cardBgGradientEnd = Color(0xFFECFCCB),
            borderColor = Color(0xFFBEF264),
            textPrimary = Color(0xFF365314),
            textSecondary = Color(0xFF4D7C0F),
            pillBg = Color(0xFFD9F99D)
        ),
        // 11: Cobalt Azure
        PremiumSetColor(
            mainAccent = Color(0xFF1D4ED8),
            cardBgGradientStart = Color(0xFFEFF6FF),
            cardBgGradientEnd = Color(0xFFDBEAFE),
            borderColor = Color(0xFF60A5FA),
            textPrimary = Color(0xFF172554),
            textSecondary = Color(0xFF1E40AF),
            pillBg = Color(0xFFBFDBFE)
        ),
        // 12: Warm Coral
        PremiumSetColor(
            mainAccent = Color(0xFFE05238),
            cardBgGradientStart = Color(0xFFFFF5F2),
            cardBgGradientEnd = Color(0xFFFFE7E0),
            borderColor = Color(0xFFFFB4A2),
            textPrimary = Color(0xFF6B1D0F),
            textSecondary = Color(0xFFB83A22),
            pillBg = Color(0xFFFFD5CC)
        ),
        // 13: Violet Plum
        PremiumSetColor(
            mainAccent = Color(0xFF9333EA),
            cardBgGradientStart = Color(0xFFFAF5FF),
            cardBgGradientEnd = Color(0xFFF3E8FF),
            borderColor = Color(0xFFD8B4FE),
            textPrimary = Color(0xFF581C87),
            textSecondary = Color(0xFF7E22CE),
            pillBg = Color(0xFFE9D5FF)
        ),
        // 14: Mint Sage
        PremiumSetColor(
            mainAccent = Color(0xFF0F766E),
            cardBgGradientStart = Color(0xFFF2FBF9),
            cardBgGradientEnd = Color(0xFFD5F5EE),
            borderColor = Color(0xFF6EE7B7),
            textPrimary = Color(0xFF134E48),
            textSecondary = Color(0xFF0F766E),
            pillBg = Color(0xFFA7F3D0)
        ),
        // 15: Golden Bronze
        PremiumSetColor(
            mainAccent = Color(0xFFB45309),
            cardBgGradientStart = Color(0xFFFEF9EE),
            cardBgGradientEnd = Color(0xFFFDF0D5),
            borderColor = Color(0xFFFCD34D),
            textPrimary = Color(0xFF5A2A04),
            textSecondary = Color(0xFF92400E),
            pillBg = Color(0xFFFDE68A)
        ),
        // 16: Sky Azure
        PremiumSetColor(
            mainAccent = Color(0xFF0284C7),
            cardBgGradientStart = Color(0xFFF0F9FF),
            cardBgGradientEnd = Color(0xFFE0F2FE),
            borderColor = Color(0xFF7DD3FC),
            textPrimary = Color(0xFF0C4A6E),
            textSecondary = Color(0xFF0369A1),
            pillBg = Color(0xFFBAE6FD)
        ),
        // 17: Raspberry Pink
        PremiumSetColor(
            mainAccent = Color(0xFFBE185D),
            cardBgGradientStart = Color(0xFFFDF2F8),
            cardBgGradientEnd = Color(0xFFFCE7F3),
            borderColor = Color(0xFFF472B6),
            textPrimary = Color(0xFF700D36),
            textSecondary = Color(0xFF9D174D),
            pillBg = Color(0xFFFBCFE8)
        ),
        // 18: Forest Pine
        PremiumSetColor(
            mainAccent = Color(0xFF15803D),
            cardBgGradientStart = Color(0xFFF0FDF4),
            cardBgGradientEnd = Color(0xFFDCFCE7),
            borderColor = Color(0xFF86EFAC),
            textPrimary = Color(0xFF14532D),
            textSecondary = Color(0xFF166534),
            pillBg = Color(0xFFBBF7D0)
        ),
        // 19: Copper Sienna
        PremiumSetColor(
            mainAccent = Color(0xFFC2410C),
            cardBgGradientStart = Color(0xFFFFF8F5),
            cardBgGradientEnd = Color(0xFFFFECE3),
            borderColor = Color(0xFFFFB899),
            textPrimary = Color(0xFF581C06),
            textSecondary = Color(0xFF9A3412),
            pillBg = Color(0xFFFFD7C7)
        ),
        // 20: Electric Iris
        PremiumSetColor(
            mainAccent = Color(0xFF6366F1),
            cardBgGradientStart = Color(0xFFF4F4FF),
            cardBgGradientEnd = Color(0xFFE5E7FF),
            borderColor = Color(0xFFB4B9FE),
            textPrimary = Color(0xFF28256A),
            textSecondary = Color(0xFF4F46E5),
            pillBg = Color(0xFFD0D4FE)
        ),
        // 21: Crimson Wine
        PremiumSetColor(
            mainAccent = Color(0xFF9F1239),
            cardBgGradientStart = Color(0xFFFFF1F4),
            cardBgGradientEnd = Color(0xFFFFE0E8),
            borderColor = Color(0xFFFDA4AF),
            textPrimary = Color(0xFF4C0519),
            textSecondary = Color(0xFF881337),
            pillBg = Color(0xFFFECDD3)
        ),
        // 22: Aegean Slate
        PremiumSetColor(
            mainAccent = Color(0xFF0369A1),
            cardBgGradientStart = Color(0xFFF0F7FA),
            cardBgGradientEnd = Color(0xFFD9EDF7),
            borderColor = Color(0xFF7DD3FC),
            textPrimary = Color(0xFF082F49),
            textSecondary = Color(0xFF0284C7),
            pillBg = Color(0xFFBAE6FD)
        ),
        // 23: Olive Moss
        PremiumSetColor(
            mainAccent = Color(0xFF4D7C0F),
            cardBgGradientStart = Color(0xFFF8FEEB),
            cardBgGradientEnd = Color(0xFFE4F7C7),
            borderColor = Color(0xFFA3E635),
            textPrimary = Color(0xFF1A2E05),
            textSecondary = Color(0xFF365314),
            pillBg = Color(0xFFCCF29B)
        )
    )

    private val curatedSetsDark = listOf(
        // 0: Sapphire Blue
        PremiumSetColor(
            mainAccent = Color(0xFF3B82F6),
            cardBgGradientStart = Color(0xFF1E3A8A).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF172554).copy(alpha = 0.45f),
            borderColor = Color(0xFF3B82F6).copy(alpha = 0.6f),
            textPrimary = Color(0xFFF1F5F9),
            textSecondary = Color(0xFF93C5FD),
            pillBg = Color(0xFF1E3A8A)
        ),
        // 1: Royal Purple
        PremiumSetColor(
            mainAccent = Color(0xFF8B5CF6),
            cardBgGradientStart = Color(0xFF2E1065).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF1E1B4B).copy(alpha = 0.45f),
            borderColor = Color(0xFF8B5CF6).copy(alpha = 0.6f),
            textPrimary = Color(0xFFF5F3FF),
            textSecondary = Color(0xFFC084FC),
            pillBg = Color(0xFF4C1D95)
        ),
        // 2: Emerald Green
        PremiumSetColor(
            mainAccent = Color(0xFF10B981),
            cardBgGradientStart = Color(0xFF064E3B).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF022C22).copy(alpha = 0.45f),
            borderColor = Color(0xFF10B981).copy(alpha = 0.6f),
            textPrimary = Color(0xFFECFDF5),
            textSecondary = Color(0xFF34D399),
            pillBg = Color(0xFF065F46)
        ),
        // 3: Vibrant Amber / Gold
        PremiumSetColor(
            mainAccent = Color(0xFFF59E0B),
            cardBgGradientStart = Color(0xFF451A03).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF1C1917).copy(alpha = 0.45f),
            borderColor = Color(0xFFF59E0B).copy(alpha = 0.6f),
            textPrimary = Color(0xFFFFFBEB),
            textSecondary = Color(0xFFFBBF24),
            pillBg = Color(0xFF78350F)
        ),
        // 4: Ruby Rose
        PremiumSetColor(
            mainAccent = Color(0xFFF43F5E),
            cardBgGradientStart = Color(0xFF4C0519).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF1F030B).copy(alpha = 0.45f),
            borderColor = Color(0xFFF43F5E).copy(alpha = 0.6f),
            textPrimary = Color(0xFFFFF1F2),
            textSecondary = Color(0xFFFDA4AF),
            pillBg = Color(0xFF881337)
        ),
        // 5: Ocean Teal
        PremiumSetColor(
            mainAccent = Color(0xFF14B8A6),
            cardBgGradientStart = Color(0xFF042F2E).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF0F172A).copy(alpha = 0.45f),
            borderColor = Color(0xFF14B8A6).copy(alpha = 0.6f),
            textPrimary = Color(0xFFF0FDFA),
            textSecondary = Color(0xFF2DD4BF),
            pillBg = Color(0xFF115E59)
        ),
        // 6: Electric Indigo
        PremiumSetColor(
            mainAccent = Color(0xFF6366F1),
            cardBgGradientStart = Color(0xFF1E1B4B).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF0F172A).copy(alpha = 0.45f),
            borderColor = Color(0xFF6366F1).copy(alpha = 0.6f),
            textPrimary = Color(0xFFEEF2FF),
            textSecondary = Color(0xFF818CF8),
            pillBg = Color(0xFF312E81)
        ),
        // 7: Sunset Tangerine
        PremiumSetColor(
            mainAccent = Color(0xFFF97316),
            cardBgGradientStart = Color(0xFF431407).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF1C1917).copy(alpha = 0.45f),
            borderColor = Color(0xFFF97316).copy(alpha = 0.6f),
            textPrimary = Color(0xFFFFF7ED),
            textSecondary = Color(0xFFFDBA74),
            pillBg = Color(0xFF7C2D12)
        ),
        // 8: Deep Cyan
        PremiumSetColor(
            mainAccent = Color(0xFF06B6D4),
            cardBgGradientStart = Color(0xFF083344).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF0F172A).copy(alpha = 0.45f),
            borderColor = Color(0xFF06B6D4).copy(alpha = 0.6f),
            textPrimary = Color(0xFFECFEFF),
            textSecondary = Color(0xFF22D3EE),
            pillBg = Color(0xFF164E63)
        ),
        // 9: Magenta Fuchsia
        PremiumSetColor(
            mainAccent = Color(0xFFD946EF),
            cardBgGradientStart = Color(0xFF4A044E).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF0F172A).copy(alpha = 0.45f),
            borderColor = Color(0xFFD946EF).copy(alpha = 0.6f),
            textPrimary = Color(0xFFFDF4FF),
            textSecondary = Color(0xFFE879F9),
            pillBg = Color(0xFF701A75)
        ),
        // 10: Lime Apple
        PremiumSetColor(
            mainAccent = Color(0xFF84CC16),
            cardBgGradientStart = Color(0xFF1A2E05).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF0F172A).copy(alpha = 0.45f),
            borderColor = Color(0xFF84CC16).copy(alpha = 0.6f),
            textPrimary = Color(0xFFF7FEE7),
            textSecondary = Color(0xFFA3E635),
            pillBg = Color(0xFF365314)
        ),
        // 11: Cobalt Azure
        PremiumSetColor(
            mainAccent = Color(0xFF60A5FA),
            cardBgGradientStart = Color(0xFF172554).copy(alpha = 0.7f),
            cardBgGradientEnd = Color(0xFF0F172A).copy(alpha = 0.45f),
            borderColor = Color(0xFF60A5FA).copy(alpha = 0.6f),
            textPrimary = Color(0xFFEFF6FF),
            textSecondary = Color(0xFF93C5FD),
            pillBg = Color(0xFF1E3A8A)
        ),
        // 12: Warm Coral
        PremiumSetColor(
            mainAccent = Color(0xFFFF6B4A),
            cardBgGradientStart = Color(0xFF4E160C).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF1C1917).copy(alpha = 0.45f),
            borderColor = Color(0xFFFF6B4A).copy(alpha = 0.6f),
            textPrimary = Color(0xFFFFF5F2),
            textSecondary = Color(0xFFFF9E88),
            pillBg = Color(0xFF7A2414)
        ),
        // 13: Violet Plum
        PremiumSetColor(
            mainAccent = Color(0xFFA855F7),
            cardBgGradientStart = Color(0xFF3B0764).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF0F172A).copy(alpha = 0.45f),
            borderColor = Color(0xFFA855F7).copy(alpha = 0.6f),
            textPrimary = Color(0xFFFAF5FF),
            textSecondary = Color(0xFFC084FC),
            pillBg = Color(0xFF581C87)
        ),
        // 14: Mint Sage
        PremiumSetColor(
            mainAccent = Color(0xFF2DD4BF),
            cardBgGradientStart = Color(0xFF042F2E).copy(alpha = 0.7f),
            cardBgGradientEnd = Color(0xFF022C22).copy(alpha = 0.45f),
            borderColor = Color(0xFF2DD4BF).copy(alpha = 0.6f),
            textPrimary = Color(0xFFF2FBF9),
            textSecondary = Color(0xFF5EEAD4),
            pillBg = Color(0xFF115E59)
        ),
        // 15: Golden Bronze
        PremiumSetColor(
            mainAccent = Color(0xFFFBBF24),
            cardBgGradientStart = Color(0xFF3F1D03).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF1C1917).copy(alpha = 0.45f),
            borderColor = Color(0xFFFBBF24).copy(alpha = 0.6f),
            textPrimary = Color(0xFFFEF9EE),
            textSecondary = Color(0xFFFCD34D),
            pillBg = Color(0xFF78350F)
        ),
        // 16: Sky Azure
        PremiumSetColor(
            mainAccent = Color(0xFF38BDF8),
            cardBgGradientStart = Color(0xFF082F49).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF0F172A).copy(alpha = 0.45f),
            borderColor = Color(0xFF38BDF8).copy(alpha = 0.6f),
            textPrimary = Color(0xFFF0F9FF),
            textSecondary = Color(0xFF7DD3FC),
            pillBg = Color(0xFF0C4A6E)
        ),
        // 17: Raspberry Pink
        PremiumSetColor(
            mainAccent = Color(0xFFF472B6),
            cardBgGradientStart = Color(0xFF500724).copy(alpha = 0.65f),
            cardBgGradientEnd = Color(0xFF1F030B).copy(alpha = 0.45f),
            borderColor = Color(0xFFF472B6).copy(alpha = 0.6f),
            textPrimary = Color(0xFFFDF2F8),
            textSecondary = Color(0xFFF9A8D4),
            pillBg = Color(0xFF831843)
        ),
        // 18: Forest Pine
        PremiumSetColor(
            mainAccent = Color(0xFF22C55E),
            cardBgGradientStart = Color(0xFF052E16).copy(alpha = 0.7f),
            cardBgGradientEnd = Color(0xFF022C22).copy(alpha = 0.45f),
            borderColor = Color(0xFF22C55E).copy(alpha = 0.6f),
            textPrimary = Color(0xFFF0FDF4),
            textSecondary = Color(0xFF4ADE80),
            pillBg = Color(0xFF14532D)
        ),
        // 19: Copper Sienna
        PremiumSetColor(
            mainAccent = Color(0xFFFB923C),
            cardBgGradientStart = Color(0xFF431407).copy(alpha = 0.7f),
            cardBgGradientEnd = Color(0xFF1C1917).copy(alpha = 0.45f),
            borderColor = Color(0xFFFB923C).copy(alpha = 0.6f),
            textPrimary = Color(0xFFFFF8F5),
            textSecondary = Color(0xFFFDBA74),
            pillBg = Color(0xFF7C2D12)
        ),
        // 20: Electric Iris
        PremiumSetColor(
            mainAccent = Color(0xFF818CF8),
            cardBgGradientStart = Color(0xFF1E1B4B).copy(alpha = 0.7f),
            cardBgGradientEnd = Color(0xFF0F172A).copy(alpha = 0.45f),
            borderColor = Color(0xFF818CF8).copy(alpha = 0.6f),
            textPrimary = Color(0xFFF4F4FF),
            textSecondary = Color(0xFFA5B4FC),
            pillBg = Color(0xFF312E81)
        ),
        // 21: Crimson Wine
        PremiumSetColor(
            mainAccent = Color(0xFFFB7185),
            cardBgGradientStart = Color(0xFF4C0519).copy(alpha = 0.7f),
            cardBgGradientEnd = Color(0xFF1F030B).copy(alpha = 0.45f),
            borderColor = Color(0xFFFB7185).copy(alpha = 0.6f),
            textPrimary = Color(0xFFFFF1F4),
            textSecondary = Color(0xFFFDA4AF),
            pillBg = Color(0xFF881337)
        ),
        // 22: Aegean Slate
        PremiumSetColor(
            mainAccent = Color(0xFF38BDF8),
            cardBgGradientStart = Color(0xFF082F49).copy(alpha = 0.7f),
            cardBgGradientEnd = Color(0xFF0F172A).copy(alpha = 0.45f),
            borderColor = Color(0xFF38BDF8).copy(alpha = 0.6f),
            textPrimary = Color(0xFFF0F7FA),
            textSecondary = Color(0xFF7DD3FC),
            pillBg = Color(0xFF0C4A6E)
        ),
        // 23: Olive Moss
        PremiumSetColor(
            mainAccent = Color(0xFFA3E635),
            cardBgGradientStart = Color(0xFF1A2E05).copy(alpha = 0.7f),
            cardBgGradientEnd = Color(0xFF0F172A).copy(alpha = 0.45f),
            borderColor = Color(0xFFA3E635).copy(alpha = 0.6f),
            textPrimary = Color(0xFFF8FEEB),
            textSecondary = Color(0xFFBEF264),
            pillBg = Color(0xFF365314)
        )
    )

    /**
     * Procedural algorithmic palette generation using the Golden Ratio for unbounded indices.
     */
    private fun generateProceduralPalette(index: Int, isSubtopic: Boolean, isDarkMode: Boolean): PremiumSetColor {
        // Golden ratio hue stepping (137.508 degrees) to guarantee maximum visual distance
        val baseOffset = if (isSubtopic) 195f else 25f
        val hue = (index * 137.508f + baseOffset) % 360f

        return if (isDarkMode) {
            PremiumSetColor(
                mainAccent = hslToColor(hue, 0.90f, 0.60f),
                cardBgGradientStart = hslToColor(hue, 0.70f, 0.16f, alpha = 0.75f),
                cardBgGradientEnd = hslToColor(hue, 0.50f, 0.08f, alpha = 0.55f),
                borderColor = hslToColor(hue, 0.80f, 0.50f, alpha = 0.65f),
                textPrimary = hslToColor(hue, 0.20f, 0.96f),
                textSecondary = hslToColor(hue, 0.75f, 0.78f),
                pillBg = hslToColor(hue, 0.70f, 0.22f),
                startButtonText = Color.White
            )
        } else {
            PremiumSetColor(
                mainAccent = hslToColor(hue, 0.85f, 0.42f),
                cardBgGradientStart = hslToColor(hue, 0.35f, 0.96f),
                cardBgGradientEnd = hslToColor(hue, 0.45f, 0.91f),
                borderColor = hslToColor(hue, 0.65f, 0.76f),
                textPrimary = hslToColor(hue, 0.90f, 0.16f),
                textSecondary = hslToColor(hue, 0.80f, 0.36f),
                pillBg = hslToColor(hue, 0.55f, 0.86f),
                startButtonText = Color.White
            )
        }
    }

    /**
     * Set 03 Style Premium Palette (Gold / Amber / Bronze) from the reference design.
     */
    val set03StylePalette = PremiumSetColor(
        mainAccent = Color(0xFFFFB800),
        cardBgGradientStart = Color(0xFFFFBF00),
        cardBgGradientEnd = Color(0xFF7A3900),
        borderColor = Color(0xFFFFD54F).copy(alpha = 0.5f),
        textPrimary = Color.White,
        textSecondary = Color(0xFFFFF0D0),
        pillBg = Color(0xFFFFC400),
        startButtonText = Color.White
    )

    /**
     * S02 Style Premium Palette (Vibrant Coral Red / Crimson / Burgundy) from the reference design.
     */
    val s02StyleSubtopicPalette = PremiumSetColor(
        mainAccent = Color(0xFFF43F5E),
        cardBgGradientStart = Color(0xFFF43F5E),
        cardBgGradientEnd = Color(0xFF6B0B1D),
        borderColor = Color(0xFFFDA4AF).copy(alpha = 0.4f),
        textPrimary = Color.White,
        textSecondary = Color(0xFFFFE4E6),
        pillBg = Color(0xFFFF5722),
        startButtonText = Color.White
    )

    /**
     * Gets the exact Set 03 style premium color palette for any Practice Set.
     */
    fun getSetPalette(setNumber: Int, isDarkMode: Boolean): PremiumSetColor {
        return set03StylePalette
    }

    /**
     * Gets the exact S02 style premium color palette for any Subtopic.
     */
    fun getSubtopicPalette(
        subtopicNumber: Int,
        isDarkMode: Boolean,
        subtopicName: String? = null
    ): PremiumSetColor {
        return s02StyleSubtopicPalette
    }

    /**
     * Universal getter by string key / ID for any card across the app.
     */
    fun getPaletteById(id: String, isSubtopic: Boolean, isDarkMode: Boolean): PremiumSetColor {
        val hash = abs(id.hashCode())
        val index = hash % 100
        return if (isSubtopic) {
            getSubtopicPalette(index + 1, isDarkMode)
        } else {
            getSetPalette(index + 1, isDarkMode)
        }
    }

    // =========================================================================
    // RICH DEEP PREMIUM GRADIENTS FOR SUBJECTS, UNITS, TOPICS & DASHBOARD CARDS
    // =========================================================================

    // APPSC GROUP-2 BESPOKE ULTRA-PREMIUM GRADIENTS (Jewel-toned with gleaming metallic borders)
    val ApHistoryBurgundyGradient = RichCardGradient(
        colors = listOf(Color(0xFF2D0712), Color(0xFF5B0E23), Color(0xFF881337), Color(0xFF9F1239)),
        borderColor = Color(0xFFFFD700), // Imperial 24K Gold
        textColorSecondary = Color(0xFFFDE68A),
        badgeBg = Color(0xFF4C0519),
        badgeText = Color(0xFFFFD700)
    )

    val IndianHistoryCrimsonGradient = RichCardGradient(
        colors = listOf(Color(0xFF38020E), Color(0xFF670921), Color(0xFF9F1239), Color(0xFFB91C1C)),
        borderColor = Color(0xFFFBBF24), // Rich Amber Gold
        textColorSecondary = Color(0xFFFECDD3),
        badgeBg = Color(0xFF881337),
        badgeText = Color.White
    )

    val GeographyEmeraldGradient = RichCardGradient(
        colors = listOf(Color(0xFF022C22), Color(0xFF064E3B), Color(0xFF047857), Color(0xFF0D9488)),
        borderColor = Color(0xFF34D399), // Radiant Mint Emerald
        textColorSecondary = Color(0xFFA7F3D0),
        badgeBg = Color(0xFF065F46),
        badgeText = Color.White
    )

    val SocietyAmethystPlumGradient = RichCardGradient(
        colors = listOf(Color(0xFF260538), Color(0xFF4A044E), Color(0xFF701A75), Color(0xFF9333EA)),
        borderColor = Color(0xFFE879F9), // Luminous Orchid
        textColorSecondary = Color(0xFFF5D0FE),
        badgeBg = Color(0xFF581C87),
        badgeText = Color.White
    )

    val MentalAbilitySapphireGradient = RichCardGradient(
        colors = listOf(Color(0xFF081225), Color(0xFF0C2340), Color(0xFF1E3A8A), Color(0xFF2563EB)),
        borderColor = Color(0xFF60A5FA), // Electric Cobalt
        textColorSecondary = Color(0xFFBFDBFE),
        badgeBg = Color(0xFF1E40AF),
        badgeText = Color.White
    )

    val ConstitutionPolityIndigoGradient = RichCardGradient(
        colors = listOf(Color(0xFF0F0B30), Color(0xFF1E1B4B), Color(0xFF3730A3), Color(0xFF4F46E5)),
        borderColor = Color(0xFFA78BFA), // Sovereign Purple Gold
        textColorSecondary = Color(0xFFDDD6FE),
        badgeBg = Color(0xFF312E81),
        badgeText = Color.White
    )

    val EconomyBronzeAmberGradient = RichCardGradient(
        colors = listOf(Color(0xFF2E1004), Color(0xFF561D07), Color(0xFF882E0B), Color(0xFFC2410C)),
        borderColor = Color(0xFFFDBA74), // Warm Bronze Gold
        textColorSecondary = Color(0xFFFED7AA),
        badgeBg = Color(0xFF7C2D12),
        badgeText = Color.White
    )

    val ScienceTechCyanGradient = RichCardGradient(
        colors = listOf(Color(0xFF031D28), Color(0xFF083344), Color(0xFF0E7490), Color(0xFF0284C7)),
        borderColor = Color(0xFF38BDF8), // Radiant Aquamarine
        textColorSecondary = Color(0xFFA5F3FC),
        badgeBg = Color(0xFF155E75),
        badgeText = Color.White
    )

    val GeneralStudiesCoralGradient = RichCardGradient(
        colors = listOf(Color(0xFF350B0B), Color(0xFF6B1111), Color(0xFF991B1B), Color(0xFFDC2626)),
        borderColor = Color(0xFFFCA5A5), // Glowing Coral
        textColorSecondary = Color(0xFFFEE2E2),
        badgeBg = Color(0xFF7F1D1D),
        badgeText = Color.White
    )

    val CurrentAffairsRubyGoldGradient = RichCardGradient(
        colors = listOf(Color(0xFF380718), Color(0xFF6B0E2E), Color(0xFF9F1239), Color(0xFFBE123C)),
        borderColor = Color(0xFFFFD700), // Regal 24K Gold
        textColorSecondary = Color(0xFFFDE68A),
        badgeBg = Color(0xFF881337),
        badgeText = Color(0xFFFFD700)
    )

    val RoyalBlueGradient = RichCardGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF2563EB)),
        borderColor = Color(0xFF60A5FA),
        textColorSecondary = Color(0xFFBFDBFE),
        badgeBg = Color(0xFF3B82F6),
        badgeText = Color.White
    )

    val EmeraldTealGradient = RichCardGradient(
        colors = listOf(Color(0xFF022C22), Color(0xFF047857), Color(0xFF0D9488)),
        borderColor = Color(0xFF34D399),
        textColorSecondary = Color(0xFFA7F3D0),
        badgeBg = Color(0xFF10B981),
        badgeText = Color.White
    )

    val MagentaPlumGradient = RichCardGradient(
        colors = listOf(Color(0xFF4A044E), Color(0xFF86198F), Color(0xFFC026D3)),
        borderColor = Color(0xFFE879F9),
        textColorSecondary = Color(0xFFF5D0FE),
        badgeBg = Color(0xFFD946EF),
        badgeText = Color.White
    )

    val CrimsonRedGradient = RichCardGradient(
        colors = listOf(Color(0xFF4C0519), Color(0xFF9F1239), Color(0xFFE11D48)),
        borderColor = Color(0xFFFDA4AF),
        textColorSecondary = Color(0xFFFECDD3),
        badgeBg = Color(0xFFF43F5E),
        badgeText = Color.White
    )

    val DeepOrangeGradient = RichCardGradient(
        colors = listOf(Color(0xFF431407), Color(0xFF9A3412), Color(0xFFEA580C)),
        borderColor = Color(0xFFFDBA74),
        textColorSecondary = Color(0xFFFED7AA),
        badgeBg = Color(0xFFF97316),
        badgeText = Color.White
    )

    val BluePurpleGradient = RichCardGradient(
        colors = listOf(Color(0xFF1E1B4B), Color(0xFF4338CA), Color(0xFF7C3AED)),
        borderColor = Color(0xFFC4B5FD),
        textColorSecondary = Color(0xFFDDD6FE),
        badgeBg = Color(0xFF8B5CF6),
        badgeText = Color.White
    )

    val DeepCyanGradient = RichCardGradient(
        colors = listOf(Color(0xFF083344), Color(0xFF0E7490), Color(0xFF0284C7)),
        borderColor = Color(0xFF67E8F9),
        textColorSecondary = Color(0xFFA5F3FC),
        badgeBg = Color(0xFF06B6D4),
        badgeText = Color.White
    )

    val GoldenBronzeGradient = RichCardGradient(
        colors = listOf(Color(0xFF451A03), Color(0xFF78350F), Color(0xFFD97706)),
        borderColor = Color(0xFFFCD34D),
        textColorSecondary = Color(0xFFFDE68A),
        badgeBg = Color(0xFFF59E0B),
        badgeText = Color(0xFF0F172A)
    )

    val DeepNavySlateGoldGradient = RichCardGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF475569)),
        borderColor = Color(0xFFFFD700).copy(alpha = 0.5f),
        textColorSecondary = Color(0xFFFFD700),
        badgeBg = Color(0xFFFFD700),
        badgeText = Color(0xFF0F172A)
    )

    private val allRichGradients = listOf(
        ApHistoryBurgundyGradient,
        IndianHistoryCrimsonGradient,
        GeographyEmeraldGradient,
        SocietyAmethystPlumGradient,
        MentalAbilitySapphireGradient,
        ConstitutionPolityIndigoGradient,
        EconomyBronzeAmberGradient,
        ScienceTechCyanGradient,
        CurrentAffairsRubyGoldGradient,
        GeneralStudiesCoralGradient,
        RoyalBlueGradient,
        EmeraldTealGradient,
        MagentaPlumGradient,
        CrimsonRedGradient,
        DeepOrangeGradient,
        BluePurpleGradient,
        DeepCyanGradient,
        GoldenBronzeGradient,
        DeepNavySlateGoldGradient
    )

    fun getGradientForSubject(subjectName: String, id: String = ""): RichCardGradient {
        val lower = "$subjectName $id".lowercase()
        return when {
            lower.contains("current") || lower.contains("affair") -> CurrentAffairsRubyGoldGradient
            lower.contains("ap-history") || (lower.contains("history") && lower.contains("andhra")) || lower.contains("social & cultural") || lower.contains("social and cultural") -> ApHistoryBurgundyGradient
            lower.contains("indian history") || lower.contains("history") || lower.contains("ancient") || lower.contains("medieval") || lower.contains("modern") -> IndianHistoryCrimsonGradient
            lower.contains("polity") || lower.contains("constitution") || lower.contains("governance") -> ConstitutionPolityIndigoGradient
            lower.contains("economy") || lower.contains("finance") || lower.contains("budget") || lower.contains("economic") -> EconomyBronzeAmberGradient
            lower.contains("geo") || lower.contains("earth") || lower.contains("environment") || lower.contains("climate") -> GeographyEmeraldGradient
            lower.contains("society") || lower.contains("social justice") || lower.contains("welfare") -> SocietyAmethystPlumGradient
            lower.contains("mental") || lower.contains("reasoning") || lower.contains("aptitude") -> MentalAbilitySapphireGradient
            lower.contains("science") || lower.contains("tech") || lower.contains("technology") -> ScienceTechCyanGradient
            lower.contains("general studies") -> GeneralStudiesCoralGradient
            lower.contains("mock") || lower.contains("test") -> RoyalBlueGradient
            else -> {
                val index = abs(lower.hashCode()) % allRichGradients.size
                allRichGradients[index]
            }
        }
    }

    fun getGradientForUnit(unitNumber: Int, subjectName: String = "", unitName: String = ""): RichCardGradient {
        val lower = "$subjectName $unitName".lowercase()
        if (lower.contains("ap-history") || (lower.contains("history") && lower.contains("andhra")) || lower.contains("social & cultural")) return ApHistoryBurgundyGradient
        if (lower.contains("history")) return IndianHistoryCrimsonGradient
        if (lower.contains("polity") || lower.contains("constitution")) return ConstitutionPolityIndigoGradient
        if (lower.contains("economy")) return EconomyBronzeAmberGradient
        if (lower.contains("geo")) return GeographyEmeraldGradient
        if (lower.contains("society")) return SocietyAmethystPlumGradient
        if (lower.contains("mental") || lower.contains("reasoning")) return MentalAbilitySapphireGradient
        if (lower.contains("science") || lower.contains("tech")) return ScienceTechCyanGradient
        if (lower.contains("current") || lower.contains("general studies")) return GeneralStudiesCoralGradient

        val index = (unitNumber + abs(subjectName.hashCode())) % allRichGradients.size
        return allRichGradients[index]
    }

    fun getGradientForTopic(topicIndex: Int, topicName: String = "", subjectName: String = ""): RichCardGradient {
        val lower = "$topicName $subjectName".lowercase()
        if (lower.contains("satavahana") || lower.contains("andhra") || lower.contains("kakatiya") || lower.contains("vijayanagara")) return ApHistoryBurgundyGradient
        if (lower.contains("history") || lower.contains("war") || lower.contains("king") || lower.contains("vedic") || lower.contains("revolt")) return IndianHistoryCrimsonGradient
        if (lower.contains("article") || lower.contains("court") || lower.contains("law") || lower.contains("preamble") || lower.contains("rights")) return ConstitutionPolityIndigoGradient
        if (lower.contains("bank") || lower.contains("tax") || lower.contains("gdp") || lower.contains("budget") || lower.contains("inflation")) return EconomyBronzeAmberGradient
        if (lower.contains("river") || lower.contains("soil") || lower.contains("forest") || lower.contains("climate") || lower.contains("drainage")) return GeographyEmeraldGradient
        if (lower.contains("caste") || lower.contains("women") || lower.contains("tribe") || lower.contains("welfare") || lower.contains("marriage")) return SocietyAmethystPlumGradient
        if (lower.contains("reasoning") || lower.contains("coding") || lower.contains("ratio") || lower.contains("number")) return MentalAbilitySapphireGradient
        if (lower.contains("space") || lower.contains("energy") || lower.contains("ai") || lower.contains("isro") || lower.contains("mission")) return ScienceTechCyanGradient
        if (lower.contains("scheme") || lower.contains("treaty") || lower.contains("summit") || lower.contains("news")) return GeneralStudiesCoralGradient

        val index = (topicIndex + abs(topicName.hashCode())) % allRichGradients.size
        return allRichGradients[index]
    }

    fun getGradientForDashboard(cardName: String, index: Int = 0): RichCardGradient {
        val lower = cardName.lowercase()
        return when {
            lower.contains("current") || lower.contains("news") -> CrimsonRedGradient
            lower.contains("mock") || lower.contains("generator") -> BluePurpleGradient
            lower.contains("practice") || lower.contains("arena") -> EmeraldTealGradient
            lower.contains("quiz") || lower.contains("daily") -> DeepOrangeGradient
            lower.contains("poster") || lower.contains("pro") || lower.contains("vip") -> GoldenBronzeGradient
            lower.contains("leaderboard") || lower.contains("rank") -> MagentaPlumGradient
            lower.contains("stats") || lower.contains("performance") -> DeepCyanGradient
            else -> {
                val idx = (index + abs(cardName.hashCode())) % allRichGradients.size
                allRichGradients[idx]
            }
        }
    }

    // =========================================================================
    // PREMIUM INDIVIDUAL MOCK TEST CARD COLOR SYSTEM
    // =========================================================================

    val mockTestPalettes = listOf(
        // 0: Mock Test 01 → Premium Indigo/Violet
        PremiumMockTestCardColor(
            name = "Indigo/Violet",
            primaryAccent = Color(0xFF4F46E5),
            secondaryAccent = Color(0xFF7C3AED),
            cardBgGradientStart = Color(0xFFF5F3FF),
            cardBgGradientEnd = Color(0xFFEEF2FF),
            borderColor = Color(0xFFC7D2FE),
            badgeBg = Color(0xFFE0E7FF),
            badgeText = Color(0xFF3730A3),
            textPrimary = Color(0xFF1E1B4B),
            textSecondary = Color(0xFF4338CA),
            iconTint = Color(0xFF4F46E5),
            actionButtonBg = Color(0xFF4F46E5)
        ),
        // 1: Mock Test 02 → Premium Emerald/Teal
        PremiumMockTestCardColor(
            name = "Emerald/Teal",
            primaryAccent = Color(0xFF059669),
            secondaryAccent = Color(0xFF0D9488),
            cardBgGradientStart = Color(0xFFECFDF5),
            cardBgGradientEnd = Color(0xFFF0FDFA),
            borderColor = Color(0xFFA7F3D0),
            badgeBg = Color(0xFFD1FAE5),
            badgeText = Color(0xFF065F46),
            textPrimary = Color(0xFF064E3B),
            textSecondary = Color(0xFF047857),
            iconTint = Color(0xFF059669),
            actionButtonBg = Color(0xFF059669)
        ),
        // 2: Mock Test 03 → Premium Orange/Amber
        PremiumMockTestCardColor(
            name = "Orange/Amber",
            primaryAccent = Color(0xFFEA580C),
            secondaryAccent = Color(0xFFD97706),
            cardBgGradientStart = Color(0xFFFFF7ED),
            cardBgGradientEnd = Color(0xFFFFFBEB),
            borderColor = Color(0xFFFED7AA),
            badgeBg = Color(0xFFFFEDD5),
            badgeText = Color(0xFF9A3412),
            textPrimary = Color(0xFF7C2D12),
            textSecondary = Color(0xFFC2410C),
            iconTint = Color(0xFFEA580C),
            actionButtonBg = Color(0xFFEA580C)
        ),
        // 3: Mock Test 04 → Premium Rose/Magenta
        PremiumMockTestCardColor(
            name = "Rose/Magenta",
            primaryAccent = Color(0xFFE11D48),
            secondaryAccent = Color(0xFFC026D3),
            cardBgGradientStart = Color(0xFFFFF1F2),
            cardBgGradientEnd = Color(0xFFFDF4FF),
            borderColor = Color(0xFFFECDD3),
            badgeBg = Color(0xFFFFE4E6),
            badgeText = Color(0xFF9F1239),
            textPrimary = Color(0xFF881337),
            textSecondary = Color(0xFFBE123C),
            iconTint = Color(0xFFE11D48),
            actionButtonBg = Color(0xFFE11D48)
        ),
        // 4: Mock Test 05 → Premium Blue/Teal
        PremiumMockTestCardColor(
            name = "Blue/Teal",
            primaryAccent = Color(0xFF0284C7),
            secondaryAccent = Color(0xFF0891B2),
            cardBgGradientStart = Color(0xFFF0F9FF),
            cardBgGradientEnd = Color(0xFFECFEFF),
            borderColor = Color(0xFFBAE6FD),
            badgeBg = Color(0xFFE0F2FE),
            badgeText = Color(0xFF075985),
            textPrimary = Color(0xFF0C4A6E),
            textSecondary = Color(0xFF0369A1),
            iconTint = Color(0xFF0284C7),
            actionButtonBg = Color(0xFF0284C7)
        ),
        // 5: Mock Test 06 → Premium Purple
        PremiumMockTestCardColor(
            name = "Purple",
            primaryAccent = Color(0xFF9333EA),
            secondaryAccent = Color(0xFFA855F7),
            cardBgGradientStart = Color(0xFFFAF5FF),
            cardBgGradientEnd = Color(0xFFF3E8FF),
            borderColor = Color(0xFFE9D5FF),
            badgeBg = Color(0xFFF3E8FF),
            badgeText = Color(0xFF6B21A8),
            textPrimary = Color(0xFF581C87),
            textSecondary = Color(0xFF7E22CE),
            iconTint = Color(0xFF9333EA),
            actionButtonBg = Color(0xFF9333EA)
        ),
        // 6: Mock Test 07 → Premium Green
        PremiumMockTestCardColor(
            name = "Green",
            primaryAccent = Color(0xFF16A34A),
            secondaryAccent = Color(0xFF65A30D),
            cardBgGradientStart = Color(0xFFF0FDF4),
            cardBgGradientEnd = Color(0xFFF7FEE7),
            borderColor = Color(0xFFBBF7D0),
            badgeBg = Color(0xFFDCFCE7),
            badgeText = Color(0xFF166534),
            textPrimary = Color(0xFF14532D),
            textSecondary = Color(0xFF15803D),
            iconTint = Color(0xFF16A34A),
            actionButtonBg = Color(0xFF16A34A)
        ),
        // 7: Mock Test 08 → Premium Coral/Orange
        PremiumMockTestCardColor(
            name = "Coral/Orange",
            primaryAccent = Color(0xFFF43F5E),
            secondaryAccent = Color(0xFFF97316),
            cardBgGradientStart = Color(0xFFFFF1F2),
            cardBgGradientEnd = Color(0xFFFFF7ED),
            borderColor = Color(0xFFFFCDD2),
            badgeBg = Color(0xFFFFE4E6),
            badgeText = Color(0xFFBE123C),
            textPrimary = Color(0xFF881337),
            textSecondary = Color(0xFFE11D48),
            iconTint = Color(0xFFF43F5E),
            actionButtonBg = Color(0xFFF43F5E)
        ),
        // 8: Mock Test 09 → Premium Sapphire/Navy
        PremiumMockTestCardColor(
            name = "Sapphire/Navy",
            primaryAccent = Color(0xFF2563EB),
            secondaryAccent = Color(0xFF1D4ED8),
            cardBgGradientStart = Color(0xFFEFF6FF),
            cardBgGradientEnd = Color(0xFFDBEAFE),
            borderColor = Color(0xFF93C5FD),
            badgeBg = Color(0xFFDBEAFE),
            badgeText = Color(0xFF1E40AF),
            textPrimary = Color(0xFF1E3A8A),
            textSecondary = Color(0xFF2563EB),
            iconTint = Color(0xFF2563EB),
            actionButtonBg = Color(0xFF2563EB)
        ),
        // 9: Mock Test 10 → Premium Amber/Bronze
        PremiumMockTestCardColor(
            name = "Amber/Bronze",
            primaryAccent = Color(0xFFD97706),
            secondaryAccent = Color(0xFFB45309),
            cardBgGradientStart = Color(0xFFFFFBEB),
            cardBgGradientEnd = Color(0xFFFEF3C7),
            borderColor = Color(0xFFFCD34D),
            badgeBg = Color(0xFFFEF3C7),
            badgeText = Color(0xFF92400E),
            textPrimary = Color(0xFF78350F),
            textSecondary = Color(0xFFB45309),
            iconTint = Color(0xFFD97706),
            actionButtonBg = Color(0xFFD97706)
        ),
        // 10: Mock Test 11 → Premium Ruby/Crimson
        PremiumMockTestCardColor(
            name = "Ruby/Crimson",
            primaryAccent = Color(0xFFBE123C),
            secondaryAccent = Color(0xFF881337),
            cardBgGradientStart = Color(0xFFFFF1F2),
            cardBgGradientEnd = Color(0xFFFFE4E6),
            borderColor = Color(0xFFFDA4AF),
            badgeBg = Color(0xFFFFE4E6),
            badgeText = Color(0xFF9F1239),
            textPrimary = Color(0xFF881337),
            textSecondary = Color(0xFFBE123C),
            iconTint = Color(0xFFBE123C),
            actionButtonBg = Color(0xFFBE123C)
        ),
        // 11: Mock Test 12 → Premium Jade/Mint
        PremiumMockTestCardColor(
            name = "Jade/Mint",
            primaryAccent = Color(0xFF0F766E),
            secondaryAccent = Color(0xFF14B8A6),
            cardBgGradientStart = Color(0xFFF0FDFA),
            cardBgGradientEnd = Color(0xFFCCFBF1),
            borderColor = Color(0xFF99F6E4),
            badgeBg = Color(0xFFCCFBF1),
            badgeText = Color(0xFF115E59),
            textPrimary = Color(0xFF134E4A),
            textSecondary = Color(0xFF0F766E),
            iconTint = Color(0xFF0F766E),
            actionButtonBg = Color(0xFF0F766E)
        )
    )

    /**
     * Deterministically maps any Mock Test (by title, id, or number) to its constant premium color.
     * Ensures "Mock Test 01" -> Indigo/Violet, "Mock Test 02" -> Emerald/Teal, etc.
     * Guaranteed to never randomize across app rebuilds or screen re-opens.
     */
    fun getMockTestColor(title: String, id: String = ""): PremiumMockTestCardColor {
        // Look for explicit trailing or embedded test number: e.g. "Mock Test 01", "Mock Test 2", "Test #3", "Prelims-2", "Mock 8"
        val explicitNumberMatch = Regex("""(?i)(?:mock\s*test|mock|test|series|exam|#|-)\s*(\d+)""").findAll(title).lastOrNull()
            ?: Regex("""\b(\d+)\b""").findAll(title).firstOrNull()
            ?: Regex("""(?i)(?:mock|test|syllabus|prelims|mains)[^\d]*(\d+)""").find(id)

        val index = if (explicitNumberMatch != null) {
            val num = explicitNumberMatch.groupValues[1].toIntOrNull() ?: 1
            (num - 1).coerceAtLeast(0) % mockTestPalettes.size
        } else {
            // Stable deterministic hash
            abs("$title $id".hashCode()) % mockTestPalettes.size
        }
        return mockTestPalettes[index]
    }
}

/**
 * Premium Individual Mock Test Card Color Scheme
 */
data class PremiumMockTestCardColor(
    val name: String,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val cardBgGradientStart: Color,
    val cardBgGradientEnd: Color,
    val borderColor: Color,
    val badgeBg: Color,
    val badgeText: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val iconTint: Color,
    val actionButtonBg: Color,
    val actionButtonText: Color = Color.White
)

/**
 * Rich Deep Premium Card Gradient Definition for Subjects, Units, Topics, and Dashboard Cards.
 */
data class RichCardGradient(
    val colors: List<Color>,
    val borderColor: Color,
    val textColorPrimary: Color = Color.White,
    val textColorSecondary: Color = Color(0xFFFDE68A),
    val badgeBg: Color = Color(0xFFF59E0B),
    val badgeText: Color = Color(0xFF0F172A)
)
