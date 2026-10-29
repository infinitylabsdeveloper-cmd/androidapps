package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HighlightLocation
import com.example.model.HighlightRange
import com.example.model.HighlightPreset
import com.example.model.QuestionHighlightConfig
import com.example.util.MathAndHighlightParser
import com.example.util.MathElement
import com.example.util.ParsedParagraph

@Composable
fun MathAndHighlightText(
    text: String,
    highlights: List<HighlightRange> = emptyList(),
    location: HighlightLocation = HighlightLocation.OTHER,
    highlightConfig: QuestionHighlightConfig = QuestionHighlightConfig(),
    fontSize: TextUnit = 16.sp,
    fontFamily: FontFamily = FontFamily.Default,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    if (text.isBlank()) return

    val isHighlightActive = remember(location, highlightConfig) {
        highlightConfig.isEnabledFor(location)
    }

    val parsedParagraphs = remember(text, highlights) {
        MathAndHighlightParser.parseText(text, highlights)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        parsedParagraphs.forEach { paragraph ->
            if (paragraph.isBlockEquation) {
                // Render as centered/left block equation card
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, color.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RenderMathElementList(
                            elements = paragraph.elements,
                            fontSize = (fontSize.value * 1.05f).sp,
                            fontFamily = fontFamily,
                            color = color,
                            isHighlightActive = isHighlightActive,
                            highlightConfig = highlightConfig
                        )
                    }
                }
            } else {
                // Inline text + math elements row
                RenderFlowMathRow(
                    elements = paragraph.elements,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    color = color,
                    isHighlightActive = isHighlightActive,
                    highlightConfig = highlightConfig
                )
            }
        }
    }
}

@Composable
private fun RenderFlowMathRow(
    elements: List<MathElement>,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    color: Color,
    isHighlightActive: Boolean,
    highlightConfig: QuestionHighlightConfig
) {
    val isDark = isSystemInDarkTheme()
    val allTextSegments = remember(elements) {
        elements.isNotEmpty() && elements.all { it is MathElement.TextSegment }
    }

    if (allTextSegments) {
        val parsedFontWeight = when (highlightConfig.fontWeightName.lowercase()) {
            "bold" -> FontWeight.Bold
            "medium" -> FontWeight.Medium
            "normal" -> FontWeight.Normal
            else -> FontWeight.SemiBold
        }

        val annotatedText = remember(elements, isHighlightActive, highlightConfig, isDark, fontWeight, parsedFontWeight) {
            buildAnnotatedString {
                elements.forEach { element ->
                    val seg = element as MathElement.TextSegment
                    if (seg.isHighlighted && isHighlightActive) {
                        val preset = seg.highlightType?.let { HighlightPreset.fromName(it) }
                        val chipBg = if (preset != null) {
                            if (isDark) {
                                Color(preset.bgColorHex).copy(alpha = 0.25f).compositeOver(Color(0xFF1E293B))
                            } else {
                                Color(preset.bgColorHex)
                            }
                        } else {
                            Color(highlightConfig.bgColorHex)
                        }
                        val chipText = if (preset != null) {
                            if (isDark) {
                                Color(preset.bgColorHex)
                            } else {
                                Color(preset.textColorHex)
                            }
                        } else {
                            Color(highlightConfig.textColorHex)
                        }
                        withStyle(
                            SpanStyle(
                                color = chipText,
                                background = chipBg,
                                fontWeight = parsedFontWeight,
                                letterSpacing = 0.sp
                            )
                        ) {
                            append(seg.text)
                        }
                    } else {
                        append(seg.text)
                    }
                }
            }
        }

        Text(
            text = annotatedText,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            color = color,
            lineHeight = (fontSize.value * 1.50f).sp,
            letterSpacing = 0.sp,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = true)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 1.dp)
        )
    } else {
        // Flow layout for paragraphs containing inline math structures (fractions, powers, roots)
        OptFlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            elements.forEach { element ->
                RenderMathElement(
                    element = element,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    color = color,
                    isHighlightActive = isHighlightActive,
                    highlightConfig = highlightConfig
                )
            }
        }
    }
}

