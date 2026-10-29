package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppConfig

@Composable
fun PracticeTimerDisplay(timeProvider: () -> Int, isDarkMode: Boolean) {
    val remainingSeconds = timeProvider()
    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    
    val alertColor = Color(0xFFEF4444)
    val normalIconColor = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF475569)
    val normalTextColor = if (isDarkMode) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    
    val iconColor = if (remainingSeconds < 300) alertColor else normalIconColor
    val textColor = if (remainingSeconds < 300) alertColor else normalTextColor

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = "Timer",
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
            fontWeight = FontWeight.Black,
            color = textColor,
            fontSize = LocalAppConfig.current.advancedFontSettings.questionFontSize.sp
        )
    }
}
