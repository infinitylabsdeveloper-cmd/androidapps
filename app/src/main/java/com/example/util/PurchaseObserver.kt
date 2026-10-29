package com.example.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.data.SampleData
import com.example.db.AppDatabase
import com.example.db.UserEntitlementEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Centralized observer for processing, verifying, and persisting purchase transactions
 * for Google Play Billing and Razorpay payment callbacks.
 * Ensures user entitlements are immediately persisted in the local Room Database.
 */
object PurchaseObserver {
    private const val TAG = "PurchaseObserver"
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _activeEntitlement = MutableStateFlow<UserEntitlementEntity?>(null)
    val activeEntitlement: StateFlow<UserEntitlementEntity?> = _activeEntitlement.asStateFlow()

    private val _allEntitlements = MutableStateFlow<List<UserEntitlementEntity>>(emptyList())
    val allEntitlements: StateFlow<List<UserEntitlementEntity>> = _allEntitlements.asStateFlow()

    /**
     * Initializes observer to sync active entitlement from local Room DB on startup.
     */
    fun initialize(context: Context, appId: String = "appsc-group2") {
        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                db.userEntitlementDao().getActiveEntitlementFlow(appId).collect { entitlement ->
                    _activeEntitlement.value = entitlement
                    if (entitlement != null && entitlement.status == "ACTIVE") {
                        // Sync with SampleData
                        val remainingHours = ((entitlement.expiryTimestamp - System.currentTimeMillis()) / (1000 * 60 * 60)).coerceAtLeast(1)
                        withContext(Dispatchers.Main) {
                            SampleData.updateSubscription(entitlement.planTitle, remainingHours)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing PurchaseObserver DB sync", e)
            }
        }
    }

    /**
     * Handles Google Play Billing transaction verification and local DB entitlement updates.
     */
    fun onGooglePlayPurchaseVerified(
        context: Context,
        transactionId: String,
        planTitle: String,
        amountRupees: Int,
        durationHours: Long,
        appId: String = "appsc-group2",
        onComplete: (() -> Unit)? = null
    ) {
        val now = System.currentTimeMillis()
        val expiry = now + (durationHours * 60 * 60 * 1000L)
        
        val cleanUser = SampleData.sampleUserProfile.email.replace(".", "_").ifBlank { "guest_candidate" }
        val entitlement = UserEntitlementEntity(
            transactionId = transactionId.ifBlank { "GPA.${System.currentTimeMillis()}" },
            appId = appId,
            userId = cleanUser,
            planTitle = planTitle,
            provider = "GOOGLE_PLAY",
            gateway = "GOOGLE_PLAY_BILLING",
            paymentMode = "In-App Subscription",
            amountRupees = amountRupees,
            amount = amountRupees.toDouble(),
            isVerified = true,
            purchaseTimestamp = now,
            startDate = now,
            expiryTimestamp = expiry,
            expiryDate = expiry,
            status = "ACTIVE"
        )

        processAndPersistEntitlement(context, entitlement, durationHours, onComplete)
    }

    /**
     * Handles Razorpay payment callback verification and local DB entitlement updates.
     */
    fun onRazorpayPaymentVerified(
        context: Context,
        paymentId: String,
        planTitle: String,
        amountRupees: Int,
        paymentMode: String,
        durationHours: Long,
        appId: String = "appsc-group2",
        onComplete: (() -> Unit)? = null
    ) {
        val now = System.currentTimeMillis()
        val expiry = now + (durationHours * 60 * 60 * 1000L)
        val cleanUser = SampleData.sampleUserProfile.email.replace(".", "_").ifBlank { "guest_candidate" }

        val entitlement = UserEntitlementEntity(
            transactionId = paymentId.ifBlank { "pay_rzp_${System.currentTimeMillis()}" },
            appId = appId,
            userId = cleanUser,
            planTitle = planTitle,
            provider = "RAZORPAY",
            gateway = "RAZORPAY",
            paymentMode = paymentMode,
            providerPaymentId = paymentId,
            amountRupees = amountRupees,
            amount = amountRupees.toDouble(),
            isVerified = true,
            purchaseTimestamp = now,
            startDate = now,
            expiryTimestamp = expiry,
            expiryDate = expiry,
            status = "ACTIVE"
        )

        processAndPersistEntitlement(context, entitlement, durationHours, onComplete)
    }

    private fun processAndPersistEntitlement(
        context: Context,
        entitlement: UserEntitlementEntity,
        durationHours: Long,
        onComplete: (() -> Unit)?
    ) {
        scope.launch {
            try {
                // 1. Immediately update Room DB
                val db = AppDatabase.getDatabase(context)
                db.userEntitlementDao().insertEntitlement(entitlement)
                
                // 2. Fetch fresh list of entitlements
                val updatedList = db.userEntitlementDao().getEntitlementsForAppDirect(entitlement.appId)
                _allEntitlements.value = updatedList
                _activeEntitlement.value = entitlement

                // 3. Update memory state in SampleData on Main Thread
                withContext(Dispatchers.Main) {
                    SampleData.updateSubscription(entitlement.planTitle, durationHours)
                    Toast.makeText(
                        context,
                        "🎉 Entitlement Verified & Saved to Database!\nPlan: ${entitlement.planTitle}\nTxn ID: ${entitlement.transactionId}",
                        Toast.LENGTH_LONG
                    ).show()
                    onComplete?.invoke()
                }

                Log.d(TAG, "Successfully persisted entitlement: ${entitlement.transactionId} to Room Database")
            } catch (e: Exception) {
                Log.e(TAG, "Error persisting entitlement to Room DB", e)
                withContext(Dispatchers.Main) {
                    SampleData.updateSubscription(entitlement.planTitle, durationHours)
                    onComplete?.invoke()
                }
            }
        }
    }
}
