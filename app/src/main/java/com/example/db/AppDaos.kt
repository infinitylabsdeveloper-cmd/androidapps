package com.example.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE appId = :appId ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(appId: String): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :questionId AND appId = :appId")
    suspend fun deleteBookmarkById(questionId: String, appId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :questionId AND appId = :appId)")
    suspend fun isBookmarked(questionId: String, appId: String): Boolean

    @Query("SELECT COUNT(*) FROM bookmarks WHERE appId = :appId")
    suspend fun getBookmarkCount(appId: String): Int
}

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE appId = :appId")
    fun getUserProgress(appId: String): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE appId = :appId")
    suspend fun getUserProgressDirect(appId: String): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: UserProgressEntity)

    @Query("UPDATE user_progress SET totalXp = totalXp + :xpDelta, completedQuestionsCount = completedQuestionsCount + :questionsDelta, dailyQuestionsAnswered = dailyQuestionsAnswered + :questionsDelta, updatedAt = :timestamp WHERE appId = :appId")
    suspend fun incrementProgress(appId: String, xpDelta: Int, questionsDelta: Int, timestamp: Long)

    @Query("UPDATE user_progress SET streakDays = :streakDays, lastActiveDate = :activeDate, updatedAt = :timestamp WHERE appId = :appId")
    suspend fun updateStreak(appId: String, streakDays: Int, activeDate: String, timestamp: Long)

    @Query("UPDATE user_progress SET dailyQuestionsAnswered = :count, updatedAt = :timestamp WHERE appId = :appId")
    suspend fun updateDailyQuestions(appId: String, count: Int, timestamp: Long)

    @Query("UPDATE user_progress SET dailyTargetQuestions = :target, updatedAt = :timestamp WHERE appId = :appId")
    suspend fun updateDailyTarget(appId: String, target: Int, timestamp: Long)

    @Query("SELECT * FROM user_progress")
    fun getAllProgress(): Flow<List<UserProgressEntity>>
}

@Dao
interface TestResultDao {
    @Query("SELECT * FROM test_results WHERE appId = :appId ORDER BY timestamp DESC")
    fun getAllResults(appId: String): Flow<List<TestResultEntity>>

    @Query("SELECT * FROM test_results WHERE appId = :appId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentResults(appId: String, limit: Int): Flow<List<TestResultEntity>>

    @Query("SELECT * FROM test_results WHERE appId = :appId ORDER BY timestamp DESC")
    suspend fun getAllResultsDirect(appId: String): List<TestResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: TestResultEntity): Long

    @Query("SELECT COUNT(*) FROM test_results WHERE appId = :appId")
    fun getTestCount(appId: String): Flow<Int>

    @Query("SELECT AVG(scorePercentage) FROM test_results WHERE appId = :appId")
    fun getAverageScorePercentage(appId: String): Flow<Float?>

    @Query("SELECT MAX(scorePercentage) FROM test_results WHERE appId = :appId")
    fun getMaxScorePercentage(appId: String): Flow<Float?>

    @Query("SELECT SUM(totalQuestions) FROM test_results WHERE appId = :appId")
    fun getTotalQuestionsAttempted(appId: String): Flow<Int?>

    @Query("SELECT SUM(correctAnswers) FROM test_results WHERE appId = :appId")
    fun getTotalCorrectAnswers(appId: String): Flow<Int?>

    @Query("SELECT * FROM test_results WHERE id = :id")
    suspend fun getResultById(id: Long): TestResultEntity?

    @Query("DELETE FROM test_results WHERE id = :id")
    suspend fun deleteResultById(id: Long)

    @Query("DELETE FROM test_results WHERE appId = :appId")
    suspend fun clearHistory(appId: String)
}

@Dao
interface UserStreakDao {
    @Query("SELECT * FROM user_streak WHERE id = 1")
    fun getStreak(): Flow<UserStreakEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStreak(streak: UserStreakEntity)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM question_bank WHERE appId = :appId")
    fun getAllQuestions(appId: String): Flow<List<QuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Query("DELETE FROM question_bank WHERE appId = :appId")
    suspend fun clearQuestions(appId: String)
}

