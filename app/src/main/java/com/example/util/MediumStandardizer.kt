package com.example.util

/**
 * Centralized utility for Medium Name Standardization across the entire multi-app platform.
 *
 * Rules:
 * - Remove the word "Medium" from all language/medium names throughout the system.
 * - Always use only the language name alone:
 *     • English Medium → English
 *     • Telugu Medium → Telugu
 *     • Hindi Medium → Hindi
 *     • Kannada Medium → Kannada
 *     • Tamil Medium → Tamil
 *     • Malayalam Medium → Malayalam
 *     • Marathi Medium → Marathi
 *     • Urdu Medium → Urdu
 * - Ensure no duplicate entries like "English" and "English Medium" exist simultaneously.
 */
object MediumStandardizer {

    val STANDARDIZED_LANGUAGES = listOf(
        "English",
        "Telugu",
        "Hindi",
        "Urdu",
        "Tamil",
        "Kannada",
        "Malayalam",
        "Marathi",
        "Gujarati",
        "Bengali",
        "Odia",
        "Punjabi",
        "Assamese",
        "Manipuri",
        "Mizo",
        "Nepali"
    )

    /**
     * Standardizes a language/medium string by removing "Medium" and extra spaces.
     * Examples:
     * - "English Medium" -> "English"
     * - "Telugu Medium" -> "Telugu"
     * - "Hindi Medium" -> "Hindi"
     * - "Telugu" -> "Telugu"
     */
    fun standardizeLanguageName(input: String?): String {
        if (input.isNullOrBlank()) return "English"
        return input
            .replace(Regex("(?i)\\s*-\\s*Medium\\b"), "")
            .replace(Regex("(?i)\\s*Medium\\b"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank { "English" }
    }

    /**
     * Standardizes an app title or description by replacing any "(Language Medium)"
     * with "(Language)".
     * Example:
     * - "APPSC Group-2 (Telugu Medium)" -> "APPSC Group-2 (Telugu)"
     * - "SBI PO (English Medium)" -> "SBI PO (English)"
     * - "CTET (Urdu Medium)" -> "CTET (Urdu)"
     */
    fun standardizeAppName(name: String?): String {
        if (name.isNullOrBlank()) return ""
        return name
            .replace(Regex("(?i)\\s*Medium\\)"), ")")
            .replace(Regex("(?i)\\s*Medium\\b"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Deduplicates and standardizes a list of language options to prevent dual entries
     * like ["English", "English Medium"] or duplicates.
     */
    fun standardizeLanguageList(languages: List<String>): List<String> {
        return languages
            .map { standardizeLanguageName(it) }
            .filter { it.isNotBlank() }
            .distinct()
    }
}
