package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.SampleData
import com.example.data.SubscriptionRepository
import com.example.model.PlanStatus
import com.example.model.SubscriptionPlan
import com.example.model.SubscriptionBackgroundModel
import androidx.compose.ui.draw.scale
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSubscriptionTab() {
    val allPlans by SubscriptionRepository.plans.collectAsState()
    val activePlans = allPlans.filter { !it.isSoftDeleted }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<PlanStatus?>(null) }
    var planToEdit by remember { mutableStateOf<SubscriptionPlan?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Plans, 1: Backgrounds
    
    // Automatically trigger status updates
    LaunchedEffect(Unit) {
        while (true) {
            SubscriptionRepository.updatePlanStatuses()
            delay(5000)
        }
    }

    val filteredPlans = activePlans.filter {
        (selectedFilter == null || it.status == selectedFilter) &&
        (it.name.contains(searchQuery, ignoreCase = true) || it.subtitle.contains(searchQuery, ignoreCase = true))
    }.sortedBy { it.displayOrder }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
    ) {
        Text(
            text = "Subscription Management",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF2563EB),
            divider = { HorizontalDivider(color = Color(0xFFE2E8F0)) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Subscription Plans", fontWeight = if(selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Page Backgrounds", fontWeight = if(selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Razorpay Gateway", fontWeight = if(selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Transactions Ledger", fontWeight = if(selectedTab == 3) FontWeight.Bold else FontWeight.Normal) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (selectedTab) {
            0 -> {
                PlansTabContent(
                    activePlans = activePlans,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    onEditPlan = { 
                        planToEdit = it
                        showCreateDialog = true
                    }
                )
            }
            1 -> {
                BackgroundsTabContent()
            }
            2 -> {
                RazorpayGatewayTabContent()
            }
            3 -> {
                AdminTransactionsLedgerTabContent()
            }
        }
    }
    
    if (showCreateDialog && planToEdit != null) {
        AdminPlanFormDialog(
            initialPlan = planToEdit!!,
            onDismiss = { showCreateDialog = false },
            onSave = { updatedPlan ->
                SubscriptionRepository.addOrUpdatePlan(updatedPlan)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun PlansTabContent(
    activePlans: List<SubscriptionPlan>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: PlanStatus?,
    onFilterChange: (PlanStatus?) -> Unit,
    onEditPlan: (SubscriptionPlan) -> Unit
) {
    val filteredPlans = activePlans.filter {
        (selectedFilter == null || it.status == selectedFilter) &&
        (it.name.contains(searchQuery, ignoreCase = true) || it.subtitle.contains(searchQuery, ignoreCase = true))
    }.sortedBy { it.displayOrder }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Manage Plans",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )
            Button(
                onClick = { 
                    onEditPlan(SubscriptionPlan(name = "", subtitle = "", price = 0, originalPrice = 0, effectiveMonthly = "", discountText = "", iconEmoji = "🌟", features = emptyList()))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Plan", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create Plan", fontSize = 13.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search and Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search plans...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedFilter?.name ?: "All")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("All") }, onClick = { onFilterChange(null); expanded = false })
                    PlanStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name) },
                            onClick = { onFilterChange(status); expanded = false }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredPlans) { plan ->
                AdminPlanCard(
                    plan = plan,
                    onEdit = { onEditPlan(plan) },
                    onToggle = { isEnabled -> SubscriptionRepository.togglePlanStatus(plan.id, isEnabled) },
                    onDelete = { SubscriptionRepository.deletePlan(plan.id) }
                )
            }
        }
    }
}

