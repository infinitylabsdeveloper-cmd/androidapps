package com.example.data

import com.example.model.PlanStatus
import com.example.model.SubscriptionPlan
import com.example.model.SubscriptionBackgroundModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object SubscriptionRepository {
    private val _plans = MutableStateFlow<List<SubscriptionPlan>>(listOf(
        SubscriptionPlan(
            id = "offline_99",
            name = "₹99 Offline Pass",
            subtitle = "Download & Practice Offline",
            price = 99,
            originalPrice = 199,
            effectiveMonthly = "₹99 / pass",
            discountText = "50% OFF",
            iconEmoji = "📥",
            features = listOf(
                "Offline MCQ Practice without Internet connection",
                "Access downloaded materials via Sets & Subtopics",
                "Validity aligns with your active plan duration",
                "Online practice remains 100% free and active",
                "Upon expiry: Offline access pauses until renewal"
            ),
            displayOrder = 0,
            buttonColorHex = "#2563EB",
            cardBgColorHex = "#EFF6FF",
            borderColorHex = "#2563EB",
            targetAppIds = listOf("ALL")
        ),
        SubscriptionPlan(
            id = "1_month",
            name = "1 Month",
            subtitle = "Starter Pass",
            price = 199,
            originalPrice = 399,
            effectiveMonthly = "₹199 / month",
            discountText = "50% OFF",
            iconEmoji = "⚡",
            features = listOf("All Subjects Unlocked", "Topic-wise MCQs", "Basic Performance Analytics"),
            displayOrder = 1,
            buttonColorHex = "#7C3AED",
            cardBgColorHex = "#F3E8FF",
            borderColorHex = "#7C3AED",
            targetAppIds = listOf("ALL")
        ),
        SubscriptionPlan(
            id = "3_months",
            name = "3 Months",
            subtitle = "Booster Plan",
            price = 349,
            originalPrice = 699,
            effectiveMonthly = "₹116 / month",
            discountText = "50% OFF",
            iconEmoji = "🚀",
            features = listOf("Everything in Starter", "Full Length Mock Tests", "Speed & Accuracy Tracker"),
            displayOrder = 2,
            buttonColorHex = "#0284C7",
            cardBgColorHex = "#E0F2FE",
            borderColorHex = "#0284C7",
            targetAppIds = listOf("ALL")
        ),
        SubscriptionPlan(
            id = "6_months",
            name = "6 Months",
            subtitle = "Achiever Plan",
            price = 499,
            originalPrice = 999,
            effectiveMonthly = "₹83 / month",
            discountText = "50% OFF",
            iconEmoji = "🎯",
            features = listOf("All Subjects Unlocked", "5000+ Topic MCQs", "Daily Practice Sets & Rank Board"),
            displayOrder = 3,
            buttonColorHex = "#059669",
            cardBgColorHex = "#ECFDF5",
            borderColorHex = "#059669",
            targetAppIds = listOf("ALL")
        ),
        SubscriptionPlan(
            id = "12_months",
            name = "12 Months",
            subtitle = "Complete Mastery",
            price = 799,
            originalPrice = 1999,
            effectiveMonthly = "₹66 / month",
            discountText = "60% OFF",
            iconEmoji = "👑",
            features = listOf("Everything in Achiever", "Full Length Mock Tests", "Monthly PDF Bundles", "Unlimited Re-attempts"),
            isBestValue = true,
            displayOrder = 4,
            buttonColorHex = "#D97706",
            cardBgColorHex = "#FFFBEB",
            borderColorHex = "#D97706",
            targetAppIds = listOf("ALL")
        )
    ))
    val plans: StateFlow<List<SubscriptionPlan>> = _plans.asStateFlow()

    private val _backgrounds = MutableStateFlow<List<SubscriptionBackgroundModel>>(listOf(
        SubscriptionBackgroundModel(
            id = "bg-default",
            imageUrl = "https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=1200&q=80",
            title = "Default Professional",
            subtitle = "Classic learning background",
            mappedAppIds = listOf("ALL"),
            isEnabled = true,
            order = 0
        )
    ))
    val backgrounds: StateFlow<List<SubscriptionBackgroundModel>> = _backgrounds.asStateFlow()

    fun updatePlanStatuses() {
        val now = System.currentTimeMillis()
        _plans.update { currentPlans ->
            var hasChanged = false
            val updated = currentPlans.map { plan ->
                if (plan.isSoftDeleted) return@map plan
                var newStatus = plan.status
                if (newStatus == PlanStatus.SCHEDULED && plan.activateAt != null && now >= plan.activateAt) {
                    newStatus = PlanStatus.ACTIVE
                }
                if (newStatus == PlanStatus.ACTIVE && plan.expiresAt != null && now >= plan.expiresAt) {
                    newStatus = PlanStatus.EXPIRED
                }
                if (newStatus != plan.status) {
                    hasChanged = true
                    plan.copy(status = newStatus)
                } else {
                    plan
                }
            }
            if (hasChanged) updated else currentPlans
        }
    }

    fun addOrUpdatePlan(plan: SubscriptionPlan) {
        _plans.update { current ->
            val existing = current.find { it.id == plan.id }
            if (existing != null) {
                // To keep existing users' purchased subscriptions intact, we actually soft delete the old one
                // and create a new one with the same display properties but new ID, OR we just let the model handle it.
                // "Never change existing users' purchased subscription terms when a plan is edited."
                // Since this is a simple mock, we'll implement it by soft deleting the old one and inserting the new one.
                val archived = existing.copy(isSoftDeleted = true)
                val newPlan = plan.copy(id = java.util.UUID.randomUUID().toString())
                current.filter { it.id != existing.id } + archived + newPlan
            } else {
                current + plan
            }
        }
    }

    fun deletePlan(id: String) {
        _plans.update { current ->
            current.map {
                if (it.id == id) it.copy(isSoftDeleted = true) else it
            }
        }
    }

    fun togglePlanStatus(id: String, isEnabled: Boolean) {
        _plans.update { current ->
            current.map {
                if (it.id == id) {
                    val newStatus = if (isEnabled) {
                        // Check if it should be scheduled or active based on current time
                        val now = System.currentTimeMillis()
                        if (it.activateAt != null && it.activateAt > now) PlanStatus.SCHEDULED
                        else if (it.expiresAt != null && it.expiresAt < now) PlanStatus.EXPIRED
                        else PlanStatus.ACTIVE
                    } else PlanStatus.INACTIVE
                    it.copy(status = newStatus)
                } else it
            }
        }
    }

    // Background Management
    fun addOrUpdateBackground(bg: SubscriptionBackgroundModel) {
        _backgrounds.update { current ->
            val existing = current.find { it.id == bg.id }
            if (existing != null) {
                current.map { if (it.id == bg.id) bg else it }
            } else {
                val maxOrder = current.maxOfOrNull { it.order } ?: -1
                current + bg.copy(order = maxOrder + 1)
            }
        }
    }

    fun deleteBackground(id: String) {
        _backgrounds.update { current ->
            current.filter { it.id != id }
        }
    }

    fun toggleBackground(id: String, isEnabled: Boolean) {
        _backgrounds.update { current ->
            current.map { if (it.id == id) it.copy(isEnabled = isEnabled) else it }
        }
    }

    fun reorderBackground(id: String, moveUp: Boolean) {
        _backgrounds.update { current ->
            val list = current.sortedBy { it.order }.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (moveUp && index > 0) {
                val item = list[index]
                val prev = list[index - 1]
                list[index] = prev.copy(order = item.order)
                list[index - 1] = item.copy(order = prev.order)
            } else if (!moveUp && index != -1 && index < list.size - 1) {
                val item = list[index]
                val next = list[index + 1]
                list[index] = next.copy(order = item.order)
                list[index + 1] = item.copy(order = next.order)
            }
            list.sortedBy { it.order }
        }
    }
}
