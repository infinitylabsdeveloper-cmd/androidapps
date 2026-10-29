package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.widget.Toast
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.repository.UserProgressRepository
import com.example.viewmodel.PracticeStateViewModel
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.composed
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.ExactTopBarView
import coil.compose.rememberAsyncImagePainter
import com.example.data.SampleData
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.screens.AdminLoginDialog
import com.example.ui.theme.*
import com.example.util.AdminSessionManager
import com.example.util.AppShareManager
import com.example.util.FirebaseSyncManager
import com.example.util.PreviousYearPaperStore
import com.example.util.ShareChannel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSubject: (Subject) -> Unit,
    onNavigateToCurrentAffairs: () -> Unit,
    onNavigateToPerformance: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToScientificCalculator: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToPrivacySection: (String) -> Unit = { onNavigateToPrivacy() },
    onNavigateToMockTests: () -> Unit,
    onNavigateToDailyQuiz: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSavedQuestions: () -> Unit = {},
    onNavigateToAdminDashboard: () -> Unit = {},
    onNavigateToTopic: ((Subject, UnitModel, TopicModel) -> Unit)? = null,
    onNavigateToUnit: ((Subject, UnitModel) -> Unit)? = null,
    onNavigateToMockTestGenerator: () -> Unit = onNavigateToMockTests,
    onNavigateToAvailableMockTests: () -> Unit = onNavigateToMockTests,
    onNavigateToFontSettings: () -> Unit = {},
    onNavigateToLeaderboard: () -> Unit = {},
    onNavigateToSpeedDuel: () -> Unit = {},
    onNavigateToOfflineManager: () -> Unit = {},
    onNavigateToAiMentor: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAppSelection: () -> Unit = {},
    onNavigateToPreviousYearPapers: () -> Unit = {},
    onNavigateToLuxuryFontStudio: () -> Unit = {}
) {
    val config = LocalAppConfig.current
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val progressRepo = remember { UserProgressRepository.getInstance(context) }

    LaunchedEffect(Unit) {
        progressRepo.milestoneEvents.collect { event ->
            if (event == "STREAK_7_DAYS") {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "🎉 Congratulations! You've hit a 7-day study streak!",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    var adminTapCount by remember { mutableIntStateOf(0) }
    var lastTapTimestamp by remember { mutableLongStateOf(0L) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }

    val userProfile by SampleData.userProfileState
    val isRoleAdmin = userProfile.role == com.example.model.UserRole.ADMIN
    var isAdminLoggedIn by remember(isRoleAdmin) { 
        mutableStateOf(AdminSessionManager.isAdminLoggedIn(context) && isRoleAdmin) 
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isAdminLoggedIn = AdminSessionManager.isAdminLoggedIn(context) && userProfile.role == com.example.model.UserRole.ADMIN
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        isAdminLoggedIn = AdminSessionManager.isAdminLoggedIn(context) && userProfile.role == com.example.model.UserRole.ADMIN
        PracticeStateViewModel.getInstance().resetPracticeContext()
    }

    val handleAppscIconTap: () -> Unit = {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTimestamp > 2500L) {
            adminTapCount = 1
        } else {
            adminTapCount++
        }
        lastTapTimestamp = currentTime

        if (adminTapCount >= 5) {
            adminTapCount = 0
            if (AdminSessionManager.isAdminLoggedIn(context)) {
                Toast.makeText(context, "👑 Welcome Admin (${AdminSessionManager.ADMIN_NAME})! Opening Dashboard...", Toast.LENGTH_SHORT).show()
                onNavigateToAdminDashboard()
            } else {
                showAdminLoginDialog = true
            }
        } else if (adminTapCount in 2..4) {
            val remaining = 5 - adminTapCount
            val tapMsg = if (remaining == 1) "Tap 1 more time for Admin Dashboard Login" else "Tap $remaining more times for Admin Dashboard Login"
            Toast.makeText(context, tapMsg, Toast.LENGTH_SHORT).show()
        }
    }

    var upgradePromptFeature by remember { mutableStateOf<String?>(null) }

    if (showAdminLoginDialog) {
        AdminLoginDialog(
            onDismiss = { showAdminLoginDialog = false },
            onLoginSuccess = {
                showAdminLoginDialog = false
                isAdminLoggedIn = true
                onNavigateToAdminDashboard()
            }
        )
    }

    val currentAppId by SampleData.currentActiveAppId
    val activeApp = SampleData.appsListState.find { it.id == currentAppId } ?: SampleData.getActiveApp()
    
    val activePosters = remember(currentAppId, SampleData.postersState.toList()) {
        val appPosters = SampleData.getPostersForApp(currentAppId)
        val active = appPosters.filter { it.isActive && it.getStatus() == PromotionStatus.ACTIVE }
        if (active.isNotEmpty()) {
            active.sortedBy { it.priorityOrder }
        } else {
            appPosters.filter { it.isActive }.sortedBy { it.priorityOrder }
        }
    }

    val displaySubjects = remember(currentAppId, SampleData.subjectsState.toList()) {
        SampleData.getSubjectsForApp(currentAppId).filter { it.id != "subj-current-affairs" && !it.id.contains("current-affairs") }
    }

    var selectedCategoryFilter by rememberSaveable { mutableStateOf("ALL") }

    // Fallback: If currently selected category corresponds to a card that got disabled, reset safely to "ALL"
    LaunchedEffect(config.isPrelimsCardEnabled, config.isMainsCardEnabled, config.isAllCardEnabled) {
        if (selectedCategoryFilter == "PRELIMS" && !config.isPrelimsCardEnabled) {
            selectedCategoryFilter = "ALL"
        } else if (selectedCategoryFilter == "MAINS" && !config.isMainsCardEnabled) {
            selectedCategoryFilter = "ALL"
        }
    }

    val filteredDisplaySubjects = remember(displaySubjects, selectedCategoryFilter) {
        displaySubjects.filter { it.matchesCategoryFilter(selectedCategoryFilter) }
    }

    // Dynamic Firestore Category Content Counts (Prelims, Mains, General, All)
    val defaultPrelims = remember(displaySubjects) { displaySubjects.count { it.matchesCategoryFilter("PRELIMS") } }
    val defaultMains = remember(displaySubjects) { displaySubjects.count { it.matchesCategoryFilter("MAINS") } }
    val defaultGeneral = remember(displaySubjects) { displaySubjects.count { it.matchesCategoryFilter("GENERAL") } }
    val defaultAll = remember(displaySubjects) { displaySubjects.size }

    var dynamicCategoryCounts by remember(currentAppId) {
        mutableStateOf(
            CategoryContentCounts(
                prelimsCount = defaultPrelims,
                mainsCount = defaultMains,
                generalCount = defaultGeneral,
                allCount = defaultAll,
                isFromFirestore = false
            )
        )
    }

    // Dynamic real-time observation of content counts from Firestore database
    LaunchedEffect(currentAppId) {
        try {
            FirebaseSyncManager.observeCategoryContentCounts(currentAppId).collect { counts ->
                dynamicCategoryCounts = counts
            }
        } catch (e: Exception) {
            android.util.Log.w("HomeScreen", "Error observing Firestore category counts: ${e.message}")
        }
    }

    // Auto-ensure baseline counts are present in Firestore database
    LaunchedEffect(currentAppId, defaultAll) {
        try {
            FirebaseSyncManager.ensureCategoryCountsInitialized(
                appId = currentAppId,
                defaultCounts = CategoryContentCounts(
                    prelimsCount = defaultPrelims,
                    mainsCount = defaultMains,
                    generalCount = defaultGeneral,
                    allCount = defaultAll
                )
            )
            FirebaseSyncManager.startMasterAdminConfigSync(currentAppId, this)
        } catch (e: Exception) {
            android.util.Log.e("HomeScreen", "Error during Firebase sync startup: ${e.message}")
        }
    }

    var textScaleFactor by remember { mutableFloatStateOf(1.0f) }
    var showShareBottomSheet by remember { mutableStateOf(false) }
    var showDailyPracticeReminderDialog by remember { mutableStateOf(false) }
    var showReportIssueDialog by remember { mutableStateOf(false) }

    BackHandler(
        enabled = drawerState.isOpen || showAdminLoginDialog || showShareBottomSheet || showDailyPracticeReminderDialog || showReportIssueDialog || upgradePromptFeature != null
    ) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            showAdminLoginDialog = false
            showShareBottomSheet = false
            showDailyPracticeReminderDialog = false
            showReportIssueDialog = false
            upgradePromptFeature = null
        }
    }




    val isDarkMode = isSystemInDarkTheme()
    val drawerBg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFFAFCFF)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            LeftNavigationDrawerContent(
                drawerConfig = SampleData.getDrawerConfigForApp(currentAppId),
                userProfile = userProfile,
                isAdminLoggedIn = isAdminLoggedIn,
                isDarkMode = false,
                onCloseDrawer = { scope.launch { drawerState.close() } },
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToHome = { scope.launch { drawerState.close() } },
                onNavigateToMockTestGenerator = onNavigateToMockTestGenerator,
                onNavigateToAvailableMockTests = onNavigateToAvailableMockTests,
                onNavigateToCurrentAffairs = onNavigateToCurrentAffairs,
                onNavigateToPerformance = onNavigateToPerformance,
                onNavigateToLeaderboard = onNavigateToLeaderboard,
                onNavigateToSpeedDuel = onNavigateToSpeedDuel,
                onNavigateToOfflineManager = onNavigateToOfflineManager,
                onNavigateToSubscription = onNavigateToSubscription,
                onNavigateToScientificCalculator = onNavigateToScientificCalculator,
                onNavigateToPrivacySection = onNavigateToPrivacySection,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToAdminPanel = onNavigateToAdminDashboard,
                onShareApp = { showShareBottomSheet = true },
                onOpenDailyReminder = { showDailyPracticeReminderDialog = true },
                onNavigateToAiMentor = onNavigateToAiMentor,
                onNavigateToSavedQuestions = onNavigateToSavedQuestions,
                onNavigateToAppSelection = onNavigateToAppSelection,
                onReportIssue = { showReportIssueDialog = true },
                onNavigateToPreviousYearPapers = onNavigateToPreviousYearPapers
            )
        }
    ) {
        AppLuminousBackground(screenName = "Home Screen") {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    val topBarConfig = SampleData.getTopBarConfigForApp(currentAppId)

                    Column(modifier = Modifier.statusBarsPadding()) {
                        PremiumTopBar(
                            tenantConfig = activeApp,
                            config = topBarConfig,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onAppIconClick = { handleAppscIconTap() },
                            onLuxuryFontStudioClick = { onNavigateToLuxuryFontStudio() },
                            onPremiumClick = { onNavigateToSubscription() },
                            onNotificationsClick = onNavigateToNotifications,
                            unreadNotifications = SampleData.getUnreadNotificationCount()
                        )
                    }
                },
                containerColor = Color(0xFFFFF0D9)
            ) { innerPadding ->
                val homeScrollState = rememberScrollState()

                // Always ensure the Home Screen appears at the very top first when opened
                LaunchedEffect(Unit) {
                    homeScrollState.scrollTo(0)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .verticalScroll(homeScrollState),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Admin Switch banner (When logged in as Admin)
                        if (isAdminLoggedIn) {
                            Box(
                                modifier = Modifier.homeSmoothReveal(
                                    index = 0,
                                    scrollState = homeScrollState
                                )
                            ) {
                                Surface(
                                    color = Color(0xFF0F172A),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToAdminDashboard() }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                Icons.Default.AdminPanelSettings,
                                                contentDescription = null,
                                                tint = Color(0xFFFBBF24),
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Admin Mode Active (${AdminSessionManager.ADMIN_NAME})",
                                                    color = Color.White,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Tap to switch to Admin Dashboard",
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 10.5.sp
                                                )
                                            }
                                        }
                                        Surface(
                                            color = Color(0xFF2563EB),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "Dashboard ➔",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }



                        // 1. Promotional & Festival Posters Single Auto-sliding Banner (Top of Home Screen with Light Parallax)
                        if (activePosters.isNotEmpty()) {
                            Box(
                                modifier = Modifier.homeSmoothReveal(
                                    index = 1,
                                    parallaxFactor = 0.035f,
                                    scrollState = homeScrollState
                                )
                            ) {
                                val pagerState = rememberPagerState(pageCount = { activePosters.size })

                                // Auto-slide every 5 seconds with smooth cross-fade animation
                                LaunchedEffect(pagerState, activePosters.size) {
                                    while (true) {
                                        delay(5000L)
                                        if (activePosters.isNotEmpty()) {
                                            val nextPage = (pagerState.currentPage + 1) % activePosters.size
                                            pagerState.animateScrollToPage(
                                                page = nextPage,
                                                animationSpec = tween(
                                                    durationMillis = 800,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Single Poster View with Smooth Cross-Fade Animation
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(168.dp)
                                    ) { page ->
                                        val poster = activePosters[page]
                                        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                        val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)

                                        PromotionalPosterCard(
                                            poster = poster,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight()
                                                .graphicsLayer {
                                                    // Smooth cross-fade alpha
                                                    alpha = 1f - absOffset
                                                    // Keep banner stationary so it cross-fades in-place instead of horizontal slide
                                                    translationX = pageOffset * size.width
                                                    // Subtle micro-scale for added depth during fade
                                                    val scale = 1f - (absOffset * 0.04f)
                                                    scaleX = scale
                                                    scaleY = scale
                                                },
                                            onPosterClick = { destination ->
                                                handlePosterRedirection(
                                                    destination = destination,
                                                    posterTitle = poster.title,
                                                    offerDetails = poster.offerDetails,
                                                    context = context,
                                                    onNavigateToSubject = onNavigateToSubject,
                                                    onNavigateToSubscription = onNavigateToSubscription,
                                                    onNavigateToMockTests = onNavigateToMockTests,
                                                    onNavigateToDailyQuiz = onNavigateToDailyQuiz,
                                                    onNavigateToCurrentAffairs = onNavigateToCurrentAffairs,
                                                    onNavigateToPerformance = onNavigateToPerformance,
                                                    onNavigateToProfile = onNavigateToProfile
                                                )
                                            }
                                        )
                                    }

                                    // Auto-slide Indicator Dots
                                    if (activePosters.size > 1) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            activePosters.forEachIndexed { index, _ ->
                                                val isCurrent = pagerState.currentPage == index
                                                Box(
                                                    modifier = Modifier
                                                        .padding(horizontal = 3.dp)
                                                        .height(5.dp)
                                                        .width(if (isCurrent) 22.dp else 6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(
                                                            if (isCurrent) Color(0xFFFF7A00)
                                                            else Color(0xFFCBD5E1)
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Small Lively Content Dashboard (Live Totals for Subjects, Units, Topics, Sets, MCQs)
                        HomeLiveContentDashboard(
                            displaySubjects = displaySubjects,
                            currentAppId = currentAppId,
                            onNavigateToSyllabus = {
                                displaySubjects.firstOrNull()?.let { onNavigateToSubject(it) }
                            },
                            onNavigateToSubject = onNavigateToSubject,
                            onNavigateToUnit = onNavigateToUnit,
                            onNavigateToTopic = onNavigateToTopic,
                            onNavigateToCurrentAffairs = onNavigateToCurrentAffairs,
                            onNavigateToMockTests = onNavigateToMockTests,
                            onRefreshCategoryCounts = { dynamicCategoryCounts = it },
                            onNavigateToTopicSets = {
                                val firstSubject = displaySubjects.firstOrNull()
                                val firstUnit = firstSubject?.units?.firstOrNull()
                                val firstTopic = firstUnit?.topics?.firstOrNull()
                                if (firstSubject != null && firstUnit != null && firstTopic != null) {
                                    onNavigateToTopic?.invoke(firstSubject, firstUnit, firstTopic) ?: onNavigateToSubject(firstSubject)
                                } else if (firstSubject != null) {
                                    onNavigateToSubject(firstSubject)
                                }
                            },
                            onNavigateToPractice = {
                                Toast.makeText(context, "Practice Arena feature coming soon!", Toast.LENGTH_SHORT).show()
                            },
                            onNavigateToLiveBank = {
                                Toast.makeText(context, "Live Bank is active and synchronized.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp, bottom = 1.dp)
                                .homeSmoothReveal(
                                    index = 2,
                                    scrollState = homeScrollState
                                )
                        )

                        // Refined, thin divider line between Live Content Dashboard and Prelims, Mains, General cards
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .homeSmoothReveal(
                                    index = 3,
                                    scrollState = homeScrollState
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 680.dp)
                                    .padding(horizontal = 20.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Single, solid, luxurious premium rose-gold colour line with no gradients or decorative effects
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.5.dp)
                                        .background(
                                            color = Color(0xFFC88A96),
                                            shape = RoundedCornerShape(1.dp)
                                        )
                                )
                            }
                        }

                        // 3. PRELIMS, MAINS, and GENERAL TEST category cards - placed very close to Exam Syllabus & Papers
                        HomeSubjectCategoryFilterBar(
                            selectedCategory = selectedCategoryFilter,
                            onSelectCategory = { newCategory ->
                                if (selectedCategoryFilter != newCategory) {
                                    selectedCategoryFilter = newCategory
                                }
                            },
                            allCount = dynamicCategoryCounts.allCount,
                            prelimsCount = dynamicCategoryCounts.prelimsCount,
                            mainsCount = dynamicCategoryCounts.mainsCount,
                            generalCount = dynamicCategoryCounts.generalCount,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 1.dp, bottom = 1.dp)
                                .homeSmoothReveal(
                                    index = 4,
                                    scrollState = homeScrollState
                                )
                        )

                        // 4. Premium "Exam Syllabus & Papers" Card matching reference image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .homeSmoothReveal(
                                    index = 5,
                                    scrollState = homeScrollState
                                ),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            ExamSyllabusAndPapersCard(
                                modifier = Modifier.wrapContentSize()
                            )
                        }


                        // 5. Dynamic Filtered Subject Cards List
                        if (filteredDisplaySubjects.isEmpty()) {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .homeSmoothReveal(index = 5, scrollState = homeScrollState),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (displaySubjects.isEmpty()) "No subjects available." else "No $selectedCategoryFilter subjects assigned.",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = if (displaySubjects.isEmpty()) {
                                            "Configure subjects in Admin Panel to populate this section."
                                        } else {
                                            "Assign this category in Admin Panel or select another filter tab."
                                        },
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 4.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    if (displaySubjects.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedButton(
                                            onClick = { selectedCategoryFilter = "ALL" },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFF2563EB))
                                        ) {
                                            Text("Show All Subjects", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        } else {
                            filteredDisplaySubjects.forEachIndexed { subIndex, subject ->
                                key(subject.id) {
                                val questionCount = remember(subject.id, SampleData.questionsState.size) {
                                    SampleData.getQuestionsCountForSubject(subject.id)
                                }
                                val isZeroMcq = remember(questionCount) {
                                    questionCount == 0
                                }
                                val unitCount = remember(subject.id) { subject.units.size }
                                val topicCount = remember(subject.id) { subject.units.sumOf { it.topics.size } }
                                val richGradient = remember(subject.id, subject.name) {
                                    com.example.ui.theme.PremiumCardColorSystem.getGradientForSubject(subject.name, subject.id)
                                }
                                val openSubjectFullScreen = {
                                    if (isZeroMcq) {
                                        Toast.makeText(
                                            context,
                                            "⚠️ ${subject.name} question bank is updating soon! Exploring syllabus outline...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    onNavigateToSubject(subject)
                                }

                                HomeCardContainer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("subject_card_${subject.id}")
                                        .homeSmoothReveal(
                                            index = 5 + subIndex,
                                            scrollState = homeScrollState,
                                            target = com.example.model.AnimatableTarget.SUBJECT_CARDS
                                        ),
                                    accentBorderColor = richGradient.borderColor
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                        border = null,
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(210.dp)
                                                .clickable { openSubjectFullScreen() }
                                        ) {
                                            // Full-Width Scenic Background Image clearly visible with minimal scrim
                                            SubjectFhdCardImage(
                                                subject = subject,
                                                modifier = Modifier.fillMaxSize()
                                            )

                                            // Very light bottom gradient scrim for readability while keeping image fully clear
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(
                                                                Color.Transparent,
                                                                Color(0x750F172A)
                                                            ),
                                                            startY = 80f
                                                        )
                                                    )
                                            )

                                            Column(
                                                 modifier = Modifier
                                                     .fillMaxSize()
                                                     .padding(horizontal = 14.dp, vertical = 14.dp),
                                                 verticalArrangement = Arrangement.Bottom
                                             ) {
                                                 // Bottom: Subject Name & Counts stacked cleanly at the bottom
                                                 Column(
                                                     modifier = Modifier.fillMaxWidth(),
                                                     verticalArrangement = Arrangement.spacedBy(8.dp)
                                                 ) {
                                                     Text(
                                                         text = subject.name,
                                                         fontFamily = CinzelFontFamily,
                                                         fontWeight = FontWeight.Bold,
                                                         color = Color.White,
                                                         fontSize = 18.sp,
                                                         lineHeight = 22.sp,
                                                         maxLines = 2,
                                                         overflow = TextOverflow.Ellipsis
                                                     )

                                                     // Number of Units, Topics, Sets, MCQs
                                                     val setCount = remember(subject.id) {
                                                         subject.units.sumOf { unit ->
                                                             unit.topics.sumOf { topic ->
                                                                 val setsForTopic = SampleData.getPracticeSetsForTopic(subject.id, unit, topic)
                                                                 if (setsForTopic.isNotEmpty()) setsForTopic.size else maxOf(topic.practiceSetsCount, 2)
                                                             }
                                                         }
                                                     }
                                                     Row(
                                                         modifier = Modifier.fillMaxWidth(),
                                                         horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                         verticalAlignment = Alignment.CenterVertically
                                                     ) {
                                                         // 1. Units
                                                         Surface(
                                                             shape = RoundedCornerShape(50),
                                                             color = Color(0xFFFFFBEB).copy(alpha = 0.95f),
                                                             border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                                             modifier = Modifier.height(26.dp)
                                                         ) {
                                                             Row(
                                                                 modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                                 verticalAlignment = Alignment.CenterVertically
                                                             ) {
                                                                 Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(11.dp))
                                                                 Spacer(modifier = Modifier.width(3.dp))
                                                                 Text(text = "$unitCount Units", color = Color(0xFF78350F), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                                             }
                                                         }

                                                         // 2. Topics
                                                         Surface(
                                                             shape = RoundedCornerShape(50),
                                                             color = Color(0xFFE0F2FE).copy(alpha = 0.95f),
                                                             border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                                             modifier = Modifier.height(26.dp)
                                                         ) {
                                                             Row(
                                                                 modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                                 verticalAlignment = Alignment.CenterVertically
                                                             ) {
                                                                 Icon(Icons.Default.Layers, contentDescription = null, tint = Color(0xFF0369A1), modifier = Modifier.size(11.dp))
                                                                 Spacer(modifier = Modifier.width(3.dp))
                                                                 Text(text = "$topicCount Topics", color = Color(0xFF0C4A6E), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                                             }
                                                         }

                                                         // 3. Sets
                                                         Surface(
                                                             shape = RoundedCornerShape(50),
                                                             color = Color(0xFFE8F5E9).copy(alpha = 0.95f),
                                                             border = BorderStroke(1.dp, Color(0xFF34D399)),
                                                             modifier = Modifier.height(26.dp)
                                                         ) {
                                                             Row(
                                                                 modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                                 verticalAlignment = Alignment.CenterVertically
                                                             ) {
                                                                 Icon(Icons.Default.Checklist, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(11.dp))
                                                                 Spacer(modifier = Modifier.width(3.dp))
                                                                 Text(text = "$setCount Sets", color = Color(0xFF064E3B), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                                             }
                                                         }

                                                         // 4. MCQs
                                                         Surface(
                                                             shape = RoundedCornerShape(50),
                                                             color = Color(0xFFFFFDE7).copy(alpha = 0.95f),
                                                             border = BorderStroke(1.dp, Color(0xFFFFB300)),
                                                             modifier = Modifier.height(26.dp)
                                                         ) {
                                                             Row(
                                                                 modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                                 verticalAlignment = Alignment.CenterVertically
                                                             ) {
                                                                 Icon(Icons.Default.FactCheck, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(11.dp))
                                                                 Spacer(modifier = Modifier.width(3.dp))
                                                                 Text(text = "$questionCount MCQs", color = Color(0xFF78350F), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                                             }
                                                         }
                                                     }
                                                 }
                                             }
                                        }
                                    }
                                }
                                }
                            }
                        }
                    }





                    // Floating "Scroll to Top" Quick Glider Action
                    AnimatedVisibility(
                        visible = homeScrollState.value > 500,
                        enter = fadeIn(animationSpec = tween(220)) + slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(250, easing = FastOutSlowInEasing)
                        ),
                        exit = fadeOut(animationSpec = tween(180)) + slideOutVertically(
                            targetOffsetY = { it / 2 },
                            animationSpec = tween(200)
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 18.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.94f),
                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.65f)),
                            shadowElevation = 8.dp,
                            modifier = Modifier.clickable {
                                scope.launch {
                                    homeScrollState.animateScrollTo(
                                        value = 0,
                                        animationSpec = tween(
                                            durationMillis = 450,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Scroll to Top",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Top",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    upgradePromptFeature?.let { feature ->
        PremiumUpgradePromptDialog(
            title = "Unlock Premium Content",
            featureName = feature,
            description = "Free candidates can access only the first Unit and first Topic Set. Upgrade to Premium for unrestricted access to all syllabus units, topic MCQs, and mock tests.",
            onDismiss = { upgradePromptFeature = null },
            onUpgradeClick = {
                upgradePromptFeature = null
                onNavigateToSubscription()
            }
        )
    }

    if (showShareBottomSheet) {
        ShareAppBottomSheet(
            appName = activeApp.name,
            onDismissRequest = { showShareBottomSheet = false }
        )
    }

    if (showDailyPracticeReminderDialog) {
        DailyPracticeReminderDialog(
            onDismissRequest = { showDailyPracticeReminderDialog = false }
        )
    }

    if (showReportIssueDialog) {
        ReportIssueDialog(
            onDismiss = { showReportIssueDialog = false }
        )
    }
}

@Composable
fun HeroPracticeArenaHeaderCard(
    onNavigateToDailyQuiz: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF8FAFC),
                            Color(0xFFF1F5F9)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFF5722)
                    ) {
                        Text(
                            text = "SUBSCRIPTIONS",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.3.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Text(
                        text = SampleData.getActiveApp().name,
                        color = Color(0xFF1E293B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Middle Row: Text on Left + 3D Graduation Cap & Books Art on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp)
                    ) {
                        Text(
                            text = "Syllabus Wise MCQ Practice &\nExam Arena",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.5.sp,
                            color = Color(0xFF0F172A),
                            lineHeight = 23.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "4,500+ Live MCQs ready for practice across all subjects",
                            color = Color(0xFF475569),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    GraduationCapBooksArt(
                        modifier = Modifier
                            .size(width = 112.dp, height = 96.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEDE9FE),
                    border = BorderStroke(1.5.dp, Color(0xFF8B5CF6)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { onNavigateToSubscription() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Subscriptions",
                            color = Color(0xFF6D28D9),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GraduationCapBooksArt(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Book 1 (Bottom book - Navy Blue)
            val pathBook1 = Path().apply {
                moveTo(w * 0.12f, h * 0.72f)
                lineTo(w * 0.82f, h * 0.65f)
                lineTo(w * 0.85f, h * 0.82f)
                lineTo(w * 0.15f, h * 0.90f)
                close()
            }
            drawPath(
                path = pathBook1,
                brush = Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF1D4ED8)))
            )
            // Book 1 Pages (White/Cream side)
            val pathBook1Pages = Path().apply {
                moveTo(w * 0.82f, h * 0.65f)
                lineTo(w * 0.92f, h * 0.70f)
                lineTo(w * 0.85f, h * 0.82f)
                close()
            }
            drawPath(path = pathBook1Pages, color = Color(0xFFFEF3C7))

            // Book 2 (Middle book - Cyan / Sky Blue)
            val pathBook2 = Path().apply {
                moveTo(w * 0.16f, h * 0.58f)
                lineTo(w * 0.80f, h * 0.52f)
                lineTo(w * 0.83f, h * 0.68f)
                lineTo(w * 0.18f, h * 0.75f)
                close()
            }
            drawPath(
                path = pathBook2,
                brush = Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFF0369A1)))
            )
            // Book 2 Pages
            val pathBook2Pages = Path().apply {
                moveTo(w * 0.80f, h * 0.52f)
                lineTo(w * 0.88f, h * 0.57f)
                lineTo(w * 0.83f, h * 0.68f)
                close()
            }
            drawPath(path = pathBook2Pages, color = Color(0xFFFFFBEB))

            // Book 3 (Top book - Vibrant Orange / Coral)
            val pathBook3 = Path().apply {
                moveTo(w * 0.20f, h * 0.44f)
                lineTo(w * 0.78f, h * 0.40f)
                lineTo(w * 0.81f, h * 0.55f)
                lineTo(w * 0.22f, h * 0.60f)
                close()
            }
            drawPath(
                path = pathBook3,
                brush = Brush.linearGradient(listOf(Color(0xFFEA580C), Color(0xFFF97316), Color(0xFFFB923C)))
            )
            // Book 3 Pages
            val pathBook3Pages = Path().apply {
                moveTo(w * 0.78f, h * 0.40f)
                lineTo(w * 0.86f, h * 0.44f)
                lineTo(w * 0.81f, h * 0.55f)
                close()
            }
            drawPath(path = pathBook3Pages, color = Color(0xFFFEF9C3))

            // Graduation Cap (Mortarboard rhombus diamond)
            val pathCap = Path().apply {
                moveTo(w * 0.50f, h * 0.10f)
                lineTo(w * 0.88f, h * 0.22f)
                lineTo(w * 0.50f, h * 0.35f)
                lineTo(w * 0.12f, h * 0.22f)
                close()
            }
            drawPath(
                path = pathCap,
                brush = Brush.linearGradient(listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF1E293B)))
            )

            // Cap Skull Cap Base underneath
            val pathSkull = Path().apply {
                moveTo(w * 0.32f, h * 0.28f)
                lineTo(w * 0.68f, h * 0.28f)
                lineTo(w * 0.64f, h * 0.40f)
                lineTo(w * 0.36f, h * 0.40f)
                close()
            }
            drawPath(
                path = pathSkull,
                brush = Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF020617)))
            )

            // Center Golden Button on Cap
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = w * 0.035f,
                center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.225f)
            )

            // Golden Tassel Ribbon hanging down to the right
            val pathTassel = Path().apply {
                moveTo(w * 0.50f, h * 0.225f)
                cubicTo(w * 0.65f, h * 0.22f, w * 0.78f, h * 0.28f, w * 0.82f, h * 0.40f)
            }
            drawPath(
                path = pathTassel,
                color = Color(0xFFFBBF24),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )

            // Tassel Fringe / Pom-pom at end
            val pathFringe = Path().apply {
                moveTo(w * 0.80f, h * 0.39f)
                lineTo(w * 0.84f, h * 0.39f)
                lineTo(w * 0.86f, h * 0.52f)
                lineTo(w * 0.78f, h * 0.52f)
                close()
            }
            drawPath(
                path = pathFringe,
                brush = Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
            )
        }
    }
}

