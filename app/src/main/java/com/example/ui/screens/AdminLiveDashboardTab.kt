package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.LiveDashboardConfig
import com.example.model.LiveDashboardMetricCardConfig
import com.example.model.parseHexColor

/**
 * Admin Panel Tab for 100% Customization of Live Content Dashboard
 * - Background gradient, border, glow
 * - Title, Subtitle, Live Indicator Color
 * - Live Bank button text and color
 * - Individual Card Titles, Badges, Colors, manual count overrides
 * - Real-time Live Preview
 * - One-tap "Reset to Default (Exact Reference Match)"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLiveDashboardTab() {
    val context = LocalContext.current
    var config by remember { mutableStateOf(SampleData.liveDashboardConfigState.value) }

    val presetColors = listOf(
        "#00D2FF" to "Cyan Blue",
        "#00FF66" to "Lime Green",
        "#B026FF" to "Neon Purple",
        "#FFAA00" to "Amber Gold",
        "#FF2255" to "Crimson Red",
        "#10B981" to "Emerald",
        "#38BDF8" to "Sky Blue",
        "#F43F5E" to "Rose"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F2942))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DashboardCustomize,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Live Content Dashboard Customizer",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Blue
                                )
                                Text(
                                    text = "Customize header, metrics, badges, colors & borders in real-time",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    SampleData.resetLiveDashboardConfig()
                                    config = SampleData.liveDashboardConfigState.value
                                    Toast.makeText(context, "🔄 Reset to Default Exact Design!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset to Default", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    SampleData.updateLiveDashboardConfig(config)
                                    val activeAppId = SampleData.getActiveApp().id
                                    com.example.util.FirebaseSyncManager.syncLiveDashboardConfigToFirestore(activeAppId, config)
                                    Toast.makeText(context, "✅ Live Content Dashboard configuration saved!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Configuration", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section: Live Interactive Preview
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)), // Light Blue (#E0F2FE)
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                            Text("Live Interactive Preview", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                        }
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFDCFCE7)) {
                            Text("100% Pixel Match", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }

                    // Embedded Live Component Preview
                    HomeLiveContentDashboard(
                        displaySubjects = SampleData.getSubjectsForApp(SampleData.getActiveApp().id),
                        currentAppId = SampleData.getActiveApp().id,
                        config = config,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section: Header & Global Controls
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)), // Light Blue (#E0F2FE)
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Header & Global Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = config.headerTitlePrefix,
                            onValueChange = { config = config.copy(headerTitlePrefix = it) },
                            label = { Text("Title Prefix") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = config.headerTitleSuffix,
                            onValueChange = { config = config.copy(headerTitleSuffix = it) },
                            label = { Text("Title Suffix") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = config.headerSubtitle,
                        onValueChange = { config = config.copy(headerSubtitle = it) },
                        label = { Text("Subtitle") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = config.containerBgStart,
                            onValueChange = { config = config.copy(containerBgStart = it) },
                            label = { Text("Card Bg Start (Hex)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = config.containerBgEnd,
                            onValueChange = { config = config.copy(containerBgEnd = it) },
                            label = { Text("Card Bg End (Hex)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = config.liveBankButtonText,
                            onValueChange = { config = config.copy(liveBankButtonText = it) },
                            label = { Text("Live Bank Text") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = config.liveBankButtonColor,
                            onValueChange = { config = config.copy(liveBankButtonColor = it) },
                            label = { Text("Button Color (Hex)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = config.liveIndicatorColor,
                            onValueChange = { config = config.copy(liveIndicatorColor = it) },
                            label = { Text("Pulse Beacon Color (Hex)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = config.containerBorderColor,
                            onValueChange = { config = config.copy(containerBorderColor = it) },
                            label = { Text("Container Border Color") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Section: Individual Card Editors
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Card 1: Subjects", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    MetricCardEditor(
                        cardConfig = config.subjectsCard,
                        onUpdate = { config = config.copy(subjectsCard = it) }
                    )

                    Divider(color = Color(0xFFE2E8F0))

                    Text("Card 2: Units", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    MetricCardEditor(
                        cardConfig = config.unitsCard,
                        onUpdate = { config = config.copy(unitsCard = it) }
                    )

                    Divider(color = Color(0xFFE2E8F0))

                    Text("Card 3: Topics", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    MetricCardEditor(
                        cardConfig = config.topicsCard,
                        onUpdate = { config = config.copy(topicsCard = it) }
                    )

                    Divider(color = Color(0xFFE2E8F0))

                    Text("Card 4: Practice Sets", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    MetricCardEditor(
                        cardConfig = config.practiceSetsCard,
                        onUpdate = { config = config.copy(practiceSetsCard = it) }
                    )

                    Divider(color = Color(0xFFE2E8F0))

                    Text("Card 5: Total MCQs", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    MetricCardEditor(
                        cardConfig = config.totalMcqsCard,
                        onUpdate = { config = config.copy(totalMcqsCard = it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricCardEditor(
    cardConfig: LiveDashboardMetricCardConfig,
    onUpdate: (LiveDashboardMetricCardConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = cardConfig.title,
                onValueChange = { onUpdate(cardConfig.copy(title = it)) },
                label = { Text("Card Title") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = cardConfig.badgeText,
                onValueChange = { onUpdate(cardConfig.copy(badgeText = it)) },
                label = { Text("Badge Text") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = cardConfig.borderColor,
                onValueChange = { onUpdate(cardConfig.copy(borderColor = it, accentGlowColor = it)) },
                label = { Text("Border / Glow (Hex)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = cardConfig.badgeTextColor,
                onValueChange = { onUpdate(cardConfig.copy(badgeTextColor = it)) },
                label = { Text("Badge Text Color") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = cardConfig.bgGradientStart,
                onValueChange = { onUpdate(cardConfig.copy(bgGradientStart = it)) },
                label = { Text("Bg Gradient Start") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = cardConfig.bgGradientEnd,
                onValueChange = { onUpdate(cardConfig.copy(bgGradientEnd = it)) },
                label = { Text("Bg Gradient End") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = cardConfig.overrideValue?.toString() ?: "",
                onValueChange = { input ->
                    val parsed = input.toIntOrNull()
                    onUpdate(cardConfig.copy(overrideValue = parsed))
                },
                label = { Text("Count Override (Optional)") },
                placeholder = { Text("Auto / Dynamic") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            // Color Swatch Badge Preview
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(parseHexColor(cardConfig.borderColor, Color.Cyan))
            )
        }
    }
}
