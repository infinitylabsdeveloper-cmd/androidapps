package com.example.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.Coil
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.math.abs

/**
 * Standard Image Specifications for every upload field in the Admin Panel.
 */
enum class UploadImageType(
    val title: String,
    val recommendedWidth: Int,
    val recommendedHeight: Int,
    val ratioLabel: String,
    val targetRatio: Float, // width / height
    val minWidth: Int,
    val minHeight: Int,
    val format: String = "JPG / PNG / WebP",
    val tip: String
) {
    APP_WALLPAPER(
        title = "App/Home Wallpaper",
        recommendedWidth = 1440,
        recommendedHeight = 3200,
        ratioLabel = "9:20",
        targetRatio = 9f / 20f,
        minWidth = 720,
        minHeight = 1600,
        tip = "Full-screen vertical background. Recommended 1440 × 3200 px for crisp AMOLED screens."
    ),
    MOBILE_WALLPAPER(
        title = "Mobile Screen Wallpaper",
        recommendedWidth = 1080,
        recommendedHeight = 2400,
        ratioLabel = "9:20",
        targetRatio = 9f / 20f,
        minWidth = 540,
        minHeight = 1200,
        tip = "Standard modern smartphone ratio. Ensures content cards layer cleanly without stretching."
    ),
    TABLET_WALLPAPER(
        title = "Tablet Wallpaper",
        recommendedWidth = 1600,
        recommendedHeight = 2560,
        ratioLabel = "10:16",
        targetRatio = 10f / 16f,
        minWidth = 800,
        minHeight = 1280,
        tip = "Optimized for large tablet displays and foldables in portrait orientation."
    ),
    HEADER_BANNER(
        title = "Header / Top Banner",
        recommendedWidth = 1600,
        recommendedHeight = 400,
        ratioLabel = "4:1",
        targetRatio = 4f / 1f,
        minWidth = 800,
        minHeight = 200,
        tip = "Wide horizontal header. Darker or subdued graphics work best behind title text."
    ),
    SUBJECT_IMAGE(
        title = "Subject Image",
        recommendedWidth = 800,
        recommendedHeight = 600,
        ratioLabel = "4:3",
        targetRatio = 4f / 3f,
        minWidth = 400,
        minHeight = 300,
        tip = "Standard 4:3 card thumbnail. Focus subject art in the center 4:3 area."
    ),
    UNIT_IMAGE(
        title = "Unit Image",
        recommendedWidth = 800,
        recommendedHeight = 600,
        ratioLabel = "4:3",
        targetRatio = 4f / 3f,
        minWidth = 400,
        minHeight = 300,
        tip = "Unit card thumbnail. Matches the subject exploration curriculum view."
    ),
    TOPIC_IMAGE(
        title = "Topic Image",
        recommendedWidth = 800,
        recommendedHeight = 600,
        ratioLabel = "4:3",
        targetRatio = 4f / 3f,
        minWidth = 400,
        minHeight = 300,
        tip = "Topic lesson visual. Displays nicely in list items and topic cards."
    ),
    MOCK_TEST_IMAGE(
        title = "Mock Test Image",
        recommendedWidth = 1200,
        recommendedHeight = 675,
        ratioLabel = "16:9",
        targetRatio = 16f / 9f,
        minWidth = 600,
        minHeight = 338,
        tip = "Widescreen 16:9 banner for test series and mock exam cards."
    ),
    CURRENT_AFFAIRS_CARD(
        title = "Current Affairs Card Image",
        recommendedWidth = 1080,
        recommendedHeight = 1350,
        ratioLabel = "4:5",
        targetRatio = 4f / 5f,
        minWidth = 540,
        minHeight = 675,
        tip = "Portrait 4:5 ratio. Ideal for daily news highlights and editorial snippets."
    ),
    CURRENT_AFFAIRS_MONTH_SET(
        title = "Current Affairs Month/Set Image",
        recommendedWidth = 1080,
        recommendedHeight = 1350,
        ratioLabel = "4:5",
        targetRatio = 4f / 5f,
        minWidth = 540,
        minHeight = 675,
        tip = "Monthly capsule cover. Displays prominently in set grids."
    ),
    POSTER_BANNER(
        title = "Poster / Promotional Banner",
        recommendedWidth = 1080,
        recommendedHeight = 1350,
        ratioLabel = "4:5",
        targetRatio = 4f / 5f,
        minWidth = 540,
        minHeight = 675,
        tip = "Promotional banner. Keep critical logos and text within the central 80% safe zone."
    ),
    PROFILE_IMAGE(
        title = "Profile Image",
        recommendedWidth = 800,
        recommendedHeight = 800,
        ratioLabel = "1:1",
        targetRatio = 1f / 1f,
        minWidth = 300,
        minHeight = 300,
        tip = "Square 1:1 avatar. Appears circularly cropped in app navigation and profile header."
    ),
    MENU_FEATURE_IMAGE(
        title = "Menu/Feature Image",
        recommendedWidth = 800,
        recommendedHeight = 800,
        ratioLabel = "1:1",
        targetRatio = 1f / 1f,
        minWidth = 300,
        minHeight = 300,
        tip = "Square icon or card visual for drawer and quick feature tiles."
    ),
    NOTIFICATION_IMAGE(
        title = "Notification Image",
        recommendedWidth = 1200,
        recommendedHeight = 600,
        ratioLabel = "2:1",
        targetRatio = 2f / 1f,
        minWidth = 600,
        minHeight = 300,
        tip = "Wide notification banner for rich push alerts."
    )
}

