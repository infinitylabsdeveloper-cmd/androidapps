package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.TopBarConfig

@Composable
fun ExactTopBarView(
    config: TopBarConfig = TopBarConfig(),
    onMenuClick: () -> Unit,
    onAppIconClick: () -> Unit,
    onTextSettingsClick: () -> Unit,
    onPremiumClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    unreadNotifications: Int = 1
) {
    if (!config.isEnabled) return

    val primaryNavy = Color(config.primaryNavyColorHex)
    val secondaryNavy = Color(config.secondaryNavyColorHex)
    val goldColor = Color(config.goldAccentColorHex)
    val solidBg = Color(config.backgroundColorHex)
    val cornerRadius = config.cornerRadiusDp.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius))
            .border(
                BorderStroke(1.dp, goldColor.copy(alpha = 0.4f)),
                RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
            ),
        color = solidBg
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius))
        ) {
            if (config.backgroundType == "IMAGE" && config.backgroundImageUrl.isNotBlank()) {
                AsyncImage(
                    model = config.backgroundImageUrl,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = config.overlayOpacity))
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(solidBg)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Group: Menu & App Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. MENU BUTTON
                        if (config.showMenu) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .shadow(2.dp, RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(BorderStroke(1.dp, goldColor.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                                    .clickable { onMenuClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = goldColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // 2. APP ICON
                        if (config.showAppIcon) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .shadow(3.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E3E62))
                                    .border(BorderStroke(1.5.dp, goldColor), CircleShape)
                                    .clickable { onAppIconClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "App Icon",
                                    tint = goldColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Center Group: EXAM TITLE & SUBTITLE CONTAINER
                    if (config.showTitle || config.showSubtitle) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .shadow(4.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF112238))
                                .border(BorderStroke(1.2.dp, goldColor.copy(alpha = 0.6f)), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (config.showTitle) {
                                    Text(
                                        text = config.title,
                                        color = goldColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        letterSpacing = 1.1.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                if (config.showSubtitle && config.subtitle.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(top = 1.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(16.dp)
                                                .height(1.dp)
                                                .background(goldColor.copy(alpha = 0.5f))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = config.subtitle,
                                            color = Color.White.copy(alpha = 0.95f),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            letterSpacing = 0.4.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(16.dp)
                                                .height(1.dp)
                                                .background(goldColor.copy(alpha = 0.5f))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Right Group: Text Settings, Premium Badge, Notifications
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 3. TEXT FONT SETTINGS ("Tt")
                        if (config.showTextSettings) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .shadow(3.dp, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.White, Color(0xFFF8FAFC))
                                        )
                                    )
                                    .border(BorderStroke(1.2.dp, goldColor), RoundedCornerShape(12.dp))
                                    .clickable { onTextSettingsClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Tt",
                                        color = primaryNavy,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // 4. PREMIUM BADGE
                        if (config.showPremium) {
                            Box(
                                modifier = Modifier
                                    .clickable { onPremiumClick() }
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp, 34.dp)
                                            .shadow(3.dp, RoundedCornerShape(8.dp))
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(secondaryNavy, primaryNavy)
                                                )
                                            )
                                            .border(BorderStroke(1.2.dp, goldColor), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = "Crown",
                                            tint = goldColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Surface(
                                        color = goldColor,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier
                                            .offset(y = (-4).dp)
                                            .shadow(2.dp, RoundedCornerShape(4.dp))
                                    ) {
                                        Text(
                                            text = config.premiumBadgeText,
                                            color = primaryNavy,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 8.sp,
                                            letterSpacing = 0.5.sp,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 5. NOTIFICATIONS BELL
                        if (config.showNotifications) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .shadow(3.dp, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.White, Color(0xFFF8FAFC))
                                        )
                                    )
                                    .border(BorderStroke(1.2.dp, goldColor), RoundedCornerShape(12.dp))
                                    .clickable { onNotificationsClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = primaryNavy,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (unreadNotifications > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(Color.Red, CircleShape)
                                                .border(BorderStroke(1.dp, Color.White), CircleShape)
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
