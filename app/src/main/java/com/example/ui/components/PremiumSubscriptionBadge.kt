package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData

/**
 * A highly polished, responsive "PREMIUM" subscription badge designed for headers or cards.
 * Supports both "Premium Active" and "Premium Locked/Upgrade" states.
 * Tapping this badge opens the Subscription / Upgrade screen.
 */
@Composable
fun PremiumSubscriptionBadge(
    onNavigateToSubscription: () -> Unit,
    modifier: Modifier = Modifier,
    isPremium: Boolean = SampleData.isUserPremium()
) {
    // 1. Color System
    val goldStart = Color(0xFFFBE48A)
    val goldPrimary = Color(0xFFE5B73E)
    val goldDark = Color(0xFFC59124)

    val activeBackgroundGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF0D5C46), Color(0xFF043825))
    )
    val activeBorderGradient = Brush.verticalGradient(
        colors = listOf(goldStart, goldPrimary, goldDark)
    )

    val lockedBackgroundGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF7F1D1D), Color(0xFF991B1B))
    )
    val lockedBorderGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFCA5A5), Color(0xFFEF4444), Color(0xFF991B1B))
    )

    val resolvedBackground = if (isPremium) activeBackgroundGradient else lockedBackgroundGradient
    val resolvedBorder = if (isPremium) activeBorderGradient else lockedBorderGradient
    val labelText = if (isPremium) "PREMIUM ACTIVE" else "UPGRADE NOW"
    val badgeTestTag = if (isPremium) "premium_badge_active" else "premium_badge_locked"

    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .clip(RoundedCornerShape(12.dp))
            .background(resolvedBackground)
            .border(
                border = BorderStroke(1.dp, resolvedBorder),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onNavigateToSubscription() }
            .testTag(badgeTestTag)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = "Subscription Badge Icon",
                tint = if (isPremium) goldStart else Color(0xFFFFF1BD),
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = labelText,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
