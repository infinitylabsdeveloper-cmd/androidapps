package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SampleData
import com.example.db.AppDatabase
import com.example.model.LegalDocType
import com.example.model.LegalDocument
import com.example.model.SupportFaq
import com.example.repository.AppRepository
import com.example.ui.theme.LocalAppConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data Classes for Sectioned Documents and Social Configuration
data class DocSection(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val order: Int = 0
)

data class AdminContactSettings(
    val supportEmail: String? = null,
    val supportPhone: String? = null,
    val whatsappNumber: String? = null,
    val whatsappLink: String? = null,
    val telegramId: String? = null,
    val telegramLink: String? = null,
    val supportWebsite: String? = null,
    val socialLinks: List<SocialLink> = emptyList()
)

data class SocialLink(
    val platform: String,
    val url: String,
    val value: String
)

/**
 * Normalizes section keys into specific LegalDocType.
 */
fun parseSection(section: String?): LegalDocType? {
    if (section.isNullOrBlank()) return null
    val s = section.trim().lowercase()
    return when {
        s == "faq" || s == "faqs" || s.contains("faq") -> LegalDocType.FAQ
        s == "privacy" || s.contains("privacy") -> LegalDocType.PRIVACY_POLICY
        s == "terms" || s.contains("terms") || s.contains("condition") -> LegalDocType.TERMS_CONDITIONS
        s == "refund" || s.contains("refund") || s.contains("cancel") -> LegalDocType.REFUND_CANCELLATION
        s == "about" || s.contains("about") -> LegalDocType.ABOUT_US
        s == "contact" || s.contains("contact") -> LegalDocType.CONTACT_US
        s == "help" || s.contains("help") -> LegalDocType.HELP_CENTER
        s == "deletion" || s.contains("delete") -> LegalDocType.ACCOUNT_DELETION
        s.contains("subscription") -> LegalDocType.SUBSCRIPTION_TERMS
        s.contains("disclaimer") -> LegalDocType.DISCLAIMER
        s.contains("copyright") -> LegalDocType.COPYRIGHT_POLICY
        s.contains("report") -> LegalDocType.REPORTING_POLICY
        s.contains("shipping") || s.contains("delivery") -> LegalDocType.SHIPPING_DELIVERY
        else -> LegalDocType.values().find { it.id.equals(s, ignoreCase = true) || it.name.equals(s, ignoreCase = true) }
    }
}

