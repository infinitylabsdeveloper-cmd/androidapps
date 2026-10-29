package com.example

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import com.example.data.SampleData
import com.example.model.GeneratedMockTest
import com.example.model.MockTestAttemptRecord
import com.example.model.Question
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.model.UnitModel
import com.example.model.PracticeSectionType
import com.example.ui.screens.*
import com.example.ui.theme.LocalAppConfig
import com.example.ui.theme.SyllabusProTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class AppScreen {
    abstract val routeKey: String

    data object Splash : AppScreen() {
        override val routeKey: String = "splash"
    }

    data object Home : AppScreen() {
        override val routeKey: String = "home"
    }

    /**
     * Level 1 -> Level 2: Full-screen Units page for the selected Subject.
     */
    data class SyllabusExplorer(
        val subjectId: String,
        val fallbackSubject: Subject? = null
    ) : AppScreen() {
        constructor(subject: Subject, initialUnit: UnitModel? = null) : this(
            subjectId = subject.id,
            fallbackSubject = subject
        )

        override val routeKey: String = "units_$subjectId"
    }

    /**
     * Level 2 -> Level 3: Full-screen Topics page for the selected Unit.
     */
    data class UnitTopics(
        val subjectId: String,
        val unitId: String,
        val fallbackSubject: Subject? = null,
        val fallbackUnit: UnitModel? = null
    ) : AppScreen() {
        constructor(subject: Subject, unit: UnitModel) : this(
            subjectId = subject.id,
            unitId = unit.id,
            fallbackSubject = subject,
            fallbackUnit = unit
        )

        override val routeKey: String = "topics_${subjectId}_$unitId"
    }

    /**
     * Level 3 -> Level 4: Full-screen Sets page for the selected Topic.
     */
    data class TopicSets(
        val subjectId: String,
        val unitId: String,
        val topicId: String,
        val fallbackSubject: Subject? = null,
        val fallbackUnit: UnitModel? = null,
        val fallbackTopic: TopicModel? = null
    ) : AppScreen() {
        constructor(subject: Subject, unit: UnitModel, topic: TopicModel) : this(
            subjectId = subject.id,
            unitId = unit.id,
            topicId = topic.id,
            fallbackSubject = subject,
            fallbackUnit = unit,
            fallbackTopic = topic
        )

        override val routeKey: String = "sets_${subjectId}_${unitId}_$topicId"
    }

    /**
     * Level 4 -> Level 5: Full-screen Subtopics page for the selected Set.
     */
    data class SetSubtopics(
        val subjectId: String,
        val unitId: String,
        val topicId: String,
        val setId: String,
        val setNumber: Int = 1,
        val setTitle: String = "",
        val fallbackSubject: Subject? = null,
        val fallbackUnit: UnitModel? = null,
        val fallbackTopic: TopicModel? = null
    ) : AppScreen() {
        override val routeKey: String = "subtopics_${subjectId}_${unitId}_${topicId}_$setId"
    }

    /**
     * Level 5 -> Dedicated Content/Detail Screen: Full-screen Practice Arena for the selected Subtopic or Set.
     */
    data class PracticeArena(
        val subjectId: String,
        val unitId: String,
        val topicId: String,
        val setName: String,
        val setId: String = "",
        val subtopicId: String = "",
        val subtopicTitle: String = "",
        val preloadedQuestions: List<Question> = emptyList(),
        val sectionType: PracticeSectionType? = null,
        val initialQuestionIndex: Int = 0,
        val preserveQuestionOrder: Boolean = false
    ) : AppScreen() {
        override val routeKey: String =
            "practice_${subjectId}_${unitId}_${topicId}_${setId}_${subtopicId}_${sectionType?.name ?: "default"}_${setName}_$initialQuestionIndex"
    }

    data class OfflineSubjectDetail(
        val subject: Subject
    ) : AppScreen() {
        override val routeKey: String = "offline_detail_${subject.id}"
    }

    data class Result(
        val subjectId: String = "",
        val unitId: String = "",
        val topicId: String = "",
        val setName: String = "",
        val setId: String = "",
        val subtopicId: String = "",
        val subtopicTitle: String = "",
        val questions: List<Question> = emptyList(),
        val userAnswers: Map<String, String> = emptyMap(),
        val correctCount: Int = 0,
        val wrongCount: Int = 0,
        val score: Double = 0.0,
        val timeSpent: Long = 0L
    ) : AppScreen() {
        override val routeKey: String = "result_${subjectId}_${unitId}_${topicId}_${setId}_${subtopicId}_${correctCount}_${wrongCount}_${score.toInt()}"
    }

    data object Subscription : AppScreen() {
        override val routeKey: String = "subscription"
    }

    data object Login : AppScreen() {
        override val routeKey: String = "login"
    }

    data object AdminDashboard : AppScreen() {
        override val routeKey: String = "admin_dashboard"
    }

    data object Profile : AppScreen() {
        override val routeKey: String = "profile"
    }

    data object SavedQuestions : AppScreen() {
        override val routeKey: String = "saved_questions"
    }

    data object Notifications : AppScreen() {
        override val routeKey: String = "notifications"
    }

    data object CurrentAffairs : AppScreen() {
        override val routeKey: String = "current_affairs"
    }

    data object DownloadedMaterials : AppScreen() {
        override val routeKey: String = "downloaded_materials"
    }




    data object LuxuryFontStudio : AppScreen() {
        override val routeKey: String = "luxury_font_studio"
    }
    data object MockTestGenerator : AppScreen() {
        override val routeKey: String = "mock_test_generator"
    }

    data object AvailableMockTests : AppScreen() {
        override val routeKey: String = "available_mock_tests"
    }

    data class FullLengthMockTest(
        val testId: String,
        val testTitle: String,
        val timeMinutes: Int
    ) : AppScreen() {
        override val routeKey: String = "full_mock_$testId"
    }

    data class MockTestResult(
        val testId: String
    ) : AppScreen() {
        override val routeKey: String = "mock_test_result_$testId"
    }

    data object MockTests : AppScreen() {
        override val routeKey: String = "mock_tests"
    }

    data object Performance : AppScreen() {
        override val routeKey: String = "performance"
    }

    data object Leaderboard : AppScreen() {
        override val routeKey: String = "leaderboard"
    }

    data object ContactUs : AppScreen() {
        override val routeKey: String = "contact_us"
    }

    data class LegalSupport(val initialSection: String? = null) : AppScreen() {
        override val routeKey: String = "legal_support_${initialSection ?: "default"}"
    }

    data object PreviousYearPapers : AppScreen() {
        override val routeKey: String = "previous_year_papers"
    }
}

