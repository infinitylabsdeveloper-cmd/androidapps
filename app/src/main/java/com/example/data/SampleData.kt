package com.example.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.R
import com.example.model.*
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

/**
 * Multi-App Competitive Exam Platform Data Core
 * Real-Data Architecture (No hardcoded demo/dummy content)
 */
object SampleData {

    // =========================================================================
    // 1. MULTI-APP TENANT CONFIGURATIONS
    // =========================================================================

    private val initialAppsList = listOf(
        AppTenantModel(
            id = "appsc-group2",
            name = "APPSC Group-2 (English)",
            shortName = "APPSC-G2 (EN)",
            category = "ANDHRA PRADESH",
            examName = "APPSC Group 2",
            description = "Complete syllabus MCQs, PYQs & Practice Arena for APPSC Group 2 Prelims and Mains (English).",
            status = AppStatus.ACTIVE,
            primaryLanguage = "English",
            allowedLanguages = listOf("English"),
            medium = "English",
            languageCode = "en",
            region = "ANDHRA PRADESH",
            primaryColorHex = 0xFF2563EB,
            secondaryColorHex = 0xFF1D4ED8,
            packageName = "com.example.appscgroup2",
            featuresEnabled = listOf("MCQs", "Current Affairs", "Interactive CA Sets", "Posters", "Subscriptions", "Daily Quiz", "Mock Tests")
        ),
        AppTenantModel(
            id = "appsc-group1",
            name = "APPSC Group-1 (English)",
            shortName = "APPSC-G1 (EN)",
            category = "ANDHRA PRADESH",
            examName = "APPSC Group 1",
            description = "Apex State Administrative Services Prelims screening MCQs & Mains analytical mastery (English).",
            status = AppStatus.ACTIVE,
            primaryLanguage = "English",
            allowedLanguages = listOf("English"),
            medium = "English",
            languageCode = "en",
            region = "ANDHRA PRADESH",
            primaryColorHex = 0xFF7C3AED,
            secondaryColorHex = 0xFF2E1065,
            packageName = "com.example.appscgroup1",
            featuresEnabled = listOf("MCQs", "Current Affairs", "Interactive CA Sets", "Posters", "Subscriptions", "Daily Quiz", "Mock Tests")
        ),
        AppTenantModel(
            id = "ap-dsc",
            name = "AP DSC (English)",
            shortName = "AP-DSC (EN)",
            category = "ANDHRA PRADESH",
            examName = "AP DSC Teacher",
            description = "Complete DSC syllabus with Child Development, English, Math, Science & Methodology (English).",
            status = AppStatus.ACTIVE,
            primaryLanguage = "English",
            allowedLanguages = listOf("English"),
            medium = "English",
            languageCode = "en",
            region = "ANDHRA PRADESH",
            primaryColorHex = 0xFF059669,
            secondaryColorHex = 0xFF064E3B,
            packageName = "com.example.apdsc",
            featuresEnabled = listOf("MCQs", "Current Affairs", "Interactive CA Sets", "Posters", "Subscriptions", "Daily Quiz", "Mock Tests")
        ),
        AppTenantModel(
            id = "ap-tet",
            name = "AP TET (English)",
            shortName = "AP-TET (EN)",
            category = "ANDHRA PRADESH",
            examName = "AP TET Eligibility",
            description = "Comprehensive TET practice question bank with pedagogy, language fundamentals & scoring mocks (English).",
            status = AppStatus.INACTIVE,
            primaryLanguage = "English",
            allowedLanguages = listOf("English"),
            medium = "English",
            languageCode = "en",
            region = "ANDHRA PRADESH",
            primaryColorHex = 0xFFD97706,
            secondaryColorHex = 0xFF78350F,
            packageName = "com.example.aptet",
            featuresEnabled = listOf("MCQs", "Current Affairs", "Interactive CA Sets", "Posters", "Subscriptions", "Daily Quiz", "Mock Tests")
        )
    )

    val appsListState = mutableStateListOf<AppTenantModel>().apply {
        addAll(initialAppsList.map { app ->
            app.copy(
                name = com.example.util.MediumStandardizer.standardizeAppName(app.name),
                medium = com.example.util.MediumStandardizer.standardizeLanguageName(app.medium),
                primaryLanguage = com.example.util.MediumStandardizer.standardizeLanguageName(app.primaryLanguage),
                allowedLanguages = com.example.util.MediumStandardizer.standardizeLanguageList(app.allowedLanguages),
                description = com.example.util.MediumStandardizer.standardizeAppName(app.description)
            )
        })
        // Add additional exams from ExamAppsData while ensuring no ID collisions
        com.example.data.ExamAppsData.allExams.forEach { newApp ->
            if (none { it.id == newApp.id }) {
                add(newApp.copy(
                    name = com.example.util.MediumStandardizer.standardizeAppName(newApp.name),
                    medium = com.example.util.MediumStandardizer.standardizeLanguageName(newApp.medium),
                    primaryLanguage = com.example.util.MediumStandardizer.standardizeLanguageName(newApp.primaryLanguage),
                    allowedLanguages = com.example.util.MediumStandardizer.standardizeLanguageList(newApp.allowedLanguages),
                    description = com.example.util.MediumStandardizer.standardizeAppName(newApp.description)
                ))
            }
        }
    }

    /**
     * Standardizes all medium/language names across all apps in the platform,
     * stripping any "Medium" suffix and ensuring only the language name alone is retained.
     */
    fun standardizeAllMediumNames() {
        val updated = appsListState.map { app ->
            app.copy(
                name = com.example.util.MediumStandardizer.standardizeAppName(app.name),
                medium = com.example.util.MediumStandardizer.standardizeLanguageName(app.medium),
                primaryLanguage = com.example.util.MediumStandardizer.standardizeLanguageName(app.primaryLanguage),
                allowedLanguages = com.example.util.MediumStandardizer.standardizeLanguageList(app.allowedLanguages),
                description = com.example.util.MediumStandardizer.standardizeAppName(app.description)
            )
        }
        appsListState.clear()
        appsListState.addAll(updated)
    }

    val auditLogsState = mutableStateListOf<AppAuditLogModel>()

    fun logAction(adminId: String, adminName: String, action: String, appId: String, appName: String, details: String? = null) {
        auditLogsState.add(0, AppAuditLogModel(
            adminId = adminId,
            adminName = adminName,
            action = action,
            appId = appId,
            appName = appName,
            details = details
        ))
    }

    val questionReportsState = mutableStateListOf<QuestionReportModel>()

    // Global App Configuration & App Overrides State
    val globalConfigState = mutableStateOf(GlobalAppConfigSystem())

    fun updateGlobalConfig(newConfig: GlobalAppConfigSystem) {
        globalConfigState.value = newConfig
    }

    fun setAppConfigOverride(override: AppSpecificConfigOverride) {
        val current = globalConfigState.value
        val updatedMap = current.overrides.toMutableMap()
        updatedMap[override.appId] = override
        globalConfigState.value = current.copy(overrides = updatedMap)
    }

    fun removeAppConfigOverride(appId: String) {
        val current = globalConfigState.value
        val updatedMap = current.overrides.toMutableMap()
        updatedMap.remove(appId)
        globalConfigState.value = current.copy(overrides = updatedMap)
    }

    private fun getInitialFlavorTenantId(): String {
        return try {
            val tid = com.example.BuildConfig.TENANT_ID
            if (tid.isNotBlank() && tid != "tenant_default") tid else "appsc-group2"
        } catch (_: Exception) {
            "appsc-group2"
        }
    }

    // Active App ID
    val currentActiveAppId = mutableStateOf(getInitialFlavorTenantId())

    // Dynamic Card Background Colors (Configurable via Admin Panel)
    val setsCardBgColorState = androidx.compose.runtime.mutableStateOf(androidx.compose.ui.graphics.Color(0xFFFFC000)) // Amber Gold Default
    val subtopicsCardBgColorState = androidx.compose.runtime.mutableStateOf(androidx.compose.ui.graphics.Color(0xFFF43F5E)) // Rose Red Default

    // Exact Premium Top Bar Config (Configurable via Admin Panel)
    val topBarConfigState = mutableStateOf(TopBarConfig())
    val appTopBarConfigsState = androidx.compose.runtime.mutableStateMapOf<String, TopBarConfig>()

    fun getTopBarConfigForApp(appId: String = currentActiveAppId.value): TopBarConfig {
        return appTopBarConfigsState[appId] ?: topBarConfigState.value
    }

    fun updateTopBarConfig(
        config: TopBarConfig,
        scope: TargetScopeType = when (config.targetScope) {
            "ALL" -> TargetScopeType.ALL_APPS
            "SELECTED" -> TargetScopeType.SELECT_APPS
            else -> TargetScopeType.SINGLE_APP
        },
        selectedAppIds: List<String> = config.selectedAppIds
    ) {
        val normalizedConfig = config.copy(
            targetScope = when (scope) {
                TargetScopeType.ALL_APPS -> "ALL"
                TargetScopeType.SELECT_APPS -> "SELECTED"
                TargetScopeType.SINGLE_APP -> "CURRENT"
            },
            selectedAppIds = selectedAppIds
        )

        when (scope) {
            TargetScopeType.ALL_APPS -> {
                topBarConfigState.value = normalizedConfig
                appsListState.forEach { app ->
                    appTopBarConfigsState[app.id] = normalizedConfig
                }
            }
            TargetScopeType.SELECT_APPS -> {
                selectedAppIds.forEach { targetId ->
                    appTopBarConfigsState[targetId] = normalizedConfig
                }
                if (selectedAppIds.contains(currentActiveAppId.value)) {
                    topBarConfigState.value = normalizedConfig
                }
            }
            TargetScopeType.SINGLE_APP -> {
                val targetId = selectedAppIds.firstOrNull() ?: currentActiveAppId.value
                appTopBarConfigsState[targetId] = normalizedConfig
                if (targetId == currentActiveAppId.value) {
                    topBarConfigState.value = normalizedConfig
                }
            }
        }
    }

    fun resetTopBarConfig(
        scope: TargetScopeType = TargetScopeType.SINGLE_APP,
        targetAppId: String = currentActiveAppId.value,
        selectedAppIds: List<String> = emptyList()
    ) {
        val defaultConfig = TopBarConfig()
        when (scope) {
            TargetScopeType.ALL_APPS -> {
                topBarConfigState.value = defaultConfig
                appTopBarConfigsState.clear()
            }
            TargetScopeType.SELECT_APPS -> {
                selectedAppIds.forEach { id ->
                    appTopBarConfigsState.remove(id)
                }
                if (selectedAppIds.contains(currentActiveAppId.value)) {
                    topBarConfigState.value = defaultConfig
                }
            }
            TargetScopeType.SINGLE_APP -> {
                appTopBarConfigsState.remove(targetAppId)
                if (targetAppId == currentActiveAppId.value) {
                    topBarConfigState.value = defaultConfig
                }
            }
        }
    }

    // Live Content Dashboard Config (Configurable via Admin Panel)
    val liveDashboardConfigState = mutableStateOf(LiveDashboardConfig())

    fun updateLiveDashboardConfig(config: LiveDashboardConfig) {
        liveDashboardConfigState.value = config
    }

    fun resetLiveDashboardConfig() {
        liveDashboardConfigState.value = LiveDashboardConfig()
    }

    // Global Broadcast Notice / Announcements (Configured & Synchronized via Admin Panel)
    val broadcastNoticeState = mutableStateOf("")

    // Card Border & Styling Config (Configurable via Admin Panel)
    val cardBorderConfigState = mutableStateOf(CardBorderConfig())

    // Left Navigation Drawer Config (Configurable via Admin Panel & Pixel Match)
    val drawerConfigState = mutableStateOf(DrawerConfig())
    val appDrawerConfigsState = androidx.compose.runtime.mutableStateMapOf<String, DrawerConfig>()

    fun getDrawerConfigForApp(appId: String = currentActiveAppId.value): DrawerConfig {
        return appDrawerConfigsState[appId] ?: drawerConfigState.value
    }

    fun updateDrawerConfig(
        config: DrawerConfig,
        scope: TargetScopeType = when (config.targetScope) {
            "ALL" -> TargetScopeType.ALL_APPS
            "SELECTED" -> TargetScopeType.SELECT_APPS
            else -> TargetScopeType.SINGLE_APP
        },
        selectedAppIds: List<String> = config.selectedAppIds
    ) {
        val normalizedConfig = config.copy(
            targetScope = when (scope) {
                TargetScopeType.ALL_APPS -> "ALL"
                TargetScopeType.SELECT_APPS -> "SELECTED"
                TargetScopeType.SINGLE_APP -> "CURRENT"
            },
            selectedAppIds = selectedAppIds
        )

        when (scope) {
            TargetScopeType.ALL_APPS -> {
                drawerConfigState.value = normalizedConfig
                appsListState.forEach { app ->
                    appDrawerConfigsState[app.id] = normalizedConfig
                }
            }
            TargetScopeType.SELECT_APPS -> {
                selectedAppIds.forEach { targetId ->
                    appDrawerConfigsState[targetId] = normalizedConfig
                }
                if (selectedAppIds.contains(currentActiveAppId.value)) {
                    drawerConfigState.value = normalizedConfig
                }
            }
            TargetScopeType.SINGLE_APP -> {
                val targetId = selectedAppIds.firstOrNull() ?: currentActiveAppId.value
                appDrawerConfigsState[targetId] = normalizedConfig
                if (targetId == currentActiveAppId.value) {
                    drawerConfigState.value = normalizedConfig
                }
            }
        }
    }

    fun resetDrawerConfig(
        scope: TargetScopeType = TargetScopeType.SINGLE_APP,
        targetAppId: String = currentActiveAppId.value,
        selectedAppIds: List<String> = emptyList()
    ) {
        val defaultConfig = DrawerConfig()
        when (scope) {
            TargetScopeType.ALL_APPS -> {
                drawerConfigState.value = defaultConfig
                appDrawerConfigsState.clear()
            }
            TargetScopeType.SELECT_APPS -> {
                selectedAppIds.forEach { id ->
                    appDrawerConfigsState.remove(id)
                }
                if (selectedAppIds.contains(currentActiveAppId.value)) {
                    drawerConfigState.value = defaultConfig
                }
            }
            TargetScopeType.SINGLE_APP -> {
                appDrawerConfigsState.remove(targetAppId)
                if (targetAppId == currentActiveAppId.value) {
                    drawerConfigState.value = defaultConfig
                }
            }
        }
    }

    // Mock Test Generator Admin Configuration (Multi-App Supported)
    val mockTestGeneratorConfigState = mutableStateOf(MockTestGeneratorAdminConfig())
    val appMockTestGeneratorConfigsState = androidx.compose.runtime.mutableStateMapOf<String, MockTestGeneratorAdminConfig>()

    fun getMockTestGeneratorConfigForApp(appId: String = currentActiveAppId.value): MockTestGeneratorAdminConfig {
        return appMockTestGeneratorConfigsState[appId] ?: mockTestGeneratorConfigState.value
    }

    fun updateMockTestGeneratorConfig(
        config: MockTestGeneratorAdminConfig,
        scope: TargetScopeType = when (config.targetScope) {
            "ALL" -> TargetScopeType.ALL_APPS
            "SELECTED" -> TargetScopeType.SELECT_APPS
            else -> TargetScopeType.SINGLE_APP
        },
        selectedAppIds: List<String> = config.selectedAppIds
    ) {
        val normalizedConfig = config.copy(
            targetScope = when (scope) {
                TargetScopeType.ALL_APPS -> "ALL"
                TargetScopeType.SELECT_APPS -> "SELECTED"
                TargetScopeType.SINGLE_APP -> "CURRENT"
            },
            selectedAppIds = selectedAppIds
        )

        when (scope) {
            TargetScopeType.ALL_APPS -> {
                mockTestGeneratorConfigState.value = normalizedConfig
                appsListState.forEach { app ->
                    appMockTestGeneratorConfigsState[app.id] = normalizedConfig
                }
            }
            TargetScopeType.SELECT_APPS -> {
                selectedAppIds.forEach { targetId ->
                    appMockTestGeneratorConfigsState[targetId] = normalizedConfig
                }
                if (selectedAppIds.contains(currentActiveAppId.value)) {
                    mockTestGeneratorConfigState.value = normalizedConfig
                }
            }
            TargetScopeType.SINGLE_APP -> {
                val targetId = selectedAppIds.firstOrNull() ?: currentActiveAppId.value
                appMockTestGeneratorConfigsState[targetId] = normalizedConfig
                if (targetId == currentActiveAppId.value) {
                    mockTestGeneratorConfigState.value = normalizedConfig
                }
            }
        }
    }

    fun resetMockTestGeneratorConfig(
        scope: TargetScopeType = TargetScopeType.SINGLE_APP,
        targetAppId: String = currentActiveAppId.value,
        selectedAppIds: List<String> = emptyList()
    ) {
        val defaultConfig = MockTestGeneratorAdminConfig()
        when (scope) {
            TargetScopeType.ALL_APPS -> {
                mockTestGeneratorConfigState.value = defaultConfig
                appMockTestGeneratorConfigsState.clear()
            }
            TargetScopeType.SELECT_APPS -> {
                selectedAppIds.forEach { id ->
                    appMockTestGeneratorConfigsState.remove(id)
                }
                if (selectedAppIds.contains(currentActiveAppId.value)) {
                    mockTestGeneratorConfigState.value = defaultConfig
                }
            }
            TargetScopeType.SINGLE_APP -> {
                appMockTestGeneratorConfigsState.remove(targetAppId)
                if (targetAppId == currentActiveAppId.value) {
                    mockTestGeneratorConfigState.value = defaultConfig
                }
            }
        }
    }

    fun updateCardBorderConfig(config: CardBorderConfig) {
        cardBorderConfigState.value = config
    }

    fun resetCardBorderConfig() {
        cardBorderConfigState.value = CardBorderConfig()
    }

    fun updateCardBgColors(setsColor: androidx.compose.ui.graphics.Color, subtopicsColor: androidx.compose.ui.graphics.Color) {
        setsCardBgColorState.value = setsColor
        subtopicsCardBgColorState.value = subtopicsColor
    }

    fun getActiveApp(): AppTenantModel {
        return appsListState.find { it.id == currentActiveAppId.value } ?: initialAppsList.first()
    }

