package com.example.util

import com.example.model.HighlightRange
import java.util.regex.Pattern

sealed class MathElement {
    data class TextSegment(val text: String, val isHighlighted: Boolean = false, val highlightType: String? = null) : MathElement()
    data class Fraction(
        val numerator: MathElement,
        val denominator: MathElement,
        val isHighlighted: Boolean = false,
        val highlightType: String? = null
    ) : MathElement()
    data class Power(
        val base: MathElement,
        val exponent: MathElement,
        val isHighlighted: Boolean = false,
        val highlightType: String? = null
    ) : MathElement()
    data class Subscript(
        val base: MathElement,
        val sub: MathElement,
        val isHighlighted: Boolean = false,
        val highlightType: String? = null
    ) : MathElement()
    data class Root(
        val degree: String? = null,
        val radicand: MathElement,
        val isHighlighted: Boolean = false,
        val highlightType: String? = null
    ) : MathElement()
    data class Group(
        val elements: List<MathElement>,
        val openBracket: String = "(",
        val closeBracket: String = ")",
        val isHighlighted: Boolean = false,
        val highlightType: String? = null
    ) : MathElement()
    data class ExpressionRow(
        val elements: List<MathElement>,
        val isHighlighted: Boolean = false,
        val highlightType: String? = null
    ) : MathElement()
}

data class ParsedParagraph(
    val elements: List<MathElement>,
    val isBlockEquation: Boolean = false
)

object MathAndHighlightParser {

    private val LATEX_SYMBOL_MAP = mapOf(
        "\\alpha" to "α", "\\beta" to "β", "\\gamma" to "γ", "\\delta" to "δ",
        "\\epsilon" to "ε", "\\zeta" to "ζ", "\\eta" to "η", "\\theta" to "θ",
        "\\iota" to "ι", "\\kappa" to "κ", "\\lambda" to "λ", "\\mu" to "μ",
        "\\nu" to "ν", "\\xi" to "ξ", "\\pi" to "π", "\\rho" to "ρ",
        "\\sigma" to "σ", "\\tau" to "τ", "\\upsilon" to "υ", "\\phi" to "φ",
        "\\chi" to "χ", "\\psi" to "ψ", "\\omega" to "ω",
        "\\Gamma" to "Γ", "\\Delta" to "Δ", "\\Theta" to "Θ", "\\Lambda" to "Λ",
        "\\Xi" to "Ξ", "\\Pi" to "Π", "\\Sigma" to "Σ", "\\Upsilon" to "Υ",
        "\\Phi" to "Φ", "\\Psi" to "Ψ", "\\Omega" to "Ω",
        "\\sum" to "∑", "\\prod" to "∏", "\\int" to "∫", "\\infty" to "∞",
        "\\lim" to "lim", "\\le" to "≤", "\\leq" to "≤", "\\ge" to "≥",
        "\\geq" to "≥", "\\ne" to "≠", "\\neq" to "≠", "\\approx" to "≈",
        "\\equiv" to "≡", "\\times" to "×", "\\div" to "÷", "\\pm" to "±",
        "\\mp" to "∓", "\\cdot" to "·", "\\to" to "→", "\\rightarrow" to "→",
        "\\Rightarrow" to "⇒", "\\leftarrow" to "←", "\\Leftarrow" to "⇐",
        "\\leftrightarrow" to "↔", "\\Leftrightarrow" to "⇔",
        "\\forall" to "∀", "\\exists" to "∃", "\\because" to "∵", "\\therefore" to "∴",
        "\\degree" to "°", "^\\circ" to "°", "\\partial" to "∂", "\\nabla" to "∇",
        "\\cap" to "∩", "\\cup" to "∪", "\\in" to "∈", "\\notin" to "∉",
        "\\subset" to "⊂", "\\subseteq" to "⊆", "\\emptyset" to "∅"
    )

    data class HighlightInterval(val start: Int, val end: Int, val type: String)

