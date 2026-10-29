package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.model.AdminNotificationModel
import com.example.model.NotificationAudience
import com.example.model.NotificationCategory
import com.example.model.NotificationStatus
import com.example.ui.components.AdminAppFilterDropdown
import com.example.ui.components.AppTargetScopeBadge
import com.example.ui.components.AppTargetSelector
import com.example.ui.components.ImageUploadGuidelinesDialog
import com.example.ui.components.RecommendedImageSizeHelper
import com.example.ui.components.UploadImageType
import com.example.util.ImageStorageManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationsTab() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Create Notification", "Sent History", "Scheduled", "Drafts")
    var editingNotification by remember { mutableStateOf<AdminNotificationModel?>(null) }
    var showGlobalGuidelines by remember { mutableStateOf(false) }

    if (showGlobalGuidelines) {
        ImageUploadGuidelinesDialog(
            currentSpec = UploadImageType.NOTIFICATION_IMAGE,
            onDismiss = { showGlobalGuidelines = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Notifications & Broadcasts", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text("Send announcements, alerts, and banners to users", fontSize = 11.5.sp, color = Color(0xFF64748B))
            }
            OutlinedButton(
                onClick = { showGlobalGuidelines = true },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF2563EB))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Guidelines", fontSize = 11.5.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp,
            containerColor = Color.White,
            contentColor = Color(0xFF2563EB),
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF2563EB),
                        height = 3.dp
                    )
                }
            },
            divider = { HorizontalDivider(color = Color(0xFFE2E8F0)) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTabIndex == index) Color(0xFF2563EB) else Color(0xFF64748B)
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(Color(0xFFF8FAFC))
        ) {
            when (selectedTabIndex) {
                0 -> NotificationCreateTab(
                    onNotificationCreated = { selectedTabIndex = 1 }
                )
                1 -> NotificationListTab(
                    statusFilter = NotificationStatus.SENT,
                    onEditNotification = { editingNotification = it }
                )
                2 -> NotificationListTab(
                    statusFilter = NotificationStatus.SCHEDULED,
                    onEditNotification = { editingNotification = it }
                )
                3 -> NotificationListTab(
                    statusFilter = NotificationStatus.DRAFT,
                    onEditNotification = { editingNotification = it }
                )
            }
        }
    }

    if (editingNotification != null) {
        EditNotificationDialog(
            notification = editingNotification!!,
            onDismiss = { editingNotification = null },
            onSave = { updated ->
                val index = SampleData.adminNotificationsState.indexOfFirst { it.id == updated.id }
                if (index != -1) {
                    SampleData.adminNotificationsState[index] = updated
                }
                editingNotification = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCreateTab(onNotificationCreated: () -> Unit = {}) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val saved = ImageStorageManager.persistPickedImage(context, uri, "notification")
                imageUrl = saved ?: uri.toString()
            }
        }
    }
    var actionText by remember { mutableStateOf("") }
    var deepLink by remember { mutableStateOf("MOCK_TESTS") }
    var priority by remember { mutableStateOf("HIGH") }
    var iconName by remember { mutableStateOf("bell") }
    var isFeatured by remember { mutableStateOf(false) }
    var targetAppIds by remember { mutableStateOf(listOf("ALL")) }

    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(NotificationCategory.IMPORTANT) }

    var expandedAudience by remember { mutableStateOf(false) }
    var selectedAudience by remember { mutableStateOf(NotificationAudience.ALL) }

    var expandedDeepLink by remember { mutableStateOf(false) }
    val deepLinkOptions = listOf(
        "MOCK_TESTS" to "🧪 Mock Tests Hub",
        "CURRENT_AFFAIRS" to "📰 Current Affairs",
        "SUBSCRIPTION" to "⭐ Subscription & Offers",
        "SYLLABUS" to "📚 Syllabus & Units",
        "DAILY_QUIZ" to "⚡ Daily Practice Quiz",
        "PERFORMANCE" to "📊 Performance Analytics",
        "LEADERBOARD" to "🏆 State Leaderboard",
        "SPEED_DUEL" to "⚔️ Speed Duel Arena"
    )

    val iconOptions = listOf(
        "siren" to "🚨 Siren Alert",
        "flask" to "🧪 Mock Test Flask",
        "newspaper" to "📰 News Paper",
        "star" to "⭐ Star Reward",
        "gift" to "🎁 Gift Offer",
        "gear" to "⚙️ System Gear",
        "trophy" to "🏆 Trophy",
        "bell" to "🔔 Standard Bell"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Create Push Notification", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A))
                Text("Central Notification Engine with Live App Preview & Multi-App Targeting", fontSize = 12.sp, color = Color(0xFF10B981))
            }
        }

        // Target App Selector
        item {
            AppTargetSelector(
                selectedTargetAppIds = targetAppIds,
                onTargetAppIdsChanged = { targetAppIds = it },
                title = "Notification App Target",
                subtitle = "Deliver push notification globally to all apps or selectively to target apps"
            )
        }

        // Notification Title & Message
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notification Title *") },
                    placeholder = { Text("e.g. 🚨 APPSC Group-2 Mains Exam Schedule Out!") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message Body *") },
                    placeholder = { Text("Enter detailed notification message for candidate alerts...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 4
                )
            }
        }

        // Image Banner URL with Device Upload & Recommended Dimensions Helper
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Notification Banner Image (Optional)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF334155)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Banner Image URL or File Path") },
                        placeholder = { Text("https://... or choose photo") },
                        trailingIcon = {
                            if (imageUrl.isNotBlank()) {
                                IconButton(onClick = { imageUrl = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Upload", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                RecommendedImageSizeHelper(
                    spec = UploadImageType.NOTIFICATION_IMAGE,
                    imageSource = imageUrl,
                    showContainerPreview = true
                )
            }
        }

        // Category & Audience Pickers
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = !expandedCategory }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = "${selectedCategory.iconPrefix} ${selectedCategory.displayName}",
                            onValueChange = { },
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            NotificationCategory.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text("${cat.iconPrefix} ${cat.displayName}") },
                                    onClick = {
                                        selectedCategory = cat
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedAudience,
                        onExpandedChange = { expandedAudience = !expandedAudience }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedAudience.name,
                            onValueChange = { },
                            label = { Text("Audience Segment") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAudience) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAudience,
                            onDismissRequest = { expandedAudience = false }
                        ) {
                            NotificationAudience.values().forEach { aud ->
                                DropdownMenuItem(
                                    text = { Text(aud.name) },
                                    onClick = {
                                        selectedAudience = aud
                                        expandedAudience = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Deep Link Target & Priority
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedDeepLink,
                        onExpandedChange = { expandedDeepLink = !expandedDeepLink }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = deepLinkOptions.find { it.first == deepLink }?.second ?: deepLink,
                            onValueChange = { },
                            label = { Text("Target Screen / Action") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDeepLink) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDeepLink,
                            onDismissRequest = { expandedDeepLink = false }
                        ) {
                            deepLinkOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt.second) },
                                    onClick = {
                                        deepLink = opt.first
                                        expandedDeepLink = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = actionText,
                    onValueChange = { actionText = it },
                    label = { Text("Button Text (Optional)") },
                    placeholder = { Text("e.g. Take Test Now") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }

        // Icon Selection & Priority Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Notification Icon", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(iconOptions) { (key, name) ->
                        val isSelected = iconName == key
                        Surface(
                            onClick = { iconName = key },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF2563EB) else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Priority & Featured Switch
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Priority:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    listOf("HIGH", "MEDIUM", "LOW").forEach { p ->
                        Surface(
                            onClick = { priority = p },
                            shape = RoundedCornerShape(6.dp),
                            color = if (priority == p) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (priority == p) Color(0xFF0F172A) else Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = p,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (priority == p) Color.White else Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Featured:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it }
                    )
                }
            }
        }

        // LIVE MOBILE NOTIFICATION PREVIEW
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF050B18), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📱 LIVE IN-APP PREVIEW", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF38BDF8))
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF10B981).copy(alpha = 0.2f)) {
                        Text("REAL-TIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val sampleModel = AdminNotificationModel(
                    id = "preview",
                    title = title.ifBlank { "🚨 APPSC Group-2 Mains Exam Schedule Announced!" },
                    message = message.ifBlank { "Official notification released. Check schedule, syllabus, and grand mock test details inside." },
                    imageUrl = imageUrl.ifBlank { null },
                    category = selectedCategory,
                    deepLink = deepLink,
                    priority = priority,
                    iconName = iconName,
                    isFeatured = isFeatured,
                    actionText = actionText.ifBlank { null },
                    sentAt = System.currentTimeMillis()
                )

                NotificationCard(
                    notification = sampleModel,
                    onCardClick = { },
                    onDismiss = { }
                )
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                            return@OutlinedButton
                        }
                        val draft = AdminNotificationModel(
                            id = "draft-${UUID.randomUUID().toString().take(6)}",
                            title = title,
                            message = message.ifBlank { "Draft notification message" },
                            imageUrl = imageUrl.ifBlank { null },
                            category = selectedCategory,
                            audience = selectedAudience,
                            deepLink = deepLink.ifBlank { null },
                            status = NotificationStatus.DRAFT,
                            priority = priority,
                            iconName = iconName,
                            isFeatured = isFeatured,
                            actionText = actionText.ifBlank { null },
                            targetAppIds = targetAppIds
                        )
                        SampleData.adminNotificationsState.add(0, draft)
                        Toast.makeText(context, "Saved as Draft!", Toast.LENGTH_SHORT).show()
                        title = ""
                        message = ""
                        imageUrl = ""
                        actionText = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Draft")
                }

                OutlinedButton(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                            return@OutlinedButton
                        }
                        val scheduled = AdminNotificationModel(
                            id = "sched-${UUID.randomUUID().toString().take(6)}",
                            title = title,
                            message = message.ifBlank { "Scheduled notification message" },
                            imageUrl = imageUrl.ifBlank { null },
                            category = selectedCategory,
                            audience = selectedAudience,
                            deepLink = deepLink.ifBlank { null },
                            status = NotificationStatus.SCHEDULED,
                            scheduledAt = System.currentTimeMillis() + 86400000L,
                            priority = priority,
                            iconName = iconName,
                            isFeatured = isFeatured,
                            actionText = actionText.ifBlank { null },
                            targetAppIds = targetAppIds
                        )
                        SampleData.adminNotificationsState.add(0, scheduled)
                        Toast.makeText(context, "Scheduled for Tomorrow!", Toast.LENGTH_SHORT).show()
                        title = ""
                        message = ""
                        imageUrl = ""
                        actionText = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Schedule")
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (title.isBlank() || message.isBlank()) {
                        Toast.makeText(context, "Please enter both Title and Message", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val sent = AdminNotificationModel(
                        id = "sent-${UUID.randomUUID().toString().take(6)}",
                        title = title,
                        message = message,
                        imageUrl = imageUrl.ifBlank { null },
                        category = selectedCategory,
                        audience = selectedAudience,
                        deepLink = deepLink.ifBlank { null },
                        status = NotificationStatus.SENT,
                        sentAt = System.currentTimeMillis(),
                        priority = priority,
                        iconName = iconName,
                        isFeatured = isFeatured,
                        actionText = actionText.ifBlank { null },
                        deliveryCount = (6500..14000).random(),
                        openCount = (2200..4900).random(),
                        failedCount = (12..45).random(),
                        targetAppIds = targetAppIds
                    )
                    SampleData.adminNotificationsState.add(0, sent)
                    Toast.makeText(context, "🚀 Notification Published & Pushed via FCM!", Toast.LENGTH_SHORT).show()
                    title = ""
                    message = ""
                    imageUrl = ""
                    actionText = ""
                    onNotificationCreated()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publish & Push Now via FCM", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun NotificationListTab(
    statusFilter: NotificationStatus,
    onEditNotification: (AdminNotificationModel) -> Unit
) {
    val context = LocalContext.current
    var appFilter by remember { mutableStateOf<String?>(null) }

    val allForStatus = SampleData.adminNotificationsState.filter { it.status == statusFilter }
    val filteredNotifications = remember(allForStatus, appFilter) {
        if (appFilter == null || appFilter == "ALL") {
            allForStatus
        } else {
            allForStatus.filter { it.targetAppIds.contains("ALL") || it.targetAppIds.contains(appFilter) }
        }
    }
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when (statusFilter) {
                        NotificationStatus.SENT -> "Sent History (${filteredNotifications.size})"
                        NotificationStatus.SCHEDULED -> "Scheduled Campaigns (${filteredNotifications.size})"
                        NotificationStatus.DRAFT -> "Draft Messages (${filteredNotifications.size})"
                        else -> "${statusFilter.name} (${filteredNotifications.size})"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                Text("Manage, edit, publish or delete notifications", fontSize = 11.sp, color = Color(0xFF64748B))
            }

            AdminAppFilterDropdown(
                selectedAppFilter = appFilter,
                onAppFilterChanged = { appFilter = it }
            )
        }

        if (filteredNotifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No notifications found for this tab.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredNotifications, key = { it.id }) { notif ->
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text(notif.category.iconPrefix, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = notif.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "${notif.category.displayName} • Priority: ${notif.priority}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                AppTargetScopeBadge(targetAppIds = notif.targetAppIds)
                            }

                            Text(
                                text = notif.message,
                                fontSize = 13.5.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier.padding(vertical = 10.dp)
                            )

                            if (!notif.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = notif.imageUrl,
                                    contentDescription = "Banner",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(4.dp)) {
                                    Text(
                                        text = "Audience: ${notif.audience.name}",
                                        color = Color(0xFF2563EB),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                val timeText = when (notif.status) {
                                    NotificationStatus.SENT -> notif.sentAt?.let { "Sent: ${dateFormat.format(Date(it))}" } ?: ""
                                    NotificationStatus.SCHEDULED -> notif.scheduledAt?.let { "Scheduled: ${dateFormat.format(Date(it))}" } ?: ""
                                    else -> "Draft"
                                }
                                Text(timeText, fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (notif.status == NotificationStatus.SENT) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        AnalyticsItem("Delivered", "${notif.deliveryCount}", Color(0xFF10B981))
                                        AnalyticsItem("Opened", "${notif.openCount}", Color(0xFF3B82F6))
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(onClick = { onEditNotification(notif) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Edit", fontSize = 11.5.sp)
                                    }

                                    if (notif.status != NotificationStatus.SENT) {
                                        TextButton(
                                            onClick = {
                                                val index = SampleData.adminNotificationsState.indexOf(notif)
                                                if (index != -1) {
                                                    SampleData.adminNotificationsState[index] = notif.copy(
                                                        status = NotificationStatus.SENT,
                                                        sentAt = System.currentTimeMillis()
                                                    )
                                                    Toast.makeText(context, "Published live!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) {
                                            Text("Publish Now", color = Color(0xFF2563EB), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            SampleData.adminNotificationsState.remove(notif)
                                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
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

@Composable
fun EditNotificationDialog(
    notification: AdminNotificationModel,
    onDismiss: () -> Unit,
    onSave: (AdminNotificationModel) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf(notification.title) }
    var message by remember { mutableStateOf(notification.message) }
    var imageUrl by remember { mutableStateOf(notification.imageUrl ?: "") }
    var deepLink by remember { mutableStateOf(notification.deepLink ?: "") }
    var priority by remember { mutableStateOf(notification.priority) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val saved = ImageStorageManager.persistPickedImage(context, uri, "notification")
                imageUrl = saved ?: uri.toString()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Notification", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message Body") },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Banner Image URL or Path") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                RecommendedImageSizeHelper(
                    spec = UploadImageType.NOTIFICATION_IMAGE,
                    imageSource = imageUrl,
                    showContainerPreview = true
                )

                OutlinedTextField(
                    value = deepLink,
                    onValueChange = { deepLink = it },
                    label = { Text("Deep Link Target") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        notification.copy(
                            title = title,
                            message = message,
                            imageUrl = imageUrl.ifBlank { null },
                            deepLink = deepLink.ifBlank { null },
                            priority = priority
                        )
                    )
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AnalyticsItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
        Text(label, fontSize = 10.5.sp, color = Color(0xFF64748B))
    }
}
