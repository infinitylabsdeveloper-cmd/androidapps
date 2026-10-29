package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.SampleData
import com.example.db.AppDatabase
import com.example.model.LegalDocType
import com.example.model.LegalDocument
import com.example.repository.AppRepository
import com.example.ui.components.DailyPracticeReminderDialog
import com.example.ui.components.PracticeAppearancePanel
import com.example.ui.components.ReportIssueDialog
import com.example.ui.components.ShareAppBottomSheet
import com.example.ui.theme.LocalAppConfig
import com.example.util.AppShareManager
import com.example.util.NotificationSettingsManager
import com.example.util.PermissionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.example.ui.theme.LocalWindowSizeClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSavedQuestions: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToPracticeHistory: () -> Unit = {},
    onNavigateToMockTestHistory: () -> Unit = {},
    onNavigateToAppearanceSettings: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = onNavigateToPracticeHistory,
    onNavigateToNotificationSettings: () -> Unit = {},
    onNavigateToContact: () -> Unit = {}
) {
    val config = LocalAppConfig.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Premium Color Palette
    val pinkBackground = Color(0xFFFDF0F4) // Plain, elegant pink background
    val creamCardBg = Color(0xFFFFFDF8)    // Ivory/cream card background
    val champagneGoldBorder = Color(0xFFE6C687) // Champagne gold border
    val burgundyTextPrimary = Color(0xFF581825) // Premium burgundy typography
    val burgundyTextSecondary = Color(0xFF8C3848) // Secondary burgundy text
    val burgundyIconColor = Color(0xFF7A2030)   // Deep burgundy for icons
    val iconCircleBg = Color(0xFFF8E7EC)         // Soft rose cream icon container
    val goldBadgeColor = Color(0xFFD4AF37)       // Gold badge accent
    val profileGradientBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFE11D48), // Rose Red
                Color(0xFF9333EA), // Royal Violet
                Color(0xFF2563EB), // Electric Blue
                Color(0xFF059669), // Emerald Green
                Color(0xFFD97706)  // Gold
            )
        )
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember(config.appId) { AppRepository(database, config.appId) }
    val userProgressRepo = remember(context) { com.example.repository.UserProgressRepository.getInstance(context) }
    
    val realUserProgress by userProgressRepo.getUserProgress(config.appId).collectAsStateWithLifecycle(
        initialValue = com.example.model.UserProgress()
    )
    val perfSummary by userProgressRepo.getPerformanceSummary(config.appId).collectAsStateWithLifecycle(
        initialValue = com.example.model.MockTestPerformanceSummary()
    )

    val allLegalDocs by repository.allLegalDocuments.collectAsStateWithLifecycle()
    val appDocs = remember(allLegalDocs, config.appId) {
        allLegalDocs.filter { it.mappedAppIds.isEmpty() || it.mappedAppIds.contains(config.appId) }
            .groupBy { it.type }
            .map { it.value.first() }
            .sortedBy { it.order }
    }

    var selectedDoc by remember { mutableStateOf<LegalDocument?>(null) }
    val userProfile by SampleData.userProfileState

    // Overlay States
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmationDialog by remember { mutableStateOf(false) }
    var showShareBottomSheet by remember { mutableStateOf(false) }
    var showDailyPracticeReminderDialog by remember { mutableStateOf(false) }
    var showReportIssueDialog by remember { mutableStateOf(false) }
    var showAppearancePanel by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    var showOfflineDownloadsDialog by remember { mutableStateOf(false) }
    var showAccountSecurityDialog by remember { mutableStateOf(false) }

    BackHandler {
        when {
            showAppearancePanel -> showAppearancePanel = false
            selectedDoc != null -> selectedDoc = null
            else -> onBack()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val file = java.io.File(context.filesDir, "profile_pic_${System.currentTimeMillis()}.jpg")
                        java.io.FileOutputStream(file).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        SampleData.userProfileState.value = SampleData.userProfileState.value.copy(
                            profileImageUrl = file.absolutePath
                        )
                        SampleData.saveUserProfile(context)
                        snackbarHostState.showSnackbar("Profile picture updated successfully!")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val isSubscriptionActive = userProfile.expiresAt > System.currentTimeMillis() || userProfile.isProSubscribed

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile & Settings",
                        style = TextStyle(
                            brush = profileGradientBrush,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = burgundyTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = burgundyTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pinkBackground)
            )
        },
        containerColor = pinkBackground
    ) { paddingVals ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
        ) {
            if (showAppearancePanel) {
                val currentSettings = remember { com.example.util.PracticeAppearanceManager.getSettings(context) }
                Box(modifier = Modifier.fillMaxSize()) {
                    PracticeAppearancePanel(
                        currentSettings = currentSettings,
                        onSave = { newSettings ->
                            val currentAdv = com.example.repository.AppConfigRepository.getInstance().getCurrentConfig().advancedFontSettings
                            val updatedAdv = currentAdv.copy(practiceAppearance = newSettings)
                            com.example.util.FontSettingsManager.saveSettings(context, updatedAdv)
                            com.example.util.PracticeAppearanceManager.saveSettings(context, newSettings)
                            val currentConfig = com.example.repository.AppConfigRepository.getInstance().getCurrentConfig()
                            com.example.repository.AppConfigRepository.getInstance().updateAppConfig(
                                currentConfig.copy(advancedFontSettings = updatedAdv)
                            )
                            showAppearancePanel = false
                        },
                        onReset = {
                            com.example.util.PracticeAppearanceManager.resetToDefault(context)
                            val defPa = com.example.model.PracticeAppearanceSettings()
                            val currentAdv = com.example.repository.AppConfigRepository.getInstance().getCurrentConfig().advancedFontSettings
                            val updatedAdv = currentAdv.copy(practiceAppearance = defPa)
                            com.example.util.FontSettingsManager.saveSettings(context, updatedAdv)
                            val currentConfig = com.example.repository.AppConfigRepository.getInstance().getCurrentConfig()
                            com.example.repository.AppConfigRepository.getInstance().updateAppConfig(
                                currentConfig.copy(advancedFontSettings = updatedAdv)
                            )
                            showAppearancePanel = false
                        },
                        onBack = { showAppearancePanel = false }
                    )
                }
            } else if (selectedDoc != null) {
                LegalDocumentViewerWrapper(
                    doc = selectedDoc!!,
                    appName = config.appName,
                    onBack = { selectedDoc = null }
                )
            } else {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { 20 })
                ) {
                    val windowSize = LocalWindowSizeClass.current
                    val isTablet = windowSize == WindowWidthSizeClass.Medium || windowSize == WindowWidthSizeClass.Expanded

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = if (isTablet) 32.dp else 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. PROFILE HEADER CARD
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = creamCardBg),
                                border = BorderStroke(1.2.dp, champagneGoldBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Circular Profile Photo with Gold Border
                                    Box(
                                        modifier = Modifier.size(92.dp),
                                        contentAlignment = Alignment.BottomEnd
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(90.dp),
                                            shape = CircleShape,
                                            color = iconCircleBg,
                                            border = BorderStroke(2.dp, champagneGoldBorder)
                                        ) {
                                            if (!userProfile.profileImageUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = userProfile.profileImageUrl,
                                                    contentDescription = "Profile Picture",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = burgundyTextPrimary,
                                                        modifier = Modifier.size(48.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Camera / Edit Photo Button
                                        Surface(
                                            onClick = {
                                                imagePickerLauncher.launch(
                                                    androidx.activity.result.PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            },
                                            shape = CircleShape,
                                            color = burgundyTextPrimary,
                                            shadowElevation = 3.dp,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.PhotoCamera,
                                                    contentDescription = "Change Photo",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Name
                                    Text(
                                        text = userProfile.name.ifBlank { "Aspirant" },
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = burgundyTextPrimary
                                    )

                                    // Aspirant Field / Target Exam
                                    Text(
                                        text = userProfile.targetExam.ifBlank { "APPSC Group-2 Aspirant" },
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = burgundyTextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Contact details grid/rows
                                    HorizontalDivider(color = champagneGoldBorder.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        ProfileDetailChip(
                                            icon = Icons.Default.Phone,
                                            text = if (userProfile.phone.isNotBlank()) "+91 ${userProfile.phone}" else "Phone not linked",
                                            burgundyTextPrimary = burgundyTextPrimary
                                        )
                                        ProfileDetailChip(
                                            icon = Icons.Default.Email,
                                            text = userProfile.email.ifBlank { "Email not linked" },
                                            burgundyTextPrimary = burgundyTextPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        ProfileDetailChip(
                                            icon = Icons.Default.LocationOn,
                                            text = userProfile.district.ifBlank { "Visakhapatnam" },
                                            burgundyTextPrimary = burgundyTextPrimary
                                        )
                                        ProfileDetailChip(
                                            icon = Icons.Default.Language,
                                            text = userProfile.preferredLanguage,
                                            burgundyTextPrimary = burgundyTextPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Edit Profile Button
                                    OutlinedButton(
                                        onClick = { showEditProfileDialog = true },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.2.dp, champagneGoldBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = iconCircleBg,
                                            contentColor = burgundyTextPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth(0.7f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Edit Profile",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                    }
                                }
                            }
                        }

                        // 2. REDESIGNED PREMIUM MEMBERSHIP CARD
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(18.dp)),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = creamCardBg),
                                border = BorderStroke(1.2.dp, champagneGoldBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = goldBadgeColor,
                                                border = BorderStroke(0.8.dp, champagneGoldBorder)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.WorkspacePremium,
                                                        contentDescription = null,
                                                        tint = burgundyTextPrimary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = if (isSubscriptionActive) "PREMIUM MEMBER" else "FREE PASS",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = burgundyTextPrimary
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = if (isSubscriptionActive) userProfile.proPlanName.ifBlank { "APPSC Group-2 Lifetime Pro" } else "Standard Free Account",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = burgundyTextPrimary
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        val expiryFormatted = remember(userProfile.expiresAt, userProfile.isProSubscribed) {
                                            if (userProfile.expiresAt > 0L) {
                                                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                                "Valid until: ${sdf.format(Date(userProfile.expiresAt))}"
                                            } else if (userProfile.isProSubscribed) {
                                                "Lifetime Validity Active"
                                            } else {
                                                "Upgrade to unlock all practice sets"
                                            }
                                        }

                                        Text(
                                            text = expiryFormatted,
                                            fontSize = 12.sp,
                                            color = burgundyTextSecondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Button(
                                        onClick = onNavigateToSubscription,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = burgundyTextPrimary,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(
                                            text = if (isSubscriptionActive) "Manage" else "Upgrade",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // 3. FOUR NAVIGATION OPTION CARDS
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Option 1: My Progress & Analytics
                                PremiumOptionCard(
                                    icon = Icons.Default.Analytics,
                                    title = "My Progress & Analytics",
                                    subtitle = "Performance, accuracy, time & test analytics",
                                    onClick = onNavigateToPerformance,
                                    creamCardBg = creamCardBg,
                                    champagneGoldBorder = champagneGoldBorder,
                                    burgundyTextPrimary = burgundyTextPrimary,
                                    burgundyTextSecondary = burgundyTextSecondary,
                                    iconCircleBg = iconCircleBg,
                                    burgundyIconColor = burgundyIconColor
                                )
                                // Option 2: Bookmarked Questions
                                PremiumOptionCard(
                                    icon = Icons.Default.Bookmark,
                                    title = "Bookmarked Questions",
                                    subtitle = "Review saved questions & practice items",
                                    onClick = onNavigateToSavedQuestions,
                                    creamCardBg = creamCardBg,
                                    champagneGoldBorder = champagneGoldBorder,
                                    burgundyTextPrimary = burgundyTextPrimary,
                                    burgundyTextSecondary = burgundyTextSecondary,
                                    iconCircleBg = iconCircleBg,
                                    burgundyIconColor = burgundyIconColor
                                )
                                // Option 3: Offline Downloads
                                PremiumOptionCard(
                                    icon = Icons.Default.FolderZip,
                                    title = "Offline Downloads",
                                    subtitle = "Downloaded practice sets & offline content",
                                    onClick = { showOfflineDownloadsDialog = true },
                                    creamCardBg = creamCardBg,
                                    champagneGoldBorder = champagneGoldBorder,
                                    burgundyTextPrimary = burgundyTextPrimary,
                                    burgundyTextSecondary = burgundyTextSecondary,
                                    iconCircleBg = iconCircleBg,
                                    burgundyIconColor = burgundyIconColor
                                )
                                // Option 4: Help & Support
                                PremiumOptionCard(
                                    icon = Icons.Default.SupportAgent,
                                    title = "Help & Support",
                                    subtitle = "FAQ, report issue & contact support",
                                    onClick = onNavigateToContact,
                                    creamCardBg = creamCardBg,
                                    champagneGoldBorder = champagneGoldBorder,
                                    burgundyTextPrimary = burgundyTextPrimary,
                                    burgundyTextSecondary = burgundyTextSecondary,
                                    iconCircleBg = iconCircleBg,
                                    burgundyIconColor = burgundyIconColor
                                )
                            }
                        }

                        // LOGOUT / SESSION BUTTON
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    if (userProfile.isLoggedIn) {
                                        showLogoutConfirmationDialog = true
                                    } else {
                                        onNavigateToLogin()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = creamCardBg,
                                    contentColor = burgundyTextPrimary
                                ),
                                border = BorderStroke(1.2.dp, champagneGoldBorder)
                            ) {
                                Icon(
                                    imageVector = if (userProfile.isLoggedIn) Icons.Default.Logout else Icons.Default.Login,
                                    contentDescription = null,
                                    tint = burgundyTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (userProfile.isLoggedIn) "Sign Out" else "Log In / Register",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = burgundyTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }

    // DIALOG 1: EDIT PROFILE DIALOG
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(userProfile.name) }
        var editPhone by remember { mutableStateOf(userProfile.phone) }
        var editEmail by remember { mutableStateOf(userProfile.email) }
        var editDistrict by remember { mutableStateOf(userProfile.district) }
        var editTargetExam by remember { mutableStateOf(userProfile.targetExam) }
        var editLanguage by remember { mutableStateOf(userProfile.preferredLanguage) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = creamCardBg,
            title = {
                Text(
                    text = "Edit Candidate Profile",
                    fontWeight = FontWeight.Bold,
                    color = burgundyTextPrimary,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name", color = burgundyTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = burgundyTextPrimary,
                            unfocusedBorderColor = champagneGoldBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Mobile Number", color = burgundyTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = burgundyTextPrimary,
                            unfocusedBorderColor = champagneGoldBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email ID", color = burgundyTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = burgundyTextPrimary,
                            unfocusedBorderColor = champagneGoldBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editDistrict,
                        onValueChange = { editDistrict = it },
                        label = { Text("District / Location", color = burgundyTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = burgundyTextPrimary,
                            unfocusedBorderColor = champagneGoldBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editTargetExam,
                        onValueChange = { editTargetExam = it },
                        label = { Text("Exam / Aspirant Field", color = burgundyTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = burgundyTextPrimary,
                            unfocusedBorderColor = champagneGoldBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        SampleData.userProfileState.value = SampleData.userProfileState.value.copy(
                            name = editName,
                            phone = editPhone,
                            email = editEmail,
                            district = editDistrict,
                            targetExam = editTargetExam,
                            preferredLanguage = editLanguage
                        )
                        SampleData.saveUserProfile(context)
                        showEditProfileDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Profile details saved persistently!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = burgundyTextPrimary)
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = burgundyTextSecondary)
                }
            }
        )
    }

    // DIALOG 2: ACHIEVEMENTS & BADGES
    if (showAchievementsDialog) {
        AlertDialog(
            onDismissRequest = { showAchievementsDialog = false },
            containerColor = creamCardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = goldBadgeColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Achievements & Badges",
                        fontWeight = FontWeight.Bold,
                        color = burgundyTextPrimary,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val effectiveStreak = realUserProgress.streakDays.coerceAtLeast(userProfile.streakDays)
                    val totalSolved = realUserProgress.completedQuestionsCount + perfSummary.totalQuestionsAttempted
                    AchievementBadgeRow(
                        title = "Practice Starter",
                        desc = if (totalSolved > 0) "Solved $totalSolved practice MCQs" else "Attempt your first practice set to unlock",
                        isUnlocked = totalSolved > 0 || realUserProgress.testsCompletedCount > 0,
                        burgundyTextPrimary = burgundyTextPrimary
                    )
                    AchievementBadgeRow(
                        title = "Streak Champion",
                        desc = "Maintained ${effectiveStreak}-day practice streak",
                        isUnlocked = effectiveStreak > 0,
                        burgundyTextPrimary = burgundyTextPrimary
                    )
                    AchievementBadgeRow(
                        title = "Mastery Achiever",
                        desc = "${realUserProgress.completedTopicIds.size} topics completed",
                        isUnlocked = realUserProgress.completedTopicIds.isNotEmpty(),
                        burgundyTextPrimary = burgundyTextPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAchievementsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = burgundyTextPrimary)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // DIALOG 3: OFFLINE DOWNLOADS
    if (showOfflineDownloadsDialog) {
        AlertDialog(
            onDismissRequest = { showOfflineDownloadsDialog = false },
            containerColor = creamCardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = null,
                        tint = burgundyTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Offline Practice Downloads",
                        fontWeight = FontWeight.Bold,
                        color = burgundyTextPrimary,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "All downloaded practice sets are saved locally for offline preparation without active internet.",
                        fontSize = 12.5.sp,
                        color = burgundyTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val appPacks = com.example.util.OfflineSyncManager.downloadedPacks.filter {
                        it.appId == config.appId || it.appId.isBlank()
                    }
                    val totalOfflineQs = appPacks.sumOf { it.questionCount }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = iconCircleBg,
                        border = BorderStroke(1.dp, champagneGoldBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${config.appName} Offline Cache", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = burgundyTextPrimary)
                                Text("${appPacks.size} Packs • $totalOfflineQs Practice Questions", fontSize = 11.5.sp, color = burgundyTextSecondary)
                            }
                            Text(if (appPacks.isNotEmpty()) "READY" else "EMPTY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (appPacks.isNotEmpty()) Color(0xFF16A34A) else burgundyTextSecondary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showOfflineDownloadsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = burgundyTextPrimary)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // DIALOG 4: ACCOUNT & SECURITY
    if (showAccountSecurityDialog) {
        AlertDialog(
            onDismissRequest = { showAccountSecurityDialog = false },
            containerColor = creamCardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = burgundyTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Account & Security",
                        fontWeight = FontWeight.Bold,
                        color = burgundyTextPrimary,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SecurityDetailRow("Mobile Verification", if (userProfile.isPhoneVerified) "VERIFIED" else "PENDING", burgundyTextPrimary)
                    SecurityDetailRow("Email Verification", if (userProfile.isEmailVerified) "VERIFIED" else "PENDING", burgundyTextPrimary)
                    SecurityDetailRow("Account Status", "ACTIVE CANDIDATE", burgundyTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            showAccountSecurityDialog = false
                            onNavigateToPrivacy()
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, champagneGoldBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Privacy Policy & Data Policy", fontSize = 12.sp, color = burgundyTextPrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAccountSecurityDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = burgundyTextPrimary)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // DIALOG 5: LOGOUT CONFIRMATION DIALOG
    if (showLogoutConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmationDialog = false },
            containerColor = creamCardBg,
            title = {
                Text(
                    text = "Sign Out Confirmation",
                    fontWeight = FontWeight.Bold,
                    color = burgundyTextPrimary,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of your APPSC Group-2 candidate profile?",
                    fontSize = 13.5.sp,
                    color = burgundyTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        SampleData.logout()
                        showLogoutConfirmationDialog = false
                        onNavigateToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B))
                ) {
                    Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmationDialog = false }) {
                    Text("Cancel", color = burgundyTextSecondary)
                }
            }
        )
    }

    // DIALOG 6: DAILY PRACTICE REMINDER DIALOG
    if (showDailyPracticeReminderDialog) {
        DailyPracticeReminderDialog(
            onDismissRequest = { showDailyPracticeReminderDialog = false }
        )
    }
}

@Composable
private fun ProfileDetailChip(
    icon: ImageVector,
    text: String,
    burgundyTextPrimary: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = burgundyTextPrimary,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 11.5.sp,
            color = burgundyTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PremiumOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    creamCardBg: Color,
    champagneGoldBorder: Color,
    burgundyTextPrimary: Color,
    burgundyTextSecondary: Color,
    iconCircleBg: Color,
    burgundyIconColor: Color
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = creamCardBg),
        border = BorderStroke(1.2.dp, champagneGoldBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconCircleBg,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = burgundyIconColor,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = burgundyTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = burgundyTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = burgundyTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AchievementBadgeRow(
    title: String,
    desc: String,
    isUnlocked: Boolean,
    burgundyTextPrimary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
            contentDescription = null,
            tint = if (isUnlocked) Color(0xFF16A34A) else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = burgundyTextPrimary)
            Text(desc, fontSize = 11.5.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun SecurityDetailRow(
    label: String,
    status: String,
    burgundyTextPrimary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = burgundyTextPrimary)
        Text(status, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalDocumentViewerWrapper(
    doc: LegalDocument,
    appName: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF0F4))
    ) {
        TopAppBar(
            title = { Text(doc.title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = doc.content,
                fontSize = 13.5.sp,
                color = Color(0xFF581825),
                lineHeight = 20.sp
            )
        }
    }
}
