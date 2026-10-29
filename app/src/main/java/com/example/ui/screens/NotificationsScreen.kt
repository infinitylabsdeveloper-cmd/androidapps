package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import com.example.ui.components.premiumCardInteraction
import com.example.ui.components.premiumTargetAnimation
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.model.AdminNotificationModel
import com.example.model.NotificationCategory
import com.example.model.NotificationStatus
import com.example.ui.theme.AppLuminousBackground
import com.example.ui.theme.AppTextDark
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.AppTextMuted
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToDeepLink: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val allNotifications = SampleData.adminNotificationsState
    val sentNotifications = remember(allNotifications.size, allNotifications.map { it.isRead to it.status }) {
        allNotifications.filter { it.status == NotificationStatus.SENT }
    }

    val unreadCount = remember(sentNotifications) {
        sentNotifications.count { !it.isRead }
    }

    var selectedCategoryFilter by remember { mutableStateOf<NotificationCategory?>(null) }

    val filteredNotifications = remember(sentNotifications, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            sentNotifications
        } else {
            sentNotifications.filter { it.category == selectedCategoryFilter }
        }
    }

    AppLuminousBackground(screenName = "Notifications") {
        Scaffold(
            topBar = {
                // Top Bar with premium glass container
                Surface(
                    color = Color.White.copy(alpha = 0.90f),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = AppTextDark
                                )
                            }

                            IconButton(
                                onClick = onNavigateToHome,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home",
                                    tint = Color(0xFF2563EB)
                                )
                            }

                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Notifications",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Black,
                                        color = AppTextDark,
                                        letterSpacing = (-0.5).sp
                                    )

                                    if (unreadCount > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "$unreadCount New",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFDC2626),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "Important announcements & offers",
                                    fontSize = 11.5.sp,
                                    color = AppTextSecondary
                                )
                            }
                        }

                        if (unreadCount > 0) {
                            Surface(
                                onClick = {
                                    SampleData.markAllNotificationsAsRead()
                                    Toast.makeText(context, "All alerts marked as read", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.25f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Mark all as read",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Mark read",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // CATEGORY FILTER ROW WITH CHIPS
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .premiumTargetAnimation(
                            target = com.example.model.AnimatableTarget.FILTER_CHIPS_TABS,
                            index = 0
                        ),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        val isAllSelected = selectedCategoryFilter == null
                        FilterChipItem(
                            label = "All (${sentNotifications.size})",
                            isSelected = isAllSelected,
                            onClick = { selectedCategoryFilter = null }
                        )
                    }

                    items(NotificationCategory.values()) { category ->
                        val count = sentNotifications.count { it.category == category }
                        if (count > 0 || selectedCategoryFilter == category) {
                            FilterChipItem(
                                label = "${category.iconPrefix} ${category.displayName}",
                                isSelected = selectedCategoryFilter == category,
                                onClick = {
                                    selectedCategoryFilter = if (selectedCategoryFilter == category) null else category
                                }
                            )
                        }
                    }
                }

                // NOTIFICATION TILES / FEED
                if (filteredNotifications.isEmpty()) {
                    NotificationEmptyState(
                        selectedCategory = selectedCategoryFilter,
                        onClearFilter = { selectedCategoryFilter = null }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredNotifications, key = { it.id }) { item ->
                            NotificationCard(
                                notification = item,
                                onCardClick = {
                                    SampleData.markNotificationAsRead(item.id)
                                    item.deepLink?.let { link ->
                                        if (link.isNotBlank()) {
                                            onNavigateToDeepLink(link)
                                        }
                                    }
                                },
                                onDismiss = {
                                    SampleData.deleteNotification(item.id)
                                    Toast.makeText(context, "Notification dismissed", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF2563EB) else Color.White.copy(alpha = 0.90f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Color(0xFF1D4ED8) else Color(0xFFE2E8F0)
        ),
        shadowElevation = if (isSelected) 3.dp else 1.dp,
        modifier = Modifier.premiumCardInteraction(target = com.example.model.AnimatableTarget.FILTER_CHIPS_TABS)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (isSelected) Color.White else AppTextSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun NotificationCard(
    notification: AdminNotificationModel,
    onCardClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val isUnread = !notification.isRead
    val category = notification.category

    val categoryBgColor = when (category) {
        NotificationCategory.IMPORTANT -> Color(0xFFEF4444)
        NotificationCategory.EXAM_UPDATES -> Color(0xFFF59E0B)
        NotificationCategory.MOCK_TEST -> Color(0xFF3B82F6)
        NotificationCategory.CURRENT_AFFAIRS -> Color(0xFF10B981)
        NotificationCategory.SUBSCRIPTION -> Color(0xFF8B5CF6)
        NotificationCategory.OFFERS -> Color(0xFFEC4899)
        NotificationCategory.SYSTEM -> Color(0xFF64748B)
        else -> Color(0xFF06B6D4)
    }

    val iconVector = when (notification.iconName.lowercase()) {
        "siren", "alert" -> Icons.Default.Warning
        "flask", "test" -> Icons.Default.Science
        "newspaper", "news" -> Icons.Default.Newspaper
        "star", "reward" -> Icons.Default.Star
        "gift", "offer" -> Icons.Default.CardGiftcard
        "gear", "system" -> Icons.Default.Settings
        "trophy" -> Icons.Default.EmojiEvents
        else -> Icons.Default.Notifications
    }

    // High fidelity glassmorphism card structure
    Surface(
        onClick = onCardClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isUnread) Color.White else Color.White.copy(alpha = 0.85f),
        border = BorderStroke(
            width = if (isUnread) 1.5.dp else 1.dp,
            brush = if (isUnread) {
                Brush.horizontalGradient(
                    listOf(
                        categoryBgColor,
                        Color(0xFF38BDF8)
                    )
                )
            } else {
                SolidColor(Color(0xFFE2E8F0))
            }
        ),
        shadowElevation = if (isUnread) 6.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .premiumTargetAnimation(
                target = com.example.model.AnimatableTarget.LIVE_CONTENT_DASHBOARD,
                index = 0
            )
            .premiumCardInteraction(
                target = com.example.model.AnimatableTarget.LIVE_CONTENT_DASHBOARD
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category, Badge, Date & Dismiss button
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(categoryBgColor.copy(alpha = 0.15f))
                            .border(1.dp, categoryBgColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = category.displayName,
                            tint = categoryBgColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = categoryBgColor.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, categoryBgColor.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "${category.iconPrefix} ${category.displayName.uppercase()}",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = categoryBgColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                                    letterSpacing = 0.5.sp
                                )
                            }

                            if (notification.isFeatured || notification.priority == "HIGH") {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFD97706).copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "URGENT",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = formatRelativeTime(notification.sentAt ?: System.currentTimeMillis()),
                            fontSize = 11.sp,
                            color = AppTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB))
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = AppTextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notification Title
            Text(
                text = notification.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AppTextDark,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Notification Body Message
            Text(
                text = notification.message,
                fontSize = 13.5.sp,
                color = AppTextSecondary,
                lineHeight = 19.sp,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )

            // Optional high-definition image card attachments
            if (!notification.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = notification.imageUrl,
                        contentDescription = "Notification Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Optional Dynamic deep link Call-to-Action button
            val buttonText = notification.actionText ?: getActionTextForDeepLink(notification.deepLink)
            if (!buttonText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = onCardClick,
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    border = BorderStroke(
                        1.2.dp,
                        Brush.horizontalGradient(
                            listOf(Color(0xFF2563EB), Color(0xFF06B6D4))
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF2563EB).copy(alpha = 0.08f),
                                        Color(0xFF06B6D4).copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = buttonText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1D4ED8)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF1D4ED8),
                                modifier = Modifier.size(14.dp)
                              )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationEmptyState(
    selectedCategory: NotificationCategory?,
    onClearFilter: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.90f),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .premiumTargetAnimation(
                    target = com.example.model.AnimatableTarget.EMPTY_ERROR_STATES,
                    index = 0
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "No notifications",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "All Caught Up!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppTextDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (selectedCategory != null) {
                        "There are no alerts matching the ${selectedCategory.displayName} category right now."
                    } else {
                        "You don't have any announcements or notices at the moment. We'll alert you when updates arrive!"
                    },
                    fontSize = 13.sp,
                    color = AppTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                if (selectedCategory != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onClearFilter,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("View All Notifications", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun formatRelativeTime(sentAt: Long): String {
    val diff = System.currentTimeMillis() - sentAt
    val min = 60 * 1000L
    val hour = 60 * min
    val day = 24 * hour

    return when {
        diff < min -> "Just now"
        diff < hour -> "${diff / min} mins ago"
        diff < day -> "${diff / hour} hours ago"
        diff < 7 * day -> "${diff / day} days ago"
        else -> SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(sentAt))
    }
}

private fun getActionTextForDeepLink(deepLink: String?): String? {
    if (deepLink.isNullOrBlank()) return null
    val clean = deepLink.lowercase()
    return when {
        clean.contains("subscription") || clean.contains("premium") -> "View Subscription Plans"
        clean.contains("mock") || clean.contains("test") -> "Go to Mock Test Arena"
        clean.contains("current") || clean.contains("affairs") -> "Check Current Affairs"
        clean.contains("quiz") || clean.contains("daily") -> "Start Daily Quiz"
        clean.contains("saved") || clean.contains("bookmark") -> "View Saved Questions"
        else -> "View Update Details"
    }
}
