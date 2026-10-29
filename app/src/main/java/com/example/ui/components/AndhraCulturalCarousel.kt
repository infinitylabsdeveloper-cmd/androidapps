package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.CulturalCarouselItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Premium Andhra Pradesh Cultural Image Carousel.
 * Features cinematic transitions, subtle Ken Burns camera motion, elegant gradients,
 * rounded corners, and premium glowing pill indicators.
 *
 * Keeps authentication distraction-free while instilling deep cultural pride in aspirants.
 */
@Composable
fun AndhraCulturalCarousel(
    items: List<CulturalCarouselItem>,
    modifier: Modifier = Modifier,
    height: Dp = 215.dp,
    compactMode: Boolean = false,
    autoScrollDelayMs: Long = 5500L,
    onItemClick: ((CulturalCarouselItem) -> Unit)? = null
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { items.size })
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-advance loop (pauses while candidate is actively dragging/touching)
    LaunchedEffect(isDragged, items.size) {
        if (!isDragged && items.size > 1) {
            while (true) {
                delay(autoScrollDelayMs)
                val nextPage = (pagerState.currentPage + 1) % items.size
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(
                        durationMillis = 850,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compactMode) 155.dp else height)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(if (compactMode) 18.dp else 22.dp),
                spotColor = Color(0xFFF59E0B).copy(alpha = 0.25f),
                ambientColor = Color(0xFF0F172A).copy(alpha = 0.35f)
            )
            .testTag("andhra_cultural_carousel"),
        shape = RoundedCornerShape(if (compactMode) 18.dp else 22.dp),
        color = Color(0xFF020617),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFF59E0B).copy(alpha = 0.65f), // Gold
                    Color(0xFF10B981).copy(alpha = 0.45f), // Emerald
                    Color(0xFF38BDF8).copy(alpha = 0.50f), // Sky Blue
                    Color(0xFFA855F7).copy(alpha = 0.35f), // Purple
                    Color(0xFFF59E0B).copy(alpha = 0.65f)  // Gold
                )
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val item = items[page]
                val isCurrent = pagerState.currentPage == page

                // Subtle Ken Burns zooming effect (scales from 1.0f to 1.07f over the slide duration)
                val scale = remember(isCurrent) { Animatable(1.0f) }
                LaunchedEffect(isCurrent) {
                    if (isCurrent) {
                        scale.animateTo(
                            targetValue = 1.07f,
                            animationSpec = tween(
                                durationMillis = (autoScrollDelayMs - 300).toInt().coerceAtLeast(3000),
                                easing = LinearEasing
                            )
                        )
                    } else {
                        scale.snapTo(1.0f)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(if (compactMode) 18.dp else 22.dp))
                        .clickable(enabled = onItemClick != null) { onItemClick?.invoke(item) }
                ) {
                    // 1. Photographic Image with Ken Burns Scale
                    if (item.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.imageUrl)
                                .crossfade(600)
                                .build(),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(scale.value)
                        )
                    } else {
                        // Artistic Andhra motif canvas fallback
                        AndhraArtisticGradientFallback(
                            startColor = Color(item.fallbackGradientStart),
                            endColor = Color(item.fallbackGradientEnd)
                        )
                    }

                    // 2. Cinematic Multi-Stop Gradients (Top vignette + Deep Bottom readability shadow)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0.0f to Color.Black.copy(alpha = 0.40f),
                                    0.25f to Color.Transparent,
                                    0.50f to Color.Black.copy(alpha = 0.25f),
                                    0.75f to Color(0xFF020617).copy(alpha = 0.78f),
                                    1.0f to Color(0xFF020617).copy(alpha = 0.95f)
                                )
                            )
                    )

                    // 3. Cultural Content Overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = if (compactMode) 12.dp else 16.dp,
                                vertical = if (compactMode) 10.dp else 12.dp
                            ),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Row: Category Tag Pill + Location Pin
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Cultural Badge Pill
                            Surface(
                                shape = RoundedCornerShape(30.dp),
                                color = Color(0xFF020617).copy(alpha = 0.75f),
                                border = BorderStroke(
                                    width = 1.dp,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFF59E0B),
                                            Color(0xFF10B981)
                                        )
                                    )
                                ),
                                shadowElevation = 3.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = if (compactMode) 8.dp else 10.dp,
                                        vertical = if (compactMode) 3.dp else 4.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Text(
                                        text = item.badgeText.uppercase(),
                                        fontSize = if (compactMode) 9.sp else 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFDE047),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            // Slide indicator count (e.g. 1/8)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.55f),
                                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.15f))
                            ) {
                                Text(
                                    text = "${page + 1}/${items.size}",
                                    fontSize = if (compactMode) 9.5.sp else 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE2E8F0),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Bottom Section: Title + Telugu Subtitle + Description
                        Column(
                            verticalArrangement = Arrangement.spacedBy(if (compactMode) 2.dp else 3.dp)
                        ) {
                            // English Title
                            Text(
                                text = item.title,
                                fontSize = if (compactMode) 14.5.sp else 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Telugu Title / Heritage Subtitle
                            if (item.teluguTitle.isNotBlank()) {
                                Text(
                                    text = item.teluguTitle,
                                    fontSize = if (compactMode) 11.sp else 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFFDE047).copy(alpha = 0.95f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Geographic Subtitle
                            Text(
                                text = item.subtitle,
                                fontSize = if (compactMode) 10.sp else 11.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Left / Right subtle navigation buttons for manual navigation
            if (items.size > 1 && !compactMode) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            val prev = if (pagerState.currentPage > 0) pagerState.currentPage - 1 else items.size - 1
                            pagerState.animateScrollToPage(prev)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.40f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Slide",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            val next = (pagerState.currentPage + 1) % items.size
                            pagerState.animateScrollToPage(next)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.40f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Slide",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom Center: Premium Dynamic Pill Indicators
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = if (compactMode) 12.dp else 16.dp, bottom = if (compactMode) 10.dp else 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, _ ->
                    val isSelected = pagerState.currentPage == index
                    val targetWidth = if (isSelected) (if (compactMode) 16.dp else 22.dp) else 5.dp
                    val animatedWidth by animateDpAsState(
                        targetValue = targetWidth,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "pill_indicator_width"
                    )

                    Box(
                        modifier = Modifier
                            .height(5.dp)
                            .width(animatedWidth)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFF59E0B), Color(0xFF10B981))
                                    )
                                } else {
                                    Brush.linearGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.35f),
                                            Color.White.copy(alpha = 0.20f)
                                        )
                                    )
                                }
                            )
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }
        }
    }
}

/**
 * High-performance decorative fallback canvas in case network images are loading
 */
@Composable
private fun AndhraArtisticGradientFallback(
    startColor: Color,
    endColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    startColor,
                    endColor,
                    Color(0xFF020617)
                ),
                center = Offset(size.width * 0.5f, size.height * 0.4f),
                radius = size.width * 0.8f
            )
        )
        // Subtle traditional aesthetic waves
        for (i in 0..4) {
            drawCircle(
                color = Color(0xFFF59E0B).copy(alpha = 0.04f * (5 - i)),
                radius = size.width * (0.3f + i * 0.15f),
                center = Offset(size.width * 0.5f, size.height * 0.3f)
            )
        }
    }
}