    fun parseText(text: String, highlights: List<HighlightRange>): List<ParsedParagraph> {
        if (text.isBlank()) return emptyList()

        val normalizedText = normalizeLatexAndMath(text)
        val highlightIntervals = computeHighlightIntervals(normalizedText, highlights)

        val lines = normalizedText.split("\n")
        val resultParagraphs = mutableListOf<ParsedParagraph>()

        var currentOffset = 0
        for (line in lines) {
            val lineLength = line.length
            val lineStart = currentOffset
            val lineEnd = lineStart + lineLength

            if (line.trim().isEmpty()) {
                currentOffset += lineLength + 1
                continue
            }

            val isBlock = isLineBlockEquation(line)
            val lineHighlights = highlightIntervals
                .filter { it.start < lineEnd && it.end > lineStart }
                .map {
                    HighlightInterval(
                        start = (it.start - lineStart).coerceAtLeast(0),
                        end = (it.end - lineStart).coerceAtMost(lineLength),
                        type = it.type
                    )
                }

            val cleanLine = line.trim()
                .removePrefix("$$").removeSuffix("$$")
                .removePrefix("\\[").removeSuffix("\\]")

            val parsedRow = parseMathAndTextString(cleanLine, lineHighlights, 0)
            resultParagraphs.add(ParsedParagraph(elements = parsedRow, isBlockEquation = isBlock))

            currentOffset += lineLength + 1
        }

        return resultParagraphs
    }

    private fun isIndicCombiningOrMatra(ch: Char): Boolean {
        val code = ch.code
        val type = Character.getType(ch)
        if (type == Character.NON_SPACING_MARK.toInt() ||
            type == Character.COMBINING_SPACING_MARK.toInt() ||
            type == Character.ENCLOSING_MARK.toInt()
        ) {
            return true
        }
        // Telugu combining signs, matras, virama, and length marks
        if (code in 0x0C00..0x0C04 || code in 0x0C3C..0x0C56 || code in 0x0C62..0x0C63) {
            return true
        }
        return false
    }

    private fun isVirama(ch: Char): Boolean {
        val code = ch.code
        return code == 0x0C4D || code == 0x094D || code == 0x0CCD || code == 0x0BCD || code == 0x0D4D || code == 0x09CD || code == 0x0A4D || code == 0x0ACD || code == 0x0B4D
    }

    private fun snapToGraphemeBoundary(text: String, index: Int, isStart: Boolean): Int {
        if (text.isEmpty()) return 0
        var idx = index.coerceIn(0, text.length)
        if (isStart) {
            while (idx > 0 && idx < text.length && (isIndicCombiningOrMatra(text[idx]) || isVirama(text[idx - 1]))) {
                idx--
            }
        } else {
            while (idx < text.length && (isIndicCombiningOrMatra(text[idx]) || (idx > 0 && isVirama(text[idx - 1])))) {
                idx++
            }
        }
        return idx
    }

    private fun normalizeLatexAndMath(input: String): String {
        var res = input
        res = res.replace(Regex("\\\\(text|mathrm|mathbf|mathsf|it)\\{([^}]+)\\}")) { it.groupValues[2] }
        res = res.replace("\\,", " ").replace("\\;", " ").replace("\\quad", " ")
        res = res.replace("\\left(", "(").replace("\\right)", ")")
        res = res.replace("\\left[", "[").replace("\\right]", "]")

        for ((macro, symbol) in LATEX_SYMBOL_MAP) {
            res = res.replace(macro, symbol)
        }

        res = res.replace(">=", " ≥ ")
            .replace("<=", " ≤ ")
            .replace("!=", " ≠ ")
            .replace("~=", " ≈ ")
            .replace("+-", " ± ")

        return res
    }