@Composable
fun BackgroundsTabContent() {
    val backgrounds by SubscriptionRepository.backgrounds.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var bgToEdit by remember { mutableStateOf<SubscriptionBackgroundModel?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Manage Backgrounds",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )
            Button(
                onClick = { 
                    bgToEdit = SubscriptionBackgroundModel(imageUrl = "")
                    showForm = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Background", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Background", fontSize = 13.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (backgrounds.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Text("No backgrounds added yet", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(backgrounds.sortedBy { it.order }) { bg ->
                    AdminBackgroundCard(
                        bg = bg,
                        onEdit = {
                            bgToEdit = bg
                            showForm = true
                        },
                        onToggle = { SubscriptionRepository.toggleBackground(bg.id, it) },
                        onDelete = { SubscriptionRepository.deleteBackground(bg.id) },
                        onMoveUp = { SubscriptionRepository.reorderBackground(bg.id, true) },
                        onMoveDown = { SubscriptionRepository.reorderBackground(bg.id, false) }
                    )
                }
            }
        }
    }

    if (showForm && bgToEdit != null) {
        AdminBackgroundFormDialog(
            initialBg = bgToEdit!!,
            onDismiss = { showForm = false },
            onSave = {
                SubscriptionRepository.addOrUpdateBackground(it)
                showForm = false
            }
        )
    }
}

@Composable
fun AdminBackgroundCard(
    bg: SubscriptionBackgroundModel,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            ) {
                AsyncImage(
                    model = bg.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(bg.title.ifBlank { "Untitled Background" }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(bg.subtitle.ifBlank { "No subtitle" }, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                
                val appLabel = when {
                    bg.mappedAppIds.contains("ALL") -> "🌐 All Apps"
                    bg.mappedAppIds.size == 1 -> "📱 ${SampleData.appsListState.find { it.id == bg.mappedAppIds.first() }?.name ?: bg.mappedAppIds.first()}"
                    else -> "📱 ${bg.mappedAppIds.size} Apps"
                }
                Text(appLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = bg.isEnabled, onCheckedChange = onToggle, modifier = Modifier.scale(0.7f))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBackgroundFormDialog(
    initialBg: SubscriptionBackgroundModel,
    onDismiss: () -> Unit,
    onSave: (SubscriptionBackgroundModel) -> Unit
) {
    var title by remember { mutableStateOf(initialBg.title) }
    var subtitle by remember { mutableStateOf(initialBg.subtitle) }
    var imageUrl by remember { mutableStateOf(initialBg.imageUrl) }
    var mappedAppIds by remember { mutableStateOf(initialBg.mappedAppIds) }
    var isEnabled by remember { mutableStateOf(initialBg.isEnabled) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Background Management", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Subtitle") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth())
                
                if (imageUrl.isNotBlank()) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray)) {
                        AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                    }
                }
                
                AppDistributionScopeSelector(
                    selectedTargetAppIds = mappedAppIds,
                    onTargetAppIdsChanged = { mappedAppIds = it }
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
                    Text("Enable Background")
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = {
                        onSave(initialBg.copy(title = title, subtitle = subtitle, imageUrl = imageUrl, mappedAppIds = mappedAppIds, isEnabled = isEnabled))
                    }) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun AdminPlanCard(
    plan: SubscriptionPlan,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDisableConfirm by remember { mutableStateOf(false) }
    
    val statusColor = when (plan.status) {
        PlanStatus.ACTIVE -> Color(0xFF10B981)
        PlanStatus.SCHEDULED -> Color(0xFF3B82F6)
        PlanStatus.INACTIVE -> Color(0xFF64748B)
        PlanStatus.EXPIRED -> Color(0xFFEF4444)
    }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val colorString = try { android.graphics.Color.parseColor(plan.cardBgColorHex) } catch(e: Exception) { android.graphics.Color.LTGRAY }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(colorString)),
                contentAlignment = Alignment.Center
            ) {
                Text(plan.iconEmoji, fontSize = 24.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = plan.subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = plan.status.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    val appScopeLabel = when {
                        plan.targetAppIds.isEmpty() || plan.targetAppIds.contains("ALL") -> "🌐 All Apps"
                        plan.targetAppIds.size == 1 -> {
                            val app = SampleData.appsListState.find { it.id == plan.targetAppIds.first() }
                            "📱 ${app?.name ?: plan.targetAppIds.first()}"
                        }
                        else -> "📱 ${plan.targetAppIds.size} Selected Apps"
                    }

                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = appScopeLabel,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${plan.price}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val isChecked = plan.status == PlanStatus.ACTIVE || plan.status == PlanStatus.SCHEDULED
                    Switch(
                        checked = isChecked,
                        onCheckedChange = { willEnable ->
                            if (!willEnable) {
                                showDisableConfirm = true
                            } else {
                                onToggle(true)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981)
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Plan?") },
            text = { Text("Are you sure you want to delete '${plan.name}'? Existing subscriptions will remain active (soft delete).") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDisableConfirm) {
        AlertDialog(
            onDismissRequest = { showDisableConfirm = false },
            title = { Text("Disable Plan?") },
            text = { Text("Disabling '${plan.name}' will hide it from the store. Existing subscribers will not be affected.") },
            confirmButton = {
                Button(
                    onClick = {
                        onToggle(false)
                        showDisableConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Text("Disable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisableConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlanFormDialog(
    initialPlan: SubscriptionPlan,
    onDismiss: () -> Unit,
    onSave: (SubscriptionPlan) -> Unit
) {
    var name by remember { mutableStateOf(initialPlan.name) }
    var subtitle by remember { mutableStateOf(initialPlan.subtitle) }
    var price by remember { mutableStateOf(initialPlan.price.toString()) }
    var originalPrice by remember { mutableStateOf(initialPlan.originalPrice.toString()) }
    var iconEmoji by remember { mutableStateOf(initialPlan.iconEmoji) }
    
    // Status & schedule
    var activateImmediately by remember { mutableStateOf(initialPlan.activateAt == null || initialPlan.activateAt <= System.currentTimeMillis()) }
    var activateAt by remember { mutableStateOf(initialPlan.activateAt ?: System.currentTimeMillis()) }
    var hasExpiry by remember { mutableStateOf(initialPlan.expiresAt != null) }
    var expiresAt by remember { mutableStateOf(initialPlan.expiresAt ?: (System.currentTimeMillis() + 86400000)) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var features by remember { mutableStateOf(initialPlan.features.joinToString("\n")) }
    var targetAppIds by remember { mutableStateOf(initialPlan.targetAppIds) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("Edit Subscription Plan", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Plan Name (e.g., 6 Months)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        label = { Text("Subtitle (e.g., Achiever Plan)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = originalPrice,
                            onValueChange = { originalPrice = it },
                            label = { Text("Original Price (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = iconEmoji,
                        onValueChange = { iconEmoji = it },
                        label = { Text("Icon / Emoji") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = features,
                        onValueChange = { features = it },
                        label = { Text("Features (One per line)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    AppDistributionScopeSelector(
                        selectedTargetAppIds = targetAppIds,
                        onTargetAppIdsChanged = { targetAppIds = it }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Lifecycle & Scheduling", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = { activateImmediately = true },
                            color = if (activateImmediately) Color(0xFFEFF6FF) else Color.White,
                            border = BorderStroke(1.dp, if (activateImmediately) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, tint = if (activateImmediately) Color(0xFF2563EB) else Color(0xFF64748B))
                                Text("Instant", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (activateImmediately) Color(0xFF2563EB) else Color(0xFF64748B))
                            }
                        }

                        Surface(
                            onClick = { activateImmediately = false },
                            color = if (!activateImmediately) Color(0xFFEDE9FE) else Color.White,
                            border = BorderStroke(1.dp, if (!activateImmediately) Color(0xFF7C3AED) else Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = if (!activateImmediately) Color(0xFF7C3AED) else Color(0xFF64748B))
                                Text("Scheduled", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!activateImmediately) Color(0xFF7C3AED) else Color(0xFF64748B))
                            }
                        }
                    }

                    if (!activateImmediately) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        OutlinedTextField(
                            value = sdf.format(Date(activateAt)),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Activation Date & Time") },
                            modifier = Modifier.fillMaxWidth().clickable { showStartDatePicker = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color(0xFFE2E8F0))
                        )
                    }

                    HorizontalDivider()

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = hasExpiry,
                            onCheckedChange = { hasExpiry = it }
                        )
                        Text("Set Optional Expiry Date/Time")
                    }
                    if (hasExpiry) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        OutlinedTextField(
                            value = sdf.format(Date(expiresAt)),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Expiry Date & Time") },
                            modifier = Modifier.fillMaxWidth().clickable { showEndDatePicker = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color(0xFFE2E8F0))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val now = System.currentTimeMillis()
                            val activateTime = if (activateImmediately) now else activateAt
                            val expireTime = if (hasExpiry) expiresAt else null

                            val updatedPlan = initialPlan.copy(
                                name = name,
                                subtitle = subtitle,
                                price = price.toIntOrNull() ?: 0,
                                originalPrice = originalPrice.toIntOrNull() ?: 0,
                                iconEmoji = iconEmoji,
                                features = features.split("\n").map { it.trim() }.filter { it.isNotEmpty() },
                                targetAppIds = targetAppIds,
                                activateAt = activateTime,
                                expiresAt = expireTime,
                                status = if (activateTime > now) PlanStatus.SCHEDULED else PlanStatus.ACTIVE
                            )
                            onSave(updatedPlan)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Save Plan")
                    }
                }
            }
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = activateAt)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        // Keep time part, change date part
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = it
                        val timeCalendar = Calendar.getInstance()
                        timeCalendar.timeInMillis = activateAt
                        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                        activateAt = calendar.timeInMillis
                    }
                    showStartDatePicker = false
                    showStartTimePicker = true
                }) { Text("Next: Time") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = activateAt
        val timePickerState = rememberTimePickerState(initialHour = calendar.get(Calendar.HOUR_OF_DAY), initialMinute = calendar.get(Calendar.MINUTE))
        Dialog(onDismissRequest = { showStartTimePicker = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select Activation Time", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                    TimePicker(state = timePickerState)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = activateAt
                            cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            cal.set(Calendar.MINUTE, timePickerState.minute)
                            activateAt = cal.timeInMillis
                            showStartTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = expiresAt)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = it
                        val timeCalendar = Calendar.getInstance()
                        timeCalendar.timeInMillis = expiresAt
                        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                        expiresAt = calendar.timeInMillis
                    }
                    showEndDatePicker = false
                    showEndTimePicker = true
                }) { Text("Next: Time") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndTimePicker) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = expiresAt
        val timePickerState = rememberTimePickerState(initialHour = calendar.get(Calendar.HOUR_OF_DAY), initialMinute = calendar.get(Calendar.MINUTE))
        Dialog(onDismissRequest = { showEndTimePicker = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select Expiry Time", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                    TimePicker(state = timePickerState)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = expiresAt
                            cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            cal.set(Calendar.MINUTE, timePickerState.minute)
                            expiresAt = cal.timeInMillis
                            showEndTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

@Composable
fun AppDistributionScopeSelector(
    selectedTargetAppIds: List<String>,
    onTargetAppIdsChanged: (List<String>) -> Unit
) {
    val allApps = SampleData.appsListState
    var distributionMode by remember(selectedTargetAppIds) {
        mutableStateOf(
            when {
                selectedTargetAppIds.isEmpty() || selectedTargetAppIds.contains("ALL") -> "ALL"
                selectedTargetAppIds.size == 1 -> "SINGLE"
                else -> "SELECT_MULTIPLE"
            }
        )
    }

    var singleSelectedAppId by remember(selectedTargetAppIds) {
        mutableStateOf(
            if (distributionMode == "SINGLE") selectedTargetAppIds.firstOrNull() ?: (allApps.firstOrNull()?.id ?: "appsc-group2")
            else allApps.firstOrNull()?.id ?: "appsc-group2"
        )
    }

    var multiSelectedAppIds by remember(selectedTargetAppIds) {
        mutableStateOf(
            if (distributionMode == "SELECT_MULTIPLE") selectedTargetAppIds.toSet()
            else setOf(allApps.firstOrNull()?.id ?: "appsc-group2")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Apps, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("App Distribution Scope", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
        }

        Text("Distribute price changes and subscription plans to a single app, select apps, or all apps.", fontSize = 11.5.sp, color = Color(0xFF64748B))

        // Segmented options: ALL, SINGLE, SELECT MULTIPLE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                onClick = {
                    distributionMode = "ALL"
                    onTargetAppIdsChanged(listOf("ALL"))
                },
                color = if (distributionMode == "ALL") Color(0xFF2563EB) else Color.White,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "🌐 All Apps",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (distributionMode == "ALL") Color.White else Color(0xFF334155),
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Surface(
                onClick = {
                    distributionMode = "SINGLE"
                    onTargetAppIdsChanged(listOf(singleSelectedAppId))
                },
                color = if (distributionMode == "SINGLE") Color(0xFF2563EB) else Color.White,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "📱 Single App",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (distributionMode == "SINGLE") Color.White else Color(0xFF334155),
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Surface(
                onClick = {
                    distributionMode = "SELECT_MULTIPLE"
                    onTargetAppIdsChanged(multiSelectedAppIds.toList())
                },
                color = if (distributionMode == "SELECT_MULTIPLE") Color(0xFF2563EB) else Color.White,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "☑️ Select Apps",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (distributionMode == "SELECT_MULTIPLE") Color.White else Color(0xFF334155),
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Sub-selector for SINGLE mode
        if (distributionMode == "SINGLE") {
            var expanded by remember { mutableStateOf(false) }
            val currentSelectedApp = allApps.find { it.id == singleSelectedAppId }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(currentSelectedApp?.name ?: "Select App", fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    allApps.forEach { app ->
                        DropdownMenuItem(
                            text = { Text("${app.name} (${app.code})") },
                            onClick = {
                                singleSelectedAppId = app.id
                                onTargetAppIdsChanged(listOf(app.id))
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Sub-selector for SELECT_MULTIPLE mode
        if (distributionMode == "SELECT_MULTIPLE") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                allApps.forEach { app ->
                    val isChecked = multiSelectedAppIds.contains(app.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updated = if (isChecked) {
                                    if (multiSelectedAppIds.size > 1) multiSelectedAppIds - app.id else multiSelectedAppIds
                                } else {
                                    multiSelectedAppIds + app.id
                                }
                                multiSelectedAppIds = updated
                                onTargetAppIdsChanged(updated.toList())
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                val updated = if (checked) multiSelectedAppIds + app.id else {
                                    if (multiSelectedAppIds.size > 1) multiSelectedAppIds - app.id else multiSelectedAppIds
                                }
                                multiSelectedAppIds = updated
                                onTargetAppIdsChanged(updated.toList())
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(app.name, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color(0xFF0F172A))
                            Text(app.examCategory, fontSize = 10.5.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RazorpayGatewayTabContent() {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        com.example.util.BillingPaymentManager.loadMerchantConfig(context)
    }
    var merchantName by remember { mutableStateOf(com.example.util.BillingPaymentManager.merchantName) }
    var merchantId by remember { mutableStateOf(com.example.util.BillingPaymentManager.merchantId) }
    var accountId by remember { mutableStateOf(com.example.util.BillingPaymentManager.accountId) }
    var razorpayKeyId by remember { mutableStateOf(com.example.util.BillingPaymentManager.razorpayKeyId) }
    var isLiveMode by remember { mutableStateOf(com.example.util.BillingPaymentManager.isLiveMode) }
    var showSavedMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Connection Status Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, Color(0xFF2563EB).copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2563EB).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Razorpay Payment Gateway",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Connected to receive payments directly",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Surface(
                        color = if (isLiveMode) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isLiveMode) Color(0xFF16A34A) else Color(0xFFD97706))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLiveMode) "LIVE" else "TEST MODE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLiveMode) Color(0xFF166534) else Color(0xFF92400E)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Connected Account Verified Badge
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Connected Business Account: $merchantName",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E3A8A)
                            )
                        }
                        Text(
                            text = "• Merchant ID (MID): $merchantId",
                            fontSize = 12.sp,
                            color = Color(0xFF1E40AF),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "• Account ID: $accountId",
                            fontSize = 12.sp,
                            color = Color(0xFF1E40AF),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "• Settlements: Direct to SOBHA DHANARAJU linked bank account",
                            fontSize = 11.sp,
                            color = Color(0xFF3B82F6)
                        )
                    }
                }
            }
        }

        // Razorpay Website & KYC Policy Verification Hub
        com.example.ui.components.RazorpayComplianceHubCard()

        // Configuration Form Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Merchant Gateway Credentials",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )

                OutlinedTextField(
                    value = merchantName,
                    onValueChange = { merchantName = it },
                    label = { Text("Account Holder / Business Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = merchantId,
                    onValueChange = { merchantId = it },
                    label = { Text("Merchant ID (MID)") },
                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = accountId,
                    onValueChange = { accountId = it },
                    label = { Text("Account ID") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Locked & Verified Razorpay Live Key (Configured directly in App BuildConfig)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Secured Key",
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Razorpay Live Key ID",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF16A34A))
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "LIVE ACTIVE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = com.example.util.BillingPaymentManager.getMaskedKeyId(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Production",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Text(
                            text = "🔒 నేరుగా App BuildConfig & Environment లో లైవ్ కీ అమర్చబడింది. పేమెంట్ భద్రత కోసం అడ్మిన్ ప్యానెల్‌లో మాన్యువల్ ఎడిటింగ్ లాక్ చేయబడింది.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Live Mode Production Settlements",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Real Live Transactions with Direct Razorpay Checkout",
                            fontSize = 11.5.sp,
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "LIVE ON",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                if (showSavedMessage) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "✅ Razorpay credentials updated & activated successfully!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        com.example.util.BillingPaymentManager.updateMerchantConfig(
                            name = merchantName,
                            mid = merchantId,
                            accId = accountId,
                            keyId = com.example.util.BillingPaymentManager.configuredLiveKeyId,
                            live = true,
                            context = context
                        )
                        showSavedMessage = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Apply Razorpay Settings", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Supported Payment Methods Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enabled Payment Channels",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )

                val channels = listOf(
                    "UPI Instant (Google Pay, PhonePe, Paytm, BHIM, Cred)" to Color(0xFF10B981),
                    "Credit & Debit Cards (Visa, Mastercard, RuPay, Maestro)" to Color(0xFF2563EB),
                    "NetBanking (50+ Major Indian Banks)" to Color(0xFF7C3AED),
                    "Digital Wallets (Amazon Pay, Mobikwik, Airtel Money)" to Color(0xFFF59E0B)
                )

                channels.forEach { (channel, color) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = channel,
                            fontSize = 12.5.sp,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 4: PAYMENT TRANSACTIONS LEDGER (REQUIREMENT 12)
// =============================================================================
@Composable
fun AdminTransactionsLedgerTabContent() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val verificationService = remember { com.example.service.SubscriptionPaymentVerificationService.getInstance(context) }
    val transactions by verificationService.getAllTransactionsFlow().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var providerFilter by remember { mutableStateOf<String?>(null) } // null = All, "GOOGLE_PLAY", "RAZORPAY", "ADMIN_GRANTED"
    var showGrantDialog by remember { mutableStateOf(false) }

    val filteredTransactions = transactions.filter { tx ->
        (providerFilter == null || tx.provider.equals(providerFilter, ignoreCase = true)) &&
        (tx.userId.contains(searchQuery, ignoreCase = true) ||
         tx.planTitle.contains(searchQuery, ignoreCase = true) ||
         tx.transactionId.contains(searchQuery, ignoreCase = true) ||
         (tx.providerPaymentId?.contains(searchQuery, ignoreCase = true) ?: false))
    }.sortedByDescending { it.purchaseTimestamp }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header: Stats Summary & Action Bar
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Payment & Subscription Ledger",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Unified verified transaction audit trail",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Button(
                        onClick = { showGrantDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.AddModerator, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Grant Admin Subscription", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val playCount = transactions.count { it.provider == "GOOGLE_PLAY" }
                    val razorpayCount = transactions.count { it.provider == "RAZORPAY" }
                    val adminCount = transactions.count { it.provider == "ADMIN_GRANTED" || it.isAdminGranted }

                    LedgerMetricCard("Total Verified", "${transactions.size}", Color(0xFF2563EB), Modifier.weight(1f))
                    LedgerMetricCard("Google Play", "$playCount", Color(0xFF10B981), Modifier.weight(1f))
                    LedgerMetricCard("Razorpay / UPI", "$razorpayCount", Color(0xFF0284C7), Modifier.weight(1f))
                    LedgerMetricCard("Admin Granted", "$adminCount", Color(0xFF7C3AED), Modifier.weight(1f))
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Financial Integrity: Verified payment records are immutable and cannot be modified directly.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }

        // Search & Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by User ID, Plan, or Txn ID...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            )
        }

        // Provider Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = providerFilter == null,
                onClick = { providerFilter = null },
                label = { Text("All (${transactions.size})") }
            )
            FilterChip(
                selected = providerFilter == "GOOGLE_PLAY",
                onClick = { providerFilter = if (providerFilter == "GOOGLE_PLAY") null else "GOOGLE_PLAY" },
                label = { Text("Google Play") }
            )
            FilterChip(
                selected = providerFilter == "RAZORPAY",
                onClick = { providerFilter = if (providerFilter == "RAZORPAY") null else "RAZORPAY" },
                label = { Text("Razorpay") }
            )
            FilterChip(
                selected = providerFilter == "ADMIN_GRANTED",
                onClick = { providerFilter = if (providerFilter == "ADMIN_GRANTED") null else "ADMIN_GRANTED" },
                label = { Text("Admin Granted") }
            )
        }

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No transactions found", fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text("Verified purchases and grants will appear here.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(filteredTransactions) { tx ->
                    TransactionItemCard(tx)
                }
            }
        }
    }

    if (showGrantDialog) {
        AdminGrantSubscriptionDialog(
            onDismiss = { showGrantDialog = false },
            onGrant = { userId, planTitle, durationDays, adminEmail ->
                scope.launch(Dispatchers.IO) {
                    verificationService.grantAdminSubscription(
                        userId = userId,
                        planTitle = planTitle,
                        durationDays = durationDays,
                        adminEmail = adminEmail
                    )
                }
                showGrantDialog = false
            }
        )
    }
}

@Composable
private fun LedgerMetricCard(title: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = accentColor)
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
        }
    }
}

@Composable
private fun TransactionItemCard(tx: com.example.db.UserEntitlementEntity) {
    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
    val startFormatted = dateFormat.format(java.util.Date(tx.startDate))
    val expiryFormatted = dateFormat.format(java.util.Date(tx.expiryDate))

    val isGooglePlay = tx.provider == "GOOGLE_PLAY"
    val isAdminGranted = tx.provider == "ADMIN_GRANTED" || tx.isAdminGranted

    val providerBadgeBg = when {
        isAdminGranted -> Color(0xFFF3E8FF)
        isGooglePlay -> Color(0xFFDCFCE7)
        else -> Color(0xFFE0F2FE)
    }
    val providerBadgeText = when {
        isAdminGranted -> Color(0xFF7C3AED)
        isGooglePlay -> Color(0xFF166534)
        else -> Color(0xFF0284C7)
    }
    val providerLabel = when {
        isAdminGranted -> "Admin Granted Subscription"
        isGooglePlay -> "Google Play Billing"
        else -> "Razorpay Alternative Billing"
    }

    val statusBadgeBg = when (tx.status.uppercase()) {
        "ACTIVE" -> Color(0xFFDCFCE7)
        "PENDING", "INITIATED" -> Color(0xFFFEF3C7)
        "FAILED" -> Color(0xFFFEE2E2)
        "REFUNDED" -> Color(0xFFFFEDD5)
        else -> Color(0xFFF1F5F9)
    }
    val statusBadgeText = when (tx.status.uppercase()) {
        "ACTIVE" -> Color(0xFF166534)
        "PENDING", "INITIATED" -> Color(0xFF92400E)
        "FAILED" -> Color(0xFF991B1B)
        "REFUNDED" -> Color(0xFF9A3412)
        else -> Color(0xFF475569)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row: User ID & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAdminGranted) Icons.Default.AddModerator else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isAdminGranted) Color(0xFF7C3AED) else Color(0xFF2563EB),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tx.userId,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                Text(
                    text = if (tx.amountRupees > 0) "₹${tx.amountRupees} ${tx.currency}" else "Admin Granted",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (tx.amountRupees > 0) Color(0xFF0F172A) else Color(0xFF7C3AED)
                )
            }

            // Plan Title & Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tx.planTitle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF334155)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = providerBadgeBg) {
                        Text(
                            text = providerLabel,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = providerBadgeText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = statusBadgeBg) {
                        Text(
                            text = tx.status,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = statusBadgeText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Timestamps and Details
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                DetailRow("Transaction ID", tx.transactionId)
                if (!tx.providerOrderId.isNullOrBlank()) {
                    DetailRow("Order ID", tx.providerOrderId)
                }
                DetailRow("Subscription Start", startFormatted)
                DetailRow("Subscription Expiry", expiryFormatted)
                DetailRow("Renewal Status", if (tx.autoRenew) "Auto-Renew Active" else "One-Time Validity Pass")
            }

            // Immutable Verification Footer
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Verified Payment • Direct modification locked",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.5.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
    }
}

// =============================================================================
// ADMIN MANUAL GRANT SUBSCRIPTION DIALOG
// =============================================================================
@Composable
private fun AdminGrantSubscriptionDialog(
    onDismiss: () -> Unit,
    onGrant: (userId: String, planTitle: String, durationDays: Int, adminEmail: String) -> Unit
) {
    var userId by remember { mutableStateOf("") }
    var planTitle by remember { mutableStateOf("12 Months Complete Mastery (Admin Grant)") }
    var durationDaysText by remember { mutableStateOf("365") }
    var adminEmail by remember { mutableStateOf("admin@appscprep.com") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.96f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Admin Granted Subscription",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color(0xFF7C3AED)
                        )
                        Text(
                            text = "Manual Premium provision (separate from verified payments)",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("Candidate User ID / Email") },
                    placeholder = { Text("e.g. aspirant_9876 or user@email.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = planTitle,
                    onValueChange = { planTitle = it },
                    label = { Text("Plan Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = durationDaysText,
                    onValueChange = { durationDaysText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Validity Duration (Days)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = adminEmail,
                    onValueChange = { adminEmail = it },
                    label = { Text("Granting Admin Email / Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF3E8FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Notice: This entitlement will be explicitly recorded as 'Admin Granted Subscription' and cannot be confused with financial payment receipts.",
                        fontSize = 11.sp,
                        color = Color(0xFF6B21A8),
                        modifier = Modifier.padding(10.dp),
                        lineHeight = 15.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val days = durationDaysText.toIntOrNull() ?: 30
                            if (userId.isNotBlank()) {
                                onGrant(userId.trim(), planTitle.trim(), days, adminEmail.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = userId.isNotBlank(),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Grant Premium", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

