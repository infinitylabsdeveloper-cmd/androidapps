package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.data.CouponRepository
import com.example.data.SampleData
import com.example.model.Coupon
import com.example.model.CouponDiscountType
import com.example.model.CouponStatus
import com.example.ui.components.AppTargetSelector
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCouponTab() {
    val allCoupons by CouponRepository.coupons.collectAsState()
    val activeCoupons = allCoupons.filter { !it.isSoftDeleted }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<CouponStatus?>(null) }
    var couponToEdit by remember { mutableStateOf<Coupon?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    
    // Automatically trigger status updates
    LaunchedEffect(Unit) {
        while (true) {
            CouponRepository.updateCouponStatuses()
            delay(5000)
        }
    }

    val filteredCoupons = activeCoupons.filter {
        (selectedFilter == null || it.status == selectedFilter) &&
        (it.code.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Coupon Management",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Button(
                onClick = { 
                    couponToEdit = Coupon(code = "", description = "", discountValue = 0.0)
                    showCreateDialog = true 
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Coupon", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Coupon")
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
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search coupons by code or description...") },
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
                    DropdownMenuItem(text = { Text("All") }, onClick = { selectedFilter = null; expanded = false })
                    CouponStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name) },
                            onClick = { selectedFilter = status; expanded = false }
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
            items(filteredCoupons) { coupon ->
                AdminCouponCard(
                    coupon = coupon,
                    onEdit = { 
                        couponToEdit = coupon
                        showCreateDialog = true
                    },
                    onToggle = { isEnabled -> CouponRepository.toggleCouponStatus(coupon.id, isEnabled) },
                    onDelete = { CouponRepository.deleteCoupon(coupon.id) }
                )
            }
        }
    }
    
    if (showCreateDialog && couponToEdit != null) {
        AdminCouponFormDialog(
            initialCoupon = couponToEdit!!,
            onDismiss = { showCreateDialog = false },
            onSave = { updatedCoupon ->
                CouponRepository.addOrUpdateCoupon(updatedCoupon)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun AdminCouponCard(
    coupon: Coupon,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDisableConfirm by remember { mutableStateOf(false) }
    
    val statusColor = when (coupon.status) {
        CouponStatus.ACTIVE -> Color(0xFF10B981)
        CouponStatus.SCHEDULED -> Color(0xFF3B82F6)
        CouponStatus.INACTIVE -> Color(0xFF64748B)
        CouponStatus.EXPIRED -> Color(0xFFEF4444)
        CouponStatus.EXHAUSTED -> Color(0xFFF59E0B)
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E7FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalOffer, contentDescription = "Coupon", tint = Color(0xFF4F46E5))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = coupon.code,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = coupon.status.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = coupon.description,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Usage Stats & App Scope
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val limitText = if (coupon.usageLimitTotal != null) "/${coupon.usageLimitTotal}" else ""
                    Text("Uses: ${coupon.timesUsed}$limitText", fontSize = 12.sp, color = Color(0xFF475569))
                    Text("Saved: ₹${coupon.totalDiscountGiven.toInt()}", fontSize = 12.sp, color = Color(0xFF475569))

                    val appScopeLabel = when {
                        coupon.targetAppIds.isEmpty() || coupon.targetAppIds.contains("ALL") -> "🌐 All Apps"
                        coupon.targetAppIds.size == 1 -> {
                            val app = SampleData.appsListState.find { it.id == coupon.targetAppIds.first() }
                            "📱 ${app?.name ?: coupon.targetAppIds.first()}"
                        }
                        else -> "📱 ${coupon.targetAppIds.size} Selected Apps"
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
                val discountLabel = if (coupon.discountType == CouponDiscountType.PERCENTAGE) "${coupon.discountValue.toInt()}% OFF" else "₹${coupon.discountValue.toInt()} OFF"
                Text(
                    text = discountLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val isChecked = coupon.status == CouponStatus.ACTIVE || coupon.status == CouponStatus.SCHEDULED
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
            title = { Text("Delete Coupon?") },
            text = { Text("Are you sure you want to delete '${coupon.code}'? This will prevent it from being used in the future.") },
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
            title = { Text("Disable Coupon?") },
            text = { Text("Disabling '${coupon.code}' will prevent users from applying it.") },
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
fun AdminCouponFormDialog(
    initialCoupon: Coupon,
    onDismiss: () -> Unit,
    onSave: (Coupon) -> Unit
) {
    var code by remember { mutableStateOf(initialCoupon.code) }
    var description by remember { mutableStateOf(initialCoupon.description) }
    
    var discountType by remember { mutableStateOf(initialCoupon.discountType) }
    var discountValue by remember { mutableStateOf(initialCoupon.discountValue.toString()) }
    var minPurchaseAmount by remember { mutableStateOf(initialCoupon.minPurchaseAmount.toString()) }
    var maxDiscountAmount by remember { mutableStateOf(initialCoupon.maxDiscountAmount?.toString() ?: "") }
    
    var usageLimitTotal by remember { mutableStateOf(initialCoupon.usageLimitTotal?.toString() ?: "") }
    var usageLimitPerUser by remember { mutableStateOf(initialCoupon.usageLimitPerUser?.toString() ?: "") }
    var targetAppIds by remember { mutableStateOf(initialCoupon.targetAppIds) }
    
    // Status & schedule
    var activateImmediately by remember { mutableStateOf(initialCoupon.activateAt == null || initialCoupon.activateAt <= System.currentTimeMillis()) }
    var activateAt by remember { mutableStateOf(initialCoupon.activateAt ?: System.currentTimeMillis()) }
    var hasExpiry by remember { mutableStateOf(initialCoupon.expiresAt != null) }
    var expiresAt by remember { mutableStateOf(initialCoupon.expiresAt ?: (System.currentTimeMillis() + 86400000)) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

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
                Text("Edit Coupon Code", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase().replace(" ", "") },
                        label = { Text("Coupon Code (e.g., FESTIVAL50)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Discount Rules", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Discount Type Dropdown Mock
                        Box(modifier = Modifier.weight(1f)) {
                            var expanded by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(discountType.name)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                CouponDiscountType.entries.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.name) },
                                        onClick = { discountType = type; expanded = false }
                                    )
                                }
                            }
                        }
                        
                        OutlinedTextField(
                            value = discountValue,
                            onValueChange = { discountValue = it },
                            label = { Text("Value") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minPurchaseAmount,
                            onValueChange = { minPurchaseAmount = it },
                            label = { Text("Min Purchase (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxDiscountAmount,
                            onValueChange = { maxDiscountAmount = it },
                            label = { Text("Max Discount (Optional)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Text("Limits", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = usageLimitTotal,
                            onValueChange = { usageLimitTotal = it },
                            label = { Text("Total Uses (Optional)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = usageLimitPerUser,
                            onValueChange = { usageLimitPerUser = it },
                            label = { Text("Uses Per User (Optional)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    AppTargetSelector(
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

                            val updatedCoupon = initialCoupon.copy(
                                code = code,
                                description = description,
                                discountType = discountType,
                                discountValue = discountValue.toDoubleOrNull() ?: 0.0,
                                minPurchaseAmount = minPurchaseAmount.toDoubleOrNull() ?: 0.0,
                                maxDiscountAmount = maxDiscountAmount.toDoubleOrNull(),
                                usageLimitTotal = usageLimitTotal.toIntOrNull(),
                                usageLimitPerUser = usageLimitPerUser.toIntOrNull(),
                                targetAppIds = targetAppIds,
                                activateAt = activateTime,
                                expiresAt = expireTime,
                                status = if (activateTime > now) CouponStatus.SCHEDULED else CouponStatus.ACTIVE
                            )
                            onSave(updatedCoupon)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Save Coupon")
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
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = it
                        val timeCalendar = Calendar.getInstance()
                        timeCalendar.timeInMillis = activateAt
                        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                        activateAt = calendar.timeInMillis
                    }
                    showStartDatePicker = false
                    // Reuse start time picker for coupon too if needed, or just date for now.
                    // Actually, let's just do Date for simplicity if user didn't specify high granularity for coupons.
                    // But they said "Start Date & Time". So I'll add time picker too.
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
