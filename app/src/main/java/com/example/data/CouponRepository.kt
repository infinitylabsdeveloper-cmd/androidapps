package com.example.data

import com.example.model.Coupon
import com.example.model.CouponDiscountType
import com.example.model.CouponStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object CouponRepository {
    private val _coupons = MutableStateFlow<List<Coupon>>(
        listOf(
            Coupon(
                id = "coup-1",
                code = "WELCOME50",
                description = "Get 50% flat off on any plan for new users.",
                discountType = CouponDiscountType.PERCENTAGE,
                discountValue = 50.0,
                minPurchaseAmount = 199.0,
                maxDiscountAmount = 500.0,
                usageLimitTotal = 1000,
                usageLimitPerUser = 1,
                timesUsed = 150,
                totalDiscountGiven = 45000.0,
                applicablePlanIds = emptyList() // All plans
            ),
            Coupon(
                id = "coup-2",
                code = "FLAT100",
                description = "Flat ₹100 off on purchases above ₹499",
                discountType = CouponDiscountType.FIXED,
                discountValue = 100.0,
                minPurchaseAmount = 499.0,
                maxDiscountAmount = 100.0,
                usageLimitTotal = 500,
                usageLimitPerUser = 2,
                timesUsed = 450,
                totalDiscountGiven = 45000.0,
                applicablePlanIds = emptyList()
            )
        )
    )

    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()

    fun updateCouponStatuses() {
        val now = System.currentTimeMillis()
        _coupons.update { currentCoupons ->
            var hasChanged = false
            val updated = currentCoupons.map { coupon ->
                if (coupon.isSoftDeleted) return@map coupon
                if (coupon.status == CouponStatus.INACTIVE) return@map coupon
                
                var newStatus = coupon.status
                
                if (coupon.usageLimitTotal != null && coupon.timesUsed >= coupon.usageLimitTotal) {
                    newStatus = CouponStatus.EXHAUSTED
                } else {
                    val isActivated = coupon.activateAt == null || now >= coupon.activateAt
                    val isExpired = coupon.expiresAt != null && now >= coupon.expiresAt
                    
                    if (isExpired) {
                        newStatus = CouponStatus.EXPIRED
                    } else if (!isActivated) {
                        newStatus = CouponStatus.SCHEDULED
                    } else {
                        newStatus = CouponStatus.ACTIVE
                    }
                }
                
                if (newStatus != coupon.status) {
                    hasChanged = true
                    coupon.copy(status = newStatus)
                } else {
                    coupon
                }
            }
            if (hasChanged) updated else currentCoupons
        }
    }

    fun addOrUpdateCoupon(coupon: Coupon) {
        _coupons.update { current ->
            val existingById = current.find { it.id == coupon.id }
            if (existingById != null) {
                current.map { if (it.id == coupon.id) coupon else it }
            } else {
                // If creating a new one, ensure code doesn't exist among non-deleted active/scheduled ones
                val codeExists = current.any { it.code.equals(coupon.code, ignoreCase = true) && !it.isSoftDeleted }
                if (codeExists) {
                    current // Ignore or throw? For simple UI we just ignore. A real app would throw an error and UI would catch it.
                } else {
                    current + coupon
                }
            }
        }
    }

    fun deleteCoupon(id: String) {
        _coupons.update { current ->
            current.map {
                if (it.id == id) it.copy(isSoftDeleted = true) else it
            }
        }
    }

    fun toggleCouponStatus(id: String, isEnabled: Boolean) {
        _coupons.update { current ->
            current.map {
                if (it.id == id) {
                    val newStatus = if (isEnabled) {
                        val now = System.currentTimeMillis()
                        if (it.usageLimitTotal != null && it.timesUsed >= it.usageLimitTotal) CouponStatus.EXHAUSTED
                        else if (it.expiresAt != null && it.expiresAt < now) CouponStatus.EXPIRED
                        else if (it.activateAt != null && it.activateAt > now) CouponStatus.SCHEDULED
                        else CouponStatus.ACTIVE
                    } else {
                        CouponStatus.INACTIVE
                    }
                    it.copy(status = newStatus)
                } else it
            }
        }
    }
    
    // Simulate applying a coupon
    fun validateAndGetCoupon(code: String, cartAmount: Double, planId: String, currentUsesByUser: Int = 0, currentAppId: String = "appsc-group2"): Result<Coupon> {
        val coupon = _coupons.value.find { it.code.equals(code, ignoreCase = true) && !it.isSoftDeleted }
            ?: return Result.failure(Exception("Invalid coupon code"))
            
        if (coupon.status == CouponStatus.INACTIVE) return Result.failure(Exception("This coupon has been disabled"))
        if (coupon.status == CouponStatus.EXPIRED) return Result.failure(Exception("This coupon has expired"))
        if (coupon.status == CouponStatus.SCHEDULED) return Result.failure(Exception("This coupon is not active yet"))
        if (coupon.status == CouponStatus.EXHAUSTED) return Result.failure(Exception("This coupon has reached its usage limit"))
        if (coupon.usageLimitTotal != null && coupon.timesUsed >= coupon.usageLimitTotal) return Result.failure(Exception("This coupon has reached its usage limit"))
        if (coupon.usageLimitPerUser != null && currentUsesByUser >= coupon.usageLimitPerUser) return Result.failure(Exception("You have reached the usage limit for this coupon"))
        if (cartAmount < coupon.minPurchaseAmount) return Result.failure(Exception("Minimum purchase amount of ₹${coupon.minPurchaseAmount} required"))
        if (coupon.applicablePlanIds.isNotEmpty() && !coupon.applicablePlanIds.contains(planId)) return Result.failure(Exception("This coupon is not applicable for this plan"))
        if (coupon.targetAppIds.isNotEmpty() && !coupon.targetAppIds.contains("ALL") && !coupon.targetAppIds.contains(currentAppId)) return Result.failure(Exception("This coupon is not valid for this application"))
        
        return Result.success(coupon)
    }
    
    fun recordCouponUsage(couponId: String, discountGiven: Double) {
        _coupons.update { current ->
            current.map { 
                if (it.id == couponId) {
                    it.copy(
                        timesUsed = it.timesUsed + 1,
                        totalDiscountGiven = it.totalDiscountGiven + discountGiven
                    )
                } else it
            }
        }
    }
}