@Composable
private fun RenderMathElementList(
    elements: List<MathElement>,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    color: Color,
    isHighlightActive: Boolean,
    highlightConfig: QuestionHighlightConfig
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        elements.forEach { element ->
            RenderMathElement(
                element = element,
                fontSize = fontSize,
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                color = color,
                isHighlightActive = isHighlightActive,
                highlightConfig = highlightConfig
            )
        }
    }
}

@Composable
private fun RenderMathElement(
    element: MathElement,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    color: Color,
    isHighlightActive: Boolean,
    highlightConfig: QuestionHighlightConfig
) {
    when (element) {
        is MathElement.TextSegment -> {
            if (element.isHighlighted && isHighlightActive) {
                HighlightChip(
                    text = element.text,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    highlightType = element.highlightType,
                    config = highlightConfig
                )
            } else {
                Text(
                    text = element.text,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    color = color,
                    lineHeight = (fontSize.value * 1.50f).sp,
                    letterSpacing = 0.sp,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = true)
                    ),
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }

        is MathElement.Fraction -> {
            MathFractionView(
                numerator = element.numerator,
                denominator = element.denominator,
                fontSize = fontSize,
                fontFamily = fontFamily,
                color = color,
                isHighlightActive = isHighlightActive && element.isHighlighted,
                highlightConfig = highlightConfig
            )
        }

        is MathElement.Power -> {
            MathPowerView(
                base = element.base,
                exponent = element.exponent,
                fontSize = fontSize,
                fontFamily = fontFamily,
                color = color,
                isHighlightActive = isHighlightActive && element.isHighlighted,
                highlightConfig = highlightConfig
            )
        }

        is MathElement.Subscript -> {
            MathSubscriptView(
                base = element.base,
                sub = element.sub,
                fontSize = fontSize,
                fontFamily = fontFamily,
                color = color,
                isHighlightActive = isHighlightActive && element.isHighlighted,
                highlightConfig = highlightConfig
            )
        }

        is MathElement.Root -> {
            MathRootView(
                radicand = element.radicand,
                fontSize = fontSize,
                fontFamily = fontFamily,
                color = color,
                isHighlightActive = isHighlightActive && element.isHighlighted,
                highlightConfig = highlightConfig
            )
        }

        is MathElement.Group -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = element.openBracket,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                element.elements.forEach { child ->
                    RenderMathElement(
                        element = child,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        color = color,
                        isHighlightActive = isHighlightActive,
                        highlightConfig = highlightConfig
                    )
                }
                Text(
                    text = element.closeBracket,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }

        is MathElement.ExpressionRow -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                element.elements.forEach { child ->
                    RenderMathElement(
                        element = child,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        color = color,
                        isHighlightActive = isHighlightActive,
                        highlightConfig = highlightConfig
                    )
                }
            }
        }
    }
}

@Composable
fun MathFractionView(
    numerator: MathElement,
    denominator: MathElement,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    color: Color,
    isHighlightActive: Boolean,
    highlightConfig: QuestionHighlightConfig
) {
    val reducedSize = (fontSize.value * 0.88f).sp
    val barColor = if (isHighlightActive) Color(highlightConfig.textColorHex) else color
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isHighlightActive) Color(highlightConfig.bgColorHex) else Color.Transparent,
        modifier = Modifier.padding(horizontal = 2.5.dp, vertical = 1.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .padding(horizontal = 4.dp, vertical = 1.5.dp)
        ) {
            RenderMathElement(
                element = numerator,
                fontSize = reducedSize,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                color = barColor,
                isHighlightActive = false,
                highlightConfig = highlightConfig
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
                    .height(1.5.dp)
                    .background(barColor.copy(alpha = 0.9f), RoundedCornerShape(1.dp))
            )
            RenderMathElement(
                element = denominator,
                fontSize = reducedSize,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                color = barColor,
                isHighlightActive = false,
                highlightConfig = highlightConfig
            )
        }
    }
}

