package com.example.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.AppTenantModel
import com.example.model.TargetScopeType

/**
 * Universal App Target Selector for Global & Universal features.
 * Allows Super Admin to apply content to:
 * - All Apps (Global)
 * - One App (Single Target)
 * - Multiple Selected Apps (Targeted Multi-Distribution)
 */
@Composable
fun AppTargetSelector(
    scope: TargetScopeType,
    selectedAppIds: List<String>,
    onScopeChange: (TargetScopeType) -> Unit,
    onAppToggled: (String, Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onSingleAppSelected: (String) -> Unit = { onAppToggled(it, true) },
    modifier: Modifier = Modifier,
    title: String = "App Target / Apply To Selector",
    subtitle: String = "Target all competitive apps or selectively assign to specific apps"
) {
    val allApps = SampleData.appsListState
    val badgeLabel = when (scope) {
        TargetScopeType.ALL_APPS -> "ALL APPS (GLOBAL)"
        TargetScopeType.SINGLE_APP -> {
            val singleApp = allApps.find { it.id == selectedAppIds.firstOrNull() }
            singleApp?.code ?: "1 APP TARGETED"
        }
        TargetScopeType.SELECT_APPS -> "${selectedAppIds.size} APPS SELECTED"
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5)),
        border = BorderStroke(1.dp, Color(0xFFE2D8C3)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F3EE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF062C22)
                        )
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = Color(0xFF1C2D27)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (scope) {
                        TargetScopeType.ALL_APPS -> Color(0xFFDCFCE7)
                        TargetScopeType.SINGLE_APP -> Color(0xFFE8F3EE)
                        TargetScopeType.SELECT_APPS -> Color(0xFFF3E8FF)
                    },
                    border = BorderStroke(
                        1.dp,
                        when (scope) {
                            TargetScopeType.ALL_APPS -> Color(0xFFA7F3D0)
                            TargetScopeType.SINGLE_APP -> Color(0xFFA7F3D0)
                            TargetScopeType.SELECT_APPS -> Color(0xFFDDD6FE)
                        }
                    )
                ) {
                    Text(
                        text = badgeLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (scope) {
                            TargetScopeType.ALL_APPS -> Color(0xFF059669)
                            TargetScopeType.SINGLE_APP -> Color(0xFF047857)
                            TargetScopeType.SELECT_APPS -> Color(0xFF7C3AED)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 3-Way Selector Row: All Apps | One App | Multiple Apps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // All Apps Option
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (scope == TargetScopeType.ALL_APPS) Color(0xFFE8F3EE) else Color(0xFFFFFDF5),
                    border = BorderStroke(
                        1.2.dp,
                        if (scope == TargetScopeType.ALL_APPS) Color(0xFF059669) else Color(0xFFE2D8C3)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onScopeChange(TargetScopeType.ALL_APPS) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🌐 All Apps",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = if (scope == TargetScopeType.ALL_APPS) Color(0xFF062C22) else Color(0xFF1C2D27)
                        )
                        Text(
                            text = "Universal default",
                            fontSize = 9.sp,
                            color = Color(0xFF4D5E56)
                        )
                    }
                }

                // One App Option
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (scope == TargetScopeType.SINGLE_APP) Color(0xFFE8F3EE) else Color(0xFFFFFDF5),
                    border = BorderStroke(
                        1.2.dp,
                        if (scope == TargetScopeType.SINGLE_APP) Color(0xFF059669) else Color(0xFFE2D8C3)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onScopeChange(TargetScopeType.SINGLE_APP) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📱 One App",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = if (scope == TargetScopeType.SINGLE_APP) Color(0xFF062C22) else Color(0xFF1C2D27)
                        )
                        Text(
                            text = "Single target",
                            fontSize = 9.sp,
                            color = Color(0xFF4D5E56)
                        )
                    }
                }

                // Multiple Apps Option
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (scope == TargetScopeType.SELECT_APPS) Color(0xFFE8F3EE) else Color(0xFFFFFDF5),
                    border = BorderStroke(
                        1.2.dp,
                        if (scope == TargetScopeType.SELECT_APPS) Color(0xFF059669) else Color(0xFFE2D8C3)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onScopeChange(TargetScopeType.SELECT_APPS) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📱 Multiple",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = if (scope == TargetScopeType.SELECT_APPS) Color(0xFF062C22) else Color(0xFF1C2D27)
                        )
                        Text(
                            text = "Selected apps",
                            fontSize = 9.sp,
                            color = Color(0xFF4D5E56)
                        )
                    }
                }
            }

            // Single App Picker
            AnimatedVisibility(
                visible = scope == TargetScopeType.SINGLE_APP,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Select the specific app this configuration applies to:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
                    )

                    allApps.forEach { appItem ->
                        val isSelected = selectedAppIds.contains(appItem.id)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFEFF6FF) else Color.White,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSingleAppSelected(appItem.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onSingleAppSelected(appItem.id) },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(appItem.primaryColorHex))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = appItem.name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.5.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "${appItem.code} • ${appItem.examCategory}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = "TARGETED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Expandable Checkbox List for "Select Multiple Apps"
            AnimatedVisibility(
                visible = scope == TargetScopeType.SELECT_APPS,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Choose target apps (${selectedAppIds.size} of ${allApps.size} selected):",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(
                                onClick = onSelectAll,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Select All", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                            TextButton(
                                onClick = onDeselectAll,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Clear", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            }
                        }
                    }

                    allApps.forEach { appItem ->
                        val isChecked = selectedAppIds.contains(appItem.id) || selectedAppIds.contains("ALL")
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isChecked) Color.White else Color(0xFFF1F5F9),
                            border = BorderStroke(
                                1.dp,
                                if (isChecked) Color(appItem.primaryColorHex).copy(alpha = 0.6f) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppToggled(appItem.id, !isChecked) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            onAppToggled(appItem.id, checked)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(appItem.primaryColorHex))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(appItem.primaryColorHex))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = appItem.name,
                                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.5.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "${appItem.code} • ${appItem.examCategory}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (appItem.isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                ) {
                                    Text(
                                        text = if (appItem.isActive) "LIVE" else "DRAFT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (appItem.isActive) Color(0xFF16A34A) else Color(0xFFDC2626),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
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

/**
 * Convenient List<String> overload of AppTargetSelector
 */
@Composable
fun AppTargetSelector(
    selectedTargetAppIds: List<String>,
    onTargetAppIdsChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "App Target / Apply To Selector",
    subtitle: String = "Target all competitive apps, a single app, or multiple selected apps"
) {
    val allApps = SampleData.appsListState
    val currentScope = when {
        selectedTargetAppIds.isEmpty() || selectedTargetAppIds.contains("ALL") -> TargetScopeType.ALL_APPS
        selectedTargetAppIds.size == 1 -> TargetScopeType.SINGLE_APP
        else -> TargetScopeType.SELECT_APPS
    }

    var scopeState by remember(selectedTargetAppIds) { mutableStateOf(currentScope) }

    AppTargetSelector(
        scope = scopeState,
        selectedAppIds = if (scopeState == TargetScopeType.ALL_APPS) listOf("ALL") else selectedTargetAppIds,
        onScopeChange = { newScope ->
            scopeState = newScope
            when (newScope) {
                TargetScopeType.ALL_APPS -> onTargetAppIdsChanged(listOf("ALL"))
                TargetScopeType.SINGLE_APP -> {
                    val fallbackId = selectedTargetAppIds.firstOrNull { it != "ALL" } ?: allApps.first().id
                    onTargetAppIdsChanged(listOf(fallbackId))
                }
                TargetScopeType.SELECT_APPS -> {
                    val currentList = selectedTargetAppIds.filter { it != "ALL" }
                    if (currentList.isNotEmpty()) onTargetAppIdsChanged(currentList)
                    else onTargetAppIdsChanged(allApps.map { it.id })
                }
            }
        },
        onAppToggled = { appId, checked ->
            val cleanList = selectedTargetAppIds.filter { it != "ALL" }.toMutableList()
            if (checked) {
                if (!cleanList.contains(appId)) cleanList.add(appId)
            } else {
                cleanList.remove(appId)
            }
            onTargetAppIdsChanged(cleanList.ifEmpty { listOf(appId) })
        },
        onSingleAppSelected = { singleId ->
            onTargetAppIdsChanged(listOf(singleId))
        },
        onSelectAll = {
            onTargetAppIdsChanged(allApps.map { it.id })
        },
        onDeselectAll = {
            onTargetAppIdsChanged(listOf(allApps.first().id))
        },
        title = title,
        subtitle = subtitle,
        modifier = modifier
    )
}

/**
 * Visual badge indicating the target app scope of a resource
 */
@Composable
fun AppTargetScopeBadge(
    targetAppIds: List<String>,
    modifier: Modifier = Modifier
) {
    val isAll = targetAppIds.isEmpty() || targetAppIds.contains("ALL")
    val isSingle = !isAll && targetAppIds.size == 1
    val singleApp = if (isSingle) SampleData.appsListState.find { it.id == targetAppIds.first() } else null
    val badgeText = when {
        isAll -> "🌐 All Apps"
        isSingle -> "📱 ${singleApp?.name ?: targetAppIds.first()}"
        else -> "📱 ${targetAppIds.size} Apps"
    }
    val badgeColor = when {
        isAll -> Color(0xFF10B981)
        isSingle -> Color(0xFF2563EB)
        else -> Color(0xFF7C3AED)
    }
    val badgeBg = when {
        isAll -> Color(0xFFECFDF5)
        isSingle -> Color(0xFFEFF6FF)
        else -> Color(0xFFF3E8FF)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = badgeBg,
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = badgeText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
        }
    }
}