    private fun computeHighlightIntervals(text: String, highlights: List<HighlightRange>): List<HighlightInterval> {
        if (highlights.isEmpty() || text.isEmpty()) return emptyList()

        val intervals = mutableListOf<HighlightInterval>()
        val textLower = text.lowercase()

        for (hl in highlights) {
            val hasValidIndices = hl.startIndex in 0 until text.length &&
                hl.endIndex in (hl.startIndex + 1)..text.length
            val indexMatchesPhrase = hasValidIndices && (
                hl.text.isBlank() ||
                    text.substring(hl.startIndex, hl.endIndex).trim().equals(hl.text.trim(), ignoreCase = true)
                )

            if (indexMatchesPhrase) {
                val safeStart = snapToGraphemeBoundary(text, hl.startIndex, isStart = true)
                val safeEnd = snapToGraphemeBoundary(text, hl.endIndex, isStart = false)
                if (safeEnd > safeStart) {
                    intervals.add(HighlightInterval(safeStart, safeEnd, hl.type))
                }
            } else if (hl.text.isNotBlank()) {
                val phrase = hl.text.trim().lowercase()
                if (phrase.isNotEmpty()) {
                    var startIndex = 0
                    while (startIndex < textLower.length) {
                        val idx = textLower.indexOf(phrase, startIndex)
                        if (idx == -1) break
                        val safeStart = snapToGraphemeBoundary(text, idx, isStart = true)
                        val safeEnd = snapToGraphemeBoundary(text, idx + phrase.length, isStart = false)
                        if (safeEnd > safeStart) {
                            intervals.add(HighlightInterval(safeStart, safeEnd, hl.type))
                        }
                        startIndex = idx + phrase.length
                    }
                }
            }
        }

        intervals.sortBy { it.start }
        val merged = mutableListOf<HighlightInterval>()
        for (interval in intervals) {
            if (merged.isEmpty()) {
                merged.add(interval)
            } else {
                val last = merged.last()
                if (interval.start <= last.end) {
                    merged[merged.size - 1] = HighlightInterval(last.start, maxOf(last.end, interval.end), last.type)
                } else {
                    merged.add(interval)
                }
            }
        }
        return merged
    }

