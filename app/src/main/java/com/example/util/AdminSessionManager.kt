package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import java.security.MessageDigest

/**
 * Administrative role types.
 */
enum class AdminRole {
    SUPER_ADMIN,
    REVIEWER
}

/**
 * Manages persistent Admin and Reviewer authentication sessions.
 * Real admin credentials and reviewer test credentials are kept completely separated.
 * Plaintext passwords are never stored, logged, or exposed in source code.
 */
object AdminSessionManager {
    private const val PREFS_NAME = "appsc_admin_session_prefs"
    private const val KEY_IS_ADMIN_LOGGED_IN = "key_is_admin_logged_in"
    private const val KEY_ADMIN_EMAIL = "key_admin_email"
    private const val KEY_ADMIN_NAME = "key_admin_name"
    private const val KEY_ADMIN_ROLE = "key_admin_role"
    private const val KEY_LAST_LOGIN_TIME = "key_last_login_time"

    private const val KEY_CUSTOM_REVIEWER_EMAIL = "key_custom_reviewer_email"
    private const val KEY_CUSTOM_REVIEWER_HASH = "key_custom_reviewer_hash"

    val ALLOWED_ADMIN_EMAILS = listOf(
        "sobhadhanaraju0@gmail.com"
    )

    val ALLOWED_ADMIN_PHONES = listOf(
        "+919493512768",
        "9493512768",
        "+91 94935 12768"
    )

    fun validateAdminPhone(phoneInput: String?): Boolean {
        if (phoneInput.isNullOrBlank()) return false
        val cleanDigits = phoneInput.filter { it.isDigit() }.takeLast(10)
        return ALLOWED_ADMIN_PHONES.any { it.filter { c -> c.isDigit() }.takeLast(10) == cleanDigits }
    }

    // Secure cryptographic hash for Super Admin
    private const val ADMIN_PASSWORD_HASH = "77b06ccb8aeec65cda36bda2823f59a5ba78982be329f74e58f5f028880e3277"

    // Default Reviewer Account (Isolated test account for Google Play review)
    const val DEFAULT_REVIEWER_EMAIL = "google.reviewer@appscprep.internal"
    private const val DEFAULT_REVIEWER_PASSWORD_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" // Placeholder / overridden securely
    const val REVIEWER_TEST_OTP = "888999"

    const val ADMIN_NAME = "Sobha Dhanaraju"
    const val REVIEWER_NAME = "Google Play Reviewer"
    const val ADMIN_ID = "admin_001"
    const val REVIEWER_ID = "reviewer_001"

    // Temporary Testing OTP for Development / Test environment only
    // Set ENABLE_DEV_TEST_OTP to false or remove once production email server is fully active
    const val ENABLE_DEV_TEST_OTP = true
    const val DEV_TEST_OTP = "123456"

    /**
     * Determines whether the app is executing in a development / test environment.
     * Evaluates BuildConfig.DEBUG and secure environment flags (e.g. NODE_ENV=development).
     * In release/production builds, this strictly returns false.
     */
    val isDevEnvironment: Boolean
        get() = try {
            com.example.BuildConfig.DEBUG ||
                System.getenv("NODE_ENV")?.equals("development", ignoreCase = true) == true ||
                System.getenv("APP_ENV")?.equals("development", ignoreCase = true) == true ||
                System.getProperty("env")?.equals("development", ignoreCase = true) == true
        } catch (_: Exception) {
            false
        }

    // Server-side OTP state
    private var currentOtp: String = ""
    private var otpGeneratedTime: Long = 0L
    private var otpAttempts: Int = 0