    fun setActiveApp(appId: String) {
        if (currentActiveAppId.value != appId) {
            resetForAppSwitch()
        }
        currentActiveAppId.value = appId
        topBarConfigState.value = getTopBarConfigForApp(appId)
        drawerConfigState.value = getDrawerConfigForApp(appId)
        sampleUserProfile = sampleUserProfile.copy(
            targetAppId = appId,
            targetExam = appsListState.find { it.id == appId }?.name ?: appId
        )
        // Sync with Centralized App Configuration Repository
        com.example.repository.AppConfigRepository.getInstance().switchActiveAppTenant(appId)
    }

    /**
     * Updates independent dashboard card visibility settings (Prelims, Mains, All) for a specific app tenant.
     */
    fun updateAppCardVisibility(appId: String, isPrelims: Boolean, isMains: Boolean, isAll: Boolean) {
        val index = appsListState.indexOfFirst { it.id == appId }
        if (index != -1) {
            val current = appsListState[index]
            val updated = current.copy(
                isPrelimsCardEnabled = isPrelims,
                isMainsCardEnabled = isMains,
                isAllCardEnabled = isAll,
                updatedAt = System.currentTimeMillis()
            )
            appsListState[index] = updated
            if (appId == currentActiveAppId.value) {
                com.example.repository.AppConfigRepository.getInstance().refreshCurrentConfig()
            }
            try {
                com.example.util.FirebaseSyncManager.saveAppCardVisibility(appId, isPrelims, isMains, isAll)
            } catch (_: Exception) {}
        }
    }

    /**
     * Resets volatile in-memory states to prevent content bleed-over during app tenant switching.
     */
    private fun resetForAppSwitch() {
        generatedMockTestsState.clear()
        testResultsState.clear()
        // Subjects and questions are filtered by appId, but clearing them ensures 
        // they are re-populated if the app logic depends on it. 
        // Actually, subjectsState and questionsState are the primary bank, so we don't clear them.
    }

    fun getSubjectName(id: String): String {
        return subjectsState.find { it.id == id }?.name ?: "Unknown Subject"
    }

    fun getUnitName(subjectId: String, unitId: String): String {
        val subject = subjectsState.find { it.id == subjectId }
        return subject?.units?.find { it.id == unitId }?.name ?: "Unknown Unit"
    }

    fun getTopicName(subjectId: String, unitId: String, topicId: String): String {
        val subject = subjectsState.find { it.id == subjectId }
        val unit = subject?.units?.find { it.id == unitId }
        return unit?.topics?.find { it.id == topicId }?.name ?: "Unknown Topic"
    }

    fun formatUnitTitle(unitNumber: Int, name: String): String {
        return if (name.startsWith("Unit", ignoreCase = true)) name else "Unit $unitNumber: $name"
    }

    // =========================================================================
    // 2. SUBJECT & SYLLABUS HIERARCHY (REAL DATA - CONFIGURABLE VIA ADMIN)
    // =========================================================================

    val generatedMockTestsState = androidx.compose.runtime.mutableStateListOf<com.example.model.GeneratedMockTest>()

    fun seedDefaultMockTestsIfEmpty(appId: String = "appsc-group2") {
        // Demo mock test templates removed; tests are populated from user generation, Room persistence, or Admin Panel.
        val demoTemplateIds = setOf(
            "mock-01", "mock-02", "mock-03",
            "mock-unit-01", "mock-unit-02",
            "mock-topic-01", "mock-topic-02"
        )
        generatedMockTestsState.removeAll { it.id in demoTemplateIds }
    }

