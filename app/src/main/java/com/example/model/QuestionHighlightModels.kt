package com.example.model

enum class HighlightLocation {
    SUBTOPICS_QUESTION,
    SETS_QUESTION,
    MOCK_TEST_QUESTION,
    SUBTOPICS_OPTION,
    SETS_OPTION,
    MOCK_TEST_OPTION,
    SUBTOPICS_EXPLANATION,
    SETS_EXPLANATION,
    OTHER
}

data class HighlightRange(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String = "",
    val startIndex: Int = -1,
    val endIndex: Int = -1,
    val type: String = "key_concepts" // key_concepts, numbers_dates, formulas, answers, exceptions, names_places
)

data class HighlightPreset(
    val name: String,
    val bgColorHex: Long,
    val textColorHex: Long,
    val borderColorHex: Long
) {
    companion object {
        val SOFT_AMBER = HighlightPreset("Soft Amber", 0xFFFFF0B3, 0xFF5B3E00, 0xFFFFE280)
        val SOFT_BLUE = HighlightPreset("Soft Blue", 0xFFDCEBFF, 0xFF1E3A8A, 0xFFB9D6FF)
        val SOFT_PURPLE = HighlightPreset("Soft Purple", 0xFFE9DFFF, 0xFF581C87, 0xFFD1BFFF)
        val SOFT_GREEN = HighlightPreset("Soft Green", 0xFFDDF5E5, 0xFF14532D, 0xFFB5ECD0)
        val SOFT_PINK = HighlightPreset("Soft Pink", 0xFFFFE1E6, 0xFF9F1239, 0xFFFFC2CE)
        val SOFT_TEAL = HighlightPreset("Soft Teal", 0xFFD8F3F0, 0xFF115E59, 0xFFAEEBE4)

        val ALL_PRESETS = listOf(SOFT_AMBER, SOFT_BLUE, SOFT_PURPLE, SOFT_GREEN, SOFT_PINK, SOFT_TEAL)

        fun fromName(name: String): HighlightPreset {
            val normalized = name.lowercase().trim()
            return when {
                normalized.contains("key_concepts") || normalized.contains("key_concept") || normalized.contains("amber") -> SOFT_AMBER
                normalized.contains("numbers_dates") || normalized.contains("numbers") || normalized.contains("dates") || normalized.contains("blue") -> SOFT_BLUE
                normalized.contains("formulas") || normalized.contains("equations") || normalized.contains("purple") -> SOFT_PURPLE
                normalized.contains("answers") || normalized.contains("conclusions") || normalized.contains("green") -> SOFT_GREEN
                normalized.contains("exceptions") || normalized.contains("important") || normalized.contains("pink") -> SOFT_PINK
                normalized.contains("names_places") || normalized.contains("special_terms") || normalized.contains("teal") -> SOFT_TEAL
                else -> ALL_PRESETS.find { 
                    it.name.equals(name, ignoreCase = true) || 
                    it.name.replace(" ", "_").equals(name, ignoreCase = true) 
                } ?: SOFT_AMBER
            }
        }
    }
}

data class QuestionHighlightConfig(
    val isMasterEnabled: Boolean = true,
    val isSubtopicsQuestionsEnabled: Boolean = true,
    val isSetsQuestionsEnabled: Boolean = true,
    val isMockTestQuestionsEnabled: Boolean = true,
    val isSubtopicsExplanationsEnabled: Boolean = true,
    val isSetsExplanationsEnabled: Boolean = true,
    // Style Settings
    val presetName: String = "Soft Amber",
    val bgColorHex: Long = 0xFFFFF0B3,
    val textColorHex: Long = 0xFF5B3E00,
    val fontWeightName: String = "SemiBold", // Normal, Medium, SemiBold, Bold
    val cornerRadiusDp: Int = 6,
    val horizontalPaddingDp: Int = 6,
    val verticalPaddingDp: Int = 3,
    val hasBorder: Boolean = true,
    val borderColorHex: Long = 0xFFFFE280,
    val borderWidthDp: Int = 1,
    val targetAppScope: String = "THIS_APP" // "THIS_APP", "SELECTED_APPS", "ALL_APPS"
) {
    fun isEnabledFor(location: HighlightLocation): Boolean {
        if (!isMasterEnabled) return false
        return when (location) {
            HighlightLocation.SUBTOPICS_QUESTION, HighlightLocation.SUBTOPICS_OPTION -> isSubtopicsQuestionsEnabled
            HighlightLocation.SETS_QUESTION, HighlightLocation.SETS_OPTION -> isSetsQuestionsEnabled
            HighlightLocation.MOCK_TEST_QUESTION, HighlightLocation.MOCK_TEST_OPTION -> isMockTestQuestionsEnabled
            HighlightLocation.SUBTOPICS_EXPLANATION -> isSubtopicsExplanationsEnabled
            HighlightLocation.SETS_EXPLANATION -> isSetsExplanationsEnabled
            HighlightLocation.OTHER -> false
        }
    }
}
