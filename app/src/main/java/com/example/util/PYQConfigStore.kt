package com.example.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class PYQAvailabilityStatus {
    ENABLED,
    DISABLED,
    SCHEDULED
}

enum class PYQDisabledMessageType {
    UPDATING_SOON,
    COMING_SOON,
    CUSTOM
}

data class PYQFeatureToggles(
    val fullPaperPractice: Boolean = true,
    val practiceMode: Boolean = true,
    val examMode: Boolean = true,
    val answerExplanations: Boolean = true,
    val bookmarks: Boolean = true,
    val wrongAnswersPractice: Boolean = true,
    val reattempts: Boolean = true,
    val analytics: Boolean = true
)

data class PYQAppConfig(
    val appId: String = "default",
    val enabled: Boolean = true,
    val availabilityStatus: PYQAvailabilityStatus = PYQAvailabilityStatus.ENABLED,
    val scheduledDateTime: String = "15 Nov 2024",
    val disabledMessageType: PYQDisabledMessageType = PYQDisabledMessageType.UPDATING_SOON,
    val disabledCustomMessage: String = "We're working hard to bring Previous Year Questions for you. Stay tuned!",
    val featureToggles: PYQFeatureToggles = PYQFeatureToggles(),
    // Exam Settings
    val defaultQuestionsCount: Int = 200,
    val defaultTimeLimitMinutes: Int = 120,
    val marksPerCorrectAnswer: Double = 1.0,
    val negativeMarks: Double = 0.33,
    val shuffleQuestions: Boolean = false,
    val showQuestionPalette: Boolean = true,
    val allowMarkForReview: Boolean = true,
    val showExplanationsAfterSubmit: Boolean = true
)

object PYQConfigStore {
    private const val PREFS_NAME = "pyq_config_prefs_v2"
    private const val KEY_CONFIGS_JSON = "pyq_app_configs_json"

    fun getConfigForApp(context: Context, appId: String): PYQAppConfig {
        val configs = getAllConfigs(context)
        return configs[appId.lowercase()] ?: PYQAppConfig(appId = appId)
    }

    fun saveConfig(
        context: Context,
        config: PYQAppConfig,
        scope: String, // "THIS_APP", "SELECTED_APPS", "ALL_APPS"
        selectedAppIds: List<String> = emptyList()
    ) {
        val currentConfigs = getAllConfigs(context).toMutableMap()

        val targetAppIds = when (scope.uppercase()) {
            "ALL_APPS" -> com.example.data.SampleData.appsListState.map { it.id.lowercase() }.ifEmpty { listOf(config.appId.lowercase()) }
            "SELECTED_APPS" -> selectedAppIds.map { it.lowercase() }.ifEmpty { listOf(config.appId.lowercase()) }
            else -> listOf(config.appId.lowercase())
        }

        for (id in targetAppIds) {
            currentConfigs[id] = config.copy(appId = id)
        }

        persistMap(context, currentConfigs)
    }

