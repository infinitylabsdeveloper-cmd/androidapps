package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subject_progress")
data class SubjectProgressEntity(
    @PrimaryKey val subjectId: String,
    val progress: Float
)
