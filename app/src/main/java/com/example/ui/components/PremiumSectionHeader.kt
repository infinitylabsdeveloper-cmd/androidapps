package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class HeaderLevel { SUBJECT, UNIT, TOPIC, SET, SUBTOPIC }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSectionHeader(
    title: String = "",
    level: HeaderLevel = HeaderLevel.SUBJECT,
    onBack: () -> Unit,
    onNavigateToHome: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    rightAction: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .premiumTargetAnimation(
                target = com.example.model.AnimatableTarget.TOP_APP_BAR_HEADER,
                index = 0
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Navigation Action Buttons: Back (Previous screen) + Direct Home Shortcut
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Back Action: Return to immediately previous content screen
            PremiumBackButton(onClick = onBack)

            // 2. Direct Home Shortcut
            if (onNavigateToHome != null) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                    modifier = Modifier
                        .size(40.dp)
                        .premiumPress(target = com.example.model.AnimatableTarget.BUTTONS_CTAS) {
                            onNavigateToHome()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Go to Home",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (rightAction != null) {
            rightAction()
        }
    }
}

@Composable
fun StatBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .premiumTargetAnimation(
                target = com.example.model.AnimatableTarget.STATISTICS_BADGES_COUNTERS,
                index = 0
            )
            .background(Color(0xFFF8FAFC), RoundedCornerShape(if (isCompact) 8.dp else 12.dp))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.2f)), RoundedCornerShape(if (isCompact) 8.dp else 12.dp))
            .padding(horizontal = if (isCompact) 4.dp else 8.dp, vertical = if (isCompact) 4.dp else 10.dp)
    ) {
        Text(
            text = value,
            style = if (isCompact) MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black) else MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
            color = color
        )
        if (!isCompact) {
            Spacer(modifier = Modifier.height(2.dp))
        }
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = if (isCompact) 9.sp else 11.sp),
            color = Color(0xFF64748B)
        )
    }
}
