package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PremiumUpgradePromptDialog(
    title: String = "Unlock Premium Content",
    featureName: String = "Premium Content",
    description: String = "This content is reserved for Premium candidates. Upgrade your plan to get unlimited access to all Units, practice MCQs, Current Affairs, and Full-Length Mock Tests.",
    onDismiss: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
                .premiumTargetAnimation(
                    target = com.example.model.AnimatableTarget.MODAL_DIALOGS,
                    index = 0
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Crown / Lock Emblem with Gradient Ring
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A), Color(0xFFF59E0B).copy(alpha = 0.2f))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0F172A),
                        border = BorderStroke(2.dp, Color(0xFFFFD700)),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Premium Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = featureName.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF92400E),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = description,
                    fontSize = 13.5.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Premium Value Highlights Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "PREMIUM INCLUDES:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                        PremiumPerkRow(icon = Icons.Default.CheckCircle, text = "Unlimited access to all Units, Topics & Sets")
                        PremiumPerkRow(icon = Icons.Default.Assignment, text = "Attempt & resume all Full-Length Mock Tests")
                        PremiumPerkRow(icon = Icons.Default.Today, text = "All Monthly Current Affairs Compilations")
                        PremiumPerkRow(icon = Icons.Default.Leaderboard, text = "State-wide percentiles, ranks & question analytics")
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Upgrade Button
                Button(
                    onClick = {
                        onDismiss()
                        onUpgradeClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF0F172A),
                                        Color(0xFF1E3A8A),
                                        Color(0xFF0F172A)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VIEW SUBSCRIPTION PLANS",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.5.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Maybe Later
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Maybe Later",
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumPerkRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF059669),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.5.sp,
            color = Color(0xFF1E293B),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PremiumLockBadge(
    modifier: Modifier = Modifier,
    text: String = "PREMIUM",
    isSmall: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Color(0xFFFFD700)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isSmall) 6.dp else 8.dp,
                vertical = if (isSmall) 2.dp else 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(if (isSmall) 11.dp else 13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = Color(0xFFFFD700),
                fontSize = if (isSmall) 9.5.sp else 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}
