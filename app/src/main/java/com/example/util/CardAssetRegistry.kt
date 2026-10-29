package com.example.util

import com.example.R
import com.example.model.Subject
import com.example.model.TopicModel
import com.example.model.UnitModel

/**
 * Universal Visual Asset Registry for APPSC Prep Platform
 * Maps every Subject, Unit, and Topic in the syllabus to guaranteed, high-resolution,
 * locally bundled landscape card vector assets.
 * Eliminates blank cards, handles offline mode gracefully, and guarantees instant zero-lag rendering.
 */
object CardAssetRegistry {

    fun getSubjectDrawable(subjectId: String, subjectName: String = ""): Int {
        val sId = subjectId.lowercase().trim()
        val sName = subjectName.lowercase().trim()

        return when {
            sId == "subj-history" || sName.contains("history") && !sName.contains("andhra") -> {
                R.drawable.card_subj_history
            }
            sId == "subj-geography" || sName.contains("geography") -> {
                R.drawable.card_subj_geography
            }
            sId == "subj-society" || sName.contains("society") || sName.contains("social justice") -> {
                R.drawable.card_subj_society
            }
            sId == "subj-mental-ability" || sName.contains("mental ability") || sName.contains("reasoning") -> {
                R.drawable.card_subj_mental_ability
            }
            sId == "subj-ap-history" || sName.contains("andhra") && sName.contains("history") -> {
                R.drawable.card_subj_ap_history
            }
            sId == "subj-constitution" || sName.contains("constitution") || sName.contains("polity") -> {
                R.drawable.card_subj_constitution
            }
            sId == "subj-economy" || sName.contains("economy") -> {
                R.drawable.card_subj_economy
            }
            sId == "subj-science-tech" || sName.contains("science") || sName.contains("technology") -> {
                R.drawable.card_subj_science_tech
            }
            sId == "subj-current-affairs" || sName.contains("current affairs") -> {
                R.drawable.card_subj_current_affairs
            }
            else -> R.drawable.card_subj_history
        }
    }

    fun getUnitDrawable(unitId: String, unitName: String = "", subjectId: String = ""): Int {
        val uId = unitId.lowercase().trim()
        val uName = unitName.lowercase().trim()

        return when {
            uId == "unit-hist-1" || uName.contains("ancient") || uName.contains("medieval") && !uName.contains("andhra") -> {
                R.drawable.card_unit_hist_ancient
            }
            uId == "unit-hist-2" || uName.contains("freedom") || uName.contains("modern indian") || uName.contains("1857") -> {
                R.drawable.card_unit_hist_modern
            }
            uId == "unit-geo-1" || uName.contains("general & physical") || uName.contains("physical geography") -> {
                R.drawable.card_unit_geo_physical
            }
            uId == "unit-geo-2" || uName.contains("regional geography of ap") || uName.contains("economic & regional") -> {
                R.drawable.card_unit_geo_ap
            }
            uId == "unit-soc-1" || uName.contains("structure of indian society") -> {
                R.drawable.card_unit_soc_structure
            }
            uId == "unit-soc-2" || uName.contains("social issues") || uName.contains("welfare policies") -> {
                R.drawable.card_unit_soc_welfare
            }
            uId == "unit-ma-1" || uName.contains("logical") || uName.contains("analytical reasoning") -> {
                R.drawable.card_unit_ma_reasoning
            }
            uId == "unit-ma-2" || uName.contains("numeracy") || uName.contains("data interpretation") -> {
                R.drawable.card_unit_ma_numeracy
            }
            uId == "unit-aph-1" || uName.contains("andhra dynasties") -> {
                R.drawable.card_unit_aph_dynasties
            }
            uId == "unit-aph-2" || uName.contains("andhra movement") || uName.contains("state formation") -> {
                R.drawable.card_unit_aph_movement
            }
            uId == "unit-pol-1" || uName.contains("constitutional framework") || uName.contains("fundamental rights") -> {
                R.drawable.card_unit_pol_constitution
            }
            uId == "unit-pol-2" || uName.contains("union & state") || uName.contains("panchayati raj") -> {
                R.drawable.card_unit_pol_governance
            }
            uId == "unit-eco-1" || uName.contains("structure of indian economy") || uName.contains("planning") -> {
                R.drawable.card_unit_eco_planning
            }
            uId == "unit-eco-2" || uName.contains("andhra pradesh economy") || uName.contains("bifurcation") -> {
                R.drawable.card_unit_eco_ap
            }
            uId == "unit-sci-1" || uName.contains("technology missions") || uName.contains("space exploration") -> {
                R.drawable.card_unit_sci_space
            }
            uId == "unit-sci-2" || uName.contains("energy") || uName.contains("environment") -> {
                R.drawable.card_unit_sci_energy
            }
            uId == "unit-ca-1" || uName.contains("international") || uName.contains("summits") -> {
                R.drawable.card_unit_ca_global
            }
            uId == "unit-ca-2" || uName.contains("national current affairs") || uName.contains("central government") -> {
                R.drawable.card_unit_ca_national
            }
            uId == "unit-ca-3" || uName.contains("state current affairs") || uName.contains("welfare schemes") -> {
                R.drawable.card_unit_ca_ap
            }
            else -> getSubjectDrawable(subjectId)
        }
    }

