package com.example.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserProgressEntity::class,
        TestResultEntity::class,
        BookmarkEntity::class,
        UserStreakEntity::class,
        QuestionEntity::class,
        GeneratedMockTestEntity::class,
        UserEntitlementEntity::class,
        LegalDocumentEntity::class,
        SupportFaqEntity::class,
        OfflineContentPackEntity::class,
        MockTestConfigEntity::class,
        StudyMaterialEntity::class,
        CurrentAffairsEntity::class,
        ExamAppConfigEntity::class,
        UserRoleEntity::class,
        SubjectEntity::class,
        LeaderboardEntryEntity::class,
        XPTransactionEntity::class,
        UserAchievementEntity::class,
        LeaderboardHistoryEntity::class,
        LeaderboardConfigEntity::class,
        CulturalCarouselEntity::class,
        DownloadedMaterialEntity::class
    ],
    version = 16,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProgressDao(): UserProgressDao
    abstract fun testResultDao(): TestResultDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun userStreakDao(): UserStreakDao
    abstract fun questionDao(): QuestionDao
    abstract fun generatedMockTestDao(): GeneratedMockTestDao
    abstract fun userEntitlementDao(): UserEntitlementDao
    abstract fun legalSupportDao(): LegalSupportDao
    abstract fun offlineContentPackDao(): OfflineContentPackDao
    abstract fun mockTestConfigDao(): MockTestConfigDao
    abstract fun studyMaterialDao(): StudyMaterialDao
    abstract fun currentAffairsDao(): CurrentAffairsDao
    abstract fun examAppConfigDao(): ExamAppConfigDao
    abstract fun subjectDao(): SubjectDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun xpTransactionDao(): XPTransactionDao
    abstract fun userAchievementDao(): UserAchievementDao
    abstract fun leaderboardHistoryDao(): LeaderboardHistoryDao
    abstract fun leaderboardConfigDao(): LeaderboardConfigDao
    abstract fun culturalCarouselDao(): CulturalCarouselDao
    abstract fun downloadedMaterialDao(): DownloadedMaterialDao

    companion object {
        private const val DATABASE_NAME = "appsc_prep_room.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = getDatabase(context)

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

