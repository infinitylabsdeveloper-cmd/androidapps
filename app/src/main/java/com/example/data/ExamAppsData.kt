package com.example.data

import com.example.model.AppTenantModel
import com.example.model.AppStatus

/**
 * Multi-Tenant Exam Applications Configuration
 * Complies with strict structure and medium rules:
 * 1. Existing initial apps are English (APPSC Group 1, Group 2, AP DSC, AP TET).
 * 2. State Government Exams: State Mother/Regional Language + English + Urdu (only if officially available).
 * 3. Central Government Exams: Hindi + English + Urdu (only if officially available).
 * 4. Strictly unique: exactly one app per Exam + Language combination (no duplicates).
 * 5. Serial ordering: State-wise first, followed by Central categories.
 * 6. Urdu only where officially conducted by the recruiting board/commission.
 */
object ExamAppsData {

    private fun createApp(
        id: String,
        name: String,
        shortName: String,
        category: String,
        examName: String,
        medium: String,
        primaryLanguage: String = medium,
        languageCode: String = when (medium) {
            "Telugu" -> "te"
            "Hindi" -> "hi"
            "Urdu" -> "ur"
            "Tamil" -> "ta"
            "Kannada" -> "kn"
            "Malayalam" -> "ml"
            "Marathi" -> "mr"
            "Gujarati" -> "gu"
            "Bengali" -> "bn"
            "Odia" -> "or"
            "Punjabi" -> "pa"
            "Assamese" -> "as"
            "Manipuri" -> "mni"
            "Mizo" -> "lus"
            "Nepali" -> "ne"
            else -> "en"
        },
        region: String = category,
        primaryColorHex: Long = when (medium) {
            "Urdu" -> 0xFF059669
            "Hindi" -> 0xFFD97706
            "Telugu" -> 0xFF7C3AED
            "Tamil" -> 0xFFDC2626
            "Kannada" -> 0xFFEA580C
            "Malayalam" -> 0xFF0D9488
            "Marathi" -> 0xFF9333EA
            "Gujarati" -> 0xFFC026D3
            "Bengali" -> 0xFF0284C7
            "Odia" -> 0xFF0891B2
            "Punjabi" -> 0xFF4F46E5
            "Assamese" -> 0xFF16A34A
            "Manipuri" -> 0xFF059669
            "Mizo" -> 0xFFE11D48
            "Nepali" -> 0xFFD97706
            else -> 0xFF2563EB
        },
        secondaryColorHex: Long = when (medium) {
            "Urdu" -> 0xFF047857
            "Hindi" -> 0xFFB45309
            "Telugu" -> 0xFF6D28D9
            "Tamil" -> 0xFFB91C1C
            "Kannada" -> 0xFFC2410C
            "Malayalam" -> 0xFF0F766E
            "Marathi" -> 0xFF7E22CE
            "Gujarati" -> 0xFFA21CAF
            "Bengali" -> 0xFF0369A1
            "Odia" -> 0xFF0E7490
            "Punjabi" -> 0xFF4338CA
            "Assamese" -> 0xFF15803D
            "Manipuri" -> 0xFF047857
            "Mizo" -> 0xFFBE123C
            "Nepali" -> 0xFFB45309
            else -> 0xFF1D4ED8
        },
        accentColorHex: Long = 0xFFF59E0B,
        backgroundColorHex: Long = 0xFFF8FAFC,
        surfaceColorHex: Long = 0xFFFFFFFF,
        textColorHex: Long = 0xFF0F172A,
        buttonColorHex: Long = primaryColorHex,
        errorColorHex: Long = 0xFFDC2626,
        successColorHex: Long = 0xFF10B981
    ): AppTenantModel {
        return AppTenantModel(
            id = id,
            name = name,
            shortName = shortName,
            category = category,
            examName = examName,
            description = "Complete syllabus MCQs, Mock Tests, Practice Arena & Study Material for $examName ($medium).",
            status = AppStatus.ACTIVE,
            primaryLanguage = primaryLanguage,
            allowedLanguages = listOf(medium, "English").distinct(),
            medium = medium,
            languageCode = languageCode,
            region = region,
            primaryColorHex = primaryColorHex,
            secondaryColorHex = secondaryColorHex,
            accentColorHex = accentColorHex,
            backgroundColorHex = backgroundColorHex,
            surfaceColorHex = surfaceColorHex,
            textColorHex = textColorHex,
            buttonColorHex = buttonColorHex,
            errorColorHex = errorColorHex,
            successColorHex = successColorHex,
            packageName = "com.example.$id",
            featuresEnabled = listOf("MCQs", "Current Affairs", "Interactive CA Sets", "Posters", "Subscriptions", "Daily Quiz", "Mock Tests")
        )
    }

