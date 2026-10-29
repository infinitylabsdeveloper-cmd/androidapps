package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Question
import com.example.ui.theme.LocalAppConfig
import com.example.util.ContactSupportManager

@Composable
fun FlagQuestionDialog(
    question: Question,
    onDismiss: () -> Unit,
    onSubmitReport: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedReason by remember { mutableStateOf("Incorrect Answer") }
    var customReason by remember { mutableStateOf("") }
    val appConfig = LocalAppConfig.current

    val reasons = listOf(
        "Incorrect Answer",
        "Wrong Question Text",
        "Grammar/Typo Issues",
        "Options are confusing/repeated",
        "Irrelevant/Out of Syllabus",
        "Other"
    )

    val finalReason = if (selectedReason == "Other") customReason.ifBlank { "Question Mistake" } else selectedReason
    val questionContext = "Question ID: ${question.id} | Q: ${question.questionText.take(90)}"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFFFFDF9),
            border = BorderStroke(1.5.dp, Color(0xFFD4AF37).copy(alpha = 0.65f)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 480.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFFBEB),
                                Color(0xFFFFFDF9),
                                Color(0xFFFAF6EE)
                            )
                        )
                    )
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Flag,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                "Report Question / Mistake",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.5.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                "Academic Quality & Content Support",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9A7B2C)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFBF7EE),
                    border = BorderStroke(1.dp, Color(0xFFE8DCC4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Q: ${question.questionText}",
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    reasons.forEach { reason ->
                        val isSelected = selectedReason == reason
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFFFEF9E7) else Color.White,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFFD4AF37) else Color(0xFFE8DCC4)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedReason = reason }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedReason = reason },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFFB45309),
                                        unselectedColor = Color(0xFF94A3B8)
                                    )
                                )
                                Text(
                                    text = reason,
                                    fontSize = appConfig.advancedFontSettings.optionFontSize.sp,
                                    color = if (isSelected) Color(0xFF1E293B) else Color(0xFF475569),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                if (selectedReason == "Other") {
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        placeholder = { Text("Describe the question mistake or correction...", fontSize = 12.5.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC59B27),
                            unfocusedBorderColor = Color(0xFFE2D5B8),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color(0xFF1E293B)
                        )
                    )
                }

                Button(
                    onClick = {
                        if (finalReason.isNotBlank()) {
                            onSubmitReport(finalReason)
                        }
                    },
                    enabled = selectedReason.isNotBlank() && (selectedReason != "Other" || customReason.isNotBlank()),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit to Academic Team", fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            ContactSupportManager.openWhatsAppSupport(
                                context = context,
                                issueCategory = "Report Question / Content Mistake",
                                customDetails = finalReason,
                                extraContext = questionContext
                            )
                            onSubmitReport(finalReason)
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFECFDF5),
                            contentColor = Color(0xFF047857)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            ContactSupportManager.openEmailSupport(
                                context = context,
                                issueCategory = "Report Question / Content Mistake",
                                customDetails = finalReason,
                                extraContext = questionContext
                            )
                            onSubmitReport(finalReason)
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFEF9E7),
                            contentColor = Color(0xFF92400E)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Email Support", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

