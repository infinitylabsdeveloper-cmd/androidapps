package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HighlightPreset
import com.example.model.QuestionHighlightConfig

/**
 * Reusable HighlightSegment that stores parsed text chunk details.
 */
data class HighlightSegment(
    val text: String,
    val isHighlighted: Boolean,
    val type: String? = null
)

/**
 * Universal HighlightRenderer component that applies soft pastel background styles to text.
 * Extensively optimized to support all 22 official Indian languages:
 * Assamese, Bengali, Bodo, Dogri, Gujarati, Hindi, Kannada, Kashmiri, Konkani, Maithili,
 * Malayalam, Manipuri, Marathi, Nepali, Odia, Punjabi, Sanskrit, Santali, Sindhi, Tamil,
 * Telugu, and Urdu.
 *
 * Major features:
 * 1. RTL script alignment (Urdu, Kashmiri, Sindhi) is automatically mapped using local LayoutDirection.
 * 2. South-Asian complex script bounds scaling to prevent conjunct/subscript vertical clipping.
 * 3. FlowRow word boundary wrapping preserving original space and punctuation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HighlightRenderer(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    fontFamily: FontFamily = FontFamily.Default,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = MaterialTheme.colorScheme.onSurface,
    highlightConfig: QuestionHighlightConfig = QuestionHighlightConfig(),
    queryPhrases: List<String> = emptyList(),
    queryHighlightType: String = "key_concepts",
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically
) {
    if (text.isBlank()) return

    // 1. Language Script Intelligence Detection
    // Urdu, Kashmiri, Sindhi use Arabic-based script written from Right-To-Left (RTL)
    val hasRtlArabicScript = remember(text) {
        text.any { it.code in 0x0600..0x06FF }
    }

    // Devanagari, Bengali/Assamese, Gurmukhi, Gujarati, Odia, Tamil, Telugu, Kannada, Malayalam complex scripts
    val hasComplexIndicScript = remember(text) {
        text.any { it.code in 0x0900..0x0D7F }
    }

    val calculatedLayoutDirection = if (hasRtlArabicScript) LayoutDirection.Rtl else LayoutDirection.Ltr
    val calculatedArrangement = remember(horizontalArrangement, hasRtlArabicScript) {
        if (hasRtlArabicScript && horizontalArrangement == Arrangement.Start) {
            Arrangement.End
        } else {
            horizontalArrangement
        }
    }

    // Parse the text into clean text and highlight segments
    val finalSegments = remember(text, queryPhrases, queryHighlightType) {
        val stage1 = parseInlineTags(text)
        if (queryPhrases.isEmpty()) {
            stage1
        } else {
            val finalResult = mutableListOf<HighlightSegment>()
            for (seg in stage1) {
                if (seg.isHighlighted) {
                    finalResult.add(seg)
                } else {
                    val subSegments = splitByQueryPhrases(seg.text, queryPhrases, queryHighlightType)
                    finalResult.addAll(subSegments)
                }
            }
            finalResult
        }
    }

    // Split segments into individual words or spaces to allow fluid line wrapping in FlowRow
    val inlineTokens = remember(finalSegments) {
        val tokens = mutableListOf<HighlightSegment>()
        for (seg in finalSegments) {
            if (seg.isHighlighted) {
                tokens.add(seg)
            } else {
                // Split plain text by space boundaries to preserve fluid wrapping
                // Uses regex lookbehind/lookahead to isolate spaces as individual tokens
                val parts = seg.text.split("(?<=\\s)|(?=\\s)".toRegex())
                for (part in parts) {
                    if (part.isNotEmpty()) {
                        tokens.add(HighlightSegment(part, false))
                    }
                }
            }
        }
        tokens
    }

    // Wrap in local LayoutDirection provider to handle RTL languages beautifully
    CompositionLocalProvider(LocalLayoutDirection provides calculatedLayoutDirection) {
        FlowRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = calculatedArrangement,
            verticalArrangement = Arrangement.spacedBy(if (hasComplexIndicScript) 8.dp else 4.dp)
        ) {
            inlineTokens.forEach { token ->
                if (token.isHighlighted && highlightConfig.isMasterEnabled) {
                    HighlightPill(
                        text = token.text,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        highlightType = token.type,
                        config = highlightConfig,
                        hasComplexIndicScript = hasComplexIndicScript,
                        hasRtlArabicScript = hasRtlArabicScript
                    )
                } else {
                    Text(
                        text = token.text,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        color = color,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDirection = if (hasRtlArabicScript) TextDirection.Rtl else TextDirection.Ltr
                        ),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

/**
 * Beautiful custom rounded pill component carrying the premium pastel styles.
 */