    private fun getAllConfigs(context: Context): Map<String, PYQAppConfig> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_CONFIGS_JSON, "{}") ?: "{}"
        val map = mutableMapOf<String, PYQAppConfig>()

        try {
            val rootObj = JSONObject(jsonStr)
            val keys = rootObj.keys()
            while (keys.hasNext()) {
                val appId = keys.next()
                val obj = rootObj.getJSONObject(appId)
                map[appId.lowercase()] = parseConfigObject(appId, obj)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return map
    }

    private fun parseConfigObject(appId: String, obj: JSONObject): PYQAppConfig {
        val enabled = obj.optBoolean("enabled", true)
        val statusStr = obj.optString("availabilityStatus", "ENABLED")
        val status = try { PYQAvailabilityStatus.valueOf(statusStr) } catch (e: Exception) { PYQAvailabilityStatus.ENABLED }

        val msgTypeStr = obj.optString("disabledMessageType", "UPDATING_SOON")
        val msgType = try { PYQDisabledMessageType.valueOf(msgTypeStr) } catch (e: Exception) { PYQDisabledMessageType.UPDATING_SOON }

        val togglesObj = obj.optJSONObject("featureToggles") ?: JSONObject()
        val featureToggles = PYQFeatureToggles(
            fullPaperPractice = togglesObj.optBoolean("fullPaperPractice", true),
            practiceMode = togglesObj.optBoolean("practiceMode", true),
            examMode = togglesObj.optBoolean("examMode", true),
            answerExplanations = togglesObj.optBoolean("answerExplanations", true),
            bookmarks = togglesObj.optBoolean("bookmarks", true),
            wrongAnswersPractice = togglesObj.optBoolean("wrongAnswersPractice", true),
            reattempts = togglesObj.optBoolean("reattempts", true),
            analytics = togglesObj.optBoolean("analytics", true)
        )

        return PYQAppConfig(
            appId = appId,
            enabled = enabled,
            availabilityStatus = status,
            scheduledDateTime = obj.optString("scheduledDateTime", "15 Nov 2024"),
            disabledMessageType = msgType,
            disabledCustomMessage = obj.optString("disabledCustomMessage", "We're working hard to bring Previous Year Questions for you. Stay tuned!"),
            featureToggles = featureToggles,
            defaultQuestionsCount = obj.optInt("defaultQuestionsCount", 200),
            defaultTimeLimitMinutes = obj.optInt("defaultTimeLimitMinutes", 120),
            marksPerCorrectAnswer = obj.optDouble("marksPerCorrectAnswer", 1.0),
            negativeMarks = obj.optDouble("negativeMarks", 0.33),
            shuffleQuestions = obj.optBoolean("shuffleQuestions", false),
            showQuestionPalette = obj.optBoolean("showQuestionPalette", true),
            allowMarkForReview = obj.optBoolean("allowMarkForReview", true),
            showExplanationsAfterSubmit = obj.optBoolean("showExplanationsAfterSubmit", true)
        )
    }

    private fun persistMap(context: Context, map: Map<String, PYQAppConfig>) {
        val rootObj = JSONObject()
        for ((appId, cfg) in map) {
            val obj = JSONObject().apply {
                put("enabled", cfg.enabled)
                put("availabilityStatus", cfg.availabilityStatus.name)
                put("scheduledDateTime", cfg.scheduledDateTime)
                put("disabledMessageType", cfg.disabledMessageType.name)
                put("disabledCustomMessage", cfg.disabledCustomMessage)
                put("defaultQuestionsCount", cfg.defaultQuestionsCount)
                put("defaultTimeLimitMinutes", cfg.defaultTimeLimitMinutes)
                put("marksPerCorrectAnswer", cfg.marksPerCorrectAnswer)
                put("negativeMarks", cfg.negativeMarks)
                put("shuffleQuestions", cfg.shuffleQuestions)
                put("showQuestionPalette", cfg.showQuestionPalette)
                put("allowMarkForReview", cfg.allowMarkForReview)
                put("showExplanationsAfterSubmit", cfg.showExplanationsAfterSubmit)

                val togglesObj = JSONObject().apply {
                    put("fullPaperPractice", cfg.featureToggles.fullPaperPractice)
                    put("practiceMode", cfg.featureToggles.practiceMode)
                    put("examMode", cfg.featureToggles.examMode)
                    put("answerExplanations", cfg.featureToggles.answerExplanations)
                    put("bookmarks", cfg.featureToggles.bookmarks)
                    put("wrongAnswersPractice", cfg.featureToggles.wrongAnswersPractice)
                    put("reattempts", cfg.featureToggles.reattempts)
                    put("analytics", cfg.featureToggles.analytics)
                }
                put("featureToggles", togglesObj)
            }
            rootObj.put(appId, obj)
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CONFIGS_JSON, rootObj.toString()).apply()
    }
}
