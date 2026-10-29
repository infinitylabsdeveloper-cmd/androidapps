package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.PracticeAppearanceSettings
import com.example.model.PracticeSectionAppearance
import com.example.model.BurstEffectType
import com.example.model.PracticeSectionType
import com.example.util.FontSettingsManager
import com.example.util.PracticeAppearanceManager

data class PracticeThemePresetItem(
    val id: String,
    val name: String,
    val description: String,
    val topColor: Color,
    val midColor: Color,
    val bottomColor: Color,
    val settings: PracticeAppearanceSettings
)

object PracticeThemePresets {
    val presets = listOf(
        PracticeThemePresetItem(
            id = "classic",
            name = "Classic",
            description = "Warm yellow question card with sky blue options and mint explanation",
            topColor = Color(0xFFFEF3C7),
            midColor = Color(0xFFBAE6FD),
            bottomColor = Color(0xFF0284C7),
            settings = PracticeAppearanceSettings(
                presetName = "Classic",
                screenBackgroundColor = "#FFF7ED",
                questionCard = PracticeSectionAppearance(
                    backgroundColor = "#FDE68A",
                    textColor = "#0F172A",
                    fontFamily = "Poppins",
                    fontSize = 16,
                    borderColor = "#FDE68A",
                    borderWidth = 1,
                    cornerRadius = 16
                ),
                options = PracticeSectionAppearance(
                    backgroundColor = "#FFFFFF",
                    textColor = "#0F172A",
                    fontFamily = "Poppins",
                    fontSize = 15,
                    borderColor = "#E2E8F0",
                    borderWidth = 1,
                    cornerRadius = 14
                ),
                explanation = PracticeSectionAppearance(
                    backgroundColor = "#86EFAC",
                    textColor = "#15803D",
                    fontFamily = "Poppins",
                    fontSize = 14,
                    borderColor = "#BBF7D0",
                    borderWidth = 1,
                    cornerRadius = 16
                )
            )
        ),
        PracticeThemePresetItem(
            id = "ocean",
            name = "Ocean",
            description = "Crisp azure blue and cyan tones for refreshing clarity",
            topColor = Color(0xFF38BDF8),
            midColor = Color(0xFFBAE6FD),
            bottomColor = Color(0xFF0369A1),
            settings = PracticeAppearanceSettings(
                presetName = "Ocean",
                screenBackgroundColor = "#F0F9FF",
                questionCard = PracticeSectionAppearance(
                    backgroundColor = "#E0F2FE",
                    textColor = "#0C4A6E",
                    fontFamily = "Poppins",
                    fontSize = 16,
                    borderColor = "#7DD3FC",
                    borderWidth = 1,
                    cornerRadius = 16
                ),
                options = PracticeSectionAppearance(
                    backgroundColor = "#BAE6FD",
                    textColor = "#0369A1",
                    fontFamily = "Poppins",
                    fontSize = 15,
                    borderColor = "#38BDF8",
                    borderWidth = 1,
                    cornerRadius = 14
                ),
                explanation = PracticeSectionAppearance(
                    backgroundColor = "#CCFBF1",
                    textColor = "#0F766E",
                    fontFamily = "Poppins",
                    fontSize = 14,
                    borderColor = "#5EEAD4",
                    borderWidth = 1,
                    cornerRadius = 16
                )
            )
        ),
        PracticeThemePresetItem(
            id = "forest",
            name = "Forest",
            description = "Natural calm focus tones with emerald and sage highlights",
            topColor = Color(0xFF22C55E),
            midColor = Color(0xFF86EFAC),
            bottomColor = Color(0xFF15803D),
            settings = PracticeAppearanceSettings(
                presetName = "Forest",
                screenBackgroundColor = "#F0FDF4",
                questionCard = PracticeSectionAppearance(
                    backgroundColor = "#DCFCE7",
                    textColor = "#14532D",
                    fontFamily = "Poppins",
                    fontSize = 16,
                    borderColor = "#86EFAC",
                    borderWidth = 1,
                    cornerRadius = 16
                ),
                options = PracticeSectionAppearance(
                    backgroundColor = "#BBF7D0",
                    textColor = "#166534",
                    fontFamily = "Poppins",
                    fontSize = 15,
                    borderColor = "#4ADE80",
                    borderWidth = 1,
                    cornerRadius = 14
                ),
                explanation = PracticeSectionAppearance(
                    backgroundColor = "#CCFBF1",
                    textColor = "#0D9488",
                    fontFamily = "Poppins",
                    fontSize = 14,
                    borderColor = "#99F6E4",
                    borderWidth = 1,
                    cornerRadius = 16
                )
            )
        ),
        PracticeThemePresetItem(
            id = "sunset",
            name = "Sunset",
            description = "Warm coral and amber tones with vibrant highlights",
            topColor = Color(0xFFFB923C),
            midColor = Color(0xFFFED7AA),
            bottomColor = Color(0xFFC2410C),
            settings = PracticeAppearanceSettings(
                presetName = "Sunset",
                screenBackgroundColor = "#FFF7ED",
                questionCard = PracticeSectionAppearance(
                    backgroundColor = "#FFEDD5",
                    textColor = "#7C2D12",
                    fontFamily = "Poppins",
                    fontSize = 16,
                    borderColor = "#FDBA74",
                    borderWidth = 1,
                    cornerRadius = 16
                ),
                options = PracticeSectionAppearance(
                    backgroundColor = "#FED7AA",
                    textColor = "#9A3412",
                    fontFamily = "Poppins",
                    fontSize = 15,
                    borderColor = "#FB923C",
                    borderWidth = 1,
                    cornerRadius = 14
                ),
                explanation = PracticeSectionAppearance(
                    backgroundColor = "#FEF08A",
                    textColor = "#854D0E",
                    fontFamily = "Poppins",
                    fontSize = 14,
                    borderColor = "#FDE047",
                    borderWidth = 1,
                    cornerRadius = 16
                )
            )
        ),
        PracticeThemePresetItem(
            id = "royal",
            name = "Royal",
            description = "Prestigious deep violet and lavender aesthetics",
            topColor = Color(0xFF818CF8),
            midColor = Color(0xFFC7D2FE),
            bottomColor = Color(0xFF4338CA),
            settings = PracticeAppearanceSettings(
                presetName = "Royal",
                screenBackgroundColor = "#F5F3FF",
                questionCard = PracticeSectionAppearance(
                    backgroundColor = "#EDE9FE",
                    textColor = "#4C1D95",
                    fontFamily = "Poppins",
                    fontSize = 16,
                    borderColor = "#C4B5FD",
                    borderWidth = 1,
                    cornerRadius = 16
                ),
                options = PracticeSectionAppearance(
                    backgroundColor = "#DDD6FE",
                    textColor = "#5B21B6",
                    fontFamily = "Poppins",
                    fontSize = 15,
                    borderColor = "#A78BFA",
                    borderWidth = 1,
                    cornerRadius = 14
                ),
                explanation = PracticeSectionAppearance(
                    backgroundColor = "#F5D0FE",
                    textColor = "#86198F",
                    fontFamily = "Poppins",
                    fontSize = 14,
                    borderColor = "#F0ABFC",
                    borderWidth = 1,
                    cornerRadius = 16
                )
            )
        ),
        PracticeThemePresetItem(
            id = "minimal",
            name = "Minimal",
            description = "Clean grayscale monochrome layout with crisp borders",
            topColor = Color(0xFF64748B),
            midColor = Color(0xFFCBD5E1),
            bottomColor = Color(0xFF1E293B),
            settings = PracticeAppearanceSettings(
                presetName = "Minimal",
                screenBackgroundColor = "#FFFFFF",
                questionCard = PracticeSectionAppearance(
                    backgroundColor = "#F8FAFC",
                    textColor = "#0F172A",
                    fontFamily = "Poppins",
                    fontSize = 16,
                    borderColor = "#CBD5E1",
                    borderWidth = 1,
                    cornerRadius = 16
                ),
                options = PracticeSectionAppearance(
                    backgroundColor = "#F1F5F9",
                    textColor = "#334155",
                    fontFamily = "Poppins",
                    fontSize = 15,
                    borderColor = "#E2E8F0",
                    borderWidth = 1,
                    cornerRadius = 14
                ),
                explanation = PracticeSectionAppearance(
                    backgroundColor = "#E2E8F0",
                    textColor = "#1E293B",
                    fontFamily = "Poppins",
                    fontSize = 14,
                    borderColor = "#CBD5E1",
                    borderWidth = 1,
                    cornerRadius = 16
                )
            )
        ),
        PracticeThemePresetItem(
            id = "dark_pro",
            name = "Dark Pro",
            description = "High-contrast dark mode optimized for night study",
            topColor = Color(0xFF1E293B),
            midColor = Color(0xFF475569),
            bottomColor = Color(0xFF0F172A),
            settings = PracticeAppearanceSettings(
                presetName = "Dark Pro",
                screenBackgroundColor = "#0B1120",
                questionCard = PracticeSectionAppearance(
                    backgroundColor = "#1E293B",
                    textColor = "#F8FAFC",
                    fontFamily = "Poppins",
                    fontSize = 16,
                    borderColor = "#334155",
                    borderWidth = 1,
                    cornerRadius = 16
                ),
                options = PracticeSectionAppearance(
                    backgroundColor = "#0F172A",
                    textColor = "#93C5FD",
                    fontFamily = "Poppins",
                    fontSize = 15,
                    borderColor = "#1E3A8A",
                    borderWidth = 1,
                    cornerRadius = 14
                ),
                explanation = PracticeSectionAppearance(
                    backgroundColor = "#064E3B",
                    textColor = "#A7F3D0",
                    fontFamily = "Poppins",
                    fontSize = 14,
                    borderColor = "#059669",
                    borderWidth = 1,
                    cornerRadius = 16
                )
            )
        ),
        PracticeThemePresetItem(
            id = "pastel",
            name = "Pastel",
            description = "Soft pink and lavender gradients with tender pastel tones",
            topColor = Color(0xFFF472B6),
            midColor = Color(0xFFFBCFE8),
            bottomColor = Color(0xFF818CF8),
            settings = PracticeAppearanceSettings(
                presetName = "Pastel",
                screenBackgroundColor = "#FFF1F2",
                questionCard = PracticeSectionAppearance(
                    backgroundColor = "#FCE7F3",
                    textColor = "#831843",
                    fontFamily = "Poppins",
                    fontSize = 16,
                    borderColor = "#FBCFE8",
                    borderWidth = 1,
                    cornerRadius = 16
                ),
                options = PracticeSectionAppearance(
                    backgroundColor = "#EDE9FE",
                    textColor = "#5B21B6",
                    fontFamily = "Poppins",
                    fontSize = 15,
                    borderColor = "#DDD6FE",
                    borderWidth = 1,
                    cornerRadius = 14
                ),
                explanation = PracticeSectionAppearance(
                    backgroundColor = "#E0F2FE",
                    textColor = "#0369A1",
                    fontFamily = "Poppins",
                    fontSize = 14,
                    borderColor = "#BAE6FD",
                    borderWidth = 1,
                    cornerRadius = 16
                )
            )
        )
    )
}

