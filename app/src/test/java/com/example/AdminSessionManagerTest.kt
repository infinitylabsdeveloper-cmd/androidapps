package com.example

import com.example.util.AdminRole
import com.example.util.AdminSessionManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AdminSessionManagerTest {

    @Before
    fun setup() {
        // Reset state
    }

    @Test
    fun testAdminEmailValidation() {
        assertTrue(AdminSessionManager.validateEmail("sobhadhanaraju0@gmail.com"))
        assertTrue(AdminSessionManager.validateEmail("sobhadhanaraju0@gmail.com"))
        assertTrue(AdminSessionManager.validateEmail("google.reviewer@appscprep.internal"))
        assertFalse(AdminSessionManager.validateEmail("unauthorized@example.com"))
        assertFalse(AdminSessionManager.validateEmail(""))
        assertFalse(AdminSessionManager.validateEmail(null))
    }

    @Test
    fun testAdminOtpLifecycle_realOtp() {
        val otp = AdminSessionManager.generateOtp(email = "sobhadhanaraju0@gmail.com", isReviewer = false)
        assertNotNull(otp)
        assertEquals(6, otp.length)
        assertTrue(otp.all { it.isDigit() })

        // Verifying with wrong OTP fails
        assertFalse(AdminSessionManager.verifyOtp("000000", isReviewer = false))

        // Verifying with correct real OTP succeeds
        assertTrue(AdminSessionManager.verifyOtp(otp, isReviewer = false))

        // Re-using same OTP fails (single use)
        assertFalse(AdminSessionManager.verifyOtp(otp, isReviewer = false))
    }

    @Test
    fun testTemporaryTestOtp_inDevEnvironment() {
        // Generate normal OTP
        AdminSessionManager.generateOtp(email = "sobhadhanaraju0@gmail.com", isReviewer = false)

        // Test OTP "123456" must verify successfully in dev environment
        if (AdminSessionManager.isDevEnvironment) {
            assertTrue("Test OTP 123456 should work in development environment", 
                AdminSessionManager.verifyOtp("123456", isReviewer = false))
        }
    }

    @Test
    fun testReviewerOtp() {
        val reviewerOtp = AdminSessionManager.generateOtp(isReviewer = true)
        assertEquals(AdminSessionManager.REVIEWER_TEST_OTP, reviewerOtp)

        assertTrue(AdminSessionManager.verifyOtp(AdminSessionManager.REVIEWER_TEST_OTP, isReviewer = true))
    }

    @Test
    fun testMaxAttemptsLockout() {
        AdminSessionManager.generateOtp(email = "sobhadhanaraju0@gmail.com", isReviewer = false)

        // 5 wrong attempts
        repeat(5) {
            assertFalse(AdminSessionManager.verifyOtp("99999$it", isReviewer = false))
        }

        // 6th attempt even with correct code must be rejected due to lockout
        assertFalse(AdminSessionManager.verifyOtp("123456", isReviewer = false))
    }
}
