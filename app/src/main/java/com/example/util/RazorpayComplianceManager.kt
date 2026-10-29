package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class CompliancePolicyPage(
    val id: String,
    val razorpayFieldLabel: String,
    val razorpayFieldTelugu: String,
    val fileName: String,
    val summary: String,
    val htmlContent: String
)

object RazorpayComplianceManager {
    private const val PREFS_NAME = "razorpay_compliance_prefs"
    private const val KEY_PUBLIC_BASE_URL = "public_base_url"

    const val DEFAULT_HOSTING_BASE_URL = "https://gen-lang-client-0698953574.web.app"

    var publicBaseUrlState = mutableStateOf(DEFAULT_HOSTING_BASE_URL)
        private set

    fun initialize(context: Context, appId: String = "appsc_group2") {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_PUBLIC_BASE_URL, null)
        if (!saved.isNullOrBlank() && !isInvalidRunAppUrl(saved)) {
            publicBaseUrlState.value = normalizeBaseUrl(saved)
        }
        // Also fetch from Firestore if available
        try {
            FirebaseSyncManager.firestore
                .collection("apps")
                .document(appId)
                .collection("configs")
                .document("razorpay_compliance")
                .get()
                .addOnSuccessListener { doc ->
                    val remoteUrl = doc.getString("publicBaseUrl")
                    if (!remoteUrl.isNullOrBlank() && !isInvalidRunAppUrl(remoteUrl)) {
                        val normalized = normalizeBaseUrl(remoteUrl)
                        publicBaseUrlState.value = normalized
                        prefs.edit().putString(KEY_PUBLIC_BASE_URL, normalized).apply()
                    }
                }
        } catch (_: Exception) {
        }
    }

    fun isInvalidRunAppUrl(url: String): Boolean {
        val lower = url.trim().lowercase()
        return lower.contains(".run.app") ||
            lower.contains("aistudio.google.com") ||
            lower.contains("ais-pre-") ||
            lower.contains("ais-dev-")
    }

    fun normalizeBaseUrl(rawUrl: String): String {
        var cleaned = rawUrl.trim()
        if (!cleaned.startsWith("http://") && !cleaned.startsWith("https://")) {
            cleaned = "https://$cleaned"
        }
        cleaned = cleaned.removeSuffix("/")
        cleaned = cleaned.removeSuffix("/index.html")
        return cleaned
    }

    fun updatePublicBaseUrl(
        context: Context,
        newBaseUrl: String,
        appId: String = "appsc_group2",
        onResult: (success: Boolean, message: String) -> Unit = { _, _ -> }
    ) {
        if (isInvalidRunAppUrl(newBaseUrl)) {
            onResult(
                false,
                "❌ AI Studio '.run.app' links are private streaming emulator sessions and are rejected by Razorpay's verification crawler. Use a public static hosting URL (Firebase Hosting, Netlify Drop, or GitHub Pages)."
            )
            return
        }
        val normalized = normalizeBaseUrl(newBaseUrl)
        publicBaseUrlState.value = normalized
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PUBLIC_BASE_URL, normalized)
            .apply()

        try {
            val payload = mapOf(
                "publicBaseUrl" to normalized,
                "merchantName" to BillingPaymentManager.merchantName,
                "merchantId" to BillingPaymentManager.merchantId,
                "accountId" to BillingPaymentManager.accountId,
                "updatedAt" to System.currentTimeMillis()
            )
            FirebaseSyncManager.firestore
                .collection("apps")
                .document(appId)
                .collection("configs")
                .document("razorpay_compliance")
                .set(payload)
        } catch (_: Exception) {
        }

        onResult(true, "✅ Public verification domain updated to $normalized")
    }

    fun getFullPageUrl(fileName: String, baseUrl: String = publicBaseUrlState.value): String {
        val cleanBase = normalizeBaseUrl(baseUrl)
        return "$cleanBase/$fileName"
    }

    fun getAllCompliancePages(): List<CompliancePolicyPage> {
        val merchantName = BillingPaymentManager.merchantName.ifBlank { "SOBHA DHANARAJU" }
        val mid = BillingPaymentManager.merchantId.ifBlank { "T89U1OXHFIQbHa" }
        val accId = BillingPaymentManager.accountId.ifBlank { "acc_T89U1OXHFIQbHa" }

        return listOf(
            CompliancePolicyPage(
                id = "home_pricing",
                razorpayFieldLabel = "1. Website / App Home & Pricing URL",
                razorpayFieldTelugu = "వెబ్‌సైట్ & సబ్‌స్క్రిప్షన్ ధరల పేజీ (Home & INR Pricing)",
                fileName = "index.html",
                summary = "Displays Legal Entity ($merchantName), MID ($mid), APPSC Group 2 course description, and INR (₹199–₹999) pricing catalog.",
                htmlContent = buildHomeHtml(merchantName, mid, accId)
            ),
            CompliancePolicyPage(
                id = "privacy_policy",
                razorpayFieldLabel = "2. Privacy Policy URL",
                razorpayFieldTelugu = "గోప్యతా విధానం (Privacy Policy)",
                fileName = "privacy-policy.html",
                summary = "DPDP Act 2023 & IT Act 2000 compliant data privacy policy, PCI-DSS Razorpay disclosure, and Grievance Officer contact.",
                htmlContent = buildPrivacyHtml(merchantName, mid)
            ),
            CompliancePolicyPage(
                id = "terms_conditions",
                razorpayFieldLabel = "3. Terms & Conditions URL",
                razorpayFieldTelugu = "నిబంధనలు & షరతులు (Terms & Conditions)",
                fileName = "terms-and-conditions.html",
                summary = "Explicitly states legal ownership by $merchantName, single-user license rules, INR billing terms, and Indian jurisdiction.",
                htmlContent = buildTermsHtml(merchantName, mid)
            ),
            CompliancePolicyPage(
                id = "refund_policy",
                razorpayFieldLabel = "4. Cancellation & Refund Policy URL",
                razorpayFieldTelugu = "రద్దు & రీఫండ్ విధానం (Cancellation & Refund Policy)",
                fileName = "refund-policy.html",
                summary = "Specifies 100% duplicate payment refund, 48-hour technical issue window, and 5–7 business days refund SLA to original source.",
                htmlContent = buildRefundHtml(merchantName, mid)
            ),
            CompliancePolicyPage(
                id = "shipping_policy",
                razorpayFieldLabel = "5. Shipping & Digital Delivery Policy URL",
                razorpayFieldTelugu = "డిజిటల్ డెలివరీ విధానం (Shipping & Delivery Policy)",
                fileName = "shipping-policy.html",
                summary = "Mandatory for Razorpay KYC: declares 100% digital educational delivery (0–5 min instant activation) and ₹0 shipping fee.",
                htmlContent = buildShippingHtml(merchantName, mid)
            ),
            CompliancePolicyPage(
                id = "contact_us",
                razorpayFieldLabel = "6. Contact Us & Grievance Redressal URL",
                razorpayFieldTelugu = "సంప్రదింపు వివరాలు (Contact Us & Grievance Officer)",
                fileName = "contact-us.html",
                summary = "Displays $merchantName, registered support emails (sobhadhanaraju0@gmail.com), operating address in Andhra Pradesh, and SLA.",
                htmlContent = buildContactHtml(merchantName, mid, accId)
            )
        )
    }

    fun copyToClipboard(context: Context, label: String, text: String, toastMessage: String? = null) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(
            context,
            toastMessage ?: "Copied $label to clipboard!",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun copyAllComplianceUrlsForRazorpay(context: Context) {
        val base = publicBaseUrlState.value
        val pages = getAllCompliancePages()
        val builder = StringBuilder()
        builder.appendLine("=== RAZORPAY WEBSITE & KYC POLICY VERIFICATION URLS ===")
        builder.appendLine("Legal Merchant Name: ${BillingPaymentManager.merchantName}")
        builder.appendLine("Merchant ID (MID): ${BillingPaymentManager.merchantId}")
        builder.appendLine("Account ID: ${BillingPaymentManager.accountId}")
        builder.appendLine("-------------------------------------------------------")
        pages.forEach { page ->
            builder.appendLine("${page.razorpayFieldLabel}:")
            builder.appendLine(getFullPageUrl(page.fileName, base))
            builder.appendLine()
        }
        copyToClipboard(
            context = context,
            label = "Razorpay Verification URLs",
            text = builder.toString().trim(),
            toastMessage = "✅ Copied all 6 Razorpay verification URLs!"
        )
    }

    fun exportAndShareComplianceWebsiteZip(context: Context) {
        try {
            val zipFile = File(context.cacheDir, "razorpay-compliance-website.zip")
            if (zipFile.exists()) {
                zipFile.delete()
            }

            val pages = getAllCompliancePages()
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                pages.forEach { page ->
                    val entry = ZipEntry(page.fileName)
                    zos.putNextEntry(entry)
                    val bytes = page.htmlContent.toByteArray(Charsets.UTF_8)
                    zos.write(bytes, 0, bytes.size)
                    zos.closeEntry()
                }

                val readmeEntry = ZipEntry("README-RAZORPAY-VERIFICATION.txt")
                zos.putNextEntry(readmeEntry)
                val readmeBytes = buildReadmeInstructions().toByteArray(Charsets.UTF_8)
                zos.write(readmeBytes, 0, readmeBytes.size)
                zos.closeEntry()
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Razorpay KYC Compliance Website Bundle - SOBHA DHANARAJU"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is the complete 6-page static HTML compliance website for Razorpay Dashboard verification (Merchant: SOBHA DHANARAJU, MID: T89U1OXHFIQbHa). Upload this ZIP to https://app.netlify.com/drop or Firebase Hosting to get a live public HTTPS domain in 30 seconds."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(shareIntent, "Save or Share Razorpay Compliance Website (.zip)")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Could not export ZIP bundle: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun buildReadmeInstructions(): String = """
        ======================================================================
        RAZORPAY WEBSITE & APP VERIFICATION GUIDE — SOBHA DHANARAJU
        ======================================================================
        Why Razorpay Rejected the AI Studio Shared Link (*.run.app):
        ----------------------------------------------------------------------
        The AI Studio link (https://ais-pre-...run.app) is a private,
        login-protected Android Streaming Emulator preview session.
        Razorpay's automated verification bot cannot crawl inside an emulator
        stream and requires publicly accessible HTML pages returning HTTP 200 OK.

        How to Host These 6 Compliance Pages for Free in 60 Seconds:
        ----------------------------------------------------------------------
        Option A — Netlify Drop (Fastest, No CLI Required):
        1. Open https://app.netlify.com/drop in your browser.
        2. Drag and drop this 'razorpay-compliance-website.zip' file directly onto the page.
        3. Netlify will immediately give you a public HTTPS URL (e.g., https://appsc-prep-pro.netlify.app).
        4. Paste that HTTPS URL into the Admin Panel -> Razorpay Gateway -> Public Verification Domain field.

        Option B — GitHub Pages / Firebase Hosting:
        1. All 6 HTML files are also saved inside the '/public' folder of your project repository.
        2. Run 'firebase deploy --only hosting' from your local machine or enable GitHub Pages on the '/public' directory.

        Exact URLs to Submit in Razorpay Dashboard (Settings -> Website/App Details):
        ----------------------------------------------------------------------
        1. Website / Pricing URL:   <YOUR_PUBLIC_DOMAIN>/index.html
        2. Privacy Policy URL:      <YOUR_PUBLIC_DOMAIN>/privacy-policy.html
        3. Terms & Conditions URL:  <YOUR_PUBLIC_DOMAIN>/terms-and-conditions.html
        4. Refund Policy URL:       <YOUR_PUBLIC_DOMAIN>/refund-policy.html
        5. Shipping/Delivery URL:   <YOUR_PUBLIC_DOMAIN>/shipping-policy.html
        6. Contact Us URL:          <YOUR_PUBLIC_DOMAIN>/contact-us.html
    """.trimIndent()

    private fun buildNavHtml(activeFile: String): String {
        val items = listOf(
            "index.html" to "Home & Pricing",
            "privacy-policy.html" to "Privacy Policy",
            "terms-and-conditions.html" to "Terms & Conditions",
            "refund-policy.html" to "Cancellation & Refund",
            "shipping-policy.html" to "Shipping & Digital Delivery",
            "contact-us.html" to "Contact Us"
        )
        val links = items.joinToString("") { (file, title) ->
            val cls = if (file == activeFile) "class=\"active\"" else ""
            "<li><a href=\"$file\" $cls>$title</a></li>"
        }
        return "<nav><div class=\"container\"><ul class=\"nav-links\">$links</ul></div></nav>"
    }

    private fun commonStyles(): String = """
        :root { --primary:#1d4ed8; --primary-dark:#1e3a8a; --bg:#f8fafc; --card:#ffffff; --text:#0f172a; --muted:#475569; --border:#e2e8f0; }
        * { box-sizing:border-box; margin:0; padding:0; }
        body { font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; background:var(--bg); color:var(--text); line-height:1.65; }
        header { background:linear-gradient(135deg,#0f172a 0%,#1e3a8a 100%); color:#fff; padding:24px 16px; border-bottom:4px solid #f59e0b; }
        .container { max-width:960px; margin:0 auto; padding:0 16px; }
        .brand-title { font-size:1.45rem; font-weight:800; }
        .brand-sub { font-size:0.88rem; color:#cbd5e1; margin-top:4px; }
        nav { background:#fff; border-bottom:1px solid var(--border); }
        .nav-links { display:flex; flex-wrap:wrap; gap:8px; padding:10px 0; list-style:none; }
        .nav-links a { text-decoration:none; color:var(--text); font-weight:600; font-size:0.84rem; padding:6px 12px; border-radius:8px; }
        .nav-links a.active, .nav-links a:hover { background:#eff6ff; color:var(--primary); }
        .card { background:var(--card); border:1px solid var(--border); border-radius:14px; padding:24px; margin:24px 0; box-shadow:0 2px 8px rgba(15,23,42,0.04); }
        h1 { font-size:1.45rem; color:var(--primary-dark); margin-bottom:8px; }
        h2 { font-size:1.1rem; color:var(--primary-dark); margin:18px 0 8px; }
        p, li { color:#334155; font-size:0.93rem; margin-bottom:8px; }
        ul { padding-left:20px; }
        .grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(210px,1fr)); gap:16px; margin-top:16px; }
        .plan { background:#fff; border:1px solid var(--border); border-radius:12px; padding:18px; }
        .price { font-size:1.6rem; font-weight:900; color:#0f172a; margin:6px 0; }
        .badge { display:inline-block; background:#dbeafe; color:#1e40af; font-size:0.72rem; font-weight:700; padding:2px 8px; border-radius:99px; }
        .highlight { background:#f0fdf4; border:1px solid #bbf7d0; color:#166534; padding:14px; border-radius:10px; margin-top:16px; font-size:0.9rem; }
        footer { background:#0f172a; color:#cbd5e1; padding:24px 0; text-align:center; font-size:0.82rem; margin-top:36px; }
    """.trimIndent()

    private fun buildHomeHtml(merchantName: String, mid: String, accId: String): String = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>APPSC Group 2 Exam Prep Pro — Official Website | $merchantName</title>
          <style>${commonStyles()}</style>
        </head>
        <body>
          <header>
            <div class="container">
              <div class="brand-title">APPSC Group 2 Exam Prep Pro</div>
              <div class="brand-sub">Legal Entity / Merchant: <strong>$merchantName</strong> &bull; MID: $mid &bull; Account: $accId</div>
            </div>
          </header>
          ${buildNavHtml("index.html")}
          <main class="container">
            <div class="card">
              <h1>Bilingual Competitive Exam Preparation Platform (English &amp; Telugu)</h1>
              <p><strong>APPSC Group 2 Exam Prep Pro</strong>, owned and operated by <strong>$merchantName</strong>, delivers comprehensive topic-wise practice MCQs, detailed explanations, current affairs sets, and mock examinations for Andhra Pradesh Public Service Commission (APPSC) Group 2 Prelims and Mains aspirants.</p>
              <h2>Digital Subscription Plans (Prices in Indian Rupees ₹)</h2>
              <div class="grid">
                <div class="plan">
                  <span class="badge">MONTHLY PASS</span>
                  <h3>1 Month Pro Access</h3>
                  <div class="price">₹199 <small style="font-size:0.8rem;font-weight:500;">/ 30 Days</small></div>
                  <p>Full access to topic-wise MCQs, Current Affairs &amp; Bilingual Explanations.</p>
                </div>
                <div class="plan">
                  <span class="badge">QUARTERLY PASS</span>
                  <h3>3 Months Exam Booster</h3>
                  <div class="price">₹499 <small style="font-size:0.8rem;font-weight:500;">/ 90 Days</small></div>
                  <p>All 150+ syllabus subtopics, timed mock tests &amp; performance analytics.</p>
                </div>
                <div class="plan" style="border:2px solid #1d4ed8;">
                  <span class="badge">POPULAR</span>
                  <h3>6 Months Complete Prep</h3>
                  <div class="price">₹799 <small style="font-size:0.8rem;font-weight:500;">/ 180 Days</small></div>
                  <p>Complete Prelims + Mains Paper I &amp; II question bank with offline revision.</p>
                </div>
                <div class="plan">
                  <span class="badge">ANNUAL MASTERY</span>
                  <h3>12 Months Mastery Pass</h3>
                  <div class="price">₹999 <small style="font-size:0.8rem;font-weight:500;">/ 365 Days</small></div>
                  <p>1 Full Year unrestricted access to all test series, updates &amp; rank tracking.</p>
                </div>
              </div>
              <div class="highlight">
                <strong>Instant Digital Fulfilment:</strong> All plans are 100% digital subscriptions activated instantaneously within the Android app upon payment confirmation via Razorpay Payment Gateway. No physical goods are shipped.
              </div>
            </div>
          </main>
          <footer><div class="container">&copy; 2025–2026 $merchantName (MID: $mid). All Rights Reserved.</div></footer>
        </body>
        </html>
    """.trimIndent()

    private fun buildPrivacyHtml(merchantName: String, mid: String): String = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>Privacy Policy — APPSC Group 2 Exam Prep Pro | $merchantName</title>
          <style>${commonStyles()}</style>
        </head>
        <body>
          <header>
            <div class="container">
              <div class="brand-title">Privacy Policy</div>
              <div class="brand-sub">Legal Entity: <strong>$merchantName</strong> &bull; MID: $mid</div>
            </div>
          </header>
          ${buildNavHtml("privacy-policy.html")}
          <main class="container">
            <div class="card">
              <h1>Privacy Policy</h1>
              <p>This Privacy Policy governs how <strong>$merchantName</strong> ("Merchant", "we", "us"), operating <strong>APPSC Group 2 Exam Prep Pro</strong>, collects, processes, and safeguards user data under the Information Technology Act, 2000 and the Digital Personal Data Protection Act (DPDP Act), 2023.</p>
              <h2>1. Information We Collect</h2>
              <ul>
                <li><strong>Account Identity:</strong> Name, email address, and mobile number submitted during Google Sign-In or Phone OTP login.</li>
                <li><strong>Study Progress:</strong> Quiz attempts, accuracy analytics, and bookmarked questions synced via Google Cloud Firestore.</li>
                <li><strong>Payment Identifiers:</strong> Order ID, Payment ID, and transaction status from Razorpay. <strong>We never store card numbers, CVV, or UPI PINs.</strong> All payments are processed by PCI-DSS Level 1 certified Razorpay Software Private Limited.</li>
              </ul>
              <h2>2. Purpose of Data Usage</h2>
              <p>We use your information strictly to authenticate your account, unlock digital subscription passes, issue payment receipts, and synchronize your exam preparation progress across devices.</p>
              <h2>3. Account &amp; Data Deletion</h2>
              <p>You may delete your account and personal records at any time from the in-app Legal &amp; Support Center or by emailing <strong>sobhadhanaraju0@gmail.com</strong>. Requests are processed within 7 business days.</p>
              <h2>4. Grievance Officer</h2>
              <p><strong>Name:</strong> $merchantName<br/><strong>Email:</strong> sobhadhanaraju0@gmail.com<br/><strong>Region:</strong> Andhra Pradesh, India</p>
            </div>
          </main>
          <footer><div class="container">&copy; 2025–2026 $merchantName. All Rights Reserved.</div></footer>
        </body>
        </html>
    """.trimIndent()

    private fun buildTermsHtml(merchantName: String, mid: String): String = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>Terms &amp; Conditions — APPSC Group 2 Exam Prep Pro | $merchantName</title>
          <style>${commonStyles()}</style>
        </head>
        <body>
          <header>
            <div class="container">
              <div class="brand-title">Terms &amp; Conditions</div>
              <div class="brand-sub">Legal Entity: <strong>$merchantName</strong> &bull; MID: $mid</div>
            </div>
          </header>
          ${buildNavHtml("terms-and-conditions.html")}
          <main class="container">
            <div class="card">
              <h1>Terms &amp; Conditions</h1>
              <p>Welcome to <strong>APPSC Group 2 Exam Prep Pro</strong>, owned and operated by <strong>$merchantName</strong>. By accessing our mobile application or purchasing a digital subscription pass, you agree to these Terms &amp; Conditions.</p>
              <h2>1. Independent Educational Service</h2>
              <p>We provide independent exam preparation study material, practice MCQs, and mock tests aligned with the APPSC Group 2 syllabus. We are not affiliated with the Andhra Pradesh Public Service Commission (APPSC) or any government entity.</p>
              <h2>2. Single-User Digital License</h2>
              <p>Subscriptions grant a personal, non-transferable, revocable license to access premium study modules for the purchased validity period. Unauthorized copying, scraping, or redistribution of content is prohibited.</p>
              <h2>3. Pricing &amp; Payments</h2>
              <p>All prices are stated in Indian Rupees (INR ₹) inclusive of applicable taxes and processed via Razorpay Payment Gateway under merchant account <strong>$merchantName ($mid)</strong> or Google Play Billing.</p>
              <h2>4. Governing Law</h2>
              <p>These terms are governed by the laws of India, with exclusive jurisdiction vested in the courts of Andhra Pradesh, India.</p>
            </div>
          </main>
          <footer><div class="container">&copy; 2025–2026 $merchantName. All Rights Reserved.</div></footer>
        </body>
        </html>
    """.trimIndent()

    private fun buildRefundHtml(merchantName: String, mid: String): String = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>Cancellation &amp; Refund Policy — APPSC Group 2 Exam Prep Pro | $merchantName</title>
          <style>${commonStyles()}</style>
        </head>
        <body>
          <header>
            <div class="container">
              <div class="brand-title">Cancellation &amp; Refund Policy</div>
              <div class="brand-sub">Legal Entity: <strong>$merchantName</strong> &bull; MID: $mid</div>
            </div>
          </header>
          ${buildNavHtml("refund-policy.html")}
          <main class="container">
            <div class="card">
              <h1>Cancellation &amp; Refund Policy</h1>
              <p><strong>$merchantName</strong> provides transparent cancellation and refund terms for all transactions on <strong>APPSC Group 2 Exam Prep Pro</strong>.</p>
              <h2>1. Duplicate or Unfulfilled Payments (100% Refund)</h2>
              <p>If your account is debited multiple times for the same order or if a payment fails to unlock Pro access within 24 hours, 100% of the duplicate/unfulfilled payment is refunded automatically.</p>
              <h2>2. Refund Eligibility Window (48 Hours)</h2>
              <p>Because digital study modules and mock tests are unlocked immediately upon purchase, consumed digital subscriptions are generally non-refundable. However, if a verified technical defect prevents access to subscribed content, you may request a full refund within <strong>48 hours of purchase</strong> by emailing <strong>sobhadhanaraju0@gmail.com</strong> with your Razorpay Payment ID.</p>
              <h2>3. Refund Settlement Timeline (5–7 Business Days)</h2>
              <div class="highlight">
                <strong>Mandatory Refund SLA:</strong> Approved refunds are initiated immediately via Razorpay and credited back to the customer's original payment method (UPI, Bank Account, Card, or Wallet) within <strong>5 to 7 business days</strong>.
              </div>
            </div>
          </main>
          <footer><div class="container">&copy; 2025–2026 $merchantName. All Rights Reserved.</div></footer>
        </body>
        </html>
    """.trimIndent()

    private fun buildShippingHtml(merchantName: String, mid: String): String = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>Shipping &amp; Digital Delivery Policy — APPSC Group 2 Exam Prep Pro | $merchantName</title>
          <style>${commonStyles()}</style>
        </head>
        <body>
          <header>
            <div class="container">
              <div class="brand-title">Shipping &amp; Digital Delivery Policy</div>
              <div class="brand-sub">Legal Entity: <strong>$merchantName</strong> &bull; MID: $mid</div>
            </div>
          </header>
          ${buildNavHtml("shipping-policy.html")}
          <main class="container">
            <div class="card">
              <h1>Shipping &amp; Digital Delivery Policy</h1>
              <div class="highlight">
                <strong>100% Digital Product Notice:</strong> APPSC Group 2 Exam Prep Pro (operated by <strong>$merchantName</strong>) provides exclusively digital educational content and online test series inside the Android app. <strong>No physical goods are shipped.</strong>
              </div>
              <h2>1. Instant Digital Delivery (0–5 Minutes)</h2>
              <p>Upon verified payment completion through Razorpay Payment Gateway, your subscription entitlement is activated <strong>instantaneously</strong> on your account. You can immediately access all premium syllabus MCQs, explanations, and mock tests.</p>
              <h2>2. Shipping Charges</h2>
              <p>Since all services are delivered digitally inside the mobile application, <strong>shipping charges are ₹0 (Nil)</strong>.</p>
              <h2>3. Delivery Assistance</h2>
              <p>If your payment is debited and your pass is not unlocked within 15 minutes due to network interruption, please contact <strong>sobhadhanaraju0@gmail.com</strong> with your Razorpay Payment ID for priority activation within 2 hours.</p>
            </div>
          </main>
          <footer><div class="container">&copy; 2025–2026 $merchantName. All Rights Reserved.</div></footer>
        </body>
        </html>
    """.trimIndent()

    private fun buildContactHtml(merchantName: String, mid: String, accId: String): String = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>Contact Us — APPSC Group 2 Exam Prep Pro | $merchantName</title>
          <style>${commonStyles()}</style>
        </head>
        <body>
          <header>
            <div class="container">
              <div class="brand-title">Contact Us &amp; Grievance Redressal</div>
              <div class="brand-sub">Legal Entity: <strong>$merchantName</strong> &bull; MID: $mid</div>
            </div>
          </header>
          ${buildNavHtml("contact-us.html")}
          <main class="container">
            <div class="card">
              <h1>Merchant Contact &amp; Grievance Officer Details</h1>
              <p>For billing support, subscription verification, or content queries, please contact us using the registered merchant details below:</p>
              <ul>
                <li><strong>Legal Merchant Name:</strong> $merchantName</li>
                <li><strong>Application Name:</strong> APPSC Group 2 Exam Prep Pro</li>
                <li><strong>Razorpay Merchant ID (MID):</strong> $mid</li>
                <li><strong>Razorpay Account ID:</strong> $accId</li>
                <li><strong>Primary Support Email:</strong> sobhadhanaraju0@gmail.com</li>
                <li><strong>Escalation / Admin Email:</strong> sobhadhanaraju0@gmail.com</li>
                <li><strong>Operating Address:</strong> SOBHA DHANARAJU, APPSC Group 2 Exam Prep Pro Digital Division, Visakhapatnam / Vijayawada, Andhra Pradesh – 530001, India</li>
                <li><strong>Support Hours:</strong> Monday to Saturday, 9:00 AM – 7:00 PM IST (Response SLA: Within 24 Hours)</li>
              </ul>
            </div>
          </main>
          <footer><div class="container">&copy; 2025–2026 $merchantName. All Rights Reserved.</div></footer>
        </body>
        </html>
    """.trimIndent()
}