class MainActivity : ComponentActivity(), com.razorpay.PaymentResultWithDataListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure ViewTree lifecycle, saved state, and view model store owners are explicitly bound to window.decorView
        window.decorView.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
        }

        com.example.repository.AppConfigRepository.getInstance().initializeWithContext(this)
        SampleData.initializeWithContext(this)
        com.example.util.AnimationConfigManager.initialize(this)
        com.example.util.BillingPaymentManager.loadMerchantConfig(this)
        com.example.ui.theme.LuxuryFontStudioManager.init(this)
        try {
            com.razorpay.Checkout.preload(applicationContext)
        } catch (_: Throwable) {}
        enableEdgeToEdge()
        setContent {
            val appConfigRepository = remember { com.example.repository.AppConfigRepository.getInstance() }
            val appConfig by appConfigRepository.currentAppConfig.collectAsStateWithLifecycle()

            CompositionLocalProvider(
                LocalAppConfig provides appConfig
            ) {
                SyllabusProTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigationHost()
                    }
                }
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: com.razorpay.PaymentData?) {
        com.example.util.BillingPaymentManager.onRazorpaySuccess(
            paymentId = razorpayPaymentId ?: paymentData?.paymentId ?: "",
            orderId = paymentData?.orderId,
            signature = paymentData?.signature
        )
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: com.razorpay.PaymentData?) {
        com.example.util.BillingPaymentManager.onRazorpayError(code, response)
    }
}