@Dao
interface GeneratedMockTestDao {
    @Query("SELECT * FROM generated_mock_tests WHERE appId = :appId ORDER BY createdAt DESC")
    fun getAllMockTests(appId: String): Flow<List<GeneratedMockTestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTest(mockTest: GeneratedMockTestEntity)

    @Query("DELETE FROM generated_mock_tests WHERE id = :testId")
    suspend fun deleteMockTestById(testId: String)
}

@Dao
interface UserEntitlementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntitlement(entitlement: UserEntitlementEntity)

    @Query("SELECT * FROM user_entitlements WHERE appId = :appId AND status = 'ACTIVE' AND expiryTimestamp > :currentTime ORDER BY expiryTimestamp DESC LIMIT 1")
    fun getActiveEntitlementFlow(appId: String, currentTime: Long = System.currentTimeMillis()): Flow<UserEntitlementEntity?>

    @Query("SELECT * FROM user_entitlements WHERE appId = :appId AND status = 'ACTIVE' AND expiryTimestamp > :currentTime ORDER BY expiryTimestamp DESC LIMIT 1")
    suspend fun getActiveEntitlementDirect(appId: String, currentTime: Long = System.currentTimeMillis()): UserEntitlementEntity?

    @Query("SELECT * FROM user_entitlements ORDER BY purchaseTimestamp DESC")
    fun getAllEntitlementsFlow(): Flow<List<UserEntitlementEntity>>

    @Query("SELECT * FROM user_entitlements WHERE transactionId = :txId LIMIT 1")
    suspend fun getEntitlementByTransactionId(txId: String): UserEntitlementEntity?

    @Query("SELECT * FROM user_entitlements WHERE providerPaymentId = :paymentId LIMIT 1")
    suspend fun getEntitlementByPaymentId(paymentId: String): UserEntitlementEntity?

    @Query("SELECT * FROM user_entitlements WHERE providerOrderId = :orderId LIMIT 1")
    suspend fun getEntitlementByOrderId(orderId: String): UserEntitlementEntity?

    @Query("SELECT * FROM user_entitlements ORDER BY purchaseTimestamp DESC")
    suspend fun getAllEntitlementsDirect(): List<UserEntitlementEntity>

    @Query("UPDATE user_entitlements SET status = :newStatus, updatedAt = :updatedAt WHERE transactionId = :transactionId")
    suspend fun updateStatus(transactionId: String, newStatus: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM user_entitlements WHERE appId = :appId ORDER BY purchaseTimestamp DESC")
    suspend fun getEntitlementsForAppDirect(appId: String): List<UserEntitlementEntity>
}

@Dao
interface LegalSupportDao {
    @Query("SELECT * FROM legal_documents")
    fun getAllDocumentsFlow(): Flow<List<LegalDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocument(doc: LegalDocumentEntity)

    @Query("DELETE FROM legal_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)

    @Query("SELECT * FROM support_faqs ORDER BY displayOrder ASC")
    fun getAllFaqsFlow(): Flow<List<SupportFaqEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFaq(faq: SupportFaqEntity)

    @Query("DELETE FROM support_faqs WHERE id = :id")
    suspend fun deleteFaqById(id: String)
}

@Dao
interface OfflineContentPackDao {
    @Query("SELECT * FROM offline_content_packs WHERE appId = :appId ORDER BY downloadedAt DESC")
    fun getAllPacksFlow(appId: String): Flow<List<OfflineContentPackEntity>>

    @Query("SELECT * FROM offline_content_packs WHERE appId = :appId ORDER BY downloadedAt DESC")
    suspend fun getAllPacksDirect(appId: String): List<OfflineContentPackEntity>

