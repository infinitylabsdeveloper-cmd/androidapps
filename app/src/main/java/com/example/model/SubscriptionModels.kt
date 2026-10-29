package com.example.model

import java.util.UUID

enum class PlanStatus {
    ACTIVE,
    SCHEDULED,
    INACTIVE,
    EXPIRED
}

data class SubscriptionPlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String, // durationLabel e.g. "12 Months"
    val subtitle: String, // e.g. "Complete Plan"
    val price: Int,
    val originalPrice: Int,
    val effectiveMonthly: String,
    val discountText: String,
    val iconEmoji: String,
    val features: List<String>,
    
    // Admin management fields
    val displayOrder: Int = 0,
    val status: PlanStatus = PlanStatus.ACTIVE,
    val isBestValue: Boolean = false,
    
    // Hex colors for UI
    val buttonColorHex: String = "#2563EB",
    val cardBgColorHex: String = "#EFF6FF",
    val borderColorHex: String = "#BFDBFE",
    
    // Target App Distribution Scope ("ALL" or specific app IDs e.g. ["appsc-group2", "ap-dsc"])
    val targetAppIds: List<String> = listOf("ALL"),

    // Scheduling and Lifecycle
    val activateAt: Long? = null,
    val expiresAt: Long? = null,
    val isSoftDeleted: Boolean = false
)

data class SubscriptionBackgroundModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val imageUrl: String,
    val title: String = "",
    val subtitle: String = "",
    val mappedAppIds: List<String> = listOf("ALL"),
    val isEnabled: Boolean = true,
    val order: Int = 0
)
