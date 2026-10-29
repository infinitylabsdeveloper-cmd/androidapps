package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import coil.Coil
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.*
import androidx.compose.foundation.text.BasicTextField
import com.example.data.SampleData
import com.example.model.LoginBgImageModel
import com.example.model.LoginQuotationModel
import com.example.util.FirebaseSyncManager
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.GoogleAuthProvider

import android.app.Activity
import android.util.Log

val TEST_PHONE_NUMBERS = setOf(
    "9876543210",
    "9999999999",
    "9000000000",
    "8888888888",
    "7777777777",
    "9123456789"
)

object LoginThemeResolver {
    fun getScreenBgBrush(themeKey: String): Brush {
        return when (themeKey) {
            "LUXURY_OBSIDIAN_GOLD" -> Brush.verticalGradient(
                colors = listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5), Color(0xFFFDE047), Color(0xFFF59E0B))
            )
            "DEEP_SUNSET_CRIMSON" -> Brush.verticalGradient(
                colors = listOf(Color(0xFFFFF1F2), Color(0xFFFFE4E6), Color(0xFFFECDD3), Color(0xFFFDA4AF))
            )
            "MIDNIGHT_EMERALD" -> Brush.verticalGradient(
                colors = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7), Color(0xFFA7F3D0), Color(0xFF6EE7B7))
            )
            "ROYAL_PURPLE_GOLD" -> Brush.verticalGradient(
                colors = listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE), Color(0xFFDDD6FE), Color(0xFFC084FC))
            )
            "WARM_CREAM_GOLD" -> Brush.verticalGradient(
                colors = listOf(Color(0xFFFFFDF9), Color(0xFFFFFBEB), Color(0xFFFEF3C7), Color(0xFFFCD34D))
            )
            else -> Brush.verticalGradient( // "ROYAL_DARK_NAVY"
                colors = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE), Color(0xFFFFEDD5), Color(0xFFFFFBEB))
            )
        }
    }

    data class CardThemeColors(
        val containerColor: Color,
        val textColorPrimary: Color,
        val textColorSecondary: Color,
        val inputContainerColor: Color,
        val inputBorderColor: Color,
        val isDarkCard: Boolean
    )

    fun getCardThemeColors(styleKey: String): CardThemeColors {
        return when (styleKey) {
            "LUXURY_DARK_GLASS" -> CardThemeColors(
                containerColor = Color(0xFF0F172A).copy(alpha = 0.94f),
                textColorPrimary = Color.White,
                textColorSecondary = Color(0xFFCBD5E1),
                inputContainerColor = Color(0xFF1E293B),
                inputBorderColor = Color(0xFF334155),
                isDarkCard = true
            )
            "PEARL_WHITE" -> CardThemeColors(
                containerColor = Color(0xFFFFFFFF),
                textColorPrimary = Color(0xFF0F172A),
                textColorSecondary = Color(0xFF475569),
                inputContainerColor = Color(0xFFF8FAFC),
                inputBorderColor = Color(0xFFCBD5E1),
                isDarkCard = false
            )
            "ROYAL_MIDNIGHT_BLUE" -> CardThemeColors(
                containerColor = Color(0xFF1E293B).copy(alpha = 0.95f),
                textColorPrimary = Color(0xFFFDE68A),
                textColorSecondary = Color(0xFFE2E8F0),
                inputContainerColor = Color(0xFF0F172A),
                inputBorderColor = Color(0xFF334155),
                isDarkCard = true
            )
            "EMERALD_MINT_LIGHT" -> CardThemeColors(
                containerColor = Color(0xFFF0FDF4),
                textColorPrimary = Color(0xFF064E3B),
                textColorSecondary = Color(0xFF047857),
                inputContainerColor = Color.White,
                inputBorderColor = Color(0xFFA7F3D0),
                isDarkCard = false
            )
            else -> CardThemeColors( // "RICH_LIGHT_CREAM"
                containerColor = Color(0xFFFEF9F3),
                textColorPrimary = Color(0xFF0C1938),
                textColorSecondary = Color(0xFF475569),
                inputContainerColor = Color.White,
                inputBorderColor = Color(0xFFD6C8B7),
                isDarkCard = false
            )
        }
    }

    fun getCardBorderBrush(borderStyleKey: String): Brush {
        return when (borderStyleKey) {
            "ROYAL_AMBER" -> Brush.horizontalGradient(
                colors = listOf(Color(0xFFEA4335), Color(0xFFFF6B00), Color(0xFFEA4335))
            )
            "EMERALD_GREEN" -> Brush.horizontalGradient(
                colors = listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFF10B981))
            )
            "SUBTLE_WARM" -> androidx.compose.ui.graphics.SolidColor(Color(0xFFEADBCE))
            else -> Brush.horizontalGradient( // "GOLD_GRADIENT"
                colors = listOf(Color(0xFFFFD700), Color(0xFF34D399), Color(0xFFFFD700))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit = {},
    onNavigateToAdminDashboard: () -> Unit = {}
) {
    val config = LocalAppConfig.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(LoginStep.ENTER_PHONE) }
    var phoneNumber by remember { mutableStateOf("") }
    val otpValues = remember { mutableStateListOf("", "", "", "", "", "") }
    var remainingSeconds by remember { mutableStateOf(60) }
    var verificationId by remember { mutableStateOf("") }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var isSendingOtp by remember { mutableStateOf(false) }
    var isVerifyingOtp by remember { mutableStateOf(false) }

    BackHandler(enabled = currentStep == LoginStep.VERIFY_OTP) {
        currentStep = LoginStep.ENTER_PHONE
    }

    // Dynamic admin configurable background theme
    val bgThemeKey = SampleData.loginScreenBgThemeState.value
    val bgBrush = remember(bgThemeKey) { LoginThemeResolver.getScreenBgBrush(bgThemeKey) }

    // Background Carousel Images (Andhra Pradesh landmarks, culture & geography)
    val apBackgroundsState = SampleData.loginBackgroundsState
    val enabledBackgrounds = remember(apBackgroundsState.size, apBackgroundsState.map { it.isEnabled to it.order }) {
        val enabled = apBackgroundsState.filter { it.isEnabled }.sortedBy { it.order }
        if (enabled.isEmpty()) {
            listOf(
                LoginBgImageModel(
                    id = "fallback",
                    url = "https://images.unsplash.com/photo-1627483262769-04d0a1401487?q=80&w=1400&auto=format&fit=crop",
                    title = "Araku Valley & Lambasingi",
                    subtitle = "Lush green valleys, coffee plantations & misty hills",
                    isEnabled = true,
                    order = 0
                )
            )
        } else {
            enabled
        }
    }

    // Asynchronously preload all configured login background assets into Coil cache
    LaunchedEffect(enabledBackgrounds) {
        val loader = Coil.imageLoader(context)
        enabledBackgrounds.forEach { bgItem ->
            val request = ImageRequest.Builder(context)
                .data(bgItem.url)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .allowHardware(true)
                .crossfade(true)
                .build()
            loader.enqueue(request)
        }
    }

    var showSupportDialog by remember { mutableStateOf(false) }

    // Countdown timer for OTP
    LaunchedEffect(currentStep, remainingSeconds) {
        if (currentStep == LoginStep.VERIFY_OTP && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4C1D95), // Deep Purple
                        Color(0xFF5B21B6), // Purple
                        Color(0xFF4C1D95)  // Deep Purple
                    )
                )
            )
    ) {
        // Ultimate Premium Decorative Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Top-Left Luminous Emerald Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF10B981).copy(alpha = 0.22f),
                        Color(0xFF059669).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.15f, h * 0.20f),
                    radius = w * 0.85f
                )
            )

            // 2. Middle-Right Luminous Teal Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF14B8A6).copy(alpha = 0.20f),
                        Color(0xFF0D9488).copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.85f, h * 0.50f),
                    radius = w * 0.8f
                )
            )

            // 3. Bottom-Left Subtle Gold/Amber Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFBBF24).copy(alpha = 0.10f),
                        Color(0xFFD97706).copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.25f, h * 0.85f),
                    radius = w * 0.75f
                )
            )

            // 4. Elegant flowing abstract gold & teal wave shapes
            // Wave 1: Cyan/Teal flowing wave across the middle
            val wave1 = Path().apply {
                moveTo(0f, h * 0.35f)
                cubicTo(
                    w * 0.35f, h * 0.20f,
                    w * 0.65f, h * 0.50f,
                    w, h * 0.38f
                )
            }
            drawPath(
                path = wave1,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF06B6D4).copy(alpha = 0.30f),
                        Color(0xFF10B981).copy(alpha = 0.18f),
                        Color(0xFFFBBF24).copy(alpha = 0.08f)
                    )
                ),
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Wave 2: S-curve gold line near bottom
            val wave2 = Path().apply {
                moveTo(0f, h * 0.75f)
                cubicTo(
                    w * 0.30f, h * 0.85f,
                    w * 0.70f, h * 0.65f,
                    w, h * 0.80f
                )
            }
            drawPath(
                path = wave2,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFBBF24).copy(alpha = 0.15f),
                        Color(0xFF0D9488).copy(alpha = 0.22f),
                        Color.Transparent
                    )
                ),
                style = Stroke(width = 1.8.dp.toPx())
            )

            // Wave 3: Fine accent wave at the top
            val wave3 = Path().apply {
                moveTo(0f, h * 0.15f)
                cubicTo(
                    w * 0.40f, h * 0.05f,
                    w * 0.60f, h * 0.25f,
                    w, h * 0.10f
                )
            }
            drawPath(
                path = wave3,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFF59E0B).copy(alpha = 0.10f),
                        Color(0xFF14B8A6).copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                style = Stroke(width = 1.2.dp.toPx())
            )
        }



    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (currentStep) {
            LoginStep.ENTER_PHONE -> {
                PhoneLoginContent(
                    phoneNumber = phoneNumber,
                    onPhoneNumberChanged = { phoneNumber = it },
                    isSendingOtp = isSendingOtp,
                    onContinueWithOtp = {
                        val cleanPhone = phoneNumber.filter { it.isDigit() }
                        if (cleanPhone.length == 10 && cleanPhone.first() in listOf('6', '7', '8', '9')) {
                            val activity = context as? Activity
                            if (activity == null) {
                                Toast.makeText(context, "Unable to start SMS verification", Toast.LENGTH_SHORT).show()
                                return@PhoneLoginContent
                            }
                            isSendingOtp = true
                            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                    isSendingOtp = false
                                    FirebaseSyncManager.auth.signInWithCredential(credential)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val firebaseUser = task.result?.user
                                                val updatedProfile = SampleData.sampleUserProfile.copy(
                                                    name = firebaseUser?.displayName ?: "${config.appName} Aspirant",
                                                    email = firebaseUser?.email ?: "aspirant.$cleanPhone@exam.portal",
                                                    isProSubscribed = true,
                                                    proPlanName = "${config.appName} VIP Access",
                                                    isLoggedIn = true
                                                )
                                                SampleData.sampleUserProfile = updatedProfile
                                                coroutineScope.launch {
                                                    FirebaseSyncManager.syncUserProfileToFirestore(
                                                        profile = updatedProfile,
                                                        onSuccess = { Log.d("LoginScreen", "Profile synced") },
                                                        onFailure = { Log.w("LoginScreen", "Profile sync offline") }
                                                    )
                                                }
                                                Toast.makeText(context, "✅ Mobile Verified Automatically!", Toast.LENGTH_SHORT).show()
                                                onLoginSuccess()
                                            } else {
                                                Toast.makeText(context, "Auto-verification failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                }

                                override fun onVerificationFailed(e: FirebaseException) {
                                    isSendingOtp = false
                                    Log.e("LoginScreen", "SMS verification failed: ${e.message}", e)
                                    Toast.makeText(context, "SMS Error: ${e.localizedMessage ?: e.message}", Toast.LENGTH_LONG).show()
                                }

                                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                                    isSendingOtp = false
                                    verificationId = id
                                    resendToken = token
                                    currentStep = LoginStep.VERIFY_OTP
                                    remainingSeconds = 60
                                    for (i in 0 until 6) {
                                        otpValues[i] = ""
                                    }
                                    Toast.makeText(context, "OTP sent to +91 $cleanPhone", Toast.LENGTH_SHORT).show()
                                }
                            }

                            FirebaseSyncManager.sendSmsOtp(
                                phoneNumber = cleanPhone,
                                activity = activity,
                                callbacks = callbacks,
                                resendToken = null
                            )
                        } else {
                            Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onGoogleLogin = {
                        coroutineScope.launch {
                            try {
                                val clientId = try {
                                    context.getString(com.example.R.string.default_web_client_id)
                                } catch (e: Exception) {
                                    Log.e("LoginScreen", "default_web_client_id missing", e)
                                    Toast.makeText(context, "Google Sign-In configuration missing", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                val credentialManager = CredentialManager.create(context)
                                val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(signInOption)
                                    .build()

                                val result = credentialManager.getCredential(
                                    context = context as android.app.Activity,
                                    request = request
                                )
                                val credential = result.credential
                                if (credential is androidx.credentials.CustomCredential && 
                                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    
                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                    val idToken = googleIdTokenCredential.idToken
                                    
                                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                                    FirebaseSyncManager.auth.signInWithCredential(firebaseCredential)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val firebaseUser = task.result?.user
                                                val updatedProfile = SampleData.sampleUserProfile.copy(
                                                    name = firebaseUser?.displayName ?: "${config.appName} Aspirant",
                                                    email = firebaseUser?.email ?: "aspirant@gmail.com",
                                                    isProSubscribed = true,
                                                    isLoggedIn = true
                                                )
                                                SampleData.sampleUserProfile = updatedProfile
                                                FirebaseSyncManager.syncUserProfileToFirestore(
                                                    profile = updatedProfile,
                                                    onSuccess = {
                                                        Toast.makeText(context, "✅ Signed in with Google (Firebase Active)!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    onFailure = { e ->
                                                        Toast.makeText(context, "✅ Signed in (Firestore offline: ${e.message})", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                                onLoginSuccess()
                                            } else {
                                                Toast.makeText(context, "❌ Firebase authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(context, "❌ Unexpected credential type", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: GetCredentialCancellationException) {
                                Log.w("LoginScreen", "Google Sign-In cancelled: ${e.message}", e)
                                Toast.makeText(context, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Log.e("LoginScreen", "Credential Manager failed: ${e.message}", e)
                                Toast.makeText(context, "Sign-in failed: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onBack = onBack,
                    onSupportClick = { showSupportDialog = true }
                )
            }
            LoginStep.VERIFY_OTP -> {
                OtpVerificationContent(
                    phoneNumber = phoneNumber,
                    otpValues = otpValues,
                    remainingSeconds = remainingSeconds,
                    isVerifyingOtp = isVerifyingOtp,
                    onEditPhone = { currentStep = LoginStep.ENTER_PHONE },
                    onVerifyOtp = {
                        val enteredOtp = otpValues.joinToString("")
                        val cleanPhone = phoneNumber.filter { it.isDigit() }
                        if (enteredOtp.length == 6) {
                            if (verificationId.isBlank()) {
                                Toast.makeText(context, "Verification session expired. Please request a new OTP.", Toast.LENGTH_SHORT).show()
                                currentStep = LoginStep.ENTER_PHONE
                                return@OtpVerificationContent
                            }
                            isVerifyingOtp = true
                            FirebaseSyncManager.verifySmsOtp(
                                verificationId = verificationId,
                                otpCode = enteredOtp,
                                onSuccess = { firebaseUser ->
                                    isVerifyingOtp = false
                                    val updatedProfile = SampleData.sampleUserProfile.copy(
                                        name = firebaseUser.displayName ?: "${config.appName} Aspirant",
                                        email = firebaseUser.email ?: "aspirant.$cleanPhone@exam.portal",
                                        isProSubscribed = true,
                                        proPlanName = "${config.appName} VIP Access",
                                        isLoggedIn = true
                                    )
                                    SampleData.sampleUserProfile = updatedProfile
                                    coroutineScope.launch {
                                        FirebaseSyncManager.syncUserProfileToFirestore(
                                            profile = updatedProfile,
                                            onSuccess = {
                                                Log.d("LoginScreen", "✅ Mobile user profile synced to Cloud Firestore!")
                                            },
                                            onFailure = { e ->
                                                Log.w("LoginScreen", "Mobile verified (Firestore note: ${e.message})")
                                            }
                                        )
                                    }
                                    Toast.makeText(context, "✅ Mobile Number Verified Successfully!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                },
                                onFailure = { e ->
                                    isVerifyingOtp = false
                                    Log.e("LoginScreen", "OTP Verification failed: ${e.message}", e)
                                    Toast.makeText(context, "❌ Invalid OTP: ${e.localizedMessage ?: "Verification failed"}", Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Please enter the complete 6-digit OTP", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onResendOtp = {
                        val cleanPhone = phoneNumber.filter { it.isDigit() }
                        val activity = context as? Activity
                        if (activity != null && cleanPhone.length == 10) {
                            remainingSeconds = 60
                            for (i in 0 until 6) {
                                otpValues[i] = ""
                            }
                            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                    FirebaseSyncManager.auth.signInWithCredential(credential)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                onLoginSuccess()
                                            }
                                        }
                                }

                                override fun onVerificationFailed(e: FirebaseException) {
                                    Toast.makeText(context, "Resend failed: ${e.localizedMessage ?: e.message}", Toast.LENGTH_LONG).show()
                                }

                                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                                    verificationId = id
                                    resendToken = token
                                    Toast.makeText(context, "OTP resent to +91 $cleanPhone", Toast.LENGTH_SHORT).show()
                                }
                            }
                            FirebaseSyncManager.sendSmsOtp(
                                phoneNumber = cleanPhone,
                                activity = activity,
                                callbacks = callbacks,
                                resendToken = resendToken
                            )
                        }
                    },
                    onNeedHelp = {
                        showSupportDialog = true
                    },
                    onBack = { currentStep = LoginStep.ENTER_PHONE }
                )
            }
        }

            if (showSupportDialog) {
                SmartSupportIssueDialog(
                    issueCategory = "Login & OTP Issues",
                    extraContext = if (phoneNumber.isNotBlank()) "Mobile: +91 $phoneNumber" else "Login Screen",
                    onDismiss = { showSupportDialog = false }
                )
            }
        }
    }
}

enum class LoginStep {
    ENTER_PHONE,
    VERIFY_OTP
}

@Composable
fun AndhraPradeshEmblem(
    modifier: Modifier = Modifier,
    emblemSize: androidx.compose.ui.unit.Dp = 58.dp
) {
    val innerSurfaceSize = emblemSize * 0.70f
    val innerCanvasSize = emblemSize * 0.50f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(emblemSize)
    ) {
        // Outer Sun Rays Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val radius = size.minDimension / 2f
            val numRays = 32
            val rayLength = radius * 0.15f
            val innerRadius = radius - rayLength
            
            // Draw 32 gold-amber sun rays
            for (i in 0 until numRays) {
                val angle = (i * 2 * Math.PI / numRays).toFloat()
                val startX = center.x + innerRadius * kotlin.math.cos(angle)
                val startY = center.y + innerRadius * kotlin.math.sin(angle)
                val endX = center.x + radius * kotlin.math.cos(angle)
                val endY = center.y + radius * kotlin.math.sin(angle)
                
                drawLine(
                    color = Color(0xFFFBBF24), // Golden Amber
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
            
            // Draw inner gold circle
            drawCircle(
                color = Color(0xFFFBBF24),
                radius = innerRadius,
                style = Stroke(width = 2.5f)
            )
        }

        // Inner Emerald Circle with Double Border for Royal look
        Surface(
            shape = CircleShape,
            color = Color(0xFF0F172A).copy(alpha = 0.85f),
            border = BorderStroke(2.dp, Color(0xFF10B981)), // Emerald Green
            modifier = Modifier.size(innerSurfaceSize)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(innerCanvasSize)) {
                    val w = size.width
                    val h = size.height
                    
                    // Draw a stylized Poorna Ghatam (Full Vase with foliage) in Gold/Emerald
                    val path = Path().apply {
                        // Vase Top rim
                        moveTo(w * 0.35f, h * 0.45f)
                        lineTo(w * 0.65f, h * 0.45f)
                        
                        // Vase neck
                        quadraticBezierTo(w * 0.60f, h * 0.55f, w * 0.70f, h * 0.65f)
                        // Vase body
                        quadraticBezierTo(w * 0.75f, h * 0.80f, w * 0.50f, h * 0.85f)
                        quadraticBezierTo(w * 0.25f, h * 0.80f, w * 0.30f, h * 0.65f)
                        quadraticBezierTo(w * 0.40f, h * 0.55f, w * 0.35f, h * 0.45f)
                        close()
                    }
                    
                    // Fill Vase with gold/amber gradient
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFBBF24), Color(0xFFD97706))
                        )
                    )
                    
                    // Draw Lotus bud and coconut leaves coming out of the vase
                    // Central Lotus Bud
                    val lotusPath = Path().apply {
                        moveTo(w * 0.5f, h * 0.15f)
                        // Left petal
                        quadraticBezierTo(w * 0.35f, h * 0.30f, w * 0.5f, h * 0.45f)
                        // Right petal
                        quadraticBezierTo(w * 0.65f, h * 0.30f, w * 0.5f, h * 0.15f)
                    }
                    drawPath(
                        path = lotusPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)) // Sacred Red/Pink Lotus
                        )
                    )
                    
                    // Left Foliage/Mango Leaf
                    val leftLeaf = Path().apply {
                        moveTo(w * 0.5f, h * 0.43f)
                        quadraticBezierTo(w * 0.2f, h * 0.35f, w * 0.22f, h * 0.25f)
                        quadraticBezierTo(w * 0.35f, h * 0.28f, w * 0.5f, h * 0.43f)
                    }
                    drawPath(
                        path = leftLeaf,
                        color = Color(0xFF10B981) // Emerald Green foliage
                    )
                    
                    // Right Foliage/Mango Leaf
                    val rightLeaf = Path().apply {
                        moveTo(w * 0.5f, h * 0.43f)
                        quadraticBezierTo(w * 0.8f, h * 0.35f, w * 0.78f, h * 0.25f)
                        quadraticBezierTo(w * 0.65f, h * 0.28f, w * 0.5f, h * 0.43f)
                    }
                    drawPath(
                        path = rightLeaf,
                        color = Color(0xFF10B981)
                    )
                    
                    // Base of the emblem
                    drawLine(
                        color = Color(0xFFFBBF24),
                        start = Offset(w * 0.38f, h * 0.85f),
                        end = Offset(w * 0.62f, h * 0.85f),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

/**
 * Beautiful Andhra Pradesh & APPSC Group-2 Image Banner Box
 * Features:
 * - Premium, elegant gold-gradient borders with dual-tier depth
 * - Smooth 20.dp rounded corners with high-clarity drop shadows
 * - High-resolution Andhra Pradesh administrative landscape artwork with resilient fallback
 * - Specular gold metallic top accent and rich contrast-enhancing multi-stop gradient scrim
 * - Official APPSC Group-2 badges, Telugu inscription, and core preparation highlight tags
 */
private data class QuotationThemeColors(
    val bgBrush: Brush,
    val borderColor: Color,
    val iconColor: Color,
    val textColor: Color,
    val authorColor: Color
)

private fun parseHexColor(hex: String, defaultColor: Color): Color {
    if (hex.isBlank()) return defaultColor
    return try {
        val cleaned = hex.removePrefix("#").trim()
        val colorInt = when (cleaned.length) {
            6 -> (0xFF000000 or cleaned.toLong(16)).toInt()
            8 -> cleaned.toLong(16).toInt()
            else -> return defaultColor
        }
        Color(colorInt)
    } catch (e: Exception) {
        defaultColor
    }
}

/**
 * Premium Education & Study-Related Quotation Box
 */
@Composable
fun ApPremiumQuotationBox(
    targetScreen: String = "BOTH", // "LOGIN" or "OTP"
    modifier: Modifier = Modifier
) {
    val quotations = SampleData.loginQuotationsState
    val enabledQuotes = remember(quotations.size, quotations.map { "${it.id}_${it.isEnabled}_${it.targetScreen}_${it.order}_${it.quoteText}_${it.themeColor}" }) {
        val list = quotations.filter { it.isEnabled && (it.targetScreen == "BOTH" || it.targetScreen == targetScreen) }.sortedBy { it.order }
        if (list.isEmpty()) {
            listOf(
                LoginQuotationModel(
                    id = "fallback-quote",
                    quoteText = "“An investment in knowledge pays the best interest for ${SampleData.getActiveApp().name} aspirants.”",
                    authorOrSource = "${SampleData.getActiveApp().name} Topper's Golden Insight",
                    targetScreen = "BOTH",
                    themeColor = "GOLD",
                    isEnabled = true
                )
            )
        } else {
            list
        }
    }

    var currentQuoteIndex by remember { mutableIntStateOf(0) }
    val activeQuote = remember(currentQuoteIndex, enabledQuotes.size) {
        if (enabledQuotes.isEmpty()) null else enabledQuotes[currentQuoteIndex % enabledQuotes.size]
    }

    LaunchedEffect(enabledQuotes.size) {
        while (true) {
            delay(5500L)
            if (enabledQuotes.isNotEmpty()) {
                currentQuoteIndex = (currentQuoteIndex + 1) % enabledQuotes.size
            }
        }
    }

    if (activeQuote != null) {
        val colors = remember(activeQuote.themeColor, activeQuote.customBgHex, activeQuote.customTextHex) {
            when (activeQuote.themeColor.uppercase()) {
                "EMERALD" -> QuotationThemeColors(
                    bgBrush = Brush.horizontalGradient(listOf(Color(0xFF022C22), Color(0xFF064E3B), Color(0xFF022C22))),
                    borderColor = Color(0xFF34D399),
                    iconColor = Color(0xFF34D399),
                    textColor = Color(0xFFECFDF5),
                    authorColor = Color(0xFFA7F3D0)
                )
                "SAPPHIRE" -> QuotationThemeColors(
                    bgBrush = Brush.horizontalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF0F172A))),
                    borderColor = Color(0xFF60A5FA),
                    iconColor = Color(0xFF60A5FA),
                    textColor = Color(0xFFEFF6FF),
                    authorColor = Color(0xFF93C5FD)
                )
                "SUNSET" -> QuotationThemeColors(
                    bgBrush = Brush.horizontalGradient(listOf(Color(0xFF451A03), Color(0xFF78350F), Color(0xFF1E1B4B))),
                    borderColor = Color(0xFFF59E0B),
                    iconColor = Color(0xFFFBBF24),
                    textColor = Color(0xFFFFFBEB),
                    authorColor = Color(0xFFFDE68A)
                )
                "PURPLE" -> QuotationThemeColors(
                    bgBrush = Brush.horizontalGradient(listOf(Color(0xFF2E1065), Color(0xFF4C1D95), Color(0xFF0F172A))),
                    borderColor = Color(0xFFC084FC),
                    iconColor = Color(0xFFC084FC),
                    textColor = Color(0xFFF5F3FF),
                    authorColor = Color(0xFFDDD6FE)
                )
                "LUXURY_CREAM" -> QuotationThemeColors(
                    bgBrush = Brush.horizontalGradient(listOf(Color(0xFFFFFDF9), Color(0xFFFEF3C7), Color(0xFFFFFDF9))),
                    borderColor = Color(0xFFD97706),
                    iconColor = Color(0xFFD97706),
                    textColor = Color(0xFF1E293B),
                    authorColor = Color(0xFF78350F)
                )
                "SLATE" -> QuotationThemeColors(
                    bgBrush = Brush.horizontalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))),
                    borderColor = Color(0xFF94A3B8),
                    iconColor = Color(0xFF38BDF8),
                    textColor = Color(0xFFF8FAFC),
                    authorColor = Color(0xFFCBD5E1)
                )
                "CUSTOM_HEX" -> {
                    val customBg = parseHexColor(activeQuote.customBgHex, Color(0xFF0F172A))
                    val customText = parseHexColor(activeQuote.customTextHex, Color.White)
                    QuotationThemeColors(
                        bgBrush = Brush.horizontalGradient(listOf(customBg, customBg.copy(alpha = 0.85f))),
                        borderColor = customText.copy(alpha = 0.7f),
                        iconColor = customText,
                        textColor = customText,
                        authorColor = customText.copy(alpha = 0.85f)
                    )
                }
                else -> QuotationThemeColors( // GOLD
                    bgBrush = Brush.horizontalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF2D1B00), Color(0xFF1E1B4B))),
                    borderColor = Color(0xFFFFD700),
                    iconColor = Color(0xFFFBBF24),
                    textColor = Color(0xFFFFFBEB),
                    authorColor = Color(0xFFFDE68A)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.Transparent,
            border = BorderStroke(1.2.dp, colors.borderColor),
            shadowElevation = 3.dp,
            modifier = modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .background(colors.bgBrush)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = "Quotation",
                        tint = colors.iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    AnimatedContent(
                        targetState = activeQuote,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                        },
                        label = "QuoteAnimation",
                        modifier = Modifier.weight(1f)
                    ) { quote ->
                        Column {
                            Text(
                                text = quote.quoteText,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textColor,
                                lineHeight = 15.sp
                            )
                            if (quote.authorOrSource.isNotBlank()) {
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "— ${quote.authorOrSource}",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.authorColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApPremiumImageCarouselBox(
    targetScreen: String = "BOTH", // "LOGIN" or "OTP"
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = SampleData.loginCarouselHeightDpState.intValue.dp
) {
    val apBackgroundsState = SampleData.loginBackgroundsState
    val enabledImages = remember(apBackgroundsState.size, apBackgroundsState.map { "${it.id}_${it.isEnabled}_${it.targetScreen}_${it.order}" }) {
        val list = apBackgroundsState.filter { it.isEnabled && (it.targetScreen == "BOTH" || it.targetScreen == targetScreen) }.sortedBy { it.order }
        if (list.isEmpty()) {
            listOf(
                LoginBgImageModel(
                    id = "fallback-araku",
                    url = "https://images.unsplash.com/photo-1627483262769-04d0a1401487?q=80&w=1200&auto=format&fit=crop",
                    title = "Araku Valley",
                    subtitle = "",
                    isEnabled = true,
                    order = 0
                ),
                LoginBgImageModel(
                    id = "fallback-culture",
                    url = "https://images.unsplash.com/photo-1599707367072-cd6ada2bc375?q=80&w=1200&auto=format&fit=crop",
                    title = "Andhra Culture",
                    subtitle = "",
                    isEnabled = true,
                    order = 1
                ),
                LoginBgImageModel(
                    id = "fallback-tirupati",
                    url = "https://images.unsplash.com/photo-1610123598147-f632aa18b875?q=80&w=1200&auto=format&fit=crop",
                    title = "Tirumala Hills",
                    subtitle = "",
                    isEnabled = true,
                    order = 2
                )
            )
        } else {
            list
        }
    }

    val context = LocalContext.current
    var currentIndex by remember { mutableIntStateOf(0) }
    val activeIndex = remember(currentIndex, enabledImages.size) {
        if (enabledImages.isEmpty()) 0 else currentIndex % enabledImages.size
    }

    // High-performance background preloading of all carousel images via Coil ImageLoader
    LaunchedEffect(enabledImages) {
        val loader = Coil.imageLoader(context)
        enabledImages.forEach { imgItem ->
            val request = ImageRequest.Builder(context)
                .data(imgItem.url)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .allowHardware(true)
                .crossfade(true)
                .build()
            loader.enqueue(request)
        }
    }

    // Configurable Auto-slide timing from Admin
    val slideTimingSeconds = SampleData.loginCarouselAutoSlideSecondsState.intValue.coerceIn(2, 15)
    LaunchedEffect(enabledImages.size, slideTimingSeconds) {
        while (true) {
            delay(slideTimingSeconds * 1000L)
            if (enabledImages.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % enabledImages.size
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(
            width = 1.8.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFD700), // Pure Gold
                    Color(0xFFF59E0B), // Warm Amber
                    Color(0xFFFFFBEB), // Soft Gold Sheen
                    Color(0xFFD97706), // Warm Bronze Gold
                    Color(0xFFFFD700)  // Pure Gold
                )
            )
        ),
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Background canvas for loading / transitions
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF0F2C59))
                    )
                )
            }

            // Smooth Auto-sliding Images with Crossfade and Subtle Slide Transition
            AnimatedContent(
                targetState = activeIndex,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(850, easing = FastOutSlowInEasing)) +
                     slideInHorizontally(animationSpec = tween(850, easing = FastOutSlowInEasing)) { fullWidth -> fullWidth / 4 })
                        .togetherWith(
                            fadeOut(animationSpec = tween(650, easing = FastOutSlowInEasing)) +
                            slideOutHorizontally(animationSpec = tween(650, easing = FastOutSlowInEasing)) { fullWidth -> -fullWidth / 4 }
                        )
                },
                label = "ApCarouselSlideAnimation",
                modifier = Modifier.fillMaxSize()
            ) { targetIndex ->
                val currentImageItem = enabledImages.getOrNull(targetIndex) ?: enabledImages.firstOrNull()

                if (currentImageItem != null) {
                    val imageRequest = remember(currentImageItem.url, context) {
                        ImageRequest.Builder(context)
                            .data(currentImageItem.url)
                            .crossfade(true)
                            .allowHardware(true)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build()
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = "Andhra Pradesh Heritage",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Inner Fine Gold Inset Border Accent for Royal Luxury Framing
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.55f),
                                Color(0xFFFBBF24).copy(alpha = 0.25f),
                                Color(0xFFFFD700).copy(alpha = 0.55f)
                            )
                        ),
                        shape = RoundedCornerShape(15.dp)
                    )
            )

            // Top specular shine highlight line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFFFD700).copy(alpha = 0.85f),
                                Color(0xFFFFFBEB),
                                Color(0xFFFFD700).copy(alpha = 0.85f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Minimalist dot indicators
            if (enabledImages.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    enabledImages.forEachIndexed { dotIdx, _ ->
                        val isSelected = dotIdx == activeIndex
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 6.dp else 4.5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppscBannerBox(modifier: Modifier = Modifier) {
    ApPremiumImageCarouselBox(modifier = modifier, height = 150.dp)
}

/**
 * Premium Andhra Pradesh Heritage Decorative Area (Placed between Carousel and Login/OTP Card)
 * Displays:
 * - Royal Midnight Navy canvas with golden horizon glow & Prakasam Barrage arch reflections
 * - Golden Amaravati Buddha silhouette (Left)
 * - "Dream Prepare Achieve" gold script typography with swoosh underline
 * - "A BRIGHTER ANDHRA / A BRIGHTER YOU"
 * - Golden Andhra Pradesh map outline + Gopuram temple spire silhouette (Right)
 */
@Composable
fun ApHeritageDecorationArea(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(118.dp),
        contentAlignment = Alignment.Center
    ) {
        // Heritage Background Canvas (Night sky, river, barrage, Buddha, AP Map & Gopuram)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Deep royal navy background
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF040B18),
                        Color(0xFF091428),
                        Color(0xFF0D1E3A),
                        Color(0xFF050E20)
                    )
                )
            )

            // 2. Horizon Golden Glow over River
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFBBF24).copy(alpha = 0.35f),
                        Color(0xFFF59E0B).copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.50f, h * 0.75f),
                    radius = w * 0.45f
                )
            )

            // 3. Prakasam Barrage / Godavari Bridge arch silhouette
            val bridgeY = h * 0.72f
            drawLine(
                color = Color(0xFFF59E0B).copy(alpha = 0.40f),
                start = Offset(w * 0.08f, bridgeY),
                end = Offset(w * 0.92f, bridgeY),
                strokeWidth = 2f
            )

            // Draw 8 arches along the barrage line
            val numArches = 8
            val archWidth = (w * 0.84f) / numArches
            val archStartX = w * 0.08f
            for (i in 0 until numArches) {
                val x1 = archStartX + i * archWidth
                val x2 = x1 + archWidth
                val archPath = Path().apply {
                    moveTo(x1, bridgeY)
                    quadraticBezierTo((x1 + x2) / 2f, bridgeY - 10f, x2, bridgeY)
                }
                drawPath(
                    path = archPath,
                    color = Color(0xFFFBBF24).copy(alpha = 0.35f),
                    style = Stroke(width = 1.5f)
                )
            }

            // River Water Reflection Waves
            val waterPath = Path().apply {
                moveTo(w * 0.15f, h * 0.82f)
                quadraticBezierTo(w * 0.50f, h * 0.80f, w * 0.85f, h * 0.82f)
            }
            drawPath(
                path = waterPath,
                color = Color(0xFFFDE68A).copy(alpha = 0.25f),
                style = Stroke(width = 1f)
            )

            // 4. Left Side: Buddha Silhouette (Amaravati Buddha)
            val buddhaX = w * 0.12f
            val buddhaY = h * 0.52f
            // Halo circle
            drawCircle(
                color = Color(0xFFFDE68A).copy(alpha = 0.35f),
                center = Offset(buddhaX, buddhaY - 14f),
                radius = 12f,
                style = Stroke(width = 1.5f)
            )
            // Head
            drawCircle(
                color = Color(0xFFD97706).copy(alpha = 0.75f),
                center = Offset(buddhaX, buddhaY - 14f),
                radius = 7f
            )
            // Body / Dhyana Mudra sitting outline
            val buddhaPath = Path().apply {
                moveTo(buddhaX, buddhaY - 7f)
                quadraticBezierTo(buddhaX - 16f, buddhaY + 4f, buddhaX - 18f, buddhaY + 16f)
                lineTo(buddhaX + 18f, buddhaY + 16f)
                quadraticBezierTo(buddhaX + 16f, buddhaY + 4f, buddhaX, buddhaY - 7f)
                close()
            }
            drawPath(
                path = buddhaPath,
                color = Color(0xFFD97706).copy(alpha = 0.65f)
            )

            // 5. Right Side: Andhra Pradesh Map Outline & Temple Gopuram Spire
            val apX = w * 0.88f
            val apY = h * 0.45f
            val mapPath = Path().apply {
                moveTo(apX, apY - 18f)
                quadraticBezierTo(apX - 12f, apY - 6f, apX - 18f, apY + 10f)
                quadraticBezierTo(apX - 8f, apY + 22f, apX + 4f, apY + 20f)
                quadraticBezierTo(apX + 12f, apY + 8f, apX + 6f, apY - 8f)
                close()
            }
            drawPath(
                path = mapPath,
                color = Color(0xFFF59E0B).copy(alpha = 0.30f)
            )
            drawPath(
                path = mapPath,
                color = Color(0xFFFFD700).copy(alpha = 0.50f),
                style = Stroke(width = 1.2f)
            )

            // Temple Gopuram Spire on Right
            val gopuramX = w * 0.93f
            val gopuramY = h * 0.65f
            val gopuramPath = Path().apply {
                moveTo(gopuramX, gopuramY - 22f)
                lineTo(gopuramX - 8f, gopuramY + 10f)
                lineTo(gopuramX + 8f, gopuramY + 10f)
                close()
            }
            drawPath(
                path = gopuramPath,
                color = Color(0xFFFBBF24).copy(alpha = 0.45f)
            )
        }

        // Foreground Text Stack
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // Cursive Script Title
            Text(
                text = "Dream   Prepare   Achieve",
                color = Color(0xFFFDE68A),
                fontSize = 20.sp,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Golden Swoosh Underline Accent
            Canvas(
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
            ) {
                val w = size.width
                val h = size.height
                val swooshPath = Path().apply {
                    moveTo(0f, h * 0.2f)
                    quadraticBezierTo(w * 0.5f, h * 0.9f, w, h * 0.2f)
                }
                drawPath(
                    path = swooshPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFFFD700),
                            Color(0xFFF59E0B),
                            Color(0xFFFFD700),
                            Color.Transparent
                        )
                    ),
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle Uppercase
            val activeRegionText = SampleData.getActiveApp().region.ifBlank { "FUTURE" }.uppercase()
            Text(
                text = "A BRIGHTER $activeRegionText",
                color = Color(0xFFFDE68A).copy(alpha = 0.9f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp
            )
            Text(
                text = "A BRIGHTER YOU",
                color = Color(0xFFFBBF24),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp
            )
        }
    }
}

