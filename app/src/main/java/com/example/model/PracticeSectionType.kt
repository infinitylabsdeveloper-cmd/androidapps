package com.example.model

/**
 * Defines the 4 independent practice sections for customizable answer-feedback effects:
 * - Sets Practice
 * - Subtopics Practice
 * - Current Affairs Practice
 * - Offline Downloaded Practice
 */
enum class PracticeSectionType(
    val id: String,
    val displayName: String,
    val tabLabel: String,
    val prefKey: String
) {
    SETS(
        id = "sets",
        displayName = "Sets Practice",
        tabLabel = "Sets",
        prefKey = "effect_sets_practice"
    ),
    SUBTOPICS(
        id = "subtopics",
        displayName = "Subtopics Practice",
        tabLabel = "Subtopics",
        prefKey = "effect_subtopics_practice"
    ),
    CURRENT_AFFAIRS(
        id = "current_affairs",
        displayName = "Current Affairs Practice",
        tabLabel = "Current Affairs",
        prefKey = "effect_ca_practice"
    ),
    OFFLINE(
        id = "offline",
        displayName = "Offline Downloaded Practice",
        tabLabel = "Offline",
        prefKey = "effect_offline_practice"
    );

    companion object {
        fun fromId(id: String?): PracticeSectionType {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: SETS
        }
    }
}
