package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Dedicated Entity for question bookmarks with custom notes and timestamp.
 */
@Entity(
    tableName = "bookmarks",
    indices = [
        Index(value = ["appId"]),
        Index(value = ["subjectId"]),
        Index(value = ["unitId"]),
        Index(value = ["topicId"]),
        Index(value = ["createdAt"])
    ]
)
data class BookmarkEntity(
    @PrimaryKey
    val questionId: String,
    val appId: String = "appsc-group2",
    val subjectId: String,
    val unitId: String = "",
    val topicId: String = "",
    val note: String? = null,
    val folderName: String = "Default",
    val createdAt: Long = System.currentTimeMillis()
)