val curatedPalette = listOf(
    "#FFFBEB", "#FEF3C7", "#FDE68A", "#FED7AA", "#FFEDD5",
    "#E0F2FE", "#BAE6FD", "#7DD3FC", "#F0F9FF", "#DBEAFE",
    "#DCFCE7", "#BBF7D0", "#86EFAC", "#F0FDF4", "#CCFBF1",
    "#EDE9FE", "#DDD6FE", "#C4B5FD", "#F5F3FF", "#FCE7F3",
    "#F8FAFC", "#F1F5F9", "#E2E8F0", "#CBD5E1", "#FFFFFF",
    "#0F172A", "#1E293B", "#334155", "#0284C7", "#15803D",
    "#D97706", "#EA580C", "#DC2626", "#7C3AED", "#0D9488"
)

fun parseHexColor(hex: String, fallback: Color = Color.White): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = when (clean.length) {
            6 -> (0xFF000000 or clean.toLong(16)).toInt()
            8 -> clean.toLong(16).toInt()
            else -> return fallback
        }
        Color(colorInt)
    } catch (e: Exception) {
        fallback
    }
}

fun colorToHex(color: Color): String {
    val a = (color.alpha * 255).toInt()
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()
    return String.format("#%02X%02X%02X", r, g, b)
}

