package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.AdminUserModel
import com.example.model.UserAccountStatus
import com.example.ui.components.AppBadgeChip
import com.example.ui.components.AppSpecificContextBar
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminUsersTab() {
    var selectedUser by remember { mutableStateOf<AdminUserModel?>(null) }

    if (selectedUser != null) {
        UserDetailsScreen(user = selectedUser!!, onBack = { selectedUser = null })
    } else {
        AdminUsersListScreen(onUserSelect = { selectedUser = it })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersListScreen(onUserSelect: (AdminUserModel) -> Unit) {
    var selectedAppId by remember { mutableStateOf(SampleData.currentActiveAppId.value) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("All Users", "Active", "Premium", "Blocked")

    // Strict App Isolation: Filter users strictly by the active appId
    val appUsers = SampleData.adminUsersState.filter { it.appId == selectedAppId }

    val filteredUsers = when (selectedTabIndex) {
        1 -> appUsers.filter { it.status == UserAccountStatus.ACTIVE }
        2 -> appUsers.filter { it.isPremium }
        3 -> appUsers.filter { it.status == UserAccountStatus.BLOCKED }
        else -> appUsers
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // App Selector Context Bar (Strict Data Isolation)
        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            AppSpecificContextBar(
                selectedAppId = selectedAppId,
                onAppSelected = { newAppId ->
                    selectedAppId = newAppId
                    SampleData.setActiveApp(newAppId)
                }
            )
        }

        // Stats Header for the Selected App
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            UserStatCard("Total", "${appUsers.size}", Color(0xFF3B82F6), Modifier.weight(1f))
            UserStatCard("Active", "${appUsers.count { it.status == UserAccountStatus.ACTIVE }}", Color(0xFF10B981), Modifier.weight(1f))
            UserStatCard("Premium", "${appUsers.count { it.isPremium }}", Color(0xFFF59E0B), Modifier.weight(1f))
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

        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No users found in this category for ${SampleData.appsListState.find { it.id == selectedAppId }?.name ?: "this app"}.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                val isExpanded = maxWidth >= 600.dp
                if (isExpanded) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredUsers) { user ->
                            UserListItemCard(user = user, onClick = { onUserSelect(user) })
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredUsers) { user ->
                            UserListItemCard(user = user, onClick = { onUserSelect(user) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserStatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.Black, color = color)
            Text(title, fontSize = 10.5.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun UserListItemCard(user: AdminUserModel, onClick: () -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(user.name.take(1).uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.width(6.dp))
                    AppBadgeChip(appId = user.appId)
                }
                Text(user.email, fontSize = 11.5.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (user.isPremium) {
                        Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(4.dp)) {
                            Text("Premium", color = Color(0xFFB45309), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    } else {
                        Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(4.dp)) {
                            Text("Free", color = Color(0xFF475569), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    
                    val statusColor = when (user.status) {
                        UserAccountStatus.ACTIVE -> Color(0xFF10B981)
                        UserAccountStatus.BLOCKED -> Color(0xFFEF4444)
                        UserAccountStatus.PENDING -> Color(0xFFF59E0B)
                    }
                    val statusBg = statusColor.copy(alpha = 0.1f)
                    Surface(color = statusBg, shape = RoundedCornerShape(4.dp)) {
                        Text(user.status.name, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFCBD5E1))
        }
    }
}

@Composable
fun UserDetailsScreen(user: AdminUserModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        // Header
        Surface(
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("User Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Profile Card
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(60.dp).clip(CircleShape).background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.name.take(1).uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(user.id, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))
                        DetailRow(Icons.Default.Email, "Email", user.email)
                        DetailRow(Icons.Default.Phone, "Phone", user.phone)
                        DetailRow(Icons.Default.DateRange, "Registered", dateFormat.format(Date(user.registeredAt)))
                        DetailRow(Icons.Default.AccessTime, "Last Active", dateFormat.format(Date(user.lastActiveAt)))
                    }
                }
            }

            // Subscription & Performance
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Subscription", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (user.isPremium) {
                                Text(user.subscriptionPlan ?: "Premium", fontWeight = FontWeight.Black, color = Color(0xFFD97706), fontSize = 16.sp)
                                Text("Active Plan", fontSize = 11.sp, color = Color(0xFF10B981))
                            } else {
                                Text("Free User", fontWeight = FontWeight.Black, color = Color(0xFF475569), fontSize = 16.sp)
                                Text("No active plan", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Performance", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${user.averageAccuracy}%", fontWeight = FontWeight.Black, color = Color(0xFF2563EB), fontSize = 16.sp)
                            Text("Average Accuracy", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Practice Stats
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Practice History", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("${user.totalMcqsAttempted}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF0F172A))
                                Text("MCQs Attempted", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                            Column {
                                Text("${user.totalMockTestsAttempted}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF0F172A))
                                Text("Mock Tests Attempted", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }

            // Admin Controls
            item {
                Text("Admin Controls", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 4.dp))
            }

            // Pro Membership Grant / Revocation
            item {
                Button(
                    onClick = {
                        val idx = SampleData.adminUsersState.indexOfFirst { it.id == user.id }
                        if (user.isPremium) {
                            if (idx != -1) {
                                SampleData.adminUsersState[idx] = user.copy(isPremium = false)
                            }
                            if (user.email == SampleData.sampleUserProfile.email) {
                                SampleData.cancelSubscription("Admin Revoked")
                            }
                            Toast.makeText(context, "Pro Subscription Revoked for ${user.name}", Toast.LENGTH_SHORT).show()
                        } else {
                            if (idx != -1) {
                                SampleData.adminUsersState[idx] = user.copy(isPremium = true)
                            }
                            if (user.email == SampleData.sampleUserProfile.email) {
                                SampleData.updateSubscription("APPSC Pro Annual Pass", 365 * 24L)
                            }
                            Toast.makeText(context, "👑 1-Year Pro Pass Granted to ${user.name}!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isPremium) Color(0xFFDC2626) else Color(0xFFF59E0B)
                    )
                ) {
                    Icon(
                        if (user.isPremium) Icons.Default.Cancel else Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (user.isPremium) "Revoke Pro Membership" else "Grant 1-Year Pro Membership")
                }
            }
            
            item {
                Button(
                    onClick = {
                        SampleData.adminNotificationsState.add(
                            0,
                            com.example.model.AdminNotificationModel(
                                id = "notif-user-${System.currentTimeMillis()}",
                                title = "📩 Message from Admin",
                                message = "Hello ${user.name}, you have a direct update regarding your test preparation and account.",
                                category = com.example.model.NotificationCategory.IMPORTANT,
                                audience = com.example.model.NotificationAudience.SPECIFIC,
                                status = com.example.model.NotificationStatus.SENT,
                                sentAt = System.currentTimeMillis(),
                                priority = "HIGH",
                                iconName = "siren",
                                isRead = false,
                                actionText = "Open"
                            )
                        )
                        Toast.makeText(context, "Notification sent directly to ${user.name}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Notification")
                }
            }
            
            item {
                if (user.status == UserAccountStatus.BLOCKED) {
                    OutlinedButton(
                        onClick = {
                            val idx = SampleData.adminUsersState.indexOfFirst { it.id == user.id }
                            if (idx != -1) {
                                SampleData.adminUsersState[idx] = user.copy(status = UserAccountStatus.ACTIVE)
                            }
                            Toast.makeText(context, "User ${user.name} Unblocked & Synchronized", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unblock Account")
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            val idx = SampleData.adminUsersState.indexOfFirst { it.id == user.id }
                            if (idx != -1) {
                                SampleData.adminUsersState[idx] = user.copy(status = UserAccountStatus.BLOCKED)
                            }
                            Toast.makeText(context, "User ${user.name} Blocked & Synchronized", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Block Account")
                    }
                }
            }
            
            item {
                TextButton(
                    onClick = {
                        SampleData.adminUsersState.removeAll { it.id == user.id }
                        Toast.makeText(context, "Account for ${user.name} deleted", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Account Permanently")
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = Color(0xFF64748B), fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A), fontSize = 14.sp)
    }
}
