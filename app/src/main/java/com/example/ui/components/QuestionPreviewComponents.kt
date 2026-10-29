package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HighlightRange
import com.example.model.Question

@Composable
fun QuestionFormulaLivePreviewCard(
    questionText: String,
    optionA: String,
    optionB: String,
    optionC: String,
    optionD: String,
    correctOption: String,
    explanationText: String,
    highlightsText: String,
    title: String = "✨ Live Mathematical Formula & Question Preview"
) {
    val highlightRanges = remember(highlightsText) {
        highlightsText.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { HighlightRange(text = it) }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.5.dp, Color(0xFF6366F1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Functions,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF312E81)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEEF2FF)
                ) {
                    Text(
                        text = "Real-time Render",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4338CA),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE0E7FF))

            // Question Display
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Question Formula Preview:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        MathAndHighlightText(
                            text = questionText.ifBlank { "(Type formula in question box above to preview...)" },
                            highlights = highlightRanges,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            // Options Display
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Options Preview:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                listOf("A" to optionA, "B" to optionB, "C" to optionC, "D" to optionD).forEach { (label, optText) ->
                    val isCorrect = label.equals(correctOption, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCorrect) Color(0xFFF0FDF4) else Color.White,
                        border = BorderStroke(1.dp, if (isCorrect) Color(0xFF86EFAC) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isCorrect) Color(0xFF16A34A) else Color(0xFF94A3B8)
                            ) {
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                MathAndHighlightText(
                                    text = optText.ifBlank { "Option $label" },
                                    highlights = highlightRanges,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }

            // Explanation Display
            if (explanationText.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Explanation Formula Preview:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7).copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "💡 Solution & Formula Steps:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            MathAndHighlightText(
                                text = explanationText,
                                highlights = highlightRanges,
                                fontSize = 13.sp,
                                color = Color(0xFF78350F)
                            )
                        }
                    }
                }
            }
        }
    }
}