/**
 * Bottom Footer Motto Text (Placed below Login / OTP Verification card)
 * Displays:
 * - Cursive gold script "Your Effort Our Support A Better Tomorrow"
 * - Golden swoosh curve accent line underneath
 */
/**
 * Bottom Footer Motto Card
 * Displays:
 * "Better Governance • A Brighter Tomorrow • Excellence in Education"
 */
@Composable
fun ApFooterMotto(
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF091224).copy(alpha = 0.70f),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "“Better Governance • A Brighter Tomorrow • Excellence in\nEducation”",
            color = Color(0xFFE2E8F0),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

data class ReferenceCarouselItem(
    val title: String,
    val subtitle: String,
    val badgeText: String,
    val imageUrl: String
)

@Composable
fun PhoneLoginContent(
    phoneNumber: String,
    onPhoneNumberChanged: (String) -> Unit,
    onContinueWithOtp: () -> Unit,
    onGoogleLogin: () -> Unit,
    onBack: () -> Unit,
    onAdminPanelClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    isSendingOtp: Boolean = false
) {
    val config = LocalAppConfig.current
    val context = LocalContext.current
    val culturalRepo = remember { com.example.repository.CulturalCarouselRepository.getInstance(context) }
    val culturalItems by culturalRepo.enabledItems.collectAsState()

    val isValidStartDigit = phoneNumber.isEmpty() || phoneNumber.first() in listOf('6', '7', '8', '9')
    val isPhoneFullLength = phoneNumber.length == 10
    val isPhoneValid = isPhoneFullLength && isValidStartDigit

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. TOP TAGLINE: "Knowledge is the Path to Success" with right underline
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 2.dp, end = 2.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Knowledge is the Path to Success",
                        color = Color(0xFFFCD34D),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color(0xFFFCD34D))
                    )
                }
            }

            // 2. BRAND / EXAM CARD: APPSC GROUP 2
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF090F1E),
                border = BorderStroke(
                    width = 1.4.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF59E0B),
                            Color(0xFFFBBF24),
                            Color(0xFF06B6D4),
                            Color(0xFF3B82F6),
                            Color(0xFF8B5CF6)
                        )
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo container: gold-bordered square with sun disc & red pedestal
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0C162D))
                            .border(1.4.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(17.dp)
                                    .background(Color(0xFFFBBF24), CircleShape)
                            )
                            Spacer(modifier = Modifier.height(2.5.dp))
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height(5.5.dp)
                                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                                    .background(Color(0xFFDC2626))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Subtitle
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = config.appName,
                                color = Color(0xFFFCD34D),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${config.appName} Preparation Portal",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // OFFICIAL Badge
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF042F2E),
                        border = BorderStroke(1.2.dp, Color(0xFF10B981))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.5.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "OFFICIAL",
                                color = Color(0xFF10B981),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }

            // 3. ANDHRA PRADESH CULTURAL IMAGE CAROUSEL
            com.example.ui.components.AndhraCulturalCarousel(
                items = culturalItems,
                height = 210.dp,
                compactMode = false
            )
            // 4. LOGIN CARD - Translucent Glassmorphic Card with Teal/Emerald and Gold Glow
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF040A1A).copy(alpha = 0.85f), // Translucent Dark Glass Surface
                border = BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF14B8A6), // Teal
                            Color(0xFF059669), // Emerald
                            Color(0xFFFBBF24), // Gold
                            Color(0xFFF59E0B), // Amber Gold
                            Color(0xFF14B8A6)  // Teal
                        )
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    // ✨ CANDIDATE LOGIN ✨ - Custom pill shape badge
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF042F2E).copy(alpha = 0.80f), // Dark Teal/Emerald Background
                        border = BorderStroke(1.2.dp, Color(0xFF06B6D4)) // Cyan glow border
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "✨", fontSize = 11.sp, color = Color(0xFFFBBF24))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CANDIDATE LOGIN",
                                color = Color(0xFFFCD34D), // Golden Text
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "✨", fontSize = 11.sp, color = Color(0xFFFBBF24))
                        }
                    }

                    // Title in Rich Gold
                    Text(
                        text = "Login with Mobile & Instant OTP",
                        color = Color(0xFFFDE68A), // Rich Gold Typo
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    // Subtitle
                    Text(
                        text = "Enter your mobile number to receive OTP",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )

                    // Mobile Number Field Container
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Mobile Number (10 Digits)",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF042F2E).copy(alpha = 0.60f), // Deep teal background
                            border = BorderStroke(1.5.dp, Color(0xFF06B6D4)), // Cyan glowing border
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // +91 Capsule with Indian Flag
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0C162D),
                                    border = BorderStroke(1.2.dp, Color(0xFFF59E0B)),
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Flag
                                        Box(
                                            modifier = Modifier
                                                .size(width = 20.dp, height = 13.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                                        ) {
                                            Column(modifier = Modifier.fillMaxSize()) {
                                                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFFF9933)))
                                                Box(
                                                    modifier = Modifier.weight(1f).fillMaxWidth().background(Color.White),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Box(modifier = Modifier.size(3.dp).background(Color(0xFF000080), CircleShape))
                                                }
                                                Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF138808)))
                                            }
                                        }
                                        Text(
                                            text = "+91",
                                            color = Color(0xFFF59E0B),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Number input
                                Box(modifier = Modifier.weight(1f)) {
                                    if (phoneNumber.isEmpty()) {
                                        Text(
                                            text = "Enter Mobile Number",
                                            color = Color(0xFF64748B),
                                            fontSize = 16.sp,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    BasicTextField(
                                        value = phoneNumber,
                                        onValueChange = {
                                            if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                                                onPhoneNumberChanged(it)
                                            }
                                        },
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            letterSpacing = 1.sp
                                        ),
                                        cursorBrush = SolidColor(Color(0xFFFBBF24)),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Green verification checkmark badge
                                if (phoneNumber.length == 10) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color(0xFF22C55E), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Verified",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val cleanPhone = phoneNumber.filter { it.isDigit() }
                    if (cleanPhone.length == 10) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "OTP will be sent to +91 $cleanPhone",
                                color = Color(0xFF34D399),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Continue with OTP Button
                    Button(
                        onClick = onContinueWithOtp,
                        enabled = !isSendingOtp,
                        shape = RoundedCornerShape(25.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("send_otp_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF06B6D4), // Cyan
                                            Color(0xFF0D9488), // Teal
                                            Color(0xFFEA580C), // Orange
                                            Color(0xFFFBBF24)  // Gold
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSendingOtp) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sending OTP...",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Continue with OTP",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.3.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Divider: OR QUICK CONNECT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, Color(0xFF0284C7).copy(alpha = 0.6f))
                                    )
                                )
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF042F2E).copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, Color(0xFF0D9488)),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "OR QUICK CONNECT",
                                color = Color(0xFF2DD4BF),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF0284C7).copy(alpha = 0.6f), Color.Transparent)
                                    )
                                )
                        )
                    }

                    // Continue with Google (Gmail)
                    Surface(
                        onClick = onGoogleLogin,
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF040A1A).copy(alpha = 0.65f), // Glassmorphic translucent dark
                        border = BorderStroke(
                            width = 1.3.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF14B8A6), // Teal
                                    Color(0xFF0D9488), // Deep Teal
                                    Color(0xFFFBBF24)  // Gold
                                )
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // White Google "G" Circle
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(18.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    drawArc(color = Color(0xFF4285F4), startAngle = -90f, sweepAngle = 90f, useCenter = true, size = androidx.compose.ui.geometry.Size(w, h))
                                    drawArc(color = Color(0xFF34A853), startAngle = 0f, sweepAngle = 90f, useCenter = true, size = androidx.compose.ui.geometry.Size(w, h))
                                    drawArc(color = Color(0xFFFBBC05), startAngle = 90f, sweepAngle = 90f, useCenter = true, size = androidx.compose.ui.geometry.Size(w, h))
                                    drawArc(color = Color(0xFFEA4335), startAngle = 180f, sweepAngle = 90f, useCenter = true, size = androidx.compose.ui.geometry.Size(w, h))
                                }
                                Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(9.dp)) {}
                                Box(
                                    modifier = Modifier
                                        .width(6.5.dp)
                                        .height(2.5.dp)
                                        .offset(x = 3.dp, y = 0.dp)
                                        .background(Color(0xFF4285F4), RoundedCornerShape(1.dp))
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Continue with Google (Gmail)",
                                        color = Color.White,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF2563EB)
                                    ) {
                                        Text(
                                            text = "FAST",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Sign in directly with your Google account",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // 5. ADMIN PANEL & LOGIN SUPPORT
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onSupportClick)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ContactSupport,
                    contentDescription = null,
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Login / OTP Help",
                    color = Color(0xFFA7F3D0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 6. FOOTER MOTTO
            ApFooterMotto(
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

@Composable
fun FeatureBadgeItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
    }
}

@Composable
fun TrustBadgeItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(text = subtitle, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun OtpVerificationContent(
    phoneNumber: String,
    otpValues: MutableList<String>,
    remainingSeconds: Int,
    onEditPhone: () -> Unit,
    onVerifyOtp: () -> Unit,
    onResendOtp: () -> Unit,
    onNeedHelp: () -> Unit,
    onBack: () -> Unit,
    isVerifyingOtp: Boolean = false
) {
    val config = LocalAppConfig.current
    val context = LocalContext.current
    val bgThemeKey = SampleData.loginScreenBgThemeState.value
    val bgBrush = remember(bgThemeKey) { LoginThemeResolver.getScreenBgBrush(bgThemeKey) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. TOP HEADER CARD: "APPSC GROUP 2" + "OFFICIAL" Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF080F23).copy(alpha = 0.92f),
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFACC15), // Gold
                            Color(0xFF10B981), // Emerald Green
                            Color(0xFF3B82F6), // Electric Blue
                            Color(0xFF8B5CF6), // Purple
                            Color(0xFFEC4899), // Pink
                            Color(0xFF06B6D4)  // Cyan
                        )
                    )
                ),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Yellow / Gold Emblem Square Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF08A),
                            border = BorderStroke(1.dp, Color(0xFFEAB308)),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Canvas(modifier = Modifier.size(32.dp)) {
                                    drawCircle(color = Color(0xFFEAB308), radius = size.minDimension / 2f)
                                    drawCircle(color = Color(0xFFDC2626), radius = size.minDimension / 3.2f)
                                    drawRect(
                                        color = Color(0xFFCA8A04),
                                        topLeft = Offset(size.width * 0.25f, size.height * 0.65f),
                                        size = Size(size.width * 0.5f, size.height * 0.2f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = config.appName,
                                    style = TextStyle(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFBBF24)
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${config.appName} Preparation Portal",
                                style = TextStyle(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // "OFFICIAL" Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF042F2C),
                        border = BorderStroke(1.2.dp, Color(0xFF10B981))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "OFFICIAL",
                                style = TextStyle(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }

            // 2. ANDHRA PRADESH CULTURAL IMAGE CAROUSEL (Compact Mode)
            val culturalRepo = remember { com.example.repository.CulturalCarouselRepository.getInstance(context) }
            val culturalItems by culturalRepo.enabledItems.collectAsState()
            com.example.ui.components.AndhraCulturalCarousel(
                items = culturalItems,
                height = 160.dp,
                compactMode = true
            )

            // 3. CANDIDATE LOGIN MAIN CARD CONTAINER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF040A1A).copy(alpha = 0.85f), // Glassmorphic translucent dark surface
                    border = BorderStroke(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF14B8A6), // Teal
                                Color(0xFF059669), // Emerald
                                Color(0xFFFBBF24), // Gold
                                Color(0xFFF59E0B), // Amber Gold
                                Color(0xFF14B8A6)  // Teal
                            )
                        )
                    ),
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(modifier = Modifier.height(6.dp))

                        // Yellow/Gold Icon Container
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            shadowElevation = 6.dp,
                            modifier = Modifier.size(62.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFFFEF08A), Color(0xFFEAB308))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(38.dp)) {
                                    drawRoundRect(
                                        color = Color(0xFF1E293B),
                                        topLeft = Offset(size.width * 0.25f, size.height * 0.1f),
                                        size = Size(size.width * 0.5f, size.height * 0.8f),
                                        cornerRadius = CornerRadius(8f, 8f)
                                    )
                                    drawRoundRect(
                                        color = Color(0xFF38BDF8),
                                        topLeft = Offset(size.width * 0.28f, size.height * 0.18f),
                                        size = Size(size.width * 0.44f, size.height * 0.55f),
                                        cornerRadius = CornerRadius(4f, 4f)
                                    )
                                    drawCircle(
                                        color = Color(0xFFCA8A04),
                                        radius = size.width * 0.18f,
                                        center = Offset(size.width * 0.5f, size.height * 0.55f)
                                    )
                                    drawCircle(
                                        color = Color(0xFFFEF08A),
                                        radius = size.width * 0.1f,
                                        center = Offset(size.width * 0.5f, size.height * 0.55f)
                                    )
                                }
                            }
                        }

                        // Title
                        Text(
                            text = "Enter 6-Digit OTP",
                            style = TextStyle(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )

                        // Subtitle
                        Text(
                            text = "Enter security verification code",
                            style = TextStyle(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF94A3B8)
                            )
                        )



                        // Phone Info Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Code sent to +91 $phoneNumber",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE2E8F0)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onEditPhone,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Mobile Number",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // 6 OTP Digit Boxes Row using Premium Palette (Teal, Cyan, Gold)
                        val boxColors = listOf(
                            Color(0xFF14B8A6), // Box 1: Teal
                            Color(0xFF06B6D4), // Box 2: Cyan
                            Color(0xFFFBBF24), // Box 3: Gold
                            Color(0xFF14B8A6), // Box 4: Teal
                            Color(0xFF06B6D4), // Box 5: Cyan
                            Color(0xFFFBBF24)  // Box 6: Gold
                        )

                        val focusRequesters = remember { List(6) { FocusRequester() } }
                        var hasAutoVerified by remember { mutableStateOf(false) }

                        LaunchedEffect(otpValues.toList()) {
                            val entered = otpValues.joinToString("")
                            if (entered.length == 6 && !hasAutoVerified && !isVerifyingOtp) {
                                hasAutoVerified = true
                                onVerifyOtp()
                            } else if (entered.length < 6) {
                                hasAutoVerified = false
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            for (i in 0 until 6) {
                                val digit = otpValues.getOrElse(i) { "" }
                                val boxColor = boxColors[i % boxColors.size]

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0B1528),
                                    border = BorderStroke(1.5.dp, boxColor),
                                    modifier = Modifier
                                        .width(46.dp)
                                        .height(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        BasicTextField(
                                            value = digit,
                                            onValueChange = { newVal ->
                                                val clean = newVal.filter { it.isDigit() }
                                                if (clean.length >= 6) {
                                                    for (idx in 0 until 6) {
                                                        if (idx < otpValues.size) {
                                                            otpValues[idx] = clean[idx].toString()
                                                        }
                                                    }
                                                } else if (clean.isNotEmpty()) {
                                                    otpValues[i] = clean.last().toString()
                                                    if (i < 5) {
                                                        try {
                                                            focusRequesters[i + 1].requestFocus()
                                                        } catch (_: Exception) {}
                                                    }
                                                } else {
                                                    otpValues[i] = ""
                                                }
                                            },
                                            textStyle = TextStyle(
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                textAlign = TextAlign.Center,
                                                color = boxColor
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            cursorBrush = SolidColor(boxColor),
                                            modifier = Modifier
                                                .focusRequester(focusRequesters[i])
                                                .onKeyEvent { keyEvent ->
                                                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace) {
                                                        if (otpValues[i].isEmpty() && i > 0) {
                                                            otpValues[i - 1] = ""
                                                            try {
                                                                focusRequesters[i - 1].requestFocus()
                                                            } catch (_: Exception) {}
                                                            true
                                                        } else if (otpValues[i].isNotEmpty()) {
                                                            otpValues[i] = ""
                                                            true
                                                        } else {
                                                            false
                                                        }
                                                    } else {
                                                        false
                                                    }
                                                }
                                        )
                                    }
                                }
                            }
                        }

                        // Resend Timer Pill Card
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF0D162B),
                            border = BorderStroke(1.2.dp, Color(0xFFD97706))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (remainingSeconds > 0) {
                                    val formattedTime = String.format("00:%02d", remainingSeconds)
                                    Text(
                                        text = "Didn't receive OTP? Resend in $formattedTime",
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFFBBF24)
                                        )
                                    )
                                } else {
                                    Text(
                                        text = "Didn't receive OTP? ",
                                        style = TextStyle(fontSize = 12.sp, color = Color(0xFFCBD5E1))
                                    )
                                    Text(
                                        text = "Resend OTP",
                                        modifier = Modifier.clickable { onResendOtp() },
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEF4444)
                                        )
                                    )
                                }
                            }
                        }

                        // Verify & Start Learning Button
                        val haptic = LocalHapticFeedback.current
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onVerifyOtp()
                            },
                            enabled = !isVerifyingOtp,
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("verify_otp_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF06B6D4), // Cyan
                                                Color(0xFF0D9488), // Teal
                                                Color(0xFFEA580C), // Orange
                                                Color(0xFFFBBF24)  // Gold
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isVerifyingOtp) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Verifying Code...",
                                            style = TextStyle(
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Verify & Start Learning",
                                            style = TextStyle(
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // TOP OVERLAY BADGE ("✨ CANDIDATE LOGIN ✨")
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF042F2E).copy(alpha = 0.80f), // Dark Teal/Emerald Background
                    border = BorderStroke(
                        width = 1.2.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF06B6D4), Color(0xFFFBBF24), Color(0xFF06B6D4)) // Cyan and Gold Gradient
                        )
                    ),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-14).dp)
                ) {
                    Text(
                        text = "✨ CANDIDATE LOGIN ✨",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFDE047),
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }
            }

            // 4. ADMIN PANEL LINK
            Row(
                modifier = Modifier
                    .clickable { onNeedHelp() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Admin Panel",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFBBF24)
                    )
                )
            }

            // 5. BOTTOM MOTTO CARD
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF081024),
                border = BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF3B82F6).copy(alpha = 0.5f),
                            Color(0xFF06B6D4).copy(alpha = 0.5f)
                        )
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "“Better Governance • A Brighter Tomorrow • Excellence in\nEducation”",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}