    val allExams = listOf(
        // =========================================================================
        // 1. ANDHRA PRADESH (Mother Language: Telugu, Official 2nd: Urdu)
        // Note: APPSC G2, G1, AP DSC, AP TET English mediums are in initialAppsList
        // =========================================================================
        createApp("appsc_group2_te", "APPSC Group-2 (Telugu)", "APPSC-G2 (TE)", "ANDHRA PRADESH", "APPSC Group 2", "Telugu"),
        createApp("appsc_group2_ur", "APPSC Group-2 (Urdu)", "APPSC-G2 (UR)", "ANDHRA PRADESH", "APPSC Group 2", "Urdu"),
        createApp("appsc_group1_te", "APPSC Group-1 (Telugu)", "APPSC-G1 (TE)", "ANDHRA PRADESH", "APPSC Group 1", "Telugu"),
        createApp("appsc_group1_ur", "APPSC Group-1 (Urdu)", "APPSC-G1 (UR)", "ANDHRA PRADESH", "APPSC Group 1", "Urdu"),
        createApp("ap_dsc_te", "AP DSC (Telugu)", "AP-DSC (TE)", "ANDHRA PRADESH", "AP DSC Teacher", "Telugu"),
        createApp("ap_dsc_ur", "AP DSC (Urdu)", "AP-DSC (UR)", "ANDHRA PRADESH", "AP DSC Teacher", "Urdu"),
        createApp("ap_tet_te", "AP TET (Telugu)", "AP-TET (TE)", "ANDHRA PRADESH", "AP TET Eligibility", "Telugu"),
        createApp("ap_tet_ur", "AP TET (Urdu)", "AP-TET (UR)", "ANDHRA PRADESH", "AP TET Eligibility", "Urdu"),
        createApp("appsc_group3_te", "APPSC Group-3 (Telugu)", "APPSC-G3 (TE)", "ANDHRA PRADESH", "APPSC Group 3", "Telugu"),
        createApp("appsc_group3_en", "APPSC Group-3 (English)", "APPSC-G3 (EN)", "ANDHRA PRADESH", "APPSC Group 3", "English"),
        createApp("appsc_group4_te", "APPSC Group-4 (Telugu)", "APPSC-G4 (TE)", "ANDHRA PRADESH", "APPSC Group 4", "Telugu"),
        createApp("appsc_group4_en", "APPSC Group-4 (English)", "APPSC-G4 (EN)", "ANDHRA PRADESH", "APPSC Group 4", "English"),
        createApp("appsc_group4_ur", "APPSC Group-4 (Urdu)", "APPSC-G4 (UR)", "ANDHRA PRADESH", "APPSC Group 4", "Urdu"),
        createApp("ap_police_constable_te", "AP Police Constable (Telugu)", "AP-PC (TE)", "ANDHRA PRADESH", "AP Police Constable", "Telugu"),
        createApp("ap_police_constable_en", "AP Police Constable (English)", "AP-PC (EN)", "ANDHRA PRADESH", "AP Police Constable", "English"),
        createApp("ap_police_constable_ur", "AP Police Constable (Urdu)", "AP-PC (UR)", "ANDHRA PRADESH", "AP Police Constable", "Urdu"),
        createApp("ap_police_si_te", "AP Police SI (Telugu)", "AP-SI (TE)", "ANDHRA PRADESH", "AP Police SI", "Telugu"),
        createApp("ap_police_si_en", "AP Police SI (English)", "AP-SI (EN)", "ANDHRA PRADESH", "AP Police SI", "English"),
        createApp("ap_police_si_ur", "AP Police SI (Urdu)", "AP-SI (UR)", "ANDHRA PRADESH", "AP Police SI", "Urdu"),
        createApp("ap_sachivalayam_te", "AP Grama/Ward Sachivalayam (Telugu)", "AP-GS (TE)", "ANDHRA PRADESH", "AP Grama/Ward Sachivalayam", "Telugu"),
        createApp("ap_sachivalayam_en", "AP Grama/Ward Sachivalayam (English)", "AP-GS (EN)", "ANDHRA PRADESH", "AP Grama/Ward Sachivalayam", "English"),
        createApp("ap_panchayat_secretary_te", "AP Panchayat Secretary (Telugu)", "AP-PS (TE)", "ANDHRA PRADESH", "AP Panchayat Secretary", "Telugu"),
        createApp("ap_panchayat_secretary_en", "AP Panchayat Secretary (English)", "AP-PS (EN)", "ANDHRA PRADESH", "AP Panchayat Secretary", "English"),
        createApp("ap_forest_te", "AP Forest Beat Officer (Telugu)", "AP-FBO (TE)", "ANDHRA PRADESH", "AP Forest Beat Officer", "Telugu"),
        createApp("ap_forest_en", "AP Forest Beat Officer (English)", "AP-FBO (EN)", "ANDHRA PRADESH", "AP Forest Beat Officer", "English"),

        // =========================================================================
        // 2. TELANGANA (Mother Language: Telugu, Official 2nd: Urdu)
        // =========================================================================
        createApp("tgpsc_group1_te", "TGPSC Group-1 (Telugu)", "TGPSC-G1 (TE)", "TELANGANA", "TGPSC Group 1", "Telugu"),
        createApp("tgpsc_group1_en", "TGPSC Group-1 (English)", "TGPSC-G1 (EN)", "TELANGANA", "TGPSC Group 1", "English"),
        createApp("tgpsc_group1_ur", "TGPSC Group-1 (Urdu)", "TGPSC-G1 (UR)", "TELANGANA", "TGPSC Group 1", "Urdu"),
        createApp("tgpsc_group2_te", "TGPSC Group-2 (Telugu)", "TGPSC-G2 (TE)", "TELANGANA", "TGPSC Group 2", "Telugu"),
        createApp("tgpsc_group2_en", "TGPSC Group-2 (English)", "TGPSC-G2 (EN)", "TELANGANA", "TGPSC Group 2", "English"),
        createApp("tgpsc_group2_ur", "TGPSC Group-2 (Urdu)", "TGPSC-G2 (UR)", "TELANGANA", "TGPSC Group 2", "Urdu"),
        createApp("tgpsc_group3_te", "TGPSC Group-3 (Telugu)", "TGPSC-G3 (TE)", "TELANGANA", "TGPSC Group 3", "Telugu"),
        createApp("tgpsc_group3_en", "TGPSC Group-3 (English)", "TGPSC-G3 (EN)", "TELANGANA", "TGPSC Group 3", "English"),
        createApp("tgpsc_group3_ur", "TGPSC Group-3 (Urdu)", "TGPSC-G3 (UR)", "TELANGANA", "TGPSC Group 3", "Urdu"),
        createApp("tgpsc_group4_te", "TGPSC Group-4 (Telugu)", "TGPSC-G4 (TE)", "TELANGANA", "TGPSC Group 4", "Telugu"),
        createApp("tgpsc_group4_en", "TGPSC Group-4 (English)", "TGPSC-G4 (EN)", "TELANGANA", "TGPSC Group 4", "English"),
        createApp("tgpsc_group4_ur", "TGPSC Group-4 (Urdu)", "TGPSC-G4 (UR)", "TELANGANA", "TGPSC Group 4", "Urdu"),
        createApp("tg_tet_te", "TG TET (Telugu)", "TG-TET (TE)", "TELANGANA", "TG TET", "Telugu"),
        createApp("tg_tet_en", "TG TET (English)", "TG-TET (EN)", "TELANGANA", "TG TET", "English"),
        createApp("tg_tet_ur", "TG TET (Urdu)", "TG-TET (UR)", "TELANGANA", "TG TET", "Urdu"),
        createApp("tg_dsc_te", "TG DSC (Telugu)", "TG-DSC (TE)", "TELANGANA", "TG DSC", "Telugu"),
        createApp("tg_dsc_en", "TG DSC (English)", "TG-DSC (EN)", "TELANGANA", "TG DSC", "English"),
        createApp("tg_dsc_ur", "TG DSC (Urdu)", "TG-DSC (UR)", "TELANGANA", "TG DSC", "Urdu"),
        createApp("tg_police_constable_te", "TG Police Constable (Telugu)", "TG-PC (TE)", "TELANGANA", "TG Police Constable", "Telugu"),
        createApp("tg_police_constable_en", "TG Police Constable (English)", "TG-PC (EN)", "TELANGANA", "TG Police Constable", "English"),
        createApp("tg_police_constable_ur", "TG Police Constable (Urdu)", "TG-PC (UR)", "TELANGANA", "TG Police Constable", "Urdu"),
        createApp("tg_police_si_te", "TG Police SI (Telugu)", "TG-SI (TE)", "TELANGANA", "TG Police SI", "Telugu"),
        createApp("tg_police_si_en", "TG Police SI (English)", "TG-SI (EN)", "TELANGANA", "TG Police SI", "English"),
        createApp("tg_police_si_ur", "TG Police SI (Urdu)", "TG-SI (UR)", "TELANGANA", "TG Police SI", "Urdu"),
        createApp("tg_gurukulam_te", "TG Gurukulam TREIRB (Telugu)", "TG-TREIRB (TE)", "TELANGANA", "TG Gurukulam", "Telugu"),
        createApp("tg_gurukulam_en", "TG Gurukulam TREIRB (English)", "TG-TREIRB (EN)", "TELANGANA", "TG Gurukulam", "English"),
        createApp("tg_gurukulam_ur", "TG Gurukulam TREIRB (Urdu)", "TG-TREIRB (UR)", "TELANGANA", "TG Gurukulam", "Urdu"),
        createApp("tg_forest_te", "TG Forest Beat Officer (Telugu)", "TG-FBO (TE)", "TELANGANA", "TG Forest Beat Officer", "Telugu"),
        createApp("tg_forest_en", "TG Forest Beat Officer (English)", "TG-FBO (EN)", "TELANGANA", "TG Forest Beat Officer", "English"),

        // =========================================================================
        // 3. TAMIL NADU (Mother Language: Tamil)
        // =========================================================================
        createApp("tnpsc_group1_ta", "TNPSC Group-1 (Tamil)", "TNPSC-G1 (TA)", "TAMIL NADU", "TNPSC Group 1", "Tamil"),
        createApp("tnpsc_group1_en", "TNPSC Group-1 (English)", "TNPSC-G1 (EN)", "TAMIL NADU", "TNPSC Group 1", "English"),
        createApp("tnpsc_group2_ta", "TNPSC Group-2 (Tamil)", "TNPSC-G2 (TA)", "TAMIL NADU", "TNPSC Group 2", "Tamil"),
        createApp("tnpsc_group2_en", "TNPSC Group-2 (English)", "TNPSC-G2 (EN)", "TAMIL NADU", "TNPSC Group 2", "English"),
        createApp("tnpsc_group4_ta", "TNPSC Group-4 & VAO (Tamil)", "TNPSC-G4 (TA)", "TAMIL NADU", "TNPSC Group 4", "Tamil"),
        createApp("tnpsc_group4_en", "TNPSC Group-4 & VAO (English)", "TNPSC-G4 (EN)", "TAMIL NADU", "TNPSC Group 4", "English"),
        createApp("tn_trb_ta", "TN TRB Assistant (Tamil)", "TN-TRB (TA)", "TAMIL NADU", "TN TRB Assistant", "Tamil"),
        createApp("tn_trb_en", "TN TRB Assistant (English)", "TN-TRB (EN)", "TAMIL NADU", "TN TRB Assistant", "English"),
        createApp("tntet_ta", "TNTET (Tamil)", "TNTET (TA)", "TAMIL NADU", "TNTET", "Tamil"),
        createApp("tntet_en", "TNTET (English)", "TNTET (EN)", "TAMIL NADU", "TNTET", "English"),
        createApp("tn_police_constable_ta", "TN Police Constable (Tamil)", "TN-PC (TA)", "TAMIL NADU", "TN Police Constable", "Tamil"),
        createApp("tn_police_constable_en", "TN Police Constable (English)", "TN-PC (EN)", "TAMIL NADU", "TN Police Constable", "English"),
        createApp("tn_police_si_ta", "TN Police SI (Tamil)", "TN-SI (TA)", "TAMIL NADU", "TN Police SI", "Tamil"),
        createApp("tn_police_si_en", "TN Police SI (English)", "TN-SI (EN)", "TAMIL NADU", "TN Police SI", "English"),
        createApp("tn_forest_ta", "TN Forest Guard (Tamil)", "TN-FG (TA)", "TAMIL NADU", "TN Forest Guard", "Tamil"),
        createApp("tn_forest_en", "TN Forest Guard (English)", "TN-FG (EN)", "TAMIL NADU", "TN Forest Guard", "English"),

        // =========================================================================
        // 4. KARNATAKA (Mother Language: Kannada)
        // =========================================================================
        createApp("kpsc_kas_kn", "KPSC KAS (Kannada)", "KPSC-KAS (KN)", "KARNATAKA", "KPSC KAS", "Kannada"),
        createApp("kpsc_kas_en", "KPSC KAS (English)", "KPSC-KAS (EN)", "KARNATAKA", "KPSC KAS", "English"),
        createApp("kpsc_group_c_kn", "KPSC Group C (Kannada)", "KPSC-GC (KN)", "KARNATAKA", "KPSC Group C", "Kannada"),
        createApp("kpsc_group_c_en", "KPSC Group C (English)", "KPSC-GC (EN)", "KARNATAKA", "KPSC Group C", "English"),
        createApp("kartet_kn", "KARTET (Kannada)", "KARTET (KN)", "KARNATAKA", "KARTET", "Kannada"),
        createApp("kartet_en", "KARTET (English)", "KARTET (EN)", "KARNATAKA", "KARTET", "English"),
        createApp("kartet_ur", "KARTET (Urdu)", "KARTET (UR)", "KARNATAKA", "KARTET", "Urdu"),
        createApp("kar_gpstr_kn", "Karnataka GPSTR (Kannada)", "GPSTR (KN)", "KARNATAKA", "Karnataka GPSTR", "Kannada"),
        createApp("kar_gpstr_en", "Karnataka GPSTR (English)", "GPSTR (EN)", "KARNATAKA", "Karnataka GPSTR", "English"),
        createApp("kar_gpstr_ur", "Karnataka GPSTR (Urdu)", "GPSTR (UR)", "KARNATAKA", "Karnataka GPSTR", "Urdu"),
        createApp("kar_police_constable_kn", "Karnataka Police Constable (Kannada)", "KAR-PC (KN)", "KARNATAKA", "Karnataka Police Constable", "Kannada"),
        createApp("kar_police_constable_en", "Karnataka Police Constable (English)", "KAR-PC (EN)", "KARNATAKA", "Karnataka Police Constable", "English"),
        createApp("kar_police_constable_ur", "Karnataka Police Constable (Urdu)", "KAR-PC (UR)", "KARNATAKA", "Karnataka Police Constable", "Urdu"),
        createApp("kar_police_psi_kn", "Karnataka Police PSI (Kannada)", "KAR-PSI (KN)", "KARNATAKA", "Karnataka Police PSI", "Kannada"),
        createApp("kar_police_psi_en", "Karnataka Police PSI (English)", "KAR-PSI (EN)", "KARNATAKA", "Karnataka Police PSI", "English"),
        createApp("kea_fda_sda_kn", "KEA FDA/SDA (Kannada)", "KEA-ASST (KN)", "KARNATAKA", "KEA FDA/SDA", "Kannada"),
        createApp("kea_fda_sda_en", "KEA FDA/SDA (English)", "KEA-ASST (EN)", "KARNATAKA", "KEA FDA/SDA", "English"),
        createApp("kar_forest_kn", "Karnataka Forest Guard (Kannada)", "KAR-FG (KN)", "KARNATAKA", "Karnataka Forest Guard", "Kannada"),
        createApp("kar_forest_en", "Karnataka Forest Guard (English)", "KAR-FG (EN)", "KARNATAKA", "Karnataka Forest Guard", "English"),

        // =========================================================================
        // 5. KERALA (Mother Language: Malayalam)
        // =========================================================================
        createApp("kpsc_kas_ml", "Kerala PSC KAS (Malayalam)", "KL-KAS (ML)", "KERALA", "Kerala PSC KAS", "Malayalam"),
        createApp("kpsc_kas_en_kl", "Kerala PSC KAS (English)", "KL-KAS (EN)", "KERALA", "Kerala PSC KAS", "English"),
        createApp("kpsc_degree_ml", "Kerala PSC Degree Level (Malayalam)", "KL-DEG (ML)", "KERALA", "Kerala PSC Degree Level", "Malayalam"),
        createApp("kpsc_degree_en", "Kerala PSC Degree Level (English)", "KL-DEG (EN)", "KERALA", "Kerala PSC Degree Level", "English"),
        createApp("kpsc_tenth_ml", "Kerala PSC 10th Level (Malayalam)", "KL-10TH (ML)", "KERALA", "Kerala PSC 10th Level", "Malayalam"),
        createApp("kpsc_tenth_en", "Kerala PSC 10th Level (English)", "KL-10TH (EN)", "KERALA", "Kerala PSC 10th Level", "English"),
        createApp("kpsc_police_ml", "Kerala Police Constable (Malayalam)", "KL-PC (ML)", "KERALA", "Kerala Police Constable", "Malayalam"),
        createApp("kpsc_police_en", "Kerala Police Constable (English)", "KL-PC (EN)", "KERALA", "Kerala Police Constable", "English"),
        createApp("kpsc_si_ml", "Kerala Police SI (Malayalam)", "KL-SI (ML)", "KERALA", "Kerala Police SI", "Malayalam"),
        createApp("kpsc_si_en", "Kerala Police SI (English)", "KL-SI (EN)", "KERALA", "Kerala Police SI", "English"),
        createApp("ktet_ml", "KTET (Malayalam)", "KTET (ML)", "KERALA", "KTET", "Malayalam"),
        createApp("ktet_en", "KTET (English)", "KTET (EN)", "KERALA", "KTET", "English"),
        createApp("kpsc_lp_up_ml", "Kerala PSC LP/UP Teacher (Malayalam)", "KL-LPUP (ML)", "KERALA", "Kerala PSC LP/UP Teacher", "Malayalam"),
        createApp("kpsc_lp_up_en", "Kerala PSC LP/UP Teacher (English)", "KL-LPUP (EN)", "KERALA", "Kerala PSC LP/UP Teacher", "English"),

        // =========================================================================
        // 6. MAHARASHTRA (Mother Language: Marathi)
        // =========================================================================
        createApp("mpsc_rajyaseva_mr", "MPSC Rajyaseva (Marathi)", "MPSC-RS (MR)", "MAHARASHTRA", "MPSC Rajyaseva", "Marathi"),
        createApp("mpsc_rajyaseva_en", "MPSC Rajyaseva (English)", "MPSC-RS (EN)", "MAHARASHTRA", "MPSC Rajyaseva", "English"),
        createApp("mpsc_combine_mr", "MPSC Combine Gr B & C (Marathi)", "MPSC-CMB (MR)", "MAHARASHTRA", "MPSC Combine", "Marathi"),
        createApp("mpsc_combine_en", "MPSC Combine Gr B & C (English)", "MPSC-CMB (EN)", "MAHARASHTRA", "MPSC Combine", "English"),
        createApp("maha_tet_mr", "MAHA TET (Marathi)", "MAHA-TET (MR)", "MAHARASHTRA", "MAHA TET", "Marathi"),
        createApp("maha_tet_en", "MAHA TET (English)", "MAHA-TET (EN)", "MAHARASHTRA", "MAHA TET", "English"),
        createApp("maha_tet_ur", "MAHA TET (Urdu)", "MAHA-TET (UR)", "MAHARASHTRA", "MAHA TET", "Urdu"),
        createApp("maha_police_mr", "Maharashtra Police Constable (Marathi)", "MH-PC (MR)", "MAHARASHTRA", "Maharashtra Police Constable", "Marathi"),
        createApp("maha_police_en", "Maharashtra Police Constable (English)", "MH-PC (EN)", "MAHARASHTRA", "Maharashtra Police Constable", "English"),
        createApp("maha_police_ur", "Maharashtra Police Constable (Urdu)", "MH-PC (UR)", "MAHARASHTRA", "Maharashtra Police Constable", "Urdu"),
        createApp("maha_talathi_mr", "Maharashtra Talathi (Marathi)", "MH-TAL (MR)", "MAHARASHTRA", "Maharashtra Talathi", "Marathi"),
        createApp("maha_talathi_en", "Maharashtra Talathi (English)", "MH-TAL (EN)", "MAHARASHTRA", "Maharashtra Talathi", "English"),

        // =========================================================================
        // 7. GUJARAT (Mother Language: Gujarati)
        // =========================================================================
        createApp("gpsc_class1_2_gu", "GPSC Class 1 & 2 (Gujarati)", "GPSC-12 (GU)", "GUJARAT", "GPSC Class 1 & 2", "Gujarati"),
        createApp("gpsc_class1_2_en", "GPSC Class 1 & 2 (English)", "GPSC-12 (EN)", "GUJARAT", "GPSC Class 1 & 2", "English"),
        createApp("gsssb_clerk_gu", "GSSSB Clerk (Gujarati)", "GSSSB (GU)", "GUJARAT", "GSSSB Clerk", "Gujarati"),
        createApp("gsssb_clerk_en", "GSSSB Clerk (English)", "GSSSB (EN)", "GUJARAT", "GSSSB Clerk", "English"),
        createApp("gujarat_police_gu", "Gujarat Police Constable (Gujarati)", "GJ-PC (GU)", "GUJARAT", "Gujarat Police Constable", "Gujarati"),
        createApp("gujarat_police_en", "Gujarat Police Constable (English)", "GJ-PC (EN)", "GUJARAT", "Gujarat Police Constable", "English"),
        createApp("gujarat_psi_gu", "Gujarat Police PSI (Gujarati)", "GJ-PSI (GU)", "GUJARAT", "Gujarat Police PSI", "Gujarati"),
        createApp("gujarat_psi_en", "Gujarat Police PSI (English)", "GJ-PSI (EN)", "GUJARAT", "Gujarat Police PSI", "English"),
        createApp("gujarat_tet_gu", "Gujarat TET/TAT (Gujarati)", "GJ-TET (GU)", "GUJARAT", "Gujarat TET", "Gujarati"),
        createApp("gujarat_tet_en", "Gujarat TET/TAT (English)", "GJ-TET (EN)", "GUJARAT", "Gujarat TET", "English"),

        // =========================================================================
        // 8. WEST BENGAL (Mother Language: Bengali)
        // =========================================================================
        createApp("wbcs_bn", "WBPSC WBCS (Bengali)", "WBCS (BN)", "WEST BENGAL", "WBPSC WBCS", "Bengali"),
        createApp("wbcs_en", "WBPSC WBCS (English)", "WBCS (EN)", "WEST BENGAL", "WBPSC WBCS", "English"),
        createApp("wb_tet_bn", "WB Primary TET (Bengali)", "WB-TET (BN)", "WEST BENGAL", "WB Primary TET", "Bengali"),
        createApp("wb_tet_en", "WB Primary TET (English)", "WB-TET (EN)", "WEST BENGAL", "WB Primary TET", "English"),
        createApp("wb_tet_ur", "WB Primary TET (Urdu)", "WB-TET (UR)", "WEST BENGAL", "WB Primary TET", "Urdu"),
        createApp("wb_police_bn", "WB Police Constable (Bengali)", "WB-PC (BN)", "WEST BENGAL", "WB Police Constable", "Bengali"),
        createApp("wb_police_en", "WB Police Constable (English)", "WB-PC (EN)", "WEST BENGAL", "WB Police Constable", "English"),
        createApp("wb_police_si_bn", "WB Police SI (Bengali)", "WB-SI (BN)", "WEST BENGAL", "WB Police SI", "Bengali"),
        createApp("wb_police_si_en", "WB Police SI (English)", "WB-SI (EN)", "WEST BENGAL", "WB Police SI", "English"),
        createApp("wbpsc_clerkship_bn", "WBPSC Clerkship (Bengali)", "WB-CLK (BN)", "WEST BENGAL", "WBPSC Clerkship", "Bengali"),
        createApp("wbpsc_clerkship_en", "WBPSC Clerkship (English)", "WB-CLK (EN)", "WEST BENGAL", "WBPSC Clerkship", "English"),

        // =========================================================================
        // 9. ODISHA (Mother Language: Odia)
        // =========================================================================
        createApp("opsc_oas_or", "OPSC OAS (Odia)", "OPSC-OAS (OR)", "ODISHA", "OPSC OAS", "Odia"),
        createApp("opsc_oas_en", "OPSC OAS (English)", "OPSC-OAS (EN)", "ODISHA", "OPSC OAS", "English"),
        createApp("ossc_cgl_or", "OSSC CGL (Odia)", "OSSC-CGL (OR)", "ODISHA", "OSSC CGL", "Odia"),
        createApp("ossc_cgl_en", "OSSC CGL (English)", "OSSC-CGL (EN)", "ODISHA", "OSSC CGL", "English"),
        createApp("odisha_police_or", "Odisha Police Constable (Odia)", "OD-PC (OR)", "ODISHA", "Odisha Police Constable", "Odia"),
        createApp("odisha_police_en", "Odisha Police Constable (English)", "OD-PC (EN)", "ODISHA", "Odisha Police Constable", "English"),
        createApp("otet_or", "OTET (Odia)", "OTET (OR)", "ODISHA", "OTET", "Odia"),
        createApp("otet_en", "OTET (English)", "OTET (EN)", "ODISHA", "OTET", "English"),

        // =========================================================================
        // 10. PUNJAB (Mother Language: Punjabi)
        // =========================================================================
        createApp("ppsc_pcs_pa", "PPSC PCS (Punjabi)", "PPSC-PCS (PA)", "PUNJAB", "PPSC PCS", "Punjabi"),
        createApp("ppsc_pcs_en", "PPSC PCS (English)", "PPSC-PCS (EN)", "PUNJAB", "PPSC PCS", "English"),
        createApp("punjab_police_pa", "Punjab Police Constable (Punjabi)", "PB-PC (PA)", "PUNJAB", "Punjab Police Constable", "Punjabi"),
        createApp("punjab_police_en", "Punjab Police Constable (English)", "PB-PC (EN)", "PUNJAB", "Punjab Police Constable", "English"),
        createApp("pstet_pa", "PSTET (Punjabi)", "PSTET (PA)", "PUNJAB", "PSTET", "Punjabi"),
        createApp("pstet_en", "PSTET (English)", "PSTET (EN)", "PUNJAB", "PSTET", "English"),
        createApp("psssb_clerk_pa", "PSSSB Clerk (Punjabi)", "PSSSB (PA)", "PUNJAB", "PSSSB Clerk", "Punjabi"),
        createApp("psssb_clerk_en", "PSSSB Clerk (English)", "PSSSB (EN)", "PUNJAB", "PSSSB Clerk", "English"),

        // =========================================================================
        // 11. BIHAR (State Language: Hindi, 2nd: Urdu)
        // =========================================================================
        createApp("bpsc_cce_hi", "BPSC CCE (Hindi)", "BPSC-CCE (HI)", "BIHAR", "BPSC CCE", "Hindi"),
        createApp("bpsc_cce_en", "BPSC CCE (English)", "BPSC-CCE (EN)", "BIHAR", "BPSC CCE", "English"),
        createApp("bpsc_cce_ur", "BPSC CCE (Urdu)", "BPSC-CCE (UR)", "BIHAR", "BPSC CCE", "Urdu"),
        createApp("bihar_tre_hi", "Bihar Teacher TRE (Hindi)", "BPSC-TRE (HI)", "BIHAR", "Bihar Teacher TRE", "Hindi"),
        createApp("bihar_tre_en", "Bihar Teacher TRE (English)", "BPSC-TRE (EN)", "BIHAR", "Bihar Teacher TRE", "English"),
        createApp("bihar_tre_ur", "Bihar Teacher TRE (Urdu)", "BPSC-TRE (UR)", "BIHAR", "Bihar Teacher TRE", "Urdu"),
        createApp("bihar_stet_hi", "Bihar STET (Hindi)", "BIHAR-STET (HI)", "BIHAR", "Bihar STET", "Hindi"),
        createApp("bihar_stet_en", "Bihar STET (English)", "BIHAR-STET (EN)", "BIHAR", "Bihar STET", "English"),
        createApp("bihar_stet_ur", "Bihar STET (Urdu)", "BIHAR-STET (UR)", "BIHAR", "Bihar STET", "Urdu"),
        createApp("bihar_police_hi", "Bihar Police Constable (Hindi)", "BH-PC (HI)", "BIHAR", "Bihar Police Constable", "Hindi"),
        createApp("bihar_police_en", "Bihar Police Constable (English)", "BH-PC (EN)", "BIHAR", "Bihar Police Constable", "English"),
        createApp("bihar_police_si_hi", "Bihar Police SI (Hindi)", "BH-SI (HI)", "BIHAR", "Bihar Police SI", "Hindi"),
        createApp("bihar_police_si_en", "Bihar Police SI (English)", "BH-SI (EN)", "BIHAR", "Bihar Police SI", "English"),
        createApp("bssc_cgl_hi", "BSSC CGL (Hindi)", "BSSC-CGL (HI)", "BIHAR", "BSSC CGL", "Hindi"),
        createApp("bssc_cgl_en", "BSSC CGL (English)", "BSSC-CGL (EN)", "BIHAR", "BSSC CGL", "English"),

        // =========================================================================
        // 12. UTTAR PRADESH (State Language: Hindi)
        // =========================================================================
        createApp("uppsc_pcs_hi", "UPPSC PCS (Hindi)", "UPPSC-PCS (HI)", "UTTAR PRADESH", "UPPSC PCS", "Hindi"),
        createApp("uppsc_pcs_en", "UPPSC PCS (English)", "UPPSC-PCS (EN)", "UTTAR PRADESH", "UPPSC PCS", "English"),
        createApp("upsssc_pet_hi", "UPSSSC PET (Hindi)", "UP-PET (HI)", "UTTAR PRADESH", "UPSSSC PET", "Hindi"),
        createApp("upsssc_pet_en", "UPSSSC PET (English)", "UP-PET (EN)", "UTTAR PRADESH", "UPSSSC PET", "English"),
        createApp("up_police_constable_hi", "UP Police Constable (Hindi)", "UP-PC (HI)", "UTTAR PRADESH", "UP Police Constable", "Hindi"),
        createApp("up_police_constable_en", "UP Police Constable (English)", "UP-PC (EN)", "UTTAR PRADESH", "UP Police Constable", "English"),
        createApp("up_police_si_hi", "UP Police SI (Hindi)", "UP-SI (HI)", "UTTAR PRADESH", "UP Police SI", "Hindi"),
        createApp("up_police_si_en", "UP Police SI (English)", "UP-SI (EN)", "UTTAR PRADESH", "UP Police SI", "English"),
        createApp("uptet_hi", "UPTET (Hindi)", "UPTET (HI)", "UTTAR PRADESH", "UPTET", "Hindi"),
        createApp("uptet_en", "UPTET (English)", "UPTET (EN)", "UTTAR PRADESH", "UPTET", "English"),
        createApp("uptet_ur", "UPTET (Urdu)", "UPTET (UR)", "UTTAR PRADESH", "UPTET", "Urdu"),
        createApp("upsssc_lekhpal_hi", "UPSSSC Lekhpal (Hindi)", "UP-LEKH (HI)", "UTTAR PRADESH", "UPSSSC Lekhpal", "Hindi"),
        createApp("upsssc_lekhpal_en", "UPSSSC Lekhpal (English)", "UP-LEKH (EN)", "UTTAR PRADESH", "UPSSSC Lekhpal", "English"),

        // =========================================================================
        // 13. MADHYA PRADESH (State Language: Hindi)
        // =========================================================================
        createApp("mppsc_hi", "MPPSC State Services (Hindi)", "MPPSC (HI)", "MADHYA PRADESH", "MPPSC State Services", "Hindi"),
        createApp("mppsc_en", "MPPSC State Services (English)", "MPPSC (EN)", "MADHYA PRADESH", "MPPSC State Services", "English"),
        createApp("mp_police_hi", "MP Police Constable (Hindi)", "MP-PC (HI)", "MADHYA PRADESH", "MP Police Constable", "Hindi"),
        createApp("mp_police_en", "MP Police Constable (English)", "MP-PC (EN)", "MADHYA PRADESH", "MP Police Constable", "English"),
        createApp("mptet_hi", "MPTET (Hindi)", "MPTET (HI)", "MADHYA PRADESH", "MPTET", "Hindi"),
        createApp("mptet_en", "MPTET (English)", "MPTET (EN)", "MADHYA PRADESH", "MPTET", "English"),
        createApp("mp_patwari_hi", "MP Patwari (Hindi)", "MP-PAT (HI)", "MADHYA PRADESH", "MP Patwari", "Hindi"),
        createApp("mp_patwari_en", "MP Patwari (English)", "MP-PAT (EN)", "MADHYA PRADESH", "MP Patwari", "English"),

        // =========================================================================
        // 14. RAJASTHAN (State Language: Hindi)
        // =========================================================================
        createApp("rpsc_ras_hi", "RPSC RAS (Hindi)", "RPSC-RAS (HI)", "RAJASTHAN", "RPSC RAS", "Hindi"),
        createApp("rpsc_ras_en", "RPSC RAS (English)", "RPSC-RAS (EN)", "RAJASTHAN", "RPSC RAS", "English"),
        createApp("reet_hi", "REET (Hindi)", "REET (HI)", "RAJASTHAN", "REET", "Hindi"),
        createApp("reet_en", "REET (English)", "REET (EN)", "RAJASTHAN", "REET", "English"),
        createApp("reet_ur", "REET (Urdu)", "REET (UR)", "RAJASTHAN", "REET", "Urdu"),
        createApp("rajasthan_police_hi", "Rajasthan Police Constable (Hindi)", "RJ-PC (HI)", "RAJASTHAN", "Rajasthan Police Constable", "Hindi"),
        createApp("rajasthan_police_en", "Rajasthan Police Constable (English)", "RJ-PC (EN)", "RAJASTHAN", "Rajasthan Police Constable", "English"),
        createApp("rsmssb_cet_hi", "RSMSSB CET (Hindi)", "RSMSSB (HI)", "RAJASTHAN", "RSMSSB CET", "Hindi"),
        createApp("rsmssb_cet_en", "RSMSSB CET (English)", "RSMSSB (EN)", "RAJASTHAN", "RSMSSB CET", "English"),

        // =========================================================================
        // 15. HARYANA (State Language: Hindi)
        // =========================================================================
        createApp("hpsc_hcs_hi", "HPSC HCS (Hindi)", "HPSC-HCS (HI)", "HARYANA", "HPSC HCS", "Hindi"),
        createApp("hpsc_hcs_en", "HPSC HCS (English)", "HPSC-HCS (EN)", "HARYANA", "HPSC HCS", "English"),
        createApp("hssc_cet_hi", "HSSC CET (Hindi)", "HSSC-CET (HI)", "HARYANA", "HSSC CET", "Hindi"),
        createApp("hssc_cet_en", "HSSC CET (English)", "HSSC-CET (EN)", "HARYANA", "HSSC CET", "English"),
        createApp("haryana_police_hi", "Haryana Police Constable (Hindi)", "HR-PC (HI)", "HARYANA", "Haryana Police Constable", "Hindi"),
        createApp("haryana_police_en", "Haryana Police Constable (English)", "HR-PC (EN)", "HARYANA", "Haryana Police Constable", "English"),
        createApp("htet_hi", "HTET (Hindi)", "HTET (HI)", "HARYANA", "HTET", "Hindi"),
        createApp("htet_en", "HTET (English)", "HTET (EN)", "HARYANA", "HTET", "English"),

        // =========================================================================
        // 16. CHHATTISGARH (State Language: Hindi)
        // =========================================================================
        createApp("cgpsc_hi", "CGPSC State Services (Hindi)", "CGPSC (HI)", "CHHATTISGARH", "CGPSC State Services", "Hindi"),
        createApp("cgpsc_en", "CGPSC State Services (English)", "CGPSC (EN)", "CHHATTISGARH", "CGPSC State Services", "English"),
        createApp("cg_police_hi", "CG Police Constable (Hindi)", "CG-PC (HI)", "CHHATTISGARH", "CG Police Constable", "Hindi"),
        createApp("cg_police_en", "CG Police Constable (English)", "CG-PC (EN)", "CHHATTISGARH", "CG Police Constable", "English"),
        createApp("cg_tet_hi", "CG TET (Hindi)", "CG-TET (HI)", "CHHATTISGARH", "CG TET", "Hindi"),
        createApp("cg_tet_en", "CG TET (English)", "CG-TET (EN)", "CHHATTISGARH", "CG TET", "English"),

        // =========================================================================
        // 17. JHARKHAND (State Language: Hindi)
        // =========================================================================
        createApp("jpsc_hi", "JPSC Civil Services (Hindi)", "JPSC (HI)", "JHARKHAND", "JPSC Civil Services", "Hindi"),
        createApp("jpsc_en", "JPSC Civil Services (English)", "JPSC (EN)", "JHARKHAND", "JPSC Civil Services", "English"),
        createApp("jssc_cgl_hi", "JSSC CGL (Hindi)", "JSSC-CGL (HI)", "JHARKHAND", "JSSC CGL", "Hindi"),
        createApp("jssc_cgl_en", "JSSC CGL (English)", "JSSC-CGL (EN)", "JHARKHAND", "JSSC CGL", "English"),
        createApp("jharkhand_police_hi", "Jharkhand Police Constable (Hindi)", "JH-PC (HI)", "JHARKHAND", "Jharkhand Police Constable", "Hindi"),
        createApp("jharkhand_police_en", "Jharkhand Police Constable (English)", "JH-PC (EN)", "JHARKHAND", "Jharkhand Police Constable", "English"),
        createApp("jtet_hi", "JTET (Hindi)", "JTET (HI)", "JHARKHAND", "JTET", "Hindi"),
        createApp("jtet_en", "JTET (English)", "JTET (EN)", "JHARKHAND", "JTET", "English"),
        createApp("jtet_ur", "JTET (Urdu)", "JTET (UR)", "JHARKHAND", "JTET", "Urdu"),

        // =========================================================================
        // 18. ASSAM (Mother Language: Assamese)
        // =========================================================================
        createApp("apsc_cce_as", "APSC CCE (Assamese)", "APSC-CCE (AS)", "ASSAM", "APSC CCE", "Assamese"),
        createApp("apsc_cce_en", "APSC CCE (English)", "APSC-CCE (EN)", "ASSAM", "APSC CCE", "English"),
        createApp("assam_police_as", "Assam Police Constable (Assamese)", "AS-PC (AS)", "ASSAM", "Assam Police Constable", "Assamese"),
        createApp("assam_police_en", "Assam Police Constable (English)", "AS-PC (EN)", "ASSAM", "Assam Police Constable", "English"),
        createApp("adre_as", "ADRE Assam (Assamese)", "ADRE (AS)", "ASSAM", "ADRE Assam", "Assamese"),
        createApp("adre_en", "ADRE Assam (English)", "ADRE (EN)", "ASSAM", "ADRE Assam", "English"),
        createApp("assam_tet_as", "Assam TET (Assamese)", "AS-TET (AS)", "ASSAM", "Assam TET", "Assamese"),
        createApp("assam_tet_en", "Assam TET (English)", "AS-TET (EN)", "ASSAM", "Assam TET", "English"),

        // =========================================================================
        // 19. HIMACHAL PRADESH (State Language: Hindi)
        // =========================================================================
        createApp("hppsc_hpas_hi", "HPPSC HPAS (Hindi)", "HPAS (HI)", "HIMACHAL PRADESH", "HPPSC HPAS", "Hindi"),
        createApp("hppsc_hpas_en", "HPPSC HPAS (English)", "HPAS (EN)", "HIMACHAL PRADESH", "HPPSC HPAS", "English"),
        createApp("hp_police_hi", "HP Police Constable (Hindi)", "HP-PC (HI)", "HIMACHAL PRADESH", "HP Police Constable", "Hindi"),
        createApp("hp_police_en", "HP Police Constable (English)", "HP-PC (EN)", "HIMACHAL PRADESH", "HP Police Constable", "English"),
        createApp("hptet_hi", "HP TET (Hindi)", "HP-TET (HI)", "HIMACHAL PRADESH", "HP TET", "Hindi"),
        createApp("hptet_en", "HP TET (English)", "HP-TET (EN)", "HIMACHAL PRADESH", "HP TET", "English"),
        createApp("hp_patwari_hi", "HP Patwari (Hindi)", "HP-PAT (HI)", "HIMACHAL PRADESH", "HP Patwari", "Hindi"),
        createApp("hp_patwari_en", "HP Patwari (English)", "HP-PAT (EN)", "HIMACHAL PRADESH", "HP Patwari", "English"),

        // =========================================================================
        // 20. UTTARAKHAND (State Language: Hindi)
        // =========================================================================
        createApp("ukpsc_pcs_hi", "UKPSC PCS (Hindi)", "UKPSC (HI)", "UTTARAKHAND", "UKPSC PCS", "Hindi"),
        createApp("ukpsc_pcs_en", "UKPSC PCS (English)", "UKPSC (EN)", "UTTARAKHAND", "UKPSC PCS", "English"),
        createApp("uksssc_cgl_hi", "UKSSSC Graduate Level (Hindi)", "UKSSSC (HI)", "UTTARAKHAND", "UKSSSC Graduate Level", "Hindi"),
        createApp("uksssc_cgl_en", "UKSSSC Graduate Level (English)", "UKSSSC (EN)", "UTTARAKHAND", "UKSSSC Graduate Level", "English"),
        createApp("uk_police_hi", "Uttarakhand Police (Hindi)", "UK-PC (HI)", "UTTARAKHAND", "Uttarakhand Police", "Hindi"),
        createApp("uk_police_en", "Uttarakhand Police (English)", "UK-PC (EN)", "UTTARAKHAND", "Uttarakhand Police", "English"),
        createApp("utet_hi", "UTET Uttarakhand (Hindi)", "UTET (HI)", "UTTARAKHAND", "UTET Uttarakhand", "Hindi"),
        createApp("utet_en", "UTET Uttarakhand (English)", "UTET (EN)", "UTTARAKHAND", "UTET Uttarakhand", "English"),
        createApp("utet_ur", "UTET Uttarakhand (Urdu)", "UTET (UR)", "UTTARAKHAND", "UTET Uttarakhand", "Urdu"),

        // =========================================================================
        // 21. JAMMU & KASHMIR (Official: Urdu, English, Hindi)
        // =========================================================================
        createApp("jkpsc_cce_en", "JKPSC CCE (English)", "JKPSC (EN)", "JAMMU & KASHMIR", "JKPSC CCE", "English"),
        createApp("jkpsc_cce_ur", "JKPSC CCE (Urdu)", "JKPSC (UR)", "JAMMU & KASHMIR", "JKPSC CCE", "Urdu"),
        createApp("jkssb_graduate_en", "JKSSB Graduate Level (English)", "JKSSB (EN)", "JAMMU & KASHMIR", "JKSSB Graduate Level", "English"),
        createApp("jkssb_graduate_ur", "JKSSB Graduate Level (Urdu)", "JKSSB (UR)", "JAMMU & KASHMIR", "JKSSB Graduate Level", "Urdu"),
        createApp("jk_police_en", "J&K Police Constable (English)", "JK-PC (EN)", "JAMMU & KASHMIR", "J&K Police Constable", "English"),
        createApp("jk_police_ur", "J&K Police Constable (Urdu)", "JK-PC (UR)", "JAMMU & KASHMIR", "J&K Police Constable", "Urdu"),
        createApp("jk_police_si_en", "J&K Police SI (English)", "JK-SI (EN)", "JAMMU & KASHMIR", "J&K Police SI", "English"),
        createApp("jk_police_si_ur", "J&K Police SI (Urdu)", "JK-SI (UR)", "JAMMU & KASHMIR", "J&K Police SI", "Urdu"),

        // =========================================================================
        // 22. DELHI (DSSSB / Delhi Police)
        // =========================================================================
        createApp("dsssb_prt_tgt_hi", "DSSSB PRT/TGT (Hindi)", "DSSSB (HI)", "DELHI", "DSSSB PRT/TGT", "Hindi"),
        createApp("dsssb_prt_tgt_en", "DSSSB PRT/TGT (English)", "DSSSB (EN)", "DELHI", "DSSSB PRT/TGT", "English"),
        createApp("dsssb_prt_tgt_ur", "DSSSB PRT/TGT (Urdu)", "DSSSB (UR)", "DELHI", "DSSSB PRT/TGT", "Urdu"),
        createApp("dsssb_dass_hi", "DSSSB DASS Grade II & IV (Hindi)", "DSSSB-DASS (HI)", "DELHI", "DSSSB DASS", "Hindi"),
        createApp("dsssb_dass_en", "DSSSB DASS Grade II & IV (English)", "DSSSB-DASS (EN)", "DELHI", "DSSSB DASS", "English"),
        createApp("delhi_police_hi", "Delhi Police Constable (Hindi)", "DP-PC (HI)", "DELHI", "Delhi Police Constable", "Hindi"),
        createApp("delhi_police_en", "Delhi Police Constable (English)", "DP-PC (EN)", "DELHI", "Delhi Police Constable", "English"),

        // =========================================================================
        // 23. GOA (Official: Konkani, Marathi, English)
        // =========================================================================
        createApp("goa_psc_en", "Goa PSC Civil Services (English)", "GPSC (EN)", "GOA", "Goa PSC Civil Services", "English"),
        createApp("goa_psc_mr", "Goa PSC Civil Services (Marathi)", "GPSC (MR)", "GOA", "Goa PSC Civil Services", "Marathi"),
        createApp("goa_police_en", "Goa Police Constable (English)", "GOA-PC (EN)", "GOA", "Goa Police Constable", "English"),
        createApp("goa_police_mr", "Goa Police Constable (Marathi)", "GOA-PC (MR)", "GOA", "Goa Police Constable", "Marathi"),

        // =========================================================================
        // 24. TRIPURA (Mother Language: Bengali)
        // =========================================================================
        createApp("tpsc_tcs_bn", "TPSC TCS/TPS (Bengali)", "TPSC (BN)", "TRIPURA", "TPSC TCS/TPS", "Bengali"),
        createApp("tpsc_tcs_en", "TPSC TCS/TPS (English)", "TPSC (EN)", "TRIPURA", "TPSC TCS/TPS", "English"),
        createApp("tripura_police_bn", "Tripura Police (Bengali)", "TR-PC (BN)", "TRIPURA", "Tripura Police", "Bengali"),
        createApp("tripura_police_en", "Tripura Police (English)", "TR-PC (EN)", "TRIPURA", "Tripura Police", "English"),
        createApp("tripura_tet_bn", "Tripura TET (Bengali)", "TR-TET (BN)", "TRIPURA", "Tripura TET", "Bengali"),
        createApp("tripura_tet_en", "Tripura TET (English)", "TR-TET (EN)", "TRIPURA", "Tripura TET", "English"),

        // =========================================================================
        // 25. MANIPUR (Mother Language: Manipuri)
        // =========================================================================
        createApp("manipur_psc_mni", "Manipur PSC (Manipuri)", "MPSC (MNI)", "MANIPUR", "Manipur PSC", "Manipuri"),
        createApp("manipur_psc_en", "Manipur PSC (English)", "MPSC (EN)", "MANIPUR", "Manipur PSC", "English"),
        createApp("manipur_police_mni", "Manipur Police (Manipuri)", "MN-PC (MNI)", "MANIPUR", "Manipur Police", "Manipuri"),
        createApp("manipur_police_en", "Manipur Police (English)", "MN-PC (EN)", "MANIPUR", "Manipur Police", "English"),

        // =========================================================================
        // 26. MEGHALAYA (Official Language: English)
        // =========================================================================
        createApp("meghalaya_psc_en", "Meghalaya PSC (English)", "MG-PSC (EN)", "MEGHALAYA", "Meghalaya PSC", "English"),
        createApp("meghalaya_police_en", "Meghalaya Police (English)", "MG-PC (EN)", "MEGHALAYA", "Meghalaya Police", "English"),

        // =========================================================================
        // 27. MIZORAM (Official Language: Mizo, English)
        // =========================================================================
        createApp("mizoram_psc_lus", "Mizoram PSC (Mizo)", "MZ-PSC (MIZO)", "MIZORAM", "Mizoram PSC", "Mizo"),
        createApp("mizoram_psc_en", "Mizoram PSC (English)", "MZ-PSC (EN)", "MIZORAM", "Mizoram PSC", "English"),
        createApp("mizoram_police_lus", "Mizoram Police (Mizo)", "MZ-PC (MIZO)", "MIZORAM", "Mizoram Police", "Mizo"),
        createApp("mizoram_police_en", "Mizoram Police (English)", "MZ-PC (EN)", "MIZORAM", "Mizoram Police", "English"),

        // =========================================================================
        // 28. NAGALAND (Official Language: English)
        // =========================================================================
        createApp("nagaland_psc_en", "Nagaland PSC (English)", "NPSC (EN)", "NAGALAND", "Nagaland PSC", "English"),
        createApp("nagaland_police_en", "Nagaland Police (English)", "NL-PC (EN)", "NAGALAND", "Nagaland Police", "English"),

        // =========================================================================
        // 29. SIKKIM (Official Language: Nepali, English)
        // =========================================================================
        createApp("sikkim_psc_ne", "Sikkim PSC (Nepali)", "SPSC (NE)", "SIKKIM", "Sikkim PSC", "Nepali"),
        createApp("sikkim_psc_en", "Sikkim PSC (English)", "SPSC (EN)", "SIKKIM", "Sikkim PSC", "English"),
        createApp("sikkim_police_ne", "Sikkim Police (Nepali)", "SK-PC (NE)", "SIKKIM", "Sikkim Police", "Nepali"),
        createApp("sikkim_police_en", "Sikkim Police (English)", "SK-PC (EN)", "SIKKIM", "Sikkim Police", "English"),

        // =========================================================================
        // 30. ARUNACHAL PRADESH (Official Language: English)
        // =========================================================================
        createApp("arunachal_psc_en", "Arunachal APPSC (English)", "AR-PSC (EN)", "ARUNACHAL PRADESH", "Arunachal APPSC", "English"),
        createApp("arunachal_police_en", "Arunachal Police (English)", "AR-PC (EN)", "ARUNACHAL PRADESH", "Arunachal Police", "English"),

        // =========================================================================
        // 31. UPSC (Union Public Service Commission)
        // =========================================================================
        createApp("upsc_civil_services_en", "UPSC Civil Services (English)", "UPSC-CSE (EN)", "UPSC", "UPSC Civil Services", "English"),
        createApp("upsc_civil_services_hi", "UPSC Civil Services (Hindi)", "UPSC-CSE (HI)", "UPSC", "UPSC Civil Services", "Hindi"),
        createApp("upsc_civil_services_ur", "UPSC Civil Services (Urdu)", "UPSC-CSE (UR)", "UPSC", "UPSC Civil Services", "Urdu"),
        createApp("upsc_cds_en", "UPSC CDS (English)", "UPSC-CDS (EN)", "UPSC", "UPSC CDS", "English"),
        createApp("upsc_cds_hi", "UPSC CDS (Hindi)", "UPSC-CDS (HI)", "UPSC", "UPSC CDS", "Hindi"),
        createApp("upsc_nda_en", "UPSC NDA (English)", "UPSC-NDA (EN)", "UPSC", "UPSC NDA", "English"),
        createApp("upsc_nda_hi", "UPSC NDA (Hindi)", "UPSC-NDA (HI)", "UPSC", "UPSC NDA", "Hindi"),
        createApp("upsc_capf_en", "UPSC CAPF AC (English)", "UPSC-CAPF (EN)", "UPSC", "UPSC CAPF", "English"),
        createApp("upsc_capf_hi", "UPSC CAPF AC (Hindi)", "UPSC-CAPF (HI)", "UPSC", "UPSC CAPF", "Hindi"),
        createApp("upsc_epfo_en", "UPSC EPFO EO/AO (English)", "UPSC-EPFO (EN)", "UPSC", "UPSC EPFO", "English"),
        createApp("upsc_epfo_hi", "UPSC EPFO EO/AO (Hindi)", "UPSC-EPFO (HI)", "UPSC", "UPSC EPFO", "Hindi"),

        // =========================================================================
        // 32. SSC (Staff Selection Commission)
        // =========================================================================
        createApp("ssc_cgl_en", "SSC CGL (English)", "SSC-CGL (EN)", "SSC", "SSC CGL", "English"),
        createApp("ssc_cgl_hi", "SSC CGL (Hindi)", "SSC-CGL (HI)", "SSC", "SSC CGL", "Hindi"),
        createApp("ssc_chsl_en", "SSC CHSL (English)", "SSC-CHSL (EN)", "SSC", "SSC CHSL", "English"),
        createApp("ssc_chsl_hi", "SSC CHSL (Hindi)", "SSC-CHSL (HI)", "SSC", "SSC CHSL", "Hindi"),
        createApp("ssc_chsl_ur", "SSC CHSL (Urdu)", "SSC-CHSL (UR)", "SSC", "SSC CHSL", "Urdu"),
        createApp("ssc_mts_en", "SSC MTS (English)", "SSC-MTS (EN)", "SSC", "SSC MTS", "English"),
        createApp("ssc_mts_hi", "SSC MTS (Hindi)", "SSC-MTS (HI)", "SSC", "SSC MTS", "Hindi"),
        createApp("ssc_mts_ur", "SSC MTS (Urdu)", "SSC-MTS (UR)", "SSC", "SSC MTS", "Urdu"),
        createApp("ssc_gd_en", "SSC GD Constable (English)", "SSC-GD (EN)", "SSC", "SSC GD Constable", "English"),
        createApp("ssc_gd_hi", "SSC GD Constable (Hindi)", "SSC-GD (HI)", "SSC", "SSC GD Constable", "Hindi"),
        createApp("ssc_gd_ur", "SSC GD Constable (Urdu)", "SSC-GD (UR)", "SSC", "SSC GD Constable", "Urdu"),
        createApp("ssc_cpo_en", "SSC CPO (English)", "SSC-CPO (EN)", "SSC", "SSC CPO", "English"),
        createApp("ssc_cpo_hi", "SSC CPO (Hindi)", "SSC-CPO (HI)", "SSC", "SSC CPO", "Hindi"),
        createApp("ssc_steno_en", "SSC Stenographer (English)", "SSC-STENO (EN)", "SSC", "SSC Stenographer", "English"),
        createApp("ssc_steno_hi", "SSC Stenographer (Hindi)", "SSC-STENO (HI)", "SSC", "SSC Stenographer", "Hindi"),
        createApp("ssc_selection_post_en", "SSC Selection Post (English)", "SSC-POST (EN)", "SSC", "SSC Selection Post", "English"),
        createApp("ssc_selection_post_hi", "SSC Selection Post (Hindi)", "SSC-POST (HI)", "SSC", "SSC Selection Post", "Hindi"),

        // =========================================================================
        // 33. RAILWAYS (RRB / RRC)
        // =========================================================================
        createApp("rrb_ntpc_en", "RRB NTPC (English)", "RRB-NTPC (EN)", "RAILWAYS", "RRB NTPC", "English"),
        createApp("rrb_ntpc_hi", "RRB NTPC (Hindi)", "RRB-NTPC (HI)", "RAILWAYS", "RRB NTPC", "Hindi"),
        createApp("rrb_ntpc_ur", "RRB NTPC (Urdu)", "RRB-NTPC (UR)", "RAILWAYS", "RRB NTPC", "Urdu"),
        createApp("rrb_group_d_en", "RRB Group D (English)", "RRB-GRPD (EN)", "RAILWAYS", "RRB Group D", "English"),
        createApp("rrb_group_d_hi", "RRB Group D (Hindi)", "RRB-GRPD (HI)", "RAILWAYS", "RRB Group D", "Hindi"),
        createApp("rrb_group_d_ur", "RRB Group D (Urdu)", "RRB-GRPD (UR)", "RAILWAYS", "RRB Group D", "Urdu"),
        createApp("rrb_alp_en", "RRB ALP & Tech (English)", "RRB-ALP (EN)", "RAILWAYS", "RRB ALP & Technician", "English"),
        createApp("rrb_alp_hi", "RRB ALP & Tech (Hindi)", "RRB-ALP (HI)", "RAILWAYS", "RRB ALP & Technician", "Hindi"),
        createApp("rrb_alp_ur", "RRB ALP & Tech (Urdu)", "RRB-ALP (UR)", "RAILWAYS", "RRB ALP & Technician", "Urdu"),
        createApp("rrb_je_en", "RRB JE (English)", "RRB-JE (EN)", "RAILWAYS", "RRB Junior Engineer", "English"),
        createApp("rrb_je_hi", "RRB JE (Hindi)", "RRB-JE (HI)", "RAILWAYS", "RRB Junior Engineer", "Hindi"),
        createApp("rpf_constable_en", "RPF Constable (English)", "RPF-PC (EN)", "RAILWAYS", "RPF Constable", "English"),
        createApp("rpf_constable_hi", "RPF Constable (Hindi)", "RPF-PC (HI)", "RAILWAYS", "RPF Constable", "Hindi"),
        createApp("rpf_constable_ur", "RPF Constable (Urdu)", "RPF-PC (UR)", "RAILWAYS", "RPF Constable", "Urdu"),
        createApp("rpf_si_en", "RPF Sub-Inspector (English)", "RPF-SI (EN)", "RAILWAYS", "RPF Sub-Inspector", "English"),
        createApp("rpf_si_hi", "RPF Sub-Inspector (Hindi)", "RPF-SI (HI)", "RAILWAYS", "RPF Sub-Inspector", "Hindi"),
        createApp("rpf_si_ur", "RPF Sub-Inspector (Urdu)", "RPF-SI (UR)", "RAILWAYS", "RPF Sub-Inspector", "Urdu"),

        // =========================================================================
        // 34. BANKING (IBPS / SBI / RBI)
        // =========================================================================
        createApp("ibps_clerk_en", "IBPS Clerk (English)", "IBPS-CLK (EN)", "BANKING", "IBPS Clerk", "English"),
        createApp("ibps_clerk_hi", "IBPS Clerk (Hindi)", "IBPS-CLK (HI)", "BANKING", "IBPS Clerk", "Hindi"),
        createApp("ibps_clerk_ur", "IBPS Clerk (Urdu)", "IBPS-CLK (UR)", "BANKING", "IBPS Clerk", "Urdu"),
        createApp("ibps_rrb_clerk_en", "IBPS RRB Clerk (English)", "RRB-CLK (EN)", "BANKING", "IBPS RRB Clerk", "English"),
        createApp("ibps_rrb_clerk_hi", "IBPS RRB Clerk (Hindi)", "RRB-CLK (HI)", "BANKING", "IBPS RRB Clerk", "Hindi"),
        createApp("ibps_rrb_clerk_ur", "IBPS RRB Clerk (Urdu)", "RRB-CLK (UR)", "BANKING", "IBPS RRB Clerk", "Urdu"),
        createApp("ibps_rrb_po_en", "IBPS RRB PO (English)", "RRB-PO (EN)", "BANKING", "IBPS RRB PO", "English"),
        createApp("ibps_rrb_po_hi", "IBPS RRB PO (Hindi)", "RRB-PO (HI)", "BANKING", "IBPS RRB PO", "Hindi"),
        createApp("ibps_rrb_po_ur", "IBPS RRB PO (Urdu)", "RRB-PO (UR)", "BANKING", "IBPS RRB PO", "Urdu"),
        createApp("ibps_po_en", "IBPS PO (English)", "IBPS-PO (EN)", "BANKING", "IBPS PO", "English"),
        createApp("ibps_po_hi", "IBPS PO (Hindi)", "IBPS-PO (HI)", "BANKING", "IBPS PO", "Hindi"),
        createApp("sbi_clerk_en", "SBI Clerk (English)", "SBI-CLK (EN)", "BANKING", "SBI Clerk", "English"),
        createApp("sbi_clerk_hi", "SBI Clerk (Hindi)", "SBI-CLK (HI)", "BANKING", "SBI Clerk", "Hindi"),
        createApp("sbi_clerk_ur", "SBI Clerk (Urdu)", "SBI-CLK (UR)", "BANKING", "SBI Clerk", "Urdu"),
        createApp("sbi_po_en", "SBI PO (English)", "SBI-PO (EN)", "BANKING", "SBI PO", "English"),
        createApp("sbi_po_hi", "SBI PO (Hindi)", "SBI-PO (HI)", "BANKING", "SBI PO", "Hindi"),
        createApp("rbi_grade_b_en", "RBI Grade B (English)", "RBI-GRB (EN)", "BANKING", "RBI Grade B", "English"),
        createApp("rbi_grade_b_hi", "RBI Grade B (Hindi)", "RBI-GRB (HI)", "BANKING", "RBI Grade B", "Hindi"),
        createApp("rbi_assistant_en", "RBI Assistant (English)", "RBI-ASST (EN)", "BANKING", "RBI Assistant", "English"),
        createApp("rbi_assistant_hi", "RBI Assistant (Hindi)", "RBI-ASST (HI)", "BANKING", "RBI Assistant", "Hindi"),
        createApp("nabard_grade_a_en", "NABARD Grade A (English)", "NABARD (EN)", "BANKING", "NABARD Grade A", "English"),
        createApp("nabard_grade_a_hi", "NABARD Grade A (Hindi)", "NABARD (HI)", "BANKING", "NABARD Grade A", "Hindi"),

        // =========================================================================
        // 35. CENTRAL TEACHING (CTET / KVS / NVS / EMRS / UGC NET)
        // =========================================================================
        createApp("ctet_en", "CTET (English)", "CTET (EN)", "CENTRAL TEACHING", "CTET", "English"),
        createApp("ctet_hi", "CTET (Hindi)", "CTET (HI)", "CENTRAL TEACHING", "CTET", "Hindi"),
        createApp("ctet_ur", "CTET (Urdu)", "CTET (UR)", "CENTRAL TEACHING", "CTET", "Urdu"),
        createApp("kvs_teaching_en", "KVS Teaching (English)", "KVS (EN)", "CENTRAL TEACHING", "KVS Teaching", "English"),
        createApp("kvs_teaching_hi", "KVS Teaching (Hindi)", "KVS (HI)", "CENTRAL TEACHING", "KVS Teaching", "Hindi"),
        createApp("nvs_teaching_en", "NVS Teaching (English)", "NVS (EN)", "CENTRAL TEACHING", "NVS Teaching", "English"),
        createApp("nvs_teaching_hi", "NVS Teaching (Hindi)", "NVS (HI)", "CENTRAL TEACHING", "NVS Teaching", "Hindi"),
        createApp("emrs_teaching_en", "EMRS Teaching (English)", "EMRS (EN)", "CENTRAL TEACHING", "EMRS Teaching", "English"),
        createApp("emrs_teaching_hi", "EMRS Teaching (Hindi)", "EMRS (HI)", "CENTRAL TEACHING", "EMRS Teaching", "Hindi"),
        createApp("ugc_net_en", "UGC NET (English)", "UGC-NET (EN)", "CENTRAL TEACHING", "UGC NET", "English"),
        createApp("ugc_net_hi", "UGC NET (Hindi)", "UGC-NET (HI)", "CENTRAL TEACHING", "UGC NET", "Hindi"),

        // =========================================================================
        // 36. DEFENCE (Defence Recruitments)
        // =========================================================================
        createApp("afcat_en", "AFCAT (English)", "AFCAT (EN)", "DEFENCE", "AFCAT", "English"),
        createApp("coast_guard_en", "Indian Coast Guard (English)", "ICG (EN)", "DEFENCE", "Indian Coast Guard", "English"),
        createApp("coast_guard_hi", "Indian Coast Guard (Hindi)", "ICG (HI)", "DEFENCE", "Indian Coast Guard", "Hindi"),
        createApp("agniveer_army_en", "Agniveer Army (English)", "AGN-ARMY (EN)", "DEFENCE", "Agniveer Army", "English"),
        createApp("agniveer_army_hi", "Agniveer Army (Hindi)", "AGN-ARMY (HI)", "DEFENCE", "Agniveer Army", "Hindi"),
        createApp("agniveer_navy_en", "Agniveer Navy (English)", "AGN-NAVY (EN)", "DEFENCE", "Agniveer Navy", "English"),
        createApp("agniveer_navy_hi", "Agniveer Navy (Hindi)", "AGN-NAVY (HI)", "DEFENCE", "Agniveer Navy", "Hindi"),
        createApp("agniveer_airforce_en", "Agniveer Air Force (English)", "AGN-IAF (EN)", "DEFENCE", "Agniveer Air Force", "English"),
        createApp("agniveer_airforce_hi", "Agniveer Air Force (Hindi)", "AGN-IAF (HI)", "DEFENCE", "Agniveer Air Force", "Hindi"),

        // =========================================================================
        // 37. OTHER CENTRAL RECRUITMENT (LIC / ESIC / EPFO / FCI)
        // =========================================================================
        createApp("lic_aao_en", "LIC AAO (English)", "LIC-AAO (EN)", "OTHER CENTRAL RECRUITMENT", "LIC AAO", "English"),
        createApp("lic_aao_hi", "LIC AAO (Hindi)", "LIC-AAO (HI)", "OTHER CENTRAL RECRUITMENT", "LIC AAO", "Hindi"),
        createApp("lic_ado_en", "LIC ADO (English)", "LIC-ADO (EN)", "OTHER CENTRAL RECRUITMENT", "LIC ADO", "English"),
        createApp("lic_ado_hi", "LIC ADO (Hindi)", "LIC-ADO (HI)", "OTHER CENTRAL RECRUITMENT", "LIC ADO", "Hindi"),
        createApp("esic_udc_en", "ESIC UDC & SSO (English)", "ESIC (EN)", "OTHER CENTRAL RECRUITMENT", "ESIC UDC & SSO", "English"),
        createApp("esic_udc_hi", "ESIC UDC & SSO (Hindi)", "ESIC (HI)", "OTHER CENTRAL RECRUITMENT", "ESIC UDC & SSO", "Hindi"),
        createApp("epfo_ssa_en", "EPFO SSA (English)", "EPFO-SSA (EN)", "OTHER CENTRAL RECRUITMENT", "EPFO SSA", "English"),
        createApp("epfo_ssa_hi", "EPFO SSA (Hindi)", "EPFO-SSA (HI)", "OTHER CENTRAL RECRUITMENT", "EPFO SSA", "Hindi"),
        createApp("fci_manager_en", "FCI Category II & III (English)", "FCI (EN)", "OTHER CENTRAL RECRUITMENT", "FCI Recruitment", "English"),
        createApp("fci_manager_hi", "FCI Category II & III (Hindi)", "FCI (HI)", "OTHER CENTRAL RECRUITMENT", "FCI Recruitment", "Hindi")
    )
}