@Composable
fun AppNavigationHost() {
    val saveableStateHolder = rememberSaveableStateHolder()
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }
    var screenHistory by remember { mutableStateOf<List<AppScreen>>(emptyList()) }
    var isNavigatingForward by remember { mutableStateOf(true) }
    var lastNavigationTimeMs by remember { mutableLongStateOf(0L) }
    var lastNavigatedRouteKey by remember { mutableStateOf("") }

    fun navigateTo(screen: AppScreen) {
        val now = SystemClock.elapsedRealtime()
        // Prevent duplicate destinations from rapid repeated taps
        if (currentScreen.routeKey == screen.routeKey) return
        if (lastNavigatedRouteKey == screen.routeKey && (now - lastNavigationTimeMs) < 300L) return

        lastNavigationTimeMs = now
        lastNavigatedRouteKey = screen.routeKey
        isNavigatingForward = true
        if (screenHistory.lastOrNull()?.routeKey != currentScreen.routeKey) {
            screenHistory = screenHistory + currentScreen
        }
        currentScreen = screen
    }

    fun goBack() {
        val poppedScreen = currentScreen
        val last = screenHistory.lastOrNull()
        isNavigatingForward = false
        if (last != null) {
            screenHistory = screenHistory.dropLast(1)
            currentScreen = last
            saveableStateHolder.removeState(poppedScreen.routeKey)
        } else if (currentScreen != AppScreen.Home && currentScreen != AppScreen.Splash) {
            currentScreen = AppScreen.Home
            saveableStateHolder.removeState(poppedScreen.routeKey)
        }
    }

    fun navigateToHome() {
        if (currentScreen != AppScreen.Home) {
            isNavigatingForward = false
            screenHistory = emptyList()
            currentScreen = AppScreen.Home
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeAppId by SampleData.currentActiveAppId
    val database = remember { com.example.db.AppDatabase.getDatabase(context) }
    val appRepository = remember(activeAppId) { com.example.repository.AppRepository(database, activeAppId) }
    val leaderboardRepository = remember { com.example.repository.LeaderboardRepository.getInstance(context) }

    val bookmarkedQuestions by appRepository.allBookmarks.collectAsStateWithLifecycle()
    val allTestResults by appRepository.allTestResults.collectAsStateWithLifecycle()
    val persistedMockTests by appRepository.allMockTests.collectAsStateWithLifecycle()

    // Sync persisted Room test results & mock tests into SampleData state for cross-screen consistency
    LaunchedEffect(allTestResults) {
        if (allTestResults.isNotEmpty()) {
            val existingIds = SampleData.testResultsState.map { it.id }.toSet()
            val newResults = allTestResults.filter { !existingIds.contains(it.id) }
            if (newResults.isNotEmpty()) {
                SampleData.testResultsState.addAll(0, newResults)
            }
        }
    }

    LaunchedEffect(persistedMockTests, activeAppId) {
        val demoTemplateIds = setOf(
            "mock-01", "mock-02", "mock-03",
            "mock-unit-01", "mock-unit-02",
            "mock-topic-01", "mock-topic-02"
        )
        SampleData.seedDefaultMockTestsIfEmpty(activeAppId)
        if (persistedMockTests.isNotEmpty()) {
            persistedMockTests.forEach { dbTest ->
                if (dbTest.id in demoTemplateIds) {
                    appRepository.deleteMockTest(dbTest.id)
                } else {
                    val idx = SampleData.generatedMockTestsState.indexOfFirst { it.id == dbTest.id }
                    if (idx != -1) {
                        SampleData.generatedMockTestsState[idx] = dbTest
                    } else {
                        SampleData.generatedMockTestsState.add(0, dbTest)
                    }
                }
            }
        }
    }

    // Start real-time Firestore Master Admin Config sync so Admin Panel updates immediately reflect in user screens
    LaunchedEffect(activeAppId) {
        try {
            com.example.util.FirebaseSyncManager.startMasterAdminConfigSync(
                appId = activeAppId,
                coroutineScope = coroutineScope
            )
        } catch (_: Exception) {}
    }

    // Cache updated subjects and questions from SampleData into Room
    LaunchedEffect(SampleData.subjectsState.size, activeAppId) {
        val currentSubjects = SampleData.getSubjectsForApp(activeAppId)
        if (currentSubjects.isNotEmpty()) {
            appRepository.cacheSubjects(currentSubjects)
        }
    }

    LaunchedEffect(SampleData.questionsState.size, activeAppId) {
        val currentQuestions = SampleData.questionsState.filter { it.appId == activeAppId || it.appId == "ALL" }
        if (currentQuestions.isNotEmpty()) {
            appRepository.saveQuestions(currentQuestions)
        }
    }

    val userProfile by SampleData.userProfileState
    LaunchedEffect(userProfile.isLoggedIn) {
        if (!userProfile.isLoggedIn && currentScreen !is AppScreen.Login && currentScreen !is AppScreen.Splash) {
            screenHistory = emptyList()
            currentScreen = AppScreen.Login
        }
    }

    val checkOfflineAccessAndProceed: (() -> Unit) -> Unit = { onProceed ->
        val profile = SampleData.userProfileState.value
        val hasActiveOfflineSub = profile.isLoggedIn && (profile.isProSubscribed || profile.expiresAt > System.currentTimeMillis())
        if (hasActiveOfflineSub) {
            onProceed()
        } else {
            android.widget.Toast.makeText(context, "Active ₹99 Offline Subscription required to access downloaded MCQs offline.", android.widget.Toast.LENGTH_LONG).show()
            navigateTo(AppScreen.Subscription)
        }
    }

    BackHandler(enabled = currentScreen != AppScreen.Home && currentScreen != AppScreen.Splash) {
        goBack()
    }

    val activeEngineConfig = com.example.util.AnimationConfigManager.getActiveEngineConfig()
    val slowMo by com.example.util.AnimationConfigManager.sandboxSlowMoFactor

    androidx.compose.animation.AnimatedContent(
        targetState = currentScreen,
        contentKey = { it.routeKey },
        transitionSpec = {
            com.example.ui.components.PremiumAnimationEngine.buildScreenTransition(
                masterConfig = activeEngineConfig.masterConfig,
                isForward = isNavigatingForward,
                slowMoFactor = slowMo
            )
        },
        label = "AppScreenTransition"
    ) { screen ->
        saveableStateHolder.SaveableStateProvider(screen.routeKey) {
            when (screen) {
                is AppScreen.Splash -> {
                    AppSplashScreen(
                        onLoadingFinished = {
                            currentScreen = AppScreen.Home
                        }
                    )
                }

                is AppScreen.Home -> {
                    SyllabusProTheme {
                        HomeScreen(
                            onNavigateToSubject = { subject ->
                                navigateTo(AppScreen.SyllabusExplorer(subject.id, subject))
                            },
                            onNavigateToCurrentAffairs = {
                                navigateTo(AppScreen.CurrentAffairs)
                            },
                            onNavigateToPerformance = {
                                navigateTo(AppScreen.Performance)
                            },
                            onNavigateToSubscription = {
                                navigateTo(AppScreen.Subscription)
                            },
                            onNavigateToScientificCalculator = {},
                            onNavigateToPrivacy = {
                                navigateTo(AppScreen.ContactUs)
                            },
                            onNavigateToPrivacySection = { section ->
                                if (section == "contact_us") {
                                    navigateTo(AppScreen.ContactUs)
                                } else {
                                    navigateTo(AppScreen.LegalSupport(section))
                                }
                            },
                            onNavigateToMockTestGenerator = {
                                navigateTo(AppScreen.MockTestGenerator)
                            },
                            onNavigateToAvailableMockTests = {
                                navigateTo(AppScreen.AvailableMockTests)
                            },
                            onNavigateToMockTests = {
                                navigateTo(AppScreen.AvailableMockTests)
                            },
                            onNavigateToDailyQuiz = {
                                navigateTo(AppScreen.AvailableMockTests)
                            },
                            onNavigateToProfile = {
                                navigateTo(AppScreen.Profile)
                            },
                            onNavigateToLogin = {
                                navigateTo(AppScreen.Login)
                            },
                            onNavigateToSavedQuestions = {
                                navigateTo(AppScreen.SavedQuestions)
                            },
                            onNavigateToAdminDashboard = {
                                navigateTo(AppScreen.AdminDashboard)
                            },
                            onNavigateToTopic = { subject, unit, topic ->
                                navigateTo(
                                    AppScreen.TopicSets(
                                        subjectId = subject.id,
                                        unitId = unit.id,
                                        topicId = topic.id,
                                        fallbackSubject = subject,
                                        fallbackUnit = unit,
                                        fallbackTopic = topic
                                    )
                                )
                            },
                            onNavigateToUnit = { subject, unit ->
                                navigateTo(
                                    AppScreen.UnitTopics(
                                        subjectId = subject.id,
                                        unitId = unit.id,
                                        fallbackSubject = subject,
                                        fallbackUnit = unit
                                    )
                                )
                            },
                            onNavigateToLeaderboard = { navigateTo(AppScreen.Leaderboard) },
                            onNavigateToOfflineManager = {
                                navigateTo(AppScreen.DownloadedMaterials)
                            },
                            onNavigateToNotifications = {
                                navigateTo(AppScreen.Notifications)
                            },
                            onNavigateToPreviousYearPapers = {
                                navigateTo(AppScreen.PreviousYearPapers)
                            },
                            onNavigateToLuxuryFontStudio = {
                                navigateTo(AppScreen.LuxuryFontStudio)
                            }
                        )
                    }
                }

                is AppScreen.SyllabusExplorer -> {
                    SyllabusExplorerScreen(
                        subjectId = screen.subjectId,
                        fallbackSubject = screen.fallbackSubject,
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onSelectUnit = { subject, unit ->
                            navigateTo(
                                AppScreen.UnitTopics(
                                    subjectId = subject.id,
                                    unitId = unit.id,
                                    fallbackSubject = subject,
                                    fallbackUnit = unit
                                )
                            )
                        },
                        onNavigateToSubscription = {
                            navigateTo(AppScreen.Subscription)
                        }
                    )
                }

                is AppScreen.UnitTopics -> {
                    UnitTopicsScreen(
                        subjectId = screen.subjectId,
                        unitId = screen.unitId,
                        fallbackSubject = screen.fallbackSubject,
                        fallbackUnit = screen.fallbackUnit,
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onSelectTopic = { subject, unit, topic ->
                            navigateTo(
                                AppScreen.TopicSets(
                                    subjectId = subject.id,
                                    unitId = unit.id,
                                    topicId = topic.id,
                                    fallbackSubject = subject,
                                    fallbackUnit = unit,
                                    fallbackTopic = topic
                                )
                            )
                        },
                        onNavigateToSubscription = {
                            navigateTo(AppScreen.Subscription)
                        }
                    )
                }

                is AppScreen.TopicSets -> {
                    TopicSetsScreen(
                        subjectId = screen.subjectId,
                        unitId = screen.unitId,
                        topicId = screen.topicId,
                        fallbackSubject = screen.fallbackSubject,
                        fallbackUnit = screen.fallbackUnit,
                        fallbackTopic = screen.fallbackTopic,
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onSelectSet = { practiceSet, _ ->
                            navigateTo(
                                AppScreen.PracticeArena(
                                    subjectId = screen.subjectId,
                                    unitId = screen.unitId,
                                    topicId = screen.topicId,
                                    setName = practiceSet.title,
                                    setId = practiceSet.id,
                                    subtopicId = "",
                                    subtopicTitle = "",
                                    preloadedQuestions = practiceSet.questions,
                                    sectionType = PracticeSectionType.SETS
                                )
                            )
                        },
                        onSelectSubtopic = { subtopicPracticeSet, subtopicTitle ->
                            navigateTo(
                                AppScreen.PracticeArena(
                                    subjectId = screen.subjectId,
                                    unitId = screen.unitId,
                                    topicId = screen.topicId,
                                    setName = subtopicTitle,
                                    setId = "",
                                    subtopicId = subtopicPracticeSet.id,
                                    subtopicTitle = subtopicTitle,
                                    preloadedQuestions = subtopicPracticeSet.questions,
                                    sectionType = PracticeSectionType.SUBTOPICS
                                )
                            )
                        },
                        testResults = allTestResults,
                        onNavigateToSubscription = {
                            navigateTo(AppScreen.Subscription)
                        }
                    )
                }

                is AppScreen.SetSubtopics -> {
                    SetSubtopicsScreen(
                        subjectId = screen.subjectId,
                        unitId = screen.unitId,
                        topicId = screen.topicId,
                        setId = screen.setId,
                        setNumber = screen.setNumber,
                        setTitle = screen.setTitle,
                        fallbackSubject = screen.fallbackSubject,
                        fallbackUnit = screen.fallbackUnit,
                        fallbackTopic = screen.fallbackTopic,
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onSelectSubtopic = { subtopicPracticeSet, subtopicTitle ->
                            navigateTo(
                                AppScreen.PracticeArena(
                                    subjectId = screen.subjectId,
                                    unitId = screen.unitId,
                                    topicId = screen.topicId,
                                    setName = subtopicTitle,
                                    setId = screen.setId,
                                    subtopicId = subtopicPracticeSet.id,
                                    subtopicTitle = subtopicTitle,
                                    preloadedQuestions = subtopicPracticeSet.questions,
                                    sectionType = PracticeSectionType.SUBTOPICS
                                )
                            )
                        },
                        onNavigateToSubscription = {
                            navigateTo(AppScreen.Subscription)
                        }
                    )
                }

                is AppScreen.DownloadedMaterials -> {
                    DownloadedMaterialsScreen(
                        onBack = { goBack() },
                        onStartPractice = { subId, unitId, topicId, setName ->
                            checkOfflineAccessAndProceed {
                                navigateTo(
                                    AppScreen.PracticeArena(
                                        subjectId = subId,
                                        unitId = unitId,
                                        topicId = topicId,
                                        setName = setName.ifBlank { "Downloaded MCQ Set" },
                                        sectionType = com.example.model.PracticeSectionType.OFFLINE
                                    )
                                )
                            }
                        }
                    )
                }

                is AppScreen.LuxuryFontStudio -> {
                    LuxuryFontStudioScreen(onBack = { goBack() })
                }


                is AppScreen.PracticeArena -> {
                    val resolvedSectionType = screen.sectionType ?: when {
                        com.example.util.OfflineSyncManager.isOfflineModeSimulated.value -> PracticeSectionType.OFFLINE
                        screen.subjectId == "subj-current-affairs" -> PracticeSectionType.CURRENT_AFFAIRS
                        screen.subtopicId.isNotBlank() || screen.subtopicTitle.isNotBlank() -> PracticeSectionType.SUBTOPICS
                        else -> PracticeSectionType.SETS
                    }

                    if (resolvedSectionType == PracticeSectionType.OFFLINE) {
                        val profile = SampleData.userProfileState.value
                        val hasActiveOfflineSub = profile.isLoggedIn && (profile.isProSubscribed || profile.expiresAt > System.currentTimeMillis())
                        if (!hasActiveOfflineSub) {
                            LaunchedEffect(Unit) {
                                android.widget.Toast.makeText(context, "Active ₹99 Offline Subscription required to access downloaded MCQs offline.", android.widget.Toast.LENGTH_LONG).show()
                                navigateTo(AppScreen.Subscription)
                            }
                            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                androidx.compose.material3.CircularProgressIndicator()
                            }
                            return@SaveableStateProvider
                        }
                    }

                    val questions = remember(
                        screen.subjectId,
                        screen.unitId,
                        screen.topicId,
                        screen.setName,
                        screen.subtopicTitle,
                        screen.preloadedQuestions
                    ) {
                        when {
                            screen.preloadedQuestions.isNotEmpty() -> screen.preloadedQuestions
                            screen.subtopicTitle.isNotBlank() -> {
                                SampleData.getQuestionsForPractice(
                                    screen.subjectId,
                                    screen.unitId,
                                    screen.topicId,
                                    screen.subtopicTitle
                                ).ifEmpty {
                                    SampleData.getQuestionsForPractice(
                                        screen.subjectId,
                                        screen.unitId,
                                        screen.topicId,
                                        ""
                                    )
                                }
                            }
                            else -> {
                                SampleData.getQuestionsForPractice(
                                    screen.subjectId,
                                    screen.unitId,
                                    screen.topicId,
                                    screen.setName
                                ).ifEmpty {
                                    SampleData.getQuestionsForPractice(
                                        screen.subjectId,
                                        screen.unitId,
                                        screen.topicId,
                                        ""
                                    )
                                }
                            }
                        }.ifEmpty { SampleData.questionsState.take(10) }
                    }
                    val sessionKey = screen.subtopicId.ifBlank {
                        screen.setId.ifBlank {
                            screen.topicId.takeIf { screen.subjectId == "subj-current-affairs" } ?: "session-${screen.setName}"
                        }
                    }
                    PracticeArenaScreen(
                        practiceSessionId = sessionKey,
                        questions = questions,
                        title = screen.subtopicTitle.ifBlank { screen.setName },
                        sectionType = screen.sectionType ?: when {
                            com.example.util.OfflineSyncManager.isOfflineModeSimulated.value -> PracticeSectionType.OFFLINE
                            screen.subjectId == "subj-current-affairs" -> PracticeSectionType.CURRENT_AFFAIRS
                            screen.subtopicId.isNotBlank() || screen.subtopicTitle.isNotBlank() -> PracticeSectionType.SUBTOPICS
                            else -> PracticeSectionType.SETS
                        },
                        initialQuestionIndex = screen.initialQuestionIndex,
                        preserveQuestionOrder = screen.preserveQuestionOrder,
                        initialBookmarkedIds = bookmarkedQuestions.map { it.id }.toSet(),
                        onToggleBookmark = { q -> appRepository.toggleBookmark(q) },
                        onBack = { goBack() },
                        onSubmitTest = { correctCount, wrongCount, scoreVal, userAnsMap, elapsedSec ->
                            val practiceTitle = screen.subtopicTitle.ifBlank { screen.setName }
                            val totalQs = questions.size.coerceAtLeast(1)
                            val attemptedQs = (correctCount + wrongCount).coerceAtLeast(1)
                            val unanswered = (totalQs - (correctCount + wrongCount)).coerceAtLeast(0)
                            val pctFloat = ((correctCount.toFloat() / totalQs.toFloat()) * 100f).coerceIn(0f, 100f)
                            val accPct = if (correctCount + wrongCount > 0) ((correctCount * 100) / attemptedQs) else 0
                            val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
                            val newResult = com.example.model.TestResult(
                                id = UUID.randomUUID().toString(),
                                testName = practiceTitle,
                                totalQuestions = totalQs,
                                score = correctCount,
                                percentage = pctFloat.toInt(),
                                accuracy = "$accPct%",
                                date = dateFormatted
                            )
                            SampleData.testResultsState.add(0, newResult)
                            appRepository.saveTestResult(
                                testTitle = practiceTitle,
                                totalQuestions = totalQs,
                                correctCount = correctCount,
                                wrongCount = wrongCount,
                                unansweredCount = unanswered,
                                percentage = pctFloat,
                                accuracyStr = "$accPct%",
                                timeSeconds = elapsedSec,
                                userAnswers = userAnsMap,
                                testType = "PRACTICE_SET"
                            )
                            coroutineScope.launch {
                                try {
                                    val completedTopic = screen.topicId.takeIf { it.isNotBlank() && it != "saved" && it != "pyq" }
                                    if (completedTopic != null) {
                                        appRepository.userProgressRepository.completeTopic(activeAppId, completedTopic)
                                    }
                                    leaderboardRepository.processVerifiedTestResult(
                                        testId = sessionKey,
                                        testTitle = practiceTitle,
                                        totalQuestions = totalQs,
                                        correctAnswers = correctCount,
                                        percentage = pctFloat,
                                        timeSeconds = elapsedSec,
                                        subjectId = screen.subjectId
                                    )
                                } catch (_: Exception) {}
                            }
                            navigateTo(
                                AppScreen.Result(
                                    subjectId = screen.subjectId,
                                    unitId = screen.unitId,
                                    topicId = screen.topicId,
                                    setName = screen.setName,
                                    setId = screen.setId,
                                    subtopicId = screen.subtopicId,
                                    subtopicTitle = screen.subtopicTitle,
                                    questions = questions,
                                    userAnswers = userAnsMap,
                                    correctCount = correctCount,
                                    wrongCount = wrongCount,
                                    score = scoreVal,
                                    timeSpent = elapsedSec.toLong()
                                )
                            )
                        }
                    )
                }

                is AppScreen.Result -> {
                    val resultQuestions = screen.questions.ifEmpty { SampleData.questionsState.take(10) }

                    // Resolve next set or next subtopic navigation
                    val (hasNextItem, nextTitle, nextNavAction) = resolveNextNavigation(
                        screen = screen,
                        navigateToPractice = { nSub, nUnit, nTopic, nSetTitle, nSetId, nSubId, nSubTitle, nQs ->
                            navigateTo(
                                AppScreen.PracticeArena(
                                    subjectId = nSub,
                                    unitId = nUnit,
                                    topicId = nTopic,
                                    setName = nSetTitle,
                                    setId = nSetId,
                                    subtopicId = nSubId,
                                    subtopicTitle = nSubTitle,
                                    preloadedQuestions = nQs
                                )
                            )
                        }
                    )

                    ResultScreen(
                        questions = resultQuestions,
                        userAnswers = screen.userAnswers,
                        correctCount = screen.correctCount,
                        wrongCount = screen.wrongCount,
                        score = screen.score,
                        onPracticeAgain = {
                            navigateTo(
                                AppScreen.PracticeArena(
                                    subjectId = screen.subjectId,
                                    unitId = screen.unitId,
                                    topicId = screen.topicId,
                                    setName = screen.setName,
                                    setId = screen.setId,
                                    subtopicId = screen.subtopicId,
                                    subtopicTitle = screen.subtopicTitle,
                                    preloadedQuestions = resultQuestions
                                )
                            )
                        },
                        onBackToPrevious = { goBack() },
                        onBackToHome = { navigateToHome() },
                        onNextSet = nextNavAction,
                        nextItemTitle = nextTitle,
                        isSubtopicPractice = screen.subtopicTitle.isNotBlank(),
                        hasNextItem = hasNextItem
                    )
                }

                is AppScreen.PreviousYearPapers -> {
                    PreviousYearPapersScreen(
                        onStartPaperAttempt = { paperTitle, questions, paperId ->
                            navigateTo(
                                AppScreen.PracticeArena(
                                    subjectId = "pyq",
                                    unitId = "pyq",
                                    topicId = paperId,
                                    setName = paperTitle,
                                    setId = paperId,
                                    preloadedQuestions = questions
                                )
                            )
                        },
                        onBack = { goBack() }
                    )
                }

                is AppScreen.OfflineSubjectDetail -> {
                    OfflineSubjectDetailScreen(
                        subject = screen.subject,
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onStartPractice = { subId, unitId, topicId, setName ->
                            checkOfflineAccessAndProceed {
                                navigateTo(
                                    AppScreen.PracticeArena(
                                        subjectId = subId,
                                        unitId = unitId,
                                        topicId = topicId,
                                        setName = setName.ifBlank { "Offline Practice Set" },
                                        sectionType = PracticeSectionType.OFFLINE
                                    )
                                )
                            }
                        }
                    )
                }

                is AppScreen.Subscription -> {
                    SubscriptionScreen(
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() }
                    )
                }

                is AppScreen.Login -> {
                    LoginScreen(
                        onBack = { goBack() },
                        onLoginSuccess = { navigateToHome() }
                    )
                }

                is AppScreen.AdminDashboard -> {
                    AdminDashboardScreen(
                        onSwitchToUserPage = { navigateToHome() },
                        onLogout = { navigateToHome() },
                        onNavigateToSubscription = { navigateTo(AppScreen.Subscription) }
                    )
                }

                is AppScreen.Profile -> {
                    UserProfileScreen(
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onNavigateToSubscription = { navigateTo(AppScreen.Subscription) },
                        onNavigateToLogin = { navigateTo(AppScreen.Login) },
                        onNavigateToSavedQuestions = { navigateTo(AppScreen.SavedQuestions) },
                        onNavigateToContact = { navigateTo(AppScreen.ContactUs) },
                        onNavigateToPrivacy = { navigateTo(AppScreen.LegalSupport("privacy_policy")) },
                        onNavigateToPerformance = { navigateTo(AppScreen.Performance) }
                    )
                }

                is AppScreen.ContactUs -> {
                    ContactUsScreen(
                        onBack = { goBack() },
                        onNavigateToFaq = { navigateTo(AppScreen.LegalSupport("faq")) },
                        onNavigateToHelpCenter = { navigateTo(AppScreen.LegalSupport("help_center")) }
                    )
                }

                is AppScreen.LegalSupport -> {
                    LegalSupportCenterScreen(
                        initialSection = screen.initialSection,
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() }
                    )
                }

                is AppScreen.SavedQuestions -> {
                    SavedQuestionsScreen(
                        savedQuestions = bookmarkedQuestions,
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onRemoveBookmark = { qId -> appRepository.removeBookmark(qId) },
                        onPracticeSaved = { savedQs, practiceTitle, initialIndex ->
                            if (savedQs.isNotEmpty()) {
                                val targetIndex = initialIndex.coerceIn(0, savedQs.size - 1)
                                val targetQuestionId = savedQs.getOrNull(targetIndex)?.id ?: "0"
                                navigateTo(
                                    AppScreen.PracticeArena(
                                        subjectId = "saved",
                                        unitId = "saved",
                                        topicId = "saved",
                                        setName = practiceTitle,
                                        setId = "saved_${practiceTitle.hashCode()}_${savedQs.size}_$targetQuestionId",
                                        preloadedQuestions = savedQs,
                                        initialQuestionIndex = targetIndex,
                                        preserveQuestionOrder = true
                                    )
                                )
                            }
                        },
                        onNavigateToExplore = { navigateToHome() }
                    )
                }

                is AppScreen.Notifications -> {
                    NotificationsScreen(
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() }
                    )
                }

                is AppScreen.CurrentAffairs -> {
                    CurrentAffairsScreen(
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onNavigateToPractice = { subId, unitId, topicId, setName ->
                            navigateTo(
                                AppScreen.PracticeArena(
                                    subjectId = subId,
                                    unitId = unitId,
                                    topicId = topicId,
                                    setName = setName,
                                    setId = topicId,
                                    sectionType = PracticeSectionType.CURRENT_AFFAIRS
                                )
                            )
                        },
                        onNavigateToSubscription = { navigateTo(AppScreen.Subscription) }
                    )
                }

                is AppScreen.MockTestGenerator -> {
                    MockTestGeneratorScreen(
                        onNavigateBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onTestGenerated = { newTest ->
                            if (SampleData.generatedMockTestsState.none { it.id == newTest.id }) {
                                SampleData.generatedMockTestsState.add(0, newTest)
                            }
                            appRepository.saveMockTest(newTest)
                        },
                        pastTests = SampleData.generatedMockTestsState,
                        onStartGeneratedTest = { test ->
                            if (SampleData.generatedMockTestsState.none { it.id == test.id }) {
                                SampleData.generatedMockTestsState.add(0, test)
                            }
                            appRepository.saveMockTest(test)
                            navigateTo(
                                AppScreen.FullLengthMockTest(
                                    testId = test.id,
                                    testTitle = test.title,
                                    timeMinutes = test.durationMinutes
                                )
                            )
                        },
                        onNavigateToAvailableTests = {
                            navigateTo(AppScreen.AvailableMockTests)
                        },
                        onNavigateToSubscription = {
                            navigateTo(AppScreen.Subscription)
                        }
                    )
                }

                is AppScreen.AvailableMockTests -> {
                    AvailableMockTestsScreen(
                        onNavigateBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onNavigateToGenerator = {
                            navigateTo(AppScreen.MockTestGenerator)
                        },
                        onStartTest = { test ->
                            navigateTo(
                                AppScreen.FullLengthMockTest(
                                    testId = test.id,
                                    testTitle = test.title,
                                    timeMinutes = test.durationMinutes
                                )
                            )
                        },
                        onResumeTest = { test ->
                            navigateTo(
                                AppScreen.FullLengthMockTest(
                                    testId = test.id,
                                    testTitle = test.title,
                                    timeMinutes = test.durationMinutes
                                )
                            )
                        },
                        onReattemptTest = { test ->
                            val reattemptTest = test.copy(
                                status = "Not Started",
                                userAnswers = emptyMap(),
                                score = null,
                                correctAnswers = 0,
                                wrongAnswers = 0,
                                unanswered = test.questions.size
                            )
                            val index = SampleData.generatedMockTestsState.indexOfFirst { it.id == test.id }
                            if (index != -1) {
                                SampleData.generatedMockTestsState[index] = reattemptTest
                            }
                            appRepository.saveMockTest(reattemptTest)
                            navigateTo(
                                AppScreen.FullLengthMockTest(
                                    testId = reattemptTest.id,
                                    testTitle = reattemptTest.title,
                                    timeMinutes = reattemptTest.durationMinutes
                                )
                            )
                        },
                        onViewResult = { test ->
                            navigateTo(AppScreen.MockTestResult(test.id))
                        },
                        onNavigateToSubscription = {
                            navigateTo(AppScreen.Subscription)
                        }
                    )
                }

                is AppScreen.FullLengthMockTest -> {
                    val activeTest = SampleData.generatedMockTestsState.find { it.id == screen.testId }
                    if (activeTest != null) {
                        FullLengthMockTestScreen(
                            test = activeTest,
                            initialBookmarkedIds = bookmarkedQuestions.map { it.id }.toSet(),
                            onToggleBookmark = { q -> appRepository.toggleBookmark(q) },
                            onSubmitTest = { correct, wrong, score, userAnswers, timeTakenSeconds ->
                                val totalQs = activeTest.questions.size
                                val unanswered = (totalQs - (correct + wrong)).coerceAtLeast(0)
                                val totalMarks = if (activeTest.totalMarks > 0) activeTest.totalMarks else (totalQs * activeTest.positiveMarks).coerceAtLeast(1.0)
                                val attempted = correct + wrong
                                val percentage = if (totalMarks > 0) (score / totalMarks * 100.0).coerceIn(0.0, 100.0) else 0.0
                                val accuracy = if (attempted > 0) "${(correct.toDouble() / attempted * 100).toInt()}%" else "0%"
                                val newRecord = MockTestAttemptRecord(
                                    attemptId = UUID.randomUUID().toString(),
                                    attemptNumber = activeTest.attemptsHistory.size + 1,
                                    completedAt = System.currentTimeMillis(),
                                    score = score,
                                    totalMarks = totalMarks,
                                    correctAnswers = correct,
                                    wrongAnswers = wrong,
                                    unanswered = unanswered,
                                    timeTakenSeconds = timeTakenSeconds,
                                    percentage = percentage,
                                    accuracyStr = accuracy,
                                    userAnswers = userAnswers
                                )
                                val updatedHistory = activeTest.attemptsHistory + newRecord
                                val updatedTest = activeTest.copy(
                                    status = "Completed",
                                    score = score,
                                    correctAnswers = correct,
                                    wrongAnswers = wrong,
                                    unanswered = unanswered,
                                    userAnswers = userAnswers,
                                    timeTakenSeconds = timeTakenSeconds,
                                    attemptsHistory = updatedHistory
                                )
                                val idx = SampleData.generatedMockTestsState.indexOfFirst { it.id == activeTest.id }
                                if (idx != -1) {
                                    SampleData.generatedMockTestsState[idx] = updatedTest
                                }
                                appRepository.saveMockTest(updatedTest)
                                val attemptedQs = (correct + wrong).coerceAtLeast(1)
                                val accPct = if (correct + wrong > 0) ((correct * 100) / attemptedQs) else 0
                                val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(newRecord.completedAt))
                                val testResult = com.example.model.TestResult(
                                    id = newRecord.attemptId,
                                    testName = activeTest.title,
                                    totalQuestions = totalQs,
                                    score = correct,
                                    percentage = percentage.toInt(),
                                    accuracy = "$accPct%",
                                    date = dateFormatted
                                )
                                SampleData.testResultsState.add(0, testResult)
                                appRepository.saveTestResult(
                                    testTitle = activeTest.title,
                                    totalQuestions = totalQs,
                                    correctCount = correct,
                                    wrongCount = wrong,
                                    unansweredCount = unanswered,
                                    percentage = percentage.toFloat(),
                                    accuracyStr = "$accPct%",
                                    timeSeconds = timeTakenSeconds,
                                    userAnswers = userAnswers,
                                    testType = activeTest.testMode.name
                                )
                                coroutineScope.launch {
                                    try {
                                        leaderboardRepository.processVerifiedTestResult(
                                            testId = activeTest.id,
                                            testTitle = activeTest.title,
                                            totalQuestions = totalQs,
                                            correctAnswers = correct,
                                            percentage = percentage.toFloat(),
                                            timeSeconds = timeTakenSeconds
                                        )
                                    } catch (_: Exception) {}
                                }
                                navigateTo(AppScreen.MockTestResult(activeTest.id))
                            },
                            onPauseTest = { correct, wrong, unanswered, markedForReview, visited, userAnswers ->
                                val updatedTest = activeTest.copy(
                                    status = if (userAnswers.isNotEmpty()) "In Progress" else activeTest.status,
                                    userAnswers = userAnswers
                                )
                                val idx = SampleData.generatedMockTestsState.indexOfFirst { it.id == activeTest.id }
                                if (idx != -1) {
                                    SampleData.generatedMockTestsState[idx] = updatedTest
                                }
                                appRepository.saveMockTest(updatedTest)
                                goBack()
                            },
                            onNavigateToSubscription = {
                                navigateTo(AppScreen.Subscription)
                            }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            goBack()
                        }
                    }
                }

                is AppScreen.MockTestResult -> {
                    val resultTest = SampleData.generatedMockTestsState.find { it.id == screen.testId }
                    if (resultTest != null) {
                        MockTestResultScreen(
                            test = resultTest,
                            onBack = { goBack() },
                            onNavigateToHome = { navigateToHome() }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            goBack()
                        }
                    }
                }

                is AppScreen.MockTests -> {
                    AvailableMockTestsScreen(
                        onNavigateBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onNavigateToGenerator = {
                            navigateTo(AppScreen.MockTestGenerator)
                        },
                        onStartTest = { test ->
                            navigateTo(
                                AppScreen.FullLengthMockTest(
                                    testId = test.id,
                                    testTitle = test.title,
                                    timeMinutes = test.durationMinutes
                                )
                            )
                        },
                        onResumeTest = { test ->
                            navigateTo(
                                AppScreen.FullLengthMockTest(
                                    testId = test.id,
                                    testTitle = test.title,
                                    timeMinutes = test.durationMinutes
                                )
                            )
                        },
                        onReattemptTest = { test ->
                            navigateTo(
                                AppScreen.FullLengthMockTest(
                                    testId = test.id,
                                    testTitle = test.title,
                                    timeMinutes = test.durationMinutes
                                )
                            )
                        },
                        onViewResult = { test ->
                            navigateTo(AppScreen.MockTestResult(test.id))
                        },
                        onNavigateToSubscription = {
                            navigateTo(AppScreen.Subscription)
                        }
                    )
                }

                is AppScreen.Performance -> {
                    PerformanceBoardScreen(
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() },
                        onNavigateToSubject = { subject ->
                            navigateTo(AppScreen.SyllabusExplorer(subject))
                        },
                        onNavigateToTopic = { subject, unit, topic ->
                            navigateTo(AppScreen.TopicSets(subject, unit, topic))
                        },
                        onNavigateToMockTests = {
                            navigateTo(AppScreen.MockTests)
                        }
                    )
                }

                is AppScreen.Leaderboard -> {
                    LeaderboardScreen(
                        onBack = { goBack() },
                        onNavigateToHome = { navigateToHome() }
                    )
                }
            }
        }
    }
}

