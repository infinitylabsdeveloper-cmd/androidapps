package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PremiumBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 40,
    iconSize: Int = 20,
    backgroundColor: Color = Color.White,
    iconColor: Color = Color(0xFF0F172A)
) {
    Surface(
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .size(size.dp)
            .premiumPress(target = com.example.model.AnimatableTarget.BUTTONS_CTAS) {
                onClick()
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = iconColor,
                modifier = Modifier.size(iconSize.dp)
            )
        }
    }
}

@Composable
fun PremiumHomeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 40,
    iconSize: Int = 20,
    backgroundColor: Color = Color.White,
    iconColor: Color = Color(0xFF2563EB)
) {
    Surface(
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
        modifier = modifier
            .size(size.dp)
            .premiumPress(target = com.example.model.AnimatableTarget.BUTTONS_CTAS) {
                onClick()
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = iconColor,
                modifier = Modifier.size(iconSize.dp)
            )
        }
    }
}

@Composable
fun BackAndHomeTopActions(
    onBack: () -> Unit,
    onNavigateToHome: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    size: Int = 38,
    iconSize: Int = 19,
    backBgColor: Color = Color.White,
    backIconColor: Color = Color(0xFF0F172A),
    homeBgColor: Color = Color.White,
    homeIconColor: Color = Color(0xFF2563EB)
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PremiumBackButton(
            onClick = onBack,
            size = size,
            iconSize = iconSize,
            backgroundColor = backBgColor,
            iconColor = backIconColor
        )
        if (onNavigateToHome != null) {
            PremiumHomeButton(
                onClick = onNavigateToHome,
                size = size,
                iconSize = iconSize,
                backgroundColor = homeBgColor,
                iconColor = homeIconColor
            )
        }
    }
}

