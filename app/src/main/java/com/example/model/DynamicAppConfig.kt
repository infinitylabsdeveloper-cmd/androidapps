package com.example.model

enum class CurrencyPosition { PREFIX, SUFFIX }
data class AdvancedFontSettings(
    val fontFamily: String = "Roboto",
    val headingFontFamily: String = "Roboto",
    val bodyFontFamily: String = "Roboto",
    val titleFontSize: Float = 22f,
    val headingFontSize: Float = 18f,
    val bodyFontSize: Float = 15f,
    val questionFontSize: Float = 16f,
    val optionFontSize: Float = 15f,
    val explanationFontSize: Float = 14f,
    val currentAffairsFontSize: Float = 15f,
    val buttonFontSize: Float = 14f,
    val labelFontSize: Float = 12f,
    val fontWeight: String = "Regular", // Light, Regular, Medium, Semi-Bold, Bold
    val headingFontWeight: String = "Bold", // Light, Regular, Medium, Semi-Bold, Bold, Extra-Bold
    val bodyFontWeight: String = "Regular", // Light, Regular, Medium, Semi-Bold, Bold
    val typographyScale: Float = 1.0f, // 0.85f to 1.25f
    val lineSpacing: String = "Normal", // Compact, Normal, Comfortable, Spacious
    val textWidthMode: String = "Normal", // Compact, Normal, Comfortable
    val letterSpacingScale: Float = 0.0f, // -0.5f to 1.0f
    val cornerRadiusStyle: String = "Balanced", // Sharp, Balanced, Soft Rounded, Pill
    val uiAestheticTheme: String = "Royal Indigo", // Royal Indigo, Emerald Gold, Amethyst Violet, Sunset Crimson, Midnight Navy
    val backgroundWallpaperTheme: String = "Midnight Royal Prestige", // Midnight Royal Prestige, Clean Slate, Royal Parchment, Subtle Heritage, Emerald Breeze, Twilight Slate, Golden Glow, Deep Azure
    val practiceAppearance: PracticeAppearanceSettings = PracticeAppearanceSettings()
)

data class PracticeSectionAppearance(
    val backgroundColor: String = "#FFFFFF",
    val textColor: String = "#0F172A",
    val fontFamily: String = "Roboto",
    val fontSize: Int = 18,
    val borderColor: String = "#E2E8F0",
    val borderWidth: Int = 1,
    val cornerRadius: Int = 16
)

private fun colorLongToHex(color: Long): String {
    val red = (color shr 16 and 0xFF).toInt()
    val green = (color shr 8 and 0xFF).toInt()
    val blue = (color and 0xFF).toInt()
    return String.format("#%02X%02X%02X", red, green, blue)
}

private fun hexToColorLong(hex: String): Long {
    return try {
        val cleanHex = hex.removePrefix("#")
        val parsed = cleanHex.toLong(16)
        if (cleanHex.length == 6) {
            0xFF000000L or parsed
        } else {
            parsed
        }
    } catch (e: Exception) {
        0xFFFFFFFFL
    }
}

