package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed as lazyItemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.MonthlyCurrentAffairsModel
import com.example.ui.components.AppFhdImage
import com.example.ui.components.PremiumLockBadge
import com.example.ui.components.PremiumUpgradePromptDialog
import com.example.ui.components.PremiumSubscriptionBadge
import com.example.ui.components.PracticeAppearancePanel
import com.example.ui.components.premiumPress
import com.example.ui.components.premiumTargetAnimation
import com.example.util.PracticeAppearanceManager
import com.example.util.FontSettingsManager
import com.example.util.PracticeSessionManager
import com.example.repository.AppConfigRepository
import com.example.model.PracticeAppearanceSettings
import android.widget.Toast
import androidx.compose.material.icons.filled.Palette
import com.example.ui.theme.*
import com.example.util.SetThemeManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt



private fun safeParseColor(hexStr: String?, defaultColor: Color): Color {
    if (hexStr.isNullOrBlank()) return defaultColor
    return try {
        val cleaned = hexStr.trim()
            .removePrefix("#")
            .removePrefix("0x")
            .removePrefix("0X")
        val colorLong = when (cleaned.length) {
            3 -> {
                val r = cleaned[0].toString().repeat(2)
                val g = cleaned[1].toString().repeat(2)
                val b = cleaned[2].toString().repeat(2)
                ("FF" + r + g + b).toLong(16)
            }
            6 -> {
                ("FF" + cleaned).toLong(16)
            }
            8 -> {
                cleaned.toLong(16)
            }
            else -> return defaultColor
        }
        Color(colorLong)
    } catch (e: Exception) {
        defaultColor
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentAffairsScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToPractice: (String, String, String, String) -> Unit, // subjectId, unitId, topicId, level
    onNavigateToSubscription: () -> Unit = {}
) {
    val context = LocalContext.current
    val configRepository = remember { AppConfigRepository.getInstance() }
    val currentAppConfig by configRepository.currentAppConfig.collectAsState()
    var showPracticeAppearanceSheet by remember { mutableStateOf(false) }

    var isDark by remember { mutableStateOf(false) }
    val isPremium = SampleData.isUserPremium()
    var selectedMonthlyCA by remember { mutableStateOf<MonthlyCurrentAffairsModel?>(null) }
    var upgradePromptInfo by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    val monthlyCA = SampleData.getMonthlyCurrentAffairs()

    BackHandler {
        if (selectedMonthlyCA != null) {
            selectedMonthlyCA = null
        } else {
            onBack()
        }
    }

    AppLuminousBackground(screenName = "Current Affairs") {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (selectedMonthlyCA == null) "Current Affairs" else selectedMonthlyCA!!.title,
                                color = AppTextDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    },
                    navigationIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                if (selectedMonthlyCA != null) {
                                    selectedMonthlyCA = null
                                } else {
                                    onBack()
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppTextDark)
                            }
                            IconButton(onClick = onNavigateToHome) {
                                Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF2563EB))
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showPracticeAppearanceSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Practice Appearance Settings",
                                tint = AppTextDark
                            )
                        }
                        PremiumSubscriptionBadge(
                            onNavigateToSubscription = onNavigateToSubscription,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        IconButton(onClick = { isDark = !isDark }) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = AppTextDark
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (selectedMonthlyCA == null) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "MONTHLY COMPILATIONS",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }

                    if (monthlyCA.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                                    .premiumTargetAnimation(
                                        target = com.example.model.AnimatableTarget.EMPTY_ERROR_STATES,
                                        index = 0,
                                        screenKey = "Current Affairs"
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("No current affairs available.", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    Text("Monthly Current Affairs sets configured by Admin will appear here.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }
                    } else {
                        gridItemsIndexed(monthlyCA, key = { _, it -> it.id }) { caIdx, ca ->
                            val defaultCaCardImage = SampleData.getCardImagesForAppAndType(SampleData.currentActiveAppId.value, "CURRENT_AFFAIRS")
                                .firstOrNull { it.isEnabled }?.imageUrl?.ifEmpty { null }
                            val bgWallpaperUrl = if (ca.cardImageUrl.isNotBlank()) {
                                ca.cardImageUrl
                            } else {
                                defaultCaCardImage ?: "https://images.unsplash.com/photo-1504711434969-e33886168f5c?q=80&w=800&auto=format&fit=crop"
                            }
                            val completedCount = ca.sets.count { set ->
                                PracticeSessionManager.getSessionStatus(context, set.id, false).equals("Completed", true)
                            }
                            val isCompletedCA = ca.sets.isNotEmpty() && completedCount == ca.sets.size
                            val isInProgressCA = !isCompletedCA && (completedCount > 0 || ca.sets.any { set ->
                                PracticeSessionManager.getSessionStatus(context, set.id, false).equals("In Progress", true)
                            })
                            val caStatus = when {
                                isCompletedCA -> "Completed"
                                isInProgressCA -> "In Progress"
                                else -> "Not Started"
                            }
                            val isCompleted = caStatus.equals("Completed", true)
                            val isInProgress = caStatus.equals("In Progress", true)

                            val vibrantBg = PremiumCardColorSystem.vibrantReferenceColors[caIdx % PremiumCardColorSystem.vibrantReferenceColors.size]
                            val cardBgColor = when {
                                isCompleted -> Color(0xFF86EFAC) // Rich mint green
                                isInProgress -> Color(0xFFFDE047) // Rich amber-orange
                                else -> vibrantBg
                            }
                            val cardBorderColor = when {
                                isCompleted -> Color(0xFF059669)
                                isInProgress -> Color(0xFFD97706)
                                else -> Color.White.copy(alpha = 0.5f)
                            }
                            val cardTextColor = when {
                                isCompleted -> Color(0xFF064E3B)
                                isInProgress -> Color(0xFF78350F)
                                else -> Color.White
                            }
                            val badgeBgColor = when {
                                isCompleted -> Color(0xFF059669)
                                isInProgress -> Color(0xFFD97706)
                                else -> Color(0xFF7C3AED)
                            }

                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                border = BorderStroke(1.8.dp, cardBorderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .premiumTargetAnimation(
                                        target = com.example.model.AnimatableTarget.TOPIC_CARDS,
                                        index = caIdx,
                                        screenKey = "Current Affairs"
                                    )
                                    .premiumPress(
                                        target = com.example.model.AnimatableTarget.TOPIC_CARDS,
                                        screenKey = "Current Affairs"
                                    ) { selectedMonthlyCA = ca }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = badgeBgColor
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(
                                                    text = if (isCompleted) "✓ Completed" else if (isInProgress) "🕒 In Progress" else "🕒 Not Started",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Surface(
                                        shape = CircleShape,
                                        color = badgeBgColor.copy(alpha = 0.2f),
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Today,
                                                contentDescription = null,
                                                tint = badgeBgColor,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = ca.month,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = cardTextColor
                                    )
                                    Text(
                                        text = ca.year,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = cardTextColor.copy(alpha = 0.8f)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = badgeBgColor
                                        ) {
                                            Text(
                                                text = "${ca.sets.size} Sets",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        if (!isPremium) {
                                            PremiumLockBadge(text = "PRO", isSmall = true)
                                        }
                                    }
                                }
                            }
                    }
                    }
                }
            } else {
                // Show Sets for selected month
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    item {
                        Text(
                            text = "Practice sets for ${selectedMonthlyCA?.title ?: ""}. Each set contains up to 10 MCQs.",
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    lazyItemsIndexed(selectedMonthlyCA?.sets ?: emptyList(), key = { _, it -> it.id }) { setIdx, set ->
                        val isLocked = !isPremium && setIdx > 0
                        val appearance = SetThemeManager.getSetAppearance(set)
                        
                        val bgColor = safeParseColor(appearance.backgroundColor, if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF))
                        val accentColor = safeParseColor(appearance.accentColor, if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB))
                        val textColor = safeParseColor(appearance.textColor, if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A))
                        val iconBgColor = safeParseColor(appearance.iconBackgroundColor, if (isDark) Color(0xFF4C1D95) else Color(0xFFF5F3FF))
                        val iconColor = safeParseColor(appearance.iconColor, if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED))
                        
                        val gradient = if (appearance.gradientColors != null) {
                            Brush.linearGradient(appearance.gradientColors.map { safeParseColor(it, accentColor) })
                        } else {
                            Brush.linearGradient(listOf(accentColor, accentColor))
                        }

                        Card(
                            shape = RoundedCornerShape(appearance.cornerRadiusDp.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(1.dp, if (isLocked) Color(0xFFFFD700).copy(alpha = 0.6f) else if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = appearance.shadowElevationDp.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .premiumTargetAnimation(
                                    target = com.example.model.AnimatableTarget.SETS_CARDS,
                                    index = setIdx,
                                    screenKey = "Current Affairs"
                                )
                                .premiumPress(
                                    target = com.example.model.AnimatableTarget.SETS_CARDS,
                                    screenKey = "Current Affairs"
                                ) {
                                    if (isLocked) {
                                        upgradePromptInfo = Triple(
                                            "Unlock Current Affairs",
                                            "${selectedMonthlyCA!!.title} - ${set.title}",
                                            "All Current Affairs MCQs and monthly compilations are exclusive to Premium subscribers. Upgrade today to practice all sets!"
                                        )
                                    } else {
                                        onNavigateToPractice(
                                            "subj-current-affairs",
                                            selectedMonthlyCA!!.id,
                                            set.id,
                                            set.title
                                        )
                                    }
                                }
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().background(bgColor)) {
                                if (appearance.backgroundPattern != null) {
                                    // Optionally implement background patterns here
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isLocked) Color(0xFF0F172A) else iconBgColor,
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (isLocked) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = "Locked",
                                                        tint = Color(0xFFFFD700),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = set.title.replace("Set ", ""),
                                                        fontWeight = FontWeight.Black,
                                                        color = iconColor
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = set.title,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = textColor
                                                )
                                                if (isLocked) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    PremiumLockBadge(text = "LOCKED", isSmall = true)
                                                }
                                            }
                                            Text(
                                                text = "${set.questions.size} Questions • MCQ Practice",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                            )
                                        }
                                    }
                                    
                                    if (isLocked) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color(0xFF0F172A),
                                            border = BorderStroke(1.dp, Color(0xFFFFD700))
                                        ) {
                                            Text(
                                                text = "Unlock (Pro)",
                                                color = Color(0xFFFFD700),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircle,
                                            contentDescription = "Start Practice",
                                            tint = accentColor,
                                            modifier = Modifier.size(32.dp)
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

    if (upgradePromptInfo != null) {
        val info = upgradePromptInfo!!
        PremiumUpgradePromptDialog(
            title = info.first,
            featureName = info.second,
            description = info.third,
            onDismiss = { upgradePromptInfo = null },
            onUpgradeClick = {
                upgradePromptInfo = null
                onNavigateToSubscription()
            }
        )
    }

    if (showPracticeAppearanceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPracticeAppearanceSheet = false },
            containerColor = Color.White
        ) {
            PracticeAppearancePanel(
                currentSettings = currentAppConfig.advancedFontSettings.practiceAppearance,
                onSave = { newPa ->
                    PracticeAppearanceManager.saveSettings(context, newPa)
                    val updatedAdvanced = currentAppConfig.advancedFontSettings.copy(
                        practiceAppearance = newPa
                    )
                    FontSettingsManager.saveSettings(context, updatedAdvanced)
                    configRepository.updateAppConfig(
                        currentAppConfig.copy(advancedFontSettings = updatedAdvanced)
                    )
                    showPracticeAppearanceSheet = false
                    Toast.makeText(context, "Practice appearance applied!", Toast.LENGTH_SHORT).show()
                },
                onReset = {
                    PracticeAppearanceManager.resetToDefault(context)
                    val defPa = PracticeAppearanceSettings()
                    val updatedAdvanced = currentAppConfig.advancedFontSettings.copy(
                        practiceAppearance = defPa
                    )
                    FontSettingsManager.saveSettings(context, updatedAdvanced)
                    configRepository.updateAppConfig(
                        currentAppConfig.copy(advancedFontSettings = updatedAdvanced)
                    )
                    showPracticeAppearanceSheet = false
                    Toast.makeText(context, "Practice appearance reset to default", Toast.LENGTH_SHORT).show()
                },
                onBack = { showPracticeAppearanceSheet = false }
            )
        }
    }
}

