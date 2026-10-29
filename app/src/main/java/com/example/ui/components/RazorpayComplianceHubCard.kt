package com.example.ui.components

import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.CompliancePolicyPage
import com.example.util.RazorpayComplianceManager

@Composable
fun RazorpayComplianceHubCard(
    appId: String = "appsc_group2",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(appId) {
        RazorpayComplianceManager.initialize(context, appId)
    }

    val currentBaseUrl by RazorpayComplianceManager.publicBaseUrlState
    var domainInput by remember(currentBaseUrl) { mutableStateOf(currentBaseUrl) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isStatusError by remember { mutableStateOf(false) }
    var showHostingGuide by remember { mutableStateOf(true) }
    var previewingPage by remember { mutableStateOf<CompliancePolicyPage?>(null) }

    val isRunAppDetected = remember(domainInput) {
        RazorpayComplianceManager.isInvalidRunAppUrl(domainInput)
    }

    val compliancePages = remember(currentBaseUrl) {
        RazorpayComplianceManager.getAllCompliancePages()
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFF0284C7).copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("razorpay_compliance_hub_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Razorpay Website Verification",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Razorpay Website & Policy Verification Hub",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "6 Mandatory KYC Policy Pages • SOBHA DHANARAJU",
                            fontSize = 11.5.sp,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "KYC READY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF166534),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Diagnostic Banner: Why AI Studio .run.app link was rejected
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFFBEB),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Rejected Link Warning",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Why Razorpay Rejected the Shared '.run.app' Link",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                    Text(
                        text = "• The AI Studio shared link (https://ais-pre-...run.app) is a private Android Streaming Emulator session behind Google login.\n" +
                            "• Razorpay's automated verification bot cannot crawl inside an emulator stream and rejects '.run.app' links.\n" +
                            "• Solution: Use the 6 public static HTML compliance pages generated below (containing Merchant Name SOBHA DHANARAJU, MID T89U1OXHFIQbHa, INR ₹ Pricing, Refund SLA, and Digital Delivery Policy).",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF78350F)
                    )
                    Text(
                        text = "💡 గమనిక: AI Studio '.run.app' లింక్ కేవలం ఎమ్యులేటర్ ప్రివ్యూ మాత్రమే, దానిని Razorpay బాట్ చదవలేదు. క్రింది 6 పబ్లిక్ పాలసీ పేజీల ZIP బండిల్‌ను Netlify Drop లేదా Firebase Hosting లో అప్‌లోడ్ చేసి ఆ పబ్లిక్ లింక్‌లను Razorpay లో ఇవ్వండి.",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Step 1: Export / Host Compliance Website Bundle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showHostingGuide = !showHostingGuide },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Step 1: Get Live Public URL in 60 Seconds (Free)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Color(0xFF166534)
                            )
                        }
                        Icon(
                            imageVector = if (showHostingGuide) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Guide",
                            tint = Color(0xFF15803D)
                        )
                    }

                    AnimatedVisibility(visible = showHostingGuide) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "1. Tap 'Download / Share Compliance Website (.zip)' below.\n" +
                                    "2. Open https://app.netlify.com/drop (or Firebase Hosting / GitHub Pages) and upload the ZIP file.\n" +
                                    "3. Copy your new public HTTPS domain (e.g. https://appsc-group2-sobha.netlify.app) and paste it in Step 2 below to generate all 6 exact Razorpay URLs!",
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                color = Color(0xFF14532D)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                RazorpayComplianceManager.exportAndShareComplianceWebsiteZip(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("export_razorpay_compliance_zip_button")
                        ) {
                            Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Download / Share Website (.zip)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    uriHandler.openUri("https://app.netlify.com/drop")
                                } catch (_: Exception) {
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF16A34A)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("open_netlify_drop_button")
                        ) {
                            Icon(
                                Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Netlify Drop",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            }

            // Step 2: Public Base Domain Input & Validator
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Step 2: Configure Your Public Website Domain",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )

                OutlinedTextField(
                    value = domainInput,
                    onValueChange = {
                        domainInput = it
                        statusMessage = null
                    },
                    label = { Text("Public HTTPS Hosting Domain (Not .run.app)") },
                    placeholder = { Text("https://gen-lang-client-0698953574.web.app") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = if (isRunAppDetected) Color(0xFFDC2626) else Color(0xFF2563EB)
                        )
                    },
                    isError = isRunAppDetected,
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("razorpay_public_domain_input")
                )

                if (isRunAppDetected) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Do NOT use an AI Studio '.run.app' link here! Razorpay will reject it because it requires Google login. Enter your public Firebase Hosting, Netlify, or GitHub Pages domain.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF991B1B),
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                if (statusMessage != null) {
                    Surface(
                        color = if (isStatusError) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusMessage!!,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isStatusError) Color(0xFF991B1B) else Color(0xFF166534),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            RazorpayComplianceManager.updatePublicBaseUrl(
                                context = context,
                                newBaseUrl = domainInput,
                                appId = appId
                            ) { success, msg ->
                                isStatusError = !success
                                statusMessage = msg
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("save_razorpay_domain_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Update Verification URLs", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            RazorpayComplianceManager.copyAllComplianceUrlsForRazorpay(context)
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF0284C7)),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("copy_all_razorpay_urls_button")
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy All 6", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            // Step 3: The 6 Mandatory Razorpay Verification Links
            Text(
                text = "Step 3: Copy & Submit These 6 URLs in Razorpay Dashboard",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = Color(0xFF0F172A)
            )

            compliancePages.forEach { page ->
                val fullUrl = RazorpayComplianceManager.getFullPageUrl(page.fileName, currentBaseUrl)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = page.razorpayFieldLabel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = page.razorpayFieldTelugu,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF0284C7),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Text(
                            text = page.summary,
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            lineHeight = 14.5.sp
                        )

                        // Monospace URL Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = fullUrl,
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E3A8A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    RazorpayComplianceManager.copyToClipboard(
                                        context = context,
                                        label = page.razorpayFieldLabel,
                                        text = fullUrl,
                                        toastMessage = "✅ Copied ${page.fileName} URL!"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("copy_razorpay_url_${page.id}")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy URL", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { previewingPage = page },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF2563EB)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("preview_razorpay_page_${page.id}")
                            ) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Preview Page",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (previewingPage != null) {
        ComplianceHtmlPreviewDialog(
            initialPage = previewingPage!!,
            allPages = compliancePages,
            baseUrl = currentBaseUrl,
            onDismiss = { previewingPage = null }
        )
    }
}