@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PremiumNavyCard,
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AppIconBadge(
    size: androidx.compose.ui.unit.Dp = 34.dp,
    modifier: Modifier = Modifier
) {
    val config = LocalAppConfig.current
    val imageVector = when (config.logoIconName) {
        "School" -> Icons.Default.School
        "MenuBook" -> Icons.Default.MenuBook
        "Shield" -> Icons.Default.Shield
        "LocalPolice" -> Icons.Default.LocalPolice
        "AccountBalance" -> Icons.Default.AccountBalance
        "Train" -> Icons.Default.Train
        else -> Icons.Default.School
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                )
            )
            .border(1.5.dp, Color(0xFFFFD700), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E40AF), Color(0xFF1E1B4B))
                    )
                )
                .border(0.8.dp, Color(0xFFFFD700).copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = "Competitive Exam Academy Logo",
                tint = Color(0xFFFFD700), // Elegant pure gold
                modifier = Modifier.size(size * 0.48f)
            )
        }
    }
}

fun handlePosterRedirection(
    destination: String,
    posterTitle: String,
    offerDetails: String = "",
    context: android.content.Context,
    onNavigateToSubject: (Subject) -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToMockTests: () -> Unit,
    onNavigateToDailyQuiz: () -> Unit,
    onNavigateToCurrentAffairs: () -> Unit,
    onNavigateToPerformance: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val cleanDest = destination.trim().lowercase()
    val combinedContext = "$cleanDest $posterTitle $offerDetails".lowercase()

    // 1. Direct or fuzzy match against known Subjects in SampleData
    val matchedSubject: Subject? = SampleData.defaultSubjects.firstOrNull { subject ->
        val sId = subject.id.lowercase()
        val sName = subject.name.lowercase()
        cleanDest == sId ||
        cleanDest == "subj-$sId" ||
        cleanDest == sName ||
        (cleanDest.contains("ap-hist") && sId.contains("ap-history")) ||
        (cleanDest.contains("ap_hist") && sId.contains("ap-history")) ||
        (cleanDest.contains("indian-hist") && sId.contains("indian-history")) ||
        (cleanDest.contains("indian_hist") && sId.contains("indian-history")) ||
        ((cleanDest.contains("economy") || cleanDest.contains("economic")) && sId.contains("economy")) ||
        ((cleanDest.contains("polity") || cleanDest.contains("constitution")) && sId.contains("constitution")) ||
        (cleanDest.contains("geography") && sId.contains("geography")) ||
        (cleanDest.contains("society") && sId.contains("society")) ||
        ((cleanDest.contains("mental") || cleanDest.contains("reasoning") || cleanDest.contains("aptitude")) && sId.contains("mental-ability")) ||
        ((cleanDest.contains("science") || cleanDest.contains("tech")) && sId.contains("science-tech"))
    }

    if (matchedSubject != null) {
        Toast.makeText(context, "📚 Opening ${matchedSubject.name} Module & Syllabus...", Toast.LENGTH_SHORT).show()
        onNavigateToSubject(matchedSubject)
        return
    }

    // 2. Primary Promotional & Feature Screen Routes
    when {
        cleanDest.contains("sub") || cleanDest.contains("plan") || cleanDest.contains("price") || cleanDest.contains("pay") || cleanDest.contains("offer") -> {
            Toast.makeText(context, "🎉 Opening Special Subscription Plans & Offers...", Toast.LENGTH_SHORT).show()
            onNavigateToSubscription()
        }
        cleanDest.contains("mock") || cleanDest.contains("test") || cleanDest.contains("exam") || cleanDest.contains("marathon") -> {
            Toast.makeText(context, "🎯 Opening Grand Mock Test Series...", Toast.LENGTH_SHORT).show()
            onNavigateToMockTests()
        }
        cleanDest.contains("daily") || cleanDest.contains("quiz") || cleanDest.contains("speed") -> {
            Toast.makeText(context, "⚡ Loading Daily Speed Quiz Arena...", Toast.LENGTH_SHORT).show()
            onNavigateToDailyQuiz()
        }
        cleanDest.contains("current") || cleanDest.contains("affair") || cleanDest.contains("news") || cleanDest.contains("capsule") -> {
            Toast.makeText(context, "📰 Opening 2026 Current Affairs Capsule...", Toast.LENGTH_SHORT).show()
            onNavigateToCurrentAffairs()
        }
        cleanDest.contains("perform") || cleanDest.contains("score") || cleanDest.contains("analytic") || cleanDest.contains("accuracy") || cleanDest.contains("weak") -> {
            Toast.makeText(context, "📊 Opening Performance Diagnostics & Accuracy Heatmap...", Toast.LENGTH_SHORT).show()
            onNavigateToPerformance()
        }
        cleanDest.contains("profile") || cleanDest.contains("account") -> {
            onNavigateToProfile()
        }
        // 3. Fallback: contextual subject matching from poster text
        combinedContext.contains("ap history") || combinedContext.contains("andhra pradesh history") || combinedContext.contains("satavahana") -> {
            val sub = SampleData.defaultSubjects.firstOrNull { it.id == "subj-ap-history" } ?: SampleData.defaultSubjects[0]
            Toast.makeText(context, "📚 Opening AP History Subject Module...", Toast.LENGTH_SHORT).show()
            onNavigateToSubject(sub)
        }
        combinedContext.contains("economy") || combinedContext.contains("socio-economic survey") -> {
            val sub = SampleData.defaultSubjects.firstOrNull { it.id == "subj-economy" } ?: SampleData.defaultSubjects[0]
            Toast.makeText(context, "📈 Opening Indian & AP Economy Module...", Toast.LENGTH_SHORT).show()
            onNavigateToSubject(sub)
        }
        combinedContext.contains("polity") || combinedContext.contains("constitution") -> {
            val sub = SampleData.defaultSubjects.firstOrNull { it.id == "subj-constitution" } ?: SampleData.defaultSubjects[0]
            Toast.makeText(context, "⚖️ Opening Indian Constitution & Polity Module...", Toast.LENGTH_SHORT).show()
            onNavigateToSubject(sub)
        }
        combinedContext.contains("geography") -> {
            val sub = SampleData.defaultSubjects.firstOrNull { it.id == "subj-geography" } ?: SampleData.defaultSubjects[0]
            Toast.makeText(context, "🌍 Opening Geography Module...", Toast.LENGTH_SHORT).show()
            onNavigateToSubject(sub)
        }
        combinedContext.contains("mental ability") || combinedContext.contains("reasoning") -> {
            val sub = SampleData.defaultSubjects.firstOrNull { it.id == "subj-mental-ability" } ?: SampleData.defaultSubjects[0]
            Toast.makeText(context, "🧠 Opening Mental Ability & Reasoning Module...", Toast.LENGTH_SHORT).show()
            onNavigateToSubject(sub)
        }
        combinedContext.contains("science") -> {
            val sub = SampleData.defaultSubjects.firstOrNull { it.id == "subj-science-tech" } ?: SampleData.defaultSubjects[0]
            Toast.makeText(context, "🔬 Opening Science & Technology Module...", Toast.LENGTH_SHORT).show()
            onNavigateToSubject(sub)
        }
        else -> {
            Toast.makeText(context, "✨ Opening Offer: $posterTitle", Toast.LENGTH_SHORT).show()
            onNavigateToSubscription()
        }
    }
}

