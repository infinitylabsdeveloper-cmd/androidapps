package com.example.model

import java.util.UUID

enum class LegalDocType(val id: String, val title: String, val icon: String) {
    PRIVACY_POLICY("privacy_policy", "Privacy Policy", "Shield"),
    TERMS_CONDITIONS("terms_conditions", "Terms & Conditions", "Assignment"),
    REFUND_CANCELLATION("refund_policy", "Refund & Cancellation Policy", "CurrencyExchange"),
    SUBSCRIPTION_TERMS("subscription_terms", "Subscription Terms", "Star"),
    ACCOUNT_DELETION("account_deletion", "Account & Data Deletion", "Delete"),
    DISCLAIMER("disclaimer", "Disclaimer", "Info"),
    COPYRIGHT_POLICY("copyright_policy", "Copyright / Intellectual Property", "Copyright"),
    REPORTING_POLICY("reporting_policy", "Content / Question Reporting", "Flag"),
    CONTACT_US("contact_us", "Contact Us", "ContactSupport"),
    FAQ("faq", "Frequently Asked Questions", "Quiz"),
    HELP_CENTER("help_center", "Help Center", "Help"),
    ABOUT_US("about_us", "About Us", "Business"),
    SHIPPING_DELIVERY("shipping_policy", "Shipping & Digital Delivery Policy", "LocalShipping")
}

data class LegalDocument(
    val id: String = UUID.randomUUID().toString(),
    val type: LegalDocType,
    val title: String,
    val content: String,
    val mappedAppIds: List<String> = emptyList(), // Empty means "All Apps"
    val isEnabled: Boolean = true,
    val order: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val bgGradientStart: String? = null,
    val bgGradientEnd: String? = null,
    val accentColorHex: String? = null,
    val iconName: String? = null,
    val isPublished: Boolean = true,
    val sectionsJson: String? = null,
    val socialLinksJson: String? = null,
    val draftContent: String? = null,
    val draftSectionsJson: String? = null,
    val draftTitle: String? = null
)

data class SupportFaq(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val mappedAppIds: List<String> = emptyList(),
    val isEnabled: Boolean = true,
    val order: Int = 0
)

data class ContactInfo(
    val id: String = UUID.randomUUID().toString(),
    val appId: String,
    val supportEmail: String,
    val supportPhone: String,
    val website: String,
    val address: String,
    val businessName: String,
    val whatsappNumber: String? = null
)
