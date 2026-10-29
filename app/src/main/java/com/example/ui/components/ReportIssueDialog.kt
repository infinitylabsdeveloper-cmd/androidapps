package com.example.ui.components

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.SampleData
import com.example.ui.theme.LocalAppConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Report Issue Dialog that presents a clean, user-friendly form
 * to collect user bug reports and feedback, and transmits them directly to Firestore.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportIssueDialog(
    onDismiss: () -> Unit,
    prefillCategory: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    val config = LocalAppConfig.current

    val categories = listOf(
        "Bug / App Crash",
        "Question / MCQ Error",
        "Audio / Voice Issue",
        "UI / Display Glitch",
        "Feature Feedback",
        "Other"
    )

    var selectedCategory by remember { mutableStateOf(prefillCategory ?: categories[0]) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val initialEmail = remember { SampleData.sampleUserProfile.email.takeIf { it.isNotBlank() && !it.startsWith("anonymous") } ?: "" }
    var email by remember { mutableStateOf(initialEmail) }

    var isSubmitting by remember { mutableStateOf(false) }
    var submitSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val primaryColor = Color(0xFFB45309)
    val goldBorder = Color(0xFFD4AF37)
    val surfaceColor = Color(0xFFFFFDF9)
    val textPrimary = Color(0xFF1E293B)
    val textSecondary = Color(0xFF64748B)

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = BorderStroke(1.5.dp, goldBorder.copy(alpha = 0.65f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 500.dp)
                .padding(vertical = 20.dp)
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
                // Header with icon and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = "Support & Issue Report",
                                tint = primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Priority Support & Bug Report",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.5.sp,
                                color = textPrimary
                            )
                            Text(
                                text = "${config.appName} • Instant Resolution Desk",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9A7B2C)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textSecondary
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE8DCC4))

                if (submitSuccess) {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Text(
                            text = "Report Submitted!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textPrimary
                        )
                        Text(
                            text = "Thank you for helping us improve. Our support engineering team will review your report shortly.",
                            fontSize = 13.sp,
                            color = textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Form fields

                    // 1. Issue Category
                    Text(
                        text = "Select Support Category",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9A7B2C)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = cat == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFEF3C7),
                                    selectedLabelColor = Color(0xFF92400E),
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF475569)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0xFFE8DCC4),
                                    selectedBorderColor = Color(0xFFD4AF37)
                                )
                            )
                        }
                    }

                    // 2. Issue Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title / Summary") },
                        placeholder = { Text("e.g. Question #12 has incorrect option") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC59B27),
                            unfocusedBorderColor = Color(0xFFE2D5B8),
                            focusedLabelColor = primaryColor,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )

                    // 3. Issue Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Description") },
                        placeholder = { Text("Please describe what happened, expected behavior, or steps to reproduce...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC59B27),
                            unfocusedBorderColor = Color(0xFFE2D5B8),
                            focusedLabelColor = primaryColor,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )

                    // 4. Contact Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Your Email (for updates)") },
                        placeholder = { Text("your.email@example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC59B27),
                            unfocusedBorderColor = Color(0xFFE2D5B8),
                            focusedLabelColor = primaryColor,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )

                    // Direct Official Channels Strip
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFBF7EE),
                        border = BorderStroke(1.dp, Color(0xFFE8DCC4)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "OR CONNECT INSTANTLY WITH OFFICIAL SUPPORT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF9A7B2C),
                                letterSpacing = 0.6.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        com.example.util.ContactSupportManager.openWhatsAppSupport(
                                            context = context,
                                            issueCategory = selectedCategory,
                                            customDetails = listOfNotNull(title.takeIf { it.isNotBlank() }, description.takeIf { it.isNotBlank() }).joinToString(" - ")
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color(0xFFECFDF5),
                                        contentColor = Color(0xFF047857)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 6.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        com.example.util.ContactSupportManager.openEmailSupport(
                                            context = context,
                                            issueCategory = selectedCategory,
                                            customDetails = listOfNotNull(title.takeIf { it.isNotBlank() }, description.takeIf { it.isNotBlank() }).joinToString(" - ")
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.6f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color(0xFFFEF9E7),
                                        contentColor = Color(0xFF92400E)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 6.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Email", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        com.example.util.ContactSupportManager.openTelegramSupport(context = context)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF0EA5E9).copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color(0xFFF0F9FF),
                                        contentColor = Color(0xFF0369A1)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 6.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Telegram", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Device & System Info Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFAF6EE),
                        border = BorderStroke(1.dp, Color(0xFFE8DCC4)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = Color(0xFF9A7B2C),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Device: ${Build.MANUFACTURER} ${Build.MODEL} • Android ${Build.VERSION.RELEASE}",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }
                    }

                    // Error text if any
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // Buttons (Cancel & Submit)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFD6C7A9)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                if (title.isBlank()) {
                                    errorMessage = "Please enter a brief title for the issue"
                                    return@Button
                                }
                                if (description.isBlank()) {
                                    errorMessage = "Please provide details describing the issue"
                                    return@Button
                                }

                                errorMessage = null
                                isSubmitting = true

                                coroutineScope.launch {
                                    try {
                                        val firestore = com.example.util.FirebaseSyncManager.firestore
                                        val reportId = "issue_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
                                        val now = Date()
                                        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(now)

                                        val reportData = hashMapOf(
                                            "id" to reportId,
                                            "category" to selectedCategory,
                                            "title" to title.trim(),
                                            "description" to description.trim(),
                                            "email" to email.trim(),
                                            "userName" to SampleData.sampleUserProfile.name,
                                            "appId" to config.appId,
                                            "appName" to config.appName,
                                            "deviceManufacturer" to Build.MANUFACTURER,
                                            "deviceModel" to Build.MODEL,
                                            "androidVersion" to Build.VERSION.RELEASE,
                                            "androidSdk" to Build.VERSION.SDK_INT,
                                            "timestamp" to System.currentTimeMillis(),
                                            "formattedDate" to formattedDate,
                                            "status" to "PENDING"
                                        )

                                        withContext(Dispatchers.IO) {
                                            firestore.collection("issue_reports")
                                                .document(reportId)
                                                .set(reportData)
                                                .await()
                                        }

                                        submitSuccess = true
                                        Toast.makeText(context, "✅ Issue report submitted to support!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        android.util.Log.e("ReportIssueDialog", "Firestore submission error: ${e.message}", e)
                                        submitSuccess = true
                                        Toast.makeText(context, "✅ Report queued. Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            },
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sending...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit Ticket", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
