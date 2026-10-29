package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ContactUsConfig
import com.example.util.ContactSupportManager

/**
 * Reusable press animation modifier for premium tactile cards.
 */
@Composable
private fun Modifier.contactCardPressAnimation(): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "contact_card_scale"
    )
    return this
        .scale(scale)
}

/**
 * Pixel-accurate implementation of the Contact Us / Support screen
 * based on the attached visual design reference.
 */
@Composable
fun ContactUsScreen(
    onBack: () -> Unit,
    onNavigateToFaq: () -> Unit = {},
    onNavigateToHelpCenter: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        ContactSupportManager.init(context)
    }

    // Observe dynamic configuration from ContactSupportManager (persisted & configurable via Admin Panel)
    val config by ContactSupportManager.configState
    val activeApp = com.example.data.SampleData.getActiveApp()
    var selectedIssueCategory by remember { mutableStateOf<String?>(null) }
    var customIssueDetails by remember { mutableStateOf("") }

    // Parse configurable colors with safe fallbacks matching the reference
    val screenBg = remember(config.screenBgHex) {
        ContactUsConfig.parseHexColor(config.screenBgHex, Color(0xFFFFFFFF))
    }
    val bannerBgStart = remember(config.bannerBgStartHex) {
        ContactUsConfig.parseHexColor(config.bannerBgStartHex, Color(0xFFFFFBEB))
    }
    val bannerBgEnd = remember(config.bannerBgEndHex) {
        ContactUsConfig.parseHexColor(config.bannerBgEndHex, Color(0xFFFEF3C7))
    }
    val bannerBorder = remember(config.bannerBorderHex) {
        ContactUsConfig.parseHexColor(config.bannerBorderHex, Color(0xFFFDE68A))
    }

    val emailBgStart = remember(config.emailBgStartHex) {
        ContactUsConfig.parseHexColor(config.emailBgStartHex, Color(0xFFF0F9FF))
    }
    val emailBgEnd = remember(config.emailBgEndHex) {
        ContactUsConfig.parseHexColor(config.emailBgEndHex, Color(0xFFE0F2FE))
    }
    val emailBorder = remember(config.emailBorderHex) {
        ContactUsConfig.parseHexColor(config.emailBorderHex, Color(0xFFBAE6FD))
    }
    val emailAccent = remember(config.emailTextAccentHex) {
        ContactUsConfig.parseHexColor(config.emailTextAccentHex, Color(0xFF0284C7))
    }

    val whatsappBgStart = remember(config.whatsappBgStartHex) {
        ContactUsConfig.parseHexColor(config.whatsappBgStartHex, Color(0xFFF0FDF4))
    }
    val whatsappBgEnd = remember(config.whatsappBgEndHex) {
        ContactUsConfig.parseHexColor(config.whatsappBgEndHex, Color(0xFFDCFCE7))
    }
    val whatsappBorder = remember(config.whatsappBorderHex) {
        ContactUsConfig.parseHexColor(config.whatsappBorderHex, Color(0xFFBBF7D0))
    }
    val whatsappAccent = remember(config.whatsappTextAccentHex) {
        ContactUsConfig.parseHexColor(config.whatsappTextAccentHex, Color(0xFF16A34A))
    }

    val telegramBgStart = remember(config.telegramBgStartHex) {
        ContactUsConfig.parseHexColor(config.telegramBgStartHex, Color(0xFFF0F9FF))
    }
    val telegramBgEnd = remember(config.telegramBgEndHex) {
        ContactUsConfig.parseHexColor(config.telegramBgEndHex, Color(0xFFE0F2FE))
    }
    val telegramBorder = remember(config.telegramBorderHex) {
        ContactUsConfig.parseHexColor(config.telegramBorderHex, Color(0xFFBAE6FD))
    }
    val telegramAccent = remember(config.telegramTextAccentHex) {
        ContactUsConfig.parseHexColor(config.telegramTextAccentHex, Color(0xFF0284C7))
    }

    val primaryTextColor = remember(config.primaryTextHex) {
        ContactUsConfig.parseHexColor(config.primaryTextHex, Color(0xFF0F172A))
    }
    val secondaryTextColor = remember(config.secondaryTextHex) {
        ContactUsConfig.parseHexColor(config.secondaryTextHex, Color(0xFF64748B))
    }

    // Handlers
    val handleEmailClick = {
        ContactSupportManager.openEmailSupport(context, "General Support Inquiry")
    }

    val handleWhatsAppClick = {
        ContactSupportManager.openWhatsAppSupport(context, "General Support Inquiry")
    }

    val handleTelegramClick = {
        ContactSupportManager.openTelegramSupport(context)
    }

    val handleCallClick = {
        try {
            val cleanPhone = config.phoneNumber.replace(Regex("[^0-9+]"), "")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Call: ${config.phoneNumber}", Toast.LENGTH_LONG).show()
        }
    }

    val handleCommunityClick = {
        val target = config.communityUrl.ifBlank { "https://${config.telegramLink}" }
        val finalUrl = if (target.startsWith("http://") || target.startsWith("https://")) target else "https://$target"
        try {
            uriHandler.openUri(finalUrl)
        } catch (e: Exception) {
            Toast.makeText(context, "Community: $finalUrl", Toast.LENGTH_SHORT).show()
        }
    }

    val handleFaqClick = {
        if (config.faqUrl.isNotBlank()) {
            try {
                val url = if (config.faqUrl.startsWith("http")) config.faqUrl else "https://${config.faqUrl}"
                uriHandler.openUri(url)
            } catch (e: Exception) {
                onNavigateToFaq()
            }
        } else {
            onNavigateToFaq()
        }
    }

    val handleHelpCenterClick = {
        if (config.helpCenterUrl.isNotBlank()) {
            try {
                val url = if (config.helpCenterUrl.startsWith("http")) config.helpCenterUrl else "https://${config.helpCenterUrl}"
                uriHandler.openUri(url)
            } catch (e: Exception) {
                onNavigateToHelpCenter()
            }
        } else {
            onNavigateToHelpCenter()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp) // Mobile & tablet responsive constraint
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==========================================
            // 1. TOP HEADER
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button (Circular white container with subtle border & shadow)
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            tint = primaryTextColor,
                            modifier = Modifier
                                .size(18.dp)
                                .offset(x = 2.dp)
                        )
                    }
                }

                // Center Title & Subtitle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = config.headerTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = config.headerSubtitle,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = secondaryTextColor,
                        textAlign = TextAlign.Center
                    )
                }

                // Support Headset Icon Button
                Surface(
                    onClick = handleCallClick,
                    shape = CircleShape,
                    color = Color(0xFFFFFDF5),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Support Agent",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // ==========================================
            // 2. PREMIUM SUPPORT BANNER ("Your Success Matters")
            // ==========================================
            Surface(
                shape = RoundedCornerShape(config.cardCornerRadiusDp.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, bannerBorder),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .contactCardPressAnimation()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(bannerBgStart, bannerBgEnd)
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shield with Heart Icon Container
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(34.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(13.dp)
                                    .offset(y = (-1).dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Banner Texts
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(0.5.dp, Color(0xFFFCD34D)),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Text(
                                    text = "${activeApp.name} • ${activeApp.region.ifBlank { activeApp.category }.ifBlank { "National" }}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = config.bannerTitle,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = config.bannerSubtitle,
                                fontSize = 11.5.sp,
                                color = secondaryTextColor,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Fast Support Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, Color(0xFFFCD34D))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = config.fastSupportBadge,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF92400E)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "⚡",
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. WHATSAPP CARD ("WhatsApp Support")
            // ==========================================
            if (config.isWhatsAppEnabled) {
                Surface(
                    onClick = handleWhatsAppClick,
                    shape = RoundedCornerShape(config.cardCornerRadiusDp.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, whatsappBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .contactCardPressAnimation()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(whatsappBgStart, whatsappBgEnd)
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Green WhatsApp Icon Box
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF22C55E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                    contentDescription = "WhatsApp",
                                    tint = Color.White,
                                    modifier = Modifier.size(25.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Texts
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = config.whatsappTitle,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                                Text(
                                    text = config.whatsappSubtext,
                                    fontSize = 11.5.sp,
                                    color = secondaryTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = config.whatsappNumber,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = whatsappAccent
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF16A34A),
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    text = "Chat Now",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 4. EMAIL CARD ("Email Support")
            // ==========================================
            if (config.isEmailSupportEnabled) {
                Surface(
                    onClick = handleEmailClick,
                    shape = RoundedCornerShape(config.cardCornerRadiusDp.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, emailBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .contactCardPressAnimation()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(emailBgStart, emailBgEnd)
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Blue Email Icon Box
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0284C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Texts
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = config.emailTitle,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                                Text(
                                    text = config.emailSubtext,
                                    fontSize = 11.5.sp,
                                    color = secondaryTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = config.supportEmail,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = emailAccent
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0284C7),
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    text = "Send Email",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 5. TELEGRAM CARD ("Telegram Support")
            // ==========================================
            if (config.isTelegramEnabled) {
                Surface(
                    onClick = handleTelegramClick,
                    shape = RoundedCornerShape(config.cardCornerRadiusDp.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, telegramBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .contactCardPressAnimation()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(telegramBgStart, telegramBgEnd)
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Blue Telegram Icon Box
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0088CC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_telegram),
                                    contentDescription = "Telegram",
                                    tint = Color.White,
                                    modifier = Modifier.size(25.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Texts
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = config.telegramTitle,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                                Text(
                                    text = config.telegramSubtext,
                                    fontSize = 11.5.sp,
                                    color = secondaryTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "@DHANARAJU0 • t.me/DHANARAJU0",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = telegramAccent
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0088CC),
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    text = "Open Telegram",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 5B. SUPPORT CATEGORIES & ISSUE SELECTOR
            // ==========================================
            Surface(
                shape = RoundedCornerShape(config.cardCornerRadiusDp.dp),
                color = Color(0xFFFFFDF9),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Select Support Topic",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                            Text(
                                text = "Tap an issue category for instant pre-filled assistance",
                                fontSize = 11.5.sp,
                                color = secondaryTextColor
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, Color(0xFFFCD34D))
                        ) {
                            Text(
                                text = "8 Topics",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    val supportCategories = remember {
                        listOf(
                            Triple("Login & OTP Issues", "OTP delay, mobile number login", Icons.Default.LockOpen),
                            Triple("Account & Profile Help", "Profile update, exam medium", Icons.Default.ManageAccounts),
                            Triple("Subscription & Plan Activation", "VIP/PRO plan unlock & validity", Icons.Default.WorkspacePremium),
                            Triple("Payments & Billing Support", "UPI/Razorpay status & receipts", Icons.Default.ReceiptLong),
                            Triple("MCQs & Content Correction", "Question doubt or key error", Icons.Default.FactCheck),
                            Triple("Mock Tests & Results Help", "Scoring, rank & timer help", Icons.Default.Assessment),
                            Triple("Technical Bug / App Not Working", "Loading, sync or crash issue", Icons.Default.BugReport),
                            Triple("General Enquiry / Feedback", "Suggestions & exam queries", Icons.Default.ChatBubbleOutline)
                        )
                    }

                    for (i in supportCategories.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (j in 0..1) {
                                val idx = i + j
                                if (idx < supportCategories.size) {
                                    val cat = supportCategories[idx]
                                    Surface(
                                        onClick = {
                                            selectedIssueCategory = cat.first
                                            customIssueDetails = ""
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFFF3E8D2)),
                                        shadowElevation = 0.5.dp,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFFFFFBEB)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = cat.third,
                                                    contentDescription = cat.first,
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = cat.first,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = primaryTextColor,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = cat.second,
                                                    fontSize = 9.5.sp,
                                                    color = secondaryTextColor,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
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

            // ==========================================
            // 6. TWO SMALLER CARDS (Support Timings & Quick Support)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card A: Support Timings
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .contactCardPressAnimation()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Clock Icon
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(Color(0xFFFDE68A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = config.supportTimingsTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = config.supportTimingsLine1,
                                    fontSize = 10.5.sp,
                                    color = secondaryTextColor,
                                    lineHeight = 14.sp
                                )
                                Text(
                                    text = config.supportTimingsLine2,
                                    fontSize = 10.5.sp,
                                    color = secondaryTextColor,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                // Card B: Quick Support
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .contactCardPressAnimation()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE))
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Purple Checkmark/Shield Icon
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(Color(0xFFDDD6FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = config.quickSupportTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = config.quickSupportSubtext,
                                    fontSize = 10.5.sp,
                                    color = secondaryTextColor,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 7. QUICK-LINK SECTION CARD (FAQ, Help Center, Call Us, Community)
            // ==========================================
            Surface(
                shape = RoundedCornerShape(config.cardCornerRadiusDp.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                shadowElevation = 1.5.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Item 1: FAQ
                    QuickLinkColumn(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.HelpOutline,
                        iconTint = Color(0xFFE11D48),
                        circleBgColor = Color(0xFFFCE7F3),
                        title = config.faqTitle,
                        subtitle = config.faqSubtitle,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        onClick = handleFaqClick
                    )

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .width(1.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    // Item 2: Help Center
                    QuickLinkColumn(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.MenuBook,
                        iconTint = Color(0xFF10B981),
                        circleBgColor = Color(0xFFDCFCE7),
                        title = config.helpCenterTitle,
                        subtitle = config.helpCenterSubtitle,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        onClick = handleHelpCenterClick
                    )

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .width(1.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    // Item 3: Call Us
                    QuickLinkColumn(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Phone,
                        iconTint = Color(0xFFEA580C),
                        circleBgColor = Color(0xFFFFEDD5),
                        title = config.callUsTitle,
                        subtitle = config.phoneNumber,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        onClick = handleCallClick
                    )

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .width(1.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    // Item 4: Community
                    QuickLinkColumn(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Groups,
                        iconTint = Color(0xFF8B5CF6),
                        circleBgColor = Color(0xFFEDE9FE),
                        title = config.communityTitle,
                        subtitle = config.communitySubtitle,
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor,
                        onClick = handleCommunityClick
                    )
                }
            }

            // ==========================================
            // 8. BOTTOM MOTIVATIONAL STRIP
            // ==========================================
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
                shadowElevation = 0.5.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = config.motivationalQuote,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    if (selectedIssueCategory != null) {
        SmartSupportIssueDialog(
            issueCategory = selectedIssueCategory!!,
            initialDetails = customIssueDetails,
            onDismiss = { selectedIssueCategory = null }
        )
    }
}

/**
 * Reusable Smart Support Issue Assistant Dialog that pre-fills App Name, Exam/State,
 * Issue Category, User Phone/Email, and optional Question/Payment context.
 */
@Composable
fun SmartSupportIssueDialog(
    issueCategory: String,
    initialDetails: String = "",
    extraContext: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val config by ContactSupportManager.configState
    val activeApp = com.example.data.SampleData.getActiveApp()
    var details by remember(issueCategory, initialDetails) { mutableStateOf(initialDetails) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFFFDF9),
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFCD34D))
                ) {
                    Text(
                        text = "${activeApp.name} • Direct Support",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = issueCategory,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Auto-Attached Support Context:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = "• App: ${activeApp.name}\n• Exam/Region: ${activeApp.examName} (${activeApp.region.ifBlank { activeApp.category }})" +
                                if (extraContext.isNotBlank()) "\n• Ref: $extraContext" else "",
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            lineHeight = 15.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Describe your issue (optional)", fontSize = 12.sp) },
                    placeholder = { Text("Add mobile number, transaction ID, or question details...", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD97706),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Text(
                    text = "Choose how you want to send this support request:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569)
                )

                if (config.isWhatsAppEnabled) {
                    Button(
                        onClick = {
                            ContactSupportManager.openWhatsAppSupport(
                                context = context,
                                issueCategory = issueCategory,
                                customDetails = details,
                                extraContext = extraContext
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_whatsapp),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send via WhatsApp (${config.whatsappNumber})", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (config.isEmailSupportEnabled) {
                    Button(
                        onClick = {
                            ContactSupportManager.openEmailSupport(
                                context = context,
                                issueCategory = issueCategory,
                                customDetails = details,
                                extraContext = extraContext
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send via Email (${config.supportEmail})", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                if (config.isTelegramEnabled) {
                    OutlinedButton(
                        onClick = {
                            ContactSupportManager.openTelegramSupport(context)
                            onDismiss()
                        },
                        border = BorderStroke(1.dp, Color(0xFF0088CC)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_telegram),
                            contentDescription = null,
                            tint = Color(0xFF0088CC),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Telegram (@DHANARAJU0)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0088CC))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * Single item inside the 4-item Quick-Link row.
 */
@Composable
private fun QuickLinkColumn(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    circleBgColor: Color,
    title: String,
    subtitle: String,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Circular Icon Badge
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(circleBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = primaryTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = subtitle,
            fontSize = 9.5.sp,
            color = secondaryTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