@Composable
fun PromotionalPosterCard(
    poster: PromotionalPosterModel,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(168.dp),
    onPosterClick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x330F172A),
                spotColor = Color(0x3D000000)
            )
            .clickable { onPosterClick(poster.destinationPage) },
        border = BorderStroke(
            2.dp,
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFD700),
                    Color(0xFFFFFBEB),
                    Color(0xFFF59E0B),
                    Color(0xFFD4AF37),
                    Color(0xFFFEF3C7),
                    Color(0xFFFFD700)
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f)
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Full-cover Background Image (fits and covers entire poster card with no empty spaces)
            if (poster.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = poster.imageUrl,
                    contentDescription = poster.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PosterFhdCardImage(
                    poster = poster,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. High-legibility Multi-stop Gradient Overlay across the full card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.45f), // Soft dark top for badge contrast
                                Color.Transparent,               // Clear midsection showcasing poster artwork
                                Color.Black.copy(alpha = 0.70f), // Smooth transition to dark text scrim
                                Color.Black.copy(alpha = 0.95f)  // High-contrast deep scrim for details & CTA
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // 3. Top Badges & Priority Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEA580C) // Vibrant amber-orange
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = poster.posterType.uppercase(),
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "Priority #${poster.priorityOrder}",
                        color = Color(0xFFFDE68A),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // 4. Bottom Information Overlay: Poster Title, Promotional Text, Offer Details & CTA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Poster Name / Main Title
                Text(
                    text = poster.title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Promotional Tagline / Subtitle
                Text(
                    text = poster.promotionalText,
                    color = Color(0xFFFDE047), // Radiant gold highlight
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (poster.offerDetails.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = poster.offerDetails,
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Row: Click hint and CTA Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Special Exam Batch Offer",
                            color = Color(0xFFFDE68A),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFF7A00),
                        shadowElevation = 3.dp,
                        modifier = Modifier.clickable { onPosterClick(poster.destinationPage) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = poster.ctaText.ifBlank { "CLAIM OFFER" },
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumHeaderIcon(
    onClick: () -> Unit,
    containerColor: Color = Color.White.copy(alpha = 0.12f),
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun HomeUnitPreviewCard(
    subject: Subject,
    unit: UnitModel,
    unitIndex: Int = 0,
    isUnitExpanded: Boolean = false,
    isLocked: Boolean = false,
    onToggleUnitExpand: (() -> Unit)? = null,
    onOpenUnit: () -> Unit = {},
    onOpenTopic: (TopicModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val richGradient = remember(unit.id, subject.name) {
        com.example.ui.theme.PremiumCardColorSystem.getGradientForUnit(unit.unitNumber, subject.name, unit.name)
    }
    val unitChevronRotation by animateAccordionChevronRotation(isExpanded = isUnitExpanded)
    val unitBorderBrush = remember(isLocked, richGradient.borderColor) {
        if (isLocked) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFD700),
                    Color(0xFFFFFBEB),
                    Color(0xFFF59E0B),
                    Color(0xFFD4AF37),
                    Color(0xFFFFD700)
                ),
                start = Offset(0f, 0f),
                end = Offset(800f, 800f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    richGradient.borderColor,
                    Color(0xFFFFD700).copy(alpha = 0.85f),
                    Color(0xFFFFFBEB),
                    Color(0xFFFFD700).copy(alpha = 0.85f),
                    richGradient.borderColor
                ),
                start = Offset(0f, 0f),
                end = Offset(800f, 800f)
            )
        }
    }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.75.dp, unitBorderBrush),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color(0x2B0F172A),
                spotColor = Color(0x33000000)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors = richGradient.colors))
        ) {
            // Unit Banner with Unit FHD Image (70% of Subjects Card: 118.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(158.dp)
                    .clickable {
                        if (onToggleUnitExpand != null) {
                            onToggleUnitExpand()
                        } else {
                            onOpenUnit()
                        }
                    }
            ) {
                UnitFhdCardImage(
                    unit = unit,
                    subjectName = subject.name,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay: Transparent top and mid, soft readable dark bottom scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent, 
                                    Color.Transparent, 
                                    Color.Black.copy(alpha = if (isLocked) 0.6f else 0.45f), 
                                    Color.Black.copy(alpha = if (isLocked) 0.9f else 0.85f)
                                )
                            )
                        )
                )

                // Unit Pill Top Left
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = richGradient.badgeBg,
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "UNIT ${unit.unitNumber.takeIf { it > 0 } ?: (unitIndex + 1)}",
                        color = richGradient.badgeText,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Top Right Action Pill (Unlock Pro or Expand/Collapse Topics)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isLocked) Color(0xFF0F172A) else Color(0xDD0F172A),
                    border = BorderStroke(
                        1.dp,
                        if (isLocked || isUnitExpanded) Color(0xFFFFD700) else Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Unlock (Pro)",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = if (isUnitExpanded) "Hide Topics" else "View Topics",
                                color = Color(0xFFFDE68A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isUnitExpanded) "Collapse Topics" else "Expand Topics",
                                tint = Color(0xFFFDE68A),
                                modifier = Modifier
                                    .size(14.dp)
                                    .graphicsLayer {
                                        rotationZ = unitChevronRotation
                                    }
                            )
                        }
                    }
                }

                // Unit Title at Bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Text(
                        text = com.example.data.SampleData.formatUnitTitle(unit.unitNumber, unit.name),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val unitSetsCount = unit.topics.size * 2
                    val unitMcqCount = remember(unit.id, SampleData.questionsState.size) {
                        unit.topics.sumOf { SampleData.getQuestionsForPractice(subject.id, unit.id, it.id, "").size }
                    }
                    Text(
                        text = "${unit.topics.size} Topics • $unitSetsCount Sets • $unitMcqCount MCQs",
                        color = Color(0xFFFDE68A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Unit -> Topics Cascade-Drop Accordion Strip with FHD Topic Images
            AnimatedVisibility(
                visible = isUnitExpanded && unit.topics.isNotEmpty(),
                enter = AppMotionSystem.cascadeContainerEnterTransition(durationMs = 270),
                exit = AppMotionSystem.cascadeContainerExitTransition(durationMs = 200)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A).copy(alpha = 0.88f))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UNIT TOPICS (${unit.topics.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFDE68A),
                            letterSpacing = 0.4.sp
                        )
                        Text(
                            text = "Open Unit →",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF93C5FD),
                            modifier = Modifier.clickable { onOpenUnit() }
                        )
                    }

                    unit.topics.forEachIndexed { topicIndex, topic ->
                        key(topic.id) {
                            val isTopicLocked = isLocked || (!com.example.data.SampleData.isUserPremium() && (unit.unitNumber > 1 || topicIndex > 0))
                            val topicGradient = remember(topic.id, topic.name) {
                                com.example.ui.theme.PremiumCardColorSystem.getGradientForTopic(topicIndex, topic.name, subject.name)
                            }

                            com.example.ui.components.CascadeItem(
                                index = topicIndex,
                                visible = isUnitExpanded,
                                target = com.example.model.AnimatableTarget.TOPIC_CARDS,
                                screenKey = "Home Screen",
                                triggerKey = "${unit.id}_topic_${topic.id}",
                                initialOffsetY = 16.dp,
                                staggerDelayMs = AppMotionSystem.CASCADE_TOPIC_STAGGER_MS,
                                durationMs = 260
                            ) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, if (isTopicLocked) Color(0xFFFFD700).copy(alpha = 0.35f) else topicGradient.borderColor.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(138.dp)
                                    .testTag("topic_card_${topic.id}")
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (isTopicLocked) {
                                            onOpenTopic(topic)
                                        } else {
                                            onOpenTopic(topic)
                                        }
                                    }
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    TopicFhdCardImage(
                                        topic = topic,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = 0.88f),
                                                        Color.Black.copy(alpha = 0.70f),
                                                        Color.Black.copy(alpha = 0.40f)
                                                    )
                                                )
                                            )
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = topicGradient.badgeBg
                                            ) {
                                                Text(
                                                    text = "TOPIC ${topicIndex + 1}",
                                                    color = topicGradient.badgeText,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = topic.name,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = topic.subtitle ?: "${topic.subtopics.size} Subtopics • MCQs",
                                                    color = Color(0xFFFDE68A),
                                                    fontSize = 10.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        if (isTopicLocked) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Practice",
                                                    color = Color(0xFFFDE68A),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFDE68A),
                                                    modifier = Modifier.size(11.dp)
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
            }
        }
    }
}