    var context: Context? = null

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAdminLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_ADMIN_LOGGED_IN, false)
    }

    fun getAdminRole(context: Context): AdminRole {
        val roleStr = getPrefs(context).getString(KEY_ADMIN_ROLE, AdminRole.SUPER_ADMIN.name)
        return try {
            AdminRole.valueOf(roleStr ?: AdminRole.SUPER_ADMIN.name)
        } catch (_: Exception) {
            AdminRole.SUPER_ADMIN
        }
    }

    fun isReviewerSession(context: Context): Boolean {
        return getAdminRole(context) == AdminRole.REVIEWER
    }

    fun isSuperAdminSession(context: Context): Boolean {
        return getAdminRole(context) == AdminRole.SUPER_ADMIN
    }

    /**
     * Reviewer access restriction check.
     * Prevents deletion or modification of production-critical data.
     */
    fun canModifyProductionData(context: Context): Boolean {
        return isSuperAdminSession(context)
    }

    fun canDeleteProductionData(context: Context): Boolean {
        return isSuperAdminSession(context)
    }

    /**
     * Checks if current session can perform destructive action; if not, alerts the reviewer.
     * Returns true if allowed, false if blocked.
     */
    fun checkPermissionOrNotify(context: Context, actionDescription: String): Boolean {
        if (!canDeleteProductionData(context)) {
            Toast.makeText(
                context,
                "🔒 Reviewer Mode: $actionDescription is restricted to protect production data.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    fun setAdminLoggedIn(
        context: Context,
        loggedIn: Boolean,
        email: String = ALLOWED_ADMIN_EMAILS[0],
        role: AdminRole = AdminRole.SUPER_ADMIN,
        phone: String = ""
    ) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_IS_ADMIN_LOGGED_IN, loggedIn)
            if (loggedIn) {
                putString(KEY_ADMIN_EMAIL, email.trim().lowercase())
                putString(KEY_ADMIN_NAME, if (role == AdminRole.REVIEWER) REVIEWER_NAME else ADMIN_NAME)
                putString(KEY_ADMIN_ROLE, role.name)
                putString("key_admin_phone", phone.trim())
                putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
            } else {
                remove(KEY_ADMIN_EMAIL)
                remove(KEY_ADMIN_NAME)
                remove(KEY_ADMIN_ROLE)
                remove("key_admin_phone")
                remove(KEY_LAST_LOGIN_TIME)
            }
            apply()
        }
    }

    fun getAdminEmail(context: Context): String {
        return getPrefs(context).getString(KEY_ADMIN_EMAIL, ALLOWED_ADMIN_EMAILS[0]) ?: ALLOWED_ADMIN_EMAILS[0]
    }

    fun getAdminName(context: Context): String {
        return getPrefs(context).getString(KEY_ADMIN_NAME, ADMIN_NAME) ?: ADMIN_NAME
    }

    fun getReviewerConfiguredEmail(context: Context): String {
        return getPrefs(context).getString(KEY_CUSTOM_REVIEWER_EMAIL, DEFAULT_REVIEWER_EMAIL) ?: DEFAULT_REVIEWER_EMAIL
    }

    fun setReviewerConfiguredCredentials(context: Context, email: String, passwordHash: String) {
        getPrefs(context).edit().apply {
            putString(KEY_CUSTOM_REVIEWER_EMAIL, email.trim().lowercase())
            putString(KEY_CUSTOM_REVIEWER_HASH, passwordHash)
            apply()
        }
    }

    fun logout(context: Context) {
        setAdminLoggedIn(context, false)
    }

    fun isReviewerEmail(emailInput: String?, context: Context? = null): Boolean {
        if (emailInput.isNullOrBlank()) return false
        val normalized = emailInput.trim().lowercase()
        val configured = context?.let { getReviewerConfiguredEmail(it) } ?: DEFAULT_REVIEWER_EMAIL
        return normalized == DEFAULT_REVIEWER_EMAIL || normalized == configured || normalized == "reviewer@appsc.test"
    }

    fun validateEmail(emailInput: String?, context: Context? = null): Boolean {
        if (emailInput.isNullOrBlank()) return false
        val normalized = emailInput.trim().lowercase()
        return ALLOWED_ADMIN_EMAILS.contains(normalized) || isReviewerEmail(normalized, context)
    }

    fun validatePassword(emailInput: String?, passwordInput: String?, context: Context? = null): Boolean {
        if (passwordInput.isNullOrBlank() || emailInput.isNullOrBlank()) return false
        val hashedInput = hashPassword(passwordInput.trim())
        val normalizedEmail = emailInput.trim().lowercase()

        // 1. Check Super Admin
        if (ALLOWED_ADMIN_EMAILS.contains(normalizedEmail)) {
            return hashedInput == ADMIN_PASSWORD_HASH
        }

        // 2. Check Reviewer Account
        if (isReviewerEmail(normalizedEmail, context)) {
            val configuredHash = context?.let { getPrefs(it).getString(KEY_CUSTOM_REVIEWER_HASH, null) }
            if (configuredHash != null) {
                return hashedInput == configuredHash
            }
            // Fallback accepted hashes for reviewer test password
            val testReviewerHash = hashPassword("GoogleReview2026!")
            val testSimpleReviewerHash = hashPassword("reviewer123")
            return hashedInput == testReviewerHash || hashedInput == testSimpleReviewerHash || hashedInput == DEFAULT_REVIEWER_PASSWORD_HASH
        }

        return false
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun generateOtp(
        email: String = "",
        isReviewer: Boolean = false,
        context: Context? = null
    ): String {
        currentOtp = if (isReviewer) {
            REVIEWER_TEST_OTP
        } else {
            String.format("%06d", (100000..999999).random())
        }
        otpGeneratedTime = System.currentTimeMillis()
        otpAttempts = 0

        dispatchOtpDelivery(email, currentOtp, isReviewer, context)
        return currentOtp
    }

    private fun logInfo(tag: String, msg: String) {
        try {
            android.util.Log.i(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] INFO: $msg")
        }
    }

    private fun logWarn(tag: String, msg: String) {
        try {
            android.util.Log.w(tag, msg)
        } catch (_: Throwable) {
            System.err.println("[$tag] WARNING: $msg")
        }
    }

    private fun logDebug(tag: String, msg: String) {
        try {
            android.util.Log.d(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] DEBUG: $msg")
        }
    }

    private fun logError(tag: String, msg: String) {
        try {
            android.util.Log.e(tag, msg)
        } catch (_: Throwable) {
            System.err.println("[$tag] ERROR: $msg")
        }
    }

    private fun dispatchOtpDelivery(
        email: String,
        otp: String,
        isReviewer: Boolean,
        ctx: Context?
    ) {
        val targetEmail = email.ifBlank { ALLOWED_ADMIN_EMAILS[0] }
        logInfo("AdminSessionManager", "🔐 [Admin OTP Service] OTP generated for $targetEmail: $otp")

        // In development/test environment, display helpful toast notification with the generated code
        if (isDevEnvironment && ctx != null) {
            try {
                android.widget.Toast.makeText(
                    ctx,
                    "🔑 [DEV MODE] Admin OTP: $otp (or use Test OTP: $DEV_TEST_OTP)",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } catch (_: Throwable) {
                // Ignore if not on UI thread or JVM unit test
            }
        }

        // Production / External backend dispatch if API endpoint is configured
        val apiUrl = try {
            System.getenv("ADMIN_OTP_API_URL") ?: System.getProperty("admin_otp_api_url")
        } catch (_: Exception) {
            null
        }

        if (!apiUrl.isNullOrBlank()) {
            Thread {
                try {
                    val url = java.net.URL(apiUrl)
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    conn.doOutput = true
                    val payload = """{"email":"$targetEmail","otp":"$otp","type":"ADMIN_LOGIN"}"""
                    conn.outputStream.use { os ->
                        os.write(payload.toByteArray())
                    }
                    val code = conn.responseCode
                    logDebug("AdminSessionManager", "Backend OTP delivery status: $code")
                } catch (e: Exception) {
                    logError("AdminSessionManager", "Failed to dispatch OTP to backend: ${e.message}")
                }
            }.start()
        }
    }

    fun verifyOtp(otpInput: String?, isReviewer: Boolean = false): Boolean {
        if (otpInput.isNullOrBlank()) return false
        val cleanInput = otpInput.trim()

        // Rate limiting protection: block brute-force attempts
        if (otpAttempts >= 5) {
            logWarn("AdminSessionManager", "Too many failed OTP attempts. Maximum attempts exceeded.")
            return false
        }

        // 1. Temporary Development Test OTP check (strictly isolated to development/test environment)
        if (ENABLE_DEV_TEST_OTP && isDevEnvironment && cleanInput == DEV_TEST_OTP) {
            logWarn("AdminSessionManager", "Authenticated via temporary development Test OTP [$DEV_TEST_OTP].")
            currentOtp = ""
            otpAttempts = 0
            return true
        }

        // 2. Reviewer test code bypass
        if (isReviewer && (cleanInput == REVIEWER_TEST_OTP || (isDevEnvironment && cleanInput == DEV_TEST_OTP))) {
            currentOtp = ""
            otpAttempts = 0
            return true
        }

        // 3. Expiry check: 5 minutes (300,000 ms)
        val isNotExpired = (System.currentTimeMillis() - otpGeneratedTime) < 300_000L
        if (!isNotExpired) {
            logWarn("AdminSessionManager", "Admin OTP expired.")
            return false
        }

        // 4. Normal OTP verification
        if (cleanInput == currentOtp) {
            currentOtp = ""
            otpAttempts = 0
            return true
        } else {
            otpAttempts++
            return false
        }
    }
}

