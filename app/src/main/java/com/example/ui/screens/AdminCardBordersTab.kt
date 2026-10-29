package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.CardBorderConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCardBordersTab() {
    val context = LocalContext.current
    val currentConfig = SampleData.cardBorderConfigState.value
    val appsList = SampleData.appsListState

    var topBorderColorHex by remember { mutableStateOf(currentConfig.topBorderColorHex) }
    var otherBorderColorHex by remember { mutableStateOf(currentConfig.otherBorderColorHex) }
    var topBorderWidthDp by remember { mutableStateOf(currentConfig.topBorderWidthDp) }
    var otherBorderWidthDp by remember { mutableStateOf(currentConfig.otherBorderWidthDp) }
    var cornerRadiusDp by remember { mutableStateOf(currentConfig.cornerRadiusDp) }
    var targetScope by remember { mutableStateOf(currentConfig.targetScope) }
    var selectedAppIds by remember { mutableStateOf(currentConfig.selectedAppIds) }

    fun saveConfig() {
        val updated = CardBorderConfig(
            topBorderColorHex = topBorderColorHex,
            otherBorderColorHex = otherBorderColorHex,
            topBorderWidthDp = topBorderWidthDp,
            otherBorderWidthDp = otherBorderWidthDp,
            cornerRadiusDp = cornerRadiusDp,
            targetScope = targetScope,
            selectedAppIds = selectedAppIds
        )
        SampleData.updateCardBorderConfig(updated)
        com.example.util.FirebaseSyncManager.syncCardBorderConfigToFirestore(SampleData.currentActiveAppId.value, updated)
        Toast.makeText(context, "Card Borders & Styling saved & synchronized with User App!", Toast.LENGTH_SHORT).show()
    }

    fun resetConfig() {
        SampleData.resetCardBorderConfig()
        val fresh = SampleData.cardBorderConfigState.value
        topBorderColorHex = fresh.topBorderColorHex
        otherBorderColorHex = fresh.otherBorderColorHex
        topBorderWidthDp = fresh.topBorderWidthDp
        otherBorderWidthDp = fresh.otherBorderWidthDp
        cornerRadiusDp = fresh.cornerRadiusDp
        targetScope = fresh.targetScope
        selectedAppIds = fresh.selectedAppIds
        Toast.makeText(context, "Card borders reset to default.", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BorderAll, contentDescription = null, tint = Color(0xFFF97316), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Home Screen Card Borders & Styling",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Customize card border thickness, orange top accent color, and corner radius across apps.",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Live Preview Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "LIVE CARD PREVIEW",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF2563EB)
                    )

                    // Preview Card
                    val previewBorderWidth = maxOf(otherBorderWidthDp, topBorderWidthDp, 2).dp
                    val isGoldAccent = topBorderColorHex == 0xFFFFD700L || topBorderColorHex == 0xFFF59E0BL || otherBorderColorHex == 0xFFD4AF37L
                    val previewBorderBrush = if (isGoldAccent) {
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(
                                Color(0xFFFFD700),
                                Color(0xFFFFFBEB),
                                Color(0xFFF59E0B),
                                Color(0xFFD4AF37),
                                Color(0xFFFEF3C7),
                                Color(0xFFFFD700)
                            )
                        )
                    } else {
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(
                                Color(topBorderColorHex),
                                Color.White.copy(alpha = 0.85f),
                                Color(otherBorderColorHex),
                                Color(topBorderColorHex).copy(alpha = 0.90f),
                                Color(otherBorderColorHex)
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(cornerRadiusDp.dp), ambientColor = Color(0x330F172A), spotColor = Color(0x3D000000))
                            .border(BorderStroke(previewBorderWidth, previewBorderBrush), RoundedCornerShape(cornerRadiusDp.dp))
                            .clip(RoundedCornerShape(cornerRadiusDp.dp))
                            .background(Color(0xFF0F172A))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Subjects / Current Affairs Card Preview",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        // Inner depth bevel frame
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .border(
                                    BorderStroke(
                                        0.75.dp,
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.35f),
                                                Color.White.copy(alpha = 0.08f),
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.22f)
                                            )
                                        )
                                    ),
                                    RoundedCornerShape(cornerRadiusDp.dp)
                                )
                        )
                        // Top luminous accent line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        listOf(
                                            Color(otherBorderColorHex).copy(alpha = 0.3f),
                                            Color(topBorderColorHex).copy(alpha = 0.85f),
                                            Color(0xFFFFFBEB),
                                            Color(topBorderColorHex).copy(alpha = 0.85f),
                                            Color(otherBorderColorHex).copy(alpha = 0.3f)
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }

        // Border Customization Controls
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "BORDER PROPERTIES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )

                    // Top Border Color Presets
                    Text("Top Accent Color (Default Imperial Gold #FFD700)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val colors = listOf(
                            0xFFFFD700L to "Imperial Gold",
                            0xFFF59E0BL to "Amber Gold",
                            0xFFF97316L to "Sunset Orange",
                            0xFF2563EBL to "Royal Blue",
                            0xFF10B981L to "Emerald"
                        )
                        colors.forEach { (hex, label) ->
                            val isSelected = topBorderColorHex == hex
                            Surface(
                                onClick = { topBorderColorHex = hex },
                                color = Color(hex),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp),
                                border = BorderStroke(if (isSelected) 2.5.dp else 1.dp, if (isSelected) Color.Black else Color.LightGray)
                            ) {}
                        }
                    }

                    // Perimeter Border Color Presets
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Perimeter Rim Color (Default Metallic Gold #D4AF37)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val otherColors = listOf(
                            0xFFD4AF37L to "Metallic Gold",
                            0xFFFFD700L to "Bright Gold",
                            0xFFFDE68AL to "Champagne",
                            0xFF1E3E62L to "Navy Rim",
                            0xFF334155L to "Slate Gray"
                        )
                        otherColors.forEach { (hex, label) ->
                            val isSelected = otherBorderColorHex == hex
                            Surface(
                                onClick = { otherBorderColorHex = hex },
                                color = Color(hex),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp),
                                border = BorderStroke(if (isSelected) 2.5.dp else 1.dp, if (isSelected) Color.Black else Color.LightGray)
                            ) {}
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Top Border Width: $topBorderWidthDp dp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    Slider(
                        value = topBorderWidthDp.toFloat(),
                        onValueChange = { topBorderWidthDp = it.toInt() },
                        valueRange = 1f..8f,
                        steps = 7
                    )

                    Text("Other Borders Width: $otherBorderWidthDp dp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    Slider(
                        value = otherBorderWidthDp.toFloat(),
                        onValueChange = { otherBorderWidthDp = it.toInt() },
                        valueRange = 0f..4f,
                        steps = 4
                    )

                    Text("Corner Radius: $cornerRadiusDp dp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                    Slider(
                        value = cornerRadiusDp.toFloat(),
                        onValueChange = { cornerRadiusDp = it.toInt() },
                        valueRange = 4f..28f,
                        steps = 24
                    )
                }
            }
        }

        // Target App Selection
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TARGET APP SELECTION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )

                    val scopes = listOf(
                        "CURRENT" to "Current App Only (${SampleData.getActiveApp().name})",
                        "SELECTED" to "Selected Apps",
                        "ALL" to "All Active Apps (${appsList.size})"
                    )

                    scopes.forEach { (key, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { targetScope = key }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = targetScope == key,
                                onClick = { targetScope = key }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                        }
                    }

                    if (targetScope == "SELECTED") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Select Target Apps:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                        appsList.forEach { app ->
                            val isChecked = selectedAppIds.contains(app.id)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedAppIds = if (isChecked) selectedAppIds - app.id else selectedAppIds + app.id
                                    }
                                    .padding(vertical = 2.dp, horizontal = 8.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedAppIds = if (checked) selectedAppIds + app.id else selectedAppIds - app.id
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(app.name, fontSize = 12.5.sp, color = Color(0xFF334155))
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { resetConfig() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset Defaults")
                }
                Button(
                    onClick = { saveConfig() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save & Apply")
                }
            }
        }
    }
}