@Composable
fun HomeCardContainer(
    modifier: Modifier = Modifier,
    accentBorderColor: Color? = null,
    content: @Composable () -> Unit
) {
    val borderConfig = SampleData.cardBorderConfigState.value
    val topBorderColor = Color(borderConfig.topBorderColorHex)
    val otherBorderColor = Color(borderConfig.otherBorderColorHex)
    val cornerRadius = borderConfig.cornerRadiusDp.dp

    // Consistent thickness on all four sides (crisp, clearly visible min 2.dp)
    val borderWidth = maxOf(borderConfig.otherBorderWidthDp, borderConfig.topBorderWidthDp, 2).dp

    // High-end metallic gold/champagne shimmer brushes complementing the Navy & Gold theme
    val isGoldAccent = borderConfig.topBorderColorHex == 0xFFFFD700L ||
            borderConfig.topBorderColorHex == 0xFFF59E0BL ||
            borderConfig.otherBorderColorHex == 0xFFD4AF37L

    // Unified, continuous 4-sided gradient border framing the entire card evenly
    val perimeterBorderBrush = remember(borderConfig.otherBorderColorHex, borderConfig.topBorderColorHex, isGoldAccent, accentBorderColor) {
        if (accentBorderColor != null) {
            Brush.linearGradient(
                colors = listOf(
                    accentBorderColor,
                    Color(0xFFFFD700),
                    Color(0xFFFFFBEB),
                    Color(0xFFFFD700),
                    accentBorderColor.copy(alpha = 0.85f),
                    accentBorderColor
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f)
            )
        } else if (isGoldAccent) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFD700), // Imperial Gold
                    Color(0xFFFFFBEB), // Glistening Diamond Highlight
                    Color(0xFFF59E0B), // Warm Deep Amber
                    Color(0xFFD4AF37), // Metallic Antique Gold
                    Color(0xFFFEF3C7), // Shimmer Glint
                    Color(0xFFFFD700)  // Imperial Gold loop
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    topBorderColor,
                    Color.White.copy(alpha = 0.85f),
                    otherBorderColor,
                    topBorderColor.copy(alpha = 0.90f),
                    otherBorderColor
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f)
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier
            .shadow(
                elevation = 0.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Transparent,
                spotColor = Color.Transparent
            )
            .border(BorderStroke(borderWidth, perimeterBorderBrush), RoundedCornerShape(cornerRadius)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(cornerRadius))
        ) {
            content()

            // Subtle inner depth bevel frame (tactile glass/metal depth)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        BorderStroke(
                            0.75.dp,
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.08f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.22f)
                                )
                            )
                        ),
                        RoundedCornerShape(cornerRadius)
                    )
            )

            // Polished luminous top sheen line complementing the regal theme
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                (accentBorderColor ?: Color(0xFFD4AF37)).copy(alpha = 0.3f),
                                Color(0xFFFFD700).copy(alpha = 0.85f),
                                Color(0xFFFFFBEB),
                                Color(0xFFFFD700).copy(alpha = 0.85f),
                                (accentBorderColor ?: Color(0xFFD4AF37)).copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }
    }
}

