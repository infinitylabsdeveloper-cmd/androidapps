package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuxuryFontStudioScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val currentConfig by LuxuryFontStudioManager.fontConfig.collectAsState()
    var tempConfig by remember(currentConfig) { mutableStateOf(currentConfig) }

    val scrollState = rememberScrollState()

    // Font families list
    val fontFamilies = listOf(
        "Roboto", "Poppins", "Montserrat", "Lora",
        "Noto Sans", "Noto Serif", "Noto Sans Telugu", "Mandali", "Tenali Rama"
    )
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredFonts = remember(searchQuery, selectedCategory) {
        fontFamilies.filter { font ->
            val matchesSearch = font.contains(searchQuery, ignoreCase = true)
            val matchesCategory = when (selectedCategory) {
                "English" -> font in listOf("Roboto", "Poppins", "Montserrat", "Lora", "Noto Sans", "Noto Serif")
                "Telugu" -> font in listOf("Noto Sans Telugu", "Mandali", "Tenali Rama")
                "Serif" -> font in listOf("Lora", "Noto Serif")
                "Sans Serif" -> font in listOf("Roboto", "Poppins", "Montserrat", "Noto Sans")
                else -> true
            }
            matchesSearch && matchesCategory
        }
    }

    val textColours = listOf("#292524", "#64748B", "#B76E79", "#D4AF37", "#800020", "#EC4899", "#F07836", "#2563EB", "#059669", "#7C3AED")
    val backgroundColours = listOf("#FFF1E6", "#FFF8F2", "#FFFFFF", "#F8BBD0", "#FAF7F2", "#E6E6FA", "#B2AC88", "#80CBC4", "#87CEEB", "#ECC66A")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Font Studio", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF881337))
                        Text("Customize your reading experience", fontSize = 12.sp, color = Color(0xFFB76E79))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFE0CC),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF292524))
                            }
                        }
                    }
                },
                actions = {
                    TextButton(onClick = { tempConfig = LuxuryFontConfig() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF292524), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", color = Color(0xFF292524), fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFF1E6))
            )
        },
        bottomBar = {
            Surface(
                color = Color(0xFFFFF1E6),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFE8C5A5)),
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF292524))
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { tempConfig = LuxuryFontConfig() },
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFE8C5A5)),
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF292524))
                    ) {
                        Text("Reset to Default", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            LuxuryFontStudioManager.updateConfig(context, tempConfig)
                            Toast.makeText(context, "Font Studio settings applied successfully!", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                        modifier = Modifier.weight(1.2f).height(48.dp)
                    ) {
                        Text("Apply Settings", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        },
        containerColor = Color(0xFFFFF1E6)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live Preview Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, Color(0xFFE8C5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Live Preview", fontWeight = FontWeight.Bold, color = Color(0xFF292524), fontSize = 16.sp)
                        Surface(color = Color(0xFFFFF1E6), shape = RoundedCornerShape(12.dp)) {
                            Text("See how text looks", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = Color(0xFFB76E79))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(android.graphics.Color.parseColor(tempConfig.backgroundColor)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "APPSC Group-2 Preparation",
                                fontSize = tempConfig.fontSizeSp.sp,
                                fontWeight = when(tempConfig.fontWeight) {
                                    "Light" -> FontWeight.Light
                                    "Medium" -> FontWeight.Medium
                                    "Semi Bold" -> FontWeight.SemiBold
                                    "Bold" -> FontWeight.Bold
                                    else -> FontWeight.Bold
                                },
                                color = Color(android.graphics.Color.parseColor(tempConfig.textColor))
                            )
                            Text(
                                text = "Your Success Our Mission",
                                fontSize = (tempConfig.fontSizeSp * 0.8f).sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "మీ లక్ష్యం మా సంకల్పం",
                                fontSize = tempConfig.fontSizeSp.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(android.graphics.Color.parseColor(tempConfig.textColor))
                            )
                            Text(
                                text = "మీ అభ్యాసాన్ని మీకు నచ్చిన శైలిలో కొనసాగించండి",
                                fontSize = (tempConfig.fontSizeSp * 0.8f).sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // 1. Font Family Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE8C5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Font Family", fontWeight = FontWeight.Bold, color = Color(0xFF292524), fontSize = 15.sp)
                    Text("Choose your preferred font", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search fonts...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEC4899),
                            unfocusedBorderColor = Color(0xFFE8C5A5)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("All", "English", "Telugu", "Serif", "Sans Serif")) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFEC4899),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Font grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredFonts.chunked(3).forEach { rowFonts ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowFonts.forEach { font ->
                                    val isSelected = tempConfig.fontFamily == font
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Color(0xFFFFE0CC) else Color(0xFFFFF8F2),
                                        border = BorderStroke(1.5.dp, if (isSelected) Color(0xFFEC4899) else Color(0xFFE8C5A5)),
                                        modifier = Modifier.weight(1f).clickable { tempConfig = tempConfig.copy(fontFamily = font) }
                                    ) {
                                        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Aa", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF292524))
                                                Text(font, fontSize = 11.sp, color = Color(0xFF64748B), maxLines = 1)
                                            }
                                            if (isSelected) {
                                                Box(modifier = Modifier.align(Alignment.TopEnd).size(16.dp).background(Color(0xFFEC4899), CircleShape), contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                                repeat(3 - rowFonts.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // 2. Font Size Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE8C5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("2. Font Size", fontWeight = FontWeight.Bold, color = Color(0xFF292524), fontSize = 15.sp)
                            Text("Adjust the text size", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFF1E6), border = BorderStroke(1.dp, Color(0xFFE8C5A5))) {
                            Text("${tempConfig.fontSizeSp.toInt()}sp", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontWeight = FontWeight.Bold, color = Color(0xFF292524))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = tempConfig.fontSizeSp,
                        onValueChange = { tempConfig = tempConfig.copy(fontSizeSp = it) },
                        valueRange = 12f..28f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFEC4899), activeTrackColor = Color(0xFFEC4899))
                    )
                }
            }

            // 3. Font Weight Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE8C5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("3. Font Weight", fontWeight = FontWeight.Bold, color = Color(0xFF292524), fontSize = 15.sp)
                    Text("Select text weight", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Light", "Regular", "Medium", "Semi Bold", "Bold").forEach { weight ->
                            val isSelected = tempConfig.fontWeight == weight
                            Button(
                                onClick = { tempConfig = tempConfig.copy(fontWeight = weight) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFFEC4899) else Color(0xFFFFF8F2),
                                    contentColor = if (isSelected) Color.White else Color(0xFF292524)
                                ),
                                border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE8C5A5)) else null,
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(weight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 4. Font Style Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE8C5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("4. Font Style", fontWeight = FontWeight.Bold, color = Color(0xFF292524), fontSize = 15.sp)
                    Text("Choose text style", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Normal", "Italic").forEach { style ->
                            val isSelected = tempConfig.fontStyle == style
                            Button(
                                onClick = { tempConfig = tempConfig.copy(fontStyle = style) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFFEC4899) else Color(0xFFFFF8F2),
                                    contentColor = if (isSelected) Color.White else Color(0xFF292524)
                                ),
                                border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE8C5A5)) else null,
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text(style, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 5. Text Colours Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE8C5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("5. Text Colours", fontWeight = FontWeight.Bold, color = Color(0xFF292524), fontSize = 15.sp)
                    Text("Select text colour", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(textColours) { hex ->
                            val isSelected = tempConfig.textColor == hex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                                    .border(if (isSelected) 2.5.dp else 1.dp, if (isSelected) Color(0xFFEC4899) else Color(0xFFE8C5A5), CircleShape)
                                    .clickable { tempConfig = tempConfig.copy(textColor = hex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 6. Background Colour Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE8C5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("6. Background Colour", fontWeight = FontWeight.Bold, color = Color(0xFF292524), fontSize = 15.sp)
                    Text("Select screen background", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(backgroundColours) { hex ->
                            val isSelected = tempConfig.backgroundColor == hex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                                    .border(if (isSelected) 2.5.dp else 1.dp, if (isSelected) Color(0xFFEC4899) else Color(0xFFE8C5A5), CircleShape)
                                    .clickable { tempConfig = tempConfig.copy(backgroundColor = hex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF292524), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 7. Advanced Settings Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE8C5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("7. Advanced Settings", fontWeight = FontWeight.Bold, color = Color(0xFF292524), fontSize = 15.sp)
                    Text("Letter & line spacing", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Letter Spacing", fontSize = 13.sp, color = Color(0xFF292524))
                        Text(String.format("%.1f", tempConfig.letterSpacing), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
                    }
                    Slider(
                        value = tempConfig.letterSpacing,
                        onValueChange = { tempConfig = tempConfig.copy(letterSpacing = it) },
                        valueRange = 0f..2f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFEC4899), activeTrackColor = Color(0xFFEC4899))
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Line Spacing", fontSize = 13.sp, color = Color(0xFF292524))
                        Text(String.format("%.1f", tempConfig.lineSpacing), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
                    }
                    Slider(
                        value = tempConfig.lineSpacing,
                        onValueChange = { tempConfig = tempConfig.copy(lineSpacing = it) },
                        valueRange = 1.0f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFEC4899), activeTrackColor = Color(0xFFEC4899))
                    )
                }
            }

            // 8. Apply To Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE8C5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("8. Apply To", fontWeight = FontWeight.Bold, color = Color(0xFF292524), fontSize = 15.sp)
                    Text("Choose where to apply", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Whole App", "Home & Navigation", "Reading Content").forEach { scope ->
                            val isSelected = tempConfig.applyScope == scope
                            OutlinedCard(
                                onClick = { tempConfig = tempConfig.copy(applyScope = scope) },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, if (isSelected) Color(0xFFEC4899) else Color(0xFFE8C5A5)),
                                colors = CardDefaults.outlinedCardColors(containerColor = if (isSelected) Color(0xFFFFE0CC) else Color(0xFFFFF8F2)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(scope, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF292524), textAlign = TextAlign.Center, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
