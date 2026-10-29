package com.example.util

import org.junit.Assert.*
import org.junit.Test

class MediumStandardizerTest {

    @Test
    fun `test medium name standardization for individual languages`() {
        assertEquals("English", MediumStandardizer.standardizeLanguageName("English Medium"))
        assertEquals("Telugu", MediumStandardizer.standardizeLanguageName("Telugu Medium"))
        assertEquals("Hindi", MediumStandardizer.standardizeLanguageName("Hindi Medium"))
        assertEquals("Kannada", MediumStandardizer.standardizeLanguageName("Kannada Medium"))
        assertEquals("Tamil", MediumStandardizer.standardizeLanguageName("Tamil Medium"))
        assertEquals("Malayalam", MediumStandardizer.standardizeLanguageName("Malayalam Medium"))
        assertEquals("Marathi", MediumStandardizer.standardizeLanguageName("Marathi Medium"))
        assertEquals("Urdu", MediumStandardizer.standardizeLanguageName("Urdu Medium"))
        assertEquals("English", MediumStandardizer.standardizeLanguageName("English"))
        assertEquals("Telugu", MediumStandardizer.standardizeLanguageName("Telugu"))
    }

    @Test
    fun `test app name standardization removes Medium from parenthesis`() {
        assertEquals("APPSC Group-2 (Telugu)", MediumStandardizer.standardizeAppName("APPSC Group-2 (Telugu Medium)"))
        assertEquals("SBI PO (Hindi)", MediumStandardizer.standardizeAppName("SBI PO (Hindi Medium)"))
        assertEquals("CTET (Urdu)", MediumStandardizer.standardizeAppName("CTET (Urdu Medium)"))
        assertEquals("AP DSC (English)", MediumStandardizer.standardizeAppName("AP DSC (English Medium)"))
        assertEquals("Railway RRB (Tamil)", MediumStandardizer.standardizeAppName("Railway RRB (Tamil Medium)"))
    }

    @Test
    fun `test language list deduplication prevents duplicate options`() {
        val mixedLanguages = listOf("English", "English Medium", "Telugu", "Telugu Medium", "Hindi", "Hindi Medium")
        val standardized = MediumStandardizer.standardizeLanguageList(mixedLanguages)

        assertEquals(3, standardized.size)
        assertEquals(listOf("English", "Telugu", "Hindi"), standardized)
        assertFalse(standardized.contains("English Medium"))
        assertFalse(standardized.contains("Telugu Medium"))
        assertFalse(standardized.contains("Hindi Medium"))
    }

    @Test
    fun `test supported languages list contains only pure language names`() {
        MediumStandardizer.STANDARDIZED_LANGUAGES.forEach { lang ->
            assertFalse("Language name should not contain 'Medium': $lang", lang.contains("Medium", ignoreCase = true))
        }
    }
}
