package com.example.model

data class DrawerHeaderConfig(
    val backgroundImageUrl: String = "",
    val showBackgroundImage: Boolean = true,
    val overlayOpacity: Float = 0.15f,
    val headerHeightDp: Int = 145,
    val title: String = "APPSC Group-2",
    val titleSparkle: String = "✨",
    val subtitle: String = "Strive for excellence, victory will follow! ✨",
    val quotation: String = "Success belongs to those who prepare today for the battles of tomorrow.",
    val sloganText: String = "Your Goal  Our Support  A Brighter Andhra",
    val sloganTagline: String = "Study Prepare Achieve",
    val logoIconName: String = "school",
    val logoBgColorHex: Long = 0xFF0A1B3A,
    val logoBorderColorHex: Long = 0xFFF59E0B,

    // Extended styling & typography properties
    val fontFamily: String = "Inter",
    val titleFontSizeSp: Float = 17.5f,
    val titleFontWeight: String = "ExtraBold",
    val titleTextColorHex: Long = 0xFFFEF08A,
    val subtitleFontSizeSp: Float = 11f,
    val subtitleFontWeight: String = "Bold",
    val subtitleTextColorHex: Long = 0xFFE2E8F0,
    val quotationFontSizeSp: Float = 11f,
    val quotationFontWeight: String = "Bold",
    val quotationTextColorHex: Long = 0xFFE2E8F0,
    val sloganFontSizeSp: Float = 10.5f,
    val sloganTextColorHex: Long = 0xFFFFFFFF,
    val imageScaleType: String = "Crop", // Crop, Fit, FillBounds, Inside, FillWidth
    val imageOverlayColorHex: Long = 0x80000000
)

data class DrawerMenuItemConfig(
    val id: String,
    val sectionKey: String, // MAIN, EXAM_HUB, EXAM_TOOLS, LEGAL_SUPPORT, ACCOUNT
    val title: String,
    val subtitle: String? = null,
    val iconName: String,
    val badgeText: String? = null,
    val badgeBgColorHex: Long = 0xFF2563EB,
    val badgeTextColorHex: Long = 0xFFFFFFFF,
    val cardBgColorHex: Long = 0xFFFFFFFF,
    val cardBorderColorHex: Long = 0xFFE2E8F0,
    val iconBoxBgColorHex: Long = 0xFFEFF6FF,
    val iconTintHex: Long = 0xFF2563EB,
    val hasLeftBarAccent: Boolean = false,
    val leftBarColorHex: Long = 0xFF4F46E5,
    val isVisible: Boolean = true,
    val order: Int = 0,

    // Extended premium styling properties
    val fontFamily: String = "Inter",
    val titleFontSizeSp: Float = 13.2f,
    val subtitleFontSizeSp: Float = 10.5f,
    val fontWeight: String = "Bold", // Light, Normal, Medium, SemiBold, Bold, ExtraBold, Black
    val titleTextColorHex: Long? = null, // null falls back to theme default
    val subtitleTextColorHex: Long? = null, // null falls back to theme default
    val useGradient: Boolean = false,
    val gradientStartColorHex: Long = 0xFF3B82F6,
    val gradientEndColorHex: Long = 0xFF1D4ED8,
    val gradientDirection: String = "Diagonal", // Horizontal, Vertical, Diagonal
    val cardImageUrl: String = "",
    val showCardImage: Boolean = false,
    val cardImageOpacity: Float = 0.25f,
    val cardImageScaleType: String = "Crop", // Crop, Fit, FillBounds, FillWidth
    val cardImageOverlayColorHex: Long = 0x40000000,
    val showIcon: Boolean = true,
    val iconBoxCornerRadiusDp: Int = 10,
    val borderThicknessDp: Int = 1,
    val cornerRadiusDp: Int = 14,
    val badgeFontSizeSp: Float = 9f,
    val badgeFontWeight: String = "Black"
)

data class DrawerSectionHeaderConfig(
    val sectionKey: String,
    val title: String,
    val iconName: String,
    val scriptText: String? = null,
    val isVisible: Boolean = true,

    // Extended styling customization
    val fontFamily: String = "Inter",
    val fontSizeSp: Float = 12f,
    val fontWeight: String = "ExtraBold",
    val titleColorHex: Long? = null, // null falls back to theme default
    val iconTintHex: Long? = null,
    val scriptTextColorHex: Long? = null,
    val scriptFontSizeSp: Float = 10f,
    val scriptFontWeight: String = "Bold"
)

data class DrawerConfig(
    val isEnabled: Boolean = true,
    val drawerWidthDp: Int = 320,
    val targetScope: String = "CURRENT", // CURRENT (Individual App), SELECTED (Multiple Selected Apps), ALL (All Apps)
    val selectedAppIds: List<String> = emptyList(),
    val header: DrawerHeaderConfig = DrawerHeaderConfig(),
    val sections: List<DrawerSectionHeaderConfig> = defaultDrawerSections(),
    val items: List<DrawerMenuItemConfig> = defaultDrawerItems()
)