@Composable
fun ComplianceHtmlPreviewDialog(
    initialPage: CompliancePolicyPage,
    allPages: List<CompliancePolicyPage>,
    baseUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var activePage by remember { mutableStateOf(initialPage) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activePage.razorpayFieldLabel,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = RazorpayComplianceManager.getFullPageUrl(activePage.fileName, baseUrl),
                            color = Color(0xFF93C5FD),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_compliance_preview_button")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close Preview",
                            tint = Color.White
                        )
                    }
                }

                // Page Switcher Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allPages.forEach { page ->
                        val selected = page.id == activePage.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) Color(0xFF2563EB) else Color.White,
                            border = BorderStroke(1.dp, if (selected) Color(0xFF2563EB) else Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activePage = page }
                        ) {
                            Text(
                                text = page.fileName.removeSuffix(".html").replace("-", " ").uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else Color(0xFF334155),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Live WebView rendering the exact compliance HTML
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                            settings.javaScriptEnabled = false
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    url: String?
                                ): Boolean {
                                    if (url != null) {
                                        val matched = allPages.find { url.endsWith(it.fileName) }
                                        if (matched != null) {
                                            activePage = matched
                                            return true
                                        }
                                    }
                                    return true
                                }

                                override fun onRenderProcessGone(
                                    view: WebView?,
                                    detail: RenderProcessGoneDetail?
                                ): Boolean {
                                    view?.let { webView ->
                                        (webView.parent as? android.view.ViewGroup)?.removeView(webView)
                                        webView.destroy()
                                    }
                                    return true
                                }
                            }
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(
                            "https://compliance.local/",
                            activePage.htmlContent,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                // Bottom Bar with Copy URL & Copy HTML
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val url = RazorpayComplianceManager.getFullPageUrl(activePage.fileName, baseUrl)
                            RazorpayComplianceManager.copyToClipboard(
                                context = context,
                                label = activePage.razorpayFieldLabel,
                                text = url
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Page URL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            RazorpayComplianceManager.copyToClipboard(
                                context = context,
                                label = activePage.fileName,
                                text = activePage.htmlContent,
                                toastMessage = "Copied ${activePage.fileName} HTML source!"
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy HTML Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