// Helpers for color parsing and iconography
fun parseColorHex(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

fun getIconForName(iconName: String?): ImageVector {
    return when (iconName?.lowercase()?.trim()) {
        "shield", "privacy", "security" -> Icons.Default.Shield
        "assignment", "terms", "rules" -> Icons.Default.Assignment
        "currencyexchange", "refund", "card", "payment" -> Icons.Default.CurrencyExchange
        "star", "subscription", "premium" -> Icons.Default.Star
        "delete", "remove", "trash", "danger" -> Icons.Default.Delete
        "info", "disclaimer", "about" -> Icons.Default.Info
        "copyright", "patent" -> Icons.Default.Copyright
        "flag", "report" -> Icons.Default.Flag
        "contactsupport", "contact", "support", "helpdesk" -> Icons.Default.ContactSupport
        "quiz", "faq", "question" -> Icons.Default.Quiz
        "help", "guide", "manual" -> Icons.Default.Help
        "business", "company", "office" -> Icons.Default.Business
        "email", "mail" -> Icons.Default.Email
        "phone", "call" -> Icons.Default.Phone
        "chat", "whatsapp" -> Icons.Default.Chat
        "send", "telegram" -> Icons.Default.Send
        "language", "website", "web" -> Icons.Default.Language
        "gavel", "legal" -> Icons.Default.Gavel
        "account", "profile", "user" -> Icons.Default.AccountCircle
        "history" -> Icons.Default.History
        "notifications", "bell" -> Icons.Default.Notifications
        "settings", "gear" -> Icons.Default.Settings
        else -> Icons.Default.Info
    }
}

fun getIconForType(type: LegalDocType): ImageVector {
    return when (type) {
        LegalDocType.PRIVACY_POLICY -> Icons.Default.Shield
        LegalDocType.TERMS_CONDITIONS -> Icons.Default.Assignment
        LegalDocType.REFUND_CANCELLATION -> Icons.Default.CurrencyExchange
        LegalDocType.SUBSCRIPTION_TERMS -> Icons.Default.Star
        LegalDocType.ACCOUNT_DELETION -> Icons.Default.Delete
        LegalDocType.DISCLAIMER -> Icons.Default.Info
        LegalDocType.COPYRIGHT_POLICY -> Icons.Default.Copyright
        LegalDocType.REPORTING_POLICY -> Icons.Default.Flag
        LegalDocType.CONTACT_US -> Icons.Default.ContactSupport
        LegalDocType.FAQ -> Icons.Default.Quiz
        LegalDocType.HELP_CENTER -> Icons.Default.Help
        LegalDocType.ABOUT_US -> Icons.Default.Business
        LegalDocType.SHIPPING_DELIVERY -> Icons.Default.LocalShipping
    }
}

// Light theme semantic system
fun getScreenAccentColor(type: LegalDocType?): Color {
    if (type == null) return Color(0xFF2563EB) // Blue for Hub
    return when (type) {
        LegalDocType.PRIVACY_POLICY -> Color(0xFF059669) // Green/Teal
        LegalDocType.TERMS_CONDITIONS -> Color(0xFFD97706) // Orange/Amber
        LegalDocType.REFUND_CANCELLATION -> Color(0xFF2563EB) // Blue
        LegalDocType.FAQ -> Color(0xFF7C3AED) // Purple
        LegalDocType.ABOUT_US -> Color(0xFF4F46E5) // Indigo
        LegalDocType.CONTACT_US -> Color(0xFFE11D48) // Crimson Red
        LegalDocType.HELP_CENTER -> Color(0xFF0D9488) // Teal
        LegalDocType.ACCOUNT_DELETION -> Color(0xFFDC2626) // Dangerous Red
        else -> Color(0xFF2563EB)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalSupportCenterScreen(
    initialSection: String? = null,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {}
) {
    val config = LocalAppConfig.current
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember(config.appId) { AppRepository(database, config.appId) }
    val uriHandler = LocalUriHandler.current

    val allDocs by repository.allLegalDocuments.collectAsStateWithLifecycle()
    val allFaqs by repository.allFaqs.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        com.example.util.FirebaseSyncManager.fetchAndSyncLegalSupport(repository) { success ->
            android.util.Log.d("PrivacyTermsScreen", "Firestore LegalSupport Sync success=$success")
        }
    }

    // Filter documents and FAQs for the active app
    val appDocs = remember(allDocs, config.appId) {
        allDocs.filter { it.isEnabled && (it.mappedAppIds.isEmpty() || it.mappedAppIds.contains(config.appId)) }
    }
    val appFaqs = remember(allFaqs, config.appId) {
        val filtered = allFaqs.filter { it.isEnabled && (it.mappedAppIds.isEmpty() || it.mappedAppIds.contains(config.appId)) }
        if (filtered.isNotEmpty()) filtered else SampleData.getDefaultFaqs()
    }

    var currentSection by remember(initialSection) {
        mutableStateOf<LegalDocType?>(parseSection(initialSection))
    }
    var showDeletionDialog by remember { mutableStateOf(false) }

    val activeDoc = remember(currentSection, appDocs) {
        if (currentSection == null) null
        else appDocs.find { it.type == currentSection }
            ?: SampleData.getDefaultLegalDocs().find { it.type == currentSection }
    }

    val accentColor = getScreenAccentColor(currentSection)

    val handleBack: () -> Unit = {
        if (initialSection != null) {
            onBack()
        } else if (currentSection != null) {
            currentSection = null
        } else {
            onBack()
        }
    }

    BackHandler {
        handleBack()
    }

    val currentTitle = when (currentSection) {
        LegalDocType.FAQ -> "Frequently Asked Questions"
        LegalDocType.PRIVACY_POLICY -> "Privacy Policy"
        LegalDocType.TERMS_CONDITIONS -> "Terms & Conditions"
        LegalDocType.REFUND_CANCELLATION -> "Refund & Cancellation"
        LegalDocType.ABOUT_US -> "About Us"
        LegalDocType.CONTACT_US -> "Contact Us"
        LegalDocType.HELP_CENTER -> "Help Centre"
        LegalDocType.ACCOUNT_DELETION -> "Account & Data Deletion"
        LegalDocType.SUBSCRIPTION_TERMS -> "Subscription Terms"
        LegalDocType.DISCLAIMER -> "Disclaimer"
        LegalDocType.COPYRIGHT_POLICY -> "Copyright Policy"
        LegalDocType.REPORTING_POLICY -> "Question Reporting Policy"
        LegalDocType.SHIPPING_DELIVERY -> "Shipping & Digital Delivery Policy"
        null -> "${config.appName.ifBlank { "Official" }} Help & Support"
    }

    if (currentSection == LegalDocType.CONTACT_US) {
        ContactUsScreen(
            onBack = handleBack,
            onNavigateToFaq = { currentSection = LegalDocType.FAQ },
            onNavigateToHelpCenter = { currentSection = LegalDocType.HELP_CENTER }
        )
        return
    }

    val goldAccent = Color(0xFFB45309)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        val headerIcon = activeDoc?.iconName?.let { getIconForName(it) } ?: currentSection?.let { getIconForType(it) } ?: Icons.Default.Support
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEF3C7))
                                .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = headerIcon,
                                contentDescription = null,
                                tint = goldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentTitle,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E293B),
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${config.appName.ifBlank { "Exam Prep" }} • Official Support & Legal",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9A7B2C),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        IconButton(
                            onClick = handleBack,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF334155)
                            )
                        }
                        IconButton(
                            onClick = onNavigateToHome,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = goldAccent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFFDF9),
                    scrolledContainerColor = Color(0xFFFFFDF9)
                ),
                modifier = Modifier.shadow(2.dp)
            )
        },
        containerColor = Color(0xFFFAF8F3)
    ) { paddingVals ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFDF9),
                            Color(0xFFFAF7F0),
                            Color(0xFFF6F1E6)
                        )
                    )
                )
                .padding(paddingVals)
        ) {
            AnimatedContent(
                targetState = currentSection,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "LegalSupportTransition"
            ) { targetSection ->
                when (targetSection) {
                    LegalDocType.FAQ -> {
                        DedicatedFaqView(
                            faqs = appFaqs,
                            accentColor = goldAccent,
                            onNavigateToContact = { currentSection = LegalDocType.CONTACT_US }
                        )
                    }
                    LegalDocType.CONTACT_US -> {
                        ContactUsScreen(
                            onBack = handleBack,
                            onNavigateToFaq = { currentSection = LegalDocType.FAQ },
                            onNavigateToHelpCenter = { currentSection = LegalDocType.HELP_CENTER }
                        )
                    }
                    LegalDocType.HELP_CENTER -> {
                        DedicatedHelpCenterView(
                            config = config,
                            doc = activeDoc,
                            onNavigateToFaqs = { currentSection = LegalDocType.FAQ },
                            onNavigateToContact = { currentSection = LegalDocType.CONTACT_US },
                            accentColor = goldAccent
                        )
                    }
                    LegalDocType.ACCOUNT_DELETION -> {
                        DedicatedAccountDeletionView(
                            config = config,
                            doc = activeDoc,
                            onDeleteClick = { showDeletionDialog = true },
                            onWebPortalClick = { uriHandler.openUri("https://t.me/DHANARAJU0") },
                            accentColor = goldAccent
                        )
                    }
                    LegalDocType.ABOUT_US -> {
                        DedicatedAboutUsView(
                            config = config,
                            doc = activeDoc,
                            accentColor = goldAccent
                        )
                    }
                    null -> {
                        LegalSupportHubView(
                            appDocs = appDocs,
                            onSelectItem = { selectedType -> currentSection = selectedType }
                        )
                    }
                    else -> {
                        val targetDoc = activeDoc ?: LegalDocument(
                            type = targetSection ?: LegalDocType.PRIVACY_POLICY,
                            title = currentTitle,
                            content = "Content is currently being updated. Please check back shortly.",
                            mappedAppIds = emptyList()
                        )

                        DedicatedLegalDocumentView(
                            doc = targetDoc,
                            appName = config.appName,
                            accentColor = goldAccent,
                            onNavigateToContact = { currentSection = LegalDocType.CONTACT_US }
                        )
                    }
                }
            }
        }
    }

    if (showDeletionDialog) {
        DeletionRequestDialog(
            accentColor = goldAccent,
            onDismiss = { showDeletionDialog = false },
            onConfirm = {
                showDeletionDialog = false
                Toast.makeText(context, "Your account deletion request has been submitted successfully.", Toast.LENGTH_LONG).show()
                onBack()
            }
        )
    }
}