fun resolveFontFamily(name: String): FontFamily {
    return when (name.lowercase()) {
        "poppins", "sans-serif", "roboto", "inter" -> FontFamily.SansSerif
        "serif", "playfair display", "lora", "merriweather" -> FontFamily.Serif
        "monospace", "mono" -> FontFamily.Monospace
        else -> FontFamily.Default
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeAppearancePanel(
    currentSettings: PracticeAppearanceSettings,
    onSave: (PracticeAppearanceSettings) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var tempSettings by remember(currentSettings) { mutableStateOf(currentSettings) }
    var selectedTab by remember { mutableStateOf("Appearance") }
    var activeColorDialogTarget by remember { mutableStateOf<String?>(null) }
    var fontDropdownExpanded by remember { mutableStateOf(false) }

    var selectedSectionForEffect by remember { mutableStateOf(PracticeSectionType.SETS) }
    var previewTrigger by remember { mutableIntStateOf(0) }
    var previewEffectName by remember { mutableStateOf(BurstEffectType.DEFAULT.displayName) }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 680

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. TOP APP BAR (Dark Navy Gradient Header)
            TopHeaderBar(
                onBack = onBack
            )

            // 2. MAIN BODY (Left Controls + Right Live Preview)
            if (isWideScreen) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column (Settings Controls)
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        TabSelectorRow(
                            selectedTab = selectedTab,
                            onSelectTab = { selectedTab = it }
                        )

                        when (selectedTab) {
                            "Appearance" -> {
                                BackgroundColoursCard(
                                    settings = tempSettings,
                                    onOpenColorPicker = { activeColorDialogTarget = it }
                                )

                                FontSettingsCard(
                                    settings = tempSettings,
                                    fontDropdownExpanded = fontDropdownExpanded,
                                    onToggleFontDropdown = { fontDropdownExpanded = it },
                                    onSelectFont = {
                                        tempSettings = tempSettings.copy(
                                            questionCard = tempSettings.questionCard.copy(fontFamily = it),
                                            options = tempSettings.options.copy(fontFamily = it),
                                            explanation = tempSettings.explanation.copy(fontFamily = it)
                                        )
                                        fontDropdownExpanded = false
                                    },
                                    onFontSizeChange = { newSize ->
                                        tempSettings = tempSettings.copy(
                                            questionCard = tempSettings.questionCard.copy(fontSize = newSize),
                                            options = tempSettings.options.copy(fontSize = (newSize - 1).coerceAtLeast(12)),
                                            explanation = tempSettings.explanation.copy(fontSize = (newSize - 2).coerceAtLeast(12))
                                        )
                                    },
                                    onOpenColorPicker = { activeColorDialogTarget = it }
                                )

                                PresetThemesCard(
                                    selectedPresetName = tempSettings.presetName,
                                    onSelectPreset = { preset ->
                                        tempSettings = preset.settings
                                    }
                                )

                                AnswerFeedbackEffectsSummaryCard(
                                    settings = tempSettings,
                                    onNavigateToEffects = { selectedTab = "Effects" }
                                )

                                BottomActionButtons(
                                    onReset = {
                                        val def = PracticeAppearanceSettings()
                                        tempSettings = def
                                        PracticeAppearanceManager.resetEffectsToDefault(context)
                                        onReset()
                                        Toast.makeText(context, "Reset to default settings", Toast.LENGTH_SHORT).show()
                                    },
                                    onApply = {
                                        PracticeSectionType.entries.forEach { s ->
                                            PracticeAppearanceManager.saveEffectForSection(context, s, tempSettings.getEffectForSection(s))
                                        }
                                        PracticeAppearanceManager.saveSettings(context, tempSettings)
                                        onSave(tempSettings)
                                        Toast.makeText(context, "Appearance applied successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            "Effects" -> {
                                AnswerFeedbackEffectsCard(
                                    currentSettings = tempSettings,
                                    selectedSection = selectedSectionForEffect,
                                    onSelectSection = { selectedSectionForEffect = it },
                                    onSelectEffect = { sec, eff ->
                                        tempSettings = tempSettings.withEffectForSection(sec, eff.displayName)
                                        previewEffectName = eff.displayName
                                        previewTrigger++
                                    },
                                    onTriggerPreview = { effName ->
                                        previewEffectName = effName
                                        previewTrigger++
                                    },
                                    onResetToDefault = {
                                        PracticeAppearanceManager.resetEffectsToDefault(context)
                                        tempSettings = tempSettings.copy(
                                            setsEffect = BurstEffectType.DEFAULT.displayName,
                                            subtopicsEffect = BurstEffectType.DEFAULT.displayName,
                                            currentAffairsEffect = BurstEffectType.DEFAULT.displayName,
                                            offlineEffect = BurstEffectType.DEFAULT.displayName
                                        )
                                        previewEffectName = BurstEffectType.DEFAULT.displayName
                                        previewTrigger++
                                        onReset()
                                        Toast.makeText(context, "✓ Reset all 4 sections to Confetti Pop", Toast.LENGTH_SHORT).show()
                                    },
                                    onApply = {
                                        PracticeSectionType.entries.forEach { s ->
                                            PracticeAppearanceManager.saveEffectForSection(context, s, tempSettings.getEffectForSection(s))
                                        }
                                        PracticeAppearanceManager.saveSettings(context, tempSettings)
                                        onSave(tempSettings)
                                        Toast.makeText(context, "✓ Effects applied successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            "Layout" -> {
                                BackgroundColoursCard(
                                    settings = tempSettings,
                                    onOpenColorPicker = { activeColorDialogTarget = it }
                                )
                                BottomActionButtons(
                                    onReset = {
                                        val def = PracticeAppearanceSettings()
                                        tempSettings = def
                                        PracticeAppearanceManager.resetEffectsToDefault(context)
                                        onReset()
                                        Toast.makeText(context, "Reset to default settings", Toast.LENGTH_SHORT).show()
                                    },
                                    onApply = {
                                        PracticeSectionType.entries.forEach { s ->
                                            PracticeAppearanceManager.saveEffectForSection(context, s, tempSettings.getEffectForSection(s))
                                        }
                                        PracticeAppearanceManager.saveSettings(context, tempSettings)
                                        onSave(tempSettings)
                                        Toast.makeText(context, "Appearance applied successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            "Preview" -> {
                                InteractivePracticePreviewCard(
                                    settings = tempSettings,
                                    onTriggerEffect = { effectName ->
                                        previewEffectName = effectName
                                        previewTrigger++
                                    }
                                )
                                BottomActionButtons(
                                    onReset = {
                                        val def = PracticeAppearanceSettings()
                                        tempSettings = def
                                        PracticeAppearanceManager.resetEffectsToDefault(context)
                                        onReset()
                                        Toast.makeText(context, "Reset to default settings", Toast.LENGTH_SHORT).show()
                                    },
                                    onApply = {
                                        PracticeSectionType.entries.forEach { s ->
                                            PracticeAppearanceManager.saveEffectForSection(context, s, tempSettings.getEffectForSection(s))
                                        }
                                        PracticeAppearanceManager.saveSettings(context, tempSettings)
                                        onSave(tempSettings)
                                        Toast.makeText(context, "Appearance applied successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Right Column (Live Interactive Preview Panel)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        InteractivePracticePreviewCard(
                            settings = tempSettings,
                            onTriggerEffect = { effectName ->
                                previewEffectName = effectName
                                previewTrigger++
                            }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            } else {
                // Mobile Layout (Tabs & Scrollable)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TabSelectorRow(
                        selectedTab = selectedTab,
                        onSelectTab = { selectedTab = it }
                    )

                    when (selectedTab) {
                        "Appearance" -> {
                            BackgroundColoursCard(
                                settings = tempSettings,
                                onOpenColorPicker = { activeColorDialogTarget = it }
                            )

                            FontSettingsCard(
                                settings = tempSettings,
                                fontDropdownExpanded = fontDropdownExpanded,
                                onToggleFontDropdown = { fontDropdownExpanded = it },
                                onSelectFont = {
                                    tempSettings = tempSettings.copy(
                                        questionCard = tempSettings.questionCard.copy(fontFamily = it),
                                        options = tempSettings.options.copy(fontFamily = it),
                                        explanation = tempSettings.explanation.copy(fontFamily = it)
                                    )
                                    fontDropdownExpanded = false
                                },
                                onFontSizeChange = { newSize ->
                                    tempSettings = tempSettings.copy(
                                        questionCard = tempSettings.questionCard.copy(fontSize = newSize),
                                        options = tempSettings.options.copy(fontSize = (newSize - 1).coerceAtLeast(12)),
                                        explanation = tempSettings.explanation.copy(fontSize = (newSize - 2).coerceAtLeast(12))
                                    )
                                },
                                onOpenColorPicker = { activeColorDialogTarget = it }
                            )

                            PresetThemesCard(
                                selectedPresetName = tempSettings.presetName,
                                onSelectPreset = { preset ->
                                    tempSettings = preset.settings
                                }
                            )

                            AnswerFeedbackEffectsSummaryCard(
                                settings = tempSettings,
                                onNavigateToEffects = { selectedTab = "Effects" }
                            )

                            BottomActionButtons(
                                onReset = {
                                    val def = PracticeAppearanceSettings()
                                    tempSettings = def
                                    PracticeAppearanceManager.resetEffectsToDefault(context)
                                    onReset()
                                    Toast.makeText(context, "Reset to default settings", Toast.LENGTH_SHORT).show()
                                },
                                onApply = {
                                    PracticeSectionType.entries.forEach { s ->
                                        PracticeAppearanceManager.saveEffectForSection(context, s, tempSettings.getEffectForSection(s))
                                    }
                                    PracticeAppearanceManager.saveSettings(context, tempSettings)
                                    onSave(tempSettings)
                                    Toast.makeText(context, "Appearance applied successfully!", Toast.LENGTH_SHORT).show()
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            InteractivePracticePreviewCard(
                                settings = tempSettings,
                                onTriggerEffect = { effectName ->
                                    previewEffectName = effectName
                                    previewTrigger++
                                }
                            )
                        }

                        "Effects" -> {
                            AnswerFeedbackEffectsCard(
                                currentSettings = tempSettings,
                                selectedSection = selectedSectionForEffect,
                                onSelectSection = { selectedSectionForEffect = it },
                                onSelectEffect = { sec, eff ->
                                    tempSettings = tempSettings.withEffectForSection(sec, eff.displayName)
                                    previewEffectName = eff.displayName
                                    previewTrigger++
                                },
                                onTriggerPreview = { effName ->
                                    previewEffectName = effName
                                    previewTrigger++
                                },
                                onResetToDefault = {
                                    PracticeAppearanceManager.resetEffectsToDefault(context)
                                    tempSettings = tempSettings.copy(
                                        setsEffect = BurstEffectType.DEFAULT.displayName,
                                        subtopicsEffect = BurstEffectType.DEFAULT.displayName,
                                        currentAffairsEffect = BurstEffectType.DEFAULT.displayName,
                                        offlineEffect = BurstEffectType.DEFAULT.displayName
                                    )
                                    previewEffectName = BurstEffectType.DEFAULT.displayName
                                    previewTrigger++
                                    onReset()
                                    Toast.makeText(context, "✓ Reset all 4 sections to Confetti Pop", Toast.LENGTH_SHORT).show()
                                },
                                onApply = {
                                    PracticeSectionType.entries.forEach { s ->
                                        PracticeAppearanceManager.saveEffectForSection(context, s, tempSettings.getEffectForSection(s))
                                    }
                                    PracticeAppearanceManager.saveSettings(context, tempSettings)
                                    onSave(tempSettings)
                                    Toast.makeText(context, "✓ Effects applied successfully!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        "Layout" -> {
                            BackgroundColoursCard(
                                settings = tempSettings,
                                onOpenColorPicker = { activeColorDialogTarget = it }
                            )
                            BottomActionButtons(
                                onReset = {
                                    val def = PracticeAppearanceSettings()
                                    tempSettings = def
                                    PracticeAppearanceManager.resetEffectsToDefault(context)
                                    onReset()
                                    Toast.makeText(context, "Reset to default settings", Toast.LENGTH_SHORT).show()
                                },
                                onApply = {
                                    PracticeSectionType.entries.forEach { s ->
                                        PracticeAppearanceManager.saveEffectForSection(context, s, tempSettings.getEffectForSection(s))
                                    }
                                    PracticeAppearanceManager.saveSettings(context, tempSettings)
                                    onSave(tempSettings)
                                    Toast.makeText(context, "Appearance applied successfully!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        "Preview" -> {
                            InteractivePracticePreviewCard(
                                settings = tempSettings,
                                onTriggerEffect = { effectName ->
                                    previewEffectName = effectName
                                    previewTrigger++
                                }
                            )

                            BottomActionButtons(
                                onReset = {
                                    val def = PracticeAppearanceSettings()
                                    tempSettings = def
                                    PracticeAppearanceManager.resetEffectsToDefault(context)
                                    onReset()
                                    Toast.makeText(context, "Reset to default settings", Toast.LENGTH_SHORT).show()
                                },
                                onApply = {
                                    PracticeSectionType.entries.forEach { s ->
                                        PracticeAppearanceManager.saveEffectForSection(context, s, tempSettings.getEffectForSection(s))
                                    }
                                    PracticeAppearanceManager.saveSettings(context, tempSettings)
                                    onSave(tempSettings)
                                    Toast.makeText(context, "Appearance applied successfully!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Color Picker Dialog
        activeColorDialogTarget?.let { target ->
            ColorPickerDialog(
                title = when (target) {
                    "q_bg" -> "Question Card Background"
                    "o_bg" -> "Options Cards Background"
                    "e_bg" -> "Explanation Card Background"
                    "s_bg" -> "Screen Background"
                    "q_text" -> "Question Text Color"
                    "o_text" -> "Options Text Color"
                    "e_text" -> "Explanation Text Color"
                    else -> "Choose Color"
                },
                initialColor = when (target) {
                    "q_bg" -> parseHexColor(tempSettings.questionCard.backgroundColor)
                    "o_bg" -> parseHexColor(tempSettings.options.backgroundColor)
                    "e_bg" -> parseHexColor(tempSettings.explanation.backgroundColor)
                    "s_bg" -> parseHexColor(tempSettings.screenBackgroundColor)
                    "q_text" -> parseHexColor(tempSettings.questionCard.textColor)
                    "o_text" -> parseHexColor(tempSettings.options.textColor)
                    "e_text" -> parseHexColor(tempSettings.explanation.textColor)
                    else -> Color.White
                },
                onDismiss = { activeColorDialogTarget = null },
                onColorSelected = { selectedColor ->
                    val hex = colorToHex(selectedColor)
                    tempSettings = when (target) {
                        "q_bg" -> tempSettings.copy(
                            presetName = "Custom",
                            questionCard = tempSettings.questionCard.copy(backgroundColor = hex)
                        )
                        "o_bg" -> tempSettings.copy(
                            presetName = "Custom",
                            options = tempSettings.options.copy(backgroundColor = hex)
                        )
                        "e_bg" -> tempSettings.copy(
                            presetName = "Custom",
                            explanation = tempSettings.explanation.copy(backgroundColor = hex)
                        )
                        "s_bg" -> tempSettings.copy(
                            presetName = "Custom",
                            screenBackgroundColor = hex
                        )
                        "q_text" -> tempSettings.copy(
                            presetName = "Custom",
                            questionCard = tempSettings.questionCard.copy(textColor = hex)
                        )
                        "o_text" -> tempSettings.copy(
                            presetName = "Custom",
                            options = tempSettings.options.copy(textColor = hex)
                        )
                        "e_text" -> tempSettings.copy(
                            presetName = "Custom",
                            explanation = tempSettings.explanation.copy(textColor = hex)
                        )
                        else -> tempSettings
                    }
                    activeColorDialogTarget = null
                }
            )
        }

        // Correct Answer Burst Effect Canvas Overlay
        CorrectAnswerBurstEffect(
            trigger = previewTrigger,
            effectName = previewEffectName,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun TopHeaderBar(
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF0C2340),
                            Color(0xFF0F3562),
                            Color(0xFF13427E)
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Artist Palette Icon (custom drawn canvas)
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(32.dp)) {
                        // Golden wooden palette
                        drawCircle(
                            color = Color(0xFFF59E0B),
                            radius = size.minDimension / 2.2f,
                            center = Offset(size.width * 0.48f, size.height * 0.5f)
                        )
                        // Paint spots: red, blue, green, orange, purple
                        drawCircle(color = Color(0xFFEF4444), radius = 3.2.dp.toPx(), center = Offset(size.width * 0.32f, size.height * 0.32f))
                        drawCircle(color = Color(0xFF3B82F6), radius = 3.2.dp.toPx(), center = Offset(size.width * 0.60f, size.height * 0.30f))
                        drawCircle(color = Color(0xFF10B981), radius = 3.2.dp.toPx(), center = Offset(size.width * 0.68f, size.height * 0.55f))
                        drawCircle(color = Color(0xFFF97316), radius = 3.2.dp.toPx(), center = Offset(size.width * 0.48f, size.height * 0.72f))
                        drawCircle(color = Color(0xFF8B5CF6), radius = 2.8.dp.toPx(), center = Offset(size.width * 0.30f, size.height * 0.58f))
                        // Thumb hole
                        drawCircle(color = Color(0xFF0C2340), radius = 3.5.dp.toPx(), center = Offset(size.width * 0.72f, size.height * 0.42f))
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Titles
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Practice Appearance Settings",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Customize your MCQ Practice experience",
                        color = Color(0xFF93C5FD),
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Far Right Badge: "Make it Yours / Learn in Your Style"
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0E315D),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Column {
                            Text(
                                text = "Make it Yours",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                            Text(
                                text = "Learn in Your Style",
                                color = Color(0xFF93C5FD),
                                fontSize = 8.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabSelectorRow(
    selectedTab: String,
    onSelectTab: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                Triple("Appearance", Icons.Default.Palette, "Appearance"),
                Triple("Effects", Icons.Default.AutoAwesome, "Effects"),
                Triple("Layout", Icons.Default.Tune, "Layout"),
                Triple("Preview", Icons.Outlined.Visibility, "Preview")
            )

            tabs.forEach { (name, icon, label) ->
                val isSelected = selectedTab == name
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFF0052CC) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTab(name) }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) Color.White else Color(0xFF1E293B),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color(0xFF1E293B),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BackgroundColoursCard(
    settings: PracticeAppearanceSettings,
    onOpenColorPicker: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Background Colours",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 15.5.sp
                    )
                    Text(
                        text = "Choose colours for each section",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Question Card
            ColorSettingRow(
                iconColor = Color(0xFFFDE68A),
                label = "Question Card",
                previewHex = settings.questionCard.backgroundColor,
                onClick = { onOpenColorPicker("q_bg") }
            )

            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))

            // 2. Options Cards
            ColorSettingRow(
                iconColor = Color(0xFFBAE6FD),
                label = "Options Cards",
                previewHex = settings.options.backgroundColor,
                onClick = { onOpenColorPicker("o_bg") }
            )

            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))

            // 3. Explanation Card
            ColorSettingRow(
                iconColor = Color(0xFFBBF7D0),
                label = "Explanation Card",
                previewHex = settings.explanation.backgroundColor,
                onClick = { onOpenColorPicker("e_bg") }
            )

            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))

            // 4. Background (Screen)
            ColorSettingRow(
                iconColor = Color(0xFFDDD6FE),
                label = "Background (Screen)",
                previewHex = settings.screenBackgroundColor,
                onClick = { onOpenColorPicker("s_bg") }
            )
        }
    }
}

@Composable
fun ColorSettingRow(
    iconColor: Color,
    label: String,
    previewHex: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Circle Swatch
        Surface(
            shape = CircleShape,
            color = iconColor,
            modifier = Modifier.size(26.dp)
        ) {}

        Spacer(modifier = Modifier.width(12.dp))

        // Label
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        // Right Color Preview Box
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = parseHexColor(previewHex),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.size(width = 54.dp, height = 28.dp)
        ) {}

        Spacer(modifier = Modifier.width(8.dp))

        // Right Chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun FontSettingsCard(
    settings: PracticeAppearanceSettings,
    fontDropdownExpanded: Boolean,
    onToggleFontDropdown: (Boolean) -> Unit,
    onSelectFont: (String) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onOpenColorPicker: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aa",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Font Settings",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 15.5.sp
                    )
                    Text(
                        text = "Choose font style, size and colour",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 1: Font Style Dropdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Font Style",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    fontSize = 14.sp
                )

                Box {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .widthIn(min = 130.dp)
                            .clickable { onToggleFontDropdown(true) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = settings.questionCard.fontFamily,
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = fontDropdownExpanded,
                        onDismissRequest = { onToggleFontDropdown(false) },
                        modifier = Modifier.background(Color.White)
                    ) {
                        listOf("Poppins", "Roboto", "Inter", "Serif", "Open Sans", "Playfair Display", "Monospace").forEach { font ->
                            DropdownMenuItem(
                                text = { Text(font, fontWeight = if (settings.questionCard.fontFamily == font) FontWeight.Bold else FontWeight.Normal) },
                                onClick = { onSelectFont(font) }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))

            // Row 2: Font Size Slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Font Size",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    fontSize = 14.sp,
                    modifier = Modifier.width(100.dp)
                )

                Slider(
                    value = settings.questionCard.fontSize.toFloat(),
                    onValueChange = { onFontSizeChange(it.toInt()) },
                    valueRange = 12f..24f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF0052CC),
                        activeTrackColor = Color(0xFF0052CC),
                        inactiveTrackColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                )

                Text(
                    text = "${settings.questionCard.fontSize}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 14.5.sp,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.End
                )
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))

            // Row 3: Font Colour (Question)
            FontColorRow(
                label = "Font Colour (Question)",
                colorHex = settings.questionCard.textColor,
                onClick = { onOpenColorPicker("q_text") }
            )

            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))

            // Row 4: Font Colour (Options)
            FontColorRow(
                label = "Font Colour (Options)",
                colorHex = settings.options.textColor,
                onClick = { onOpenColorPicker("o_text") }
            )

            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))

            // Row 5: Font Colour (Explanation)
            FontColorRow(
                label = "Font Colour (Explanation)",
                colorHex = settings.explanation.textColor,
                onClick = { onOpenColorPicker("e_text") }
            )
        }
    }
}

@Composable
fun FontColorRow(
    label: String,
    colorHex: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = parseHexColor(colorHex),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.size(width = 36.dp, height = 24.dp)
        ) {}

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun PresetThemesCard(
    selectedPresetName: String,
    onSelectPreset: (PracticeThemePresetItem) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Preset Themes",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 15.5.sp
                    )
                    Text(
                        text = "Quickly apply a beautiful theme",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 8 Presets in 2 rows of 4
            val presets = PracticeThemePresets.presets
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1 (Classic, Ocean, Forest, Sunset)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.take(4).forEach { preset ->
                        PresetThemeItem(
                            preset = preset,
                            isSelected = selectedPresetName.equals(preset.name, ignoreCase = true),
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectPreset(preset) }
                        )
                    }
                }

                // Row 2 (Royal, Minimal, Dark Pro, Pastel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.drop(4).take(4).forEach { preset ->
                        PresetThemeItem(
                            preset = preset,
                            isSelected = selectedPresetName.equals(preset.name, ignoreCase = true),
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectPreset(preset) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PresetThemeItem(
    preset: PracticeThemePresetItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) Color(0xFF0052CC) else Color(0xFFE2E8F0)
            ),
            shadowElevation = if (isSelected) 3.dp else 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
            ) {
                // Mini preview cards layout
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Question Bar
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = preset.topColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                    ) {}

                    // Middle Option Bar
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = preset.midColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                    ) {}

                    // Bottom Bar
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = preset.bottomColor,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(8.dp)
                    ) {}
                }

                // Selection checkmark badge
                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0052CC),
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = preset.name,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isSelected) Color(0xFF0052CC) else Color(0xFF1E293B),
            fontSize = 11.5.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BottomActionButtons(
    onReset: () -> Unit,
    onApply: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Reset to Default Button
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE2E8F0),
            modifier = Modifier
                .weight(1f)
                .clickable { onReset() }
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Reset to Default",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }
        }

        // Apply Settings Button
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0052CC),
            modifier = Modifier
                .weight(1f)
                .clickable { onApply() }
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Apply Settings",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }
        }
    }
}