data class PracticeAppearanceSettings(
    val presetName: String = "Classic",
    val screenBackgroundColor: String = "#FFF7ED",
    val questionCard: PracticeSectionAppearance = PracticeSectionAppearance(
        backgroundColor = "#FDE68A",
        textColor = "#0F172A",
        fontFamily = "Poppins",
        fontSize = 16,
        borderColor = "#FDE68A",
        borderWidth = 1,
        cornerRadius = 16
    ),
    val options: PracticeSectionAppearance = PracticeSectionAppearance(
        backgroundColor = "#FFFFFF",
        textColor = "#0F172A",
        fontFamily = "Poppins",
        fontSize = 15,
        borderColor = "#E2E8F0",
        borderWidth = 1,
        cornerRadius = 14
    ),
    val explanation: PracticeSectionAppearance = PracticeSectionAppearance(
        backgroundColor = "#86EFAC",
        textColor = "#15803D",
        fontFamily = "Poppins",
        fontSize = 14,
        borderColor = "#BBF7D0",
        borderWidth = 1,
        cornerRadius = 16
    ),
    val answerAnimationEffect: String = "Confetti Pop",
    val setsEffect: String = "Confetti Pop",
    val subtopicsEffect: String = "Confetti Pop",
    val currentAffairsEffect: String = "Confetti Pop",
    val offlineEffect: String = "Confetti Pop",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun getEffectForSection(section: PracticeSectionType): String {
        return when (section) {
            PracticeSectionType.SETS -> setsEffect
            PracticeSectionType.SUBTOPICS -> subtopicsEffect
            PracticeSectionType.CURRENT_AFFAIRS -> currentAffairsEffect
            PracticeSectionType.OFFLINE -> offlineEffect
        }
    }

    fun withEffectForSection(section: PracticeSectionType, effect: String): PracticeAppearanceSettings {
        return when (section) {
            PracticeSectionType.SETS -> copy(setsEffect = effect, lastUpdated = System.currentTimeMillis())
            PracticeSectionType.SUBTOPICS -> copy(subtopicsEffect = effect, lastUpdated = System.currentTimeMillis())
            PracticeSectionType.CURRENT_AFFAIRS -> copy(currentAffairsEffect = effect, lastUpdated = System.currentTimeMillis())
            PracticeSectionType.OFFLINE -> copy(offlineEffect = effect, lastUpdated = System.currentTimeMillis())
        }
    }

    val screenBgColor: Long get() = hexToColorLong(screenBackgroundColor)
    val questionBgColor: Long get() = hexToColorLong(questionCard.backgroundColor)
    val questionTextColor: Long get() = hexToColorLong(questionCard.textColor)
    val questionFontFamily: String get() = questionCard.fontFamily
    val questionFontSize: Float get() = questionCard.fontSize.toFloat()
    val questionBorderColor: Long get() = hexToColorLong(questionCard.borderColor)
    val questionBorderWidth: Int get() = questionCard.borderWidth
    val questionCornerRadius: Int get() = questionCard.cornerRadius
    
    val optionBgColor: Long get() = hexToColorLong(options.backgroundColor)
    val optionTextColor: Long get() = hexToColorLong(options.textColor)
    val optionFontFamily: String get() = options.fontFamily
    val optionFontSize: Float get() = options.fontSize.toFloat()
    val optionBorderColor: Long get() = hexToColorLong(options.borderColor)
    val optionBorderWidth: Int get() = options.borderWidth
    val optionCornerRadius: Int get() = options.cornerRadius
    
    val explanationBgColor: Long get() = hexToColorLong(explanation.backgroundColor)
    val explanationTextColor: Long get() = hexToColorLong(explanation.textColor)
    val explanationFontFamily: String get() = explanation.fontFamily
    val explanationFontSize: Float get() = explanation.fontSize.toFloat()
    val explanationBorderColor: Long get() = hexToColorLong(explanation.borderColor)
    val explanationBorderWidth: Int get() = explanation.borderWidth
    val explanationCornerRadius: Int get() = explanation.cornerRadius
}

enum class FontPreset { MODERN, CLASSIC, SANS, SERIF, MONO }

enum class ExamPreset {
    APPSC_GROUP_2,
    APPSC_GROUP_1,
    AP_DSC,
    AP_TET,
    AP_POLICE,
    SSC,
    BANKING,
    RAILWAY,
    UPSC,
    TSPSC
}

// Global active preset build parameter. Change this one value to completely transform the app!
val CURRENT_EXAM_PRESET: ExamPreset = ExamPreset.APPSC_GROUP_2

data class DynamicCurrencyConfig(
    val currencySymbol: String = "₹",
    val currencyCode: String = "INR",
    val currencyPosition: CurrencyPosition = CurrencyPosition.PREFIX
)

data class AppThemeConfig(
    val primaryColorHex: Long = getThemeColors().first,
    val secondaryColorHex: Long = getThemeColors().second,
    val accentColorHex: Long = getThemeColors().third,
    val backgroundColorHex: Long = 0xFFF8FAFC,
    val surfaceColorHex: Long = 0xFFFFFFFF,
    val textColorHex: Long = 0xFF0F172A,
    val buttonColorHex: Long = 0xFF2563EB,
    val errorColorHex: Long = 0xFFDC2626,
    val successColorHex: Long = 0xFF10B981
)

/**
 * Centralized AppConfiguration data class providing app-specific branding,
 * theme colors, typography, currency, and functional settings based on 
 * the current build's package name / App ID.
 */