@Composable
private fun DedicatedLegalDocumentView(
    doc: LegalDocument,
    appName: String,
    accentColor: Color,
    onNavigateToContact: () -> Unit
) {
    val scrollState = rememberScrollState()
    val resolvedAppName = appName.ifBlank { "Official Exam Prep" }
    val processedContent = remember(doc.content, resolvedAppName) {
        doc.content.replace("{{APP_NAME}}", resolvedAppName)
            .replace("{{COMPANY_NAME}}", "$resolvedAppName Academic Systems")
    }

    val sections = remember(doc.sectionsJson) {
        if (!doc.sectionsJson.isNullOrBlank()) {
            try {
                val typeToken = object : TypeToken<List<DocSection>>() {}.type
                Gson().fromJson<List<DocSection>>(doc.sectionsJson, typeToken).sortedBy { it.order }
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()
    }

    // Pre-defined default detailed sections for core policies if database sections are empty
    val fallbackSections = remember(doc.type) {
        when (doc.type) {
            LegalDocType.REFUND_CANCELLATION -> listOf(
                DocSection("ref_1", "1. Strict 24-Hour Review Window", "We understand payment slip-ups happen. We grant a brief 24-hour review period for unintended multiple transactions or subscription plan mistakes."),
                DocSection("ref_2", "2. Accidental Double Charges", "If you are charged twice due to system latency or bank gateway delays, submit both transaction IDs within 24 hours for a full manual refund of the duplicate fee."),
                DocSection("ref_3", "3. No Refund Post Access Consumption", "Once a premium mock test is unlocked, answers submitted, or preparation sets downloaded, we cannot offer any refund due to digital content access."),
                DocSection("ref_4", "4. Cancellation of Active Subscriptions", "You can cancel your subscription plan renewal at any moment. Your premium features will stay active until the current billing cycle expires."),
                DocSection("ref_5", "5. Refund Gateway & Processing Cycle", "All authorized refunds are processed through Razorpay back into the source banking account. This generally takes 5-7 business working days."),
                DocSection("ref_6", "6. Fraudulent Dispute Submissions", "Initiating false payment chargebacks or chargeback fraud on genuine active subscriptions will lead to automatic, instant candidate profile bans."),
                DocSection("ref_7", "7. Dedicated Billing Support Helpdesk", "Need clarification regarding a billing deduction? Reach out directly to our official candidate support desk at sobhadhanaraju0@gmail.com or WhatsApp +91 94935 12768.")
            )
            LegalDocType.PRIVACY_POLICY -> listOf(
                DocSection("priv_1", "1. Information We Collect", "We gather specific data including your display name, registered email address, mobile number, and chosen state/exam filters to set up your account profile."),
                DocSection("priv_2", "2. Academic Performance Tracking", "To generate premium mock insights, dynamic score grids, and daily streaks, we record and store your answered question histories."),
                DocSection("priv_3", "3. Device Logs & Unique ID", "To prevent subscription abuse (such as sharing login details with unentitled users), we lock registration logs to your primary active device ID."),
                DocSection("priv_4", "4. Bank-Grade Encryption Security", "All data transfer between your smartphone and our cloud databases is shielded using AES-256 and HTTPS secure network protocols."),
                DocSection("priv_5", "5. No Third-Party Selling or Trading", "We adhere to a strict policy: we never sell, rent, lease, or trade your personal email, profile details, or phone contact lists to advertising brokers."),
                DocSection("priv_6", "6. System Cookie Storage usage", "We utilize small secure session keys local to your device to preserve login states and specific dark/light theme choices."),
                DocSection("priv_7", "7. Your Access & Rectification Rights", "You retain absolute control to view, correct, update, export, or permanently erase your user credentials directly from our profile control panel."),
                DocSection("priv_8", "8. Specific Data Retention Guidelines", "We retain mock performance databases as long as your candidate account remains registered and active to keep analytics charts accurate."),
                DocSection("priv_9", "9. Safety of Candidate Children", "Our educational materials are engineered exclusively for adult candidates preparing for governmental recruitment examinations."),
                DocSection("priv_10", "10. Major Policy Revision Notifications", "Whenever we revise privacy steps, we notify you transparently via high-priority push messages or dynamic main screen pop-up notifications."),
                DocSection("priv_11", "11. Dedicated Privacy Support Desk", "For privacy inquiries or data assistance, reach our official support desk at sobhadhanaraju0@gmail.com or WhatsApp +91 94935 12768.")
            )
            LegalDocType.TERMS_CONDITIONS -> listOf(
                DocSection("t_1", "1. Binding Agreement Acceptance", "By downloading, registering, or using $resolvedAppName, you agree to comply with and be bound by these legal, governing terms."),
                DocSection("t_2", "2. Single Candidate User License", "We grant you an individual, non-exclusive, non-transferable license to access premium exam questions strictly for personal preparation."),
                DocSection("t_3", "3. Candidate Account Security", "You are fully responsible for guarding your mobile OTPs and login details. Do not share your credentials with anyone."),
                DocSection("t_4", "4. Intellectual Property Rights", "The entire bank of MCQs, mock test structures, subject descriptions, explanations, and visual icons are protected intellectual property."),
                DocSection("t_5", "5. Billing Gateways & Paid Plans", "Subscription charges are managed safely through encrypted third-party processors. Active plans are non-refundable."),
                DocSection("t_6", "6. Play Store Auto-Renewal Cycle", "Auto-recurring packages renew automatically at the end of the term. You can disable auto-renewal directly inside the Google Play account."),
                DocSection("t_7", "7. Strictly Prohibited Platform Usage", "Redistributing, scraping, screen-recording, or publishing our proprietary question banks onto other websites will trigger legal notices."),
                DocSection("t_8", "8. Accuracy & Material 'As-Is' Warranty", "Our teachers verify questions with extreme care. However, exam materials are delivered 'as-is' without legal warranties of absolute success."),
                DocSection("t_9", "9. Uptime & Server Maintenance Windows", "We aim for 99.9% server availability. Regular system security checks are scheduled during off-peak hours with brief prior notifications."),
                DocSection("t_10", "10. Comprehensive Liability Limitations", "$resolvedAppName is not liable for mock score fluctuations, exam results, or smartphone hardware performance compatibility limitations."),
                DocSection("t_11", "11. Terms Breach Account Termination", "Violating intellectual property agreements or attempting system attacks will result in immediate profile bans with no refund of active plans."),
                DocSection("t_12", "12. Governing Jurisdiction & Regulations", "These terms are interpreted and governed under state and federal educational guidelines and laws of the country."),
                DocSection("t_13", "13. Scheduled Amendments & Variations", "We periodically update and vary terms to support new exam features. Continuous usage of the app implies agreement to updated terms.")
            )
            LegalDocType.SHIPPING_DELIVERY -> listOf(
                DocSection("ship_1", "1. 100% Digital Educational Service", "APPSC Group 2 Exam Prep Pro (operated by SOBHA DHANARAJU, MID: T89U1OXHFIQbHa) provides exclusively digital study material, topic-wise MCQs, and online mock tests inside the mobile application. No physical books or tangible items are shipped."),
                DocSection("ship_2", "2. Instant In-App Fulfilment (0–5 Minutes)", "Upon payment verification via Razorpay (UPI, Cards, NetBanking, Wallets) or Google Play Billing, your premium subscription pass is unlocked instantaneously on your account."),
                DocSection("ship_3", "3. Shipping & Handling Charges (₹0 Nil)", "Because all preparation sets and mock exams are delivered digitally over the internet inside the Android app, shipping and handling charges are ₹0 across all subscription plans."),
                DocSection("ship_4", "4. Delayed Activation Support", "If your payment is debited and your pass is not unlocked within 15 minutes due to a banking network interruption, please email sobhadhanaraju0@gmail.com with your Razorpay Payment ID for priority activation within 2 hours.")
            )
            else -> emptyList()
        }
    }

    val activeSections = if (sections.isNotEmpty()) sections else fallbackSections

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Document Meta Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = doc.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = "OFFICIAL POLICY",
                            fontSize = 9.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val updatedDate = if (doc.lastUpdated > 0) {
                    SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(doc.lastUpdated))
                } else "15 March 2026"

                Text(
                    text = "Last Updated: $updatedDate",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Quick Summary Box
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = accentColor.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Quick Summary",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (doc.type) {
                            LegalDocType.REFUND_CANCELLATION -> "Digital packages (Mock Tests & Current Affairs) are activated instantly and are non-refundable. Exception reviews are strictly processed for double transactions within 24 hours."
                            LegalDocType.PRIVACY_POLICY -> "Your data security is our top priority. We encrypt progress reports, study preferences, and contact metrics. We never sell your personal information to third-party brokers."
                            LegalDocType.TERMS_CONDITIONS -> "Your access to Infinity mocks and notes constitutes a binding agreement. redistribution or bulk scraping of our exam databases is strictly illegal."
                            LegalDocType.SHIPPING_DELIVERY -> "100% digital educational subscription operated by SOBHA DHANARAJU. Instant 0–5 minute in-app activation upon Razorpay payment confirmation with ₹0 physical shipping."
                            else -> "Our policies are designed to maintain academic integrity, transparent billing, and user security. If you have inquiries, write to support."
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Sectioned Content Cards
        if (activeSections.isNotEmpty()) {
            activeSections.forEach { section ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = section.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = section.content.replace("{{APP_NAME}}", "Infinity"),
                            fontSize = 13.sp,
                            color = Color(0xFF334155),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        } else {
            // Raw Fallback content card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = processedContent,
                        fontSize = 13.sp,
                        color = Color(0xFF334155),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Bottom "Need Help" Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Still have questions?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Our candidate care team is available 24/7 to resolve billing, technical, or content inquiries.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onNavigateToContact,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.ContactSupport, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Contact Support Hub", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DedicatedFaqView(
    faqs: List<SupportFaq>,
    accentColor: Color,
    onNavigateToContact: () -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedIssueDialog by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Account", "Subscription", "Payments", "Mock Tests", "Current Affairs", "Technical", "Refunds")

    val defaultFaqs = remember {
        listOf(
            SupportFaq("df_1", "Can I log in on multiple devices?", "No. To protect candidate account integrity and prevent subscription leakage, licenses are strictly restricted to one active smartphone device at any given time.", order = 1),
            SupportFaq("df_2", "How do I reset my account or resolve OTP issues?", "Ensure your mobile number has active SMS network reception. If OTP is delayed, tap 'Resend OTP' or tap 'Contact Support' to message our WhatsApp support desk (+91 94935 12768).", order = 2),
            SupportFaq("df_3", "Are there free trial mock tests available?", "Yes! We provide free full-length mock tests, subject-wise practice sets, and daily current affairs digests so you can experience the platform before upgrading.", order = 3),
            SupportFaq("df_4", "How do I upgrade to the PRO plan?", "Tap on the Subscription card on your dashboard or Profile screen, choose your preferred validity plan, and complete payment via UPI, Card, or Google Play.", order = 4),
            SupportFaq("df_5", "What secure payment gateways are supported?", "We support UPI (Google Pay, PhonePe, Paytm, BHIM), Credit/Debit cards, Net Banking via Razorpay, and Google Play Billing.", order = 5),
            SupportFaq("df_6", "Payment was debited but subscription is not unlocked.", "Tap the WhatsApp Support card (+91 94935 12768) or email sobhadhanaraju0@gmail.com with your UPI Reference / Transaction ID for priority manual activation.", order = 6),
            SupportFaq("df_7", "How do I report an error in an MCQ or Mock Test?", "Inside any practice or mock test screen, tap the Flag icon on the question card to submit a report to our academic verification team or message us on WhatsApp.", order = 7),
            SupportFaq("df_8", "Can I pause an active mock test and resume later?", "Yes! Practice and mock sessions auto-save locally so you can resume right where you left off.", order = 8),
            SupportFaq("df_9", "What is the ₹99 Offline Subscription Plan and how does it work?", "The ₹99 Offline Pass allows you to download and practice MCQs offline without an internet connection. Once active, you can access downloaded content through Sets, Subtopics, and Downloaded Materials anytime.", order = 9),
            SupportFaq("df_10", "What is the validity period of the ₹99 Offline Pass?", "The validity period of your ₹99 Offline Pass automatically aligns with your existing active subscription plan duration.", order = 10),
            SupportFaq("df_11", "What happens when the ₹99 Offline Pass expires?", "Upon expiry of your offline pass, offline access to downloaded materials is paused until renewal. However, you will still be able to practice MCQs online anytime for free.", order = 11)
        )
    }

    val activeFaqs = if (faqs.isNotEmpty()) faqs else defaultFaqs

    val filteredFaqs = remember(activeFaqs, searchQuery, selectedCategory) {
        activeFaqs.filter {
            val matchesSearch = it.question.contains(searchQuery, ignoreCase = true) ||
                    it.answer.contains(searchQuery, ignoreCase = true)
            val matchesCat = if (selectedCategory == "All") true else {
                when (selectedCategory) {
                    "Account" -> it.question.contains("account", ignoreCase = true) || it.question.contains("login", ignoreCase = true) || it.question.contains("otp", ignoreCase = true) || it.question.contains("device", ignoreCase = true)
                    "Subscription" -> it.question.contains("subscription", ignoreCase = true) || it.question.contains("pro", ignoreCase = true) || it.question.contains("trial", ignoreCase = true) || it.question.contains("upgrade", ignoreCase = true)
                    "Payments" -> it.question.contains("payment", ignoreCase = true) || it.question.contains("gateway", ignoreCase = true) || it.question.contains("debited", ignoreCase = true)
                    "Mock Tests" -> it.question.contains("mock", ignoreCase = true) || it.question.contains("mcq", ignoreCase = true) || it.question.contains("resume", ignoreCase = true)
                    "Current Affairs" -> it.question.contains("current", ignoreCase = true) || it.question.contains("affairs", ignoreCase = true)
                    "Refunds" -> it.question.contains("refund", ignoreCase = true) || it.question.contains("debited", ignoreCase = true)
                    else -> true
                }
            }
            matchesSearch && matchesCat
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Luxury Header Banner
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFFDF9),
                border = BorderStroke(1.2.dp, Color(0xFFD4AF37).copy(alpha = 0.55f)),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFFBEB),
                                    Color(0xFFFFFDF9),
                                    Color(0xFFFDF8EC)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF3C7))
                                .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Frequently Asked Questions",
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Instant answers for aspirants • Verified Help Center",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9A7B2C)
                            )
                        }
                    }
                }
            }
        }

        // Search Input Card
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(14.dp)),
                placeholder = { Text("Search frequently asked questions...", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFB45309)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF64748B))
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFC59B27),
                    unfocusedBorderColor = Color(0xFFE8DCC4),
                    focusedContainerColor = Color(0xFFFFFDF9),
                    unfocusedContainerColor = Color(0xFFFFFDF9),
                    focusedTextColor = Color(0xFF1E293B),
                    unfocusedTextColor = Color(0xFF1E293B)
                ),
                singleLine = true
            )
        }

        // Horizontal Category Chips
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEF3C7),
                            selectedLabelColor = Color(0xFF92400E),
                            containerColor = Color(0xFFFFFDF9),
                            labelColor = Color(0xFF475569)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color(0xFFE8DCC4),
                            selectedBorderColor = Color(0xFFD4AF37)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp)
                    )
                }
            }
        }

        // FAQs Results List
        if (filteredFaqs.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFFDF9),
                    border = BorderStroke(1.dp, Color(0xFFE8DCC4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, tint = Color(0xFFC59B27), modifier = Modifier.size(42.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No matching FAQs found", color = Color(0xFF1E293B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Connect directly with our candidate support team on WhatsApp or Email.", color = Color(0xFF64748B), fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(filteredFaqs) { faq ->
                FaqAccordionItem(faq, accentColor = Color(0xFFB45309))
            }
        }

        // Direct Contact Channels Card inside FAQ
        item {
            LuxuryDirectChannelsStrip(
                onEmailClick = {
                    com.example.util.ContactSupportManager.openEmailSupport(
                        context = context,
                        issueCategory = "FAQ & General Help Inquiry"
                    )
                },
                onWhatsAppClick = {
                    com.example.util.ContactSupportManager.openWhatsAppSupport(
                        context = context,
                        issueCategory = "FAQ & General Help Inquiry"
                    )
                },
                onTelegramClick = {
                    com.example.util.ContactSupportManager.openTelegramSupport(context = context)
                },
                onOpenContactHub = onNavigateToContact
            )
        }
    }

    if (selectedIssueDialog != null) {
        SmartSupportIssueDialog(
            issueCategory = selectedIssueDialog!!,
            onDismiss = { selectedIssueDialog = null }
        )
    }
}

