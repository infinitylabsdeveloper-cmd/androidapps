package com.example.service

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.db.AppDatabase
import com.example.db.UserEntitlementDao
import com.example.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SubscriptionPaymentVerificationServiceTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var entitlementDao: UserEntitlementDao
    private lateinit var service: SubscriptionPaymentVerificationService
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        entitlementDao = db.userEntitlementDao()
        service = SubscriptionPaymentVerificationService.createForTesting(
            context = context,
            entitlementDao = entitlementDao,
            firestore = null,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    // =========================================================================
    // 1. RAZORPAY SERVER-SIDE ORDER & SIGNATURE VERIFICATION
    // =========================================================================

    @Test
    fun createRazorpayOrder_generatesValidOrderIdWithMerchantId() = runTest(testDispatcher) {
        val orderId = service.createRazorpayOrder(
            planId = "12_months",
            amountRupees = 799,
            userId = "candidate_123"
        )
        assertNotNull(orderId)
        assertTrue(orderId.startsWith("order_T89U1OXHFIQbHa_"))
    }

    @Test
    fun verifyAndActivateSubscription_razorpaySuccess_activatesEntitlement() = runTest(testDispatcher) {
        val paymentId = "pay_test_1001"
        val orderId = "order_test_1001"

        val req = PaymentVerificationRequest(
            userId = "candidate_123",
            planId = "12_months",
            planTitle = "12 Months Complete Mastery",
            provider = PaymentProvider.RAZORPAY,
            providerOrderId = orderId,
            providerPaymentId = paymentId,
            providerSignature = "", // test mode signature fallback
            amountRupees = 799,
            durationHours = 365L * 24L
        )

        val result = service.verifyAndActivateSubscription(req)
        assertTrue("Verification should succeed", result is PaymentVerificationResult.Success)

        val success = result as PaymentVerificationResult.Success
        assertEquals(PaymentProvider.RAZORPAY, success.provider)
        assertEquals(799, success.amountRupees)
        assertNotNull(success.reportingToken)

        // Verify local Room DB state
        val stored = entitlementDao.getEntitlementByPaymentId(paymentId)
        assertNotNull(stored)
        assertEquals("ACTIVE", stored?.status)
        assertEquals("RAZORPAY", stored?.provider)
        assertEquals(false, stored?.isAdminGranted)
    }

    @Test
    fun verifyAndActivateSubscription_idempotencyPreventsDuplicateActivation() = runTest(testDispatcher) {
        val paymentId = "pay_idempotent_1"
        val req = PaymentVerificationRequest(
            userId = "candidate_123",
            planId = "12_months",
            planTitle = "12 Months Access",
            provider = PaymentProvider.RAZORPAY,
            providerOrderId = "order_123",
            providerPaymentId = paymentId,
            amountRupees = 799,
            durationHours = 24L
        )

        val first = service.verifyAndActivateSubscription(req)
        assertTrue(first is PaymentVerificationResult.Success)

        // Repeat the exact same payment request
        val duplicate = service.verifyAndActivateSubscription(req)
        assertTrue("Subsequent request must return Duplicate", duplicate is PaymentVerificationResult.Duplicate)
    }

    // =========================================================================
    // 2. GOOGLE PLAY BILLING FLOW
    // =========================================================================

    @Test
    fun verifyAndActivateSubscription_googlePlaySuccess_activatesUnifiedEntitlement() = runTest(testDispatcher) {
        val txnId = "GPA.1234-5678-9012"
        val req = PaymentVerificationRequest(
            userId = "candidate_play",
            planId = "6_months",
            planTitle = "6 Months Achiever Plan",
            provider = PaymentProvider.GOOGLE_PLAY,
            providerOrderId = txnId,
            providerPaymentId = txnId,
            purchaseToken = "tok_play_valid_abc",
            amountRupees = 499,
            durationHours = 180L * 24L
        )

        val result = service.verifyAndActivateSubscription(req)
        assertTrue(result is PaymentVerificationResult.Success)

        // Unified entitlement check
        val isPremium = service.isPremiumActive("appsc-group2")
        assertTrue("Unified entitlement must report active for Google Play", isPremium)
    }

    @Test
    fun verifyAndActivateSubscription_googlePlayMissingToken_fails() = runTest(testDispatcher) {
        val req = PaymentVerificationRequest(
            userId = "candidate_play",
            planId = "6_months",
            planTitle = "6 Months Achiever",
            provider = PaymentProvider.GOOGLE_PLAY,
            providerOrderId = "GPA.000",
            providerPaymentId = "GPA.000",
            purchaseToken = "", // missing token
            amountRupees = 499,
            durationHours = 24L
        )

        val result = service.verifyAndActivateSubscription(req)
        assertTrue(result is PaymentVerificationResult.Failed)
        val failed = result as PaymentVerificationResult.Failed
        assertEquals("Payment was unsuccessful. No Premium subscription has been activated.", failed.userMessage)
    }

    // =========================================================================
    // 3. RAZORPAY WEBHOOKS & IDEMPOTENCY
    // =========================================================================

    @Test
    fun processRazorpayWebhook_paymentCaptured_activatesSubscription() = runTest(testDispatcher) {
        val payload = """
            {
                "id": "evt_test_001",
                "event": "payment.captured",
                "payload": {
                    "payment": {
                        "entity": {
                            "id": "pay_wh_captured_1",
                            "order_id": "order_wh_1",
                            "amount": 79900,
                            "status": "captured",
                            "notes": {
                                "userId": "user_webhook_1",
                                "planId": "12_months",
                                "planTitle": "12 Months Mastery",
                                "appId": "appsc-group2"
                            }
                        }
                    }
                }
            }
        """.trimIndent()

        val (success, message) = service.processRazorpayWebhook(payload, signatureHeader = "")
        assertTrue(success)

        // Verify duplicate webhook event is detected and handled idempotently
        val (dupSuccess, dupMessage) = service.processRazorpayWebhook(payload, signatureHeader = "")
        assertTrue(dupSuccess)
        assertTrue(dupMessage.contains("Idempotent OK"))
    }

    @Test
    fun processRazorpayWebhook_paymentFailed_updatesStatus() = runTest(testDispatcher) {
        // Pre-create entitlement in Room
        val entitlement = com.example.db.UserEntitlementEntity(
            transactionId = "pay_failed_test",
            userId = "user_fail",
            planTitle = "1 Month Plan",
            amountRupees = 199,
            status = "PENDING"
        )
        entitlementDao.insertEntitlement(entitlement)

        val payload = """
            {
                "id": "evt_fail_002",
                "event": "payment.failed",
                "payload": {
                    "payment": {
                        "entity": {
                            "id": "pay_failed_test",
                            "order_id": "order_fail_test",
                            "amount": 19900,
                            "status": "failed"
                        }
                    }
                }
            }
        """.trimIndent()

        val (success, _) = service.processRazorpayWebhook(payload, signatureHeader = "")
        assertTrue(success)

        val updated = entitlementDao.getEntitlementByTransactionId("pay_failed_test")
        assertEquals("FAILED", updated?.status)
    }

    // =========================================================================
    // 4. ADMIN GRANTED SUBSCRIPTION SUPPORT
    // =========================================================================

    @Test
    fun grantAdminSubscription_createsClearlyMarkedAdminRecord() = runTest(testDispatcher) {
        val entitlement = service.grantAdminSubscription(
            userId = "student_scholarship",
            planTitle = "Full Syllabus Scholarship",
            durationDays = 90,
            adminEmail = "superadmin@appscprep.com"
        )

        assertNotNull(entitlement)
        assertTrue(entitlement.transactionId.startsWith("ADMIN_GRANT_"))
        assertEquals("ADMIN_GRANTED", entitlement.provider)
        assertTrue(entitlement.isAdminGranted)
        assertEquals(0, entitlement.amountRupees)
        assertTrue(entitlement.paymentMode.contains("superadmin@appscprep.com"))

        val isPremium = service.isPremiumActive("appsc-group2")
        assertTrue("Admin granted subscription must grant unified premium access", isPremium)
    }

    // =========================================================================
    // 5. ALTERNATIVE BILLING CONFIGURATION & INDIA ELIGIBILITY
    // =========================================================================

    @Test
    fun alternativeBillingConfig_indiaComplianceVerification() {
        val config = AlternativeBillingConfig.DEFAULT
        assertTrue(config.isAlternativeBillingEnabled)
        assertTrue(config.isEligibleForCountry("IN"))
        assertFalse(config.isEligibleForCountry("US"))
        assertEquals(listOf("google_play", "razorpay"), config.paymentProviders)
        assertEquals(listOf("google_play", "razorpay"), config.enabledPaymentMethods)
    }
}