    val subjectsState = mutableStateListOf<Subject>(
        Subject(
            id = "subj-history",
            appId = "appsc-group2",
            name = "Indian History & Culture",
            category = "prelims,general",
            categories = listOf("prelims", "general"),
            icon = "BookOpen",
            imageRes = R.drawable.card_subj_history,
            imageUrl = "https://images.unsplash.com/photo-1564507592333-c60657eea523?w=800&auto=format&fit=crop&q=80",
            heroQuote = "Ancient, Medieval & Modern Indian Freedom Struggle",
            color = "from-amber-600 to-orange-700",
            units = listOf(
                UnitModel(
                    id = "unit-hist-1",
                    appId = "appsc-group2",
                    name = "Ancient & Medieval Indian History",
                    unitNumber = 1,
                    subtitle = "Vedic Heritage, Empires & Classical Indian Culture",
                    imageRes = R.drawable.card_unit_hist_ancient,
                    imageUrl = "https://images.unsplash.com/photo-1548013146-72479768bada?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-hist-1",
                            appId = "appsc-group2",
                            name = "Indus Valley Civilization & Vedic Age",
                            subtitle = "Harappan Architecture, Urban Planning & Vedic Philosophy",
                            imageRes = R.drawable.card_topic_indus_vedic,
                            imageUrl = "https://images.unsplash.com/photo-1608958435020-e8a7109ba809?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf(
                                "Harappan Culture & Town Planning",
                                "Early & Later Vedic Literature",
                                "Religious Movements – Jainism & Buddhism",
                                "Discovery, Sites & Geographical Extent",
                                "Town Planning, Drainage & Architecture",
                                "Economy, Trade, Crafts & Seals",
                                "Art, Culture & Daily Life"
                            )
                        ),
                        TopicModel(
                            id = "top-hist-2",
                            appId = "appsc-group2",
                            name = "Mauryan Empire & Gupta Dynasty",
                            subtitle = "Ashoka Dhamma Edicts, Golden Age Arts & Southern Dynasties",
                            imageRes = R.drawable.card_topic_mauryan_gupta,
                            imageUrl = "https://images.unsplash.com/photo-1628155930542-3c7a64e2c833?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Ashoka Edicts & Administration", "Gupta Golden Age in Art & Science", "South Indian Kingdoms - Pallavas & Cholas")
                        )
                    )
                ),
                UnitModel(
                    id = "unit-hist-2",
                    appId = "appsc-group2",
                    name = "Modern Indian Freedom Movement (1857-1947)",
                    unitNumber = 2,
                    subtitle = "Great Revolt, Satyagraha Movements & Independence Dawn",
                    imageRes = R.drawable.card_unit_hist_modern,
                    imageUrl = "https://images.unsplash.com/photo-1532375810709-75b1da00537c?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-hist-3",
                            appId = "appsc-group2",
                            name = "1857 Revolt & Rise of Nationalism",
                            subtitle = "Sepoy Mutiny, Social Reforms & Early INC Sessions",
                            imageRes = R.drawable.card_topic_revolt_1857,
                            imageUrl = "https://images.unsplash.com/photo-1590076215667-875d4ef2d7ee?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Causes & Impact of 1857 Revolt", "Socio-Religious Reform Movements", "Formation of Indian National Congress")
                        ),
                        TopicModel(
                            id = "top-hist-4",
                            appId = "appsc-group2",
                            name = "Gandhian Era & Independence",
                            subtitle = "Non-Cooperation, Civil Disobedience & Integration of States",
                            imageRes = R.drawable.card_topic_gandhian_era,
                            imageUrl = "https://images.unsplash.com/photo-1587474260584-136574528ed5?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Non-Cooperation & Civil Disobedience", "Quit India Movement & INA", "Partition & Integration of Princely States")
                        )
                    )
                )
            )
        ),
        Subject(
            id = "subj-geography",
            appId = "appsc-group2",
            name = "Geography of India & Andhra Pradesh",
            category = "prelims,general",
            categories = listOf("prelims", "general"),
            icon = "Compass",
            imageRes = R.drawable.card_subj_geography,
            imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&auto=format&fit=crop&q=80",
            heroQuote = "Physical Features, Climate, River Basins & Demographics",
            color = "from-emerald-600 to-teal-800",
            units = listOf(
                UnitModel(
                    id = "unit-geo-1",
                    appId = "appsc-group2",
                    name = "General & Physical Geography",
                    unitNumber = 1,
                    subtitle = "Himalayan Ranges, River Basins & Monsoon Circulation",
                    imageRes = R.drawable.card_unit_geo_physical,
                    imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-geo-1",
                            appId = "appsc-group2",
                            name = "Physiographic Divisions & Drainage",
                            subtitle = "Peninsular Plateaus, Coastal Plains & River Networks",
                            imageRes = R.drawable.card_topic_physiography,
                            imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Himalayas & Peninsular Plateau", "River Systems - Godavari, Krishna & Penna", "Monsoon Mechanism & Rainfall")
                        )
                    )
                ),
                UnitModel(
                    id = "unit-geo-2",
                    appId = "appsc-group2",
                    name = "Economic & Regional Geography of AP",
                    unitNumber = 2,
                    subtitle = "Coastal Corridors, Mineral Belts & Agro-Climatic Zones",
                    imageRes = R.drawable.card_unit_geo_ap,
                    imageUrl = "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-geo-2",
                            appId = "appsc-group2",
                            name = "Agriculture, Minerals & Ports of AP",
                            subtitle = "Irrigation Schemes, Mineral Reserves & Deepwater Harbors",
                            imageRes = R.drawable.card_topic_ap_ports,
                            imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Major Crops & Irrigation Projects", "Mineral Resources in AP", "Ports, Corridors & Industrial Corridors")
                        )
                    )
                )
            )
        ),
        Subject(
            id = "subj-society",
            appId = "appsc-group2",
            name = "Indian Society & Social Justice",
            category = "prelims",
            categories = listOf("prelims"),
            icon = "Users",
            imageRes = R.drawable.card_subj_society,
            imageUrl = "https://images.unsplash.com/photo-1573164713988-8665fc963095?w=800&auto=format&fit=crop&q=80",
            heroQuote = "Social Structure, Issues, Policies & Welfare Mechanisms",
            color = "from-purple-600 to-indigo-800",
            units = listOf(
                UnitModel(
                    id = "unit-soc-1",
                    appId = "appsc-group2",
                    name = "Structure of Indian Society",
                    unitNumber = 1,
                    subtitle = "Family Dynamics, Kinship Systems & Pluralistic Diversity",
                    imageUrl = "https://images.unsplash.com/photo-1511632765486-a01980e01a18?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-soc-1",
                            appId = "appsc-group2",
                            name = "Family, Marriage, Caste & Kinship",
                            subtitle = "Social Stratification, Tribal Communities & Cultural Harmony",
                            imageUrl = "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Caste System & Social Hierarchy", "Tribal Communities & Vulnerabilities", "Secularism & Diversity")
                        )
                    )
                ),
                UnitModel(
                    id = "unit-soc-2",
                    appId = "appsc-group2",
                    name = "Social Issues & Welfare Policies",
                    unitNumber = 2,
                    subtitle = "Eradicating Poverty, Child Protection & Affirmative Action",
                    imageUrl = "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-soc-2",
                            appId = "appsc-group2",
                            name = "Poverty, Child Labor & Women Empowerment",
                            subtitle = "Constitutional Safeguards, DWCRA & State Welfare Schemes",
                            imageUrl = "https://images.unsplash.com/photo-1509062522246-3755977927d7?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Constitutional Safeguards for SC/ST/BC", "Women & Child Welfare Schemes in AP", "Social Justice Initiatives")
                        )
                    )
                )
            )
        ),
        Subject(
            id = "subj-mental-ability",
            appId = "appsc-group2",
            name = "General Mental Ability & Reasoning",
            category = "prelims,general",
            categories = listOf("prelims", "general"),
            icon = "Cpu",
            imageUrl = "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=800&auto=format&fit=crop&q=80",
            heroQuote = "Logical Reasoning, Data Interpretation & Quantitative Aptitude",
            color = "from-cyan-600 to-blue-800",
            units = listOf(
                UnitModel(
                    id = "unit-ma-1",
                    appId = "appsc-group2",
                    name = "Logical & Analytical Reasoning",
                    unitNumber = 1,
                    subtitle = "Deductive Sequences, Direction Tests & Syllogism Puzzles",
                    imageUrl = "https://images.unsplash.com/photo-1529699211952-734e80c4d42b?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-ma-1",
                            appId = "appsc-group2",
                            name = "Deductive Reasoning & Coding-Decoding",
                            subtitle = "Pattern Decoding, Blood Relations & Venn Logic",
                            imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Coding & Decoding Sequences", "Blood Relations & Direction Sense", "Syllogism & Venn Diagrams")
                        )
                    )
                ),
                UnitModel(
                    id = "unit-ma-2",
                    appId = "appsc-group2",
                    name = "Basic Numeracy & Data Interpretation",
                    unitNumber = 2,
                    subtitle = "Statistical Charts, Tabular Analytics & Ratio Calculations",
                    imageUrl = "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-ma-2",
                            appId = "appsc-group2",
                            name = "Percentages, Ratios & Charts",
                            subtitle = "Bar Graphs, Pie Charts & Time-Speed Problems",
                            imageUrl = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Ratios & Averages", "Bar Charts & Pie Charts Analysis", "Time, Speed and Work Calculations")
                        )
                    )
                )
            )
        ),
        Subject(
            id = "subj-ap-history",
            appId = "appsc-group2",
            name = "Social & Cultural History of Andhra Pradesh",
            category = "mains",
            categories = listOf("mains"),
            icon = "Shield",
            imageUrl = "https://images.unsplash.com/photo-1608958435020-e8a7109ba809?w=800&auto=format&fit=crop&q=80",
            heroQuote = "Satavahanas, Kakatiyas, Vijayanagara & Modern AP Formation",
            color = "from-rose-600 to-pink-800",
            units = listOf(
                UnitModel(
                    id = "unit-aph-1",
                    appId = "appsc-group2",
                    name = "Ancient & Medieval Andhra Dynasties",
                    unitNumber = 1,
                    subtitle = "Gopurams, Hydraulic Engineering & Golden Telugu Literature",
                    imageUrl = "https://images.unsplash.com/photo-1600100397608-f010f443c5b5?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-aph-1",
                            appId = "appsc-group2",
                            name = "Satavahanas & Ikshvakus",
                            subtitle = "Amaravati Art School, Buddhist Stupas & Maritime Trade",
                            imageUrl = "https://images.unsplash.com/photo-1561361513-2d000a50f0dc?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Satavahana Administration, Art & Religion", "Amaravati School of Art", "Eastern Chalukyas of Vengi")
                        ),
                        TopicModel(
                            id = "top-aph-2",
                            appId = "appsc-group2",
                            name = "Kakatiyas & Vijayanagara Empire",
                            subtitle = "Warangal Thoranam, Hampi Monuments & Krishnadevaraya Court",
                            imageUrl = "https://images.unsplash.com/photo-1609766857041-ed402ea8069a?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Kakatiya Hydraulic System & Temples", "Sri Krishnadevaraya & Telugu Literature", "Decline of Vijayanagara")
                        )
                    )
                ),
                UnitModel(
                    id = "unit-aph-2",
                    appId = "appsc-group2",
                    name = "Modern Andhra Movement & State Formation",
                    unitNumber = 2,
                    subtitle = "Telugu Renaissance, Potti Sreeramulu Sacrifice & 1953 Andhra State",
                    imageUrl = "https://images.unsplash.com/photo-1541872703-74c5e44368f9?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-aph-3",
                            appId = "appsc-group2",
                            name = "Kandukuri, Andhra Movement & Potti Sreeramulu",
                            subtitle = "Social Reformers, Linguistic State Dawn & 2014 Bifurcation",
                            imageUrl = "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Renaissance in Andhra Literature", "1953 Andhra State Formation", "1956 Andhra Pradesh Formation & 2014 Bifurcation")
                        )
                    )
                )
            )
        ),
        Subject(
            id = "subj-constitution",
            appId = "appsc-group2",
            name = "Indian Constitution & Polity",
            category = "mains,general",
            categories = listOf("mains", "general"),
            icon = "BookOpen",
            imageUrl = "https://images.unsplash.com/photo-1541872703-74c5e44368f9?w=800&auto=format&fit=crop&q=80",
            heroQuote = "Preamble, Fundamental Rights, Union-State Relations & Judiciary",
            color = "from-blue-700 to-indigo-900",
            units = listOf(
                UnitModel(
                    id = "unit-pol-1",
                    appId = "appsc-group2",
                    name = "Constitutional Framework & Fundamental Rights",
                    unitNumber = 1,
                    subtitle = "Founding Principles, Preamble & Constitutional Remedies",
                    imageUrl = "https://images.unsplash.com/photo-1450133064473-71024230f91b?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-pol-1",
                            appId = "appsc-group2",
                            name = "Preamble, Fundamental Rights & DPSP",
                            subtitle = "Articles 12-35, Directive Principles & Fundamental Duties",
                            imageUrl = "https://images.unsplash.com/photo-1589578527966-fdac0f44566c?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Salient Features of Constitution", "Enforceability of Fundamental Rights", "Directive Principles & Fundamental Duties")
                        )
                    )
                ),
                UnitModel(
                    id = "unit-pol-2",
                    appId = "appsc-group2",
                    name = "Union & State Government, Panchayati Raj",
                    unitNumber = 2,
                    subtitle = "Parliamentary Procedures, Supreme Court & Grassroots Democracy",
                    imageUrl = "https://images.unsplash.com/photo-1575517111478-7f6afd0973db?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-pol-2",
                            appId = "appsc-group2",
                            name = "Executive, Parliament & 73rd/74th Amendments",
                            subtitle = "Presidential Prerogatives, Judicial Review & Local Bodies",
                            imageUrl = "https://images.unsplash.com/photo-1540910419892-4a36d2c3266c?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("President & Governor Powers", "Supreme Court & High Courts", "Local Self Government & Devolution")
                        )
                    )
                )
            )
        ),
        Subject(
            id = "subj-economy",
            appId = "appsc-group2",
            name = "Indian & Andhra Pradesh Economy",
            category = "mains",
            categories = listOf("mains"),
            icon = "TrendingUp",
            imageUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=800&auto=format&fit=crop&q=80",
            heroQuote = "Planning, Fiscal Policy, Agriculture, AP Budget & Bifurcation Act",
            color = "from-emerald-700 to-green-950",
            units = listOf(
                UnitModel(
                    id = "unit-eco-1",
                    appId = "appsc-group2",
                    name = "Structure of Indian Economy & Planning",
                    unitNumber = 1,
                    subtitle = "Macroeconomic Growth, Banking Architecture & NITI Reforms",
                    imageUrl = "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-eco-1",
                            appId = "appsc-group2",
                            name = "National Income, Banking & NITI Aayog",
                            subtitle = "GDP Trends, Inflation Indices & RBI Monetary Guidelines",
                            imageUrl = "https://images.unsplash.com/photo-1621416894569-0f39ed31d247?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("GDP Trends & Sectoral Shares", "RBI Monetary Policy & Inflation", "Taxation & Fiscal Deficit")
                        )
                    )
                ),
                UnitModel(
                    id = "unit-eco-2",
                    appId = "appsc-group2",
                    name = "Andhra Pradesh Economy & Post-Bifurcation Challenges",
                    unitNumber = 2,
                    subtitle = "GSVA Contributions, Logistics Corridors & Industrial Hubs",
                    imageUrl = "https://images.unsplash.com/photo-1578575437130-527eed3abbec?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-eco-2",
                            appId = "appsc-group2",
                            name = "AP State Gross Product & AP Reorganisation Act",
                            subtitle = "Agro Allied Value Chains, Port Connectivity & AP Budget",
                            imageUrl = "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Agriculture & Allied Sectors in AP", "Industrial Corridors & Port-Led Development", "AP Reorganisation Act 2014 & Special Category Status")
                        )
                    )
                )
            )
        ),
        Subject(
            id = "subj-science-tech",
            appId = "appsc-group2",
            name = "Science & Technology",
            category = "mains,general",
            categories = listOf("mains", "general"),
            icon = "Cpu",
            imageUrl = "https://images.unsplash.com/photo-1507668077129-56e32842fceb?w=800&auto=format&fit=crop&q=80",
            heroQuote = "Space Missions, Defence, IT, Energy & Environmental Ecology",
            color = "from-violet-700 to-purple-950",
            units = listOf(
                UnitModel(
                    id = "unit-sci-1",
                    appId = "appsc-group2",
                    name = "Technology Missions & Space Exploration",
                    unitNumber = 1,
                    subtitle = "ISRO Deep-Space Missions, DRDO Defence Shields & Quantum AI",
                    imageUrl = "https://images.unsplash.com/photo-1517976487504-59a1a049a247?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-sci-1",
                            appId = "appsc-group2",
                            name = "ISRO Satellite Missions & Defence Tech",
                            subtitle = "LVM3 Launchers, Chandrayaan, Gaganyaan & Missile Systems",
                            imageUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("PSLV, GSLV & Chandrayaan/Aditya Missions", "DRDO Missile Systems", "Cyber Security & AI Governance")
                        )
                    )
                ),
                UnitModel(
                    id = "unit-sci-2",
                    appId = "appsc-group2",
                    name = "Energy, Environment & Waste Management",
                    unitNumber = 2,
                    subtitle = "Green Hydrogen, Solar Parks, Biodiversity & Climate Action",
                    imageUrl = "https://images.unsplash.com/photo-1497435334941-8c899ee9e8e9?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-sci-2",
                            appId = "appsc-group2",
                            name = "Renewable Energy & Climate Change Mitigation",
                            subtitle = "Kurnool Solar Park, COP Goals & Carbon Neutrality Protocols",
                            imageUrl = "https://images.unsplash.com/photo-1509391365360-2e959784a276?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Solar & Wind Energy Initiatives in India", "Biodiversity Hotspots & Conservation", "Pollution Control & Carbon Neutrality")
                        )
                    )
                )
            )
        ),
        Subject(
            id = "subj-current-affairs",
            appId = "appsc-group2",
            name = "Current Affairs (Major Events - International, National & AP)",
            category = "prelims,general",
            categories = listOf("prelims", "general"),
            icon = "Layers",
            imageUrl = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=800&auto=format&fit=crop&q=80",
            heroQuote = "Major Current Events: International Summits, National Governance, Bilateral Treaties & AP State Welfare",
            color = "from-rose-700 to-amber-900",
            units = listOf(
                UnitModel(
                    id = "unit-ca-1",
                    appId = "appsc-group2",
                    name = "Major Current Events - International & Global Summits",
                    unitNumber = 1,
                    subtitle = "G20 Deliberations, Multilateral Accords & UN Peace Treaties",
                    imageUrl = "https://images.unsplash.com/photo-1526470608268-f674ce90ebd4?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-ca-1",
                            appId = "appsc-group2",
                            name = "Global Summits, Treaties & International Organizations",
                            subtitle = "BRICS Expansion, COP Climate Agreements & Global Security",
                            imageUrl = "https://images.unsplash.com/photo-1577495508048-b635879837f1?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("G20, SCO & BRICS Summits", "United Nations & Climate Agreements (COP)", "Global Peace Treaties & Bilateral Relations")
                        )
                    )
                ),
                UnitModel(
                    id = "unit-ca-2",
                    appId = "appsc-group2",
                    name = "National Current Affairs & Central Government Policies",
                    unitNumber = 2,
                    subtitle = "Union Budget Milestones, Infrastructure Corridors & Honors",
                    imageUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-ca-2",
                            appId = "appsc-group2",
                            name = "National Governance, Flagship Schemes & Milestones",
                            subtitle = "Digital Public Infrastructure, PM Gati Shakti & National Awards",
                            imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("Union Budget & Economic Initiatives", "National Education Policy & Digital India", "Science, Space Missions & National Awards")
                        )
                    )
                ),
                UnitModel(
                    id = "unit-ca-3",
                    appId = "appsc-group2",
                    name = "Andhra Pradesh State Current Affairs & Welfare Schemes",
                    unitNumber = 3,
                    subtitle = "State Direct Benefit Transfers, Capital Works & Port Corridors",
                    imageUrl = "https://images.unsplash.com/photo-1497366216548-37526070297c?w=800&auto=format&fit=crop&q=80",
                    topics = listOf(
                        TopicModel(
                            id = "top-ca-3",
                            appId = "appsc-group2",
                            name = "AP State Schemes, Governance & Development Projects",
                            subtitle = "Polavaram Irrigation Project, Amaravati & Port Logistics",
                            imageUrl = "https://images.unsplash.com/photo-1531206715517-5c0ba140b2b8?w=800&auto=format&fit=crop&q=80",
                            subtopics = listOf("AP Navaratnalu & Social Security Schemes", "Polavaram & Key Irrigation Milestones", "Amaravati & Port Industrial Corridors")
                        )
                    )
                )
            )
        )
    )

    val defaultSubjects: List<Subject>
        get() = getSubjectsForApp(currentActiveAppId.value)

    /**
     * App-Specific Syllabus Isolation.
     * Every app has its own independent syllabus. If an app has no subjects configured yet,
     * it cleanly returns an empty list without leaking or cloning another app's syllabus.
     */
    fun getSubjectsForApp(appId: String): List<Subject> {
        val targetAppId = if (appId.isBlank()) currentActiveAppId.value else appId
        return subjectsState.filter { it.appId == targetAppId }
    }

    // =========================================================================
    // 3. QUESTIONS & PRACTICE ARENA (REAL DATA ONLY)
    // =========================================================================

    val questionsState = mutableStateListOf<Question>().apply {
        addAll(
            listOf(
                Question(
                    id = "sample-hist-1",
                    subjectId = "subj-history",
                    questionText = "Who was the first ruler of the Satavahana dynasty?",
                    options = mapOf("A" to "Simuka", "B" to "Satakarni I", "C" to "Gautamiputra Satakarni", "D" to "Pulumavi"),
                    correctOption = "A",
                    explanation = "Simuka was the founder and first king of the Satavahana dynasty."
                ),
                Question(
                    id = "sample-maths-1",
                    subjectId = "subj-mental-ability",
                    questionText = "If 2x + 3 = 11, what is the value of x?",
                    options = mapOf("A" to "2", "B" to "3", "C" to "4", "D" to "5"),
                    correctOption = "C",
                    explanation = "2x = 11 - 3 => 2x = 8 => x = 4."
                ),
                Question(
                    id = "hist-mcq-101",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-1",
                    setName = "Set 1",
                    questionText = "Which Indus Valley Civilization site is famous for possessing a artificial dockyard?",
                    options = mapOf("A" to "Harappa", "B" to "Lothal", "C" to "Mohenjo-daro", "D" to "Kalibangan"),
                    correctOption = "B",
                    explanation = "Lothal in Gujarat was a major port city of the Harappan civilization featuring a massive tidal dockyard connected to an ancient course of the Sabarmati river."
                ),
                Question(
                    id = "hist-mcq-102",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-1",
                    setName = "Set 1",
                    questionText = "Who was the court poet of King Harshavardhana and author of 'Harshacharita'?",
                    options = mapOf("A" to "Banabhatta", "B" to "Kalidasa", "C" to "Harisena", "D" to "Dandin"),
                    correctOption = "A",
                    explanation = "Banabhatta was the Sanskrit scholar and court poet (Asthana Kavi) of King Harshavardhana. He composed 'Harshacharita' and 'Kadambari'."
                ),
                Question(
                    id = "hist-mcq-103",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-2",
                    setName = "Set 1",
                    questionText = "In which Rock Edict did Emperor Ashoka mention the devastation caused by the Kalinga War?",
                    options = mapOf("A" to "Major Rock Edict V", "B" to "Major Rock Edict X", "C" to "Major Rock Edict XIII", "D" to "Pillar Edict VII"),
                    correctOption = "C",
                    explanation = "Major Rock Edict XIII gives a detailed account of Ashoka's conquest of Kalinga, his deep remorse over the loss of life, and his adoption of Dhamma."
                ),
                Question(
                    id = "hist-mcq-104",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-2",
                    setName = "Set 1",
                    questionText = "Which Gupta ruler adopted the title of 'Vikramaditya' after defeating the Saka rulers of Western India?",
                    options = mapOf("A" to "Chandragupta I", "B" to "Samudragupta", "C" to "Chandragupta II", "D" to "Kumaragupta I"),
                    correctOption = "C",
                    explanation = "Chandragupta II (reigned c. 380–415 CE) defeated the Saka Kshatrapas of Gujarat and Malwa, adopting the title Vikramaditya. Chinese pilgrim Fa-Hien visited India during his reign."
                ),
                Question(
                    id = "hist-mcq-105",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-1",
                    setName = "Set 1",
                    questionText = "Who introduced the silver coin named 'Rupiya' weighing 178 grains in medieval India?",
                    options = mapOf("A" to "Alauddin Khilji", "B" to "Sher Shah Suri", "C" to "Akbar", "D" to "Iltutmish"),
                    correctOption = "B",
                    explanation = "Sher Shah Suri (1540–1545 CE) introduced a standardized silver coin weighing 178 grains named 'Rupiya', which served as the precursor to the modern Indian Rupee."
                ),
                Question(
                    id = "hist-mcq-106",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-2",
                    setName = "Set 1",
                    questionText = "The famous Italian traveler Niccolò de' Conti visited the Vijayanagara Empire during the reign of which king?",
                    options = mapOf("A" to "Harihara I", "B" to "Deva Raya I", "C" to "Krishnadevaraya", "D" to "Achyuta Deva Raya"),
                    correctOption = "B",
                    explanation = "Niccolò de' Conti visited Vijayanagara around 1420 CE during the reign of Deva Raya I of the Sangama dynasty and left detailed records of the city's grandeur."
                ),
                Question(
                    id = "hist-mcq-107",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-2",
                    topicId = "top-hist-3",
                    setName = "Set 1",
                    questionText = "Which Governor-General of India abolished the practice of Sati in 1829?",
                    options = mapOf("A" to "Lord Wellesley", "B" to "Lord William Bentinck", "C" to "Lord Dalhousie", "D" to "Lord Canning"),
                    correctOption = "B",
                    explanation = "Lord William Bentinck, encouraged by social reformer Raja Ram Mohan Roy, promulgated the Bengal Sati Regulation XVII of 1829 banning the practice of Sati."
                ),
                Question(
                    id = "hist-mcq-108",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-2",
                    topicId = "top-hist-4",
                    setName = "Set 1",
                    questionText = "Who presided over the 1929 Lahore Session of the INC where 'Purna Swaraj' (Complete Independence) was declared?",
                    options = mapOf("A" to "Mahatma Gandhi", "B" to "Jawaharlal Nehru", "C" to "Subhash Chandra Bose", "D" to "Sardar Vallabhbhai Patel"),
                    correctOption = "B",
                    explanation = "Jawaharlal Nehru presided over the 1929 Lahore Session of the Indian National Congress, declaring Purna Swaraj as its ultimate objective."
                ),
                Question(
                    id = "hist-mcq-109",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-2",
                    topicId = "top-hist-3",
                    setName = "Set 1",
                    questionText = "The Bardoli Satyagraha of 1928 was led by which prominent leader, earning him the title 'Sardar'?",
                    options = mapOf("A" to "Vallabhbhai Patel", "B" to "Gopabandhu Das", "C" to "C. Rajagopalachari", "D" to "Lala Lajpat Rai"),
                    correctOption = "A",
                    explanation = "Vallabhbhai Patel successfully led the Bardoli farmers against an unfair 30% revenue hike in 1928, after which Mahatma Gandhi and the women of Bardoli bestowed upon him the title 'Sardar'."
                ),
                Question(
                    id = "hist-mcq-110",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-2",
                    topicId = "top-hist-3",
                    setName = "Set 1",
                    questionText = "Which Act introduced Dyarchy at the Provincial level in British India?",
                    options = mapOf("A" to "Indian Councils Act 1892", "B" to "Morley-Minto Reforms 1909", "C" to "Government of India Act 1919", "D" to "Government of India Act 1935"),
                    correctOption = "C",
                    explanation = "The Government of India Act 1919 (Montagu-Chelmsford Reforms) introduced Dyarchy in the provinces by dividing provincial subjects into Transferred and Reserved."
                ),
                Question(
                    id = "hist-mcq-201",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-1",
                    setName = "Set 2",
                    questionText = "Which Upanishad is the source of India's national motto 'Satyamev Jayate'?",
                    options = mapOf("A" to "Katha Upanishad", "B" to "Mundaka Upanishad", "C" to "Chandogya Upanishad", "D" to "Brihadaranyaka Upanishad"),
                    correctOption = "B",
                    explanation = "'Satyamev Jayate' (Truth Alone Triumphs) is a mantra from the ancient Indian scripture Mundaka Upanishad, adopted as the national motto on 26 January 1950."
                ),
                Question(
                    id = "hist-mcq-202",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-1",
                    setName = "Set 2",
                    questionText = "Who was the ruler of Magadha during the invasion of Alexander the Great in 326 BCE?",
                    options = mapOf("A" to "Bimbisara", "B" to "Ajatashatru", "C" to "Dhana Nanda", "D" to "Chandragupta Maurya"),
                    correctOption = "C",
                    explanation = "Dhana Nanda was the last ruler of the Nanda dynasty ruling Magadha when Alexander invaded North-Western India."
                ),
                Question(
                    id = "hist-mcq-203",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-2",
                    setName = "Set 2",
                    questionText = "Which ancient Indian mathematician and astronomer wrote 'Aryabhatiya' and calculated the value of Pi (π) to four decimal places?",
                    options = mapOf("A" to "Varahamihira", "B" to "Aryabhata", "C" to "Brahmagupta", "D" to "Bhaskara I"),
                    correctOption = "B",
                    explanation = "Aryabhata (476–550 CE) composed the mathematical and astronomical treatise Aryabhatiya, introducing the concept of zero, explaining solar and lunar eclipses, and calculating Pi as 3.1416."
                ),
                Question(
                    id = "hist-mcq-204",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-2",
                    setName = "Set 2",
                    questionText = "The famous iron pillar of Delhi, known for its rust-resistant composition, was erected during the reign of which king?",
                    options = mapOf("A" to "Chandragupta II", "B" to "Samudragupta", "C" to "Ashoka", "D" to "Kanishka"),
                    correctOption = "A",
                    explanation = "The rust-resistant Iron Pillar of Delhi was erected by Chandragupta II Vikramaditya (Gupta dynasty) in the late 4th or early 5th century CE to honor Lord Vishnu."
                ),
                Question(
                    id = "hist-mcq-205",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-1",
                    setName = "Set 2",
                    questionText = "Who was the founder of the famous Khilji dynasty of the Delhi Sultanate?",
                    options = mapOf("A" to "Alauddin Khilji", "B" to "Jalaluddin Khilji", "C" to "Qutbuddin Mubarak Shah", "D" to "Ghiyasuddin Balban"),
                    correctOption = "B",
                    explanation = "Jalaluddin Firuz Khilji founded the Khilji dynasty in 1290 CE by overthrowing the Mamluk (Slave) dynasty."
                ),
                Question(
                    id = "hist-mcq-206",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-1",
                    setName = "Set 2",
                    questionText = "Which Mughal emperor built the famous Buland Darwaza at Fatehpur Sikri to commemorate his victory over Gujarat?",
                    options = mapOf("A" to "Babur", "B" to "Humayun", "C" to "Akbar", "D" to "Shah Jahan"),
                    correctOption = "C",
                    explanation = "Akbar built the Buland Darwaza (Great Gate) at Fatehpur Sikri in 1601 CE to celebrate his military conquest and annexation of Gujarat."
                ),
                Question(
                    id = "hist-mcq-207",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-2",
                    topicId = "top-hist-3",
                    setName = "Set 2",
                    questionText = "The Treaty of Seringapatam was signed in 1792 between Tipu Sultan and the British East India Company after which war?",
                    options = mapOf("A" to "First Anglo-Mysore War", "B" to "Second Anglo-Mysore War", "C" to "Third Anglo-Mysore War", "D" to "Fourth Anglo-Mysore War"),
                    correctOption = "C",
                    explanation = "The Third Anglo-Mysore War ended in 1792 with the Treaty of Seringapatam, under which Tipu Sultan surrendered half of his kingdom to the British and their allies."
                ),
                Question(
                    id = "hist-mcq-208",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-2",
                    topicId = "top-hist-3",
                    setName = "Set 2",
                    questionText = "Who was the founder of the 'Servants of India Society' in 1905?",
                    options = mapOf("A" to "Gopal Krishna Gokhale", "B" to "Bal Gangadhar Tilak", "C" to "Lala Lajpat Rai", "D" to "Dadabhai Naoroji"),
                    correctOption = "A",
                    explanation = "Gopal Krishna Gokhale founded the Servants of India Society in Pune in 1905 to promote education, sanitation, health care, and social reforms."
                ),
                Question(
                    id = "hist-mcq-209",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-2",
                    topicId = "top-hist-3",
                    setName = "Set 2",
                    questionText = "In which year did the partition of Bengal take place under Lord Curzon, leading to the Swadeshi Movement?",
                    options = mapOf("A" to "1903", "B" to "1905", "C" to "1907", "D" to "1911"),
                    correctOption = "B",
                    explanation = "The Partition of Bengal was carried out by Viceroy Lord Curzon on 16 October 1905, triggering nationwide protests and the Swadeshi and Boycott movement."
                ),
                Question(
                    id = "hist-mcq-210",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-2",
                    topicId = "top-hist-3",
                    subtopic = "Formation of Indian National Congress",
                    setName = "Set 2",
                    questionText = "Who was the first woman President of the Indian National Congress?",
                    options = mapOf("A" to "Sarojini Naidu", "B" to "Annie Besant", "C" to "Nellie Sengupta", "D" to "Kadambini Ganguly"),
                    correctOption = "B",
                    explanation = "Annie Besant presided over the 1917 Calcutta Session of the INC, becoming its first female President. Sarojini Naidu was the first Indian woman President (1925 Kanpur Session)."
                ),
                // ==========================================
                // INDIAN HISTORY SET 3 (High-Yield 10 MCQs)
                // ==========================================
                Question(
                    id = "hist-mcq-301",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-2",
                    subtopic = "Ashoka Edicts & Administration",
                    setName = "Set 3",
                    difficulty = "Medium",
                    questionText = "The Third Buddhist Council was convened during the reign of Emperor Ashoka at Pataliputra under the presidency of which Buddhist monk?",
                    options = mapOf("A" to "Moggaliputta Tissa", "B" to "Mahakassapa", "C" to "Sabbakami", "D" to "Vasumitra"),
                    correctOption = "A",
                    explanation = "The Third Buddhist Council was held at Pataliputra around 250 BCE during the reign of Emperor Ashoka. It was presided over by Moggaliputta Tissa. The council compiled the Kathavatthu, the last section of the Abhidhamma Pitaka, establishing Theravada orthodoxy."
                ),
                Question(
                    id = "hist-mcq-302",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-2",
                    subtopic = "South Indian Kingdoms - Pallavas & Cholas",
                    setName = "Set 3",
                    difficulty = "Hard",
                    questionText = "Which famous Chola inscription found in Kanchipuram district provides elaborate details about village administration and the 'Kudavolai' lottery electoral system?",
                    options = mapOf("A" to "Aihole Inscription", "B" to "Uttaramerur Inscription", "C" to "Hathigumpha Inscription", "D" to "Halmidi Inscription"),
                    correctOption = "B",
                    explanation = "The Uttaramerur Inscription of Chola King Parantaka I (dated 919 and 921 CE) gives comprehensive rules for village assembly (Sabha), eligibility qualifications, disqualifications, and the Kudavolai (pot-ticket) lottery system for electing village executive committees (Variyams)."
                ),
                Question(
                    id = "hist-mcq-303",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-1",
                    subtopic = "Ancient & Medieval Indian History",
                    setName = "Set 3",
                    difficulty = "Medium",
                    questionText = "Which Delhi Sultan established the market control regulation system and created the office of 'Shahna-i-Mandi' to fix prices of essential commodities?",
                    options = mapOf("A" to "Ghiyasuddin Balban", "B" to "Alauddin Khilji", "C" to "Muhammad bin Tughlaq", "D" to "Feroz Shah Tughlaq"),
                    correctOption = "B",
                    explanation = "Alauddin Khilji introduced strict market control mechanisms, fixing grain, cloth, cattle, and slave prices. He created Diwan-i-Riyasat and appointed Shahna-i-Mandi (market superintendents) backed by barids (intelligence spies) to enforce price stability."
                ),
                Question(
                    id = "hist-mcq-304",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-1",
                    subtopic = "Ancient & Medieval Indian History",
                    setName = "Set 3",
                    difficulty = "Medium",
                    questionText = "In the Mughal Mansabdari administrative system introduced by Akbar, what did the rank designation 'Zat' signify?",
                    options = mapOf("A" to "The number of cavalrymen and horses to be maintained", "B" to "The personal status and salary of the Mansabdar", "C" to "The royal land revenue assignment", "D" to "The title of commander of artillery"),
                    correctOption = "B",
                    explanation = "In Akbar's Mansabdari system, 'Zat' fixed the officer's personal rank, status in the royal hierarchy, and salary scale, whereas 'Sawar' denoted the contingent of horsemen/cavalry the Mansabdar was obligated to maintain for state military service."
                ),
                Question(
                    id = "hist-mcq-305",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-1",
                    topicId = "top-hist-1",
                    subtopic = "Ancient & Medieval Indian History",
                    setName = "Set 3",
                    difficulty = "Medium",
                    questionText = "Which prominent Vaishnavite philosopher and Bhakti saint propagated the philosophy of 'Vishishtadvaita' (Qualified Non-Dualism)?",
                    options = mapOf("A" to "Adi Shankaracharya", "B" to "Sri Ramanujacharya", "C" to "Madhvacharya", "D" to "Nimbarkacharya"),
                    correctOption = "B",
                    explanation = "Sri Ramanujacharya (1017–1137 CE) expounded Vishishtadvaita (qualified non-dualism), arguing that Brahman alone exists but is characterized by multiplicity (individual souls and matter). His philosophical magnum opus is the 'Shribhashya'."
                ),
                Question(
                    id = "hist-mcq-306",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-2",
                    topicId = "top-hist-3",
                    subtopic = "Socio-Religious Reform Movements",
                    setName = "Set 3",
                    difficulty = "Medium",
                    questionText = "Who formulated the 'Drain of Wealth' theory and authored the historic work 'Poverty and Un-British Rule in India'?",
                    options = mapOf("A" to "R.C. Dutt", "B" to "Dadabhai Naoroji", "C" to "Justice M.G. Ranade", "D" to "Dinshaw Wacha"),
                    correctOption = "B",
                    explanation = "Dadabhai Naoroji ('The Grand Old Man of India') first propounded the Drain of Wealth theory in 1867. His classic 1901 book 'Poverty and Un-British Rule in India' calculated the massive unilateral transfer of wealth from India to Britain without adequate return."
                ),
                Question(
                    id = "hist-mcq-307",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-2",
                    topicId = "top-hist-3",
                    subtopic = "Causes & Impact of 1857 Revolt",
                    setName = "Set 3",
                    difficulty = "Medium",
                    questionText = "The famous 'Kakori Train Action' of 9 August 1925 was planned and executed by members of which revolutionary organization?",
                    options = mapOf("A" to "Anushilan Samiti", "B" to "Ghadar Party", "C" to "Hindustan Republican Association (HRA)", "D" to "Hindustan Socialist Republican Association (HSRA)"),
                    correctOption = "C",
                    explanation = "The Kakori train robbery was carried out on August 9, 1925 near Lucknow by the Hindustan Republican Association (HRA), including Ram Prasad Bismil, Ashfaqullah Khan, Chandrashekhar Azad, and Rajendra Lahiri, to fund revolutionary activities against British rule."
                ),
                Question(
                    id = "hist-mcq-308",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-2",
                    topicId = "top-hist-4",
                    subtopic = "Non-Cooperation & Civil Disobedience",
                    setName = "Set 3",
                    difficulty = "Medium",
                    questionText = "The historic Poona Pact was signed on 24 September 1932 primarily between which two leaders to resolve the issue of separate electorates for Depressed Classes?",
                    options = mapOf("A" to "Mahatma Gandhi and Lord Willingdon", "B" to "Dr. B.R. Ambedkar and Madan Mohan Malaviya (on behalf of Gandhi)", "C" to "Jawaharlal Nehru and Muhammad Ali Jinnah", "D" to "Subhash Chandra Bose and Lord Linlithgow"),
                    correctOption = "B",
                    explanation = "The Poona Pact replaced Ramsay MacDonald's Communal Award separate electorates with reserved joint electorates for the Depressed Classes, increasing their seats from 71 to 148 in provincial legislatures. Signed by Dr. B.R. Ambedkar and Madan Mohan Malaviya."
                ),
                Question(
                    id = "hist-mcq-309",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-2",
                    topicId = "top-hist-4",
                    subtopic = "Quit India Movement & INA",
                    setName = "Set 3",
                    difficulty = "Medium",
                    questionText = "In which city did Netaji Subhash Chandra Bose proclaim the formation of the Provisional Government of Free India ('Azad Hind Sarkar') on 21 October 1943?",
                    options = mapOf("A" to "Tokyo", "B" to "Rangoon", "C" to "Singapore", "D" to "Berlin"),
                    correctOption = "C",
                    explanation = "Netaji Subhash Chandra Bose proclaimed the establishment of the Provisional Government of Free India (Arzi Hukumat-e-Azad Hind) at Cathay Cinema Hall in Singapore on 21 October 1943, rallying the Indian National Army (INA) with the slogan 'Chalo Delhi'."
                ),
                Question(
                    id = "hist-mcq-310",
                    appId = "appsc-group2",
                    subjectId = "subj-history",
                    unitId = "unit-hist-2",
                    topicId = "top-hist-4",
                    subtopic = "Partition & Integration of Princely States",
                    setName = "Set 3",
                    difficulty = "Hard",
                    questionText = "Which British mission came to India in March 1946 comprising Lord Pethick-Lawrence, Sir Stafford Cripps, and A.V. Alexander, recommending the formation of the Constituent Assembly?",
                    options = mapOf("A" to "Simon Commission", "B" to "Cripps Mission", "C" to "Cabinet Mission", "D" to "Wavell Plan"),
                    correctOption = "C",
                    explanation = "The Cabinet Mission of 1946 (consisting of Lord Pethick-Lawrence, Sir Stafford Cripps, and A.V. Alexander) proposed a three-tier federal union, rejected partition into Pakistan at that stage, and formulated the scheme for electing the Constituent Assembly of India."
                )
            )
        )
        addAll(IndusValleyData.getQuestions())
    }

    fun updateQuestion(updated: Question) {
        val index = questionsState.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            questionsState[index] = updated
        }
        // Auto-sync question update to Cloud Firestore
        try {
            com.example.util.FirebaseSyncManager.syncQuestionModelToFirestore(updated)
            // Refresh real-time category counts
            val targetApp = updated.appId.ifBlank { currentActiveAppId.value }
            val subjects = getSubjectsForApp(targetApp)
            val prel = subjects.count { it.matchesCategoryFilter("PRELIMS") }
            val main = subjects.count { it.matchesCategoryFilter("MAINS") }
            val gen = subjects.count { it.matchesCategoryFilter("GENERAL") }
            val all = questionsState.count { it.appId == targetApp }
            com.example.util.FirebaseSyncManager.syncCategoryContentCountsToFirestore(
                appId = targetApp,
                counts = com.example.model.CategoryContentCounts(
                    prelimsCount = prel,
                    mainsCount = main,
                    generalCount = gen,
                    allCount = all
                )
            )
        } catch (_: Exception) {}
    }

    fun deleteQuestion(questionId: String, appId: String = currentActiveAppId.value) {
        val q = questionsState.find { it.id == questionId }
        val targetApp = q?.appId?.ifBlank { appId } ?: appId
        questionsState.removeIf { it.id == questionId }
        // Auto-delete from Cloud Firestore
        try {
            com.example.util.FirebaseSyncManager.deleteQuestionFromFirestore(targetApp, questionId)
            // Refresh real-time category counts
            val subjects = getSubjectsForApp(targetApp)
            val prel = subjects.count { it.matchesCategoryFilter("PRELIMS") }
            val main = subjects.count { it.matchesCategoryFilter("MAINS") }
            val gen = subjects.count { it.matchesCategoryFilter("GENERAL") }
            val all = questionsState.count { it.appId == targetApp }
            com.example.util.FirebaseSyncManager.syncCategoryContentCountsToFirestore(
                appId = targetApp,
                counts = com.example.model.CategoryContentCounts(
                    prelimsCount = prel,
                    mainsCount = main,
                    generalCount = gen,
                    allCount = all
                )
            )
        } catch (_: Exception) {}
    }

    fun getQuestionsCountForSubject(subjectId: String, appId: String = ""): Int {
        val targetAppId = if (appId.isNotBlank()) appId else {
            subjectsState.find { it.id == subjectId }?.appId ?: currentActiveAppId.value
        }
        return questionsState.count { it.appId == targetAppId && it.subjectId == subjectId && it.isActive }
    }

    fun getQuestionsCountForUnit(subjectId: String, unitId: String, appId: String = ""): Int {
        val targetAppId = if (appId.isNotBlank()) appId else {
            subjectsState.find { it.id == subjectId }?.appId ?: currentActiveAppId.value
        }
        return questionsState.count { it.appId == targetAppId && it.subjectId == subjectId && it.unitId == unitId && it.isActive }
    }

    fun getQuestionsCountForTopic(subjectId: String, unitId: String, topicId: String, appId: String = ""): Int {
        val targetAppId = if (appId.isNotBlank()) appId else {
            subjectsState.find { it.id == subjectId }?.appId ?: currentActiveAppId.value
        }
        return questionsState.count { it.appId == targetAppId && it.subjectId == subjectId && it.unitId == unitId && it.topicId == topicId && it.isActive }
    }

    fun getQuestionsCountForSubtopic(subjectId: String, unitId: String, topicId: String, subtopic: String, appId: String = ""): Int {
        val targetAppId = if (appId.isNotBlank()) appId else {
            subjectsState.find { it.id == subjectId }?.appId ?: currentActiveAppId.value
        }
        return questionsState.count { 
            it.appId == targetAppId && 
            it.subjectId == subjectId && 
            it.unitId == unitId && 
            it.topicId == topicId && 
            (subtopic.isBlank() || it.subtopic.equals(subtopic, ignoreCase = true)) &&
            it.isActive 
        }
    }

    // App-Specific Data Isolation & Queries (Strictly isolated by appId)
    fun getSubjects(appId: String): List<Subject> = getSubjectsForApp(appId)

    fun getUnits(appId: String, subjectId: String): List<UnitModel> {
        val subject = getSubjectsForApp(appId).find { it.id == subjectId }
        return subject?.units ?: emptyList()
    }

    fun getTopics(appId: String, unitId: String): List<TopicModel> {
        val subjects = getSubjectsForApp(appId)
        for (subj in subjects) {
            val unit = subj.units.find { it.id == unitId }
            if (unit != null) return unit.topics
        }
        return emptyList()
    }

    fun getSubtopics(appId: String, topicId: String): List<String> {
        val subjects = getSubjectsForApp(appId)
        for (subj in subjects) {
            for (unit in subj.units) {
                val topic = unit.topics.find { it.id == topicId }
                if (topic != null) return topic.subtopics
            }
        }
        return emptyList()
    }

    fun getMCQs(
        appId: String,
        subjectId: String = "",
        unitId: String = "",
        topicId: String = "",
        subtopic: String = ""
    ): List<Question> {
        return questionsState.filter { q ->
            q.appId == appId &&
            (subjectId.isBlank() || q.subjectId == subjectId) &&
            (unitId.isBlank() || q.unitId == unitId) &&
            (topicId.isBlank() || q.topicId == topicId) &&
            (subtopic.isBlank() || q.subtopic.equals(subtopic, ignoreCase = true)) &&
            q.isActive
        }
    }

    fun getQuestionsForPractice(subjectId: String, unitId: String, topicId: String, subtopic: String, appId: String = ""): List<Question> {
        if (subjectId == "subj-current-affairs") {
            val monthlyCA = monthlyCurrentAffairsState.find { it.id == unitId }
                ?: monthlyCurrentAffairsState.firstOrNull { it.sets.any { s -> s.id == topicId || s.title.equals(subtopic, ignoreCase = true) } }
            if (monthlyCA != null) {
                val matchedSet = monthlyCA.sets.find {
                    it.id == topicId || it.title.equals(subtopic, ignoreCase = true)
                }
                if (matchedSet != null && matchedSet.questions.isNotEmpty()) {
                    return matchedSet.questions
                }
                val allMonthQuestions = monthlyCA.sets.flatMap { it.questions }
                if (allMonthQuestions.isNotEmpty()) {
                    return allMonthQuestions
                }
            }
        }

        val targetAppId = if (appId.isNotBlank()) appId else {
            subjectsState.find { it.id == subjectId }?.appId ?: currentActiveAppId.value
        }
        
        return questionsState.filter { q ->
            q.appId == targetAppId &&
            q.subjectId == subjectId &&
            (unitId.isBlank() || q.unitId == unitId) &&
            (topicId.isBlank() || q.topicId == topicId) &&
            (subtopic.isBlank() || q.subtopic.equals(subtopic, ignoreCase = true)) &&
            q.isActive
        }
    }

    fun getSetsForHierarchyLevel(subjectId: String, unitId: String = "", topicId: String = "", appId: String = ""): List<PracticeSetModel> {
        val targetAppId = if (appId.isNotBlank()) appId else {
            subjectsState.find { it.id == subjectId }?.appId ?: currentActiveAppId.value
        }
        val filtered = questionsState.filter {
            it.appId == targetAppId &&
            it.subjectId == subjectId &&
            (unitId.isBlank() || it.unitId == unitId) &&
            (topicId.isBlank() || it.topicId == topicId) &&
            it.isActive
        }
        val candidates = if (filtered.isNotEmpty()) {
            filtered
        } else {
            questionsState.filter {
                it.appId == targetAppId &&
                it.subjectId == subjectId &&
                (unitId.isBlank() || it.unitId == unitId || it.unitId.isBlank()) &&
                it.isActive
            }
        }
        if (candidates.isEmpty()) return emptyList()

        val grouped = candidates.groupBy { it.setName.takeIf { name -> name.isNotBlank() } ?: "Set 1" }

        var currentStartQ = 1
        var setIndex = 1
        return grouped.map { (name, qs) ->
            val setNumber = Regex("""\d+""").find(name)?.value?.toIntOrNull() ?: setIndex
            val startNum = currentStartQ
            val endNum = currentStartQ + qs.size - 1
            currentStartQ += qs.size
            setIndex++

            PracticeSetModel(
                id = "set-$subjectId-$unitId-$topicId-$setNumber",
                setNumber = setNumber,
                title = name,
                subtitle = "${qs.size} Qs • Q$startNum – Q$endNum",
                questionsCount = qs.size,
                startQuestionNumber = startNum,
                endQuestionNumber = endNum,
                status = "Not Started",
                isCompleted = false,
                durationMinutes = if (qs.size > 5) 10 else 5,
                totalMarks = qs.size,
                negativeMarks = 0.33,
                difficulty = "Medium",
                badge = when (setNumber % 5) {
                    1 -> "Foundation"
                    2 -> "Concept Booster"
                    3 -> "PYQs Special"
                    4 -> "Speed & Precision"
                    else -> "High Yield"
                },
                questions = qs
            )
        }.sortedBy { it.setNumber }
    }

    fun getPracticeSetsForTopic(subjectId: String, unit: UnitModel, topic: TopicModel): List<PracticeSetModel> {
        return getSetsForHierarchyLevel(subjectId, unit.id, topic.id)
    }

    // =========================================================================
    // 4. CURRENT AFFAIRS & MONTHLY BUNDLES (CONFIGURED VIA ADMIN)
    // =========================================================================

    val currentAffairsState = mutableStateListOf<CurrentAffair>().apply {
        addAll(CurrentAffairsData.initialCurrentAffairsArticles)
    }
    val monthlyCurrentAffairsState = mutableStateListOf<MonthlyCurrentAffairsModel>().apply {
        addAll(CurrentAffairsData.initialMonthlyCurrentAffairs)
    }

    fun getMonthlyCurrentAffairs(): List<MonthlyCurrentAffairsModel> {
        val activeAppId = currentActiveAppId.value
        return monthlyCurrentAffairsState.filter {
            it.mappedAppIds.isEmpty() || it.mappedAppIds.contains(activeAppId) || it.mappedAppIds.contains("ALL") || it.appId == activeAppId
        }
    }

    val defaultCurrentAffairs: List<CurrentAffair>
        get() = currentAffairsState.filter { it.mappedAppIds.contains("ALL") || it.mappedAppIds.contains(currentActiveAppId.value) }

    fun getCurrentAffairsForApp(appId: String): List<CurrentAffair> {
        return currentAffairsState.filter { it.mappedAppIds.contains("ALL") || it.mappedAppIds.contains(appId) }
    }

    fun publishCurrentAffairToApps(ca: CurrentAffair, targetAppIds: List<String>) {
        val updatedCa = ca.copy(
            mappedAppIds = targetAppIds,
            isSharedMultiApp = targetAppIds.size > 1
        )
        val existingIndex = currentAffairsState.indexOfFirst { it.id == ca.id }
        if (existingIndex != -1) {
            currentAffairsState[existingIndex] = updatedCa
        } else {
            currentAffairsState.add(updatedCa)
        }
    }

    fun processCurrentAffairsMcqs(
        jsonString: String,
        month: String,
        year: String,
        targetAppIds: List<String> = emptyList(),
        isActive: Boolean = true,
        startDate: String? = null,
        startTime: String = "00:00",
        endDate: String = "2030-12-31",
        endTime: String = "23:59",
        cardImageUrl: String? = null
    ): Result<Int> {
        try {
            val trimmed = jsonString.trim()
            val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
            
            var targetMonth = month
            var targetYear = year
            var compilationTitle = "$month $year Current Affairs"
            var language = "English"

            val jsonArray: org.json.JSONArray = if (trimmed.startsWith("{")) {
                val rootObj = org.json.JSONObject(trimmed)
                val rawTitle = rootObj.optString("title", "")
                if (rawTitle.isNotBlank()) {
                    compilationTitle = rawTitle
                    for (m in monthNames) {
                        if (rawTitle.contains(m, ignoreCase = true)) {
                            targetMonth = m
                            break
                        }
                    }
                    val yMatch = Regex("""\b(202\d|203\d)\b""").find(rawTitle)
                    if (yMatch != null) {
                        targetYear = yMatch.value
                    }
                }
                language = rootObj.optString("language", "English")
                
                when {
                    rootObj.has("questions") -> rootObj.getJSONArray("questions")
                    rootObj.has("mcqs") -> rootObj.getJSONArray("mcqs")
                    rootObj.has("data") -> rootObj.getJSONArray("data")
                    rootObj.has("items") -> rootObj.getJSONArray("items")
                    else -> throw IllegalArgumentException("Root JSON object must contain 'questions' or 'mcqs' array.")
                }
            } else if (trimmed.startsWith("[")) {
                org.json.JSONArray(trimmed)
            } else {
                throw IllegalArgumentException("Invalid format: Must be a JSON object or array.")
            }

            val newQuestions = mutableListOf<Question>()
            val optionLetters = listOf("A", "B", "C", "D", "E", "F")
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val questionText = obj.optString("questionText", obj.optString("question", "")).trim()
                if (questionText.isBlank()) continue
                
                val optionsMap = mutableMapOf<String, String>()
                if (obj.has("options")) {
                    val optRaw = obj.get("options")
                    if (optRaw is org.json.JSONArray) {
                        for (idx in 0 until optRaw.length()) {
                            val letter = optionLetters.getOrElse(idx) { ('A' + idx).toString() }
                            optionsMap[letter] = optRaw.getString(idx).trim()
                        }
                    } else if (optRaw is org.json.JSONObject) {
                        val keys = optRaw.keys()
                        while (keys.hasNext()) {
                            val k = keys.next().trim().uppercase()
                            optionsMap[k] = optRaw.optString(k, "").trim()
                        }
                    }
                } else {
                    // Fallback for flat option keys
                    listOf("A", "B", "C", "D").forEach { key ->
                        val value = obj.optString("option$key", obj.optString(key, "")).trim()
                        if (value.isNotBlank()) optionsMap[key] = value
                    }
                }

                val explanation = obj.optString("explanation", obj.optString("expl", obj.optString("note", ""))).trim()

                // Determine correct option
                var correctOption = obj.optString(
                    "correctOption",
                    obj.optString("correct_option", obj.optString("answer", obj.optString("correctAnswer", "")))
                ).trim().uppercase()

                if (correctOption.length > 1 && optionsMap.containsValue(correctOption)) {
                    // Answer was given as the option text directly
                    correctOption = optionsMap.entries.firstOrNull { it.value.equals(correctOption, ignoreCase = true) }?.key ?: "A"
                } else if (correctOption.isEmpty() && explanation.isNotBlank() && optionsMap.isNotEmpty()) {
                    // Smart Answer Inference from explanation text
                    val matchingOptions = optionsMap.filter { (_, text) ->
                        text.isNotBlank() && explanation.contains(text, ignoreCase = true)
                    }
                    correctOption = matchingOptions.maxByOrNull { it.value.length }?.key ?: "A"
                } else if (correctOption.isEmpty()) {
                    correctOption = "A"
                }
                
                val q = Question(
                    id = "ca-q-${targetMonth.lowercase()}-$targetYear-${i + 1}-${java.util.UUID.randomUUID().toString().take(6)}",
                    appId = currentActiveAppId.value,
                    subjectId = "subj-current-affairs",
                    unitId = "",
                    topicId = "",
                    contentLevel = "SUBJECT",
                    subtopic = "$targetMonth $targetYear Current Affairs",
                    questionText = questionText,
                    options = optionsMap,
                    correctOption = correctOption,
                    explanation = explanation.ifEmpty { null },
                    language = language
                )
                newQuestions.add(q)
            }
            
            if (newQuestions.isEmpty()) {
                return Result.failure(Exception("No valid Current Affairs MCQs found in the JSON."))
            }

            val caSets = newQuestions.chunked(10).mapIndexed { idx, chunk ->
                val startQ = idx * 10 + 1
                val endQ = startQ + chunk.size - 1
                MonthlyMcqSetModel(
                    id = "ca-set-$targetMonth-$targetYear-${idx + 1}",
                    title = "Set ${idx + 1} (Q$startQ – Q$endQ)",
                    month = targetMonth,
                    year = targetYear,
                    questions = chunk
                )
            }

            val finalStartDate = startDate ?: java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

            val existingCaIndex = monthlyCurrentAffairsState.indexOfFirst { it.month.equals(targetMonth, ignoreCase = true) && it.year == targetYear }
            if (existingCaIndex != -1) {
                val existingCa = monthlyCurrentAffairsState[existingCaIndex]
                val updatedSets = existingCa.sets + caSets
                val updatedMappedApps = (existingCa.mappedAppIds + targetAppIds).distinct()
                monthlyCurrentAffairsState[existingCaIndex] = existingCa.copy(
                    title = compilationTitle.ifBlank { existingCa.title },
                    sets = updatedSets,
                    mappedAppIds = if (updatedMappedApps.isNotEmpty()) updatedMappedApps else existingCa.mappedAppIds,
                    isActive = isActive,
                    startDate = finalStartDate,
                    startTime = startTime,
                    endDate = endDate,
                    endTime = endTime,
                    cardImageUrl = if (!cardImageUrl.isNullOrBlank()) cardImageUrl else existingCa.cardImageUrl
                )
            } else {
                val apps = if (targetAppIds.isNotEmpty()) targetAppIds else listOf(currentActiveAppId.value, "ALL")
                val newMonthlyCa = MonthlyCurrentAffairsModel(
                    id = "ca-$targetMonth-$targetYear",
                    appId = apps.firstOrNull() ?: currentActiveAppId.value,
                    mappedAppIds = apps,
                    month = targetMonth,
                    year = targetYear,
                    title = compilationTitle,
                    sets = caSets,
                    isActive = isActive,
                    startDate = finalStartDate,
                    startTime = startTime,
                    endDate = endDate,
                    endTime = endTime,
                    cardImageUrl = cardImageUrl ?: ""
                )
                monthlyCurrentAffairsState.add(newMonthlyCa)
            }

            return Result.success(newQuestions.size)
        } catch (e: Exception) {
            return Result.failure(Exception("Failed to process Current Affairs JSON: ${e.message}"))
        }
    }

    fun deleteMonthlyCurrentAffairs(id: String) {
        monthlyCurrentAffairsState.removeAll { it.id == id }
    }

    fun unlinkMonthlyCurrentAffairFromApp(id: String, appIdToUnlink: String) {
        val index = monthlyCurrentAffairsState.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = monthlyCurrentAffairsState[index]
            monthlyCurrentAffairsState[index] = item.copy(
                mappedAppIds = item.mappedAppIds.filter { it != appIdToUnlink }
            )
        }
    }

    fun updateMonthlyCurrentAffairsApps(id: String, targetAppIds: List<String>) {
        val index = monthlyCurrentAffairsState.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = monthlyCurrentAffairsState[index]
            monthlyCurrentAffairsState[index] = item.copy(
                mappedAppIds = targetAppIds
            )
        }
    }

    fun updateMonthlyCurrentAffairs(
        id: String,
        title: String? = null,
        isActive: Boolean? = null,
        cardImageUrl: String? = null,
        mappedAppIds: List<String>? = null,
        startDate: String? = null,
        startTime: String? = null,
        endDate: String? = null,
        endTime: String? = null
    ) {
        val index = monthlyCurrentAffairsState.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = monthlyCurrentAffairsState[index]
            monthlyCurrentAffairsState[index] = old.copy(
                title = title ?: old.title,
                isActive = isActive ?: old.isActive,
                cardImageUrl = cardImageUrl ?: old.cardImageUrl,
                mappedAppIds = mappedAppIds ?: old.mappedAppIds,
                startDate = startDate ?: old.startDate,
                startTime = startTime ?: old.startTime,
                endDate = endDate ?: old.endDate,
                endTime = endTime ?: old.endTime
            )
        }
    }

    fun updateAllMonthlyCurrentAffairsWallpaper(imageUrl: String) {
        for (i in monthlyCurrentAffairsState.indices) {
            monthlyCurrentAffairsState[i] = monthlyCurrentAffairsState[i].copy(cardImageUrl = imageUrl)
        }
    }

    // =========================================================================
    // 5. PROMOTIONAL POSTERS & BANNERS (CONFIGURED VIA ADMIN)
    // =========================================================================

    val initialPosters = emptyList<PromotionalPosterModel>()

    val postersState = mutableStateListOf<PromotionalPosterModel>()

    val defaultPosters: List<PromotionalPosterModel>
        get() = postersState.filter { it.mappedAppIds.contains("ALL") || it.mappedAppIds.contains(currentActiveAppId.value) }

    fun getPostersForApp(appId: String): List<PromotionalPosterModel> {
        return postersState.filter { it.mappedAppIds.contains("ALL") || it.mappedAppIds.contains(appId) || it.appId == appId }
    }

    fun publishPosterToApps(poster: PromotionalPosterModel, targetAppIds: List<String>) {
        val updatedPoster = poster.copy(
            mappedAppIds = targetAppIds,
            isSharedMultiApp = targetAppIds.size > 1
        )
        val idx = postersState.indexOfFirst { it.id == poster.id }
        if (idx != -1) {
            postersState[idx] = updatedPoster
        } else {
            postersState.add(updatedPoster)
        }
    }

    // =========================================================================
    // 6. LOGIN QUOTATIONS & BACKGROUND MANAGEMENT (CONFIGURED VIA ADMIN)
    // =========================================================================

    val loginQuotationsState = mutableStateListOf<LoginQuotationModel>()

    fun addLoginQuotation(quote: LoginQuotationModel) {
        val maxOrder = loginQuotationsState.maxOfOrNull { it.order } ?: -1
        loginQuotationsState.add(quote.copy(order = maxOrder + 1))
    }

    fun updateLoginQuotation(quote: LoginQuotationModel) {
        val idx = loginQuotationsState.indexOfFirst { it.id == quote.id }
        if (idx != -1) {
            loginQuotationsState[idx] = quote
        }
    }

    fun deleteLoginQuotation(id: String) {
        loginQuotationsState.removeAll { it.id == id }
        val sorted = loginQuotationsState.sortedBy { it.order }
        loginQuotationsState.clear()
        sorted.forEachIndexed { index, item ->
            loginQuotationsState.add(item.copy(order = index))
        }
    }

    fun reorderQuotationMoveUp(id: String) {
        val list = loginQuotationsState.sortedBy { it.order }.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index > 0) {
            val item = list[index]
            val prev = list[index - 1]
            list[index] = prev.copy(order = item.order)
            list[index - 1] = item.copy(order = prev.order)
            loginQuotationsState.clear()
            loginQuotationsState.addAll(list.sortedBy { it.order })
        }
    }

    fun reorderQuotationMoveDown(id: String) {
        val list = loginQuotationsState.sortedBy { it.order }.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1 && index < list.size - 1) {
            val item = list[index]
            val next = list[index + 1]
            list[index] = next.copy(order = item.order)
            list[index + 1] = item.copy(order = next.order)
            loginQuotationsState.clear()
            loginQuotationsState.addAll(list.sortedBy { it.order })
        }
    }

    // --- Login Backgrounds Management ---
    val loginBackgroundsState = mutableStateListOf<LoginBgImageModel>()

    fun addLoginBackground(bg: LoginBgImageModel) {
        val maxOrder = loginBackgroundsState.maxOfOrNull { it.order } ?: -1
        loginBackgroundsState.add(bg.copy(order = maxOrder + 1))
    }

    fun updateLoginBackground(bg: LoginBgImageModel) {
        val idx = loginBackgroundsState.indexOfFirst { it.id == bg.id }
        if (idx != -1) {
            loginBackgroundsState[idx] = bg
        }
    }

    fun deleteLoginBackground(id: String) {
        loginBackgroundsState.removeAll { it.id == id }
        val sorted = loginBackgroundsState.sortedBy { it.order }
        loginBackgroundsState.clear()
        sorted.forEachIndexed { index, item ->
            loginBackgroundsState.add(item.copy(order = index))
        }
    }

    fun reorderBgMoveUp(id: String) {
        val list = loginBackgroundsState.sortedBy { it.order }.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index > 0) {
            val item = list[index]
            val prev = list[index - 1]
            list[index] = prev.copy(order = item.order)
            list[index - 1] = item.copy(order = prev.order)
            loginBackgroundsState.clear()
            loginBackgroundsState.addAll(list.sortedBy { it.order })
        }
    }

    fun reorderBgMoveDown(id: String) {
        val list = loginBackgroundsState.sortedBy { it.order }.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1 && index < list.size - 1) {
            val item = list[index]
            val next = list[index + 1]
            list[index] = next.copy(order = item.order)
            list[index + 1] = item.copy(order = next.order)
            loginBackgroundsState.clear()
            loginBackgroundsState.addAll(list.sortedBy { it.order })
        }
    }

    // =========================================================================
    // 7. APP WALLPAPER MANAGEMENT (CONFIGURED VIA ADMIN)
    // =========================================================================

    val appWallpapersState = mutableStateListOf<AppWallpaperModel>()

    fun addAppWallpaper(wallpaper: AppWallpaperModel) {
        val maxOrder = appWallpapersState.maxOfOrNull { it.order } ?: -1
        appWallpapersState.add(wallpaper.copy(order = maxOrder + 1))
    }

    fun updateAppWallpaper(wallpaper: AppWallpaperModel) {
        val idx = appWallpapersState.indexOfFirst { it.id == wallpaper.id }
        if (idx != -1) {
            appWallpapersState[idx] = wallpaper
        }
    }

    fun deleteAppWallpaper(id: String) {
        appWallpapersState.removeAll { it.id == id }
        val sorted = appWallpapersState.sortedBy { it.order }
        appWallpapersState.clear()
        sorted.forEachIndexed { index, item ->
            appWallpapersState.add(item.copy(order = index))
        }
    }

    fun reorderWallpaperMoveUp(id: String) {
        val list = appWallpapersState.sortedBy { it.order }.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index > 0) {
            val item = list[index]
            val prev = list[index - 1]
            list[index] = prev.copy(order = item.order)
            list[index - 1] = item.copy(order = prev.order)
            appWallpapersState.clear()
            appWallpapersState.addAll(list.sortedBy { it.order })
        }
    }

    fun reorderWallpaperMoveDown(id: String) {
        val list = appWallpapersState.sortedBy { it.order }.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1 && index < list.size - 1) {
            val item = list[index]
            val next = list[index + 1]
            list[index] = next.copy(order = item.order)
            list[index + 1] = item.copy(order = next.order)
            appWallpapersState.clear()
            appWallpapersState.addAll(list.sortedBy { it.order })
        }
    }

    // =========================================================================
    // 8. SUBSCRIPTIONS & MONETIZATION (CONFIGURED VIA ADMIN)
    // =========================================================================

    val subscriptionPlansState = mutableStateListOf<SubscriptionPlanModel>()

    fun getSubscriptionPlansForApp(appId: String): List<SubscriptionPlanModel> {
        return subscriptionPlansState.filter { it.appId == appId }
    }

    val couponsState = mutableStateListOf<CouponModel>()

    @Volatile
    private var appContext: android.content.Context? = null

    var userProfileState = mutableStateOf(UserProfileModel(
        name = "",
        email = "",
        phone = "",
        district = "Visakhapatnam",
        targetExam = "APPSC Group-2",
        targetAppId = "appsc-group2",
        preferredLanguage = "English",
        dailyGoalQuestions = 30,
        streakDays = 0,
        isProSubscribed = false,
        proPlanName = "",
        isLoggedIn = true,
        isPhoneVerified = false,
        isEmailVerified = false
    ))

    fun initializeWithContext(context: android.content.Context) {
        val appCtx = context.applicationContext
        appContext = appCtx
        try {
            val prefs = appCtx.getSharedPreferences("appsc_user_profile_prefs", android.content.Context.MODE_PRIVATE)
            val savedJson = prefs.getString("user_profile_json", null)
            if (!savedJson.isNullOrBlank()) {
                val restored = com.google.gson.Gson().fromJson(savedJson, UserProfileModel::class.java)
                if (restored != null) {
                    val isLegacyDemoProfile = restored.email == "candidate@prep.portal" ||
                        (restored.proPlanName == "Lifetime Pass" && restored.activatedAt == 0L)
                    userProfileState.value = if (isLegacyDemoProfile) {
                        restored.copy(
                            name = if (restored.name == "Aspirant") "" else restored.name,
                            email = if (restored.email == "candidate@prep.portal") "" else restored.email,
                            isProSubscribed = false,
                            proPlanName = ""
                        )
                    } else {
                        restored
                    }
                }
            }
            val firebaseUser = try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null }
            if (firebaseUser != null) {
                val current = userProfileState.value
                userProfileState.value = current.copy(
                    name = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: current.name,
                    email = firebaseUser.email?.takeIf { it.isNotBlank() } ?: current.email,
                    phone = firebaseUser.phoneNumber?.takeIf { it.isNotBlank() } ?: current.phone,
                    profileImageUrl = firebaseUser.photoUrl?.toString()?.takeIf { it.isNotBlank() } ?: current.profileImageUrl,
                    isLoggedIn = true,
                    isEmailVerified = firebaseUser.isEmailVerified || current.isEmailVerified
                )
            }
        } catch (e: Exception) {
            android.util.Log.w("SampleData", "Failed to initialize userProfile from preferences: ${e.message}")
        }
    }

    fun saveUserProfile(context: android.content.Context? = appContext) {
        val ctx = context?.applicationContext ?: appContext ?: return
        try {
            val prefs = ctx.getSharedPreferences("appsc_user_profile_prefs", android.content.Context.MODE_PRIVATE)
            val profile = userProfileState.value
            val json = com.google.gson.Gson().toJson(profile)
            prefs.edit().putString("user_profile_json", json).apply()
            if (profile.email.isNotBlank() || profile.phone.isNotBlank()) {
                com.example.util.FirebaseSyncManager.syncUserProfileToFirestore(profile)
            }
        } catch (e: Exception) {
            android.util.Log.w("SampleData", "Failed to save userProfile: ${e.message}")
        }
    }

    var sampleUserProfile: UserProfileModel
        get() = userProfileState.value
        set(value) {
            userProfileState.value = value
            saveUserProfile()
        }

    fun logout() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        userProfileState.value = userProfileState.value.copy(
            isLoggedIn = false,
            isProSubscribed = false,
            proPlanName = ""
        )
        saveUserProfile()
    }
    
    fun updateSubscription(planName: String, durationHours: Long = 24L) {
        val now = System.currentTimeMillis()
        val expiresAt = now + (durationHours * 60 * 60 * 1000)
        sampleUserProfile = sampleUserProfile.copy(
            isProSubscribed = true,
            proPlanName = planName,
            activatedAt = now,
            expiresAt = expiresAt,
            isSubscriptionCancelled = false
        )
        saveUserProfile()
    }

    fun cancelSubscription(reason: String, details: String? = null) {
        sampleUserProfile = sampleUserProfile.copy(
            isSubscriptionCancelled = true,
            cancellationReason = reason,
            cancellationDetails = details
        )
        saveUserProfile()
    }

    fun isUserPremium(): Boolean {
        val profile = userProfileState.value
        return profile.isProSubscribed && !profile.isSubscriptionCancelled &&
                (profile.expiresAt == 0L || profile.expiresAt > System.currentTimeMillis())
    }

    // =========================================================================
    // 9. DYNAMIC LISTS DERIVED FROM REAL STATE
    // =========================================================================

    val samplePackages: List<SubscriptionPackage>
        get() = subscriptionPlansState.map { plan ->
            SubscriptionPackage(
                id = plan.id,
                appId = plan.appId,
                name = plan.name,
                price = plan.price,
                currencySymbol = plan.currencySymbol,
                billingCycle = plan.duration,
                features = plan.features,
                isPopular = plan.isPopular
            )
        }

    val testResultsState = mutableStateListOf<TestResult>()

    val samplePyqPapers: List<PyqPaper> = emptyList()

    val sampleMockTests: List<MockTestModel>
        get() = generatedMockTestsState.map { test ->
            MockTestModel(
                id = test.id,
                appId = test.appId,
                title = test.title,
                paperType = "Custom Mock Test",
                durationMinutes = test.durationMinutes,
                totalQuestions = test.questions.size,
                totalMarks = test.questions.size,
                difficulty = "Exam Standard",
                description = "User created custom mock test.",
                isFree = true
            )
        }

    // Admin Panel Managed Data Collections (Real Data)
    val transactionsState = androidx.compose.runtime.mutableStateListOf<com.example.model.TransactionModel>()

    val refundsState = androidx.compose.runtime.mutableStateListOf<com.example.model.RefundRequestModel>()

    val adminUsersState = androidx.compose.runtime.mutableStateListOf<com.example.model.AdminUserModel>()

    val adminNotificationsState = androidx.compose.runtime.mutableStateListOf<com.example.model.AdminNotificationModel>()

    fun getUnreadNotificationCount(): Int {
        return adminNotificationsState.count { it.status == com.example.model.NotificationStatus.SENT && !it.isRead }
    }

    fun markAllNotificationsAsRead() {
        for (i in 0 until adminNotificationsState.size) {
            val item = adminNotificationsState[i]
            if (item.status == com.example.model.NotificationStatus.SENT && !item.isRead) {
                adminNotificationsState[i] = item.copy(isRead = true)
            }
        }
    }

    fun markNotificationAsRead(id: String) {
        val index = adminNotificationsState.indexOfFirst { it.id == id }
        if (index != -1) {
            adminNotificationsState[index] = adminNotificationsState[index].copy(isRead = true)
        }
    }

    fun deleteNotification(id: String) {
        adminNotificationsState.removeAll { it.id == id }
    }

    val adminLegalDocsState = androidx.compose.runtime.mutableStateListOf<com.example.model.LegalDocumentModel>()

    val adminFAQsState = androidx.compose.runtime.mutableStateListOf<com.example.model.FAQModel>()

    val adminSocialLinksState = androidx.compose.runtime.mutableStateListOf<com.example.model.SocialMediaLinkModel>()

    val adminSupportContactState = androidx.compose.runtime.mutableStateOf(
        com.example.model.SupportContactModel(
            email = "support@appscprep.com",
            phone = "+91 98765 43210",
            targetAppIds = listOf("ALL")
        )
    )

    val loginCarouselAutoSlideSecondsState = androidx.compose.runtime.mutableIntStateOf(5)
    val loginCarouselHeightDpState = androidx.compose.runtime.mutableIntStateOf(180)
    val loginScreenBgThemeState = androidx.compose.runtime.mutableStateOf("ROYAL_BLUE")
    val loginCardBgStyleState = androidx.compose.runtime.mutableStateOf("CLASSIC_WHITE")
    val loginCardBorderStyleState = androidx.compose.runtime.mutableStateOf("SILVER_OUTLINE")

    val sampleQuestions: List<Question> get() = questionsState

    fun addLoginBgImage(bg: LoginBgImageModel) = addLoginBackground(bg)
    fun updateLoginBgImage(bg: LoginBgImageModel) = updateLoginBackground(bg)
    fun deleteLoginBgImage(id: String) = deleteLoginBackground(id)
    fun reorderMoveUp(id: String) = reorderBgMoveUp(id)
    fun reorderMoveDown(id: String) = reorderBgMoveDown(id)

    fun deletePoster(id: String) {
        postersState.removeAll { it.id == id }
    }

    fun reorderPosterMoveUp(id: String) {
        val list = postersState.sortedBy { it.priorityOrder }.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index > 0) {
            val item = list[index]
            val prev = list[index - 1]
            list[index] = prev.copy(priorityOrder = item.priorityOrder)
            list[index - 1] = item.copy(priorityOrder = prev.priorityOrder)
            postersState.clear()
            postersState.addAll(list.sortedBy { it.priorityOrder })
        }
    }

    fun reorderPosterMoveDown(id: String) {
        val list = postersState.sortedBy { it.priorityOrder }.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1 && index < list.size - 1) {
            val item = list[index]
            val next = list[index + 1]
            list[index] = next.copy(priorityOrder = item.priorityOrder)
            list[index + 1] = item.copy(priorityOrder = next.priorityOrder)
            postersState.clear()
            postersState.addAll(list.sortedBy { it.priorityOrder })
        }
    }

    fun addSubject(subject: Subject) {
        subjectsState.add(subject)
        try {
            com.example.util.FirebaseSyncManager.syncSubjectToFirestore(subject)
            val activeId = subject.appId.ifBlank { currentActiveAppId.value }
            val subjects = getSubjectsForApp(activeId)
            com.example.util.FirebaseSyncManager.syncCategoryContentCountsToFirestore(
                appId = activeId,
                counts = com.example.model.CategoryContentCounts(
                    prelimsCount = subjects.count { it.matchesCategoryFilter("PRELIMS") },
                    mainsCount = subjects.count { it.matchesCategoryFilter("MAINS") },
                    generalCount = subjects.count { it.matchesCategoryFilter("GENERAL") },
                    allCount = questionsState.count { it.appId == activeId }
                )
            )
        } catch (_: Exception) {}
    }

    fun updateSubject(subject: Subject) {
        val idx = subjectsState.indexOfFirst { it.id == subject.id }
        if (idx != -1) {
            subjectsState[idx] = subject
        }
        try {
            com.example.util.FirebaseSyncManager.syncSubjectToFirestore(subject)
        } catch (_: Exception) {}
    }

    fun deleteSubject(subjectId: String) {
        val target = subjectsState.find { it.id == subjectId }
        val targetApp = target?.appId ?: currentActiveAppId.value
        subjectsState.removeAll { it.id == subjectId }
        try {
            com.example.util.FirebaseSyncManager.deleteSubjectFromFirestore(targetApp, subjectId)
            val subjects = getSubjectsForApp(targetApp)
            com.example.util.FirebaseSyncManager.syncCategoryContentCountsToFirestore(
                appId = targetApp,
                counts = com.example.model.CategoryContentCounts(
                    prelimsCount = subjects.count { it.matchesCategoryFilter("PRELIMS") },
                    mainsCount = subjects.count { it.matchesCategoryFilter("MAINS") },
                    generalCount = subjects.count { it.matchesCategoryFilter("GENERAL") },
                    allCount = questionsState.count { it.appId == targetApp }
                )
            )
        } catch (_: Exception) {}
    }

    fun addUnitToSubject(subjectId: String, unit: UnitModel) {
        val idx = subjectsState.indexOfFirst { it.id == subjectId }
        if (idx != -1) {
            val subject = subjectsState[idx]
            val updated = subject.copy(units = subject.units + unit)
            subjectsState[idx] = updated
            try {
                com.example.util.FirebaseSyncManager.syncSubjectToFirestore(updated)
            } catch (_: Exception) {}
        }
    }

    fun updateUnitInSubject(subjectId: String, unit: UnitModel) {
        val idx = subjectsState.indexOfFirst { it.id == subjectId }
        if (idx != -1) {
            val subject = subjectsState[idx]
            val updatedUnits = subject.units.map { if (it.id == unit.id) unit else it }
            val updated = subject.copy(units = updatedUnits)
            subjectsState[idx] = updated
            try {
                com.example.util.FirebaseSyncManager.syncSubjectToFirestore(updated)
            } catch (_: Exception) {}
        }
    }

    fun deleteUnitFromSubject(subjectId: String, unitId: String) {
        val idx = subjectsState.indexOfFirst { it.id == subjectId }
        if (idx != -1) {
            val subject = subjectsState[idx]
            val updatedUnits = subject.units.filter { it.id != unitId }
            val updated = subject.copy(units = updatedUnits)
            subjectsState[idx] = updated
            try {
                com.example.util.FirebaseSyncManager.syncSubjectToFirestore(updated)
            } catch (_: Exception) {}
        }
    }

    fun addTopicToUnit(subjectId: String, unitId: String, topic: TopicModel) {
        val idx = subjectsState.indexOfFirst { it.id == subjectId }
        if (idx != -1) {
            val subject = subjectsState[idx]
            val updatedUnits = subject.units.map { u ->
                if (u.id == unitId) {
                    u.copy(topics = u.topics + topic)
                } else u
            }
            val updated = subject.copy(units = updatedUnits)
            subjectsState[idx] = updated
            try {
                com.example.util.FirebaseSyncManager.syncSubjectToFirestore(updated)
            } catch (_: Exception) {}
        }
    }

    fun updateTopicInUnit(subjectId: String, unitId: String, topic: TopicModel) {
        val idx = subjectsState.indexOfFirst { it.id == subjectId }
        if (idx != -1) {
            val subject = subjectsState[idx]
            val updatedUnits = subject.units.map { u ->
                if (u.id == unitId) {
                    val updatedTopics = u.topics.map { if (it.id == topic.id) topic else it }
                    u.copy(topics = updatedTopics)
                } else u
            }
            val updated = subject.copy(units = updatedUnits)
            subjectsState[idx] = updated
            try {
                com.example.util.FirebaseSyncManager.syncSubjectToFirestore(updated)
            } catch (_: Exception) {}
        }
    }

    fun deleteTopicFromUnit(subjectId: String, unitId: String, topicId: String) {
        val idx = subjectsState.indexOfFirst { it.id == subjectId }
        if (idx != -1) {
            val subject = subjectsState[idx]
            val updatedUnits = subject.units.map { u ->
                if (u.id == unitId) {
                    val updatedTopics = u.topics.filter { it.id != topicId }
                    u.copy(topics = updatedTopics)
                } else u
            }
            val updated = subject.copy(units = updatedUnits)
            subjectsState[idx] = updated
            try {
                com.example.util.FirebaseSyncManager.syncSubjectToFirestore(updated)
            } catch (_: Exception) {}
        }
    }

    fun updateSubjectImage(subjectId: String, imageUrl: String) {
        val idx = subjectsState.indexOfFirst { it.id == subjectId }
        if (idx != -1) {
            val updated = subjectsState[idx].copy(imageUrl = imageUrl)
            subjectsState[idx] = updated
            try {
                com.example.util.FirebaseSyncManager.syncSubjectToFirestore(updated)
            } catch (_: Exception) {}
        }
    }

    fun updateUnitImage(subjectId: String, unitId: String, imageUrl: String?) {
        val idx = subjectsState.indexOfFirst { it.id == subjectId }
        if (idx != -1) {
            val subject = subjectsState[idx]
            val updatedUnits = subject.units.map { if (it.id == unitId) it.copy(imageUrl = imageUrl) else it }
            val updated = subject.copy(units = updatedUnits)
            subjectsState[idx] = updated
            try {
                com.example.util.FirebaseSyncManager.syncSubjectToFirestore(updated)
            } catch (_: Exception) {}
        }
    }

    fun updateTopicImage(subjectId: String, unitId: String, topicId: String, imageUrl: String?) {
        val idx = subjectsState.indexOfFirst { it.id == subjectId }
        if (idx != -1) {
            val subject = subjectsState[idx]
            val updatedUnits = subject.units.map { u ->
                if (u.id == unitId) {
                    val updatedTopics = u.topics.map { if (it.id == topicId) it.copy(imageUrl = imageUrl) else it }
                    u.copy(topics = updatedTopics)
                } else u
            }
            val updated = subject.copy(units = updatedUnits)
            subjectsState[idx] = updated
            try {
                com.example.util.FirebaseSyncManager.syncSubjectToFirestore(updated)
            } catch (_: Exception) {}
        }
    }

    fun loadBundledHierarchyJson(context: android.content.Context) {
        try {
            val assetManager = context.assets
            val files = assetManager.list("") ?: emptyArray()
            if (files.contains("mcq_syllabus_hierarchy.json")) {
                val jsonString = assetManager.open("mcq_syllabus_hierarchy.json").bufferedReader().use { it.readText() }
                processUploadedMcqs(
                    jsonString = jsonString,
                    subjectId = "",
                    unitId = "",
                    topicId = "",
                    contentLevel = "TOPIC",
                    appId = currentActiveAppId.value
                )
            }
            if (files.contains("satavahanas_mcqs.json")) {
                val satavahanasJson = assetManager.open("satavahanas_mcqs.json").bufferedReader().use { it.readText() }
                processUploadedMcqs(
                    jsonString = satavahanasJson,
                    subjectId = "subj-ap-history",
                    unitId = "unit-aph-1",
                    topicId = "",
                    contentLevel = "UNIT",
                    appId = currentActiveAppId.value
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("SampleData", "Failed to load bundled hierarchy json: ${e.message}")
        }
    }

    /**
     * Loads 331+ app configurations from the bundled assets/apps_config.json.
     */
    fun loadAppsConfigJson(context: android.content.Context) {
        try {
            val assetManager = context.assets
            val files = assetManager.list("") ?: emptyArray()
            if (files.contains("apps_config.json")) {
                val jsonString = assetManager.open("apps_config.json").bufferedReader().use { it.readText() }
                val gson = com.google.gson.Gson()
                val listType = object : com.google.gson.reflect.TypeToken<List<Map<String, String>>>() {}.type
                val rawList: List<Map<String, String>> = gson.fromJson(jsonString, listType)
                
                val newApps = rawList.map { map ->
                    val rawMedium = map["medium"] ?: "English"
                    val rawName = map["name"] ?: ""
                    val cleanMedium = com.example.util.MediumStandardizer.standardizeLanguageName(rawMedium)
                    val cleanName = com.example.util.MediumStandardizer.standardizeAppName(rawName)
                    AppTenantModel(
                        id = map["id"] ?: "",
                        name = cleanName,
                        shortName = cleanName,
                        packageName = map["package"] ?: "",
                        category = "Competitive Exams",
                        examName = cleanName,
                        medium = cleanMedium,
                        primaryLanguage = cleanMedium,
                        allowedLanguages = listOf(cleanMedium),
                        languageCode = map["languageCode"] ?: "en",
                        status = AppStatus.ACTIVE
                    )
                }
                
                if (newApps.isNotEmpty()) {
                    try {
                        appsListState.clear()
                        appsListState.addAll(newApps)
                    } catch (e: Exception) {
                        android.util.Log.w("SampleData", "appsListState update caught: ${e.message}")
                    }
                    android.util.Log.d("SampleData", "Successfully loaded ${newApps.size} apps from JSON config")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SampleData", "Failed to load apps_config.json: ${e.message}")
        }
    }

    var lastImportExecutionResult by mutableStateOf<ImportExecutionResult?>(null)
        private set

    /**
     * Pre-Import Validation Engine
     * Validates JSON syntax, question completeness, and strict hierarchy conformance.
     */
    fun validateImportPackage(
        jsonString: String,
        subjectId: String = "",
        unitId: String = "",
        topicId: String = "",
        appId: String = currentActiveAppId.value
    ): ImportValidationReport {
        val errors = mutableListOf<String>()
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) {
            return ImportValidationReport(
                totalQuestions = 0,
                validQuestions = 0,
                invalidQuestions = 1,
                errors = listOf("JSON content is empty."),
                hierarchySummary = "Empty input",
                targetAppId = appId
            )
        }

        val targetSubject = if (subjectId.isNotBlank()) subjectsState.find { it.id == subjectId } else null
        val targetUnit = if (unitId.isNotBlank() && targetSubject != null) {
            targetSubject.units.find { it.id == unitId }
        } else if (unitId.isNotBlank()) {
            subjectsState.flatMap { it.units }.find { it.id == unitId }
        } else null
        val targetTopic = if (topicId.isNotBlank() && targetUnit != null) {
            targetUnit.topics.find { it.id == topicId }
        } else null

        val subjName = targetSubject?.name ?: "Auto / From JSON"
        val unitName = targetUnit?.let { "Unit ${it.unitNumber}: ${it.name}" } ?: "Auto / From JSON"
        val topicName = targetTopic?.name ?: "All Topics / Auto"

        val parseResult = com.example.util.SyllabusJsonParser.parseJson(trimmed, appId)
        val allQuestions = mutableListOf<Question>()
        
        when (parseResult) {
            is com.example.util.SyllabusParseResult.HierarchyMcqs -> {
                parseResult.packages.forEach { pkg ->
                    pkg.subtopicPackages.forEach { subPkg ->
                        allQuestions.addAll(subPkg.questions)
                    }
                }
            }
            is com.example.util.SyllabusParseResult.FlatMcqs -> {
                allQuestions.addAll(parseResult.questions)
            }
            is com.example.util.SyllabusParseResult.Error -> {
                return ImportValidationReport(
                    totalQuestions = 0,
                    validQuestions = 0,
                    invalidQuestions = 1,
                    errors = listOf("JSON Parse Error: ${parseResult.message}"),
                    hierarchySummary = "Syntax / Schema Error",
                    targetAppId = appId
                )
            }
            else -> {
                return ImportValidationReport(
                    totalQuestions = 0,
                    validQuestions = 0,
                    invalidQuestions = 1,
                    errors = listOf("Unsupported JSON format for MCQ import."),
                    hierarchySummary = "Unrecognized Format",
                    targetAppId = appId
                )
            }
        }

        val report = com.example.util.QuestionImportValidator.validate(
            questions = allQuestions,
            targetAppId = appId,
            targetSubjectId = subjectId,
            targetUnitId = unitId,
            targetTopicId = topicId,
            targetSubtopic = "", 
            targetSetName = "", 
            existingQuestions = questionsState.toList(),
            subjects = subjectsState.toList()
        )

        return report.copy(
            hierarchySummary = when (parseResult) {
                is com.example.util.SyllabusParseResult.HierarchyMcqs -> "${parseResult.packages.size} Topic Packages, ${allQuestions.size} Questions"
                is com.example.util.SyllabusParseResult.FlatMcqs -> "Flat MCQ List (${allQuestions.size} items)"
                else -> "Validated Data"
            },
            errors = report.errors.take(15)
        )
    }

    fun processUploadedMcqs(
        jsonString: String,
        subjectId: String = "",
        unitId: String = "",
        topicId: String = "",
        contentLevel: String = "TOPIC",
        appId: String = currentActiveAppId.value,
        allowDuplicates: Boolean = false,
        adminId: String = "admin-123",
        adminName: String = "System Admin",
        fileName: String = "syllabus_upload.json"
    ): Result<Int> {
        try {
            val trimmed = jsonString.trim()
            if (trimmed.isEmpty()) return Result.failure(Exception("JSON content is empty."))

            val jsonHash = com.example.util.SyllabusJsonParser.calculateHash(trimmed)
            val parseResult = com.example.util.SyllabusJsonParser.parseJson(trimmed, appId)

            val questionsToInsert = mutableListOf<Question>()
            val skippedCount = 0
            var rejectedCount = 0
            var duplicateCount = 0

            when (parseResult) {
                is com.example.util.SyllabusParseResult.HierarchyMcqs -> {
                    for (pkg in parseResult.packages) {
                        // 1. Resolve Target Subject and Unit (STRICT)
                        var targetSubj: Subject? = null
                        var targetUnit: UnitModel? = null

                        if (unitId.isNotBlank()) {
                            for (s in subjectsState) {
                                val u = s.units.find { it.id == unitId }
                                if (u != null) {
                                    targetSubj = s
                                    targetUnit = u
                                    break
                                }
                            }
                            if (targetUnit == null) {
                                return Result.failure(Exception("Strict Hierarchy Rejection: Selected Unit ID '$unitId' does not exist in any Subject."))
                            }
                        } else if (subjectId.isNotBlank()) {
                            targetSubj = subjectsState.find { it.id == subjectId }
                            if (targetSubj == null) {
                                return Result.failure(Exception("Strict Hierarchy Rejection: Selected Subject ID '$subjectId' not found."))
                            }
                        }

                        // FALLBACKS (ONLY when NO target unit or subject was explicitly provided by Admin)
                        if (targetSubj == null) {
                            targetSubj = subjectsState.find { it.appId == appId && it.name.equals(pkg.subjectName, ignoreCase = true) }
                        }
                        if (targetSubj == null) {
                            targetSubj = subjectsState.firstOrNull { it.appId == appId }
                                ?: return Result.failure(Exception("No subject found for App ID '$appId'"))
                        }

                        if (targetUnit == null) {
                            targetUnit = targetSubj.units.find { it.name.equals(pkg.unitName, ignoreCase = true) || (pkg.unitNumber > 0 && it.unitNumber == pkg.unitNumber) }
                        }

                        if (targetUnit == null) {
                            val newUnit = UnitModel(
                                id = "unit-${targetSubj.id}-${System.currentTimeMillis() % 10000}",
                                appId = appId,
                                name = pkg.unitName,
                                unitNumber = if (pkg.unitNumber > 0) pkg.unitNumber else (targetSubj.units.size + 1),
                                topics = emptyList()
                            )
                            val updatedUnits = targetSubj.units + newUnit
                            val subjIdx = subjectsState.indexOfFirst { it.id == targetSubj!!.id }
                            if (subjIdx != -1) subjectsState[subjIdx] = targetSubj.copy(units = updatedUnits)
                            targetUnit = newUnit
                        }

                        // 2. Resolve Target Topic under the Unit (STRICT)
                        val currentTopics = targetUnit.topics.toMutableList()
                        var targetTopic = if (topicId.isNotBlank()) {
                            currentTopics.find { it.id == topicId }
                        } else null

                        if (targetTopic == null) {
                            targetTopic = currentTopics.find { it.name.equals(pkg.topicName, ignoreCase = true) }
                        }

                        if (targetTopic == null) {
                            val sanitized = pkg.topicName.lowercase().replace(Regex("[^a-z0-9]"), "-").take(20)
                            targetTopic = TopicModel(
                                id = "top-${targetUnit.id}-$sanitized-${java.util.UUID.randomUUID().toString().take(4)}",
                                appId = appId,
                                name = pkg.topicName,
                                subtopics = pkg.subtopicPackages.map { it.subtopicName }.filter { it.isNotBlank() }.distinct()
                            )
                            val updatedTopics = targetUnit.topics + targetTopic
                            targetUnit = targetUnit.copy(topics = updatedTopics)
                            val updatedUnits = targetSubj.units.toMutableList()
                            val unitIdx = updatedUnits.indexOfFirst { it.id == targetUnit.id }
                            if (unitIdx != -1) updatedUnits[unitIdx] = targetUnit
                            val subjIdx = subjectsState.indexOfFirst { it.id == targetSubj.id }
                            if (subjIdx != -1) subjectsState[subjIdx] = targetSubj.copy(units = updatedUnits)
                        } else {
                            // Ensure subtopics are registered under the existing topic
                            val existingSubs = targetTopic.subtopics
                            val newSubs = pkg.subtopicPackages.map { it.subtopicName }.filter { it.isNotBlank() }
                            val combined = (existingSubs + newSubs).distinct()
                            if (combined.size > existingSubs.size) {
                                targetTopic = targetTopic.copy(subtopics = combined)
                                val topIdx = currentTopics.indexOfFirst { it.id == targetTopic!!.id }
                                if (topIdx != -1) currentTopics[topIdx] = targetTopic
                                targetUnit = targetUnit.copy(topics = currentTopics)
                                val updatedUnits = targetSubj.units.toMutableList()
                                val unitIdx = updatedUnits.indexOfFirst { it.id == targetUnit.id }
                                if (unitIdx != -1) updatedUnits[unitIdx] = targetUnit
                                val subjIdx = subjectsState.indexOfFirst { it.id == targetSubj.id }
                                if (subjIdx != -1) subjectsState[subjIdx] = targetSubj.copy(units = updatedUnits)
                            }
                        }

                        // 3. Prepare Questions with Strict Foreign Keys and Topic-Scoped IDs
                        pkg.subtopicPackages.forEach { subPkg ->
                            subPkg.questions.forEach { q ->
                                // Hierarchy mismatch validation
                                if (q.unitId.isNotBlank() && unitId.isNotBlank() && q.unitId != targetUnit!!.id) {
                                    rejectedCount++
                                    return@forEach
                                }
                                if (q.topicId.isNotBlank() && topicId.isNotBlank() && q.topicId != targetTopic!!.id) {
                                    rejectedCount++
                                    return@forEach
                                }

                                val subtopicName = if (subPkg.subtopicName.isNotBlank()) subPkg.subtopicName else (q.subtopic.ifBlank { "General" })
                                val rawQId = q.id.trim()
                                // Strict Topic Scoping: prevents simple question IDs (e.g. q1, 1, 2) from colliding across different Topics under the same Unit
                                val scopedId = if (rawQId.startsWith("${targetTopic!!.id}_")) {
                                    rawQId
                                } else if (rawQId.isNotBlank() && !rawQId.startsWith("mcq-")) {
                                    "${targetTopic!!.id}_$rawQId"
                                } else {
                                    "mcq-${targetTopic!!.id}-${java.util.UUID.randomUUID().toString().take(8)}"
                                }

                                val boundQ = q.copy(
                                    id = scopedId,
                                    appId = targetSubj!!.appId.ifBlank { appId },
                                    subjectId = targetSubj!!.id,
                                    unitId = targetUnit!!.id,
                                    topicId = targetTopic!!.id,
                                    contentLevel = "SUBTOPIC",
                                    subtopic = subtopicName,
                                    setName = subtopicName,
                                    language = pkg.language,
                                    contentHash = q.contentHash ?: com.example.util.SyllabusJsonParser.calculateQuestionHash(q.questionText, q.options)
                                )
                                questionsToInsert.add(boundQ)
                            }
                        }
                    }
                }
                is com.example.util.SyllabusParseResult.FlatMcqs -> {
                    val targetSubj = if (subjectId.isNotBlank()) subjectsState.find { it.id == subjectId } else subjectsState.firstOrNull { it.appId == appId }
                    val targetUnit = if (unitId.isNotBlank() && targetSubj != null) targetSubj.units.find { it.id == unitId } else targetSubj?.units?.firstOrNull()

                    parseResult.questions.forEach { q ->
                        var targetTopic: TopicModel? = null
                        if (topicId.isNotBlank() && targetUnit != null) {
                            targetTopic = targetUnit.topics.find { it.id == topicId }
                        } else if (targetUnit != null) {
                            val qTopicName = q.topicId.takeIf { it.isNotBlank() } ?: q.subtopic.takeIf { it.isNotBlank() }
                            if (!qTopicName.isNullOrBlank()) {
                                targetTopic = targetUnit.topics.find { it.name.equals(qTopicName, ignoreCase = true) || it.id == qTopicName }
                            }
                            if (targetTopic == null) {
                                targetTopic = targetUnit.topics.firstOrNull()
                            }
                        }

                        val resolvedTopicId = targetTopic?.id ?: (if (topicId.isNotBlank()) topicId else q.topicId)
                        val rawQId = q.id.trim()
                        val scopedId = if (resolvedTopicId.isNotBlank() && !rawQId.startsWith("${resolvedTopicId}_")) {
                            if (rawQId.isNotBlank() && !rawQId.startsWith("mcq-")) "${resolvedTopicId}_$rawQId"
                            else "mcq-$resolvedTopicId-${java.util.UUID.randomUUID().toString().take(8)}"
                        } else {
                            if (rawQId.isNotBlank()) rawQId else "mcq-${java.util.UUID.randomUUID().toString().take(8)}"
                        }

                        val boundQ = q.copy(
                            id = scopedId,
                            appId = targetSubj?.appId?.ifBlank { appId } ?: appId,
                            subjectId = if (subjectId.isNotBlank()) subjectId else (targetSubj?.id ?: q.subjectId),
                            unitId = if (unitId.isNotBlank()) unitId else (targetUnit?.id ?: q.unitId),
                            topicId = resolvedTopicId,
                            contentLevel = contentLevel,
                            contentHash = q.contentHash ?: com.example.util.SyllabusJsonParser.calculateQuestionHash(q.questionText, q.options)
                        )
                        questionsToInsert.add(boundQ)
                    }
                }
                is com.example.util.SyllabusParseResult.Error -> return Result.failure(Exception(parseResult.message))
                else -> return Result.failure(Exception("Unsupported JSON format for MCQ import."))
            }

            // 4. Transactional Insertion with Strict Topic Isolation
            // An upload to Topic B will NEVER overwrite, reduce, or merge MCQs from Topic A under the same Unit.
            // Create a lookup map for constant-time lookups to avoid O(M*N) quadratic linear scans
            val existingQuestionsMap = mutableMapOf<String, Int>()
            questionsState.forEachIndexed { index, q ->
                existingQuestionsMap["${q.appId}|${q.subjectId}|${q.unitId}|${q.topicId}|${q.id}"] = index
                q.contentHash?.let { hash ->
                    existingQuestionsMap["${q.appId}|${q.subjectId}|${q.unitId}|${q.topicId}|$hash"] = index
                }
            }

            var importedCount = 0
            val newQuestions = mutableListOf<Question>()
            val updatedQuestions = mutableMapOf<Int, Question>()

            questionsToInsert.forEach { boundQ ->
                val keyById = "${boundQ.appId}|${boundQ.subjectId}|${boundQ.unitId}|${boundQ.topicId}|${boundQ.id}"
                val keyByHash = boundQ.contentHash?.let { "${boundQ.appId}|${boundQ.subjectId}|${boundQ.unitId}|${boundQ.topicId}|$it" }
                
                val existingIdx = existingQuestionsMap[keyById] ?: keyByHash?.let { existingQuestionsMap[it] } ?: -1

                if (existingIdx != -1) {
                    if (allowDuplicates) {
                        newQuestions.add(boundQ.copy(id = "mcq-${boundQ.topicId}-${java.util.UUID.randomUUID().toString().take(8)}"))
                        importedCount++
                    } else {
                        updatedQuestions[existingIdx] = boundQ
                        duplicateCount++
                    }
                } else {
                    newQuestions.add(boundQ)
                    importedCount++
                }
            }

            // Apply all modifications in a single batch to avoid redundant Compose recompositions and snapshot updates
            if (updatedQuestions.isNotEmpty() || newQuestions.isNotEmpty()) {
                updatedQuestions.forEach { (index, boundQ) ->
                    questionsState[index] = boundQ
                }
                if (newQuestions.isNotEmpty()) {
                    questionsState.addAll(newQuestions)
                }
            }

            val targetSubjName = subjectsState.find { it.id == subjectId }?.name ?: "All Subjects"
            val targetUnitName = subjectsState.flatMap { it.units }.find { it.id == unitId }?.let { "Unit ${it.unitNumber}: ${it.name}" } ?: "All Units"
            val targetTopicName = subjectsState.flatMap { it.units }.flatMap { it.topics }.find { it.id == topicId }?.name ?: "All Topics"

            val execResult = ImportExecutionResult(
                isSuccess = true,
                totalProcessed = questionsToInsert.size,
                importedCount = importedCount,
                skippedCount = skippedCount,
                rejectedCount = rejectedCount,
                duplicatesCount = duplicateCount,
                targetAppId = appId,
                targetSubjectName = targetSubjName,
                targetUnitName = targetUnitName,
                targetTopicName = targetTopicName,
                message = "Import completed: $importedCount inserted, $duplicateCount duplicates updated, $rejectedCount rejected.",
                jsonHash = jsonHash
            )
            lastImportExecutionResult = execResult

            // Record Audit Log
            logAction(
                adminId = adminId,
                adminName = adminName,
                action = "IMPORT_MCQS",
                appId = appId,
                appName = getActiveApp().name,
                details = "File: $fileName | Processed: ${questionsToInsert.size}, Imported: $importedCount, Duplicates: $duplicateCount, Rejected: $rejectedCount | Destination: ($targetSubjName -> $targetUnitName -> $targetTopicName) | SHA-256: $jsonHash"
            )

            // Sync imported & updated questions to Cloud Firestore for User App
            val affectedQuestions = mutableListOf<Question>()
            affectedQuestions.addAll(newQuestions)
            affectedQuestions.addAll(updatedQuestions.values)
            if (affectedQuestions.isNotEmpty()) {
                try {
                    com.example.util.FirebaseSyncManager.syncQuestionsBatchToFirestore(appId, affectedQuestions)
                    val subjects = getSubjectsForApp(appId)
                    val prel = subjects.count { it.matchesCategoryFilter("PRELIMS") }
                    val main = subjects.count { it.matchesCategoryFilter("MAINS") }
                    val gen = subjects.count { it.matchesCategoryFilter("GENERAL") }
                    val all = questionsState.count { it.appId == appId }
                    com.example.util.FirebaseSyncManager.syncCategoryContentCountsToFirestore(
                        appId = appId,
                        counts = com.example.model.CategoryContentCounts(
                            prelimsCount = prel,
                            mainsCount = main,
                            generalCount = gen,
                            allCount = all
                        )
                    )
                } catch (_: Exception) {}
            }

            return Result.success(importedCount)
        } catch (e: Exception) {
            return Result.failure(Exception("Failed to process MCQs: ${e.message}"))
        }
    }

    /**
     * Database Data Integrity Diagnostic Tool
     * Scans all questions in memory / database for broken foreign keys, orphan links,
     * cross-app leakage, and duplicate hash conflicts.
     */
    fun runDataIntegrityCheck(appId: String = currentActiveAppId.value): DataIntegrityReport {
        val appSubjects = subjectsState.filter { it.appId == appId }
        val subjectIds = appSubjects.map { it.id }.toSet()
        val unitIds = appSubjects.flatMap { it.units }.map { it.id }.toSet()
        val topicMap = mutableMapOf<String, String>() // topicId -> unitId
        val unitMap = mutableMapOf<String, String>() // unitId -> subjectId

        appSubjects.forEach { s ->
            s.units.forEach { u ->
                unitMap[u.id] = s.id
                u.topics.forEach { t ->
                    topicMap[t.id] = u.id
                }
            }
        }

        val appQuestions = questionsState.filter { it.appId == appId }
        val issues = mutableListOf<QuestionHierarchyDiagnostic>()
        var correctCount = 0
        var orphanCount = 0
        var invalidHierarchyCount = 0
        var duplicateHashCount = 0

        val seenHashes = mutableSetOf<String>()

        appQuestions.forEach { q ->
            var hasIssue = false

            // 1. Orphan Subject Check
            if (q.subjectId.isNotBlank() && !subjectIds.contains(q.subjectId)) {
                issues.add(
                    QuestionHierarchyDiagnostic(
                        questionId = q.id,
                        questionText = q.questionText.take(50),
                        issueType = "ORPHAN_SUBJECT",
                        details = "Subject ID '${q.subjectId}' does not exist in App '$appId'.",
                        currentAppId = q.appId,
                        currentSubjectId = q.subjectId,
                        currentUnitId = q.unitId,
                        currentTopicId = q.topicId
                    )
                )
                orphanCount++
                hasIssue = true
            }

            // 2. Orphan Unit Check
            if (q.unitId.isNotBlank() && !unitIds.contains(q.unitId)) {
                issues.add(
                    QuestionHierarchyDiagnostic(
                        questionId = q.id,
                        questionText = q.questionText.take(50),
                        issueType = "ORPHAN_UNIT",
                        details = "Unit ID '${q.unitId}' does not exist in any Subject for App '$appId'.",
                        currentAppId = q.appId,
                        currentSubjectId = q.subjectId,
                        currentUnitId = q.unitId,
                        currentTopicId = q.topicId
                    )
                )
                orphanCount++
                hasIssue = true
            }

            // 3. Unit-Subject Mismatch Check
            if (q.unitId.isNotBlank() && q.subjectId.isNotBlank()) {
                val expectedSubjectId = unitMap[q.unitId]
                if (expectedSubjectId != null && expectedSubjectId != q.subjectId) {
                    issues.add(
                        QuestionHierarchyDiagnostic(
                            questionId = q.id,
                            questionText = q.questionText.take(50),
                            issueType = "UNIT_SUBJECT_MISMATCH",
                            details = "Unit '${q.unitId}' belongs to Subject '$expectedSubjectId', but question points to '${q.subjectId}'.",
                            currentAppId = q.appId,
                            currentSubjectId = q.subjectId,
                            currentUnitId = q.unitId,
                            currentTopicId = q.topicId
                        )
                    )
                    invalidHierarchyCount++
                    hasIssue = true
                }
            }

            // 4. Topic-Unit Mismatch Check
            if (q.topicId.isNotBlank() && q.unitId.isNotBlank()) {
                val expectedUnitId = topicMap[q.topicId]
                if (expectedUnitId != null && expectedUnitId != q.unitId) {
                    issues.add(
                        QuestionHierarchyDiagnostic(
                            questionId = q.id,
                            questionText = q.questionText.take(50),
                            issueType = "TOPIC_UNIT_MISMATCH",
                            details = "Topic '${q.topicId}' belongs to Unit '$expectedUnitId', but question points to '${q.unitId}'.",
                            currentAppId = q.appId,
                            currentSubjectId = q.subjectId,
                            currentUnitId = q.unitId,
                            currentTopicId = q.topicId
                        )
                    )
                    invalidHierarchyCount++
                    hasIssue = true
                }
            }

            // 5. Duplicate Hash Check
            val hash = q.contentHash ?: com.example.util.SyllabusJsonParser.calculateQuestionHash(q.questionText, q.options)
            val fullKey = "${q.appId}|${q.subjectId}|${q.unitId}|${q.topicId}|$hash"
            if (seenHashes.contains(fullKey)) {
                duplicateHashCount++
            } else {
                seenHashes.add(fullKey)
            }

            if (!hasIssue) correctCount++
        }

        return DataIntegrityReport(
            totalQuestionsChecked = appQuestions.size,
            correctMappingsCount = correctCount,
            orphanQuestionsCount = orphanCount,
            invalidHierarchyCount = invalidHierarchyCount,
            duplicateQuestionsCount = duplicateHashCount,
            issues = issues,
            summaryText = if (issues.isEmpty()) "All ${appQuestions.size} questions have verified 100% valid hierarchy relationships."
            else "Found ${issues.size} integrity issues across ${appQuestions.size} questions."
        )
    }

    // =========================================================================
    // 7. CARD IMAGE MANAGEMENT (CURRENT AFFAIRS, MOCK TEST GENERATOR, MOCK TESTS)
    // =========================================================================

    val cardImagesState = mutableStateListOf<CardImageConfigModel>(
        CardImageConfigModel(
            id = "ci-1",
            cardType = "CURRENT_AFFAIRS",
            cardTitle = "Daily Current Affairs Capsule & Updates",
            imageUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=800",
            isEnabled = true,
            order = 0,
            mappedAppIds = listOf("appsc-group2", "appsc-group1", "ap-dsc")
        ),
        CardImageConfigModel(
            id = "ci-2",
            cardType = "MOCK_TEST_GENERATOR",
            cardTitle = "Custom Mock Test Generator Arena",
            imageUrl = "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=800",
            isEnabled = true,
            order = 0,
            mappedAppIds = listOf("appsc-group2", "appsc-group1", "ap-dsc")
        ),
        CardImageConfigModel(
            id = "ci-3",
            cardType = "MOCK_TEST",
            cardTitle = "Full Length Grand Test Series",
            imageUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800",
            isEnabled = true,
            order = 0,
            mappedAppIds = listOf("appsc-group2", "appsc-group1")
        )
    )

    fun addCardImage(config: CardImageConfigModel) {
        val maxOrder = cardImagesState.filter { it.cardType == config.cardType }.maxOfOrNull { it.order } ?: -1
        cardImagesState.add(config.copy(order = maxOrder + 1))
    }

    fun updateCardImage(config: CardImageConfigModel) {
        val idx = cardImagesState.indexOfFirst { it.id == config.id }
        if (idx != -1) {
            cardImagesState[idx] = config
        }
    }

    fun deleteCardImage(id: String) {
        cardImagesState.removeAll { it.id == id }
    }

    fun reorderCardImageMoveUp(id: String) {
        val list = cardImagesState.sortedBy { it.order }.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index > 0) {
            val item = list[index]
            val prev = list[index - 1]
            list[index] = prev.copy(order = item.order)
            list[index - 1] = item.copy(order = prev.order)
            cardImagesState.clear()
            cardImagesState.addAll(list.sortedBy { it.order })
        }
    }

    fun reorderCardImageMoveDown(id: String) {
        val list = cardImagesState.sortedBy { it.order }.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1 && index < list.size - 1) {
            val item = list[index]
            val next = list[index + 1]
            list[index] = next.copy(order = item.order)
            list[index + 1] = item.copy(order = next.order)
            cardImagesState.clear()
            cardImagesState.addAll(list.sortedBy { it.order })
        }
    }

    fun getCardImagesForAppAndType(appId: String, cardType: String): List<CardImageConfigModel> {
        return cardImagesState.filter { it.isEnabled && it.cardType == cardType && (it.mappedAppIds.isEmpty() || it.mappedAppIds.contains(appId) || it.mappedAppIds.contains("ALL")) }
    }

    // =========================================================================
    // 1.1 DEFAULT LEGAL & SUPPORT CONTENT
    // =========================================================================

    fun getDefaultLegalDocs(): List<LegalDocument> = listOf(
        LegalDocument(
            id = "default_privacy_policy",
            type = LegalDocType.PRIVACY_POLICY,
            title = "Privacy Policy",
            content = """
                Your privacy is important to us. It is {{APP_NAME}}'s policy to respect your privacy regarding any information we may collect from you through our app.

                1. Information We Collect
                We only ask for personal information when we truly need it to provide a service to you. We collect it by fair and lawful means, with your knowledge and consent.

                2. Data Storage
                We only retain collected information for as long as necessary to provide you with your requested service. What data we store, we’ll protect within commercially acceptable means to prevent loss and theft, as well as unauthorized access, disclosure, copying, use or modification.

                3. Information Sharing
                We don’t share any personally identifying information publicly or with third-parties, except when required to by law.

                4. Third-Party Services
                Our app may link to external sites that are not operated by us. Please be aware that we have no control over the content and practices of these sites, and cannot accept responsibility or liability for their respective privacy policies.

                5. Your Rights
                You are free to refuse our request for your personal information, with the understanding that we may be unable to provide you with some of your desired services.

                Your continued use of our app will be regarded as acceptance of our practices around privacy and personal information. If you have any questions about how we handle user data and personal information, feel free to contact us.
            """.trimIndent(),
            mappedAppIds = emptyList() // All apps
        ),
        LegalDocument(
            id = "default_terms_conditions",
            type = LegalDocType.TERMS_CONDITIONS,
            title = "Terms & Conditions",
            content = """
                Welcome to {{APP_NAME}}! By using our service, you agree to these terms.

                1. License to Use
                We grant you a non-transferable, non-exclusive license to use the app for personal, non-commercial educational purposes.

                2. User Conduct
                You agree not to use the app for any purpose that is unlawful or prohibited by these Terms. You may not use the app in any manner that could damage, disable, overburden, or impair the app.

                3. Intellectual Property
                The content, including but not limited to MCQs, study material, and interface design, is the property of {{APP_NAME}} and is protected by copyright laws.

                4. Termination
                We may terminate or suspend your access to the app immediately, without prior notice or liability, for any reason whatsoever, including without limitation if you breach the Terms.

                5. Disclaimer
                Your use of the app is at your sole risk. The service is provided on an "AS IS" and "AS AVAILABLE" basis.
            """.trimIndent(),
            mappedAppIds = emptyList()
        ),
        LegalDocument(
            id = "default_refund_policy",
            type = LegalDocType.REFUND_CANCELLATION,
            title = "Refund & Cancellation Policy",
            content = """
                1. No Refund Policy
                Due to the digital nature of our educational content (MCQs, Mock Tests, Study Material), all purchases are final. We do not offer refunds once access to the premium content has been granted.

                2. Subscription Cancellation
                You can cancel your subscription renewal at any time. However, your current active subscription will remain active until the end of its duration.

                3. Special Cases
                In cases of accidental double payment for the same plan, please contact support within 24 hours with your transaction IDs for a manual review and refund of the duplicate amount.
            """.trimIndent(),
            mappedAppIds = emptyList()
        ),
        LegalDocument(
            id = "default_account_deletion",
            type = LegalDocType.ACCOUNT_DELETION,
            title = "Account & Data Deletion",
            content = """
                You have the right to request the deletion of your account and all associated personal data from our systems.

                1. How to Delete
                You can initiate account deletion directly from the Profile -> Legal & Support Center section in the app, or by contacting our support team via email.

                2. What is Deleted
                - Your profile information (Name, Email, Phone)
                - Your exam progress and mock test history
                - Your bookmarked questions
                - Any saved notes or preferences

                3. Retention
                Some data may be retained for a limited period as required by law or for legitimate business purposes (e.g., transaction records for tax compliance).
            """.trimIndent(),
            mappedAppIds = emptyList()
        ),
        LegalDocument(
            id = "default_about_us",
            type = LegalDocType.ABOUT_US,
            title = "About Us",
            content = """
                Welcome to {{APP_NAME}}, your premier destination for competitive exam preparation.

                1. Our Mission
                Our mission is to provide high-quality, accessible, and affordable educational resources to every aspirant. We believe in empowering students through technology and expert-curated content.

                2. What We Offer
                - Extensive Question Banks (MCQs)
                - Realistic Full-Length Mock Tests
                - Detailed Explanations for every concept
                - Performance Analytics and Streak Tracking

                3. Our Vision
                We envision a world where every candidate has an equal opportunity to succeed in their career goals, regardless of their background.

                Thank you for choosing {{APP_NAME}} as your learning partner.
            """.trimIndent(),
            mappedAppIds = emptyList()
        ),
        LegalDocument(
            id = "default_help_center",
            type = LegalDocType.HELP_CENTER,
            title = "Help Center",
            content = """
                Welcome to the {{APP_NAME}} Help Center & Support Desk.

                1. Getting Started & Dashboard
                Explore the daily updated Syllabus topics, Grand Mock Tests, and Instant Analysis directly from your personalized dashboard.

                2. Full-Length Mock Tests
                Practice timed tests matching the actual exam pattern with negative marking and detailed solution explanations.

                3. Subscriptions & Payment Help
                Instant activation upon payment via UPI/Cards. In case of transaction issues or access queries, please contact our support team.

                4. Technical Assistance
                For any performance or sync issues, verify that you are connected to the internet and running the latest app version.
            """.trimIndent(),
            mappedAppIds = emptyList()
        ),
        LegalDocument(
            id = "default_contact_us",
            type = LegalDocType.CONTACT_US,
            title = "Contact Us",
            content = """
                We are dedicated to providing fast and reliable support to all aspirants.

                1. Customer & Candidate Support
                Reach out to our academic and technical support team for any queries regarding course material, test series, or subscriptions.

                2. Support Hours
                Our team is available Monday through Saturday from 9:00 AM to 6:00 PM IST.

                3. Escalation & Feedback
                We value your feedback. For urgent concerns, please provide your registered candidate details.
            """.trimIndent(),
            mappedAppIds = emptyList()
        ),
        LegalDocument(
            id = "default_shipping_policy",
            type = LegalDocType.SHIPPING_DELIVERY,
            title = "Shipping & Digital Delivery Policy",
            content = """
                1. 100% Digital Educational Service
                APPSC Group 2 Exam Prep Pro (operated by SOBHA DHANARAJU, MID: T89U1OXHFIQbHa) provides exclusively digital study material, topic-wise MCQs, and online mock tests inside the mobile application. No physical books or tangible items are shipped.

                2. Instant In-App Fulfilment (0–5 Minutes)
                Upon payment verification via Razorpay (UPI, Cards, NetBanking, Wallets) or Google Play Billing, your premium subscription pass is unlocked instantaneously on your account.

                3. Shipping & Handling Charges
                Because all preparation sets and mock exams are delivered digitally over the internet inside the Android app, shipping and handling charges are ₹0 (Nil) across all subscription plans.
            """.trimIndent(),
            mappedAppIds = emptyList()
        )
    )

    fun getDefaultFaqs(): List<SupportFaq> = listOf(
        SupportFaq(
            id = "faq_access_mock",
            question = "How do I access Mock Tests?",
            answer = "Go to the Dashboard and click on the 'Mock Tests' card. You can then choose between Subject-wise tests or Full-length grand mocks.",
            mappedAppIds = emptyList()
        ),
        SupportFaq(
            id = "faq_offline_use",
            question = "Can I use the app offline?",
            answer = "Currently, an active internet connection is required to fetch the latest questions and sync your progress. However, once a test is loaded, you can complete it without a continuous connection.",
            mappedAppIds = emptyList()
        ),
        SupportFaq(
            id = "faq_renew_subscription",
            question = "How do I renew my subscription?",
            answer = "When your plan expires, you will see a 'Renew' button on your profile page and on the home screen banners. Click it to choose a new plan and pay via Razorpay.",
            mappedAppIds = emptyList()
        ),
        SupportFaq(
            id = "faq_latest_syllabus",
            question = "Are the questions based on the latest syllabus?",
            answer = "Yes! Our content team updates the question bank regularly based on the official notifications issued by the respective exam boards.",
            mappedAppIds = emptyList()
        ),
        SupportFaq(
            id = "faq_offline_99_plan",
            question = "What is the ₹99 Offline Subscription Plan and how does it work?",
            answer = "The ₹99 Offline Pass allows you to download and practice MCQs offline without an internet connection. Once active, you can access downloaded content through Sets, Subtopics, and Downloaded Materials anytime.",
            mappedAppIds = emptyList()
        ),
        SupportFaq(
            id = "faq_offline_99_validity",
            question = "What is the validity period of the ₹99 Offline Pass?",
            answer = "The validity period of your ₹99 Offline Pass automatically aligns with your existing active subscription plan duration.",
            mappedAppIds = emptyList()
        ),
        SupportFaq(
            id = "faq_offline_99_expiry",
            question = "What happens when the ₹99 Offline Pass expires?",
            answer = "Upon expiry of your offline pass, offline access to downloaded materials is paused until renewal. However, you will still be able to practice MCQs online anytime for free.",
            mappedAppIds = emptyList()
        )
    )
}