@Composable
fun HighlightPill(
    text: String,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    highlightType: String?,
    config: QuestionHighlightConfig,
    hasComplexIndicScript: Boolean,
    hasRtlArabicScript: Boolean
) {
    val preset = highlightType?.let { HighlightPreset.fromName(it) } ?: HighlightPreset.SOFT_AMBER
    val isSystemInDark = isSystemInDarkTheme()

    val chipBg = remember(preset, isSystemInDark, config) {
        if (isSystemInDark) {
            Color(preset.bgColorHex).copy(alpha = 0.22f).compositeOver(Color(0xFF1E293B))
        } else {
            Color(preset.bgColorHex)
        }
    }

    val chipText = remember(preset, isSystemInDark, config) {
        if (isSystemInDark) {
            Color(preset.bgColorHex)
        } else {
            Color(preset.textColorHex)
        }
    }

    val borderStroke = remember(preset, isSystemInDark, config) {
        if (config.hasBorder) {
            val borderColor = if (isSystemInDark) Color(preset.borderColorHex).copy(alpha = 0.5f) else Color(preset.borderColorHex)
            BorderStroke(config.borderWidthDp.dp, borderColor)
        } else {
            null
        }
    }

    val parsedFontWeight = when (config.fontWeightName.lowercase()) {
        "bold" -> FontWeight.Bold
        "medium" -> FontWeight.Medium
        "normal" -> FontWeight.Normal
        else -> FontWeight.SemiBold
    }

    // Scale padding vertically for highly nested conjunct scripts (Kannada, Telugu, Hindi, Malayalam etc.)
    // to prevent vertical boundary clipping.
    val calculatedVerticalPadding = remember(config.verticalPaddingDp, hasComplexIndicScript) {
        if (hasComplexIndicScript) {
            (config.verticalPaddingDp + 1).coerceAtLeast(4).dp
        } else {
            config.verticalPaddingDp.dp
        }
    }

    Surface(
        shape = RoundedCornerShape(config.cornerRadiusDp.dp),
        color = chipBg,
        border = borderStroke,
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = parsedFontWeight,
            color = chipText,
            style = MaterialTheme.typography.bodyLarge.copy(
                textDirection = if (hasRtlArabicScript) TextDirection.Rtl else TextDirection.Ltr
            ),
            modifier = Modifier.padding(
                horizontal = config.horizontalPaddingDp.dp,
                vertical = calculatedVerticalPadding
            )
        )
    }
}

/**
 * Extracts custom highlight markup tags like [important:exceptions] or [text]{amber}.
 */
private fun parseInlineTags(originalText: String): List<HighlightSegment> {
    val segments = mutableListOf<HighlightSegment>()
    val combinedRegex = Regex(
        "(\\[([^\\]]+)\\]\\{([a-zA-Z_0-9]+)\\})|" +
        "(\\[([a-zA-Z_0-9]+):([^\\]]+)\\])|" +
        "(<highlight\\s+type=\"([^\"]+)\">([^<]+)</highlight>)|" +
        "(==([^=]+)==)"
    )

    var currentIndex = 0
    val matches = combinedRegex.findAll(originalText).toList()

    for (match in matches) {
        val matchRange = match.range

        // Add preceding plain text
        if (matchRange.first > currentIndex) {
            segments.add(
                HighlightSegment(
                    text = originalText.substring(currentIndex, matchRange.first),
                    isHighlighted = false
                )
            )
        }

        // Process matched groups
        val matchText: String
        val matchType: String

        if (match.groupValues[1].isNotEmpty()) { // [text]{type}
            matchText = match.groupValues[2]
            matchType = match.groupValues[3]
        } else if (match.groupValues[4].isNotEmpty()) { // [type:text]
            matchType = match.groupValues[5]
            matchText = match.groupValues[6]
        } else if (match.groupValues[7].isNotEmpty()) { // <highlight type="type">text</highlight>
            matchType = match.groupValues[8]
            matchText = match.groupValues[9]
        } else if (match.groupValues[10].isNotEmpty()) { // ==text==
            matchText = match.groupValues[11]
            matchType = "amber"
        } else {
            matchText = match.value
            matchType = "amber"
        }

        segments.add(
            HighlightSegment(
                text = matchText,
                isHighlighted = true,
                type = matchType
            )
        )

        currentIndex = matchRange.last + 1
    }

    if (currentIndex < originalText.length) {
        segments.add(
            HighlightSegment(
                text = originalText.substring(currentIndex),
                isHighlighted = false
            )
        )
    }

    return segments
}

/**
 * Splits plain text segments further based on exact search queries.
 */
private fun splitByQueryPhrases(
    text: String,
    queryPhrases: List<String>,
    highlightType: String
): List<HighlightSegment> {
    if (queryPhrases.isEmpty() || text.isEmpty()) {
        return listOf(HighlightSegment(text, false))
    }

    val validQueries = queryPhrases.filter { it.isNotBlank() }
    if (validQueries.isEmpty()) {
        return listOf(HighlightSegment(text, false))
    }

    // Match longer queries first
    val sortedQueries = validQueries.sortedByDescending { it.length }
    val segments = mutableListOf<HighlightSegment>()
    var currentIndex = 0
    val textLower = text.lowercase()

    while (currentIndex < text.length) {
        var earliestMatchIndex = -1
        var selectedQuery: String? = null

        for (query in sortedQueries) {
            val queryLower = query.lowercase()
            val matchIdx = textLower.indexOf(queryLower, currentIndex)
            if (matchIdx != -1) {
                if (earliestMatchIndex == -1 || matchIdx < earliestMatchIndex) {
                    earliestMatchIndex = matchIdx
                    selectedQuery = query
                }
            }
        }

        if (selectedQuery != null && earliestMatchIndex != -1) {
            if (earliestMatchIndex > currentIndex) {
                segments.add(HighlightSegment(text.substring(currentIndex, earliestMatchIndex), false))
            }
            val matchEndIndex = earliestMatchIndex + selectedQuery.length
            segments.add(HighlightSegment(text.substring(earliestMatchIndex, matchEndIndex), true, highlightType))
            currentIndex = matchEndIndex
        } else {
            segments.add(HighlightSegment(text.substring(currentIndex), false))
            break
        }
    }

    return segments
}
