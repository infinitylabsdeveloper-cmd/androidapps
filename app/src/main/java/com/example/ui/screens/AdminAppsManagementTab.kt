package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.AppStatus
import com.example.model.AppTenantModel
import com.example.ui.components.PremiumVisualColorPicker
import com.example.util.AdminSessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppsManagementTab(
    onNavigateToTenantGenerator: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableStateOf("All Apps") }
    val subTabs = listOf("All Apps", "Active Apps", "Inactive Apps", "Archived Apps")
    
    var selectedLanguageFilter by remember { mutableStateOf("All") }
    val languageFilters = listOf("All") + com.example.util.MediumStandardizer.STANDARDIZED_LANGUAGES

    var selectedAppForDetails by remember { mutableStateOf<AppTenantModel?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val appsList = SampleData.appsListState
    val filteredApps = remember(selectedSubTab, selectedLanguageFilter, searchQuery, appsList.size) {
        appsList.filter { app ->
            val matchesTab = when (selectedSubTab) {
                "Active Apps" -> app.status == AppStatus.ACTIVE
                "Inactive Apps" -> app.status == AppStatus.INACTIVE
                "Archived Apps" -> app.status == AppStatus.ARCHIVED
                else -> true
            }
            val cleanAppMedium = com.example.util.MediumStandardizer.standardizeLanguageName(app.medium)
            val matchesLang = selectedLanguageFilter == "All" || cleanAppMedium.equals(selectedLanguageFilter, ignoreCase = true)
            val matchesSearch = app.name.contains(searchQuery, ignoreCase = true) || 
                              app.id.contains(searchQuery, ignoreCase = true) ||
                              app.shortName.contains(searchQuery, ignoreCase = true) ||
                              cleanAppMedium.contains(searchQuery, ignoreCase = true)
            matchesTab && matchesLang && matchesSearch
        }
    }

    if (selectedAppForDetails != null) {
        AppDetailsView(
            app = selectedAppForDetails!!,
            onBack = { selectedAppForDetails = null },
            onUpdate = { updatedApp ->
                val cleanApp = updatedApp.copy(
                    name = com.example.util.MediumStandardizer.standardizeAppName(updatedApp.name),
                    medium = com.example.util.MediumStandardizer.standardizeLanguageName(updatedApp.medium),
                    primaryLanguage = com.example.util.MediumStandardizer.standardizeLanguageName(updatedApp.primaryLanguage),
                    allowedLanguages = com.example.util.MediumStandardizer.standardizeLanguageList(updatedApp.allowedLanguages)
                )
                val index = SampleData.appsListState.indexOfFirst { it.id == cleanApp.id }
                if (index != -1) {
                    SampleData.appsListState[index] = cleanApp
                    
                    // Trigger dynamic sync if this is the currently active app context
                    if (cleanApp.id == SampleData.currentActiveAppId.value) {
                        com.example.repository.AppConfigRepository.getInstance().refreshCurrentConfig()
                    }

                    SampleData.logAction(
                        AdminSessionManager.ADMIN_ID,
                        AdminSessionManager.ADMIN_NAME,
                        "EDITED",
                        cleanApp.id,
                        cleanApp.name,
                        "Updated configuration (Medium: ${cleanApp.medium})"
                    )
                }
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            // Sub-Navigation & Header
            Surface(
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "App Management",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Manage and configure 662+ app instances",
                                fontSize = 10.5.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = onNavigateToTenantGenerator,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Add App Tenant", 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    ScrollableTabRow(
                        selectedTabIndex = subTabs.indexOf(selectedSubTab),
                        containerColor = Color.White,
                        contentColor = Color(0xFF2563EB),
                        edgePadding = 12.dp,
                        divider = {}
                    ) {
                        subTabs.forEach { tab ->
                            Tab(
                                selected = selectedSubTab == tab,
                                onClick = { selectedSubTab = tab },
                                text = {
                                    Text(
                                        text = tab,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedSubTab == tab) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Toolbar: Search & Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, ID or code...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = true
                )
                
                Surface(
                    onClick = { /* Open Filters */ },
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Filters", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Standardized Language Filter Chips Row (No "Medium" Suffix)
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(languageFilters) { lang ->
                    FilterChip(
                        selected = selectedLanguageFilter == lang,
                        onClick = { selectedLanguageFilter = lang },
                        label = { Text(if (lang == "All") "All Languages" else lang, fontSize = 12.sp) }
                    )
                }
            }

            // Apps Grid/List with Adaptive Screen Widths
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                val isExpanded = maxWidth >= 600.dp

                if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFFCBD5E1)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No apps found matching your criteria",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        }
                    }
                } else if (isExpanded) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredApps, key = { it.id }) { app ->
                            AppManagementCard(
                                app = app,
                                onClick = { selectedAppForDetails = app },
                                onStatusChange = { newStatus ->
                                    val index = SampleData.appsListState.indexOfFirst { it.id == app.id }
                                    if (index != -1) {
                                        SampleData.appsListState[index] = app.copy(status = newStatus)
                                    }
                                },
                                onDuplicate = {
                                    val newId = "copy_${app.id}_${System.currentTimeMillis() % 1000}"
                                    val duplicatedApp = app.copy(
                                        id = newId,
                                        name = "${app.name} (Copy)",
                                        status = AppStatus.DRAFT,
                                        createdAt = System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    SampleData.appsListState.add(duplicatedApp)
                                    Toast.makeText(context, "App Duplicated!", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    SampleData.appsListState.removeIf { it.id == app.id }
                                    Toast.makeText(context, "App Deleted!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredApps, key = { it.id }) { app ->
                            AppManagementCard(
                                app = app,
                                onClick = { selectedAppForDetails = app },
                                onStatusChange = { newStatus ->
                                    val index = SampleData.appsListState.indexOfFirst { it.id == app.id }
                                    if (index != -1) {
                                        SampleData.appsListState[index] = app.copy(status = newStatus)
                                    }
                                },
                                onDuplicate = {
                                    val newId = "copy_${app.id}_${System.currentTimeMillis() % 1000}"
                                    val duplicatedApp = app.copy(
                                        id = newId,
                                        name = "${app.name} (Copy)",
                                        status = AppStatus.DRAFT,
                                        createdAt = System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    SampleData.appsListState.add(duplicatedApp)
                                    Toast.makeText(context, "App Duplicated!", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    SampleData.appsListState.removeIf { it.id == app.id }
                                    Toast.makeText(context, "App Deleted!", Toast.LENGTH_SHORT).show()
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
fun AppManagementCard(
    app: AppTenantModel,
    onClick: () -> Unit,
    onStatusChange: (AppStatus) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val isActive = app.id == SampleData.currentActiveAppId.value
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) Color(0xFF2563EB) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(app.primaryColorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.shortName.take(2),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = app.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isActive) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = Color(0xFF2563EB).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        color = Color(0xFF2563EB),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        val cleanMedium = com.example.util.MediumStandardizer.standardizeLanguageName(app.medium)
                        Text(
                            text = "ID: ${app.id} • ${app.examName} • $cleanMedium",
                            fontSize = 10.5.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                AppStatusBadge(status = app.status)
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricInfo(label = "Active Users", value = "${app.activeUsers}")
                    MetricInfo(label = "Subscriptions", value = "${app.subscriptionUsers}")
                    MetricInfo(label = "Content", value = "${app.contentCount}")
                }
                
                Row {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color(0xFF64748B))
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Configuration") },
                            onClick = { showMenu = false; onClick() },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate App") },
                            onClick = { showMenu = false; onDuplicate() },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        
                        AppStatus.values().filter { it != app.status }.forEach { status ->
                            DropdownMenuItem(
                                text = { Text("Mark as $status") },
                                onClick = { showMenu = false; onStatusChange(status) },
                                leadingIcon = { 
                                    Icon(
                                        imageVector = when(status) {
                                            AppStatus.ACTIVE -> Icons.Default.CheckCircle
                                            AppStatus.INACTIVE -> Icons.Default.PauseCircle
                                            AppStatus.ARCHIVED -> Icons.Default.Archive
                                            AppStatus.MAINTENANCE -> Icons.Default.Settings
                                            AppStatus.DRAFT -> Icons.Default.Description
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { Text("Delete App", color = Color.Red) },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppStatusBadge(status: AppStatus) {
    val color = when (status) {
        AppStatus.ACTIVE -> Color(0xFF10B981)
        AppStatus.DRAFT -> Color(0xFF64748B)
        AppStatus.INACTIVE -> Color(0xFFF59E0B)
        AppStatus.MAINTENANCE -> Color(0xFF3B82F6)
        AppStatus.ARCHIVED -> Color(0xFFBE123C)
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status.name,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun MetricInfo(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 14.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Black)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsView(
    app: AppTenantModel,
    onBack: () -> Unit,
    onUpdate: (AppTenantModel) -> Unit
) {
    var editingApp by remember { mutableStateOf(app) }
    var activeSubTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("General", "Branding", "Contact & Support", "Audit Logs")

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        // Detail Header
        Surface(color = Color.White, shadowElevation = 1.dp) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = app.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "App ID: ${app.id}", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    
                    Button(
                        onClick = { onUpdate(editingApp); Toast.makeText(AdminSessionManager.context, "Changes Saved!", Toast.LENGTH_SHORT).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Changes")
                    }
                }
                
                ScrollableTabRow(
                    selectedTabIndex = activeSubTab,
                    containerColor = Color.White,
                    contentColor = Color(0xFF2563EB),
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = activeSubTab == index,
                            onClick = { activeSubTab = index },
                            text = { Text(text = tab, fontSize = 13.sp) }
                        )
                    }
                }
            }
        }
        
        // Detail Content
        Box(modifier = Modifier.weight(1f).padding(16.dp)) {
            when (activeSubTab) {
                0 -> GeneralSettings(editingApp) { editingApp = it }
                1 -> BrandingSettings(editingApp) { editingApp = it }
                2 -> AppContactSupportSettings(editingApp) { editingApp = it }
                3 -> AppAuditLogs(editingApp.id)
            }
        }
    }
}

@Composable
fun GeneralSettings(app: AppTenantModel, onUpdate: (AppTenantModel) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Identity & Info", fontWeight = FontWeight.Bold)
                OutlinedTextField(value = app.name, onValueChange = { onUpdate(app.copy(name = it)) }, label = { Text("App Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = app.shortName, onValueChange = { onUpdate(app.copy(shortName = it)) }, label = { Text("Short Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = app.examName, onValueChange = { onUpdate(app.copy(examName = it)) }, label = { Text("Exam Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = app.orgName, onValueChange = { onUpdate(app.copy(orgName = it)) }, label = { Text("Organization Name") }, modifier = Modifier.fillMaxWidth())
                
                Text("Language / Medium", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                val langOptions = com.example.util.MediumStandardizer.STANDARDIZED_LANGUAGES
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(langOptions) { lang ->
                        val cleanCurrent = com.example.util.MediumStandardizer.standardizeLanguageName(app.medium)
                        FilterChip(
                            selected = cleanCurrent.equals(lang, ignoreCase = true),
                            onClick = {
                                onUpdate(app.copy(
                                    medium = lang,
                                    primaryLanguage = lang,
                                    allowedLanguages = listOf(lang)
                                ))
                            },
                            label = { Text(lang, fontSize = 12.sp) }
                        )
                    }
                }

                OutlinedTextField(value = app.description, onValueChange = { onUpdate(app.copy(description = it)) }, label = { Text("App Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
        }
        
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Technical & Compliance", fontWeight = FontWeight.Bold)
                OutlinedTextField(value = app.packageName, onValueChange = { onUpdate(app.copy(packageName = it)) }, label = { Text("Android Package Name") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = app.versionName, onValueChange = { onUpdate(app.copy(versionName = it)) }, label = { Text("Version") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = app.versionCode.toString(), onValueChange = { onUpdate(app.copy(versionCode = it.toIntOrNull() ?: 1)) }, label = { Text("Code") }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun BrandingSettings(app: AppTenantModel, onUpdate: (AppTenantModel) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Primary Theme Colors", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                PremiumVisualColorPicker(
                    label = "Primary Brand Color",
                    selectedColor = app.primaryColorHex,
                    onColorSelected = { onUpdate(app.copy(primaryColorHex = it)) }
                )
                
                PremiumVisualColorPicker(
                    label = "Secondary Variant Color",
                    selectedColor = app.secondaryColorHex,
                    onColorSelected = { onUpdate(app.copy(secondaryColorHex = it)) }
                )
                
                PremiumVisualColorPicker(
                    label = "Accent / CTA Action Color",
                    selectedColor = app.accentColorHex,
                    onColorSelected = { onUpdate(app.copy(accentColorHex = it)) }
                )
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Surface & Background Colors", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                PremiumVisualColorPicker(
                    label = "App Background Color",
                    selectedColor = app.backgroundColorHex,
                    onColorSelected = { onUpdate(app.copy(backgroundColorHex = it)) }
                )

                PremiumVisualColorPicker(
                    label = "Surface / Card Color",
                    selectedColor = app.surfaceColorHex,
                    onColorSelected = { onUpdate(app.copy(surfaceColorHex = it)) }
                )
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Typography & Status Colors", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                PremiumVisualColorPicker(
                    label = "Main Text Color",
                    selectedColor = app.textColorHex,
                    onColorSelected = { onUpdate(app.copy(textColorHex = it)) }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        PremiumVisualColorPicker(
                            label = "Success Color",
                            selectedColor = app.successColorHex,
                            onColorSelected = { onUpdate(app.copy(successColorHex = it)) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PremiumVisualColorPicker(
                            label = "Error Color",
                            selectedColor = app.errorColorHex,
                            onColorSelected = { onUpdate(app.copy(errorColorHex = it)) }
                        )
                    }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("App Typography", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                val fonts = listOf("Roboto", "Plus Jakarta Sans", "Montserrat", "Poppins", "Inter")
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fonts.forEach { font ->
                        FilterChip(
                            selected = app.fontFamily == font,
                            onClick = { onUpdate(app.copy(fontFamily = font)) },
                            label = { Text(font) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppAuditLogs(appId: String) {
    val logs = remember(appId) { SampleData.auditLogsState.filter { it.appId == appId } }
    
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No activity logs recorded for this app yet.", color = Color(0xFF64748B))
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(logs) { log ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when(log.action) {
                                    "CREATED" -> Icons.Default.Add
                                    "EDITED" -> Icons.Default.Edit
                                    "STATUS_CHANGE" -> Icons.Default.Sync
                                    else -> Icons.Default.History
                                },
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "${log.action}: ${log.details ?: ""}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "By ${log.adminName} • ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm").format(java.util.Date(log.timestamp))}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppContactSupportSettings(app: AppTenantModel, onUpdate: (AppTenantModel) -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Support Email Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Email Support", fontSize = 14.sp)
                    Switch(
                        checked = app.isEmailSupportEnabled,
                        onCheckedChange = { onUpdate(app.copy(isEmailSupportEnabled = it)) }
                    )
                }
                OutlinedTextField(
                    value = app.supportEmail,
                    onValueChange = { onUpdate(app.copy(supportEmail = it)) },
                    label = { Text("Support Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = app.isEmailSupportEnabled
                )
                if (app.supportEmail.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            if (!app.supportEmail.contains("@") || !app.supportEmail.contains(".")) {
                                Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${app.supportEmail}")
                                    putExtra(Intent.EXTRA_SUBJECT, "Test Support Inquiry")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                uriHandler.openUri("mailto:${app.supportEmail}")
                            }
                        }
                    ) {
                        Text("Test Email Configuration")
                    }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("WhatsApp Support Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable WhatsApp Support", fontSize = 14.sp)
                    Switch(
                        checked = app.isWhatsAppEnabled,
                        onCheckedChange = { onUpdate(app.copy(isWhatsAppEnabled = it)) }
                    )
                }
                OutlinedTextField(
                    value = app.whatsappNumber,
                    onValueChange = { onUpdate(app.copy(whatsappNumber = it)) },
                    label = { Text("WhatsApp Number (e.g. +919876543210)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = app.isWhatsAppEnabled
                )
                OutlinedTextField(
                    value = app.whatsappLink,
                    onValueChange = { onUpdate(app.copy(whatsappLink = it)) },
                    label = { Text("WhatsApp Link (Optional override)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = app.isWhatsAppEnabled
                )
                if (app.whatsappNumber.isNotBlank() || app.whatsappLink.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            val target = app.whatsappLink.ifBlank { "https://wa.me/${app.whatsappNumber.replace(Regex("[^0-9+]"), "")}" }
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(target))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                uriHandler.openUri(target)
                            }
                        }
                    ) {
                        Text("Test WhatsApp Link")
                    }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Telegram Support Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Telegram Support", fontSize = 14.sp)
                    Switch(
                        checked = app.isTelegramEnabled,
                        onCheckedChange = { onUpdate(app.copy(isTelegramEnabled = it)) }
                    )
                }
                OutlinedTextField(
                    value = app.telegramId,
                    onValueChange = { onUpdate(app.copy(telegramId = it)) },
                    label = { Text("Telegram ID / Username (e.g. @support)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = app.isTelegramEnabled
                )
                OutlinedTextField(
                    value = app.telegramLink,
                    onValueChange = { onUpdate(app.copy(telegramLink = it)) },
                    label = { Text("Telegram Link (Optional override)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = app.isTelegramEnabled
                )
                if (app.telegramId.isNotBlank() || app.telegramLink.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            val target = app.telegramLink.ifBlank { "https://t.me/${app.telegramId.removePrefix("@")}" }
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(target))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                uriHandler.openUri(target)
                            }
                        }
                    ) {
                        Text("Test Telegram Link")
                    }
                }
            }
        }
    }
}
