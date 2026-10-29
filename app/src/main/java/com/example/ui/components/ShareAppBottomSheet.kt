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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppShareManager
import com.example.util.ShareChannel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareAppBottomSheet(
    appName: String = "APPSC Group-2",
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF475569))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Icon + Title + Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.6f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Share $appName",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Invite fellow aspirants to prepare together",
                            fontSize = 12.sp,
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

            // Play Store Link Card with 1-Tap Copy
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Official Google Play Link",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0)
                            )
                            Text(
                                text = AppShareManager.PLAY_STORE_WEB_URL,
                                fontSize = 11.sp,
                                color = Color(0xFF38BDF8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            AppShareManager.copyToClipboard(context, AppShareManager.PLAY_STORE_WEB_URL)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF38BDF8).copy(alpha = 0.2f),
                            contentColor = Color(0xFF38BDF8)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick Share Channels Grid (WhatsApp, Telegram, SMS, More Apps)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShareChannelTile(
                    modifier = Modifier.weight(1f),
                    label = "WhatsApp",
                    icon = Icons.Default.Chat,
                    bgColor = Color(0xFF25D366).copy(alpha = 0.18f),
                    borderColor = Color(0xFF25D366).copy(alpha = 0.5f),
                    tintColor = Color(0xFF25D366),
                    onClick = {
                        AppShareManager.shareApp(context, appName, ShareChannel.WHATSAPP)
                        onDismissRequest()
                    }
                )

                ShareChannelTile(
                    modifier = Modifier.weight(1f),
                    label = "Telegram",
                    icon = Icons.Default.Send,
                    bgColor = Color(0xFF0088CC).copy(alpha = 0.18f),
                    borderColor = Color(0xFF0088CC).copy(alpha = 0.5f),
                    tintColor = Color(0xFF38BDF8),
                    onClick = {
                        AppShareManager.shareApp(context, appName, ShareChannel.TELEGRAM)
                        onDismissRequest()
                    }
                )

                ShareChannelTile(
                    modifier = Modifier.weight(1f),
                    label = "SMS",
                    icon = Icons.Default.Sms,
                    bgColor = Color(0xFF8B5CF6).copy(alpha = 0.18f),
                    borderColor = Color(0xFF8B5CF6).copy(alpha = 0.5f),
                    tintColor = Color(0xFFA78BFA),
                    onClick = {
                        AppShareManager.shareApp(context, appName, ShareChannel.SMS)
                        onDismissRequest()
                    }
                )

                ShareChannelTile(
                    modifier = Modifier.weight(1f),
                    label = "More",
                    icon = Icons.Default.MoreHoriz,
                    bgColor = Color(0xFFF59E0B).copy(alpha = 0.18f),
                    borderColor = Color(0xFFF59E0B).copy(alpha = 0.5f),
                    tintColor = Color(0xFFFBBF24),
                    onClick = {
                        AppShareManager.shareApp(context, appName, ShareChannel.NATIVE_CHOOSER)
                        onDismissRequest()
                    }
                )
            }

            // Play Store Rating & Reviews Action Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        AppShareManager.openPlayStore(context)
                        onDismissRequest()
                    },
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFD700).copy(alpha = 0.15f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Rate 5 Stars on Google Play",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "4.9 ★",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD700)
                                )
                            }
                            Text(
                                text = "Support our development by leaving a 5-star review",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ShareChannelTile(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    bgColor: Color,
    borderColor: Color,
    tintColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
