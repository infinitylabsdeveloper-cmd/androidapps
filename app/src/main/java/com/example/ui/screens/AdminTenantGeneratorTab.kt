package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.AppTenantModel
import com.example.model.AppStatus
import com.example.ui.components.PremiumVisualColorPicker
import com.example.worker.DataSyncAndBackupWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTenantGeneratorTab() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val totalApps = SampleData.appsListState.size
    val nextNumber = totalApps + 1
    val suggestedTenantId = "APP" + nextNumber.toString().padStart(3, '0')

    var tenantId by remember { mutableStateOf(suggestedTenantId) }
    var examName by remember { mutableStateOf("Railway RRB JE") }
    var category by remember { mutableStateOf("CENTRAL RAILWAYS") }
    var medium by remember { mutableStateOf("English") }
    var customPackageName by remember { mutableStateOf("") }
    var primaryColorHex by remember { mutableLongStateOf(0xFF0284C7) }
    var generatedSnippet by remember { mutableStateOf<String?>(null) }
    var showSuccessCard by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var backupStatus by remember { mutableStateOf(DataSyncAndBackupWorker.getLastBackupStatus(context)) }
    var isRunningBackup by remember { mutableStateOf(false) }

    // Auto-compute unique package name based on input
    val cleanCategory = category.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
    val cleanExam = examName.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
    val cleanMedium = com.example.util.MediumStandardizer.standardizeLanguageName(medium)
    val mediumCode = when (cleanMedium.lowercase()) {
        "telugu" -> "te"
        "hindi" -> "hi"
        "urdu" -> "ur"
        "tamil" -> "ta"
        "kannada" -> "kn"
        "malayalam" -> "ml"
        "marathi" -> "mr"
        "gujarati" -> "gu"
        "bengali" -> "bn"
        else -> "en"
    }
    val autoPackage = "com.independent.examapps.$cleanCategory.$cleanExam.$mediumCode"
    val finalPackage = customPackageName.ifBlank { autoPackage }
    val fullAppName = "$examName ($cleanMedium)"
    val shortName = "${examName.take(10).uppercase()} (${mediumCode.uppercase()})"
    val uniqueAppId = "$cleanCategory-$cleanExam-$mediumCode"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            // Stats Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFE8DC)),
                border = BorderStroke(1.dp, Color(0xFFD8CEBD)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Multi-Tenant App Platform",
                                color = Color(0xFF047857),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$totalApps Apps Configured",
                                color = Color(0xFF062C22),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Text(
                                text = "Next: $suggestedTenantId",
                                color = Color(0xFF15803D),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Deploy 662+ independent Play Store APKs/AABs from this single Kotlin + Jetpack Compose codebase.",
                        color = Color(0xFF1C2D27),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // WorkManager Storage Audit & Backup Snapshot Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFEFF6FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "WorkManager Auto-Sync & Backup",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Automated 24h background file scan & multi-tenant backup",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = Color(0xFF166534),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Storage Audit", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (backupStatus.scannedFileCount > 0) "${backupStatus.scannedFileCount} files (${backupStatus.formattedSize})" else "Awaiting scan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Latest Snapshot", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (backupStatus.lastBackupTimestamp > 0) backupStatus.formattedDate else "No backup yet",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status: ${backupStatus.lastStatus}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (backupStatus.lastStatus.contains("SUCCESS")) Color(0xFF16A34A) else Color(0xFF64748B)
                        )
                        if (backupStatus.lastBackupFileName.isNotBlank()) {
                            Text(
                                text = backupStatus.lastBackupFileName,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (!isRunningBackup) {
                                isRunningBackup = true
                                DataSyncAndBackupWorker.runImmediate(context)
                                Toast.makeText(context, "WorkManager file scan & snapshot started", Toast.LENGTH_SHORT).show()
                                coroutineScope.launch {
                                    delay(2000)
                                    backupStatus = DataSyncAndBackupWorker.getLastBackupStatus(context)
                                    isRunningBackup = false
                                }
                            }
                        },
                        enabled = !isRunningBackup,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    ) {
                        if (isRunningBackup) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Running Background Scan & Backup...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Trigger WorkManager Scan & Backup Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Generate New Unique App Tenant",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        item {
            OutlinedTextField(
                value = tenantId,
                onValueChange = { tenantId = it },
                label = { Text("Tenant / App ID (Immutable)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF2563EB)) }
            )
        }

        item {
            OutlinedTextField(
                value = examName,
                onValueChange = { examName = it },
                label = { Text("Exam Name (e.g. Railway RRB, SSC CGL)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.School, contentDescription = null) }
            )
        }

        item {
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category / State / Agency") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
            )
        }

        item {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("App Brand Identity", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                PremiumVisualColorPicker(
                    label = "Primary App Color",
                    selectedColor = primaryColorHex,
                    onColorSelected = { primaryColorHex = it }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val mediums = com.example.util.MediumStandardizer.STANDARDIZED_LANGUAGES
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Select Medium / Language", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(mediums.size) { idx ->
                            val m = mediums[idx]
                            val isSel = cleanMedium.equals(m, ignoreCase = true)
                            FilterChip(
                                selected = isSel,
                                onClick = { medium = m },
                                label = { Text(m, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = finalPackage,
                onValueChange = { customPackageName = it },
                label = { Text("Unique Play Store Package ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("Auto-generated compliant package namespace") },
                leadingIcon = { Icon(Icons.Default.Apps, contentDescription = null) }
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Generated Profile Preview", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• App Name: $fullAppName", fontSize = 12.sp, color = Color(0xFF475569))
                    Text("• Tenant ID: $tenantId", fontSize = 12.sp, color = Color(0xFF475569))
                    Text("• Internal Slug: $uniqueAppId", fontSize = 12.sp, color = Color(0xFF475569))
                    Text("• Package ID: $finalPackage", fontSize = 12.sp, color = Color(0xFF475569))
                    Text("• Medium: $cleanMedium ($mediumCode)", fontSize = 12.sp, color = Color(0xFF475569))
                }
            }
        }

        item {
            Button(
                onClick = {
                    val newTenant = AppTenantModel(
                        id = uniqueAppId,
                        name = fullAppName,
                        shortName = shortName,
                        category = category,
                        examName = examName,
                        medium = cleanMedium,
                        primaryLanguage = cleanMedium,
                        allowedLanguages = listOf(cleanMedium),
                        languageCode = mediumCode,
                        packageName = finalPackage,
                        primaryColorHex = primaryColorHex,
                        status = AppStatus.ACTIVE
                    )
                    // Check duplicate
                    if (SampleData.appsListState.any { it.id == uniqueAppId || it.packageName == finalPackage }) {
                        Toast.makeText(context, "An app with this ID/Package already exists!", Toast.LENGTH_SHORT).show()
                    } else {
                        SampleData.appsListState.add(newTenant)
                        showSuccessCard = true
                        generatedSnippet = """
                            # --- Gradle Build Command ---
                            ./gradlew assembleRelease -PappId="$uniqueAppId" -PtenantId="$tenantId" -PappPackage="$finalPackage"

                            # --- .env Configuration ---
                            APP_ID=$uniqueAppId
                            TENANT_ID=$tenantId
                            PACKAGE_NAME=$finalPackage
                            EXAM_NAME="$examName"
                            MEDIUM="$cleanMedium"
                            API_BASE_URL="https://api.aspirantportal.com"
                        """.trimIndent()
                        Toast.makeText(context, "✅ App Tenant '$fullAppName' generated successfully!", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Icon(Icons.Default.AddBox, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate & Register App Tenant", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        if (showSuccessCard && generatedSnippet != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tenant Registered! Build Ready", fontWeight = FontWeight.Bold, color = Color(0xFF15803D), fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = generatedSnippet ?: "",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(generatedSnippet ?: ""))
                                    Toast.makeText(context, "Build snippet copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Build CLI", fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    SampleData.setActiveApp(uniqueAppId)
                                    Toast.makeText(context, "Switched active app to $fullAppName", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Preview App", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