/**
 * Extracted dimensions and metadata from an uploaded or linked image.
 */
data class UploadedImageMetadata(
    val width: Int = 0,
    val height: Int = 0,
    val fileSizeBytes: Long = 0L,
    val mimeType: String = "",
    val error: String? = null
) {
    val isValid: Boolean get() = width > 0 && height > 0
    val aspectDecimal: Float get() = if (height > 0) width.toFloat() / height.toFloat() else 1f

    val formattedDimensions: String get() = if (isValid) "$width × $height px" else "Unknown"

    val formattedRatio: String get() = if (isValid) {
        val gcdVal = gcd(width, height)
        val rw = width / gcdVal
        val rh = height / gcdVal
        if (rw <= 24 && rh <= 24) "$rw:$rh" else String.format(Locale.US, "%.2f:1", aspectDecimal)
    } else "Unknown"

    val formattedSize: String get() = when {
        fileSizeBytes <= 0L -> ""
        fileSizeBytes < 1024L -> "$fileSizeBytes B"
        fileSizeBytes < 1024L * 1024L -> String.format(Locale.US, "%.1f KB", fileSizeBytes / 1024f)
        else -> String.format(Locale.US, "%.2f MB", fileSizeBytes / (1024f * 1024f))
    }

    private fun gcd(a: Int, b: Int): Int {
        var n1 = abs(a)
        var n2 = abs(b)
        while (n2 != 0) {
            val temp = n2
            n2 = n1 % n2
            n1 = temp
        }
        return if (n1 == 0) 1 else n1
    }
}

/**
 * Inspects image bounds and file size asynchronously without loading full bitmaps into memory.
 */
suspend fun inspectImageMetadata(context: Context, imageSource: String): UploadedImageMetadata = withContext(Dispatchers.IO) {
    if (imageSource.isBlank()) return@withContext UploadedImageMetadata()
    try {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

        when {
            imageSource.startsWith("content://") -> {
                val uri = Uri.parse(imageSource)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
                val size = try {
                    context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0L
                } catch (e: Exception) { 0L }

                UploadedImageMetadata(
                    width = options.outWidth,
                    height = options.outHeight,
                    fileSizeBytes = size,
                    mimeType = options.outMimeType ?: "image/jpeg"
                )
            }
            imageSource.startsWith("file://") -> {
                val path = Uri.parse(imageSource).path ?: imageSource.removePrefix("file://")
                val file = File(path)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath, options)
                    UploadedImageMetadata(
                        width = options.outWidth,
                        height = options.outHeight,
                        fileSizeBytes = file.length(),
                        mimeType = options.outMimeType ?: "image/jpeg"
                    )
                } else {
                    UploadedImageMetadata(error = "File not found")
                }
            }
            imageSource.startsWith("/") -> {
                val file = File(imageSource)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath, options)
                    UploadedImageMetadata(
                        width = options.outWidth,
                        height = options.outHeight,
                        fileSizeBytes = file.length(),
                        mimeType = options.outMimeType ?: "image/jpeg"
                    )
                } else {
                    UploadedImageMetadata(error = "File not found")
                }
            }
            imageSource.startsWith("http://") || imageSource.startsWith("https://") -> {
                // Execute lightweight Coil probe
                val loader = Coil.imageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(imageSource)
                    .size(coil.size.Size.ORIGINAL)
                    .allowHardware(false)
                    .build()
                val result = loader.execute(request)
                val drawable = result.drawable
                if (drawable != null) {
                    UploadedImageMetadata(
                        width = drawable.intrinsicWidth.coerceAtLeast(0),
                        height = drawable.intrinsicHeight.coerceAtLeast(0),
                        fileSizeBytes = 0L,
                        mimeType = "image/web"
                    )
                } else {
                    UploadedImageMetadata(width = 0, height = 0)
                }
            }
            else -> UploadedImageMetadata()
        }
    } catch (e: Exception) {
        UploadedImageMetadata(error = e.message)
    }
}

