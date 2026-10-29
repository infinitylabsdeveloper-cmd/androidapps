package com.example.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.BuildConfig
import com.example.model.TenantFeatureFlag
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TenantConfigRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: TenantConfigRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        repository = TenantConfigRepository.getInstance(context)
        repository.clearCache()
    }

    @Test
    fun `test initial configuration reflects BuildConfig TENANT_ID`() {
        val config = repository.getCurrentConfig()
        assertNotNull(config)
        assertEquals(BuildConfig.TENANT_ID, config.tenantId)
        assertNotNull(config.branding)
        assertNotNull(config.featureFlags)
        assertNotNull(config.theme)
    }

    @Test
    fun `test branding and theme accessors`() {
        val branding = repository.getBranding()
        val theme = repository.getTheme()

        assertNotNull(branding.appName)
        assertTrue(branding.appName.isNotBlank())
        assertNotNull(theme.primaryColorHex)
    }

    @Test
    fun `test feature flag querying`() {
        val isMcqEnabled = repository.isFeatureEnabled("mcqsEnabled")
        assertTrue(isMcqEnabled)

        val enumFlagEnabled = repository.isFeatureEnabled(TenantFeatureFlag.MCQS)
        assertTrue(enumFlagEnabled)
    }

    @Test
    fun `test clearing cache resets to defaults`() {
        repository.clearCache()
        val config = repository.getCurrentConfig()
        assertFalse(config.isCached)
    }
}
