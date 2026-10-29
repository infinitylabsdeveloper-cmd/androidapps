package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_materials")
data class DownloadedMaterialEntity(
    @PrimaryKey val id: String, // Likely setId or subtopicId
    val appId: String = "appsc-group2",
    val title: String,
    val type: String, // "SET", "SUBTOPIC"
    val subjectId: String,
    val unitId: String,
    val topicId: String,
    val questionIds: List<String>,
    val downloadedAt: Long = System.currentTimeMillis()
)