data class AppConfiguration(
    // Identification & Build Parameters
    val appId: String = getPresetData().appId,
    val appName: String = getPresetData().appName,
    val appCode: String = getPresetData().appCode,
    val examCategory: String = getPresetData().examCategory,
    val tagline: String = getPresetData().tagline,
    val description: String = getPresetData().description,
    val packageName: String = getPresetData().packageName,
    val applicationId: String = getPresetData().applicationId,
    val bundleId: String = getPresetData().bundleId,
    val apiBaseUrl: String = getPresetData().apiBaseUrl,
    val supportPhone: String = getPresetData().supportPhone,
    val supportEmail: String = getPresetData().supportEmail,
    val whatsappNumber: String = "+91 94935 12768",
    val whatsappLink: String = "https://wa.me/919493512768",
    val telegramId: String = "@DHANARAJU0",
    val telegramLink: String = "https://t.me/DHANARAJU0",
    val isWhatsAppEnabled: Boolean = true,
    val isTelegramEnabled: Boolean = true,
    val isEmailSupportEnabled: Boolean = true,
    val logoIconName: String = getPresetData().logoIconName,

    // Branding & Theme Colors
    val theme: AppThemeConfig = AppThemeConfig(),
    val primaryColorHex: Long = getPresetData().primaryColor,
    val secondaryColorHex: Long = getPresetData().secondaryColor,
    val accentColorHex: Long = getPresetData().accentColor,
    val surfaceColorHex: Long = 0xFFFFFFFF,
    val backgroundColorHex: Long = 0xFFF8FAFC,
    val textColorHex: Long = 0xFF0F172A,
    val buttonColorHex: Long = 0xFF2563EB,
    val errorColorHex: Long = 0xFFDC2626,
    val successColorHex: Long = 0xFF10B981,

    // UI Layout & Sizing
    val fontPreset: FontPreset = FontPreset.MODERN,
    val cardCornerRadiusDp: Int = 12,
    val buttonCornerRadiusDp: Int = 8,
    val chipCornerRadiusDp: Int = 16,
    val fontScaleRatio: Float = 1.0f,
    val advancedFontSettings: AdvancedFontSettings = AdvancedFontSettings(),

    // Currency & Regional
    val currency: DynamicCurrencyConfig = DynamicCurrencyConfig(),

    // Feature Flags & Modular Settings
    val allowedLanguages: List<String> = listOf("Telugu", "English"),
    val featuresEnabled: List<String> = listOf("MCQs", "Current Affairs", "Monthly PDFs", "Posters", "Subscriptions", "Daily Quiz", "Mock Tests"),
    val isMockTestActive: Boolean = true,
    val isCurrentAffairsActive: Boolean = true,
    val isSubscriptionActive: Boolean = true,
    val isAiDoubtSolverActive: Boolean = true,
    val isDarkThemeSupported: Boolean = true,
    val isScientificCalculatorEnabled: Boolean = true,
    val isNotificationsEnabled: Boolean = true,
    val isNotificationSoundEnabled: Boolean = true,
    val isNotificationVibrationEnabled: Boolean = true,
    val maxDailyFreeQuestions: Int = 30,
    val watermarkText: String = "Verified APPSC Prep",
    val isPrelimsCardEnabled: Boolean = true,
    val isMainsCardEnabled: Boolean = true,
    val isAllCardEnabled: Boolean = true,
    val questionHighlightConfig: QuestionHighlightConfig = QuestionHighlightConfig()
) {
    val primaryColor: androidx.compose.ui.graphics.Color get() = androidx.compose.ui.graphics.Color(primaryColorHex)
    val secondaryColor: androidx.compose.ui.graphics.Color get() = androidx.compose.ui.graphics.Color(secondaryColorHex)
    val accentColor: androidx.compose.ui.graphics.Color get() = androidx.compose.ui.graphics.Color(accentColorHex)
    val surfaceColor: androidx.compose.ui.graphics.Color get() = androidx.compose.ui.graphics.Color(surfaceColorHex)
    val backgroundColor: androidx.compose.ui.graphics.Color get() = androidx.compose.ui.graphics.Color(backgroundColorHex)
    val textColor: androidx.compose.ui.graphics.Color get() = androidx.compose.ui.graphics.Color(textColorHex)
    val buttonColor: androidx.compose.ui.graphics.Color get() = androidx.compose.ui.graphics.Color(buttonColorHex)
    val errorColor: androidx.compose.ui.graphics.Color get() = androidx.compose.ui.graphics.Color(errorColorHex)
    val successColor: androidx.compose.ui.graphics.Color get() = androidx.compose.ui.graphics.Color(successColorHex)

    companion object {
        /**
         * Resolves the AppConfiguration based on current build package name, application ID, or custom App ID.
         */
        fun fromPackageOrAppId(identifier: String? = null): AppConfiguration {
            val target = (identifier ?: try {
                val bAppId = com.example.BuildConfig.APP_ID
                if (bAppId.isNotBlank() && bAppId != "andhra-pradesh-appsc-group-2") bAppId else com.example.BuildConfig.APPLICATION_ID
            } catch (e: Exception) {
                "com.aistudio.appscprep"
            }).lowercase()

            // 1. Check dynamic apps list in SampleData for runtime overrides/updates
            val dynamicApp = com.example.data.SampleData.appsListState.find { 
                it.id.lowercase() == target || it.packageName.lowercase() == target 
            }
            if (dynamicApp != null) {
                return fromTenantModel(dynamicApp)
            }

            // 2. Fallback to hardcoded presets
            val preset = when {
                target.contains("group1") || target.contains("group-1") || target.contains("g1") -> ExamPreset.APPSC_GROUP_1
                target.contains("group2") || target.contains("group-2") || target.contains("g2") || target.contains("appscprep") -> ExamPreset.APPSC_GROUP_2
                target.contains("apdsc") || target.contains("dsc") -> ExamPreset.AP_DSC
                target.contains("aptet") || target.contains("tet") -> ExamPreset.AP_TET
                target.contains("police") -> ExamPreset.AP_POLICE
                target.contains("ssc") -> ExamPreset.SSC
                target.contains("bank") || target.contains("ibps") || target.contains("sbi") -> ExamPreset.BANKING
                target.contains("railway") || target.contains("rrb") -> ExamPreset.RAILWAY
                target.contains("upsc") || target.contains("ias") -> ExamPreset.UPSC
                target.contains("tspsc") -> ExamPreset.TSPSC
                else -> ExamPreset.APPSC_GROUP_2
            }

            return fromPreset(preset)
        }

        fun fromTenantModel(model: com.example.model.AppTenantModel): AppConfiguration {
            return AppConfiguration(
                appId = model.id,
                appName = model.name,
                appCode = model.shortName,
                examCategory = model.examName,
                tagline = model.description.take(50),
                description = model.description,
                packageName = model.packageName,
                primaryColorHex = model.primaryColorHex,
                secondaryColorHex = model.secondaryColorHex,
                accentColorHex = model.accentColorHex,
                surfaceColorHex = model.surfaceColorHex,
                backgroundColorHex = model.backgroundColorHex,
                textColorHex = model.textColorHex,
                buttonColorHex = model.buttonColorHex,
                errorColorHex = model.errorColorHex,
                successColorHex = model.successColorHex,
                supportEmail = model.supportEmail.ifBlank { model.contactEmail },
                supportPhone = model.supportPhone,
                whatsappNumber = model.whatsappNumber,
                whatsappLink = model.whatsappLink,
                telegramId = model.telegramId,
                telegramLink = model.telegramLink,
                isWhatsAppEnabled = model.isWhatsAppEnabled,
                isTelegramEnabled = model.isTelegramEnabled,
                isEmailSupportEnabled = model.isEmailSupportEnabled,
                isPrelimsCardEnabled = model.isPrelimsCardEnabled,
                isMainsCardEnabled = model.isMainsCardEnabled,
                isAllCardEnabled = model.isAllCardEnabled,
                theme = AppThemeConfig(
                    primaryColorHex = model.primaryColorHex,
                    secondaryColorHex = model.secondaryColorHex,
                    accentColorHex = model.accentColorHex,
                    surfaceColorHex = model.surfaceColorHex,
                    backgroundColorHex = model.backgroundColorHex,
                    textColorHex = model.textColorHex,
                    buttonColorHex = model.buttonColorHex,
                    errorColorHex = model.errorColorHex,
                    successColorHex = model.successColorHex
                )
            )
        }

        fun fromPreset(preset: ExamPreset): AppConfiguration {
            val data = getPresetData(preset)
            return AppConfiguration(
                appId = data.appId,
                appName = data.appName,
                appCode = data.appCode,
                examCategory = data.examCategory,
                tagline = data.tagline,
                description = data.description,
                packageName = data.packageName,
                applicationId = data.applicationId,
                bundleId = data.bundleId,
                apiBaseUrl = data.apiBaseUrl,
                supportPhone = data.supportPhone,
                supportEmail = data.supportEmail,
                logoIconName = data.logoIconName,
                theme = AppThemeConfig(
                    primaryColorHex = data.primaryColor,
                    secondaryColorHex = data.secondaryColor,
                    accentColorHex = data.accentColor
                ),
                primaryColorHex = data.primaryColor,
                secondaryColorHex = data.secondaryColor,
                accentColorHex = data.accentColor,
                watermarkText = "Verified ${data.appName} Portal"
            )
        }
    }
}