/**
 * LIVE CONTENT DASHBOARD - 100% Exact Replica of reference image
 * - Clean, modern, warm ivory container
 * - Left: Glowing green status indicator
 * - Title: "LIVE CONTENT " (Dark Emerald Green) + "DASHBOARD" (Vibrant Orange)
 * - Subtitle: "Real-time question bank & syllabus metrics"
 * - Accent gold underline under header
 * - Right: "Live Bank >" vibrant green pill button
 * - SINGLE HORIZONTAL ROW of 5 equal-height metric cards:
 *   1. Subjects (Coral Red gradient, white circle with coral doc icon, dynamic number, white chevron)
 *   2. Units (Emerald Green gradient, white circle with green layers icon, dynamic number, white chevron)
 *   3. Topics (Amber Gold gradient, white circle with gold book icon, dynamic number, white chevron)
 *   4. Practice Sets (Violet Purple gradient, white circle with violet grid icon, dynamic number, white chevron)
 *   5. Total MCQs (Pink Magenta gradient, white circle with pink question icon, dynamic number, white chevron)
 * - Strictly NO black, grey, or blue in dashboard metrics.
 */
@Composable
fun HomeLiveContentDashboard(
    displaySubjects: List<Subject>,
    currentAppId: String,
    modifier: Modifier = Modifier,
    onNavigateToSyllabus: () -> Unit = {},
    onNavigateToTopicSets: () -> Unit = {},
    onNavigateToPractice: () -> Unit = {},
    onNavigateToLiveBank: () -> Unit = onNavigateToTopicSets,
    onNavigateToSubject: (Subject) -> Unit = {},
    onNavigateToUnit: ((Subject, UnitModel) -> Unit)? = null,
    onNavigateToTopic: ((Subject, UnitModel, TopicModel) -> Unit)? = null,
    onNavigateToCurrentAffairs: () -> Unit = {},
    onNavigateToMockTests: () -> Unit = {},
    onRefreshCategoryCounts: ((CategoryContentCounts) -> Unit)? = null,
    config: com.example.model.LiveDashboardConfig = SampleData.liveDashboardConfigState.value
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var isAppOffline by remember { mutableStateOf(false) }
    var refreshCounter by remember { mutableIntStateOf(0) }
    var activeMetricsSheet by remember { mutableStateOf<LiveMetricsSheetType?>(null) }
    var lastSyncedTime by remember {
        mutableStateOf(
            java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        )
    }

    // 1. Total Subjects (including Current Affairs as the permanent featured subject arena)
    val totalSubjects = remember(displaySubjects.size, refreshCounter) {
        displaySubjects.size + 1
    }

    // 2. Total Units across all subjects for the current app
    val totalUnits = remember(displaySubjects, refreshCounter) {
        displaySubjects.sumOf { it.units.size }
    }

    // 3. Total Topics across all syllabus units
    val totalTopics = remember(displaySubjects, refreshCounter) {
        displaySubjects.sumOf { subject -> subject.units.sumOf { unit -> unit.topics.size } }
    }

    // 4. Total Practice Sets (dynamic uploaded sets + syllabus configured sets + current affairs sets)
    val totalSets = remember(
        displaySubjects,
        SampleData.questionsState.size,
        SampleData.monthlyCurrentAffairsState.size,
        refreshCounter
    ) {
        val subjectTopicSets = displaySubjects.sumOf { subject ->
            subject.units.sumOf { unit ->
                unit.topics.sumOf { topic ->
                    val setsForTopic = SampleData.getPracticeSetsForTopic(subject.id, unit, topic)
                    if (setsForTopic.isNotEmpty()) setsForTopic.size else maxOf(topic.practiceSetsCount, 2)
                }
            }
        }
        val caMonthlySets = SampleData.getMonthlyCurrentAffairs().sumOf { it.sets.size }
        val subjectLevelSets = displaySubjects.sumOf { subject ->
            SampleData.questionsState.filter { it.subjectId == subject.id && it.unitId.isBlank() && it.isActive }
                .map { it.setName }.filter { it.isNotBlank() }.distinct().size
        }
        subjectTopicSets + caMonthlySets + subjectLevelSets
    }

    // 5. Total MCQs (real-time question bank from subjects, monthly current affairs, and active questions)
    val totalMcqs = remember(
        displaySubjects,
        SampleData.questionsState.size,
        SampleData.monthlyCurrentAffairsState.size,
        SampleData.currentAffairsState.size,
        refreshCounter
    ) {
        val subjectMcqs = displaySubjects.sumOf { subject ->
            SampleData.getQuestionsCountForSubject(subject.id)
        }
        val caMonthlyMcqs = SampleData.getMonthlyCurrentAffairs().sumOf { monthly ->
            monthly.sets.sumOf { it.questions.size }
        }
        val caAttachedMcqs = SampleData.getCurrentAffairsForApp(currentAppId).sumOf {
            it.attachedMcqs.size
        }
        val otherAppMcqs = SampleData.questionsState.count { q ->
            (q.appId == currentAppId || q.appId == "ALL") && q.isActive && displaySubjects.none { it.id == q.subjectId }
        }
        subjectMcqs + caMonthlyMcqs + caAttachedMcqs + otherAppMcqs
    }

    val liveColor = Color(0xFF10B981)
    val liveDotInnerColor = Color(0xFF34D399)
    // Live Content Dashboard: plain rose color scheme.
    val containerBgStart = Color(0xFFFFF1F2)
    val containerBgEnd = Color(0xFFFFE4E6)
    val containerBorder = Color(0xFFFB7185)
    val liveBankBtnColor = Color(0xFFE11D48)

    val beaconInfiniteTransition = rememberInfiniteTransition(label = "beaconPulse")
    val beaconPulseScale by beaconInfiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconPulseScale"
    )
    val beaconPulseAlpha by beaconInfiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconPulseAlpha"
    )
    var refreshSpinAngle by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            while (isRefreshing) {
                refreshSpinAngle = (refreshSpinAngle + 12f) % 360f
                delay(16)
            }
        } else {
            refreshSpinAngle = 0f
        }
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFD4AF37),
                    Color(0xFFF8BBD0),
                    Color(0xFFD4AF37)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(containerBgStart, containerBgEnd)
                    )
                )
                .padding(horizontal = 6.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header: Status Beacon + Title + Subtitle + Refresh Button + "Live Bank >" Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Green beacon indicator + Title + Subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Glowing circular green Live status indicator
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(18.dp)
                    ) {
                        // Outer halo
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer {
                                    scaleX = if (isAppOffline) 1f else beaconPulseScale
                                    scaleY = if (isAppOffline) 1f else beaconPulseScale
                                    alpha = if (isAppOffline) 1f else beaconPulseAlpha
                                }
                                .clip(CircleShape)
                                .background(liveColor)
                        )
                        // Inner solid vibrant green dot
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            liveDotInnerColor,
                                            liveColor
                                        )
                                    )
                                )
                        )
                    }

                    if (isAppOffline) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = "Offline",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${config.headerTitlePrefix} ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF065F46),
                                letterSpacing = 0.2.sp
                            )
                            Text(
                                text = config.headerTitleSuffix,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFEA580C),
                                letterSpacing = 0.2.sp
                            )
                        }
                        Text(
                            text = config.headerSubtitle,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF4B5563),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right: Small Circular Refresh Button + "Live Bank >" Pill Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Small circular refresh button to manually re-fetch and update count metrics from Firestore
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, if (isRefreshing) liveColor else containerBorder),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable(enabled = !isRefreshing) {
                                val isOnline = checkNetworkOnline(context)
                                if (!isOnline) {
                                    isAppOffline = true
                                    Toast.makeText(context, "No internet connection. Switched to offline mode.", Toast.LENGTH_SHORT).show()
                                } else {
                                    isAppOffline = false
                                    isRefreshing = true
                                    coroutineScope.launch {
                                        FirebaseSyncManager.fetchQuestionsAndSyncMetrics(
                                            appId = currentAppId,
                                            context = context
                                        ) { success, count, message ->
                                            isRefreshing = false
                                            refreshCounter++
                                            lastSyncedTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        }
                                        FirebaseSyncManager.fetchCategoryContentCounts(currentAppId) { updatedCounts ->
                                            onRefreshCategoryCounts?.invoke(updatedCounts)
                                        }
                                    }
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh live content metrics from Firestore",
                                tint = if (isRefreshing) liveColor else Color(0xFF15803D),
                                modifier = Modifier
                                    .size(13.dp)
                                    .graphicsLayer {
                                        rotationZ = if (isRefreshing) refreshSpinAngle else 0f
                                    }
                            )
                        }
                    }

                    // "Live Bank >" Pill Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                Toast.makeText(context, "Live Bank is active and synchronized.", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            liveBankBtnColor,
                                            Color(0xFF008938)
                                        )
                                    )
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SignalCellularAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = config.liveBankButtonText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Small 'Last updated: [Time]' timestamp label below the header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = "Last updated: $lastSyncedTime",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }

            // SINGLE HORIZONTAL ROW: All 5 Statistic Cards with exact reference color treatments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Card 1: 10 Subjects — blush pink background & border, solid pink badge
                SingleRowMetricCard(
                    modifier = Modifier.weight(1f),
                    title = config.subjectsCard.title,
                    count = config.subjectsCard.overrideValue ?: totalSubjects,
                    icon = Icons.AutoMirrored.Filled.Article,
                    cardBgColor = Color(0xFFFDE8E9),
                    cardBorderColor = Color(0xFFF472B6),
                    badgeBgColor = Color(0xFFE91E63),
                    textColor = Color(0xFF880E4F),
                    onClick = { activeMetricsSheet = LiveMetricsSheetType.SUBJECTS }
                )

                // Card 2: 19 Units — champagne peach background & border, solid orange badge
                SingleRowMetricCard(
                    modifier = Modifier.weight(1f),
                    title = config.unitsCard.title,
                    count = config.unitsCard.overrideValue ?: totalUnits,
                    icon = Icons.Default.Layers,
                    cardBgColor = Color(0xFFFFF3E0),
                    cardBorderColor = Color(0xFFFFB74D),
                    badgeBgColor = Color(0xFFFF6D00),
                    textColor = Color(0xFF7C2D12),
                    onClick = { activeMetricsSheet = LiveMetricsSheetType.UNITS }
                )

                // Card 3: 22 Topics — soft lavender background & border, solid purple badge
                SingleRowMetricCard(
                    modifier = Modifier.weight(1f),
                    title = config.topicsCard.title,
                    count = config.topicsCard.overrideValue ?: totalTopics,
                    icon = Icons.Default.Description,
                    cardBgColor = Color(0xFFF3E5F5),
                    cardBorderColor = Color(0xFFCE93D8),
                    badgeBgColor = Color(0xFF8E24AA),
                    textColor = Color(0xFF4A148C),
                    onClick = { activeMetricsSheet = LiveMetricsSheetType.TOPICS }
                )

                // Card 4: 72 Sets — soft mint background & border, solid green badge
                SingleRowMetricCard(
                    modifier = Modifier.weight(1f),
                    title = config.practiceSetsCard.title,
                    count = config.practiceSetsCard.overrideValue ?: totalSets,
                    icon = Icons.Default.Checklist,
                    cardBgColor = Color(0xFFE8F5E9),
                    cardBorderColor = Color(0xFFA5D6A7),
                    badgeBgColor = Color(0xFF10B981),
                    textColor = Color(0xFF064E3B),
                    onClick = { activeMetricsSheet = LiveMetricsSheetType.PRACTICE_SETS }
                )

                // Card 5: 187 MCQs — warm ivory-gold background & border, solid gold badge
                SingleRowMetricCard(
                    modifier = Modifier.weight(1f),
                    title = config.totalMcqsCard.title,
                    count = config.totalMcqsCard.overrideValue ?: totalMcqs,
                    icon = Icons.Default.GridView,
                    cardBgColor = Color(0xFFFFFDE7),
                    cardBorderColor = Color(0xFFFFE082),
                    badgeBgColor = Color(0xFFFFB300),
                    textColor = Color(0xFF78350F),
                    onClick = { activeMetricsSheet = LiveMetricsSheetType.TOTAL_MCQS }
                )
            }
        }
    }

    // Modal Bottom Sheet displaying full breakdown of total available subjects, units, topics, practice sets, or total MCQs
    activeMetricsSheet?.let { sheetType ->
        LiveMetricsDetailBottomSheet(
            sheetType = sheetType,
            displaySubjects = displaySubjects,
            currentAppId = currentAppId,
            onDismissRequest = { activeMetricsSheet = null },
            onSelectSubject = { subject ->
                activeMetricsSheet = null
                onNavigateToSubject(subject)
            },
            onSelectUnit = { subject, unit ->
                activeMetricsSheet = null
                onNavigateToUnit?.invoke(subject, unit) ?: onNavigateToSubject(subject)
            },
            onSelectTopic = { subject, unit, topic ->
                activeMetricsSheet = null
                onNavigateToTopic?.invoke(subject, unit, topic) ?: onNavigateToSubject(subject)
            },
            onNavigateToPracticeSets = {
                activeMetricsSheet = null
                onNavigateToTopicSets()
            },
            onNavigateToMockTests = {
                activeMetricsSheet = null
                onNavigateToMockTests()
            },
            onNavigateToCurrentAffairs = {
                activeMetricsSheet = null
                onNavigateToCurrentAffairs()
            }
        )
    }
}

