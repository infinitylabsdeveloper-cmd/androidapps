package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.SubjectProgressEntity
import com.example.data.local.entity.TopicProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for high-level analytics, subject progress, and topic mastery.
 */
@Dao
interface ProgressAnalyticsDao {

    @Query("SELECT * FROM subject_progress WHERE appId = :appId")
    fun getAllSubjectProgressFlow(appId: String = "appsc-group2"): Flow<List<SubjectProgressEntity>>

    @Query("SELECT * FROM subject_progress WHERE subjectId = :subjectId LIMIT 1")
    fun getProgressForSubjectFlow(subjectId: String): Flow<SubjectProgressEntity?>

    @Query("SELECT * FROM topic_progress WHERE appId = :appId AND subjectId = :subjectId")
    fun getTopicProgressBySubjectFlow(appId: String, subjectId: String): Flow<List<TopicProgressEntity>>

    @Query("SELECT * FROM topic_progress WHERE topicId = :topicId LIMIT 1")
    fun getProgressForTopicFlow(topicId: String): Flow<TopicProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubjectProgress(progress: SubjectProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTopicProgress(progress: TopicProgressEntity)

    @Query("DELETE FROM subject_progress WHERE appId = :appId")
    suspend fun clearSubjectProgress(appId: String)

    @Query("DELETE FROM topic_progress WHERE appId = :appId")
    suspend fun clearTopicProgress(appId: String)
}
