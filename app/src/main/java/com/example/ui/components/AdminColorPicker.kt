package com.example.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumVisualColorPicker(
    label: String,
    selectedColor: Long,
    onColorSelected: (Long) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val color = Color(selectedColor)
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .clickable { showDialog = true }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Instant Preview Box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
                    .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = String.format("#%08X", selectedColor).uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Tap to open visual picker",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
            
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (showDialog) {
        VisualColorPickerDialog(
            initialColor = selectedColor,
            onDismiss = { showDialog = false },
            onConfirm = {
                onColorSelected(it)
                showDialog = false
            }
        )
    }
}

@Composable
private fun VisualColorPickerDialog(
    initialColor: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var currentColor by remember { mutableLongStateOf(initialColor) }
    var hexInput by remember { mutableStateOf(String.format("%06X", initialColor and 0xFFFFFF)) }
    
    val presetColors = listOf(
        // Modern Brand Colors
        0xFF2563EB, 0xFF3B82F6, 0xFF60A5FA, // Blues
        0xFF7C3AED, 0xFF8B5CF6, 0xFFA78BFA, // Violets
        0xFF059669, 0xFF10B981, 0xFF34D399, // Greens
        0xFFD97706, 0xFFF59E0B, 0xFFFBBF24, // Ambers
        0xFFDC2626, 0xFFEF4444, 0xFFF87171, // Reds
        0xFF0891B2, 0xFF06B6D4, 0xFF22D3EE, // Cyans
        0xFF4F46E5, 0xFF6366F1, 0xFF818CF8, // Indigos
        // Neutral / System Colors
        0xFF0F172A, 0xFF1E293B, 0xFF334155, // Dark Slates
        0xFFF8FAFC, 0xFFF1F5F9, 0xFFE2E8F0, // Light Slates
        0xFFFFFFFF, 0xFF000000, 0xFF64748B  // Basic
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Visual Color Picker",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                // Current Selection Preview
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(currentColor))
                            .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Selected HEX", fontSize = 11.sp, color = Color(0xFF64748B))
                        OutlinedTextField(
                            value = hexInput,
                            onValueChange = { input ->
                                if (input.length <= 6) {
                                    hexInput = input.uppercase().filter { it.isDigit() || it in 'A'..'F' }
                                    if (hexInput.length == 6) {
                                        try {
                                            currentColor = 0xFF000000L or hexInput.toLong(16)
                                        } catch (e: Exception) {}
                                    }
                                }
                            },
                            modifier = Modifier.width(120.dp),
                            prefix = { Text("#") },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )
                    }
                }

                Text("Quick Presets", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                
                // Color Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(40.dp),
                    modifier = Modifier.height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetColors) { colorValue ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(colorValue))
                                .border(
                                    width = if (currentColor == colorValue) 3.dp else 1.dp,
                                    color = if (currentColor == colorValue) Color.Black else Color.Black.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                                .clickable { 
                                    currentColor = colorValue
                                    hexInput = String.format("%06X", colorValue and 0xFFFFFF)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentColor == colorValue) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = if (Color(colorValue).isDark()) Color.White else Color.Black, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // Spectrum Slider (Simplified Hue Bar)
                Text("Visual Spectrum", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(currentColor) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

fun Color.isDark(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance < 0.5
}
