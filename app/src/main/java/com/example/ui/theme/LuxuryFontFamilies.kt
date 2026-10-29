package com.example.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R

val PlayfairDisplayFontFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal)
)

val MontserratFontFamily = FontFamily(
    Font(R.font.montserrat, FontWeight.Normal)
)

val RobotoFontFamily = FontFamily(
    Font(R.font.roboto, FontWeight.Normal)
)

val OpenSansFontFamily = FontFamily(
    Font(R.font.open_sans, FontWeight.Normal)
)

val QuicksandFontFamily = FontFamily(
    Font(R.font.quicksand, FontWeight.Normal)
)

val RobotoSlabFontFamily = FontFamily(
    Font(R.font.roboto_slab, FontWeight.Normal)
)

val PacificoFontFamily = FontFamily(
    Font(R.font.pacifico, FontWeight.Normal)
)

val CinzelFontFamily = FontFamily(
    Font(R.font.cinzel, FontWeight.Normal)
)

val AvailableFontFamilies = mapOf(
    "Cinzel" to CinzelFontFamily,
    "Playfair Display" to PlayfairDisplayFontFamily,
    "Montserrat" to MontserratFontFamily,
    "Roboto" to RobotoFontFamily,
    "Open Sans" to OpenSansFontFamily,
    "Quicksand" to QuicksandFontFamily,
    "Roboto Slab" to RobotoSlabFontFamily,
    "Pacifico" to PacificoFontFamily
)
