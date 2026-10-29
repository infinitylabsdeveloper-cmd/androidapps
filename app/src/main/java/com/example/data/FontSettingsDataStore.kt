package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.fontSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "font_settings")

object FontSettingsDataStore {
    val FONT_FAMILY = stringPreferencesKey("font_family")
    val FONT_SIZE = floatPreferencesKey("font_size")
    val FONT_WEIGHT = intPreferencesKey("font_weight")
    val TEXT_COLOR = stringPreferencesKey("text_color")
    val BACKGROUND_COLOR = stringPreferencesKey("background_color")
}