/**
 * Standard App Filter Dropdown for list headers
 */
@Composable
fun AdminAppFilterDropdown(
    selectedAppFilter: String?,
    onAppFilterChanged: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val allApps = SampleData.appsListState
    val currentApp = allApps.find { it.id == selectedAppFilter }
    val label = if (selectedAppFilter == null || selectedAppFilter == "ALL") "🌐 All Apps" else "📱 ${currentApp?.name ?: selectedAppFilter}"

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter: $label",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌐 All Apps", fontWeight = if (selectedAppFilter == null || selectedAppFilter == "ALL") FontWeight.Bold else FontWeight.Normal)
                    }
                },
                onClick = {
                    onAppFilterChanged(null)
                    expanded = false
                }
            )
            allApps.forEach { app ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(app.primaryColorHex)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(app.name, fontWeight = if (selectedAppFilter == app.id) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    onClick = {
                        onAppFilterChanged(app.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * App Specific Context Bar
 * Displayed at the top of every APP-SPECIFIC section (Syllabus, MCQs, Users, Reports, App Dashboard, App Revenue)
 * Strictly guarantees that an Admin selecting App A sees ONLY App A's data!
 */
@Composable
fun AppSpecificContextBar(
    selectedAppId: String,
    onAppSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val appsList = SampleData.appsListState
    val currentApp = appsList.find { it.id == selectedAppId } ?: appsList.first()

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFE8DC)),
        border = BorderStroke(1.dp, Color(0xFFD8CEBD)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "APP-SPECIFIC MANAGEMENT CONTEXT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF047857),
                        letterSpacing = 0.8.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFB91C1C),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "STRICT DATA ISOLATION",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB91C1C)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(currentApp.primaryColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = currentApp.name,
                            color = Color(0xFF062C22),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentApp.code} • ${currentApp.examCategory}",
                            color = Color(0xFF1C2D27),
                            fontSize = 11.sp
                        )
                    }
                }

                // App Selector Dropdown
                Box {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F3EE)),
                        border = BorderStroke(1.dp, Color(0xFF059669)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Switch App",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        appsList.forEach { app ->
                            val isSelected = app.id == selectedAppId
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(app.primaryColorHex))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = app.name,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp,
                                                color = Color(0xFF062C22)
                                            )
                                            Text(
                                                text = "${app.code} • ${app.examCategory}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF1C2D27)
                                            )
                                        }
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("✓ Active", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                },
                                onClick = {
                                    onAppSelected(app.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual badge for App Identity across list views
 */
@Composable
fun AppBadgeChip(
    appId: String,
    modifier: Modifier = Modifier
) {
    val app = SampleData.appsListState.find { it.id == appId }
    val appName = app?.name ?: appId
    val appColor = Color(app?.primaryColorHex ?: 0xFF2563EB)

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = appColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, appColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(appColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = appName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = appColor
            )
        }
    }
}