private fun resolveNextNavigation(
    screen: AppScreen.Result,
    navigateToPractice: (String, String, String, String, String, String, String, List<Question>) -> Unit
): Triple<Boolean, String?, (() -> Unit)?> {
    val activeAppId = SampleData.currentActiveAppId.value

    // 1. If subtopic practice session, check if there is a next subtopic
    if (screen.subtopicTitle.isNotBlank()) {
        val subtopics = SampleData.getSubtopics(activeAppId, screen.topicId)
        val currentIdx = subtopics.indexOfFirst { it.equals(screen.subtopicTitle, ignoreCase = true) }
        if (currentIdx != -1 && currentIdx < subtopics.size - 1) {
            val nextSubTitle = subtopics[currentIdx + 1]
            val nextQs = SampleData.getQuestionsForPractice(screen.subjectId, screen.unitId, screen.topicId, nextSubTitle)
            val action = {
                navigateToPractice(
                    screen.subjectId,
                    screen.unitId,
                    screen.topicId,
                    nextSubTitle,
                    screen.setId,
                    "subtopic-$nextSubTitle",
                    nextSubTitle,
                    nextQs
                )
            }
            return Triple(true, nextSubTitle, action)
        }
    }

    // 2. Resolve sets for hierarchy level
    val sets = SampleData.getSetsForHierarchyLevel(screen.subjectId, screen.unitId, screen.topicId)
    val currentSetIdx = sets.indexOfFirst { it.title.equals(screen.setName, ignoreCase = true) || it.id == screen.setId }
    if (currentSetIdx != -1 && currentSetIdx < sets.size - 1) {
        val nextSet = sets[currentSetIdx + 1]
        val action = {
            navigateToPractice(
                screen.subjectId,
                screen.unitId,
                screen.topicId,
                nextSet.title,
                nextSet.id,
                "",
                "",
                nextSet.questions
            )
        }
        return Triple(true, nextSet.title, action)
    }

    // 3. Fallback: If sets exist and size > 1
    if (sets.size > 1) {
        val nextSet = sets[1]
        val action = {
            navigateToPractice(
                screen.subjectId,
                screen.unitId,
                screen.topicId,
                nextSet.title,
                nextSet.id,
                "",
                "",
                nextSet.questions
            )
        }
        return Triple(true, nextSet.title, action)
    }

    return Triple(false, null, null)
}
