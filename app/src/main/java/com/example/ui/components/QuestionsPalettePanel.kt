package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.theme.LocalAppConfig
import com.example.model.Question

@Composable
fun QuestionsPalettePanel(
    questions: List<Question>,
    currentIndex: Int,
    userAnswers: Map<String, String>,
    markedForReviewIds: Set<String>,
    visitedIndices: Set<Int>,
    onQuestionSelected: (Int) -> Unit,
    onSubmitClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val appConfig = LocalAppConfig.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Questions",
                    fontWeight = FontWeight.Bold,
                    fontSize = appConfig.advancedFontSettings.optionFontSize.sp,
                    color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Expand",
                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(12.dp))

            // Legend Rows
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem(color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9), text = "Not Visited", border = BorderStroke(1.dp, if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)))
                    LegendItem(color = Color(0XFFDCFCE7), text = "Answered")
                    LegendItem(color = Color(0XFFF3E8FF), text = "Review")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem(color = Color(0XFFFEF3C7), text = "Ans. & Review")
                    LegendItem(color = Color.White, text = "Current", border = BorderStroke(2.dp, Color(0XFF2563EB)))
                    Spacer(modifier = Modifier.width(60.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of Numbers
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
            ) {
                itemsIndexed(questions) { idx, q ->
                    val isVisited = visitedIndices.contains(idx)
                    val isAnswered = userAnswers.containsKey(q.id)
                    val isReview = markedForReviewIds.contains(q.id)
                    val isCurrent = idx == currentIndex

                    // State color logic
                    val (bgColor, textColor, border) = when {
                        isCurrent -> Triple(Color.White, Color(0XFF2563EB), BorderStroke(2.dp, Color(0XFF2563EB)))
                        isAnswered && isReview -> Triple(Color(0XFFFEF3C7), Color(0XFFD97706), null)
                        isReview -> Triple(Color(0XFFF3E8FF), Color(0XFF7E22CE), null)
                        isAnswered -> Triple(Color(0XFFDCFCE7), Color(0XFF15803D), null)
                        !isVisited -> Triple(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9), if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B), BorderStroke(1.dp, if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)))
                        else -> Triple(if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC), if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155), null)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = bgColor,
                        border = border,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onQuestionSelected(idx) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${idx + 1}",
                                fontWeight = FontWeight.Bold,
                                fontSize = appConfig.advancedFontSettings.explanationFontSize.sp,
                                color = textColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom View All button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* View All */ }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "View All Questions",
                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = appConfig.advancedFontSettings.explanationFontSize.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String, border: BorderStroke? = null) {
    val isDark = isSystemInDarkTheme()
    val appConfig = LocalAppConfig.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = color,
            border = border,
            modifier = Modifier.size(10.dp)
        ) {}
        Text(
            text = text,
            fontSize = appConfig.advancedFontSettings.explanationFontSize.sp,
            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
            fontWeight = FontWeight.Medium
        )
    }
}