/**
 * Professional helper component rendered below/beside every image upload field.
 * Displays:
 * - Recommended: Width × Height px
 * - Aspect Ratio
 * - Minimum: Width × Height px
 * - Format: JPG / PNG / WebP
 * - Tip
 * - When an image is provided:
 *   * Actual dimensions
 *   * Actual aspect ratio
 *   * File size
 *   * Quality/status badge
 *   * Container preview in the actual app aspect ratio
 *   * Warning banner if cropped, stretched, or low resolution
 */
@Composable
fun RecommendedImageSizeHelper(
    spec: UploadImageType,
    imageSource: String = "",
    modifier: Modifier = Modifier,
    showContainerPreview: Boolean = true
) {
    val context = LocalContext.current
    var showGuidelinesDialog by remember { mutableStateOf(false) }
    var metadata by remember { mutableStateOf(UploadedImageMetadata()) }
    var isInspecting by remember { mutableStateOf(false) }

    // Inspect image whenever the source string changes
    LaunchedEffect(imageSource) {
        if (imageSource.isNotBlank()) {
            isInspecting = true
            metadata = inspectImageMetadata(context, imageSource)
            isInspecting = false
        } else {
            metadata = UploadedImageMetadata()
            isInspecting = false
        }
    }

    if (showGuidelinesDialog) {
        ImageUploadGuidelinesDialog(
            currentSpec = spec,
            onDismiss = { showGuidelinesDialog = false }
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Recommended Specs Bar + Guidelines Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recommended: ${spec.recommendedWidth} × ${spec.recommendedHeight} px",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.8.dp, Color(0xFFBFDBFE))
                    ) {
                        Text(
                            text = "Ratio: ${spec.ratioLabel}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Guidelines ℹ️",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier
                            .clickable { showGuidelinesDialog = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Specs Detail Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Minimum: ${spec.minWidth} × ${spec.minHeight} px",
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )
                Text(text = "•", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                Text(
                    text = "Format: ${spec.format}",
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )
            }

            // Tip Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(text = "💡 ", fontSize = 11.sp)
                Text(
                    text = "Tip: ${spec.tip}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 15.sp
                )
            }

            // Uploaded Image Analysis (when an image is chosen / entered)
            if (imageSource.isNotBlank()) {
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)

                if (isInspecting) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp, color = Color(0xFF2563EB))
                        Text("Inspecting image dimensions & aspect ratio...", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                } else if (metadata.isValid) {
                    val ratioDiff = abs(metadata.aspectDecimal - spec.targetRatio) / spec.targetRatio
                    val isLowRes = metadata.width < spec.minWidth || metadata.height < spec.minHeight
                    val isRatioMismatch = ratioDiff > 0.08f
                    val isOptimal = !isLowRes && !isRatioMismatch

                    // Uploaded vs Recommended stats summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Uploaded: ",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = "${metadata.formattedDimensions} • ${metadata.formattedRatio}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isOptimal) Color(0xFF16A34A) else Color(0xFFB45309)
                                )
                                if (metadata.formattedSize.isNotBlank()) {
                                    Text(
                                        text = " • ${metadata.formattedSize}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        // Quality / Status Indicator Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                isOptimal -> Color(0xFFDCFCE7)
                                isLowRes && isRatioMismatch -> Color(0xFFFEE2E2)
                                isLowRes -> Color(0xFFFFEDD5)
                                else -> Color(0xFFFEF3C7)
                            }
                        ) {
                            Text(
                                text = when {
                                    isOptimal -> "✅ Optimal Size"
                                    isLowRes && isRatioMismatch -> "⚠️ Unsuitable"
                                    isLowRes -> "⚠️ Low Resolution"
                                    else -> "⚠️ Ratio Mismatch"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isOptimal -> Color(0xFF16A34A)
                                    isLowRes && isRatioMismatch -> Color(0xFFDC2626)
                                    isLowRes -> Color(0xFFC2410C)
                                    else -> Color(0xFFD97706)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Warning details banner if unsuitable
                    if (!isOptimal) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isLowRes && isRatioMismatch) Color(0xFFFFF1F2) else Color(0xFFFFFBEB),
                            border = BorderStroke(0.8.dp, if (isLowRes && isRatioMismatch) Color(0xFFFECDD3) else Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isLowRes && isRatioMismatch) Color(0xFFE11D48) else Color(0xFFD97706),
                                    modifier = Modifier.size(15.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    if (isRatioMismatch) {
                                        Text(
                                            text = "Different aspect ratio (${metadata.formattedRatio} vs recommended ${spec.ratioLabel}) — image may be cropped in the app container.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF78350F)
                                        )
                                    }
                                    if (isLowRes) {
                                        Text(
                                            text = "Resolution is below minimum (${spec.minWidth} × ${spec.minHeight} px) — image may appear pixelated or blurry on high-density devices.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF78350F)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // App Container Aspect-Ratio Preview
                    if (showContainerPreview) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Preview in Real App Container (${spec.ratioLabel}):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                                Text(
                                    text = if (isRatioMismatch) "Crop view" else "Exact fit",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            // Dynamic container with clamped height matching targetRatio
                            val previewHeight = when (spec) {
                                UploadImageType.HEADER_BANNER -> 70.dp
                                UploadImageType.APP_WALLPAPER,
                                UploadImageType.MOBILE_WALLPAPER,
                                UploadImageType.TABLET_WALLPAPER -> 160.dp
                                UploadImageType.POSTER_BANNER,
                                UploadImageType.CURRENT_AFFAIRS_CARD,
                                UploadImageType.CURRENT_AFFAIRS_MONTH_SET -> 140.dp
                                UploadImageType.MOCK_TEST_IMAGE,
                                UploadImageType.NOTIFICATION_IMAGE -> 90.dp
                                UploadImageType.SUBJECT_IMAGE,
                                UploadImageType.UNIT_IMAGE,
                                UploadImageType.TOPIC_IMAGE -> 110.dp
                                UploadImageType.PROFILE_IMAGE,
                                UploadImageType.MENU_FEATURE_IMAGE -> 90.dp
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(previewHeight)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = imageSource,
                                    contentDescription = "Container Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Container Ratio Watermark
                                Surface(
                                    color = Color.Black.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = "${spec.title} • ${spec.ratioLabel}",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

/**
 * Modal Dialog showing the complete image dimension reference table.
 */
@Composable
fun ImageUploadGuidelinesDialog(
    currentSpec: UploadImageType? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Image Upload Guidelines", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("Recommended dimensions & aspect ratios for all app assets", fontSize = 11.5.sp, color = Color(0xFF64748B))
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Table of Specifications
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Upload Asset Type", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1.5f))
                                Text("Recommended", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(1.2f))
                                Text("Ratio", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
                            }
                        }
                    }

                    items(UploadImageType.values()) { spec ->
                        val isHighlighted = currentSpec == spec
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isHighlighted) Color(0xFFEFF6FF) else Color.White,
                            border = BorderStroke(
                                width = if (isHighlighted) 1.5.dp else 1.dp,
                                color = if (isHighlighted) Color(0xFF3B82F6) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.5f)) {
                                        if (isHighlighted) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF2563EB))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = spec.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isHighlighted) Color(0xFF1D4ED8) else Color(0xFF1E293B)
                                        )
                                    }

                                    Text(
                                        text = "${spec.recommendedWidth} × ${spec.recommendedHeight} px",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier.weight(1.2f)
                                    )

                                    Surface(
                                        color = if (isHighlighted) Color(0xFFDBEAFE) else Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = spec.ratioLabel,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isHighlighted) Color(0xFF1E40AF) else Color(0xFF475569),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = spec.tip,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF64748B),
                                        lineHeight = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Min: ${spec.minWidth}×${spec.minHeight}",
                                        fontSize = 9.5.sp,
                                        color = Color(0xFF94A3B8),
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Best Practices for App Images:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text("• Recommended Formats: JPG for rich photos, PNG for graphics with transparency, WebP for optimal compression.", fontSize = 10.5.sp, color = Color(0xFF475569))
                                Text("• Safe Zone: Place logos, subjects, and text away from outer 10% edges to accommodate dynamic aspect ratios.", fontSize = 10.5.sp, color = Color(0xFF475569))
                                Text("• File Size: Keep individual images under 2 MB for instantaneous loading over cellular networks.", fontSize = 10.5.sp, color = Color(0xFF475569))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Got It, Close Guidelines", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