fun defaultDrawerSections(): List<DrawerSectionHeaderConfig> {
    return listOf(
        DrawerSectionHeaderConfig(
            sectionKey = "EXAM_HUB",
            title = "EXAM HUB & PRACTICE",
            iconName = "menu_book",
            scriptText = null
        ),
        DrawerSectionHeaderConfig(
            sectionKey = "EXAM_TOOLS",
            title = "EXAM TOOLS",
            iconName = "settings",
            scriptText = "Smart Tools Brighter Results"
        ),
        DrawerSectionHeaderConfig(
            sectionKey = "LEGAL_SUPPORT",
            title = "LEGAL & SUPPORT",
            iconName = "verified_user",
            scriptText = "Official Policy & Help"
        )
    )
}

fun defaultDrawerItems(): List<DrawerMenuItemConfig> {
    return listOf(
        // TOP CARDS
        DrawerMenuItemConfig(
            id = "my_profile",
            sectionKey = "MAIN",
            title = "My Profile & Settings",
            subtitle = "Tap to customize account",
            iconName = "person",
            cardBgColorHex = 0xFFEBF5FF,
            cardBorderColorHex = 0xFFBAE6FD,
            iconBoxBgColorHex = 0xFF38BDF8,
            iconTintHex = 0xFFFFFFFF,
            order = 1
        ),
        DrawerMenuItemConfig(
            id = "syllabus_home",
            sectionKey = "MAIN",
            title = "Syllabus Home",
            subtitle = null,
            iconName = "home",
            hasLeftBarAccent = true,
            leftBarColorHex = 0xFF4F46E5,
            cardBgColorHex = 0xFFF3F0FF,
            cardBorderColorHex = 0xFFE0E7FF,
            iconBoxBgColorHex = 0xFFDDD6FE,
            iconTintHex = 0xFF4F46E5,
            order = 2
        ),

        // EXAM HUB & PRACTICE
        DrawerMenuItemConfig(
            id = "mock_test_generator",
            sectionKey = "EXAM_HUB",
            title = "Mock Test Generator",
            subtitle = "Create custom mock tests",
            iconName = "auto_awesome",
            badgeText = "CUSTOM",
            badgeBgColorHex = 0xFF8B5CF6,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFFAF5FF,
            cardBorderColorHex = 0xFFF3E8FF,
            iconBoxBgColorHex = 0xFFF3E8FF,
            iconTintHex = 0xFFA855F7,
            order = 3
        ),
        DrawerMenuItemConfig(
            id = "available_mock_tests",
            sectionKey = "EXAM_HUB",
            title = "Available Mock Tests",
            subtitle = "Attempt topic & full tests",
            iconName = "assignment",
            badgeText = "0 TESTS",
            badgeBgColorHex = 0xFF22C55E,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFF0FDF4,
            cardBorderColorHex = 0xFFDCFCE7,
            iconBoxBgColorHex = 0xFFDCFCE7,
            iconTintHex = 0xFF16A34A,
            order = 4
        ),
        DrawerMenuItemConfig(
            id = "current_affairs",
            sectionKey = "EXAM_HUB",
            title = "Current Affairs",
            subtitle = "Daily updates & PDFs",
            iconName = "newspaper",
            badgeText = "DAILY",
            badgeBgColorHex = 0xFF0EA5E9,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFFFF7ED,
            cardBorderColorHex = 0xFFFFEDD5,
            iconBoxBgColorHex = 0xFFFFEDD5,
            iconTintHex = 0xFFEA580C,
            order = 5
        ),
        DrawerMenuItemConfig(
            id = "bookmarks",
            sectionKey = "EXAM_HUB",
            title = "Bookmarks",
            subtitle = "Saved questions & revision",
            iconName = "bookmark",
            badgeText = "SAVED",
            badgeBgColorHex = 0xFFF59E0B,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFFFFBEB,
            cardBorderColorHex = 0xFFFEF3C7,
            iconBoxBgColorHex = 0xFFFEF3C7,
            iconTintHex = 0xFFD97706,
            order = 6
        ),
        DrawerMenuItemConfig(
            id = "previous_year_papers",
            sectionKey = "EXAM_HUB",
            title = "Previous Year Papers",
            subtitle = "Solved question papers & solutions",
            iconName = "history_edu",
            badgeText = "PYP",
            badgeBgColorHex = 0xFF2563EB,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFEFF6FF,
            cardBorderColorHex = 0xFFDBEAFE,
            iconBoxBgColorHex = 0xFFDBEAFE,
            iconTintHex = 0xFF2563EB,
            order = 7
        ),
        DrawerMenuItemConfig(
            id = "performance_analytics",
            sectionKey = "EXAM_HUB",
            title = "Performance & Analytics",
            subtitle = "Track your progress",
            iconName = "bar_chart",
            badgeText = "LIVE",
            badgeBgColorHex = 0xFF22C55E,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFFFF1F2,
            cardBorderColorHex = 0xFFFFE4E6,
            iconBoxBgColorHex = 0xFFFFE4E6,
            iconTintHex = 0xFFE11D48,
            order = 6
        ),
        DrawerMenuItemConfig(
            id = "state_leaderboard",
            sectionKey = "EXAM_HUB",
            title = "State Live Leaderboard",
            subtitle = "Compete with aspirants",
            iconName = "emoji_events",
            badgeText = "TOP LEARNERS",
            badgeBgColorHex = 0xFFEAB308,
            badgeTextColorHex = 0xFF0F172A,
            cardBgColorHex = 0xFFFEFCE8,
            cardBorderColorHex = 0xFFFEF08A,
            iconBoxBgColorHex = 0xFFFEF08A,
            iconTintHex = 0xFFD97706,
            order = 7
        ),
        DrawerMenuItemConfig(
            id = "offline_cloud_sync",
            sectionKey = "EXAM_HUB",
            title = "Offline & Cloud Sync",
            subtitle = "Study anytime, anywhere",
            iconName = "cloud_sync",
            badgeText = "OFFLINE",
            badgeBgColorHex = 0xFF06B6D4,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFECFEFF,
            cardBorderColorHex = 0xFFCFFAFE,
            iconBoxBgColorHex = 0xFFCFFAFE,
            iconTintHex = 0xFF0D9488,
            order = 8
        ),

        // EXAM TOOLS
        DrawerMenuItemConfig(
            id = "subscriptions",
            sectionKey = "EXAM_TOOLS",
            title = "Subscriptions",
            subtitle = "Manage your plans",
            iconName = "workspace_premium",
            badgeText = "PRO",
            badgeBgColorHex = 0xFFF97316,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFFFFBEB,
            cardBorderColorHex = 0xFFFEF08A,
            iconBoxBgColorHex = 0xFFFEF08A,
            iconTintHex = 0xFFD97706,
            order = 9
        ),
        DrawerMenuItemConfig(
            id = "scientific_calculator",
            sectionKey = "EXAM_TOOLS",
            title = "Scientific Calculator",
            subtitle = "Fast & accurate calculations",
            iconName = "calculate",
            badgeText = null,
            cardBgColorHex = 0xFFEFF6FF,
            cardBorderColorHex = 0xFFDBEAFE,
            iconBoxBgColorHex = 0xFFDBEAFE,
            iconTintHex = 0xFF2563EB,
            order = 10
        ),
        DrawerMenuItemConfig(
            id = "share_app",
            sectionKey = "EXAM_TOOLS",
            title = "Share App with Friends",
            subtitle = "Help others in their journey",
            iconName = "share",
            badgeText = "SHARE",
            badgeBgColorHex = 0xFF2563EB,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFF0FDF4,
            cardBorderColorHex = 0xFFDCFCE7,
            iconBoxBgColorHex = 0xFFDCFCE7,
            iconTintHex = 0xFF16A34A,
            order = 11
        ),

        // LEGAL & SUPPORT
        DrawerMenuItemConfig(
            id = "legal_refund_policy",
            sectionKey = "LEGAL_SUPPORT",
            title = "Refund and cancellation policy",
            subtitle = "Terms for purchases & refunds",
            iconName = "receipt_long",
            badgeText = "REFUND",
            badgeBgColorHex = 0xFFD97706,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFFFFBEB,
            cardBorderColorHex = 0xFFFDE68A,
            iconBoxBgColorHex = 0xFFFEF3C7,
            iconTintHex = 0xFFD97706,
            hasLeftBarAccent = true,
            leftBarColorHex = 0xFFD97706,
            order = 14
        ),
        DrawerMenuItemConfig(
            id = "legal_privacy_policy",
            sectionKey = "LEGAL_SUPPORT",
            title = "Privacy policy",
            subtitle = "Data security & privacy guidelines",
            iconName = "security",
            badgeText = "SECURE",
            badgeBgColorHex = 0xFF059669,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFECFDF5,
            cardBorderColorHex = 0xFFA7F3D0,
            iconBoxBgColorHex = 0xFFD1FAE5,
            iconTintHex = 0xFF059669,
            hasLeftBarAccent = true,
            leftBarColorHex = 0xFF059669,
            order = 15
        ),
        DrawerMenuItemConfig(
            id = "legal_terms_conditions",
            sectionKey = "LEGAL_SUPPORT",
            title = "Terms and conditions",
            subtitle = "App usage & service agreement",
            iconName = "description",
            badgeText = "RULES",
            badgeBgColorHex = 0xFF4F46E5,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFEEF2FF,
            cardBorderColorHex = 0xFFC7D2FE,
            iconBoxBgColorHex = 0xFFE0E7FF,
            iconTintHex = 0xFF4F46E5,
            hasLeftBarAccent = true,
            leftBarColorHex = 0xFF4F46E5,
            order = 16
        ),
        DrawerMenuItemConfig(
            id = "legal_faq",
            sectionKey = "LEGAL_SUPPORT",
            title = "FAQ'S",
            subtitle = "Frequently asked questions",
            iconName = "quiz",
            badgeText = "FAQS",
            badgeBgColorHex = 0xFF0284C7,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFF0F9FF,
            cardBorderColorHex = 0xFFBAE6FD,
            iconBoxBgColorHex = 0xFFE0F2FE,
            iconTintHex = 0xFF0284C7,
            hasLeftBarAccent = true,
            leftBarColorHex = 0xFF0284C7,
            order = 17
        ),
        DrawerMenuItemConfig(
            id = "legal_about_us",
            sectionKey = "LEGAL_SUPPORT",
            title = "About us",
            subtitle = "App mission & story",
            iconName = "info",
            badgeText = "INFO",
            badgeBgColorHex = 0xFF9333EA,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFFAF5FF,
            cardBorderColorHex = 0xFFE9D5FF,
            iconBoxBgColorHex = 0xFFF3E8FF,
            iconTintHex = 0xFF9333EA,
            hasLeftBarAccent = true,
            leftBarColorHex = 0xFF9333EA,
            order = 18
        ),
        DrawerMenuItemConfig(
            id = "legal_contact_us",
            sectionKey = "LEGAL_SUPPORT",
            title = "Contact us",
            subtitle = "Support desk & email",
            iconName = "contact_support",
            badgeText = "24/7",
            badgeBgColorHex = 0xFFE11D48,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFFFF1F2,
            cardBorderColorHex = 0xFFFECDD3,
            iconBoxBgColorHex = 0xFFFFE4E6,
            iconTintHex = 0xFFE11D48,
            hasLeftBarAccent = true,
            leftBarColorHex = 0xFFE11D48,
            order = 19
        ),
        DrawerMenuItemConfig(
            id = "legal_help_center",
            sectionKey = "LEGAL_SUPPORT",
            title = "Help center",
            subtitle = "Tutorials & guides",
            iconName = "help",
            badgeText = "HELP",
            badgeBgColorHex = 0xFF0D9488,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFF0FDFA,
            cardBorderColorHex = 0xFF99F6E4,
            iconBoxBgColorHex = 0xFFCCFBF1,
            iconTintHex = 0xFF0D9488,
            hasLeftBarAccent = true,
            leftBarColorHex = 0xFF0D9488,
            order = 20
        ),
        DrawerMenuItemConfig(
            id = "account_deletion",
            sectionKey = "LEGAL_SUPPORT",
            title = "Account and data deletion",
            subtitle = "Manage personal data removal",
            iconName = "delete_forever",
            badgeText = "DANGER",
            badgeBgColorHex = 0xFFDC2626,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFFEF2F2,
            cardBorderColorHex = 0xFFFECACA,
            iconBoxBgColorHex = 0xFFFEE2E2,
            iconTintHex = 0xFFDC2626,
            hasLeftBarAccent = true,
            leftBarColorHex = 0xFFDC2626,
            order = 21
        ),
        DrawerMenuItemConfig(
            id = "report_issue",
            sectionKey = "LEGAL_SUPPORT",
            title = "Report an issue",
            subtitle = "Submit bugs & feedback to dev team",
            iconName = "bug_report",
            badgeText = "SUPPORT",
            badgeBgColorHex = 0xFFDC2626,
            badgeTextColorHex = 0xFFFFFFFF,
            cardBgColorHex = 0xFFFEF2F2,
            cardBorderColorHex = 0xFFFECACA,
            iconBoxBgColorHex = 0xFFFEE2E2,
            iconTintHex = 0xFFDC2626,
            hasLeftBarAccent = true,
            leftBarColorHex = 0xFFDC2626,
            order = 22
        ),

        // ACCOUNT
        DrawerMenuItemConfig(
            id = "account_logout",
            sectionKey = "ACCOUNT",
            title = "Candidate Account / Logout",
            subtitle = "Sign out or manage login",
            iconName = "logout",
            cardBgColorHex = 0xFFF3E8FF,
            cardBorderColorHex = 0xFFE9D5FF,
            iconBoxBgColorHex = 0xFFE9D5FF,
            iconTintHex = 0xFF7C3AED,
            order = 23
        )
    )
}