/**
 * Compact Equal-Width Metric Card for the Single-Row Live Content Dashboard:
 * - Arranges all 5 statistic cards (Subjects, Units, Topics, Sets, MCQs) in ONE horizontal row
 * - Rich orange gradient background (#FF9800 to #FF6D00) with clean non-floral luminous sheen
 * - Strong layered orange-and-white rounded border and subtle orange glow
 * - Compact height (58.dp) with equal weight distribution
 * - Circular frosted icon container on top, bold dynamic count in middle, clean label at bottom
 * - Tactile press animation with scale and elevation transition
 */
@Composable
fun SingleRowMetricCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    cardBgColor: Color,
    cardBorderColor: Color,
    badgeBgColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayTitle = remember(title) {
        when {
            title.contains("Subject", ignoreCase = true) -> "Subjects"
            title.contains("Unit", ignoreCase = true) -> "Units"
            title.contains("Topic", ignoreCase = true) -> "Topics"
            title.contains("Set", ignoreCase = true) -> "Sets"
            title.contains("MCQ", ignoreCase = true) -> "MCQs"
            else -> title
        }
    }

    val pressState = rememberCardPressAnimationState(
        targetScale = 0.96f,
        defaultElevation = 3.dp,
        pressedElevation = 1.dp,
        durationMs = 120
    )

    Card(
        modifier = modifier
            .height(92.dp)
            .graphicsLayer {
                scaleX = pressState.scale
                scaleY = pressState.scale
            }
            .clickable(
                interactionSource = pressState.interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.5.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = pressState.elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 6.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Circular solid icon badge
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(badgeBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Dynamic animated statistic count number
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "singleRowCardCountAnim"
            ) { targetCount ->
                Text(
                    text = "$targetCount",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }

            // Clean concise label
            Text(
                text = displayTitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.9f),
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Wide Rectangular Metric Box for Subjects and Units:
 * - Prominent rectangular ratio (~3.4 : 1)
 * - Translucent circular icon container on left
 * - Bold count & title on right
 * - Crisp right indicator
 * - Subtle tactile press animation (scale 0.975f, smooth 140ms transition)
 */
@Composable
fun RectangularPrimaryMetricCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgStart: Color,
    bgEnd: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBorderBrush = remember(bgStart, bgEnd) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.55f),
                bgStart.copy(alpha = 0.20f)
            )
        )
    }
    val pressState = rememberCardPressAnimationState(
        targetScale = 0.975f,
        defaultElevation = 2.dp,
        pressedElevation = 0.5.dp,
        durationMs = 140
    )

    Surface(
        modifier = modifier
            .height(48.dp)
            .graphicsLayer {
                scaleX = pressState.scale
                scaleY = pressState.scale
            }
            .clip(RoundedCornerShape(11.dp))
            .clickable(
                interactionSource = pressState.interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(11.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, cardBorderBrush),
        shadowElevation = pressState.elevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(bgStart, bgEnd)
                    )
                )
                .padding(horizontal = 9.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Left: Translucent white icon circle
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Middle: Bold Count & Clean Label
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedContent(
                        targetState = count,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                        },
                        label = "primaryRectCardCountAnim"
                    ) { targetCount ->
                        Text(
                            text = "$targetCount",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 15.sp,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.95f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.70f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

/**
 * Rectangular Metric Box for Topics, Practice Sets, and Total MCQs:
 * - Sleek rectangular ratio (~2.5 : 1) - distinctly NOT square
 * - Translucent circular icon container on left
 * - Bold count & title on right
 * - Fits screen width without truncation
 * - Subtle tactile press animation (scale 0.975f, smooth 140ms transition)
 */
@Composable
fun RectangularSecondaryMetricCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgStart: Color,
    bgEnd: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBorderBrush = remember(bgStart, bgEnd) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.50f),
                bgStart.copy(alpha = 0.20f)
            )
        )
    }
    val pressState = rememberCardPressAnimationState(
        targetScale = 0.975f,
        defaultElevation = 2.dp,
        pressedElevation = 0.5.dp,
        durationMs = 140
    )

    Surface(
        modifier = modifier
            .height(44.dp)
            .graphicsLayer {
                scaleX = pressState.scale
                scaleY = pressState.scale
            }
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = pressState.interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, cardBorderBrush),
        shadowElevation = pressState.elevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(bgStart, bgEnd)
                    )
                )
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Left: Translucent white icon circle
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Middle: Count and Label
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedContent(
                        targetState = count,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                        },
                        label = "secondaryRectCardCountAnim"
                    ) { targetCount ->
                        Text(
                            text = "$targetCount",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 13.sp,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = title,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.95f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}


/**
 * Balanced 2-column action strip for Community & Gamification + Offline Cloud Sync
 * - State Live Leaderboard (#28 Rank)
 * - Offline & Cloud Sync Center (Offline Mode / Cloud Backup)
 */
@Composable
fun HomeCommunityAndOfflineBar(
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToOfflineManager: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOfflineMode by com.example.util.OfflineSyncManager.isOfflineModeSimulated
    val pendingSyncCount by com.example.util.OfflineSyncManager.pendingSyncCountState
    val offlinePacksCount = com.example.util.OfflineSyncManager.downloadedPacks.size.let { if (it <= 0) 2 else it }
    val dailyRank by com.example.util.GamificationManager.currentUserRankDaily
    val displayRank = if (dailyRank > 0) dailyRank else 28

    val rankPressState = rememberCardPressAnimationState(targetScale = 0.975f, defaultElevation = 2.dp, pressedElevation = 0.5.dp, durationMs = 140)
    val syncPressState = rememberCardPressAnimationState(targetScale = 0.975f, defaultElevation = 2.dp, pressedElevation = 0.5.dp, durationMs = 140)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isTablet = maxWidth >= 600.dp
        val isSmallMobile = maxWidth < 380.dp

        val cardHeight = when {
            isTablet -> 48.dp
            isSmallMobile -> 40.dp
            else -> 44.dp
        }
        val iconCircleSize = when {
            isTablet -> 28.dp
            isSmallMobile -> 22.dp
            else -> 25.dp
        }
        val iconInnerSize = when {
            isTablet -> 16.dp
            isSmallMobile -> 12.dp
            else -> 14.dp
        }
        val chevronCircleSize = when {
            isTablet -> 18.dp
            isSmallMobile -> 14.dp
            else -> 16.dp
        }
        val chevronInnerSize = when {
            isTablet -> 12.dp
            isSmallMobile -> 9.dp
            else -> 10.dp
        }
        val titleFontSize = when {
            isTablet -> 12.5.sp
            isSmallMobile -> 10.sp
            else -> 11.sp
        }
        val subtitleFontSize = when {
            isTablet -> 9.5.sp
            isSmallMobile -> 7.5.sp
            else -> 8.5.sp
        }
        val horizontalCardPadding = when {
            isTablet -> 7.dp
            isSmallMobile -> 3.5.dp
            else -> 4.5.dp
        }
        val cornerRadius = when {
            isTablet -> 14.dp
            else -> 12.dp
        }
        val cardSpacing = if (isTablet) 8.dp else 4.dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(cardSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Card 1 (Left): Pink-Orange Gradient (Leaderboard / Top Learners)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(cardHeight)
                    .graphicsLayer {
                        scaleX = rankPressState.scale
                        scaleY = rankPressState.scale
                    }
                    .clip(RoundedCornerShape(cornerRadius))
                    .clickable(
                        interactionSource = rankPressState.interactionSource,
                        indication = null
                    ) { onNavigateToLeaderboard() },
                shape = RoundedCornerShape(cornerRadius),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = rankPressState.elevation)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE91E63),
                                    Color(0xFFFF5722),
                                    Color(0xFFFF8F00)
                                )
                            )
                        )
                        .padding(horizontal = horizontalCardPadding, vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left White Circle with Trophy Icon (filled, magenta)
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 0.5.dp,
                            modifier = Modifier.size(iconCircleSize)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Trophy",
                                    tint = Color(0xFFE91E63),
                                    modifier = Modifier.size(iconInnerSize)
                                )
                            }
                        }

                        // Center Text Column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 3.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Leaderboard",
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Top Learners",
                                fontSize = subtitleFontSize,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.95f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Right Circular Chevron Button
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.45f),
                            modifier = Modifier.size(chevronCircleSize)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Navigate",
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(chevronInnerSize)
                                )
                            }
                        }
                    }
                }
            }

            // Card 2 (Right): Green Gradient (Synced / 2 Packs)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(cardHeight)
                    .graphicsLayer {
                        scaleX = syncPressState.scale
                        scaleY = syncPressState.scale
                    }
                    .clip(RoundedCornerShape(cornerRadius))
                    .clickable(
                        interactionSource = syncPressState.interactionSource,
                        indication = null
                    ) { onNavigateToOfflineManager() },
                shape = RoundedCornerShape(cornerRadius),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = syncPressState.elevation)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF00E676),
                                    Color(0xFF10B981),
                                    Color(0xFF059669)
                                )
                            )
                        )
                        .padding(horizontal = horizontalCardPadding, vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left White Circle with Sync Icon
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 0.5.dp,
                            modifier = Modifier.size(iconCircleSize)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync",
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(iconInnerSize)
                                )
                            }
                        }

                        // Center Text Column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 3.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isOfflineMode) "Offline" else if (pendingSyncCount > 0) "$pendingSyncCount Pend" else "Synced",
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (offlinePacksCount > 0) "$offlinePacksCount Packs" else "2 Packs",
                                fontSize = subtitleFontSize,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.95f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Right Circular Chevron Button
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.45f),
                            modifier = Modifier.size(chevronCircleSize)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Navigate",
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(chevronInnerSize)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Exact Pixel-to-Pixel recreation of the 3-card category selector from the reference design.
 * Cards:
 * 1. Prelims (Solid Orange, School/Mortarboard cap icon, pale peach count badge)
 * 2. Mains (Solid Hot Pink/Rose, WorkspacePremium/Award Medal icon, pale rose count badge)
 * 3. General (Solid Emerald Green, Public/Globe icon, pale mint count badge)
 */
