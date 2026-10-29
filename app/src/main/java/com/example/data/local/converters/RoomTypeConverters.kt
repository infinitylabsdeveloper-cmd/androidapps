package com.example.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Type converters for Room database to handle complex types such as Maps, Lists, and JSON objects.
 */
class RoomTypeConverters {

    private val gson = Gson()

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String {
        if (value == null) return "{}"
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String> {
        if (value.isNullOrBlank()) return emptyMap()
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return try {
            gson.fromJson(value, mapType) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return "[]"
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