@Composable
fun LivePreviewCard(
    settings: PracticeAppearanceSettings
) {
    val qBg = parseHexColor(settings.questionCard.backgroundColor)
    val qText = parseHexColor(settings.questionCard.textColor)
    val qFont = resolveFontFamily(settings.questionCard.fontFamily)
    val qSize = settings.questionCard.fontSize.sp

    val oBg = parseHexColor(settings.options.backgroundColor)
    val oText = parseHexColor(settings.options.textColor)
    val oFont = resolveFontFamily(settings.options.fontFamily)
    val oSize = settings.options.fontSize.sp

    val eBg = parseHexColor(settings.explanation.backgroundColor)
    val eText = parseHexColor(settings.explanation.textColor)
    val eFont = resolveFontFamily(settings.explanation.fontFamily)
    val eSize = settings.explanation.fontSize.sp

    val screenBg = parseHexColor(settings.screenBackgroundColor, Color(0xFFF1F5F9))

    Column(modifier = Modifier.fillMaxWidth()) {
        // Live Preview Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Live Preview",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 15.5.sp
                    )
                    Text(
                        text = "See changes instantly",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFDCFCE7)
            ) {
                Text(
                    text = "Live Preview",
                    color = Color(0xFF16A34A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Mock Device Container
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = screenBg),
            border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Navy Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F2C59))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Practice MCQs",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "1/10",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Practice Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Question Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = qBg),
                        border = BorderStroke(1.dp, parseHexColor(settings.questionCard.borderColor, Color(0xFFFDE68A))),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Q",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Which of the following rivers does NOT belong to the Indus river system?",
                                color = qText,
                                fontFamily = qFont,
                                fontSize = qSize,
                                fontWeight = FontWeight.Bold,
                                lineHeight = (qSize.value + 5).sp
                            )
                        }
                    }

                    // Options A, B, C, D
                    val options = listOf(
                        Triple("A", "Jhelum", false),
                        Triple("B", "Chenab", false),
                        Triple("C", "Mahanadi", true),
                        Triple("D", "Ravi", false)
                    )

                    options.forEach { (label, text, isSelected) ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFDCFCE7) else oBg
                            ),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) Color(0xFF16A34A) else parseHexColor(settings.options.borderColor, Color(0xFFBAE6FD))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) Color(0xFF16A34A) else Color(0xFF0284C7),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = text,
                                    color = oText,
                                    fontFamily = oFont,
                                    fontSize = oSize,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )

                                // Radio indicator
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Transparent,
                                    border = BorderStroke(
                                        if (isSelected) 2.dp else 1.5.dp,
                                        if (isSelected) Color(0xFF16A34A) else Color(0xFF94A3B8)
                                    ),
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    if (isSelected) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF16A34A),
                                                modifier = Modifier.size(8.dp)
                                            ) {}
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Explanation Box
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = eBg),
                        border = BorderStroke(1.dp, parseHexColor(settings.explanation.borderColor, Color(0xFFBBF7D0))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Explanation",
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Mahanadi is a peninsular river and does not belong to the Indus river system. Jhelum, Chenab and Ravi are tributaries of the Indus river system.",
                                color = eText,
                                fontFamily = eFont,
                                fontSize = eSize,
                                lineHeight = (eSize.value + 4).sp
                            )
                        }
                    }

                    // Previous / Next Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFDBEAFE),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = null,
                                    tint = Color(0xFF1D4ED8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Previous",
                                    color = Color(0xFF1D4ED8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF0052CC),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Next",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Motivational Artwork & APPSC Group-2 Banner
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFFFFBEB),
                                            Color(0xFFFEF3C7),
                                            Color(0xFFFED7AA)
                                        )
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "“",
                                        color = Color(0xFFF59E0B),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        text = "Small Steps Today",
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = "Lead to Big Success Tomorrow”",
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 13.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "APPSC GROUP-2",
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "DREAM  •  PREPARE  •  ACHIEVE",
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.5.sp,
                                    fontSize = 10.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "— • —",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PracticePreviewSectionData(
    val section: PracticeSectionType,
    val title: String,
    val tabLabel: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

val practicePreviewSections = listOf(
    PracticePreviewSectionData(
        section = PracticeSectionType.SETS,
        title = "Sets Practice",
        tabLabel = "Sets",
        question = "భారతదేశ రాజధాని ఏది?",
        options = listOf("ముంబై", "న్యూఢిల్లీ", "కోల్‌కతా", "చెన్నై"),
        correctIndex = 1,
        explanation = "న్యూఢిల్లీ భారతదేశ రాజధాని. 1911లో కోల్‌కతా నుండి ఢిల్లీకి రాజధాని మార్పు ప్రకటించారు."
    ),
    PracticePreviewSectionData(
        section = PracticeSectionType.SUBTOPICS,
        title = "Subtopics Practice",
        tabLabel = "Subtopics",
        question = "భారత రాజ్యాంగం ఎప్పుడు అమలులోకి వచ్చింది?",
        options = listOf("1947", "1950", "1952", "1935"),
        correctIndex = 1,
        explanation = "భారత రాజ్యాంగం 1950 జనవరి 26న అమలులోకి వచ్చింది."
    ),
    PracticePreviewSectionData(
        section = PracticeSectionType.CURRENT_AFFAIRS,
        title = "Current Affairs Practice",
        tabLabel = "Current Affairs",
        question = "ISRO ప్రధాన కార్యాలయం ఏ నగరంలో ఉంది?",
        options = listOf("హైదరాబాద్", "బెంగళూరు", "చెన్నై", "తిరువనంతపురం"),
        correctIndex = 1,
        explanation = "ISRO ప్రధాన కార్యాలయం బెంగళూరులో ఉంది."
    ),
    PracticePreviewSectionData(
        section = PracticeSectionType.OFFLINE,
        title = "Offline Downloaded Practice",
        tabLabel = "Offline",
        question = "తెలంగాణ రాష్ట్ర రాజధాని ఏది?",
        options = listOf("విజయవాడ", "హైదరాబాద్", "వరంగల్", "కరీంనగర్"),
        correctIndex = 1,
        explanation = "హైదరాబాద్ తెలంగాణ రాష్ట్ర రాజధాని."
    )
)

@Composable
fun InteractivePracticePreviewCard(
    settings: PracticeAppearanceSettings,
    onTriggerEffect: (String) -> Unit
) {
    var selectedPreviewIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }

    val currentData = practicePreviewSections[selectedPreviewIndex.coerceIn(0, practicePreviewSections.size - 1)]
    val sectionEffect = settings.getEffectForSection(currentData.section)

    val qBg = parseHexColor(settings.questionCard.backgroundColor)
    val qText = parseHexColor(settings.questionCard.textColor)
    val qFont = resolveFontFamily(settings.questionCard.fontFamily)
    val qSize = settings.questionCard.fontSize.sp

    val oBg = parseHexColor(settings.options.backgroundColor)
    val oText = parseHexColor(settings.options.textColor)
    val oFont = resolveFontFamily(settings.options.fontFamily)
    val oSize = settings.options.fontSize.sp

    val eBg = parseHexColor(settings.explanation.backgroundColor)
    val eText = parseHexColor(settings.explanation.textColor)
    val eFont = resolveFontFamily(settings.explanation.fontFamily)
    val eSize = settings.explanation.fontSize.sp

    val screenBg = parseHexColor(settings.screenBackgroundColor, Color(0xFFF1F5F9))

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Live Interactive Preview",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 15.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFDCFCE7)
            ) {
                Text(
                    text = "Interactive",
                    color = Color(0xFF16A34A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Section Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            practicePreviewSections.forEachIndexed { idx, sec ->
                val isSelected = selectedPreviewIndex == idx
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFF16A34A) else Color.White,
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF16A34A) else Color(0xFFCBD5E1)),
                    modifier = Modifier.clickable {
                        selectedPreviewIndex = idx
                        selectedOptionIndex = null
                    }
                ) {
                    Text(
                        text = sec.tabLabel,
                        color = if (isSelected) Color.White else Color(0xFF1E293B),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Preview Screen Card Container with localized burst effect
        var cardBurstTrigger by remember { mutableIntStateOf(0) }
        var cardBurstEffect by remember { mutableStateOf(sectionEffect) }

        Box(modifier = Modifier.fillMaxWidth()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = screenBg),
                border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Mini Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F2C59))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${currentData.title} • Q 1/10",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                        IconButton(
                            onClick = { selectedOptionIndex = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset question", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Question Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = qBg),
                        border = BorderStroke(1.dp, parseHexColor(settings.questionCard.borderColor, Color(0xFFFDE68A))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("Q", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = currentData.question,
                                color = qText,
                                fontFamily = qFont,
                                fontSize = qSize,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Options A, B, C, D
                    val optionLabels = listOf("A", "B", "C", "D")
                    currentData.options.forEachIndexed { optIdx, optText ->
                        val isChosen = selectedOptionIndex == optIdx
                        val isCorrect = optIdx == currentData.correctIndex
                        val hasAnswered = selectedOptionIndex != null

                        val optCardBg = when {
                            !hasAnswered -> oBg
                            isCorrect && (hasAnswered && (selectedOptionIndex == currentData.correctIndex || isChosen)) -> Color(0xFFDCFCE7)
                            isChosen && !isCorrect -> Color(0xFFFEE2E2)
                            else -> oBg
                        }

                        val optBorderColor = when {
                            !hasAnswered -> parseHexColor(settings.options.borderColor, Color(0xFFBAE6FD))
                            isCorrect && (hasAnswered && (selectedOptionIndex == currentData.correctIndex || isChosen)) -> Color(0xFF16A34A)
                            isChosen && !isCorrect -> Color(0xFFDC2626)
                            else -> parseHexColor(settings.options.borderColor, Color(0xFFBAE6FD))
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = optCardBg),
                            border = BorderStroke(1.5.dp, optBorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    enabled = selectedOptionIndex == null,
                                    interactionSource = remember(optIdx) { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = ripple()
                                ) {
                                    if (selectedOptionIndex == null) {
                                        selectedOptionIndex = optIdx
                                        if (isCorrect) {
                                            cardBurstEffect = sectionEffect
                                            cardBurstTrigger++
                                            onTriggerEffect(sectionEffect)
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = when {
                                        !hasAnswered -> Color(0xFF0284C7)
                                        isCorrect && (selectedOptionIndex == currentData.correctIndex || isChosen) -> Color(0xFF16A34A)
                                        isChosen && !isCorrect -> Color(0xFFDC2626)
                                        else -> Color(0xFF0284C7)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(optionLabels[optIdx], color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = optText,
                                    color = oText,
                                    fontFamily = oFont,
                                    fontSize = oSize,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )

                                if (hasAnswered && isCorrect) {
                                    Icon(Icons.Default.Check, contentDescription = "Correct", tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                } else if (isChosen && !isCorrect) {
                                    Icon(Icons.Default.Close, contentDescription = "Wrong", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // Explanation Box
                    if (selectedOptionIndex != null) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = eBg),
                            border = BorderStroke(1.dp, parseHexColor(settings.explanation.borderColor, Color(0xFFBBF7D0))),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("వివరణ (Explanation)", color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentData.explanation,
                                    color = eText,
                                    fontFamily = eFont,
                                    fontSize = eSize,
                                    lineHeight = (eSize.value + 4).sp
                                )
                            }
                        }
                    }

                    // Bottom info bar matching HTML
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Effect: $sectionEffect" + if (sectionEffect == BurstEffectType.DEFAULT.displayName) " (default)" else "",
                            fontSize = 11.5.sp,
                            color = Color(0xFF475569),
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(
                            onClick = { selectedOptionIndex = null },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF0052CC))
                                Text("↻ Replay / మళ్ళీ", fontSize = 11.5.sp, color = Color(0xFF0052CC), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Localized Canvas Overlay directly over preview card
        CorrectAnswerBurstEffect(
            trigger = cardBurstTrigger,
            effectName = cardBurstEffect,
            modifier = Modifier.matchParentSize()
        )
    }
}
}

@Composable
fun AnswerFeedbackEffectsCard(
    currentSettings: PracticeAppearanceSettings,
    selectedSection: PracticeSectionType,
    onSelectSection: (PracticeSectionType) -> Unit,
    onSelectEffect: (PracticeSectionType, BurstEffectType) -> Unit,
    onTriggerPreview: (String) -> Unit,
    onResetToDefault: () -> Unit,
    onApply: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Correct-Answer Feedback Effects",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 15.5.sp
                    )
                    Text(
                        text = "Choose separate effects for each practice section",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }

            // Section selection chips
            Text(
                text = "Select Practice Section to Configure:",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155),
                fontSize = 12.5.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PracticeSectionType.entries.forEach { section ->
                    val isSelected = selectedSection == section
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF0052CC) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF0052CC) else Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { onSelectSection(section) }
                    ) {
                        Text(
                            text = when (section) {
                                PracticeSectionType.SETS -> "📚 Sets"
                                PracticeSectionType.SUBTOPICS -> "📑 Subtopics"
                                PracticeSectionType.CURRENT_AFFAIRS -> "📰 Current Affairs"
                                PracticeSectionType.OFFLINE -> "💾 Offline"
                            },
                            color = if (isSelected) Color.White else Color(0xFF1E293B),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.5.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Active section status banner
            val activeEffectForSection = currentSettings.getEffectForSection(selectedSection)
            var cardBurstTrigger by remember { mutableIntStateOf(0) }
            var cardBurstEffect by remember { mutableStateOf(activeEffectForSection) }
            val currentSectionData = remember(selectedSection) {
                practicePreviewSections.find { it.section == selectedSection } ?: practicePreviewSections.first()
            }
            var previewOptionSelected by remember(selectedSection) { mutableStateOf<Int?>(null) }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Currently configuring: ${selectedSection.displayName}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534),
                            fontSize = 12.5.sp
                        )
                        Text(
                            text = "Active effect: $activeEffectForSection" + if (activeEffectForSection == BurstEffectType.DEFAULT.displayName) " (Default)" else "",
                            color = Color(0xFF15803D),
                            fontSize = 11.5.sp
                        )
                    }

                    Button(
                        onClick = {
                            cardBurstEffect = activeEffectForSection
                            cardBurstTrigger++
                            onTriggerPreview(activeEffectForSection)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Text("Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Live Interactive Preview Box inside Effects tab
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = Color(0xFF16A34A), modifier = Modifier.size(10.dp)) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Preview Stage (${selectedSection.displayName})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            TextButton(
                                onClick = {
                                    previewOptionSelected = null
                                    cardBurstEffect = activeEffectForSection
                                    cardBurstTrigger++
                                    onTriggerPreview(activeEffectForSection)
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF16A34A))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Play Preview", fontSize = 11.5.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = currentSectionData.question,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E293B)
                        )

                        // 2 Quick Interactive Options to test answer burst
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentSectionData.options.take(2).forEachIndexed { optIdx, optTxt ->
                                val isChosen = previewOptionSelected == optIdx
                                val isCorrect = optIdx == currentSectionData.correctIndex
                                val hasAnswered = previewOptionSelected != null

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        !hasAnswered -> Color.White
                                        isCorrect && (hasAnswered && (previewOptionSelected == currentSectionData.correctIndex || isChosen)) -> Color(0xFFDCFCE7)
                                        isChosen && !isCorrect -> Color(0xFFFEE2E2)
                                        else -> Color.White
                                    },
                                    border = BorderStroke(
                                        1.5.dp,
                                        when {
                                            !hasAnswered -> Color(0xFFBAE6FD)
                                            isCorrect && (hasAnswered && (previewOptionSelected == currentSectionData.correctIndex || isChosen)) -> Color(0xFF16A34A)
                                            isChosen && !isCorrect -> Color(0xFFDC2626)
                                            else -> Color(0xFFCBD5E1)
                                        }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            if (previewOptionSelected == null) {
                                                previewOptionSelected = optIdx
                                                if (isCorrect) {
                                                    cardBurstEffect = activeEffectForSection
                                                    cardBurstTrigger++
                                                    onTriggerPreview(activeEffectForSection)
                                                }
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${if (optIdx == 0) "A" else "B"}. $optTxt",
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = if (isChosen && isCorrect) Color(0xFF14532D) else Color(0xFF0F172A),
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (hasAnswered && isCorrect) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Effect Burst directly over this live preview stage!
                CorrectAnswerBurstEffect(
                    trigger = cardBurstTrigger,
                    effectName = cardBurstEffect,
                    modifier = Modifier.matchParentSize()
                )
            }

            Text(
                text = "Available Effects (Tap to preview and select):",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155),
                fontSize = 12.5.sp
            )

            // 10 Effects List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BurstEffectType.entries.forEachIndexed { index, effect ->
                    val isChosen = activeEffectForSection.equals(effect.displayName, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isChosen) Color(0xFFE5F6EA) else Color.White,
                        border = BorderStroke(
                            width = if (isChosen) 2.dp else 1.dp,
                            color = if (isChosen) Color(0xFF16A34A) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                cardBurstEffect = effect.displayName
                                cardBurstTrigger++
                                onSelectEffect(selectedSection, effect)
                                onTriggerPreview(effect.displayName)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Radio indicator
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        if (isChosen) Color(0xFF16A34A) else Color(0xFF94A3B8),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChosen) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF16A34A))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Emoji icon
                            Text(
                                text = effect.emoji,
                                fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            // Name & Description
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${index + 1}. ${effect.displayName}",
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isChosen) Color(0xFF14532D) else Color(0xFF0F172A),
                                        fontSize = 13.5.sp
                                    )
                                    if (effect.isDefault) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF16A34A)
                                        ) {
                                            Text(
                                                text = "Default",
                                                color = Color.White,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = effect.description,
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Play button
                            IconButton(
                                onClick = {
                                    cardBurstEffect = effect.displayName
                                    cardBurstTrigger++
                                    onTriggerPreview(effect.displayName)
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = "Test effect",
                                    tint = if (isChosen) Color(0xFF16A34A) else Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // 4-Section Summary Matrix
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Current Section Configuration:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        fontSize = 12.sp
                    )
                    PracticeSectionType.entries.forEach { section ->
                        val eff = currentSettings.getEffectForSection(section)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = section.displayName,
                                color = Color(0xFF64748B),
                                fontSize = 11.5.sp
                            )
                            Text(
                                text = eff + if (eff == BurstEffectType.DEFAULT.displayName) " (Default)" else "",
                                fontWeight = FontWeight.SemiBold,
                                color = if (eff == BurstEffectType.DEFAULT.displayName) Color(0xFF16A34A) else Color(0xFF0052CC),
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }

            // Bottom action buttons: Apply and Reset to Default
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE2E8F0),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onResetToDefault() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reset to Default",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF16A34A),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onApply() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Apply Changes",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnswerFeedbackEffectsSummaryCard(
    settings: PracticeAppearanceSettings,
    onNavigateToEffects: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Answer Feedback Effects",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Independent effect per section (Confetti Pop default)",
                            color = Color(0xFF64748B),
                            fontSize = 11.5.sp
                        )
                    }
                }

                Button(
                    onClick = onNavigateToEffects,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Configure", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Summary chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PracticeSectionType.entries.forEach { section ->
                    val effectName = settings.getEffectForSection(section)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(section.displayName, fontSize = 12.sp, color = Color(0xFF334155))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    effectName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (effectName == BurstEffectType.DEFAULT.displayName) Color(0xFF16A34A) else Color(0xFF0052CC)
                                )
                                if (effectName == BurstEffectType.DEFAULT.displayName) {
                                    Text("(Default)", fontSize = 10.sp, color = Color(0xFF16A34A))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerDialog(
    title: String,
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var selectedColor by remember { mutableStateOf(initialColor) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 16.5.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Current Selected Preview Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = selectedColor,
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = colorToHex(selectedColor),
                            color = if (selectedColor.luminance() > 0.5f) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Color Swatches Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(curatedPalette) { hex ->
                        val color = parseHexColor(hex)
                        val isPicked = color == selectedColor
                        Surface(
                            shape = CircleShape,
                            color = color,
                            border = BorderStroke(
                                if (isPicked) 2.5.dp else 1.dp,
                                if (isPicked) Color(0xFF0052CC) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { selectedColor = color }
                        ) {
                            if (isPicked) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onColorSelected(selectedColor) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Select Color", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