private data class ReferenceCategoryCardItem(
    val key: String,
    val label: String,
    val count: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val cardColor: Color,
    val badgeBgColor: Color,
    val badgeTextColor: Color
)

@Composable
fun HomeSubjectCategoryFilterBar(
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    allCount: Int,
    prelimsCount: Int,
    mainsCount: Int,
    generalCount: Int,
    modifier: Modifier = Modifier
) {
    val config = LocalAppConfig.current
    val items = remember(
        allCount,
        prelimsCount,
        mainsCount,
        generalCount,
        config.isAllCardEnabled,
        config.isPrelimsCardEnabled,
        config.isMainsCardEnabled
    ) {
        buildList {
            if (config.isAllCardEnabled) {
                add(
                    ReferenceCategoryCardItem(
                        key = "ALL",
                        label = "All",
                        count = allCount,
                        icon = Icons.Default.Apps,
                        cardColor = Color(0xFFFF7A00),
                        badgeBgColor = Color(0xFFFFEDE0),
                        badgeTextColor = Color(0xFFD9480F)
                    )
                )
            }
            if (config.isPrelimsCardEnabled) {
                add(
                    ReferenceCategoryCardItem(
                        key = "PRELIMS",
                        label = "Prelims",
                        count = prelimsCount,
                        icon = Icons.Default.School,
                        cardColor = Color(0xFFFF7A00),
                        badgeBgColor = Color(0xFFFFEDE0),
                        badgeTextColor = Color(0xFFD9480F)
                    )
                )
            }
            if (config.isMainsCardEnabled) {
                add(
                    ReferenceCategoryCardItem(
                        key = "MAINS",
                        label = "Mains",
                        count = mainsCount,
                        icon = Icons.Default.WorkspacePremium,
                        cardColor = Color(0xFFFF7A00),
                        badgeBgColor = Color(0xFFFFEDE0),
                        badgeTextColor = Color(0xFFD9480F)
                    )
                )
            }
            add(
                ReferenceCategoryCardItem(
                    key = "GENERAL",
                    label = "General",
                    count = generalCount,
                    icon = Icons.Default.Public,
                    cardColor = Color(0xFFFF7A00),
                    badgeBgColor = Color(0xFFFFEDE0),
                    badgeTextColor = Color(0xFFD9480F)
                )
            )
        }
    }

    if (items.isEmpty()) return

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
        ) {
            val minCardWidth = 90.dp
            val spacing = 8.dp
            val horizontalPad = 28.dp
            val totalNeededWidth = (minCardWidth * items.size) + (spacing * (items.size - 1).coerceAtLeast(0)) + horizontalPad
            val canFitInRow = maxWidth >= totalNeededWidth

            if (canFitInRow) {
                // Adaptive Row for Tablets & Wide Screens: 4 items evenly weighted across container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        val isSelected = selectedCategory.equals(item.key, ignoreCase = true)
                        SingleCategoryReferenceCard(
                            item = item,
                            isSelected = isSelected,
                            onClick = { onSelectCategory(item.key) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // Scrollable Row for Mobile Screens: cards maintain a comfortable, unclipped width
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        val isSelected = selectedCategory.equals(item.key, ignoreCase = true)
                        SingleCategoryReferenceCard(
                            item = item,
                            isSelected = isSelected,
                            onClick = { onSelectCategory(item.key) },
                            modifier = Modifier.widthIn(min = 115.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleCategoryReferenceCard(
    item: ReferenceCategoryCardItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleAnim by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.965f
            isSelected -> 1.02f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "category_card_scale_${item.key}"
    )

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = item.cardColor,
        shadowElevation = when {
            isPressed -> 1.dp
            isSelected -> 3.5.dp
            else -> 1.2.dp
        },
        border = if (isSelected) {
            BorderStroke(2.dp, Color.White)
        } else {
            BorderStroke(0.8.dp, Color.White.copy(alpha = 0.22f))
        },
        modifier = modifier
            .height(38.dp)
            .graphicsLayer {
                scaleX = scaleAnim
                scaleY = scaleAnim
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(
                        bounded = true,
                        color = Color.White.copy(alpha = 0.38f)
                    ),
                    onClick = onClick
                )
                .padding(horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: White circle badge with card-tinted icon
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label.ifEmpty { "All" },
                        tint = item.cardColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (item.label.isNotEmpty()) {
                Spacer(modifier = Modifier.width(5.dp))

                // Center: Title Text
                Text(
                    text = item.label,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f, fill = false))
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                Spacer(modifier = Modifier.weight(1f, fill = false))
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Right: Count Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = item.badgeBgColor,
                modifier = Modifier
                    .height(20.dp)
                    .widthIn(min = 22.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${item.count}",
                        color = item.badgeTextColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

/**
 * Custom modifier providing smooth entrance upward reveal with subtle fade-in,
 * light parallax tracking with scroll, and micro-scale transition for home cards and sections.
 * Optimized for high performance and buttery smoothness across low-end and flagship devices.
 */
fun Modifier.homeSmoothReveal(
    index: Int = 0,
    parallaxFactor: Float = 0.018f,
    scrollState: ScrollState? = null,
    target: com.example.model.AnimatableTarget = com.example.model.AnimatableTarget.APP_LAUNCH_HOME_ENTRANCE
): Modifier = composed {
    val spec = com.example.util.AnimationConfigManager.resolveSpec(
        target = target,
        screenKey = "Home Screen"
    )
    val replayCount by com.example.util.AnimationConfigManager.replayTriggerCounter
    val slowMo by com.example.util.AnimationConfigManager.sandboxSlowMoFactor

    if (!spec.enabled || spec.entranceType == com.example.model.EntranceAnimationType.NONE) {
        return@composed this
    }

    var isRevealed by remember(replayCount, spec.entranceType) { mutableStateOf(false) }

    LaunchedEffect(replayCount, spec.entranceType, spec.staggerDelayMs, spec.maxStaggerItems) {
        isRevealed = false
        val staggerDelay = ((index.coerceIn(0, spec.maxStaggerItems) * spec.staggerDelayMs.coerceAtLeast(15)) / slowMo.coerceIn(0.15f, 2f)).toLong()
        if (staggerDelay > 0L) {
            delay(staggerDelay)
        }
        isRevealed = true
    }

    val dur = (spec.durationMs.coerceAtLeast(160) / slowMo.coerceIn(0.15f, 2f)).toInt()

    val alphaAnim by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0.04f,
        animationSpec = tween(
            durationMillis = dur,
            easing = FastOutSlowInEasing
        ),
        label = "home_card_alpha_$index"
    )

    val mainProgress by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0f,
        animationSpec = com.example.ui.components.PremiumAnimationEngine.buildFloatAnimationSpec(spec, slowMo),
        label = "home_card_progress_$index"
    )

    this.graphicsLayer {
        alpha = alphaAnim
        val scrollOffset = scrollState?.value?.toFloat() ?: 0f
        val parallaxOffset = scrollOffset * parallaxFactor
        val distPx = spec.translationDistanceDp.dp.toPx()
        val inv = 1f - mainProgress
        val dirSign = if (spec.direction == com.example.model.AnimationDirection.TOP) -1f else 1f
        translationY = (dirSign * distPx * inv) - parallaxOffset
        val startScale = spec.initialScale.coerceIn(0.85f, 1.0f)
        val curScale = startScale + (1f - startScale) * mainProgress
        scaleX = curScale
        scaleY = curScale
        if (spec.entranceType == com.example.model.EntranceAnimationType.FLIP_3D_X ||
            spec.entranceType == com.example.model.EntranceAnimationType.PERSPECTIVE_ZOOM ||
            spec.entranceType == com.example.model.EntranceAnimationType.DEPTH_BASED_ENTRANCE
        ) {
            rotationX = -10f * inv
            cameraDistance = spec.cameraDistance * density
        } else if (spec.entranceType == com.example.model.EntranceAnimationType.FLIP_3D_Y) {
            rotationY = 12f * inv
            cameraDistance = spec.cameraDistance * density
        }
    }
}

@Composable
private fun PremiumDrawerMenuItem(
    icon: Any,
    label: String,
    badgeText: String? = null,
    badgeBgColor: Color = Color(0xFF2563EB),
    badgeTextColor: Color = Color.White,
    iconTint: Color = Color(0xFF2563EB),
    isSelected: Boolean = false,
    isDarkMode: Boolean = false,
    onClick: () -> Unit
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) {
            if (isDarkMode) Color(0xFF1E3A8A).copy(alpha = 0.5f) else Color(0xFFEFF6FF)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "drawerItemBg"
    )

    val labelColor = if (isSelected) {
        if (isDarkMode) Color(0xFF60A5FA) else Color(0xFF1D4ED8)
    } else {
        if (isDarkMode) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = animatedBg,
        border = if (isSelected) BorderStroke(1.dp, if (isDarkMode) Color(0xFF3B82F6).copy(alpha = 0.4f) else Color(0xFFBFDBFE)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF2563EB))
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        if (isSelected) iconTint.copy(alpha = 0.2f)
                        else if (isDarkMode) Color(0xFF1E293B)
                        else iconTint.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (icon is androidx.compose.ui.graphics.vector.ImageVector) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) (if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF2563EB)) else iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = label,
                color = labelColor,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontSize = 13.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (!badgeText.isNullOrEmpty()) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = badgeBgColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeTextColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.3.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumDrawerSectionHeader(
    title: String,
    isDarkMode: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF2563EB))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

private fun checkNetworkOnline(context: android.content.Context): Boolean {
    val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
    if (connectivityManager != null) {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            return capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)
        }
    }
    return false
}

/**
 * Premium "Exam Syllabus & Papers" Card matching reference image:
 * Features:
 * - Rich royal-blue -> purple -> magenta -> pink -> orange -> gold gradient
 * - Multi-color glowing neon border (cyan / blue / magenta / orange / gold)
 * - Inner gloss/sheen arc highlight
 * - "Exam" highlighted in vibrant gold with subtle text depth
 * - "Syllabus & Papers" in crisp, clean white with subtle drop-shadow
 * - Circular right-arrow icon button with clean white border and translucent fill
 * - Touch-friendly ripple interaction and responsive layout
 */
@Composable
fun ExamSyllabusAndPapersCard(
    modifier: Modifier = Modifier
) {
    // Plain premium Soft Gold design; no gradient or decorative overlay.
    val softGold = Color(0xFFEFF2FF)       // Soft Blue background
    val champagneGoldBorder = Color(0xFFD4AF37)
    val darkBrown = Color(0xFF633F2D)
    val bluePinkBackground = Color(0xFFF5E9FF) // Light Pink-Lavender
    val bluePinkBorder = Color(0xFF8B5CF6)  // Premium Purple border
    val bluePinkAccent = Color(0xFFEC4899)  // Luxury Pink accent
    val bluePinkText = Color(0xFF1E1B4B)   // Deep Navy text
    val bluePinkArrow = Color(0xFF3B82F6)  // Blue arrow button

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = softGold,
            border = BorderStroke(1.5.dp, champagneGoldBorder),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .background(softGold)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Exam ",
                    style = TextStyle(
                        color = Color(0xFF9A6700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.2).sp
                    )
                )
                Text(
                    text = "Syllabus & Papers",
                    style = TextStyle(
                        color = darkBrown,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.1).sp
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE5B943),
                    border = BorderStroke(1.2.dp, Color(0xFFB8872B)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open Exam Syllabus and Papers",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Featured Monthly Current Affairs Card for the Home Screen:
 * - Replaces the static Current Affairs subject card on the Home Screen.
 * - Displays dynamic month count and practice set count.
 * - Clean solid peach-accented container with M3 design tokens and Telugu subtitle.
 * - Tapping navigates directly to the Monthly Current Affairs feature.
 */
@Composable
fun FeaturedMonthlyCurrentAffairsCard(
    onNavigateToCurrentAffairs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthlyList = remember { SampleData.getMonthlyCurrentAffairs() }
    val monthCount = monthlyList.size
    val totalSetsCount = remember(monthlyList) { monthlyList.sumOf { it.sets.size } }

    val pressState = rememberCardPressAnimationState(
        targetScale = 0.98f,
        defaultElevation = 2.dp,
        pressedElevation = 1.dp,
        durationMs = 120
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("featured_monthly_current_affairs_card")
            .graphicsLayer {
                scaleX = pressState.scale
                scaleY = pressState.scale
            }
            .clickable(
                interactionSource = pressState.interactionSource,
                indication = null
            ) { onNavigateToCurrentAffairs() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFFFED7AA)),
        elevation = CardDefaults.cardElevation(defaultElevation = pressState.elevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF7ED),
                            Color(0xFFFFFFFF)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header row: Badge + Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEA580C),
                        contentColor = Color.White
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Newspaper,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "MONTHLY CURRENT AFFAIRS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFEDD5),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Monthly Current Affairs",
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Titles
                Text(
                    text = "Monthly Current Affairs Compilations",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Text(
                    text = "మీ అభ్యాసాన్ని నెలావారీ శైలిలో కొనసాగించండి",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0284C7),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stats and CTA Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = "📅 $monthCount Months",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = "📝 $totalSetsCount Sets",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Action Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF2563EB),
                        contentColor = Color.White
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Explore",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}





