package com.example.db

import androidx.room.TypeConverter
import com.example.model.Question
import com.example.model.SubjectQuestionConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromQuestionList(value: List<Question>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toQuestionList(value: String): List<Question> {
        val type = object : TypeToken<List<Question>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromSubjectConfigList(value: List<SubjectQuestionConfig>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toSubjectConfigList(value: String): List<SubjectQuestionConfig> {
        val type = object : TypeToken<List<SubjectQuestionConfig>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromMap(value: Map<String, String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMap(value: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromStringListMap(value: Map<String, List<String>>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringListMap(value: String): Map<String, List<String>> {
        val type = object : TypeToken<Map<String, List<String>>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromStringSet(value: Set<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringSet(value: String): Set<String> {
        val type = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(value, type) ?: emptySet()
    }

    @TypeConverter
    fun fromAttemptHistory(value: List<com.example.model.MockTestAttemptRecord>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAttemptHistory(value: String): List<com.example.model.MockTestAttemptRecord> {
        val type = object : TypeToken<List<com.example.model.MockTestAttemptRecord>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromUnitModelList(value: List<com.example.model.UnitModel>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toUnitModelList(value: String): List<com.example.model.UnitModel> {
        val type = object : TypeToken<List<com.example.model.UnitModel>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
