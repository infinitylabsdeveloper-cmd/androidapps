package com.example.model

/**
 * Payment Providers recognized in the unified subscription ecosystem.
 */
enum class PaymentProvider(val id: String, val displayName: String) {
    GOOGLE_PLAY("google_play", "Google Play"),
    RAZORPAY("razorpay", "Razorpay Alternative Billing")
}

/**
 * Lifecycle states of a payment transaction.
 */
enum class PaymentTransactionStatus {
    INITIATED,
    PENDING,
    SUCCESSFUL,
    FAILED,
    CANCELLED,
    REFUNDED,
    EXPIRED,
    RENEWAL_SUCCESSFUL,
    RENEWAL_FAILED
}

/**
 * India Alternative Billing / User Choice Billing Configuration.
 * Fully configurable from remote / admin settings without app republication.
 */
data class AlternativeBillingConfig(
    val isAlternativeBillingEnabled: Boolean = true,
    val paymentProviders: List<String> = listOf("google_play", "razorpay"),
    val enabledPaymentMethods: List<String> = listOf("google_play", "razorpay"),
    val enabledProviders: List<PaymentProvider> = listOf(PaymentProvider.GOOGLE_PLAY, PaymentProvider.RAZORPAY),
    val eligibleCountries: List<String> = listOf("IN"), // India compliance
    val defaultProvider: PaymentProvider = PaymentProvider.GOOGLE_PLAY,
    val supportedRazorpayMethods: List<String> = listOf("upi", "google_pay", "phonepe", "paytm", "cards", "netbanking"),
    val merchantName: String = "SOBHA DHANARAJU",
    val merchantId: String = "T89U1OXHFIQbHa",
    val accountId: String = "acc_T89U1OXHFIQbHa",
    val razorpayKeyId: String = com.example.BuildConfig.RAZORPAY_KEY_ID,
    val currency: String = "INR",
    val termsUrl: String = "https://appscprep.com/terms",
    val privacyUrl: String = "https://appscprep.com/privacy",
    val refundPolicyUrl: String = "https://appscprep.com/refund",
    val termsNotice: String = "By purchasing, you agree to our Terms & Conditions and Refund & Cancellation Policy.",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun isEligibleForCountry(countryCode: String): Boolean {
        if (!isAlternativeBillingEnabled) return false
        val cleanCode = countryCode.trim().uppercase()
        return eligibleCountries.isEmpty() || eligibleCountries.contains(cleanCode) || eligibleCountries.contains("ALL")
    }

    companion object {
        val DEFAULT = AlternativeBillingConfig()
    }
}

/**
 * Unified Payment Verification Request payload.
 */
data class PaymentVerificationRequest(
    val userId: String,
    val planId: String,
    val planTitle: String,
    val provider: PaymentProvider,
    val providerOrderId: String,
    val providerPaymentId: String,
    val providerSignature: String? = null,
    val purchaseToken: String? = null,
    val amountRupees: Int,
    val currency: String = "INR",
    val durationHours: Long,
    val appId: String = "appsc-group2",
    val clientTimestamp: Long = System.currentTimeMillis()
)

/**
 * Result emitted after server-side transaction verification.
 */
sealed class PaymentVerificationResult {
    data class Success(
        val transactionId: String,
        val provider: PaymentProvider,
        val planTitle: String,
        val amountRupees: Int,
        val expiryTimestamp: Long,
        val reportingToken: String? = null,
        val auditMessage: String = "Payment verified and subscription activated."
    ) : PaymentVerificationResult()

    data class Pending(
        val providerPaymentId: String,
        val userMessage: String = "Your payment is being verified. Premium access will be activated automatically after confirmation."
    ) : PaymentVerificationResult()

    data class Failed(
        val errorCode: String,
        val userMessage: String = "Payment was unsuccessful. No Premium subscription has been activated.",
        val technicalReason: String
    ) : PaymentVerificationResult()

    data class Verifying(
        val providerPaymentId: String,
        val userMessage: String = "Payment received. We are verifying your transaction."
    ) : PaymentVerificationResult()

    data class Duplicate(
        val existingTransactionId: String,
        val userMessage: String = "Payment already processed. Premium subscription is active."
    ) : PaymentVerificationResult()
}

/**
 * Model for installed UPI applications dynamically detected on device.
 */
data class DetectedUpiApp(
    val packageName: String,
    val displayName: String,
    val isInstalled: Boolean,
    val iconEmoji: String = "⚡"
)

object UpiAppDetector {
    private val KNOWN_UPI_PACKAGES = mapOf(
        "com.google.android.apps.nbu.paisa.user" to "Google Pay",
        "com.phonepe.app" to "PhonePe",
        "net.one97.paytm" to "Paytm",
        "in.org.npci.upiapp" to "BHIM UPI",
        "com.cred.android" to "CRED",
        "com.amazon.mShop.android.shopping" to "Amazon Pay"
    )

    fun getInstalledUpiApps(context: android.content.Context): List<DetectedUpiApp> {
        val pm = context.packageManager
        val detected = mutableListOf<DetectedUpiApp>()

        // Check each known UPI package
        KNOWN_UPI_PACKAGES.forEach { (pkg, name) ->
            val isInstalled = try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (_: Exception) {
                false
            }
            if (isInstalled) {
                val emoji = when {
                    pkg.contains("google") -> "🟢"
                    pkg.contains("phonepe") -> "🟣"
                    pkg.contains("paytm") -> "🔵"
                    pkg.contains("npci") -> "🇮🇳"
                    pkg.contains("cred") -> "💎"
                    else -> "⚡"
                }
                detected.add(DetectedUpiApp(pkg, name, true, emoji))
            }
        }
        return detected
    }
}
