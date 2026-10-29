package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class LuxuryFontConfig(
    val fontFamilyName: String = "Roboto",
    val fontSizeSp: Float = 16f,
    val fontWeight: Int = 400, // FontWeight.Normal
    val letterSpacingSp: Float = 0.5f,
    val lineHeightSp: Float = 24f,
    val backgroundColor: String = "#FAF7F2",
    val textColor: String = "#292524",
    val headingColor: String = "#633F2D"
)