@Composable
fun MathPowerView(
    base: MathElement,
    exponent: MathElement,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    color: Color,
    isHighlightActive: Boolean,
    highlightConfig: QuestionHighlightConfig
) {
    val expSize = (fontSize.value * 0.72f).sp
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.padding(horizontal = 1.dp)
    ) {
        RenderMathElement(
            element = base,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            color = color,
            isHighlightActive = isHighlightActive,
            highlightConfig = highlightConfig
        )
        Box(
            modifier = Modifier.offset(y = (- (fontSize.value * 0.38f)).dp)
        ) {
            RenderMathElement(
                element = exponent,
                fontSize = expSize,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                color = color,
                isHighlightActive = isHighlightActive,
                highlightConfig = highlightConfig
            )
        }
    }
}

@Composable
fun MathSubscriptView(
    base: MathElement,
    sub: MathElement,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    color: Color,
    isHighlightActive: Boolean,
    highlightConfig: QuestionHighlightConfig
) {
    val subSize = (fontSize.value * 0.72f).sp
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.padding(horizontal = 1.dp)
    ) {
        RenderMathElement(
            element = base,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            color = color,
            isHighlightActive = isHighlightActive,
            highlightConfig = highlightConfig
        )
        Box(
            modifier = Modifier.offset(y = (fontSize.value * 0.22f).dp)
        ) {
            RenderMathElement(
                element = sub,
                fontSize = subSize,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                color = color,
                isHighlightActive = isHighlightActive,
                highlightConfig = highlightConfig
            )
        }
    }
}

@Composable
fun MathRootView(
    radicand: MathElement,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    color: Color,
    isHighlightActive: Boolean,
    highlightConfig: QuestionHighlightConfig
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Text(
            text = "√",
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Box(
            modifier = Modifier
                .drawBehind {
                    drawLine(
                        color = color,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
                .padding(top = 2.dp, start = 2.dp, end = 2.dp)
        ) {
            RenderMathElement(
                element = radicand,
                fontSize = fontSize,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                color = color,
                isHighlightActive = isHighlightActive,
                highlightConfig = highlightConfig
            )
        }
    }
}

@Composable
fun HighlightChip(
    text: String,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    highlightType: String?,
    config: QuestionHighlightConfig
) {
    val preset = highlightType?.let { HighlightPreset.fromName(it) }
    val isSystemInDark = isSystemInDarkTheme()

    val chipBg = if (preset != null) {
        if (isSystemInDark) {
            Color(preset.bgColorHex).copy(alpha = 0.22f).compositeOver(Color(0xFF1E293B))
        } else {
            Color(preset.bgColorHex)
        }
    } else {
        Color(config.bgColorHex)
    }

    val chipText = if (preset != null) {
        if (isSystemInDark) {
            Color(preset.bgColorHex)
        } else {
            Color(preset.textColorHex)
        }
    } else {
        Color(config.textColorHex)
    }

    val borderStroke = if (preset != null) {
        val borderColor = if (isSystemInDark) Color(preset.borderColorHex).copy(alpha = 0.5f) else Color(preset.borderColorHex)
        BorderStroke(1.dp, borderColor)
    } else if (config.hasBorder) {
        BorderStroke(config.borderWidthDp.dp, Color(config.borderColorHex))
    } else {
        null
    }

    val parsedFontWeight = when (config.fontWeightName.lowercase()) {
        "bold" -> FontWeight.Bold
        "medium" -> FontWeight.Medium
        "normal" -> FontWeight.Normal
        else -> FontWeight.SemiBold
    }

    Surface(
        shape = RoundedCornerShape(config.cornerRadiusDp.dp),
        color = chipBg,
        border = borderStroke,
        modifier = Modifier.padding(horizontal = 1.dp, vertical = 1.dp)
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = parsedFontWeight,
            color = chipText,
            lineHeight = (fontSize.value * 1.45f).sp,
            letterSpacing = 0.sp,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = true)
            ),
            modifier = Modifier.padding(
                horizontal = config.horizontalPaddingDp.dp,
                vertical = config.verticalPaddingDp.dp
            )
        )
    }
}

/**
 * Flow Row container for wrapping math & text tokens seamlessly across lines
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptFlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable () -> Unit
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = Arrangement.Center
    ) {
        content()
    }
}
