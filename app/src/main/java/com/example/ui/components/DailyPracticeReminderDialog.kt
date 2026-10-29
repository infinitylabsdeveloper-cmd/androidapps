package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.db.AppDatabase
import com.example.repository.AppConfigRepository
import com.example.util.NotificationSettingsManager
import com.example.worker.NotificationScheduler
import kotlinx.coroutines.launch

@Composable
fun DailyPracticeReminderDialog(
    onDismissRequest: () -> Unit,
    onSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var isReminderEnabled by remember {
        mutableStateOf(NotificationSettingsManager.isDailyPracticeReminderEnabled(context))
    }
    var selectedTarget by remember {
        mutableIntStateOf(NotificationSettingsManager.getDailyQuestionTarget(context))
    }
    var customTargetText by remember {
        mutableStateOf("")
    }
    var isCustomTargetSelected by remember {
        mutableStateOf(selectedTarget !in listOf(15, 25, 30, 50, 75, 100))
    }

    var selectedHour by remember {
        mutableIntStateOf(NotificationSettingsManager.getReminderHour(context))
    }
    var selectedMinute by remember {
        mutableIntStateOf(NotificationSettingsManager.getReminderMinute(context))
    }

    var soundEnabled by remember {
        mutableStateOf(NotificationSettingsManager.isNotificationSoundEnabled(context))
    }
    var vibrationEnabled by remember {
        mutableStateOf(NotificationSettingsManager.isNotificationVibrationEnabled(context))
    }

    // Read current user progress from Room
    var answeredToday by remember { mutableIntStateOf(0) }
    var streakDays by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        val activeConfig = AppConfigRepository.getInstance().getCurrentConfig()
        val db = AppDatabase.getDatabase(context)
        val entity = db.userProgressDao().getUserProgressDirect(activeConfig.appId)
        answeredToday = entity?.dailyQuestionsAnswered ?: 0
        streakDays = entity?.streakDays ?: 1
    }

    val targetOptions = listOf(15, 25, 30, 50, 75, 100)
    val timePresets = listOf(
        Triple("07:00 AM", 7, 0),
        Triple("01:00 PM", 13, 0),
        Triple("08:00 PM", 20, 0),
        Triple("09:30 PM", 21, 30)
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Daily Practice Quota",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "WorkManager Scheduled Reminders",
                                fontSize = 11.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Current Today Progress Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Today's Target Progress",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }

                                Surface(
                                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "🔥 $streakDays Day Streak",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val effectiveTarget = if (isCustomTargetSelected) {
                                customTargetText.toIntOrNull() ?: selectedTarget
                            } else selectedTarget

                            val progressFraction = if (effectiveTarget > 0) {
                                (answeredToday.toFloat() / effectiveTarget).coerceIn(0f, 1f)
                            } else 0f

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF38BDF8),
                                trackColor = Color(0xFF334155)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$answeredToday / $effectiveTarget Questions Solved",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "${(progressFraction * 100).toInt()}% Done",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (progressFraction >= 1f) Color(0xFF10B981) else Color(0xFF38BDF8)
                                )
                            }
                        }
                    }

                    // Toggle Master Reminder
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (isReminderEnabled) Color(0xFF0284C7).copy(alpha = 0.5f) else Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable Daily Practice Reminder",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isReminderEnabled) "Notification scheduled daily via WorkManager" else "Reminders paused",
                                    fontSize = 11.sp,
                                    color = if (isReminderEnabled) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                )
                            }

                            Switch(
                                checked = isReminderEnabled,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    isReminderEnabled = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF38BDF8),
                                    checkedTrackColor = Color(0xFF0284C7).copy(alpha = 0.4f),
                                    uncheckedThumbColor = Color(0xFF94A3B8),
                                    uncheckedTrackColor = Color(0xFF334155)
                                )
                            )
                        }
                    }

                    // Section: Daily Question Target
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Daily Question Target",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Question Target Chips Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            targetOptions.take(3).forEach { count ->
                                val isSelected = !isCustomTargetSelected && selectedTarget == count
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clickable {
                                            selectedTarget = count
                                            isCustomTargetSelected = false
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$count Qs",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            targetOptions.drop(3).forEach { count ->
                                val isSelected = !isCustomTargetSelected && selectedTarget == count
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clickable {
                                            selectedTarget = count
                                            isCustomTargetSelected = false
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$count Qs",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                        )
                                    }
                                }
                            }
                        }

                        // Custom Target Input
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCustomTargetSelected = true },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCustomTargetSelected) Color(0xFF1E293B) else Color(0xFF1E293B).copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isCustomTargetSelected) Color(0xFF38BDF8) else Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Custom Target Count:",
                                    fontSize = 12.sp,
                                    color = if (isCustomTargetSelected) Color.White else Color(0xFF94A3B8)
                                )

                                OutlinedTextField(
                                    value = customTargetText,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() } && input.length <= 4) {
                                            customTargetText = input
                                            isCustomTargetSelected = true
                                        }
                                    },
                                    placeholder = { Text("e.g. 40", fontSize = 12.sp, color = Color(0xFF64748B)) },
                                    modifier = Modifier.width(90.dp).height(48.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF475569)
                                    )
                                )
                            }
                        }
                    }

                    // Section: Reminder Time
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Reminder Schedule Time",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            timePresets.forEach { (label, hr, min) ->
                                val isSelected = selectedHour == hr && selectedMinute == min
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clickable {
                                            selectedHour = hr
                                            selectedMinute = min
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            fontSize = 10.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                        )
                                    }
                                }
                            }
                        }

                        // Selected Time Display Badge
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Scheduled Daily at:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = NotificationSettingsManager.formatTime(selectedHour, selectedMinute),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }
                    }

                    // Section: Notification Preferences (Sound & Vibration)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(text = "Play Sound", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                            }
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    soundEnabled = it
                                },
                                modifier = Modifier.scale(0.85f),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFFFFD700),
                                    checkedTrackColor = Color(0xFFFFD700).copy(alpha = 0.3f),
                                    uncheckedThumbColor = Color(0xFF94A3B8),
                                    uncheckedTrackColor = Color(0xFF334155)
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(text = "Vibrate on Alert", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                            }
                            Switch(
                                checked = vibrationEnabled,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    vibrationEnabled = it
                                },
                                modifier = Modifier.scale(0.85f),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFFFFD700),
                                    checkedTrackColor = Color(0xFFFFD700).copy(alpha = 0.3f),
                                    uncheckedThumbColor = Color(0xFF94A3B8),
                                    uncheckedTrackColor = Color(0xFF334155)
                                )
                            )
                        }
                    }

                    // Test Notification Button
                    OutlinedButton(
                        onClick = {
                            val target = if (isCustomTargetSelected) {
                                customTargetText.toIntOrNull() ?: selectedTarget
                            } else selectedTarget

                            // Save target first so worker picks up the preview value
                            NotificationSettingsManager.saveDailyQuestionTarget(context, target)
                            NotificationSettingsManager.saveNotificationSoundEnabled(context, soundEnabled)
                            NotificationSettingsManager.saveNotificationVibrationEnabled(context, vibrationEnabled)
                            NotificationSettingsManager.saveDailyPracticeReminderEnabled(context, true)

                            NotificationScheduler.triggerImmediatePracticeReminder(context)
                            Toast.makeText(context, "Testing Notification: Check your status bar! 🎯", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Send Test Notification Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8))
                    ) {
                        Text(text = "Cancel", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val finalTarget = if (isCustomTargetSelected) {
                                (customTargetText.toIntOrNull() ?: selectedTarget).coerceAtLeast(5)
                            } else selectedTarget

                            // Persist settings
                            NotificationSettingsManager.saveDailyPracticeReminderEnabled(context, isReminderEnabled)
                            NotificationSettingsManager.saveDailyQuestionTarget(context, finalTarget)
                            NotificationSettingsManager.saveReminderTime(context, selectedHour, selectedMinute)
                            NotificationSettingsManager.saveNotificationSoundEnabled(context, soundEnabled)
                            NotificationSettingsManager.saveNotificationVibrationEnabled(context, vibrationEnabled)

                            // Update Room database user progress daily target
                            scope.launch {
                                val activeConfig = AppConfigRepository.getInstance().getCurrentConfig()
                                val db = AppDatabase.getDatabase(context)
                                db.userProgressDao().updateDailyTarget(
                                    appId = activeConfig.appId,
                                    target = finalTarget,
                                    timestamp = System.currentTimeMillis()
                                )
                            }

                            // Schedule WorkManager
                            if (isReminderEnabled) {
                                NotificationScheduler.scheduleDailyPracticeReminder(context, replaceExisting = true)
                                Toast.makeText(
                                    context,
                                    "Daily Reminder scheduled at ${NotificationSettingsManager.formatTime(selectedHour, selectedMinute)} for $finalTarget MCQs!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                NotificationScheduler.cancelDailyPracticeReminder(context)
                                Toast.makeText(context, "Daily Practice Reminder turned off.", Toast.LENGTH_SHORT).show()
                            }

                            onSaved()
                            onDismissRequest()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier.weight(1.5f).height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Save & Schedule", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.padding(0.dp) // placeholder or standard scaling modifier
)