    @Query("SELECT * FROM offline_content_packs WHERE id = :id")
    suspend fun getPackById(id: String): OfflineContentPackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPack(pack: OfflineContentPackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPacks(packs: List<OfflineContentPackEntity>)

    @Query("DELETE FROM offline_content_packs WHERE id = :id")
    suspend fun deletePackById(id: String)

    @Query("DELETE FROM offline_content_packs WHERE subjectId = :subjectId")
    suspend fun deletePacksForSubject(subjectId: String)

    @Query("DELETE FROM offline_content_packs WHERE appId = :appId")
    suspend fun clearPacksForApp(appId: String)

    @Query("UPDATE offline_content_packs SET syncStatus = :syncStatus, lastSyncedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String, timestamp: Long)

    @Query("UPDATE offline_content_packs SET syncStatus = 'SYNCED', lastSyncedTimestamp = :timestamp WHERE appId = :appId")
    suspend fun markAllSynced(appId: String, timestamp: Long)
}

@Dao
interface MockTestConfigDao {
    @Query("SELECT * FROM mock_test_configs WHERE appId = :appId")
    suspend fun getConfigForApp(appId: String): MockTestConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: MockTestConfigEntity)

    @Query("DELETE FROM mock_test_configs WHERE appId = :appId")
    suspend fun deleteConfigForApp(appId: String)
}

@Dao
interface StudyMaterialDao {
    @Query("SELECT * FROM study_materials WHERE appId = :appId")
    fun getAllMaterials(appId: String): Flow<List<StudyMaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<StudyMaterialEntity>)

    @Query("DELETE FROM study_materials WHERE appId = :appId")
    suspend fun clearMaterials(appId: String)
}

@Dao
interface CurrentAffairsDao {
    @Query("SELECT * FROM current_affairs WHERE appId = :appId OR isSharedMultiApp = 1 ORDER BY lastUpdated DESC")
    fun getAllCurrentAffairs(appId: String): Flow<List<CurrentAffairsEntity>>

    @Query("SELECT * FROM current_affairs ORDER BY lastUpdated DESC")
    fun getAll(): Flow<List<CurrentAffairsEntity>>

    @Query("SELECT * FROM current_affairs WHERE id = :id")
    suspend fun getCurrentAffairById(id: String): CurrentAffairsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentAffairs(items: List<CurrentAffairsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentAffair(item: CurrentAffairsEntity)

    @Query("DELETE FROM current_affairs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM current_affairs WHERE appId = :appId")
    suspend fun clearCurrentAffairs(appId: String)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE appId = :appId ORDER BY name ASC")
    fun getSubjectsForApp(appId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: String)

    @Query("DELETE FROM subjects WHERE appId = :appId")
    suspend fun clearSubjectsForApp(appId: String)
}

@Dao
interface ExamAppConfigDao {
    @Query("SELECT * FROM exam_app_config")
    fun getAllConfigs(): Flow<List<ExamAppConfigEntity>>

    @Query("SELECT * FROM exam_app_config WHERE id = :id")
    suspend fun getConfigById(id: String): ExamAppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigs(configs: List<ExamAppConfigEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: ExamAppConfigEntity)

    @Query("DELETE FROM exam_app_config")
    suspend fun clearAllConfigs()
}

@Dao
interface CulturalCarouselDao {
    @Query("SELECT * FROM cultural_carousel_items ORDER BY orderIndex ASC")
    fun getAllItems(): Flow<List<CulturalCarouselEntity>>

    @Query("SELECT * FROM cultural_carousel_items ORDER BY orderIndex ASC")
    suspend fun getAllItemsDirect(): List<CulturalCarouselEntity>

    @Query("SELECT * FROM cultural_carousel_items WHERE isEnabled = 1 ORDER BY orderIndex ASC")
    fun getEnabledItems(): Flow<List<CulturalCarouselEntity>>

    @Query("SELECT * FROM cultural_carousel_items WHERE isEnabled = 1 ORDER BY orderIndex ASC")
    suspend fun getEnabledItemsDirect(): List<CulturalCarouselEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CulturalCarouselEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<CulturalCarouselEntity>)

    @Update
    suspend fun updateItem(item: CulturalCarouselEntity)

    @Delete
    suspend fun deleteItem(item: CulturalCarouselEntity)

    @Query("DELETE FROM cultural_carousel_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM cultural_carousel_items")
    suspend fun clearAll()
}

@Dao
interface DownloadedMaterialDao {
    @Query("SELECT * FROM downloaded_materials WHERE appId = :appId")
    fun getAllDownloadedMaterials(appId: String): Flow<List<DownloadedMaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: DownloadedMaterialEntity)

    @Query("DELETE FROM downloaded_materials WHERE id = :id")
    suspend fun deleteMaterialById(id: String)
}
