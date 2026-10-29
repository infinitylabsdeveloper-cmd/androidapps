package com.example.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Represents an App Instance in the Multi-Tenant Platform.
 * Stores core identity, status, and high-level metadata.
 */
data class FirestoreApp(
    @DocumentId
    val id: String = "", // This will be the appId (e.g., "app_001")
    val name: String = "",
    val shortName: String = "",
    val packageName: String = "",
    val status: AppStatus = AppStatus.DRAFT,
    val branding: BrandingPaths = BrandingPaths(),
    val settingsMetadata: Map<String, Any> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Paths to visual branding assets stored in Cloud Storage or remote URLs.
 */
data class BrandingPaths(
    val logoUrl: String = "",
    val iconUrl: String = "",
    val splashUrl: String = "",
    val faviconUrl: String = "",
    val heroBannerUrl: String = ""
)

/**
 * Represents the detailed configuration for a specific App.
 * Controls the Look & Feel (Theme), Features, and Regional settings.
 */
data class FirestoreAppConfiguration(
    @DocumentId
    val id: String = "", // Usually the same as appId or a unique configId
    val appId: String = "",
    val theme: FirestoreThemeConfig = FirestoreThemeConfig(),
    val typography: FirestoreTypographyConfig = FirestoreTypographyConfig(),
    val features: List<String> = emptyList(),
    val regional: FirestoreRegionalConfig = FirestoreRegionalConfig(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class FirestoreThemeConfig(
    val primaryColor: String = "#2563EB",
    val secondaryColor: String = "#1E40AF",
    val accentColor: String = "#F59E0B",
    val surfaceColor: String = "#FFFFFF",
    val backgroundColor: String = "#F8FAFC"
)

data class FirestoreTypographyConfig(
    val fontFamily: String = "Roboto",
    val baseFontSize: Int = 16,
    val headingFontFamily: String = "Roboto"
)

data class FirestoreRegionalConfig(
    val currencyCode: String = "INR",
    val currencySymbol: String = "₹",
    val primaryLanguage: String = "English",
    val allowedLanguages: List<String> = listOf("English", "Telugu")
)

/**
 * Dynamic content counts per syllabus category fetched from Cloud Firestore.
 */
data class CategoryContentCounts(
    val prelimsCount: Int = 0,
    val mainsCount: Int = 0,
    val generalCount: Int = 0,
    val allCount: Int = 0,
    val isFromFirestore: Boolean = false,
    val source: String = "local",
    val lastUpdated: Long = System.currentTimeMillis()
)