// Full backward compatibility type alias
typealias DynamicAppConfig = AppConfiguration

// Preset Data Class
data class PresetData(
    val appId: String,
    val appName: String,
    val appCode: String,
    val examCategory: String,
    val tagline: String,
    val description: String,
    val packageName: String,
    val applicationId: String,
    val bundleId: String,
    val apiBaseUrl: String,
    val supportPhone: String = "+91 94935 12768",
    val supportEmail: String = "sobhadhanaraju0@gmail.com",
    val logoIconName: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val accentColor: Long
)

fun getPresetData(preset: ExamPreset = CURRENT_EXAM_PRESET): PresetData {
    return when (preset) {
        ExamPreset.APPSC_GROUP_2 -> PresetData(
            appId = "appsc-group2",
            appName = "APPSC Group-2",
            appCode = "APPSC-G2",
            examCategory = "APPSC Group 2",
            tagline = "Executive & Non-Executive State Services",
            description = "Comprehensive APPSC Group 2 preparation portal with syllabus, MCQs & Mock Tests.",
            packageName = "com.example",
            applicationId = "com.aistudio.appsc.group2",
            bundleId = "com.aistudio.appsc.group2",
            apiBaseUrl = "https://api.appscprep.com/v1",
            supportPhone = "+91 94935 12768",
            supportEmail = "sobhadhanaraju0@gmail.com",
            logoIconName = "School",
            primaryColor = 0xFF2563EB, // Royal Blue
            secondaryColor = 0xFF1E1B4B, // Dark Indigo
            accentColor = 0xFFFFD700 // Pure Gold
        )
        ExamPreset.APPSC_GROUP_1 -> PresetData(
            appId = "appsc-group1",
            appName = "APPSC Group-1",
            appCode = "APPSC-G1",
            examCategory = "APPSC Group 1",
            tagline = "State Administrative & Police Services (Deputy Collectors & DSPs)",
            description = "Elite State Civil Services screening MCQs, Mains analytical modules & comprehensive mocks.",
            packageName = "com.example",
            applicationId = "com.aistudio.appsc.group1",
            bundleId = "com.aistudio.appsc.group1",
            apiBaseUrl = "https://api.appscg1prep.com/v1",
            supportPhone = "+91 94935 12768",
            supportEmail = "sobhadhanaraju0@gmail.com",
            logoIconName = "AccountBalance",
            primaryColor = 0xFF7C3AED, // Royal Violet
            secondaryColor = 0xFF2E1065, // Midnight Violet
            accentColor = 0xFFF59E0B // Amber Accent
        )
        ExamPreset.AP_DSC -> PresetData(
            appId = "ap-dsc",
            appName = "AP DSC",
            appCode = "AP-DSC",
            examCategory = "AP DSC Teacher",
            tagline = "District Selection Committee Teacher Recruitment",
            description = "Complete syllabus MCQs, School Assistant & Secondary Grade Teacher test preparation.",
            packageName = "com.example",
            applicationId = "com.aistudio.apdsc.teacher",
            bundleId = "com.aistudio.apdsc.teacher",
            apiBaseUrl = "https://api.apdscacademy.com/v1",
            supportPhone = "+91 94935 12768",
            supportEmail = "sobhadhanaraju0@gmail.com",
            logoIconName = "MenuBook",
            primaryColor = 0xFF059669, // Emerald Green
            secondaryColor = 0xFF064E3B, // Forest Green
            accentColor = 0xFFF59E0B // Amber
        )
        ExamPreset.AP_TET -> PresetData(
            appId = "ap-tet",
            appName = "AP TET",
            appCode = "AP-TET",
            examCategory = "AP TET Eligibility",
            tagline = "Teacher Eligibility Test Paper-I & Paper-II Master Arena",
            description = "Child development pedagogy, language fundamentals, and qualifying mock test modules.",
            packageName = "com.example",
            applicationId = "com.aistudio.aptet.exam",
            bundleId = "com.aistudio.aptet.exam",
            apiBaseUrl = "https://api.aptetmaster.com/v1",
            supportPhone = "+91 94935 12768",
            supportEmail = "sobhadhanaraju0@gmail.com",
            logoIconName = "AutoStories",
            primaryColor = 0xFFD97706, // Rich Ochre Amber
            secondaryColor = 0xFF78350F, // Deep Walnut
            accentColor = 0xFF10B981 // Emerald Accent
        )
        ExamPreset.AP_POLICE -> PresetData(
            appId = "ap-police",
            appName = "AP Police Guard",
            appCode = "AP-POLICE",
            examCategory = "AP Police Constable",
            tagline = "Sub-Inspector & Constable Recruitment Prep",
            description = "Physical standards, arithmetic, general studies, and complete state police question archives.",
            packageName = "com.example",
            applicationId = "com.aistudio.appolice.recruitment",
            bundleId = "com.aistudio.appolice.recruitment",
            apiBaseUrl = "https://api.appolicerecruit.com/v1",
            supportPhone = "+91 94935 12768",
            supportEmail = "sobhadhanaraju0@gmail.com",
            logoIconName = "Shield",
            primaryColor = 0xFFDC2626, // Crimson Red
            secondaryColor = 0xFF450A0A, // Rich Mahogany
            accentColor = 0xFFFBBF24 // Gold Yellow
        )
        ExamPreset.SSC -> PresetData(
            appId = "ssc-cgl",
            appName = "SSC Exam Master",
            appCode = "SSC-CGL",
            examCategory = "SSC CGL / CHSL",
            tagline = "Staff Selection Commission Tier 1 & 2 Arena",
            description = "Quantitative aptitude, English, general reasoning, and mock exams strictly matching SSC guidelines.",
            packageName = "com.example",
            applicationId = "com.aistudio.ssc.master",
            bundleId = "com.aistudio.ssc.master",
            apiBaseUrl = "https://api.sscmaster.com/v1",
            supportPhone = "+91 94935 12768",
            supportEmail = "sobhadhanaraju0@gmail.com",
            logoIconName = "AccountBalance",
            primaryColor = 0xFF4F46E5, // Premium Indigo
            secondaryColor = 0xFF312E81, // Midnight Blue
            accentColor = 0xFF10B981 // Emerald Accent
        )
        ExamPreset.BANKING -> PresetData(
            appId = "banking-ibps",
            appName = "Bank Exams Hub",
            appCode = "IBPS-BANK",
            examCategory = "Bank PO & Clerk",
            tagline = "IBPS, SBI, RBI Grade-B Comprehensive Companion",
            description = "Data interpretation, bank awareness, reasoning puzzles, and interactive speed tests.",
            packageName = "com.example",
            applicationId = "com.aistudio.bankexams.hub",
            bundleId = "com.aistudio.bankexams.hub",
            apiBaseUrl = "https://api.bankexamshub.com/v1",
            supportPhone = "+91 94935 12768",
            supportEmail = "sobhadhanaraju0@gmail.com",
            logoIconName = "AccountBalance",
            primaryColor = 0xFF0284C7, // Cyan/Sky Blue
            secondaryColor = 0xFF0C4A6E, // Deep Slate Blue
            accentColor = 0xFFF59E0B // Amber Accent
        )
        ExamPreset.RAILWAY -> PresetData(
            appId = "railway-rrb",
            appName = "Railway RRB Guide",
            appCode = "RRB-NTPC",
            examCategory = "RRB NTPC / Group D",
            tagline = "Indian Railways Recruitment Board Simulator",
            description = "General science, technical arithmetic, and mock simulations matching RRB-NTPC rules.",
            packageName = "com.example",
            applicationId = "com.aistudio.rrb.guide",
            bundleId = "com.aistudio.rrb.guide",
            apiBaseUrl = "https://api.rrbguide.com/v1",
            supportPhone = "+91 94935 12768",
            supportEmail = "sobhadhanaraju0@gmail.com",
            logoIconName = "Train",
            primaryColor = 0xFF7C3AED, // Royal Violet
            secondaryColor = 0xFF2E1065, // Midnight Violet
            accentColor = 0xFFFFD700 // Gold
        )
        ExamPreset.UPSC -> PresetData(
            appId = "upsc-ias",
            appName = "UPSC Civil Suite",
            appCode = "UPSC-IAS",
            examCategory = "UPSC IAS Prelims",
            tagline = "Union Public Service Commission Toppers Portal",
            description = "Polity, history, economics, environment essays, and analytical reasoning keys.",
            packageName = "com.example",
            applicationId = "com.aistudio.upsc.civil",
            bundleId = "com.aistudio.upsc.civil",
            apiBaseUrl = "https://api.upsccivilsuite.com/v1",
            supportPhone = "+91 94935 12768",
            supportEmail = "sobhadhanaraju0@gmail.com",
            logoIconName = "School",
            primaryColor = 0xFFB45309, // Antique Amber
            secondaryColor = 0xFF451A03, // Antique Dark Brown
            accentColor = 0xFF10B981 // Jade Green Accent
        )
        ExamPreset.TSPSC -> PresetData(
            appId = "tspsc-group2",
            appName = "TSPSC Prep Hub",
            appCode = "TSPSC-G2",
            examCategory = "TSPSC Group 2",
            tagline = "Telangana State Services Companion",
            description = "Telangana movement history, state geography, policies, and local GS question syllabus.",
            packageName = "com.example",
            applicationId = "com.aistudio.tspsc.group2",
            bundleId = "com.aistudio.tspsc.group2",
            apiBaseUrl = "https://api.tspscprephub.com/v1",
            supportPhone = "+91 94935 12768",
            supportEmail = "sobhadhanaraju0@gmail.com",
            logoIconName = "MenuBook",
            primaryColor = 0xFF0D9488, // Teal Blue-Green
            secondaryColor = 0xFF115E59, // Dark Teal
            accentColor = 0xFFF59E0B // Amber Accent
        )
    }
}

