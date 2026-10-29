package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.QuestionReportModel
import com.example.ui.components.AppBadgeChip
import com.example.ui.components.AppSpecificContextBar
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminQuestionReportsTab() {
    var selectedAppId by remember { mutableStateOf(SampleData.currentActiveAppId.value) }

    // Strict App Isolation: Filter reports strictly by selectedAppId
    val appReports = SampleData.questionReportsState.filter { it.appId == selectedAppId }.reversed()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Selector Context Bar (Mandatory Requirement 11)
        item {
            AppSpecificContextBar(
                selectedAppId = selectedAppId,
                onAppSelected = { newAppId ->
                    selectedAppId = newAppId
                    SampleData.setActiveApp(newAppId)
                }
            )
        }

        item {
            Column {
                Text(
                    text = "Question Reports & Flags",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Review and address problematic questions reported by users in this app",
                    fontSize = 12.5.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        if (appReports.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No pending question reports for ${SampleData.appsListState.find { it.id == selectedAppId }?.name ?: "this app"}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        } else {
            items(appReports) { report ->
                ReportCard(report)
            }
        }
    }
}

@Composable
private fun ReportCard(report: QuestionReportModel) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(report.reportedAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Status, App Badge and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = report.status,
                                color = Color(0xFFB91C1C),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    AppBadgeChip(appId = report.appId)
                }
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reason Section
            Surface(
                color = Color(0xFFFFF7ED),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Report,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "User's Reason:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9A3412)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.reportReason,
                        fontSize = 13.sp,
                        color = Color(0xFF7C2D12),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Context: Subject > Unit > Topic
            Text(
                text = "${report.subjectName} › ${report.unitName} › ${report.topicName}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2563EB)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Question Text
            Text(
                text = report.questionText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Options Preview
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                report.options.forEach { (key, value) ->
                    val isCorrect = key == report.correctOption
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCorrect) Color(0xFFF0FDF4) else Color(0xFFF8FAFC))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$key.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) Color(0xFF16A34A) else Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = value,
                            fontSize = 12.sp,
                            color = if (isCorrect) Color(0xFF15803D) else Color(0xFF334155),
                            modifier = Modifier.weight(1f)
                        )
                        if (isCorrect) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer: Reporter and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "By: ${report.reporterEmail}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { SampleData.questionReportsState.remove(report) },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF64748B))
                    ) {
                        Text("Dismiss", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { /* Navigate to Edit Question or similar */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Edit Question", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