    private fun isLineBlockEquation(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.startsWith("$$") && trimmed.endsWith("$$")) return true
        if (trimmed.startsWith("\\[") && trimmed.endsWith("\\]")) return true
        return false
    }

    private fun parseMathAndTextString(
        text: String,
        lineHighlights: List<HighlightInterval>,
        offset: Int
    ): List<MathElement> {
        val elements = mutableListOf<MathElement>()
        var i = 0
        val len = text.length

        var currentTextStart = 0

        while (i < len) {
            if (text.startsWith("\\frac", i) || text.startsWith("\\dfrac", i) || text.startsWith("\\tfrac", i)) {
                val cmdLength = if (text.startsWith("\\dfrac", i) || text.startsWith("\\tfrac", i)) 6 else 5
                val firstGroup = extractCurlyBraceGroup(text, i + cmdLength)
                if (firstGroup != null) {
                    val secondGroup = extractCurlyBraceGroup(text, firstGroup.second)
                    if (secondGroup != null) {
                        if (i > currentTextStart) {
                            val chunk = text.substring(currentTextStart, i)
                            elements.addAll(splitTextWithHighlights(chunk, offset + currentTextStart, lineHighlights))
                        }

                        val matchedHl = getRangeHighlight(offset + i, offset + secondGroup.second, lineHighlights)

                        val numElements = parseMathAndTextString(firstGroup.first, lineHighlights, offset + i + cmdLength + 1)
                        val denElements = parseMathAndTextString(secondGroup.first, lineHighlights, offset + firstGroup.second + 1)

                        val numElement = if (numElements.size == 1) numElements.first() else MathElement.ExpressionRow(numElements)
                        val denElement = if (denElements.size == 1) denElements.first() else MathElement.ExpressionRow(denElements)

                        elements.add(
                            MathElement.Fraction(
                                numerator = numElement,
                                denominator = denElement,
                                isHighlighted = matchedHl != null,
                                highlightType = matchedHl?.type
                            )
                        )

                        i = secondGroup.second
                        currentTextStart = i
                        continue
                    }
                }
            }

            if (text.startsWith("\\sqrt", i)) {
                var pos = i + 5
                var degreeStr: String? = null
                if (pos < len && text[pos] == '[') {
                    val closeBracket = text.indexOf(']', pos)
                    if (closeBracket != -1) {
                        degreeStr = text.substring(pos + 1, closeBracket)
                        pos = closeBracket + 1
                    }
                }

                val group = extractCurlyBraceGroup(text, pos)
                if (group != null) {
                    if (i > currentTextStart) {
                        val chunk = text.substring(currentTextStart, i)
                        elements.addAll(splitTextWithHighlights(chunk, offset + currentTextStart, lineHighlights))
                    }

                    val matchedHl = getRangeHighlight(offset + i, offset + group.second, lineHighlights)
                    val radElements = parseMathAndTextString(group.first, lineHighlights, offset + pos + 1)
                    val radElement = if (radElements.size == 1) radElements.first() else MathElement.ExpressionRow(radElements)

                    elements.add(
                        MathElement.Root(
                            degree = degreeStr,
                            radicand = radElement,
                            isHighlighted = matchedHl != null,
                            highlightType = matchedHl?.type
                        )
                    )

                    i = group.second
                    currentTextStart = i
                    continue
                }
            }

            if (text[i] == '^') {
                val group = extractCurlyBraceGroup(text, i + 1)
                if (group != null) {
                    val expText = group.first
                    val expElements = parseMathAndTextString(expText, lineHighlights, offset + i + 2)
                    val expElement = if (expElements.size == 1) expElements.first() else MathElement.ExpressionRow(expElements)

                    val lastElem = elements.removeLastOrNull() ?: MathElement.TextSegment("", false)
                    val matchedHl = getRangeHighlight(offset + i, offset + group.second, lineHighlights)

                    elements.add(
                        MathElement.Power(
                            base = lastElem,
                            exponent = expElement,
                            isHighlighted = matchedHl != null,
                            highlightType = matchedHl?.type
                        )
                    )

                    i = group.second
                    currentTextStart = i
                    continue
                }
            }

            if (text[i] == '_') {
                val group = extractCurlyBraceGroup(text, i + 1)
                if (group != null) {
                    val subText = group.first
                    val subElements = parseMathAndTextString(subText, lineHighlights, offset + i + 2)
                    val subElement = if (subElements.size == 1) subElements.first() else MathElement.ExpressionRow(subElements)

                    val lastElem = elements.removeLastOrNull() ?: MathElement.TextSegment("", false)
                    val matchedHl = getRangeHighlight(offset + i, offset + group.second, lineHighlights)

                    elements.add(
                        MathElement.Subscript(
                            base = lastElem,
                            sub = subElement,
                            isHighlighted = matchedHl != null,
                            highlightType = matchedHl?.type
                        )
                    )

                    i = group.second
                    currentTextStart = i
                    continue
                }
            }

            i++
        }

        if (currentTextStart < len) {
            val remainingChunk = text.substring(currentTextStart)
            elements.addAll(parseInlineTokens(remainingChunk, offset + currentTextStart, lineHighlights))
        }

        return elements
    }

    private fun extractCurlyBraceGroup(text: String, startIdx: Int): Pair<String, Int>? {
        if (startIdx >= text.length || text[startIdx] != '{') return null
        var depth = 0
        for (idx in startIdx until text.length) {
            if (text[idx] == '{') depth++
            else if (text[idx] == '}') {
                depth--
                if (depth == 0) {
                    val inner = text.substring(startIdx + 1, idx)
                    return Pair(inner, idx + 1)
                }
            }
        }
        return null
    }

    private fun parseInlineTokens(
        text: String,
        offset: Int,
        highlights: List<HighlightInterval>
    ): List<MathElement> {
        val elements = mutableListOf<MathElement>()
        var pos = 0
        val len = text.length

        // Only match explicit caret powers (x^2) or square roots (√(x+1), √x).
        // Never split normal prose on '/' slashes or native Unicode superscripts/subscripts.
        val tokenRegex = Pattern.compile(
            "([A-Za-z0-9_\\(\\)]+\\^[A-Za-z0-9_\\(\\)\\+]+)" +
            "|(√\\([^\\)]+\\)|√[0-9A-Za-z]+)"
        )

        val matcher = tokenRegex.matcher(text)
        while (matcher.find()) {
            val matchStart = snapToGraphemeBoundary(text, matcher.start(), isStart = true)
            val matchEnd = snapToGraphemeBoundary(text, matcher.end(), isStart = false)

            if (matchStart > pos) {
                val nonMathText = text.substring(pos, matchStart)
                elements.addAll(splitTextWithHighlights(nonMathText, offset + pos, highlights))
            }

            val token = text.substring(matchStart, matchEnd)
            val matchedHl = getRangeHighlight(offset + matchStart, offset + matchEnd, highlights)

            val parsedToken = parseSingleMathToken(token, matchedHl != null, matchedHl?.type)
            elements.add(parsedToken)

            pos = matchEnd
        }

        if (pos < len) {
            val remainingText = text.substring(pos)
            elements.addAll(splitTextWithHighlights(remainingText, offset + pos, highlights))
        }

        return elements
    }

    private fun parseSingleMathToken(token: String, isHighlighted: Boolean, highlightType: String?): MathElement {
        if (token.contains("^")) {
            val parts = token.split("^")
            val baseStr = parts[0].trim()
            val expStr = parts.getOrNull(1)?.trim() ?: ""
            return MathElement.Power(
                base = MathElement.TextSegment(baseStr, isHighlighted, highlightType),
                exponent = MathElement.TextSegment(expStr, isHighlighted, highlightType),
                isHighlighted = isHighlighted,
                highlightType = highlightType
            )
        }

        if (token.startsWith("√")) {
            val inner = token.substring(1).removeSurrounding("(", ")")
            return MathElement.Root(
                radicand = MathElement.TextSegment(inner, isHighlighted, highlightType),
                isHighlighted = isHighlighted,
                highlightType = highlightType
            )
        }

        return MathElement.TextSegment(token, isHighlighted, highlightType)
    }

    private fun splitTextWithHighlights(text: String, startOffset: Int, highlights: List<HighlightInterval>): List<MathElement> {
        if (highlights.isEmpty() || text.isEmpty()) {
            return listOf(MathElement.TextSegment(text, false))
        }

        val result = mutableListOf<MathElement>()
        var pos = 0
        val len = text.length

        while (pos < len) {
            val globalIndex = startOffset + pos
            val activeHl = highlights.find { globalIndex >= it.start && globalIndex < it.end }

            if (activeHl != null) {
                val rawStart = maxOf(pos, activeHl.start - startOffset)
                val rawEnd = minOf(len, activeHl.end - startOffset)
                val hlStartInText = snapToGraphemeBoundary(text, rawStart, isStart = true).coerceAtLeast(pos)
                val hlEndInText = snapToGraphemeBoundary(text, rawEnd, isStart = false).coerceIn(hlStartInText, len)

                if (hlStartInText > pos) {
                    result.add(MathElement.TextSegment(text.substring(pos, hlStartInText), false))
                }

                if (hlEndInText > hlStartInText) {
                    val highlightedChunk = text.substring(hlStartInText, hlEndInText)
                    result.add(MathElement.TextSegment(highlightedChunk, true, activeHl.type))
                    pos = hlEndInText
                } else {
                    val fallbackEnd = (pos + 1).coerceAtMost(len)
                    result.add(MathElement.TextSegment(text.substring(pos, fallbackEnd), false))
                    pos = fallbackEnd
                }
            } else {
                val nextHl = highlights
                    .filter { it.start > globalIndex }
                    .minByOrNull { it.start }

                val rawNext = if (nextHl != null) minOf(len, nextHl.start - startOffset) else len
                val nextHlStartInText = snapToGraphemeBoundary(text, rawNext, isStart = true).coerceIn(pos, len)
                val safeNext = if (nextHlStartInText > pos) nextHlStartInText else len
                val plainChunk = text.substring(pos, safeNext)
                if (plainChunk.isNotEmpty()) {
                    result.add(MathElement.TextSegment(plainChunk, false))
                }
                pos = safeNext
            }
        }

        return result
    }

    private fun getRangeHighlight(start: Int, end: Int, highlights: List<HighlightInterval>): HighlightInterval? {
        return highlights.find { start < it.end && end > it.start }
    }
}