fun getThemeColors(preset: ExamPreset = CURRENT_EXAM_PRESET): Triple<Long, Long, Long> {
    val d = getPresetData(preset)
    return Triple(d.primaryColor, d.secondaryColor, d.accentColor)
}

/**
 * Centralized App Configuration System
 * Single source of truth controlling active app tenant settings across the codebase.
 */
object CentralAppConfigManager {
    val activeConfigState = androidx.compose.runtime.mutableStateOf(getPresetData(CURRENT_EXAM_PRESET))

    fun getActiveConfig(): PresetData = activeConfigState.value

    fun getAppPreset(appId: String): ExamPreset {
        return when (appId.lowercase()) {
            "appsc-group2", "appsc_group_2" -> ExamPreset.APPSC_GROUP_2
            "appsc-group1", "appsc_group_1" -> ExamPreset.APPSC_GROUP_1
            "ap-dsc", "ap_dsc", "dsc" -> ExamPreset.AP_DSC
            "ap-tet", "ap_tet", "tet" -> ExamPreset.AP_TET
            "ap-police", "ap_police" -> ExamPreset.AP_POLICE
            "ssc-cgl", "ssc" -> ExamPreset.SSC
            "banking-ibps", "banking" -> ExamPreset.BANKING
            "railway-rrb", "railway" -> ExamPreset.RAILWAY
            "upsc-ias", "upsc" -> ExamPreset.UPSC
            "tspsc-group2", "tspsc" -> ExamPreset.TSPSC
            else -> ExamPreset.APPSC_GROUP_2
        }
    }