@Composable
private fun DedicatedContactView(
    config: com.example.model.AppConfiguration,
    doc: LegalDocument?,
    accentColor: Color
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // Parse contact channels from admin if exists
    val adminSettings = remember(doc?.socialLinksJson) {
        if (!doc?.socialLinksJson.isNullOrBlank()) {
            try {
                Gson().fromJson(doc.socialLinksJson, AdminContactSettings::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val email = adminSettings?.supportEmail?.ifBlank { null } ?: config.supportEmail
    val phone = adminSettings?.supportPhone?.ifBlank { null } ?: config.supportPhone
    val whatsappVal = adminSettings?.whatsappNumber?.ifBlank { null } ?: config.whatsappNumber
    val whatsappLink = adminSettings?.whatsappLink?.ifBlank { null } ?: config.whatsappLink
    val telegramVal = adminSettings?.telegramId?.ifBlank { null } ?: config.telegramId
    val telegramLink = adminSettings?.telegramLink?.ifBlank { null } ?: config.telegramLink
    val website = adminSettings?.supportWebsite?.ifBlank { null } ?: config.apiBaseUrl
    val socialLinks = adminSettings?.socialLinks ?: emptyList()

    // Form inputs state
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Billing & Subscriptions") }
    var messageInput by remember { mutableStateOf("") }
    var isScreenshotAttached by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val subjects = listOf("Billing & Subscriptions", "Mock Test Query", "Academic Content Doubts", "App Crash & Sync Errors", "Refund Reversal Requests")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Tagline
        item {
            Column {
                Text(
                    text = "We're Here to Help",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Reach out via our direct support networks below.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Direct support cards 2x2 simulated grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ContactChannelCard(
                            title = "Email Support",
                            value = email.ifBlank { "sobhadhanaraju0@gmail.com" },
                            buttonText = "Send Email",
                            accentColor = accentColor,
                            icon = Icons.Default.Email,
                            onClick = {
                                val targetMail = email.ifBlank { "sobhadhanaraju0@gmail.com" }
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$targetMail")
                                        putExtra(Intent.EXTRA_SUBJECT, "[${config.appName}] Support Request")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    uriHandler.openUri("mailto:$targetMail")
                                }
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ContactChannelCard(
                            title = "WhatsApp Support",
                            value = whatsappVal.ifBlank { "+91 94935 12768" },
                            buttonText = "Chat Now",
                            accentColor = Color(0xFF10B981),
                            icon = Icons.Default.Chat,
                            onClick = {
                                val cleanNum = whatsappVal.ifBlank { "919493512768" }.replace(Regex("[^0-9]"), "")
                                val targetWa = whatsappLink.ifBlank { "https://wa.me/$cleanNum" }
                                try {
                                    uriHandler.openUri(targetWa)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp: +91 94935 12768", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ContactChannelCard(
                            title = "Call Helpline",
                            value = phone.ifBlank { "+91 94935 12768" },
                            buttonText = "Call Now",
                            accentColor = accentColor,
                            icon = Icons.Default.Phone,
                            onClick = {
                                val num = phone.ifBlank { "+919493512768" }
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$num")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Call: +91 94935 12768", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ContactChannelCard(
                            title = "Telegram Support",
                            value = telegramVal.ifBlank { "@DHANARAJU0" },
                            buttonText = "Open Telegram",
                            accentColor = Color(0xFF0088CC),
                            icon = Icons.Default.Send,
                            onClick = {
                                val targetTg = telegramLink.ifBlank { "https://t.me/DHANARAJU0" }
                                try {
                                    uriHandler.openUri(targetTg)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Telegram: t.me/DHANARAJU0", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        // Send a message interactive form card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Send Us a Message",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Your Display Name", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Your Registered Email", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Select Inquiry Subject:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(6.dp))

                    // Chip group for category subjects
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(subjects) { subject ->
                            val isSel = selectedSubject == subject
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedSubject = subject },
                                label = { Text(subject, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = Color(0xFF475569)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        label = { Text("Describe your support query in detail...", fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated File Attachment Picker Button
                    OutlinedButton(
                        onClick = { isScreenshotAttached = !isScreenshotAttached },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (isScreenshotAttached) Color(0xFF10B981) else Color(0xFFCBD5E1)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isScreenshotAttached) Color(0xFFECFDF5) else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (isScreenshotAttached) Icons.Default.CheckCircle else Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = if (isScreenshotAttached) Color(0xFF10B981) else Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isScreenshotAttached) "Screenshot Attached (img_receipt.png)" else "Attach Receipt/Screenshot (Optional)",
                            color = if (isScreenshotAttached) Color(0xFF065F46) else Color(0xFF475569),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (nameInput.isBlank() || emailInput.isBlank() || messageInput.isBlank()) {
                                Toast.makeText(context, "Please fill in all details before submitting.", Toast.LENGTH_SHORT).show()
                            } else {
                                isSubmitting = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Support Request", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Support operational hours
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Candidate Care Hours",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Monday to Saturday: 9:00 AM - 6:00 PM IST\n• Sunday & Holidays: Urgent email ticketing reviews.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    if (isSubmitting) {
        AlertDialog(
            onDismissRequest = { isSubmitting = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Ticket Submitted!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Thank you $nameInput! Your support ticket on '$selectedSubject' has been received successfully.\n\nOur candidate assistants will review your request and contact you at $emailInput within 2-4 working hours.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSubmitting = false
                        nameInput = ""
                        emailInput = ""
                        messageInput = ""
                        isScreenshotAttached = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun ContactChannelCard(
    title: String,
    value: String,
    buttonText: String,
    accentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(value, fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                Text(buttonText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DedicatedHelpCenterView(
    config: com.example.model.AppConfiguration,
    doc: LegalDocument?,
    onNavigateToFaqs: () -> Unit,
    onNavigateToContact: () -> Unit,
    accentColor: Color
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedIssueCategory by remember { mutableStateOf<String?>(null) }
    var expandedQuestionIndex by remember { mutableStateOf<Int?>(0) }

    LaunchedEffect(Unit) {
        com.example.util.ContactSupportManager.init(context)
    }
    val supportConfig by com.example.util.ContactSupportManager.configState
    val activeApp = remember { com.example.data.SampleData.getActiveApp() }
    val appTitle = activeApp.name.ifBlank { config.appName.ifBlank { "Exam Prep" } }

    data class HelpCenterTopic(
        val title: String,
        val subtitle: String,
        val icon: ImageVector,
        val iconTint: Color,
        val iconBg: Color
    )

    val popularTopics = remember {
        listOf(
            HelpCenterTopic("Subscription & Billing", "PRO unlock, receipts & validity", Icons.Default.WorkspacePremium, Color(0xFFB45309), Color(0xFFFEF3C7)),
            HelpCenterTopic("Payment Issue", "UPI debit, failed order or refund", Icons.Default.Payment, Color(0xFF047857), Color(0xFFECFDF5)),
            HelpCenterTopic("Login / Account Help", "OTP delay, mobile change & sync", Icons.Default.LockPerson, Color(0xFF1D4ED8), Color(0xFFEFF6FF)),
            HelpCenterTopic("Mock Test / Practice Issue", "Timer, rank, score or pause help", Icons.Default.Timer, Color(0xFF7C3AED), Color(0xFFF5F3FF)),
            HelpCenterTopic("Report Question Mistake", "Wrong answer key or translation", Icons.Default.Flag, Color(0xFFBE123C), Color(0xFFFFF1F2)),
            HelpCenterTopic("Current Affairs & Notes", "Daily digests, PDFs & bilingual sets", Icons.Default.MenuBook, Color(0xFF0F766E), Color(0xFFF0FDFA)),
            HelpCenterTopic("Technical Bug / Crash", "Slow loading, display or audio issue", Icons.Default.BugReport, Color(0xFFB45309), Color(0xFFFEF3C7)),
            HelpCenterTopic("Feature Request / Feedback", "Suggest new exams or improvements", Icons.Default.AutoAwesome, Color(0xFF9A7B2C), Color(0xFFFEF9E7))
        )
    }

    val filteredTopics = remember(popularTopics, searchQuery) {
        if (searchQuery.isBlank()) popularTopics
        else popularTopics.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.subtitle.contains(searchQuery, ignoreCase = true)
        }
    }

    val popularQs = remember {
        listOf(
            Pair(
                "My payment was debited via UPI, how do I unlock PRO immediately?",
                "If your payment was deducted and your plan hasn't activated automatically within 2 minutes, tap the WhatsApp Support card (+91 94935 12768) or Email (sobhadhanaraju0@gmail.com) with your 12-digit UPI Reference ID for priority activation."
            ),
            Pair(
                "How do I switch between English and Bilingual/Regional language in Mock Tests?",
                "You can switch your preferred exam medium anytime from the Language switcher in the Top Bar, Profile settings, or directly inside the Practice Arena header."
            ),
            Pair(
                "How do I report an incorrect answer key or translation error in a question?",
                "Tap 'Report Question Mistake' above or tap the Flag icon on any MCQ card during practice. Your report is sent with the Question ID directly to our academic verification team."
            ),
            Pair(
                "Can I access my subscription if I change my phone?",
                "Yes! Simply sign in with the same registered mobile number or Google account on your new device and tap 'Restore Purchases' in the Subscription screen."
            )
        )
    }

    val filteredQs = remember(popularQs, searchQuery) {
        if (searchQuery.isBlank()) popularQs
        else popularQs.filter {
            it.first.contains(searchQuery, ignoreCase = true) ||
                    it.second.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 36.dp)
    ) {
        // 1. Luxury Champagne-Gold Hero Header Card
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFFFFFDF9),
                border = BorderStroke(1.4.dp, Color(0xFFD4AF37).copy(alpha = 0.65f)),
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFFBEB),
                                    Color(0xFFFFFDF9),
                                    Color(0xFFFBF5E6)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "$appTitle • Official Help Center",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Live Support",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "How can we assist your preparation today?",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B),
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reach our dedicated candidate care team via Email, WhatsApp, or Telegram, or select an issue category below for guided assistance.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Bar inside Hero Card
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Search help topics, billing, mock tests, OTP...",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.5.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFFB45309)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF64748B))
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC59B27),
                            unfocusedBorderColor = Color(0xFFE2D5B8),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color(0xFF1E293B)
                        ),
                        singleLine = true
                    )
                }
            }
        }

        // 2. Responsive Direct Contact Action Cards (Email, WhatsApp, Telegram)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Direct Official Support Channels",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = supportConfig.fastSupportBadge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9A7B2C)
                    )
                }

                // 3 Responsive Luxury Channel Cards
                HelpCenterLuxuryChannelCard(
                    title = "WhatsApp Priority Support",
                    subtitle = "Instant chat for payment, OTP & technical help",
                    contactValue = supportConfig.whatsappNumber.ifBlank { "+91 94935 12768" },
                    badgeText = "FASTEST • < 15 MIN",
                    icon = Icons.Default.Chat,
                    iconTint = Color(0xFF047857),
                    iconBg = Color(0xFFECFDF5),
                    buttonText = "Chat on WhatsApp",
                    buttonColor = Color(0xFF059669),
                    onClick = {
                        com.example.util.ContactSupportManager.openWhatsAppSupport(
                            context = context,
                            issueCategory = "Help Center Inquiry"
                        )
                    }
                )

                HelpCenterLuxuryChannelCard(
                    title = "Official Email Support Desk",
                    subtitle = "Detailed queries, billing receipts & feedback",
                    contactValue = supportConfig.supportEmail.ifBlank { "sobhadhanaraju0@gmail.com" },
                    badgeText = "OFFICIAL DESK",
                    icon = Icons.Default.Email,
                    iconTint = Color(0xFFB45309),
                    iconBg = Color(0xFFFEF3C7),
                    buttonText = "Send Email",
                    buttonColor = Color(0xFFB45309),
                    onClick = {
                        com.example.util.ContactSupportManager.openEmailSupport(
                            context = context,
                            issueCategory = "Help Center Inquiry"
                        )
                    }
                )

                HelpCenterLuxuryChannelCard(
                    title = "Telegram Updates & Support",
                    subtitle = "Exam alerts, daily PDFs & aspirant support channel",
                    contactValue = "t.me/DHANARAJU0",
                    badgeText = "COMMUNITY & HELP",
                    icon = Icons.Default.Send,
                    iconTint = Color(0xFF0369A1),
                    iconBg = Color(0xFFF0F9FF),
                    buttonText = "Join Telegram",
                    buttonColor = Color(0xFF0284C7),
                    onClick = {
                        com.example.util.ContactSupportManager.openTelegramSupport(context = context)
                    }
                )
            }
        }

        // 3. Interactive Support Categories Grid (2 columns)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Issue Category for Guided Help",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Tap any topic",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9A7B2C)
                    )
                }

                for (i in filteredTopics.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (j in 0..1) {
                            val index = i + j
                            if (index < filteredTopics.size) {
                                val topic = filteredTopics[index]
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFFFFFDF9),
                                    border = BorderStroke(1.dp, Color(0xFFE8DCC4)),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            selectedIssueCategory = topic.title
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(topic.iconBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = topic.icon,
                                                contentDescription = null,
                                                tint = topic.iconTint,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = topic.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF1E293B),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = topic.subtitle,
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF64748B),
                                                maxLines = 2,
                                                lineHeight = 13.5.sp,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 4. Expandable Popular Questions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Candidate Questions",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B)
                    )
                    TextButton(
                        onClick = onNavigateToFaqs,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "View All FAQs →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                filteredQs.forEachIndexed { idx, pair ->
                    val isExpanded = expandedQuestionIndex == idx
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFFDF9),
                        border = BorderStroke(
                            1.dp,
                            if (isExpanded) Color(0xFFD4AF37) else Color(0xFFE8DCC4)
                        ),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                expandedQuestionIndex = if (isExpanded) null else idx
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = pair.first,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            AnimatedVisibility(visible = isExpanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color(0xFFE8DCC4))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = pair.second,
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF475569),
                                        lineHeight = 18.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Support Hours & Dedicated Contact Us CTA Footer
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFFFDF9),
                border = BorderStroke(1.2.dp, Color(0xFFD4AF37).copy(alpha = 0.55f)),
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFFBEB), Color(0xFFFFFDF9))
                            )
                        )
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Candidate Care Hours: ${supportConfig.supportTimingsLine1}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Open the full Contact Us & Support Concierge screen to copy contact details or submit a custom support ticket.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToContact,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Full Contact Us Concierge", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (selectedIssueCategory != null) {
        SmartSupportIssueDialog(
            issueCategory = selectedIssueCategory!!,
            onDismiss = { selectedIssueCategory = null }
        )
    }
}

@Composable
private fun HelpCenterLuxuryChannelCard(
    title: String,
    subtitle: String,
    contactValue: String,
    badgeText: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    buttonText: String,
    buttonColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFFFDF9),
        border = BorderStroke(1.2.dp, Color(0xFFE8DCC4)),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg)
                    .border(1.dp, iconTint.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = contactValue,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconTint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(buttonText, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun LuxuryDirectChannelsStrip(
    onEmailClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onTelegramClick: () -> Unit,
    onOpenContactHub: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFFFDF9),
        border = BorderStroke(1.2.dp, Color(0xFFD4AF37).copy(alpha = 0.55f)),
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFFBEB), Color(0xFFFFFDF9))
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Still need help? Connect directly with us",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onWhatsAppClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFECFDF5),
                        contentColor = Color(0xFF047857)
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onEmailClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFEF9E7),
                        contentColor = Color(0xFF92400E)
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Email", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onTelegramClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF0EA5E9).copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFF0F9FF),
                        contentColor = Color(0xFF0369A1)
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Telegram", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DedicatedAboutUsView(
    config: com.example.model.AppConfiguration,
    doc: LegalDocument?,
    accentColor: Color
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Brand Identity Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive Infinity Logo
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "INFINITY",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Empowering Aspirants For a Brighter Tomorrow",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Vision & Mission Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Our Vision & Mission", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "To democratize competitive exam preparation and make premium mock tests and current affairs digests accessible to every single aspirant across the nation, regardless of geographical barriers.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF334155),
                    lineHeight = 19.sp
                )
            }
        }

        // What We Do
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("What We Offer", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Infinity delivers high-quality interactive test environments, realistic full-length and topic-wise mock sets, instant timing marking systems, streak analysis, and daily curated syllabus guidelines.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF334155),
                    lineHeight = 19.sp
                )
            }
        }

        // Why we built this
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Why We Built This", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Traditional competitive testing systems are often too rigid, expensive, and out of touch. We designed Infinity as a real-time, responsive, auto-saving exam companion built purely around the candidate's custom requirements.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF334155),
                    lineHeight = 19.sp
                )
            }
        }

        // Feature highlights grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureSmallHighlightCard("Quality Content", "Curated by top teachers", Icons.Default.Verified, accentColor, modifier = Modifier.weight(1f))
                FeatureSmallHighlightCard("Mock Practice", "Instant marks analysis", Icons.Default.Timer, accentColor, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureSmallHighlightCard("Track Progress", "Streaks & reports", Icons.Default.History, accentColor, modifier = Modifier.weight(1f))
                FeatureSmallHighlightCard("Achieve Goals", "Succeed in exams", Icons.Default.Star, accentColor, modifier = Modifier.weight(1f))
            }
        }

        // Footer version details
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "App Version 1.0.0 (Release Build)\nCopyright © 2026 Infinity Systems. All Rights Reserved.",
            color = Color(0xFF94A3B8),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun FeatureSmallHighlightCard(
    title: String,
    desc: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.shadow(1.dp, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), textAlign = TextAlign.Center)
            Text(desc, fontSize = 9.5.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun DedicatedHelpCenterViewFallback(
    config: com.example.model.AppConfiguration,
    doc: LegalDocument?,
    accentColor: Color
) {
    // Left empty since DedicatedHelpCenterView already handles everything inside
}

@Composable
private fun DedicatedAccountDeletionView(
    config: com.example.model.AppConfiguration,
    doc: LegalDocument?,
    onDeleteClick: () -> Unit,
    onWebPortalClick: () -> Unit,
    accentColor: Color
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warning Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Permanent Profile Erasure",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF991B1B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Once initiated, your Infinity candidate account, solved mock records, streaks, performance history, bookmarks, and VIP subscription plans are completely deleted and cannot be recovered.",
                        fontSize = 12.sp,
                        color = Color(0xFF7F1D1D),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Deletion instructions
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("How to Request Deletion", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. You can submit a deletion request instantly in-app using the button below.\n2. Complete the safety double-confirmation steps in the dialog window.\n3. Your account deletion will be initiated instantly and completed within 24 hours.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )
            }
        }

        // Action Trigger Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Delete Account In-App", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onWebPortalClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    border = BorderStroke(1.dp, Color(0xFF94A3B8))
                ) {
                    Text("External Web Deletion Request", color = Color(0xFF475569), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun LegalSupportHubView(
    appDocs: List<LegalDocument>,
    onSelectItem: (LegalDocType) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val activeApp = remember { com.example.data.SampleData.getActiveApp() }
    val appTitle = activeApp.name.ifBlank { "Official Exam Prep" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Champagne-Gold Hero Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFFFDF9),
            border = BorderStroke(1.3.dp, Color(0xFFD4AF37).copy(alpha = 0.6f)),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFFBEB),
                                Color(0xFFFFFDF9),
                                Color(0xFFFDF8EC)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "$appTitle Support & Legal Hub",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Official Candidate Assistance & Policy Center",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9A7B2C)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Access our priority candidate support channels (WhatsApp +91 94935 12768, Email sobhadhanaraju0@gmail.com, Telegram), interactive Help Center, and official legal policies.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )
            }
        }

        // Direct Channels Strip inside Hub
        LuxuryDirectChannelsStrip(
            onEmailClick = {
                com.example.util.ContactSupportManager.openEmailSupport(
                    context = context,
                    issueCategory = "Support & Legal Hub Inquiry"
                )
            },
            onWhatsAppClick = {
                com.example.util.ContactSupportManager.openWhatsAppSupport(
                    context = context,
                    issueCategory = "Support & Legal Hub Inquiry"
                )
            },
            onTelegramClick = {
                com.example.util.ContactSupportManager.openTelegramSupport(context = context)
            },
            onOpenContactHub = { onSelectItem(LegalDocType.CONTACT_US) }
        )

        // Section support
        Text(
            "Candidate Help & Assistance Desk",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LegalMenuCard(
                title = "Contact Us & Support Concierge",
                subtitle = "WhatsApp (+91 94935 12768), Email & Telegram support",
                icon = Icons.Default.ContactSupport,
                accentColor = Color(0xFFB45309),
                badgeText = "PRIORITY",
                onClick = { onSelectItem(LegalDocType.CONTACT_US) }
            )

            LegalMenuCard(
                title = "Help Center & Guided Support",
                subtitle = "Interactive issue resolution, billing & mock guides",
                icon = Icons.Default.Help,
                accentColor = Color(0xFF047857),
                badgeText = "HUB",
                onClick = { onSelectItem(LegalDocType.HELP_CENTER) }
            )

            LegalMenuCard(
                title = "Frequently Asked Questions (FAQs)",
                subtitle = "Instant answers for account, OTP, payments & mocks",
                icon = Icons.Default.Quiz,
                accentColor = Color(0xFF7C3AED),
                badgeText = "Q&A",
                onClick = { onSelectItem(LegalDocType.FAQ) }
            )
        }

        // Section header
        Text(
            "Legal Policies & Agreements",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LegalMenuCard(
                title = "Refund & Cancellation Policy",
                subtitle = "Rules for digital packages, double transactions",
                icon = Icons.Default.CurrencyExchange,
                accentColor = Color(0xFFB45309),
                badgeText = "BILLING",
                onClick = { onSelectItem(LegalDocType.REFUND_CANCELLATION) }
            )

            LegalMenuCard(
                title = "Privacy Policy Agreement",
                subtitle = "How we secure your mock performance data",
                icon = Icons.Default.Shield,
                accentColor = Color(0xFF059669),
                badgeText = "SECURE",
                onClick = { onSelectItem(LegalDocType.PRIVACY_POLICY) }
            )

            LegalMenuCard(
                title = "Terms & Conditions Agreement",
                subtitle = "Aspirant license, content rules & payments",
                icon = Icons.Default.Assignment,
                accentColor = Color(0xFFD97706),
                badgeText = "RULES",
                onClick = { onSelectItem(LegalDocType.TERMS_CONDITIONS) }
            )

            LegalMenuCard(
                title = "Shipping & Digital Delivery Policy",
                subtitle = "100% digital instant in-app activation & ₹0 shipping",
                icon = Icons.Default.LocalShipping,
                accentColor = Color(0xFF0D9488),
                badgeText = "DELIVERY",
                onClick = { onSelectItem(LegalDocType.SHIPPING_DELIVERY) }
            )

            LegalMenuCard(
                title = "About $appTitle",
                subtitle = "Our exam vision, core features, and version",
                icon = Icons.Default.Business,
                accentColor = Color(0xFF9A7B2C),
                badgeText = "BRAND",
                onClick = { onSelectItem(LegalDocType.ABOUT_US) }
            )
        }

        // Razorpay Website & KYC Policy Verification Hub
        com.example.ui.components.RazorpayComplianceHubCard()

        // Danger Deletion
        Text(
            "Account Safety Center",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B)
        )

        LegalMenuCard(
            title = "Account & Profile Deletion",
            subtitle = "Initiate permanent deletion of candidate profile",
            icon = Icons.Default.Delete,
            accentColor = Color(0xFFDC2626),
            badgeText = "DANGER",
            onClick = { onSelectItem(LegalDocType.ACCOUNT_DELETION) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LegalMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badgeText: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFFDF9),
        border = BorderStroke(1.dp, Color(0xFFE8DCC4)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.1f))
                    .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.1f),
                        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun FaqAccordionItem(faq: SupportFaq, accentColor: Color) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFFDF9),
        border = BorderStroke(1.dp, if (expanded) Color(0xFFD4AF37) else Color(0xFFE8DCC4)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = faq.question,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFFB45309),
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFE8DCC4))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = faq.answer,
                        fontSize = 12.5.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DeletionRequestDialog(
    accentColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var isConfirmedCheckbox by remember { mutableStateOf(false) }
    var confirmationInput by remember { mutableStateOf("") }
    val isReady = isConfirmedCheckbox && confirmationInput.trim().uppercase() == "DELETE"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Confirm Profile Deletion", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Please read these deletion conditions carefully:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF334155)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("• ", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        Text("All exam reports, mock metrics, and streak charts are wiped permanently.", fontSize = 11.5.sp, color = Color(0xFF475569))
                    }
                    Row(verticalAlignment = Alignment.Top) {
                        Text("• ", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        Text("Active VIP packages are deleted instantly and cannot be refunded.", fontSize = 11.5.sp, color = Color(0xFF475569))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isConfirmedCheckbox, onCheckedChange = { isConfirmedCheckbox = it })
                    Text("I understand that this action is irreversible and final.", fontSize = 11.5.sp, color = Color(0xFF334155))
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Type \"DELETE\" in capitals below to authorize:", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = confirmationInput,
                        onValueChange = { confirmationInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("DELETE") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEF4444)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isReady,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text("Permanently Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF475569))
            }
        }
    )
}
