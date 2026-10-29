package com.example.model

import java.util.UUID

enum class CouponStatus {
    ACTIVE,
    SCHEDULED,
    INACTIVE,
    EXPIRED,
    EXHAUSTED
}

data class Coupon(
    val id: String = UUID.randomUUID().toString(),
    val code: String,
    val description: String = "",
    val discountType: CouponDiscountType = CouponDiscountType.PERCENTAGE,
    val discountValue: Double,
    val minPurchaseAmount: Double = 0.0,
    val maxDiscountAmount: Double? = null,
    val usageLimitTotal: Int? = null,
    val usageLimitPerUser: Int? = null,
    val timesUsed: Int = 0,
    val totalDiscountGiven: Double = 0.0,
    val applicablePlanIds: List<String> = emptyList(), // empty = All Plans
    val targetAppIds: List<String> = listOf("ALL"), // "ALL" or specific app IDs e.g. ["appsc-group2", "ap-dsc"]
    val activateAt: Long? = null,
    val expiresAt: Long? = null,
    val status: CouponStatus = CouponStatus.ACTIVE,
    val isSoftDeleted: Boolean = false
) {
    fun calculateDiscount(cartAmount: Double): Double {
        if (cartAmount < minPurchaseAmount) return 0.0
        val discount = when (discountType) {
            CouponDiscountType.PERCENTAGE -> cartAmount * (discountValue / 100.0)
            CouponDiscountType.FIXED -> discountValue
        }
        val actualDiscount = if (maxDiscountAmount != null) {
            discount.coerceAtMost(maxDiscountAmount)
        } else {
            discount
        }
        return actualDiscount.coerceAtMost(cartAmount)
    }
    
    fun isValid(cartAmount: Double, planId: String, currentUsesByUser: Int, currentAppId: String = "appsc-group2"): Boolean {
        if (status != CouponStatus.ACTIVE) return false
        if (isSoftDeleted) return false
        if (usageLimitTotal != null && timesUsed >= usageLimitTotal) return false
        if (usageLimitPerUser != null && currentUsesByUser >= usageLimitPerUser) return false
        if (cartAmount < minPurchaseAmount) return false
        if (applicablePlanIds.isNotEmpty() && !applicablePlanIds.contains(planId)) return false
        if (targetAppIds.isNotEmpty() && !targetAppIds.contains("ALL") && !targetAppIds.contains(currentAppId)) return false
        return true
    }
}