    fun switchActiveApp(appId: String) {
        val preset = getAppPreset(appId)
        activeConfigState.value = getPresetData(preset)
    }

    fun updateConfig(updated: PresetData) {
        activeConfigState.value = updated
    }
}

data class TopBarConfig(
    val isEnabled: Boolean = true,
    val title: String = "APPSC GROUP-2",
    val subtitle: String = "EXECUTIVE & NON - EXECUTIVE",
    val additionalText: String = "OFFICIAL EXAM PREPARATION",
    val premiumBadgeText: String = "GOLD VIP",
    val showMenu: Boolean = true,
    val showAppIcon: Boolean = true,
    val showTitle: Boolean = true,
    val showSubtitle: Boolean = true,
    val showAdditionalText: Boolean = true,
    val showTextSettings: Boolean = true,
    val showPremium: Boolean = true,
    val showNotifications: Boolean = true,

    // Typography
    val titleFontFamily: String = "Montserrat",
    val titleFontSizeSp: Int = 12,
    val titleFontWeight: String = "Black",
    val titleColorHex: String = "#FFFBEB",
    val titleFontStyle: String = "Normal",

    val subtitleFontFamily: String = "Montserrat",
    val subtitleFontSizeSp: Int = 8,
    val subtitleFontWeight: String = "Bold",
    val subtitleColorHex: String = "#F1F5F9",
    val subtitleFontStyle: String = "Normal",

    val additionalFontFamily: String = "Inter",
    val additionalFontSizeSp: Int = 7,
    val additionalFontWeight: String = "Medium",
    val additionalColorHex: String = "#FFD700",
    val additionalFontStyle: String = "Normal",

    val textAlignment: String = "CENTER", // "LEFT", "CENTER", "RIGHT"

    // Header Layout & Sizing
    val headerHeightDp: Int = 0, // 0 = Auto / Wrap Content
    val headerPaddingHorizontalDp: Int = 8,
    val headerPaddingVerticalDp: Int = 4,
    val headerMarginHorizontalDp: Int = 0,
    val cornerRadiusDp: Int = 16,
    val elevationDp: Int = 8,
    val glowEffectEnabled: Boolean = true,
    val glowColorHex: String = "#FFD700",

    // Colors & Gradients
    val primaryNavyColorHex: Long = 0xFF0B192C,
    val secondaryNavyColorHex: Long = 0xFF1E3E62,
    val goldAccentColorHex: Long = 0xFFFFD700,
    val creamBgColorHex: Long = 0xFFFFFBF0,
    val backgroundColorHex: Long = 0xFF0B192C,
    val backgroundColorStringHex: String = "#0B192C",
    val secondaryBackgroundColorHex: String = "#1E3E62",
    val useGradient: Boolean = true,
    val gradientPreset: String = "NAVY_GOLD",
    val gradientStartColorHex: String = "#071224",
    val gradientEndColorHex: String = "#1E3E62",
    val gradientDirection: String = "HORIZONTAL", // "HORIZONTAL", "VERTICAL", "DIAGONAL"
    val gradientIntensity: Float = 1.0f,

    // Background Image
    val backgroundType: String = "GRADIENT", // "SOLID", "GRADIENT", "IMAGE"
    val backgroundImageUrl: String = "",
    val imageOpacity: Float = 1.0f,
    val imageScale: String = "Crop", // "Crop", "Fit", "FillBounds", "Inside", "FillWidth"
    val imagePosition: String = "Center", // "Center", "Top", "Bottom"
    val imageOverlayColorHex: String = "#071224",
    val overlayOpacity: Float = 0.2f,

    // Borders & Dividers
    val headerBorderColorHex: String = "#FFD700",
    val headerBorderThicknessDp: Int = 1,
    val dividerColorHex: String = "#FFD700",
    val dividerStyle: String = "GRADIENT_HAIRLINE", // "SOLID", "GRADIENT_HAIRLINE", "DASHED", "NONE"

    // Buttons & Icons Appearance
    val buttonBgColorHex: String = "#132A4A",
    val buttonBorderColorHex: String = "#FFD700",
    val buttonIconColorHex: String = "#FFD700",
    val buttonBorderThicknessDp: Int = 1,
    val buttonCornerRadiusDp: Int = 9,
    val buttonSizeDp: Int = 32,

    // Individual Action Icons Selection
    val iconSelectionMenu: String = "Menu", // "Menu", "MenuOpen", "Widgets", "GridView", "Apps"
    val iconSelectionAppIcon: String = "School", // "School", "AutoAwesome", "WorkspacePremium", "Star", "Workspace"
    val iconSelectionTextSettings: String = "Tt", // "Tt", "FontDownload", "FormatSize", "Settings"
    val iconSelectionPremium: String = "WorkspacePremium", // "WorkspacePremium", "Crown", "Star"
    val iconSelectionNotifications: String = "Notifications", // "Notifications", "NotificationsActive", "Bell"

    // Scope Control
    val targetScope: String = "CURRENT", // CURRENT, SELECTED, ALL
    val selectedAppIds: List<String> = emptyList()
)

data class CardBorderConfig(
    val topBorderColorHex: Long = 0xFFFFD700, // Premium Imperial Gold (#FFD700)
    val otherBorderColorHex: Long = 0xFFD4AF37, // Metallic Gold Accent Rim (#D4AF37)
    val topBorderWidthDp: Int = 2,
    val otherBorderWidthDp: Int = 2,
    val cornerRadiusDp: Int = 16,
    val targetScope: String = "CURRENT", // CURRENT, SELECTED, ALL
    val selectedAppIds: List<String> = emptyList()
)


