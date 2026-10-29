package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.model.MockTestModel
import com.example.model.PromotionalPosterModel
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.model.UnitModel
import com.example.util.CardAssetRegistry

/**
 * Universal FHD Image Engine for APPSC Prep Platform
 * Guarantees zero blank images, high-speed cached loading, crossfade transitions,
 * and high-fidelity thematic vector artworks for Subjects, Units, Topics, and Posters.
 */

@Composable
fun AppFhdImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    imageRes: Int? = null,
    contentScale: ContentScale = ContentScale.Crop,
    themeCategory: String = "general",
    title: String = "",
    subtitle: String = "",
    tagBadge: String? = null
) {
    val context = LocalContext.current
    val cleanUrl = remember(imageUrl) { imageUrl?.trim()?.takeIf { it.isNotEmpty() } }

    val resolvedResId: Int? = remember(cleanUrl, imageRes, themeCategory, title, context) {
        if (imageRes != null && imageRes != 0) {
            imageRes
        } else if (cleanUrl != null && !cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://") && !cleanUrl.startsWith("file://") && !cleanUrl.startsWith("content://")) {
            val resName = cleanUrl.substringBeforeLast(".")
                .lowercase()
                .replace("\\s+".toRegex(), "_")
            val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (id != 0) id else null
        } else {
            null
        }
    }

    val isRemoteUrl = remember(cleanUrl) {
        cleanUrl != null && (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://"))
    }

    Box(modifier = modifier) {
        // 1. Instant local drawable rendering if available
        if (resolvedResId != null && resolvedResId != 0) {
            Image(
                painter = painterResource(id = resolvedResId),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Remote image layer loaded asynchronously via Coil if URL is available
        if (isRemoteUrl && cleanUrl != null) {
            val imageRequest = remember(cleanUrl, context) {
                ImageRequest.Builder(context)
                    .data(cleanUrl)
                    .crossfade(true)
                    .allowHardware(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                    .build()
            }

            var isLoading by remember { mutableStateOf(resolvedResId == null) }
            var isError by remember { mutableStateOf(false) }

            coil.compose.AsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                onState = { state ->
                    isLoading = state is coil.compose.AsyncImagePainter.State.Loading && resolvedResId == null
                    isError = state is coil.compose.AsyncImagePainter.State.Error
                }
            )

            if (isLoading && resolvedResId == null) {
                FhdImageShimmer(modifier = Modifier.fillMaxSize())
            }

            if (isError && resolvedResId == null) {
                FhdThematicArtwork(
                    category = themeCategory,
                    title = title,
                    subtitle = subtitle,
                    tagBadge = tagBadge,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else if (resolvedResId == null) {
            // High-definition vibrant thematic artwork banner with jewel tones and glowing emblem
            FhdThematicArtwork(
                category = themeCategory,
                title = title,
                subtitle = subtitle,
                tagBadge = tagBadge,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Shimmer Loading Placeholder
 */
@Composable
fun FhdImageShimmer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = remember {
        listOf(
            Color(0xFF1E293B),
            Color(0xFF334155),
            Color(0xFF1E293B)
        )
    }

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 500f, translateAnim - 500f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .background(brush)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "Loading image",
            tint = Color.White.copy(alpha = 0.25f),
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Rich Thematic Vector Graphic Fallback for FHD Displays
 */
@Composable
fun FhdThematicArtwork(
    category: String,
    title: String,
    subtitle: String,
    tagBadge: String?,
    modifier: Modifier = Modifier
) {
    val theme = remember(category, title) { resolveThemePalette(category, title) }

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = theme.gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .fillMaxSize()
    ) {
        // Decorative geometric mesh on canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Abstract ambient glow circles
            drawCircle(
                color = theme.accentGlow.copy(alpha = 0.18f),
                radius = width * 0.45f,
                center = Offset(width * 0.85f, height * 0.15f)
            )
            drawCircle(
                color = theme.secondaryGlow.copy(alpha = 0.14f),
                radius = width * 0.35f,
                center = Offset(width * 0.15f, height * 0.85f)
            )

            // Dynamic diagonal light streak
            val path = Path().apply {
                moveTo(0f, height * 0.65f)
                cubicTo(
                    width * 0.35f, height * 0.35f,
                    width * 0.65f, height * 0.85f,
                    width, height * 0.2f
                )
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.10f),
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Center / Corner Thematic Emblem
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Icon Watermark in background right
            Icon(
                imageVector = theme.icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.16f),
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 10.dp)
            )

            // Content Info overlay removed to prevent duplicate text.
            // The enclosing screens (HomeScreen, etc.) provide much better styled overlays.
            // Centered glowing thematic emblem
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = theme.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

private data class ThematicStyle(
    val gradientColors: List<Color>,
    val accentGlow: Color,
    val secondaryGlow: Color,
    val icon: ImageVector
)

private fun resolveThemePalette(category: String, title: String): ThematicStyle {
    val lower = "$category $title".lowercase()
    return when {
        lower.contains("andhra") || lower.contains("satavahana") || lower.contains("kakatiya") || lower.contains("vijayanagara") || lower.contains("ikshvaku") -> {
            ThematicStyle(
                gradientColors = listOf(Color(0xFF881337), Color(0xFFBE123C), Color(0xFF4C0519)),
                accentGlow = Color(0xFFFFD700),
                secondaryGlow = Color(0xFFFB7185),
                icon = Icons.Default.AccountBalance
            )
        }
        lower.contains("mental") || lower.contains("reasoning") || lower.contains("aptitude") || lower.contains("deductive") || lower.contains("numeracy") -> {
            ThematicStyle(
                gradientColors = listOf(Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFF082F49)),
                accentGlow = Color(0xFF38BDF8),
                secondaryGlow = Color(0xFF7DD3FC),
                icon = Icons.Default.Psychology
            )
        }
        lower.contains("history") || lower.contains("ancient") || lower.contains("vedic") || lower.contains("indus") || lower.contains("buddhism") -> {
            ThematicStyle(
                gradientColors = listOf(Color(0xFF831843), Color(0xFF9D174D), Color(0xFF500724)),
                accentGlow = Color(0xFFF59E0B),
                secondaryGlow = Color(0xFFEC4899),
                icon = Icons.Default.AccountBalance
            )
        }
        lower.contains("medieval") || lower.contains("sultanate") || lower.contains("mughal") || lower.contains("monument") -> {
            ThematicStyle(
                gradientColors = listOf(Color(0xFF9A3412), Color(0xFFC2410C), Color(0xFF431407)),
                accentGlow = Color(0xFFFBBF24),
                secondaryGlow = Color(0xFFEA580C),
                icon = Icons.Default.Fort
            )
        }
        lower.contains("modern") || lower.contains("freedom") || lower.contains("1857") || lower.contains("movement") -> {
            ThematicStyle(
                gradientColors = listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFF0F172A)),
                accentGlow = Color(0xFF38BDF8),
                secondaryGlow = Color(0xFF60A5FA),
                icon = Icons.Default.Flag
            )
        }
        lower.contains("geo") || lower.contains("climate") || lower.contains("river") || lower.contains("earth") || lower.contains("physical") -> {
            ThematicStyle(
                gradientColors = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF022C22)),
                accentGlow = Color(0xFF34D399),
                secondaryGlow = Color(0xFF10B981),
                icon = Icons.Default.Public
            )
        }
        lower.contains("polity") || lower.contains("constitution") || lower.contains("article") || lower.contains("governance") -> {
            ThematicStyle(
                gradientColors = listOf(Color(0xFF312E81), Color(0xFF4338CA), Color(0xFF1E1B4B)),
                accentGlow = Color(0xFFA78BFA),
                secondaryGlow = Color(0xFF818CF8),
                icon = Icons.Default.Gavel
            )
        }
        lower.contains("economy") || lower.contains("finance") || lower.contains("budget") || lower.contains("planning") -> {
            ThematicStyle(
                gradientColors = listOf(Color(0xFF14532D), Color(0xFF15803D), Color(0xFF052E16)),
                accentGlow = Color(0xFF4ADE80),
                secondaryGlow = Color(0xFF22C55E),
                icon = Icons.Default.TrendingUp
            )
        }
        lower.contains("science") || lower.contains("tech") || lower.contains("space") || lower.contains("isro") || lower.contains("it") -> {
            ThematicStyle(
                gradientColors = listOf(Color(0xFF0E7490), Color(0xFF0891B2), Color(0xFF164E63)),
                accentGlow = Color(0xFF22D3EE),
                secondaryGlow = Color(0xFF06B6D4),
                icon = Icons.Default.Biotech
            )
        }
        lower.contains("society") || lower.contains("welfare") || lower.contains("tribe") || lower.contains("caste") -> {
            ThematicStyle(
                gradientColors = listOf(Color(0xFF701A75), Color(0xFF86198F), Color(0xFF4A044E)),
                accentGlow = Color(0xFFE879F9),
                secondaryGlow = Color(0xFFC026D3),
                icon = Icons.Default.Groups
            )
        }
        lower.contains("affair") || lower.contains("current") || lower.contains("news") || lower.contains("daily") -> {
            ThematicStyle(
                gradientColors = listOf(Color(0xFFBE123C), Color(0xFFE11D48), Color(0xFF4C0519)),
                accentGlow = Color(0xFFFB7185),
                secondaryGlow = Color(0xFFFDA4AF),
                icon = Icons.Default.Bolt
            )
        }
        lower.contains("mock") || lower.contains("test") || lower.contains("grand") || lower.contains("series") -> {
            ThematicStyle(
                gradientColors = listOf(Color(0xFF1E1B4B), Color(0xFF3730A3), Color(0xFF0F172A)),
                accentGlow = Color(0xFFF59E0B),
                secondaryGlow = Color(0xFF818CF8),
                icon = Icons.Default.EmojiEvents
            )
        }
        lower.contains("pro") || lower.contains("plan") || lower.contains("vip") || lower.contains("subscription") || lower.contains("poster") -> {
            ThematicStyle(
                gradientColors = listOf(Color(0xFF451A03), Color(0xFF78350F), Color(0xFF1C1917)),
                accentGlow = Color(0xFFFBBF24),
                secondaryGlow = Color(0xFFF59E0B),
                icon = Icons.Default.WorkspacePremium
            )
        }
        else -> {
            ThematicStyle(
                gradientColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF020617)),
                accentGlow = Color(0xFF38BDF8),
                secondaryGlow = Color(0xFF64748B),
                icon = Icons.Default.MenuBook
            )
        }
    }
}

/**
 * High-Convenience Composable for Subject Cards
 */
@Composable
fun SubjectFhdCardImage(
    subject: Subject,
    modifier: Modifier = Modifier
) {
    val cleanUrl = remember(subject.imageUrl) {
        subject.imageUrl?.trim()?.takeIf { it.isNotEmpty() && (it.startsWith("http://") || it.startsWith("https://")) }
    }
    val resolvedRes = remember(subject.id, subject.imageRes, subject.name) {
        if (subject.imageRes != null && subject.imageRes != 0) {
            subject.imageRes
        } else {
            CardAssetRegistry.getSubjectDrawable(subject.id, subject.name)
        }
    }
    val effectiveUrl = cleanUrl ?: ""
    val effectiveRes = if (effectiveUrl.isEmpty()) resolvedRes else null

    AppFhdImage(
        imageUrl = effectiveUrl,
        imageRes = effectiveRes,
        contentDescription = subject.name,
        modifier = modifier,
        themeCategory = subject.name,
        title = subject.name,
        subtitle = subject.heroQuote.replace("\n", " • ")
    )
}

/**
 * High-Convenience Composable for Unit Cards & Unit Hero Headers
 */
@Composable
fun UnitFhdCardImage(
    unit: UnitModel,
    subjectName: String = "",
    subjectId: String = "",
    modifier: Modifier = Modifier
) {
    val formattedTitle = remember(unit.unitNumber, unit.name) {
        com.example.data.SampleData.formatUnitTitle(unit.unitNumber, unit.name)
    }
    val resolvedRes = remember(unit.id, unit.imageRes, unit.name, subjectId) {
        if (unit.imageRes != null && unit.imageRes != 0) {
            unit.imageRes
        } else {
            CardAssetRegistry.getUnitDrawable(unit.id, unit.name, subjectId)
        }
    }
    AppFhdImage(
        imageUrl = unit.imageUrl,
        imageRes = resolvedRes,
        contentDescription = unit.name,
        modifier = modifier,
        themeCategory = "$subjectName ${unit.name}",
        title = formattedTitle,
        subtitle = unit.subtitle ?: unit.tagline ?: "",
        tagBadge = "Unit ${unit.unitNumber}"
    )
}

/**
 * High-Convenience Composable for Topic Cards
 */
@Composable
fun TopicFhdCardImage(
    topic: TopicModel,
    unitId: String = "",
    subjectId: String = "",
    modifier: Modifier = Modifier
) {
    val resolvedRes = remember(topic.id, topic.imageRes, topic.name, unitId, subjectId) {
        if (topic.imageRes != null && topic.imageRes != 0) {
            topic.imageRes
        } else {
            CardAssetRegistry.getTopicDrawable(topic.id, topic.name, unitId, subjectId)
        }
    }
    AppFhdImage(
        imageUrl = topic.imageUrl,
        imageRes = resolvedRes,
        contentDescription = topic.name,
        modifier = modifier,
        themeCategory = "${topic.tagBadge} ${topic.name}",
        title = topic.name,
        subtitle = topic.subtitle ?: "",
        tagBadge = topic.tagBadge
    )
}

/**
 * High-Convenience Composable for Promotional Posters
 */
@Composable
fun PosterFhdCardImage(
    poster: PromotionalPosterModel,
    modifier: Modifier = Modifier
) {
    AppFhdImage(
        imageUrl = poster.imageUrl,
        contentDescription = poster.title,
        modifier = modifier,
        themeCategory = "poster ${poster.title}",
        title = poster.title,
        subtitle = poster.promotionalText,
        tagBadge = poster.posterType
    )
}

/**
 * High-Convenience Composable for Mock Test Cards
 */
@Composable
fun MockTestFhdCardImage(
    mockTest: MockTestModel,
    modifier: Modifier = Modifier
) {
    AppFhdImage(
        imageUrl = "",
        contentDescription = mockTest.title,
        modifier = modifier,
        themeCategory = "mock test ${mockTest.paperType}",
        title = mockTest.title,
        subtitle = "${mockTest.totalQuestions} Questions • ${mockTest.durationMinutes} Mins",
        tagBadge = mockTest.difficulty
    )
}
