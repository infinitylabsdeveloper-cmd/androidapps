package com.example.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.BuildConfig
import com.example.model.TenantFeatureFlag
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TenantInitializationManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
    }

    @Test
    fun `test tenant initialization completes and returns result`() = runBlocking {
        val result = TenantInitializationManager.initializeTenant(
            context = context,
            timeoutMs = 1500L
        )

        assertNotNull(result)
        assertEquals(BuildConfig.TENANT_ID, result.tenantId)
        assertEquals(BuildConfig.APP_ID, result.appId)
        assertNotNull(result.appName)
        assertTrue(result.appName.isNotBlank())
        assertTrue(result.primaryColorHex != 0L)
        assertNotNull(result.appliedFeatureFlags)
        assertTrue(result.appliedFeatureFlags.containsKey("mcqsEnabled"))
        assertTrue(TenantInitializationManager.isReady.value)
    }

    @Test
    fun `test active theme and branding accessors after initialization`() = runBlocking {
        TenantInitializationManager.initializeTenant(context = context, timeoutMs = 1000L)

        val branding = TenantInitializationManager.getActiveBranding()
        val theme = TenantInitializationManager.getActiveTheme()
        val flags = TenantInitializationManager.getActiveFeatureFlags()

        assertNotNull(branding)
        assertNotNull(theme)
        assertNotNull(flags)
        assertTrue(branding.appName.isNotBlank())
        assertTrue(theme.primaryColorHex != 0L)
    }

    @Test
    fun `test feature flag querying via manager`() = runBlocking {
        TenantInitializationManager.initializeTenant(context = context, timeoutMs = 1000L)

        val isMcqsEnabled = TenantInitializationManager.isFeatureEnabled("mcqsEnabled")
        assertTrue(isMcqsEnabled)

        val isAiMentorEnabled = TenantInitializationManager.isFeatureEnabled(TenantFeatureFlag.AI_MENTOR)
        assertTrue(isAiMentorEnabled)
    }
}