    fun getTopicDrawable(topicId: String, topicName: String = "", unitId: String = "", subjectId: String = ""): Int {
        val tId = topicId.lowercase().trim()
        val tName = topicName.lowercase().trim()

        return when {
            tId == "top-hist-1" || tName.contains("indus") || tName.contains("vedic") -> {
                R.drawable.card_topic_indus_vedic
            }
            tId == "top-hist-2" || tName.contains("mauryan") || tName.contains("gupta") -> {
                R.drawable.card_topic_mauryan_gupta
            }
            tId == "top-hist-3" || tName.contains("1857") || tName.contains("nationalism") -> {
                R.drawable.card_topic_revolt_1857
            }
            tId == "top-hist-4" || tName.contains("gandhian") || tName.contains("independence") -> {
                R.drawable.card_topic_gandhian_era
            }
            tId == "top-geo-1" || tName.contains("physiographic") || tName.contains("drainage") -> {
                R.drawable.card_topic_physiography
            }
            tId == "top-geo-2" || tName.contains("minerals") || tName.contains("ports") -> {
                R.drawable.card_topic_ap_ports
            }
            tId == "top-soc-1" || tName.contains("family") || tName.contains("marriage") || tName.contains("caste") -> {
                R.drawable.card_topic_family_caste
            }
            tId == "top-soc-2" || tName.contains("poverty") || tName.contains("women") || tName.contains("child") -> {
                R.drawable.card_topic_women_empowerment
            }
            tId == "top-ma-1" || tName.contains("deductive") || tName.contains("coding") -> {
                R.drawable.card_topic_deductive_coding
            }
            tId == "top-ma-2" || tName.contains("percentages") || tName.contains("ratios") || tName.contains("charts") -> {
                R.drawable.card_topic_percentages_charts
            }
            tId == "top-aph-1" || tName.contains("satavahana") || tName.contains("ikshvaku") -> {
                R.drawable.card_topic_satavahanas
            }
            tId == "top-aph-2" || tName.contains("kakatiya") || tName.contains("vijayanagara") -> {
                R.drawable.card_topic_kakatiyas
            }
            tId == "top-aph-3" || tName.contains("kandukuri") || tName.contains("potti sreeramulu") -> {
                R.drawable.card_topic_potti_sreeramulu
            }
            tId == "top-pol-1" || tName.contains("preamble") || tName.contains("fundamental rights") || tName.contains("dpsp") -> {
                R.drawable.card_topic_fundamental_rights
            }
            tId == "top-pol-2" || tName.contains("parliament") || tName.contains("executive") || tName.contains("amendment") -> {
                R.drawable.card_unit_pol_governance
            }
            tId == "top-eco-1" || tName.contains("national income") || tName.contains("niti") || tName.contains("banking") -> {
                R.drawable.card_unit_eco_planning
            }
            tId == "top-eco-2" || tName.contains("gross product") || tName.contains("reorganisation") -> {
                R.drawable.card_unit_eco_ap
            }
            tId == "top-sci-1" || tName.contains("isro") || tName.contains("satellite") || tName.contains("missile") -> {
                R.drawable.card_unit_sci_space
            }
            tId == "top-sci-2" || tName.contains("renewable") || tName.contains("climate") || tName.contains("energy") -> {
                R.drawable.card_unit_sci_energy
            }
            tId == "top-ca-1" || tName.contains("summits") || tName.contains("treaties") -> {
                R.drawable.card_unit_ca_global
            }
            tId == "top-ca-2" || tName.contains("governance") || tName.contains("flagship") || tName.contains("milestones") -> {
                R.drawable.card_unit_ca_national
            }
            tId == "top-ca-3" || tName.contains("polavaram") || tName.contains("schemes") || tName.contains("development") -> {
                R.drawable.card_unit_ca_ap
            }
            else -> getUnitDrawable(unitId, "", subjectId)
        }
    }
}
